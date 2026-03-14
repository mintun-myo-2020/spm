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
- [ ] Create Gradle project with Kotlin DSL (`build.gradle.kts`)
- [ ] Create `settings.gradle.kts`
- [ ] Create main application class `SpmApplication.java`
- [ ] Create `application.yml` with profiles (dev, prod)
- [ ] Create `application-dev.yml` for local development
- [ ] Create `.env.example` for environment variables
- [ ] Create `Dockerfile` (multi-stage build)
- [ ] Create `.dockerignore`

### Step 2: Common Module - Shared Infrastructure
- [ ] Create common exception classes (`NotFoundException`, `ConflictException`, `ForbiddenException`, `BadRequestException`, `ServiceUnavailableException`)
- [ ] Create `GlobalExceptionHandler` with `@RestControllerAdvice`
- [ ] Create common DTOs (`ErrorResponseDTO`, `PagedResponseDTO`, `ApiResponseDTO`)
- [ ] Create base entity class `BaseEntity` with id, createdAt, updatedAt
- [ ] Create audit entity class `AuditableEntity` extending BaseEntity with createdBy, updatedBy

### Step 3: Auth Module - Security Configuration
- [ ] Create `SecurityConfig` (Spring Security 6 filter chain, OAuth2 Resource Server, JWT)
- [ ] Create `JwtAuthenticationConverter` (extract roles from Keycloak JWT)
- [ ] Create `AuthorizationService` (canAccessStudent, canAccessClass, ownership checks)
- [ ] Create `CurrentUserService` (extract current user from SecurityContext)
- [ ] Create CORS configuration

### Step 4: Database Migrations (Flyway)
- [ ] Create `V1__initial_schema.sql` - Users, UserRole, Teacher, Parent, Student, Admin tables
- [ ] Create `V2__academic_structure.sql` - Subject, Topic tables
- [ ] Create `V3__class_management.sql` - Class, ClassStudent, TeacherClassHistory tables
- [ ] Create `V4__assessment.sql` - TestScore, Question, SubQuestion, SubQuestionTopic tables
- [ ] Create `V5__feedback.sql` - Feedback, FeedbackTemplate tables
- [ ] Create `V6__notifications.sql` - Notification table
- [ ] Create `V7__reports.sql` - ProgressReport table
- [ ] Create `V8__indexes.sql` - Performance indexes
- [ ] Create `V9__default_subjects.sql` - Default subjects and topics seed data

### Step 5: Domain Entities (JPA)
- [ ] Create `User` entity
- [ ] Create `UserRole` entity with `Role` enum
- [ ] Create `Teacher` entity
- [ ] Create `Parent` entity with `ContactMethod` enum
- [ ] Create `Student` entity
- [ ] Create `Admin` entity
- [ ] Create `Subject` entity
- [ ] Create `Topic` entity
- [ ] Create `Class` entity (mapped as `TuitionClass` to avoid Java keyword)
- [ ] Create `ClassStudent` entity with `EnrollmentStatus` enum
- [ ] Create `TeacherClassHistory` entity
- [ ] Create `TestScore` entity
- [ ] Create `Question` entity
- [ ] Create `SubQuestion` entity
- [ ] Create `SubQuestionTopic` entity
- [ ] Create `Feedback` entity
- [ ] Create `FeedbackTemplate` entity with `FeedbackCategory` enum
- [ ] Create `Notification` entity with `NotificationType`, `NotificationChannel`, `NotificationStatus` enums
- [ ] Create `ProgressReport` entity with `ReportType`, `ReportFormat` enums

### Step 6: User Management Module
- [ ] Create `UserRepository`, `TeacherRepository`, `ParentRepository`, `StudentRepository`, `AdminRepository`, `UserRoleRepository`
- [ ] Create user DTOs: `UserInfoDTO`, `CreateTeacherRequestDTO`, `TeacherDTO`, `CreateParentRequestDTO`, `ParentDTO`, `CreateStudentRequestDTO`, `StudentDTO`, `UserDTO`
- [ ] Create `UserService` (create teacher/parent/student, deactivate, reactivate, get user info)
- [ ] Create `UserController` (REST endpoints for user management)
- [ ] Create `AuthController` (GET /auth/me endpoint)

### Step 7: Subject & Topic Management Module
- [ ] Create `SubjectRepository`, `TopicRepository`
- [ ] Create subject DTOs: `SubjectDTO`, `SubjectDetailDTO`, `CreateSubjectRequestDTO`, `TopicDTO`, `CreateTopicRequestDTO`
- [ ] Create `SubjectService` (CRUD, list with topics, deactivate)
- [ ] Create `SubjectController` (REST endpoints)
- [ ] Create `DefaultSubjectConfig` (load default subjects from application.yml)

