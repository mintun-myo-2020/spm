# AI-DLC State Tracking

## Project Information
- **Project Type**: Brownfield (Sprint 2)
- **Start Date**: 2026-03-08T00:00:00Z
- **Sprint 2 Start Date**: 2026-03-15T00:00:00Z
- **Current Stage**: CONSTRUCTION - Functional Design (Unit 2: OCR Test Upload) - IN PROGRESS

## Workspace State
- **Existing Code**: Yes (Sprint 1 complete)
- **Reverse Engineering Needed**: No
- **Workspace Root**: Current directory

## Code Location Rules
- **Application Code**: Workspace root (NEVER in aidlc-docs/)
- **Documentation**: aidlc-docs/ only
- **Structure patterns**: See code-generation.md Critical Rules

## Sprint 2 Execution Plan Summary
- **Total Stages to Execute**: 7
- **Unit Structure**: Per-feature (3 units)
- **INCEPTION Stages**: Workspace Detection ✓, Requirements Analysis ✓, Workflow Planning (in progress)
- **CONSTRUCTION Stages**: Functional Design (Units 2,3), Code Generation (Units 1,2,3), Build and Test
- **Stages Skipped**: User Stories, App Design, Units Gen, NFR Req (all), NFR Design (all), Infra Design (all), Functional Design (Unit 1)

## Extension Configuration
| Extension | Enabled | Decided At |
|---|---|---|
| Security Baseline | No | Requirements Analysis (Sprint 1) |

## Sprint 1 — COMPLETED
All INCEPTION and CONSTRUCTION stages complete for both units (Backend API + Frontend Application).

## Sprint 2 — Stage Progress

### INCEPTION PHASE
- [x] Workspace Detection - COMPLETED
- [x] Requirements Analysis - COMPLETED
- [x] Workflow Planning - IN PROGRESS
- [ ] User Stories - SKIP
- [ ] Application Design - SKIP
- [ ] Units Generation - SKIP

### CONSTRUCTION PHASE — Unit 1: Progress Report Content
- [x] Functional Design - SKIP
- [x] NFR Requirements - SKIP
- [x] NFR Design - SKIP
- [x] Infrastructure Design - SKIP
- [x] Code Generation - COMPLETED

### CONSTRUCTION PHASE — Unit 2: OCR Test Upload
- [x] Functional Design - COMPLETED
- [ ] NFR Requirements - SKIP
- [ ] NFR Design - SKIP
- [ ] Infrastructure Design - SKIP
- [x] Code Generation - COMPLETED

### CONSTRUCTION PHASE — Unit 3: Class Scheduling & Attendance
- [x] Functional Design - COMPLETED
- [ ] NFR Requirements - SKIP
- [ ] NFR Design - SKIP
- [ ] Infrastructure Design - SKIP
- [x] Code Generation - COMPLETED

### Build and Test
- [ ] Build and Test - EXECUTE

## Current Status
- **Lifecycle Phase**: CONSTRUCTION
- **Current Stage**: Sprint 5 — User Onboarding & Account Management — IN PROGRESS
- **Next Stage**: Resolve onboarding blockers (self-service signup, data isolation)
- **Status**: Sprint 5 in progress. Temporary passwords, change password, settings page, create-and-enroll all done. Blocked on decisions for self-service signup and multi-tenancy.

## Sprint 4 — Class Page Restructuring & Session Notes

### Completed
- [x] Nested routes: `/classes/:classId/students`, `/classes/:classId/schedule`, `/classes/:classId/notes`
- [x] ClassLayout with persistent sub-nav (Students | Schedule | Notes)
- [x] Session notes: per-session structured fields (topic, homework, weaknesses, additional notes)
- [x] Backend: V13 migration, entity fields, DTOs, service methods, REST endpoints
- [x] Role-based filtering: parents/students see only topic + homework (no weaknesses/additional notes)
- [x] Notes editable from both Notes tab and SessionDetail page

## Sprint 3 — Report Plan Improvements

### Completed
- [x] Fix progress page to use percentages instead of raw scores (ProgressService, StudentProgressView)
- [x] Fix LLM stub returning empty plan (parseResponse fallback validation)
- [x] Switch LLM to Bedrock (.env, docker-compose.yml)
- [x] Consolidate BedrockConfig (single shared BedrockRuntimeClient)
- [x] LLM prompt fixes: gender-neutral language, no prescriptive advice, checkable action items
- [x] Action plan checklist: `completed` field on ActionItem, toggle endpoint
- [x] JSON plan storage: `plan_json` column on progress_reports, serialized on generation
- [x] Async report generation: fire-and-forget with IN_PROGRESS/COMPLETED/FAILED status
- [x] Transaction commit ordering fix for async dispatch
- [x] Pluggable job dispatcher abstraction (ReportJobDispatcher interface)
- [x] Config-driven environment swaps (dispatcher, storage, LLM, extraction)

