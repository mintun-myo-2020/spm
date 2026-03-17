package com.eggtive.spm.common.llm;

/**
 * Runtime exception for LLM service failures.
 */
public class LlmException extends RuntimeException {
    public LlmException(String message, Throwable cause) {
        super(message, cause);
    }
}
