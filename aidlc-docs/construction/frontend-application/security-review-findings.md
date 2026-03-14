# Frontend Security Review Findings

**Date**: 2026-03-14
**Scope**: spm-frontend — all TypeScript/TSX source files
**Status**: Pre-UI-migration logic review

---

## Summary

The frontend codebase is in solid shape for a Keycloak-integrated SPA. No critical vulnerabilities found. There are several medium and low severity findings that should be addressed before production deployment.

---

## Finding 1: 401 Retry Has No Guard Against Infinite Loop (MEDIUM)

**File**: `src/services/apiClient.ts` (lines 29-37)

The 401 interceptor refreshes the token and retries the request, but there's no flag to prevent a second 401 from triggering another refresh+retry cycle indefinitely.

**Scenario**: If `refreshToken()` returns `true` but the new token is still rejected (e.g., Keycloak returns a token the backend doesn't accept), the interceptor will loop: 401 → refresh → retry → 401 → refresh → retry → ...

**Fix**: Add a `_authRetried` flag to the config, similar to how `_retryCount` works for 5xx retries.

```typescript
// 401: try token refresh (once only)
if (error.response?.status === 401 && !config._authRetried) {
  config._authRetried = true;
  const refreshed = await keycloakService.refreshToken();
  if (refreshed) {
    config.headers.Authorization = `Bearer ${keycloakService.getToken()}`;
    return apiClient.request(config);
  }
  keycloakService.logout();
  return Promise.reject(error);
}
```

---

## Finding 2: Token Exposed via AuthContext (MEDIUM)

**File**: `src/context/AuthContext.tsx` (line 12, line 63)

The raw JWT token is exposed as a property on the auth context (`token: string | null`). Any component in the tree can read it. This increases the attack surface — if any component has an XSS vulnerability or a third-party dependency is compromised, the token is trivially accessible.

**Current code**:
```typescript
const token = keycloakService.getToken();
// ...
<AuthContext.Provider value={{ ..., token }}>
```

**Recommendation**: Remove `token` from the context entirely. The `apiClient` already attaches the token via its request interceptor, so no component should need direct access. If a component genuinely needs the token (rare), expose a method like `getToken()` that reads it on-demand rather than holding it in React state.

---

## Finding 3: `console.error` Leaks Error Objects in Production (LOW)

**Files**:
- `src/services/keycloakService.ts:27` — `console.error('Keycloak init failed:', error)`
- `src/context/AuthContext.tsx:42` — `console.error('Failed to fetch user info:', err)`
- `src/components/shared/ErrorBoundary.tsx:22` — `console.error('ErrorBoundary caught:', error, info)`

In production, these log full error objects (including stack traces, potentially internal URLs, and token-related info from Keycloak errors) to the browser console. An attacker with console access or a browser extension could harvest this.

**Fix**: Either strip console statements in production builds (via a Vite plugin or ESLint rule), or replace with a structured logging utility that sanitizes output.

---

## Finding 4: ErrorBoundary Renders `error.message` to Users (LOW)

**File**: `src/components/shared/ErrorBoundary.tsx` (line 28)

```tsx
<p className="mt-2 text-sm text-gray-500">{this.state.error?.message}</p>
```

Error messages from unhandled exceptions can contain internal details (e.g., "Cannot read properties of undefined (reading 'data')" or network error URLs). Displaying these to end users is an information disclosure risk.

**Fix**: Show a generic message instead. Log the real error for debugging but don't render it.

```tsx
<p className="mt-2 text-sm text-gray-500">
  An unexpected error occurred. Please try again or contact support.
</p>
```

---

## Finding 5: `scheduleTokenRefresh` Interval Is Never Cleared (LOW)

**File**: `src/services/keycloakService.ts` (lines 68-72)

```typescript
scheduleTokenRefresh(): void {
  setInterval(async () => {
    if (keycloak.authenticated) {
      await this.refreshToken();
    }
  }, 60000);
}
```

The `setInterval` is never cleared — not on logout, not on tab close, not on re-init. If `init()` is called multiple times (e.g., React StrictMode in dev, or a hot reload), multiple intervals stack up. After logout, the interval keeps firing (the `keycloak.authenticated` check prevents the refresh call, but the timer still runs).

More importantly, if `refreshToken()` fails and triggers `logout()`, the interval continues running in the background.

**Fix**: Store the interval ID and clear it on logout.

```typescript
private refreshIntervalId: ReturnType<typeof setInterval> | null = null;

scheduleTokenRefresh(): void {
  this.clearTokenRefresh();
  this.refreshIntervalId = setInterval(async () => {
    if (keycloak.authenticated) {
      await this.refreshToken();
    }
  }, 60000);
},

clearTokenRefresh(): void {
  if (this.refreshIntervalId) {
    clearInterval(this.refreshIntervalId);
    this.refreshIntervalId = null;
  }
},

logout(): void {
  this.clearTokenRefresh();
  keycloak.logout({ redirectUri: window.location.origin });
},
```

---

## Finding 6: FeedbackForm Has No Input Validation (LOW)

**File**: `src/components/teacher/FeedbackForm.tsx`

Unlike `CreateUserForm` and `TestScoreForm` which use Zod schemas for validation, `FeedbackForm` uses `react-hook-form` with no validation schema at all. The textarea fields (`strengths`, `areasForImprovement`, `recommendations`, `additionalNotes`) have no max-length constraints.

While the backend should enforce limits, defense-in-depth means the frontend should also validate. A user could paste megabytes of text into these fields.

**Fix**: Add a Zod schema with `z.string().max(5000)` (or whatever the backend limit is) for each field.

---

## Finding 7: Missing `silent-check-sso.html` Origin Validation (LOW)

**File**: `public/silent-check-sso.html`

```html
<script>
  parent.postMessage(location.href, location.origin);
</script>
```

This is the standard Keycloak pattern and the `location.origin` target is correct (it restricts who can receive the message). However, the keycloak-js library on the receiving end should validate `event.origin` when processing the message — this is handled internally by keycloak-js, so this is informational only.

---

## Finding 8: `.env` File Committed to Git (INFO)

**File**: `spm-frontend/.env`

The `.env` file exists and contains Keycloak URLs. While `VITE_*` variables are public by design (they're embedded in the built JS bundle), having a `.env` file in the repo can set a bad precedent. The root `.gitignore` excludes `.env`, but there's no `spm-frontend/.gitignore` — verify the root gitignore pattern covers subdirectories.

**Current root `.gitignore`**:
```
.env
.env.local
.env.*.local
```

These patterns should match `spm-frontend/.env` since git applies `.gitignore` patterns recursively. Verify with `git status` that the file isn't tracked.

---

## Not Found (Good Practices Already in Place)

- No `dangerouslySetInnerHTML` usage anywhere
- No `eval()` or `new Function()` usage
- No `localStorage`/`sessionStorage` for tokens (Keycloak manages in-memory)
- No `innerHTML` or `outerHTML` DOM manipulation
- PKCE (`S256`) enabled for Keycloak auth flow
- Strict TypeScript configuration (`strict: true`)
- Zod validation on most forms
- API client has request timeout (15s)
- 5xx retry has a cap (`MAX_RETRIES = 2`)
- Role-based route protection via `ProtectedRoute`
- No open redirect vectors (no user-controlled URL parameters in navigation)
- React's built-in JSX escaping prevents XSS in rendered content

---

## Priority Order for Fixes

1. **Finding 1** — 401 infinite loop guard (quick fix, prevents potential DoS)
2. **Finding 5** — Clear token refresh interval (prevents resource leak + post-logout behavior)
3. **Finding 2** — Remove token from context (reduces attack surface)
4. **Finding 4** — Generic error message in ErrorBoundary (information disclosure)
5. **Finding 6** — Add validation to FeedbackForm (defense-in-depth)
6. **Finding 3** — Strip/sanitize console.error in production (information disclosure)
7. **Finding 8** — Verify .env isn't tracked in git
