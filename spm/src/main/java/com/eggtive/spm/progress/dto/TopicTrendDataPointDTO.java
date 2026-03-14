package com.eggtive.spm.progress.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TopicTrendDataPointDTO(
    LocalDate testDate, String testName,
    BigDecimal topicScore, BigDecimal topicMaxScore,
    BigDecimal percentage
) {}
