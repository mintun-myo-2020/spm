# Unit 2: OCR Test Upload — Code Generation Plan

## Unit Context
- **Unit**: OCR Test Paper Upload
- **Scope**: New `testpaper` module (backend), upload component + side panel (frontend)
- **Dependencies**: TestScore entity (isDraft field addition), User entity, Student entity, TuitionClass entity
- **New Tables**: `test_paper_uploads`, `test_paper_pages`
- **New Interfaces**: FileStorageService, OcrService, TestPaperParser
- **Modified Entities**: TestScore (add isDraft), CreateTestScoreRequestDTO (add uploadIds, isDraft)
- **Functional Design**: `aidlc-docs/construction/ocr-test-upload/functional-design/`

## Existing Patterns to Follow
- Entities extend `BaseEntity` (UUID id, createdAt, updatedAt)
- DTOs are Java records
- Services are `@Service @Transactional`
- Controllers use `@RestController @RequestMapping("/api/v1")`
- Error handling via `AppException(ErrorCode, message)`
- Interface abstractions: follow `ReportStorage` / `StubReportStorage` pattern
- Profile selection: `@ConditionalOnProperty`
- Frontend: services use `apiClient`, types in `types/domain.ts` and `types/forms.ts`

---

## Plan Steps

### Step 1: Flyway Migration — New Tables + TestScore isDraft Column
- [ ] Create `V5__test_paper_uploads.sql`:
  - `test_paper_uploads` table (id, test_score_id FK nullable, student_id FK, class_id FK, status, uploaded_by FK, created_at)
  - `test_paper_pages` table (id, upload_id FK cascade, page_number, s3_bucket, s3_key, file_name, content_type, file_size_bytes, extracted_text, parsed_result, ocr_confidence, status, created_at)
  - Add `is_draft` boolean column to `test_scores` table (default false)
  - Indexes on student_id, class_id, test_score_id, upload_id
  - Unique constraint on (upload_id, page_number)

### Step 2: Backend — Enums + Value Objects
- [ ] Create `UploadStatus` enum in `testpaper/enums/`
- [ ] Create `PageStatus` enum in `testpaper/enums/`
- [ ] Create `OcrResult` record in `testpaper/ocr/`
- [ ] Create `OcrTextBlock` record in `testpaper/ocr/`
- [ ] Create `ParsedResult` record in `testpaper/parser/`
- [ ] Create `ParsedQuestion` record in `testpaper/parser/`
- [ ] Create `ParsedSubQuestion` record in `testpaper/parser/`
- [ ] Add new error codes to `ErrorCode` enum: INVALID_FILE_TYPE, FILE_TOO_LARGE, INVALID_FILE_CONTENT, STORAGE_ERROR, OCR_ERROR, UPLOAD_ALREADY_PROCESSING, UPLOAD_ALREADY_PROCESSED

### Step 3: Backend — Entities
- [ ] Create `TestPaperUpload` entity in `testpaper/entity/`
- [ ] Create `TestPaperPage` entity in `testpaper/entity/`
- [ ] Modify `TestScore` entity — add `isDraft` boolean field (default false)

### Step 4: Backend — Repositories
- [ ] Create `TestPaperUploadRepository` in `testpaper/repository/`
- [ ] Create `TestPaperPageRepository` in `testpaper/repository/`

### Step 5: Backend — Interfaces (Abstractions)
- [ ] Create `FileStorageService` interface in `testpaper/storage/`
- [ ] Create `OcrService` interface in `testpaper/ocr/`
- [ ] Create `TestPaperParser` interface in `testpaper/parser/`

### Step 6: Backend — Local Dev Implementations (Stubs)
- [ ] Create `LocalFileStorageService` in `testpaper/storage/` — writes to configurable local path, `@ConditionalOnProperty`
- [ ] Create `StubOcrService` in `testpaper/ocr/` — returns sample text with question patterns, `@ConditionalOnProperty`
- [ ] Create `BasicTestPaperParser` in `testpaper/parser/` — regex-based question/sub-question/marks detection
- [ ] Add config properties to `application.yml`: `app.storage.type`, `app.storage.local-path`, `app.ocr.type`

