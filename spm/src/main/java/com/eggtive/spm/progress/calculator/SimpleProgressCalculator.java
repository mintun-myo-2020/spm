package com.eggtive.spm.progress.calculator;

import com.eggtive.spm.common.enums.Trend;
import com.eggtive.spm.progress.dto.ImprovementVelocityDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Simple implementation: plain averages, first-half vs second-half comparison.
 * Replace with a stats-library-backed impl for regression, weighted averages, etc.
 */
/**
 * Simple implementation: plain averages, first-half vs second-half comparison.
 * Replace with a stats-library-backed impl for regression, weighted averages, etc.
 */
/**
 * Simple implementation: plain averages, first-half vs second-half comparison.
 * Replace with a stats-library-backed impl for regression, weighted averages, etc.
 */
@Component
public class SimpleProgressCalculator implements ProgressCalculator {

    /** Minimum absolute difference (in percentage points) to count as a real change. */
    private static final BigDecimal THRESHOLD = new BigDecimal("2.00");

    @Override
    public BigDecimal average(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) return BigDecimal.ZERO;
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }

    @Override
    public ImprovementVelocityDTO calculateVelocity(List<BigDecimal> scores, long monthsSpan) {
        if (scores == null || scores.size() < 2) {
            return new ImprovementVelocityDTO(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        int mid = scores.size() / 2;
        BigDecimal firstAvg = average(scores.subList(0, mid));
        BigDecimal recentAvg = average(scores.subList(mid, scores.size()));
        BigDecimal improvement = recentAvg.subtract(firstAvg);
        BigDecimal velocity = monthsSpan > 0
            ? improvement.divide(BigDecimal.valueOf(monthsSpan), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        return new ImprovementVelocityDTO(improvement, velocity, firstAvg, recentAvg);
    }

    @Override
    public Trend determineTrend(List<BigDecimal> scores) {
        if (scores == null || scores.size() < 2) return Trend.INSUFFICIENT_DATA;
        // Compare latest score against the average of all previous scores
        BigDecimal previousAvg = average(scores.subList(0, scores.size() - 1));
        BigDecimal latest = scores.getLast();
        BigDecimal diff = latest.subtract(previousAvg);
        if (diff.compareTo(THRESHOLD) > 0) return Trend.IMPROVING;
        if (diff.compareTo(THRESHOLD.negate()) < 0) return Trend.DECLINING;
        return Trend.STABLE;
    }
}
