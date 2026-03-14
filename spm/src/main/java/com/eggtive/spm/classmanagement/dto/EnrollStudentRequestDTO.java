package com.eggtive.spm.classmanagement.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record EnrollStudentRequestDTO(@NotNull UUID studentId) {}
