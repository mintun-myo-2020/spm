package com.eggtive.spm.user.repository;

import com.eggtive.spm.user.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {
    Optional<Student> findByUserId(UUID userId);
    List<Student> findByParentId(UUID parentId);
}
