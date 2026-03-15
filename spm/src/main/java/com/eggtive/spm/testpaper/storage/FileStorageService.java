package com.eggtive.spm.testpaper.storage;

/**
 * Abstraction for file storage. Local dev writes to disk; production uses S3.
 */
public interface FileStorageService {
    String upload(String key, byte[] content, String contentType);
    String generatePresignedUrl(String key, int expiryMinutes);
    void delete(String key);
}
