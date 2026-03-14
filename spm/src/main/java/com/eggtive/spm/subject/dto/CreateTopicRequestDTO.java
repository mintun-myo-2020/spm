package com.eggtive.spm.subject.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTopicRequestDTO(
    @NotBlank String name,
    @NotBlank String code,
    String description
) {}
