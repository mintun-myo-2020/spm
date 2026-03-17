package com.eggtive.spm.report.service;

import com.eggtive.spm.common.enums.Trend;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Rich data record containing all inputs needed to generate a progress report.
 * Designed to be consumed by both template-based and AI-powered report generators.
 */
public record ReportData(
    StudentInfo student,
    ClassInfo classInfo,
    LocalDate startDate,
    LocalDate endDate,
    OverallSummary overallSummary,
    List<ScoreEntry> scores,
    List<TopicSummary> topics,
    List<FeedbackEntry> feedback,
    List<TestDetail> testDetails
) {

    public record StudentInfo(UUID id, String name) {}

    public record ClassInfo(UUID id, String className, String subjectName, String teacherName) {}

    public record OverallSummary(BigDecimal averagePercentage, int testCount, Trend trend) {}

    public record ScoreEntry(
        String testName, LocalDate testDate,
        BigDecimal score, BigDecimal maxScore
    ) {}

    public record TopicSummary(
        UUID topicId, String topicName,
        int questionCount, BigDecimal averagePercent, Trend trend
    ) {}

    public record FeedbackEntry(
        LocalDate date, String strengths,
        String areasForImprovement, String recommendations
    ) {}

    /** Question-level detail per test — included for future AI consumption. */
    public record TestDetail(
        String testName, LocalDate testDate,
        List<QuestionDetail> questions
    ) {}

    public record QuestionDetail(
        String questionNumber, String questionText, String questionType,
        BigDecimal maxScore, List<SubQuestionDetail> subQuestions
    ) {}

    public record SubQuestionDetail(
        String label, String topicName,
        BigDecimal score, BigDecimal maxScore,
        String studentAnswer
    ) {}
}
