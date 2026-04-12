# Sprint 6 — Business Logic Model — Multi-Tenant Data Segregation

## 1. Tenant Resolution Flow

### Request Lifecycle

```
HTTP Request with JWT
    │
    ▼
Spring Security validates JWT (existing)
    │
    ▼
TenantResolutionFilter (new, runs after JWT auth)
    │
    ├── Extract "organization" claim from JWT
    │   Format: { "<org-id>": { "name": "<org-name>" } }
    │
    ├── Get first (and only) key from organization map = keycloakOrgId
    │
    ├── Lookup: tenantRepository.findByKeycloakOrgId(keycloakOrgId)
    │   - If not found → 403 Forbidden ("Unknown organization")
    │   - If tenant.isActive == false → 403 Forbidden ("Tenant is disabled")
    │
    ├── Set tenantContext.setTenantId(tenant.getId())
    │   Set tenantContext.setTenantName(tenant.getName())
    │
    └── Continue filter chain
    │
    ▼
Controller → Service → Repository (all use tenantContext.getTenantId())
```

### TenantContext (Request-Scoped Bean)

```java
@Component
@RequestScope
public class TenantContext {
    private UUID tenantId;
    private String tenantName;
    
    // getters and setters
    
    public UUID requireTenantId() {
        if (tenantId == null) throw new AppException(FORBIDDEN, "No tenant context");
        return tenantId;
    }
}
```

- Injected via constructor into services that need it
- `requireTenantId()` is the primary accessor — fails fast if tenant wasn't resolved
- Request-scoped = automatically cleaned up after request completes
- No ThreadLocal cleanup concerns

### TenantResolutionFilter

- Implements `OncePerRequestFilter`
- Runs AFTER Spring Security's JWT authentication filter
- Skips tenant resolution for unauthenticated endpoints (actuator health, etc.)
- Extracts organization claim, resolves to tenant, populates TenantContext
- On failure: returns 403 with JSON error response (consistent with existing error format)

---

## 2. Service Layer Changes

### Pattern: Every service method that reads/writes tenant-scoped data

```java
// BEFORE (current)
public Page<TuitionClass> getClassesByTeacher(UUID teacherId, Pageable pageable) {
    return classRepository.findByTeacherIdAndIsActiveTrue(teacherId, pageable);
}

// AFTER (with tenant scoping)
public Page<TuitionClass> getClassesByTeacher(UUID teacherId, Pageable pageable) {
    UUID tenantId = tenantContext.requireTenantId();
    return classRepository.findByTenantIdAndTeacherIdAndIsActiveTrue(tenantId, teacherId, pageable);
}
```

### Services requiring TenantContext injection:

| Service | Module |
|---|---|
| UserService | user |
| ClassService | classmanagement |
| SubjectService | subject |
| TestScoreService | testscore |
| FeedbackService | feedback |
| NotificationService | notification |
| ReportService / ReportContentGenerator | report |
| ProgressService | progress |
| TestPaperService | testpaper |
| ScheduleService | scheduling |
| CurrentUserService | auth |
| StudentAccessService | auth |

### Entity Creation Pattern

All service methods that create entities must set tenant from context:

```java
// BEFORE
TuitionClass newClass = new TuitionClass();
newClass.setName(request.getName());
// ...

// AFTER
TuitionClass newClass = new TuitionClass();
newClass.setTenant(tenantContext.requireTenant()); // or set via TenantAwareBaseEntity helper
newClass.setName(request.getName());
// ...
```

Option: `TenantAwareBaseEntity` could have a static factory or `@PrePersist` hook that reads from TenantContext. However, since we're using a request-scoped bean (not ThreadLocal), explicit setting in the service layer is cleaner and more testable.

---

## 3. Repository Layer Changes

### Pattern: Every query method gains a `tenantId` parameter

```java
// BEFORE
Page<TuitionClass> findByTeacherIdAndIsActiveTrue(UUID teacherId, Pageable pageable);

// AFTER
Page<TuitionClass> findByTenantIdAndTeacherIdAndIsActiveTrue(UUID tenantId, UUID teacherId, Pageable pageable);
```

### Repository Change Inventory

#### UserRepository
```
findByKeycloakId(String) → findByTenantIdAndKeycloakId(UUID, String)
findByEmail(String) → findByTenantIdAndEmail(UUID, String)
existsByEmail(String) → existsByTenantIdAndEmail(UUID, String)
```

#### TeacherRepository
```
findByUserId(UUID) → findByTenantIdAndUserId(UUID, UUID)
```

