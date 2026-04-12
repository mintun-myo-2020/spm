package com.eggtive.spm.testpaper.service;

import com.eggtive.spm.classmanagement.entity.TuitionClass;
import com.eggtive.spm.classmanagement.service.ClassService;
import com.eggtive.spm.common.enums.ErrorCode;
import com.eggtive.spm.common.exception.AppException;
import com.eggtive.spm.testpaper.dto.TestPaperPageDTO;
import com.eggtive.spm.testpaper.dto.TestPaperUploadDTO;
import com.eggtive.spm.testpaper.entity.TestPaperPage;
import com.eggtive.spm.testpaper.entity.TestPaperUpload;
import com.eggtive.spm.testpaper.enums.PageStatus;
import com.eggtive.spm.testpaper.enums.UploadStatus;
import com.eggtive.spm.testpaper.llm.TestPaperExtractionService;
import com.eggtive.spm.testpaper.parser.ParsedQuestion;
import com.eggtive.spm.testpaper.parser.ParsedResult;
import com.eggtive.spm.testpaper.repository.TestPaperPageRepository;
import com.eggtive.spm.testpaper.repository.TestPaperUploadRepository;
import com.eggtive.spm.testpaper.storage.FileStorageService;
import com.eggtive.spm.subject.entity.Topic;
import com.eggtive.spm.subject.repository.TopicRepository;
import com.eggtive.spm.user.entity.Student;
import com.eggtive.spm.user.entity.User;
import com.eggtive.spm.user.service.UserService;
import tools.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@Transactional
public class TestPaperService {

    private static final Logger log = LoggerFactory.getLogger(TestPaperService.class);
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    private final TestPaperUploadRepository uploadRepository;
    private final TestPaperPageRepository pageRepository;
    private final FileStorageService fileStorageService;
    private final TestPaperExtractionService extractionService;
    private final UserService userService;
    private final ClassService classService;
    private final JsonMapper jsonMapper;
    private final TopicRepository topicRepository;

    public TestPaperService(TestPaperUploadRepository uploadRepository,
                            TestPaperPageRepository pageRepository,
                            FileStorageService fileStorageService,
                            TestPaperExtractionService extractionService,
                            UserService userService,
                            ClassService classService,
                            JsonMapper jsonMapper,
                            TopicRepository topicRepository) {
        this.uploadRepository = uploadRepository;
        this.pageRepository = pageRepository;
        this.fileStorageService = fileStorageService;
        this.extractionService = extractionService;
        this.userService = userService;
        this.classService = classService;
        this.jsonMapper = jsonMapper;
        this.topicRepository = topicRepository;
    }

    /**
     * Upload multiple files (pages of the same test paper).
     * Validates files, stores them, creates Upload + Page records.
     */
    public TestPaperUploadDTO uploadFiles(List<MultipartFile> files, UUID studentId, UUID classId, User currentUser) {
        if (files == null || files.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "At least one file is required");
        }

        Student student = userService.findStudentOrThrow(studentId);
        TuitionClass tuitionClass = classService.findClassOrThrow(classId);

        TestPaperUpload upload = new TestPaperUpload();
        upload.setStudent(student);
        upload.setTuitionClass(tuitionClass);
        upload.setUploadedBy(currentUser);
        upload.setStatus(UploadStatus.UPLOADED);
        upload = uploadRepository.save(upload);

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            log.info("Storing file [{}]: name='{}', contentType='{}', size={}",
                    i, file.getOriginalFilename(), file.getContentType(), file.getSize());
            validateFile(file);

            // Sanitize filename: strip path components, replace unsafe chars
            String safeName = sanitizeFilename(file.getOriginalFilename());
            String key = upload.getId() + "/page-" + (i + 1) + "/" + safeName;
            try {
                fileStorageService.upload(key, file.getBytes(), file.getContentType());
            } catch (IOException e) {
                throw new AppException(ErrorCode.STORAGE_ERROR, "Failed to store file: " + file.getOriginalFilename());
            }

