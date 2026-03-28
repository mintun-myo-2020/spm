package com.eggtive.spm.report.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Structured output from the LLM-generated strengths and improvement plan.
 * Parsed from the LLM's JSON response.
 */
public record StrengthsImprovementPlan(
    String studentName,
    String subjectName,
    List<Strength> strengths,
    List<ImprovementArea> improvementAreas,
    List<ActionItem> actionPlan,
    List<PeriodComparison> periodComparisons,
    String overallSummary
) {

    public record Strength(
        String topic,
        String description,
        String evidence
    ) {}

    public record ImprovementArea(
        String topic,
        String description,
        String evidence,
        String suggestedApproach
    ) {}

    public record ActionItem(
        int priority,
        String action,
        String targetTopic,
        String timeframe,
        String expectedOutcome,
        boolean completed
    ) {}

    /** Comparison of a topic's performance between the current and a previous period. */
    public record PeriodComparison(
        String topic,
        String previousPeriod,
        BigDecimal previousAvgPercent,
        BigDecimal currentAvgPercent,
        BigDecimal change,
        String commentary
    ) {}
}
