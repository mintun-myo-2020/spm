# Unit of Work Dependencies

## Dependency Matrix

| Unit | Depends On | Dependency Type | Integration Method |
|------|------------|-----------------|-------------------|
| UNIT-01: Backend API | Keycloak | External | OAuth2/OIDC |
| UNIT-01: Backend API | PostgreSQL 18 | External | JDBC |
| UNIT-01: Backend API | Email Service (AWS SES) | External | REST API |
| UNIT-01: Backend API | SMS Service (AWS SNS) | External | REST API |
| UNIT-02: Frontend Application | UNIT-01: Backend API | Internal | REST API |
| UNIT-02: Frontend Application | Keycloak | External | OAuth2/OIDC |

---

## Dependency Details

### UNIT-01: Backend API Dependencies

#### External Dependencies

**1. Keycloak (Authentication Server)**
- **Type**: External Service
- **Purpose**: OAuth2/OIDC authentication provider
- **Integration**: Spring Security 6 OAuth2 Resource Server
- **Configuration**: 
  - Issuer URI in application.yml
  - JWK Set URI for token validation
  - Client ID and secret for OAuth2 client flow
- **Criticality**: High - Required for all authentication

**2. PostgreSQL 18 (Database)**
- **Type**: External Service
- **Purpose**: Data persistence
- **Integration**: Spring Data JPA with PostgreSQL driver
- **Configuration**:
  - JDBC connection string
  - Username and password
  - Connection pool settings
- **Criticality**: High - Required for all data operations

**3. Email Service (AWS SES or similar)**
- **Type**: External Service
- **Purpose**: Send email notifications
- **Integration**: AWS SDK or SMTP
- **Configuration**:
  - AWS credentials or SMTP settings
  - From email address
  - Region (if AWS)
- **Criticality**: Medium - Notifications can be queued/retried

**4. SMS Service (AWS SNS or similar)**
- **Type**: External Service
- **Purpose**: Send SMS notifications
- **Integration**: AWS SDK
- **Configuration**:
  - AWS credentials
  - Phone number or sender ID
  - Region
- **Criticality**: Medium - Notifications can be queued/retried

#### Internal Module Dependencies

**Shared Modules** (used by all feature modules):
- `auth/` - Authentication and authorization
- `notification/` - Notification publishing
- `common/` - Shared utilities, exceptions, generic DTOs (ErrorResponseDTO, PagedResponseDTO, ApiResponseDTO)

**Note**: Feature-specific DTOs (StudentDTO, TestScoreDTO, etc.) are kept within their respective feature modules for better encapsulation.

**Feature Module Dependencies**:
```
user-management/
  └─> auth/ (for authentication)
  └─> common/ (for generic DTOs, exceptions)
  └─> Own DTOs: UserDTO, CreateUserRequestDTO, etc.

student-management/
  └─> auth/ (for authentication)
  └─> user-management/ (for user references)
  └─> common/ (for generic DTOs, exceptions)
  └─> Own DTOs: StudentDTO, CreateStudentRequestDTO, etc.

class-management/
  └─> auth/ (for authentication)
  └─> student-management/ (for student enrollment)
  └─> user-management/ (for teacher assignment)
  └─> common/ (for generic DTOs, exceptions)
  └─> Own DTOs: ClassDTO, EnrollmentDTO, etc.

subject-management/
  └─> auth/ (for authentication)
  └─> common/ (for generic DTOs, exceptions)
  └─> Own DTOs: SubjectDTO, TopicDTO, etc.

test-score-management/
  └─> auth/ (for authentication)
  └─> student-management/ (for student reference)
  └─> subject-management/ (for topic reference)
  └─> notification/ (for score notifications)
  └─> common/ (for generic DTOs, exceptions)
  └─> Own DTOs: TestScoreDTO, TopicScoreDTO, RecordScoreRequestDTO, etc.

progress-tracking/
  └─> auth/ (for authentication)
  └─> test-score-management/ (for score data)
  └─> student-management/ (for student reference)
  └─> common/ (for generic DTOs, exceptions)
  └─> Own DTOs: ProgressDTO, ChartDataDTO, etc.

feedback-management/
  └─> auth/ (for authentication)
  └─> test-score-management/ (for test reference)
  └─> student-management/ (for student reference)
  └─> notification/ (for feedback notifications)
  └─> common/ (for generic DTOs, exceptions)
  └─> Own DTOs: FeedbackDTO, CreateFeedbackRequestDTO, etc.

reporting/
  └─> auth/ (for authentication)
  └─> progress-tracking/ (for progress data)
  └─> test-score-management/ (for score data)
  └─> feedback-management/ (for feedback data)
  └─> common/ (for generic DTOs, exceptions)
  └─> Own DTOs: ReportDTO, ReportDataDTO, etc.
```

---

### UNIT-02: Frontend Application Dependencies

#### External Dependencies

**1. UNIT-01: Backend API**
- **Type**: Internal Service
- **Purpose**: All data operations and business logic
- **Integration**: REST API over HTTPS with JWT authentication
- **Configuration**:
  - API base URL (environment variable)
  - Timeout settings
  - Retry logic
- **Criticality**: High - Required for all functionality

