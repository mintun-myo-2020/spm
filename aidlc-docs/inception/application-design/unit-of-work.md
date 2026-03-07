# Unit of Work Breakdown

## Overview

The Student Progress Tracking System is decomposed into 2 development units following a modular monolith architecture with clear separation between backend and frontend.

**Deployment Model**: Modular monolith with separate backend and frontend deployments
**Total Units**: 2
**MVP Approach**: Prioritize core tracking features first

---

## Unit 1: Backend API (Spring Boot)

### Unit Information
- **Unit ID**: UNIT-01
- **Unit Name**: Backend API
- **Technology**: Java 25, Spring Boot 4.x, Spring Security 6, PostgreSQL 18
- **Deployment**: Single Spring Boot application (modular monolith)
- **Database**: PostgreSQL (single shared database)

### Responsibilities

This unit handles all server-side logic, data persistence, business rules, and API endpoints.

**Core Responsibilities**:
1. User authentication and authorization (OAuth2/OIDC with Keycloak)
2. User management (Teachers, Parents, Students, Administrators)
3. Student profile and class management
4. Subject and topic management
5. Test score recording and storage
6. Progress calculation and data aggregation
7. Teacher feedback management
8. Report generation
9. Notification orchestration (email/SMS)
10. Multi-tenant data isolation
11. RESTful API exposure

### Module Structure

The backend follows a modular monolith pattern with feature-based modules:

