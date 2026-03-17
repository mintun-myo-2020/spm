package com.eggtive.spm.testpaper.llm;

import com.eggtive.spm.common.llm.LlmResponseUtil;
import com.eggtive.spm.testpaper.parser.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

import java.math.BigDecimal;
import java.util.*;

/**
 * Extraction implementation using Spring AI's Bedrock Converse API.
 *
 * <p>Spring AI handles all model-specific payload formatting (Claude, Nova, Titan, etc.)
 * via the Converse API — no manual JSON building or {@link ModelAdapter} needed.
 * Model selection is purely config: {@code spring.ai.bedrock.converse.chat.options.model}.</p>
 *
 * <p>Activated by {@code app.extraction.type=spring-ai}. When Spring AI reaches GA,
 * this can replace the raw-SDK {@link BedrockExtractionService} entirely.</p>
 */
@Component
@ConditionalOnProperty(name = "app.extraction.type", havingValue = "spring-ai")
public class SpringAiExtractionService implements TestPaperExtractionService {

    private static final Logger log = LoggerFactory.getLogger(SpringAiExtractionService.class);

    private final ChatClient chatClient;
    private final JsonMapper jsonMapper;

    public SpringAiExtractionService(ChatModel chatModel, JsonMapper jsonMapper) {
        this.chatClient = ChatClient.create(chatModel);
        this.jsonMapper = jsonMapper;
        log.info("Spring AI extraction configured — model managed by spring.ai.bedrock.converse properties");
    }

    @Override
    public ParsedResult extractQuestions(byte[] imageBytes, String contentType, String fileName) {
        log.info("Spring AI extraction for '{}' ({}, {} bytes)", fileName, contentType, imageBytes.length);
        try {
            MimeType mimeType = contentType != null
                    ? MimeType.valueOf(contentType)
                    : MimeType.valueOf("image/jpeg");

            String textContent = chatClient.prompt()
                    .user(u -> u.text(BedrockExtractionService.EXTRACTION_PROMPT)
                            .media(mimeType, new ByteArrayResource(imageBytes)))
                    .call()
                    .content();

            return mapToParsedResult(textContent);

        } catch (Exception e) {
            log.error("Spring AI extraction failed for '{}'", fileName, e);
            return new ParsedResult(List.of(), null,
                    List.of("Spring AI extraction failed: " + e.getMessage()));
        }
    }

    // ── Response mapping (reuses same logic as BedrockExtractionService) ─

    private ParsedResult mapToParsedResult(String textContent) throws Exception {
        String jsonContent = extractJsonFromText(textContent);
        JsonNode data = jsonMapper.readTree(jsonContent);

        List<ParsedQuestion> questions = new ArrayList<>();
        BigDecimal totalMarks = BigDecimal.ZERO;
        List<String> notes = new ArrayList<>();

        JsonNode questionsNode = data.get("questions");
        if (questionsNode != null && questionsNode.isArray()) {
            for (JsonNode qNode : questionsNode) {
                ParsedQuestion pq = mapQuestion(qNode);
                questions.add(pq);
                if (pq.maxScore() != null) {
                    totalMarks = totalMarks.add(pq.maxScore());
                }
            }
        }

        JsonNode notesNode = data.get("notes");
        if (notesNode != null && notesNode.isArray()) {
            for (JsonNode n : notesNode) {
                notes.add(n.asString());
            }
        }

        return new ParsedResult(questions,
                totalMarks.compareTo(BigDecimal.ZERO) > 0 ? totalMarks : null,
                notes);
    }

    private ParsedQuestion mapQuestion(JsonNode node) {
        String questionNumber = textOrNull(node, "questionNumber");
        String questionText = textOrNull(node, "questionText");
        String questionType = textOrNull(node, "questionType");
        BigDecimal maxScore = decimalOrNull(node);
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
                        .maxScore(decimalOrNull(s))
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

    private String extractJsonFromText(String text) {
        return LlmResponseUtil.extractJsonFromText(text);
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode child = node.get(field);
        return (child != null && !child.isNull()) ? child.asString() : null;
    }

    private BigDecimal decimalOrNull(JsonNode node) {
        JsonNode child = node.get("maxScore");
        return (child != null && !child.isNull() && child.isNumber())
                ? new BigDecimal(child.asString()) : null;
    }
}
