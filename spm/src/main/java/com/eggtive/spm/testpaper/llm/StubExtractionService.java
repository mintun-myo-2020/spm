package com.eggtive.spm.testpaper.llm;

import com.eggtive.spm.testpaper.parser.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Stub implementation for local development. Returns realistic sample data
 * so the full upload → extract → review flow can be tested without calling Bedrock.
 */
@Component
@ConditionalOnProperty(name = "app.extraction.type", havingValue = "stub", matchIfMissing = true)
public class StubExtractionService implements TestPaperExtractionService {

    private static final Logger log = LoggerFactory.getLogger(StubExtractionService.class);

    @Override
    public ParsedResult extractQuestions(byte[] imageBytes, String contentType, String fileName) {
        log.info("Stub extraction for file '{}' ({}, {} bytes)", fileName, contentType, imageBytes.length);

        List<ParsedQuestion> questions = List.of(
            new ParsedQuestion(
                "1",
                "What is the capital of France?",
                "MCQ",
                List.of(
                    new McqOption("A", "London"),
                    new McqOption("B", "Paris"),
                    new McqOption("C", "Berlin"),
                    new McqOption("D", "Madrid")
                ),
                new BigDecimal("2"),
                List.of(),
                false,
                false,
                0.95f,
                null
            ),
            new ParsedQuestion(
                "2",
                "Explain the water cycle.",
                "OPEN",
                List.of(),
                new BigDecimal("10"),
                List.of(
                    new ParsedSubQuestion("a", "Describe evaporation", new BigDecimal("5"),
                            "Water heats up and turns into vapor", null, null, 0.90f),
                    new ParsedSubQuestion("b", "Describe condensation", new BigDecimal("5"),
                            "Vapor cools and forms clouds", null, null, 0.88f)
                ),
                false,
                false,
                0.92f,
                null
            ),
            new ParsedQuestion(
                "3",
                "Solve the equation 2x + 5 = 15",
                "OPEN",
                List.of(),
                new BigDecimal("8"),
                List.of(
                    new ParsedSubQuestion("a", "Show your working", new BigDecimal("5"),
                            "2x = 10, x = 5", null, null, 0.85f),
                    new ParsedSubQuestion("b", "Verify your answer", new BigDecimal("3"),
                            "2(5) + 5 = 15, correct", null, null, 0.87f)
                ),
                false,
                false,
                0.90f,
                null
            )
        );

        return new ParsedResult(questions, new BigDecimal("20"), List.of("Stub extraction — sample data"));
    }
}
