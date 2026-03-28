package com.eggtive.spm.scheduling.service;

import com.eggtive.spm.classmanagement.service.ClassService;
import com.eggtive.spm.common.enums.*;
import com.eggtive.spm.common.exception.AppException;
import com.eggtive.spm.scheduling.dto.*;
import com.eggtive.spm.scheduling.entity.ClassSession;
import com.eggtive.spm.scheduling.entity.SessionAttendance;
import com.eggtive.spm.scheduling.repository.ClassSessionRepository;
import com.eggtive.spm.scheduling.repository.SessionAttendanceRepository;
import com.eggtive.spm.classmanagement.entity.TuitionClass;
import com.eggtive.spm.user.entity.Student;
import com.eggtive.spm.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock private SessionAttendanceRepository attendanceRepo;
    @Mock private ClassSessionRepository sessionRepo;
    @Mock private ClassService classService;

    @InjectMocks private AttendanceService attendanceService;

    private UUID sessionId;
    private UUID classId;
    private User markedBy;

    @BeforeEach
    void setUp() {
        sessionId = UUID.randomUUID();
        classId = UUID.randomUUID();
        markedBy = new User();
        markedBy.setId(UUID.randomUUID());
        markedBy.setFirstName("Teacher");
        markedBy.setLastName("One");
    }

    private SessionAttendance buildAttendance(UUID studentId, AttendanceStatus status) {
        User user = new User();
        user.setFirstName("Student");
        user.setLastName("Name");
        Student student = new Student();
        student.setId(studentId);
        student.setUser(user);

        ClassSession session = new ClassSession();
        session.setId(sessionId);

        SessionAttendance att = new SessionAttendance();
        att.setId(UUID.randomUUID());
        att.setSession(session);
        att.setStudent(student);
        att.setStatus(status);
        att.setStudentRsvp(RsvpStatus.ATTENDING);
        return att;
    }

    // --- batchMarkAttendance tests ---

    @Test
    void batchMarkAttendance_updatesStatusForAllEntries() {
        UUID student1Id = UUID.randomUUID();
        UUID student2Id = UUID.randomUUID();
        SessionAttendance att1 = buildAttendance(student1Id, AttendanceStatus.UNMARKED);
        SessionAttendance att2 = buildAttendance(student2Id, AttendanceStatus.UNMARKED);

        when(attendanceRepo.findBySessionId(sessionId)).thenReturn(List.of(att1, att2));
        when(attendanceRepo.saveAll(anyCollection())).thenReturn(List.of(att1, att2));

        List<AttendanceEntryDTO> entries = List.of(
            new AttendanceEntryDTO(student1Id, "PRESENT"),
            new AttendanceEntryDTO(student2Id, "ABSENT")
        );

        List<AttendanceDTO> result = attendanceService.batchMarkAttendance(sessionId, entries, markedBy);

        assertThat(result).hasSize(2);
        assertThat(att1.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
        assertThat(att2.getStatus()).isEqualTo(AttendanceStatus.ABSENT);
        assertThat(att1.getMarkedBy()).isEqualTo(markedBy);
    }

    @Test
    void batchMarkAttendance_studentNotFound_throwsException() {
        UUID unknownStudentId = UUID.randomUUID();
        when(attendanceRepo.findBySessionId(sessionId)).thenReturn(List.of());

        List<AttendanceEntryDTO> entries = List.of(new AttendanceEntryDTO(unknownStudentId, "PRESENT"));

        assertThatThrownBy(() -> attendanceService.batchMarkAttendance(sessionId, entries, markedBy))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Attendance record not found for student");
    }

    // --- updateAttendance tests ---

    @Test
    void updateAttendance_updatesStatusSuccessfully() {
        UUID studentId = UUID.randomUUID();
        SessionAttendance att = buildAttendance(studentId, AttendanceStatus.UNMARKED);

        when(attendanceRepo.findBySessionIdAndStudentId(sessionId, studentId)).thenReturn(Optional.of(att));
        when(attendanceRepo.save(att)).thenReturn(att);

        AttendanceDTO result = attendanceService.updateAttendance(sessionId, studentId, "LATE", markedBy);

        assertThat(result.status()).isEqualTo("LATE");
        assertThat(att.getMarkedBy()).isEqualTo(markedBy);
    }

    @Test
    void updateAttendance_notFound_throwsException() {
        UUID studentId = UUID.randomUUID();
        when(attendanceRepo.findBySessionIdAndStudentId(sessionId, studentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.updateAttendance(sessionId, studentId, "PRESENT", markedBy))
            .isInstanceOf(AppException.class)
            .extracting(e -> ((AppException) e).getErrorCode())
            .isEqualTo(ErrorCode.NOT_FOUND);
    }

    // --- updateRsvp tests ---

    @Test
    void updateRsvp_scheduledFutureSession_updatesSuccessfully() {
        UUID studentId = UUID.randomUUID();
        SessionAttendance att = buildAttendance(studentId, AttendanceStatus.UNMARKED);

        ClassSession session = new ClassSession();
        session.setId(sessionId);
        session.setStatus(SessionStatus.SCHEDULED);
        session.setSessionDate(LocalDate.now().plusDays(7));

        when(attendanceRepo.findBySessionIdAndStudentId(sessionId, studentId)).thenReturn(Optional.of(att));
        when(sessionRepo.findById(sessionId)).thenReturn(Optional.of(session));
        when(attendanceRepo.save(att)).thenReturn(att);

        AttendanceDTO result = attendanceService.updateRsvp(sessionId, studentId, "NOT_ATTENDING", "Sick");

        assertThat(att.getStudentRsvp()).isEqualTo(RsvpStatus.NOT_ATTENDING);
        assertThat(att.getRsvpReason()).isEqualTo("Sick");
    }

    @Test
    void updateRsvp_cancelledSession_throwsException() {
        UUID studentId = UUID.randomUUID();
        SessionAttendance att = buildAttendance(studentId, AttendanceStatus.UNMARKED);

        ClassSession session = new ClassSession();
        session.setId(sessionId);
        session.setStatus(SessionStatus.CANCELLED);

        when(attendanceRepo.findBySessionIdAndStudentId(sessionId, studentId)).thenReturn(Optional.of(att));
        when(sessionRepo.findById(sessionId)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> attendanceService.updateRsvp(sessionId, studentId, "NOT_ATTENDING", null))
            .isInstanceOf(AppException.class)
            .extracting(e -> ((AppException) e).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_RSVP);
    }

    @Test
    void updateRsvp_pastSession_throwsException() {
        UUID studentId = UUID.randomUUID();
        SessionAttendance att = buildAttendance(studentId, AttendanceStatus.UNMARKED);

        ClassSession session = new ClassSession();
        session.setId(sessionId);
        session.setStatus(SessionStatus.SCHEDULED);
        session.setSessionDate(LocalDate.now().minusDays(1));

        when(attendanceRepo.findBySessionIdAndStudentId(sessionId, studentId)).thenReturn(Optional.of(att));
        when(sessionRepo.findById(sessionId)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> attendanceService.updateRsvp(sessionId, studentId, "NOT_ATTENDING", null))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Cannot RSVP for past sessions");
    }

    @Test
    void updateRsvp_attendingStatus_clearsReason() {
        UUID studentId = UUID.randomUUID();
        SessionAttendance att = buildAttendance(studentId, AttendanceStatus.UNMARKED);
        att.setStudentRsvp(RsvpStatus.NOT_ATTENDING);
        att.setRsvpReason("Was sick");

        ClassSession session = new ClassSession();
        session.setId(sessionId);
        session.setStatus(SessionStatus.SCHEDULED);
        session.setSessionDate(LocalDate.now().plusDays(3));

        when(attendanceRepo.findBySessionIdAndStudentId(sessionId, studentId)).thenReturn(Optional.of(att));
        when(sessionRepo.findById(sessionId)).thenReturn(Optional.of(session));
        when(attendanceRepo.save(att)).thenReturn(att);

        attendanceService.updateRsvp(sessionId, studentId, "ATTENDING", "some reason");

        assertThat(att.getStudentRsvp()).isEqualTo(RsvpStatus.ATTENDING);
        assertThat(att.getRsvpReason()).isNull(); // reason cleared for ATTENDING
    }

    // --- getStudentAttendanceStats tests ---

    @Test
    void getStudentAttendanceStats_calculatesRateCorrectly() {
        UUID studentId = UUID.randomUUID();
        // 3 PRESENT, 1 LATE, 1 ABSENT = rate = (3+1)/5 * 100 = 80%
        List<SessionAttendance> records = List.of(
            buildAttendance(studentId, AttendanceStatus.PRESENT),
            buildAttendance(studentId, AttendanceStatus.PRESENT),
            buildAttendance(studentId, AttendanceStatus.PRESENT),
            buildAttendance(studentId, AttendanceStatus.LATE),
            buildAttendance(studentId, AttendanceStatus.ABSENT)
        );

        when(attendanceRepo.findByStudentAndClass(eq(studentId), eq(classId), any(), isNull()))
            .thenReturn(records);

        StudentAttendanceStatsDTO stats = attendanceService.getStudentAttendanceStats(studentId, classId, null, null);

        assertThat(stats.totalSessions()).isEqualTo(5);
        assertThat(stats.presentCount()).isEqualTo(3);
        assertThat(stats.lateCount()).isEqualTo(1);
        assertThat(stats.absentCount()).isEqualTo(1);
        assertThat(stats.attendanceRate()).isCloseTo(80.0, within(0.01));
    }

    @Test
    void getStudentAttendanceStats_noRecords_returnsZeroRate() {
        UUID studentId = UUID.randomUUID();
        when(attendanceRepo.findByStudentAndClass(eq(studentId), eq(classId), any(), isNull()))
            .thenReturn(List.of());

        StudentAttendanceStatsDTO stats = attendanceService.getStudentAttendanceStats(studentId, classId, null, null);

        assertThat(stats.totalSessions()).isEqualTo(0);
        assertThat(stats.attendanceRate()).isEqualTo(0.0);
    }

    @Test
    void getStudentAttendanceStats_unmarkedNotCountedInTotal() {
        UUID studentId = UUID.randomUUID();
        List<SessionAttendance> records = List.of(
            buildAttendance(studentId, AttendanceStatus.PRESENT),
            buildAttendance(studentId, AttendanceStatus.UNMARKED)
        );

        when(attendanceRepo.findByStudentAndClass(eq(studentId), eq(classId), any(), isNull()))
            .thenReturn(records);

        StudentAttendanceStatsDTO stats = attendanceService.getStudentAttendanceStats(studentId, classId, null, null);

        assertThat(stats.totalSessions()).isEqualTo(1); // UNMARKED not counted
        assertThat(stats.unmarkedCount()).isEqualTo(1);
        assertThat(stats.attendanceRate()).isCloseTo(100.0, within(0.01));
    }
}
