package com.eggtive.spm.report.service;

import com.eggtive.spm.common.enums.Trend;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Template-based HTML report generator. Produces self-contained HTML with inline CSS.
 * Marked {@code @Primary} so it's the default; a future AI implementation can override.
 */
@Service
@Primary
public class TemplateReportContentGenerator implements ReportContentGenerator {

    @Override
    public String generate(ReportData data, StrengthsImprovementPlan plan) {
        var sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">");
        sb.append("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">");
        sb.append("<title>Progress Report — ").append(esc(data.student().name())).append("</title>");
        appendStyles(sb);
        sb.append("</head><body><div class=\"container\">");

        appendHeader(sb, data);
        appendOverallSummary(sb, data);
        appendScoreTrend(sb, data);
        appendTopicPerformance(sb, data);
        appendFeedbackSummary(sb, data);
        if (plan != null) {
            appendImprovementPlan(sb, plan);
        }
        appendFooter(sb);

        sb.append("</div></body></html>");
        return sb.toString();
    }

    private void appendStyles(StringBuilder sb) {
        sb.append("<style>");
        sb.append("*{margin:0;padding:0;box-sizing:border-box}");
        sb.append("body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;");
        sb.append("color:#1f2937;background:#f9fafb;line-height:1.6}");
        sb.append(".container{max-width:800px;margin:0 auto;padding:32px 24px}");
        sb.append("h1{font-size:1.5rem;margin-bottom:4px}");
        sb.append("h2{font-size:1.15rem;margin:24px 0 12px;padding-bottom:6px;border-bottom:2px solid #e5e7eb}");
        sb.append(".meta{color:#6b7280;font-size:0.875rem;margin-bottom:20px}");
        sb.append(".summary-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:12px;margin-bottom:8px}");
        sb.append(".summary-card{background:#fff;border:1px solid #e5e7eb;border-radius:8px;padding:16px;text-align:center}");
        sb.append(".summary-card .value{font-size:1.5rem;font-weight:700;color:#1d4ed8}");
        sb.append(".summary-card .label{font-size:0.75rem;color:#6b7280;text-transform:uppercase}");
        sb.append("table{width:100%;border-collapse:collapse;margin-bottom:8px;background:#fff;border-radius:8px;overflow:hidden}");
        sb.append("th{background:#f3f4f6;text-align:left;padding:10px 12px;font-size:0.8rem;text-transform:uppercase;color:#6b7280}");
        sb.append("td{padding:10px 12px;border-top:1px solid #e5e7eb;font-size:0.875rem}");
        sb.append(".trend-up{color:#059669}.trend-down{color:#dc2626}.trend-stable{color:#6b7280}");
        sb.append(".feedback-card{background:#fff;border:1px solid #e5e7eb;border-radius:8px;padding:16px;margin-bottom:12px}");
        sb.append(".feedback-card .date{font-size:0.75rem;color:#6b7280;margin-bottom:8px}");
        sb.append(".feedback-card dt{font-weight:600;font-size:0.8rem;color:#374151;margin-top:8px}");
        sb.append(".feedback-card dd{font-size:0.875rem;margin-left:0}");
        sb.append(".footer{margin-top:32px;padding-top:16px;border-top:1px solid #e5e7eb;font-size:0.75rem;color:#9ca3af;text-align:center}");
        // Plan section styles
        sb.append(".plan-section{margin-top:24px}");
        sb.append(".plan-card{background:#fff;border:1px solid #e5e7eb;border-radius:8px;padding:16px;margin-bottom:12px}");
        sb.append(".plan-card h3{font-size:0.95rem;margin-bottom:8px;color:#1f2937}");
        sb.append(".plan-card .evidence{font-size:0.8rem;color:#6b7280;margin-top:4px;font-style:italic}");
        sb.append(".plan-card .approach{font-size:0.85rem;color:#1d4ed8;margin-top:4px}");
        sb.append(".action-list{list-style:none;counter-reset:action}");
        sb.append(".action-list li{counter-increment:action;padding:12px 16px;background:#fff;border:1px solid #e5e7eb;border-radius:8px;margin-bottom:8px}");
        sb.append(".action-list li::before{content:counter(action);font-weight:700;color:#1d4ed8;margin-right:8px}");
        sb.append(".action-meta{font-size:0.8rem;color:#6b7280;margin-top:4px}");
        sb.append(".comparison-positive{color:#059669}.comparison-negative{color:#dc2626}.comparison-neutral{color:#6b7280}");
        sb.append("</style>");
    }


