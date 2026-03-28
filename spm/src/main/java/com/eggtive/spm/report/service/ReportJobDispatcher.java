package com.eggtive.spm.report.service;

import com.eggtive.spm.report.dto.GenerateReportRequestDTO;

import java.util.UUID;

/**
 * Abstraction for dispatching report generation jobs.
 * Implementations can use in-process @Async, SQS, or any other mechanism.
 */
public interface ReportJobDispatcher {

    /**
     * Dispatch a report generation job. Called after the ProgressReport
     * row has been committed to the database with IN_PROGRESS status.
     */
    void dispatch(UUID reportId, UUID studentId, GenerateReportRequestDTO request);
}
