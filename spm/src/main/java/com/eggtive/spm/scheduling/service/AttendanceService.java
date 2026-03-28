package com.eggtive.spm.scheduling.service;

import com.eggtive.spm.classmanagement.service.ClassService;
import com.eggtive.spm.common.enums.*;
import com.eggtive.spm.common.exception.AppException;
import com.eggtive.spm.scheduling.dto.*;
import com.eggtive.spm.scheduling.entity.ClassSession;
import com.eggtive.spm.scheduling.entity.SessionAttendance;
import com.eggtive.spm.scheduling.repository.ClassSessionRepository;
import com.eggtive.spm.scheduling.repository.SessionAttendanceRepository;
import com.eggtive.spm.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AttendanceService {

    private final SessionAttendanceRepository attendanceRepo;
    private final ClassSessionRepository sessionRepo;
    private final ClassService classService;

    public AttendanceService(SessionAttendanceRepository attendanceRepo,
                             ClassSessionRepository sessionRepo, ClassService classService) {
        this.attendanceRepo = attendanceRepo;
        this.sessionRepo = sessionRepo;
        this.classService = classService;
    }

    public List<AttendanceDTO> batchMarkAttendance(UUID sessionId, List<AttendanceEntryDTO> entries, User markedBy) {
        List<SessionAttendance> records = attendanceRepo.findBySessionId(sessionId);
        Map<UUID, SessionAttendance> byStudent = records.stream()
            .collect(Collectors.toMap(a -> a.getStudent().getId(), a -> a));

        for (AttendanceEntryDTO entry : entries) {
            SessionAttendance att = byStudent.get(entry.studentId());
            if (att == null) {
                throw new AppException(ErrorCode.NOT_FOUND, "Attendance record not found for student " + entry.studentId());
            }
            att.setStatus(AttendanceStatus.valueOf(entry.status()));
            att.setMarkedBy(markedBy);
            att.setMarkedAt(Instant.now());
        }
        attendanceRepo.saveAll(byStudent.values());
        return records.stream().map(this::toDTO).toList();
    }

    public AttendanceDTO updateAttendance(UUID sessionId, UUID studentId, String status, User markedBy) {
        SessionAttendance att = attendanceRepo.findBySessionIdAndStudentId(sessionId, studentId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Attendance record not found"));
        att.setStatus(AttendanceStatus.valueOf(status));
        att.setMarkedBy(markedBy);
        att.setMarkedAt(Instant.now());
        return toDTO(attendanceRepo.save(att));
    }

    public AttendanceDTO updateRsvp(UUID sessionId, UUID studentId, String rsvpStatus, String reason) {
        SessionAttendance att = attendanceRepo.findBySessionIdAndStudentId(sessionId, studentId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Attendance record not found"));

        ClassSession session = sessionRepo.findById(sessionId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Session not found"));
        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new AppException(ErrorCode.INVALID_RSVP, "Cannot RSVP for non-scheduled sessions");
        }
        if (session.getSessionDate().isBefore(LocalDate.now())) {
            throw new AppException(ErrorCode.INVALID_RSVP, "Cannot RSVP for past sessions");
        }

        RsvpStatus newStatus = RsvpStatus.valueOf(rsvpStatus);
        att.setStudentRsvp(newStatus);
        att.setRsvpReason(newStatus == RsvpStatus.NOT_ATTENDING ? reason : null);
        return toDTO(attendanceRepo.save(att));
    }

    @Transactional(readOnly = true)
    public StudentAttendanceStatsDTO getStudentAttendanceStats(UUID studentId, UUID classId,
                                                                LocalDate startDate, LocalDate endDate) {
        LocalDate from = startDate != null ? startDate : LocalDate.of(2000, 1, 1);
        List<SessionAttendance> records = attendanceRepo.findByStudentAndClass(studentId, classId, from, endDate);

        int total = 0, present = 0, absent = 0, late = 0, excused = 0, unmarked = 0;
        for (SessionAttendance a : records) {
            switch (a.getStatus()) {
                case PRESENT -> { total++; present++; }
                case ABSENT -> { total++; absent++; }
                case LATE -> { total++; late++; }
                case EXCUSED -> { total++; excused++; }
                case UNMARKED -> unmarked++;
            }
        }
        double rate = total > 0 ? ((present + late) * 100.0 / total) : 0;

        // Get student name
        String name = records.isEmpty() ? "" :
            records.getFirst().getStudent().getUser().getFirstName() + " " + records.getFirst().getStudent().getUser().getLastName();

        return new StudentAttendanceStatsDTO(studentId, name, classId, total, present, absent, late, excused, unmarked, rate);
    }

    @Transactional(readOnly = true)
    public ClassAttendanceStatsDTO getClassAttendanceStats(UUID classId, LocalDate startDate, LocalDate endDate) {
        LocalDate from = startDate != null ? startDate : LocalDate.of(2000, 1, 1);
        List<SessionAttendance> allRecords = attendanceRepo.findByClassId(classId, from, endDate);

        // Group by student
        Map<UUID, List<SessionAttendance>> byStudent = allRecords.stream()
            .collect(Collectors.groupingBy(a -> a.getStudent().getId()));

        List<StudentAttendanceStatsDTO> studentStats = byStudent.entrySet().stream().map(e -> {
            UUID sid = e.getKey();
            List<SessionAttendance> recs = e.getValue();
            int total = 0, present = 0, absent = 0, late = 0, excused = 0, unmarked = 0;
            String name = "";
            for (SessionAttendance a : recs) {
                if (name.isEmpty()) name = a.getStudent().getUser().getFirstName() + " " + a.getStudent().getUser().getLastName();
                switch (a.getStatus()) {
                    case PRESENT -> { total++; present++; }
                    case ABSENT -> { total++; absent++; }
                    case LATE -> { total++; late++; }
                    case EXCUSED -> { total++; excused++; }
                    case UNMARKED -> unmarked++;
                }
            }
            double rate = total > 0 ? ((present + late) * 100.0 / total) : 0;
            return new StudentAttendanceStatsDTO(sid, name, classId, total, present, absent, late, excused, unmarked, rate);
        }).toList();

        long totalSessions = allRecords.stream().map(a -> a.getSession().getId()).distinct().count();
        long sessionsWithAtt = attendanceRepo.countSessionsWithAttendance(classId, from, endDate);
        double avgRate = studentStats.isEmpty() ? 0 :
            studentStats.stream().mapToDouble(StudentAttendanceStatsDTO::attendanceRate).average().orElse(0);

        String className = allRecords.isEmpty() ? "" : allRecords.getFirst().getSession().getTuitionClass().getName();

        return new ClassAttendanceStatsDTO(classId, className, (int) totalSessions, (int) sessionsWithAtt, avgRate, studentStats);
    }

    private AttendanceDTO toDTO(SessionAttendance a) {
        String name = a.getStudent().getUser().getFirstName() + " " + a.getStudent().getUser().getLastName();
        return new AttendanceDTO(a.getId(), a.getSession().getId(), a.getStudent().getId(), name,
            a.getStatus().name(), a.getStudentRsvp().name(), a.getRsvpReason(),
            a.getMarkedBy() != null ? a.getMarkedBy().getId() : null, a.getMarkedAt());
    }
}
