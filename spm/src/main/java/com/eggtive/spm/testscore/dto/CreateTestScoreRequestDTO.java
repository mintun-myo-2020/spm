package com.eggtive.spm.testscore.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateTestScoreRequestDTO(
    @NotNull UUID studentId,
    @NotNull UUID classId,
    @NotBlank String testName,
    @NotNull LocalDate testDate,
    @NotNull @Positive BigDecimal overallScore,
    BigDecimal maxScore,
    @Valid List<QuestionRequest> questions,
    List<UUID> uploadIds,
    Boolean isDraft
) {
    public record McqOptionRequest(String key, String text) {}

    public record QuestionRequest(
        @NotBlank String questionNumber,
        @NotNull @Positive BigDecimal maxScore,
        String questionText,
        String questionType,
        List<McqOptionRequest> mcqOptions,
        @Valid List<SubQuestionRequest> subQuestions
    ) {}

    public record SubQuestionRequest(
        @NotBlank String label,
        @NotNull BigDecimal score,
        @NotNull @Positive BigDecimal maxScore,
        @NotNull UUID topicId,
        String studentAnswer
    ) {}
}
