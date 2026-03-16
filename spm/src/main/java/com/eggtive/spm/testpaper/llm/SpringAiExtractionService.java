package com.eggtive.spm.testpaper.llm;

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
                notes.add(n.asText());
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
        BigDecimal maxScore = decimalOrNull(node, "maxScore");
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
                subQuestions.add(new ParsedSubQuestion(
                        textOrNull(s, "label"),
                        textOrNull(s, "questionText"),
                        decimalOrNull(s, "maxScore"),
                        textOrNull(s, "studentAnswer"),
                        s.has("confidence") ? (float) s.get("confidence").asDouble() : 0.80f
                ));
            }
        }

        return new ParsedQuestion(questionNumber, questionText,
                questionType != null ? questionType : "OPEN",
                mcqOptions, maxScore, subQuestions, confidence, null);
    }

    private String extractJsonFromText(String text) {
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

    private String textOrNull(JsonNode node, String field) {
        JsonNode child = node.get(field);
        return (child != null && !child.isNull()) ? child.asText() : null;
    }

    private BigDecimal decimalOrNull(JsonNode node, String field) {
        JsonNode child = node.get(field);
        return (child != null && !child.isNull() && child.isNumber())
                ? new BigDecimal(child.asText()) : null;
    }
}
