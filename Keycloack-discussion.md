# Keycloak Integration — How It Works in SPM

## Overview

SPM uses Keycloak as the identity provider. The frontend (React/Vite) authenticates users via the Keycloak JS adapter, and the backend (Spring Boot) validates JWTs as an OAuth2 resource server. Business user records are auto-provisioned in the app database on first login.

## Architecture

```
Browser → React (keycloak-js) → Keycloak (login/token) → React (JWT in memory)
                                                              ↓
                                                    API call with Bearer token
                                                              ↓
                                              Spring Boot (validates JWT via JWK)
                                                              ↓
                                              CurrentUserService (lookup/provision user)
                                                              ↓
                                                        App PostgreSQL
```

## Keycloak Setup

Keycloak runs in Docker with `start-dev --import-realm`. The realm config lives in `spm/keycloak/realm-export.json` and is imported on first start.

Key config points:
- Realm: `spm`
- Frontend client: `spm-frontend` (public client, PKCE enabled)
- Backend client: `spm-backend` (bearer-only, used for audience validation)
- Keycloak runs on port 8180 (mapped from container port 8080)
- Health endpoint is on port 9000 (Keycloak 26.x management interface)
- `defaultClientScopes` must include `openid` — without it, the JWT won't contain `sub` or `aud` claims and Spring will reject the token

### Seeded test users

| Username   | Password   | Role    |
|------------|------------|---------|
| admin      | admin      | ADMIN   |
| teacher1   | teacher1   | TEACHER |
| parent1    | parent1    | PARENT  |
| student1   | student1   | STUDENT |

### Realm import behavior

Keycloak's `--import-realm` uses `IGNORE_EXISTING` strategy. If the realm already exists in the Keycloak database, the import is skipped. To re-import after changing `realm-export.json`, you must wipe the Keycloak volume:

```bash
docker compose down -v
docker compose up
```

## Mapping Keycloak Users to App Users

### The problem

Keycloak assigns a random UUID (`sub` claim) to each user at creation time. You can't predict these UUIDs in advance, so you can't pre-seed matching rows in the app database.

### Our solution: auto-provision on first login

`CurrentUserService.getCurrentUser()` looks up the user by `keycloakId` (the JWT `sub` claim). If no match is found, it creates the user and their profile record from the JWT claims:

```java
return userRepository.findByKeycloakId(keycloakId)
    .orElseGet(() -> provisionFromToken(jwt));
```

The provisioning method:
1. Creates a `User` row with `keycloakId`, email, name from JWT claims
2. Maps `realm_access.roles` from the JWT to the app's `Role` enum
3. Creates the matching profile record (Teacher, Student, Parent, or nothing for Admin)

This means the first API call after login is slightly slower (DB writes), but every subsequent call is a simple lookup.

### User entity mapping

| JWT Claim         | User Field   | Notes                                    |
|-------------------|--------------|------------------------------------------|
| `sub`             | `keycloakId` | Stable UUID, unique per Keycloak realm   |
| `email`           | `email`      | From Keycloak user profile               |
| `given_name`      | `firstName`  | From Keycloak user profile               |
| `family_name`     | `lastName`   | From Keycloak user profile               |
| `realm_access.roles` | `roles`  | Mapped to app `Role` enum, unknown roles skipped |

### Why `sub` and not `email` or `username`

- `sub` is immutable — it never changes even if the user's email or username is updated in Keycloak
- `email` can change and isn't guaranteed unique across identity providers
- `preferred_username` can also change

We store `keycloakId` (the `sub` value) as the stable foreign key to Keycloak.

## Frontend Auth Flow

1. App loads → `AuthProvider` calls `keycloakService.init()` with `onLoad: 'check-sso'`
2. If not authenticated → show login page with "Sign in with Keycloak" button
3. Button calls `keycloak.login()` → full redirect to Keycloak login form
4. After login → Keycloak redirects back → `keycloak-js` processes the auth code via PKCE
5. `AuthContext` calls `GET /api/v1/auth/me` with the Bearer token
6. Backend returns user info including `profileType` → `DashboardRedirect` routes to the correct dashboard

### Token management

- PKCE (`S256`) is used for the auth code exchange (no client secret needed for public clients)
- Silent SSO check via hidden iframe (`silent-check-sso.html`)
- Token refresh every 60 seconds via `keycloak.updateToken(30)`
- API client interceptor attaches the token to every request
- On 401 response, the interceptor attempts a token refresh before retrying

## Backend JWT Validation

Spring Boot validates JWTs using the standard `oauth2-resource-server` starter:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8180/realms/spm
          jwk-set-uri: http://localhost:8180/realms/spm/protocol/openid-connect/certs
```

Spring fetches the JWK set from Keycloak to verify token signatures and validates the `iss` claim matches the configured issuer URI.

### Role extraction

`KeycloakRoleConverter` reads roles from `realm_access.roles` in the JWT and maps them to Spring Security `GrantedAuthority` objects with a `ROLE_` prefix. This is abstracted behind a `RoleConverter` interface so the auth provider can be swapped without touching `SecurityConfig`.

## Docker Compose Dependencies

```
keycloak-postgres (healthy) → keycloak (healthy) → app
postgres (healthy) ─────────────────────────────────→ app
```

The `app` service waits for both Keycloak and Postgres to be healthy before starting. This ensures:
- Keycloak is ready to serve JWK sets when Spring Boot starts
- Postgres is ready for Flyway migrations

### Keycloak health check

Keycloak 26.x serves health endpoints on port 9000 (management interface), not on the main HTTP port. The health check hits `GET /health/ready` on port 9000 and looks for `"status": "UP"` in the response.

## Common Pitfalls

1. **Missing `basic` scope** — If `defaultClientScopes` doesn't include `basic`, the JWT won't have a `sub` claim. In Keycloak 26.x, the `basic` client scope contains the `oidc-sub-mapper` that adds `sub` to the access token. Without it, Spring rejects the token with "The iss claim is not valid". Note: there is no `openid` client scope in Keycloak 26.x — the `basic` scope is what you need.

2. **Realm already exists** — Changing `realm-export.json` has no effect if the realm is already in Keycloak's database. Use `docker compose down -v` to wipe and re-import.

3. **Health check on wrong port** — Keycloak 26.x health endpoints are on port 9000, not 8080. Docker health checks must target the management port.

4. **Backend starts before Keycloak** — If Spring Boot can't reach Keycloak's OIDC discovery endpoint at startup, JWT validation will fail. The docker-compose dependency chain prevents this, but when running locally you must start Keycloak first.

5. **White screen after login** — If `/auth/me` fails (backend down, token rejected, etc.), `user` stays null and `DashboardRedirect` used to redirect to `/login`, creating an infinite loop. Now it shows an error message with a retry button instead.
