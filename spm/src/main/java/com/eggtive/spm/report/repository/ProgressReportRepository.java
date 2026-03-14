package com.eggtive.spm.report.repository;

import com.eggtive.spm.report.entity.ProgressReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProgressReportRepository extends JpaRepository<ProgressReport, UUID> {
    Page<ProgressReport> findByStudentId(UUID studentId, Pageable pageable);
}
