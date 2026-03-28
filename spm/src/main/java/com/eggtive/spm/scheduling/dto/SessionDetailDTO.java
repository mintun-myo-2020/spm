package com.eggtive.spm.scheduling.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record SessionDetailDTO(
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
    List<AttendanceDTO> attendance,
    String topicCovered,
    String homeworkGiven,
    String commonWeaknesses,
    String additionalNotes,
    Instant createdAt
) {}
