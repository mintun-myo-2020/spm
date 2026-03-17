package com.eggtive.spm.progress.dto;

import com.eggtive.spm.common.enums.Trend;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ClassSummaryDTO(
    UUID classId,
    int studentCount,
    int testCount,
    BigDecimal meanScore,
    BigDecimal medianScore,
    TopicStat strongestTopic,
    TopicStat weakestTopic,
    List<TopicStat> topicStats,
    Trend overallTrend
) {
    public record TopicStat(
        UUID topicId,
        String topicName,
        BigDecimal averagePercentage,
        Trend trend
    ) {}
}
