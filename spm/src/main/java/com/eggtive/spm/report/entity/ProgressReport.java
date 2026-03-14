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

    @Column(nullable = false)
    private String s3Key;

    @Column(nullable = false)
    private String s3Bucket;

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
    public String getS3Key() { return s3Key; }
    public void setS3Key(String s3Key) { this.s3Key = s3Key; }
    public String getS3Bucket() { return s3Bucket; }
    public void setS3Bucket(String s3Bucket) { this.s3Bucket = s3Bucket; }
    public Instant getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
