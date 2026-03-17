package com.eggtive.spm.common.llm;

/**
 * Abstraction for LLM model-specific request/response formatting.
 * Each model provider (Anthropic Claude, Amazon Nova, etc.) has its own
 * API payload structure. Implementations handle that translation so the
 * LLM service stays model-agnostic.
 */
public interface ModelAdapter {

    /**
     * Build a text-only request body.
     */
    String buildTextRequestBody(String prompt) throws Exception;

    /**
     * Build a multimodal (image + text) request body.
     */
    String buildImageRequestBody(String base64Image, String mediaType, String prompt) throws Exception;

    /**
     * Extract the text content from the model's response body.
     */
    String extractResponseText(String responseBody) throws Exception;
}
