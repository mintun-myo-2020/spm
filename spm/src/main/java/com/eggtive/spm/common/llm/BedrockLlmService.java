package com.eggtive.spm.common.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;

import java.util.Base64;

/**
 * Production LLM service using Amazon Bedrock InvokeModel API.
 * Model-specific formatting is delegated to {@link ModelAdapter}.
 */
@Component
@ConditionalOnProperty(name = "app.llm.type", havingValue = "bedrock")
public class BedrockLlmService implements LlmService {

    private static final Logger log = LoggerFactory.getLogger(BedrockLlmService.class);

    private final BedrockRuntimeClient bedrockClient;
    private final ModelAdapter modelAdapter;
    private final String modelId;

    public BedrockLlmService(BedrockRuntimeClient bedrockClient,
                             ModelAdapter modelAdapter,
                             @Value("${app.llm.bedrock.model-id}") String modelId) {
        this.bedrockClient = bedrockClient;
        this.modelAdapter = modelAdapter;
        this.modelId = modelId;
        log.info("Bedrock LLM configured — model: {}, adapter: {}",
                modelId, modelAdapter.getClass().getSimpleName());
    }

    @Override
    public String complete(String prompt) {
        log.debug("Bedrock text completion — prompt: {} chars, model: {}", prompt.length(), modelId);
        try {
            String requestBody = modelAdapter.buildTextRequestBody(prompt);
            var response = bedrockClient.invokeModel(
                    InvokeModelRequest.builder()
                            .modelId(modelId)
                            .contentType("application/json")
                            .accept("application/json")
                            .body(SdkBytes.fromUtf8String(requestBody))
                            .build());
            return modelAdapter.extractResponseText(response.body().asUtf8String());
        } catch (Exception e) {
            log.error("Bedrock text completion failed", e);
            throw new LlmException("LLM completion failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String completeWithImage(byte[] imageBytes, String contentType, String prompt) {
        log.debug("Bedrock image completion — image: {} bytes, model: {}", imageBytes.length, modelId);
        try {
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String mediaType = contentType != null ? contentType : "image/jpeg";
            String requestBody = modelAdapter.buildImageRequestBody(base64Image, mediaType, prompt);

            var response = bedrockClient.invokeModel(
                    InvokeModelRequest.builder()
                            .modelId(modelId)
                            .contentType("application/json")
                            .accept("application/json")
                            .body(SdkBytes.fromUtf8String(requestBody))
                            .build());
            return modelAdapter.extractResponseText(response.body().asUtf8String());
        } catch (Exception e) {
            log.error("Bedrock image completion failed", e);
            throw new LlmException("LLM image completion failed: " + e.getMessage(), e);
        }
    }
}
