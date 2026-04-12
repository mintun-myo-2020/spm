package com.eggtive.spm.testpaper.llm;

import com.eggtive.spm.testpaper.parser.*;
import tools.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

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
    public ParsedResult extractQuestions(byte[] imageBytes, String contentType, String fileName, List<String> topicNames) {
        log.info("Spring AI extraction for '{}' ({}, {} bytes)", fileName, contentType, imageBytes.length);
        try {
            MimeType mimeType = contentType != null
                    ? MimeType.valueOf(contentType)
                    : MimeType.valueOf("image/jpeg");

            String prompt = BedrockExtractionService.buildPrompt(topicNames);
            String textContent = chatClient.prompt()
                    .user(u -> u.text(prompt)
                            .media(mimeType, new ByteArrayResource(imageBytes)))
                    .call()
                    .content();

            return ExtractionResponseMapper.map(textContent, jsonMapper);

        } catch (Exception e) {
            log.error("Spring AI extraction failed for '{}'", fileName, e);
            return new ParsedResult(List.of(), null,
                    List.of("Spring AI extraction failed: " + e.getMessage()));
        }
    }
}
