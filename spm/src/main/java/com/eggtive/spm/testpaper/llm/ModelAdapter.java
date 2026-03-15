package com.eggtive.spm.testpaper.llm;

/**
 * Abstraction for LLM model-specific request/response formatting.
 * Each model provider (Anthropic Claude, Amazon Titan, etc.) has its own
 * API payload structure. Implementations handle that translation so the
 * extraction service stays model-agnostic.
 *
 * To add a new model: implement this interface and register it with a
 * {@code @ConditionalOnProperty} on {@code app.extraction.bedrock.model-adapter}.
 */
public interface ModelAdapter {

    /**
     * Build the JSON request body for the model.
     *
     * @param base64Image  base64-encoded image data
     * @param mediaType    MIME type of the image (e.g. "image/jpeg")
     * @param systemPrompt the extraction prompt telling the model what to return
     * @return JSON string ready to send to Bedrock InvokeModel
     */
    String buildRequestBody(String base64Image, String mediaType, String systemPrompt) throws Exception;

    /**
     * Extract the text content from the model's response body.
     * The returned string should be the raw text/JSON the model generated.
     *
     * @param responseBody full JSON response from Bedrock InvokeModel
     * @return the model's text output
     */
    String extractResponseText(String responseBody) throws Exception;
}
