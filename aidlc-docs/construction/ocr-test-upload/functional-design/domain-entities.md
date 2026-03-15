# Unit 2: OCR Test Upload — Domain Entities

## Entity: TestPaperUpload

Represents a single upload session containing one or more file pages of a student's test paper.

| Field | Type | Constraints | Description |
|---|---|---|---|
| id | UUID | PK, auto-generated | Unique upload identifier |
| testScoreId | UUID | FK → test_scores(id), NULLABLE | Linked test score (null until linked) |
| studentId | UUID | FK → students(id), NOT NULL | Student whose paper this is |
| classId | UUID | FK → classes(id), NOT NULL | Class context for the upload |
| status | UploadStatus | NOT NULL, default UPLOADED | Current processing status |
| uploadedBy | UUID | FK → users(id), NOT NULL | User who performed the upload |
| createdAt | Instant | NOT NULL, auto-set | Upload timestamp |

### Relationship to TestScore
- Many-to-one: multiple uploads can link to the same test score (e.g., re-upload after correction)
- Nullable FK: upload can exist before a test score is created
- Linking happens when test score is created — frontend sends uploadId(s) with the create request

### Relationship to TestPaperPage
- One-to-many: one upload contains one or more pages (files)

---

## Entity: TestPaperPage

Represents a single file/page within an upload. Each page is stored separately and OCR'd independently.

| Field | Type | Constraints | Description |
|---|---|---|---|
| id | UUID | PK, auto-generated | Unique page identifier |
| uploadId | UUID | FK → test_paper_uploads(id), NOT NULL | Parent upload |
| pageNumber | int | NOT NULL | Ordering within the upload (1-based) |
| s3Bucket | String | NOT NULL | Storage bucket name |
| s3Key | String(500) | NOT NULL | Storage object key |
| fileName | String(255) | NOT NULL | Original file name |
| contentType | String(100) | NOT NULL | MIME type (image/jpeg, image/png, application/pdf) |
| fileSizeBytes | long | NOT NULL | File size in bytes |
| extractedText | String (TEXT) | NULLABLE | Raw OCR text for this page |
| parsedResult | String (TEXT/JSON) | NULLABLE | Structured parsing result (JSON) |
| ocrConfidence | float | NULLABLE | Average OCR confidence score (0.0-1.0) |
| status | PageStatus | NOT NULL, default PENDING | OCR processing status for this page |
| createdAt | Instant | NOT NULL, auto-set | Page creation timestamp |

### Storage Key Pattern
`uploads/{classId}/{studentId}/{uploadId}/{pageNumber}.{ext}`

---

## Enum: UploadStatus

Tracks the overall upload lifecycle.

| Value | Description |
|---|---|
| UPLOADED | Files received and stored, OCR not yet started |
| PROCESSING | OCR extraction in progress (at least one page processing) |
| COMPLETED | All pages processed successfully |
| PARTIALLY_COMPLETED | Some pages succeeded, some failed |
| FAILED | All pages failed OCR extraction |

### State Transitions
```
UPLOADED → PROCESSING (when extract is triggered)
PROCESSING → COMPLETED (all pages succeed)
PROCESSING → PARTIALLY_COMPLETED (some pages fail)
PROCESSING → FAILED (all pages fail)
```

---

## Enum: PageStatus

Tracks OCR processing status per page.

| Value | Description |
|---|---|
| PENDING | Stored but not yet processed |
| PROCESSING | OCR in progress |
| COMPLETED | OCR extraction successful |
| FAILED | OCR extraction failed |

---

## Value Object: ParsedQuestion

Represents a question parsed from OCR text. Stored as JSON in `TestPaperPage.parsedResult` within a `ParsedResult` wrapper.

| Field | Type | Description |
|---|---|---|
| questionNumber | String | Detected question number (e.g., "1", "2a") |
| questionText | String | Extracted question text (may be partial) |
| questionType | String | "OPEN" or "MCQ" (detected from options presence) |
| mcqOptions | List of McqOption | Detected MCQ options (if any) |
| maxScore | BigDecimal | Detected max marks (nullable if not parseable) |
| subQuestions | List of ParsedSubQuestion | Detected sub-questions |
| confidence | float | Parsing confidence for this question (0.0-1.0) |
| rawTextSpan | String | The raw text segment this was parsed from |

