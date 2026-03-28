package com.eggtive.spm.scheduling.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record SessionDTO(
    UUID id,
    UUID scheduleId,
    UUID classId,
    String className,
    LocalDate sessionDate,
    String dayOfWeekName,
    LocalTime startTime,
    LocalTime endTime,
    String location,
    String status,
    String cancelReason,
    int enrolledCount,
    int markedCount,
    int notAttendingRsvpCount,
    String topicCovered,
    String homeworkGiven,
    String commonWeaknesses,
    String additionalNotes,
    Instant createdAt
) {}
