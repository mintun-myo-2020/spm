package com.eggtive.spm.report.service;

import com.eggtive.spm.common.dto.PagedResponse;
import com.eggtive.spm.common.enums.ErrorCode;
import com.eggtive.spm.common.exception.AppException;
import com.eggtive.spm.report.dto.GeneratePlanRequestDTO;
import com.eggtive.spm.report.dto.GenerateReportRequestDTO;
import com.eggtive.spm.report.dto.ProgressReportDTO;
import com.eggtive.spm.report.entity.ProgressReport;
import com.eggtive.spm.report.entity.ReportStatus;
import com.eggtive.spm.report.repository.ProgressReportRepository;
import com.eggtive.spm.report.storage.ReportStorage;
import com.eggtive.spm.user.entity.Student;
import com.eggtive.spm.user.entity.User;
import com.eggtive.spm.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final ProgressReportRepository reportRepository;
    private final UserService userService;
    private final ReportStorage reportStorage;
    private final ReportDataAssembler reportDataAssembler;
    private final StrengthsImprovementPlanGenerator planGenerator;
    private final JsonMapper jsonMapper;
    private final ReportJobDispatcher jobDispatcher;

    public ReportService(ProgressReportRepository reportRepo, UserService userService,
                         ReportStorage reportStorage, ReportDataAssembler reportDataAssembler,
                         StrengthsImprovementPlanGenerator planGenerator, JsonMapper jsonMapper,
                         ReportJobDispatcher jobDispatcher) {
        this.reportRepository = reportRepo;
        this.userService = userService;
        this.reportStorage = reportStorage;
        this.reportDataAssembler = reportDataAssembler;
        this.planGenerator = planGenerator;
        this.jsonMapper = jsonMapper;
        this.jobDispatcher = jobDispatcher;
    }

    /**
     * Creates a report record with IN_PROGRESS status and kicks off async generation.
     * Returns immediately so the caller doesn't wait for LLM.
     */
    @Transactional
    public ProgressReportDTO generateReport(UUID studentId, GenerateReportRequestDTO req, User currentUser) {
        Student student = userService.findStudentOrThrow(studentId);

        ProgressReport report = new ProgressReport();
        report.setStudent(student);
        report.setGeneratedBy(currentUser);
        report.setReportType(req.reportType());
        report.setStartDate(req.startDate());
        report.setEndDate(req.endDate());
        report.setStatus(ReportStatus.IN_PROGRESS);
        report.setGeneratedAt(Instant.now());
        report.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        report = reportRepository.save(report);

        // Fire async only AFTER this transaction commits, so the row is visible
        final UUID reportId = report.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                jobDispatcher.dispatch(reportId, studentId, req);
            }
        });
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

    /** Update a single action item's completed status in the stored plan JSON. */
    @Transactional
    public ProgressReportDTO toggleActionItem(UUID reportId, int actionIndex, boolean completed) {
        ProgressReport report = reportRepository.findById(reportId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Report not found"));
        if (report.getPlanJson() == null) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Report has no plan");
        }
        try {
            StrengthsImprovementPlan plan = jsonMapper.readValue(report.getPlanJson(), StrengthsImprovementPlan.class);
            if (actionIndex < 0 || actionIndex >= plan.actionPlan().size()) {
                throw new AppException(ErrorCode.VALIDATION_FAILED, "Invalid action index");
            }
            var updated = new ArrayList<>(plan.actionPlan());
            var old = updated.get(actionIndex);
            updated.set(actionIndex, new StrengthsImprovementPlan.ActionItem(
                old.priority(), old.action(), old.targetTopic(), old.timeframe(), old.expectedOutcome(), completed));
            var newPlan = new StrengthsImprovementPlan(plan.studentName(), plan.subjectName(),
                plan.strengths(), plan.improvementAreas(), updated, plan.periodComparisons(), plan.overallSummary());
            report.setPlanJson(jsonMapper.writeValueAsString(newPlan));
            reportRepository.save(report);
            return toDTO(report);
        } catch (AppException e) { throw e; }
        catch (Exception e) { throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to update plan: " + e.getMessage()); }
    }

    @Transactional(readOnly = true)
    public StrengthsImprovementPlan generatePlan(UUID studentId, GeneratePlanRequestDTO req) {
        Student student = userService.findStudentOrThrow(studentId);
        ReportData currentData = reportDataAssembler.assemble(student, req.classId(), req.startDate(), req.endDate());
        List<ReportData> previousPeriods = new ArrayList<>();
        if (req.compareReportIds() != null && !req.compareReportIds().isEmpty()) {
            for (UUID reportId : req.compareReportIds()) {
                ProgressReport prev = reportRepository.findById(reportId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Previous report not found: " + reportId));
                previousPeriods.add(reportDataAssembler.assemble(student, req.classId(), prev.getStartDate(), prev.getEndDate()));
            }
        }
        return planGenerator.generate(currentData, previousPeriods);
    }

    private ProgressReportDTO toDTO(ProgressReport r) {
        String url = (r.getStorageKey() != null && r.getStorageLocation() != null)
            ? reportStorage.generateUrl(r.getStorageLocation(), r.getStorageKey()) : null;
        return new ProgressReportDTO(r.getId(), r.getStudent().getId(), r.getReportType(),
            r.getStartDate(), r.getEndDate(), url, r.getGeneratedAt(), r.getExpiresAt(),
            r.getStatus().name(), r.getPlanJson(), r.getErrorMessage());
    }
}
