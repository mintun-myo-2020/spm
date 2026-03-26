package com.eggtive.spm.report.service;

/**
 * Abstraction for report content generation.
 * <p>
 * {@code TemplateReportContentGenerator} produces HTML from structured data,
 * optionally including an LLM-generated strengths and improvement plan.
 */
public interface ReportContentGenerator {

    /**
     * Generate report content (HTML) from assembled report data, without a plan.
     */
    default String generate(ReportData data) {
        return generate(data, null);
    }

    /**
     * Generate report content (HTML) from assembled report data,
     * optionally including a strengths and improvement plan.
     *
     * @param data fully assembled report data
     * @param plan LLM-generated plan, or null to omit the plan section
     * @return self-contained HTML string
     */
    String generate(ReportData data, StrengthsImprovementPlan plan);
}
