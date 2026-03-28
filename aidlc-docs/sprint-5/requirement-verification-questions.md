# Sprint 5 — Requirement Verification Questions

## Critical Gaps Identified

### Q1: Platform Operator Account Creation
**Problem**: There is currently no way for you (the platform operator) to create a teacher/admin account without directly accessing the Keycloak admin console. This is not viable for onboarding real users.

**Options**:
- A) Add a SUPER_ADMIN role (platform-level) that can create teachers and admins via the app UI. This user is you.
- B) Build a self-service signup page where a tutor can register themselves (picks TEACHER role, creates their own account)
- C) Keep using Keycloak admin console (only works if you have access to the server)

**Recommendation**: Option B for MVP — a simple signup page. Tutor goes to `/signup`, enters name + email + password, gets a TEACHER account. No approval needed for now. You can add approval later.

**[Answer]**: i want to create users for each user. that way i can disable the user as needed

---

### Q2: Data Isolation Between Users
**Problem**: Right now all data is in one flat database. If you onboard a private tutor AND a tuition centre:
- The centre's ADMIN can see ALL students, ALL classes, ALL scores globally
- The tutor's students are visible to the centre admin
- The tutor can see the centre's students in the "enroll student" dropdown

This is a hard blocker for having multiple customers.

**Options**:
- A) Build full multi-tenancy now (centre_id on all tables, ~1-2 weeks)
- B) Deploy separate instances per customer (separate DB + Keycloak realm per customer, ops overhead but zero code changes)
- C) Accept the risk for now — only onboard one customer at a time, manually clean up between demos

**Recommendation**: Option B for immediate testing (spin up a separate instance per customer). Option A when you have 3+ customers and the ops overhead becomes painful.

**[Answer]**: B. so each customer = new instance setup on aws right?

---

### Q3: Self-Service Signup vs Operator-Created Accounts
**Problem**: The current flow requires someone with Keycloak admin access to create every teacher account. This doesn't scale.

For the MVP, who should be able to create accounts?
- Teachers create their own account (self-service signup)
- Teachers create students and parents (already works via the app)
- Centre admins create teachers (already works via admin UI)

The missing piece is: how does the first teacher/admin get created?

**Options**:
- A) Self-service signup page (anyone can register as TEACHER)
- B) Invite link system (you generate a link, tutor clicks it to register)
- C) You create them via a simple CLI/script that calls the Keycloak Admin API

**Recommendation**: Option A is simplest for MVP. Add rate limiting and maybe email verification later.

**[Answer]**: how do i handle the email and password? 

---

## Summary of Blockers

| Issue | Severity | Blocks |
|-------|----------|--------|
| No way to create teacher accounts without Keycloak console | Critical | Onboarding any new customer |
| No data isolation between customers | Critical | Onboarding multiple customers |
| No self-service signup | High | Scaling beyond manual onboarding |

## Decisions Made

### Q1: Platform Operator Account Creation
**Decision**: Operator (you) creates all teacher/admin accounts via the existing admin UI. Each customer instance has you as the ADMIN. No self-service signup for now.

**Flow**:
1. Deploy a new instance for the customer
2. You log in as ADMIN (seeded during instance setup)
3. Go to Users → Create User → TEACHER role → set temporary password
4. Share credentials with the teacher
5. Teacher logs in → forced password change → ready to use

### Q2: Data Isolation
**Decision**: Option B — separate instance per customer. Each customer gets their own backend + DB + Keycloak realm on AWS. Zero code changes needed. Multi-tenancy (Option A) deferred until 3+ customers.

**Per-instance setup**:
- Separate ECS task / EC2 instance / docker-compose
- Separate PostgreSQL database
- Separate Keycloak realm (can share one Keycloak server with multiple realms)
- Separate frontend deployment (different VITE_TENANT_NAME, VITE_API_BASE_URL)

### Q3: Email and Password Handling
**Decision**: You set a temporary password when creating the user. Keycloak forces password change on first login. The teacher then creates their own students/parents with temporary passwords too. Everyone changes their password on first login.

**No self-service signup needed** — you are the gatekeeper for teacher/admin accounts. Teachers are the gatekeeper for student/parent accounts.
