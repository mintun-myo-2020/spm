# Application Design

## Overview

This document describes the high-level application architecture for the Student Progress Tracking System. The system follows a feature-based organization with layers within each feature, deployed separately per tuition centre.

---

## Architecture Principles

1. **Feature-Based Organization**: Backend organized by features (Authentication, Student Management, Progress Tracking, etc.) with layers within each feature
2. **Coarse-Grained Services**: One service per major feature area
3. **Role-Based Frontend**: React components organized by user role (Teacher, Parent, Student, Admin)
4. **RESTful API**: Resource-based endpoints following REST principles
5. **Separate Deployments**: Each tuition centre gets its own deployment with separate database
6. **JWT Authentication**: Keycloak JS Adapter for React, Spring Security for backend validation
7. **Event-Driven Notifications**: Notification events published, separate handlers process email/SMS
8. **Repository Pattern**: Repository interfaces for data access
9. **React Context API**: For frontend state management
10. **Exception Hierarchy**: Custom exceptions with global exception handler

---

## System Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Tuition Centre A Deployment              │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────┐         ┌──────────────────────────┐    │
│  │   React SPA  │────────▶│   Spring Boot Backend    │    │
│  │  (Frontend)  │  JWT    │      (REST API)          │    │
│  └──────────────┘         └──────────────────────────┘    │
│         │                            │                      │
│         │                            │                      │
│         ▼                            ▼                      │
│  ┌──────────────┐         ┌──────────────────────────┐    │
│  │   Keycloak   │         │   PostgreSQL Database    │    │
│  │ (Auth Server)│         │   (Centre A Data)        │    │
│  └──────────────┘         └──────────────────────────┘    │
│                                      │                      │
│                                      ▼                      │
│                           ┌──────────────────────────┐    │
│                           │  Notification Services   │    │
│                           │  (Email/SMS Handlers)    │    │
│                           └──────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    Tuition Centre B Deployment              │
│                    (Separate Infrastructure)                │
└─────────────────────────────────────────────────────────────┘
```

---

## Backend Architecture

### Feature Modules

The backend is organized into feature modules, each containing:
- **Controller Layer**: REST endpoints
- **Service Layer**: Business logic
- **Repository Layer**: Data access
- **Entity Layer**: JPA entities
- **DTO Layer**: Data transfer objects

#### 1. Authentication Feature
**Purpose**: Handle user authentication and authorization

**Components**:
- `AuthController`: Login/logout endpoints
- `SecurityConfig`: Spring Security 6 OAuth2 configuration
- `JwtAuthenticationFilter`: JWT validation filter
- `OAuth2ResourceServerConfig`: OAuth2 resource server configuration
- `RoleService`: Role-based access control

**Key Methods**:
- `login()`: Redirect to Keycloak (handled by Spring Security OAuth2)
- `handleCallback()`: Process OAuth callback (handled by Spring Security OAuth2)
- `validateToken()`: Validate JWT (Spring Security OAuth2 JWT decoder)
- `getUserRoles()`: Extract roles from JWT claims
- `logout()`: Invalidate session

**Technology**:
- **Spring Security 6** with OAuth2 Resource Server support
- **spring-boot-starter-oauth2-resource-server** dependency
- **spring-boot-starter-oauth2-client** for OAuth2 login flow
- NO deprecated Keycloak adapters - using generic OIDC/OAuth2 libraries

#### 2. User Management Feature
**Purpose**: Manage teachers, parents, students, and administrators

**Components**:
- `UserController`: User CRUD endpoints
- `UserService`: User management logic
- `UserRepository`: User data access
- `User` entity: Base user entity
- `Teacher`, `Parent`, `Student`, `Admin` entities: Role-specific entities

**Key Methods**:
- `createUser()`: Create new user
- `updateUser()`: Update user details
- `getUserById()`: Retrieve user
- `getUsersByRole()`: Get users by role
- `deactivateUser()`: Deactivate user account

#### 3. Student Management Feature
**Purpose**: Manage student profiles and enrollments

**Components**:
- `StudentController`: Student CRUD endpoints
- `StudentService`: Student management logic
- `StudentRepository`: Student data access
- `Student` entity: Student profile
- `StudentProfile` entity: Extended profile information

**Key Methods**:
- `createStudent()`: Create student profile
- `updateStudent()`: Update student details
- `getStudentById()`: Retrieve student
- `getStudentsByClass()`: Get students in class
- `enrollStudent()`: Enroll in class

#### 4. Class Management Feature
**Purpose**: Manage classes and enrollments

**Components**:
- `ClassController`: Class CRUD endpoints
- `ClassService`: Class management logic
- `ClassRepository`: Class data access
- `EnrollmentRepository`: Enrollment data access
- `Class` entity: Class information
- `Enrollment` entity: Student-class relationship

**Key Methods**:
- `createClass()`: Create new class
- `updateClass()`: Update class details
- `getClassById()`: Retrieve class
- `getClassesByTeacher()`: Get teacher's classes
- `enrollStudents()`: Enroll students in class
- `removeStudent()`: Remove student from class

#### 5. Subject & Topic Management Feature
**Purpose**: Manage subjects and topics

**Components**:
- `SubjectController`: Subject CRUD endpoints
- `SubjectService`: Subject management logic
- `SubjectRepository`: Subject data access
- `TopicRepository`: Topic data access
- `Subject` entity: Subject information
- `Topic` entity: Topic under subject

**Key Methods**:
- `createSubject()`: Create custom subject
- `updateSubject()`: Update subject
- `getDefaultSubjects()`: Get default subjects
- `getSubjectsByCentre()`: Get centre subjects
- `createTopic()`: Create topic under subject
- `getTopicsBySubject()`: Get topics for subject

#### 6. Test Score Management Feature
**Purpose**: Record and manage test scores

**Components**:
- `TestScoreController`: Test score endpoints
- `TestScoreService`: Score management logic
- `TestScoreRepository`: Score data access
- `TopicScoreRepository`: Topic score data access
- `TestScore` entity: Overall test score
- `TopicScore` entity: Topic-level score

**Key Methods**:
- `recordTestScore()`: Record new test score
- `updateTestScore()`: Update test score
- `getTestScoreById()`: Retrieve test score
- `getTestScoresByStudent()`: Get student's scores
- `recordTopicScores()`: Record topic-level scores
- `getTopicScoresByTest()`: Get topic scores for test

#### 7. Progress Tracking Feature
**Purpose**: Calculate and provide progress data

**Components**:
- `ProgressController`: Progress data endpoints
- `ProgressService`: Progress calculation logic
- `ChartDataService`: Chart data generation
- `ProgressRepository`: Progress data access

**Key Methods**:
- `getStudentProgress()`: Get overall progress
- `getScoreTrends()`: Calculate score trends
- `getTopicPerformance()`: Get topic-level performance
- `generateChartData()`: Generate chart data points
- `getHistoricalData()`: Get historical performance

#### 8. Feedback Management Feature
**Purpose**: Manage teacher feedback

**Components**:
- `FeedbackController`: Feedback endpoints
- `FeedbackService`: Feedback management logic
- `FeedbackRepository`: Feedback data access
- `Feedback` entity: Teacher feedback

**Key Methods**:
- `addFeedback()`: Add teacher feedback
- `updateFeedback()`: Update feedback
- `getFeedbackById()`: Retrieve feedback
- `getFeedbackByStudent()`: Get student's feedback
- `getFeedbackByTest()`: Get feedback for test

#### 9. Reporting Feature
**Purpose**: Generate progress reports

**Components**:
- `ReportController`: Report generation endpoints
- `ReportService`: Report generation logic
- `ReportDataAggregator`: Aggregate report data

**Key Methods**:
- `generateProgressReport()`: Generate basic report
- `getReportData()`: Aggregate report data
- `formatReport()`: Format report output

#### 10. Notification Feature
**Purpose**: Handle email and SMS notifications

**Components**:
- `NotificationController`: Notification preference endpoints
- `NotificationService`: Notification orchestration
- `NotificationEventPublisher`: Publish notification events
- `EmailNotificationHandler`: Handle email notifications
- `SMSNotificationHandler`: Handle SMS notifications
- `NotificationPreferenceRepository`: Preference data access

**Key Methods**:
- `publishNotificationEvent()`: Publish event
- `handleEmailNotification()`: Process email
- `handleSMSNotification()`: Process SMS
- `updatePreferences()`: Update user preferences
- `getPreferences()`: Get user preferences

---

## Frontend Architecture

### Role-Based Component Organization

```
src/
├── components/
│   ├── teacher/
│   │   ├── TeacherDashboard.tsx
│   │   ├── ClassList.tsx
│   │   ├── StudentList.tsx
│   │   ├── ScoreEntryForm.tsx
│   │   └── ProgressChartView.tsx
│   ├── parent/
│   │   ├── ParentDashboard.tsx
│   │   ├── ChildSelector.tsx
│   │   ├── ProgressOverview.tsx
│   │   ├── TestScoreHistory.tsx
│   │   └── NotificationPreferences.tsx
│   ├── student/
│   │   ├── StudentDashboard.tsx
│   │   ├── PersonalProgress.tsx
│   │   ├── TestScoreHistory.tsx
│   │   └── FeedbackView.tsx
│   ├── admin/
│   │   ├── AdminDashboard.tsx
│   │   ├── TeacherManagement.tsx
│   │   ├── StudentManagement.tsx
│   │   ├── ClassManagement.tsx
│   │   └── SubjectManagement.tsx
│   └── shared/
│       ├── LineChart.tsx
│       ├── FormInput.tsx
│       ├── Navigation.tsx
│       └── Layout.tsx
├── auth/
│   ├── Login.tsx
│   ├── ProtectedRoute.tsx
│   └── KeycloakProvider.tsx
├── services/
│   ├── api.ts
│   └── keycloak.ts
├── types/
│   ├── user.types.ts
│   ├── student.types.ts
│   ├── class.types.ts
│   ├── score.types.ts
│   └── api.types.ts
└── context/
    └── AppContext.tsx
