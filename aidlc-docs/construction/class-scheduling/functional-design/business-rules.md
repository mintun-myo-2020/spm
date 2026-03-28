# Unit 3: Class Scheduling & Attendance — Business Rules

## 1. Schedule Creation Rules

### BR-SCH-001: Schedule Validation
- `start_time` must be before `end_time`
- `effective_from` must be today or in the future
- `effective_until` (if provided) must be >= `effective_from`
- For recurring schedules: `day_of_week` must be 1-7 (Monday-Sunday)
- For one-off schedules: `day_of_week` must be NULL, `is_recurring` = false

### BR-SCH-002: Multiple Schedules Per Class
- A class can have multiple active recurring schedules (e.g., Monday 4-6pm AND Wednesday 4-6pm)
- Constraint: one active recurring schedule per day-of-week per class
- Attempting to create a second recurring schedule for the same day-of-week on the same class returns a conflict error
- One-off schedules have no day-of-week uniqueness constraint

### BR-SCH-003: Schedule Creation with Class (FR-14.8)
- When creating a class, an optional initial schedule can be provided
- If schedule fields are provided, the schedule and initial sessions are created atomically with the class
- If schedule fields are omitted, the class is created without a schedule
- Validation: if `scheduleDayOfWeek` is provided, `scheduleStartTime`, `scheduleEndTime`, and `scheduleEffectiveFrom` are required

### BR-SCH-004: Authorization
- Teachers can create/manage schedules for their own classes only
- Admins can create/manage schedules for any class
- Students and parents cannot create or modify schedules

---

## 2. Session Generation Rules

### BR-SES-001: Eager Generation on Schedule Creation
- When a recurring schedule is created, sessions are generated eagerly up to `effective_until`
- If `effective_until` is NULL, no sessions are auto-generated (teacher must use "Generate More Sessions")
- For one-off schedules: exactly one session is generated for `effective_from` date

### BR-SES-002: Generate More Sessions
- Teacher/admin specifies a new target end date
- System generates sessions from the day after the last existing session date up to the target date
- Only generates for dates matching the schedule's `day_of_week`
- Skips dates that already have a session for this schedule (idempotent)
- Target end date must be after the last existing session date

### BR-SES-003: Session Pre-population
- When a session is generated, `SessionAttendance` records are created for all currently ACTIVE enrolled students in the class
- Attendance status defaults to `UNMARKED`
- RSVP status defaults to `ATTENDING` (opt-out model)

### BR-SES-004: Session Conflict Warning
- When rescheduling a session, the system checks for other sessions in the same class on the target date
- If a conflict exists, a warning is returned in the API response but the operation proceeds
- The warning includes details of the conflicting session(s)

---

## 3. Schedule Deactivation Rules

### BR-DEACT-001: Deactivation via effective_until
- Setting `effective_until` on a schedule deactivates it (no hard delete)
- All future sessions (session_date > effective_until) with status SCHEDULED are automatically cancelled
- Past sessions and already-completed sessions are preserved
- Cancel reason is set to "Schedule deactivated"

### BR-DEACT-002: No Hard Delete
- Schedules are never deleted — only deactivated via `effective_until`
- This preserves session history and attendance records

---

## 4. Session Management Rules

### BR-SESM-001: Cancel Session
- Teacher/admin can cancel a specific session with an optional reason
- Status changes from SCHEDULED → CANCELLED
- Only SCHEDULED sessions can be cancelled
- Cancelled sessions retain their attendance records (for history)

### BR-SESM-002: Reschedule Session
- Teacher/admin can change date, start_time, end_time, and location of a SCHEDULED session
- Cannot reschedule CANCELLED or COMPLETED sessions
- Conflict warning returned if another session exists on the target date (same class)

### BR-SESM-003: Complete Session
- Sessions with `session_date` in the past are considered implicitly completed
- No explicit "complete" action needed — status can remain SCHEDULED for past sessions
- Teacher can optionally mark a session as COMPLETED

---

## 5. Attendance Rules

