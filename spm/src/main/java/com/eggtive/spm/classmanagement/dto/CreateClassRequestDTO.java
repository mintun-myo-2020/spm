package com.eggtive.spm.classmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateClassRequestDTO(
    @NotBlank String name,
    @NotNull UUID subjectId,
    @NotNull UUID teacherId,
    String description,
    Integer maxStudents,
    // Optional initial schedule (FR-14.8)
    Integer scheduleDayOfWeek,
    java.time.LocalTime scheduleStartTime,
    java.time.LocalTime scheduleEndTime,
    String scheduleLocation,
    java.time.LocalDate scheduleEffectiveFrom,
    java.time.LocalDate scheduleEffectiveUntil
) {}
