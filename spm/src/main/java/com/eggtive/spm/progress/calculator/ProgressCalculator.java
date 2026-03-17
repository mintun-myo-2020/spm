package com.eggtive.spm.progress.calculator;

import com.eggtive.spm.common.enums.Trend;
import com.eggtive.spm.progress.dto.ImprovementVelocityDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * Abstraction for progress calculation logic.
 * Swap this implementation to change how averages, velocity, and trends are computed
 * (e.g. weighted averages, linear regression, moving averages).
 */
public interface ProgressCalculator {

    BigDecimal average(List<BigDecimal> values);

    ImprovementVelocityDTO calculateVelocity(List<BigDecimal> scoresChronological, long monthsSpan);

    /**
     * Determines the trend by comparing first-half vs second-half averages.
     */
    Trend determineTrend(List<BigDecimal> scoresChronological);
}
