package com.eggtive.spm.feedback.controller;

import com.eggtive.spm.auth.CurrentUserService;
import com.eggtive.spm.common.dto.ApiResponse;
import com.eggtive.spm.common.enums.ErrorCode;
import com.eggtive.spm.common.enums.FeedbackCategory;
import com.eggtive.spm.common.exception.AppException;
import com.eggtive.spm.feedback.dto.*;
import com.eggtive.spm.feedback.service.FeedbackService;
import com.eggtive.spm.user.entity.Teacher;
import com.eggtive.spm.user.entity.User;
import com.eggtive.spm.user.repository.TeacherRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final CurrentUserService currentUserService;
    private final TeacherRepository teacherRepository;

    public FeedbackController(FeedbackService fbService, CurrentUserService currentUserService,
                               TeacherRepository teacherRepo) {
        this.feedbackService = fbService;
        this.currentUserService = currentUserService;
        this.teacherRepository = teacherRepo;
    }

    @PostMapping("/test-scores/{testScoreId}/feedback")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FeedbackDTO> createFeedback(@PathVariable UUID testScoreId,
                                                    @RequestBody CreateFeedbackRequestDTO req) {
        User user = currentUserService.getCurrentUser();
        Teacher teacher = getTeacher(user);
        return ApiResponse.ok(feedbackService.createFeedback(testScoreId, req, user, teacher));
    }

    @PutMapping("/feedback/{feedbackId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<FeedbackDTO> updateFeedback(@PathVariable UUID feedbackId,
                                                    @RequestBody CreateFeedbackRequestDTO req) {
        return ApiResponse.ok(feedbackService.updateFeedback(feedbackId, req, currentUserService.getCurrentUser()));
    }

    @GetMapping("/feedback/templates")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<List<FeedbackTemplateDTO>> getTemplates(
            @RequestParam(required = false) FeedbackCategory category) {
        User user = currentUserService.getCurrentUser();
        Teacher teacher = getTeacher(user);
        return ApiResponse.ok(feedbackService.getTemplates(teacher.getId(), category));
    }

    @PostMapping("/feedback/templates")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FeedbackTemplateDTO> createTemplate(
            @Valid @RequestBody CreateFeedbackTemplateRequestDTO req) {
        User user = currentUserService.getCurrentUser();
        Teacher teacher = getTeacher(user);
        return ApiResponse.ok(feedbackService.createTemplate(req, teacher));
    }

    private Teacher getTeacher(User user) {
        return teacherRepository.findByUserId(user.getId())
            .orElseThrow(() -> new AppException(ErrorCode.FORBIDDEN, "Teacher profile required"));
    }
}
