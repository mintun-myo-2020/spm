package com.eggtive.spm.subject.repository;

import com.eggtive.spm.subject.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubjectRepository extends JpaRepository<Subject, UUID> {
    List<Subject> findByIsActiveTrue();
    boolean existsByCode(String code);
}
