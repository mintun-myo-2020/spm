# Sprint 2 — Requirement Verification Questions

Please answer the following questions to clarify a few details before we proceed with design and implementation.

---

## Feature 1: Progress Report Content

### Question 1
The report includes "most recent 5 feedback entries." Should the report also include feedback from other teachers if the student is in multiple classes, or only feedback from the teacher who triggered the report?

A) All feedback across all classes within the date range (any teacher)
B) Only feedback from the requesting teacher's classes
C) Other (please describe after [Answer]: tag below)

[Answer]: B. Make it clear that it's a report for that class and subject by that teacher

### Question 2
The report date range — how should it be determined?

A) Teacher specifies start and end dates when generating the report
B) Fixed window (e.g., last 30 days, last term)
C) Since the last report was generated for this student
D) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Feature 2: OCR Test Paper Upload

### Question 3
For the local dev stub (`LocalFileStorageService`), where should files be stored?

A) A `uploads/` directory under the project root (e.g., `spm/uploads/`)
B) System temp directory (`java.io.tmpdir`)
C) A configurable path via application properties
D) Other (please describe after [Answer]: tag below)

[Answer]: D - i don't know, what are the differences and how to choose?

### Question 4
Should the `test_paper_uploads.test_score_id` foreign key be nullable? (i.e., can a teacher upload a paper before creating the test score entry, then link it later?)

A) Yes, nullable — upload first, link to test score later
B) No, not nullable — test score must exist before uploading
C) Other (please describe after [Answer]: tag below)

[Answer]: A. 

### Question 5
File size limit for uploads?

A) 5 MB
B) 10 MB
C) 20 MB
D) Other (please describe after [Answer]: tag below)

[Answer]: what makes sense for ~20 pages of regular photos? PROBABLY taken by smartphone 

---

## Feature 3: Class Scheduling & Attendance

### Question 6
For auto-generating sessions from recurring schedules (4 weeks ahead), when should this generation happen?

A) Eagerly on schedule creation + a scheduled job (e.g., daily cron) to maintain the 4-week window
B) Lazily — generate sessions on-demand when someone queries upcoming sessions
C) Eagerly on creation only, teacher manually generates more when needed
D) Other (please describe after [Answer]: tag below)

[Answer]: C. SHOULD be able to create lessons on a day and repeatable across different patterns like x times a week, weekly, monthly etc for how long (until what end date)

### Question 7
Can a teacher or admin delete a schedule entirely (not just cancel individual sessions)?

A) Yes — soft delete (mark inactive), existing sessions remain
B) Yes — hard delete, also cancel all future sessions
C) No — schedules can only be deactivated by setting an `effective_until` date
D) Other (please describe after [Answer]: tag below)

[Answer]: B. if the guy wants to cancel all future sessions means the student is no longer in the class right?

### Question 8
For the attendance marking endpoint (`POST /api/v1/sessions/{sessionId}/attendance`), should it accept a batch of all students at once, or one student at a time?

A) Batch — single request with list of {studentId, status} pairs
B) One at a time — separate request per student
C) Both — batch endpoint plus individual update endpoint
D) Other (please describe after [Answer]: tag below)

[Answer]: C. 

---

## Cross-Cutting

### Question 9
Should Sprint 2 features follow the same unit structure (Unit 1 = Backend, Unit 2 = Frontend), or would you prefer a different breakdown (e.g., per-feature units)?

A) Same as Sprint 1 — Unit 1 (Backend) then Unit 2 (Frontend)
B) Per-feature units — each feature is its own unit (backend + frontend together)
C) Other (please describe after [Answer]: tag below)

[Answer]: B

### Question 10
For the backend, should we continue with the same agent split (separate agent for backend, you handle frontend), or should this agent handle both?

A) Same split — I have another agent for backend, you do frontend
B) This agent handles both backend and frontend
C) This agent handles backend only, another handles frontend
D) Other (please describe after [Answer]: tag below)

[Answer]: ignore this question, we will do per-feature units.