### Remaining / Future
- [ ] Frontend report detail view — render planJson interactively with checkable action items
- [ ] SQS-based ReportJobDispatcher implementation (when needed for prod)
- [ ] S3-based ReportStorage implementation (when needed for prod)


## Sprint 5 — User Onboarding & Account Management

### Status: IN PROGRESS

### Completed
- [x] Temporary password on user creation (Keycloak `temporary: true`)
- [x] Password field UX — "Temporary Password" label + helper text on all create forms
- [x] In-app change password (Settings page with current/new/confirm form)
- [x] Password verification uses `spm-frontend` public client (directAccessGrantsEnabled: true)
- [x] Settings page with account info, change password, logout
- [x] Sidebar "Settings" gear icon at bottom for all roles
- [x] Logout moved from Navbar to Settings page
- [x] Admin/teacher reset password API (`PUT /api/v1/users/{userId}/reset-password`)
- [x] Create-and-enroll student from teacher enroll modal (ClassLayout + AdminClassDetails)
- [x] Dead code cleanup: deleted orphaned ClassDetails.tsx

### Pending (blocked on requirement decisions)
- [ ] Self-service signup (how do new teachers register without Keycloak console access?)
- [ ] Data isolation / multi-tenancy (blocking for multiple customers)
- [ ] Parent creation from teacher view
- [ ] Forgot password (Keycloak SMTP config)
- [ ] Reset password button in frontend user management pages

### Decisions Made (from requirement-verification-questions.md)
- Q1: Operator creates teacher/admin accounts via admin UI (no self-service signup)
- Q2: Separate instance per customer (no multi-tenancy code changes needed)
- Q3: Temporary passwords set by operator/teacher, forced change on first login

### New Issue Identified: Teacher Data Isolation
- Teachers can see ALL students globally in the enroll dropdown (not just their own)
- See `sprint-5/teacher-data-isolation-analysis.md` for full analysis
- Fix: scope `GET /users/students` by `created_by` + class enrollment for TEACHER role
- Effort: ~1.5 hours

### Remaining Work
- [ ] Teacher data isolation fix (scope student list to teacher's own students)
- [ ] Production seed migration (admin-only, no demo data)
- [ ] Deployment guide for spinning up new customer instances
- [ ] Parent creation from teacher view
- [ ] Forgot password (Keycloak SMTP config)
- [ ] Reset password button in frontend user management pages

## Next Phase: Maintainability & Prod-Readiness

### Priority Order
1. Customer-driven feature requests (highest priority — if they come in, they jump the queue)
2. Abstraction & vendor lock-in reduction
3. Prod-readiness hardening

### Vendor Lock-in Assessment

| Component | Current Implementation | Lock-in Risk | Abstraction Status |
|---|---|---|---|
| Auth (IdP) | Keycloak | LOW | Backend uses Spring Security OAuth2 Resource Server (generic OIDC/JWT). No Keycloak-specific APIs in business logic. Frontend uses keycloak-js adapter. Swapping IdP = change Keycloak JS config + issuer URL. |
| File Storage | LocalFileStorageService / S3 | LOW | Behind `FileStorageService` interface. S3 impl ready. |
| OCR | StubOcrService / Textract | LOW | Behind `OcrService` interface. Textract impl ready. |
| LLM | BedrockLlmService | MEDIUM | Behind `LlmService` interface but prompt format is Bedrock-specific. |
| Job Dispatch | Sync (local) / SQS | LOW | Behind `ReportJobDispatcher` interface. SQS impl planned. |
| Report Storage | StubReportStorage / S3 | LOW | Behind `ReportStorage` interface. S3 impl planned. |
| Database | PostgreSQL 18 | MEDIUM | JPA/Hibernate abstracts most SQL. Some native queries exist. |
| Frontend Auth | keycloak-js | MEDIUM | Wrapped in `keycloakService.ts`. Swap = rewrite this one file to use generic OIDC client (e.g., oidc-client-ts). |

### Keycloak Specifically
The backend is NOT locked into Keycloak. It uses `spring-boot-starter-oauth2-resource-server` which validates any OIDC-compliant JWT. The only Keycloak-specific code is `KeycloakRoleConverter` which reads roles from Keycloak's JWT structure (`realm_access.roles`). Swapping to another IdP (Auth0, Cognito, etc.) requires:
- Backend: Update `RoleConverter` to read roles from the new IdP's JWT claim structure + change issuer/jwk-set-uri in application.yml
- Frontend: Replace `keycloakService.ts` with a generic OIDC client wrapper

### Prod-Readiness Backlog
- [ ] SQS-based ReportJobDispatcher implementation
- [ ] S3-based ReportStorage implementation
- [ ] Frontend report detail view (render planJson interactively with checkable action items)
- [x] Test coverage improvements (Sprint 4 — 54 backend unit tests, 53 frontend tests)
- [ ] CI/CD pipeline
- [ ] Deployment automation (ECS/Fargate or similar)
- [ ] Monitoring & alerting setup
- [ ] Error tracking integration
