# Sprint 5 — User Onboarding & Account Management

## Intent Analysis
- **Request Type**: Enhancement + Fix (user creation flow, password management, dead code cleanup)
- **Scope**: Multiple Components (backend Keycloak integration, frontend enroll modals, user flows)
- **Complexity**: Moderate
- **Depth**: Standard

## Problem Statement

The current user creation flow has several issues:
1. Teachers set permanent passwords for students/parents — students have no way to change them
2. No "forgot password" or "change password" flow exists
3. The teacher enroll modal only lets you pick existing students — no way to create new ones from the teacher UI (fixed in this sprint's predecessor, but needs the password flow sorted)
4. Dead code: `ClassDetails.tsx` was an orphaned component never routed (already deleted)
5. The admin `CreateUserForm` also sets permanent passwords with no forced change

## Current State

- `KeycloakAdminService.createKeycloakUser()` creates users with `"temporary": false` — password is permanent
- Keycloak supports `"temporary": true` which forces password change on first login
- Keycloak has built-in "forgot password" email flow but it's not configured/exposed
- No "change password" option exists in the app for any role
- Teacher enroll modal (in `ClassLayout.tsx`) now has a "+ New Student" form that asks for email + password

---

## Feature 1: Temporary Password on User Creation

### FR-5.1: Keycloak Temporary Password
When a teacher/admin creates a student or parent, the password should be marked as temporary in Keycloak.

- **FR-5.1.1**: `KeycloakAdminService.createKeycloakUser()` shall accept a `boolean temporary` parameter
- **FR-5.1.2**: When creating students and parents, `temporary` shall be `true` — Keycloak will force password change on first login
- **FR-5.1.3**: When creating teachers (admin-only), `temporary` shall also be `true`
- **FR-5.1.4**: The Keycloak login page handles the "update password" flow natively — no custom UI needed
- **FR-5.1.5**: The `UserService.createStudent()`, `createParent()`, `createTeacher()` methods shall pass `temporary=true` to `KeycloakAdminService`

### FR-5.2: Frontend Password Field UX
Update the create-student forms to clarify the password is temporary.

- **FR-5.2.1**: Password field label shall read "Temporary Password" instead of "Password"
- **FR-5.2.2**: Helper text below the field: "Student will be asked to change this on first login"
- **FR-5.2.3**: Applies to: teacher enroll modal (`ClassLayout.tsx`), admin enroll modal (`AdminClassDetails.tsx`), admin create user form (`CreateUserForm.tsx`)

---

## Feature 2: Change Password (Self-Service)

### FR-5.3: Change Password via In-App Form
All users should be able to change their own password from the Settings page.

- **FR-5.3.1**: Settings page (`/{role}/settings`) has a "Change Password" section with current password, new password, confirm password fields
- **FR-5.3.2**: Backend endpoint: `PUT /api/v1/auth/change-password` (authenticated, any role)
- **FR-5.3.3**: Backend verifies current password by attempting a Keycloak token exchange using the `spm-frontend` public client (which has `directAccessGrantsEnabled: true`)
- **FR-5.3.4**: If current password is correct, sets new password via Keycloak Admin API with `temporary: false` (permanent, since user chose it themselves)
- **FR-5.3.5**: The `spm-backend` confidential client has `directAccessGrantsEnabled: false` and cannot be used for password verification — must use the frontend public client ID instead
- **FR-5.3.6**: Frontend client ID is configured via `app.keycloak.public-client-id` property (defaults to `spm-frontend`)

### FR-5.4: Settings Page
- **FR-5.4.1**: New shared component `SettingsPage.tsx` accessible at `/{role}/settings` for all roles
- **FR-5.4.2**: Contains: Account info section, Change Password form, Logout button
- **FR-5.4.3**: Sidebar has "Settings" gear icon at the bottom linking to the settings page
- **FR-5.4.4**: Logout button moved from Navbar to Settings page

---

## Feature 3: Forgot Password

### FR-5.5: Keycloak Forgot Password Configuration
Enable the "forgot password" flow in Keycloak so users can reset their password via email.

- **FR-5.5.1**: Enable "Forgot Password" in Keycloak realm settings (Login tab → "Forgot password" toggle)
- **FR-5.5.2**: Configure SMTP settings in Keycloak for sending reset emails
- **FR-5.5.3**: The Keycloak login page will show a "Forgot password?" link automatically once enabled
- **FR-5.5.4**: No backend or frontend code changes needed — this is Keycloak configuration only
- **FR-5.5.5**: Document the Keycloak SMTP configuration steps in the deployment guide

---

## Feature 4: Teacher Create-and-Enroll Flow Cleanup

### FR-5.6: Streamlined Student Creation from Teacher View
Ensure the teacher can create and enroll students in one step with a clean UX.

- **FR-5.6.1**: Teacher enroll modal (`ClassLayout.tsx`) has two modes: "Select existing" and "Create new"
- **FR-5.6.2**: Create mode fields: First Name, Last Name, Email, Temporary Password, Grade (optional)
- **FR-5.6.3**: On submit: creates student in Keycloak + DB, then auto-enrolls in the current class
- **FR-5.6.4**: Success toast: "Student created and enrolled"
- **FR-5.6.5**: Error handling: if Keycloak creation succeeds but enrollment fails, student still exists (can be enrolled manually later)

### FR-5.7: Parent Creation from Teacher View
Teachers should also be able to create a parent account and link it to a student.

- **FR-5.7.1**: On the student detail page (teacher view), add a "Link Parent" action
- **FR-5.7.2**: If student has no parent linked: show "Add Parent" button
- **FR-5.7.3**: Add Parent form: First Name, Last Name, Email, Phone (optional), Temporary Password
- **FR-5.7.4**: On submit: creates parent in Keycloak + DB, links to the student
- **FR-5.7.5**: If student already has a parent linked: show parent info (name, email) with no action needed

---

## Feature 5: Admin Reset Password

### FR-5.8: Admin/Teacher Password Reset for Users
Allow admins and teachers to reset a user's password if they're locked out.

- **FR-5.8.1**: New backend endpoint: `PUT /api/v1/users/{userId}/reset-password`
- **FR-5.8.2**: Auth: ADMIN (any user), TEACHER (only students/parents in their classes)
- **FR-5.8.3**: Request body: `{ "newPassword": "string" }` — sets a new temporary password
- **FR-5.8.4**: Calls Keycloak Admin API to set the password with `temporary: true`
- **FR-5.8.5**: Frontend: "Reset Password" button on user detail/management pages
- **FR-5.8.6**: Confirmation dialog: "This will set a temporary password. The user will be asked to change it on next login."

---

## Non-Functional Requirements

### NFR-5.1: No Custom Password Storage
- Passwords are never stored in the application database
- All password management goes through Keycloak
- The app only passes passwords to Keycloak during user creation

### NFR-5.2: Keycloak Password Policy
- Keycloak realm should have a password policy configured (min length, complexity)
- This is Keycloak configuration, not application code
- Document recommended policy in deployment guide

---

## Out of Scope (decided via requirement verification)

- Self-service signup (you create all teacher/admin accounts manually via admin UI)
- Multi-tenancy / centre_id (using separate instances per customer instead)
- Email verification on user creation (future — requires SMTP)
- Social login for students/parents (Google, etc.) — already supported by Keycloak but not configured
- Two-factor authentication

---

## Implementation Priority

| Feature | Priority | Effort |
|---------|----------|--------|
| FR-5.1: Temporary password | P0 — must have | 1 hour |
| FR-5.2: Password field UX | P0 — must have | 30 min |
| FR-5.3-5.4: Change password | P1 — should have | 1-2 hours |
| FR-5.5: Forgot password | P1 — should have | Config only |
| FR-5.6: Create-and-enroll cleanup | P0 — already done | Verify only |
| FR-5.7: Parent creation from teacher | P2 — nice to have | 2-3 hours |
| FR-5.8: Admin reset password | P2 — nice to have | 2-3 hours |

---

## User Flows

### Flow 0: Onboarding a New Teacher / Private Tutor
1. Platform operator creates a user in Keycloak admin console (email, name, temporary password)
2. Assigns the `TEACHER` realm role in Keycloak
3. Tells the tutor: "Your login is [email], password is [temp]"
4. Tutor logs in → Keycloak forces password change → sets own password
5. `autoProvisionUser()` creates the User + Teacher profile in the app DB automatically
6. Tutor lands on teacher dashboard, ready to create classes and students
7. No admin role or admin UI needed — this is a Keycloak-level operation only

### Flow 1: Teacher Creates a New Student
1. Teacher opens class → clicks "Enroll Student" → clicks "+ New Student"
2. Fills in: name, email, temporary password, grade
3. Clicks "Create & Enroll"
4. System creates Keycloak user (temporary password) + DB student + enrolls in class
5. Teacher tells student: "Your login is [email], password is [temp]. You'll be asked to change it."
6. Student logs in → Keycloak forces password change → student sets own password → lands on student dashboard

### Flow 2: Teacher Links a Parent to a Student
1. Teacher opens class → clicks student → sees student detail page
2. If no parent linked: clicks "Add Parent"
3. Fills in: name, email, phone, temporary password
4. System creates Keycloak user + DB parent + links to student
5. Teacher tells parent: "Your login is [email], password is [temp]."
6. Parent logs in → forced password change → sees child's progress dashboard

### Flow 3: User Changes Their Password
1. User clicks their name in navbar → "Change Password"
2. Redirected to Keycloak account page
3. Enters current password + new password
4. Redirected back to app

### Flow 4: User Forgot Password
1. User on login page → clicks "Forgot password?"
2. Keycloak sends reset email
3. User clicks link → sets new password → logs in

---

**Document Version**: 1.0
**Last Updated**: 2026-03-28
**Status**: Draft