    private void appendHeader(StringBuilder sb, ReportData data) {
        sb.append("<h1>Progress Report</h1>");
        sb.append("<div class=\"meta\">");
        sb.append("<strong>").append(esc(data.student().name())).append("</strong> &middot; ");
        sb.append(esc(data.classInfo().className())).append(" (").append(esc(data.classInfo().subjectName())).append(")<br>");
        sb.append("Teacher: ").append(esc(data.classInfo().teacherName())).append(" &middot; ");
        sb.append("Period: ").append(data.startDate()).append(" to ").append(data.endDate());
        sb.append("</div>");
    }

    private void appendOverallSummary(StringBuilder sb, ReportData data) {
        sb.append("<h2>Overall Summary</h2>");
        var s = data.overallSummary();
        sb.append("<div class=\"summary-grid\">");
        appendSummaryCard(sb, s.averagePercentage().toPlainString() + "%", "Average Score");
        appendSummaryCard(sb, String.valueOf(s.testCount()), "Tests Taken");
        appendSummaryCard(sb, s.trend().name(), "Trend");
        sb.append("</div>");
    }

    private void appendSummaryCard(StringBuilder sb, String value, String label) {
        sb.append("<div class=\"summary-card\"><div class=\"value\">").append(esc(value));
        sb.append("</div><div class=\"label\">").append(esc(label)).append("</div></div>");
    }

    private void appendScoreTrend(StringBuilder sb, ReportData data) {
        if (data.scores().isEmpty()) return;
        sb.append("<h2>Score Trend</h2><table><thead><tr>");
        sb.append("<th>Test</th><th>Date</th><th>Score</th><th>Max</th><th>%</th>");
        sb.append("</tr></thead><tbody>");
        for (var e : data.scores()) {
            BigDecimal pct = e.maxScore().compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                    : e.score().multiply(BigDecimal.valueOf(100)).divide(e.maxScore(), 1, java.math.RoundingMode.HALF_UP);
            sb.append("<tr><td>").append(esc(e.testName())).append("</td>");
            sb.append("<td>").append(e.testDate()).append("</td>");
            sb.append("<td>").append(e.score().toPlainString()).append("</td>");
            sb.append("<td>").append(e.maxScore().toPlainString()).append("</td>");
            sb.append("<td>").append(pct.toPlainString()).append("%</td></tr>");
        }
        sb.append("</tbody></table>");
    }

    private void appendTopicPerformance(StringBuilder sb, ReportData data) {
        if (data.topics().isEmpty()) return;
        sb.append("<h2>Topic Performance</h2><table><thead><tr>");
        sb.append("<th>Topic</th><th>Questions</th><th>Avg %</th><th>Trend</th>");
        sb.append("</tr></thead><tbody>");
        for (var t : data.topics()) {
            String trendClass = switch (t.trend()) {
                case IMPROVING -> "trend-up";
                case DECLINING -> "trend-down";
                default -> "trend-stable";
            };
            sb.append("<tr><td>").append(esc(t.topicName())).append("</td>");
            sb.append("<td>").append(t.questionCount()).append("</td>");
            sb.append("<td>").append(t.averagePercent().toPlainString()).append("%</td>");
            sb.append("<td class=\"").append(trendClass).append("\">").append(esc(t.trend().name())).append("</td></tr>");
        }
        sb.append("</tbody></table>");
    }

    private void appendFeedbackSummary(StringBuilder sb, ReportData data) {
        if (data.feedback().isEmpty()) return;
        sb.append("<h2>Teacher Feedback</h2>");
        for (var fb : data.feedback()) {
            sb.append("<div class=\"feedback-card\">");
            sb.append("<div class=\"date\">").append(fb.date()).append("</div><dl>");
            if (fb.strengths() != null && !fb.strengths().isBlank()) {
                sb.append("<dt>Strengths</dt><dd>").append(esc(fb.strengths())).append("</dd>");
            }
            if (fb.areasForImprovement() != null && !fb.areasForImprovement().isBlank()) {
                sb.append("<dt>Areas for Improvement</dt><dd>").append(esc(fb.areasForImprovement())).append("</dd>");
            }
            if (fb.recommendations() != null && !fb.recommendations().isBlank()) {
                sb.append("<dt>Recommendations</dt><dd>").append(esc(fb.recommendations())).append("</dd>");
            }
            sb.append("</dl></div>");
        }
    }

