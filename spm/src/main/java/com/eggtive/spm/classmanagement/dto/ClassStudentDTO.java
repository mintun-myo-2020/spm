package com.eggtive.spm.classmanagement.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ClassStudentDTO(
    UUID id, String name, String email, LocalDate enrollmentDate, String status
) {}
