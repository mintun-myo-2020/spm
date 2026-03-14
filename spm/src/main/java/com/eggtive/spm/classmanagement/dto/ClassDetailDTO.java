package com.eggtive.spm.classmanagement.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ClassDetailDTO(
    UUID id, String name, UUID subjectId, String subjectName,
    UUID teacherId, String teacherName, String description,
    int maxStudents, long currentStudentCount, boolean isActive, Instant createdAt,
    List<ClassStudentDTO> students
) {}
