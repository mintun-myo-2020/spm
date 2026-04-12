package com.eggtive.spm.report.service;

import com.eggtive.spm.common.enums.ErrorCode;
import com.eggtive.spm.common.exception.AppException;
import com.eggtive.spm.report.dto.GenerateReportRequestDTO;
import com.eggtive.spm.report.entity.ProgressReport;
import com.eggtive.spm.report.entity.ReportStatus;
import com.eggtive.spm.report.repository.ProgressReportRepository;
import com.eggtive.spm.report.storage.ReportStorage;
import com.eggtive.spm.user.entity.Student;
import com.eggtive.spm.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * In-process async dispatcher using Spring @Async.
 * Active when app.report.dispatcher=local (default).
 * Swap for an SQS-based implementation by setting app.report.dispatcher=sqs.
 */
@Component
@ConditionalOnProperty(name = "app.report.dispatcher", havingValue = "local", matchIfMissing = true)
public class ReportAsyncWorker implements ReportJobDispatcher {

    private static final Logger log = LoggerFactory.getLogger(ReportAsyncWorker.class);

    private final ProgressReportRepository reportRepository;
    private final UserService userService;
    private final ReportStorage reportStorage;
    private final ReportDataAssembler reportDataAssembler;
    private final ReportContentGenerator reportContentGenerator;
    private final StrengthsImprovementPlanGenerator planGenerator;
    private final JsonMapper jsonMapper;

    public ReportAsyncWorker(ProgressReportRepository reportRepository, UserService userService,
                             ReportStorage reportStorage, ReportDataAssembler reportDataAssembler,
                             ReportContentGenerator reportContentGenerator,
                             StrengthsImprovementPlanGenerator planGenerator, JsonMapper jsonMapper) {
        this.reportRepository = reportRepository;
        this.userService = userService;
        this.reportStorage = reportStorage;
        this.reportDataAssembler = reportDataAssembler;
        this.reportContentGenerator = reportContentGenerator;
        this.planGenerator = planGenerator;
        this.jsonMapper = jsonMapper;
    }

    @Override
    @Async
    @Transactional
    public void dispatch(UUID reportId, UUID studentId, GenerateReportRequestDTO req) {
        log.info("Async report generation started — reportId: {}", reportId);
        ProgressReport report = reportRepository.findById(reportId).orElse(null);
        if (report == null) return;

        try {
            Student student = userService.findStudentOrThrow(studentId);
            ReportData data = reportDataAssembler.assemble(student, req.classId(), req.startDate(), req.endDate());

            StrengthsImprovementPlan plan = null;
            if (req.shouldIncludePlan()) {
                List<ReportData> prev = new ArrayList<>();
                if (req.compareReportIds() != null) {
                    for (UUID prevId : req.compareReportIds()) {
                        ProgressReport p = reportRepository.findById(prevId)
                            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Previous report not found: " + prevId));
                        prev.add(reportDataAssembler.assemble(student, req.classId(), p.getStartDate(), p.getEndDate()));
                    }
                }
                plan = planGenerator.generate(data, prev);
            }

            String html = reportContentGenerator.generate(data, plan);
            String storageKey = "reports/" + studentId + "/" + UUID.randomUUID() + ".html";
            reportStorage.upload(storageKey, html.getBytes(StandardCharsets.UTF_8), "text/html");

            if (plan != null) report.setPlanJson(jsonMapper.writeValueAsString(plan));
            report.setStorageKey(storageKey);
            report.setStorageLocation(reportStorage.storageType().value());
            report.setStatus(ReportStatus.COMPLETED);
            reportRepository.save(report);
            log.info("Report generation completed — reportId: {}", reportId);

        } catch (Exception e) {
            log.error("Report generation failed — reportId: {}", reportId, e);
            report.setStatus(ReportStatus.FAILED);
            report.setErrorMessage(e.getMessage());
            reportRepository.save(report);
        }
    }
}
