# Unit 3: Class Scheduling & Attendance — API Contracts

## DTOs

### Request DTOs

```java
// Schedule creation
public record CreateScheduleRequestDTO(
    @NotNull Integer dayOfWeek,        // 1-7 for recurring, null handled by one-off variant
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime,
    String location,
    @NotNull LocalDate effectiveFrom,
    LocalDate effectiveUntil           // NULL = indefinite
) {}

// One-off session (makeup lesson)
public record CreateOneOffScheduleRequestDTO(
    @NotNull LocalDate sessionDate,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime,
    String location
) {}

// Generate more sessions
public record GenerateSessionsRequestDTO(
    @NotNull LocalDate targetEndDate
) {}

// Reschedule session
public record RescheduleSessionRequestDTO(
    LocalDate newDate,
    LocalTime newStartTime,
    LocalTime newEndTime,
    String newLocation
) {}

// Cancel session
public record CancelSessionRequestDTO(
    String reason
) {}

// Batch attendance
public record BatchAttendanceRequestDTO(
    @NotNull List<AttendanceEntryDTO> entries
) {}

public record AttendanceEntryDTO(
    @NotNull UUID studentId,
    @NotNull String status    // PRESENT, ABSENT, LATE, EXCUSED
) {}

// Individual attendance update
public record UpdateAttendanceRequestDTO(
    @NotNull String status    // PRESENT, ABSENT, LATE, EXCUSED
) {}

// RSVP
public record RsvpRequestDTO(
    @NotNull String rsvpStatus,   // ATTENDING, NOT_ATTENDING
    String reason                  // Optional, for NOT_ATTENDING
) {}

// Updated class creation (FR-14.8)
// Extends existing CreateClassRequestDTO with optional schedule fields
public record CreateClassRequestDTO(
    @NotBlank String name,
    @NotNull UUID subjectId,
    @NotNull UUID teacherId,
    String description,
    Integer maxStudents,
    // Optional initial schedule
    Integer scheduleDayOfWeek,
    LocalTime scheduleStartTime,
    LocalTime scheduleEndTime,
    String scheduleLocation,
    LocalDate scheduleEffectiveFrom,
    LocalDate scheduleEffectiveUntil
) {}
```

### Response DTOs

```java
public record ScheduleDTO(
    UUID id,
    UUID classId,
    String className,
    Integer dayOfWeek,
    String dayOfWeekName,      // "Monday", "Tuesday", etc.
    LocalTime startTime,
    LocalTime endTime,
    String location,
    boolean isRecurring,
    LocalDate effectiveFrom,
    LocalDate effectiveUntil,
    int sessionCount,          // Total generated sessions
    Instant createdAt
) {}

public record SessionDTO(
    UUID id,
    UUID scheduleId,
    UUID classId,
    String className,
    LocalDate sessionDate,
    String dayOfWeekName,
    LocalTime startTime,
    LocalTime endTime,
    String location,
    String status,             // SCHEDULED, CANCELLED, COMPLETED
    String cancelReason,
    int enrolledCount,         // Total attendance records
    int markedCount,           // Attendance records where status != UNMARKED
    int notAttendingRsvpCount, // Students who RSVP'd NOT_ATTENDING
    Instant createdAt,
    String myRsvp,             // Current user's RSVP status (ATTENDING/NOT_ATTENDING), null for non-student views
    String myRsvpReason        // Current user's RSVP reason, null if attending or non-student view
) {}

public record SessionDetailDTO(
    UUID id,
    UUID scheduleId,
    UUID classId,
    String className,
    LocalDate sessionDate,
    String dayOfWeekName,
    LocalTime startTime,
    LocalTime endTime,
    String location,
    String status,
    String cancelReason,
    List<AttendanceDTO> attendance,
    Instant createdAt
) {}

public record AttendanceDTO(
    UUID id,
    UUID sessionId,
    UUID studentId,
    String studentName,
    String status,             // UNMARKED, PRESENT, ABSENT, LATE, EXCUSED
    String studentRsvp,        // ATTENDING, NOT_ATTENDING
    String rsvpReason,
    UUID markedBy,
    Instant markedAt
) {}

// Conflict warning wrapper
public record SessionUpdateResponseDTO(
    SessionDTO session,
    List<String> warnings      // e.g., "Conflict: session already exists on 2026-04-05"
) {}

// Attendance statistics
public record StudentAttendanceStatsDTO(
    UUID studentId,
    String studentName,
    UUID classId,
    int totalSessions,
    int presentCount,
    int absentCount,
    int lateCount,
    int excusedCount,
    int unmarkedCount,
    double attendanceRate      // percentage
) {}

public record ClassAttendanceStatsDTO(
    UUID classId,
    String className,
    int totalSessions,
    int sessionsWithAttendance,
    double averageAttendanceRate,
    List<StudentAttendanceStatsDTO> studentStats
) {}
```

---

## API Endpoints

### Schedule Endpoints

#### POST /api/v1/classes/{classId}/schedules
Create a recurring schedule for a class.

- Auth: TEACHER (own class), ADMIN
- Request: `CreateScheduleRequestDTO`
- Response: `ApiResponse<ScheduleDTO>` (201 Created)
- Errors: 404 (class not found), 403 (not owner), 409 (duplicate day-of-week)