### BR-ATT-001: Attendance Marking
- Teacher/admin marks attendance for each student: PRESENT, ABSENT, LATE, EXCUSED
- Supports batch marking (all students at once) and individual updates
- Only teacher who owns the class or admin can mark attendance

### BR-ATT-002: Always Editable
- Attendance can be updated at any time — no lock period
- Teacher can correct attendance records retroactively

### BR-ATT-003: Batch Attendance
- Batch endpoint accepts list of `{studentId, status}` pairs
- All students in the batch must be enrolled in the class
- Partial failures: if one student is invalid, the entire batch fails (atomic)

### BR-ATT-004: Attendance Pre-population
- Attendance records are pre-populated with UNMARKED status when sessions are generated
- If a new student enrolls after sessions were generated, attendance records for future sessions are NOT auto-created
- Teacher sees only pre-populated students; new enrollees appear in future sessions

---

## 6. RSVP Rules

### BR-RSVP-001: Opt-Out Model
- All students default to ATTENDING for every session
- Students/parents only interact to flag NOT_ATTENDING as exceptions
- RSVP is a soft indicator — does not affect attendance marking

### BR-RSVP-002: RSVP Timing
- Students can update RSVP any time before the session starts
- Cannot RSVP for past sessions or cancelled sessions

### BR-RSVP-003: Parent RSVP
- Parents can RSVP on behalf of their linked children
- Parent must have a valid parent-student link
- Parent RSVP has the same effect as student RSVP

### BR-RSVP-004: RSVP Reason
- When setting RSVP to NOT_ATTENDING, an optional reason can be provided
- When reverting to ATTENDING, the reason is cleared

### BR-RSVP-005: Makeup Lessons
- One-off sessions (is_recurring=false) can serve as makeup lessons
- Teacher creates a one-off schedule for the makeup date/time
- The one-off session is generated and attendance pre-populated like any other session

---

## 7. Attendance Statistics Rules

### BR-STAT-001: Per-Student Stats
- Attendance rate = (PRESENT + LATE) / (total sessions where status != UNMARKED) * 100
- Calculated on-the-fly from attendance records (no denormalized counters)
- Scoped to a specific class

### BR-STAT-002: Per-Class Stats
- Class attendance rate = average of all students' attendance rates
- Total sessions count (excluding cancelled)
- Sessions with attendance marked vs total

### BR-STAT-003: Stat Scope
- Stats are always scoped to a class
- Optional date range filter (startDate, endDate)

---

## 8. Authorization Rules

### BR-AUTH-001: Teacher Permissions
- Create schedules for own classes
- Update/deactivate schedules for own classes
- Generate sessions for own classes
- Cancel/reschedule sessions for own classes
- Mark attendance for own classes
- View sessions and attendance for own classes

### BR-AUTH-002: Admin Permissions
- Full CRUD on all schedules across all classes
- Generate sessions for any class
- Cancel/reschedule any session
- Mark attendance for any class
- View all sessions and attendance

### BR-AUTH-003: Student Permissions
- View upcoming sessions for enrolled classes only
- Update own RSVP status only
- View own attendance history
- Cannot view other students' attendance

### BR-AUTH-004: Parent Permissions
- View upcoming sessions for linked children's enrolled classes
- Update RSVP for linked children only
- View linked children's attendance history
- Read-only access — cannot modify schedules or attendance

---

## 9. Frontend Display Rules

### BR-UI-001: Attendance Marking Sort Order
- NOT_ATTENDING RSVP students sorted to bottom of the attendance list
- Visual badge/indicator on NOT_ATTENDING students
- ATTENDING students sorted alphabetically at top

### BR-UI-002: Calendar View
- Monthly calendar with dot indicators on session dates
- Clicking a date shows session details for that day
- Color coding: green = completed, blue = scheduled, red = cancelled

### BR-UI-003: Class Creation Form
- Accordion/collapsible "Add Schedule" section
- Collapsed by default — expands when clicked
- Schedule fields only validated when section is expanded and fields are filled

---

**Document Version**: 1.0
**Last Updated**: 2026-03-28
