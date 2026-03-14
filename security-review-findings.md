# SPM Security Review Findings

**Date**: 2026-03-15
**Scope**: Full-stack review — backend (Spring Boot + Keycloak integration), frontend (React + keycloak-js)

---

## HIGH Severity

### 1. Deactivated users can still authenticate and use the API

**Files**: `CurrentUserService.java`, `SecurityConfig.java`

`getCurrentUser()` never checks `user.isActive()`. When an admin deactivates a user via `UserService.deactivateUser()`, the user retains full API access until their JWT expires. On next login, `provisionFromToken()` re-links them without checking active status. There is also no corresponding Keycloak-side disable — the user remains `enabled: true` in Keycloak.

**Fix**: Add an `isActive` check in `getCurrentUser()` that throws `FORBIDDEN` if the user is deactivated. Also call the Keycloak Admin API to set `enabled: false` when deactivating, and `enabled: true` when reactivating.

---

### 2. Email-based account takeover via provisionFromToken

**File**: `CurrentUserService.java`

`provisionFromToken()` matches by email and links any Keycloak account to an existing DB user record. If an attacker obtains a Keycloak account with the same email as an admin-created user (e.g. if self-registration is ever enabled, or via Keycloak admin access), they inherit that user's DB record — including overwriting `firstName`, `lastName`, and `roles`.

**Fix**: Only do email-based linking for users whose `keycloakId` starts with `"pending-"` (the marker set by admin creation). If the existing user already has a real keycloakId, reject the link.

---

### 3. Silent role assignment failure

**File**: `KeycloakAdminService.java`

`assignRealmRole()` catches all exceptions and only logs a warning. If role assignment fails, the user is created in Keycloak with no role. They can then log in and `provisionFromToken` may assign them a default or empty role set.

**Fix**: Make role assignment failure throw an exception so the entire user creation rolls back (Keycloak user gets deleted via the existing rollback logic in `UserService`).

---

### 4. Client secret committed to git

**Files**: `realm-export.json`, `docker-compose.yml`

`spm-backend-secret` is hardcoded in `realm-export.json` (`"secret": "spm-backend-secret"`) and used as the default in `docker-compose.yml` (`KEYCLOAK_ADMIN_CLIENT_SECRET: ${KEYCLOAK_ADMIN_CLIENT_SECRET:-spm-backend-secret}`). Both files are committed to git.

**Fix**: For dev this is acceptable, but document that production must override this value. Consider generating a random secret in `realm-export.json` or using Keycloak's client secret rotation. Remove the hardcoded default from `docker-compose.yml` so it fails loudly if the env var is missing.

---

## MEDIUM Severity

### 5. Error message leaks internal details

**File**: `KeycloakAdminService.java`

Exception messages from Keycloak HTTP calls are included in `AppException` messages: `"Failed to create Keycloak user: " + e.getMessage()` and `"Keycloak authentication failed: " + e.getMessage()`. These propagate to the API response via `GlobalExceptionHandler` and can expose internal URLs, stack traces, or Keycloak error details to the client.

**Fix**: Log the full exception server-side, return a generic message to the client (e.g. `"User creation failed"`).

---

### 6. Admin token not cached

**File**: `KeycloakAdminService.java`

`getAdminToken()` makes a full HTTP round-trip to Keycloak's token endpoint on every single user creation call. No caching of the `client_credentials` token.

**Fix**: Cache the token and reuse it until near expiry. Parse the `expires_in` from the token response and refresh proactively (e.g. at 80% of TTL).

---

### 7. CORS allows all headers

**File**: `SecurityConfig.java`

```java
config.setAllowedHeaders(List.of("*"));
```

This is overly permissive. While `allowCredentials: true` prevents `allowedOrigins: *`, the wildcard headers still broadens the attack surface.

**Fix**: Restrict to specific headers: `Authorization`, `Content-Type`, `Accept`, `X-Requested-With`.

---

### 8. No rate limiting on user creation endpoints

**Files**: `UserController.java`, `KeycloakAdminService.java`

The create teacher/student/parent endpoints have no rate limiting. An authenticated admin could spam user creation, flooding both the DB and Keycloak.

**Fix**: Add rate limiting via Spring's `RateLimiter` or a filter. Even a simple per-IP or per-user throttle would help.

---

## LOW Severity

### 9. Frontend role checks are client-side only (by design)

**Files**: `keycloakService.ts`, `DashboardRedirect.tsx`, `AuthContext.tsx`

`hasRole()` reads from the JWT on the client. This is fine for UI routing, and the backend enforces roles via `@PreAuthorize` — so this is defense-in-depth, not a vulnerability. Just noting it for completeness.

---

### 10. 5xx retry in apiClient could amplify issues

**File**: `apiClient.ts`

The response interceptor retries up to 2 times on 5xx or network errors with a linear backoff. On a real outage, this triples the load. Non-idempotent requests (POST for user creation) could cause duplicate side effects if the server processed the request but the response was lost.

**Fix**: Only retry on idempotent methods (GET, PUT, DELETE). Skip retry for POST requests, or add an idempotency key header.

---

### 11. Seed user passwords in realm-export.json

**File**: `realm-export.json`

All seed users have trivial passwords (`admin`, `teacher1`, `student1`, etc.) committed to git. This is expected for dev, but should be documented as dev-only.

**Fix**: Add a comment or README note that these are dev-only credentials. For any staging/production environment, use a different import or disable the import entirely.

---

### 12. CSRF disabled

**File**: `SecurityConfig.java`

CSRF protection is disabled (`.csrf(csrf -> csrf.disable())`). This is correct for a stateless JWT-based API with no cookie-based auth. No action needed — just documenting the decision.

---

## Summary

| # | Finding | Severity | Status |
|---|---------|----------|--------|
| 1 | Deactivated users not blocked | HIGH | FIXED |
| 2 | Email-based account takeover | HIGH | FIXED |
| 3 | Silent role assignment failure | HIGH | FIXED |
| 4 | Client secret in git | HIGH | FIXED |
| 5 | Error message leaks internals | MEDIUM | FIXED |
| 6 | Admin token not cached | MEDIUM | FIXED |
| 7 | CORS allows all headers | MEDIUM | FIXED |
| 8 | No rate limiting on creation | MEDIUM | FIXED |
| 9 | Frontend role checks client-side | LOW | N/A (by design) |
| 10 | POST retry on 5xx | LOW | FIXED |
| 11 | Seed passwords in git | LOW | FIXED |
| 12 | CSRF disabled (by design) | LOW | N/A (by design) |
