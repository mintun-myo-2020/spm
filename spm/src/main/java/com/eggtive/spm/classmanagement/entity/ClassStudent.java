package com.eggtive.spm.classmanagement.entity;

import com.eggtive.spm.common.entity.BaseEntity;
import com.eggtive.spm.common.enums.EnrollmentStatus;
import com.eggtive.spm.user.entity.Student;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "class_students")
public class ClassStudent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private TuitionClass tuitionClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private LocalDate enrollmentDate;

    private LocalDate withdrawalDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    public TuitionClass getTuitionClass() { return tuitionClass; }
    public void setTuitionClass(TuitionClass tuitionClass) { this.tuitionClass = tuitionClass; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }
    public LocalDate getWithdrawalDate() { return withdrawalDate; }
    public void setWithdrawalDate(LocalDate withdrawalDate) { this.withdrawalDate = withdrawalDate; }
    public EnrollmentStatus getStatus() { return status; }
    public void setStatus(EnrollmentStatus status) { this.status = status; }
}
