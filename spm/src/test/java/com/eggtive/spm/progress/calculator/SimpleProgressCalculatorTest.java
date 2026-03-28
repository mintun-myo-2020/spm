package com.eggtive.spm.progress.calculator;

import com.eggtive.spm.common.enums.Trend;
import com.eggtive.spm.progress.dto.ImprovementVelocityDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class SimpleProgressCalculatorTest {

    private final SimpleProgressCalculator calculator = new SimpleProgressCalculator();

    // --- average tests ---

    @Test
    void average_emptyList_returnsZero() {
        assertThat(calculator.average(List.of())).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void average_nullList_returnsZero() {
        assertThat(calculator.average(null)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void average_singleValue_returnsThatValue() {
        assertThat(calculator.average(List.of(new BigDecimal("75.00"))))
            .isEqualByComparingTo(new BigDecimal("75.00"));
    }

    @Test
    void average_multipleValues_calculatesCorrectly() {
        var values = List.of(new BigDecimal("60"), new BigDecimal("80"), new BigDecimal("100"));
        assertThat(calculator.average(values)).isEqualByComparingTo(new BigDecimal("80.00"));
    }

    // --- calculateVelocity tests ---

    @Test
    void calculateVelocity_lessThanTwoScores_returnsZeros() {
        ImprovementVelocityDTO result = calculator.calculateVelocity(List.of(new BigDecimal("50")), 3);
        assertThat(result.improvement()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.velocityPerMonth()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void calculateVelocity_improvingScores_positiveVelocity() {
        var scores = List.of(new BigDecimal("50"), new BigDecimal("60"), new BigDecimal("70"), new BigDecimal("80"));
        ImprovementVelocityDTO result = calculator.calculateVelocity(scores, 4);

        // First half avg: (50+60)/2 = 55, Recent half avg: (70+80)/2 = 75
        assertThat(result.firstAverage()).isEqualByComparingTo(new BigDecimal("55.00"));
        assertThat(result.recentAverage()).isEqualByComparingTo(new BigDecimal("75.00"));
        assertThat(result.improvement()).isEqualByComparingTo(new BigDecimal("20.00"));
        // velocity = 20/4 = 5
        assertThat(result.velocityPerMonth()).isEqualByComparingTo(new BigDecimal("5.00"));
    }

    @Test
    void calculateVelocity_decliningScores_negativeImprovement() {
        var scores = List.of(new BigDecimal("90"), new BigDecimal("80"), new BigDecimal("60"), new BigDecimal("50"));
        ImprovementVelocityDTO result = calculator.calculateVelocity(scores, 2);

        assertThat(result.improvement()).isNegative();
    }

    // --- determineTrend tests ---

    @Test
    void determineTrend_lessThanTwoScores_returnsInsufficientData() {
        assertThat(calculator.determineTrend(List.of(new BigDecimal("50")))).isEqualTo(Trend.INSUFFICIENT_DATA);
        assertThat(calculator.determineTrend(null)).isEqualTo(Trend.INSUFFICIENT_DATA);
    }

    @Test
    void determineTrend_improvingScores_returnsImproving() {
        // Previous avg = 50, latest = 60, diff = 10 > threshold(2)
        var scores = List.of(new BigDecimal("50"), new BigDecimal("60"));
        assertThat(calculator.determineTrend(scores)).isEqualTo(Trend.IMPROVING);
    }

    @Test
    void determineTrend_decliningScores_returnsDeclining() {
        // Previous avg = 80, latest = 60, diff = -20 < -threshold
        var scores = List.of(new BigDecimal("80"), new BigDecimal("60"));
        assertThat(calculator.determineTrend(scores)).isEqualTo(Trend.DECLINING);
    }

    @Test
    void determineTrend_stableScores_returnsStable() {
        // Previous avg = 75, latest = 76, diff = 1 < threshold(2)
        var scores = List.of(new BigDecimal("75"), new BigDecimal("76"));
        assertThat(calculator.determineTrend(scores)).isEqualTo(Trend.STABLE);
    }

    @Test
    void determineTrend_multipleScores_comparesLatestVsPreviousAverage() {
        // Previous avg = (50+60+70)/3 = 60, latest = 80, diff = 20 > threshold
        var scores = List.of(new BigDecimal("50"), new BigDecimal("60"), new BigDecimal("70"), new BigDecimal("80"));
        assertThat(calculator.determineTrend(scores)).isEqualTo(Trend.IMPROVING);
    }
}
