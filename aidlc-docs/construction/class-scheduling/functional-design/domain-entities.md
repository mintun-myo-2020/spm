# Unit 3: Class Scheduling & Attendance — Domain Entities

## Entity Relationship Overview

```
TuitionClass (existing)
    |
    |--- 1:N ---> ClassSchedule
    |                  |
    |                  |--- 1:N ---> ClassSession
    |                                    |
    |                                    |--- 1:N ---> SessionAttendance
    |                                                       |
    |                                                       |--- N:1 ---> Student (existing)
    |
    |--- 1:N ---> ClassStudent (existing)
```

### Text Alternative
- TuitionClass has many ClassSchedules
- ClassSchedule has many ClassSessions
- ClassSession has many SessionAttendance records
- SessionAttendance references one Student
- TuitionClass has many ClassStudents (existing relationship)

---

## Entity 1: ClassSchedule

Represents a recurring or one-off schedule template for a class. Sessions are generated from this template.

| Field | Type | Constraints | Description |
|---|---|---|---|
| id | UUID | PK, auto-generated | Unique identifier |
| class_id | UUID | FK → classes(id), NOT NULL | The class this schedule belongs to |
| day_of_week | Integer | NULL for one-off, 1-7 (Mon-Sun) for recurring | Day of week for recurring schedules |
| start_time | LocalTime | NOT NULL | Session start time |
| end_time | LocalTime | NOT NULL | Session end time |
| location | String(255) | NULLABLE | Optional location text |
| is_recurring | Boolean | NOT NULL, DEFAULT true | Whether this is a recurring or one-off schedule |
| effective_from | LocalDate | NOT NULL | Start date of the schedule |
| effective_until | LocalDate | NULLABLE | End date; NULL = indefinite. Set to deactivate. |
| created_by | UUID | FK → users(id), NOT NULL | User who created the schedule |
| created_at | Instant | NOT NULL, auto | Creation timestamp |
| updated_at | Instant | NOT NULL, auto | Last update timestamp |

### Relationships
- Many-to-One: `TuitionClass` (class_id)
- Many-to-One: `User` (created_by)
- One-to-Many: `ClassSession` (schedule_id)

### Constraints
- For recurring schedules: `day_of_week` must be 1-7
- For one-off schedules: `day_of_week` is NULL, `is_recurring` = false
- `end_time` must be after `start_time`
- `effective_until` (if set) must be >= `effective_from`
- A class can have multiple active schedules (one per day-of-week slot)

---

## Entity 2: ClassSession

Represents a single scheduled session instance, generated from a ClassSchedule or created as a one-off.

| Field | Type | Constraints | Description |
|---|---|---|---|
| id | UUID | PK, auto-generated | Unique identifier |
| schedule_id | UUID | FK → class_schedules(id), NULLABLE | Source schedule (NULL for orphaned sessions) |
| class_id | UUID | FK → classes(id), NOT NULL | The class this session belongs to |
| session_date | LocalDate | NOT NULL | Date of the session |
| start_time | LocalTime | NOT NULL | Session start time |
| end_time | LocalTime | NOT NULL | Session end time |
| location | String(255) | NULLABLE | Location (inherited from schedule, overridable) |
| status | SessionStatus | NOT NULL, DEFAULT SCHEDULED | SCHEDULED, CANCELLED, COMPLETED |
| cancel_reason | String (TEXT) | NULLABLE | Reason for cancellation |
| created_at | Instant | NOT NULL, auto | Creation timestamp |
| updated_at | Instant | NOT NULL, auto | Last update timestamp |

### Relationships
- Many-to-One: `ClassSchedule` (schedule_id) — nullable
- Many-to-One: `TuitionClass` (class_id)
- One-to-Many: `SessionAttendance` (session_id)

### Enums
```java
public enum SessionStatus {
    SCHEDULED,   // Future session, not yet held
    CANCELLED,   // Cancelled by teacher/admin
    COMPLETED    // Session has occurred (past date or manually marked)
}
```

---

## Entity 3: SessionAttendance

Tracks attendance and RSVP status for each student in a session. Pre-populated when sessions are generated.

