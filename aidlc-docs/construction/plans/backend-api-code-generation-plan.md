# Code Generation Plan - Backend API (UNIT-01)

## Unit Context
- **Unit**: Backend API (Spring Boot 4.x Modular Monolith)
- **Technology**: Java 25, Spring Boot 4.x, Spring Security 6, PostgreSQL 18, Gradle (Kotlin DSL)
- **Code Location**: `spm/` directory in workspace root
- **Package Base**: `com.eggtive.spm`
- **Deployment**: Single Spring Boot application with feature-based modules

## Stories Implemented
AUTH-1, AUTH-2, AUTH-3, AUTH-4, TEACH-1 through TEACH-6, PARENT-1 through PARENT-7,
STUDENT-1 through STUDENT-3, ADMIN-1 through ADMIN-6, DATA-1, REPORT-1

## Dependencies
- Keycloak (external, OAuth2/OIDC provider)
- PostgreSQL 18 (external, database)
- AWS SES (external, email)
- AWS SNS (external, SMS)
- AWS S3 (external, report storage)

---

## Code Generation Steps

### Step 1: Project Structure Setup
- [x] Create Gradle project with Kotlin DSL (`build.gradle.kts`) — user set up via Spring Initializr
- [x] Create `settings.gradle.kts` — created by Spring Initializr
- [x] Create main application class `SpmApplication.java` — created by Spring Initializr
- [x] Create `application.yml` with profiles (dev, prod) — created with env var defaults
- [ ] Create `application-dev.yml` for local development — SKIPPED (env var defaults in application.yml suffice)
- [ ] Create `.env.example` for environment variables — DEFERRED
- [ ] Create `Dockerfile` (multi-stage build) — DEFERRED
- [ ] Create `.dockerignore` — DEFERRED

### Step 2: Common Module - Shared Infrastructure
- [x] Create common exception classes — simplified to single `AppException` using `ErrorCode` enum
- [x] Create `GlobalExceptionHandler` with `@RestControllerAdvice`
- [x] Create common DTOs (`ErrorResponse`, `PagedResponse`, `ApiResponse`) — using Java records
- [x] Create base entity class `BaseEntity` with id, createdAt, updatedAt
- [ ] Create audit entity class `AuditableEntity` extending BaseEntity with createdBy, updatedBy — SKIPPED (audit fields handled per-entity where needed)

### Step 3: Auth Module - Security Configuration
- [x] Create `SecurityConfig` (Spring Security 6 filter chain, OAuth2 Resource Server, JWT, CORS)
- [x] Create `RoleConverter` interface + `KeycloakRoleConverter` impl — decoupled from vendor
- [x] Create `CurrentUserService` (extract current user from SecurityContext via JWT sub claim)
- [x] Create `AuthController` (GET /auth/me)
- [ ] Create `AuthorizationService` (canAccessStudent, canAccessClass, ownership checks) — DEFERRED

### Step 4: Database Migrations (Flyway)
- [x] Create `V1__initial_schema.sql` — consolidated ALL tables + indexes into single migration (users, roles, teachers, parents, students, admins, subjects, topics, classes, class_students, test_scores, questions, sub_questions, feedback, feedback_templates, notifications, progress_reports, indexes)

### Step 5: Domain Entities (JPA)
- [x] Create `User` entity (with `@ElementCollection` for roles — no separate UserRole entity needed)
- [x] Create `Teacher` entity
- [x] Create `Parent` entity with `ContactMethod` enum
- [x] Create `Student` entity
- [x] Create `Admin` entity
- [x] Create `Subject` entity
- [x] Create `Topic` entity
- [x] Create `TuitionClass` entity (mapped to `classes` table)
- [x] Create `ClassStudent` entity with `EnrollmentStatus` enum
- [x] Create `TestScore` entity
- [x] Create `Question` entity
- [x] Create `SubQuestion` entity (topic linked directly via `@ManyToOne` — no separate SubQuestionTopic join entity)
- [x] Create `Feedback` entity
- [x] Create `FeedbackTemplate` entity with `FeedbackCategory` enum
- [x] Create `Notification` entity with `NotificationType`, `NotificationChannel`, `NotificationStatus` enums
- [x] Create `ProgressReport` entity
- [ ] Create `TeacherClassHistory` entity — DEFERRED (not core MVP)

