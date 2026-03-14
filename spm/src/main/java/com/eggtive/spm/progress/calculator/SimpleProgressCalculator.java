package com.eggtive.spm.progress.calculator;

import com.eggtive.spm.progress.dto.ImprovementVelocityDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Simple implementation: plain averages, first-half vs second-half comparison.
 * Replace with a stats-library-backed impl for regression, weighted averages, etc.
 */
@Component
public class SimpleProgressCalculator implements ProgressCalculator {

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
    public String determineTrend(List<BigDecimal> scores) {
        if (scores == null || scores.size() < 2) return "STABLE";
        int mid = scores.size() / 2;
        BigDecimal firstAvg = average(scores.subList(0, mid));
        BigDecimal recentAvg = average(scores.subList(mid, scores.size()));
        BigDecimal diff = recentAvg.subtract(firstAvg);
        // Threshold: 2% change to count as improving/declining
        if (diff.compareTo(new BigDecimal("2.00")) > 0) return "IMPROVING";
        if (diff.compareTo(new BigDecimal("-2.00")) < 0) return "DECLINING";
        return "STABLE";
    }
}
