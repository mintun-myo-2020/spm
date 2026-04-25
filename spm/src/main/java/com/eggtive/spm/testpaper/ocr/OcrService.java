package com.eggtive.spm.testpaper.ocr;

/**
 * Abstraction for OCR text extraction. Stub for dev; Textract for production.
 * Implementations: StubOcrService (default), TextractOcrService (prod).
 */
public interface OcrService {
    OcrResult extractText(String storageLocation, String storageKey);
}
