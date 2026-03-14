package com.eggtive.spm.testscore.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TestScoreDTO(
    UUID id, UUID studentId, String studentName,
    UUID classId, String className,
    UUID teacherId, String teacherName,
    String testName, LocalDate testDate,
    BigDecimal overallScore, BigDecimal maxScore,
    List<QuestionDTO> questions,
    Instant createdAt, Instant updatedAt
) {
    public record QuestionDTO(
        UUID id, String questionNumber, BigDecimal maxScore,
        List<SubQuestionDTO> subQuestions
    ) {}

    public record SubQuestionDTO(
        UUID id, String label, BigDecimal score, BigDecimal maxScore,
        UUID topicId, String topicName
    ) {}
}
