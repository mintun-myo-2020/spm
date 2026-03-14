package com.eggtive.spm.progress.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TrendDataPointDTO(
    LocalDate testDate,
    String testName,
    BigDecimal score
) {}
