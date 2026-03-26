package com.eggtive.spm.report.service;

import com.eggtive.spm.common.dto.PagedResponse;
import com.eggtive.spm.common.enums.ErrorCode;
import com.eggtive.spm.common.exception.AppException;
import com.eggtive.spm.report.dto.GeneratePlanRequestDTO;
import com.eggtive.spm.report.dto.GenerateReportRequestDTO;
import com.eggtive.spm.report.dto.ProgressReportDTO;
import com.eggtive.spm.report.entity.ProgressReport;
import com.eggtive.spm.report.repository.ProgressReportRepository;
import com.eggtive.spm.report.storage.ReportStorage;
import com.eggtive.spm.user.entity.Student;
import com.eggtive.spm.user.entity.User;
import com.eggtive.spm.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class ReportService {

    private final ProgressReportRepository reportRepository;
    private final UserService userService;
    private final ReportStorage reportStorage;
    private final ReportDataAssembler reportDataAssembler;
    private final ReportContentGenerator reportContentGenerator;
    private final StrengthsImprovementPlanGenerator planGenerator;

    public ReportService(ProgressReportRepository reportRepo, UserService userService,
                         ReportStorage reportStorage, ReportDataAssembler reportDataAssembler,
                         ReportContentGenerator reportContentGenerator,
                         StrengthsImprovementPlanGenerator planGenerator) {
        this.reportRepository = reportRepo;
        this.userService = userService;
        this.reportStorage = reportStorage;
        this.reportDataAssembler = reportDataAssembler;
        this.reportContentGenerator = reportContentGenerator;
        this.planGenerator = planGenerator;
    }

    public ProgressReportDTO generateReport(UUID studentId, GenerateReportRequestDTO req, User currentUser) {
        Student student = userService.findStudentOrThrow(studentId);

        String storageKey = "reports/" + studentId + "/" + UUID.randomUUID() + ".html";
        String storageLocation = "local";

        // Assemble data and generate HTML content
        ReportData reportData = reportDataAssembler.assemble(student, req.classId(), req.startDate(), req.endDate());

        // Optionally generate the improvement plan
        StrengthsImprovementPlan plan = null;
        if (req.shouldIncludePlan()) {
            List<ReportData> previousPeriods = new ArrayList<>();
            if (req.compareReportIds() != null && !req.compareReportIds().isEmpty()) {
                for (UUID reportId : req.compareReportIds()) {
                    ProgressReport prev = reportRepository.findById(reportId)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND,
                            "Previous report not found: " + reportId));
                    ReportData prevData = reportDataAssembler.assemble(
                        student, req.classId(), prev.getStartDate(), prev.getEndDate());
                    previousPeriods.add(prevData);
                }
            }
            plan = planGenerator.generate(reportData, previousPeriods);
        }

        String html = reportContentGenerator.generate(reportData, plan);
        byte[] content = html.getBytes(StandardCharsets.UTF_8);
        reportStorage.upload(storageKey, content, "text/html");

        ProgressReport report = new ProgressReport();
        report.setStudent(student);
        report.setGeneratedBy(currentUser);
        report.setReportType(req.reportType());
        report.setStartDate(req.startDate());
        report.setEndDate(req.endDate());
        report.setStorageKey(storageKey);
        report.setStorageLocation(storageLocation);
        report.setGeneratedAt(Instant.now());
        report.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        report = reportRepository.save(report);

        return toDTO(report);
    }

    @Transactional(readOnly = true)
    public ProgressReportDTO getReport(UUID reportId) {
        ProgressReport report = reportRepository.findById(reportId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Report not found"));
        return toDTO(report);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProgressReportDTO> listStudentReports(UUID studentId, Pageable pageable) {
        Page<ProgressReport> page = reportRepository.findByStudentId(studentId, pageable);
        return PagedResponse.from(page, page.getContent().stream().map(this::toDTO).toList());
    }

    /**
     * Generate a strengths and improvement plan for a student in a class over a date range.
     * Optionally includes data from previous reports for comparison.
     */
    @Transactional(readOnly = true)
    public StrengthsImprovementPlan generatePlan(UUID studentId, GeneratePlanRequestDTO req) {
        Student student = userService.findStudentOrThrow(studentId);
        ReportData currentData = reportDataAssembler.assemble(
            student, req.classId(), req.startDate(), req.endDate());

        List<ReportData> previousPeriods = new ArrayList<>();
        if (req.compareReportIds() != null && !req.compareReportIds().isEmpty()) {
            for (UUID reportId : req.compareReportIds()) {
                ProgressReport prev = reportRepository.findById(reportId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND,
                        "Previous report not found: " + reportId));
                // Re-assemble data for the previous report's date range using the same class
                ReportData prevData = reportDataAssembler.assemble(
                    student, req.classId(), prev.getStartDate(), prev.getEndDate());
                previousPeriods.add(prevData);
            }
        }

        return planGenerator.generate(currentData, previousPeriods);
    }


    private ProgressReportDTO toDTO(ProgressReport r) {
        String url = reportStorage.generateUrl(r.getStorageLocation(), r.getStorageKey());
        return new ProgressReportDTO(r.getId(), r.getStudent().getId(), r.getReportType(),
            r.getStartDate(), r.getEndDate(), url, r.getGeneratedAt(), r.getExpiresAt());
    }
}

