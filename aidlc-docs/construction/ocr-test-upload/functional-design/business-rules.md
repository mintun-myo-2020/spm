# Unit 2: OCR Test Upload — Business Rules

## BR-1: File Upload Validation

### BR-1.1: Allowed File Types
- Accepted MIME types: `image/jpeg`, `image/png`, `application/pdf`
- Validation: check both file extension AND magic bytes (content sniffing)
- Reject with `INVALID_FILE_TYPE` if mismatch or unsupported type

### BR-1.2: File Size Limit
- Maximum: 50 MB per file (52,428,800 bytes)
- Reject with `FILE_TOO_LARGE` if exceeded
- No aggregate limit across files in one upload (practical limit ~5-10 files)

### BR-1.3: File Content Validation
- Verify file is a valid image or PDF (not just correct extension)
- For images: validate image headers (JPEG SOI marker, PNG signature)
- For PDF: validate PDF header (`%PDF-`)
- Reject with `INVALID_FILE_CONTENT` if file is corrupt or not a valid image/PDF

### BR-1.4: All-or-Nothing Upload
- If any file in a multi-file upload fails validation, reject the entire upload
- No partial uploads — either all files are accepted or none are
- Clean up any files already stored if a later file fails

---

## BR-2: Access Control

### BR-2.1: Upload Permissions
| Role | Can Upload | Scope |
|---|---|---|
| TEACHER | Yes | For students in their own classes only |
| ADMIN | No | View only (per C3 clarification — admins can view but not upload) |
| STUDENT | Yes | For themselves only, in classes they are enrolled in |
| PARENT | No | No upload access |

### BR-2.2: View Permissions
| Role | Can View | Scope |
|---|---|---|
| TEACHER | Yes | Uploads for students in their own classes |
| ADMIN | Yes | All uploads across all classes |
| STUDENT | Yes | Their own uploads only |
| PARENT | No | No direct upload access (view test scores via existing endpoints) |

### BR-2.3: Extract Permissions
- Same as upload permissions — only the uploader or a teacher of the class can trigger extraction
- Admins can view results but cannot trigger extraction

### BR-2.4: Student Self-Upload Authorization
- Student must be enrolled in the specified class (ACTIVE enrollment status)
- Student can only upload for themselves (`studentId` must match their own student record)
- Reject with `FORBIDDEN` if student tries to upload for another student

---

## BR-3: Upload Status Transitions

### BR-3.1: Valid Transitions
| From | To | Trigger |
|---|---|---|
| UPLOADED | PROCESSING | Extract endpoint called |
| PROCESSING | COMPLETED | All pages OCR'd successfully |
| PROCESSING | PARTIALLY_COMPLETED | Some pages succeeded, some failed |
| PROCESSING | FAILED | All pages failed |

### BR-3.2: Invalid Transitions
- Cannot extract an upload that is already PROCESSING (reject with `UPLOAD_ALREADY_PROCESSING`)
- Cannot extract an upload that is already COMPLETED/PARTIALLY_COMPLETED/FAILED (reject with `UPLOAD_ALREADY_PROCESSED`)
- Re-extraction requires a new upload

### BR-3.3: Page Status Transitions
| From | To | Trigger |
|---|---|---|
| PENDING | PROCESSING | OCR started for this page |
| PROCESSING | COMPLETED | OCR + parsing succeeded |
| PROCESSING | FAILED | OCR failed for this page |

---

## BR-4: OCR and Parsing Rules

### BR-4.1: OCR Processing Order
- Pages are processed in page_number order (1, 2, 3, ...)
- Processing is sequential per upload (not parallel) to maintain ordering
- Each page is independently OCR'd — failure of one page does not stop others

### BR-4.2: Parsing Confidence Thresholds
| Confidence Range | Treatment |
|---|---|
| >= 0.8 | High confidence — auto-populate field normally |
| 0.5 - 0.79 | Medium confidence — auto-populate but flag for review (yellow highlight) |
| < 0.5 | Low confidence — include in raw text panel, do NOT auto-populate into form field |

### BR-4.3: Parsing Fallback
- If structured parsing fails entirely (no questions detected), the upload still succeeds
- Raw OCR text is always stored regardless of parsing success
- Frontend shows raw text panel as fallback when no structured data is available

### BR-4.4: Question Aggregation Across Pages
- Questions from all pages are collected in page order
- No deduplication — if Q1 appears on page 1 and page 2, both entries are included
- Teacher resolves any duplicates during review

---

## BR-5: Test Score Linking

