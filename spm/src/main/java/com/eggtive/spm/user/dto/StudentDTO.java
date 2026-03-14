package com.eggtive.spm.user.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record StudentDTO(
    UUID id, UUID userId, String email, String firstName, String lastName,
    LocalDate dateOfBirth, String grade, LocalDate enrollmentDate,
    UUID parentId, String parentName, boolean isActive, Instant createdAt
) {}
