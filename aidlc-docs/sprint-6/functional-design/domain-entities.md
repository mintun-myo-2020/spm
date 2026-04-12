# Sprint 6 — Domain Entities — Multi-Tenant Data Segregation

## New Entity: Tenant

```
Tenant
├── id: UUID (PK, auto-generated)
├── name: String (NOT NULL) — display name of the tuition center
├── keycloakOrgId: String (UNIQUE, NOT NULL) — maps to Keycloak Organization ID
├── isActive: boolean (NOT NULL, default true)
├── createdAt: Instant (NOT NULL, auto-set)
├── updatedAt: Instant (NOT NULL, auto-set)
```

Table: `tenants`
- Does NOT extend BaseEntity (standalone lifecycle, no tenant_id on itself)
- `keycloak_org_id` has a unique index for fast lookup during tenant resolution
- `is_active` allows soft-disabling a tenant without deleting data

---

## New Abstract Class: TenantAwareBaseEntity

Extends `BaseEntity`, adds `tenant_id` column.

```
TenantAwareBaseEntity extends BaseEntity
├── tenant: Tenant (ManyToOne, NOT NULL, FK to tenants.id)
```

JPA mapping:
- `@ManyToOne(fetch = FetchType.LAZY)` 
- `@JoinColumn(name = "tenant_id", nullable = false, updatable = false)`
- `updatable = false` — tenant assignment is immutable after creation

---

## Entity Inheritance Changes

### Entities that change from `extends BaseEntity` to `extends TenantAwareBaseEntity`:

| Entity | Module | Table |
|---|---|---|
| User | user | users |
| Teacher | user | teachers |
| Student | user | students |
| Parent | user | parents |
| TuitionClass | classmanagement | classes |
| ClassStudent | classmanagement | class_students |
| Subject | subject | subjects |
| Topic | subject | topics |
| TestScore | testscore | test_scores |
| Question | testscore | questions |
| SubQuestion | testscore | sub_questions |
| Feedback | feedback | feedback |
| FeedbackTemplate | feedback | feedback_templates |
| Notification | notification | notifications |
| ProgressReport | report | progress_reports |
| ClassSchedule | scheduling | class_schedules |
| ClassSession | scheduling | class_sessions |
| SessionAttendance | scheduling | session_attendance |
| TestPaperUpload | testpaper | test_paper_uploads |
| TestPaperPage | testpaper | test_paper_pages |

Total: 20 entities gain `tenant_id`

### Entities that remain `extends BaseEntity` (no tenant_id):
- None — all domain entities are tenant-scoped

### Standalone entity (no BaseEntity):
- `Tenant` — the tenant table itself

---

## Email Uniqueness Change

Current: `User.email` has a global unique constraint (`@Column(unique = true)`)

New: Composite unique constraint on `(tenant_id, email)` — same email can exist in different tenants.

JPA: Replace `@Column(unique = true)` on email with `@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "email"}))` on the User entity.

Keycloak: Email uniqueness within a Keycloak Organization is handled by Keycloak itself (users are scoped to orgs). The DB constraint is a defense-in-depth measure.

---

## Composite Indexes

Every table with `tenant_id` gets a composite index with `tenant_id` as the leading column on frequently queried columns:

| Table | Index | Columns |
|---|---|---|
| users | idx_users_tenant_email | (tenant_id, email) — UNIQUE |
| users | idx_users_tenant_keycloak | (tenant_id, keycloak_id) |
| teachers | idx_teachers_tenant_user | (tenant_id, user_id) |
| students | idx_students_tenant_user | (tenant_id, user_id) |
| students | idx_students_tenant_created_by | (tenant_id, created_by) |
| parents | idx_parents_tenant_user | (tenant_id, user_id) |
| classes | idx_classes_tenant_teacher | (tenant_id, teacher_id) |
| class_students | idx_cs_tenant_class | (tenant_id, class_id) |
| subjects | idx_subjects_tenant_active | (tenant_id, is_active) |
| subjects | idx_subjects_tenant_code | (tenant_id, code) — UNIQUE |
| topics | idx_topics_tenant_subject | (tenant_id, subject_id) |
| test_scores | idx_ts_tenant_student | (tenant_id, student_id) |
| test_scores | idx_ts_tenant_class | (tenant_id, class_id) |
| feedback | idx_feedback_tenant_student | (tenant_id, student_id) |
| feedback_templates | idx_ft_tenant_teacher | (tenant_id, teacher_id) |
| notifications | idx_notif_tenant_user_status | (tenant_id, user_id, status) |
| notifications | idx_notif_tenant_status | (tenant_id, status) |
| progress_reports | idx_pr_tenant_student | (tenant_id, student_id) |
| class_schedules | idx_sched_tenant_class | (tenant_id, class_id) |
| class_sessions | idx_sess_tenant_class | (tenant_id, class_id) |
| test_paper_uploads | idx_tpu_tenant_student | (tenant_id, student_id) |

---

## Subject Code Uniqueness Change

Current: `Subject.code` has a global unique check (`existsByCode()`)

New: Composite unique constraint on `(tenant_id, code)` — same subject code can exist in different tenants (each tenant gets their own copy of subjects seeded on provisioning).