### Step 7: Backend — AWS Implementations (Production)
- [ ] Create `S3FileStorageService` in `testpaper/storage/` — uses AWS S3 SDK, `@ConditionalOnProperty`
- [ ] Create `TextractOcrService` in `testpaper/ocr/` — uses AWS Textract SDK, `@ConditionalOnProperty`
- [ ] Add Textract dependency to `build.gradle.kts`

### Step 8: Backend — DTOs
- [ ] Create `TestPaperUploadDTO` response record in `testpaper/dto/`
- [ ] Create `TestPaperPageDTO` response record in `testpaper/dto/`
- [ ] Create `ParsedResultDTO` response record in `testpaper/dto/`
- [ ] Create `ParsedQuestionDTO` response record in `testpaper/dto/`
- [ ] Modify `CreateTestScoreRequestDTO` — add optional `uploadIds` (List of UUID) and `isDraft` (Boolean)

### Step 9: Backend — TestPaperService (Core Business Logic)
- [ ] Create `TestPaperService` in `testpaper/service/`:
  - `uploadFiles()` — validate files, store via FileStorageService, create Upload + Pages
  - `triggerExtraction()` — async OCR + parsing for all pages, update statuses
  - `getUpload()` — return upload with pages, presigned URLs, parsed results
  - `linkToTestScore()` — set testScoreId on upload(s)
  - `createDraftTestScore()` — create draft TestScore from parsed results (student self-upload)
- [ ] Use `@Async` for extraction processing

### Step 10: Backend — TestPaperController
- [ ] Create `TestPaperController` in `testpaper/controller/`:
  - `POST /api/v1/test-papers/upload` — multipart upload
  - `POST /api/v1/test-papers/{uploadId}/extract` — trigger extraction (returns 202)
  - `GET /api/v1/test-papers/{uploadId}` — get status + results
  - `GET /api/v1/test-papers/files` — serve local files (dev only, conditional)
- [ ] Access control: teacher (own classes), student (self), admin (view only)

### Step 11: Backend — Modify TestScoreService for isDraft + uploadIds
- [ ] Update `createTestScore()` — accept uploadIds, link uploads, handle isDraft
- [ ] Update `updateTestScore()` — handle isDraft transition (false→true triggers full validation)
- [ ] Update progress/report queries to filter `WHERE is_draft = false`

### Step 12: Backend — Enable @Async
- [ ] Add `@EnableAsync` to `SpmApplication.java` (or create AsyncConfig)
- [ ] Add `uploads/` to `.gitignore`

### Step 13: Frontend — TypeScript Types
- [ ] Add OCR-related types to `types/domain.ts`: TestPaperUploadDTO, TestPaperPageDTO, ParsedResultDTO, ParsedQuestionDTO, ParsedSubQuestionDTO
- [ ] Update `CreateTestScoreForm` in `types/forms.ts` — add optional `uploadIds` and `isDraft`

### Step 14: Frontend — testPaperService
- [ ] Create `services/testPaperService.ts`:
  - `uploadFiles(files, studentId, classId)` — multipart POST
  - `triggerExtraction(uploadId)` — POST extract
  - `getUpload(uploadId)` — GET status + results
  - `pollForCompletion(uploadId)` — poll GET until terminal status

### Step 15: Frontend — TestPaperUpload Component
- [ ] Create `components/shared/TestPaperUpload.tsx`:
  - File drop zone / file picker (accept JPEG, PNG, PDF)
  - Multi-file selection
  - Upload progress indicator
  - Status display (uploading → processing → done/failed)
  - Triggers extraction after upload
  - Polls for completion
  - On completion: calls `onParsedResults` callback with aggregated questions

### Step 16: Frontend — OcrResultPanel Component
- [ ] Create `components/shared/OcrResultPanel.tsx`:
  - Collapsible side panel showing raw OCR text per page
  - Page tabs/selector for multi-page uploads
  - Confidence indicators per text block
  - File viewer (image/PDF preview via presigned URL)

### Step 17: Frontend — Integrate into TestScoreForm
- [ ] Add TestPaperUpload component to TestScoreForm
  - "Upload Test Paper" button/area above the questions section
  - On parsed results received: auto-populate form fields via `reset()` or `setValue()`
  - Low-confidence fields highlighted (yellow background)
  - OcrResultPanel shown as collapsible side panel
  - Upload is optional — form works without it
- [ ] Update form submission to include `uploadIds` if upload was done
- [ ] Handle isDraft flag for student self-upload flow

