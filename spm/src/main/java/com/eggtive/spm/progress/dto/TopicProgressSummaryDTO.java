package com.eggtive.spm.progress.dto;

import com.eggtive.spm.common.enums.Trend;

import java.math.BigDecimal;
import java.util.UUID;

public record TopicProgressSummaryDTO(
    UUID topicId, String topicName,
    int testCount, int questionCount,
    BigDecimal averagePercentage,
    BigDecimal latestPercentage, Trend trend
) {}
