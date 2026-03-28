# Unit 3: Class Scheduling & Attendance — Code Generation Plan

## Unit Context
- **Unit**: Class Scheduling & Attendance
- **Scope**: 3 new DB tables, new `scheduling` module (backend), new frontend components for all 4 roles
- **Dependencies**: Existing `classmanagement`, `user`, `auth` modules
- **Project Type**: Brownfield (Spring Boot 4.x + React 18 TypeScript)

## Code Location
- **Backend**: `spm/src/main/java/com/eggtive/spm/scheduling/`
- **Frontend**: `spm-frontend/src/` (components, services, types)
- **Migration**: `spm/src/main/resources/db/migration/V11__class_scheduling.sql`
- **Modified files**: ClassController, ClassService, CreateClassRequestDTO, ErrorCode, Sidebar, Routes, domain.ts, classService.ts, CreateClassForm (teacher + admin)

---

## Steps

### Backend

- [x] Step 1: Database Migration (V11)
  - Create `spm/src/main/resources/db/migration/V11__class_scheduling.sql`
  - Tables: class_schedules, class_sessions, session_attendance
  - Indexes on class_id, schedule_id, session_date, status, session_id, student_id

- [x] Step 2: Enums
  - Create `spm/src/main/java/com/eggtive/spm/common/enums/SessionStatus.java` (SCHEDULED, CANCELLED, COMPLETED)
  - Create `spm/src/main/java/com/eggtive/spm/common/enums/AttendanceStatus.java` (UNMARKED, PRESENT, ABSENT, LATE, EXCUSED)
  - Create `spm/src/main/java/com/eggtive/spm/common/enums/RsvpStatus.java` (ATTENDING, NOT_ATTENDING)
  - Modify `ErrorCode.java` — add SCHEDULE_CONFLICT, SESSION_NOT_SCHEDULED, INVALID_RSVP

- [x] Step 3: JPA Entities
  - Create `scheduling/entity/ClassSchedule.java` — extends BaseEntity, ManyToOne TuitionClass + User
  - Create `scheduling/entity/ClassSession.java` — extends BaseEntity, ManyToOne ClassSchedule + TuitionClass
  - Create `scheduling/entity/SessionAttendance.java` — extends BaseEntity, ManyToOne ClassSession + Student + User

- [x] Step 4: Repositories
  - Create `scheduling/repository/ClassScheduleRepository.java`
  - Create `scheduling/repository/ClassSessionRepository.java`
  - Create `scheduling/repository/SessionAttendanceRepository.java`
  - Custom queries: findByClassId, findUpcomingSessions (role-based), findByScheduleIdAndSessionDateAfter, attendance stats queries

- [x] Step 5: DTOs
  - Create `scheduling/dto/CreateScheduleRequestDTO.java`
  - Create `scheduling/dto/CreateOneOffScheduleRequestDTO.java`
  - Create `scheduling/dto/GenerateSessionsRequestDTO.java`
  - Create `scheduling/dto/RescheduleSessionRequestDTO.java`
  - Create `scheduling/dto/CancelSessionRequestDTO.java`
  - Create `scheduling/dto/BatchAttendanceRequestDTO.java`
  - Create `scheduling/dto/AttendanceEntryDTO.java`
  - Create `scheduling/dto/UpdateAttendanceRequestDTO.java`
  - Create `scheduling/dto/RsvpRequestDTO.java`
  - Create `scheduling/dto/ScheduleDTO.java`
  - Create `scheduling/dto/SessionDTO.java`
  - Create `scheduling/dto/SessionDetailDTO.java`
  - Create `scheduling/dto/AttendanceDTO.java`
  - Create `scheduling/dto/SessionUpdateResponseDTO.java`
  - Create `scheduling/dto/StudentAttendanceStatsDTO.java`
  - Create `scheduling/dto/ClassAttendanceStatsDTO.java`

- [x] Step 6: ScheduleService
  - Create `scheduling/service/ScheduleService.java`
  - createSchedule, createOneOffSchedule, getClassSchedules, updateSchedule (including deactivation with auto-cancel), generateSessions
  - Session generation algorithm: iterate dates matching dayOfWeek, create sessions, pre-populate attendance

- [x] Step 7: SessionService
  - Create `scheduling/service/SessionService.java`
  - getUpcomingSessions (role-based filtering), getClassSessions, getSessionDetail, rescheduleSession (with conflict warning), cancelSession

- [x] Step 8: AttendanceService
  - Create `scheduling/service/AttendanceService.java`
  - batchMarkAttendance, updateAttendance, updateRsvp (student + parent), getStudentAttendanceStats, getClassAttendanceStats

