package com.eggtive.spm.testpaper.parser;

/**
 * Abstraction for structured parsing of OCR text into questions/sub-questions.
 * Current: BasicTestPaperParser (regex-based). Future: LlmTestPaperParser.
 */
public interface TestPaperParser {
    ParsedResult parse(String rawOcrText, float ocrConfidence);
}
