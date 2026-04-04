# Sprint 6 — Requirement Clarification Questions

I detected ambiguities and a potential contradiction in your responses that need clarification before I can generate the requirements document.

---

## Ambiguity 1: SUPER_ADMIN Role Definition (Q4 + Q5 + Q12)

Your responses across Q4, Q5, and Q12 paint a clear picture of intent — you (the product operator) administer centers and users, while each center has its own ADMIN. But Q4 wasn't answered with a letter choice, so I need to confirm the implementation approach.

From your answers I understand:
- **You** = platform operator who creates/manages centers and their admin users
- **ADMIN** = center-level admin who manages their center's teachers, students, classes, etc.

### Clarification Question 1
What should the platform operator role be called, and how should it work in Keycloak?

A) Add a `SUPER_ADMIN` realm-level role in Keycloak. Users with this role are not scoped to any organization. They access a separate "Platform Admin" UI to manage tenants and create center admins. They do NOT use the regular SPM app features (no classes, scores, etc.)
B) No new role — the platform operator uses the Keycloak Admin Console directly to manage organizations and users. The SPM app only has tenant-scoped roles (ADMIN, TEACHER, STUDENT, PARENT)
C) Add a `SUPER_ADMIN` role but they CAN also operate within a tenant context (e.g., impersonate a center admin to troubleshoot)
D) Other (please describe after [Answer]: tag below)

[Answer]: i will just use the /admin from keycloak

---

## Contradiction 1: Tenant Selection vs Per-Tenant URL (Q10 + Q11 + Q15)

You chose:
- Q10: **B** — Tenant selector at login (user picks center before authenticating)
- Q11: **B** — Users can belong to multiple orgs, need to specify which one
- Q15: **B** — Separate frontend per tenant (different URL via CloudFront)

These conflict: if each center has its own URL/subdomain (Q15), the tenant is already known from the URL — there's no need for a tenant selector (Q10/Q11). A user at `center-a.example.com` is implicitly in Center A's tenant.

### Clarification Question 2
How should tenant context be determined?

A) **Per-tenant URL** — each center gets its own subdomain/URL (e.g., `center-a.spm.com`). The frontend passes the tenant identifier (derived from the URL) to Keycloak during login. No tenant selector needed. Users who work at multiple centers use different URLs.
B) **Single URL + tenant selector** — one shared frontend URL. After Keycloak login, if the user belongs to multiple orgs, they pick which one to use. The JWT's `organization` claim determines the tenant context.
C) **Hybrid** — per-tenant URLs for regular users (teachers, parents, students), but a single "platform admin" URL for the operator (you). Regular users always land on their center's URL. The operator uses a separate admin portal.
D) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Ambiguity 2: Keycloak Organizations Login Flow (Q3 + Q10)

You chose Keycloak Organizations' built-in `organization` claim (Q3:B) and tenant selector at login (Q10:B). Keycloak Organizations v26+ supports an org-specific login flow where the user selects their organization during the Keycloak login process itself (not in the SPM app). This is the native way to handle multi-org users.

### Clarification Question 3
Where should the organization selection happen?

A) **In Keycloak's login page** — Keycloak Organizations supports an org selection step during authentication. The user picks their org on the Keycloak login screen, and the resulting JWT contains the selected org in the `organization` claim. No custom UI needed in SPM.
B) **In the SPM app** — after Keycloak login, the SPM frontend reads the user's org memberships and shows a custom org picker. The selected org is sent as a header/parameter on API calls.
C) **Implicit from URL** — if using per-tenant URLs (from Clarification Q2 option A), the frontend passes the org ID to Keycloak as a login hint parameter (`kc_org`), so Keycloak auto-selects the org. No picker needed.
D) Other (please describe after [Answer]: tag below)

[Answer]: no need custom ui.

---

## Ambiguity 3: Provisioning Script Scope (Q8)

You chose B (automated script/CLI for tenant provisioning). Need to clarify what this script should do.

### Clarification Question 4
What should the provisioning script create?

A) **Full provisioning** — script creates: (1) Keycloak Organization, (2) DB `tenants` row, (3) first ADMIN user in Keycloak assigned to the org, (4) seeds default subjects/topics for the tenant in the DB
B) **Keycloak + DB only** — script creates: (1) Keycloak Organization, (2) DB `tenants` row. The center admin is created separately via the platform admin UI or another script call
C) **Keycloak only** — script creates the Keycloak Organization. The DB tenant record and seed data are auto-created on first login from that org (lazy provisioning)
D) Other (please describe after [Answer]: tag below)

[Answer]: A

---
