package com.eggtive.spm.testpaper.llm;

import com.eggtive.spm.common.llm.LlmResponseUtil;
import com.eggtive.spm.common.llm.LlmService;
import com.eggtive.spm.testpaper.parser.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * Test paper extraction implementation that delegates to the shared {@link LlmService}.
 * This class owns the test-paper-specific prompt and response mapping.
 * The actual LLM call is handled by whatever LlmService implementation is active.
 *
 * <p>Activated when {@code app.extraction.type=llm} (i.e. use the shared LLM service).</p>
 */
@Component
@ConditionalOnProperty(name = "app.extraction.type", havingValue = "llm")
public class LlmExtractionService implements TestPaperExtractionService {

    private static final Logger log = LoggerFactory.getLogger(LlmExtractionService.class);

    private final LlmService llmService;
    private final JsonMapper jsonMapper;

    public LlmExtractionService(LlmService llmService, JsonMapper jsonMapper) {
        this.llmService = llmService;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public ParsedResult extractQuestions(byte[] imageBytes, String contentType, String fileName) {
        log.info("LLM extraction for '{}' ({}, {} bytes)", fileName, contentType, imageBytes.length);
        try {
            String rawResponse = llmService.completeWithImage(imageBytes, contentType, EXTRACTION_PROMPT);
            return mapToParsedResult(rawResponse);
        } catch (Exception e) {
            log.error("LLM extraction failed for '{}'", fileName, e);
            return new ParsedResult(List.of(), null,
                    List.of("LLM extraction failed: " + e.getMessage()));
        }
    }

    // ── Response mapping ────────────────────────────────────────────────

    private ParsedResult mapToParsedResult(String textContent) throws Exception {
        String jsonContent = LlmResponseUtil.extractJsonFromText(textContent);
        JsonNode data = jsonMapper.readTree(jsonContent);

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

    private ParsedQuestion mapQuestion(JsonNode node) {
        String questionNumber = textOrNull(node, "questionNumber");
        String questionText = textOrNull(node, "questionText");
        String questionType = textOrNull(node, "questionType");
        BigDecimal maxScore = decimalOrNull(node, "maxScore");
        float confidence = node.has("confidence") ? (float) node.get("confidence").asDouble() : 0.85f;

        List<McqOption> mcqOptions = new ArrayList<>();
        JsonNode optsNode = node.get("mcqOptions");
        if (optsNode != null && optsNode.isArray()) {
            for (JsonNode o : optsNode) mcqOptions.add(new McqOption(textOrNull(o, "key"), textOrNull(o, "text")));
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
                .subQuestions(subQuestions)
                .hasDiagramInQuestion(node.has("hasDiagramInQuestion") && node.get("hasDiagramInQuestion").asBoolean())
                .requiresDiagramAnswer(node.has("requiresDiagramAnswer") && node.get("requiresDiagramAnswer").asBoolean())
                .confidence(confidence)
                .build();
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode child = node.get(field);
        return (child != null && !child.isNull()) ? child.asText() : null;
    }

    private BigDecimal decimalOrNull(JsonNode node, String field) {
        JsonNode child = node.get(field);
        return (child != null && !child.isNull() && child.isNumber()) ? new BigDecimal(child.asText()) : null;
    }

    static final String EXTRACTION_PROMPT = """
            You are analysing a photograph of a student's test paper. Extract ALL questions \
            visible on this page and return them as structured JSON.

            Return ONLY valid JSON (no markdown, no explanation) with this exact schema:
            {
              "questions": [
                {
                  "questionNumber": "1",
                  "questionText": "Full question text as written",
                  "questionType": "MCQ" or "OPEN",
                  "mcqOptions": [{ "key": "A", "text": "Option text" }],
                  "maxScore": 10,
                  "hasDiagramInQuestion": false,
                  "requiresDiagramAnswer": false,
                  "subQuestions": [
                    {
                      "label": "a",
                      "questionText": "Sub-question text",
                      "maxScore": 5,
                      "studentAnswer": "What the student originally wrote (null if blank/illegible)",
                      "studentCorrection": "Green text showing the correct answer (null if none)",
                      "teacherRemarks": "Teacher's feedback or comments (null if none)",
                      "confidence": 0.85
                    }
                  ],
                  "confidence": 0.90
                }
              ],
              "notes": ["Any observations about image quality or illegible sections"]
            }

            Rules:
            - questionType is "MCQ" if the question has multiple-choice options, otherwise "OPEN"
            - For MCQ questions, include all visible options in mcqOptions
            - For MCQ answers, the student may have written the letter/number OR circled the chosen option — detect either method and record the selected option key in studentAnswer
            - For OPEN questions with parts (a, b, c or i, ii, iii), list them as subQuestions
            - studentAnswer should ONLY contain what the student originally wrote (typically blue/black ink)
            - GREEN TEXT indicates corrections — extract this into studentCorrection, NOT studentAnswer
            - teacherRemarks is for any other teacher feedback, comments, or annotations (not the correction itself)
            - hasDiagramInQuestion: true if the question includes a diagram, graph, chart, or image as part of the question itself
            - requiresDiagramAnswer: true if the student was required to draw a diagram, graph, or chart as part of their answer
            - If handwriting is illegible, set the field to null and lower the confidence
            - maxScore is the marks allocated (look for "[X marks]", "(X)", "/X" patterns)
            - confidence is your certainty about the extraction (0.0 to 1.0)
            - If you cannot determine a field, set it to null
            """;
}
