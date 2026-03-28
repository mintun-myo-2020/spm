# Sprint 4 — Requirements

## Intent Analysis
- **Request Type**: Enhancement (2 features)
- **Scope**: Multiple Components (frontend routing + backend session notes)
- **Complexity**: Moderate
- **Depth**: Standard

## Feature 1: Nested Route Restructuring

### FR-4.1: Class Detail Page Nested Routes
Convert the current tab-based ClassDetails page to use React Router nested routes.

- **FR-4.1.1**: `/teacher/classes/:classId` redirects to `/teacher/classes/:classId/students` (default sub-route)
- **FR-4.1.2**: `/teacher/classes/:classId/students` — students list with enroll/withdraw actions and ClassSummaryPanel (current Students tab content)
- **FR-4.1.3**: `/teacher/classes/:classId/schedule` — schedule management (current Schedule tab content)
- **FR-4.1.4**: `/teacher/classes/:classId/notes` — session notes list (new, see Feature 2)
- **FR-4.1.5**: Class detail page shows a persistent nav bar (sub-navigation) with links to Students, Schedule, Notes
- **FR-4.1.6**: Page header (class name, subject, student count, back button) remains visible across all sub-routes
- **FR-4.1.7**: Enroll Student action button remains in the page header (visible on students sub-route)

## Feature 2: Session Notes

### FR-4.2: Session Notes Data Model
Add structured notes to class sessions.

- **FR-4.2.1**: Each session can have notes with fields: `topicCovered` (TEXT), `homeworkGiven` (TEXT), `commonWeaknesses` (TEXT), `additionalNotes` (TEXT)
- **FR-4.2.2**: Notes are stored directly on the `class_sessions` table (4 new nullable TEXT columns)
- **FR-4.2.3**: Flyway migration V13 adds the columns

### FR-4.3: Session Notes API
- **FR-4.3.1**: `PUT /api/v1/sessions/{sessionId}/notes` — update session notes (teacher/admin only)
- **FR-4.3.2**: Session notes are returned in existing SessionDTO and SessionDetailDTO responses
- **FR-4.3.3**: New endpoint `GET /api/v1/sessions/class/{classId}/notes` — returns sessions with notes for a class (paginated, sorted by date desc)

### FR-4.4: Session Notes Visibility
- **FR-4.4.1**: Teachers and admins see all 4 fields (topicCovered, homeworkGiven, commonWeaknesses, additionalNotes)
- **FR-4.4.2**: Parents and students see only `topicCovered` and `homeworkGiven` — `commonWeaknesses` and `additionalNotes` are excluded from their responses
- **FR-4.4.3**: Role-based field filtering happens at the API response level

### FR-4.5: Session Notes Frontend
- **FR-4.5.1**: Notes tab (`/teacher/classes/:classId/notes`) shows a list of sessions with their notes, sorted by date desc
- **FR-4.5.2**: Teacher can click a session to edit its notes (inline or modal)
- **FR-4.5.3**: Notes are editable on the SessionDetail page as well
- **FR-4.5.4**: Empty notes show a prompt to add notes
