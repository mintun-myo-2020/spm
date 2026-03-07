# Unit of Work Clarification Questions

## Clarification: Shared Components (Question 4)

You asked for explanation of Question 4 about how to handle shared components (authentication, notifications).

### Context
Since you chose:
- **Q1**: Modular monolith (single deployment with clear module boundaries)
- **Q2**: Backend + Frontend units (two separate units)

The question is: Within the Backend unit, how should authentication and notification code be organized?

### The Options Explained:

**A) Integrated in each unit**
- Auth and notification code duplicated/embedded in each feature module
- Example: Student module has its own auth checks, Progress module has its own auth checks
- **Not recommended** - leads to code duplication

**B) Separate shared unit**
- This doesn't apply since you chose 2 units (Backend + Frontend), not multiple backend units
- **Skip this option**

**C) Library/module approach** ⭐ **RECOMMENDED for your choices**
- Create shared modules within the Backend unit
- Example structure:
  ```
  backend/
    ├── auth-module/          (shared authentication logic)
    ├── notification-module/  (shared notification logic)
    ├── student-module/       (uses auth-module)
    ├── progress-module/      (uses auth-module)
    └── common/               (shared utilities)
  ```
- Each feature module imports and uses the shared modules
- Clean separation, no duplication, easy to maintain

**D) External services**
- Keycloak is already external (you chose this in Application Design)
- Notification services (email/SMS) could be external AWS services
- But the integration code still needs to live somewhere in your backend
- **Partial fit** - Keycloak is external, but you still need internal modules to integrate with it

### Clarification Question

Given your choices (Modular monolith with Backend + Frontend units), which approach do you prefer for organizing shared code WITHIN the backend unit?

A) **Library/module approach** - Shared modules (auth-module, notification-module) within backend, feature modules import them (RECOMMENDED)
B) **Integrated approach** - Each feature module has its own auth/notification code (not recommended, causes duplication)
C) **Hybrid** - Auth is a shared module (since it's used everywhere), but notifications are integrated per feature
X) Other (please describe after [Answer]: tag below)

[Answer]: A

---

### Additional Context

**Recommended Structure (Option A)**:
```
backend/ (Spring Boot - Modular Monolith)
├── auth/                    (Shared authentication module)
│   ├── SecurityConfig       (Spring Security 6 OAuth2 configuration)
│   ├── OAuth2ResourceServerConfig
│   ├── JwtAuthenticationFilter
│   └── RoleService
├── notification/            (Shared notification module)
│   ├── NotificationEventPublisher
│   ├── EmailNotificationHandler
│   └── SMSNotificationHandler
├── student-management/      (Feature module)
│   ├── StudentController
│   ├── StudentService
│   └── StudentRepository
├── progress-tracking/       (Feature module)
│   ├── ProgressController
│   ├── ProgressService
│   └── ProgressRepository
└── common/                  (Shared utilities)
    ├── exceptions/
    ├── dto/                 (Generic DTOs only: ErrorResponseDTO, PagedResponseDTO, ApiResponseDTO)
    └── config/

**Note**: Feature-specific DTOs (StudentDTO, TestScoreDTO, etc.) are kept within their respective feature modules, not in common/.

**Important**: Backend uses **Spring Security 6 OAuth2 Resource Server** (generic OIDC/OAuth2 libraries), NOT deprecated Keycloak adapters.

Each feature module imports the shared auth and notification modules as needed.
