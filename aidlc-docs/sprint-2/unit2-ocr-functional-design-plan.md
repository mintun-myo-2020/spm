# Unit 2: OCR Test Upload — Functional Design Plan

## Unit Context
- **Unit**: OCR Test Paper Upload (FR-13)
- **Scope**: New `testpaper` module — entity, repository, service, controller, interfaces (FileStorageService, OcrService), local dev stubs, AWS implementations, frontend upload component
- **Dependencies**: TestScore entity (nullable FK), User entity (uploaded_by), existing TestScoreForm (frontend integration point)
- **New Table**: `test_paper_uploads`
- **Key Design Goal**: Interface abstractions for file storage and OCR so implementations can be swapped (local dev stubs vs AWS S3/Textract)

---

## Questions

### Domain Model & Entity Design

**Q1**: The `test_paper_uploads` table has a `status` column with values UPLOADED, PROCESSING, COMPLETED, FAILED. Should the extraction be synchronous (teacher waits for Textract response inline) or asynchronous (upload returns immediately, teacher polls for status)?

Given the scope (single document, DetectDocumentText is fast ~2-5 seconds), synchronous is simpler. But async gives better UX for large PDFs.

- A) Synchronous — upload + extract in one request, teacher waits (simpler, fewer endpoints)
- B) Asynchronous — upload returns immediately, separate extract call, frontend polls for completion
- C) Hybrid — upload is instant, extract is a separate call but blocks until done (current API design: POST upload, then POST extract)

[Answer]: B, show that it's processessing and then completed when completed.

---

**Q2**: The requirements specify `test_paper_uploads.test_score_id` is nullable — teachers can upload before creating the test score. How should the linking work?

- A) Frontend sends `testScoreId` in the upload request if available, otherwise null. A separate PATCH/PUT endpoint links the upload to a test score later.
- B) Frontend always uploads without testScoreId. When the test score is created/saved, the frontend sends the uploadId(s) along with the test score data, and the backend links them.
- C) Both — upload accepts optional testScoreId, and test score creation accepts optional uploadId(s). Either direction works.

[Answer]: B

---

**Q3**: Should a single test score be linkable to multiple uploads (e.g., teacher uploads front page, then back page separately)? Or is it one upload per test score?

- A) One-to-many — a test score can have multiple uploads (multiple photos/pages uploaded separately)
- B) One-to-one — one upload per test score (teacher combines pages into one PDF or uploads one image)
- C) One-to-many but with a practical limit (e.g., max 5 uploads per test score)

[Answer]:the point of the upload is to extract the questions (question text + student's answer), if mcq give all options and update accordingly etc, then also add how much marks the question (and subquestions) are worth and how much the student got. score is just 1 page and teacher can add that number themselves.

---

### Business Logic & Workflow

**Q4**: When the teacher uploads a file and triggers extraction, should the extracted text be persisted in the `extracted_text` column permanently, or should it be treated as ephemeral (only shown during the current session)?

- A) Persist — store extracted text in DB, retrievable later via GET endpoint
- B) Ephemeral — return extracted text in the extract response only, don't store
- C) Persist with TTL — store but clear after a period (e.g., 90 days, matching file retention)

[Answer]: A, BASED ON Q3 RIGHT?

---

**Q5**: The requirements mention the extracted text is shown in a "read-only panel beside the test score form." Should the teacher be able to:

- A) View only — extracted text is read-only, teacher manually types scores while referencing it
- B) Copy-paste — extracted text is selectable/copyable so teacher can copy values into score fields
- C) Both view and copy-paste (standard text selection behavior)

[Answer]: this should be automatically pasted in, teacher just validates

---

**Q6**: Should there be any validation on the uploaded file beyond file type (JPEG, PNG, PDF) and size (50 MB)?

- A) No additional validation — just type and size checks
- B) Basic image validation — verify the file is a valid image/PDF (not just checking extension)
- C) Content validation — check that the file contains readable text (reject blank images)

[Answer]: B

---

### API Design

**Q7**: The current API design has 3 endpoints: POST upload, POST extract, GET status. Should the upload endpoint accept multiple files in a single request, or one file per request?

- A) Single file per request — simpler, teacher uploads one at a time
- B) Multi-file per request — teacher can select multiple files and upload in batch
- C) Single file per request, but frontend handles sequential uploads with progress for each

[Answer]: B. MULTIPLE FILES 

---

**Q8**: For the GET endpoint (`GET /api/v1/test-papers/{uploadId}`), what should be returned?

- A) Upload metadata + extracted text + presigned URL to view the original file
- B) Upload metadata + extracted text only (no file URL — file is just for OCR processing)
- C) Upload metadata + extracted text + presigned URL + OCR confidence scores per text block

[Answer]: A

---

### Integration & Access Control

**Q9**: Who should be able to upload test papers and view uploads?

- A) Only the teacher who owns the class can upload and view
- B) Teachers upload for their classes, admins can upload/view for any class
- C) Teachers upload for their classes, admins can view (but not upload) for any class

[Answer]: C. STUDENTS can also upload for themselves.

---

**Q10**: Should there be a list/history endpoint to see all uploads for a given test score or student?

- A) Yes — `GET /api/v1/test-papers?testScoreId={id}` to list uploads linked to a test score
- B) Yes — both by test score and by student: `GET /api/v1/test-papers?testScoreId={id}` and `GET /api/v1/test-papers?studentId={id}`
- C) No — uploads are only accessed by their individual uploadId, no listing needed

[Answer]: C

---

## Plan Steps

### Step 1: Analyze Unit Context
- [x] Review requirements (FR-13.1 through FR-13.8)
- [x] Review existing TestScore entity and relationships
- [x] Review existing ReportStorage interface pattern (for consistency)

### Step 2: Collect and Analyze Answers
- [x] Wait for user answers to Q1-Q10
- [x] Analyze for ambiguities
- [x] Create clarification questions if needed

### Step 3: Generate Domain Entities
- [x] Create `domain-entities.md` — TestPaperUpload entity, enums (UploadStatus), relationships
- [x] Document FileStorageService and OcrService interfaces with method signatures
- [x] Document OcrResult and OcrTextBlock records

### Step 4: Generate Business Logic Model
- [x] Create `business-logic-model.md` — upload workflow, extraction workflow, linking workflow
- [x] Document data flow: upload → store → extract → display
- [x] Document stub vs production implementation switching

### Step 5: Generate Business Rules
- [x] Create `business-rules.md` — validation rules, access control rules, file retention rules
- [x] Document file type/size constraints
- [x] Document status transitions (UPLOADED → PROCESSING → COMPLETED/FAILED)

### Step 6: Generate API Contracts
- [x] Create `api-contracts.md` — endpoint specifications with request/response DTOs
- [x] Document error responses
- [x] Document presigned URL generation

### Step 7: Present Completion Message
- [ ] Present functional design for user review

---

**Document Version**: 1.0
**Last Updated**: 2026-03-15
**Status**: Draft — Awaiting User Answers
