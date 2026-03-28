# Unit 3: Class Scheduling & Attendance — Business Logic Model

## 1. Schedule Creation Workflow

### 1.1 Create Recurring Schedule
```
Input: classId, dayOfWeek, startTime, endTime, location?, effectiveFrom, effectiveUntil?
```

1. Validate authorization (teacher owns class OR admin)
2. Validate class exists and is active
3. Validate time range (startTime < endTime)
4. Validate date range (effectiveFrom >= today, effectiveUntil >= effectiveFrom if provided)
5. Check uniqueness: no active recurring schedule for same class + dayOfWeek
6. Create ClassSchedule record (is_recurring=true)
7. If effectiveUntil is provided: generate sessions from effectiveFrom to effectiveUntil
8. If effectiveUntil is NULL: no sessions generated (teacher uses "Generate More Sessions" later)
9. Return schedule with generated session count

### 1.2 Create One-Off Schedule (Makeup Lesson)
```
Input: classId, sessionDate, startTime, endTime, location?
```

1. Validate authorization
2. Validate class exists and is active
3. Validate sessionDate >= today
4. Create ClassSchedule record (is_recurring=false, effective_from=sessionDate, day_of_week=NULL)
5. Generate exactly one session for sessionDate
6. Pre-populate attendance for all active enrolled students
7. Return schedule with session

### 1.3 Create Class with Initial Schedule (FR-14.8)
```
Input: CreateClassRequestDTO (with optional schedule fields)
```

1. Create class via existing ClassService.createClass()
2. If schedule fields provided:
   a. Validate schedule fields (dayOfWeek, startTime, endTime, effectiveFrom required)
   b. Create ClassSchedule atomically
   c. Generate sessions if effectiveUntil provided
3. Return ClassDTO (schedule creation is a side effect)

---

## 2. Session Generation Algorithm

### 2.1 Generate Sessions from Schedule
```
Input: scheduleId, targetEndDate
```

1. Load schedule, validate it exists and is active (effective_until is NULL or >= today)
2. Validate targetEndDate > last existing session date for this schedule
3. Determine start date:
   - If no existing sessions: use schedule.effectiveFrom
   - If existing sessions: day after the last session date
4. For recurring schedules:
   a. Iterate from startDate to targetEndDate
   b. For each date matching schedule.dayOfWeek:
      - Skip if session already exists for this schedule + date (idempotent)
      - Create ClassSession (status=SCHEDULED, inherit time/location from schedule)
      - Pre-populate SessionAttendance for all ACTIVE enrolled students
5. For one-off schedules: should already have exactly one session (no-op)
6. Return list of newly created sessions

### 2.2 Attendance Pre-population
```
Input: classSession (newly created)
```

1. Query ClassStudent where class_id = session.class_id AND status = ACTIVE
2. For each enrolled student:
   - Create SessionAttendance (status=UNMARKED, student_rsvp=ATTENDING)
3. Bulk insert all attendance records

---

## 3. Schedule Deactivation Workflow

### 3.1 Deactivate Schedule
```
Input: scheduleId, effectiveUntilDate
```

1. Load schedule, validate authorization
2. Set effective_until = effectiveUntilDate
3. Find all ClassSessions where:
   - schedule_id = scheduleId
   - session_date > effectiveUntilDate
   - status = SCHEDULED
4. Bulk update: set status = CANCELLED, cancel_reason = "Schedule deactivated"
5. Return updated schedule

---

## 4. Session Management Workflows

### 4.1 Cancel Session
```
Input: sessionId, cancelReason?
```

1. Load session, validate authorization (teacher owns class OR admin)
2. Validate status = SCHEDULED (cannot cancel already cancelled/completed)
3. Set status = CANCELLED, cancel_reason = provided reason
4. Return updated session

### 4.2 Reschedule Session
```
Input: sessionId, newDate?, newStartTime?, newEndTime?, newLocation?
```

1. Load session, validate authorization
2. Validate status = SCHEDULED
3. Apply changes (only non-null fields)
4. Check for conflicts: other sessions in same class on newDate
5. If conflict found: include warning in response (but proceed)
6. Return updated session with optional conflict warning

---

## 5. Attendance Workflows

### 5.1 Batch Mark Attendance
```
Input: sessionId, List<{studentId, status}>
```

1. Load session, validate authorization
2. Validate all studentIds have existing attendance records for this session
3. For each entry:
   - Update attendance status
   - Set marked_by = current user
   - Set marked_at = now
