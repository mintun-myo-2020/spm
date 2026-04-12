package com.eggtive.spm.subject.repository;

import com.eggtive.spm.subject.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TopicRepository extends JpaRepository<Topic, UUID> {
    boolean existsBySubjectIdAndCode(UUID subjectId, String code);
    List<Topic> findBySubjectIdAndIsActiveTrue(UUID subjectId);
}
