package com.eggtive.spm.testscore.repository;

import com.eggtive.spm.testscore.entity.TestScore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TestScoreRepository extends JpaRepository<TestScore, UUID> {

    @Query("""
        SELECT ts FROM TestScore ts
        WHERE ts.student.id = :studentId
        AND ts.isDraft = :isDraft
        AND (:startDate IS NULL OR ts.testDate >= :startDate)
        AND (:endDate IS NULL OR ts.testDate <= :endDate)
        AND (:classId IS NULL OR ts.tuitionClass.id = :classId)
        """)
    Page<TestScore> findByStudentWithFilters(
        UUID studentId, LocalDate startDate, LocalDate endDate,
        UUID classId, boolean isDraft, Pageable pageable);

    @Query("SELECT ts FROM TestScore ts WHERE ts.student.id = :studentId AND ts.isDraft = false ORDER BY ts.testDate ASC")
    List<TestScore> findByStudentIdOrderByTestDateAsc(UUID studentId);

    @Query("SELECT ts FROM TestScore ts WHERE ts.tuitionClass.id = :classId AND ts.isDraft = false ORDER BY ts.testDate ASC")
    List<TestScore> findByClassIdOrderByTestDateAsc(UUID classId);
}
