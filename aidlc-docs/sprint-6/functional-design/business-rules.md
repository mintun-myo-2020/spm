# Sprint 6 — Business Rules — Multi-Tenant Data Segregation

## BR-1: Tenant Isolation (Hard Rules)

### BR-1.1: Every API request must have a valid tenant context
- If JWT has no `organization` claim → 403 Forbidden
- If `organization` claim maps to no known tenant → 403 Forbidden
- If tenant `is_active = false` → 403 Forbidden
- No request may proceed without a resolved tenant ID

### BR-1.2: All data reads are tenant-scoped
- Every repository query on a tenant-scoped entity MUST include `tenant_id` in the WHERE clause
- No query may return data from a different tenant than the current request's tenant
- This applies to both Spring Data derived queries and custom @Query methods

### BR-1.3: All data writes are tenant-scoped
- Every entity created MUST have `tenant_id` set from `TenantContext` before persisting
- `tenant_id` is immutable after creation (`updatable = false` on JPA column)
- No entity may be updated or deleted if it belongs to a different tenant

### BR-1.4: Tenant ID is never exposed in API
- `tenant_id` is never included in request bodies or response DTOs
- `tenant_id` is always derived from the JWT `organization` claim
- API contracts remain unchanged from pre-multi-tenancy

---

## BR-2: Email Uniqueness

### BR-2.1: Email is unique per tenant
- Composite unique constraint: `(tenant_id, email)` on the `users` table
- The same email address MAY exist in different tenants
- Within a single tenant, email MUST be unique

### BR-2.2: Email linking during provisioning
- When `CurrentUserService.provisionFromToken()` runs, it searches for existing users by `(tenant_id, email)` — not globally
- If a user with the same email exists in a DIFFERENT tenant, it is ignored (separate user)
- If a user with the same email exists in the SAME tenant with a `pending-` keycloakId, it is linked

---

## BR-3: Subject and Topic Scoping

### BR-3.1: Subjects are fully tenant-scoped
- Each tenant gets their own copy of subjects and topics (seeded during provisioning)
- Subject code uniqueness is per-tenant: `(tenant_id, code)` composite unique
- No shared/global subject catalog

### BR-3.2: Topic code uniqueness is per-subject (unchanged)
- `existsBySubjectIdAndCode()` becomes `existsByTenantIdAndSubjectIdAndCode()`
- Since subjects are already tenant-scoped, this is defense-in-depth

---

## BR-4: Feedback Template Scoping

### BR-4.1: System-wide templates are tenant-scoped
- `isSystemWide = true` means visible to all teachers WITHIN the same tenant
- The query `findByTeacherOrSystemWide()` adds `tenant_id` to the WHERE clause
- Center A's system-wide templates are invisible to Center B

---

## BR-5: Background Job Scoping

### BR-5.1: Notification background processing is per-tenant
- `NotificationRepository.findByStatus()` becomes `findByTenantIdAndStatus()`
- Background jobs iterate over active tenants and process notifications per-tenant
- This ensures consistent tenant scoping even in background contexts

### BR-5.2: Report generation is tenant-scoped
- Async report generation (fire-and-forget) must propagate tenant context to the async thread
- The `TenantContext` (request-scoped) is not available in async threads
- Solution: pass `tenantId` explicitly to the async job dispatcher as a parameter
- The async worker sets up its own tenant context from the passed tenant ID

---

## BR-6: Admin Role Scoping

### BR-6.1: ADMIN is per-tenant
- An ADMIN user can only manage users, classes, subjects, and data within their own tenant
- `findAllUpcoming()` for sessions is scoped to the admin's tenant
- No cross-tenant admin views exist in the application

### BR-6.2: No SUPER_ADMIN role
- Platform operations (create tenants, manage organizations) are done via Keycloak Admin Console
- The provisioning CLI script handles tenant setup
- No in-app cross-tenant management

---

## BR-7: Tenant Provisioning

### BR-7.1: Provisioning is config-driven
- The CLI reads tenant definitions from a config source (JSON file locally, SSM Parameter Store on AWS)
- `TenantConfigSource` interface with `FileTenantConfigSource` and `SsmTenantConfigSource` implementations
- Running the CLI provisions all tenants in the config that don't already exist (idempotent)
- Safe to run on every deploy — only creates new tenants

### BR-7.2: Provisioning creates a complete tenant
- One CLI invocation creates: Keycloak Organization + DB tenant + first admin user + seed subjects/topics
- The admin user gets a temporary password (forced change on first login)

### BR-7.3: Tenant deactivation
- Setting `tenant.is_active = false` blocks all API access for that tenant
- Existing data is preserved (soft disable, not delete)
- Reactivation restores access

---

## BR-8: Tenant Resolution (No Cache)

### BR-8.1: DB lookup per request
- Every authenticated request performs a `tenantRepository.findByKeycloakOrgId()` lookup
- No caching — simplest approach, optimize later if needed
- The lookup is a single indexed query on `keycloak_org_id` (unique index)

---

## BR-9: Frontend Tenant Identity

### BR-9.1: JWT is the source of truth for tenant identity
- After login, the frontend extracts `organization` from the JWT `tokenParsed`
- The org ID and org name are stored in React TenantContext
- Navbar displays the org name from the JWT (not from config.json)

### BR-9.2: kc_org login hint from URL
- The frontend derives a tenant slug from the URL hostname
- This slug is passed as `kc_org` parameter to Keycloak during login
- Keycloak uses this to auto-select the organization (no org picker UI needed)

### BR-9.3: Fallback display
- If JWT `organization` claim is missing (shouldn't happen), fall back to `config.json` tenantName
- This is a defensive measure, not a normal flow

---

## BR-10: Data Migration

### BR-10.1: Destructive migration
- Existing data is wiped (dev/staging environment, fresh start)
- Flyway migration V16 adds `tenants` table and `tenant_id` columns
- No data backfill — all data is created fresh via provisioning script

### BR-10.2: Seed data changes
- Existing Flyway seed migrations (V14 subjects/topics) are removed or made conditional
- Seed data is now created per-tenant via the provisioning script
- Dev environment uses the provisioning script to set up test tenants

---

## BR-11: Async Context Propagation

### BR-11.1: Request-scoped TenantContext is not available in async threads
- When dispatching async work (e.g., report generation), the tenant ID must be passed explicitly
- The async worker receives `tenantId` as a parameter and loads the Tenant entity directly
- This avoids coupling async processing to the request-scoped bean lifecycle
