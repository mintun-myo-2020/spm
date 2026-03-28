package com.eggtive.spm.scheduling.controller;

import com.eggtive.spm.auth.CurrentUserService;
import com.eggtive.spm.classmanagement.service.ClassService;
import com.eggtive.spm.common.dto.ApiResponse;
import com.eggtive.spm.common.dto.PagedResponse;
import com.eggtive.spm.common.enums.ErrorCode;
import com.eggtive.spm.common.enums.Role;
import com.eggtive.spm.common.enums.SessionStatus;
import com.eggtive.spm.common.exception.AppException;
import com.eggtive.spm.scheduling.dto.*;
import com.eggtive.spm.scheduling.service.SessionService;
import com.eggtive.spm.user.entity.User;
import com.eggtive.spm.user.repository.TeacherRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final ClassService classService;
    private final CurrentUserService currentUserService;
    private final TeacherRepository teacherRepo;

    public SessionController(SessionService sessionService, ClassService classService,
                             CurrentUserService currentUserService, TeacherRepository teacherRepo) {
        this.sessionService = sessionService;
        this.classService = classService;
        this.currentUserService = currentUserService;
        this.teacherRepo = teacherRepo;
    }

    @GetMapping("/upcoming")
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<SessionDTO> getUpcomingSessions(Pageable pageable) {
        User user = currentUserService.getCurrentUser();
        return sessionService.getUpcomingSessions(user, pageable);
    }

    @GetMapping("/class/{classId}")
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<SessionDTO> getClassSessions(@PathVariable UUID classId,
                                                       @RequestParam(required = false) LocalDate startDate,
                                                       @RequestParam(required = false) LocalDate endDate,
                                                       @RequestParam(required = false) SessionStatus status,
                                                       Pageable pageable) {
        return sessionService.getClassSessions(classId, startDate, endDate, status, pageable);
    }

    @GetMapping("/{sessionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<SessionDetailDTO> getSessionDetail(@PathVariable UUID sessionId) {
        return ApiResponse.ok(sessionService.getSessionDetail(sessionId));
    }

    @PutMapping("/{sessionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<SessionUpdateResponseDTO> rescheduleSession(@PathVariable UUID sessionId,
                                                                    @Valid @RequestBody RescheduleSessionRequestDTO req) {
        User user = currentUserService.getCurrentUser();
        var session = sessionService.findSessionOrThrow(sessionId);
        verifyClassAccess(session.getTuitionClass().getId(), user);
        return ApiResponse.ok(sessionService.rescheduleSession(sessionId, req));
    }

    @PutMapping("/{sessionId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<SessionDTO> cancelSession(@PathVariable UUID sessionId,
                                                  @RequestBody(required = false) CancelSessionRequestDTO req) {
        User user = currentUserService.getCurrentUser();
        var session = sessionService.findSessionOrThrow(sessionId);
        verifyClassAccess(session.getTuitionClass().getId(), user);
        return ApiResponse.ok(sessionService.cancelSession(sessionId, req != null ? req.reason() : null));
    }

    private void verifyClassAccess(UUID classId, User user) {
        if (user.hasRole(Role.ADMIN)) return;
        var teacher = teacherRepo.findByUserId(user.getId())
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Teacher profile not found"));
        classService.verifyTeacherOwnsClass(classId, teacher.getId());
    }
}
