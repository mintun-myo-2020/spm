package com.eggtive.spm.scheduling.dto;

import java.time.Instant;
import java.util.UUID;

public record AttendanceDTO(
    UUID id,
    UUID sessionId,
    UUID studentId,
    String studentName,
    String status,
    String studentRsvp,
    String rsvpReason,
    UUID markedBy,
    Instant markedAt
) {}
