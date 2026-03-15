package com.eggtive.spm.testpaper.ocr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Local dev OCR using the otiai10/ocrserver Docker image (HTTP API).
 * Reads the file from local storage, POSTs it as multipart to the OCR server,
 * and returns the extracted text.
 */
@Component
@ConditionalOnProperty(name = "app.ocr.type", havingValue = "tesseract", matchIfMissing = true)
public class TesseractOcrService implements OcrService {

    private static final Logger log = LoggerFactory.getLogger(TesseractOcrService.class);

    private final RestClient restClient;
    private final Path storagePath;

    public TesseractOcrService(
            @Value("${app.ocr.tesseract.server-url:http://spm-tesseract:8080}") String serverUrl,
            @Value("${app.storage.local-path:./uploads}") String localStoragePath) {
        this.restClient = RestClient.builder().baseUrl(serverUrl).build();
        this.storagePath = Path.of(localStoragePath);
        log.info("Tesseract OCR HTTP client configured: server={}, storage={}", serverUrl, storagePath);
    }

    @Override
    public OcrResult extractText(String storageLocation, String storageKey) {
        Path filePath = storagePath.resolve(storageKey);
        log.info("OCR extracting text from: {}", filePath);

        try {
            if (!Files.exists(filePath)) {
                log.error("File not found for OCR: {}", filePath);
                return new OcrResult(List.of(), "", "FAILED", 0.0f);
            }

            byte[] fileBytes = Files.readAllBytes(filePath);
            String fileName = filePath.getFileName().toString();

            // Build multipart request
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            }).contentType(MediaType.APPLICATION_OCTET_STREAM);

            // POST to ocrserver's /api/upload/file endpoint
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri("/api/upload/file")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(builder.build())
                    .retrieve()
                    .body(Map.class);

            if (response == null || !response.containsKey("result")) {
                log.warn("OCR server returned unexpected response: {}", response);
                return new OcrResult(List.of(), "", "FAILED", 0.0f);
            }

            String extractedText = (String) response.get("result");

            if (extractedText == null || extractedText.isBlank()) {
                log.warn("OCR returned empty text for: {}", storageKey);
                return new OcrResult(List.of(), "", "COMPLETED", 0.0f);
            }

            List<OcrTextBlock> blocks = extractedText.lines()
                    .filter(line -> !line.isBlank())
                    .map(line -> new OcrTextBlock(line.trim(), 0.85f, "LINE"))
                    .toList();

            log.info("OCR extracted {} text blocks from: {}", blocks.size(), storageKey);
            return new OcrResult(blocks, extractedText, "COMPLETED", 0.85f);

        } catch (Exception e) {
            log.error("OCR failed for: {}", storageKey, e);
            return new OcrResult(List.of(), "", "FAILED", 0.0f);
        }
    }
}
