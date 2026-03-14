package com.eggtive.spm.common.dto;

import com.eggtive.spm.common.enums.ErrorCode;
import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
    ErrorCode code,
    String message,
    Map<String, Object> details,
    Instant timestamp
) {
    public static ErrorResponse of(ErrorCode code, String message) {
        return new ErrorResponse(code, message, null, Instant.now());
    }
    public static ErrorResponse of(ErrorCode code, String message, Map<String, Object> details) {
        return new ErrorResponse(code, message, details, Instant.now());
    }
}
