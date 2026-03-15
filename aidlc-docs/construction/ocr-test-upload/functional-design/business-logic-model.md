# Unit 2: OCR Test Upload — Business Logic Model

## Workflow 1: Multi-File Upload

### Trigger
Teacher or student uploads one or more files (images/PDF) for a test paper.

### Flow
1. Frontend sends `POST /api/v1/test-papers/upload` with multipart form data:
   - `files[]` — one or more files (JPEG, PNG, PDF)
   - `studentId` — the student whose paper this is
   - `classId` — the class context
2. Backend validates:
   - File types (JPEG, PNG, PDF only)
   - Individual file size (each ≤ 50 MB)
   - File content validity (magic bytes check — not just extension)
   - User authorization (teacher owns class, or student is self-uploading for their own class)
3. Create `TestPaperUpload` record with status `UPLOADED`
4. For each file, in order:
   - Determine page number (1-based, ordered by upload position)
   - Generate S3 key: `uploads/{classId}/{studentId}/{uploadId}/{pageNumber}.{ext}`
   - Store file via `FileStorageService.upload()`
   - Create `TestPaperPage` record with status `PENDING`
5. Return upload ID + page metadata (IDs, file names, page numbers)

### Error Handling
- If any file fails validation, reject the entire upload (no partial uploads)
- If file storage fails mid-upload, mark upload as FAILED, clean up any stored files
- Return specific error codes for: invalid file type, file too large, invalid file content

---

## Workflow 2: OCR Extraction (Asynchronous)

### Trigger
Frontend calls `POST /api/v1/test-papers/{uploadId}/extract` after upload completes.

### Flow
1. Load `TestPaperUpload` and all its pages
2. Validate status is `UPLOADED` (not already processing/completed)
3. Set upload status to `PROCESSING`
4. For each page (ordered by page number):
   a. Set page status to `PROCESSING`
   b. Call `OcrService.extractText(bucket, key)` for this page
   c. If OCR succeeds:
      - Store raw text in `TestPaperPage.extractedText`
      - Store OCR confidence in `TestPaperPage.ocrConfidence`
      - Call `TestPaperParser.parse(rawText, confidence)` to attempt structured parsing
      - Store parsed result JSON in `TestPaperPage.parsedResult`
      - Set page status to `COMPLETED`
   d. If OCR fails:
      - Set page status to `FAILED`
      - Log error details
5. Determine overall upload status:
   - All pages COMPLETED → upload status = `COMPLETED`
   - Some pages COMPLETED, some FAILED → upload status = `PARTIALLY_COMPLETED`
   - All pages FAILED → upload status = `FAILED`
6. Return aggregated result to frontend (see API contracts for response shape)

### Asynchronous Behavior
- The extract endpoint triggers processing and returns immediately with status `PROCESSING`
- Frontend polls `GET /api/v1/test-papers/{uploadId}` to check completion
- Polling interval: frontend starts at 1s, backs off to 3s
- Processing is synchronous on the server side (Textract DetectDocumentText is fast, ~2-5s per page)
- The "async" aspect is from the frontend's perspective — upload and extract are separate calls

### Aggregation Across Pages
- Parsed questions from all pages are aggregated in page order
- Question numbering is preserved from the original paper (parser detects question numbers)
- If the same question number appears on multiple pages (e.g., question continues across pages), the parser treats them as separate entries — the teacher resolves during review

---

## Workflow 3: Structured Parsing (Hybrid Approach)

### Purpose
Attempt to extract structured question/sub-question/marks data from raw OCR text. This is the "hybrid" approach — parse what's confidently detectable, show raw text for the rest.

### BasicTestPaperParser Logic

#### Question Detection
1. Scan for patterns matching question numbers:
   - `Q1`, `Q2`, `Question 1`, `Question 2`
   - `1.`, `2.`, `1)`, `2)`
   - Case-insensitive matching
2. For each detected question, extract:
   - Question number (normalized to "1", "2", etc.)
   - Question text (text between question number and next question/marks indicator)
   - Max marks if present (patterns: `[10 marks]`, `(10)`, `/10`, `10 marks`)

#### Sub-Question Detection
1. Within each question block, scan for sub-question patterns:
   - `a)`, `b)`, `(a)`, `(b)`
   - `i)`, `ii)`, `(i)`, `(ii)`
   - `a.`, `b.`, `i.`, `ii.`
2. Extract label, text, and marks for each sub-question

#### MCQ Detection
1. Detect MCQ patterns within a question:
   - `A.`, `B.`, `C.`, `D.` or `A)`, `B)`, `C)`, `D)`
   - At least 3 options to classify as MCQ
2. If MCQ detected, set `questionType = "MCQ"` and populate `mcqOptions`
3. Attempt to detect student's selected answer (circled/marked option — limited accuracy from OCR)

#### Student Answer Detection
1. For open questions, look for handwritten text regions (lower confidence blocks from OCR)
2. Store as `studentAnswer` on the sub-question
3. This is best-effort — handwriting OCR accuracy varies significantly

