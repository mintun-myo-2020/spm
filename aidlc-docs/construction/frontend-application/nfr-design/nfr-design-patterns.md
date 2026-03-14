# NFR Design Patterns - Frontend Application

## Overview
Design patterns and architectural approaches for implementing NFRs in the Frontend Application.

---

## 1. Performance Patterns

### 1.1 Code Splitting Pattern

**Purpose**: Reduce initial bundle size by loading code on demand

**Implementation**: React.lazy with route-based splitting

```typescript
// Route-based code splitting
const TeacherDashboard = lazy(() => import('./components/teacher/TeacherDashboard'));
const ParentDashboard = lazy(() => import('./components/parent/ParentDashboard'));
const StudentDashboard = lazy(() => import('./components/student/StudentDashboard'));
const AdminDashboard = lazy(() => import('./components/admin/AdminDashboard'));

function App() {
  return (
    <Suspense fallback={<LoadingSpinner />}>
      <Routes>
        <Route path="/teacher/*" element={<TeacherDashboard />} />
        <Route path="/parent/*" element={<ParentDashboard />} />
        <Route path="/student/*" element={<StudentDashboard />} />
        <Route path="/admin/*" element={<AdminDashboard />} />
      </Routes>
    </Suspense>
  );
}
```

**Splitting Strategy**:
- Split by role (teacher, parent, student, admin)
- Split chart library (loaded only on progress pages)
- Split form library (loaded only on data entry pages)

---

### 1.2 Memoization Pattern

**Purpose**: Prevent unnecessary re-renders

**Implementation**: React.memo, useMemo, useCallback

```typescript
// Memoize expensive chart calculations
const chartData = useMemo(() => {
  return scores.map(s => ({ date: s.testDate, score: s.overallScore }));
}, [scores]);

// Memoize callback to prevent child re-renders
const handleScoreSubmit = useCallback(async (data: TestScoreForm) => {
  await apiClient.post('/test-scores', data);
  refetch();
}, [refetch]);

// Memoize component
const ScoreCard = React.memo(({ score }: { score: TestScore }) => (
  <div>{score.testName}: {score.overallScore}</div>
));
```

---

### 1.3 Debounce Pattern

**Purpose**: Reduce API calls for search and filter inputs

**Implementation**: Custom useDebounce hook

```typescript
function useDebounce<T>(value: T, delay: number = 300): T {
  const [debouncedValue, setDebouncedValue] = useState(value);
  useEffect(() => {
    const timer = setTimeout(() => setDebouncedValue(value), delay);
    return () => clearTimeout(timer);
  }, [value, delay]);
  return debouncedValue;
}

// Usage in search
const [searchTerm, setSearchTerm] = useState('');
const debouncedSearch = useDebounce(searchTerm, 300);

useEffect(() => {
  if (debouncedSearch) fetchResults(debouncedSearch);
}, [debouncedSearch]);
```

---

### 1.4 Optimistic Updates Pattern

**Purpose**: Improve perceived performance by updating UI before server confirms

**Applied To**: Toggle operations (notification preferences, deactivate/activate)

```typescript
const toggleEmailNotifications = async () => {
  // Optimistic update
  setPreferences(prev => ({ ...prev, emailEnabled: !prev.emailEnabled }));
  try {
    await apiClient.put('/users/me/notification-preferences', {
      emailNotificationsEnabled: !preferences.emailEnabled
    });
  } catch (error) {
    // Rollback on failure
    setPreferences(prev => ({ ...prev, emailEnabled: !prev.emailEnabled }));
    showError('Failed to update preferences');
  }
};
```

---

## 2. Security Patterns

### 2.1 Token Management Pattern

**Purpose**: Securely manage JWT tokens without exposing to XSS

**Implementation**: In-memory token storage with Keycloak JS adapter

```typescript
class TokenManager {
  private accessToken: string | null = null;
  private refreshToken: string | null = null;

  setTokens(access: string, refresh: string) {
    this.accessToken = access;
    this.refreshToken = refresh;
  }

  getAccessToken(): string | null {
    return this.accessToken;
  }

  clearTokens() {
    this.accessToken = null;
    this.refreshToken = null;
  }
}
```

**Key Decisions**:
- Tokens stored in memory only (not localStorage)
- Tokens cleared on page refresh (re-auth via Keycloak silent check)
- Refresh token used to obtain new access token before expiry

---

### 2.2 API Interceptor Pattern

**Purpose**: Centralize authentication and error handling for all API calls

**Implementation**: Axios interceptors

```typescript
const apiClient = axios.create({ baseURL: '/api/v1' });

// Request interceptor: attach JWT
apiClient.interceptors.request.use((config) => {
  const token = tokenManager.getAccessToken();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Response interceptor: handle errors
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      const refreshed = await keycloakService.refreshToken();
      if (refreshed) return apiClient.request(error.config); // Retry
      keycloakService.logout(); // Force re-login
    }
    return Promise.reject(error);
  }
);
```

