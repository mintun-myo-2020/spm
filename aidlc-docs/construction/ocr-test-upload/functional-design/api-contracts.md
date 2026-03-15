# Unit 2: OCR Test Upload — API Contracts

## Endpoint 1: Upload Test Paper Files

### `POST /api/v1/test-papers/upload`

Uploads one or more files for a student's test paper. Files are stored and an upload record is created.

**Authorization**: TEACHER (own classes), STUDENT (self only)

**Request**: `multipart/form-data`

| Field | Type | Required | Description |
|---|---|---|---|
| files | File[] | Yes | One or more files (JPEG, PNG, PDF). Max 50 MB each. |
| studentId | UUID | Yes | Student whose paper this is |
| classId | UUID | Yes | Class context |

**Response**: `201 Created`
```json
{
  "status": "success",
  "data": {
    "uploadId": "uuid",
    "status": "UPLOADED",
    "studentId": "uuid",
    "classId": "uuid",
    "pages": [
      {
        "pageId": "uuid",
        "pageNumber": 1,
        "fileName": "page1.jpg",
        "contentType": "image/jpeg",
        "fileSizeBytes": 2048000,
        "status": "PENDING"
      }
    ],
    "createdAt": "2026-03-15T10:00:00Z"
  }
}
```

**Error Responses**:
| Status | Code | Condition |
|---|---|---|
| 400 | INVALID_FILE_TYPE | Unsupported MIME type |
| 400 | FILE_TOO_LARGE | File exceeds 50 MB |
| 400 | INVALID_FILE_CONTENT | File is corrupt or not a valid image/PDF |
| 403 | FORBIDDEN | User not authorized for this class/student |

---

## Endpoint 2: Trigger OCR Extraction

### `POST /api/v1/test-papers/{uploadId}/extract`

Triggers OCR extraction on all pages of the upload. Returns immediately with PROCESSING status. Frontend polls GET endpoint for results.

**Authorization**: TEACHER (own classes), STUDENT (own uploads)

**Path Parameters**:
| Param | Type | Description |
|---|---|---|
| uploadId | UUID | The upload to process |

**Request Body**: None

**Response**: `202 Accepted`
```json
{
  "status": "success",
  "data": {
    "uploadId": "uuid",
    "status": "PROCESSING",
    "message": "OCR extraction started. Poll GET /api/v1/test-papers/{uploadId} for results."
  }
}
```

**Note**: The server processes OCR synchronously in a background thread. The 202 response is returned before processing completes. For the MVP, this uses `@Async` on the service method. The frontend polls for completion.

**Error Responses**:
| Status | Code | Condition |
|---|---|---|
| 404 | NOT_FOUND | Upload not found |
| 409 | UPLOAD_ALREADY_PROCESSING | Upload is currently being processed |
| 409 | UPLOAD_ALREADY_PROCESSED | Upload has already been processed |
| 403 | FORBIDDEN | User not authorized |

---

## Endpoint 3: Get Upload Status and Results

### `GET /api/v1/test-papers/{uploadId}`

Returns upload metadata, page details with presigned URLs, extracted text, and parsed results.

**Authorization**: TEACHER (own classes), ADMIN (any), STUDENT (own uploads)

**Path Parameters**:
| Param | Type | Description |
|---|---|---|
| uploadId | UUID | The upload to retrieve |

**Response**: `200 OK`
```json
{
  "status": "success",
  "data": {
    "uploadId": "uuid",
    "testScoreId": null,
    "studentId": "uuid",
    "classId": "uuid",
    "status": "COMPLETED",
    "pages": [
      {
        "pageId": "uuid",
        "pageNumber": 1,
        "fileName": "page1.jpg",
        "contentType": "image/jpeg",
        "fileSizeBytes": 2048000,
        "status": "COMPLETED",
        "fileUrl": "https://s3.../presigned-url or /api/v1/test-papers/files?key=...",
        "extractedText": "Question 1: What is...\n(a) ...\n(b) ...",
        "ocrConfidence": 0.92,
        "parsedResult": {
          "questions": [
            {
              "questionNumber": "1",
              "questionText": "What is the capital of France?",
              "questionType": "MCQ",
              "mcqOptions": [
                { "key": "A", "text": "London" },
                { "key": "B", "text": "Paris" },
                { "key": "C", "text": "Berlin" },
                { "key": "D", "text": "Madrid" }
              ],
              "maxScore": 2.0,
              "subQuestions": [],
              "confidence": 0.88,
              "rawTextSpan": "Question 1: What is the capital..."
            },
            {
              "questionNumber": "2",
              "questionText": "Explain the water cycle.",
              "questionType": "OPEN",
              "mcqOptions": [],
              "maxScore": 10.0,
              "subQuestions": [
                {
                  "label": "a",
                  "questionText": "Describe evaporation",
                  "maxScore": 5.0,
                  "studentAnswer": "Water heats up and...",
                  "confidence": 0.72
                },
                {
                  "label": "b",
                  "questionText": "Describe condensation",
                  "maxScore": 5.0,
                  "studentAnswer": null,
                  "confidence": 0.65
                }
              ],
              "confidence": 0.70,
              "rawTextSpan": "Question 2: Explain the water cycle..."
            }
          ],
          "totalDetectedMarks": 12.0,
          "parsingNotes": [
            "Sub-question 2b: student answer not detected (low confidence handwriting)"
          ]
        }
      }
    ],
    "aggregatedQuestions": [
      {
        "questionNumber": "1",
        "questionText": "What is the capital of France?",
        "questionType": "MCQ",
        "mcqOptions": [
          { "key": "A", "text": "London" },
          { "key": "B", "text": "Paris" },
          { "key": "C", "text": "Berlin" },
          { "key": "D", "text": "Madrid" }
        ],
        "maxScore": 2.0,
        "subQuestions": [],
        "confidence": 0.88,
        "sourcePage": 1
      },
      {
        "questionNumber": "2",
        "questionText": "Explain the water cycle.",
        "questionType": "OPEN",
        "mcqOptions": [],
        "maxScore": 10.0,
        "subQuestions": [
          {
            "label": "a",
            "questionText": "Describe evaporation",
            "maxScore": 5.0,
            "studentAnswer": "Water heats up and...",
            "confidence": 0.72
          },
          {
            "label": "b",
            "questionText": "Describe condensation",
            "maxScore": 5.0,
            "studentAnswer": null,
            "confidence": 0.65
          }
        ],
        "confidence": 0.70,
        "sourcePage": 1
      }
    ],
    "createdAt": "2026-03-15T10:00:00Z"
  }
}
```

