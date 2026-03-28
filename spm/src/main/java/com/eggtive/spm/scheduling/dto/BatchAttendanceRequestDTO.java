package com.eggtive.spm.scheduling.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record BatchAttendanceRequestDTO(
    @NotNull List<AttendanceEntryDTO> entries
) {}
