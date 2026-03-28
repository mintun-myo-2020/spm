package com.eggtive.spm.scheduling.controller;

import com.eggtive.spm.auth.CurrentUserService;
import com.eggtive.spm.classmanagement.service.ClassService;
import com.eggtive.spm.common.dto.ApiResponse;
import com.eggtive.spm.common.enums.Role;
import com.eggtive.spm.scheduling.dto.*;
import com.eggtive.spm.scheduling.service.ScheduleService;
import com.eggtive.spm.user.entity.User;
import com.eggtive.spm.user.repository.TeacherRepository;
import com.eggtive.spm.common.enums.ErrorCode;
import com.eggtive.spm.common.exception.AppException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final ClassService classService;
    private final CurrentUserService currentUserService;
    private final TeacherRepository teacherRepo;

    public ScheduleController(ScheduleService scheduleService, ClassService classService,
                              CurrentUserService currentUserService, TeacherRepository teacherRepo) {
        this.scheduleService = scheduleService;
        this.classService = classService;
        this.currentUserService = currentUserService;
        this.teacherRepo = teacherRepo;
    }

    @PostMapping("/classes/{classId}/schedules")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ScheduleDTO> createSchedule(@PathVariable UUID classId,
                                                    @Valid @RequestBody CreateScheduleRequestDTO req) {
        User user = currentUserService.getCurrentUser();
        verifyClassAccess(classId, user);
        return ApiResponse.ok(scheduleService.createSchedule(classId, req, user));
    }

    @PostMapping("/classes/{classId}/schedules/one-off")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ScheduleDTO> createOneOffSchedule(@PathVariable UUID classId,
                                                          @Valid @RequestBody CreateOneOffScheduleRequestDTO req) {
        User user = currentUserService.getCurrentUser();
        verifyClassAccess(classId, user);
        return ApiResponse.ok(scheduleService.createOneOffSchedule(classId, req, user));
    }

    @GetMapping("/classes/{classId}/schedules")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<List<ScheduleDTO>> getClassSchedules(@PathVariable UUID classId,
                                                             @RequestParam(defaultValue = "true") boolean activeOnly) {
        return ApiResponse.ok(scheduleService.getClassSchedules(classId, activeOnly));
    }

    @PutMapping("/schedules/{scheduleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<ScheduleDTO> updateSchedule(@PathVariable UUID scheduleId,
                                                    @RequestParam(required = false) LocalDate effectiveUntil) {
        User user = currentUserService.getCurrentUser();
        var schedule = scheduleService.findScheduleOrThrow(scheduleId);
        verifyClassAccess(schedule.getTuitionClass().getId(), user);
        return ApiResponse.ok(scheduleService.updateSchedule(scheduleId, effectiveUntil));
    }

    @PostMapping("/schedules/{scheduleId}/generate-sessions")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<List<SessionDTO>> generateSessions(@PathVariable UUID scheduleId,
                                                           @Valid @RequestBody GenerateSessionsRequestDTO req) {
        User user = currentUserService.getCurrentUser();
        var schedule = scheduleService.findScheduleOrThrow(scheduleId);
        verifyClassAccess(schedule.getTuitionClass().getId(), user);
        return ApiResponse.ok(scheduleService.generateSessions(scheduleId, req));
    }

    private void verifyClassAccess(UUID classId, User user) {
        if (user.hasRole(Role.ADMIN)) return;
        var teacher = teacherRepo.findByUserId(user.getId())
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Teacher profile not found"));
        classService.verifyTeacherOwnsClass(classId, teacher.getId());
    }
}
