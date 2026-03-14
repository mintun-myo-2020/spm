package com.eggtive.spm.progress.dto;

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
    String overallTrend
) {
    public record TopicStat(
        UUID topicId,
        String topicName,
        BigDecimal averagePercentage,
        String trend
    ) {}
}