### BR-5.1: Linking Mechanism
- Upload is created without a `testScoreId` (always null on creation)
- When teacher creates/saves a test score, frontend includes `uploadId` in the request
- Backend sets `TestPaperUpload.testScoreId` to the newly created test score ID
- This is a one-time link — once linked, the upload stays linked

### BR-5.2: Student Draft Creation
- When a student uploads, the system automatically creates a draft TestScore:
  - `isDraft = true` (new field on TestScore entity)
  - `overallScore = 0`
  - `maxScore = 100` (default)
  - Questions/sub-questions populated from parsed results
  - All `score` fields = 0 (teacher must grade)
  - All `topicId` fields = null (teacher must assign — validation relaxed for drafts)
  - `testName` = "Draft - {original file name}" 
  - `testDate` = upload date
  - `createdBy` = student's user
- The upload is immediately linked to this draft test score
- Draft test scores are excluded from progress calculations and reports

### BR-5.3: Draft Approval
- Teacher opens draft, reviews/corrects data, fills in scores and topics
- Teacher saves → `isDraft` set to `false`
- Normal validation applies on save (all required fields must be filled)
- Once approved, the test score behaves like any other test score

---

## BR-6: File Storage Rules

### BR-6.1: Storage Key Pattern
`uploads/{classId}/{studentId}/{uploadId}/{pageNumber}.{ext}`
- Ensures unique keys per upload
- Organized by class and student for easy S3 lifecycle management

### BR-6.2: File Retention
- Files retained for 90 days from upload date
- S3 lifecycle policy handles deletion (not application code)
- Extracted text and parsed results persist in DB beyond file deletion

### BR-6.3: Presigned URL Expiry
- Presigned URLs valid for 15 minutes
- Frontend re-fetches if URL expires (re-call GET endpoint)
- Local dev: URLs are API-relative paths (no expiry concept, served by controller)

---

## BR-7: Stub/Local Dev Behavior

### BR-7.1: LocalFileStorageService
- Writes files to configurable path (default `./uploads/`)
- `uploads/` directory added to `.gitignore`
- Path configured via `app.file-storage.local-path` in `application.yml`
- `generatePresignedUrl()` returns API-relative path: `/api/v1/test-papers/files?key={key}`

### BR-7.2: StubOcrService
- Returns sample extracted text for any input
- Sample text includes question patterns so parsing can be tested
- Returns confidence of 0.95 for all blocks

### BR-7.3: BasicTestPaperParser
- Always active (not a stub — this is the real implementation for this sprint)
- Uses regex patterns to detect questions, sub-questions, marks, MCQ options
- Future LLM implementation will implement the same `TestPaperParser` interface

### BR-7.4: Profile-Based Selection
- `@ConditionalOnProperty("app.storage.type")` with values `local` / `s3`
- `@ConditionalOnProperty("app.ocr.type")` with values `stub` / `textract`
- Default profile uses local storage + stub OCR
- Production profile uses S3 + Textract

---

## BR-8: Error Handling

### BR-8.1: Upload Errors
| Error | Code | HTTP Status |
|---|---|---|
| Invalid file type | INVALID_FILE_TYPE | 400 |
| File too large | FILE_TOO_LARGE | 400 |
| Invalid file content | INVALID_FILE_CONTENT | 400 |
| Storage failure | STORAGE_ERROR | 500 |
| Unauthorized | FORBIDDEN | 403 |

### BR-8.2: Extraction Errors
| Error | Code | HTTP Status |
|---|---|---|
| Upload not found | NOT_FOUND | 404 |
| Already processing | UPLOAD_ALREADY_PROCESSING | 409 |
| Already processed | UPLOAD_ALREADY_PROCESSED | 409 |
| OCR service failure | OCR_ERROR | 500 |

### BR-8.3: Graceful Degradation
- If OCR fails for a page, the page is marked FAILED but other pages continue
- If parsing fails, raw text is still stored — teacher can reference it manually
- If all pages fail, upload status is FAILED but the files remain stored (teacher can retry or reference manually)

---

## BR-9: TestScore Entity Changes

### BR-9.1: New Field — isDraft
- `isDraft` boolean, default `false`, added to `TestScore` entity
- Draft test scores created by student self-upload have `isDraft = true`
- Drafts excluded from: progress calculations, report generation, trend data
- Existing queries for progress/reports must filter `WHERE is_draft = false`

### BR-9.2: Validation Relaxation for Drafts
- When `isDraft = true`, `topicId` on sub-questions can be null
- When `isDraft = true`, `overallScore` can be 0
- Normal validation enforced when `isDraft` is set to `false` (on teacher approval)

---

**Document Version**: 1.0
**Last Updated**: 2026-03-15
