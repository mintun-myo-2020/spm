package com.eggtive.spm.testscore.entity;

import com.eggtive.spm.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
public class Question extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_score_id", nullable = false)
    private TestScore testScore;

    @Column(nullable = false)
    private String questionNumber;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal maxScore;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubQuestion> subQuestions = new ArrayList<>();

    public TestScore getTestScore() { return testScore; }
    public void setTestScore(TestScore testScore) { this.testScore = testScore; }
    public String getQuestionNumber() { return questionNumber; }
    public void setQuestionNumber(String questionNumber) { this.questionNumber = questionNumber; }
    public BigDecimal getMaxScore() { return maxScore; }
    public void setMaxScore(BigDecimal maxScore) { this.maxScore = maxScore; }
    public List<SubQuestion> getSubQuestions() { return subQuestions; }
}
