package com.eggtive.spm.scheduling.dto;

import jakarta.validation.constraints.NotNull;

public record RsvpRequestDTO(
    @NotNull String rsvpStatus,
    String reason
) {}
