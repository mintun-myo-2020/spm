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
import com.eggtive.spm.user.entity.Student;
import com.eggtive.spm.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock private ClassScheduleRepository scheduleRepo;
    @Mock private ClassSessionRepository sessionRepo;
    @Mock private SessionAttendanceRepository attendanceRepo;
    @Mock private ClassStudentRepository classStudentRepo;
    @Mock private ClassService classService;

    @InjectMocks private ScheduleService scheduleService;

    private UUID classId;
    private TuitionClass tuitionClass;
    private User currentUser;

    @BeforeEach
    void setUp() {
        classId = UUID.randomUUID();
        tuitionClass = new TuitionClass();
        tuitionClass.setId(classId);
        tuitionClass.setName("Math 101");

        currentUser = new User();
        currentUser.setId(UUID.randomUUID());
        currentUser.setFirstName("Test");
        currentUser.setLastName("Teacher");
    }

    // --- createSchedule tests ---

    @Test
    void createSchedule_success_withEffectiveUntil_generatesSessionsOnMatchingDays() {
        // Monday = 1, effectiveFrom is a Monday, effectiveUntil is 2 weeks later (3 Mondays total)
        LocalDate from = LocalDate.of(2026, 1, 5); // Monday
        LocalDate until = LocalDate.of(2026, 1, 19); // Monday, 2 weeks later
        var req = new CreateScheduleRequestDTO(1, LocalTime.of(9, 0), LocalTime.of(10, 0),
            "Room A", from, until);

        when(classService.findClassOrThrow(classId)).thenReturn(tuitionClass);
        when(scheduleRepo.existsActiveRecurringByClassIdAndDayOfWeek(classId, 1)).thenReturn(false);
        when(scheduleRepo.save(any(ClassSchedule.class))).thenAnswer(inv -> {
            ClassSchedule s = inv.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });
        when(sessionRepo.existsByScheduleIdAndSessionDate(any(), any())).thenReturn(false);
        when(sessionRepo.save(any(ClassSession.class))).thenAnswer(inv -> {
            ClassSession s = inv.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });
        when(classStudentRepo.findByTuitionClassIdAndStatus(eq(classId), eq(EnrollmentStatus.ACTIVE)))
            .thenReturn(List.of());

        ScheduleDTO result = scheduleService.createSchedule(classId, req, currentUser);

        assertThat(result).isNotNull();
        assertThat(result.isRecurring()).isTrue();
        assertThat(result.dayOfWeek()).isEqualTo(1);
        assertThat(result.sessionCount()).isEqualTo(3); // Jan 5, 12, 19
        verify(sessionRepo, times(3)).save(any(ClassSession.class));
    }

    @Test
    void createSchedule_withoutEffectiveUntil_generatesZeroSessions() {
        var req = new CreateScheduleRequestDTO(3, LocalTime.of(14, 0), LocalTime.of(15, 0),
            "Room B", LocalDate.of(2026, 2, 1), null);

        when(classService.findClassOrThrow(classId)).thenReturn(tuitionClass);
        when(scheduleRepo.existsActiveRecurringByClassIdAndDayOfWeek(classId, 3)).thenReturn(false);
        when(scheduleRepo.save(any(ClassSchedule.class))).thenAnswer(inv -> {
            ClassSchedule s = inv.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        ScheduleDTO result = scheduleService.createSchedule(classId, req, currentUser);

        assertThat(result.sessionCount()).isEqualTo(0);
        verify(sessionRepo, never()).save(any());
    }

    @Test
    void createSchedule_endTimeBeforeStartTime_throwsException() {
        var req = new CreateScheduleRequestDTO(1, LocalTime.of(10, 0), LocalTime.of(9, 0),
            "Room A", LocalDate.of(2026, 1, 5), null);

        when(classService.findClassOrThrow(classId)).thenReturn(tuitionClass);

        assertThatThrownBy(() -> scheduleService.createSchedule(classId, req, currentUser))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("End time must be after start time");
    }

    @Test
    void createSchedule_effectiveUntilBeforeFrom_throwsException() {
        var req = new CreateScheduleRequestDTO(1, LocalTime.of(9, 0), LocalTime.of(10, 0),
            "Room A", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1));

        when(classService.findClassOrThrow(classId)).thenReturn(tuitionClass);

        assertThatThrownBy(() -> scheduleService.createSchedule(classId, req, currentUser))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("effectiveUntil must be >= effectiveFrom");
    }

    @Test
    void createSchedule_duplicateDayOfWeek_throwsConflict() {
        var req = new CreateScheduleRequestDTO(1, LocalTime.of(9, 0), LocalTime.of(10, 0),
            "Room A", LocalDate.of(2026, 1, 5), null);

        when(classService.findClassOrThrow(classId)).thenReturn(tuitionClass);
        when(scheduleRepo.existsActiveRecurringByClassIdAndDayOfWeek(classId, 1)).thenReturn(true);

        assertThatThrownBy(() -> scheduleService.createSchedule(classId, req, currentUser))
            .isInstanceOf(AppException.class)
            .extracting(e -> ((AppException) e).getErrorCode())
            .isEqualTo(ErrorCode.SCHEDULE_CONFLICT);
    }

    // --- createOneOffSchedule tests ---

    @Test
    void createOneOffSchedule_createsExactlyOneSession() {
        var req = new CreateOneOffScheduleRequestDTO(LocalDate.of(2026, 3, 15),
            LocalTime.of(10, 0), LocalTime.of(11, 0), "Lab 1");

        when(classService.findClassOrThrow(classId)).thenReturn(tuitionClass);
        when(scheduleRepo.save(any(ClassSchedule.class))).thenAnswer(inv -> {
            ClassSchedule s = inv.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });
        when(sessionRepo.save(any(ClassSession.class))).thenAnswer(inv -> {
            ClassSession s = inv.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });
        when(classStudentRepo.findByTuitionClassIdAndStatus(eq(classId), eq(EnrollmentStatus.ACTIVE)))
            .thenReturn(List.of());

        ScheduleDTO result = scheduleService.createOneOffSchedule(classId, req, currentUser);

        assertThat(result.isRecurring()).isFalse();
        assertThat(result.sessionCount()).isEqualTo(1);
        verify(sessionRepo, times(1)).save(any(ClassSession.class));
    }

    // --- generateSessions tests ---

    @Test
    void generateSessions_forRecurringSchedule_generatesCorrectDays() {
        UUID scheduleId = UUID.randomUUID();
        ClassSchedule schedule = new ClassSchedule();
        schedule.setId(scheduleId);
        schedule.setTuitionClass(tuitionClass);
        schedule.setDayOfWeek(3); // Wednesday
        schedule.setStartTime(LocalTime.of(14, 0));
        schedule.setEndTime(LocalTime.of(15, 0));
        schedule.setLocation("Room C");
        schedule.setRecurring(true);
        schedule.setEffectiveFrom(LocalDate.of(2026, 1, 1));

        when(scheduleRepo.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(sessionRepo.findLastSessionDateByScheduleId(scheduleId)).thenReturn(Optional.empty());
        when(sessionRepo.existsByScheduleIdAndSessionDate(any(), any())).thenReturn(false);
        when(sessionRepo.save(any(ClassSession.class))).thenAnswer(inv -> {
            ClassSession s = inv.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });
        when(attendanceRepo.findBySessionId(any())).thenReturn(List.of());
        when(classStudentRepo.findByTuitionClassIdAndStatus(eq(classId), eq(EnrollmentStatus.ACTIVE)))
            .thenReturn(List.of());

        // Generate for January 2026 — Wednesdays are Jan 7, 14, 21, 28
        var req = new GenerateSessionsRequestDTO(LocalDate.of(2026, 1, 31));
        List<SessionDTO> sessions = scheduleService.generateSessions(scheduleId, req);

        // Jan 7, 14, 21, 28 = 4 Wednesdays
        assertThat(sessions).hasSize(4);
    }

    @Test
    void generateSessions_forOneOffSchedule_throwsException() {
        UUID scheduleId = UUID.randomUUID();
        ClassSchedule schedule = new ClassSchedule();
        schedule.setId(scheduleId);
        schedule.setRecurring(false);

        when(scheduleRepo.findById(scheduleId)).thenReturn(Optional.of(schedule));

        var req = new GenerateSessionsRequestDTO(LocalDate.of(2026, 2, 28));
        assertThatThrownBy(() -> scheduleService.generateSessions(scheduleId, req))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Cannot generate sessions for a one-off schedule");
    }

    @Test
    void generateSessions_skipsExistingDates() {
        UUID scheduleId = UUID.randomUUID();
        ClassSchedule schedule = new ClassSchedule();
        schedule.setId(scheduleId);
        schedule.setTuitionClass(tuitionClass);
        schedule.setDayOfWeek(1); // Monday
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(10, 0));
        schedule.setLocation("Room A");
        schedule.setRecurring(true);
        schedule.setEffectiveFrom(LocalDate.of(2026, 1, 5));

        when(scheduleRepo.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(sessionRepo.findLastSessionDateByScheduleId(scheduleId)).thenReturn(Optional.empty());
        // First Monday already exists
        when(sessionRepo.existsByScheduleIdAndSessionDate(scheduleId, LocalDate.of(2026, 1, 5))).thenReturn(true);
        when(sessionRepo.existsByScheduleIdAndSessionDate(eq(scheduleId), argThat(d -> !d.equals(LocalDate.of(2026, 1, 5))))).thenReturn(false);
        when(sessionRepo.save(any(ClassSession.class))).thenAnswer(inv -> {
            ClassSession s = inv.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });
        when(attendanceRepo.findBySessionId(any())).thenReturn(List.of());
        when(classStudentRepo.findByTuitionClassIdAndStatus(eq(classId), eq(EnrollmentStatus.ACTIVE)))
            .thenReturn(List.of());

        var req = new GenerateSessionsRequestDTO(LocalDate.of(2026, 1, 19));
        List<SessionDTO> sessions = scheduleService.generateSessions(scheduleId, req);

        // Jan 5 skipped (exists), Jan 12 and 19 generated
        assertThat(sessions).hasSize(2);
    }

    // --- prePopulateAttendance tests ---

    @Test
    void createSchedule_prePopulatesAttendanceForEnrolledStudents() {
        var req = new CreateOneOffScheduleRequestDTO(LocalDate.of(2026, 3, 15),
            LocalTime.of(10, 0), LocalTime.of(11, 0), "Lab 1");

        Student student1 = new Student();
        student1.setId(UUID.randomUUID());
        Student student2 = new Student();
        student2.setId(UUID.randomUUID());

        ClassStudent cs1 = new ClassStudent();
        cs1.setStudent(student1);
        ClassStudent cs2 = new ClassStudent();
        cs2.setStudent(student2);

        when(classService.findClassOrThrow(classId)).thenReturn(tuitionClass);
        when(scheduleRepo.save(any())).thenAnswer(inv -> { ClassSchedule s = inv.getArgument(0); s.setId(UUID.randomUUID()); return s; });
        when(sessionRepo.save(any())).thenAnswer(inv -> { ClassSession s = inv.getArgument(0); s.setId(UUID.randomUUID()); return s; });
        when(classStudentRepo.findByTuitionClassIdAndStatus(classId, EnrollmentStatus.ACTIVE))
            .thenReturn(List.of(cs1, cs2));

        scheduleService.createOneOffSchedule(classId, req, currentUser);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SessionAttendance>> captor = ArgumentCaptor.forClass(List.class);
        verify(attendanceRepo).saveAll(captor.capture());
        List<SessionAttendance> records = captor.getValue();
        assertThat(records).hasSize(2);
        assertThat(records).allSatisfy(a -> {
            assertThat(a.getStatus()).isEqualTo(AttendanceStatus.UNMARKED);
            assertThat(a.getStudentRsvp()).isEqualTo(RsvpStatus.ATTENDING);
        });
    }

    // --- findScheduleOrThrow tests ---

    @Test
    void findScheduleOrThrow_notFound_throwsException() {
        UUID id = UUID.randomUUID();
        when(scheduleRepo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scheduleService.findScheduleOrThrow(id))
            .isInstanceOf(AppException.class)
            .extracting(e -> ((AppException) e).getErrorCode())
            .isEqualTo(ErrorCode.NOT_FOUND);
    }
}
