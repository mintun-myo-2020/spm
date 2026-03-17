package com.eggtive.spm.testpaper.parser;

import lombok.Builder;

import java.math.BigDecimal;

@Builder(toBuilder = true)
public record ParsedSubQuestion(
    String label,
    String questionText,
    BigDecimal maxScore,
    String studentAnswer,
    String studentCorrection,
    String teacherRemarks,
    float confidence
) {
    /** Returns a builder pre-loaded with sensible defaults. */
    public static ParsedSubQuestionBuilder defaults() {
        return ParsedSubQuestion.builder()
                .confidence(0.80f);
    }
}
