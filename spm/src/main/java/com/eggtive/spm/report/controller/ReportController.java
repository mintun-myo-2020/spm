package com.eggtive.spm.report.controller;

import com.eggtive.spm.auth.CurrentUserService;
import com.eggtive.spm.auth.StudentAccessService;
import com.eggtive.spm.common.dto.ApiResponse;
import com.eggtive.spm.common.dto.PagedResponse;
import com.eggtive.spm.report.dto.GeneratePlanRequestDTO;
import com.eggtive.spm.report.dto.GenerateReportRequestDTO;
import com.eggtive.spm.report.dto.ProgressReportDTO;
import com.eggtive.spm.report.service.ReportService;
import com.eggtive.spm.report.service.StrengthsImprovementPlan;
import com.eggtive.spm.report.storage.ReportStorage;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ReportController {

    private final ReportService reportService;
    private final CurrentUserService currentUserService;
    private final StudentAccessService studentAccessService;
    private final ReportStorage reportStorage;

    public ReportController(ReportService reportService, CurrentUserService currentUserService,
                             StudentAccessService studentAccessService, ReportStorage reportStorage) {
        this.reportService = reportService;
        this.currentUserService = currentUserService;
        this.studentAccessService = studentAccessService;
        this.reportStorage = reportStorage;
    }

    @PostMapping("/students/{studentId}/improvement-plan")
    public ApiResponse<StrengthsImprovementPlan> generatePlan(@PathVariable UUID studentId,
                                                               @Valid @RequestBody GeneratePlanRequestDTO req) {
        studentAccessService.verifyAccess(currentUserService.getCurrentUser(), studentId);
        return ApiResponse.ok(reportService.generatePlan(studentId, req));
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

    @PatchMapping("/reports/{reportId}/actions/{actionIndex}")
    public ApiResponse<ProgressReportDTO> toggleAction(@PathVariable UUID reportId,
                                                        @PathVariable int actionIndex,
                                                        @RequestParam boolean completed) {
        return ApiResponse.ok(reportService.toggleActionItem(reportId, actionIndex, completed));
    }

    @GetMapping("/students/{studentId}/reports")
    public PagedResponse<ProgressReportDTO> listReports(@PathVariable UUID studentId, Pageable pageable) {
        studentAccessService.verifyAccess(currentUserService.getCurrentUser(), studentId);
        return reportService.listStudentReports(studentId, pageable);
    }

    @GetMapping("/reports/content")
    public ResponseEntity<byte[]> getReportContent(@RequestParam String key) {
        byte[] content = reportStorage.readFile(key);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(content);
    }
}
