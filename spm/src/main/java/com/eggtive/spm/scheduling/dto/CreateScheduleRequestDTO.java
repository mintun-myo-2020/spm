package com.eggtive.spm.scheduling.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record CreateScheduleRequestDTO(
    @NotNull @Min(1) @Max(7) Integer dayOfWeek,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime,
    String location,
    @NotNull LocalDate effectiveFrom,
    LocalDate effectiveUntil
) {}
