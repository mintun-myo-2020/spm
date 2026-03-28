package com.eggtive.spm.user.controller;

import com.eggtive.spm.common.dto.ApiResponse;
import com.eggtive.spm.common.dto.PagedResponse;
import com.eggtive.spm.user.dto.*;
import com.eggtive.spm.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final com.eggtive.spm.auth.CurrentUserService currentUserService;

    public UserController(UserService userService, com.eggtive.spm.auth.CurrentUserService currentUserService) {
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/teachers")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public PagedResponse<TeacherDTO> getTeachers(Pageable pageable) {
        return userService.getTeachers(pageable);
    }

    @GetMapping("/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public PagedResponse<StudentDTO> getStudents(Pageable pageable) {
        var user = currentUserService.getCurrentUser();
        if (user.hasRole(com.eggtive.spm.common.enums.Role.ADMIN)) {
            return userService.getStudents(pageable);
        }
        return userService.getStudentsForTeacher(user, pageable);
    }

    @GetMapping("/parents")
    @PreAuthorize("hasRole('ADMIN')")
    public PagedResponse<ParentDTO> getParents(Pageable pageable) {
        return userService.getParents(pageable);
    }

    @PostMapping("/teachers")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TeacherDTO> createTeacher(@Valid @RequestBody CreateTeacherRequestDTO req) {
        return ApiResponse.ok(userService.createTeacher(req));
    }

    @PostMapping("/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StudentDTO> createStudent(@Valid @RequestBody CreateStudentRequestDTO req) {
        return ApiResponse.ok(userService.createStudent(req, currentUserService.getCurrentUser()));
    }

    @PostMapping("/parents")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ParentDTO> createParent(@Valid @RequestBody CreateParentRequestDTO req) {
        return ApiResponse.ok(userService.createParent(req));
    }

    @PutMapping("/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deactivate(@PathVariable UUID userId) {
        userService.deactivateUser(userId);
        return ApiResponse.ok(null, "User deactivated");
    }

    @PutMapping("/{userId}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> reactivate(@PathVariable UUID userId) {
        userService.reactivateUser(userId);
        return ApiResponse.ok(null, "User reactivated");
    }

    @PutMapping("/{userId}/reset-password")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<Void> resetPassword(@PathVariable UUID userId, @RequestBody Map<String, String> body) {
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.length() < 8) {
            throw new com.eggtive.spm.common.exception.AppException(
                com.eggtive.spm.common.enums.ErrorCode.INVALID_INPUT, "Password must be at least 8 characters");
        }
        userService.resetPassword(userId, newPassword);
        return ApiResponse.ok(null, "Password reset — user will be asked to change it on next login");
    }
}