#### StudentRepository
```
findByUserId(UUID) → findByTenantIdAndUserId(UUID, UUID)
findByParentId(UUID) → findByTenantIdAndParentId(UUID, UUID)
findByCreatorOrTeacher(UUID, UUID) → add tenant_id to @Query WHERE clause
```

#### ParentRepository
```
findByUserId(UUID) → findByTenantIdAndUserId(UUID, UUID)
```

#### TuitionClassRepository
```
findByTeacherIdAndIsActiveTrue(UUID, Pageable) → findByTenantIdAndTeacherIdAndIsActiveTrue(UUID, UUID, Pageable)
countActiveStudents(UUID) → add tenant_id to @Query (via class_students join)
```

#### ClassStudentRepository
```
findByTuitionClassIdAndStudentId(UUID, UUID) → findByTenantIdAndTuitionClassIdAndStudentId(UUID, UUID, UUID)
existsByTuitionClassIdAndStudentIdAndStatus(UUID, UUID, EnrollmentStatus) → add tenantId
findByTuitionClassId(UUID) → findByTenantIdAndTuitionClassId(UUID, UUID)
findByTuitionClassIdAndStatus(UUID, EnrollmentStatus) → add tenantId
existsByStudentIdAndTeacherId(UUID, UUID) → add tenant_id to @Query
findByStudentIdAndStatus(UUID, EnrollmentStatus) → findByTenantIdAndStudentIdAndStatus(UUID, UUID, EnrollmentStatus)
```

#### SubjectRepository
```
findByIsActiveTrue() → findByTenantIdAndIsActiveTrue(UUID)
existsByCode(String) → existsByTenantIdAndCode(UUID, String)
```

#### TopicRepository
```
existsBySubjectIdAndCode(UUID, String) → existsByTenantIdAndSubjectIdAndCode(UUID, UUID, String)
```

#### TestScoreRepository
```
findByStudentWithFilters(...) → add tenant_id to @Query WHERE clause
findByStudentIdOrderByTestDateAsc(UUID) → add tenant_id to @Query
findByClassIdOrderByTestDateAsc(UUID) → add tenant_id to @Query
```

#### FeedbackRepository
```
findByTestScoreId(UUID) → findByTenantIdAndTestScoreId(UUID, UUID)
findRecentByStudentAndTeacher(...) → add tenant_id to @Query
```

#### FeedbackTemplateRepository
```
findByTeacherOrSystemWide(UUID, FeedbackCategory) → add tenant_id to @Query (system-wide is tenant-scoped)
```

#### NotificationRepository
```
findByUserIdAndStatus(UUID, NotificationStatus, Pageable) → add tenantId
findByUserId(UUID, Pageable) → add tenantId
findByStatus(NotificationStatus) → findByTenantIdAndStatus(UUID, NotificationStatus)
```

#### ProgressReportRepository
```
findByStudentId(UUID, Pageable) → findByTenantIdAndStudentId(UUID, UUID, Pageable)
```

#### ClassScheduleRepository
```
findByTuitionClassId(UUID) → findByTenantIdAndTuitionClassId(UUID, UUID)
findActiveByClassId(UUID) → add tenant_id to @Query
existsActiveRecurringByClassIdAndDayOfWeek(UUID, int) → add tenant_id to @Query
```

#### ClassSessionRepository
```
findByTuitionClassId(UUID, Pageable) → add tenantId
findByClassIdFiltered(...) → add tenant_id to @Query
findUpcomingForTeacher(UUID, LocalDate, Pageable) → add tenant_id to @Query
findUpcomingForStudent(UUID, LocalDate, Pageable) → add tenant_id to @Query
findUpcomingForParent(UUID, LocalDate, Pageable) → add tenant_id to @Query
findAllUpcoming(LocalDate, Pageable) → findByTenantIdAndUpcoming(UUID, LocalDate, Pageable)
existsByScheduleIdAndSessionDate(UUID, LocalDate) → add tenantId
findLastSessionDateByScheduleId(UUID) → add tenant_id to @Query
findByTuitionClassIdAndSessionDateAndStatusNot(...) → add tenantId
cancelFutureSessionsBySchedule(...) → add tenant_id to @Query
findByClassIdWithNotes(UUID, Pageable) → add tenant_id to @Query
```

#### SessionAttendanceRepository
```
findBySessionId(UUID) → findByTenantIdAndSessionId(UUID, UUID)  
findBySessionIdAndStudentId(UUID, UUID) → add tenantId
findByStudentAndClass(...) → add tenant_id to @Query
findByClassId(...) → add tenant_id to @Query
countSessionsWithAttendance(...) → add tenant_id to @Query
```