#### POST /api/v1/classes/{classId}/schedules/one-off
Create a one-off schedule (makeup lesson).

- Auth: TEACHER (own class), ADMIN
- Request: `CreateOneOffScheduleRequestDTO`
- Response: `ApiResponse<ScheduleDTO>` (201 Created)
- Errors: 404, 403

#### GET /api/v1/classes/{classId}/schedules
List all schedules for a class.

- Auth: TEACHER (own class), ADMIN
- Response: `ApiResponse<List<ScheduleDTO>>`
- Query params: `activeOnly` (boolean, default true)

#### PUT /api/v1/schedules/{scheduleId}
Update a schedule (time, location, effective_until for deactivation).

- Auth: TEACHER (own class), ADMIN
- Request: `UpdateScheduleRequestDTO` (partial update — only non-null fields applied)
- Response: `ApiResponse<ScheduleDTO>`
- Side effect: if effective_until is set/changed, auto-cancel future sessions beyond that date

#### POST /api/v1/schedules/{scheduleId}/generate-sessions
Generate more sessions from a recurring schedule.

- Auth: TEACHER (own class), ADMIN
- Request: `GenerateSessionsRequestDTO`
- Response: `ApiResponse<List<SessionDTO>>` (newly created sessions)
- Errors: 400 (targetEndDate not after last session), 404

---

### Session Endpoints

#### GET /api/v1/sessions/upcoming
Get upcoming sessions for the current user (role-based filtering).

- Auth: ALL ROLES
- Response: `PagedResponse<SessionDTO>`
- Query params: `startDate`, `endDate`, `page`, `size`
- Filtering:
  - TEACHER: own classes
  - STUDENT: enrolled classes
  - PARENT: linked children's enrolled classes
  - ADMIN: all classes

#### GET /api/v1/classes/{classId}/sessions
List sessions for a specific class.

- Auth: TEACHER (own class), ADMIN, STUDENT (enrolled), PARENT (child enrolled)
- Response: `PagedResponse<SessionDTO>`
- Query params: `status`, `startDate`, `endDate`, `page`, `size`

#### GET /api/v1/sessions/{sessionId}
Get session detail with attendance list.

- Auth: TEACHER (own class), ADMIN
- Response: `ApiResponse<SessionDetailDTO>`
- Attendance sorted: NOT_ATTENDING RSVPs at bottom, then alphabetical

#### PUT /api/v1/sessions/{sessionId}
Reschedule a session (change date/time/location).

- Auth: TEACHER (own class), ADMIN
- Request: `RescheduleSessionRequestDTO`
- Response: `ApiResponse<SessionUpdateResponseDTO>` (includes conflict warnings)
- Errors: 400 (session not SCHEDULED), 404

#### PUT /api/v1/sessions/{sessionId}/cancel
Cancel a session.

- Auth: TEACHER (own class), ADMIN
- Request: `CancelSessionRequestDTO`
- Response: `ApiResponse<SessionDTO>`
- Errors: 400 (session not SCHEDULED), 404

---

### Attendance Endpoints

#### POST /api/v1/sessions/{sessionId}/attendance
Batch mark attendance for a session.

- Auth: TEACHER (own class), ADMIN
- Request: `BatchAttendanceRequestDTO`
- Response: `ApiResponse<List<AttendanceDTO>>`
- Errors: 400 (invalid student), 404

#### PUT /api/v1/sessions/{sessionId}/attendance/{studentId}
Update individual student attendance.

- Auth: TEACHER (own class), ADMIN
- Request: `UpdateAttendanceRequestDTO`
- Response: `ApiResponse<AttendanceDTO>`

#### PUT /api/v1/sessions/{sessionId}/rsvp
Student/parent/teacher/admin RSVP for a session.

- Auth: STUDENT (own attendance), PARENT (linked child), TEACHER (own class), ADMIN (any class)
- Request: `RsvpRequestDTO`
- Response: `ApiResponse<AttendanceDTO>`
- Note: For parents/teachers/admins, include `studentId` as query param
- Teachers can only update RSVP for students in their own classes
- Errors: 400 (session not SCHEDULED or in past), 403

---

### Statistics Endpoints

#### GET /api/v1/classes/{classId}/attendance-stats
Get class-level attendance statistics.

- Auth: TEACHER (own class), ADMIN
- Response: `ApiResponse<ClassAttendanceStatsDTO>`
- Query params: `startDate`, `endDate`

#### GET /api/v1/students/{studentId}/classes/{classId}/attendance-stats
Get student attendance statistics for a specific class.

- Auth: TEACHER (own class), ADMIN, STUDENT (own), PARENT (linked child)
- Response: `ApiResponse<StudentAttendanceStatsDTO>`
- Query params: `startDate`, `endDate`

---

## Modified Existing Endpoint

### POST /api/v1/classes (Updated)
Updated to accept optional initial schedule fields.

- Existing behavior preserved when schedule fields are omitted
- When schedule fields provided: creates class + schedule + sessions atomically
- Request body: updated `CreateClassRequestDTO` with optional schedule fields

---

**Document Version**: 1.1
**Last Updated**: 2026-03-28
