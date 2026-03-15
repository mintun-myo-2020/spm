package com.eggtive.spm.feedback.repository;

import com.eggtive.spm.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {
    Optional<Feedback> findByTestScoreId(UUID testScoreId);

    @Query("SELECT f FROM Feedback f WHERE f.student.id = :studentId AND f.teacher.id = :teacherId " +
           "AND f.createdAt >= :from AND f.createdAt < :to ORDER BY f.createdAt DESC LIMIT 5")
    List<Feedback> findRecentByStudentAndTeacher(@Param("studentId") UUID studentId,
                                                 @Param("teacherId") UUID teacherId,
                                                 @Param("from") Instant from,
                                                 @Param("to") Instant to);
}