4. Atomic: all succeed or all fail
5. Return updated attendance list

### 5.2 Individual Attendance Update
```
Input: sessionId, studentId, status
```

1. Load attendance record (session_id + student_id)
2. Validate authorization
3. Update status, marked_by, marked_at
4. Return updated attendance

---

## 6. RSVP Workflow

### 6.1 Student/Parent RSVP
```
Input: sessionId, studentId (from auth context or parent's child), rsvpStatus, reason?
```

1. Load attendance record (session_id + student_id)
2. Validate authorization:
   - Student: must be the student themselves
   - Parent: must be linked to the student
3. Validate session is SCHEDULED and session_date is in the future
4. Update student_rsvp:
   - If NOT_ATTENDING: set rsvp_reason
   - If ATTENDING: clear rsvp_reason
5. Return updated attendance

---

## 7. Query Workflows

### 7.1 Get Upcoming Sessions (Role-Based)
```
Input: currentUser, page, size, startDate?, endDate?
```

- TEACHER: sessions for all classes where teacher_id = current teacher, status = SCHEDULED, session_date >= today
- STUDENT: sessions for enrolled classes (via class_students), status = SCHEDULED, session_date >= today
- PARENT: sessions for linked children's enrolled classes, status = SCHEDULED, session_date >= today
- ADMIN: all sessions, status = SCHEDULED, session_date >= today

Default sort: session_date ASC, start_time ASC

### 7.2 Get Class Sessions
```
Input: classId, status?, startDate?, endDate?, page, size
```

1. Validate authorization (teacher owns class, admin, or enrolled student/parent)
2. Query sessions filtered by class_id and optional filters
3. Return paged results

### 7.3 Get Session Attendance
```
Input: sessionId
```

1. Load all attendance records for session
2. Sort: NOT_ATTENDING RSVPs to bottom, then alphabetical by student name
3. Include student name, RSVP status, attendance status

---

## 8. Attendance Statistics

### 8.1 Student Attendance Stats (Per Class)
```
Input: studentId, classId, startDate?, endDate?
```

1. Query attendance records for student in class within date range
2. Calculate:
   - totalSessions: count where status != UNMARKED
   - presentCount: count where status IN (PRESENT, LATE)
   - absentCount: count where status = ABSENT
   - excusedCount: count where status = EXCUSED
   - lateCount: count where status = LATE
   - attendanceRate: (presentCount + lateCount) / totalSessions * 100
3. Return stats DTO

### 8.2 Class Attendance Stats
```
Input: classId, startDate?, endDate?
```

1. Query all attendance records for class within date range
2. Calculate:
   - totalSessions: distinct session count (excluding cancelled)
   - sessionsWithAttendance: sessions where at least one student is marked
   - averageAttendanceRate: mean of all students' attendance rates
3. Return class stats DTO

---

## 9. Service Layer Structure

### ScheduleService
- createSchedule(classId, CreateScheduleRequestDTO) → ScheduleDTO
- getClassSchedules(classId) → List<ScheduleDTO>
- updateSchedule(scheduleId, UpdateScheduleRequestDTO) → ScheduleDTO
- deactivateSchedule(scheduleId, effectiveUntilDate) → ScheduleDTO
- generateSessions(scheduleId, targetEndDate) → List<SessionDTO>

### SessionService
- getUpcomingSessions(currentUser, filters) → PagedResponse<SessionDTO>
- getClassSessions(classId, filters) → PagedResponse<SessionDTO>
- getSessionDetail(sessionId) → SessionDetailDTO
- rescheduleSession(sessionId, RescheduleRequestDTO) → SessionDTO
- cancelSession(sessionId, cancelReason) → SessionDTO

### AttendanceService
- getSessionAttendance(sessionId) → List<AttendanceDTO>
- batchMarkAttendance(sessionId, List<AttendanceEntryDTO>) → List<AttendanceDTO>
- updateAttendance(sessionId, studentId, status) → AttendanceDTO
- updateRsvp(sessionId, studentId, rsvpStatus, reason?) → AttendanceDTO
- getStudentAttendanceStats(studentId, classId, dateRange?) → AttendanceStatsDTO
- getClassAttendanceStats(classId, dateRange?) → ClassAttendanceStatsDTO

---

**Document Version**: 1.0
**Last Updated**: 2026-03-28
