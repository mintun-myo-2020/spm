# Sprint 5 — Code Generation Plan

## P0: Temporary Password on User Creation

- [x] `KeycloakAdminService.createUser()` — add `boolean temporaryPassword` parameter, default `true`
- [x] `KeycloakAdminService.createKeycloakUser()` — pass `temporary` flag to Keycloak credentials
- [x] All callers (`UserService.createStudent/createParent/createTeacher`) use default `temporary=true`

## P0: Frontend Password Field UX

- [x] `ClassLayout.tsx` — label "Temporary Password" + helper text
- [x] `AdminClassDetails.tsx` — label "Temporary Password" + helper text
- [x] `CreateUserForm.tsx` — label "Temporary Password" + helper text

## P1: Change Password (Self-Service)

- [x] `SettingsPage.tsx` — in-app change password form (current + new + confirm)
- [x] `AuthController.java` — `PUT /api/v1/auth/change-password` endpoint
- [x] `KeycloakAdminService.verifyPassword()` — uses `spm-frontend` public client for direct access grant password check
- [x] `KeycloakAdminService.resetPasswordNonTemporary()` — sets new password as permanent
- [ ] `application.yml` — add `app.keycloak.public-client-id` property for password verification client
- [x] Sidebar — "Settings" gear icon at bottom, links to `/{role}/settings`
- [x] Routes — `/settings` route added to all 4 role route files
- [x] Navbar — logout button removed (moved to Settings page)

### Technical Note: Password Verification Client
The `spm-backend` Keycloak client has `directAccessGrantsEnabled: false` (it's a confidential service-account client). Password verification requires a direct access grant (resource owner password credentials flow), which needs `directAccessGrantsEnabled: true`. The `spm-frontend` client has this enabled. The `verifyPassword()` method uses the frontend client ID (`spm-frontend`) for the token exchange to verify the current password.

## P1: Forgot Password

- [ ] Keycloak realm config: enable "Forgot Password" toggle (manual config step)
- [ ] Keycloak SMTP config for reset emails (manual config step)
- [ ] Document in deployment guide

## P2: Admin/Teacher Reset Password

- [x] `KeycloakAdminService.resetPassword()` — calls Keycloak Admin API to set temporary password
- [x] `UserService.resetPassword()` — validates user exists and has keycloakId
- [x] `UserController` — `PUT /api/v1/users/{userId}/reset-password` endpoint (ADMIN + TEACHER)
- [ ] Frontend: "Reset Password" button on user management pages

## P2: Parent Creation from Teacher View

- [ ] Student detail page: "Add Parent" button when no parent linked
- [ ] Add Parent form: name, email, phone, temporary password
- [ ] Backend: existing `POST /api/v1/users/parents` already works for TEACHER role

## Cleanup

- [x] Deleted dead `ClassDetails.tsx` (orphaned, never routed)
