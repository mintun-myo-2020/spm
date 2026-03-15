package com.eggtive.spm.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record GenerateReportRequestDTO(
    @NotBlank String reportType,
    @NotNull UUID classId,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate
) {}
