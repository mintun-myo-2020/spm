package com.eggtive.spm.scheduling.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AttendanceEntryDTO(
    @NotNull UUID studentId,
    @NotNull String status
) {}
