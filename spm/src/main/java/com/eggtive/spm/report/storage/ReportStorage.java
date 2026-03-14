package com.eggtive.spm.report.storage;

/**
 * Abstraction for report file storage.
 * MVP: local/stub. Production: S3 implementation.
 */
public interface ReportStorage {
    String upload(String key, byte[] content, String contentType);
    String generateUrl(String bucket, String key);
}