**Key Design Notes**:
- `aggregatedQuestions` is a convenience field — flattened list of all parsed questions across all pages, in page order
- `fileUrl` is generated on-the-fly (presigned URL, 15-min expiry for S3; API path for local dev)
- `parsedResult` is null for pages with status PENDING or FAILED

**Error Responses**:
| Status | Code | Condition |
|---|---|---|
| 404 | NOT_FOUND | Upload not found |
| 403 | FORBIDDEN | User not authorized |

---

## Endpoint 4: Serve Local File (Dev Only)

### `GET /api/v1/test-papers/files`

Serves uploaded files from local filesystem. Only active when `app.storage.type=local`.

**Authorization**: TEACHER (own classes), ADMIN (any), STUDENT (own uploads)

**Query Parameters**:
| Param | Type | Description |
|---|---|---|
| key | String | Storage key (path) of the file |

**Response**: `200 OK` with file content and appropriate `Content-Type` header

**Error Responses**:
| Status | Code | Condition |
|---|---|---|
| 404 | NOT_FOUND | File not found at key |
| 403 | FORBIDDEN | User not authorized |

---

## Modified Endpoint: Create Test Score (Existing)

### `POST /api/v1/test-scores` — Updated

**Change**: Accept optional `uploadIds` field to link uploads to the new test score.

**Updated Request Body** (additions only):
```json
{
  "studentId": "uuid",
  "classId": "uuid",
  "testName": "...",
  "testDate": "2026-03-15",
  "overallScore": 85.0,
  "maxScore": 100.0,
  "uploadIds": ["uuid1", "uuid2"],
  "questions": [...]
}
```

| New Field | Type | Required | Description |
|---|---|---|---|
| uploadIds | List of UUID | No | Upload IDs to link to this test score |

**Behavior**:
- If `uploadIds` provided, set `testScoreId` on each referenced `TestPaperUpload`
- Validate that each upload exists and belongs to the same student/class
- If validation fails, reject the entire request (don't create test score)

---

## Modified Endpoint: Update Test Score (Existing)

### `PUT /api/v1/test-scores/{testScoreId}` — Updated

**Change**: When updating a draft test score (`isDraft = true`), setting `isDraft = false` triggers full validation.

**Updated Request Body** (additions only):
```json
{
  "isDraft": false,
  "...existing fields..."
}
```

| New Field | Type | Required | Description |
|---|---|---|---|
| isDraft | boolean | No | Set to false to approve a draft. Defaults to current value. |

**Behavior**:
- If `isDraft` transitions from `true` to `false`, enforce full validation (all topicIds required, overallScore > 0)
- If `isDraft` remains `true`, relaxed validation applies

---

## DTO Summary

### TestPaperUploadDTO (Response)
```
uploadId: UUID
testScoreId: UUID (nullable)
studentId: UUID
classId: UUID
status: String (UPLOADED | PROCESSING | COMPLETED | PARTIALLY_COMPLETED | FAILED)
pages: List<TestPaperPageDTO>
aggregatedQuestions: List<ParsedQuestionDTO>
createdAt: Instant
```

### TestPaperPageDTO (Response)
```
pageId: UUID
pageNumber: int
fileName: String
contentType: String
fileSizeBytes: long
status: String (PENDING | PROCESSING | COMPLETED | FAILED)
fileUrl: String (presigned URL or API path)
extractedText: String (nullable)
ocrConfidence: Float (nullable)
parsedResult: ParsedResultDTO (nullable)
```

### ParsedResultDTO (Response)
```
questions: List<ParsedQuestionDTO>
totalDetectedMarks: BigDecimal (nullable)
parsingNotes: List<String>
```

### ParsedQuestionDTO (Response)
```
questionNumber: String
questionText: String
questionType: String (OPEN | MCQ)
mcqOptions: List<McqOptionDTO>
maxScore: BigDecimal (nullable)
subQuestions: List<ParsedSubQuestionDTO>
confidence: float
rawTextSpan: String
sourcePage: int (only in aggregatedQuestions)
```

### ParsedSubQuestionDTO (Response)
```
label: String
questionText: String
maxScore: BigDecimal (nullable)
studentAnswer: String (nullable)
confidence: float
```

### CreateTestScoreRequestDTO (Updated)
```
...existing fields...
uploadIds: List<UUID> (optional, new)
isDraft: Boolean (optional, new, default false)
```

---

**Document Version**: 1.0
**Last Updated**: 2026-03-15
