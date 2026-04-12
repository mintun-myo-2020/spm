package com.eggtive.spm.testpaper.llm;

import com.eggtive.spm.testpaper.parser.*;
import tools.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.*;

/**
 * Production implementation that sends test paper images to Amazon Bedrock
 * and receives structured question data back.
 *
 * <p>Model-specific request/response formatting is delegated to a
 * {@link ModelAdapter}, so switching models (Claude → Nova → Titan, etc.)
 * is a config change: set {@code app.extraction.bedrock.model-id} and
 * {@code app.extraction.bedrock.model-adapter} accordingly.</p>
 */
@Component
@ConditionalOnProperty(name = "app.extraction.type", havingValue = "bedrock")
public class BedrockExtractionService implements TestPaperExtractionService {

    private static final Logger log = LoggerFactory.getLogger(BedrockExtractionService.class);

    private final BedrockRuntimeClient bedrockClient;
    private final ModelAdapter modelAdapter;
    private final JsonMapper jsonMapper;
    private final String modelId;

    public BedrockExtractionService(BedrockRuntimeClient bedrockClient,
                                    ModelAdapter modelAdapter,
                                    JsonMapper jsonMapper,
                                    @Value("${app.extraction.bedrock.model-id}") String modelId) {
        this.bedrockClient = bedrockClient;
        this.modelAdapter = modelAdapter;
        this.jsonMapper = jsonMapper;
        this.modelId = modelId;
        log.info("Bedrock extraction configured — model: {}, adapter: {}",
                modelId, modelAdapter.getClass().getSimpleName());
    }

    @Override
    public ParsedResult extractQuestions(byte[] imageBytes, String contentType, String fileName, List<String> topicNames) {
        log.info("Bedrock extraction for '{}' ({}, {} bytes) using model {}",
                fileName, contentType, imageBytes.length, modelId);
        try {
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String mediaType = contentType != null ? contentType : "image/jpeg";

            String prompt = buildPrompt(topicNames);
            String requestBody = modelAdapter.buildRequestBody(base64Image, mediaType, prompt);

            InvokeModelResponse response = bedrockClient.invokeModel(
                    InvokeModelRequest.builder()
                            .modelId(modelId)
                            .contentType("application/json")
                            .accept("application/json")
                            .body(SdkBytes.fromUtf8String(requestBody))
                            .build());

            String responseBody = response.body().asUtf8String();
            String textContent = modelAdapter.extractResponseText(responseBody);

            return ExtractionResponseMapper.map(textContent, jsonMapper);

        } catch (Exception e) {
            log.error("Bedrock extraction failed for '{}'", fileName, e);
            return new ParsedResult(List.of(), null,
                    List.of("LLM extraction failed: " + e.getMessage()));
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    /** Build the extraction prompt, injecting topic names when available. */
    static String buildPrompt(List<String> topicNames) {
        if (topicNames == null || topicNames.isEmpty()) {
            return EXTRACTION_PROMPT;
        }
        String topicList = String.join(", ", topicNames);
        return EXTRACTION_PROMPT + "\n\nAvailable topics for this subject: [" + topicList
                + "]. For each question, set \"topicHint\" to the best-matching topic name from this list. "
                + "If no topic clearly matches, set topicHint to null.";
    }

    static final String EXTRACTION_PROMPT = """
            You are analysing a photograph of a student's test paper. Extract ALL questions \
            visible on this page and return them as structured JSON.

            Return ONLY valid JSON (no markdown, no explanation) with this exact schema:
            {
              "questions": [
                {
                  "questionNumber": "1",
                  "questionText": "Full question text as written",
                  "questionType": "MCQ" or "OPEN",
                  "mcqOptions": [
                    { "key": "A", "text": "Option text" }
                  ],
                  "maxScore": 10,
                  "studentAnswer": "The student's selected answer (e.g. 'A' for MCQ, or written text for OPEN)",
                  "topicHint": "Best-matching topic name from the provided list, or null",
                  "hasDiagramInQuestion": false,
                  "requiresDiagramAnswer": false,
                  "subQuestions": [
                    {
                      "label": "a",
                      "questionText": "Sub-question text",
                      "maxScore": 5,
                      "studentAnswer": "What the student originally wrote (null if blank/illegible)",
                      "studentCorrection": "Green text showing the correct answer (null if none)",
                      "teacherRemarks": "Teacher's feedback or comments (null if none)",
                      "confidence": 0.85
                    }
                  ],
                  "confidence": 0.90
                }
              ],
              "notes": ["Any observations about image quality or illegible sections"]
            }

            Rules:
            - questionType is "MCQ" if the question has multiple-choice options, otherwise "OPEN"
            - For MCQ questions, include all visible options in mcqOptions
            - For MCQ questions, the student may have written the letter/number OR circled/ticked the chosen option — detect either method and record the selected option key in the top-level studentAnswer field
            - For OPEN questions without sub-parts, record the student's written answer in the top-level studentAnswer field
            - For OPEN questions with parts (a, b, c or i, ii, iii), list them as subQuestions with individual studentAnswer fields; the top-level studentAnswer can be null
            - studentAnswer should ONLY contain what the student originally wrote (typically blue/black ink)
            - GREEN TEXT indicates corrections — extract this into studentCorrection, NOT studentAnswer
            - teacherRemarks is for any other teacher feedback, comments, or annotations (not the correction itself)
            - topicHint: if a list of topics is provided, classify each question to the best-matching topic; otherwise set to null
            - hasDiagramInQuestion: true if the question includes a diagram, graph, chart, or image as part of the question itself
            - requiresDiagramAnswer: true if the student was required to draw a diagram, graph, or chart as part of their answer
            - If handwriting is illegible, set the field to null and lower the confidence
            - maxScore is the marks allocated (look for "[X marks]", "(X)", "/X" patterns)
            - confidence is your certainty about the extraction (0.0 to 1.0)
            - If you cannot determine a field, set it to null
            """;
}
