package com.eggtive.spm.classmanagement.repository;

import com.eggtive.spm.classmanagement.entity.ClassStudent;
import com.eggtive.spm.common.enums.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClassStudentRepository extends JpaRepository<ClassStudent, UUID> {
    Optional<ClassStudent> findByTuitionClassIdAndStudentId(UUID classId, UUID studentId);
    boolean existsByTuitionClassIdAndStudentIdAndStatus(UUID classId, UUID studentId, EnrollmentStatus status);
}
