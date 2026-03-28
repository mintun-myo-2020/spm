package com.eggtive.spm.progress.service;

import com.eggtive.spm.classmanagement.entity.TuitionClass;
import com.eggtive.spm.common.enums.Trend;
import com.eggtive.spm.common.exception.AppException;
import com.eggtive.spm.progress.calculator.SimpleProgressCalculator;
import com.eggtive.spm.progress.dto.*;
import com.eggtive.spm.subject.entity.Topic;
import com.eggtive.spm.testscore.entity.Question;
import com.eggtive.spm.testscore.entity.SubQuestion;
import com.eggtive.spm.testscore.entity.TestScore;
import com.eggtive.spm.testscore.service.TestScoreService;
import com.eggtive.spm.user.entity.Student;
import com.eggtive.spm.user.entity.User;
import com.eggtive.spm.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {

    @Mock private TestScoreService testScoreService;
    @Mock private UserService userService;

    // Use real calculator — it's pure logic, no I/O
    private final SimpleProgressCalculator calculator = new SimpleProgressCalculator();
    private ProgressService progressService;

    private UUID studentId;
    private Student student;

    @BeforeEach
    void setUp() {
        progressService = new ProgressService(testScoreService, userService, calculator);
        studentId = UUID.randomUUID();
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        student = new Student();
        student.setId(studentId);
        student.setUser(user);
    }

    private TestScore buildTestScore(String name, LocalDate date, BigDecimal score, BigDecimal max, UUID classId) {
        TuitionClass tc = new TuitionClass();
        tc.setId(classId);
        tc.setName("Class");

        User teacherUser = new User();
        teacherUser.setFirstName("T");
        teacherUser.setLastName("T");
        var teacher = new com.eggtive.spm.user.entity.Teacher();
        teacher.setId(UUID.randomUUID());
        teacher.setUser(teacherUser);

        TestScore ts = new TestScore();
        ts.setId(UUID.randomUUID());
        ts.setStudent(student);
        ts.setTuitionClass(tc);
        ts.setTeacher(teacher);
        ts.setTestName(name);
        ts.setTestDate(date);
        ts.setOverallScore(score);
        ts.setMaxScore(max);
        return ts;
    }

    private void addSubQuestion(TestScore ts, UUID topicId, String topicName,
                                BigDecimal score, BigDecimal maxScore) {
        Topic topic = new Topic();
        topic.setId(topicId);
        topic.setName(topicName);

        SubQuestion sq = new SubQuestion();
        sq.setScore(score);
        sq.setMaxScore(maxScore);
        sq.setTopic(topic);
        sq.setSubQuestionLabel("1a");

        Question q;
        if (ts.getQuestions().isEmpty()) {
            q = new Question();
            q.setTestScore(ts);
            q.setQuestionNumber("1");
            q.setMaxScore(maxScore);
            ts.getQuestions().add(q);
        } else {
            q = ts.getQuestions().getFirst();
        }
        sq.setQuestion(q);
        q.getSubQuestions().add(sq);
    }

    // --- getOverallProgress tests ---

    @Test
    void getOverallProgress_noScores_returnsZeroAverage() {
        when(userService.findStudentOrThrow(studentId)).thenReturn(student);
        when(testScoreService.findByStudentOrderByDateAsc(studentId)).thenReturn(List.of());

        OverallProgressDTO result = progressService.getOverallProgress(studentId);

        assertThat(result.studentId()).isEqualTo(studentId);
        assertThat(result.studentName()).isEqualTo("John Doe");
        assertThat(result.averageScore()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.trendData()).isEmpty();
        assertThat(result.improvementVelocity()).isNull();
    }

    @Test
    void getOverallProgress_withScores_calculatesAverageAndVelocity() {
        UUID classId = UUID.randomUUID();
        TestScore ts1 = buildTestScore("Test 1", LocalDate.of(2026, 1, 15), new BigDecimal("60"), new BigDecimal("100"), classId);
        TestScore ts2 = buildTestScore("Test 2", LocalDate.of(2026, 3, 15), new BigDecimal("80"), new BigDecimal("100"), classId);

        when(userService.findStudentOrThrow(studentId)).thenReturn(student);
        when(testScoreService.findByStudentOrderByDateAsc(studentId)).thenReturn(List.of(ts1, ts2));

        OverallProgressDTO result = progressService.getOverallProgress(studentId);

        // Average of 60% and 80% = 70%
        assertThat(result.averageScore()).isEqualByComparingTo(new BigDecimal("70.00"));
        assertThat(result.trendData()).hasSize(2);
        assertThat(result.improvementVelocity()).isNotNull();
        // Improvement = 80 - 60 = 20
        assertThat(result.improvementVelocity().improvement()).isEqualByComparingTo(new BigDecimal("20.00"));
    }

    // --- getProgressByClass tests ---

    @Test
    void getProgressByClass_filtersScoresByClass() {
        UUID classA = UUID.randomUUID();
        UUID classB = UUID.randomUUID();
        TestScore ts1 = buildTestScore("Test 1", LocalDate.of(2026, 1, 10), new BigDecimal("70"), new BigDecimal("100"), classA);
        TestScore ts2 = buildTestScore("Test 2", LocalDate.of(2026, 2, 10), new BigDecimal("90"), new BigDecimal("100"), classB);
        TestScore ts3 = buildTestScore("Test 3", LocalDate.of(2026, 3, 10), new BigDecimal("80"), new BigDecimal("100"), classA);

        when(userService.findStudentOrThrow(studentId)).thenReturn(student);
        when(testScoreService.findByStudentOrderByDateAsc(studentId)).thenReturn(List.of(ts1, ts2, ts3));

        OverallProgressDTO result = progressService.getProgressByClass(studentId, classA);

        // Only classA scores: 70% and 80%, avg = 75%
        assertThat(result.trendData()).hasSize(2);
        assertThat(result.averageScore()).isEqualByComparingTo(new BigDecimal("75.00"));
    }

    @Test
    void getProgressByClass_noMatchingClass_returnsEmpty() {
        UUID classA = UUID.randomUUID();
        UUID classB = UUID.randomUUID();
        TestScore ts1 = buildTestScore("Test 1", LocalDate.of(2026, 1, 10), new BigDecimal("70"), new BigDecimal("100"), classA);

        when(userService.findStudentOrThrow(studentId)).thenReturn(student);
        when(testScoreService.findByStudentOrderByDateAsc(studentId)).thenReturn(List.of(ts1));

        OverallProgressDTO result = progressService.getProgressByClass(studentId, classB);

        assertThat(result.trendData()).isEmpty();
        assertThat(result.averageScore()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // --- getAllTopicsProgress tests ---

    @Test
    void getAllTopicsProgress_aggregatesAcrossTests() {
        UUID topicId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();

        TestScore ts1 = buildTestScore("Test 1", LocalDate.of(2026, 1, 10), new BigDecimal("70"), new BigDecimal("100"), classId);
        addSubQuestion(ts1, topicId, "Algebra", new BigDecimal("7"), new BigDecimal("10"));

        TestScore ts2 = buildTestScore("Test 2", LocalDate.of(2026, 2, 10), new BigDecimal("80"), new BigDecimal("100"), classId);
        addSubQuestion(ts2, topicId, "Algebra", new BigDecimal("9"), new BigDecimal("10"));

        when(userService.studentExists(studentId)).thenReturn(true);
        when(testScoreService.findByStudentOrderByDateAsc(studentId)).thenReturn(List.of(ts1, ts2));

        List<TopicProgressSummaryDTO> result = progressService.getAllTopicsProgress(studentId);

        assertThat(result).hasSize(1);
        TopicProgressSummaryDTO topic = result.getFirst();
        assertThat(topic.topicName()).isEqualTo("Algebra");
        assertThat(topic.testCount()).isEqualTo(2);
        // Sum-based average: (7+9)/(10+10)*100 = 80%
        assertThat(topic.averagePercentage()).isEqualByComparingTo(new BigDecimal("80.00"));
        // Latest is test 2: 9/10 = 90%
        assertThat(topic.latestPercentage()).isEqualByComparingTo(new BigDecimal("90.00"));
    }

    @Test
    void getAllTopicsProgress_studentNotFound_throwsException() {
        when(userService.studentExists(studentId)).thenReturn(false);

        assertThatThrownBy(() -> progressService.getAllTopicsProgress(studentId))
            .isInstanceOf(AppException.class);
    }

    // --- getTopicProgress tests ---

    @Test
    void getTopicProgress_returnsPerTestTrendData() {
        UUID topicId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();

        TestScore ts1 = buildTestScore("Test 1", LocalDate.of(2026, 1, 10), new BigDecimal("50"), new BigDecimal("100"), classId);
        addSubQuestion(ts1, topicId, "Geometry", new BigDecimal("5"), new BigDecimal("10"));

        TestScore ts2 = buildTestScore("Test 2", LocalDate.of(2026, 2, 10), new BigDecimal("80"), new BigDecimal("100"), classId);
        addSubQuestion(ts2, topicId, "Geometry", new BigDecimal("8"), new BigDecimal("10"));

        when(userService.findStudentOrThrow(studentId)).thenReturn(student);
        when(testScoreService.findByStudentOrderByDateAsc(studentId)).thenReturn(List.of(ts1, ts2));

        TopicProgressDTO result = progressService.getTopicProgress(studentId, topicId);

        assertThat(result.topicName()).isEqualTo("Geometry");
        assertThat(result.trendData()).hasSize(2);
        // Average of 50% and 80% = 65%
        assertThat(result.averagePercentage()).isEqualByComparingTo(new BigDecimal("65.00"));
    }

    @Test
    void getTopicProgress_noDataForTopic_throwsException() {
        UUID topicId = UUID.randomUUID();
        when(userService.findStudentOrThrow(studentId)).thenReturn(student);
        when(testScoreService.findByStudentOrderByDateAsc(studentId)).thenReturn(List.of());

        assertThatThrownBy(() -> progressService.getTopicProgress(studentId, topicId))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("No data for this topic");
    }

    // --- getClassSummary tests ---

    @Test
    void getClassSummary_noScores_returnsEmptySummary() {
        UUID classId = UUID.randomUUID();
        when(testScoreService.findByClassOrderByDateAsc(classId)).thenReturn(List.of());

        ClassSummaryDTO result = progressService.getClassSummary(classId);

        assertThat(result.studentCount()).isEqualTo(0);
        assertThat(result.testCount()).isEqualTo(0);
        assertThat(result.overallTrend()).isEqualTo(Trend.INSUFFICIENT_DATA);
    }

    @Test
    void getClassSummary_calculatesMeanAndMedian() {
        UUID classId = UUID.randomUUID();
        // 3 scores: 60%, 80%, 90%
        TestScore ts1 = buildTestScore("T1", LocalDate.of(2026, 1, 1), new BigDecimal("60"), new BigDecimal("100"), classId);
        TestScore ts2 = buildTestScore("T2", LocalDate.of(2026, 2, 1), new BigDecimal("80"), new BigDecimal("100"), classId);
        TestScore ts3 = buildTestScore("T3", LocalDate.of(2026, 3, 1), new BigDecimal("90"), new BigDecimal("100"), classId);

        // Different students for each
        User u2 = new User(); u2.setFirstName("A"); u2.setLastName("B");
        Student s2 = new Student(); s2.setId(UUID.randomUUID()); s2.setUser(u2);
        ts2.setStudent(s2);
        User u3 = new User(); u3.setFirstName("C"); u3.setLastName("D");
        Student s3 = new Student(); s3.setId(UUID.randomUUID()); s3.setUser(u3);
        ts3.setStudent(s3);

        when(testScoreService.findByClassOrderByDateAsc(classId)).thenReturn(List.of(ts1, ts2, ts3));

        ClassSummaryDTO result = progressService.getClassSummary(classId);

        assertThat(result.studentCount()).isEqualTo(3);
        assertThat(result.testCount()).isEqualTo(3);
        // Mean = (60+80+90)/3 = 76.67
        assertThat(result.meanScore()).isEqualByComparingTo(new BigDecimal("76.67"));
        // Median of [60, 80, 90] = 80
        assertThat(result.medianScore()).isEqualByComparingTo(new BigDecimal("80.00"));
    }
}