            TestPaperPage page = new TestPaperPage();
            page.setUpload(upload);
            page.setPageNumber(i + 1);
            page.setStorageLocation(fileStorageService.storageType().value());
            page.setStorageKey(key);
            page.setFileName(file.getOriginalFilename());
            page.setContentType(file.getContentType());
            page.setFileSizeBytes(file.getSize());
            page.setStatus(PageStatus.PENDING);
            upload.getPages().add(page);
        }

        upload = uploadRepository.save(upload);
        return toDTO(upload);
    }

    /**
     * Trigger async OCR extraction + parsing for all pages of an upload.
     */
    public void triggerExtraction(UUID uploadId) {
        TestPaperUpload upload = findUploadOrThrow(uploadId);
        if (upload.getStatus() == UploadStatus.PROCESSING) {
            throw new AppException(ErrorCode.UPLOAD_ALREADY_PROCESSING, "Upload is already being processed");
        }
        if (upload.getStatus() == UploadStatus.COMPLETED) {
            throw new AppException(ErrorCode.UPLOAD_ALREADY_PROCESSED, "Upload has already been processed");
        }
        upload.setStatus(UploadStatus.PROCESSING);
        uploadRepository.save(upload);
        processExtractionAsync(uploadId);
    }

    @Async
    public void processExtractionAsync(UUID uploadId) {
        try {
            TestPaperUpload upload = findUploadOrThrow(uploadId);

            // Load topic names for the subject so the LLM can classify questions
            UUID subjectId = upload.getTuitionClass().getSubject().getId();
            List<String> topicNames = topicRepository.findBySubjectIdAndIsActiveTrue(subjectId)
                    .stream().map(Topic::getName).toList();

            boolean allSuccess = true;

            for (TestPaperPage page : upload.getPages()) {
                try {
                    page.setStatus(PageStatus.EXTRACTING);
                    pageRepository.save(page);

                    byte[] imageBytes = fileStorageService.readFileBytes(
                            page.getStorageKey());

                    ParsedResult parsed = extractionService.extractQuestions(
                            imageBytes, page.getContentType(), page.getFileName(), topicNames);

                    page.setParsedResult(jsonMapper.writeValueAsString(parsed));
                    page.setOcrConfidence(averageConfidence(parsed));
                    page.setStatus(PageStatus.COMPLETED);
                } catch (Exception e) {
                    log.error("Failed to process page {} of upload {}", page.getPageNumber(), uploadId, e);
                    page.setStatus(PageStatus.FAILED);
                    allSuccess = false;
                }
                pageRepository.save(page);
            }

            upload.setStatus(allSuccess ? UploadStatus.COMPLETED : UploadStatus.PARTIALLY_FAILED);
            uploadRepository.save(upload);
            log.info("Extraction complete for upload {} — status: {}", uploadId, upload.getStatus());

        } catch (Exception e) {
            log.error("Extraction failed for upload {}", uploadId, e);
            try {
                TestPaperUpload upload = findUploadOrThrow(uploadId);
                upload.setStatus(UploadStatus.FAILED);
                uploadRepository.save(upload);
            } catch (Exception inner) {
                log.error("Failed to update upload status after error", inner);
            }
        }
    }

    /**
     * Get upload with pages, presigned URLs, and aggregated parsed results.
     */
    @Transactional(readOnly = true)
    public TestPaperUploadDTO getUpload(UUID uploadId) {
        return toDTO(findUploadOrThrow(uploadId));
    }

    /**
     * Link upload(s) to a test score.
     */
    public void linkToTestScore(List<UUID> uploadIds, UUID testScoreId) {
        if (uploadIds == null || uploadIds.isEmpty()) return;
        uploadRepository.linkToTestScore(uploadIds, testScoreId);
    }

    public TestPaperUpload findUploadOrThrow(UUID uploadId) {
        return uploadRepository.findById(uploadId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Upload not found"));
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_FILE_CONTENT,
                "File is empty: name='" + file.getOriginalFilename()
                + "', contentType='" + file.getContentType()
                + "', size=" + file.getSize());
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE,
                "Only JPEG and PNG images are supported. Got: " + file.getContentType());
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE,
                "File exceeds maximum size of 10MB: " + file.getOriginalFilename());
        }
    }

    /**
     * Strip path separators and unsafe characters from the original filename
     * to prevent path traversal via crafted filenames.
     */
    private String sanitizeFilename(String original) {
        if (original == null || original.isBlank()) {
            return "unnamed";
        }
        // Take only the filename part (after last / or \)
        String name = original;
        int lastSlash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (lastSlash >= 0) {
            name = name.substring(lastSlash + 1);
        }
        // Replace anything that isn't alphanumeric, dot, hyphen, or underscore
        name = name.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
        return name.isBlank() ? "unnamed" : name;
    }

    private TestPaperUploadDTO toDTO(TestPaperUpload upload) {
        List<TestPaperPageDTO> pageDTOs = upload.getPages().stream()
            .map(this::toPageDTO)
            .toList();

        List<TestPaperUploadDTO.AggregatedQuestion> aggregated = aggregateQuestions(upload.getPages());

        return new TestPaperUploadDTO(
            upload.getId(),
            upload.getTestScore() != null ? upload.getTestScore().getId() : null,
            upload.getStudent().getId(),
            upload.getTuitionClass().getId(),
            upload.getStatus().name(),
            pageDTOs,
            aggregated,
            upload.getCreatedAt()
        );
    }

    private TestPaperPageDTO toPageDTO(TestPaperPage page) {
        String fileUrl = fileStorageService.generatePresignedUrl(page.getStorageKey(), 60);
        ParsedResult parsed = deserializeParsedResult(page.getParsedResult());
        return new TestPaperPageDTO(
            page.getId(), page.getPageNumber(), page.getFileName(),
            page.getContentType(), page.getFileSizeBytes(),
            page.getStatus().name(), fileUrl,
            page.getExtractedText(), page.getOcrConfidence(),
            parsed, page.getCreatedAt()
        );
    }

    private List<TestPaperUploadDTO.AggregatedQuestion> aggregateQuestions(List<TestPaperPage> pages) {
        List<TestPaperUploadDTO.AggregatedQuestion> result = new ArrayList<>();
        for (TestPaperPage page : pages) {
            ParsedResult parsed = deserializeParsedResult(page.getParsedResult());
            if (parsed == null || parsed.questions() == null) continue;
            for (ParsedQuestion pq : parsed.questions()) {
                var subs = pq.subQuestions() == null ? List.<TestPaperUploadDTO.AggregatedSubQuestion>of() :
                    pq.subQuestions().stream()
                        .map(sq -> new TestPaperUploadDTO.AggregatedSubQuestion(
                            sq.label(), sq.questionText(), sq.maxScore(), sq.studentAnswer(), sq.confidence()))
                        .toList();
                var opts = pq.mcqOptions() == null ? List.<TestPaperUploadDTO.AggregatedMcqOption>of() :
                    pq.mcqOptions().stream()
                        .map(o -> new TestPaperUploadDTO.AggregatedMcqOption(o.key(), o.text()))
                        .toList();
                result.add(new TestPaperUploadDTO.AggregatedQuestion(
                    pq.questionNumber(), pq.questionText(), pq.questionType(),
                    pq.maxScore(), pq.studentAnswer(), pq.topicHint(),
                    subs, opts, pq.confidence(), page.getPageNumber()));
            }
        }
        return result;
    }

    private ParsedResult deserializeParsedResult(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return jsonMapper.readValue(json, ParsedResult.class);
        } catch (Exception e) {
            log.warn("Failed to deserialize parsed result", e);
            return null;
        }
    }

    /** Average confidence across all questions returned by the LLM. */
    private float averageConfidence(ParsedResult parsed) {
        if (parsed == null || parsed.questions() == null || parsed.questions().isEmpty()) {
            return 0f;
        }
        return (float) parsed.questions().stream()
                .mapToDouble(ParsedQuestion::confidence)
                .average()
                .orElse(0);
    }
}
