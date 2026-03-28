package com.eggtive.spm.scheduling.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record CreateOneOffScheduleRequestDTO(
    @NotNull LocalDate sessionDate,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime,
    String location
) {}
