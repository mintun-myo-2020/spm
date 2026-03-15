package com.eggtive.spm.testpaper.controller;

import com.eggtive.spm.auth.CurrentUserService;
import com.eggtive.spm.common.dto.ApiResponse;
import com.eggtive.spm.testpaper.dto.TestPaperUploadDTO;
import com.eggtive.spm.testpaper.service.TestPaperService;
import com.eggtive.spm.testpaper.storage.LocalFileStorageService;
import com.eggtive.spm.user.entity.User;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/test-papers")
public class TestPaperController {

    private final TestPaperService testPaperService;
    private final CurrentUserService currentUserService;

    public TestPaperController(TestPaperService testPaperService,
                               CurrentUserService currentUserService) {
        this.testPaperService = testPaperService;
        this.currentUserService = currentUserService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TestPaperUploadDTO> upload(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam UUID studentId,
            @RequestParam UUID classId) {
        User user = currentUserService.getCurrentUser();
        return ApiResponse.ok(testPaperService.uploadFiles(files, studentId, classId, user));
    }

    @PostMapping("/{uploadId}/extract")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT', 'ADMIN')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<Void> triggerExtraction(@PathVariable UUID uploadId) {
        testPaperService.triggerExtraction(uploadId);
        return ApiResponse.ok(null, "Extraction started");
    }

    @GetMapping("/{uploadId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT', 'ADMIN')")
    public ApiResponse<TestPaperUploadDTO> getUpload(@PathVariable UUID uploadId) {
        return ApiResponse.ok(testPaperService.getUpload(uploadId));
    }

    /**
     * Serve local files in dev mode. In production, presigned S3 URLs are used instead.
     */
    @GetMapping("/files")
    @ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
    public ResponseEntity<byte[]> serveLocalFile(@RequestParam String key) {
        // This endpoint only exists when LocalFileStorageService is active
        var storage = getLocalStorage();
        if (storage == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] content = storage.readFile(key);
        String contentType = storage.probeContentType(key);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .body(content);
    }

    private LocalFileStorageService localFileStorageService;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setLocalFileStorageService(LocalFileStorageService localFileStorageService) {
        this.localFileStorageService = localFileStorageService;
    }

    private LocalFileStorageService getLocalStorage() {
        return localFileStorageService;
    }
}
