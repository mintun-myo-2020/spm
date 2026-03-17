package com.eggtive.spm.common.llm;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Formats requests/responses for Anthropic Claude models on Bedrock.
 */
@Component
@ConditionalOnProperty(name = "app.llm.bedrock.model-adapter", havingValue = "anthropic", matchIfMissing = true)
public class AnthropicModelAdapter implements ModelAdapter {

    private final JsonMapper jsonMapper;

    public AnthropicModelAdapter(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public String buildTextRequestBody(String prompt) throws Exception {
        return jsonMapper.writeValueAsString(Map.of(
                "anthropic_version", "bedrock-2023-05-31",
                "max_tokens", 4096,
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", List.of(Map.of("type", "text", "text", prompt))
                ))
        ));
    }

    @Override
    public String buildImageRequestBody(String base64Image, String mediaType, String prompt) throws Exception {
        return jsonMapper.writeValueAsString(Map.of(
                "anthropic_version", "bedrock-2023-05-31",
                "max_tokens", 4096,
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", List.of(
                                Map.of("type", "image", "source", Map.of(
                                        "type", "base64", "media_type", mediaType, "data", base64Image)),
                                Map.of("type", "text", "text", prompt)
                        )
                ))
        ));
    }

    @Override
    public String extractResponseText(String responseBody) throws Exception {
        JsonNode root = jsonMapper.readTree(responseBody);
        JsonNode contentArray = root.get("content");
        if (contentArray != null && contentArray.isArray()) {
            for (JsonNode block : contentArray) {
                if ("text".equals(block.get("type").asText())) {
                    return block.get("text").asText();
                }
            }
        }
        return "";
    }
}
