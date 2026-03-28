package com.eggtive.spm.scheduling.dto;

import java.util.List;
import java.util.UUID;

public record ClassAttendanceStatsDTO(
    UUID classId,
    String className,
    int totalSessions,
    int sessionsWithAttendance,
    double averageAttendanceRate,
    List<StudentAttendanceStatsDTO> studentStats
) {}