### Step 18: Frontend — Student Upload Entry Point
- [ ] Add upload capability to student's test score view (if student role)
  - Student sees "Upload My Paper" on their class/test page
  - Triggers upload → OCR → creates draft test score
  - Student sees confirmation that draft was created for teacher review

### Step 19: Code Summary Documentation
- [ ] Create `aidlc-docs/sprint-2/unit2-ocr-content/code-summary.md`

---

## File Inventory

### New Backend Files (~20 files)
- `spm/src/main/resources/db/migration/V5__test_paper_uploads.sql`
- `spm/src/main/java/com/eggtive/spm/testpaper/enums/UploadStatus.java`
- `spm/src/main/java/com/eggtive/spm/testpaper/enums/PageStatus.java`
- `spm/src/main/java/com/eggtive/spm/testpaper/entity/TestPaperUpload.java`
- `spm/src/main/java/com/eggtive/spm/testpaper/entity/TestPaperPage.java`
- `spm/src/main/java/com/eggtive/spm/testpaper/repository/TestPaperUploadRepository.java`
- `spm/src/main/java/com/eggtive/spm/testpaper/repository/TestPaperPageRepository.java`
- `spm/src/main/java/com/eggtive/spm/testpaper/storage/FileStorageService.java`
- `spm/src/main/java/com/eggtive/spm/testpaper/storage/LocalFileStorageService.java`
- `spm/src/main/java/com/eggtive/spm/testpaper/storage/S3FileStorageService.java`
- `spm/src/main/java/com/eggtive/spm/testpaper/ocr/OcrService.java`
- `spm/src/main/java/com/eggtive/spm/testpaper/ocr/OcrResult.java`
- `spm/src/main/java/com/eggtive/spm/testpaper/ocr/OcrTextBlock.java`
- `spm/src/main/java/com/eggtive/spm/testpaper/ocr/StubOcrService.java`
- `spm/src/main/java/com/eggtive/spm/testpaper/ocr/TextractOcrService.java`
- `spm/src/main/java/com/eggtive/spm/testpaper/parser/TestPaperParser.java`
- `spm/src/main/java/com/eggtive/spm/testpaper/parser/ParsedResult.java`
- `spm/src/main/java/com/eggtive/spm/testpaper/parser/ParsedQuestion.java`
- `spm/src/main/java/com/eggtive/spm/testpaper/parser/ParsedSubQuestion.java`
- `spm/src/main/java/com/eggtive/spm/testpaper/parser/BasicTestPaperParser.java`
- `spm/src/main/java/com/eggtive/spm/testpaper/dto/TestPaperUploadDTO.java`
- `spm/src/main/java/com/eggtive/spm/testpaper/dto/TestPaperPageDTO.java`
- `spm/src/main/java/com/eggtive/spm/testpaper/service/TestPaperService.java`
- `spm/src/main/java/com/eggtive/spm/testpaper/controller/TestPaperController.java`

### Modified Backend Files (~6 files)
- `spm/src/main/java/com/eggtive/spm/common/enums/ErrorCode.java` — add new error codes
- `spm/src/main/java/com/eggtive/spm/testscore/entity/TestScore.java` — add isDraft field
- `spm/src/main/java/com/eggtive/spm/testscore/dto/CreateTestScoreRequestDTO.java` — add uploadIds, isDraft
- `spm/src/main/java/com/eggtive/spm/testscore/service/TestScoreService.java` — isDraft + upload linking
- `spm/src/main/resources/application.yml` — add storage/OCR config
- `spm/build.gradle.kts` — add Textract dependency
- `spm/src/main/java/com/eggtive/spm/SpmApplication.java` — add @EnableAsync
- `.gitignore` — add uploads/

### New Frontend Files (~3 files)
- `spm-frontend/src/services/testPaperService.ts`
- `spm-frontend/src/components/shared/TestPaperUpload.tsx`
- `spm-frontend/src/components/shared/OcrResultPanel.tsx`

### Modified Frontend Files (~3 files)
- `spm-frontend/src/types/domain.ts` — add OCR types
- `spm-frontend/src/types/forms.ts` — update CreateTestScoreForm
- `spm-frontend/src/components/teacher/TestScoreForm.tsx` — integrate upload + auto-populate

---

**Document Version**: 1.0
**Last Updated**: 2026-03-15
**Status**: Draft — Awaiting Approval
