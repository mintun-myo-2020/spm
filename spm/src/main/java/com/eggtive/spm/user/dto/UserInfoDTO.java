package com.eggtive.spm.user.dto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record UserInfoDTO(
    UUID id,
    String email,
    String firstName,
    String lastName,
    Set<String> roles,
    UUID profileId,
    String profileType,
    List<LinkedStudentDTO> linkedStudents
) {
    public record LinkedStudentDTO(UUID studentId, String studentName) {}
}
