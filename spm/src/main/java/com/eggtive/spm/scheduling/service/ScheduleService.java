package com.eggtive.spm.scheduling.service;

import com.eggtive.spm.classmanagement.entity.ClassStudent;
import com.eggtive.spm.classmanagement.entity.TuitionClass;
import com.eggtive.spm.classmanagement.repository.ClassStudentRepository;
import com.eggtive.spm.classmanagement.service.ClassService;
import com.eggtive.spm.common.enums.*;
import com.eggtive.spm.common.exception.AppException;
import com.eggtive.spm.scheduling.dto.*;
import com.eggtive.spm.scheduling.entity.ClassSchedule;
import com.eggtive.spm.scheduling.entity.ClassSession;
import com.eggtive.spm.scheduling.entity.SessionAttendance;
import com.eggtive.spm.scheduling.repository.ClassScheduleRepository;
import com.eggtive.spm.scheduling.repository.ClassSessionRepository;
import com.eggtive.spm.scheduling.repository.SessionAttendanceRepository;
import com.eggtive.spm.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

@Service
@Transactional
public class ScheduleService {

    private final ClassScheduleRepository scheduleRepo;
    private final ClassSessionRepository sessionRepo;
    private final SessionAttendanceRepository attendanceRepo;
    private final ClassStudentRepository classStudentRepo;
    private final ClassService classService;

    public ScheduleService(ClassScheduleRepository scheduleRepo, ClassSessionRepository sessionRepo,
                           SessionAttendanceRepository attendanceRepo, ClassStudentRepository classStudentRepo,
                           ClassService classService) {
        this.scheduleRepo = scheduleRepo;
        this.sessionRepo = sessionRepo;
        this.attendanceRepo = attendanceRepo;
        this.classStudentRepo = classStudentRepo;
        this.classService = classService;
    }

    public ScheduleDTO createSchedule(UUID classId, CreateScheduleRequestDTO req, User currentUser) {
        TuitionClass tc = classService.findClassOrThrow(classId);
        validateTimeRange(req.startTime(), req.endTime());
        if (req.effectiveUntil() != null && req.effectiveUntil().isBefore(req.effectiveFrom())) {
            throw new AppException(ErrorCode.INVALID_DATE, "effectiveUntil must be >= effectiveFrom");
        }
        if (scheduleRepo.existsActiveRecurringByClassIdAndDayOfWeek(classId, req.dayOfWeek())) {
            throw new AppException(ErrorCode.SCHEDULE_CONFLICT, "Active recurring schedule already exists for this day");
        }

        ClassSchedule schedule = new ClassSchedule();
        schedule.setTuitionClass(tc);
        schedule.setDayOfWeek(req.dayOfWeek());
        schedule.setStartTime(req.startTime());
        schedule.setEndTime(req.endTime());
        schedule.setLocation(req.location());
        schedule.setRecurring(true);
        schedule.setEffectiveFrom(req.effectiveFrom());
        schedule.setEffectiveUntil(req.effectiveUntil());
        schedule.setCreatedBy(currentUser);
        schedule = scheduleRepo.save(schedule);

        int sessionCount = 0;
        if (req.effectiveUntil() != null) {
            sessionCount = generateSessionsInternal(schedule, req.effectiveFrom(), req.effectiveUntil());
        }
        return toScheduleDTO(schedule, sessionCount);
    }

    public ScheduleDTO createOneOffSchedule(UUID classId, CreateOneOffScheduleRequestDTO req, User currentUser) {
        TuitionClass tc = classService.findClassOrThrow(classId);
        validateTimeRange(req.startTime(), req.endTime());

        ClassSchedule schedule = new ClassSchedule();
        schedule.setTuitionClass(tc);
        schedule.setDayOfWeek(null);
        schedule.setStartTime(req.startTime());
        schedule.setEndTime(req.endTime());
        schedule.setLocation(req.location());
        schedule.setRecurring(false);
        schedule.setEffectiveFrom(req.sessionDate());
        schedule.setEffectiveUntil(req.sessionDate());
        schedule.setCreatedBy(currentUser);
        schedule = scheduleRepo.save(schedule);

        ClassSession session = createSession(schedule, tc, req.sessionDate());
        prePopulateAttendance(session, tc.getId());
        return toScheduleDTO(schedule, 1);
    }

