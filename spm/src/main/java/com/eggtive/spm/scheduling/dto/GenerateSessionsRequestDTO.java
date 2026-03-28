package com.eggtive.spm.scheduling.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record GenerateSessionsRequestDTO(
    @NotNull LocalDate targetEndDate
) {}
