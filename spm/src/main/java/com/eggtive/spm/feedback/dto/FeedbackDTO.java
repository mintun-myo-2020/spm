package com.eggtive.spm.feedback.dto;

import java.time.Instant;
import java.util.UUID;

public record FeedbackDTO(
    UUID id, UUID testScoreId, UUID teacherId, String teacherName,
    UUID studentId, String strengths, String areasForImprovement,
    String recommendations, String additionalNotes,
    boolean isEdited, Instant createdAt, Instant updatedAt
) {}
