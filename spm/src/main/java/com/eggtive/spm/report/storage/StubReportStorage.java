package com.eggtive.spm.report.storage;

import com.eggtive.spm.common.enums.StorageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Local dev implementation. Writes report HTML to disk and serves via API endpoint.
 * In production, replace with S3ReportStorage using pre-signed URLs.
 */
@Component
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class StubReportStorage implements ReportStorage {

    private static final Logger log = LoggerFactory.getLogger(StubReportStorage.class);

    private final Path basePath;

    public StubReportStorage(@Value("${app.storage.report-local-path:./report-files}") String localPath) {
        Path preferred = Path.of(localPath);
        Path resolved;
        try {
            Files.createDirectories(preferred);
            resolved = preferred;
        } catch (IOException e) {
            // Fallback to /tmp when the preferred path isn't writable (e.g. Docker containers)
            Path fallback = Path.of(System.getProperty("java.io.tmpdir"), "report-files");
            try {
                Files.createDirectories(fallback);
                resolved = fallback;
                log.warn("Cannot write to {}; falling back to {}", preferred, fallback);
            } catch (IOException e2) {
                throw new UncheckedIOException("Failed to create report storage directory at both "
                        + preferred + " and " + fallback, e2);
            }
        }
        this.basePath = resolved;
        log.info("Report storage directory: {}", this.basePath.toAbsolutePath());
    }

    @Override
    public String upload(String key, byte[] content, String contentType) {
        try {
            Path filePath = basePath.resolve(key);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, content);
            log.info("Report written to {}", filePath);
            return key;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write report file: " + key, e);
        }
    }

    @Override
    public String generateUrl(String bucket, String key) {
        // Path relative to API base — frontend apiClient prepends /api/v1 automatically
        return "/reports/content?key=" + key;
    }

    @Override
    public byte[] readFile(String key) {
        try {
            Path filePath = basePath.resolve(key);
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read report file: " + key, e);
        }
    }

    @Override
    public StorageType storageType() {
        return StorageType.LOCAL;
    }
}
