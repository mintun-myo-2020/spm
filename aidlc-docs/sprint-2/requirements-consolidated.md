# Sprint 2 — Consolidated Requirements

**Date**: 2026-03-15
**Scope**: 3 features — Progress Report Content, OCR Test Upload, Class Scheduling/Attendance
**Unit Structure**: Per-feature units (each feature = backend + frontend together)

---

## Feature 1: Progress Report Content Generation (FR-9)

### Context
The report generation endpoint already exists (`POST /api/v1/students/{studentId}/reports`). ReportService, ReportStorage interface, S3 key generation, and ProgressReport entity are all in place. The only gap is the actual report content — currently a placeholder HTML string.

### Requirements

**FR-9.1**: When a report is generated, the system shall produce an HTML document containing:
- Student name and report date range (teacher-specified start/end dates)
- Class name, subject, and teacher name (report is scoped to a specific class/teacher)
- Overall score summary (average, number of tests, improvement) within the date range
- Score trend table (test name, date, score, max score) for the date range
- Topic performance summary (topic name, average %, trend)
- Teacher feedback summary (most recent 5 feedback entries from the requesting teacher's classes only)

**FR-9.2**: The generated HTML shall be self-contained (inline CSS, no external dependencies) so it renders correctly when opened standalone or served from S3.

**FR-9.3**: The report shall be viewable in-browser via the existing `reportUrl` field returned by the API. No PDF generation required for this sprint.

**FR-9.4**: The report generation endpoint shall accept `startDate` and `endDate` parameters specified by the teacher.

**FR-9.5**: The report is scoped to the requesting teacher's classes — only test scores and feedback from that teacher's classes are included.

### Existing Code to Modify
- `ReportService.generateReport()` — replace stub content with actual data assembly, accept date range params
- `ReportController` — update endpoint to accept startDate/endDate query params
- Pull data from `ProgressService`, `TestScoreService`, `FeedbackService`

### New Code Needed
- `ReportContentBuilder` — service/utility that takes student data and produces HTML string

---

## Feature 2: OCR Test Paper Upload (New)

### Context
Teachers currently enter test scores manually one at a time. This feature lets teachers photograph/scan a test paper, upload it, and have the system extract text via AWS Textract. The extracted text is shown to the teacher for review before they manually map scores — this is NOT auto-grading.

### Requirements

**FR-13.1**: Teachers shall be able to upload one or more images (JPEG, PNG) or a multi-page PDF of a test paper from the test score entry page.

**FR-13.2**: The uploaded file shall be stored in S3 under a structured key: `uploads/{classId}/{studentId}/{timestamp}.{ext}`

**FR-13.3**: The system shall call AWS Textract `DetectDocumentText` (or `AnalyzeDocument`) on the uploaded file and return the extracted text blocks to the frontend.

**FR-13.4**: The extracted text shall be displayed in a read-only panel beside the test score form so the teacher can reference it while filling in scores manually.

**FR-13.5**: The upload is optional — teachers can still enter scores manually without uploading anything.

**FR-13.6**: Uploaded files shall be retained for 90 days then eligible for S3 lifecycle deletion.

**FR-13.7**: Maximum file size: 50 MB (supports multi-page PDF scans of ~20 pages or multiple high-res smartphone photos).

**FR-13.8**: The `test_paper_uploads.test_score_id` foreign key is nullable — teachers can upload a paper before creating the test score entry, then link it later.

### Architecture — Interface Abstractions

Both the file storage and OCR extraction MUST be behind interfaces so implementations can be swapped.

```java
public interface FileStorageService {
    String upload(String key, byte[] content, String contentType);
    String generatePresignedUrl(String bucket, String key, int expiryMinutes);
    void delete(String bucket, String key);
}

public interface OcrService {
    OcrResult extractText(String s3Bucket, String s3Key);
}

public record OcrResult(
    List<OcrTextBlock> textBlocks,
    String rawText,
    String status  // COMPLETED, FAILED
) {}

public record OcrTextBlock(
    String text,
    float confidence,
    String blockType  // LINE, WORD, etc.
) {}
```

### Local Dev Stubs
- `LocalFileStorageService` — writes to configurable path (default `./uploads/`), path set via `application.yml` property. `uploads/` added to `.gitignore`.
- `StubOcrService` — returns sample extracted text for dev/testing.
- Spring profile-based `@ConditionalOnProperty` or `@Profile` selects active implementation.

### Tech Stack
- AWS S3 for file storage (via `FileStorageService` interface — `S3FileStorageService` impl)
- AWS Textract for OCR (via `OcrService` interface — `TextractOcrService` impl)
- Local dev stubs for both

### New DB Schema
```sql
CREATE TABLE test_paper_uploads (
    id UUID PRIMARY KEY,
    test_score_id UUID REFERENCES test_scores(id),  -- NULLABLE
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
- Simple weekly recurrence (e.g., every Monday 4pm-6pm) with end date support
- One-off sessions (e.g., extra class on Saturday)
- Each session has: class, date, start time, end time, location (optional text field)
- Admins have full scheduling permissions (create, update, cancel, deactivate) across all classes

**FR-14.2**: The system shall generate session instances from recurring schedules eagerly on creation only. Teachers/admins manually generate more sessions when needed (no background cron job).

**FR-14.3**: Teachers and admins shall be able to:
- Cancel a specific session (with optional reason)
- Reschedule a session (change date/time)
- Mark attendance for each student (PRESENT, ABSENT, LATE, EXCUSED)
- Deactivate a schedule by setting an `effective_until` date (preserves history, no hard delete)

**FR-14.8**: When creating a new class (existing Sprint 1 flow), the class creation form shall include an optional initial schedule section:
- Teacher/admin can optionally specify a recurring weekly schedule (day of week, start time, end time, location) as part of class creation
- If provided, the schedule and initial sessions are created atomically with the class
- If omitted, the class is created without a schedule (schedule can be added later via FR-14.1)

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

**FR-14.7**: Attendance marking supports both:
- Batch endpoint — single request with list of {studentId, status} pairs (for marking whole class at once)
- Individual update endpoint — single student attendance update

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
    effective_until DATE,         -- NULL = indefinite, set to deactivate
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
- `PUT /api/v1/schedules/{scheduleId}` — update schedule (including setting effective_until to deactivate)
- `POST /api/v1/schedules/{scheduleId}/generate-sessions` — manually generate sessions from schedule
- `GET /api/v1/sessions/upcoming` — get upcoming sessions for current user
- `GET /api/v1/classes/{classId}/sessions` — list sessions for a class
- `PUT /api/v1/sessions/{sessionId}` — update session (reschedule)
- `PUT /api/v1/sessions/{sessionId}/cancel` — cancel session
- `POST /api/v1/sessions/{sessionId}/attendance` — mark attendance (batch: list of {studentId, status})
- `PUT /api/v1/sessions/{sessionId}/attendance/{studentId}` — update individual student attendance
- `PUT /api/v1/sessions/{sessionId}/rsvp` — student RSVP

### Frontend Changes
- New "Schedule" tab/page in teacher class detail view
- Schedule creation form (weekly recurring with end date, or one-off)
- Session list with attendance marking (checkboxes per student)
- "Generate More Sessions" button for recurring schedules
- Student: upcoming sessions list with RSVP toggle
- Parent: child's schedule view (read-only)
- Admin: all sessions overview + full scheduling permissions
- Update existing class creation form (teacher + admin) to include optional initial schedule fields (day of week, start/end time, location)

---

## Units of Work (Per-Feature, Implementation Order)

### Unit 1: Progress Report Content
- **Scope**: Smallest — modifies existing code only, no new tables
- **Backend**: Modify ReportService, ReportController; new ReportContentBuilder
- **Frontend**: Minor — update report generation form to accept date range

### Unit 2: OCR Test Upload
- **Scope**: Moderate — new table, S3 + Textract integration, interface abstractions
- **Backend**: New testpaper module (entity, repo, service, controller), FileStorageService + OcrService interfaces, local dev stubs, S3/Textract implementations
- **Frontend**: Upload component on TestScoreForm, extracted text panel

### Unit 3: Class Scheduling & Attendance
- **Scope**: Largest — 3 new tables, new scheduling module, multiple role views, class creation form update
- **Backend**: New scheduling module (entities, repos, services, controllers), session generation logic, attendance batch/individual endpoints. Modify class creation endpoint to accept optional initial schedule.
- **Frontend**: Schedule creation form, session list, attendance marking, student RSVP, parent schedule view, admin overview + admin scheduling. Update existing CreateClassForm to include optional schedule fields.

---

## Extension Configuration
| Extension | Enabled | Decided At |
|---|---|---|
| Security Baseline | No | Requirements Analysis (Sprint 1) |

---

**Document Version**: 1.0
**Last Updated**: 2026-03-15
**Status**: Draft — Pending Approval