    @Transactional(readOnly = true)
    public List<ScheduleDTO> getClassSchedules(UUID classId, boolean activeOnly) {
        List<ClassSchedule> schedules = activeOnly
            ? scheduleRepo.findActiveByClassId(classId)
            : scheduleRepo.findByTuitionClassId(classId);
        return schedules.stream().map(s -> {
            long count = sessionRepo.findByTuitionClassId(classId, org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
            return toScheduleDTO(s, (int) count);
        }).toList();
    }

    public ScheduleDTO updateSchedule(UUID scheduleId, LocalDate effectiveUntil) {
        ClassSchedule schedule = findScheduleOrThrow(scheduleId);
        if (effectiveUntil != null) {
            schedule.setEffectiveUntil(effectiveUntil);
            // Auto-cancel future sessions beyond effective_until
            sessionRepo.cancelFutureSessionsBySchedule(scheduleId, effectiveUntil, "Schedule deactivated");
        }
        schedule = scheduleRepo.save(schedule);
        long count = sessionRepo.findByTuitionClassId(schedule.getTuitionClass().getId(), org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
        return toScheduleDTO(schedule, (int) count);
    }

    public List<SessionDTO> generateSessions(UUID scheduleId, GenerateSessionsRequestDTO req) {
        ClassSchedule schedule = findScheduleOrThrow(scheduleId);
        if (!schedule.isRecurring()) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Cannot generate sessions for a one-off schedule");
        }

        LocalDate startDate = sessionRepo.findLastSessionDateByScheduleId(scheduleId)
            .map(d -> d.plusDays(1))
            .orElse(schedule.getEffectiveFrom());

        if (!req.targetEndDate().isAfter(startDate.minusDays(1))) {
            throw new AppException(ErrorCode.INVALID_DATE, "Target end date must be after the last existing session");
        }

        List<ClassSession> newSessions = new ArrayList<>();
        LocalDate date = startDate;
        while (!date.isAfter(req.targetEndDate())) {
            if (date.getDayOfWeek().getValue() == schedule.getDayOfWeek()
                && !sessionRepo.existsByScheduleIdAndSessionDate(scheduleId, date)) {
                ClassSession session = createSession(schedule, schedule.getTuitionClass(), date);
                prePopulateAttendance(session, schedule.getTuitionClass().getId());
                newSessions.add(session);
            }
            date = date.plusDays(1);
        }
        return newSessions.stream().map(this::toSessionDTO).toList();
    }

    // --- Internal helpers ---

    private int generateSessionsInternal(ClassSchedule schedule, LocalDate from, LocalDate until) {
        int count = 0;
        LocalDate date = from;
        while (!date.isAfter(until)) {
            if (date.getDayOfWeek().getValue() == schedule.getDayOfWeek()
                && !sessionRepo.existsByScheduleIdAndSessionDate(schedule.getId(), date)) {
                ClassSession session = createSession(schedule, schedule.getTuitionClass(), date);
                prePopulateAttendance(session, schedule.getTuitionClass().getId());
                count++;
            }
            date = date.plusDays(1);
        }
        return count;
    }

    private ClassSession createSession(ClassSchedule schedule, TuitionClass tc, LocalDate date) {
        ClassSession session = new ClassSession();
        session.setSchedule(schedule);
        session.setTuitionClass(tc);
        session.setSessionDate(date);
        session.setStartTime(schedule.getStartTime());
        session.setEndTime(schedule.getEndTime());
        session.setLocation(schedule.getLocation());
        session.setStatus(SessionStatus.SCHEDULED);
        return sessionRepo.save(session);
    }

    private void prePopulateAttendance(ClassSession session, UUID classId) {
        List<ClassStudent> enrolled = classStudentRepo.findByTuitionClassIdAndStatus(classId, EnrollmentStatus.ACTIVE);
        List<SessionAttendance> records = enrolled.stream().map(cs -> {
            SessionAttendance a = new SessionAttendance();
            a.setSession(session);
            a.setStudent(cs.getStudent());
            a.setStatus(AttendanceStatus.UNMARKED);
            a.setStudentRsvp(RsvpStatus.ATTENDING);
            return a;
        }).toList();
        attendanceRepo.saveAll(records);
    }

    private void validateTimeRange(java.time.LocalTime start, java.time.LocalTime end) {
        if (!end.isAfter(start)) {
            throw new AppException(ErrorCode.INVALID_INPUT, "End time must be after start time");
        }
    }

    public ClassSchedule findScheduleOrThrow(UUID scheduleId) {
        return scheduleRepo.findById(scheduleId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Schedule not found"));
    }

    private ScheduleDTO toScheduleDTO(ClassSchedule s, int sessionCount) {
        String dayName = s.getDayOfWeek() != null
            ? DayOfWeek.of(s.getDayOfWeek()).getDisplayName(TextStyle.FULL, Locale.ENGLISH) : null;
        return new ScheduleDTO(s.getId(), s.getTuitionClass().getId(), s.getTuitionClass().getName(),
            s.getDayOfWeek(), dayName, s.getStartTime(), s.getEndTime(), s.getLocation(),
            s.isRecurring(), s.getEffectiveFrom(), s.getEffectiveUntil(), sessionCount, s.getCreatedAt());
    }

    SessionDTO toSessionDTO(ClassSession s) {
        List<SessionAttendance> att = attendanceRepo.findBySessionId(s.getId());
        int enrolled = att.size();
        int marked = (int) att.stream().filter(a -> a.getStatus() != AttendanceStatus.UNMARKED).count();
        int notAttending = (int) att.stream().filter(a -> a.getStudentRsvp() == RsvpStatus.NOT_ATTENDING).count();
        String dayName = s.getSessionDate().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        return new SessionDTO(s.getId(), s.getSchedule() != null ? s.getSchedule().getId() : null,
            s.getTuitionClass().getId(), s.getTuitionClass().getName(), s.getSessionDate(), dayName,
            s.getStartTime(), s.getEndTime(), s.getLocation(), s.getStatus().name(),
            s.getCancelReason(), enrolled, marked, notAttending, s.getCreatedAt());
    }
}