**2. Keycloak (Authentication Server)**
- **Type**: External Service
- **Purpose**: User authentication
- **Integration**: Keycloak JS library (@react-keycloak/web or keycloak-js)
- **Configuration**:
  - Keycloak URL
  - Realm name
  - Client ID
- **Criticality**: High - Required for login

#### Internal Component Dependencies

**Shared Components** (used by all role components):
- `shared/` - Charts, forms, navigation, layout
- `auth/` - Authentication wrapper, protected routes
- `services/` - API client, Keycloak integration
- `types/` - TypeScript type definitions
- `context/` - Global state management

**Role Component Dependencies**:
```
teacher/
  └─> auth/ (for protected routes)
  └─> shared/ (for charts, forms, navigation)
  └─> services/ (for API calls)
  └─> types/ (for TypeScript types)
  └─> context/ (for state)

parent/
  └─> auth/ (for protected routes)
  └─> shared/ (for charts, forms, navigation)
  └─> services/ (for API calls)
  └─> types/ (for TypeScript types)
  └─> context/ (for state)

student/
  └─> auth/ (for protected routes)
  └─> shared/ (for charts, navigation)
  └─> services/ (for API calls)
  └─> types/ (for TypeScript types)
  └─> context/ (for state)

admin/
  └─> auth/ (for protected routes)
  └─> shared/ (for forms, navigation)
  └─> services/ (for API calls)
  └─> types/ (for TypeScript types)
  └─> context/ (for state)
```

---

## Integration Points

### Backend ↔ Frontend Integration

**Protocol**: HTTPS
**Format**: JSON
**Authentication**: JWT Bearer token in Authorization header

**Request Flow**:
1. Frontend makes HTTP request to backend API
2. Frontend includes JWT token in Authorization header
3. Backend validates JWT using Spring Security OAuth2
4. Backend processes request and returns JSON response
5. Frontend handles response and updates UI

**Error Handling**:
- 401 Unauthorized → Redirect to login
- 403 Forbidden → Show access denied message
- 400 Bad Request → Show validation errors
- 500 Server Error → Show generic error message

### Backend ↔ Keycloak Integration

**Protocol**: HTTPS
**Format**: OAuth2/OIDC standard
**Integration**: Spring Security 6 OAuth2 Resource Server

**Token Validation Flow**:
1. Frontend sends JWT to backend in Authorization header
2. Backend extracts JWT from header
3. Spring Security validates JWT signature using Keycloak's JWK Set
4. Spring Security extracts user info and roles from JWT claims
5. Backend enforces role-based access control

### Frontend ↔ Keycloak Integration

**Protocol**: HTTPS
**Format**: OAuth2 Authorization Code Flow
**Integration**: Keycloak JS library

**Login Flow**:
1. User clicks login in frontend
2. Frontend redirects to Keycloak login page
3. User authenticates with Keycloak
4. Keycloak redirects back to frontend with authorization code
5. Frontend exchanges code for JWT access token
6. Frontend stores token in memory
7. Frontend includes token in all API requests

---

## Dependency Management

### Version Management

**Backend**:
- Spring Boot 4.x manages most dependency versions
- Use Spring Boot BOM (Bill of Materials)
- Explicit versions only for non-Spring dependencies

**Frontend**:
- package.json defines all dependency versions
- Use exact versions (not ranges) for reproducibility
- Regular updates for security patches

### Dependency Updates

**Backend**:
- Monitor Spring Boot releases
- Update Spring Boot version for security patches
- Test thoroughly after updates

**Frontend**:
- Monitor npm security advisories
- Update dependencies regularly
- Use `npm audit` to check for vulnerabilities

---

## Deployment Dependencies

### Backend Deployment Requirements

**Infrastructure**:
- Java 25 runtime
- Application server (embedded Tomcat in Spring Boot)
- PostgreSQL 18 database
- Network access to Keycloak
- Network access to AWS services (SES, SNS)

**Configuration**:
- Environment variables for all external service URLs
- Secrets management for credentials
- Database connection pool configuration

### Frontend Deployment Requirements

**Infrastructure**:
- Static file hosting (S3, CloudFront, Vercel, etc.)
- CDN for global distribution
- HTTPS certificate

**Configuration**:
- Environment variables for API URL and Keycloak URL
- Build-time configuration injection

---

## Circular Dependency Prevention

**Rule**: No circular dependencies between modules

**Validation**:
- Feature modules can depend on shared modules
- Feature modules can depend on other feature modules (with caution)
- Shared modules cannot depend on feature modules
- If circular dependency detected, extract shared logic to common module

**Current Status**: No circular dependencies detected in design

---

## Summary

**Total Dependencies**:
- Backend: 4 external, multiple internal module dependencies
- Frontend: 2 external (Backend API + Keycloak), multiple internal component dependencies

**Critical Dependencies**:
- Keycloak (authentication)
- PostgreSQL 18 (data persistence)
- Backend API (for frontend)

**Integration Complexity**: Medium
- Standard REST API integration
- Standard OAuth2/OIDC integration
- Well-defined contracts

---

**Document Version**: 1.0  
**Created**: 2026-03-08  
**Status**: Draft - Pending Approval
