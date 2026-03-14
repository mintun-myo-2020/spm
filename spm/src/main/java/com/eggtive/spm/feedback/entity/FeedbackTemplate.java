package com.eggtive.spm.feedback.entity;

import com.eggtive.spm.common.entity.BaseEntity;
import com.eggtive.spm.common.enums.FeedbackCategory;
import com.eggtive.spm.user.entity.Teacher;
import jakarta.persistence.*;

@Entity
@Table(name = "feedback_templates")
public class FeedbackTemplate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedbackCategory category;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private boolean isSystemWide = false;

    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }
    public FeedbackCategory getCategory() { return category; }
    public void setCategory(FeedbackCategory category) { this.category = category; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isSystemWide() { return isSystemWide; }
    public void setSystemWide(boolean systemWide) { isSystemWide = systemWide; }
}
