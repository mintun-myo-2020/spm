package com.eggtive.spm.auth;

import com.eggtive.spm.classmanagement.repository.ClassStudentRepository;
import com.eggtive.spm.common.enums.ErrorCode;
import com.eggtive.spm.common.enums.Role;
import com.eggtive.spm.common.exception.AppException;
import com.eggtive.spm.user.entity.User;
import com.eggtive.spm.user.repository.ParentRepository;
import com.eggtive.spm.user.repository.StudentRepository;
import com.eggtive.spm.user.repository.TeacherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Verifies that the current user has permission to access a given student's data.
 */
@Service
@Transactional(readOnly = true)
public class StudentAccessService {

    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final TeacherRepository teacherRepository;
    private final ClassStudentRepository classStudentRepository;

    public StudentAccessService(StudentRepository studentRepository,
                                 ParentRepository parentRepository,
                                 TeacherRepository teacherRepository,
                                 ClassStudentRepository classStudentRepository) {
        this.studentRepository = studentRepository;
        this.parentRepository = parentRepository;
        this.teacherRepository = teacherRepository;
        this.classStudentRepository = classStudentRepository;
    }

    /**
     * Throws FORBIDDEN if the user cannot access the given student's data.
     * - ADMIN: always allowed
     * - STUDENT: only their own data
     * - PARENT: only their linked children
     * - TEACHER: only students in their classes
     */
    public void verifyAccess(User user, UUID studentId) {
        if (user.hasRole(Role.ADMIN)) return;

        if (user.hasRole(Role.STUDENT)) {
            var student = studentRepository.findByUserId(user.getId());
            if (student.isPresent() && student.get().getId().equals(studentId)) return;
            throw new AppException(ErrorCode.FORBIDDEN, "You can only access your own data");
        }

        if (user.hasRole(Role.PARENT)) {
            var parent = parentRepository.findByUserId(user.getId());
            if (parent.isPresent()) {
                var children = studentRepository.findByParentId(parent.get().getId());
                if (children.stream().anyMatch(s -> s.getId().equals(studentId))) return;
            }
            throw new AppException(ErrorCode.FORBIDDEN, "You can only access your children's data");
        }

        if (user.hasRole(Role.TEACHER)) {
            var teacher = teacherRepository.findByUserId(user.getId());
            if (teacher.isPresent()) {
                boolean enrolled = classStudentRepository.existsByStudentIdAndTeacherId(studentId, teacher.get().getId());
                if (enrolled) return;
            }
            throw new AppException(ErrorCode.FORBIDDEN, "You can only access students in your classes");
        }

        throw new AppException(ErrorCode.FORBIDDEN, "Access denied");
    }
}
