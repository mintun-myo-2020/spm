package com.eggtive.spm.testpaper.llm;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Formats requests/responses for Amazon Nova models on Bedrock (InvokeModel API).
 *
 * <p>Image input uses base64-encoded strings per the Invoke API spec.</p>
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
        return jsonMapper.writeValueAsString(Map.of(
                "schemaVersion", "messages-v1",
                "system", List.of(Map.of(
                        "text", "You are a test paper analysis assistant. Follow the user's instructions exactly."
                )),
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
                        "maxTokens", 4096,
                        "temperature", 0.1
                )
        ));
    }

    @Override
    public String extractResponseText(String responseBody) throws Exception {
        JsonNode root = jsonMapper.readTree(responseBody);
        // Nova response: output.message.content[].text
        JsonNode output = root.get("output");
        if (output != null) {
            JsonNode message = output.get("message");
            if (message != null) {
                JsonNode content = message.get("content");
                if (content != null && content.isArray()) {
                    for (JsonNode block : content) {
                        JsonNode textNode = block.get("text");
                        if (textNode != null) return textNode.asText();
                    }
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
