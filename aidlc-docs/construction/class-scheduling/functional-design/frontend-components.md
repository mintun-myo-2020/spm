# Unit 3: Class Scheduling & Attendance — Frontend Components

## Component Hierarchy

```
Shared Components:
  ScheduleCalendar          — Monthly calendar with session dots
  SessionList               — List/table of sessions
  AttendanceTable           — Attendance marking table with RSVP indicators
  AttendanceStatsPanel      — Attendance rate display

Teacher Components:
  ClassDetails (modified)   — Add "Schedule" tab
  ScheduleTab               — Schedule management within class detail
  CreateScheduleForm        — Create recurring/one-off schedule
  GenerateSessionsModal     — Specify target end date
  SessionDetail             — Session detail with attendance marking
  CreateClassForm (modified)— Add optional schedule accordion

Student Components:
  MySchedule                — Upcoming sessions with RSVP toggle
  StudentRoutes (modified)  — Add schedule route

Parent Components:
  ChildSchedule             — Child's upcoming sessions (read-only)
  ParentRoutes (modified)   — Add schedule route

Admin Components:
  ScheduleOverview          — All sessions across all classes
  AdminClassDetails (mod)   — Add "Schedule" tab (same as teacher but for any class)
  AdminRoutes (modified)    — Add schedule overview route
```

---

## Shared Components

### ScheduleCalendar
Monthly calendar view with dot indicators on session dates.

Props:
- `sessions: SessionDTO[]` — sessions to display
- `onDateClick: (date: string) => void` — callback when a date is clicked
- `selectedDate: string | null` — currently selected date

State:
- `currentMonth: Date` — currently displayed month
- Navigation: prev/next month buttons

Behavior:
- Renders a monthly grid
- Dots on dates with sessions: green (completed), blue (scheduled), red (cancelled)
- Clicking a date calls onDateClick
- Selected date highlighted

### SessionList
Sortable list/table of sessions.

Props:
- `sessions: SessionDTO[]`
- `onSessionClick: (sessionId: string) => void`
- `showClassName: boolean` — true for cross-class views (admin, student)
- `loading: boolean`

Columns: Date, Day, Time, Class (if showClassName), Location, Status, Enrolled, Marked

### AttendanceTable
Table for marking attendance with RSVP indicators.

Props:
- `attendance: AttendanceDTO[]`
- `onMarkAttendance: (entries: AttendanceEntryDTO[]) => void`
- `readOnly: boolean` — true for student/parent views
- `loading: boolean`

State:
- `localAttendance: Map<string, string>` — local edits before save

Behavior:
- Sort: NOT_ATTENDING RSVPs at bottom, then alphabetical
- NOT_ATTENDING students have a badge/indicator
- Dropdown per student: PRESENT, ABSENT, LATE, EXCUSED
- "Save All" button for batch submission
- Read-only mode shows status without dropdowns

### AttendanceStatsPanel
Displays attendance statistics.

Props:
- `stats: StudentAttendanceStatsDTO | ClassAttendanceStatsDTO`
- `type: 'student' | 'class'`

Displays: attendance rate (%), present/absent/late/excused counts, visual bar

---

## Teacher Components

### ScheduleTab
Schedule management tab within ClassDetails.

Props:
- `classId: string`

State:
- `schedules: ScheduleDTO[]`
- `sessions: SessionDTO[]`
- `selectedDate: string | null`
- `showCreateForm: boolean`
- `showGenerateModal: boolean`

Layout:
1. Schedule list (active schedules with day/time/location)
2. "Add Schedule" and "Add One-Off Session" buttons
3. ScheduleCalendar showing sessions
4. SessionList filtered by selected date (if date clicked)
5. Click session → navigate to SessionDetail

### CreateScheduleForm
Form for creating recurring or one-off schedules.

Props:
- `classId: string`
- `onSuccess: () => void`
- `onCancel: () => void`

Fields (recurring):
- Day of week (dropdown: Monday-Sunday)
- Start time (time picker)
- End time (time picker)
- Location (text, optional)
- Effective from (date picker)
- Effective until (date picker, optional)

Fields (one-off):
- Session date (date picker)
- Start time, End time, Location

Toggle: "Recurring" / "One-off" switch at top

### GenerateSessionsModal
Modal for generating more sessions.

Props:
- `scheduleId: string`
- `onSuccess: (count: number) => void`
- `onClose: () => void`

Fields:
- Target end date (date picker)

Displays: schedule summary (day, time) and estimated session count

### SessionDetail (Teacher)
Session detail page with attendance marking.

Props: route param `sessionId`

State:
- `session: SessionDetailDTO`
- `attendanceStats: ClassAttendanceStatsDTO`

Layout:
1. Session info header (date, time, location, status)
2. Action buttons: Reschedule, Cancel (if SCHEDULED)
3. AttendanceTable (editable)
4. AttendanceStatsPanel

### CreateClassForm (Modified)
Add collapsible "Add Schedule" accordion section.

