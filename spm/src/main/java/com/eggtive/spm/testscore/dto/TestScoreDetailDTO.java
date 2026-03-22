package com.eggtive.spm.testscore.dto;

import com.eggtive.spm.feedback.dto.FeedbackDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Flat DTO that extends TestScoreDTO fields with optional feedback.
 */
public record TestScoreDetailDTO(
    UUID id, UUID studentId, String studentName,
    UUID classId, String className,
    UUID teacherId, String teacherName,
    String testName, LocalDate testDate,
    BigDecimal overallScore, BigDecimal maxScore,
    String testSource,
    List<TestScoreDTO.QuestionDTO> questions,
    Instant createdAt, Instant updatedAt,
    FeedbackDTO feedback
) {
    public static TestScoreDetailDTO from(TestScoreDTO ts, FeedbackDTO feedback) {
        return new TestScoreDetailDTO(ts.id(), ts.studentId(), ts.studentName(),
            ts.classId(), ts.className(), ts.teacherId(), ts.teacherName(),
            ts.testName(), ts.testDate(), ts.overallScore(), ts.maxScore(),
            ts.testSource(), ts.questions(), ts.createdAt(), ts.updatedAt(), feedback);
    }
}
