package com.eggtive.spm.report.storage;

import com.eggtive.spm.common.enums.StorageType;

/**
 * Abstraction for report file storage.
 * MVP: local/stub. Production: S3 implementation.
 */
public interface ReportStorage {
    String upload(String key, byte[] content, String contentType);
    String generateUrl(String bucket, String key);
    byte[] readFile(String key);

    /** Returns the storage type for this implementation. */
    StorageType storageType();
}
