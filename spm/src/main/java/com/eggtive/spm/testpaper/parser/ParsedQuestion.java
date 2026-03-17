package com.eggtive.spm.testpaper.parser;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder(toBuilder = true)
public record ParsedQuestion(
    String questionNumber,
    String questionText,
    String questionType,
    List<McqOption> mcqOptions,
    BigDecimal maxScore,
    List<ParsedSubQuestion> subQuestions,
    boolean hasDiagramInQuestion,
    boolean requiresDiagramAnswer,
    float confidence,
    String rawTextSpan
) {
    /** Returns a builder pre-loaded with sensible defaults. */
    public static ParsedQuestionBuilder defaults() {
        return ParsedQuestion.builder()
                .questionType("OPEN")
                .mcqOptions(List.of())
                .subQuestions(List.of())
                .confidence(0.85f);
    }
}
