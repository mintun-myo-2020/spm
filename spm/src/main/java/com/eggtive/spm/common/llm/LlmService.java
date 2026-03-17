package com.eggtive.spm.common.llm;

/**
 * Generic LLM service abstraction. Use cases inject a prompt and get text back.
 * The implementation handles model selection, API formatting, and error handling.
 *
 * <p>For text-only prompts, use {@link #complete(String)}.
 * For multimodal (image + text), use {@link #completeWithImage(byte[], String, String)}.</p>
 *
 * <p>Implementations: {@code StubLlmService} (dev), {@code BedrockLlmService} (prod),
 * {@code SpringAiLlmService} (Spring AI).</p>
 */
public interface LlmService {

    /**
     * Send a text prompt and get a text response.
     *
     * @param prompt the full prompt text
     * @return the model's text response
     */
    String complete(String prompt);

    /**
     * Send an image + text prompt and get a text response (multimodal).
     *
     * @param imageBytes  raw image content
     * @param contentType MIME type (image/jpeg, image/png)
     * @param prompt      the text prompt to accompany the image
     * @return the model's text response
     */
    String completeWithImage(byte[] imageBytes, String contentType, String prompt);
}
