package com.eggtive.spm.progress.service;

import com.eggtive.spm.common.enums.ErrorCode;
import com.eggtive.spm.common.enums.Trend;
import com.eggtive.spm.common.exception.AppException;
import com.eggtive.spm.progress.calculator.ProgressCalculator;
import com.eggtive.spm.progress.dto.*;
import com.eggtive.spm.testscore.entity.SubQuestion;
import com.eggtive.spm.testscore.entity.TestScore;
import com.eggtive.spm.testscore.service.TestScoreService;
import com.eggtive.spm.user.entity.Student;
import com.eggtive.spm.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true)
public class ProgressService {

    private final TestScoreService testScoreService;
    private final UserService userService;
    private final ProgressCalculator calculator;

    public ProgressService(TestScoreService testScoreService, UserService userService,
                           ProgressCalculator calculator) {
        this.testScoreService = testScoreService;
        this.userService = userService;
        this.calculator = calculator;
    }

    public OverallProgressDTO getOverallProgress(UUID studentId) {
        Student student = userService.findStudentOrThrow(studentId);
        String name = student.getUser().getFirstName() + " " + student.getUser().getLastName();

        List<TestScore> scores = testScoreService.findByStudentOrderByDateAsc(studentId);
        if (scores.isEmpty()) {
            return new OverallProgressDTO(studentId, name, List.of(), BigDecimal.ZERO, null);
        }

        List<TrendDataPointDTO> trendData = scores.stream()
            .map(ts -> new TrendDataPointDTO(ts.getTestDate(), ts.getTestName(), ts.getOverallScore()))
            .toList();

        List<BigDecimal> scoreValues = scores.stream().map(TestScore::getOverallScore).toList();
        BigDecimal avg = calculator.average(scoreValues);

        long months = ChronoUnit.MONTHS.between(
            scores.getFirst().getTestDate(), scores.getLast().getTestDate());
        ImprovementVelocityDTO velocity = calculator.calculateVelocity(scoreValues, Math.max(months, 1));

        return new OverallProgressDTO(studentId, name, trendData, avg, velocity);
    }

    public OverallProgressDTO getProgressByClass(UUID studentId, UUID classId) {
        Student student = userService.findStudentOrThrow(studentId);
        String name = student.getUser().getFirstName() + " " + student.getUser().getLastName();

        List<TestScore> allScores = testScoreService.findByStudentOrderByDateAsc(studentId);
        List<TestScore> scores = allScores.stream()
            .filter(ts -> ts.getTuitionClass().getId().equals(classId))
            .toList();

        if (scores.isEmpty()) {
            return new OverallProgressDTO(studentId, name, List.of(), BigDecimal.ZERO, null);
        }

        List<TrendDataPointDTO> trendData = scores.stream()
            .map(ts -> new TrendDataPointDTO(ts.getTestDate(), ts.getTestName(), ts.getOverallScore()))
            .toList();

        List<BigDecimal> scoreValues = scores.stream().map(TestScore::getOverallScore).toList();
        BigDecimal avg = calculator.average(scoreValues);

        long months = ChronoUnit.MONTHS.between(
            scores.getFirst().getTestDate(), scores.getLast().getTestDate());
        ImprovementVelocityDTO velocity = calculator.calculateVelocity(scoreValues, Math.max(months, 1));

        return new OverallProgressDTO(studentId, name, trendData, avg, velocity);
    }

