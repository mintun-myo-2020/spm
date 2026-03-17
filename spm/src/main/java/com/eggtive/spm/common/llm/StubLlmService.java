package com.eggtive.spm.common.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Stub implementation for local development. Returns a placeholder response
 * so callers can test their prompt→parse flow without calling a real model.
 *
 * <p>Callers should handle the stub response gracefully — the returned text
 * is intentionally minimal so use-case-specific stubs can override if needed.</p>
 */
@Component
@ConditionalOnProperty(name = "app.llm.type", havingValue = "stub", matchIfMissing = true)
public class StubLlmService implements LlmService {

    private static final Logger log = LoggerFactory.getLogger(StubLlmService.class);

    @Override
    public String complete(String prompt) {
        log.info("Stub LLM complete — prompt length: {} chars", prompt.length());
        return "{}";
    }

    @Override
    public String completeWithImage(byte[] imageBytes, String contentType, String prompt) {
        log.info("Stub LLM completeWithImage — image: {} bytes ({}), prompt: {} chars",
                imageBytes.length, contentType, prompt.length());
        return "{}";
    }
}