#### Shared Modules
- **auth/** - Authentication and authorization (Spring Security 6 OAuth2)
- **notification/** - Notification event publishing and handling
- **common/** - Shared utilities, exceptions, DTOs, configurations

#### Feature Modules
- **user-management/** - User CRUD operations
- **student-management/** - Student profiles and enrollment
- **class-management/** - Class creation and management
- **subject-management/** - Subjects and topics
- **test-score-management/** - Test score recording
- **progress-tracking/** - Progress calculation and chart data
- **feedback-management/** - Teacher feedback
- **reporting/** - Report generation

### Technology Stack

**Framework & Libraries**:
- Spring Boot 4.x
- Spring Security 6 with OAuth2 Resource Server
- Spring Data JPA
- Spring Web (REST)
- Spring Events (for notifications)

**Database**:
- PostgreSQL 18
- Flyway or Liquibase for migrations

**Authentication**:
- spring-boot-starter-oauth2-resource-server
- spring-boot-starter-oauth2-client
- Keycloak as OIDC provider

**Build Tool**:
- Maven or Gradle

**Testing**:
- JUnit 5
- Mockito
- Spring Boot Test
- Testcontainers (for integration tests)

### API Endpoints

**Authentication**: `/api/auth/*`
**Users**: `/api/users/*`
**Students**: `/api/students/*`
**Classes**: `/api/classes/*`
**Subjects**: `/api/subjects/*`
**Test Scores**: `/api/test-scores/*`
**Progress**: `/api/students/{id}/progress/*`
**Feedback**: `/api/feedback/*`
**Reports**: `/api/students/{id}/reports/*`
**Notifications**: `/api/notifications/*`

### MVP Features (Priority 1)

**Must-Have for MVP**:
1. OAuth2 authentication with Keycloak
2. User management (Teachers, Parents, Students)
3. Student profile creation
4. Class creation and enrollment
5. Subject and topic management (with defaults)
6. Test score recording (manual entry)
7. Topic-level score breakdown
8. Teacher feedback
9. Progress chart data generation
10. Basic progress reports
11. Email and SMS notifications
12. Parent dashboard data APIs
13. Multi-tenant data isolation

**Post-MVP Features**:
1. Advanced reporting
2. Bulk operations
3. Data export
4. Analytics and insights

### Dependencies

**External Dependencies**:
- Keycloak (authentication server)
- PostgreSQL database
- Email service (AWS SES or similar)
- SMS service (AWS SNS or similar)

**Internal Dependencies**:
- None (this is the backend unit)

---

## Unit 2: Frontend Application (React TypeScript)

### Unit Information
- **Unit ID**: UNIT-02
- **Unit Name**: Frontend Application
- **Technology**: React 18+, TypeScript, React Router, Context API
- **Deployment**: Static site hosting (S3 + CloudFront, Vercel, or similar)
- **Build Tool**: Vite or Create React App

### Responsibilities

This unit handles all user interface, user interactions, and client-side logic.

**Core Responsibilities**:
1. User authentication flow (OAuth2 redirect)
2. Role-based UI rendering (Teacher, Parent, Student, Admin)
3. Dashboard views for each user role
4. Test score entry forms
5. Progress visualization (charts)
6. Teacher feedback display
7. Notification preference management
8. Responsive design (mobile-friendly)
9. API communication with backend
10. Client-side routing
11. State management

### Component Structure

The frontend follows a role-based organization:

#### Authentication Components
- **auth/** - Login, logout, protected routes, Keycloak provider

#### Role-Based Components
- **teacher/** - Teacher dashboard, class list, student list, score entry, progress charts
- **parent/** - Parent dashboard, child selector, progress overview, test history, preferences
- **student/** - Student dashboard, personal progress, test history, feedback view
- **admin/** - Admin dashboard, teacher management, student management, class management, subject management

#### Shared Components
- **shared/** - Charts, forms, navigation, layout, buttons, modals

#### Services & Utilities
- **services/** - API client, Keycloak integration
- **types/** - TypeScript type definitions
- **context/** - React Context for state management
- **hooks/** - Custom React hooks
- **utils/** - Utility functions

### Technology Stack

**Framework & Libraries**:
- React 18+
- TypeScript 5+
- React Router 6
- React Context API (state management)
- @react-keycloak/web or keycloak-js (authentication)
- Axios or Fetch API (HTTP client)
- Chart.js or Recharts (data visualization)
- Tailwind CSS or Material-UI (styling)

**Build Tool**:
- Vite (recommended) or Create React App
- ESLint + Prettier

**Testing**:
- Jest
- React Testing Library
- Cypress or Playwright (E2E tests)

### Routes

**Public Routes**:
- `/login` - Login page

**Teacher Routes**:
- `/teacher/dashboard` - Teacher dashboard
- `/teacher/classes` - Class list
- `/teacher/classes/:id` - Class details with students
- `/teacher/students/:id` - Student details and score entry
- `/teacher/students/:id/progress` - Student progress charts

**Parent Routes**:
- `/parent/dashboard` - Parent dashboard
- `/parent/children/:id` - Child progress overview
- `/parent/children/:id/scores` - Test score history
- `/parent/preferences` - Notification preferences

**Student Routes**:
- `/student/dashboard` - Student dashboard
- `/student/progress` - Personal progress charts
- `/student/scores` - Test score history
- `/student/feedback` - Teacher feedback

**Admin Routes**:
- `/admin/dashboard` - Admin dashboard
- `/admin/teachers` - Teacher management
- `/admin/students` - Student management
- `/admin/classes` - Class management
- `/admin/subjects` - Subject and topic management

### MVP Features (Priority 1)

**Must-Have for MVP**:
1. Login with Keycloak redirect
2. Role-based routing and access control
3. Teacher dashboard with class/student lists
4. Test score entry form with topic breakdown
5. Progress line charts (score trends)
6. Parent dashboard with child selector
7. Parent view of test scores and feedback
8. Student dashboard with personal progress
9. Basic notification preference settings
10. Mobile-responsive design
11. Error handling and loading states

**Post-MVP Features**:
1. Advanced chart types
2. Data export functionality
3. Bulk operations UI
4. Advanced filtering and search
5. Real-time notifications

### Dependencies

**External Dependencies**:
- Backend API (UNIT-01)
- Keycloak (authentication server)

**Internal Dependencies**:
- None (this is the frontend unit)

---

## Unit Integration

### Backend → Frontend Integration

**Communication**: RESTful API over HTTPS
**Authentication**: JWT tokens in Authorization header
**Data Format**: JSON

**Flow**:
1. Frontend redirects to Keycloak for login
2. Keycloak redirects back with authorization code
3. Frontend exchanges code for JWT access token
4. Frontend includes JWT in all API requests to backend
5. Backend validates JWT using Spring Security OAuth2
6. Backend returns JSON responses
7. Frontend renders data in UI

### Shared Contracts

**API Contract**: RESTful endpoints with JSON payloads
**Authentication**: OAuth2 + JWT
**Error Format**: Standardized error response DTOs

---

## Development Workflow

### Unit 1 (Backend) Development
1. Set up Spring Boot project with Maven/Gradle
2. Configure Spring Security 6 OAuth2
3. Implement shared modules (auth, notification, common)
4. Implement feature modules one by one
5. Write unit tests for each module
6. Write integration tests
7. Set up database migrations
8. Configure Keycloak connection
9. Test API endpoints

### Unit 2 (Frontend) Development
1. Set up React TypeScript project with Vite
2. Configure Keycloak JS adapter
3. Implement authentication flow
4. Implement shared components
5. Implement role-based components
6. Connect to backend API
7. Write component tests
8. Write E2E tests
9. Ensure mobile responsiveness

### Integration Testing
1. Run backend locally
2. Run frontend locally
3. Test complete user flows
4. Verify authentication works end-to-end
5. Test all API integrations
6. Verify role-based access control
7. Test notification flow

---

## Deployment Strategy

### Backend Deployment
- **Environment**: AWS (ECS, EC2, or Elastic Beanstalk)
- **Database**: AWS RDS PostgreSQL
- **Configuration**: Environment variables for Keycloak, database, AWS services
- **Scaling**: Horizontal scaling with load balancer

### Frontend Deployment
- **Environment**: AWS S3 + CloudFront (or Vercel, Netlify)
- **Build**: Static files generated by Vite
- **Configuration**: Environment variables for API URL, Keycloak URL
- **CDN**: CloudFront for global distribution

### Per-Centre Deployment
Each tuition centre gets:
- Separate backend deployment
- Separate database instance
- Separate frontend deployment (or shared with centre-specific config)
- Shared or separate Keycloak realm

---

## Code Organization

### Backend Directory Structure
```
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── tuitioncentre/
│   │   │           └── spm/
│   │   │               ├── auth/                    (Shared module)
│   │   │               │   ├── config/
│   │   │               │   │   ├── SecurityConfig.java
│   │   │               │   │   └── OAuth2ResourceServerConfig.java
│   │   │               │   ├── filter/
│   │   │               │   │   └── JwtAuthenticationFilter.java
│   │   │               │   └── service/
│   │   │               │       └── RoleService.java
│   │   │               ├── notification/            (Shared module)
│   │   │               │   ├── publisher/
│   │   │               │   └── handler/
│   │   │               ├── common/                  (Shared module)
│   │   │               │   ├── exception/
│   │   │               │   ├── dto/                (Generic DTOs only)
│   │   │               │   │   ├── ErrorResponseDTO.java
│   │   │               │   │   ├── PagedResponseDTO.java
│   │   │               │   │   └── ApiResponseDTO.java
│   │   │               │   └── config/
│   │   │               ├── user/                    (Feature module)
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   └── dto/                (Feature-specific DTOs)
│   │   │               │       ├── UserDTO.java
│   │   │               │       ├── CreateUserRequestDTO.java
│   │   │               │       └── UpdateUserRequestDTO.java
│   │   │               ├── student/                 (Feature module)
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   └── dto/                (Feature-specific DTOs)
│   │   │               │       ├── StudentDTO.java
│   │   │               │       └── CreateStudentRequestDTO.java
│   │   │               ├── class/                   (Feature module)
│   │   │               ├── subject/                 (Feature module)
│   │   │               ├── testscore/               (Feature module)
│   │   │               ├── progress/                (Feature module)
│   │   │               ├── feedback/                (Feature module)
│   │   │               └── report/                  (Feature module)
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/
│   │           └── migration/
│   └── test/
├── pom.xml (or build.gradle)
└── README.md
```

### Frontend Directory Structure
```
frontend/
├── src/
│   ├── components/
│   │   ├── teacher/
│   │   ├── parent/
│   │   ├── student/
│   │   ├── admin/
│   │   └── shared/
│   ├── auth/
│   ├── services/
│   ├── types/
│   ├── context/
│   ├── hooks/
│   ├── utils/
│   ├── App.tsx
│   └── main.tsx
├── public/
├── package.json
├── tsconfig.json
├── vite.config.ts
└── README.md
```

---

## Summary

**Total Units**: 2
- **Unit 1**: Backend API (Spring Boot modular monolith)
- **Unit 2**: Frontend Application (React TypeScript)

**Integration**: RESTful API with OAuth2/JWT authentication

**Deployment**: Separate deployments per centre with isolated databases

**Development Approach**: MVP features first, then enhancements

---

**Document Version**: 1.0  
**Created**: 2026-03-08  
**Status**: Draft - Pending Approval
