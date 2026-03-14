package com.eggtive.spm.user.dto;

import java.time.Instant;
import java.util.UUID;

public record TeacherDTO(
    UUID id, UUID userId, String email, String firstName, String lastName,
    String phoneNumber, String specialization, boolean isActive, Instant createdAt
) {}
