package com.eggtive.spm.testpaper.storage;

/**
 * Abstraction for file storage. Local dev writes to disk; production uses S3.
 */
public interface FileStorageService {
    String upload(String key, byte[] content, String contentType);
    String generatePresignedUrl(String key, int expiryMinutes);
    void delete(String key);

    /**
     * Read file bytes from storage. Used by the LLM extraction service
     * to pass image content to the model.
     *
     * @param storageLocation storage location identifier (e.g. "local", bucket name)
     * @param storageKey      storage key / path
     * @return raw file bytes
     */
    byte[] readFileBytes(String storageLocation, String storageKey);
}
