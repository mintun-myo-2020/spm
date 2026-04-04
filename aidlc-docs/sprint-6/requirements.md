# Sprint 6 — Multi-Tenant Center-Level Data Segregation — Requirements

## Intent Analysis

- **User Request**: Add multi-tenant center-level data segregation to the SPM application using Keycloak Organizations (v26+) with a shared realm
- **Request Type**: Enhancement (cross-cutting architectural change)
- **Scope Estimate**: System-wide — touches database schema, all entities/repositories/services, auth layer, Keycloak config, frontend auth flow, deployment
- **Complexity Estimate**: Complex — multi-tenancy is a cross-cutting concern affecting nearly every layer of the application

---

## Functional Requirements

### FR-1: Tenant Data Model

- **FR-1.1**: A `tenants` table stores tenant metadata: `id` (UUID, PK), `name`, `keycloak_org_id` (unique, maps to Keycloak Organization ID), `is_active`, `created_at`, `updated_at`
- **FR-1.2**: A `tenant_id` UUID column (FK to `tenants.id`) is added to every tenant-scoped table
- **FR-1.3**: Tenant-scoped tables: `users`, `user_roles`, `teachers`, `students`, `parents`, `classes`, `class_students`, `class_sessions`, `session_attendance`, `test_scores`, `questions`, `sub_questions`, `feedback`, `feedback_templates`, `notifications`, `progress_reports`, `teacher_history`, `subjects`, `topics`, `test_paper_uploads`, `test_paper_pages`
- **FR-1.4**: No tables are shared across tenants — subjects and topics are fully tenant-scoped (each tenant gets their own copy seeded on provisioning)
- **FR-1.5**: The `tenant_id` column is NOT NULL on all scoped tables

### FR-2: Tenant Scoping Enforcement

- **FR-2.1**: All repository queries on tenant-scoped entities explicitly include `tenant_id` in the WHERE clause (no Hibernate filters — explicit and visible)
- **FR-2.2**: A `TenantContext` holder (ThreadLocal or request-scoped bean) stores the current tenant ID extracted from the JWT for the duration of the request
- **FR-2.3**: All service methods that create entities set `tenant_id` from `TenantContext` before persisting
- **FR-2.4**: All service methods that query entities pass `tenant_id` from `TenantContext` to repository methods
- **FR-2.5**: The existing `created_by` scoping (Sprint 5 teacher data isolation) is retained alongside `tenant_id` — `tenant_id` for center-level isolation, `created_by` for teacher-level isolation within a tenant

### FR-3: Keycloak Organizations Integration

- **FR-3.1**: Keycloak Organizations feature is enabled in the `spm` realm
- **FR-3.2**: Each tuition center is represented as a Keycloak Organization
- **FR-3.3**: The JWT contains the built-in `organization` claim (Keycloak 26+ format: `{ "<org-id>": { "name": "<org-name>" } }`)
- **FR-3.4**: The backend extracts the organization ID from the `organization` claim in the JWT and resolves it to a `tenant_id` via the `tenants` table (`keycloak_org_id` → `id`)
- **FR-3.5**: If the JWT has no `organization` claim or the org ID doesn't match any tenant, the request is rejected with 403 Forbidden
- **FR-3.6**: Users are assigned to Keycloak Organizations — a user belongs to the organization(s) they are a member of

### FR-4: Role Model

- **FR-4.1**: The existing `ADMIN` role becomes a per-tenant (center) admin — can only manage users, classes, and data within their own tenant
- **FR-4.2**: No `SUPER_ADMIN` role in the application — the platform operator uses the Keycloak Admin Console directly to manage organizations and users
- **FR-4.3**: The `Role` enum remains: `ADMIN`, `TEACHER`, `STUDENT`, `PARENT` — no new roles added
- **FR-4.4**: All role-based access checks continue to work as before, but are now implicitly scoped to the current tenant via `tenant_id` filtering

