package com.eggtive.spm.testscore.service;

import com.eggtive.spm.classmanagement.entity.TuitionClass;
import com.eggtive.spm.classmanagement.service.ClassService;
import com.eggtive.spm.common.enums.ErrorCode;
import com.eggtive.spm.common.enums.TestSource;
import com.eggtive.spm.common.exception.AppException;
import com.eggtive.spm.feedback.repository.FeedbackRepository;
import com.eggtive.spm.subject.entity.Topic;
import com.eggtive.spm.subject.service.SubjectService;
import com.eggtive.spm.testscore.dto.CreateTestScoreRequestDTO;
import com.eggtive.spm.testscore.dto.TestScoreDTO;
import com.eggtive.spm.testscore.entity.TestScore;
import com.eggtive.spm.testscore.repository.TestScoreRepository;
import com.eggtive.spm.testpaper.service.TestPaperService;
import com.eggtive.spm.user.entity.Student;
import com.eggtive.spm.user.entity.Teacher;
import com.eggtive.spm.user.entity.User;
import com.eggtive.spm.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestScoreServiceTest {

    @Mock private TestScoreRepository testScoreRepository;
    @Mock private UserService userService;
    @Mock private ClassService classService;
    @Mock private SubjectService subjectService;
    @Mock private FeedbackRepository feedbackRepository;
    @Mock private TestPaperService testPaperService;

    @InjectMocks private TestScoreService testScoreService;

    private User currentUser;
    private Teacher teacher;
    private Student student;
    private TuitionClass tuitionClass;
    private UUID studentId;
    private UUID classId;

    @BeforeEach
    void setUp() {
        studentId = UUID.randomUUID();
        classId = UUID.randomUUID();

        currentUser = new User();
        currentUser.setId(UUID.randomUUID());
        currentUser.setFirstName("Teacher");
        currentUser.setLastName("One");

        User teacherUser = new User();
        teacherUser.setFirstName("Teacher");
        teacherUser.setLastName("One");
        teacher = new Teacher();
        teacher.setId(UUID.randomUUID());
        teacher.setUser(teacherUser);

        User studentUser = new User();
        studentUser.setFirstName("Student");
        studentUser.setLastName("One");
        student = new Student();
        student.setId(studentId);
        student.setUser(studentUser);

        tuitionClass = new TuitionClass();
        tuitionClass.setId(classId);
        tuitionClass.setName("Math 101");
    }

    private CreateTestScoreRequestDTO buildRequest(BigDecimal score, BigDecimal maxScore) {
        return new CreateTestScoreRequestDTO(studentId, classId, "Mid-term", LocalDate.of(2026, 3, 15),
            score, maxScore, null, null, null, null);
    }

    // --- createTestScore tests ---

    @Test
    void createTestScore_validScore_savesAndReturnsDTO() {
        var req = buildRequest(new BigDecimal("85"), new BigDecimal("100"));

        when(userService.findStudentOrThrow(studentId)).thenReturn(student);
        when(classService.findClassOrThrow(classId)).thenReturn(tuitionClass);
        when(testScoreRepository.save(any(TestScore.class))).thenAnswer(inv -> {
            TestScore ts = inv.getArgument(0);
            ts.setId(UUID.randomUUID());
            return ts;
        });

        TestScoreDTO result = testScoreService.createTestScore(req, currentUser, teacher);

        assertThat(result).isNotNull();
        assertThat(result.testName()).isEqualTo("Mid-term");
        assertThat(result.overallScore()).isEqualByComparingTo(new BigDecimal("85"));
        assertThat(result.maxScore()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(result.testSource()).isEqualTo("CENTRE");
        verify(testScoreRepository).save(any(TestScore.class));
    }

    @Test
    void createTestScore_scoreExceedsMax_throwsException() {
        var req = buildRequest(new BigDecimal("110"), new BigDecimal("100"));

        when(userService.findStudentOrThrow(studentId)).thenReturn(student);
        when(classService.findClassOrThrow(classId)).thenReturn(tuitionClass);

        assertThatThrownBy(() -> testScoreService.createTestScore(req, currentUser, teacher))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Overall score cannot exceed max score")
            .extracting(e -> ((AppException) e).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_SCORE);
    }

    @Test
    void createTestScore_nullMaxScore_defaultsTo100() {
        var req = buildRequest(new BigDecimal("85"), null);

        when(userService.findStudentOrThrow(studentId)).thenReturn(student);
        when(classService.findClassOrThrow(classId)).thenReturn(tuitionClass);
        when(testScoreRepository.save(any(TestScore.class))).thenAnswer(inv -> {
            TestScore ts = inv.getArgument(0);
            ts.setId(UUID.randomUUID());
            return ts;
        });

        TestScoreDTO result = testScoreService.createTestScore(req, currentUser, teacher);

        assertThat(result.maxScore()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void createTestScore_withUploadIds_linksUploads() {
        UUID uploadId = UUID.randomUUID();
        var req = new CreateTestScoreRequestDTO(studentId, classId, "Test", LocalDate.of(2026, 3, 15),
            new BigDecimal("70"), new BigDecimal("100"), null, List.of(uploadId), null, null);

        when(userService.findStudentOrThrow(studentId)).thenReturn(student);
        when(classService.findClassOrThrow(classId)).thenReturn(tuitionClass);
        when(testScoreRepository.save(any(TestScore.class))).thenAnswer(inv -> {
            TestScore ts = inv.getArgument(0);
            ts.setId(UUID.randomUUID());
            return ts;
        });

        testScoreService.createTestScore(req, currentUser, teacher);

        verify(testPaperService).linkToTestScore(eq(List.of(uploadId)), any(UUID.class));
    }

    @Test
    void createTestScore_withQuestions_buildsQuestionHierarchy() {
        UUID topicId = UUID.randomUUID();
        Topic topic = new Topic();
        topic.setId(topicId);
        topic.setName("Algebra");

        var subQ = new CreateTestScoreRequestDTO.SubQuestionRequest("1a", new BigDecimal("8"), new BigDecimal("10"), topicId, "x=5", "Good");
        var question = new CreateTestScoreRequestDTO.QuestionRequest("1", new BigDecimal("10"), "Solve for x", "OPEN", null, List.of(subQ));
        var req = new CreateTestScoreRequestDTO(studentId, classId, "Quiz", LocalDate.of(2026, 3, 15),
            new BigDecimal("80"), new BigDecimal("100"), List.of(question), null, null, null);

        when(userService.findStudentOrThrow(studentId)).thenReturn(student);
        when(classService.findClassOrThrow(classId)).thenReturn(tuitionClass);
        when(subjectService.findTopicOrThrow(topicId)).thenReturn(topic);
        when(testScoreRepository.save(any(TestScore.class))).thenAnswer(inv -> {
            TestScore ts = inv.getArgument(0);
            ts.setId(UUID.randomUUID());
            // Simulate JPA setting IDs on questions
            ts.getQuestions().forEach(q -> {
                q.setId(UUID.randomUUID());
                q.getSubQuestions().forEach(sq -> sq.setId(UUID.randomUUID()));
            });
            return ts;
        });

        TestScoreDTO result = testScoreService.createTestScore(req, currentUser, teacher);

        assertThat(result.questions()).hasSize(1);
        assertThat(result.questions().getFirst().subQuestions()).hasSize(1);
        assertThat(result.questions().getFirst().subQuestions().getFirst().topicName()).isEqualTo("Algebra");
    }

    // --- updateTestScore tests ---

    @Test
    void updateTestScore_validUpdate_savesChanges() {
        UUID testScoreId = UUID.randomUUID();
        TestScore existing = new TestScore();
        existing.setId(testScoreId);
        existing.setStudent(student);
        existing.setTuitionClass(tuitionClass);
        existing.setTeacher(teacher);
        existing.setTestName("Old Name");
        existing.setTestDate(LocalDate.of(2026, 1, 1));
        existing.setOverallScore(new BigDecimal("50"));
        existing.setMaxScore(new BigDecimal("100"));
        existing.setTestSource(TestSource.CENTRE);

        var req = buildRequest(new BigDecimal("90"), new BigDecimal("100"));

        when(testScoreRepository.findById(testScoreId)).thenReturn(Optional.of(existing));
        when(testScoreRepository.save(any(TestScore.class))).thenAnswer(inv -> inv.getArgument(0));

        TestScoreDTO result = testScoreService.updateTestScore(testScoreId, req, currentUser);

        assertThat(result.overallScore()).isEqualByComparingTo(new BigDecimal("90"));
        assertThat(result.testName()).isEqualTo("Mid-term");
    }

    @Test
    void updateTestScore_notFound_throwsException() {
        UUID testScoreId = UUID.randomUUID();
        when(testScoreRepository.findById(testScoreId)).thenReturn(Optional.empty());

        var req = buildRequest(new BigDecimal("80"), new BigDecimal("100"));

        assertThatThrownBy(() -> testScoreService.updateTestScore(testScoreId, req, currentUser))
            .isInstanceOf(AppException.class)
            .extracting(e -> ((AppException) e).getErrorCode())
            .isEqualTo(ErrorCode.NOT_FOUND);
    }

    // --- deleteTestScore tests ---

    @Test
    void deleteTestScore_exists_deletesSuccessfully() {
        UUID testScoreId = UUID.randomUUID();
        when(testScoreRepository.existsById(testScoreId)).thenReturn(true);

        testScoreService.deleteTestScore(testScoreId);

        verify(testScoreRepository).deleteById(testScoreId);
    }

    @Test
    void deleteTestScore_notFound_throwsException() {
        UUID testScoreId = UUID.randomUUID();
        when(testScoreRepository.existsById(testScoreId)).thenReturn(false);

        assertThatThrownBy(() -> testScoreService.deleteTestScore(testScoreId))
            .isInstanceOf(AppException.class)
            .extracting(e -> ((AppException) e).getErrorCode())
            .isEqualTo(ErrorCode.NOT_FOUND);
    }

    // --- getTestScore tests ---

    @Test
    void getTestScore_notFound_throwsException() {
        UUID testScoreId = UUID.randomUUID();
        when(testScoreRepository.findById(testScoreId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> testScoreService.getTestScore(testScoreId))
            .isInstanceOf(AppException.class)
            .extracting(e -> ((AppException) e).getErrorCode())
            .isEqualTo(ErrorCode.NOT_FOUND);
    }
}
