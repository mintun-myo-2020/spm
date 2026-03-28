package com.eggtive.spm.classmanagement.repository;

import com.eggtive.spm.classmanagement.entity.ClassStudent;
import com.eggtive.spm.common.enums.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClassStudentRepository extends JpaRepository<ClassStudent, UUID> {
    Optional<ClassStudent> findByTuitionClassIdAndStudentId(UUID classId, UUID studentId);
    boolean existsByTuitionClassIdAndStudentIdAndStatus(UUID classId, UUID studentId, EnrollmentStatus status);
    List<ClassStudent> findByTuitionClassId(UUID classId);
    List<ClassStudent> findByTuitionClassIdAndStatus(UUID classId, EnrollmentStatus status);

    @Query("SELECT CASE WHEN COUNT(cs) > 0 THEN true ELSE false END FROM ClassStudent cs " +
           "WHERE cs.student.id = :studentId AND cs.tuitionClass.teacher.id = :teacherId AND cs.status = 'ACTIVE'")
    boolean existsByStudentIdAndTeacherId(UUID studentId, UUID teacherId);

    List<ClassStudent> findByStudentIdAndStatus(UUID studentId, EnrollmentStatus status);
}
