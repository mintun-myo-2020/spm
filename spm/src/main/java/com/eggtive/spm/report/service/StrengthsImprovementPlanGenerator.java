package com.eggtive.spm.report.service;

import com.eggtive.spm.common.llm.LlmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates a structured strengths and improvement plan by sending assembled
 * report data to an LLM. Uses teacher feedback (overall + per-question remarks),
 * topic performance trends, and question-level detail to produce actionable insights.
 * Optionally includes previous period data for comparison.
 */
@Service
public class StrengthsImprovementPlanGenerator {

    private static final Logger log = LoggerFactory.getLogger(StrengthsImprovementPlanGenerator.class);

    private final LlmService llmService;
    private final JsonMapper jsonMapper;

    public StrengthsImprovementPlanGenerator(LlmService llmService, JsonMapper jsonMapper) {
        this.llmService = llmService;
        this.jsonMapper = jsonMapper;
        log.info("LLM service active: {}", llmService.getClass().getSimpleName());
    }

    /**
     * Generate a plan without previous period comparison.
     */
    public StrengthsImprovementPlan generate(ReportData data) {
        return generate(data, List.of());
    }

    /**
     * Generate a plan with optional previous period data for comparison.
     *
     * @param data           current period report data
     * @param previousPeriods assembled data from selected previous reports (may be empty)
     */
    public StrengthsImprovementPlan generate(ReportData data, List<ReportData> previousPeriods) {
        String prompt = buildPrompt(data, previousPeriods);
        log.debug("Generating strengths/improvement plan — prompt: {} chars, previous periods: {}",
                prompt.length(), previousPeriods.size());

        try {
            String response = llmService.complete(prompt);
            return parseResponse(response, data);
        } catch (Exception e) {
            log.error("LLM plan generation failed, returning fallback", e);
            return buildFallbackPlan(data);
        }
    }

    // ── Prompt construction ─────────────────────────────────────────────

    String buildPrompt(ReportData data, List<ReportData> previousPeriods) {
        var sb = new StringBuilder();
        sb.append(SYSTEM_INSTRUCTION);

        if (!previousPeriods.isEmpty()) {
            sb.append(COMPARISON_INSTRUCTION);
        }

        sb.append("\n\n");

        // Student context
        sb.append("## Student Context\n");
        sb.append("Student: ").append(data.student().name()).append("\n");
        sb.append("Subject: ").append(data.classInfo().subjectName()).append("\n");
        sb.append("Class: ").append(data.classInfo().className()).append("\n");
        sb.append("Period: ").append(data.startDate()).append(" to ").append(data.endDate()).append("\n");
        sb.append("Overall Average: ").append(data.overallSummary().averagePercentage()).append("%\n");
        sb.append("Overall Trend: ").append(data.overallSummary().trend()).append("\n");
        sb.append("Tests Taken: ").append(data.overallSummary().testCount()).append("\n\n");

        // Topic performance summary
        appendTopicPerformance(sb, data);

        // Teacher's overall feedback on tests
        appendTeacherFeedback(sb, data);

        // Question-level detail with per-question teacher remarks
        appendQuestionDetail(sb, data);

        // Previous period comparisons
        if (!previousPeriods.isEmpty()) {
            appendPreviousPeriods(sb, previousPeriods);
        }

        sb.append("\n").append(previousPeriods.isEmpty() ? OUTPUT_INSTRUCTION : OUTPUT_INSTRUCTION_WITH_COMPARISON);
        return sb.toString();
    }

    private void appendTopicPerformance(StringBuilder sb, ReportData data) {
        if (data.topics().isEmpty()) return;
        sb.append("## Topic Performance (Strengths & Weaknesses)\n");
        for (var t : data.topics()) {
            sb.append("- ").append(t.topicName())
              .append(": avg ").append(t.averagePercent()).append("%, ")
              .append(t.questionCount()).append(" questions, trend: ").append(t.trend())
              .append("\n");
        }
        sb.append("\n");
    }

    private void appendTeacherFeedback(StringBuilder sb, ReportData data) {
        if (data.feedback().isEmpty()) return;
        sb.append("## Teacher's Overall Feedback on Tests\n");
        for (var fb : data.feedback()) {
            sb.append("### Feedback (").append(fb.date()).append(")\n");
            if (fb.strengths() != null && !fb.strengths().isBlank()) {
                sb.append("Strengths: ").append(fb.strengths()).append("\n");
            }
            if (fb.areasForImprovement() != null && !fb.areasForImprovement().isBlank()) {
                sb.append("Areas for Improvement: ").append(fb.areasForImprovement()).append("\n");
            }
            if (fb.recommendations() != null && !fb.recommendations().isBlank()) {
                sb.append("Recommendations: ").append(fb.recommendations()).append("\n");
            }
            sb.append("\n");
        }
    }

