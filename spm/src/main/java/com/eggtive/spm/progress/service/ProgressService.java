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
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;


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
            .map(ts -> new TrendDataPointDTO(ts.getTestDate(), ts.getTestName(), toPercentage(ts)))
            .toList();

        List<BigDecimal> pctValues = scores.stream().map(this::toPercentage).toList();
        BigDecimal avg = calculator.average(pctValues);

        long months = ChronoUnit.MONTHS.between(
            scores.getFirst().getTestDate(), scores.getLast().getTestDate());
        ImprovementVelocityDTO velocity = calculator.calculateVelocity(pctValues, Math.max(months, 1));

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
            .map(ts -> new TrendDataPointDTO(ts.getTestDate(), ts.getTestName(), toPercentage(ts)))
            .toList();

        List<BigDecimal> pctValues = scores.stream().map(this::toPercentage).toList();
        BigDecimal avg = calculator.average(pctValues);

        long months = ChronoUnit.MONTHS.between(
            scores.getFirst().getTestDate(), scores.getLast().getTestDate());
        ImprovementVelocityDTO velocity = calculator.calculateVelocity(pctValues, Math.max(months, 1));

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
        return aggregateTopicProgress(scores);
    }

    public List<TopicProgressSummaryDTO> getAllTopicsProgress(UUID studentId) {
        if (!userService.studentExists(studentId)) {
            throw new AppException(ErrorCode.NOT_FOUND, "Student not found");
        }
        List<TestScore> scores = testScoreService.findByStudentOrderByDateAsc(studentId);
        return aggregateTopicProgress(scores);
    }

    /**
     * Shared topic aggregation logic.
     *
     * Average percentage = totalScore / totalMaxScore * 100 across ALL questions for a topic
     * (i.e. every question counts equally regardless of which test it came from).
     *
     * Trend is still computed from per-test percentages (chronological) so we can detect
     * improvement/decline over time.
     *
     * Latest percentage is selected by test date, not by insertion order.
     */
    private List<TopicProgressSummaryDTO> aggregateTopicProgress(List<TestScore> scores) {
        // Running totals for the sum-based average
        Map<UUID, BigDecimal> topicTotalScore = new LinkedHashMap<>();
        Map<UUID, BigDecimal> topicTotalMaxScore = new LinkedHashMap<>();
        // Per-test percentages for trend calculation
        Map<UUID, List<BigDecimal>> topicPerTestPct = new LinkedHashMap<>();
        Map<UUID, String> topicNames = new HashMap<>();
        Map<UUID, Integer> topicTestCounts = new HashMap<>();
        Map<UUID, Integer> topicQuestionCounts = new HashMap<>();
        // Latest percentage by date (not insertion order)
        Map<UUID, LocalDate> topicLatestDate = new HashMap<>();
        Map<UUID, BigDecimal> topicLatestPct = new HashMap<>();

        for (TestScore ts : scores) {
            Map<UUID, BigDecimal> testTopicScore = new LinkedHashMap<>();
            Map<UUID, BigDecimal> testTopicMaxScore = new LinkedHashMap<>();
            Map<UUID, Integer> testTopicQCount = new HashMap<>();

            for (var q : ts.getQuestions()) {
                for (SubQuestion sq : q.getSubQuestions()) {
                    UUID topicId = sq.getTopic().getId();
                    topicNames.putIfAbsent(topicId, sq.getTopic().getName());
                    testTopicScore.merge(topicId, sq.getScore(), BigDecimal::add);
                    testTopicMaxScore.merge(topicId, sq.getMaxScore(), BigDecimal::add);
                    testTopicQCount.merge(topicId, 1, Integer::sum);
                }
            }

            for (UUID topicId : testTopicScore.keySet()) {
                BigDecimal maxScore = testTopicMaxScore.get(topicId);
                if (maxScore.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal score = testTopicScore.get(topicId);
                    BigDecimal pct = score.multiply(BigDecimal.valueOf(100))
                        .divide(maxScore, 2, RoundingMode.HALF_UP);
                    int qCount = testTopicQCount.get(topicId);

                    // Accumulate raw totals for sum-based average
                    topicTotalScore.merge(topicId, score, BigDecimal::add);
                    topicTotalMaxScore.merge(topicId, maxScore, BigDecimal::add);
                    // Per-test percentage for trend
                    topicPerTestPct.computeIfAbsent(topicId, k -> new ArrayList<>()).add(pct);
                    topicTestCounts.merge(topicId, 1, Integer::sum);
                    topicQuestionCounts.merge(topicId, qCount, Integer::sum);

                    // Track latest by date
                    LocalDate prevDate = topicLatestDate.get(topicId);
                    if (prevDate == null || ts.getTestDate().isAfter(prevDate)) {
                        topicLatestDate.put(topicId, ts.getTestDate());
                        topicLatestPct.put(topicId, pct);
                    }
                }
            }
        }

        return topicTotalScore.entrySet().stream().map(entry -> {
            UUID topicId = entry.getKey();
            // Average = totalScore / totalMaxScore * 100
            BigDecimal avgPct = entry.getValue().multiply(BigDecimal.valueOf(100))
                .divide(topicTotalMaxScore.get(topicId), 2, RoundingMode.HALF_UP);
            BigDecimal latestPct = topicLatestPct.get(topicId);
            Trend trend = calculator.determineTrend(topicPerTestPct.get(topicId));

            return new TopicProgressSummaryDTO(topicId, topicNames.get(topicId),
                topicTestCounts.get(topicId), topicQuestionCounts.get(topicId),
                avgPct, latestPct, trend);
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
            int questionCount = 0;
            boolean found = false;

            for (var q : ts.getQuestions()) {
                for (SubQuestion sq : q.getSubQuestions()) {
                    if (sq.getTopic().getId().equals(topicId)) {
                        topicScore = topicScore.add(sq.getScore());
                        topicMaxScore = topicMaxScore.add(sq.getMaxScore());
                        if (topicName == null) topicName = sq.getTopic().getName();
                        questionCount++;
                        found = true;
                    }
                }
            }

            if (found && topicMaxScore.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal pct = topicScore.multiply(BigDecimal.valueOf(100))
                    .divide(topicMaxScore, 2, RoundingMode.HALF_UP);
                trendData.add(new TopicTrendDataPointDTO(ts.getId(), ts.getTestDate(), ts.getTestName(),
                    topicScore, topicMaxScore, pct, questionCount));
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

        // Topic-level: sum-based average, per-test percentages for trend
        Map<UUID, BigDecimal> topicTotalScore = new LinkedHashMap<>();
        Map<UUID, BigDecimal> topicTotalMaxScore = new LinkedHashMap<>();
        Map<UUID, List<BigDecimal>> topicPerTestPct = new LinkedHashMap<>();
        Map<UUID, String> topicNames = new HashMap<>();

        for (TestScore ts : scores) {
            Map<UUID, BigDecimal> testTopicScore = new LinkedHashMap<>();
            Map<UUID, BigDecimal> testTopicMaxScore = new LinkedHashMap<>();

            for (var q : ts.getQuestions()) {
                for (SubQuestion sq : q.getSubQuestions()) {
                    UUID topicId = sq.getTopic().getId();
                    topicNames.putIfAbsent(topicId, sq.getTopic().getName());
                    testTopicScore.merge(topicId, sq.getScore(), BigDecimal::add);
                    testTopicMaxScore.merge(topicId, sq.getMaxScore(), BigDecimal::add);
                }
            }

            for (UUID topicId : testTopicScore.keySet()) {
                BigDecimal maxScore = testTopicMaxScore.get(topicId);
                if (maxScore.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal score = testTopicScore.get(topicId);
                    BigDecimal pct = score.multiply(BigDecimal.valueOf(100))
                        .divide(maxScore, 2, RoundingMode.HALF_UP);
                    topicTotalScore.merge(topicId, score, BigDecimal::add);
                    topicTotalMaxScore.merge(topicId, maxScore, BigDecimal::add);
                    topicPerTestPct.computeIfAbsent(topicId, k -> new ArrayList<>()).add(pct);
                }
            }
        }

        List<ClassSummaryDTO.TopicStat> topicStats = topicTotalScore.entrySet().stream()
            .map(e -> {
                UUID topicId = e.getKey();
                BigDecimal avgPct = e.getValue().multiply(BigDecimal.valueOf(100))
                    .divide(topicTotalMaxScore.get(topicId), 2, RoundingMode.HALF_UP);
                return new ClassSummaryDTO.TopicStat(
                    topicId,
                    topicNames.get(topicId),
                    avgPct,
                    calculator.determineTrend(topicPerTestPct.get(topicId))
                );
            })
            .toList();

        ClassSummaryDTO.TopicStat strongest = topicStats.stream()
            .max(Comparator.comparing(ClassSummaryDTO.TopicStat::averagePercentage))
            .orElse(null);
        ClassSummaryDTO.TopicStat weakest = topicStats.stream()
            .min(Comparator.comparing(ClassSummaryDTO.TopicStat::averagePercentage))
            .orElse(null);

        Trend overallTrend = calculator.determineTrend(new ArrayList<>(allPercentages));

        return new ClassSummaryDTO(classId, studentCount, scores.size(), mean, median,
            strongest, weakest, topicStats, overallTrend);
    }

    /** Convert a test score to percentage: overallScore / maxScore * 100 */
    private BigDecimal toPercentage(TestScore ts) {
        if (ts.getMaxScore().compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return ts.getOverallScore().multiply(BigDecimal.valueOf(100))
            .divide(ts.getMaxScore(), 2, RoundingMode.HALF_UP);
    }
}
