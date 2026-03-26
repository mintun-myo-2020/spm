package com.eggtive.spm.report.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Request to generate a strengths and improvement plan.
 *
 * @param classId           the class to generate the plan for
 * @param startDate         start of the current reporting period
 * @param endDate           end of the current reporting period
 * @param compareReportIds  optional list of previous report IDs to include for comparison;
 *                          if null or empty, no comparison is performed
 */
public record GeneratePlanRequestDTO(
    @NotNull UUID classId,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    List<UUID> compareReportIds
) {}
