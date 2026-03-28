package com.eggtive.spm.common.exception;

import com.eggtive.spm.common.dto.ErrorResponse;
import com.eggtive.spm.common.enums.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex) {
        HttpStatus status = mapToHttpStatus(ex.getErrorCode());
        return ResponseEntity.status(status)
            .body(ErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> details = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e ->
            details.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(ErrorCode.VALIDATION_FAILED, "Validation failed", details));
    }

    private HttpStatus mapToHttpStatus(ErrorCode code) {
        return HttpStatus.valueOf(code.getHttpStatus());
    }
}
