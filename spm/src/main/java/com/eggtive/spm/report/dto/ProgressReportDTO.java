package com.eggtive.spm.report.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProgressReportDTO(
    UUID id, UUID studentId, String reportType,
    LocalDate startDate, LocalDate endDate,
    String reportUrl, Instant generatedAt, Instant expiresAt
) {}
