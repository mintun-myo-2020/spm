package com.eggtive.spm.testpaper.ocr;

/**
 * Abstraction for OCR text extraction. Stub for dev; Textract for production.
 */
public interface OcrService {
    OcrResult extractText(String bucket, String key);
}