#### TestPaperUploadRepository
```
findByStudentId(UUID) → findByTenantIdAndStudentId(UUID, UUID)
linkToTestScore(List<UUID>, UUID) → add tenant_id to @Query
```

#### TenantRepository (NEW)
```
findByKeycloakOrgId(String keycloakOrgId) → Optional<Tenant>
findByName(String name) → Optional<Tenant>
existsByKeycloakOrgId(String keycloakOrgId) → boolean
```

---

## 4. CurrentUserService Updates

### provisionFromToken() Changes

```
1. Extract organization claim from JWT
2. Resolve keycloakOrgId → Tenant (same as TenantResolutionFilter)
3. When creating new User: set user.setTenant(tenant)
4. When linking existing user by email: validate tenant matches
   - If existing user has different tenant → reject (403, "Email belongs to another center")
   - If existing user has same tenant → proceed with linking
5. When creating profile records (Teacher, Student, Parent): propagate tenant from User
```

### Email Linking Tenant Validation

```
provisionFromToken(jwt):
    tenant = tenantContext.requireTenant()
    existingUser = userRepository.findByTenantIdAndEmail(tenant.id, email)
    
    if existingUser present:
        // Same tenant, proceed with linking (existing behavior)
        link keycloakId to existing user
    else:
        // No user with this email in this tenant — create new
        create new User with tenant
```

Note: We no longer need to check globally for email conflicts since email uniqueness is per-tenant.

---

## 5. Provisioning CLI (Spring Boot CommandLineRunner)

### Input: Tenant Config File

The CLI reads tenant definitions from a config file. Two sources depending on environment:

- **Local dev / non-AWS**: JSON file on disk (e.g., `spm/tenants.json`)
- **AWS environments**: AWS SSM Parameter Store (read at startup)

The CLI accepts a `--source` flag:

```bash
# Local: read from file
java -jar spm.jar --spring.profiles.active=cli --source=file --config=tenants.json

# AWS: read from Parameter Store
java -jar spm.jar --spring.profiles.active=cli --source=ssm --ssm-prefix=/spm/tenants
```

### Config File Format (tenants.json)

```json
{
  "tenants": [
    {
      "name": "ABC Tuition Center",
      "slug": "center-abc",
      "adminEmail": "[email]",
      "adminPassword": "tempPass123",
      "adminFirstName": "Admin",
      "adminLastName": "User"
    },
    {
      "name": "XYZ Learning Hub",
      "slug": "center-xyz",
      "adminEmail": "[email]",
      "adminPassword": "tempPass456",
      "adminFirstName": "Admin",
      "adminLastName": "User"
    }
  ]
}
```

### SSM Parameter Store Layout (AWS)

```
/spm/tenants/center-abc/name           = "ABC Tuition Center"
/spm/tenants/center-abc/admin-email    = "[email]"
/spm/tenants/center-abc/admin-password = "tempPass123"  (SecureString)
/spm/tenants/center-abc/admin-first    = "Admin"
/spm/tenants/center-abc/admin-last     = "User"

/spm/tenants/center-xyz/name           = "XYZ Learning Hub"
/spm/tenants/center-xyz/admin-email    = "[email]"
/spm/tenants/center-xyz/admin-password = "tempPass456"  (SecureString)
/spm/tenants/center-xyz/admin-first    = "Admin"
/spm/tenants/center-xyz/admin-last     = "User"
```

The CLI lists all parameters under the `--ssm-prefix`, groups by tenant slug, and builds the same tenant definition list as the JSON file.

### Flow (per tenant in the config)

```
For each tenant definition in config:
    │
    ├── 1. Create Keycloak Organization via Admin REST API
    │   POST /admin/realms/spm/organizations
    │   Body: { "name": "<slug>", "displayName": "<name>" }
    │   → Returns org ID
    │
    ├── 2. Create Tenant row in DB
    │   INSERT INTO tenants (id, name, keycloak_org_id, is_active)
    │   VALUES (gen_uuid, '<name>', '<kc-org-id>', true)
    │
    ├── 3. Create Admin user in Keycloak
    │   POST /admin/realms/spm/users
    │   Body: { email, firstName, lastName, enabled, credentials: [{temporary: true}] }
    │   → Assign to organization
    │   → Assign ADMIN realm role
    │
    ├── 4. Create Admin User row in DB
    │   INSERT INTO users (tenant_id, keycloak_id, email, ..., roles: [ADMIN])
    │
    └── 5. Seed default subjects and topics for this tenant
        INSERT INTO subjects (tenant_id, code, name, ...) — standard catalog
        INSERT INTO topics (tenant_id, subject_id, code, name, ...) — per subject
```