New section (accordion, collapsed by default):
- "Add Initial Schedule" header with expand/collapse toggle
- When expanded: day of week, start time, end time, location, effective from, effective until
- Fields only validated when accordion is expanded and at least one field is filled
- If scheduleDayOfWeek is set, scheduleStartTime/EndTime/EffectiveFrom become required

---

## Student Components

### MySchedule
Upcoming sessions for the student with RSVP toggle.

Route: `/student/schedule`

State:
- `sessions: SessionDTO[]` — from GET /api/v1/sessions/upcoming
- `attendanceBySession: Map<string, AttendanceDTO>` — student's own attendance/RSVP

Layout:
1. ScheduleCalendar (read-only, shows enrolled class sessions)
2. SessionList for selected date or upcoming list
3. Per session: RSVP toggle (ATTENDING ↔ NOT_ATTENDING)
   - Default: ATTENDING (no action needed)
   - Toggle to NOT_ATTENDING: shows optional reason text field
   - Toggle back to ATTENDING: clears reason
4. Past sessions: show attendance status (read-only)

---

## Parent Components

### ChildSchedule
Child's upcoming sessions and attendance history (read-only).

Route: `/parent/children/:studentId/schedule`

State:
- `sessions: SessionDTO[]`
- `attendanceStats: StudentAttendanceStatsDTO`

Layout:
1. Child selector (if parent has multiple children — uses existing linkedStudents)
2. ScheduleCalendar
3. SessionList (upcoming)
4. Per session: RSVP toggle (parent can RSVP on behalf of child)
5. AttendanceStatsPanel (child's attendance rate)
6. Past sessions: attendance history (read-only)

---

## Admin Components

### ScheduleOverview
Dedicated page showing all sessions across all classes.

Route: `/admin/schedule`

State:
- `sessions: SessionDTO[]` — from GET /api/v1/sessions/upcoming (admin sees all)
- `filters: { classId?, status?, startDate?, endDate? }`

Layout:
1. Filter bar: class dropdown, status dropdown, date range
2. ScheduleCalendar (all classes, color-coded by class)
3. SessionList (showClassName=true)
4. Click session → navigate to admin session detail

### AdminClassDetails (Modified)
Add "Schedule" tab (reuses ScheduleTab component with admin permissions).

- Same as teacher ScheduleTab but works for any class (admin authorization)
- Added as a new tab alongside existing Students tab

---

## Service Layer

### schedulingService.ts
```typescript
// Schedules
createSchedule(classId, data) → ScheduleDTO
createOneOffSchedule(classId, data) → ScheduleDTO
getClassSchedules(classId, activeOnly?) → ScheduleDTO[]
updateSchedule(scheduleId, data) → ScheduleDTO
generateSessions(scheduleId, targetEndDate) → SessionDTO[]

// Sessions
getUpcomingSessions(params?) → PagedResponse<SessionDTO>
getClassSessions(classId, params?) → PagedResponse<SessionDTO>
getSessionDetail(sessionId) → SessionDetailDTO
rescheduleSession(sessionId, data) → SessionUpdateResponseDTO
cancelSession(sessionId, reason?) → SessionDTO

// Attendance
batchMarkAttendance(sessionId, entries) → AttendanceDTO[]
updateAttendance(sessionId, studentId, status) → AttendanceDTO
updateRsvp(sessionId, rsvpStatus, reason?, studentId?) → AttendanceDTO

// Stats
getClassAttendanceStats(classId, params?) → ClassAttendanceStatsDTO
getStudentAttendanceStats(studentId, classId, params?) → StudentAttendanceStatsDTO
```

---

## TypeScript Types (additions to domain.ts)

```typescript
// Enums
export type SessionStatus = 'SCHEDULED' | 'CANCELLED' | 'COMPLETED';
export type AttendanceStatus = 'UNMARKED' | 'PRESENT' | 'ABSENT' | 'LATE' | 'EXCUSED';
export type RsvpStatus = 'ATTENDING' | 'NOT_ATTENDING';

// DTOs
export interface ScheduleDTO { ... }
export interface SessionDTO { ... }
export interface SessionDetailDTO { ... }
export interface AttendanceDTO { ... }
export interface SessionUpdateResponseDTO { ... }
export interface StudentAttendanceStatsDTO { ... }
export interface ClassAttendanceStatsDTO { ... }
```

---

## Routing Changes

| Route | Component | Role |
|---|---|---|
| `/teacher/classes/:classId` (existing) | ClassDetails + ScheduleTab | Teacher |
| `/student/schedule` (new) | MySchedule | Student |
| `/parent/children/:studentId/schedule` (new) | ChildSchedule | Parent |
| `/admin/schedule` (new) | ScheduleOverview | Admin |
| `/admin/classes/:classId` (existing) | AdminClassDetails + ScheduleTab | Admin |

---

**Document Version**: 1.0
**Last Updated**: 2026-03-28