### FR-5: Per-Tenant URL (Frontend Deployment)

- **FR-5.1**: Each center gets its own frontend URL/subdomain (e.g., `center-a.spm.com`, `center-b.spm.com`) served via CloudFront
- **FR-5.2**: The frontend derives the tenant identifier from the URL (subdomain or path) and passes it to Keycloak as the `kc_org` login hint parameter during authentication
- **FR-5.3**: Keycloak auto-selects the organization based on `kc_org`, so the resulting JWT contains the correct `organization` claim — no custom org picker UI needed in SPM
- **FR-5.4**: The frontend stores the resolved tenant context (org ID, org name) in React context after login for display purposes (e.g., center name in the navbar)
- **FR-5.5**: Users who work at multiple centers use different URLs to access each center

### FR-6: Tenant Provisioning

- **FR-6.1**: A CLI provisioning script creates a new tenant end-to-end:
  1. Creates a Keycloak Organization via the Keycloak Admin REST API
  2. Creates a `tenants` row in the database with the Keycloak org ID mapping
  3. Creates the first ADMIN user in Keycloak and assigns them to the organization
  4. Seeds default subjects and topics for the tenant in the database
- **FR-6.2**: The script accepts parameters: center name, admin email, admin temporary password, and optionally a list of subjects to seed (defaults to the standard subject catalog)
- **FR-6.3**: The script is idempotent — running it again with the same center name skips already-created resources

### FR-7: Data Migration

- **FR-7.1**: Existing data is wiped — this is a dev/staging environment, fresh start with the new schema
- **FR-7.2**: A new Flyway migration adds the `tenants` table and `tenant_id` columns to all scoped tables
- **FR-7.3**: Existing seed data migrations are updated to be tenant-aware (seed data is created per-tenant via the provisioning script, not via Flyway)

### FR-8: User Provisioning (Updated)

- **FR-8.1**: `CurrentUserService.provisionFromToken()` is updated to extract the tenant ID from the JWT's `organization` claim and set it on the provisioned `User` record
- **FR-8.2**: When creating profile records (Teacher, Student, Parent), the `tenant_id` is propagated from the User
- **FR-8.3**: The existing email-based linking logic (admin-created users with `pending-` keycloakId) continues to work, but now also validates that the tenant matches

### FR-9: Keycloak Realm Configuration (Dev)

- **FR-9.1**: The dev `realm-export.json` is updated to enable the Organizations feature and include a sample organization for development
- **FR-9.2**: The sample org includes test users (admin, teacher, student, parent) assigned to it
- **FR-9.3**: Production organizations are created via the provisioning script (FR-6), not via realm export

---

## Non-Functional Requirements

### NFR-1: Data Isolation

- **NFR-1.1**: No API endpoint returns data from a different tenant than the authenticated user's tenant
- **NFR-1.2**: Tenant isolation is enforced at the service/repository layer — not just at the controller level
- **NFR-1.3**: Missing or invalid tenant context results in request rejection, never in cross-tenant data leakage

### NFR-2: Performance

- **NFR-2.1**: Adding `tenant_id` to queries should not degrade performance — composite indexes include `tenant_id` as the leading column on frequently queried tables
- **NFR-2.2**: Tenant resolution (JWT org claim → tenant_id lookup) is cached to avoid repeated DB lookups per request

### NFR-3: Backward Compatibility

- **NFR-3.1**: Existing API contracts (request/response shapes) remain unchanged — `tenant_id` is never exposed in API responses or required in API requests (it's always derived from the JWT)
- **NFR-3.2**: Frontend components do not need to pass tenant_id — it's handled transparently by the backend

---

## Out of Scope (Sprint 6)

- Cross-tenant reporting or analytics
- Tenant-level configuration (custom branding, feature flags per tenant)
- Tenant suspension/deactivation workflow
- Billing or usage metering per tenant
- Self-service tenant signup
- Forgot password / SMTP configuration
- Parent creation from teacher view
