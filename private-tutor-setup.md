# Private Tutor Setup Guide

## How It Works Today

A private tutor only needs the TEACHER role. No admin role required.

Teachers can already:
- Create classes and assign themselves as teacher
- Create students and parents
- Enroll students into classes
- Record test scores with topic breakdowns
- Add feedback
- View student progress charts
- Create and manage subjects and topics
- Manage class schedules and attendance
- Generate progress reports

The only things that require ADMIN (that a private tutor doesn't need):
- Deactivating subjects/topics/users (cleanup operations)
- Viewing ALL classes across all teachers (irrelevant for a solo tutor)
- Changing a class's assigned teacher (irrelevant for a solo tutor)

## Setup Steps

### 1. Create the teacher in Keycloak (done by you, the platform operator)

In the Keycloak admin console:
- Create a new user (email, first name, last name)
- Set a temporary password (Keycloak → User → Credentials → Set Password → toggle "Temporary" ON)
- Go to Role Mappings → assign the `TEACHER` realm role
- Tell the tutor: "Your login is [email], password is [temp password]. You'll be asked to change it on first login."

### 2. First login (auto-provisioning)

When the tutor logs in for the first time:
- Keycloak forces them to change their temporary password
- The app's `autoProvisionUser()` reads the JWT, sees the TEACHER role
- Automatically creates a `User` record + `Teacher` profile in the DB
- No manual DB setup needed

### 3. What the tutor sees

The teacher sidebar:
- Dashboard — overview of their classes
- My Classes — create classes, manage students, record scores
- Subjects — create and manage subjects/topics

### 4. Typical workflow

1. Log in → lands on teacher dashboard
2. Create a subject (e.g. "Math") and topics (e.g. "Algebra", "Geometry")
3. Create a class (e.g. "Sec 3 Math")
4. Enroll students — from the class detail page, click "Enroll Student":
   - Select an existing student from the dropdown, OR
   - Click "+ Create new student" to create one inline (name, email, password, grade) and auto-enroll them
5. Optionally set up a class schedule
6. After each lesson: record test scores, add feedback
7. Parents: created via the admin API (`POST /api/v1/users/parents` with the student's ID) — no teacher UI for this yet, but the API accepts TEACHER role
8. Parents log in and see their child's progress dashboard

### 5. What's slightly awkward (but works)

- The teacher sidebar says "My Classes" — fine for a solo tutor, but the wording implies they're one of many teachers
- No deactivate buttons for subjects/topics — if they create something wrong, they can edit it but not remove it

---

# Multi-Tenancy Roadmap

## Current State

No multi-tenancy. All data lives in one flat database with no `centre_id` or tenant isolation. Role-based access (teacher sees own classes, parent sees own child) provides functional isolation but not data isolation. An ADMIN user sees all data globally.

## Phase 1: Centre Entity + FK (Backend)

Estimated effort: 3-4 days

### 1.1 Create Centre entity and table

```sql
CREATE TABLE centres (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,  -- for URL/subdomain
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

### 1.2 Add centre_id FK to core tables

Tables that need `centre_id`:
- `users` — every user belongs to a centre
- `teachers` — inherited through user, but explicit FK simplifies queries
- `students` — same
- `parents` — same
- `classes` — each class belongs to a centre
- `subjects` — per-centre subjects (with global defaults)

Tables that DON'T need it (they cascade through parent entities):
- `class_students` — scoped through class
- `test_scores` — scoped through class
- `questions`, `sub_questions` — scoped through test_score
- `feedback` — scoped through test_score
- `class_schedules`, `class_sessions`, `session_attendance` — scoped through class
- `notifications` — scoped through user

### 1.3 Migration to backfill existing data

```sql
-- Create a default centre for existing data
INSERT INTO centres (id, name, slug) VALUES ('default-centre-uuid', 'Default Centre', 'default');

-- Backfill
ALTER TABLE users ADD COLUMN centre_id UUID REFERENCES centres(id);
UPDATE users SET centre_id = 'default-centre-uuid';
ALTER TABLE users ALTER COLUMN centre_id SET NOT NULL;

-- Repeat for classes, subjects, etc.
```

### 1.4 Update entities

Add `@ManyToOne Centre centre` to User, TuitionClass, Subject entities.

## Phase 2: Scope Admin Queries (Backend)

Estimated effort: 2-3 days

### 2.1 Resolve centre from current user

Add `getCentreId()` to `CurrentUserService` — reads the logged-in user's `centre_id`.

### 2.2 Update admin repository queries

Every admin-facing query needs a `WHERE centre_id = :centreId` filter:
- `UserController` — list users, create users
- `ClassController` — list all classes (admin view)
- `SubjectController` — list subjects, create subjects
- `SessionController` — upcoming sessions (admin sees all in their centre, not globally)
- `ReportController` — reports scoped to centre
- `NotificationController` — notifications scoped to centre

Teacher/student/parent queries are already naturally scoped through class ownership and enrollment — these mostly don't change.

### 2.3 Subjects strategy

Decision needed: global defaults + per-centre custom, or fully isolated?

Recommended: Global defaults (is_global=true) visible to all centres, plus per-centre custom subjects. Centres can't edit global subjects but can add their own.

## Phase 3: Centre Onboarding (Backend + Frontend)

Estimated effort: 2-3 days

### 3.1 Centre creation API

- POST /api/v1/centres — creates a centre + its first admin user
- Could be a super-admin endpoint or a self-service signup flow

### 3.2 Keycloak integration

Options:
- Single realm, custom `centre_id` attribute on Keycloak users
- Multiple realms per centre (more isolated but harder to manage)

Recommended: Single realm with `centre_id` user attribute. Simpler, and the backend handles data isolation.

### 3.3 Frontend onboarding flow

- Sign-up page: centre name, admin email, password
- Creates centre + admin user atomically
- Redirects to admin dashboard with empty state prompts ("Add your first class", "Add students")

## Phase 4: Frontend Scoping

Estimated effort: 1-2 days

- Admin views already work — just need to ensure API responses are scoped (backend handles this)
- Add centre name/branding to the navbar
- Optional: subdomain routing (mathtutor.yourapp.com) — nice to have, not required

## Total Estimated Effort

| Phase | Effort | Priority |
|-------|--------|----------|
| Phase 1: Centre entity + FK | 3-4 days | When centre #2 signs up |
| Phase 2: Scope admin queries | 2-3 days | Same time as Phase 1 |
| Phase 3: Centre onboarding | 2-3 days | Can defer if manually creating centres |
| Phase 4: Frontend scoping | 1-2 days | Same time as Phase 2 |
| Total | ~8-12 days | |

## Recommendation

Don't build multi-tenancy until you have a second centre ready to onboard. For now:
- Private tutors: TEACHER+ADMIN dual role, works today
- Single centre: works today as-is
- Second centre: that's when you build Phase 1+2, manually create the centre in DB
- Self-service signup: Phase 3, only when you have enough demand to justify it
