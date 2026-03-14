package com.eggtive.spm.progress.dto;

import java.math.BigDecimal;

public record ImprovementVelocityDTO(
    BigDecimal improvement,
    BigDecimal velocityPerMonth,
    BigDecimal firstAverage,
    BigDecimal recentAverage
) {}
