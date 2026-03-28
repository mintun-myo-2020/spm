package com.eggtive.spm.scheduling.repository;

import com.eggtive.spm.scheduling.entity.SessionAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionAttendanceRepository extends JpaRepository<SessionAttendance, UUID> {

    List<SessionAttendance> findBySessionId(UUID sessionId);

    Optional<SessionAttendance> findBySessionIdAndStudentId(UUID sessionId, UUID studentId);

    // Student attendance stats for a class
    @Query("SELECT a FROM SessionAttendance a WHERE a.student.id = :studentId AND a.session.tuitionClass.id = :classId AND a.session.sessionDate >= :startDate AND (:endDate IS NULL OR a.session.sessionDate <= :endDate) AND a.session.status <> 'CANCELLED'")
    List<SessionAttendance> findByStudentAndClass(UUID studentId, UUID classId, LocalDate startDate, LocalDate endDate);

    // All attendance for a class (for class stats)
    @Query("SELECT a FROM SessionAttendance a WHERE a.session.tuitionClass.id = :classId AND a.session.sessionDate >= :startDate AND (:endDate IS NULL OR a.session.sessionDate <= :endDate) AND a.session.status <> 'CANCELLED'")
    List<SessionAttendance> findByClassId(UUID classId, LocalDate startDate, LocalDate endDate);

    // Count distinct sessions with at least one marked attendance
    @Query("SELECT COUNT(DISTINCT a.session.id) FROM SessionAttendance a WHERE a.session.tuitionClass.id = :classId AND a.status <> 'UNMARKED' AND a.session.sessionDate >= :startDate AND (:endDate IS NULL OR a.session.sessionDate <= :endDate)")
    long countSessionsWithAttendance(UUID classId, LocalDate startDate, LocalDate endDate);
}