    public List<TopicProgressSummaryDTO> getTopicsProgressByClass(UUID studentId, UUID classId) {
        if (!userService.studentExists(studentId)) {
            throw new AppException(ErrorCode.NOT_FOUND, "Student not found");
        }

        List<TestScore> allScores = testScoreService.findByStudentOrderByDateAsc(studentId);
        List<TestScore> scores = allScores.stream()
            .filter(ts -> ts.getTuitionClass().getId().equals(classId))
            .toList();

        Map<UUID, List<SubQuestionData>> topicData = new LinkedHashMap<>();
        Map<UUID, String> topicNames = new HashMap<>();

        for (TestScore ts : scores) {
            for (var q : ts.getQuestions()) {
                for (SubQuestion sq : q.getSubQuestions()) {
                    UUID topicId = sq.getTopic().getId();
                    topicNames.putIfAbsent(topicId, sq.getTopic().getName());
                    topicData.computeIfAbsent(topicId, k -> new ArrayList<>())
                        .add(new SubQuestionData(sq.getScore(), sq.getMaxScore(), ts.getTestDate()));
                }
            }
        }

        return topicData.entrySet().stream().map(entry -> {
            UUID topicId = entry.getKey();
            List<SubQuestionData> data = entry.getValue();

            List<BigDecimal> percentages = data.stream()
                .map(d -> d.score.multiply(BigDecimal.valueOf(100))
                    .divide(d.maxScore, 2, RoundingMode.HALF_UP))
                .toList();

            BigDecimal avgPct = calculator.average(percentages);
            BigDecimal latestPct = percentages.getLast();
            Trend trend = calculator.determineTrend(percentages);

            return new TopicProgressSummaryDTO(topicId, topicNames.get(topicId),
                data.size(), avgPct, latestPct, trend);
        }).toList();
    }


    public List<TopicProgressSummaryDTO> getAllTopicsProgress(UUID studentId) {
        if (!userService.studentExists(studentId)) {
            throw new AppException(ErrorCode.NOT_FOUND, "Student not found");
        }

        List<TestScore> scores = testScoreService.findByStudentOrderByDateAsc(studentId);

        // Collect all sub-question scores grouped by topic
        Map<UUID, List<SubQuestionData>> topicData = new LinkedHashMap<>();
        Map<UUID, String> topicNames = new HashMap<>();

        for (TestScore ts : scores) {
            for (var q : ts.getQuestions()) {
                for (SubQuestion sq : q.getSubQuestions()) {
                    UUID topicId = sq.getTopic().getId();
                    topicNames.putIfAbsent(topicId, sq.getTopic().getName());
                    topicData.computeIfAbsent(topicId, k -> new ArrayList<>())
                        .add(new SubQuestionData(sq.getScore(), sq.getMaxScore(), ts.getTestDate()));
                }
            }
        }

        return topicData.entrySet().stream().map(entry -> {
            UUID topicId = entry.getKey();
            List<SubQuestionData> data = entry.getValue();

            List<BigDecimal> percentages = data.stream()
                .map(d -> d.score.multiply(BigDecimal.valueOf(100))
                    .divide(d.maxScore, 2, RoundingMode.HALF_UP))
                .toList();

            BigDecimal avgPct = calculator.average(percentages);
            BigDecimal latestPct = percentages.getLast();
            Trend trend = calculator.determineTrend(percentages);

            return new TopicProgressSummaryDTO(topicId, topicNames.get(topicId),
                data.size(), avgPct, latestPct, trend);
        }).toList();
    }

    public TopicProgressDTO getTopicProgress(UUID studentId, UUID topicId) {
        Student student = userService.findStudentOrThrow(studentId);
        List<TestScore> scores = testScoreService.findByStudentOrderByDateAsc(studentId);

        String topicName = null;
        List<TopicTrendDataPointDTO> trendData = new ArrayList<>();

        for (TestScore ts : scores) {
            BigDecimal topicScore = BigDecimal.ZERO;
            BigDecimal topicMaxScore = BigDecimal.ZERO;
            boolean found = false;

            for (var q : ts.getQuestions()) {
                for (SubQuestion sq : q.getSubQuestions()) {
                    if (sq.getTopic().getId().equals(topicId)) {
                        topicScore = topicScore.add(sq.getScore());
                        topicMaxScore = topicMaxScore.add(sq.getMaxScore());
                        if (topicName == null) topicName = sq.getTopic().getName();
                        found = true;
                    }
                }
            }

            if (found && topicMaxScore.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal pct = topicScore.multiply(BigDecimal.valueOf(100))
                    .divide(topicMaxScore, 2, RoundingMode.HALF_UP);
                trendData.add(new TopicTrendDataPointDTO(ts.getId(), ts.getTestDate(), ts.getTestName(),
                    topicScore, topicMaxScore, pct));
            }
        }

        if (topicName == null) {
            throw new AppException(ErrorCode.NOT_FOUND, "No data for this topic");
        }

        List<BigDecimal> percentages = trendData.stream()
            .map(TopicTrendDataPointDTO::percentage).toList();
        BigDecimal avg = calculator.average(percentages);

        return new TopicProgressDTO(studentId, topicId, topicName, trendData, avg);
    }


