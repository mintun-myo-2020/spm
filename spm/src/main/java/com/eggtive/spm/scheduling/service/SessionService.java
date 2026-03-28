package com.eggtive.spm.scheduling.service;

import com.eggtive.spm.common.dto.PagedResponse;
import com.eggtive.spm.common.enums.*;
import com.eggtive.spm.common.exception.AppException;
import com.eggtive.spm.scheduling.dto.*;
import com.eggtive.spm.scheduling.entity.ClassSession;
import com.eggtive.spm.scheduling.entity.SessionAttendance;
import com.eggtive.spm.scheduling.repository.ClassSessionRepository;
import com.eggtive.spm.scheduling.repository.SessionAttendanceRepository;
import com.eggtive.spm.user.entity.User;
import com.eggtive.spm.user.repository.ParentRepository;
import com.eggtive.spm.user.repository.StudentRepository;
import com.eggtive.spm.user.repository.TeacherRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

@Service
@Transactional
public class SessionService {

    private final ClassSessionRepository sessionRepo;
    private final SessionAttendanceRepository attendanceRepo;
    private final TeacherRepository teacherRepo;
    private final StudentRepository studentRepo;
    private final ParentRepository parentRepo;
    private final ScheduleService scheduleService;

    public SessionService(ClassSessionRepository sessionRepo, SessionAttendanceRepository attendanceRepo,
                          TeacherRepository teacherRepo, StudentRepository studentRepo,
                          ParentRepository parentRepo, ScheduleService scheduleService) {
        this.sessionRepo = sessionRepo;
        this.attendanceRepo = attendanceRepo;
        this.teacherRepo = teacherRepo;
        this.studentRepo = studentRepo;
        this.parentRepo = parentRepo;
        this.scheduleService = scheduleService;
    }

    @Transactional(readOnly = true)
    public PagedResponse<SessionDTO> getUpcomingSessions(User user, Pageable pageable) {
        LocalDate today = LocalDate.now();
        Page<ClassSession> page;

        if (user.hasRole(Role.ADMIN)) {
            page = sessionRepo.findAllUpcoming(today, pageable);
        } else if (user.hasRole(Role.TEACHER)) {
            var teacher = teacherRepo.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Teacher profile not found"));
            page = sessionRepo.findUpcomingForTeacher(teacher.getId(), today, pageable);
        } else if (user.hasRole(Role.STUDENT)) {
            var student = studentRepo.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Student profile not found"));
            page = sessionRepo.findUpcomingForStudent(student.getId(), today, pageable);
        } else if (user.hasRole(Role.PARENT)) {
            var parent = parentRepo.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Parent profile not found"));
            page = sessionRepo.findUpcomingForParent(parent.getId(), today, pageable);
        } else {
            throw new AppException(ErrorCode.FORBIDDEN, "Access denied");
        }

        var dtos = page.getContent().stream().map(scheduleService::toSessionDTO).toList();
        return PagedResponse.from(page, dtos);
    }

    @Transactional(readOnly = true)
    public PagedResponse<SessionDTO> getClassSessions(UUID classId, LocalDate startDate, LocalDate endDate,
                                                       SessionStatus status, Pageable pageable) {
        LocalDate from = startDate != null ? startDate : LocalDate.of(2000, 1, 1);
        Page<ClassSession> page = sessionRepo.findByClassIdFiltered(classId, from, endDate, status, pageable);
        var dtos = page.getContent().stream().map(scheduleService::toSessionDTO).toList();
        return PagedResponse.from(page, dtos);
    }

    @Transactional(readOnly = true)
    public SessionDetailDTO getSessionDetail(UUID sessionId) {
        ClassSession session = findSessionOrThrow(sessionId);
        List<SessionAttendance> attendance = attendanceRepo.findBySessionId(sessionId);
        // Sort: NOT_ATTENDING at bottom, then alphabetical
        attendance.sort(Comparator
            .comparing((SessionAttendance a) -> a.getStudentRsvp() == RsvpStatus.NOT_ATTENDING ? 1 : 0)
            .thenComparing(a -> a.getStudent().getUser().getFirstName() + " " + a.getStudent().getUser().getLastName()));

        List<AttendanceDTO> attDtos = attendance.stream().map(this::toAttendanceDTO).toList();
        String dayName = session.getSessionDate().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        return new SessionDetailDTO(session.getId(),
            session.getSchedule() != null ? session.getSchedule().getId() : null,
            session.getTuitionClass().getId(), session.getTuitionClass().getName(),
            session.getSessionDate(), dayName, session.getStartTime(), session.getEndTime(),
            session.getLocation(), session.getStatus().name(), session.getCancelReason(),
            attDtos, session.getCreatedAt());
    }

    public SessionUpdateResponseDTO rescheduleSession(UUID sessionId, RescheduleSessionRequestDTO req) {
        ClassSession session = findSessionOrThrow(sessionId);
        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new AppException(ErrorCode.SESSION_NOT_SCHEDULED, "Can only reschedule SCHEDULED sessions");
        }

        List<String> warnings = new ArrayList<>();
        if (req.newDate() != null) {
            session.setSessionDate(req.newDate());
            // Check conflicts
            var conflicts = sessionRepo.findByTuitionClassIdAndSessionDateAndStatusNot(
                session.getTuitionClass().getId(), req.newDate(), SessionStatus.CANCELLED);
            conflicts.stream()
                .filter(c -> !c.getId().equals(sessionId))
                .forEach(c -> warnings.add("Conflict: session already exists on " + req.newDate() + " at " + c.getStartTime()));
        }
        if (req.newStartTime() != null) session.setStartTime(req.newStartTime());
        if (req.newEndTime() != null) session.setEndTime(req.newEndTime());
        if (req.newLocation() != null) session.setLocation(req.newLocation());

        session = sessionRepo.save(session);
        return new SessionUpdateResponseDTO(scheduleService.toSessionDTO(session), warnings);
    }

    public SessionDTO cancelSession(UUID sessionId, String reason) {
        ClassSession session = findSessionOrThrow(sessionId);
        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new AppException(ErrorCode.SESSION_NOT_SCHEDULED, "Can only cancel SCHEDULED sessions");
        }
        session.setStatus(SessionStatus.CANCELLED);
        session.setCancelReason(reason);
        return scheduleService.toSessionDTO(sessionRepo.save(session));
    }

    public ClassSession findSessionOrThrow(UUID sessionId) {
        return sessionRepo.findById(sessionId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Session not found"));
    }

    private AttendanceDTO toAttendanceDTO(SessionAttendance a) {
        String name = a.getStudent().getUser().getFirstName() + " " + a.getStudent().getUser().getLastName();
        return new AttendanceDTO(a.getId(), a.getSession().getId(), a.getStudent().getId(), name,
            a.getStatus().name(), a.getStudentRsvp().name(), a.getRsvpReason(),
            a.getMarkedBy() != null ? a.getMarkedBy().getId() : null, a.getMarkedAt());
    }
}