### Step 8: Class Management Module
- [ ] Create `ClassRepository` (TuitionClassRepository), `ClassStudentRepository`, `TeacherClassHistoryRepository`
- [ ] Create class DTOs: `ClassDTO`, `ClassDetailDTO`, `CreateClassRequestDTO`, `EnrollmentDTO`, `EnrollStudentRequestDTO`, `ChangeTeacherRequestDTO`
- [ ] Create `ClassService` (create class, enroll/withdraw student, change teacher, get teacher's classes)
- [ ] Create `ClassController` (REST endpoints)

### Step 9: Test Score Management Module
- [ ] Create `TestScoreRepository`, `QuestionRepository`, `SubQuestionRepository`, `SubQuestionTopicRepository`
- [ ] Create test score DTOs: `CreateTestScoreRequestDTO`, `TestScoreDTO`, `TestScoreDetailDTO`, `QuestionDTO`, `SubQuestionDTO`, `UpdateTestScoreRequestDTO`
- [ ] Create `TestScoreService` (create with topic breakdown, update, delete, list by student with filters)
- [ ] Create `TestScoreController` (REST endpoints)

### Step 10: Progress Tracking Module
- [ ] Create progress DTOs: `OverallProgressDTO`, `TopicProgressDTO`, `TopicProgressSummaryDTO`, `ImprovementVelocityDTO`, `TrendDataPointDTO`
- [ ] Create `ProgressService` (overall trend, topic trend, all topics summary, improvement velocity calculation)
- [ ] Create `ProgressController` (REST endpoints)

### Step 11: Feedback Management Module
- [ ] Create `FeedbackRepository`, `FeedbackTemplateRepository`
- [ ] Create feedback DTOs: `FeedbackDTO`, `CreateFeedbackRequestDTO`, `UpdateFeedbackRequestDTO`, `FeedbackTemplateDTO`, `CreateFeedbackTemplateRequestDTO`
- [ ] Create `FeedbackService` (create, update, get templates, create template)
- [ ] Create `FeedbackController` (REST endpoints)

### Step 12: Notification Module
- [ ] Create `NotificationRepository`
- [ ] Create notification DTOs: `NotificationDTO`, `NotificationPreferencesDTO`, `UpdateNotificationPreferencesRequestDTO`
- [ ] Create `NotificationEvent` classes (NewTestScoreEvent, TestScoreUpdatedEvent, NewFeedbackEvent, FeedbackUpdatedEvent)
- [ ] Create `NotificationEventPublisher` (Spring Events)
- [ ] Create `NotificationEventHandler` (@EventListener, creates Notification records)
- [ ] Create `EmailService` (AWS SES integration, with Resilience4j retry/circuit breaker)
- [ ] Create `SmsService` (AWS SNS integration, with Resilience4j retry/circuit breaker)
- [ ] Create `NotificationSender` (async processing of pending notifications)
- [ ] Create `NotificationController` (get my notifications, update preferences)

### Step 13: Report Generation Module
- [ ] Create `ProgressReportRepository`
- [ ] Create report DTOs: `GenerateReportRequestDTO`, `ProgressReportDTO`
- [ ] Create `ReportGenerationService` (generate HTML report, upload to S3, create record)
- [ ] Create `S3StorageService` (upload, generate pre-signed URL)
- [ ] Create `ReportController` (generate report, get report, list student reports)

### Step 14: Bulk Operations
- [ ] Create `BulkOperationResultDTO`
- [ ] Create `CsvParserService` (parse student CSV, enrollment CSV)
- [ ] Create bulk endpoints in `UserController` (POST /users/students/bulk)
- [ ] Create bulk endpoints in `ClassController` (POST /classes/enrollments/bulk)

### Step 15: Cross-Cutting Concerns
- [ ] Create `CacheConfig` (Caffeine cache manager with named caches)
- [ ] Create `AsyncConfig` (thread pools for notifications, reports)
- [ ] Create `Resilience4jConfig` (circuit breaker, retry for external services)
- [ ] Create `WebConfig` (CORS, request logging)
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
- Code is written to `backend/` directory (never `aidlc-docs/`)
- All entities use UUID primary keys
- All DTOs use Java records where possible
- Bean Validation annotations on all request DTOs
- `data-testid` attributes not applicable (backend API only)
- Greenfield multi-unit structure: `backend/` and `frontend/` directories

## Total Estimated Files: ~120+ Java files, ~9 SQL migrations, build configs, Docker, README
