package com.eggtive.spm.testscore.controller;

import com.eggtive.spm.auth.CurrentUserService;
import com.eggtive.spm.auth.StudentAccessService;
import com.eggtive.spm.common.dto.ApiResponse;
import com.eggtive.spm.common.dto.PagedResponse;
import com.eggtive.spm.common.enums.ErrorCode;
import com.eggtive.spm.common.exception.AppException;
import com.eggtive.spm.testscore.dto.CreateTestScoreRequestDTO;

@PutMapping("/test-scores/{testScoreId}")
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
public ApiResponse<TestScoreDTO> update(@PathVariable UUID testScoreId,
                                         @Valid @RequestBody CreateTestScoreRequestDTO req) {
    User user = currentUserService.getCurrentUser();
    return ApiResponse.ok(testScoreService.updateTestScore(testScoreId, req, user));
}

import com.eggtive.spm.testscore.dto.TestScoreDTO;
import com.eggtive.spm.testscore.dto.TestScoreDetailDTO;
import com.eggtive.spm.testscore.service.TestScoreService;
import com.eggtive.spm.user.entity.Teacher;
import com.eggtive.spm.user.entity.User;
import com.eggtive.spm.user.repository.TeacherRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class TestScoreController {

    private final TestScoreService testScoreService;
    private final CurrentUserService currentUserService;
    private final TeacherRepository teacherRepository;
    private final StudentAccessService studentAccessService;

    public TestScoreController(TestScoreService tsService, CurrentUserService currentUserService,
                                TeacherRepository teacherRepo, StudentAccessService studentAccessService) {
        this.testScoreService = tsService;
        this.currentUserService = currentUserService;
        this.teacherRepository = teacherRepo;
        this.studentAccessService = studentAccessService;
    }

    @PostMapping("/test-scores")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TestScoreDTO> create(@Valid @RequestBody CreateTestScoreRequestDTO req) {
        User user = currentUserService.getCurrentUser();
        Teacher teacher = teacherRepository.findByUserId(user.getId())
            .orElseThrow(() -> new AppException(ErrorCode.FORBIDDEN, "Only teachers can create test scores"));
        return ApiResponse.ok(testScoreService.createTestScore(req, user, teacher));
    }

    @GetMapping("/students/{studentId}/test-scores")
    public PagedResponse<TestScoreDTO> getStudentScores(
            @PathVariable UUID studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) UUID classId,
            Pageable pageable) {
        studentAccessService.verifyAccess(currentUserService.getCurrentUser(), studentId);
        return testScoreService.getStudentTestScores(studentId, startDate, endDate, classId, pageable);
    }

    @GetMapping("/test-scores/{testScoreId}")
    public ApiResponse<TestScoreDetailDTO> getTestScore(@PathVariable UUID testScoreId) {
        return ApiResponse.ok(testScoreService.getTestScoreDetail(testScoreId));
    }

    @DeleteMapping("/test-scores/{testScoreId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<Void> delete(@PathVariable UUID testScoreId) {
        testScoreService.deleteTestScore(testScoreId);
        return ApiResponse.ok(null, "Test score deleted");
    }
}
