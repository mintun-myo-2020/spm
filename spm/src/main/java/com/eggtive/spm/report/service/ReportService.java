package com.eggtive.spm.report.service;

import com.eggtive.spm.common.dto.PagedResponse;
import com.eggtive.spm.common.enums.ErrorCode;
import com.eggtive.spm.common.exception.AppException;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;


@Service
@Transactional
public class ReportService {

    private final ProgressReportRepository reportRepository;
    private final UserService userService;
    private final ReportStorage reportStorage;

    public ReportService(ProgressReportRepository reportRepo, UserService userService,
                         ReportStorage reportStorage) {
        this.reportRepository = reportRepo;
        this.userService = userService;
        this.reportStorage = reportStorage;
    }

    public ProgressReportDTO generateReport(UUID studentId, GenerateReportRequestDTO req, User currentUser) {
        Student student = userService.findStudentOrThrow(studentId);

        String s3Key = "reports/" + studentId + "/" + UUID.randomUUID() + ".html";
        String s3Bucket = "spm-reports";

        // Stub: in production, generate actual HTML report content here
        byte[] content = "<html><body>Progress Report</body></html>".getBytes();
        reportStorage.upload(s3Key, content, "text/html");

        ProgressReport report = new ProgressReport();
        report.setStudent(student);
        report.setGeneratedBy(currentUser);
        report.setReportType(req.reportType());
        report.setStartDate(req.startDate());
        report.setEndDate(req.endDate());
        report.setS3Key(s3Key);
        report.setS3Bucket(s3Bucket);
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

    private ProgressReportDTO toDTO(ProgressReport r) {
        String url = reportStorage.generateUrl(r.getS3Bucket(), r.getS3Key());
        return new ProgressReportDTO(r.getId(), r.getStudent().getId(), r.getReportType(),
            r.getStartDate(), r.getEndDate(), url, r.getGeneratedAt(), r.getExpiresAt());
    }
}