    private void appendQuestionDetail(StringBuilder sb, ReportData data) {
        if (data.testDetails().isEmpty()) return;
        sb.append("## Question-Level Detail with Teacher Remarks\n");
        for (var test : data.testDetails()) {
            sb.append("### ").append(test.testName()).append(" (").append(test.testDate()).append(")\n");
            for (var q : test.questions()) {
                sb.append("Q").append(q.questionNumber());
                if (q.questionText() != null) {
                    sb.append(": ").append(truncate(q.questionText(), 100));
                }
                sb.append(" [max: ").append(q.maxScore()).append("]\n");
                for (var sq : q.subQuestions()) {
                    sb.append("  ").append(sq.label()).append(") ");
                    sb.append("[").append(sq.topicName()).append("] ");
                    sb.append(sq.score()).append("/").append(sq.maxScore());
                    if (sq.teacherRemarks() != null && !sq.teacherRemarks().isBlank()) {
                        sb.append(" — Teacher: \"").append(truncate(sq.teacherRemarks(), 150)).append("\"");
                    }
                    if (sq.studentAnswer() != null && !sq.studentAnswer().isBlank()) {
                        sb.append(" | Student wrote: \"").append(truncate(sq.studentAnswer(), 100)).append("\"");
                    }
                    sb.append("\n");
                }
            }
            sb.append("\n");
        }
    }

    private void appendPreviousPeriods(StringBuilder sb, List<ReportData> previousPeriods) {
        sb.append("## Previous Report Periods (for comparison)\n");
        sb.append("The teacher has selected the following previous reports for comparison.\n");
        sb.append("Use these to identify improvement, decline, or stagnation per topic.\n\n");

        for (int i = 0; i < previousPeriods.size(); i++) {
            var prev = previousPeriods.get(i);
            sb.append("### Previous Period ").append(i + 1).append(": ")
              .append(prev.startDate()).append(" to ").append(prev.endDate()).append("\n");
            sb.append("Overall Average: ").append(prev.overallSummary().averagePercentage()).append("%\n");
            sb.append("Overall Trend: ").append(prev.overallSummary().trend()).append("\n");
            sb.append("Tests Taken: ").append(prev.overallSummary().testCount()).append("\n");

            if (!prev.topics().isEmpty()) {
                sb.append("Topic Performance:\n");
                for (var t : prev.topics()) {
                    sb.append("- ").append(t.topicName())
                      .append(": avg ").append(t.averagePercent()).append("%, ")
                      .append(t.questionCount()).append(" questions, trend: ").append(t.trend())
                      .append("\n");
                }
            }

            // Include previous feedback summaries (not question-level detail — too verbose)
            if (!prev.feedback().isEmpty()) {
                sb.append("Teacher Feedback Summary:\n");
                for (var fb : prev.feedback()) {
                    sb.append("- (").append(fb.date()).append(") ");
                    if (fb.strengths() != null && !fb.strengths().isBlank()) {
                        sb.append("Strengths: ").append(truncate(fb.strengths(), 200)).append(" ");
                    }
                    if (fb.areasForImprovement() != null && !fb.areasForImprovement().isBlank()) {
                        sb.append("Improve: ").append(truncate(fb.areasForImprovement(), 200));
                    }
                    sb.append("\n");
                }
            }
            sb.append("\n");
        }
    }

    // ── Response parsing ────────────────────────────────────────────────

