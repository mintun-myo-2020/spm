# Component Architecture - Frontend Application

## Overview
This document defines the component architecture for the Student Progress Tracking System Frontend Application, built with React 18+ and TypeScript.

---

## 1. Application Structure

### 1.1 High-Level Architecture

```
src/
├── components/          # React components
│   ├── auth/           # Authentication components
│   ├── teacher/        # Teacher role components
│   ├── parent/         # Parent role components
│   ├── student/        # Student role components
│   ├── admin/          # Admin role components
│   └── shared/         # Shared/common components
├── services/           # API and external services
├── hooks/              # Custom React hooks
├── context/            # React Context providers
├── types/              # TypeScript type definitions
├── utils/              # Utility functions
├── styles/             # Global styles
├── App.tsx             # Root component
└── main.tsx            # Application entry point
```

---

## 2. Authentication Components

### 2.1 Login Component

**Purpose**: Handle OAuth2 login flow with Keycloak

**Component**: `components/auth/Login.tsx`

**Functionality**:
- Display login button
- Redirect to Keycloak login page
- Handle OAuth2 callback
- Store JWT token
- Redirect to role-appropriate dashboard

**Props**: None (route component)

**State**:
- `isLoading`: boolean (loading state during redirect)
- `error`: string | null (error message if login fails)

---

### 2.2 Protected Route Component

**Purpose**: Protect routes requiring authentication

**Component**: `components/auth/ProtectedRoute.tsx`

**Functionality**:
- Check if user is authenticated
- Redirect to login if not authenticated
- Check user role for role-specific routes
- Render children if authorized

**Props**:
```typescript
interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredRoles?: string[]; // Optional role restriction
}
```

---

### 2.3 Keycloak Provider

**Purpose**: Manage Keycloak authentication state

**Component**: `components/auth/KeycloakProvider.tsx`

**Functionality**:
- Initialize Keycloak client
- Handle token refresh
- Provide authentication context
- Handle logout

**Context Value**:
```typescript
interface AuthContextValue {
  isAuthenticated: boolean;
  user: UserInfo | null;
  token: string | null;
  login: () => void;
  logout: () => void;
  hasRole: (role: string) => boolean;
}
```

---

## 3. Teacher Components

### 3.1 Teacher Dashboard

**Component**: `components/teacher/TeacherDashboard.tsx`

**Functionality**:
- Display overview of teacher's classes
- Show recent activity (new scores, feedback)
- Quick actions (add score, view class)
- Statistics (total students, classes, recent tests)

**Data Fetched**:
- GET /api/v1/classes/my-classes
- GET /api/v1/test-scores (recent)

**State**:
- `classes`: Class[]
- `recentScores`: TestScore[]
- `isLoading`: boolean
- `error`: string | null

---

### 3.2 Class List Component

**Component**: `components/teacher/ClassList.tsx`

**Functionality**:
- Display list of teacher's classes
- Show student count per class
- Navigate to class details
- Filter and search classes

**Props**:
```typescript
interface ClassListProps {
  classes: Class[];
  onClassClick: (classId: string) => void;
}
```

---

### 3.3 Class Details Component

**Component**: `components/teacher/ClassDetails.tsx`

**Functionality**:
- Display class information
- List enrolled students with status (active/withdrawn)
- Enroll existing students via dropdown
- Create new students inline and auto-enroll them
- Withdraw and re-enroll students
- Show student performance summary via ClassSummaryPanel
- Navigate to student details
- Schedule management via ScheduleTab

**Data Fetched**:
- GET /api/v1/classes/{classId}
- GET /api/v1/users/students (for enroll modal)

**State**:
- `classDetail`: ClassDetailDTO | null
- `students`: StudentDTO[] (available for enrollment)
- `showEnroll`: boolean
- `createMode`: boolean (toggle between select-existing and create-new in enroll modal)
- `newStudent`: { email, firstName, lastName, password, grade }
- `isLoading`: boolean

