package com.eggtive.spm.report.controller;

import com.eggtive.spm.auth.CurrentUserService;
import com.eggtive.spm.auth.StudentAccessService;
import com.eggtive.spm.common.dto.ApiResponse;
import com.eggtive.spm.common.dto.PagedResponse;
import com.eggtive.spm.report.dto.GenerateReportRequestDTO;
import com.eggtive.spm.report.dto.ProgressReportDTO;
import com.eggtive.spm.report.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ReportController {

    private final ReportService reportService;
    private final CurrentUserService currentUserService;
    private final StudentAccessService studentAccessService;

    public ReportController(ReportService reportService, CurrentUserService currentUserService,
                             StudentAccessService studentAccessService) {
        this.reportService = reportService;
        this.currentUserService = currentUserService;
        this.studentAccessService = studentAccessService;
    }

    @PostMapping("/students/{studentId}/reports")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProgressReportDTO> generate(@PathVariable UUID studentId,
                                                    @Valid @RequestBody GenerateReportRequestDTO req) {
        studentAccessService.verifyAccess(currentUserService.getCurrentUser(), studentId);
        return ApiResponse.ok(reportService.generateReport(studentId, req, currentUserService.getCurrentUser()));
    }

    @GetMapping("/reports/{reportId}")
    public ApiResponse<ProgressReportDTO> getReport(@PathVariable UUID reportId) {
        return ApiResponse.ok(reportService.getReport(reportId));
    }

    @GetMapping("/students/{studentId}/reports")
    public PagedResponse<ProgressReportDTO> listReports(@PathVariable UUID studentId, Pageable pageable) {
        studentAccessService.verifyAccess(currentUserService.getCurrentUser(), studentId);
        return reportService.listStudentReports(studentId, pageable);
    }
}
