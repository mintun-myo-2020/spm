package com.eggtive.spm.progress.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record TopicProgressDTO(
    UUID studentId, UUID topicId, String topicName,
    List<TopicTrendDataPointDTO> trendData,
    BigDecimal averagePercentage
) {}