**Enroll Modal (dual-mode)**:
- Default mode: dropdown of existing students not yet enrolled → "Enroll" button
- Create mode (via "+ Create new student" link): inline form with name, email, password, grade → "Create & Enroll" button
- Creates student via POST /api/v1/users/students, then enrolls via POST /api/v1/classes/{classId}/students
- Toggle between modes with links at bottom of modal

---

### 3.4 Test Score Entry Form

**Component**: `components/teacher/TestScoreForm.tsx`

**Functionality**:
- Form to enter test score
- Dynamic question/sub-question fields
- Topic selection per sub-question
- Validation (scores sum to overall)
- Submit to backend

**Props**:
```typescript
interface TestScoreFormProps {
  studentId: string;
  classId: string;
  onSuccess: () => void;
  onCancel: () => void;
}
```

**Form State**:
```typescript
interface TestScoreFormState {
  testName: string;
  testDate: Date;
  overallScore: number;
  questions: {
    questionNumber: string;
    maxScore: number;
    subQuestions: {
      label: string;
      score: number;
      maxScore: number;
      topicId: string;
    }[];
  }[];
}
```

**Validation**:
- All fields required
- Scores between 0-100
- Test date not in future
- Sub-question scores sum to overall score

---

### 3.5 Student Progress View

**Component**: `components/teacher/StudentProgress.tsx`

**Functionality**:
- Display student's test score history
- Show progress charts (line chart)
- Display topic-level performance
- Show teacher feedback

**Data Fetched**:
- GET /api/v1/students/{studentId}/test-scores
- GET /api/v1/students/{studentId}/progress/overall
- GET /api/v1/students/{studentId}/progress/topics

**Charts**:
- Overall score trend (line chart)
- Topic performance (multiple line charts)

---

### 3.6 Feedback Form

**Component**: `components/teacher/FeedbackForm.tsx`

**Functionality**:
- Form to add/edit feedback
- Structured fields (strengths, improvements, recommendations)
- Optional free-form notes
- Template selection
- Submit to backend

**Props**:
```typescript
interface FeedbackFormProps {
  testScoreId: string;
  existingFeedback?: Feedback;
  onSuccess: () => void;
  onCancel: () => void;
}
```

---

## 4. Parent Components

### 4.1 Parent Dashboard

**Component**: `components/parent/ParentDashboard.tsx`

**Functionality**:
- Display child's information
- Show recent test scores
- Display progress summary
- Quick links (view scores, progress, preferences)

**Data Fetched**:
- GET /api/v1/auth/me (get child info)
- GET /api/v1/students/{studentId}/test-scores (recent)
- GET /api/v1/students/{studentId}/progress/overall

**State**:
- `child`: Student | null
- `recentScores`: TestScore[]
- `progressSummary`: ProgressSummary | null

---

### 4.2 Test Score History

**Component**: `components/parent/TestScoreHistory.tsx`

**Functionality**:
- Display list of all test scores
- Show test name, date, score, feedback
- Filter by date range, subject
- Sort by date or score
- View test details

**Data Fetched**:
- GET /api/v1/students/{studentId}/test-scores (with filters)

**Filters**:
- Date range picker
- Subject dropdown
- Sort options

---

### 4.3 Progress Charts View

**Component**: `components/parent/ProgressCharts.tsx`

**Functionality**:
- Display overall progress chart
- Display topic-specific charts
- Show improvement metrics
- Interactive chart (hover for details)

**Data Fetched**:
- GET /api/v1/students/{studentId}/progress/overall
- GET /api/v1/students/{studentId}/progress/topics

**Charts**:
- Overall score trend (line chart with trend line)
- Topic performance (grouped line charts)

---

### 4.4 Notification Preferences

**Component**: `components/parent/NotificationPreferences.tsx`

**Functionality**:
- Toggle email notifications
- Toggle SMS notifications
- Select preferred contact method
- Save preferences

**Data Fetched**:
- GET /api/v1/users/me/notification-preferences
- PUT /api/v1/users/me/notification-preferences

