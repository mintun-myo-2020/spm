# Sprint 2 — Requirements

**Date**: 2026-03-15
**Scope**: 3 features — Progress Report Content, OCR Test Upload, Class Scheduling/Attendance

---

## Feature 1: Progress Report Content Generation (FR-9)

### Context
The report generation endpoint already exists (`POST /api/v1/students/{studentId}/reports`). ReportService, ReportStorage interface, S3 key generation, and ProgressReport entity are all in place. The only gap is the actual report content — currently a placeholder HTML string.

### Requirements

**FR-9.1**: When a report is generated, the system shall produce an HTML document containing:
- Student name and report date range
- Overall score summary (average, number of tests, improvement)
- Score trend table (test name, date, score, max score) for the date range
- Topic performance summary (topic name, average %, trend)
- Teacher feedback summary (most recent 5 feedback entries with strengths/improvements)

**FR-9.2**: The generated HTML shall be self-contained (inline CSS, no external dependencies) so it renders correctly when opened standalone or served from S3.

**FR-9.3**: The report shall be viewable in-browser via the existing `reportUrl` field returned by the API. No PDF generation required for this sprint.

### Existing Code to Modify
- `ReportService.generateReport()` — replace stub content with actual data assembly
- Pull data from `ProgressService`, `TestScoreService`, `FeedbackService`

### New Code Needed
- `ReportContentBuilder` — service/utility that takes student data and produces HTML string

---

## Feature 2: OCR Test Paper Upload (New)

### Context
Teachers currently enter test scores manually one at a time. This feature lets teachers photograph/scan a test paper, upload it, and have the system extract text via AWS Textract. The extracted text is shown to the teacher for review before they manually map scores — this is NOT auto-grading.

### Requirements

**FR-13.1**: Teachers shall be able to upload an image (JPEG, PNG) or PDF of a test paper from the test score entry page.

**FR-13.2**: The uploaded file shall be stored in S3 under a structured key: `uploads/{classId}/{studentId}/{timestamp}.{ext}`

**FR-13.3**: The system shall call AWS Textract `DetectDocumentText` (or `AnalyzeDocument`) on the uploaded file and return the extracted text blocks to the frontend.

**FR-13.4**: The extracted text shall be displayed in a read-only panel beside the test score form so the teacher can reference it while filling in scores manually.

**FR-13.5**: The upload is optional — teachers can still enter scores manually without uploading anything.

**FR-13.6**: Uploaded files shall be retained for 90 days then eligible for S3 lifecycle deletion.

### Architecture — Interface Abstractions

Both the file storage and OCR extraction MUST be behind interfaces so implementations can be swapped (e.g., local dev stubs, AWS prod, or future alternatives like Google Vision).

```java
/**
 * Abstraction for file storage (uploads, presigned URLs).
 * Implementations: LocalFileStorageService (dev), S3FileStorageService (prod)
 */
public interface FileStorageService {
    String upload(String key, byte[] content, String contentType);
    String generatePresignedUrl(String bucket, String key, int expiryMinutes);
    void delete(String bucket, String key);
}

/**
 * Abstraction for OCR text extraction from documents/images.
 * Implementations: StubOcrService (dev — returns dummy text), TextractOcrService (prod)
 */
public interface OcrService {
    OcrResult extractText(String s3Bucket, String s3Key);
}

public record OcrResult(
    List<OcrTextBlock> textBlocks,
    String rawText,           // all blocks concatenated
    String status             // COMPLETED, FAILED
) {}

public record OcrTextBlock(
    String text,
    float confidence,
    String blockType          // LINE, WORD, etc.
) {}
```

The `TestPaperService` depends on `FileStorageService` and `OcrService` interfaces only — never on AWS SDK classes directly. Spring profile-based `@ConditionalOnProperty` or `@Profile` selects the active implementation.

### Tech Stack
- AWS S3 for file storage (via `FileStorageService` interface — `S3FileStorageService` impl)
- AWS Textract for OCR (via `OcrService` interface — `TextractOcrService` impl)
- Local dev stubs: `LocalFileStorageService` (writes to disk), `StubOcrService` (returns sample text)
- Presigned URL or multipart upload from frontend

