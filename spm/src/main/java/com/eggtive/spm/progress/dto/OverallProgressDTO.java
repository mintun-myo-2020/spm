package com.eggtive.spm.progress.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OverallProgressDTO(
    UUID studentId, String studentName,
    List<TrendDataPointDTO> trendData,
    BigDecimal averageScore,
    ImprovementVelocityDTO improvementVelocity
) {}