    private StrengthsImprovementPlan parseResponse(String response, ReportData data) throws Exception {
        String json = extractJson(response);
        JsonNode root = jsonMapper.readTree(json);

        List<StrengthsImprovementPlan.Strength> strengths = new ArrayList<>();
        JsonNode strengthsNode = root.get("strengths");
        if (strengthsNode != null && strengthsNode.isArray()) {
            for (JsonNode s : strengthsNode) {
                strengths.add(new StrengthsImprovementPlan.Strength(
                    text(s, "topic"), text(s, "description"), text(s, "evidence")));
            }
        }

        List<StrengthsImprovementPlan.ImprovementArea> improvements = new ArrayList<>();
        JsonNode improvementsNode = root.get("improvementAreas");
        if (improvementsNode != null && improvementsNode.isArray()) {
            for (JsonNode i : improvementsNode) {
                improvements.add(new StrengthsImprovementPlan.ImprovementArea(
                    text(i, "topic"), text(i, "description"),
                    text(i, "evidence"), text(i, "suggestedApproach")));
            }
        }

        // If the LLM returned an empty/useless response, fall back to data-driven plan
        if (strengths.isEmpty() && improvements.isEmpty()) {
            log.warn("LLM returned empty plan, falling back to data-driven plan");
            return buildFallbackPlan(data);
        }

        List<StrengthsImprovementPlan.ActionItem> actionPlan = new ArrayList<>();
        JsonNode actionNode = root.get("actionPlan");
        if (actionNode != null && actionNode.isArray()) {
            for (JsonNode a : actionNode) {
                actionPlan.add(new StrengthsImprovementPlan.ActionItem(
                    a.has("priority") ? a.get("priority").asInt() : 0,
                    text(a, "action"), text(a, "targetTopic"),
                    text(a, "timeframe"), text(a, "expectedOutcome")));
            }
        }

        List<StrengthsImprovementPlan.PeriodComparison> comparisons = new ArrayList<>();
        JsonNode compNode = root.get("periodComparisons");
        if (compNode != null && compNode.isArray()) {
            for (JsonNode c : compNode) {
                comparisons.add(new StrengthsImprovementPlan.PeriodComparison(
                    text(c, "topic"), text(c, "previousPeriod"),
                    decimal(c, "previousAvgPercent"), decimal(c, "currentAvgPercent"),
                    decimal(c, "change"), text(c, "commentary")));
            }
        }

        String summary = text(root, "overallSummary");

        return new StrengthsImprovementPlan(
            data.student().name(), data.classInfo().subjectName(),
            strengths, improvements, actionPlan, comparisons,
            summary != null ? summary : "Plan generated successfully.");
    }

