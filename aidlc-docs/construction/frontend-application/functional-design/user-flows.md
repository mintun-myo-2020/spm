# User Flows & Frontend Business Rules - Frontend Application

## Overview
This document defines the user interaction flows, routing, state management, and client-side validation rules for the Student Progress Tracking System Frontend.

---

## 1. Routing Structure

### 1.1 Route Definitions

**Public Routes**:
- `/login` - Login page (redirect to Keycloak)
- `/callback` - OAuth2 callback handler

**Teacher Routes** (requires TEACHER role):
- `/teacher/dashboard` - Teacher dashboard
- `/teacher/classes` - Class list
- `/teacher/classes/:classId` - Class details with students
- `/teacher/classes/:classId/students/:studentId` - Student details
- `/teacher/classes/:classId/students/:studentId/scores/new` - New test score
- `/teacher/classes/:classId/students/:studentId/progress` - Student progress

**Parent Routes** (requires PARENT role):
- `/parent/dashboard` - Parent dashboard
- `/parent/scores` - Child's test score history
- `/parent/progress` - Child's progress charts
- `/parent/preferences` - Notification preferences

**Student Routes** (requires STUDENT role):
- `/student/dashboard` - Student dashboard
- `/student/scores` - My test scores
- `/student/progress` - My progress charts

**Admin Routes** (requires ADMIN role):
- `/admin/dashboard` - Admin dashboard
- `/admin/users` - User management
- `/admin/users/new` - Create user
- `/admin/classes` - Class management
- `/admin/classes/new` - Create class
- `/admin/classes/:classId` - Class details
- `/admin/subjects` - Subject management

**Shared Routes**:
- `/` - Redirect to role-appropriate dashboard
- `/404` - Not found page
- `/403` - Access denied page

### 1.2 Route Guards

**Authentication Guard**: Redirect to `/login` if not authenticated
**Role Guard**: Redirect to `/403` if user lacks required role
**Dashboard Redirect**: `/` redirects based on primary role:
- ADMIN → `/admin/dashboard`
- TEACHER → `/teacher/dashboard`
- PARENT → `/parent/dashboard`
- STUDENT → `/student/dashboard`

---

## 2. Core User Flows

### 2.1 Authentication Flow

```
User visits app
  → Check if authenticated (JWT in storage)
    → Yes: Redirect to role dashboard
    → No: Show login page
      → User clicks "Login"
        → Redirect to Keycloak
          → User enters credentials / social login
            → Keycloak redirects to /callback
              → Exchange code for JWT
                → Store JWT in memory
                  → Fetch user info (GET /api/v1/auth/me)
                    → Redirect to role dashboard
```

**Token Refresh Flow**:
```
JWT expires (1 hour)
  → Interceptor detects 401 response
    → Attempt refresh token exchange
      → Success: Retry original request with new token
      → Failure: Redirect to login
```

---

### 2.2 Teacher: Record Test Score Flow

```
Teacher Dashboard
  → Click class → Class Details
    → Click student → Student Details
      → Click "Record Test Score"
        → Test Score Form displayed
          → Enter test name, date
          → Add questions (Q1, Q2, ...)
            → For each question, add sub-questions (a, b, c, ...)
              → For each sub-question, select topic and enter score
          → Form validates:
            - All fields filled
            - Scores between 0-100
            - Sub-question scores sum to overall
            - Date not in future
          → Click "Save"
            → POST /api/v1/test-scores
              → Success: Show confirmation, redirect to student details
              → Error: Show error message, keep form data
```

**Optional: Add Feedback**:
```
After saving test score
  → "Add Feedback" prompt
    → Feedback Form displayed
      → Fill structured fields (strengths, improvements, recommendations)
      → Optionally use template snippets
      → Click "Save Feedback"
        → POST /api/v1/test-scores/{id}/feedback
          → Success: Show confirmation
          → Error: Show error message
```

---

### 2.3 Parent: View Child Progress Flow

```
Parent Dashboard
  → See child's recent scores and summary
    → Click "View All Scores"
      → Test Score History page
        → Filter by date range, subject
        → Click on test → Test Detail modal
          → See topic breakdown and feedback
    → Click "View Progress"
      → Progress Charts page
        → Overall score trend chart
        → Topic-specific charts
        → Improvement velocity display
```

---

### 2.4 Admin: Create User Flow

```
Admin Dashboard
  → Click "Users" in sidebar
    → User Management page
      → Click "Add User"
        → Select role (Teacher/Parent/Student)
        → Fill user form:
          - Email, first name, last name
          - Role-specific fields
          - For Parent: select student to link
        → Click "Create"
          → POST /api/v1/users/{role}
            → Success: Show confirmation, user appears in list
            → Error: Show validation errors
```

---