### Idempotency

- Check `tenants.name` before creating (skip if exists)
- Check Keycloak org by name before creating (skip if exists)
- Check user by email+tenant before creating (skip if exists)
- Check subjects by code+tenant before seeding (skip if exists)
- Safe to run on every deploy — only provisions new tenants

### Implementation

- Spring Boot app with `@Profile("cli")` or separate `main()` method
- Reuses existing `TenantRepository`, `UserRepository`, `SubjectRepository`
- Uses Keycloak Admin REST API client (keycloak-admin-client library or raw HTTP)
- `TenantConfigSource` interface with two implementations:
  - `FileTenantConfigSource` — reads from JSON file on disk
  - `SsmTenantConfigSource` — reads from AWS SSM Parameter Store
- Activated by `--source=file` or `--source=ssm`

---

## 6. Frontend Tenant Context

### Login Flow with kc_org

```
1. User navigates to center-a.spm.com
2. Frontend loads config.json (contains keycloakUrl, realm, clientId)
3. Frontend extracts tenant slug from URL hostname (e.g., "center-a")
4. keycloakService.login() passes kc_org=<tenant-slug> as login hint
5. Keycloak auto-selects the organization → user authenticates
6. JWT contains "organization" claim: { "<org-id>": { "name": "Center A" } }
7. Frontend extracts org info from JWT tokenParsed.organization
8. Stores in TenantContext React provider: { orgId, orgName }
9. Navbar displays orgName (from JWT, not config.json)
```

### keycloakService Changes

```typescript
// New: pass kc_org hint during login
login(): void {
    const tenantSlug = getTenantSlugFromUrl(); // extract from hostname
    keycloak!.login({ 
        redirectUri: window.location.origin + '/',
        idpHint: undefined,
        // Keycloak 26+ supports kc_org as login option
        // Pass via loginOptions or URL parameter
    });
}

// New: extract organization from token
getOrganization(): { orgId: string; orgName: string } | null {
    const org = keycloak?.tokenParsed?.organization;
    if (!org) return null;
    const [orgId, details] = Object.entries(org)[0];
    return { orgId, orgName: (details as any).name };
}
```

### TenantProvider (React Context)

```typescript
interface TenantContextType {
    orgId: string;
    orgName: string;
}

// Populated after successful Keycloak login from JWT organization claim
// Used by Navbar for center name display
// Available to any component that needs tenant identity
```

### Navbar Change

```
// BEFORE
<Link ...>{getConfig().tenantName}</Link>

// AFTER  
const { orgName } = useTenant();
<Link ...>{orgName}</Link>
```

Falls back to config.json tenantName if JWT org claim is unavailable (shouldn't happen in normal flow, but defensive).


---

## 7. Provisioning — Infrastructure & Automation Notes

> For the infra/ops team. The provisioning CLI supports two config sources out of the box.

### Config-Driven Provisioning (Active Approach)

The CLI reads tenant definitions from a config source and provisions any that don't already exist. It's idempotent — safe to run on every deploy.

**Local dev**: `tenants.json` file checked into the repo at `spm/tenants.json`. Add a new tenant entry, run the CLI, done.

**AWS environments**: Tenant definitions stored in SSM Parameter Store under `/spm/tenants/*`. Admin passwords use `SecureString` type. The deploy pipeline runs the CLI with `--source=ssm` after each deploy.

### SSM Parameter Store Layout

```
/spm/tenants/center-abc/name           = "ABC Tuition Center"
/spm/tenants/center-abc/admin-email    = "[email]"
/spm/tenants/center-abc/admin-password = "tempPass123"  (SecureString)
/spm/tenants/center-abc/admin-first    = "Admin"
/spm/tenants/center-abc/admin-last     = "User"
```

To onboard a new customer: add their parameters to SSM, then either re-deploy or manually trigger the provisioning CLI. Idempotency ensures existing tenants are skipped.

### Deploy Pipeline Integration

Add a post-deploy step to the GitHub Actions workflow:

```yaml
- name: Provision tenants
  run: |
    java -jar spm.jar --spring.profiles.active=cli --source=ssm --ssm-prefix=/spm/tenants
```

This runs after the backend deploy, reads all tenant definitions from Parameter Store, and provisions any new ones. Existing tenants are skipped automatically.
