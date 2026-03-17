package com.eggtive.spm.common.enums;

/**
 * Describes the direction of a student's performance over time.
 *
 * <p>Trend is determined by comparing the average of the first half of scores
 * against the average of the second half. A threshold prevents noise from
 * being misinterpreted as a real change.</p>
 */
public enum Trend {
    /** Second-half average is meaningfully higher than first-half average. */
    IMPROVING,
    /** Second-half average is meaningfully lower than first-half average. */
    DECLINING,
    /** Difference between halves is within the noise threshold. */
    STABLE,
    /** Not enough data points to determine a trend (fewer than 2 scores). */
    INSUFFICIENT_DATA
}
