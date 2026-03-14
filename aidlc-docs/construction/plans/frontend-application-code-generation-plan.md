# Code Generation Plan - Frontend Application (UNIT-02)

## Unit Context

- **Unit ID**: UNIT-02
- **Unit Name**: Frontend Application
- **Technology**: React 18+, TypeScript 5+, Vite, Tailwind CSS, Recharts, React Router 6, Axios, keycloak-js, React Hook Form, Zod
- **Deployment**: Static site (S3 + CloudFront)
- **Dependencies**: Backend API (UNIT-01), Keycloak (external)
- **Code Location**: `spm-frontend/` (workspace root, sibling to `spm/` backend)

## Stories Covered

**Foundation**: AUTH-1, AUTH-2, AUTH-3, AUTH-4
**Teacher Journey**: TEACH-1, TEACH-2, TEACH-3, TEACH-4, TEACH-5, TEACH-6
**Parent Journey**: PARENT-1, PARENT-2, PARENT-3, PARENT-4, PARENT-7
**Student Journey**: STUDENT-1, STUDENT-2, STUDENT-3
**Admin Journey**: ADMIN-1, ADMIN-2, ADMIN-3, ADMIN-4, ADMIN-5
**Supporting**: UI-1, REPORT-1

## Code Generation Steps

### Step 1: Project Setup & Configuration
- [x] Initialize Vite + React + TypeScript project
- [x] Configure `tsconfig.json` with strict mode
- [x] Configure `vite.config.ts` with proxy for API
- [x] Install and configure dependencies: react-router-dom, axios, keycloak-js, recharts, tailwindcss, @headlessui/react, react-hook-form, zod, @hookform/resolvers
- [x] Configure Tailwind CSS (`postcss.config.js`)
- [x] Configure ESLint (`eslint.config.js`)
- [x] Create `.env` and `.env.example` with `VITE_API_BASE_URL` and `VITE_KEYCLOAK_URL`, `VITE_KEYCLOAK_REALM`, `VITE_KEYCLOAK_CLIENT_ID`
- [x] Create base `index.html` with meta tags

### Step 2: TypeScript Types & API Response Models
- [x] Create `src/types/api.ts` - ApiResponse, PagedResponse, ErrorResponse types
- [x] Create `src/types/domain.ts` - User, Student, Teacher, Parent, Class, TestScore, Question, SubQuestion, Feedback, Subject, Topic, Notification, ProgressReport types
- [x] Create `src/types/forms.ts` - Form input types for test score entry, user creation, class creation, feedback

### Step 3: Keycloak Authentication Service
- [x] Create `src/services/keycloakService.ts` - Keycloak initialization, login, logout, token management, role checking
- [x] Create `src/context/AuthContext.tsx` - AuthProvider with Keycloak state, user info, role helpers
- [x] Create `src/hooks/useAuth.ts` - Hook to consume AuthContext

### Step 4: API Client & Service Layer
- [x] Create `src/services/apiClient.ts` - Axios instance with JWT interceptor, error interceptor, retry logic, base URL config
- [x] Create `src/services/userService.ts` - createTeacher, createParent, createStudent, deactivateUser, reactivateUser, getUsers
- [x] Create `src/services/classService.ts` - createClass, getMyClasses, getClassDetails, enrollStudent, withdrawStudent, changeTeacher
- [x] Create `src/services/testScoreService.ts` - createTestScore, getStudentTestScores, getTestScoreDetails, updateTestScore, deleteTestScore
- [x] Create `src/services/progressService.ts` - getOverallProgress, getTopicProgress, getAllTopicsProgress
- [x] Create `src/services/feedbackService.ts` - createFeedback, updateFeedback, getFeedbackTemplates, createFeedbackTemplate
- [x] Create `src/services/subjectService.ts` - getSubjects, getSubjectWithTopics, createSubject, createTopic, deactivateSubject, deactivateTopic
- [x] Create `src/services/notificationService.ts` - getMyNotifications, updateNotificationPreferences
- [x] Create `src/services/reportService.ts` - generateReport, getReport, listStudentReports

### Step 5: Custom Hooks
- [x] Create `src/hooks/useApi.ts` - Generic data fetching hook with loading/error/data states
- [x] Create `src/hooks/useDebounce.ts` - Debounce hook for search/filter inputs
- [x] Create `src/hooks/usePagination.ts` - Pagination state management hook

### Step 6: Shared UI Components
- [x] Create `src/components/shared/Layout.tsx` - Main layout with sidebar and top nav
- [x] Create `src/components/shared/Sidebar.tsx` - Role-based sidebar navigation
- [x] Create `src/components/shared/Navbar.tsx` - Top navigation bar with user menu, logout
- [x] Create `src/components/shared/LoadingSpinner.tsx` - Loading indicator
- [x] Create `src/components/shared/ErrorMessage.tsx` - Error display component
- [x] Create `src/components/shared/EmptyState.tsx` - Empty state placeholder
- [x] Create `src/components/shared/DataTable.tsx` - Reusable table with sorting, pagination
- [x] Create `src/components/shared/Modal.tsx` - Modal dialog
- [x] Create `src/components/shared/Chart.tsx` - Reusable line chart wrapper using Recharts
- [x] Create `src/components/shared/ConfirmDialog.tsx` - Confirmation dialog
- [x] Create `src/components/shared/Toast.tsx` - Toast notification system
- [x] Create `src/components/shared/PageHeader.tsx` - Page header with breadcrumbs

