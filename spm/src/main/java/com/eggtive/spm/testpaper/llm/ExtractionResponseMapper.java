package com.eggtive.spm.testpaper.llm;

import com.eggtive.spm.common.llm.LlmResponseUtil;
import com.eggtive.spm.testpaper.parser.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared mapper that converts raw LLM JSON text into {@link ParsedResult}.
 * Used by all extraction service implementations to avoid duplicating
 * the JSON → domain mapping logic.
 */
public final class ExtractionResponseMapper {

    private static final Logger log = LoggerFactory.getLogger(ExtractionResponseMapper.class);

    private ExtractionResponseMapper() {}

    public static ParsedResult map(String textContent, JsonMapper jsonMapper) throws Exception {
        String json = LlmResponseUtil.extractJsonFromText(textContent);
        log.info("LLM extraction raw JSON: {}", json);
        JsonNode data = jsonMapper.readTree(json);

        List<ParsedQuestion> questions = new ArrayList<>();
        BigDecimal totalMarks = BigDecimal.ZERO;
        List<String> notes = new ArrayList<>();

        JsonNode questionsNode = data.get("questions");
        if (questionsNode != null && questionsNode.isArray()) {
            for (JsonNode qNode : questionsNode) {
                ParsedQuestion pq = mapQuestion(qNode);
                questions.add(pq);
                if (pq.maxScore() != null) totalMarks = totalMarks.add(pq.maxScore());
            }
        }

        JsonNode notesNode = data.get("notes");
        if (notesNode != null && notesNode.isArray()) {
            for (JsonNode n : notesNode) notes.add(n.asText());
        }

        return new ParsedResult(questions,
                totalMarks.compareTo(BigDecimal.ZERO) > 0 ? totalMarks : null, notes);
    }

    private static ParsedQuestion mapQuestion(JsonNode node) {
        String questionNumber = textOrNull(node, "questionNumber");
        String questionText = textOrNull(node, "questionText");
        String questionType = textOrNull(node, "questionType");
        BigDecimal maxScore = decimalOrNull(node, "maxScore");
        String studentAnswer = textOrNull(node, "studentAnswer");
        String topicHint = textOrNull(node, "topicHint");
        float confidence = node.has("confidence")
                ? (float) node.get("confidence").asDouble() : 0.85f;

        List<McqOption> mcqOptions = new ArrayList<>();
        JsonNode optsNode = node.get("mcqOptions");
        if (optsNode != null && optsNode.isArray()) {
            for (JsonNode o : optsNode) {
                mcqOptions.add(new McqOption(textOrNull(o, "key"), textOrNull(o, "text")));
            }
        }

        List<ParsedSubQuestion> subQuestions = new ArrayList<>();
        JsonNode subsNode = node.get("subQuestions");
        if (subsNode != null && subsNode.isArray()) {
            for (JsonNode s : subsNode) {
                subQuestions.add(ParsedSubQuestion.defaults()
                        .label(textOrNull(s, "label"))
                        .questionText(textOrNull(s, "questionText"))
                        .maxScore(decimalOrNull(s, "maxScore"))
                        .studentAnswer(textOrNull(s, "studentAnswer"))
                        .studentCorrection(textOrNull(s, "studentCorrection"))
                        .teacherRemarks(textOrNull(s, "teacherRemarks"))
                        .confidence(s.has("confidence") ? (float) s.get("confidence").asDouble() : 0.80f)
                        .build());
            }
        }

        return ParsedQuestion.defaults()
                .questionNumber(questionNumber)
                .questionText(questionText)
                .questionType(questionType != null ? questionType : "OPEN")
                .mcqOptions(mcqOptions)
                .maxScore(maxScore)
                .studentAnswer(studentAnswer)
                .topicHint(topicHint)
                .subQuestions(subQuestions)
                .hasDiagramInQuestion(node.has("hasDiagramInQuestion") && node.get("hasDiagramInQuestion").asBoolean())
                .requiresDiagramAnswer(node.has("requiresDiagramAnswer") && node.get("requiresDiagramAnswer").asBoolean())
                .confidence(confidence)
                .build();
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode child = node.get(field);
        return (child != null && !child.isNull()) ? child.asText() : null;
    }

    private static BigDecimal decimalOrNull(JsonNode node, String field) {
        JsonNode child = node.get(field);
        return (child != null && !child.isNull() && child.isNumber())
                ? new BigDecimal(child.asText()) : null;
    }
}
