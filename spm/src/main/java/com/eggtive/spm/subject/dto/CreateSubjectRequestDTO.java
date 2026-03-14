package com.eggtive.spm.subject.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateSubjectRequestDTO(
    @NotBlank String name,
    @NotBlank String code,
    String description
) {}
