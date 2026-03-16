# Unit 2: OCR Test Upload — Code Summary

## Overview
Full-stack implementation of test paper upload with OCR extraction. Teachers upload test paper images/PDFs from the test score form; the system extracts text via OCR, parses questions, and auto-populates the form. Students can also upload papers to create draft scores for teacher review.

## Backend (Spring Boot)

### New Module: `testpaper/`
- `entity/` — `TestPaperUpload`, `TestPaperPage` (2-table design with cascade)
- `enums/` — `UploadStatus` (UPLOADED→PROCESSING→COMPLETED/FAILED), `PageStatus`
- `repository/` — JPA repositories for both entities
- `storage/` — `FileStorageService` interface, `LocalFileStorageService` (dev), `S3FileStorageService` (prod)
- `ocr/` — `OcrService` interface, `TesseractOcrService` (dev via Docker), `StubOcrService` (fallback), `TextractOcrService` (prod)
- `parser/` — `TestPaperParser` interface, `BasicTestPaperParser` (regex-based), value objects (`ParsedResult`, `ParsedQuestion`, `ParsedSubQuestion`)
- `llm/` — `TestPaperExtractionService` interface, `BedrockExtractionService` (AWS Bedrock), `StubExtractionService`, model adapters (`AnthropicModelAdapter`, `TitanModelAdapter`)
- `dto/` — `TestPaperUploadDTO` (with `AggregatedQuestion` sub-records), `TestPaperPageDTO`
- `service/TestPaperService` — upload, async extraction, polling, linking to test scores
- `controller/TestPaperController` — 4 endpoints (upload, extract, get status, serve local files)

### Modified Files
- `TestScore` entity — added `isDraft` boolean
- `CreateTestScoreRequestDTO` — added `uploadIds`, `isDraft`
- `TestScoreService` — isDraft handling, upload linking
- `ErrorCode` — new OCR-related error codes
- `SpmApplication` — `@EnableAsync`
- `application.yml` — storage/OCR config properties
- `build.gradle.kts` — Textract dependency
- `docker-compose.yml` — Tesseract OCR container
- Flyway `V5__test_paper_uploads.sql` — new tables + isDraft column

## Frontend (React/TypeScript)

### New Files
- `services/testPaperService.ts` — upload, extract, poll API calls
- `components/shared/TestPaperUpload.tsx` — file picker, upload progress, extraction status, parsed results callback
- `components/shared/OcrResultPanel.tsx` — collapsible panel showing raw OCR text, page tabs, confidence indicators, file preview
- `components/student/UploadTestPaper.tsx` — student self-upload page (class selector → upload → draft score creation)

### Modified Files
- `types/domain.ts` — OCR types (TestPaperUploadDTO, TestPaperPageDTO, ParsedResultDTO, AggregatedQuestion, etc.)
- `types/forms.ts` — `CreateTestScoreFormWithUpload` extending base form with uploadIds/isDraft
- `services/testScoreService.ts` — accepts extended form type
- `components/teacher/TestScoreForm.tsx` — integrated TestPaperUpload + OcrResultPanel, auto-populates form from OCR results, sends uploadIds on submit
- `components/student/StudentDashboard.tsx` — added "Upload My Paper" button
- `components/student/StudentRoutes.tsx` — added `/student/upload` route
- `components/shared/Sidebar.tsx` — added "Upload Paper" nav link for students