#### Confidence Scoring
- Each parsed field gets a confidence score based on:
  - OCR block confidence for the source text
  - Pattern match strength (exact match = high, fuzzy = lower)
  - Structural consistency (question numbers in sequence = higher confidence)
- Overall question confidence = minimum of its field confidences
- Fields below 0.5 confidence are included but flagged for teacher review

#### Limitations (Documented)
- Cannot reliably parse handwritten text (OCR confidence will be low)
- Cannot detect diagrams, graphs, or images within papers
- Question numbering schemes vary widely — parser handles common patterns only
- Multi-column layouts may confuse text ordering
- These limitations are acceptable for the hybrid approach — teacher validates everything

---

## Workflow 4: Auto-Population into TestScoreForm

### Purpose
Map parsed OCR results to the TestScoreForm fields so the teacher (or student-uploaded draft) starts with pre-filled data instead of an empty form.

### Frontend Flow
1. After extraction completes, frontend receives aggregated parsed results
2. Frontend maps `ParsedQuestion` list to the form's `questions` array:
   - `parsedQuestion.questionNumber` → `questionNumber` field
   - `parsedQuestion.questionText` → `questionText` field
   - `parsedQuestion.questionType` → `questionType` selector (OPEN/MCQ)
   - `parsedQuestion.mcqOptions` → MCQ options array
   - `parsedQuestion.maxScore` → `maxScore` field (if detected)
   - For each `parsedSubQuestion`:
     - `label` → sub-question `label`
     - `questionText` → not directly mapped (shown as reference)
     - `maxScore` → sub-question `maxScore` (if detected)
     - `studentAnswer` → sub-question `studentAnswer`
     - `score` → left as 0 (teacher must grade)
     - `topicId` → left empty (teacher must assign topic)
3. Fields that couldn't be parsed are left empty/default
4. Low-confidence fields are visually highlighted (yellow background) so teacher knows to verify
5. Raw OCR text is shown in a collapsible side panel for reference

### Teacher Review
- Teacher can modify any auto-populated field
- Teacher must fill in: `score` (grading), `topicId` (topic mapping), `overallScore`, `testName`, `testDate`
- Teacher can add/remove questions and sub-questions as needed
- Form validation runs normally — all required fields must be filled before save

### Student Self-Upload Flow
1. Student uploads their own paper via the same upload endpoint
2. System runs OCR + parsing
3. System creates a "draft" test score entry:
   - Auto-populated questions/sub-questions from parsing
   - `overallScore` = 0 (teacher must set)
   - `score` on each sub-question = 0 (teacher must grade)
   - `topicId` on each sub-question = empty (teacher must assign)
   - Status concept: the test score is created but marked as needing teacher review
4. Teacher sees pending drafts in their class view
5. Teacher opens the draft, reviews/corrects the auto-populated data, fills in scores and topics, then approves (saves)

### Draft Test Score Mechanism
- When a student uploads, the system creates a `TestScore` with a new boolean field `isDraft = true`
- Draft test scores are excluded from progress calculations and reports
- Teacher sets `isDraft = false` when they approve (via the normal update endpoint)
- Frontend shows draft scores with a visual indicator (badge/tag)

---

## Workflow 5: Upload Status Polling

### Frontend Polling Flow
1. After calling extract, frontend enters polling state
2. Poll `GET /api/v1/test-papers/{uploadId}` every 1-3 seconds
3. Display status indicator:
   - `PROCESSING` → spinner with "Extracting text..."
   - `COMPLETED` → success indicator, show parsed results + auto-populate form
   - `PARTIALLY_COMPLETED` → warning indicator, show what was parsed, note failed pages
   - `FAILED` → error indicator, show raw text panel only (no auto-population)
4. Stop polling when status is terminal (COMPLETED, PARTIALLY_COMPLETED, FAILED)

---

## Workflow 6: Presigned URL for File Viewing

### Purpose
Allow teacher to view the original uploaded file(s) alongside the extracted text.

### Flow
1. Frontend requests `GET /api/v1/test-papers/{uploadId}` which includes page details
2. Each page includes a presigned URL (generated on-the-fly, valid for 15 minutes)
3. Frontend renders file viewer (image tag for JPEG/PNG, embedded PDF viewer for PDF)
4. URLs expire after 15 minutes — frontend can re-fetch if needed

### Local Dev Behavior
- `LocalFileStorageService.generatePresignedUrl()` returns an API-relative path
- A controller endpoint serves the file from the local filesystem
- Same frontend code works for both local and S3 — just different URL formats

---

## Data Flow Summary

```
Teacher/Student uploads files
        |
        v
POST /upload (multipart)
        |
        v
Validate files → Store via FileStorageService → Create Upload + Pages
        |
        v
POST /extract (trigger)
        |
        v
For each page: OcrService.extractText() → TestPaperParser.parse() → Store results
        |
        v
GET /status (poll)
        |
        v
Frontend receives parsed results → Auto-populate TestScoreForm
        |
        v
Teacher reviews, corrects, fills scores/topics → Save TestScore
        |
        v
Link upload to test score (uploadId sent with create request)
```

---

**Document Version**: 1.0
**Last Updated**: 2026-03-15
