package com.eggtive.spm.scheduling.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ScheduleDTO(
    UUID id,
    UUID classId,
    String className,
    Integer dayOfWeek,
    String dayOfWeekName,
    LocalTime startTime,
    LocalTime endTime,
    String location,
    boolean isRecurring,
    LocalDate effectiveFrom,
    LocalDate effectiveUntil,
    int sessionCount,
    Instant createdAt
) {}
