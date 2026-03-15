package com.eggtive.spm.testpaper.parser;

import java.math.BigDecimal;
import java.util.List;

public record ParsedQuestion(
    String questionNumber,
    String questionText,
    String questionType,
    List<McqOption> mcqOptions,
    BigDecimal maxScore,
    List<ParsedSubQuestion> subQuestions,
    float confidence,
    String rawTextSpan
) {}
