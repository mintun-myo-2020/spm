package com.eggtive.spm.testpaper.parser;

import java.math.BigDecimal;

public record ParsedSubQuestion(
    String label,
    String questionText,
    BigDecimal maxScore,
    String studentAnswer,
    String studentCorrection,
    String teacherRemarks,
    float confidence
) {}
