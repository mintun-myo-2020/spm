package com.eggtive.spm.user.repository;

import com.eggtive.spm.user.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {
    Optional<Student> findByUserId(UUID userId);
    List<Student> findByParentId(UUID parentId);

    @Query("""
        SELECT DISTINCT s FROM Student s
        LEFT JOIN ClassStudent cs ON cs.student.id = s.id
        LEFT JOIN TuitionClass c ON c.id = cs.tuitionClass.id
        WHERE s.createdBy.id = :userId
           OR c.teacher.id = :teacherId
        """)
    Page<Student> findByCreatorOrTeacher(UUID userId, UUID teacherId, Pageable pageable);
}