## Value Object: ParsedSubQuestion

| Field | Type | Description |
|---|---|---|
| label | String | Sub-question label (e.g., "a", "b", "i", "ii") |
| questionText | String | Sub-question text |
| maxScore | BigDecimal | Detected max marks (nullable) |
| studentAnswer | String | Detected student answer (nullable) |
| confidence | float | Parsing confidence |

## Value Object: McqOption

| Field | Type | Description |
|---|---|---|
| key | String | Option key (A, B, C, D) |
| text | String | Option text |

## Value Object: ParsedResult

Top-level wrapper stored as JSON in `TestPaperPage.parsedResult`.

| Field | Type | Description |
|---|---|---|
| questions | List of ParsedQuestion | All parsed questions from this page |
| totalDetectedMarks | BigDecimal | Sum of detected max marks (nullable) |
| parsingNotes | List of String | Warnings/notes about parsing quality |

---

## Interface: FileStorageService

Abstraction for file storage operations. Implementations: `LocalFileStorageService` (dev), `S3FileStorageService` (prod).

```java
public interface FileStorageService {
    String upload(String key, byte[] content, String contentType);
    String generatePresignedUrl(String key, int expiryMinutes);
    void delete(String key);
}
```

### Method Contracts
- `upload`: Stores file at the given key, returns the key. Throws on failure.
- `generatePresignedUrl`: Returns a URL valid for `expiryMinutes`. Local impl returns API-relative path. S3 impl returns presigned URL.
- `delete`: Removes file at key. Idempotent (no error if not found).

---

## Interface: OcrService

Abstraction for OCR text extraction. Implementations: `StubOcrService` (dev), `TextractOcrService` (prod).

```java
public interface OcrService {
    OcrResult extractText(String bucket, String key);
}
```

---

## Record: OcrResult

| Field | Type | Description |
|---|---|---|
| textBlocks | List of OcrTextBlock | Individual text blocks with positions |
| rawText | String | Concatenated full text |
| status | String | "COMPLETED" or "FAILED" |
| confidence | float | Average confidence across all blocks |

## Record: OcrTextBlock

| Field | Type | Description |
|---|---|---|
| text | String | Text content |
| confidence | float | Confidence score (0.0-1.0) |
| blockType | String | LINE, WORD, etc. |

---

## Interface: TestPaperParser

Abstraction for structured parsing of OCR text into questions/sub-questions. This is the key abstraction for future LLM expansion.

Implementations: `BasicTestPaperParser` (regex-based, this sprint), future `LlmTestPaperParser`.

```java
public interface TestPaperParser {
    ParsedResult parse(String rawOcrText, float ocrConfidence);
}
```

### Design Rationale
- Separates OCR (text extraction) from parsing (text understanding)
- `BasicTestPaperParser` uses regex/heuristics to detect question numbers, marks, MCQ patterns
- Future `LlmTestPaperParser` can use an LLM API for more accurate structured extraction
- Parser is stateless — takes raw text, returns structured result
- Confidence scores propagate from OCR through parsing to help the teacher identify low-confidence fields

---

## DB Schema (New Tables)

```sql
CREATE TABLE test_paper_uploads (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    test_score_id UUID REFERENCES test_scores(id),
    student_id UUID NOT NULL REFERENCES students(id),
    class_id UUID NOT NULL REFERENCES classes(id),
    status VARCHAR(25) NOT NULL DEFAULT 'UPLOADED',
    uploaded_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE test_paper_pages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    upload_id UUID NOT NULL REFERENCES test_paper_uploads(id) ON DELETE CASCADE,
    page_number INT NOT NULL,
    s3_bucket VARCHAR(255) NOT NULL,
    s3_key VARCHAR(500) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    extracted_text TEXT,
    parsed_result TEXT,
    ocr_confidence REAL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (upload_id, page_number)
);

CREATE INDEX idx_test_paper_uploads_student ON test_paper_uploads(student_id);
CREATE INDEX idx_test_paper_uploads_class ON test_paper_uploads(class_id);
CREATE INDEX idx_test_paper_uploads_test_score ON test_paper_uploads(test_score_id);
CREATE INDEX idx_test_paper_pages_upload ON test_paper_pages(upload_id);
```

---

**Document Version**: 1.0
**Last Updated**: 2026-03-15