| Field | Type | Constraints | Description |
|---|---|---|---|
| id | UUID | PK, auto-generated | Unique identifier |
| session_id | UUID | FK → class_sessions(id), NOT NULL | The session |
| student_id | UUID | FK → students(id), NOT NULL | The student |
| status | AttendanceStatus | NOT NULL, DEFAULT UNMARKED | Attendance status |
| student_rsvp | RsvpStatus | NOT NULL, DEFAULT ATTENDING | Student's RSVP (opt-out model) |
| rsvp_reason | String (TEXT) | NULLABLE | Reason for NOT_ATTENDING |
| marked_by | UUID | FK → users(id), NULLABLE | Teacher/admin who marked attendance |
| marked_at | Instant | NULLABLE | When attendance was marked |
| created_at | Instant | NOT NULL, auto | Creation timestamp |
| updated_at | Instant | NOT NULL, auto | Last update timestamp |

### Unique Constraint
- `UNIQUE (session_id, student_id)` — one attendance record per student per session

### Relationships
- Many-to-One: `ClassSession` (session_id)
- Many-to-One: `Student` (student_id)
- Many-to-One: `User` (marked_by) — nullable

### Enums
```java
public enum AttendanceStatus {
    UNMARKED,   // Not yet marked by teacher
    PRESENT,    // Student attended
    ABSENT,     // Student did not attend
    LATE,       // Student arrived late
    EXCUSED     // Absent with valid excuse
}

public enum RsvpStatus {
    ATTENDING,       // Default — student is expected to attend
    NOT_ATTENDING    // Student/parent flagged they cannot attend
}
```

### RSVP Model
- Opt-out model: all students default to ATTENDING
- Students/parents only interact to flag NOT_ATTENDING as exceptions
- RSVP is a soft indicator — teacher still marks actual attendance
- NOT_ATTENDING students can still be marked PRESENT if they show up

---

## Existing Entity Modifications

### TuitionClass (no schema change)
No column changes. The relationship to ClassSchedule is managed from the ClassSchedule side (class_id FK).

### CreateClassRequestDTO (modified)
Add optional schedule fields for FR-14.8 (initial schedule on class creation):

| New Field | Type | Description |
|---|---|---|
| scheduleDayOfWeek | Integer | Optional. 1-7 for initial recurring schedule |
| scheduleStartTime | LocalTime | Required if scheduleDayOfWeek provided |
| scheduleEndTime | LocalTime | Required if scheduleDayOfWeek provided |
| scheduleLocation | String | Optional location |
| scheduleEffectiveFrom | LocalDate | Required if scheduleDayOfWeek provided |
| scheduleEffectiveUntil | LocalDate | Optional end date for initial schedule |

---

## Database Migration (V11)

```sql
-- V11: Class Scheduling & Attendance

CREATE TABLE class_schedules (
    id UUID PRIMARY KEY,
    class_id UUID NOT NULL REFERENCES classes(id),
    day_of_week INT,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    location VARCHAR(255),
    is_recurring BOOLEAN NOT NULL DEFAULT true,
    effective_from DATE NOT NULL,
    effective_until DATE,
    created_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE class_sessions (
    id UUID PRIMARY KEY,
    schedule_id UUID REFERENCES class_schedules(id),
    class_id UUID NOT NULL REFERENCES classes(id),
    session_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    location VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    cancel_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE session_attendance (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES class_sessions(id),
    student_id UUID NOT NULL REFERENCES students(id),
    status VARCHAR(20) NOT NULL DEFAULT 'UNMARKED',
    student_rsvp VARCHAR(20) NOT NULL DEFAULT 'ATTENDING',
    rsvp_reason TEXT,
    marked_by UUID REFERENCES users(id),
    marked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (session_id, student_id)
);

-- Performance indexes
CREATE INDEX idx_class_schedules_class_id ON class_schedules(class_id);
CREATE INDEX idx_class_sessions_class_id ON class_sessions(class_id);
CREATE INDEX idx_class_sessions_schedule_id ON class_sessions(schedule_id);
CREATE INDEX idx_class_sessions_date ON class_sessions(session_date);
CREATE INDEX idx_class_sessions_status ON class_sessions(status);
CREATE INDEX idx_session_attendance_session_id ON session_attendance(session_id);
CREATE INDEX idx_session_attendance_student_id ON session_attendance(student_id);
```

---

**Document Version**: 1.0
**Last Updated**: 2026-03-28
