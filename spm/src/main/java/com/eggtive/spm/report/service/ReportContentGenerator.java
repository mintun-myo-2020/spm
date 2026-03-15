package com.eggtive.spm.report.service;

/**
 * Abstraction for report content generation.
 * <p>
 * This sprint: {@code TemplateReportContentGenerator} produces HTML from structured data.
 * Future: An AI-powered implementation can consume the same {@link ReportData}
 * (including question-level detail, topic breakdowns, student answers) to produce
 * study plans, weakness analysis, and personalised learning recommendations.
 */
public interface ReportContentGenerator {

    /**
     * Generate report content (HTML) from assembled report data.
     *
     * @param data fully assembled report data including scores, topics, feedback, and question detail
     * @return self-contained HTML string
     */
    String generate(ReportData data);
}
