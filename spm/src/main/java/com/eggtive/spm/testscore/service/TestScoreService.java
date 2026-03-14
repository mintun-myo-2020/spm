package com.eggtive.spm.testscore.service;

import com.eggtive.spm.classmanagement.service.ClassService;
import com.eggtive.spm.common.dto.PagedResponse;
import com.eggtive.spm.common.enums.ErrorCode;
import com.eggtive.spm.common.exception.AppException;
import com.eggtive.spm.subject.entity.Topic;
import com.eggtive.spm.subject.service.SubjectService;
import com.eggtive.spm.testscore.dto.CreateTestScoreRequestDTO;

public TestScoreDTO updateTestScore(UUID testScoreId, CreateTestScoreRequestDTO req, User currentUser) {
    TestScore ts = testScoreRepository.findById(testScoreId)
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Test score not found"));

    if (req.overallScore().compareTo(req.maxScore() != null ? req.maxScore() : new BigDecimal("100.00")) > 0) {
        throw new AppException(ErrorCode.INVALID_SCORE, "Overall score cannot exceed max score");
    }

    ts.setTestName(req.testName());
    ts.setTestDate(req.testDate());
    ts.setOverallScore(req.overallScore());
    ts.setMaxScore(req.maxScore() != null ? req.maxScore() : new BigDecimal("100.00"));
    ts.setUpdatedBy(currentUser);

    // Clear existing questions and rebuild
    ts.getQuestions().clear();

    if (req.questions() != null) {
        for (var qReq : req.questions()) {
            Question q = new Question();
            q.setTestScore(ts);
            q.setQuestionNumber(qReq.questionNumber());
            q.setMaxScore(qReq.maxScore());
            ts.getQuestions().add(q);

            if (qReq.subQuestions() != null) {
                for (var sqReq : qReq.subQuestions()) {
                    Topic topic = subjectService.findTopicOrThrow(sqReq.topicId());
                    SubQuestion sq = new SubQuestion();
                    sq.setQuestion(q);
                    sq.setSubQuestionLabel(sqReq.label());
                    sq.setScore(sqReq.score());
                    sq.setMaxScore(sqReq.maxScore());
                    sq.setTopic(topic);
                    q.getSubQuestions().add(sq);
                }
            }
        }
    }

    ts = testScoreRepository.save(ts);
    return toDTO(ts);
}

