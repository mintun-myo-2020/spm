package com.eggtive.spm.scheduling.repository;

import com.eggtive.spm.common.enums.SessionStatus;
import com.eggtive.spm.scheduling.entity.ClassSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClassSessionRepository extends JpaRepository<ClassSession, UUID> {

    Page<ClassSession> findByTuitionClassId(UUID classId, Pageable pageable);

    @Query("SELECT s FROM ClassSession s WHERE s.tuitionClass.id = :classId AND s.sessionDate >= :startDate AND (:endDate IS NULL OR s.sessionDate <= :endDate) AND (:status IS NULL OR s.status = :status)")
    Page<ClassSession> findByClassIdFiltered(UUID classId, LocalDate startDate, LocalDate endDate, SessionStatus status, Pageable pageable);

    // Teacher: upcoming sessions for their classes
    @Query("SELECT s FROM ClassSession s WHERE s.tuitionClass.teacher.id = :teacherId AND s.sessionDate >= :fromDate AND s.status = 'SCHEDULED' ORDER BY s.sessionDate, s.startTime")
    Page<ClassSession> findUpcomingForTeacher(UUID teacherId, LocalDate fromDate, Pageable pageable);

    // Student: upcoming sessions for enrolled classes
    @Query("SELECT s FROM ClassSession s WHERE s.tuitionClass.id IN (SELECT cs.tuitionClass.id FROM ClassStudent cs WHERE cs.student.id = :studentId AND cs.status = 'ACTIVE') AND s.sessionDate >= :fromDate AND s.status = 'SCHEDULED' ORDER BY s.sessionDate, s.startTime")
    Page<ClassSession> findUpcomingForStudent(UUID studentId, LocalDate fromDate, Pageable pageable);

    // Parent: upcoming sessions for linked children's enrolled classes
    @Query("SELECT s FROM ClassSession s WHERE s.tuitionClass.id IN (SELECT cs.tuitionClass.id FROM ClassStudent cs WHERE cs.student.id IN (SELECT st.id FROM Student st WHERE st.parent.id = :parentId) AND cs.status = 'ACTIVE') AND s.sessionDate >= :fromDate AND s.status = 'SCHEDULED' ORDER BY s.sessionDate, s.startTime")
    Page<ClassSession> findUpcomingForParent(UUID parentId, LocalDate fromDate, Pageable pageable);

    // Admin: all upcoming
    @Query("SELECT s FROM ClassSession s WHERE s.sessionDate >= :fromDate AND s.status = 'SCHEDULED' ORDER BY s.sessionDate, s.startTime")
    Page<ClassSession> findAllUpcoming(LocalDate fromDate, Pageable pageable);

    // For session generation: check if session already exists for schedule + date
    boolean existsByScheduleIdAndSessionDate(UUID scheduleId, LocalDate sessionDate);

    // Find last session date for a schedule
    @Query("SELECT MAX(s.sessionDate) FROM ClassSession s WHERE s.schedule.id = :scheduleId")
    Optional<LocalDate> findLastSessionDateByScheduleId(UUID scheduleId);

    // For conflict check
    List<ClassSession> findByTuitionClassIdAndSessionDateAndStatusNot(UUID classId, LocalDate date, SessionStatus excludeStatus);

    // Auto-cancel future sessions on deactivation
    @Modifying
    @Query("UPDATE ClassSession s SET s.status = 'CANCELLED', s.cancelReason = :reason WHERE s.schedule.id = :scheduleId AND s.sessionDate > :afterDate AND s.status = 'SCHEDULED'")
    int cancelFutureSessionsBySchedule(UUID scheduleId, LocalDate afterDate, String reason);

    // Sessions with notes for a class (any note field non-null), sorted by date desc
    @Query("SELECT s FROM ClassSession s WHERE s.tuitionClass.id = :classId ORDER BY s.sessionDate DESC, s.startTime DESC")
    Page<ClassSession> findByClassIdWithNotes(UUID classId, Pageable pageable);
}
