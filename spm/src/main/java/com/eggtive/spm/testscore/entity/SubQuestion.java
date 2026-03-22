package com.eggtive.spm.testscore.entity;

import com.eggtive.spm.common.entity.BaseEntity;
import com.eggtive.spm.subject.entity.Topic;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "sub_questions")
public class SubQuestion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false)
    private String subQuestionLabel;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal score;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal maxScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(columnDefinition = "TEXT")
    private String studentAnswer;

    @Column(columnDefinition = "TEXT")
    private String teacherRemarks;

    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public String getSubQuestionLabel() { return subQuestionLabel; }
    public void setSubQuestionLabel(String label) { this.subQuestionLabel = label; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public BigDecimal getMaxScore() { return maxScore; }
    public void setMaxScore(BigDecimal maxScore) { this.maxScore = maxScore; }
    public Topic getTopic() { return topic; }
    public void setTopic(Topic topic) { this.topic = topic; }
    public String getStudentAnswer() { return studentAnswer; }
    public void setStudentAnswer(String studentAnswer) { this.studentAnswer = studentAnswer; }
    public String getTeacherRemarks() { return teacherRemarks; }
    public void setTeacherRemarks(String teacherRemarks) { this.teacherRemarks = teacherRemarks; }
}
