package com.eggtive.spm.classmanagement.repository;

import com.eggtive.spm.classmanagement.entity.TuitionClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface TuitionClassRepository extends JpaRepository<TuitionClass, UUID> {
    Page<TuitionClass> findByTeacherIdAndIsActiveTrue(UUID teacherId, Pageable pageable);

    @Query("SELECT COUNT(cs) FROM ClassStudent cs WHERE cs.tuitionClass.id = :classId AND cs.status = 'ACTIVE'")
    long countActiveStudents(UUID classId);
}
