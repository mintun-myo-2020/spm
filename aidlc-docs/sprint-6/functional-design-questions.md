# Sprint 6 — Functional Design Clarification Questions

Please answer the following questions to finalize the functional design for multi-tenant data segregation.

## Question 1
The current `User.email` has a unique constraint globally. With multi-tenancy, should the same email be allowed across different tenants (e.g., the same teacher works at two centers)?

A) Yes — email uniqueness should be per-tenant (same email can exist in different tenants)
B) No — email must remain globally unique across all tenants
C) Other (please describe after [Answer]: tag below)

[Answer]: isn't a "user" primary key = tenantid+user.email? so it will always be unique as long as diff tenant(center)

## Question 2
For the `TenantContext` implementation, which approach do you prefer for making the current tenant available throughout the request?

A) ThreadLocal-based holder (simple, works with synchronous Spring MVC — `TenantContext.getCurrentTenantId()`)
B) Request-scoped Spring bean (injected via constructor — `@RequestScope TenantContext`)
C) Other (please describe after [Answer]: tag below)

[Answer]: B

## Question 3
The `FeedbackTemplate` entity has an `isSystemWide` flag for templates shared across all teachers. With multi-tenancy, should system-wide templates be scoped to a tenant (each tenant has their own "system-wide" templates), or truly global across all tenants?

A) Tenant-scoped — "system-wide" means visible to all teachers within the same tenant only
B) Truly global — shared across all tenants (no tenant_id on system-wide templates)
C) Other (please describe after [Answer]: tag below)

[Answer]: what is this this template sharing thing even for?

## Question 4
The `NotificationRepository.findByStatus()` method currently returns notifications across all users (used for background processing of pending notifications). With multi-tenancy, should background jobs process notifications across all tenants, or per-tenant?

A) Cross-tenant — background job processes all pending notifications regardless of tenant (simpler, single shared backend)
B) Per-tenant — background job filters by tenant (more isolated but adds complexity)
C) Other (please describe after [Answer]: tag below)

[Answer]: why add complexity? isnt it just add 1 filter

## Question 5
For the tenant resolution cache (JWT org claim → tenant_id lookup), what cache eviction strategy do you prefer?

A) Simple TTL-based (e.g., cache for 5 minutes, then re-lookup from DB)
B) Cache until application restart (tenants rarely change)
C) No cache — DB lookup per request is fine for now (simplest, optimize later if needed)
D) Other (please describe after [Answer]: tag below)

[Answer]: C

## Question 6
The `ClassSessionRepository` has an `findAllUpcoming()` method used by the ADMIN role to see all upcoming sessions. With multi-tenancy, should this be automatically scoped to the admin's tenant?

A) Yes — admin sees only their tenant's sessions (consistent with all other tenant scoping)
B) No — keep a cross-tenant view for platform operators
C) Other (please describe after [Answer]: tag below)

[Answer]: A. Why would B even make sense? platform operator is superadmin right ? or no?

## Question 7
For the provisioning CLI script, what technology should it use?

A) Shell script (bash) calling Keycloak Admin REST API via curl + psql for DB operations
B) Java CLI application (Spring Boot with `CommandLineRunner`) reusing existing service layer
C) Python script using requests library for Keycloak API + psycopg2 for DB
D) Other (please describe after [Answer]: tag below)

[Answer]: B

## Question 8
The frontend currently derives `tenantName` from `config.json`. With per-tenant URLs, should the frontend also extract the Keycloak organization info (org ID, org name) from the JWT after login for display and API purposes?

A) Yes — extract org info from JWT `organization` claim after login and store in React context (source of truth for tenant identity)
B) No — continue using `config.json` tenantName only (simpler, org info stays backend-only)
C) Both — config.json for display name, JWT org claim for tenant ID verification
D) Other (please describe after [Answer]: tag below)

[Answer]: A — but with a constraint: use JWT as the identity source, not blindly as the display source.
