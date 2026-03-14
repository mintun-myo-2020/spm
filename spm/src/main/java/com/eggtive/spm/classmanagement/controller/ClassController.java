package com.eggtive.spm.classmanagement.controller;

import com.eggtive.spm.auth.CurrentUserService;
import com.eggtive.spm.classmanagement.dto.*;
import com.eggtive.spm.classmanagement.service.ClassService;
import com.eggtive.spm.common.dto.ApiResponse;
import com.eggtive.spm.common.dto.PagedResponse;
import com.eggtive.spm.user.entity.Teacher;
import com.eggtive.spm.user.entity.User;
import com.eggtive.spm.user.repository.TeacherRepository;
import com.eggtive.spm.common.enums.ErrorCode;
import com.eggtive.spm.common.exception.AppException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/classes")
public class ClassController {

    private final ClassService classService;
    private final CurrentUserService currentUserService;
    private final TeacherRepository teacherRepository;

    public ClassController(ClassService classService, CurrentUserService currentUserService,
                           TeacherRepository teacherRepository) {
        this.classService = classService;
        this.currentUserService = currentUserService;
        this.teacherRepository = teacherRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PagedResponse<ClassDTO> getAllClasses(Pageable pageable) {
        return classService.getAllClasses(pageable);
    }

    @GetMapping("/{classId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<ClassDetailDTO> getClassDetails(@PathVariable UUID classId) {
        return ApiResponse.ok(classService.getClassDetail(classId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ClassDTO> createClass(@Valid @RequestBody CreateClassRequestDTO req) {
        User user = currentUserService.getCurrentUser();
        if (user.hasRole(com.eggtive.spm.common.enums.Role.TEACHER)) {
            Teacher teacher = teacherRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Teacher profile not found"));
            // Teachers can only create classes for themselves
            req = new CreateClassRequestDTO(req.name(), req.subjectId(), teacher.getId(), req.description(), req.maxStudents());
        }
        return ApiResponse.ok(classService.createClass(req));
    }

    @GetMapping("/my-classes")
    @PreAuthorize("hasRole('TEACHER')")
    public PagedResponse<ClassDTO> myClasses(Pageable pageable) {
        User user = currentUserService.getCurrentUser();
        Teacher teacher = teacherRepository.findByUserId(user.getId())
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Teacher profile not found"));
        return classService.getTeacherClasses(teacher.getId(), pageable);
    }

    @PostMapping("/{classId}/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<EnrollmentDTO> enrollStudent(@PathVariable UUID classId,
                                                     @Valid @RequestBody EnrollStudentRequestDTO req) {
        User user = currentUserService.getCurrentUser();
        if (user.hasRole(com.eggtive.spm.common.enums.Role.TEACHER)) {
            Teacher teacher = teacherRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Teacher profile not found"));
            classService.verifyTeacherOwnsClass(classId, teacher.getId());
        }
        return ApiResponse.ok(classService.enrollStudent(classId, req.studentId()));
    }

    @PutMapping("/{classId}/students/{studentId}/withdraw")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<EnrollmentDTO> withdrawStudent(@PathVariable UUID classId,
                                                       @PathVariable UUID studentId) {
        User user = currentUserService.getCurrentUser();
        if (user.hasRole(com.eggtive.spm.common.enums.Role.TEACHER)) {
            Teacher teacher = teacherRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Teacher profile not found"));
            classService.verifyTeacherOwnsClass(classId, teacher.getId());
        }
        return ApiResponse.ok(classService.withdrawStudent(classId, studentId));
    }

    @PutMapping("/{classId}/teacher")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ClassDTO> changeTeacher(@PathVariable UUID classId,
                                                @RequestBody UUID newTeacherId) {
        return ApiResponse.ok(classService.changeTeacher(classId, newTeacherId));
    }
}
