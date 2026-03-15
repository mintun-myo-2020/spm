package com.eggtive.spm.testpaper.llm;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Formats requests/responses for Amazon Nova models on Bedrock
 * (Converse-compatible payload with imageBlock).
 */
@Component
@ConditionalOnProperty(name = "app.extraction.bedrock.model-adapter", havingValue = "nova")
public class TitanModelAdapter implements ModelAdapter {

    private final JsonMapper jsonMapper;

    public TitanModelAdapter(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public String buildRequestBody(String base64Image, String mediaType, String systemPrompt) throws Exception {
        // Amazon Nova / Titan vision uses a different message structure
        return jsonMapper.writeValueAsString(Map.of(
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", List.of(
                                Map.of(
                                        "image", Map.of(
                                                "format", mediaTypeToFormat(mediaType),
                                                "source", Map.of(
                                                        "bytes", base64Image
                                                )
                                        )
                                ),
                                Map.of(
                                        "text", systemPrompt
                                )
                        )
                )),
                "inferenceConfig", Map.of(
                        "maxTokens", 4096
                )
        ));
    }

    @Override
    public String extractResponseText(String responseBody) throws Exception {
        JsonNode root = jsonMapper.readTree(responseBody);
        // Nova response: output.message.content[0].text
        JsonNode output = root.get("output");
        if (output != null) {
            JsonNode message = output.get("message");
            if (message != null) {
                JsonNode content = message.get("content");
                if (content != null && content.isArray() && !content.isEmpty()) {
                    JsonNode textNode = content.get(0).get("text");
                    if (textNode != null) return textNode.asText();
                }
            }
        }
        return "";
    }

    private String mediaTypeToFormat(String mediaType) {
        return switch (mediaType) {
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            default -> "jpeg";
        };
    }
}
