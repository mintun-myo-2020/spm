package com.eggtive.spm.subject.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateSubjectRequestDTO(
    @NotBlank String name,
    String description
) {}
