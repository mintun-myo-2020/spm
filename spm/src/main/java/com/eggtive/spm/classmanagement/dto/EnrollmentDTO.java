package com.eggtive.spm.classmanagement.dto;

import java.time.LocalDate;
import java.util.UUID;

public record EnrollmentDTO(
    UUID id, UUID classId, UUID studentId, String studentName,
    LocalDate enrollmentDate, String status
) {}