    private void appendImprovementPlan(StringBuilder sb, StrengthsImprovementPlan plan) {
        sb.append("<div class=\"plan-section\">");
        sb.append("<h2>Strengths &amp; Improvement Plan</h2>");
        sb.append("<p style=\"font-size:0.85rem;color:#6b7280;margin-bottom:16px\">")
          .append(esc(plan.overallSummary())).append("</p>");

        // Strengths
        if (!plan.strengths().isEmpty()) {
            sb.append("<h2 style=\"font-size:1rem\">Strengths</h2>");
            for (var s : plan.strengths()) {
                sb.append("<div class=\"plan-card\">");
                sb.append("<h3>").append(esc(s.topic())).append("</h3>");
                sb.append("<p>").append(esc(s.description())).append("</p>");
                if (s.evidence() != null) {
                    sb.append("<p class=\"evidence\">").append(esc(s.evidence())).append("</p>");
                }
                sb.append("</div>");
            }
        }

        // Improvement areas
        if (!plan.improvementAreas().isEmpty()) {
            sb.append("<h2 style=\"font-size:1rem\">Areas for Improvement</h2>");
            for (var ia : plan.improvementAreas()) {
                sb.append("<div class=\"plan-card\">");
                sb.append("<h3>").append(esc(ia.topic())).append("</h3>");
                sb.append("<p>").append(esc(ia.description())).append("</p>");
                if (ia.evidence() != null) {
                    sb.append("<p class=\"evidence\">").append(esc(ia.evidence())).append("</p>");
                }
                if (ia.suggestedApproach() != null) {
                    sb.append("<p class=\"approach\">Suggested: ").append(esc(ia.suggestedApproach())).append("</p>");
                }
                sb.append("</div>");
            }
        }

        // Period comparisons
        if (!plan.periodComparisons().isEmpty()) {
            sb.append("<h2 style=\"font-size:1rem\">Progress Since Previous Report</h2>");
            sb.append("<table><thead><tr><th>Topic</th><th>Previous</th><th>Current</th><th>Change</th><th>Notes</th></tr></thead><tbody>");
            for (var c : plan.periodComparisons()) {
                String changeClass = c.change() == null ? "comparison-neutral"
                    : c.change().signum() > 0 ? "comparison-positive"
                    : c.change().signum() < 0 ? "comparison-negative" : "comparison-neutral";
                String changeStr = c.change() != null
                    ? (c.change().signum() > 0 ? "+" : "") + c.change().toPlainString() + "%" : "—";
                sb.append("<tr>");
                sb.append("<td>").append(esc(c.topic())).append("</td>");
                sb.append("<td>").append(c.previousAvgPercent() != null ? c.previousAvgPercent().toPlainString() + "%" : "—").append("</td>");
                sb.append("<td>").append(c.currentAvgPercent() != null ? c.currentAvgPercent().toPlainString() + "%" : "—").append("</td>");
                sb.append("<td class=\"").append(changeClass).append("\">").append(changeStr).append("</td>");
                sb.append("<td>").append(esc(c.commentary())).append("</td>");
                sb.append("</tr>");
            }
            sb.append("</tbody></table>");
        }

        // Action plan
        if (!plan.actionPlan().isEmpty()) {
            sb.append("<h2 style=\"font-size:1rem\">Action Plan</h2>");
            sb.append("<ol class=\"action-list\">");
            for (var a : plan.actionPlan()) {
                sb.append("<li>");
                sb.append("<strong>").append(esc(a.action())).append("</strong>");
                sb.append("<div class=\"action-meta\">");
                if (a.targetTopic() != null) sb.append("Topic: ").append(esc(a.targetTopic())).append(" &middot; ");
                if (a.timeframe() != null) sb.append("Timeframe: ").append(esc(a.timeframe())).append(" &middot; ");
                if (a.expectedOutcome() != null) sb.append("Expected: ").append(esc(a.expectedOutcome()));
                sb.append("</div></li>");
            }
            sb.append("</ol>");
        }

        sb.append("</div>");
    }

    private void appendFooter(StringBuilder sb) {
        sb.append("<div class=\"footer\">Generated by Eggtive Student Progress Manager</div>");
    }

    private String esc(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;")
                   .replace(">", "&gt;").replace("\"", "&quot;");
    }

}
