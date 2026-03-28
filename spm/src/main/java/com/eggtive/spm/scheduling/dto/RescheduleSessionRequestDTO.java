package com.eggtive.spm.scheduling.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record RescheduleSessionRequestDTO(
    LocalDate newDate,
    LocalTime newStartTime,
    LocalTime newEndTime,
    String newLocation
) {}
