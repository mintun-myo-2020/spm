package com.eggtive.spm.progress.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TopicProgressSummaryDTO(
    UUID topicId, String topicName,
    int testCount, BigDecimal averagePercentage,
    BigDecimal latestPercentage, String trend
) {}
