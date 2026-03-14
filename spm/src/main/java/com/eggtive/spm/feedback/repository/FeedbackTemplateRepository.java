package com.eggtive.spm.feedback.repository;

import com.eggtive.spm.common.enums.FeedbackCategory;
import com.eggtive.spm.feedback.entity.FeedbackTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface FeedbackTemplateRepository extends JpaRepository<FeedbackTemplate, UUID> {
    @Query("""
        SELECT ft FROM FeedbackTemplate ft
        WHERE (ft.teacher.id = :teacherId OR ft.isSystemWide = true)
        AND (:category IS NULL OR ft.category = :category)
        """)
    List<FeedbackTemplate> findByTeacherOrSystemWide(UUID teacherId, FeedbackCategory category);
}
