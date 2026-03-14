package com.eggtive.spm.progress.service;

import com.eggtive.spm.common.enums.ErrorCode;
import com.eggtive.spm.common.exception.AppException;
import com.eggtive.spm.progress.calculator.ProgressCalculator;
import com.eggtive.spm.progress.dto.*;
import com.eggtive.spm.testscore.entity.SubQuestion;
import com.eggtive.spm.testscore.entity.TestScore;
import com.eggtive.spm.testscore.repository.TestScoreRepository;
import com.eggtive.spm.user.entity.Student;
import com.eggtive.spm.user.repository.StudentRepository;
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

    private final TestScoreRepository testScoreRepository;
    private final StudentRepository studentRepository;
    private final ProgressCalculator calculator;

    public ProgressService(TestScoreRepository tsRepo, StudentRepository studentRepo,
                           ProgressCalculator calculator) {
        this.testScoreRepository = tsRepo;
        this.studentRepository = studentRepo;
        this.calculator = calculator;
    }

    public OverallProgressDTO getOverallProgress(UUID studentId) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Student not found"));
        String name = student.getUser().getFirstName() + " " + student.getUser().getLastName();

        List<TestScore> scores = testScoreRepository.findByStudentIdOrderByTestDateAsc(studentId);
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

    public List<TopicProgressSummaryDTO> getAllTopicsProgress(UUID studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new AppException(ErrorCode.NOT_FOUND, "Student not found");
        }

        List<TestScore> scores = testScoreRepository.findByStudentIdOrderByTestDateAsc(studentId);

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
            String trend = calculator.determineTrend(percentages);

            return new TopicProgressSummaryDTO(topicId, topicNames.get(topicId),
                data.size(), avgPct, latestPct, trend);
        }).toList();
    }

    private record SubQuestionData(BigDecimal score, BigDecimal maxScore, java.time.LocalDate testDate) {}
}
