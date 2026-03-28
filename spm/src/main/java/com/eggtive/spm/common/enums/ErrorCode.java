package com.eggtive.spm.common.enums;

public enum ErrorCode {
    INVALID_INPUT(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    CONFLICT(409),
    INVALID_SCORE(400),
    INVALID_DATE(400),
    CLASS_FULL(409),
    INTERNAL_ERROR(500),
    VALIDATION_FAILED(400),
    INVALID_FILE_TYPE(400),
    FILE_TOO_LARGE(400),
    INVALID_FILE_CONTENT(400),
    STORAGE_ERROR(500),
    OCR_ERROR(500),
    UPLOAD_ALREADY_PROCESSING(409),
    UPLOAD_ALREADY_PROCESSED(409),
    SCHEDULE_CONFLICT(409),
    SESSION_NOT_SCHEDULED(400),
    INVALID_RSVP(400);

    private final int httpStatus;

    ErrorCode(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
