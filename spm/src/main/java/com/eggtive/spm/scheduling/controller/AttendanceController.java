package com.eggtive.spm.scheduling.controller;

import com.eggtive.spm.auth.CurrentUserService;
import com.eggtive.spm.classmanagement.service.ClassService;
import com.eggtive.spm.common.dto.ApiResponse;
import com.eggtive.spm.common.enums.ErrorCode;
import com.eggtive.spm.common.enums.Role;
import com.eggtive.spm.common.exception.AppException;
import com.eggtive.spm.scheduling.dto.*;
import com.eggtive.spm.scheduling.service.AttendanceService;
import com.eggtive.spm.scheduling.service.SessionService;
import com.eggtive.spm.user.entity.User;
import com.eggtive.spm.user.repository.ParentRepository;
import com.eggtive.spm.user.repository.StudentRepository;
import com.eggtive.spm.user.repository.TeacherRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final SessionService sessionService;
    private final ClassService classService;
    private final CurrentUserService currentUserService;
    private final TeacherRepository teacherRepo;
    private final StudentRepository studentRepo;
    private final ParentRepository parentRepo;

    public AttendanceController(AttendanceService attendanceService, SessionService sessionService,
                                ClassService classService, CurrentUserService currentUserService,
                                TeacherRepository teacherRepo, StudentRepository studentRepo,
                                ParentRepository parentRepo) {
        this.attendanceService = attendanceService;
        this.sessionService = sessionService;
        this.classService = classService;
        this.currentUserService = currentUserService;
        this.teacherRepo = teacherRepo;
        this.studentRepo = studentRepo;
        this.parentRepo = parentRepo;
    }

    @PostMapping("/sessions/{sessionId}/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<List<AttendanceDTO>> batchMarkAttendance(@PathVariable UUID sessionId,
                                                                 @Valid @RequestBody BatchAttendanceRequestDTO req) {
        User user = currentUserService.getCurrentUser();
        var session = sessionService.findSessionOrThrow(sessionId);
        verifyTeacherOrAdmin(session.getTuitionClass().getId(), user);
        return ApiResponse.ok(attendanceService.batchMarkAttendance(sessionId, req.entries(), user));
    }

    @PutMapping("/sessions/{sessionId}/attendance/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<AttendanceDTO> updateAttendance(@PathVariable UUID sessionId,
                                                        @PathVariable UUID studentId,
                                                        @Valid @RequestBody UpdateAttendanceRequestDTO req) {
        User user = currentUserService.getCurrentUser();
        var session = sessionService.findSessionOrThrow(sessionId);
        verifyTeacherOrAdmin(session.getTuitionClass().getId(), user);
        return ApiResponse.ok(attendanceService.updateAttendance(sessionId, studentId, req.status(), user));
    }

    @PutMapping("/sessions/{sessionId}/rsvp")
    @PreAuthorize("hasAnyRole('STUDENT', 'PARENT')")
    public ApiResponse<AttendanceDTO> updateRsvp(@PathVariable UUID sessionId,
                                                  @Valid @RequestBody RsvpRequestDTO req,
                                                  @RequestParam(required = false) UUID studentId) {
        User user = currentUserService.getCurrentUser();
        UUID resolvedStudentId;

        if (user.hasRole(Role.STUDENT)) {
            var student = studentRepo.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Student profile not found"));
            resolvedStudentId = student.getId();
        } else if (user.hasRole(Role.PARENT)) {
            if (studentId == null) throw new AppException(ErrorCode.INVALID_INPUT, "studentId required for parent RSVP");
            // Verify parent-child link
            var parent = parentRepo.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Parent profile not found"));
            var children = studentRepo.findByParentId(parent.getId());
            if (children.stream().noneMatch(c -> c.getId().equals(studentId))) {
                throw new AppException(ErrorCode.FORBIDDEN, "You can only RSVP for your own children");
            }
            resolvedStudentId = studentId;
        } else {
            throw new AppException(ErrorCode.FORBIDDEN, "Access denied");
        }

        return ApiResponse.ok(attendanceService.updateRsvp(sessionId, resolvedStudentId, req.rsvpStatus(), req.reason()));
    }

    @GetMapping("/classes/{classId}/attendance-stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<ClassAttendanceStatsDTO> getClassAttendanceStats(@PathVariable UUID classId,
                                                                        @RequestParam(required = false) LocalDate startDate,
                                                                        @RequestParam(required = false) LocalDate endDate) {
        return ApiResponse.ok(attendanceService.getClassAttendanceStats(classId, startDate, endDate));
    }

    @GetMapping("/students/{studentId}/classes/{classId}/attendance-stats")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<StudentAttendanceStatsDTO> getStudentAttendanceStats(@PathVariable UUID studentId,
                                                                            @PathVariable UUID classId,
                                                                            @RequestParam(required = false) LocalDate startDate,
                                                                            @RequestParam(required = false) LocalDate endDate) {
        return ApiResponse.ok(attendanceService.getStudentAttendanceStats(studentId, classId, startDate, endDate));
    }

    private void verifyTeacherOrAdmin(UUID classId, User user) {
        if (user.hasRole(Role.ADMIN)) return;
        var teacher = teacherRepo.findByUserId(user.getId())
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Teacher profile not found"));
        classService.verifyTeacherOwnsClass(classId, teacher.getId());
    }
}
