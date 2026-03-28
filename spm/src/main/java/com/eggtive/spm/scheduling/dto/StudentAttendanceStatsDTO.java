package com.eggtive.spm.scheduling.dto;

import java.util.UUID;

public record StudentAttendanceStatsDTO(
    UUID studentId,
    String studentName,
    UUID classId,
    int totalSessions,
    int presentCount,
    int absentCount,
    int lateCount,
    int excusedCount,
    int unmarkedCount,
    double attendanceRate
) {}
