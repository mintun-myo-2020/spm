package com.eggtive.spm.testpaper.llm;

import com.eggtive.spm.testpaper.parser.ParsedResult;

/**
 * Abstraction for extracting structured question data from test paper images
 * using a multimodal LLM. Replaces the OCR + regex-parser pipeline with a
 * single vision-model call that returns structured results directly.
 *
 * Implementations: StubExtractionService (dev), BedrockExtractionService (prod).
 */
public interface TestPaperExtractionService {

    /**
     * Analyse a test paper image and extract structured question data.
     *
     * @param imageBytes  raw image content (JPEG or PNG)
     * @param contentType MIME type of the image (image/jpeg, image/png)
     * @param fileName    original file name (for logging / context)
     * @return parsed result containing questions, marks, and parsing notes
     */
    ParsedResult extractQuestions(byte[] imageBytes, String contentType, String fileName);
}
