# Sprint 4 — Code Generation Plan

## Step 1: Database Migration
- [x] Create `V13__session_notes.sql` — add `topic_covered`, `homework_given`, `common_weaknesses`, `additional_notes` TEXT columns to `class_sessions`

## Step 2: Backend Entity Update
- [x] Add 4 new fields to `ClassSession.java` with getters/setters

## Step 3: Backend DTOs
- [x] Add note fields to `SessionDTO.java`
- [x] Add note fields to `SessionDetailDTO.java`
- [x] Create `UpdateSessionNotesRequestDTO.java`

## Step 4: Backend Service & Controller
- [x] Add `updateSessionNotes()` to `SessionService.java`
- [x] Add `getClassSessionNotes()` to `SessionService.java`
- [x] Update `toSessionDTO()` in `ScheduleService.java` to include note fields
- [x] Update `getSessionDetail()` in `SessionService.java` to include note fields
- [x] Add `PUT /sessions/{sessionId}/notes` endpoint to `SessionController.java`
- [x] Add `GET /sessions/class/{classId}/notes` endpoint to `SessionController.java`
- [x] Implement role-based field filtering for parent/student responses

## Step 5: Frontend Types
- [x] Add note fields to `SessionDTO` in `domain.ts`
- [x] Add `UpdateSessionNotesForm` to `forms.ts`

## Step 6: Frontend API Service
- [x] Add `updateSessionNotes()` to `schedulingService.ts`
- [x] Add `getClassSessionNotes()` to `schedulingService.ts`

## Step 7: Frontend — Class Detail Restructuring
- [x] Create `ClassLayout.tsx` — shared layout with page header + sub-nav (Students | Schedule | Notes)
- [x] Create `ClassStudents.tsx` — extracted from ClassDetails students tab content
- [x] Update `TeacherRoutes.tsx` — nested routes under `/classes/:classId/*`

## Step 8: Frontend — Notes Components
- [x] Create `SessionNotesTab.tsx` — list of sessions with notes for the class
- [x] Create `SessionNotesForm.tsx` — edit form for session notes (4 structured fields)
- [x] Update `SessionDetail.tsx` — add notes section with edit capability

## Step 9: Verification
- [x] TypeScript compilation check — all new files compile clean (pre-existing errors in test files unrelated)
- [x] Verify routing works (nested routes, default redirect)
