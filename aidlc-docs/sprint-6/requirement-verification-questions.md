# Sprint 6 — Multi-Tenant Center-Level Data Segregation — Requirement Verification Questions

Please answer the following questions to help clarify the multi-tenancy requirements. Fill in the letter choice after each `[Answer]:` tag. If none of the options match, choose the last option (Other) and describe your preference.

---

## Question 1
How should the tenant identifier be represented in the database?

A) UUID column (`tenant_id`) on every tenant-scoped table, referencing a `tenants` table
B) String column (`org_id`) matching the Keycloak Organization ID directly (no separate tenants table)
C) Composite approach — `tenants` table with metadata (name, status, config) + UUID FK on scoped tables, with a mapping to the Keycloak org ID stored in the tenants table
D) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Question 2
How should tenant scoping be enforced at the data layer?

A) Hibernate `@Filter` / `@FilterDef` — automatic WHERE clause injection on all tenant-scoped entities (transparent, less boilerplate, but harder to debug)
B) Explicit repository methods — every query includes `AND tenant_id = ?` manually (more verbose, but fully visible and testable)
C) Spring Data JPA Specifications — reusable `TenantSpecification` composed into every query (middle ground)
D) PostgreSQL Row-Level Security (RLS) policies — enforced at the DB level using `SET app.current_tenant` per connection (strongest isolation, but complex to manage with connection pools)
E) Other (please describe after [Answer]: tag below)

[Answer]: B

---

## Question 3
Which Keycloak JWT claim should carry the tenant/organization ID?

A) Custom claim added via a Keycloak protocol mapper (e.g., `org_id` or `tenant_id` at the top level of the JWT)
B) Keycloak Organizations' built-in `organization` claim (available in Keycloak 26+ when Organizations feature is enabled — contains `{ "org-id": { "name": "org-name" } }`)
C) Use the `groups` claim with a convention like `/orgs/{org-id}`
D) Other (please describe after [Answer]: tag below)

[Answer]: B.

---

## Question 4
How should the SUPER_ADMIN role work?

A) Keycloak realm-level role (`SUPER_ADMIN`) — not scoped to any organization. Super-admins can query across all tenants and have a tenant-switcher in the UI
B) Keycloak realm-level role, but super-admins must explicitly select a tenant context to operate in (no cross-tenant queries — they just have access to all tenants one at a time)
C) Separate Keycloak realm for super-admins (complete isolation from tenant users)
D) Other (please describe after [Answer]: tag below)

[Answer]: what are superadmins?

---

## Question 5
How should the existing ADMIN role relate to tenants?

A) ADMIN becomes a per-tenant role — each center has its own admin(s) who can only manage users/data within their tenant
B) ADMIN remains a global role (same as today) and SUPER_ADMIN is just a rename
C) Two-tier: ADMIN is per-tenant (center admin), SUPER_ADMIN is cross-tenant (platform operator)
D) Other (please describe after [Answer]: tag below)

[Answer]: admin for the tenant (center) is not the same as admin for the product (me)

---

## Question 6
How should Subjects and Topics be handled in a multi-tenant context?

A) Fully shared — one global set of subjects/topics, all tenants see the same catalog (current behavior, just no tenant_id on subjects/topics)
B) Shared defaults + tenant overrides — global subjects exist as templates, but each tenant can add their own custom subjects/topics alongside the shared ones
C) Fully tenant-scoped — each tenant has their own subjects/topics (no sharing). Seed data provides defaults per tenant on creation
D) Other (please describe after [Answer]: tag below)

[Answer]: C

---

## Question 7
How should existing data be migrated when introducing multi-tenancy?

A) All existing data belongs to a "default" tenant — create a migration that adds a default tenant record and sets `tenant_id` on all existing rows
B) Existing data is wiped — this is a dev/staging environment, start fresh with the new schema
C) Existing data is exported, schema is migrated, data is re-imported with tenant assignment via a migration script
D) Other (please describe after [Answer]: tag below)

[Answer]: B

---

## Question 8
How should new tenant (center) provisioning work?

A) SUPER_ADMIN creates a new tenant via the app UI → app creates the Keycloak Organization + tenant DB record, then the super-admin creates the first center admin user
B) Automated script/CLI — a provisioning script creates the Keycloak org, DB tenant record, and first admin user in one step
C) Manual Keycloak setup + app sync — super-admin creates the org in Keycloak, then the app detects it on first login from that org
D) Other (please describe after [Answer]: tag below)

[Answer]: B

---

## Question 9
Should tenant-scoped data be physically separated or logically separated?

A) Logical separation — single database, `tenant_id` column on scoped tables (simpler, current single-DB approach extended)
B) Schema-per-tenant — single database, separate PostgreSQL schema per tenant (stronger isolation, more complex migrations)
C) Database-per-tenant — separate PostgreSQL database per tenant (strongest isolation, most complex)
D) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Question 10
How should the frontend handle tenant context?

A) Tenant is extracted from the JWT automatically after login — no tenant selection UI needed (user belongs to exactly one org)
B) Tenant selector at login — user picks their center before authenticating
C) Tenant selector after login — for users who belong to multiple organizations (e.g., a teacher at two centers)
D) Other (please describe after [Answer]: tag below)

[Answer]: B (in case there's same username across centers)

---

## Question 11
What happens when a user belongs to multiple Keycloak Organizations?

A) Not supported — each user belongs to exactly one organization. Enforce this at the Keycloak level
B) Supported — user sees a tenant switcher in the app to switch between their organizations
C) Supported — user's "primary" organization is used by default, with an option to switch
D) Other (please describe after [Answer]: tag below)

[Answer]: B. they need to specify which organization they are logging in to.

---

## Question 12
How should cross-tenant reporting work for SUPER_ADMIN?

A) SUPER_ADMIN can view aggregated reports across all tenants (e.g., total students, total classes per center)
B) SUPER_ADMIN can only view per-tenant reports by switching tenant context (no cross-tenant aggregation)
C) No cross-tenant reporting needed initially — SUPER_ADMIN just manages tenants and users
D) Other (please describe after [Answer]: tag below)

[Answer]: C super admin is me, admin is center's admin who will actually use the app, i just administer centers and users

---

## Question 13
Should the Keycloak realm export (`realm-export.json`) be updated to include Organizations configuration?

A) Yes — update the dev realm export to include a sample organization, org-level roles, and the `organization` token claim mapper
B) No — Organizations will be configured manually or via Keycloak Admin API at runtime
C) Both — realm export includes the Organizations feature enabled + a sample org for dev, but production orgs are created via API
D) Other (please describe after [Answer]: tag below)

[Answer]: C. production orgs for customers are created via api

---

## Question 14
How should the `created_by` pattern (from Sprint 5 teacher data isolation) interact with tenant scoping?

A) Replace `created_by` scoping with tenant scoping — tenant_id is the primary isolation mechanism, `created_by` becomes redundant within a tenant
B) Keep both — tenant_id for center-level isolation, `created_by` for teacher-level isolation within a tenant (defense in depth)
C) Merge — `created_by` is kept for audit purposes but not used for query filtering; tenant_id handles all isolation
D) Other (please describe after [Answer]: tag below)

[Answer]: B

---

## Question 15
What is the deployment model for multi-tenant?

A) Single deployment serves all tenants (shared infrastructure, logical isolation only)
B) Shared backend + separate frontend per tenant (each center gets its own URL/subdomain)
C) Fully shared — single backend, single frontend, tenant context from JWT (simplest)
D) Other (please describe after [Answer]: tag below)

[Answer]: B. different url thru cloudfront 

---
