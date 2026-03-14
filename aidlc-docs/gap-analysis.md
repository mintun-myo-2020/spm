# Gap Analysis: Requirements vs Implementation

**Date**: 2026-03-14
**Source**: `aidlc-docs/inception/requirements/requirements.md`

---

## Implemented and Working

| Requirement | Description | Status |
|---|---|---|
| FR-1.1 | Keycloak authentication integration | ✅ Done |
| FR-1.2 / FR-1.3 | Four user roles with RBAC | ✅ Done |
| FR-3 | Student profile management | ✅ Done |
| FR-4.1 | Default subjects and topics | ✅ Done |
| FR-4.3 (partial) | Teacher create classes (for themselves) | ✅ Done |
| FR-4.3 (partial) | Admin create classes | ✅ Done |
| FR-5 | Test score recording with topic breakdown | ✅ Done |
| FR-6 | Performance tracking (overall + topic-level) | ✅ Done |
| FR-8 | Progress charts (teacher, parent, student views) | ✅ Done |
| FR-10.2 | Parent historical data and trends | ✅ Done |

---

## Gaps Fixed (2026-03-14)

### GAP-2: Admin Edit/Update Subjects and Topics (FR-4.2) — ✅ FIXED

- Backend: Added `PUT /subjects/{subjectId}` and `PUT /subjects/{subjectId}/topics/{topicId}` endpoints
- Backend: Added `UpdateSubjectRequestDTO`, `UpdateTopicRequestDTO`, service methods
- Frontend: `SubjectManagement` now has Edit buttons for subjects, clickable topic badges to edit, Add Topic button per subject

### GAP-3: Teacher Enroll Students in Their Classes (FR-4.3) — ✅ FIXED

- Backend: `POST /classes/{classId}/students` and `PUT /classes/{classId}/students/{studentId}/withdraw` now allow `TEACHER` role (scoped to own classes via `verifyTeacherOwnsClass`)
- Frontend: Teacher `ClassDetails` now has "Enroll Student" button with student picker modal and "Withdraw" buttons per student

### GAP-4: Feedback Visibility to Parents and Students (FR-7.1, FR-10.1) — ✅ FIXED

- Backend: `GET /test-scores/{testScoreId}` now returns `TestScoreDetailDTO` with embedded `FeedbackDTO`
- Frontend: Created shared `TestScoreDetail` component showing topic breakdown + teacher feedback
- Student `MyTestScores`: Rows are now clickable, opening detail modal with feedback
- Parent `TestScoreHistory`: Rows are now clickable, opening detail modal with feedback
- This also covers GAP-9 (student test score detail view)

### GAP-5: Progress Reports UI (FR-9) — ✅ FIXED

- Frontend: Created shared `ReportList` component with generate/view functionality
- Teacher: Added `/teacher/classes/:classId/students/:studentId/reports` route with `StudentReports`
- Student: Added `/student/reports` route with `MyReports` + sidebar link
- Parent: Added `/parent/reports` route with `ChildReports` (child selector) + sidebar link

### GAP-7: Data Access Control (FR-12) — ✅ FIXED

- Backend: Created `StudentAccessService` with role-based verification:
  - ADMIN: always allowed
  - STUDENT: only own data
  - PARENT: only linked children
  - TEACHER: only students in their classes
- Applied to: `ProgressController`, `TestScoreController` (student scores endpoint), `ReportController` (generate + list)
- Added `existsByStudentIdAndTeacherId` query to `ClassStudentRepository`

### GAP-8: Admin Enroll Students in Classes (FR-4.3) — ✅ FIXED

- Frontend: Created `AdminClassDetails` component with enroll/withdraw UI
- Admin `ClassManagement` rows are now clickable, navigating to `/admin/classes/:classId`
- Added route in `AdminRoutes`

### GAP-9: Student Test Score Detail View — ✅ FIXED

- Covered by GAP-4 fix: student `MyTestScores` rows open detail modal with topic breakdown + feedback

---

## Remaining Gaps (Not Addressed)

### GAP-1: Multi-Tenancy (FR-2) — DEFERRED

Architectural change requiring centre entity, tenant isolation in all queries, centre-scoped admin operations. Likely deferred for MVP.

### GAP-6: Notifications (FR-11) — DEFERRED

Requires email/SMS integration infrastructure. Backend has basic `GET /my-notifications` but no sending mechanism or frontend inbox.