```

### Key Frontend Components

**Note**: All frontend components use TypeScript (.tsx files) for type safety.

#### Authentication Components
- **Login**: Keycloak login redirect
- **ProtectedRoute**: Route guard based on roles
- **KeycloakProvider**: Keycloak JS adapter wrapper

#### Teacher Components
- **TeacherDashboard**: Main teacher view
- **ClassList**: List of teacher's classes
- **StudentList**: Students in selected class
- **ScoreEntryForm**: Form to record test scores
- **ProgressChartView**: View student progress charts

#### Parent Components
- **ParentDashboard**: Main parent view
- **ChildSelector**: Select which child to view
- **ProgressOverview**: Child's progress summary
- **TestScoreHistory**: List of test scores
- **NotificationPreferences**: Configure notifications

#### Student Components
- **StudentDashboard**: Main student view
- **PersonalProgress**: Own progress charts
- **TestScoreHistory**: Own test scores
- **FeedbackView**: Teacher feedback

#### Admin Components
- **AdminDashboard**: Main admin view
- **TeacherManagement**: Manage teachers
- **StudentManagement**: Manage students
- **ClassManagement**: Manage classes
- **SubjectManagement**: Manage subjects/topics

#### Shared Components
- **LineChart**: Reusable chart component
- **FormInput**: Reusable form inputs
- **Navigation**: Navigation bar
- **Layout**: Page layout wrapper

---

## API Design

### RESTful Endpoints

#### Authentication
- `POST /api/auth/login` - Initiate login
- `GET /api/auth/callback` - OAuth callback
- `POST /api/auth/logout` - Logout

#### Users
- `GET /api/users` - List users
- `POST /api/users` - Create user
- `GET /api/users/{id}` - Get user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Deactivate user

#### Students
- `GET /api/students` - List students
- `POST /api/students` - Create student
- `GET /api/students/{id}` - Get student
- `PUT /api/students/{id}` - Update student
- `GET /api/students/{id}/classes` - Get student's classes

#### Classes
- `GET /api/classes` - List classes
- `POST /api/classes` - Create class
- `GET /api/classes/{id}` - Get class
- `PUT /api/classes/{id}` - Update class
- `POST /api/classes/{id}/enroll` - Enroll students
- `DELETE /api/classes/{id}/students/{studentId}` - Remove student

#### Subjects & Topics
- `GET /api/subjects` - List subjects
- `POST /api/subjects` - Create subject
- `GET /api/subjects/{id}` - Get subject
- `PUT /api/subjects/{id}` - Update subject
- `GET /api/subjects/{id}/topics` - Get topics
- `POST /api/subjects/{id}/topics` - Create topic

#### Test Scores
- `POST /api/test-scores` - Record test score
- `GET /api/test-scores/{id}` - Get test score
- `PUT /api/test-scores/{id}` - Update test score
- `GET /api/students/{id}/test-scores` - Get student's scores
- `POST /api/test-scores/{id}/topic-scores` - Record topic scores

#### Progress
- `GET /api/students/{id}/progress` - Get student progress
- `GET /api/students/{id}/progress/trends` - Get score trends
- `GET /api/students/{id}/progress/topics` - Get topic performance
- `GET /api/students/{id}/progress/chart-data` - Get chart data

#### Feedback
- `POST /api/feedback` - Add feedback
- `GET /api/feedback/{id}` - Get feedback
- `PUT /api/feedback/{id}` - Update feedback
- `GET /api/students/{id}/feedback` - Get student's feedback

#### Reports
- `GET /api/students/{id}/reports/progress` - Generate progress report

#### Notifications
- `GET /api/notifications/preferences` - Get preferences
- `PUT /api/notifications/preferences` - Update preferences

---

## Data Flow

### Test Score Recording Flow

```
1. Teacher → ScoreEntryForm (React TypeScript)
2. ScoreEntryForm → POST /api/test-scores (REST API)
3. TestScoreController → TestScoreService
4. TestScoreService → TestScoreRepository (save score)
5. TestScoreService → NotificationEventPublisher (publish event)
6. NotificationEventPublisher → Event Bus
7. EmailNotificationHandler → Send email to parent
8. SMSNotificationHandler → Send SMS to parent
9. TestScoreController → Return success response
10. ScoreEntryForm → Show confirmation
```

### Progress Viewing Flow

```
1. Parent → ProgressOverview (React TypeScript)
2. ProgressOverview → GET /api/students/{id}/progress (REST API)
3. ProgressController → ProgressService
4. ProgressService → TestScoreRepository (fetch scores)
5. ProgressService → Calculate trends and metrics
6. ProgressController → Return progress data
7. ProgressOverview → LineChart (render chart)
```

---

## Component Dependencies

### Backend Dependencies

```
SecurityConfig (Spring Security 6)
  └─> OAuth2ResourceServerConfig
  └─> JwtAuthenticationFilter

