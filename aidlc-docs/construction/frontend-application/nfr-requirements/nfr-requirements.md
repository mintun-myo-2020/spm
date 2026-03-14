# Non-Functional Requirements - Frontend Application

## Overview
NFR requirements for the Student Progress Tracking System Frontend Application (React TypeScript SPA).

---

## 1. Performance Requirements

### 1.1 Page Load Performance
- **First Contentful Paint (FCP)**: < 1.5 seconds
- **Largest Contentful Paint (LCP)**: < 2.5 seconds
- **Time to Interactive (TTI)**: < 3.5 seconds
- **Cumulative Layout Shift (CLS)**: < 0.1
- **First Input Delay (FID)**: < 100ms

### 1.2 Runtime Performance
- **Route transitions**: < 300ms
- **API response rendering**: < 500ms after data received
- **Chart rendering**: < 1 second for up to 100 data points
- **Form interactions**: Immediate (< 50ms)
- **Search/filter**: < 200ms debounced

### 1.3 Bundle Size
- **Initial bundle**: < 200 KB gzipped
- **Code splitting**: Lazy load per route/role
- **Tree shaking**: Remove unused code
- **Image optimization**: WebP format, lazy loading

---

## 2. Availability Requirements

### 2.1 Static Hosting
- **Uptime**: 99.9% (S3 + CloudFront)
- **CDN**: CloudFront for global distribution
- **Failover**: CloudFront origin failover

### 2.2 Offline Behavior
- **No offline support** for MVP
- **Graceful degradation**: Show "connection lost" message when API unavailable
- **Retry**: Automatic retry for failed API calls

---

## 3. Security Requirements

### 3.1 Authentication
- **Token storage**: In-memory only (not localStorage/sessionStorage)
- **Token refresh**: Automatic before expiration
- **Logout**: Clear all tokens and redirect to Keycloak logout
- **PKCE**: Use PKCE flow for OAuth2 (public client)

### 3.2 Client-Side Security
- **XSS prevention**: React's built-in escaping, no dangerouslySetInnerHTML
- **CSP headers**: Content Security Policy via CloudFront
- **HTTPS only**: Enforce HTTPS via CloudFront
- **No sensitive data in URL**: Use POST for sensitive operations
- **Input sanitization**: Sanitize user inputs before display

### 3.3 Dependency Security
- **npm audit**: Run on every build
- **Dependabot**: Automated dependency updates
- **Lock file**: package-lock.json committed

---

## 4. Tech Stack Decisions

### 4.1 Core Framework
- **React 18+**: Component-based UI framework
- **TypeScript 5+**: Type safety and developer experience
- **Vite**: Fast build tool with HMR

### 4.2 Routing & State
- **React Router 6**: Client-side routing
- **React Context API**: Global state (auth, theme)
- **Custom hooks**: Server state management

### 4.3 UI & Styling
- **Tailwind CSS**: Utility-first CSS framework
- **Headless UI**: Accessible component primitives
- **Recharts**: Chart library (React-native, responsive)

### 4.4 HTTP & Auth
- **Axios**: HTTP client with interceptors
- **keycloak-js**: Keycloak JavaScript adapter

### 4.5 Forms & Validation
- **React Hook Form**: Performant form management
- **Zod**: Schema-based validation

### 4.6 Testing
- **Vitest**: Unit testing (Vite-native)
- **React Testing Library**: Component testing
- **Playwright**: E2E testing
- **Coverage target**: 70% line coverage

### 4.7 Code Quality
- **ESLint**: Linting with React/TypeScript rules
- **Prettier**: Code formatting
- **Husky**: Pre-commit hooks

### 4.8 Build & Deploy
- **Vite**: Build tool
- **GitHub Actions**: CI/CD pipeline
- **AWS S3 + CloudFront**: Static hosting with CDN
- **Deployment**: Upload build artifacts to S3, invalidate CloudFront cache

---

## 5. Scalability Requirements

### 5.1 Client-Side Scalability
- **Pagination**: All list views paginated (no infinite scroll for MVP)
- **Lazy loading**: Route-based code splitting
- **Virtual scrolling**: For large lists (> 100 items)
- **Debouncing**: Search and filter inputs debounced (300ms)

### 5.2 CDN Scalability
- **CloudFront**: Handles any traffic volume
- **Cache headers**: Long cache for static assets (1 year), short for index.html (5 min)
- **Versioned assets**: Hash-based filenames for cache busting

---

## 6. Monitoring & Logging

### 6.1 Error Tracking
- **Console errors**: Captured and logged
- **API errors**: Logged with request context
- **React Error Boundaries**: Catch and report component errors

### 6.2 Analytics (Future)
- **Page views**: Track route changes
- **User actions**: Track key interactions
- **Performance metrics**: Web Vitals reporting

---

## 7. Responsive Design

### 7.1 Breakpoints
- Mobile: < 640px
- Tablet: 640px - 1024px
- Desktop: > 1024px

### 7.2 Browser Support
- Chrome (last 2 versions)
- Firefox (last 2 versions)
- Safari (last 2 versions)
- Edge (last 2 versions)
- Mobile Safari (iOS 15+)
- Chrome Mobile (Android 10+)

---

**Document Version**: 1.0  
**Created**: 2026-03-08  
**Status**: Draft