    private StrengthsImprovementPlan buildFallbackPlan(ReportData data) {
        var strengths = data.topics().stream()
            .filter(t -> t.averagePercent().compareTo(new BigDecimal("70")) >= 0)
            .map(t -> new StrengthsImprovementPlan.Strength(
                t.topicName(),
                "Performing well with " + t.averagePercent() + "% average",
                t.questionCount() + " questions across " + data.overallSummary().testCount() + " tests"))
            .toList();

        var improvementTopics = data.topics().stream()
            .filter(t -> t.averagePercent().compareTo(new BigDecimal("70")) < 0)
            .toList();

        var improvements = improvementTopics.stream()
            .map(t -> new StrengthsImprovementPlan.ImprovementArea(
                t.topicName(),
                "Below target with " + t.averagePercent() + "% average",
                t.questionCount() + " questions assessed",
                "Focus on practice exercises for this topic"))
            .toList();

        // Generate action items from improvement areas
        var actionPlan = new ArrayList<StrengthsImprovementPlan.ActionItem>();
        int priority = 1;
        for (var t : improvementTopics) {
            actionPlan.add(new StrengthsImprovementPlan.ActionItem(
                priority++,
                "Practice " + t.topicName() + " with targeted exercises focusing on weak sub-topics",
                t.topicName(),
                "Next 2 weeks",
                "Improve " + t.topicName() + " average above 70%"));
        }
        // Add a general action item for maintaining strengths
        if (!strengths.isEmpty()) {
            actionPlan.add(new StrengthsImprovementPlan.ActionItem(
                priority,
                "Continue regular revision of strong topics to maintain performance",
                null,
                "Ongoing",
                "Maintain current averages in strong topics"));
        }

        String summary = String.format(
            "%s is averaging %.1f%% overall in %s. %s",
            data.student().name(),
            data.overallSummary().averagePercentage().doubleValue(),
            data.classInfo().subjectName(),
            improvements.isEmpty()
                ? "All topics are above target — keep up the good work."
                : improvements.size() + " topic(s) need attention to reach the 70% target.");

        return new StrengthsImprovementPlan(
            data.student().name(), data.classInfo().subjectName(),
            strengths, improvements, actionPlan, List.of(), summary);
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private String extractJson(String text) {
        if (text == null || text.isBlank()) return "{}";
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNewline > 0 && lastFence > firstNewline) {
                trimmed = trimmed.substring(firstNewline + 1, lastFence).trim();
            }
        }
        return trimmed;
    }

    private String text(JsonNode node, String field) {
        JsonNode child = node.get(field);
        return (child != null && !child.isNull()) ? child.asText() : null;
    }

    private BigDecimal decimal(JsonNode node, String field) {
        JsonNode child = node.get(field);
        return (child != null && !child.isNull() && child.isNumber())
                ? new BigDecimal(child.asText()) : null;
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "…";
    }

    // ── Prompt templates ────────────────────────────────────────────────

    static final String SYSTEM_INSTRUCTION = """
            You are an experienced education analyst helping a tuition teacher create a \
            personalised strengths and improvement plan for a student. You will be given:

            1. **Topic Performance**: Average scores and trends per topic (from test data)
            2. **Teacher's Overall Feedback**: The teacher's written feedback on each test \
               (strengths, areas for improvement, recommendations)
            3. **Question-Level Detail**: Per-question scores, the student's actual answers, \
               and the teacher's remarks on individual questions/sub-questions

            Your job is to synthesise ALL three inputs into a coherent, actionable plan. \
            Specifically:
            - Identify genuine strengths backed by both quantitative data AND teacher observations
            - Identify improvement areas where low scores AND teacher remarks converge
            - Create a prioritised action plan with concrete, specific steps
            - Use the teacher's own language and observations as evidence where possible
            - Be encouraging but honest — parents and students will read this

            IMPORTANT:
            - Do NOT invent information. Only reference topics, scores, and feedback actually provided.
            - If teacher remarks mention specific misconceptions or errors, incorporate those.
            - Prioritise improvement areas by impact (low score + declining trend = highest priority).
            - Keep language clear and accessible for parents who may not be subject experts.""";

    static final String COMPARISON_INSTRUCTION = """

            4. **Previous Report Periods**: The teacher has included data from previous reporting \
               periods. You MUST compare current performance against these previous periods and:
            - Highlight topics where the student has improved (with specific % changes)
            - Flag topics where performance has declined or stagnated
            - Note whether previous teacher recommendations have been addressed
            - Reference previous feedback when it's relevant to current observations
            - Use phrases like "improved from X% to Y%" or "previously noted as a weakness, now showing progress"
            - If a topic was flagged for improvement before and hasn't improved, escalate its priority""";

    static final String OUTPUT_INSTRUCTION = """
            Return ONLY valid JSON (no markdown fences, no explanation) with this exact schema:
            {
              "strengths": [
                {
                  "topic": "Topic name",
                  "description": "What the student does well in this area",
                  "evidence": "Specific data points or teacher quotes supporting this"
                }
              ],
              "improvementAreas": [
                {
                  "topic": "Topic name",
                  "description": "What needs improvement and why",
                  "evidence": "Specific scores, trends, or teacher remarks",
                  "suggestedApproach": "Concrete study strategy or exercise type"
                }
              ],
              "actionPlan": [
                {
                  "priority": 1,
                  "action": "Specific actionable step",
                  "targetTopic": "Which topic this addresses",
                  "timeframe": "e.g. 'Next 2 weeks', 'Ongoing'",
                  "expectedOutcome": "What improvement to expect"
                }
              ],
              "periodComparisons": [],
              "overallSummary": "2-3 sentence summary of the student's position and path forward"
            }

            Rules:
            - Include 2-5 strengths (only topics with genuine evidence of competence)
            - Include 1-5 improvement areas (prioritised by severity)
            - Include 3-7 action items (ordered by priority, most urgent first)
            - periodComparisons should be an empty array (no previous data provided)
            - Every strength and improvement MUST reference specific evidence from the data provided
            - Action items must be concrete and achievable, not generic advice""";

    static final String OUTPUT_INSTRUCTION_WITH_COMPARISON = """
            Return ONLY valid JSON (no markdown fences, no explanation) with this exact schema:
            {
              "strengths": [
                {
                  "topic": "Topic name",
                  "description": "What the student does well, referencing change from previous periods",
                  "evidence": "Specific data points, teacher quotes, and comparison with previous periods"
                }
              ],
              "improvementAreas": [
                {
                  "topic": "Topic name",
                  "description": "What needs improvement, noting if this is a recurring or new issue",
                  "evidence": "Current and previous scores, trends, or teacher remarks",
                  "suggestedApproach": "Concrete study strategy, adjusted based on what has/hasn't worked before"
                }
              ],
              "actionPlan": [
                {
                  "priority": 1,
                  "action": "Specific actionable step",
                  "targetTopic": "Which topic this addresses",
                  "timeframe": "e.g. 'Next 2 weeks', 'Ongoing'",
                  "expectedOutcome": "What improvement to expect, informed by rate of past progress"
                }
              ],
              "periodComparisons": [
                {
                  "topic": "Topic name",
                  "previousPeriod": "e.g. '2025-01-01 to 2025-03-31'",
                  "previousAvgPercent": 52.0,
                  "currentAvgPercent": 71.0,
                  "change": 19.0,
                  "commentary": "Brief note on what changed and why"
                }
              ],
              "overallSummary": "2-3 sentence summary referencing progress since previous periods"
            }

            Rules:
            - Include 2-5 strengths (reference previous period data where relevant)
            - Include 1-5 improvement areas (flag recurring issues from previous periods with higher priority)
            - Include 3-7 action items (adjust strategies if previous recommendations didn't work)
            - Include periodComparisons for EVERY topic that appears in both current and previous data
            - change = currentAvgPercent - previousAvgPercent (positive = improvement)
            - If multiple previous periods exist, compare against the most recent one for periodComparisons
            - Every strength and improvement MUST reference specific evidence from the data provided
            - Action items must be concrete and achievable, not generic advice""";
}
