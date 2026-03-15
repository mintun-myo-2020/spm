package com.eggtive.spm.testpaper.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TestPaperUploadDTO(
    UUID uploadId,
    UUID testScoreId,
    UUID studentId,
    UUID classId,
    String status,
    List<TestPaperPageDTO> pages,
    List<AggregatedQuestion> aggregatedQuestions,
    Instant createdAt
) {
    /** Flattened parsed question aggregated across all pages. */
    public record AggregatedQuestion(
        String questionNumber,
        String questionText,
        String questionType,
        BigDecimal maxScore,
        List<AggregatedSubQuestion> subQuestions,
        List<AggregatedMcqOption> mcqOptions,
        float confidence,
        int sourcePage
    ) {}

    public record AggregatedSubQuestion(
        String label, String questionText, BigDecimal maxScore,
        String studentAnswer, float confidence
    ) {}

    public record AggregatedMcqOption(String key, String text) {}
}
