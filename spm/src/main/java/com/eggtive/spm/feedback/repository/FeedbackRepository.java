package com.eggtive.spm.feedback.repository;

import com.eggtive.spm.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {
    Optional<Feedback> findByTestScoreId(UUID testScoreId);
}