**Form State**:
```typescript
interface NotificationPreferences {
  emailNotificationsEnabled: boolean;
  smsNotificationsEnabled: boolean;
  preferredContactMethod: 'EMAIL' | 'SMS' | 'BOTH';
}
```

---

## 5. Student Components

### 5.1 Student Dashboard

**Component**: `components/student/StudentDashboard.tsx`

**Functionality**:
- Display student's own information
- Show recent test scores
- Display progress summary
- Quick links (view scores, progress)

**Data Fetched**:
- GET /api/v1/auth/me
- GET /api/v1/students/{studentId}/test-scores (recent)
- GET /api/v1/students/{studentId}/progress/overall

---

### 5.2 My Test Scores

**Component**: `components/student/MyTestScores.tsx`

**Functionality**:
- Display list of own test scores
- Show test name, date, score, feedback
- View test details with topic breakdown
- Filter and sort

**Data Fetched**:
- GET /api/v1/students/{studentId}/test-scores

---

### 5.3 My Progress

**Component**: `components/student/MyProgress.tsx`

**Functionality**:
- Display own progress charts
- Show improvement over time
- Display topic-specific performance
- Motivational messages based on progress

**Data Fetched**:
- GET /api/v1/students/{studentId}/progress/overall
- GET /api/v1/students/{studentId}/progress/topics

---

## 6. Admin Components

### 6.1 Admin Dashboard

**Component**: `components/admin/AdminDashboard.tsx`

**Functionality**:
- Display system overview
- Show statistics (total users, classes, students)
- Quick actions (add user, create class)
- Recent activity

---

### 6.2 User Management

**Component**: `components/admin/UserManagement.tsx`

**Functionality**:
- List all users (teachers, parents, students)
- Filter by role
- Create new user
- Edit user details
- Deactivate/reactivate user

**Data Fetched**:
- GET /api/v1/users (with role filter)
- POST /api/v1/users/teachers
- POST /api/v1/users/parents
- POST /api/v1/users/students

---

### 6.3 Class Management

**Component**: `components/admin/ClassManagement.tsx`

**Functionality**:
- List all classes
- Create new class
- Edit class details
- Assign teacher to class
- Enroll/withdraw students

**Data Fetched**:
- GET /api/v1/classes
- POST /api/v1/classes
- POST /api/v1/classes/{classId}/students

---

### 6.4 Subject Management

**Component**: `components/admin/SubjectManagement.tsx`

**Functionality**:
- List all subjects and topics
- Add custom subject
- Add custom topic
- Deactivate subject/topic

**Data Fetched**:
- GET /api/v1/subjects
- POST /api/v1/subjects
- POST /api/v1/subjects/{subjectId}/topics

---

## 7. Shared Components

### 7.1 Chart Component

**Component**: `components/shared/Chart.tsx`

**Purpose**: Reusable chart component using Chart.js or Recharts

**Props**:
```typescript
interface ChartProps {
  type: 'line' | 'bar' | 'pie';
  data: ChartData;
  options?: ChartOptions;
  title?: string;
}
```

---

### 7.2 Data Table Component

**Component**: `components/shared/DataTable.tsx`

**Purpose**: Reusable table with sorting, filtering, pagination

**Props**:
```typescript
interface DataTableProps<T> {
  data: T[];
  columns: Column<T>[];
  onRowClick?: (row: T) => void;
  pagination?: boolean;
  pageSize?: number;
}
```

---

### 7.3 Form Components

**Components**:
- `components/shared/Input.tsx` - Text input with validation
- `components/shared/Select.tsx` - Dropdown select
- `components/shared/DatePicker.tsx` - Date picker
- `components/shared/Button.tsx` - Styled button
- `components/shared/Modal.tsx` - Modal dialog

---

### 7.4 Layout Components

**Components**:
- `components/shared/Layout.tsx` - Main layout with navigation
- `components/shared/Navbar.tsx` - Top navigation bar
- `components/shared/Sidebar.tsx` - Side navigation (role-based)
- `components/shared/Footer.tsx` - Footer

---

### 7.5 Loading and Error Components