### Step 6: User Management Module
- [x] Create `UserRepository`, `TeacherRepository`, `ParentRepository`, `StudentRepository`
- [x] Create user DTOs: `UserInfoDTO`, `CreateTeacherRequestDTO`, `TeacherDTO`, `CreateParentRequestDTO`, `ParentDTO`, `CreateStudentRequestDTO`, `StudentDTO`
- [x] Create `UserService` (create teacher/parent/student, deactivate, reactivate, get user info)
- [x] Create `UserController` (REST endpoints for user management)
- [x] Create `AuthController` (GET /auth/me endpoint) — in auth package

### Step 7: Subject & Topic Management Module
- [x] Create `SubjectRepository`, `TopicRepository`
- [x] Create subject DTOs: `SubjectDTO`, `SubjectDetailDTO`, `CreateSubjectRequestDTO`, `TopicDTO`, `CreateTopicRequestDTO`
- [x] Create `SubjectService` (CRUD, list with topics, deactivate)
- [x] Create `SubjectController` (REST endpoints)
- [ ] Create `DefaultSubjectConfig` (load default subjects from application.yml) — DEFERRED

### Step 8: Class Management Module
- [x] Create `TuitionClassRepository`, `ClassStudentRepository`
- [x] Create class DTOs: `ClassDTO`, `CreateClassRequestDTO`, `EnrollmentDTO`, `EnrollStudentRequestDTO`
- [x] Create `ClassService` (create class, enroll/withdraw student, change teacher, get teacher's classes)
- [x] Create `ClassController` (REST endpoints)

### Step 9: Test Score Management Module
- [x] Create `TestScoreRepository`
- [x] Create test score DTOs: `CreateTestScoreRequestDTO`, `TestScoreDTO` (with nested QuestionDTO, SubQuestionDTO)
- [x] Create `TestScoreService` (create with topic breakdown, delete, list by student with filters)
- [x] Create `TestScoreController` (REST endpoints)

### Step 10: Progress Tracking Module
- [x] Create progress DTOs: `OverallProgressDTO`, `TopicProgressSummaryDTO`, `ImprovementVelocityDTO`, `TrendDataPointDTO`
- [x] Create `ProgressCalculator` interface + `SimpleProgressCalculator` impl
- [x] Create `ProgressService` (overall trend, topic trend, all topics summary, improvement velocity calculation)
- [x] Create `ProgressController` (REST endpoints)

### Step 11: Feedback Management Module
- [x] Create `FeedbackRepository`, `FeedbackTemplateRepository`
- [x] Create feedback DTOs: `FeedbackDTO`, `CreateFeedbackRequestDTO`, `FeedbackTemplateDTO`, `CreateFeedbackTemplateRequestDTO`
- [x] Create `FeedbackService` (create, update, get templates, create template)
- [x] Create `FeedbackController` (REST endpoints)

### Step 12: Notification Module
- [x] Create `NotificationRepository`
- [x] Create notification DTOs: `NotificationDTO`
- [ ] Create notification DTOs: `NotificationPreferencesDTO`, `UpdateNotificationPreferencesRequestDTO`
- [ ] Create `NotificationEvent` classes (NewTestScoreEvent, NewFeedbackEvent)
- [ ] Create `NotificationEventHandler` (@EventListener, creates Notification records)
- [ ] Create `EmailService` (AWS SES integration — no Resilience4j, simple try/catch for MVP)
- [ ] Create `SmsService` (AWS SNS integration — no Resilience4j, simple try/catch for MVP)
- [ ] Create `NotificationSender` (async processing of pending notifications)
- [x] Create `NotificationService` (basic CRUD)
- [x] Create `NotificationController` (get my notifications, update preferences)

### Step 13: Report Generation Module
- [x] Create `ProgressReportRepository`
- [x] Create report DTOs: `GenerateReportRequestDTO`, `ProgressReportDTO`
- [x] Create `ReportService` (generate report, create record — uses `ReportStorage` interface)
- [x] Create `ReportStorage` interface + `StubReportStorage` impl (S3 deferred)
- [x] Create `ReportController` (generate report, get report, list student reports)

### Step 14: Bulk Operations
- [ ] Create `BulkOperationResultDTO`
- [ ] Create `CsvParserService` (parse student CSV, enrollment CSV)
- [ ] Create bulk endpoints in `UserController` (POST /users/students/bulk)
- [ ] Create bulk endpoints in `ClassController` (POST /classes/enrollments/bulk)

### Step 15: Cross-Cutting Concerns
- [ ] Create `CacheConfig` (Caffeine cache manager with named caches)
- [ ] Create `AsyncConfig` (thread pools for notifications, reports)
- [ ] ~~Create `Resilience4jConfig`~~ — REMOVED (compatibility concerns with Spring Boot 4)
- [ ] Create `WebConfig` (request logging)
- [ ] Create `AuditConfig` (JPA auditing with createdBy/updatedBy)

### Step 16: Unit Tests - Core Services
- [ ] Create `UserServiceTest` (create teacher/parent/student, deactivate, reactivate)
- [ ] Create `TestScoreServiceTest` (create, update, delete, validation rules)
- [ ] Create `ProgressServiceTest` (overall trend, topic trend, velocity calculation)
- [ ] Create `ClassServiceTest` (create, enroll, withdraw, change teacher)
- [ ] Create `FeedbackServiceTest` (create, update, templates)
- [ ] Create `SubjectServiceTest` (CRUD, defaults)
- [ ] Create `AuthorizationServiceTest` (ownership checks for all roles)

### Step 17: Unit Tests - Controllers
- [ ] Create `UserControllerTest` (MockMvc tests for user endpoints)
- [ ] Create `TestScoreControllerTest` (MockMvc tests for score endpoints)
- [ ] Create `ProgressControllerTest` (MockMvc tests for progress endpoints)
- [ ] Create `ClassControllerTest` (MockMvc tests for class endpoints)
- [ ] Create `FeedbackControllerTest` (MockMvc tests for feedback endpoints)
- [ ] Create `SubjectControllerTest` (MockMvc tests for subject endpoints)

### Step 18: Documentation & Deployment
- [ ] Create `backend/README.md` with setup instructions
- [ ] Create code summary in `aidlc-docs/construction/backend-api/code/code-summary.md`

---

## Execution Notes
- Steps are executed sequentially (1 → 18)
- Each step is marked [x] immediately upon completion
- Code is written to `spm/` directory (never `aidlc-docs/`)
- All entities use UUID primary keys
- All DTOs use Java records where possible
- Bean Validation annotations on all request DTOs
- Auth layer decoupled via `RoleConverter` interface (Keycloak impl swappable)
- Resilience4j removed — Spring Boot 4 compatibility uncertain, not needed for MVP
- Docker Compose set up for local Postgres + app container (via `bootBuildImage`, no Dockerfile)
- Flyway migrations consolidated into single V1 file

## Completion Summary
- **Steps 1-5**: DONE (project setup, common module, auth, migrations, entities)
- **Steps 6-9**: DONE (user, subject, class, test score — repos, DTOs, services, controllers)
- **Step 10**: DONE (progress tracking — calculator interface, service, controller, DTOs)
- **Step 11**: DONE (feedback)
- **Step 12**: PARTIAL (notification — repo, DTO, service, controller done; event classes, email/SMS services, notification sender still TODO)
- **Step 13**: DONE (report generation — repo, DTOs, service, storage interface + stub, controller)
- **Steps 14-15**: TODO (bulk ops, cross-cutting)
- **Steps 16-18**: TODO (tests, docs)
- **Estimated completion**: ~80% of core backend code done
