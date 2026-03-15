package com.eggtive.spm.testpaper.parser;

import java.math.BigDecimal;
import java.util.List;

public record ParsedResult(
    List<ParsedQuestion> questions,
    BigDecimal totalDetectedMarks,
    List<String> parsingNotes
) {}