    private record SubQuestionData(BigDecimal score, BigDecimal maxScore, java.time.LocalDate testDate) {}
    public ClassSummaryDTO getClassSummary(UUID classId) {
        List<TestScore> scores = testScoreService.findByClassOrderByDateAsc(classId);

        if (scores.isEmpty()) {
            return new ClassSummaryDTO(classId, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO,
                null, null, List.of(), Trend.INSUFFICIENT_DATA);
        }

        int studentCount = (int) scores.stream()
            .map(ts -> ts.getStudent().getId()).distinct().count();

        // Mean and median of overall score percentages
        List<BigDecimal> allPercentages = scores.stream()
            .map(ts -> ts.getOverallScore().multiply(BigDecimal.valueOf(100))
                .divide(ts.getMaxScore(), 2, RoundingMode.HALF_UP))
            .toList();

        BigDecimal mean = calculator.average(allPercentages);

        List<BigDecimal> sorted = allPercentages.stream().sorted().toList();
        BigDecimal median;
        int size = sorted.size();
        if (size % 2 == 0) {
            median = sorted.get(size / 2 - 1).add(sorted.get(size / 2))
                .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        } else {
            median = sorted.get(size / 2);
        }

        // Topic-level aggregation across all students
        Map<UUID, List<BigDecimal>> topicPercentages = new LinkedHashMap<>();
        Map<UUID, String> topicNames = new HashMap<>();

        for (TestScore ts : scores) {
            for (var q : ts.getQuestions()) {
                for (SubQuestion sq : q.getSubQuestions()) {
                    UUID topicId = sq.getTopic().getId();
                    topicNames.putIfAbsent(topicId, sq.getTopic().getName());
                    BigDecimal pct = sq.getMaxScore().compareTo(BigDecimal.ZERO) > 0
                        ? sq.getScore().multiply(BigDecimal.valueOf(100))
                            .divide(sq.getMaxScore(), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                    topicPercentages.computeIfAbsent(topicId, k -> new ArrayList<>()).add(pct);
                }
            }
        }

        List<ClassSummaryDTO.TopicStat> topicStats = topicPercentages.entrySet().stream()
            .map(e -> new ClassSummaryDTO.TopicStat(
                e.getKey(),
                topicNames.get(e.getKey()),
                calculator.average(e.getValue()),
                calculator.determineTrend(e.getValue())
            ))
            .toList();

        ClassSummaryDTO.TopicStat strongest = topicStats.stream()
            .max(Comparator.comparing(ClassSummaryDTO.TopicStat::averagePercentage))
            .orElse(null);
        ClassSummaryDTO.TopicStat weakest = topicStats.stream()
            .min(Comparator.comparing(ClassSummaryDTO.TopicStat::averagePercentage))
            .orElse(null);

        // Overall trend from all score percentages chronologically
        Trend overallTrend = calculator.determineTrend(new ArrayList<>(allPercentages));

        return new ClassSummaryDTO(classId, studentCount, scores.size(), mean, median,
            strongest, weakest, topicStats, overallTrend);
    }


}

