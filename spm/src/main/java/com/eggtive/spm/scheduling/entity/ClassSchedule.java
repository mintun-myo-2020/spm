package com.eggtive.spm.scheduling.entity;

import com.eggtive.spm.classmanagement.entity.TuitionClass;
import com.eggtive.spm.common.entity.BaseEntity;
import com.eggtive.spm.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "class_schedules")
public class ClassSchedule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private TuitionClass tuitionClass;

    private Integer dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    private String location;

    @Column(nullable = false)
    private boolean isRecurring = true;

    @Column(nullable = false)
    private LocalDate effectiveFrom;

    private LocalDate effectiveUntil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    public TuitionClass getTuitionClass() { return tuitionClass; }
    public void setTuitionClass(TuitionClass tuitionClass) { this.tuitionClass = tuitionClass; }
    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public boolean isRecurring() { return isRecurring; }
    public void setRecurring(boolean recurring) { isRecurring = recurring; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }
    public LocalDate getEffectiveUntil() { return effectiveUntil; }
    public void setEffectiveUntil(LocalDate effectiveUntil) { this.effectiveUntil = effectiveUntil; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
}