### 2.5 Admin: Create Class & Enroll Students Flow

```
Admin Dashboard
  → Click "Classes" in sidebar
    → Class Management page
      → Click "Create Class"
        → Fill class form (name, subject, teacher, max students)
        → Click "Create"
          → POST /api/v1/classes
            → Success: Class created
      → Click on class → Class Details
        → Click "Enroll Student"
          → Student search/select modal
            → Select student → POST /api/v1/classes/{id}/students
              → Success: Student appears in class list
```

---

## 3. State Management

### 3.1 Global State (React Context)

**AuthContext**:
- `user`: Current user info (id, email, name, roles)
- `token`: JWT access token
- `isAuthenticated`: Boolean
- `login()`, `logout()`, `hasRole()`

**ThemeContext**:
- `theme`: 'light' | 'dark'
- `toggleTheme()`

### 3.2 Server State (Custom Hooks)

All API data is managed via custom hooks with loading/error states:

```typescript
// Pattern for all data fetching
function useStudentScores(studentId: string) {
  const [data, setData] = useState<TestScore[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  const fetch = useCallback(async () => {
    setLoading(true);
    try {
      const response = await apiClient.get(`/students/${studentId}/test-scores`);
      setData(response.data.content);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [studentId]);

  useEffect(() => { fetch(); }, [fetch]);
  return { data, loading, error, refetch: fetch };
}
```

### 3.3 Form State (useForm Hook)

All forms use a custom `useForm` hook:
- Tracks field values
- Validates on change and submit
- Tracks dirty/touched state
- Handles submission with loading state

---

## 4. Client-Side Validation Rules

### 4.1 Test Score Form Validation

| Field | Rule | Error Message |
|-------|------|---------------|
| testName | Required, max 255 chars | "Test name is required" |
| testDate | Required, not future | "Test date cannot be in the future" |
| overallScore | Required, 0-100, decimal allowed | "Score must be between 0 and 100" |
| questions | At least 1 question | "At least one question is required" |
| subQuestions | At least 1 per question | "Each question needs at least one sub-question" |
| subQuestion.topicId | Required | "Topic is required" |
| subQuestion.score | 0 to maxScore | "Score exceeds maximum" |
| Sum validation | Sub-question scores = overall | "Scores do not sum to overall score" |

### 4.2 User Creation Form Validation

| Field | Rule | Error Message |
|-------|------|---------------|
| email | Required, valid email format | "Valid email is required" |
| firstName | Required, max 255 chars | "First name is required" |
| lastName | Required, max 255 chars | "Last name is required" |
| phoneNumber | Optional, valid phone format | "Invalid phone number" |
| studentId (parent) | Required for parent role | "Student must be selected" |

### 4.3 Class Creation Form Validation

| Field | Rule | Error Message |
|-------|------|---------------|
| name | Required, max 255 chars | "Class name is required" |
| subjectId | Required | "Subject is required" |
| teacherId | Required | "Teacher is required" |
| maxStudents | 1-100, integer | "Max students must be between 1 and 100" |

---

## 5. Error Handling Strategy

### 5.1 API Error Handling

**Global Error Handler** (API interceptor):
- 401 Unauthorized → Attempt token refresh, then redirect to login
- 403 Forbidden → Show access denied message
- 404 Not Found → Show "not found" message
- 409 Conflict → Show specific conflict message (e.g., "Email already exists")
- 422 Validation → Show field-level errors
- 500 Server Error → Show generic error with retry option
- Network Error → Show "connection lost" with retry

### 5.2 Error Display Patterns

**Inline Errors**: Form field validation errors shown below fields
**Toast Notifications**: Success/error messages for operations (save, delete)
**Error Pages**: Full-page errors for 404, 403
**Error Boundaries**: React error boundaries for component crashes

---

## 6. Responsive Design Rules

### 6.1 Breakpoints

- **Mobile**: < 640px (single column, stacked layout)
- **Tablet**: 640px - 1024px (two column, collapsible sidebar)
- **Desktop**: > 1024px (full layout with sidebar)

### 6.2 Mobile Adaptations

- Navigation: Hamburger menu instead of sidebar
- Tables: Horizontal scroll or card layout
- Charts: Full-width, simplified labels
- Forms: Full-width inputs, stacked layout
- Buttons: Minimum 44px touch target

---

## 7. Accessibility Rules

- All interactive elements keyboard navigable
- ARIA labels on icons and non-text elements
- Color contrast ratio minimum 4.5:1
- Focus indicators visible on all interactive elements
- Form labels associated with inputs
- Error messages announced to screen readers
- Charts have text alternatives (data tables)
- Skip navigation link for keyboard users

---

**Document Version**: 1.0  
**Created**: 2026-03-08  
**Status**: Draft