import com.eggtive.spm.testscore.dto.TestScoreDTO;
import com.eggtive.spm.testscore.dto.TestScoreDetailDTO;
import com.eggtive.spm.testscore.entity.Question;
import com.eggtive.spm.testscore.entity.SubQuestion;
import com.eggtive.spm.testscore.entity.TestScore;
import com.eggtive.spm.testscore.repository.TestScoreRepository;
import com.eggtive.spm.user.entity.Student;
import com.eggtive.spm.user.entity.Teacher;
import com.eggtive.spm.user.entity.User;
import com.eggtive.spm.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class TestScoreService {

    private final TestScoreRepository testScoreRepository;
    private final UserService userService;
    private final ClassService classService;
    private final SubjectService subjectService;
    private final com.eggtive.spm.feedback.repository.FeedbackRepository feedbackRepository;

    public TestScoreService(TestScoreRepository tsRepo, UserService userService,
                            ClassService classService, SubjectService subjectService,
                            com.eggtive.spm.feedback.repository.FeedbackRepository feedbackRepository) {
        this.testScoreRepository = tsRepo;
        this.userService = userService;
        this.classService = classService;
        this.subjectService = subjectService;
        this.feedbackRepository = feedbackRepository;
    }

    public TestScoreDTO createTestScore(CreateTestScoreRequestDTO req, User currentUser, Teacher teacher) {
        Student student = userService.findStudentOrThrow(req.studentId());
        var tuitionClass = classService.findClassOrThrow(req.classId());

        if (req.overallScore().compareTo(req.maxScore() != null ? req.maxScore() : new BigDecimal("100.00")) > 0) {
            throw new AppException(ErrorCode.INVALID_SCORE, "Overall score cannot exceed max score");
        }

        TestScore ts = new TestScore();
        ts.setStudent(student);
        ts.setTuitionClass(tuitionClass);
        ts.setTeacher(teacher);
        ts.setTestName(req.testName());
        ts.setTestDate(req.testDate());
        ts.setOverallScore(req.overallScore());
        ts.setMaxScore(req.maxScore() != null ? req.maxScore() : new BigDecimal("100.00"));
        ts.setCreatedBy(currentUser);
        ts.setUpdatedBy(currentUser);

        if (req.questions() != null) {
            for (var qReq : req.questions()) {
                Question q = new Question();
                q.setTestScore(ts);
                q.setQuestionNumber(qReq.questionNumber());
                q.setMaxScore(qReq.maxScore());
                ts.getQuestions().add(q);

                if (qReq.subQuestions() != null) {
                    for (var sqReq : qReq.subQuestions()) {
                        Topic topic = subjectService.findTopicOrThrow(sqReq.topicId());
                        SubQuestion sq = new SubQuestion();
                        sq.setQuestion(q);
                        sq.setSubQuestionLabel(sqReq.label());
                        sq.setScore(sqReq.score());
                        sq.setMaxScore(sqReq.maxScore());
                        sq.setTopic(topic);
                        q.getSubQuestions().add(sq);
                    }
                }
            }
        }

        ts = testScoreRepository.save(ts);
        return toDTO(ts);
    }

    @Transactional(readOnly = true)
    public PagedResponse<TestScoreDTO> getStudentTestScores(UUID studentId, LocalDate startDate,
                                                             LocalDate endDate, UUID classId, Pageable pageable) {
        Page<TestScore> page = testScoreRepository.findByStudentWithFilters(studentId, startDate, endDate, classId, pageable);
        return PagedResponse.from(page, page.getContent().stream().map(this::toDTO).toList());
    }

    @Transactional(readOnly = true)
    public TestScoreDTO getTestScore(UUID testScoreId) {
        TestScore ts = testScoreRepository.findById(testScoreId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Test score not found"));
        return toDTO(ts);
    }

    @Transactional(readOnly = true)
    public TestScoreDetailDTO getTestScoreDetail(UUID testScoreId) {
        TestScore ts = testScoreRepository.findById(testScoreId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Test score not found"));
        TestScoreDTO dto = toDTO(ts);
        com.eggtive.spm.feedback.dto.FeedbackDTO feedbackDTO = feedbackRepository.findByTestScoreId(testScoreId)
            .map(f -> {
                String teacherName = f.getTeacher().getUser().getFirstName() + " " + f.getTeacher().getUser().getLastName();
                return new com.eggtive.spm.feedback.dto.FeedbackDTO(f.getId(), f.getTestScore().getId(),
                    f.getTeacher().getId(), teacherName, f.getStudent().getId(),
                    f.getStrengths(), f.getAreasForImprovement(), f.getRecommendations(),
                    f.getAdditionalNotes(), f.isEdited(), f.getCreatedAt(), f.getUpdatedAt());
            }).orElse(null);
        return TestScoreDetailDTO.from(dto, feedbackDTO);
    }

    public void deleteTestScore(UUID testScoreId) {
        if (!testScoreRepository.existsById(testScoreId)) {
            throw new AppException(ErrorCode.NOT_FOUND, "Test score not found");
        }
        testScoreRepository.deleteById(testScoreId);
    }

    // --- module-boundary lookups (used by other modules' services) ---

    @Transactional(readOnly = true)
    public TestScore findTestScoreOrThrow(UUID testScoreId) {
        return testScoreRepository.findById(testScoreId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Test score not found"));
    }

    @Transactional(readOnly = true)
    public List<TestScore> findByStudentOrderByDateAsc(UUID studentId) {
        return testScoreRepository.findByStudentIdOrderByTestDateAsc(studentId);
    }

    private TestScoreDTO toDTO(TestScore ts) {
        var questions = ts.getQuestions().stream().map(q -> {
            var subs = q.getSubQuestions().stream().map(sq ->
                new TestScoreDTO.SubQuestionDTO(sq.getId(), sq.getSubQuestionLabel(), sq.getScore(),
                    sq.getMaxScore(), sq.getTopic().getId(), sq.getTopic().getName())
            ).toList();
            return new TestScoreDTO.QuestionDTO(q.getId(), q.getQuestionNumber(), q.getMaxScore(), subs);
        }).toList();

        Student s = ts.getStudent();
        String studentName = s.getUser().getFirstName() + " " + s.getUser().getLastName();
        Teacher t = ts.getTeacher();
        String teacherName = t.getUser().getFirstName() + " " + t.getUser().getLastName();

        return new TestScoreDTO(ts.getId(), s.getId(), studentName,
            ts.getTuitionClass().getId(), ts.getTuitionClass().getName(),
            t.getId(), teacherName, ts.getTestName(), ts.getTestDate(),
            ts.getOverallScore(), ts.getMaxScore(), questions,
            ts.getCreatedAt(), ts.getUpdatedAt());
    }
}

