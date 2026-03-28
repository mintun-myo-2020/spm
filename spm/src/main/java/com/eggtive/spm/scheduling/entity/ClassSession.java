package com.eggtive.spm.scheduling.entity;

import com.eggtive.spm.classmanagement.entity.TuitionClass;
import com.eggtive.spm.common.entity.BaseEntity;
import com.eggtive.spm.common.enums.SessionStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "class_sessions")
public class ClassSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private ClassSchedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private TuitionClass tuitionClass;

    @Column(nullable = false)
    private LocalDate sessionDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.SCHEDULED;

    @Column(columnDefinition = "TEXT")
    private String cancelReason;

    @Column(columnDefinition = "TEXT")
    private String topicCovered;

    @Column(columnDefinition = "TEXT")
    private String homeworkGiven;

    @Column(columnDefinition = "TEXT")
    private String commonWeaknesses;

    @Column(columnDefinition = "TEXT")
    private String additionalNotes;

    public ClassSchedule getSchedule() { return schedule; }
    public void setSchedule(ClassSchedule schedule) { this.schedule = schedule; }
    public TuitionClass getTuitionClass() { return tuitionClass; }
    public void setTuitionClass(TuitionClass tuitionClass) { this.tuitionClass = tuitionClass; }
    public LocalDate getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public String getTopicCovered() { return topicCovered; }
    public void setTopicCovered(String topicCovered) { this.topicCovered = topicCovered; }
    public String getHomeworkGiven() { return homeworkGiven; }
    public void setHomeworkGiven(String homeworkGiven) { this.homeworkGiven = homeworkGiven; }
    public String getCommonWeaknesses() { return commonWeaknesses; }
    public void setCommonWeaknesses(String commonWeaknesses) { this.commonWeaknesses = commonWeaknesses; }
    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }
}
