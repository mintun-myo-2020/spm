package com.eggtive.spm.testscore.repository;

import com.eggtive.spm.testscore.entity.TestScore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.UUID;

public interface TestScoreRepository extends JpaRepository<TestScore, UUID> {

    @Query("""
        SELECT ts FROM TestScore ts
        WHERE ts.student.id = :studentId
        AND (:startDate IS NULL OR ts.testDate >= :startDate)
        AND (:endDate IS NULL OR ts.testDate <= :endDate)
        AND (:classId IS NULL OR ts.tuitionClass.id = :classId)
        """)
    Page<TestScore> findByStudentWithFilters(
        UUID studentId, LocalDate startDate, LocalDate endDate,
        UUID classId, Pageable pageable);

    @Query("SELECT ts FROM TestScore ts WHERE ts.student.id = :studentId ORDER BY ts.testDate ASC")
    java.util.List<TestScore> findByStudentIdOrderByTestDateAsc(UUID studentId);
}
