package com.eggtive.spm.classmanagement.entity;

import com.eggtive.spm.common.entity.BaseEntity;
import com.eggtive.spm.subject.entity.Subject;
import com.eggtive.spm.user.entity.Teacher;
import jakarta.persistence.*;

@Entity
@Table(name = "classes")
public class TuitionClass extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int maxStudents = 100;

    @Column(nullable = false)
    private boolean isActive = true;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getMaxStudents() { return maxStudents; }
    public void setMaxStudents(int maxStudents) { this.maxStudents = maxStudents; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
