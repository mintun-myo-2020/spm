package com.eggtive.spm.testpaper.ocr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Local dev OCR using Tesseract via the jitesoft/tesseract-ocr Docker container.
 * Reads the file from local storage, runs tesseract CLI inside the container,
 * and returns the extracted text.
 *
 * Requires the tesseract-ocr service running in docker-compose.
 * The container shares a volume with the app's upload directory.
 */
@Component
@ConditionalOnProperty(name = "app.ocr.type", havingValue = "tesseract", matchIfMissing = true)
public class TesseractOcrService implements OcrService {

    private static final Logger log = LoggerFactory.getLogger(TesseractOcrService.class);

    private final String containerName;
    private final String localStoragePath;

    public TesseractOcrService(
            @Value("${app.ocr.tesseract.container-name:spm-tesseract}") String containerName,
            @Value("${app.storage.local-path:./uploads}") String localStoragePath) {
        this.containerName = containerName;
        this.localStoragePath = localStoragePath;
    }

    @Override
    public OcrResult extractText(String bucket, String key) {
        // The 'bucket' param is ignored for local — file is at localStoragePath/key
        // Inside the container, the shared volume is mounted at /data
        String containerPath = "/data/" + key;

        try {
            // For PDF files, tesseract needs them converted to images first.
            // We handle this by checking the extension and using appropriate flags.
            String ext = key.substring(key.lastIndexOf('.') + 1).toLowerCase();
            boolean isPdf = "pdf".equals(ext);

            List<String> command = new ArrayList<>();
            command.add("docker");
            command.add("exec");
            command.add(containerName);
            command.add("tesseract");
            command.add(containerPath);
            command.add("stdout"); // output to stdout
            command.add("-l");
            command.add("eng");

            if (isPdf) {
                // Tesseract can't directly read PDFs — we need to convert first.
                // Use a two-step approach: convert PDF pages to images, then OCR.
                // For simplicity, we'll try direct tesseract on the file and fall back.
                log.info("PDF detected — attempting OCR via tesseract on: {}", containerPath);
            }

            log.info("Running tesseract OCR: {}", String.join(" ", command));

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(false);
            Process process = pb.start();

            String stdout;
            String stderr;
            try (BufferedReader outReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                stdout = outReader.lines().collect(Collectors.joining("\n"));
                stderr = errReader.lines().collect(Collectors.joining("\n"));
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                log.error("Tesseract failed (exit {}): {}", exitCode, stderr);
                return new OcrResult(List.of(), "", "FAILED", 0.0f);
            }

            if (stdout.isBlank()) {
                log.warn("Tesseract returned empty text for: {}", key);
                return new OcrResult(List.of(), "", "COMPLETED", 0.0f);
            }

            // Build text blocks from lines
            List<OcrTextBlock> blocks = stdout.lines()
                    .filter(line -> !line.isBlank())
                    .map(line -> new OcrTextBlock(line.trim(), 0.85f, "LINE"))
                    .toList();

            // Tesseract CLI doesn't provide per-line confidence, so we use a default.
            // For more granular confidence, use tesseract's hOCR output format.
            float avgConfidence = blocks.isEmpty() ? 0.0f : 0.85f;

            log.info("Tesseract extracted {} text blocks from: {}", blocks.size(), key);
            return new OcrResult(blocks, stdout, "COMPLETED", avgConfidence);

        } catch (IOException | InterruptedException e) {
            log.error("Failed to run tesseract OCR on: {}", key, e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return new OcrResult(List.of(), "", "FAILED", 0.0f);
        }
    }
}
