package com.eggtive.spm.common.llm;

/**
 * Utility methods for processing LLM responses.
 */
public final class LlmResponseUtil {

    private LlmResponseUtil() {}

    /**
     * Strip markdown code fences that models often wrap around JSON output.
     * e.g. ```json\n{...}\n``` → {...}
     */
    public static String extractJsonFromText(String text) {
        if (text == null || text.isBlank()) return "{}";
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNewline > 0 && lastFence > firstNewline) {
                trimmed = trimmed.substring(firstNewline + 1, lastFence).trim();
            }
        }
        return trimmed;
    }
}
