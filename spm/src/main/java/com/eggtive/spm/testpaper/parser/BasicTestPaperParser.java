package com.eggtive.spm.testpaper.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regex-based parser that attempts to extract structured question data from raw OCR text.
 * Handles common exam paper patterns. Future LLM implementation will replace this.
 */
@Component
public class BasicTestPaperParser implements TestPaperParser {

    private static final Logger log = LoggerFactory.getLogger(BasicTestPaperParser.class);

    // Matches: "Question 1", "Q1", "1.", "1)"
    private static final Pattern QUESTION_PATTERN = Pattern.compile(
            "(?i)(?:question\\s*|q)(\\d+)[.:\\s)]|^(\\d+)[.)\\s]",
            Pattern.MULTILINE);

    // Matches: "[10 marks]", "(10)", "/10", "10 marks"
    private static final Pattern MARKS_PATTERN = Pattern.compile(
            "\\[(\\d+)\\s*marks?]|\\((\\d+)\\)|/(\\d+)|\\b(\\d+)\\s*marks?\\b",
            Pattern.CASE_INSENSITIVE);

    // Matches: "(a)", "a)", "a.", "(i)", "i)"
    private static final Pattern SUB_QUESTION_PATTERN = Pattern.compile(
            "^\\s*\\(?([a-z]|[ivx]+)[.)\\s]",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    // Matches MCQ options: "A.", "A)", "B.", etc.
    private static final Pattern MCQ_OPTION_PATTERN = Pattern.compile(
            "^\\s*([A-D])[.)\\s]\\s*(.+)",
            Pattern.MULTILINE);

    // Matches: "Student answer:", "Student selected:", "Answer:"
    private static final Pattern ANSWER_PATTERN = Pattern.compile(
            "(?i)(?:student\\s+(?:answer|selected)|answer)\\s*:\\s*(.+)");

    @Override
    public ParsedResult parse(String rawOcrText, float ocrConfidence) {
        if (rawOcrText == null || rawOcrText.isBlank()) {
            return new ParsedResult(List.of(), null, List.of("No text to parse"));
        }

        List<ParsedQuestion> questions = new ArrayList<>();
        List<String> notes = new ArrayList<>();
        BigDecimal totalMarks = BigDecimal.ZERO;

        // Split text into question blocks
        List<QuestionBlock> blocks = splitIntoQuestionBlocks(rawOcrText);

        for (QuestionBlock block : blocks) {
            try {
                ParsedQuestion pq = parseQuestionBlock(block, ocrConfidence);
                questions.add(pq);
                if (pq.maxScore() != null) {
                    totalMarks = totalMarks.add(pq.maxScore());
                }
            } catch (Exception e) {
                log.warn("Failed to parse question block: {}", block.rawText(), e);
                notes.add("Failed to parse question starting with: " + block.rawText().substring(0, Math.min(50, block.rawText().length())));
            }
        }

        if (questions.isEmpty()) {
            notes.add("No questions detected — raw text available for manual reference");
        }

        return new ParsedResult(questions, totalMarks.compareTo(BigDecimal.ZERO) > 0 ? totalMarks : null, notes);
    }

    private List<QuestionBlock> splitIntoQuestionBlocks(String text) {
        List<QuestionBlock> blocks = new ArrayList<>();
        Matcher matcher = QUESTION_PATTERN.matcher(text);
        List<int[]> positions = new ArrayList<>();

        while (matcher.find()) {
            String num = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            positions.add(new int[]{matcher.start(), Integer.parseInt(num)});
        }

        for (int i = 0; i < positions.size(); i++) {
            int start = positions.get(i)[0];
            int end = (i + 1 < positions.size()) ? positions.get(i + 1)[0] : text.length();
            String blockText = text.substring(start, end).trim();
            blocks.add(new QuestionBlock(String.valueOf(positions.get(i)[1]), blockText));
        }

        return blocks;
    }

    private ParsedQuestion parseQuestionBlock(QuestionBlock block, float ocrConfidence) {
        String text = block.rawText();
        String questionNumber = block.number();

        // Extract marks
        BigDecimal maxScore = extractMarks(text);

        // Detect MCQ options
        List<McqOption> mcqOptions = extractMcqOptions(text);
        boolean isMcq = mcqOptions.size() >= 3;

        // Extract sub-questions
        List<ParsedSubQuestion> subQuestions = isMcq ? List.of() : extractSubQuestions(text, ocrConfidence);

        // Extract question text (first line after question number, before options/sub-questions)
        String questionText = extractQuestionText(text);

        // Extract student answer for MCQ
        String studentAnswer = null;
        if (isMcq) {
            Matcher answerMatcher = ANSWER_PATTERN.matcher(text);
            if (answerMatcher.find()) {
                studentAnswer = answerMatcher.group(1).trim();
            }
        }

        float confidence = calculateConfidence(ocrConfidence, questionText, maxScore, isMcq ? mcqOptions.size() : subQuestions.size());

        return new ParsedQuestion(
                questionNumber, questionText,
                isMcq ? "MCQ" : "OPEN",
                mcqOptions, maxScore, subQuestions,
                confidence, text
        );
    }

    private BigDecimal extractMarks(String text) {
        // Look for marks pattern in the first line (question-level marks)
        String firstLine = text.lines().findFirst().orElse("");
        Matcher m = MARKS_PATTERN.matcher(firstLine);
        if (m.find()) {
            for (int i = 1; i <= m.groupCount(); i++) {
                if (m.group(i) != null) return new BigDecimal(m.group(i));
            }
        }
        return null;
    }

    private List<McqOption> extractMcqOptions(String text) {
        List<McqOption> options = new ArrayList<>();
        Matcher m = MCQ_OPTION_PATTERN.matcher(text);
        while (m.find()) {
            options.add(new McqOption(m.group(1), m.group(2).trim()));
        }
        return options;
    }

    private List<ParsedSubQuestion> extractSubQuestions(String text, float ocrConfidence) {
        List<ParsedSubQuestion> subs = new ArrayList<>();
        String[] lines = text.split("\n");
        String currentLabel = null;
        StringBuilder currentText = new StringBuilder();
        BigDecimal currentMarks = null;
        String currentAnswer = null;

        for (String line : lines) {
            Matcher subMatcher = SUB_QUESTION_PATTERN.matcher(line);
            if (subMatcher.find()) {
                // Save previous sub-question
                if (currentLabel != null) {
                    subs.add(new ParsedSubQuestion(currentLabel, currentText.toString().trim(), currentMarks, currentAnswer, ocrConfidence * 0.85f));
                }
                currentLabel = subMatcher.group(1);
                currentText = new StringBuilder(line.substring(subMatcher.end()).trim());
                currentMarks = extractMarks(line);
                currentAnswer = null;
            } else if (currentLabel != null) {
                Matcher answerMatcher = ANSWER_PATTERN.matcher(line);
                if (answerMatcher.find()) {
                    currentAnswer = answerMatcher.group(1).trim();
                } else {
                    Matcher marksMatcher = MARKS_PATTERN.matcher(line);
                    if (marksMatcher.find() && currentMarks == null) {
                        for (int i = 1; i <= marksMatcher.groupCount(); i++) {
                            if (marksMatcher.group(i) != null) {
                                currentMarks = new BigDecimal(marksMatcher.group(i));
                                break;
                            }
                        }
                    }
                }
            }
        }
        // Save last sub-question
        if (currentLabel != null) {
            subs.add(new ParsedSubQuestion(currentLabel, currentText.toString().trim(), currentMarks, currentAnswer, ocrConfidence * 0.85f));
        }
        return subs;
    }

    private String extractQuestionText(String text) {
        // Take text after question number pattern, before first sub-question or MCQ option
        String[] lines = text.split("\n");
        if (lines.length == 0) return "";
        // First line minus the question number prefix and marks
        String first = lines[0].replaceAll("(?i)(?:question\\s*|q)\\d+[.:\\s)]*", "").trim();
        first = MARKS_PATTERN.matcher(first).replaceAll("").trim();
        return first.isEmpty() && lines.length > 1 ? lines[1].trim() : first;
    }

    private float calculateConfidence(float ocrConfidence, String questionText, BigDecimal maxScore, int childCount) {
        float conf = ocrConfidence;
        if (questionText == null || questionText.isBlank()) conf *= 0.7f;
        if (maxScore == null) conf *= 0.8f;
        if (childCount == 0) conf *= 0.9f;
        return Math.max(0.0f, Math.min(1.0f, conf));
    }

    private record QuestionBlock(String number, String rawText) {}
}
