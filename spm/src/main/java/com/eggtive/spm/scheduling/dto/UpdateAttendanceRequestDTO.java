package com.eggtive.spm.scheduling.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateAttendanceRequestDTO(@NotNull String status) {}
