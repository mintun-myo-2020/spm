package com.eggtive.spm.testpaper.dto;

import com.eggtive.spm.testpaper.parser.ParsedResult;
import java.time.Instant;
import java.util.UUID;

public record TestPaperPageDTO(
    UUID pageId,
    int pageNumber,
    String fileName,
    String contentType,
    long fileSizeBytes,
    String status,
    String fileUrl,
    String extractedText,
    Float ocrConfidence,
    ParsedResult parsedResult,
    Instant createdAt
) {}
