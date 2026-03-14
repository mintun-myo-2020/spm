# Gap Analysis: Requirements vs Implementation

**Date**: 2026-03-14
**Source**: `aidlc-docs/inception/requirements/requirements.md`

---

## Implemented and Working

| Requirement | Description | Status |
|---|---|---|
| FR-1.1 | Keycloak authentication integration | ✅ Done (social login is Keycloak admin config) |
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

## Gaps

### GAP-1: Multi-Tenancy (FR-2)

**Severity**: High (architectural)
**Effort**: Large

No `centre` / `tenant` concept exists in the data model or API. Requirements state "each centre's data is completely separated" and admins manage centre-specific settings. This is the biggest structural gap but may be intentionally deferred for MVP given the scale (3-10 centres initially).

**What's missing**:
- Centre entity and data model
- Tenant isolation in all queries
- Centre-scoped admin operations
- FR-2.1: Data separation between centres
- FR-2.2: Centre-specific settings and management

---

### GAP-2: Admin Edit/Update Subjects and Topics (FR-4.2)

**Severity**: Low
**Effort**: Small

Admin can add and deactivate subjects via `SubjectController`, but there is no `PUT` endpoint to edit/update a subject or topic name/details.

**What's missing**:
- `PUT /api/v1/subjects/{subjectId}` endpoint
- `PUT /api/v1/subjects/{subjectId}/topics/{topicId}` endpoint
- Frontend edit UI in `SubjectManagement`

---

### GAP-3: Teacher Enroll Students in Their Classes (FR-4.3)

**Severity**: Medium
**Effort**: Small

The enrollment endpoint `POST /classes/{classId}/students` is restricted to `@PreAuthorize("hasRole('ADMIN')")`. Per FR-4.3, teachers should be able to assign students to their own classes.

**What's missing**:
- Backend: Allow `TEACHER` role on enrollment endpoint (scoped to own classes)
- Frontend: Enrollment UI in teacher's `ClassDetails` view

---

### GAP-4: Feedback Visibility to Parents and Students (FR-7.1, FR-10.1)

**Severity**: Medium
**Effort**: Small

Backend stores feedback on test scores and `TestScoreDTO` includes a `feedback` field. However, neither the parent `TestScoreHistory` nor the student `MyTestScores` components display feedback. Parents and students have no way to see teacher feedback anywhere in the UI.

**What's missing**:
- Parent `TestScoreHistory`: Show feedback column or expandable row
- Student `MyTestScores`: Show feedback column or expandable row
- Optionally: Test score detail view with full feedback display

---

### GAP-5: Progress Reports UI (FR-9)

**Severity**: Medium
**Effort**: Medium

Backend has full report endpoints (`POST /students/{studentId}/reports`, `GET /reports/{reportId}`, `GET /students/{studentId}/reports`). But there is zero frontend UI for generating or viewing reports — no routes, no sidebar links, no components for any role.

**What's missing**:
- Teacher: Report generation UI + report list for their students
- Parent: View reports for their children
- Student: View own reports
- Admin: View/generate reports for any student
- Sidebar links and routes for all roles

---

### GAP-6: Notifications (FR-11)

**Severity**: Medium
**Effort**: Large

Backend has `GET /notifications/my-notifications` but the feature is largely incomplete.

**What's missing**:
- No notification-sending mechanism (no email/SMS integration)
- No frontend notification list/inbox for any role
- No sidebar links for notifications
- `PUT /notifications/preferences` endpoint may not exist (parent `NotificationPreferences` page exists in frontend but backend support unclear)
- FR-11.1: Trigger notifications on new scores and feedback
- FR-11.2: Parent notification preference management

---

### GAP-7: Data Access Control (FR-12)

**Severity**: High (security)
**Effort**: Medium

`GET /students/{studentId}/test-scores` and `GET /students/{studentId}/progress/overall` perform no authorization check beyond "is authenticated." Any authenticated user can query any student's data by guessing the UUID.

**What's missing**:
- Parents: Verify requested `studentId` is one of their linked children
- Teachers: Verify requested `studentId` is enrolled in one of their classes
- Students: Verify requested `studentId` matches their own profile
- Apply same checks to report endpoints

---

### GAP-8: Admin Enroll Students in Classes (FR-4.3)

**Severity**: Low
**Effort**: Small

Backend enrollment endpoint (`POST /classes/{classId}/students`) exists and works. But the admin `ClassManagement` UI only shows a flat table of classes with no way to click into a class detail view or enroll/withdraw students.

**What's missing**:
- Admin class detail view (or link to existing `ClassDetailDTO` endpoint)
- Enroll student UI (student picker + enroll button)
- Withdraw student UI

---

### GAP-9: Student Test Score Detail View

**Severity**: Low
**Effort**: Small

Students see a list of scores in `MyTestScores` but can't click into one to see topic breakdown or teacher feedback. Backend `GET /test-scores/{testScoreId}` exists and returns full detail.

**What's missing**:
- Student route: `/student/scores/:testScoreId`
- `TestScoreDetail` component showing topic breakdown + feedback
- Clickable rows in `MyTestScores` table

---

## Priority Recommendation

| Priority | Gap | Rationale |
|---|---|---|
| 1 | GAP-7: Data Access Control | Security concern — any user can read any student's data |
| 2 | GAP-4: Feedback Visibility | Quick frontend win, core requirement |
| 3 | GAP-3: Teacher Enrollment | Small backend + frontend change |
| 4 | GAP-8: Admin Enrollment UI | Backend exists, just needs UI |
| 5 | GAP-9: Student Score Detail | Backend exists, just needs UI |
| 6 | GAP-2: Edit Subjects/Topics | Small backend + frontend change |
| 7 | GAP-5: Reports UI | Medium effort, backend ready |
| 8 | GAP-6: Notifications | Large effort, needs email/SMS infra |
| 9 | GAP-1: Multi-Tenancy | Architectural change, possibly deferred for MVP |
