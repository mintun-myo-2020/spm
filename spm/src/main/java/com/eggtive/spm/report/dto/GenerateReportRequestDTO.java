package com.eggtive.spm.report.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record GenerateReportRequestDTO(
    @NotBlank String reportType,
    LocalDate startDate,
    LocalDate endDate
) {}
