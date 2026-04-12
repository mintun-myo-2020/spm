package com.eggtive.spm.testpaper.storage;

import com.eggtive.spm.common.enums.StorageType;

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
     * @param storageKey storage key / path
     * @return raw file bytes
     */
    byte[] readFileBytes(String storageKey);

    /** Returns the storage type for this implementation. */
    StorageType storageType();
}
