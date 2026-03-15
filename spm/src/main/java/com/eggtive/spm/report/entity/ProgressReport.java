package com.eggtive.spm.report.entity;

import com.eggtive.spm.common.entity.BaseEntity;
import com.eggtive.spm.user.entity.Student;
import com.eggtive.spm.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "progress_reports")
public class ProgressReport extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by", nullable = false)
    private User generatedBy;

    @Column(nullable = false)
    private String reportType;

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    @Column(name = "storage_location", nullable = false)
    private String storageLocation;

    @Column(nullable = false)
    private Instant generatedAt;

    @Column(nullable = false)
    private Instant expiresAt;

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public User getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(User user) { this.generatedBy = user; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }
    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
    public Instant getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
