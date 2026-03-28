package com.eggtive.spm.scheduling.entity;

import com.eggtive.spm.common.entity.BaseEntity;
import com.eggtive.spm.common.enums.AttendanceStatus;
import com.eggtive.spm.common.enums.RsvpStatus;
import com.eggtive.spm.user.entity.Student;
import com.eggtive.spm.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "session_attendance", uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "student_id"}))
public class SessionAttendance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ClassSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status = AttendanceStatus.UNMARKED;

    @Enumerated(EnumType.STRING)
    @Column(name = "student_rsvp", nullable = false)
    private RsvpStatus studentRsvp = RsvpStatus.ATTENDING;

    @Column(columnDefinition = "TEXT")
    private String rsvpReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marked_by")
    private User markedBy;

    private Instant markedAt;

    public ClassSession getSession() { return session; }
    public void setSession(ClassSession session) { this.session = session; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
    public RsvpStatus getStudentRsvp() { return studentRsvp; }
    public void setStudentRsvp(RsvpStatus studentRsvp) { this.studentRsvp = studentRsvp; }
    public String getRsvpReason() { return rsvpReason; }
    public void setRsvpReason(String rsvpReason) { this.rsvpReason = rsvpReason; }
    public User getMarkedBy() { return markedBy; }
    public void setMarkedBy(User markedBy) { this.markedBy = markedBy; }
    public Instant getMarkedAt() { return markedAt; }
    public void setMarkedAt(Instant markedAt) { this.markedAt = markedAt; }
}