---

### 2.3 Route Protection Pattern

**Purpose**: Prevent unauthorized access to role-specific pages

**Implementation**: Higher-order component wrapping routes

```typescript
function ProtectedRoute({ children, requiredRoles }: ProtectedRouteProps) {
  const { isAuthenticated, hasRole } = useAuth();

  if (!isAuthenticated) return <Navigate to="/login" />;
  if (requiredRoles && !requiredRoles.some(r => hasRole(r))) {
    return <Navigate to="/403" />;
  }
  return <>{children}</>;
}

// Usage
<Route path="/teacher/*" element={
  <ProtectedRoute requiredRoles={['TEACHER']}>
    <TeacherRoutes />
  </ProtectedRoute>
} />
```

---

## 3. Resilience Patterns

### 3.1 Error Boundary Pattern

**Purpose**: Catch and handle React component errors gracefully

**Implementation**: React Error Boundary

```typescript
class ErrorBoundary extends React.Component<Props, State> {
  state = { hasError: false, error: null };

  static getDerivedStateFromError(error: Error) {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, info: React.ErrorInfo) {
    console.error('Component error:', error, info);
  }

  render() {
    if (this.state.hasError) {
      return <ErrorFallback error={this.state.error} onRetry={() => 
        this.setState({ hasError: false })} />;
    }
    return this.props.children;
  }
}
```

**Placement**:
- Root level: Catch any unhandled error
- Route level: Isolate errors per page
- Widget level: Isolate errors per component (charts, tables)

---

### 3.2 Retry Pattern

**Purpose**: Automatically retry failed API calls

**Implementation**: Axios retry interceptor

```typescript
const MAX_RETRIES = 2;

apiClient.interceptors.response.use(null, async (error) => {
  const config = error.config;
  if (!config._retryCount) config._retryCount = 0;
  
  if (config._retryCount < MAX_RETRIES && isRetryable(error)) {
    config._retryCount++;
    await delay(1000 * config._retryCount); // Linear backoff
    return apiClient.request(config);
  }
  return Promise.reject(error);
});

function isRetryable(error: AxiosError): boolean {
  return !error.response || error.response.status >= 500;
}
```

---

### 3.3 Loading State Pattern

**Purpose**: Consistent loading experience across the app

**Implementation**: Custom hook with loading/error/data states

```typescript
type AsyncState<T> = 
  | { status: 'idle' }
  | { status: 'loading' }
  | { status: 'success'; data: T }
  | { status: 'error'; error: string };

function useAsync<T>(asyncFn: () => Promise<T>, deps: any[]) {
  const [state, setState] = useState<AsyncState<T>>({ status: 'idle' });

  useEffect(() => {
    setState({ status: 'loading' });
    asyncFn()
      .then(data => setState({ status: 'success', data }))
      .catch(err => setState({ status: 'error', error: err.message }));
  }, deps);

  return state;
}
```

---

## 4. Accessibility Patterns

### 4.1 Focus Management Pattern

**Purpose**: Manage focus for keyboard navigation

**Implementation**:
- Auto-focus first input on form mount
- Return focus to trigger element on modal close
- Skip navigation link at top of page
- Focus trap inside modals

### 4.2 ARIA Pattern

**Purpose**: Provide screen reader context

**Implementation**:
- `aria-label` on icon buttons
- `aria-live="polite"` on dynamic content (toast notifications)
- `role="alert"` on error messages
- `aria-describedby` linking inputs to error messages

---

## 5. Deployment Pattern

### 5.1 Static Site Deployment

**Build**: `vite build` → produces `dist/` folder

**Deploy to S3**:
1. Upload `dist/` contents to S3 bucket
2. Set `index.html` as default document
3. Configure error document to `index.html` (SPA routing)

**CloudFront Configuration**:
- Origin: S3 bucket
- Default root object: index.html
- Custom error response: 404 → /index.html (200 status)
- Cache behavior: 
  - `index.html`: max-age=300 (5 minutes)
  - `assets/*`: max-age=31536000 (1 year, immutable)
- HTTPS only, redirect HTTP to HTTPS
- Compress objects automatically (gzip, brotli)

**Cache Busting**: Vite adds content hash to asset filenames

---

## Summary

Frontend NFR design patterns:
- **Performance**: Code splitting, memoization, debouncing, optimistic updates
- **Security**: In-memory token storage, API interceptors, route protection
- **Resilience**: Error boundaries, retry logic, consistent loading states
- **Accessibility**: Focus management, ARIA attributes
- **Deployment**: S3 + CloudFront with cache optimization

---

**Document Version**: 1.0  
**Created**: 2026-03-08  
**Status**: Draft
