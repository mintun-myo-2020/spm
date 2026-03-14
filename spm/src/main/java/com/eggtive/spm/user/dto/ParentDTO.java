package com.eggtive.spm.user.dto;

import java.time.Instant;
import java.util.UUID;

public record ParentDTO(
    UUID id, UUID userId, String email, String firstName, String lastName,
    String phoneNumber, UUID studentId, String studentName,
    String preferredContactMethod, boolean emailNotificationsEnabled,
    boolean smsNotificationsEnabled, boolean isActive, Instant createdAt
) {}
