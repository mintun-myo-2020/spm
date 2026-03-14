package com.eggtive.spm.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CreateStudentRequestDTO(
    @NotBlank @Email String email,
    @NotBlank String firstName,
    @NotBlank String lastName,
    LocalDate dateOfBirth,
    String grade
) {}
