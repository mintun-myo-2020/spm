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
import com.eggtive.spm.scheduling.dto.CreateScheduleRequestDTO;
import com.eggtive.spm.scheduling.service.ScheduleService;
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
    private final com.eggtive.spm.user.repository.StudentRepository studentRepository;
    private final ScheduleService scheduleService;

    public ClassController(ClassService classService, CurrentUserService currentUserService,
                           TeacherRepository teacherRepository,
                           com.eggtive.spm.user.repository.StudentRepository studentRepository,
                           ScheduleService scheduleService) {
        this.classService = classService;
        this.currentUserService = currentUserService;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.scheduleService = scheduleService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PagedResponse<ClassDTO> getAllClasses(Pageable pageable) {
        return classService.getAllClasses(pageable);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ClassDTO> createClass(@Valid @RequestBody CreateClassRequestDTO req) {
        User user = currentUserService.getCurrentUser();
        if (user.hasRole(com.eggtive.spm.common.enums.Role.TEACHER)) {
            Teacher teacher = teacherRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Teacher profile not found"));
            req = new CreateClassRequestDTO(req.name(), req.subjectId(), teacher.getId(), req.description(), req.maxStudents(),
                req.scheduleDayOfWeek(), req.scheduleStartTime(), req.scheduleEndTime(),
                req.scheduleLocation(), req.scheduleEffectiveFrom(), req.scheduleEffectiveUntil());
        }
        ClassDTO classDTO = classService.createClass(req);

        // FR-14.8: Create initial schedule if schedule fields provided
        if (req.scheduleDayOfWeek() != null) {
            var scheduleReq = new CreateScheduleRequestDTO(
                req.scheduleDayOfWeek(), req.scheduleStartTime(), req.scheduleEndTime(),
                req.scheduleLocation(), req.scheduleEffectiveFrom(), req.scheduleEffectiveUntil());
            scheduleService.createSchedule(classDTO.id(), scheduleReq, user);
        }

        return ApiResponse.ok(classDTO);
    }

    @GetMapping("/my-classes")
    @PreAuthorize("hasRole('TEACHER')")
    public PagedResponse<ClassDTO> myClasses(Pageable pageable) {
        User user = currentUserService.getCurrentUser();
        Teacher teacher = teacherRepository.findByUserId(user.getId())
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Teacher profile not found"));
        return classService.getTeacherClasses(teacher.getId(), pageable);
    }

    @GetMapping("/my-enrollments")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ApiResponse<java.util.List<com.eggtive.spm.classmanagement.dto.ClassDTO>> myEnrollments() {
        User user = currentUserService.getCurrentUser();
        com.eggtive.spm.user.entity.Student student = studentRepository.findByUserId(user.getId())
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Student profile not found"));
        return ApiResponse.ok(classService.getStudentEnrolledClasses(student.getId()));
    }

    @GetMapping("/{classId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<ClassDetailDTO> getClassDetails(@PathVariable UUID classId) {
        return ApiResponse.ok(classService.getClassDetail(classId));
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
    @PutMapping("/{classId}/students/{studentId}/re-enroll")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<EnrollmentDTO> reEnrollStudent(@PathVariable UUID classId,
                                                       @PathVariable UUID studentId) {
        User user = currentUserService.getCurrentUser();
        if (user.hasRole(com.eggtive.spm.common.enums.Role.TEACHER)) {
            Teacher teacher = teacherRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Teacher profile not found"));
            classService.verifyTeacherOwnsClass(classId, teacher.getId());
        }
        return ApiResponse.ok(classService.reEnrollStudent(classId, studentId));
    }

    @PutMapping("/{classId}/teacher")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ClassDTO> changeTeacher(@PathVariable UUID classId,
                                                @RequestBody UUID newTeacherId) {
        return ApiResponse.ok(classService.changeTeacher(classId, newTeacherId));
    }
}
