package com.eggtive.spm.testpaper.llm;

import com.eggtive.spm.common.llm.LlmService;
import com.eggtive.spm.testpaper.parser.*;
import tools.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

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
    public ParsedResult extractQuestions(byte[] imageBytes, String contentType, String fileName, List<String> topicNames) {
        log.info("LLM extraction for '{}' ({}, {} bytes)", fileName, contentType, imageBytes.length);
        try {
            String prompt = BedrockExtractionService.buildPrompt(topicNames);
            String rawResponse = llmService.completeWithImage(imageBytes, contentType, prompt);
            return ExtractionResponseMapper.map(rawResponse, jsonMapper);
        } catch (Exception e) {
            log.error("LLM extraction failed for '{}'", fileName, e);
            return new ParsedResult(List.of(), null,
                    List.of("LLM extraction failed: " + e.getMessage()));
        }
    }
}
