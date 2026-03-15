package com.eggtive.spm.report.service;

import com.eggtive.spm.classmanagement.entity.TuitionClass;
import com.eggtive.spm.classmanagement.service.ClassService;
import com.eggtive.spm.feedback.entity.Feedback;
import com.eggtive.spm.feedback.repository.FeedbackRepository;
import com.eggtive.spm.testscore.entity.Question;
import com.eggtive.spm.testscore.entity.SubQuestion;
import com.eggtive.spm.testscore.entity.TestScore;
import com.eggtive.spm.testscore.service.TestScoreService;
import com.eggtive.spm.user.entity.Student;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class ReportDataAssembler {

    private final TestScoreService testScoreService;
    private final FeedbackRepository feedbackRepository;
    private final ClassService classService;

    public ReportDataAssembler(TestScoreService testScoreService,
                               FeedbackRepository feedbackRepository,
                               ClassService classService) {
        this.testScoreService = testScoreService;
        this.feedbackRepository = feedbackRepository;
        this.classService = classService;
    }

    public ReportData assemble(Student student, UUID classId, LocalDate startDate, LocalDate endDate) {
        TuitionClass tc = classService.findClassOrThrow(classId);
        String studentName = student.getUser().getFirstName() + " " + student.getUser().getLastName();
        String teacherName = tc.getTeacher().getUser().getFirstName() + " " + tc.getTeacher().getUser().getLastName();

        var studentInfo = new ReportData.StudentInfo(student.getId(), studentName);
        var classInfo = new ReportData.ClassInfo(classId, tc.getName(),
                tc.getSubject().getName(), teacherName);

        // Fetch test scores for this student in this class within date range
        List<TestScore> allScores = testScoreService.findByStudentOrderByDateAsc(student.getId());
        List<TestScore> scores = allScores.stream()
                .filter(ts -> ts.getTuitionClass().getId().equals(classId))
                .filter(ts -> !ts.getTestDate().isBefore(startDate) && !ts.getTestDate().isAfter(endDate))
                .toList();

        // Score entries
        List<ReportData.ScoreEntry> scoreEntries = scores.stream()
                .map(ts -> new ReportData.ScoreEntry(ts.getTestName(), ts.getTestDate(),
                        ts.getOverallScore(), ts.getMaxScore()))
                .toList();

        // Overall summary
        ReportData.OverallSummary overallSummary = buildOverallSummary(scores);

        // Topic summaries
        List<ReportData.TopicSummary> topicSummaries = buildTopicSummaries(scores);

        // Feedback (most recent 5 from this class's teacher)
        UUID teacherId = tc.getTeacher().getId();
        Instant from = startDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to = endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        List<Feedback> feedbackList = feedbackRepository
                .findRecentByStudentAndTeacher(student.getId(), teacherId, from, to);
        List<ReportData.FeedbackEntry> feedbackEntries = feedbackList.stream()
                .map(fb -> new ReportData.FeedbackEntry(
                        fb.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate(),
                        fb.getStrengths(), fb.getAreasForImprovement(), fb.getRecommendations()))
                .toList();

        // Question-level detail per test (for future AI use)
        List<ReportData.TestDetail> testDetails = scores.stream()
                .map(this::buildTestDetail)
                .toList();

        return new ReportData(studentInfo, classInfo, startDate, endDate,
                overallSummary, scoreEntries, topicSummaries, feedbackEntries, testDetails);
    }

    private ReportData.OverallSummary buildOverallSummary(List<TestScore> scores) {
        if (scores.isEmpty()) {
            return new ReportData.OverallSummary(BigDecimal.ZERO, 0, "N/A");
        }
        BigDecimal sum = scores.stream().map(TestScore::getOverallScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avg = sum.divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);

        String trend = "STABLE";
        if (scores.size() >= 2) {
            BigDecimal first = scores.getFirst().getOverallScore();
            BigDecimal last = scores.getLast().getOverallScore();
            int cmp = last.compareTo(first);
            if (cmp > 0) trend = "IMPROVING";
            else if (cmp < 0) trend = "DECLINING";
        }
        return new ReportData.OverallSummary(avg, scores.size(), trend);
    }

    private List<ReportData.TopicSummary> buildTopicSummaries(List<TestScore> scores) {
        Map<UUID, List<BigDecimal>> topicPercentages = new LinkedHashMap<>();
        Map<UUID, String> topicNames = new HashMap<>();
        Map<UUID, Integer> topicCounts = new HashMap<>();

        for (TestScore ts : scores) {
            for (Question q : ts.getQuestions()) {
                for (SubQuestion sq : q.getSubQuestions()) {
                    UUID topicId = sq.getTopic().getId();
                    topicNames.putIfAbsent(topicId, sq.getTopic().getName());
                    topicCounts.merge(topicId, 1, Integer::sum);
                    BigDecimal pct = sq.getMaxScore().compareTo(BigDecimal.ZERO) == 0
                            ? BigDecimal.ZERO
                            : sq.getScore().multiply(BigDecimal.valueOf(100))
                                .divide(sq.getMaxScore(), 2, RoundingMode.HALF_UP);
                    topicPercentages.computeIfAbsent(topicId, k -> new ArrayList<>()).add(pct);
                }
            }
        }

        return topicPercentages.entrySet().stream().map(entry -> {
            UUID topicId = entry.getKey();
            List<BigDecimal> pcts = entry.getValue();
            BigDecimal avg = pcts.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(pcts.size()), 2, RoundingMode.HALF_UP);
            String trend = determineTrend(pcts);
            return new ReportData.TopicSummary(topicId, topicNames.get(topicId),
                    topicCounts.get(topicId), avg, trend);
        }).toList();
    }

    private String determineTrend(List<BigDecimal> values) {
        if (values.size() < 2) return "N/A";
        BigDecimal first = values.getFirst();
        BigDecimal last = values.getLast();
        int cmp = last.compareTo(first);
        if (cmp > 0) return "IMPROVING";
        if (cmp < 0) return "DECLINING";
        return "STABLE";
    }

    private ReportData.TestDetail buildTestDetail(TestScore ts) {
        List<ReportData.QuestionDetail> questions = ts.getQuestions().stream()
                .map(q -> {
                    List<ReportData.SubQuestionDetail> subs = q.getSubQuestions().stream()
                            .map(sq -> new ReportData.SubQuestionDetail(
                                    sq.getSubQuestionLabel(), sq.getTopic().getName(),
                                    sq.getScore(), sq.getMaxScore(), sq.getStudentAnswer()))
                            .toList();
                    return new ReportData.QuestionDetail(q.getQuestionNumber(), q.getQuestionText(),
                            q.getQuestionType(), q.getMaxScore(), subs);
                }).toList();
        return new ReportData.TestDetail(ts.getTestName(), ts.getTestDate(), questions);
    }
}
