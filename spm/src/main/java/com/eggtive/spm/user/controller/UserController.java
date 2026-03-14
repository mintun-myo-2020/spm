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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/teachers")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public PagedResponse<TeacherDTO> getTeachers(Pageable pageable) {
        return userService.getTeachers(pageable);
    }

    @GetMapping("/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public PagedResponse<StudentDTO> getStudents(Pageable pageable) {
        return userService.getStudents(pageable);
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
        return ApiResponse.ok(userService.createStudent(req));
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
}
