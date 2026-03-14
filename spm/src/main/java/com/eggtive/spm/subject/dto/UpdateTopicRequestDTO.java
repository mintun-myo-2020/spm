package com.eggtive.spm.subject.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateTopicRequestDTO(
    @NotBlank String name,
    String description
) {}