UserService
  └─> UserRepository
  └─> RoleService

StudentService
  └─> StudentRepository
  └─> UserService

ClassService
  └─> ClassRepository
  └─> EnrollmentRepository
  └─> StudentService

TestScoreService
  └─> TestScoreRepository
  └─> TopicScoreRepository
  └─> StudentService
  └─> NotificationEventPublisher

ProgressService
  └─> TestScoreRepository
  └─> ChartDataService

FeedbackService
  └─> FeedbackRepository
  └─> TestScoreService

NotificationService
  └─> NotificationEventPublisher
  └─> NotificationPreferenceRepository

EmailNotificationHandler
  └─> Email Service (AWS SES or similar)

SMSNotificationHandler
  └─> SMS Service (AWS SNS or similar)
```

### Frontend Dependencies

```
All Role Components
  └─> AppContext (state management)
  └─> api.ts (API calls with TypeScript types)
  └─> keycloak.ts (authentication)

ProtectedRoute
  └─> KeycloakProvider (auth check)

All Dashboard Components
  └─> Navigation (shared)
  └─> Layout (shared)

Chart Components
  └─> LineChart (shared)

Form Components
  └─> FormInput (shared)
```

---

## Integration Points

### Keycloak Integration
- **Frontend**: `@react-keycloak/web` or `keycloak-js` library (actively maintained)
- **Backend**: **Spring Security 6 OAuth2 Resource Server** (generic OIDC/OAuth2 support)
  - Uses `spring-boot-starter-oauth2-resource-server` for JWT validation
  - Uses `spring-boot-starter-oauth2-client` for OAuth2 login flow
  - **NO deprecated Keycloak adapters** - follows best practices with generic OAuth2 libraries
- **Flow**: OAuth2 authorization code flow with JWT
- **Token**: JWT stored in memory, included in API calls
- **Configuration**: Keycloak configured as standard OIDC provider in application.yml

### Notification Integration
- **Email**: AWS SES or similar email service
- **SMS**: AWS SNS or similar SMS service
- **Pattern**: Event-driven, asynchronous processing

### Database Integration
- **ORM**: JPA/Hibernate
- **Pattern**: Repository pattern with Spring Data JPA
- **Database**: PostgreSQL per centre deployment

---

## Deployment Architecture

### Per-Centre Deployment

Each tuition centre gets:
1. **Separate AWS Account/VPC** (or isolated resources)
2. **Separate PostgreSQL Database**
3. **Separate Spring Boot Application Instance**
4. **Separate React Frontend Deployment**
5. **Shared Keycloak Instance** (or separate if required)

**Benefits**:
- Complete data isolation
- Independent scaling per centre
- Isolated failures
- Centre-specific customization possible

---

## Security Considerations

1. **Authentication**: Keycloak OAuth2 with JWT
2. **Authorization**: Role-based access control enforced in backend
3. **Data Isolation**: Separate databases per centre
4. **API Security**: JWT validation on all endpoints
5. **HTTPS**: Required for all communications
6. **Token Storage**: In-memory storage in frontend
7. **CORS**: Configured to allow only frontend origin

---

## Next Steps

This application design will be used to:
1. Generate development units in Units Generation stage
2. Create detailed functional designs per unit
3. Define NFR requirements and designs per unit
4. Plan infrastructure per unit
5. Generate code per unit

---

**Document Version**: 1.0  
**Created**: 2026-03-08  
**Status**: Draft - Pending Approval