### New DB Schema
```sql
CREATE TABLE test_paper_uploads (
    id UUID PRIMARY KEY,
    test_score_id UUID REFERENCES test_scores(id),
    s3_bucket VARCHAR(255) NOT NULL,
    s3_key VARCHAR(500) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    extracted_text TEXT,
    textract_job_id VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'UPLOADED',  -- UPLOADED, PROCESSING, COMPLETED, FAILED
    uploaded_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

### API Endpoints
- `POST /api/v1/test-papers/upload` — upload file, store in S3, return upload ID
- `POST /api/v1/test-papers/{uploadId}/extract` — trigger Textract, return extracted text
- `GET /api/v1/test-papers/{uploadId}` — get upload status and extracted text

### Frontend Changes
- Add "Upload Test Paper" button/area on `TestScoreForm`
- Show extracted text in a collapsible side panel
- Upload status indicator (uploading → processing → done)

---

## Feature 3: Class Scheduling & Attendance (New)

### Context
No scheduling exists currently. Classes have students and teachers but no concept of scheduled sessions or attendance.

### Requirements

**FR-14.1**: Teachers and admins shall be able to create class schedules:
- Recurring weekly schedule (e.g., every Monday 4pm-6pm)
- One-off sessions (e.g., extra class on Saturday)
- Each session has: class, date, start time, end time, location (optional text field)

**FR-14.2**: The system shall auto-generate upcoming session instances from recurring schedules (generate 4 weeks ahead).

**FR-14.3**: Teachers and admins shall be able to:
- Cancel a specific session (with optional reason)
- Reschedule a session (change date/time)
- Mark attendance for each student (PRESENT, ABSENT, LATE, EXCUSED)

**FR-14.4**: Students shall be able to indicate availability for upcoming sessions:
- ATTENDING (default)
- NOT_ATTENDING (with optional reason)
- This is a soft RSVP, not binding — teacher still marks actual attendance

**FR-14.5**: Parents shall be able to view their child's upcoming schedule and attendance history.

**FR-14.6**: A simple calendar/list view shall show upcoming sessions for the logged-in user's role:
- Teacher: all their classes' sessions
- Student: their enrolled classes' sessions
- Parent: their child's sessions
- Admin: all sessions

### New DB Schema
```sql
CREATE TABLE class_schedules (
    id UUID PRIMARY KEY,
    class_id UUID NOT NULL REFERENCES classes(id),
    day_of_week INT,              -- 1=Monday..7=Sunday, NULL for one-off
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    location VARCHAR(255),
    is_recurring BOOLEAN NOT NULL DEFAULT true,
    effective_from DATE NOT NULL,
    effective_until DATE,         -- NULL = indefinite
    created_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE class_sessions (
    id UUID PRIMARY KEY,
    schedule_id UUID REFERENCES class_schedules(id),
    class_id UUID NOT NULL REFERENCES classes(id),
    session_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    location VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',  -- SCHEDULED, CANCELLED, COMPLETED
    cancel_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE session_attendance (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES class_sessions(id),
    student_id UUID NOT NULL REFERENCES students(id),
    status VARCHAR(20) NOT NULL DEFAULT 'UNMARKED',  -- UNMARKED, PRESENT, ABSENT, LATE, EXCUSED
    student_rsvp VARCHAR(20) DEFAULT 'ATTENDING',    -- ATTENDING, NOT_ATTENDING
    rsvp_reason TEXT,
    marked_by UUID REFERENCES users(id),
    marked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (session_id, student_id)
);
```

### API Endpoints
- `POST /api/v1/classes/{classId}/schedules` — create schedule
- `GET /api/v1/classes/{classId}/schedules` — list schedules
- `GET /api/v1/sessions/upcoming` — get upcoming sessions for current user
- `GET /api/v1/classes/{classId}/sessions` — list sessions for a class
- `PUT /api/v1/sessions/{sessionId}` — update session (reschedule)
- `PUT /api/v1/sessions/{sessionId}/cancel` — cancel session
- `POST /api/v1/sessions/{sessionId}/attendance` — mark attendance (batch)
- `PUT /api/v1/sessions/{sessionId}/rsvp` — student RSVP

### Frontend Changes
- New "Schedule" tab/page in teacher class detail view
- Schedule creation form (recurring or one-off)
- Session list with attendance marking (checkboxes per student)
- Student: upcoming sessions list with RSVP toggle
- Parent: child's schedule view (read-only)
- Admin: all sessions overview

---

## Units of Work (Implementation Order)

1. **Progress Report Content** — smallest scope, modifies existing code only, no new tables
2. **OCR Test Upload** — new feature but isolated (new table, S3 + Textract integration, form addition)
3. **Class Scheduling/Attendance** — largest scope (3 new tables, new module, multiple role views)
