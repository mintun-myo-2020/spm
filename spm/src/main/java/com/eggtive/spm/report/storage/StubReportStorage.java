package com.eggtive.spm.report.storage;

import org.springframework.stereotype.Component;

/**
 * Stub implementation for local dev. Replace with S3ReportStorage for production.
 */
@Component
public class StubReportStorage implements ReportStorage {

    @Override
    public String upload(String key, byte[] content, String contentType) {
        // In production, upload to S3 and return the key
        return key;
    }

    @Override
    public String generateUrl(String bucket, String key) {
        // In production, generate a pre-signed S3 URL
        return "http://localhost:8080/reports/" + key;
    }
}
