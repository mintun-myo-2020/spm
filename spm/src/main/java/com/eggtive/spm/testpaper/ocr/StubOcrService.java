package com.eggtive.spm.testpaper.ocr;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Stub OCR service for local development. Returns sample text with question patterns
 * so the BasicTestPaperParser can be tested end-to-end.
 */
@Component
@ConditionalOnProperty(name = "app.ocr.type", havingValue = "stub")
public class StubOcrService implements OcrService {

    private static final String SAMPLE_TEXT = """
            Question 1: What is the capital of France? [2 marks]
            A. London
            B. Paris
            C. Berlin
            D. Madrid
            Student selected: B

            Question 2: Explain the water cycle. [10 marks]
            (a) Describe evaporation [5 marks]
            Student answer: Water heats up and turns into vapor
            (b) Describe condensation [5 marks]
            Student answer: Vapor cools and forms clouds

            Question 3: Solve the equation 2x + 5 = 15 [8 marks]
            (a) Show your working [5 marks]
            Student answer: 2x = 10, x = 5
            (b) Verify your answer [3 marks]
            Student answer: 2(5) + 5 = 15, correct
            """;

    @Override
    public OcrResult extractText(String bucket, String key) {
        List<OcrTextBlock> blocks = SAMPLE_TEXT.lines()
                .filter(line -> !line.isBlank())
                .map(line -> new OcrTextBlock(line.trim(), 0.95f, "LINE"))
                .toList();
        return new OcrResult(blocks, SAMPLE_TEXT, "COMPLETED", 0.95f);
    }
}