- [x] Step 9: Controllers
  - Create `scheduling/controller/ScheduleController.java` — schedule CRUD + generate-sessions endpoints
  - Create `scheduling/controller/SessionController.java` — session queries, reschedule, cancel
  - Create `scheduling/controller/AttendanceController.java` — batch/individual attendance, RSVP, stats

- [x] Step 10: Modify Existing Backend Code
  - Modify `CreateClassRequestDTO.java` — add optional schedule fields
  - Modify `ClassService.java` — createClass() to handle optional initial schedule creation
  - Modify `ClassController.java` — inject ScheduleService, pass schedule fields through

- [ ] Step 11: Backend Code Summary
  - Create `aidlc-docs/construction/class-scheduling/code/code-summary.md`

### Frontend

- [ ] Step 12: TypeScript Types
  - Modify `spm-frontend/src/types/domain.ts` — add ScheduleDTO, SessionDTO, SessionDetailDTO, AttendanceDTO, SessionUpdateResponseDTO, StudentAttendanceStatsDTO, ClassAttendanceStatsDTO, enums
  - Modify `spm-frontend/src/types/forms.ts` (if exists) or domain.ts — add CreateScheduleForm, CreateOneOffScheduleForm, etc.

- [ ] Step 13: Scheduling Service
  - Create `spm-frontend/src/services/schedulingService.ts` — all schedule, session, attendance, RSVP, stats API calls

- [ ] Step 14: Shared Components
  - Create `spm-frontend/src/components/shared/ScheduleCalendar.tsx` — monthly calendar with session dots
  - Create `spm-frontend/src/components/shared/SessionList.tsx` — sortable session table
  - Create `spm-frontend/src/components/shared/AttendanceTable.tsx` — attendance marking with RSVP indicators
  - Create `spm-frontend/src/components/shared/AttendanceStatsPanel.tsx` — attendance rate display

- [ ] Step 15: Teacher Components
  - Create `spm-frontend/src/components/teacher/ScheduleTab.tsx` — schedule management within ClassDetails
  - Create `spm-frontend/src/components/teacher/CreateScheduleForm.tsx` — recurring/one-off schedule form
  - Create `spm-frontend/src/components/teacher/GenerateSessionsModal.tsx` — target end date modal
  - Create `spm-frontend/src/components/teacher/SessionDetail.tsx` — session detail with attendance marking
  - Modify `spm-frontend/src/components/teacher/ClassDetails.tsx` — add Schedule tab
  - Modify `spm-frontend/src/components/teacher/CreateClassForm.tsx` — add accordion schedule section

- [ ] Step 16: Student Components
  - Create `spm-frontend/src/components/student/MySchedule.tsx` — upcoming sessions with RSVP toggle

- [ ] Step 17: Parent Components
  - Create `spm-frontend/src/components/parent/ChildSchedule.tsx` — child's schedule with parent RSVP

- [ ] Step 18: Admin Components
  - Create `spm-frontend/src/components/admin/ScheduleOverview.tsx` — all sessions across all classes
  - Modify `spm-frontend/src/components/admin/AdminClassDetails.tsx` — add Schedule tab
  - Modify `spm-frontend/src/components/admin/CreateClassForm.tsx` — add accordion schedule section

- [ ] Step 19: Routing & Navigation
  - Modify `spm-frontend/src/components/teacher/TeacherRoutes.tsx` — add session detail route
  - Modify `spm-frontend/src/components/student/StudentRoutes.tsx` — add schedule route
  - Modify `spm-frontend/src/components/parent/ParentRoutes.tsx` — add child schedule route
  - Modify `spm-frontend/src/components/admin/AdminRoutes.tsx` — add schedule overview + session detail routes
  - Modify `spm-frontend/src/components/shared/Sidebar.tsx` — add Schedule nav items for all roles

- [ ] Step 20: Frontend Code Summary
  - Create or update `aidlc-docs/construction/class-scheduling/code/code-summary.md`

---

## Estimated Scope
- **New backend files**: ~20 (1 migration, 3 enums, 3 entities, 3 repos, 16 DTOs, 3 services, 3 controllers)
- **Modified backend files**: 3 (CreateClassRequestDTO, ClassService, ClassController) + ErrorCode
- **New frontend files**: ~10 (1 service, 4 shared components, 4 teacher, 1 student, 1 parent, 1 admin)
- **Modified frontend files**: ~9 (domain.ts, ClassDetails x2, CreateClassForm x2, Routes x4, Sidebar)
- **Total**: ~30 new files, ~12 modified files

---

**Document Version**: 1.0
**Last Updated**: 2026-03-28
**Status**: Awaiting Approval
