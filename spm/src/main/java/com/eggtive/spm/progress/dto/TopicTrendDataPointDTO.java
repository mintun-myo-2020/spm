package com.eggtive.spm.progress.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TopicTrendDataPointDTO(
    UUID testScoreId,
    LocalDate testDate, String testName,
    BigDecimal topicScore, BigDecimal topicMaxScore,
    BigDecimal percentage
) {}
