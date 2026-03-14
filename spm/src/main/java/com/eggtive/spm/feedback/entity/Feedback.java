package com.eggtive.spm.feedback.entity;

import com.eggtive.spm.common.entity.BaseEntity;
import com.eggtive.spm.testscore.entity.TestScore;
import com.eggtive.spm.user.entity.Student;
import com.eggtive.spm.user.entity.Teacher;
import com.eggtive.spm.user.entity.User;
import jakarta.persistence.*;

@Entity
@Table(name = "feedback")
public class Feedback extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_score_id", nullable = false)
    private TestScore testScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(columnDefinition = "TEXT")
    private String strengths;

    @Column(columnDefinition = "TEXT")
    private String areasForImprovement;

    @Column(columnDefinition = "TEXT")
    private String recommendations;

    @Column(columnDefinition = "TEXT")
    private String additionalNotes;

    @Column(nullable = false)
    private boolean isEdited = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", nullable = false)
    private User updatedBy;

    public TestScore getTestScore() { return testScore; }
    public void setTestScore(TestScore ts) { this.testScore = ts; }
    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public String getStrengths() { return strengths; }
    public void setStrengths(String strengths) { this.strengths = strengths; }
    public String getAreasForImprovement() { return areasForImprovement; }
    public void setAreasForImprovement(String s) { this.areasForImprovement = s; }
    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String s) { this.recommendations = s; }
    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String s) { this.additionalNotes = s; }
    public boolean isEdited() { return isEdited; }
    public void setEdited(boolean edited) { isEdited = edited; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public User getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(User updatedBy) { this.updatedBy = updatedBy; }
}