**Components**:
- `components/shared/LoadingSpinner.tsx` - Loading indicator
- `components/shared/ErrorMessage.tsx` - Error display
- `components/shared/EmptyState.tsx` - Empty state placeholder

---

## 8. Services

### 8.1 API Client Service

**Service**: `services/apiClient.ts`

**Purpose**: Centralized HTTP client with authentication

**Functions**:
```typescript
class ApiClient {
  private baseURL: string;
  private getAuthToken: () => string | null;

  async get<T>(url: string, params?: any): Promise<T>;
  async post<T>(url: string, data: any): Promise<T>;
  async put<T>(url: string, data: any): Promise<T>;
  async delete<T>(url: string): Promise<T>;
}
```

**Features**:
- Automatic JWT token injection
- Request/response interceptors
- Error handling
- Retry logic for transient failures

---

### 8.2 Keycloak Service

**Service**: `services/keycloakService.ts`

**Purpose**: Keycloak integration

**Functions**:
```typescript
class KeycloakService {
  init(): Promise<boolean>;
  login(): void;
  logout(): void;
  getToken(): string | null;
  getUserInfo(): UserInfo | null;
  hasRole(role: string): boolean;
  refreshToken(): Promise<boolean>;
}
```

---

## 9. Custom Hooks

### 9.1 useAuth Hook

**Hook**: `hooks/useAuth.ts`

**Purpose**: Access authentication context

**Returns**:
```typescript
interface UseAuthReturn {
  isAuthenticated: boolean;
  user: UserInfo | null;
  login: () => void;
  logout: () => void;
  hasRole: (role: string) => boolean;
}
```

---

### 9.2 useApi Hook

**Hook**: `hooks/useApi.ts`

**Purpose**: Fetch data from API with loading and error states

**Usage**:
```typescript
const { data, loading, error, refetch } = useApi<TestScore[]>(
  '/api/v1/test-scores',
  { params: { studentId } }
);
```

---

### 9.3 useForm Hook

**Hook**: `hooks/useForm.ts`

**Purpose**: Form state management and validation

**Usage**:
```typescript
const { values, errors, handleChange, handleSubmit } = useForm({
  initialValues: { testName: '', testDate: '', overallScore: 0 },
  validate: validateTestScore,
  onSubmit: submitTestScore
});
```

---

## 10. Context Providers

### 10.1 Auth Context

**Context**: `context/AuthContext.tsx`

**Purpose**: Global authentication state

**Provided Value**:
- User information
- Authentication status
- Login/logout functions
- Role checking

---

### 10.2 Theme Context

**Context**: `context/ThemeContext.tsx`

**Purpose**: Theme management (light/dark mode)

**Provided Value**:
- Current theme
- Toggle theme function

---

## 11. TypeScript Types

### 11.1 API Response Types

**File**: `types/api.ts`

**Types**:
```typescript
interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
}

interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

interface ErrorResponse {
  code: string;
  message: string;
  details?: any;
  timestamp: string;
}
```

---

### 11.2 Domain Types

**File**: `types/domain.ts`

**Types**: Match backend DTOs
```typescript
interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
}

interface Student {
  id: string;
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  dateOfBirth?: string;
  grade?: string;
  parentId?: string;
}

interface TestScore {
  id: string;
  studentId: string;
  studentName: string;
  classId: string;
  className: string;
  testName: string;
  testDate: string;
  overallScore: number;
  questions: Question[];
}

// ... other domain types
```

---

## Summary

This component architecture provides:
- **Authentication**: Login, protected routes, Keycloak integration
- **Role-Based Components**: Teacher, Parent, Student, Admin dashboards and features
- **Shared Components**: Reusable UI components (charts, tables, forms, layout)
- **Services**: API client, Keycloak service
- **Custom Hooks**: useAuth, useApi, useForm
- **Context Providers**: Auth, Theme
- **TypeScript Types**: Strong typing for API responses and domain models

All components follow React best practices, use TypeScript for type safety, and integrate with the backend API defined in Unit 1.

---

**Document Version**: 1.0  
**Created**: 2026-03-08  
**Status**: Draft