### Step 7: Auth Components & Routing
- [x] Create `src/components/auth/Login.tsx` - Login page with Keycloak redirect
- [x] Create `src/components/auth/DashboardRedirect.tsx` - Role-based dashboard redirect (replaces Callback)
- [x] Create `src/components/auth/ProtectedRoute.tsx` - Route guard with role checking
- [x] Create `src/components/auth/AccessDenied.tsx` - 403 page
- [x] Create `src/components/auth/NotFound.tsx` - 404 page
- [x] Create `src/App.tsx` - Root component with routing, lazy loading, error boundary
- [x] Create `src/main.tsx` - Entry point with AuthProvider, Router

### Step 8: Teacher Components (Stories: TEACH-1 through TEACH-6)
- [x] Create `src/components/teacher/TeacherDashboard.tsx` - Dashboard with class overview, recent activity, stats
- [x] Create `src/components/teacher/ClassList.tsx` - List of teacher's classes with search
- [x] Create `src/components/teacher/ClassDetails.tsx` - Class info with enrolled students list
- [x] Create `src/components/teacher/StudentDetails.tsx` - Student info with score history and actions
- [x] Create `src/components/teacher/TestScoreForm.tsx` - Dynamic form for recording test scores with questions/sub-questions/topics
- [x] Create `src/components/teacher/FeedbackForm.tsx` - Feedback form with template selection
- [x] Create `src/components/teacher/StudentProgress.tsx` - Student progress charts (overall + topic)
- [x] Create `src/components/teacher/TeacherRoutes.tsx` - Teacher route definitions

### Step 9: Parent Components (Stories: PARENT-1 through PARENT-4, PARENT-7)
- [x] Create `src/components/parent/ParentDashboard.tsx` - Dashboard with child info, recent scores, progress summary
- [x] Create `src/components/parent/TestScoreHistory.tsx` - Filterable test score list with detail view
- [x] Create `src/components/parent/ProgressCharts.tsx` - Overall and topic progress charts
- [x] Create `src/components/parent/NotificationPreferences.tsx` - Email/SMS preference toggles
- [x] Create `src/components/parent/ParentRoutes.tsx` - Parent route definitions

### Step 10: Student Components (Stories: STUDENT-1 through STUDENT-3)
- [x] Create `src/components/student/StudentDashboard.tsx` - Dashboard with own scores and progress
- [x] Create `src/components/student/MyTestScores.tsx` - Own test score history with filters
- [x] Create `src/components/student/MyProgress.tsx` - Own progress charts
- [x] Create `src/components/student/StudentRoutes.tsx` - Student route definitions

### Step 11: Admin Components (Stories: ADMIN-1 through ADMIN-5)
- [x] Create `src/components/admin/AdminDashboard.tsx` - System overview with stats
- [x] Create `src/components/admin/UserManagement.tsx` - User list with role filter, create/edit/deactivate
- [x] Create `src/components/admin/CreateUserForm.tsx` - User creation form (teacher/parent/student)
- [x] Create `src/components/admin/ClassManagement.tsx` - Class list with create/edit, student enrollment
- [x] Create `src/components/admin/CreateClassForm.tsx` - Class creation form
- [x] Create `src/components/admin/SubjectManagement.tsx` - Subject/topic list with create/deactivate
- [x] Create `src/components/admin/AdminRoutes.tsx` - Admin route definitions

### Step 12: Error Boundary & Global Error Handling
- [x] Create `src/components/shared/ErrorBoundary.tsx` - React error boundary with fallback UI
- [x] Add error boundary wrapping at route level in App.tsx

### Step 13: Global Styles & Responsive Design (Story: UI-1)
- [x] Create `src/index.css` - Tailwind imports, base styles, custom utilities
- [ ] Verify all components use responsive Tailwind classes (mobile-first)
- [ ] Verify touch targets >= 44px on interactive elements

### Step 14: Unit Tests - Services & Hooks
- [ ] Create `src/services/__tests__/apiClient.test.ts` - API client interceptor tests
- [ ] Create `src/hooks/__tests__/useAuth.test.ts` - Auth hook tests
- [ ] Create `src/hooks/__tests__/useApi.test.ts` - API hook tests
- [ ] Create `src/hooks/__tests__/useDebounce.test.ts` - Debounce hook tests

### Step 15: Unit Tests - Components
- [ ] Create `src/components/auth/__tests__/ProtectedRoute.test.tsx` - Route guard tests
- [ ] Create `src/components/teacher/__tests__/TestScoreForm.test.tsx` - Score form validation tests
- [ ] Create `src/components/shared/__tests__/DataTable.test.tsx` - Table component tests
- [ ] Create `src/components/shared/__tests__/Chart.test.tsx` - Chart component tests

### Step 16: Documentation
- [ ] Create `spm-frontend/README.md` - Setup instructions, environment variables, scripts, project structure
- [ ] Create `aidlc-docs/construction/frontend-application/code/code-summary.md` - Summary of generated code

### Step 17: Build & Deployment Configuration
- [ ] Verify `vite build` produces optimized output
- [ ] Configure code splitting in vite.config.ts (vendor chunk, per-role chunks)

### Step 18: Final Verification
- [ ] Verify all TypeScript types compile without errors
- [ ] Verify all imports resolve correctly
- [ ] Verify route structure matches user-flows.md
- [ ] Mark all stories as implemented

## Estimated Scope

- **Total Files**: ~75 TypeScript/TSX files
- **Steps**: 18
- **Key Libraries**: React 18, TypeScript 5, Vite, Tailwind CSS, React Router 6, Axios, keycloak-js, Recharts, React Hook Form, Zod, Headless UI
- **Test Files**: ~8 test files covering critical paths

## Extension Compliance

| Extension | Status | Rationale |
|---|---|---|
| Security Baseline | Disabled | Decided at Requirements Analysis stage |
