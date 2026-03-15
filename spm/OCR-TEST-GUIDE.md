# OCR Test Paper Upload — Local Testing Guide

## Prerequisites

Make sure Docker containers are running:

```bash
cd spm
docker-compose up -d postgres keycloak-postgres keycloak tesseract
```

Then start the Spring Boot app:

```bash
./gradlew bootRun
```

Wait for Keycloak to be healthy (can take ~60s on first start).

---

## Test Data (from V2 seed migration)

### Teacher: Ms. Lim (Math)

| Field | Value |
|---|---|
| Keycloak username | `teacher1` |
| Keycloak password | `teacher1` |
| User ID | `10000000-0000-0000-0000-000000000002` |
| Teacher ID | `30000000-0000-0000-0000-000000000001` |

### Math Class

| Field | Value |
|---|---|
| Class ID | `60000000-0000-0000-0000-000000000001` |
| Class Name | Math 10A |
| Subject | Mathematics |

### Students in Math Class (pick any)

| Name | Student ID |
|---|---|
| GoodStudent Alice | `50000000-0000-0000-0000-000000000001` |
| GoodStudent Ben | `50000000-0000-0000-0000-000000000002` |
| StrugglingStudent Ethan | `50000000-0000-0000-0000-000000000005` |

---

## Step 1: Get a JWT Token

```bash
TOKEN=$(curl -s -X POST \
  "http://localhost:8180/realms/spm/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=spm-frontend&username=teacher1&password=teacher1" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['access_token'])")

echo $TOKEN
```

---

## Step 2: Create a Test Image

Option A — If you have ImageMagick (`brew install imagemagick`):

```bash
convert -size 800x600 xc:white \
  -font Helvetica -pointsize 24 \
  -draw "text 50,50 'Question 1 [10 marks]'" \
  -draw "text 50,100 'What is the capital of France?'" \
  -draw "text 50,160 '(a) Explain your reasoning [5 marks]'" \
  -draw "text 50,220 '(b) Give an example [5 marks]'" \
  -draw "text 50,300 'Question 2 [20 marks]'" \
  -draw "text 50,360 'Solve the following equation'" \
  -draw "text 50,420 '(a) Find x [10 marks]'" \
  -draw "text 50,480 '(b) Verify your answer [10 marks]'" \
  /tmp/test-paper-page1.png
```

Option B — Just use any JPEG or PNG with text. A screenshot of a document, a photo of a real exam paper, anything works.

---

## Step 3: Upload Files

Upload for Alice in Math 10A:

```bash
curl -v -X POST "http://localhost:8080/api/v1/test-papers/upload" \
  -H "Authorization: Bearer $TOKEN" \
  -F "files=@/tmp/test-paper-page1.png" \
  -F "studentId=50000000-0000-0000-0000-000000000001" \
  -F "classId=60000000-0000-0000-0000-000000000001"
```

Multi-page upload (if you have multiple images):

```bash
curl -v -X POST "http://localhost:8080/api/v1/test-papers/upload" \
  -H "Authorization: Bearer $TOKEN" \
  -F "files=@/tmp/test-paper-page1.png" \
  -F "files=@/tmp/test-paper-page2.png" \
  -F "studentId=50000000-0000-0000-0000-000000000001" \
  -F "classId=60000000-0000-0000-0000-000000000001"
```

Save the `uploadId` from the response.

---

## Step 4: Trigger OCR Extraction

Replace `<UPLOAD_ID>` with the UUID from Step 3:

```bash
curl -v -X POST "http://localhost:8080/api/v1/test-papers/<UPLOAD_ID>/extract" \
  -H "Authorization: Bearer $TOKEN"
```

Returns HTTP 202. Extraction runs async in the background.

---

## Step 5: Poll for Results

```bash
curl -s "http://localhost:8080/api/v1/test-papers/<UPLOAD_ID>" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

Keep polling until `status` changes from `PROCESSING` to `COMPLETED`.

### What to look for in the response:

- `status` — should go `UPLOADED` → `PROCESSING` → `COMPLETED`
- `pages[].extractedText` — raw OCR text from Tesseract
- `pages[].ocrConfidence` — confidence score (0.85 default for Tesseract CLI)
- `pages[].parsedResult` — structured questions/sub-questions parsed by BasicTestPaperParser
- `aggregatedQuestions` — flattened questions across all pages

---

## Step 6: View Uploaded File (dev only)

The local file serving endpoint lets you view the uploaded image:

```bash
# Get the s3Key from the upload response pages[].fileUrl
curl -s "http://localhost:8080/api/v1/test-papers/files?key=uploads/<UPLOAD_ID>/page-1/test-paper-page1.png" \
  -H "Authorization: Bearer $TOKEN" \
  --output /tmp/downloaded.png

open /tmp/downloaded.png
```

---

## Troubleshooting

### Tesseract container not running

```bash
docker ps | grep spm-tesseract
# If not running:
docker-compose up -d tesseract
```

### OCR returns empty text

- Make sure the image has clear, readable text
- Check the tesseract container can see the file:
  ```bash
  docker exec spm-tesseract ls /data/uploads/
  ```
- The `spm/uploads/` directory on your host maps to `/data/` in the container

### Token expired

Tokens expire after 5 minutes. Re-run the Step 1 curl to get a fresh one.

### File type rejected

Only `image/jpeg` and `image/png` are accepted. PDFs are a known limitation for this sprint.
