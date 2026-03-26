package com.eggtive.spm.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * @param reportType        report type label
 * @param classId           the class to report on
 * @param startDate         reporting period start
 * @param endDate           reporting period end
 * @param includePlan       if true, generate an LLM-powered strengths and improvement plan
 *                          and embed it in the HTML report (takes a few extra seconds)
 * @param compareReportIds  optional previous report IDs for period comparison in the plan;
 *                          ignored when includePlan is false
 */
public record GenerateReportRequestDTO(
    @NotBlank String reportType,
    @NotNull UUID classId,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    Boolean includePlan,
    List<UUID> compareReportIds
) {
    /** Convenience: defaults to false when null. */
    public boolean shouldIncludePlan() {
        return includePlan != null && includePlan;
    }
}
