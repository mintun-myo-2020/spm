package com.eggtive.spm.testpaper.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageService.class);
    private final Path basePath;

    public LocalFileStorageService(@Value("${app.storage.local-path:./uploads}") String localPath) {
        this.basePath = Path.of(localPath);
        try {
            Files.createDirectories(basePath);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create upload directory: " + localPath, e);
        }
        log.info("Local file storage directory: {}", basePath.toAbsolutePath());
    }

    @Override
    public String upload(String key, byte[] content, String contentType) {
        try {
            Path filePath = basePath.resolve(key);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, content);
            log.info("File written to {}", filePath);
            return key;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write file: " + key, e);
        }
    }

    @Override
    public String generatePresignedUrl(String key, int expiryMinutes) {
        return "/api/v1/test-papers/files?key=" + key;
    }

    @Override
    public void delete(String key) {
        try {
            Path filePath = basePath.resolve(key);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", key, e);
        }
    }

    /** Read file bytes — used by the local file serving endpoint. */
    public byte[] readFile(String key) {
        try {
            return Files.readAllBytes(basePath.resolve(key));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read file: " + key, e);
        }
    }

    /** Detect content type from stored file. */
    public String probeContentType(String key) {
        try {
            String ct = Files.probeContentType(basePath.resolve(key));
            return ct != null ? ct : "application/octet-stream";
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }
}
