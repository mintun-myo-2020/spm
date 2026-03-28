# SPM Frontend — Student Progress Management

React 19 + TypeScript + Vite 8 + Tailwind CSS 4 frontend for the Student Progress Management system.

## Prerequisites

- Node.js 20.19+ or 22.12+
- Backend API running at `http://localhost:8080`
- Keycloak instance running at `http://localhost:8180`

## Setup

```bash
npm install
cp .env.example .env   # Defaults work for local dev
npm run dev             # Starts on http://localhost:5173
```

## Environment Variables

| Variable | Description | Default |
|---|---|---|
| `VITE_API_BASE_URL` | Backend API base URL | `http://localhost:8080/api/v1` |
| `VITE_KEYCLOAK_URL` | Keycloak server URL | `http://localhost:8180` |
| `VITE_KEYCLOAK_REALM` | Keycloak realm name | `spm` |
| `VITE_KEYCLOAK_CLIENT_ID` | Keycloak client ID | `spm-frontend` |
| `VITE_TENANT_NAME` | Navbar display name | `Eggtive SPM` |

## Scripts

```bash
npm run dev        # Dev server (port 5173)
npm run build      # TypeScript check + production build
npm run preview    # Preview production build
npm run test       # Unit tests (single run)
npm run lint       # ESLint
```

## Project Structure

```
src/
├── components/
│   ├── admin/       # Admin dashboard, users, classes, subjects, schedule overview
│   ├── auth/        # Login, ProtectedRoute, AccessDenied, NotFound
│   ├── parent/      # Dashboard, scores, progress, schedule, notifications
│   ├── shared/      # Layout, DataTable, Calendar, AttendanceTable, Chart, Modal, Toast
│   ├── student/     # Dashboard, scores, progress, schedule with RSVP, upload
│   └── teacher/     # Dashboard, classes, scores, feedback, scheduling, attendance
├── context/         # AuthContext (Keycloak)
├── hooks/           # useAuth, useApi, useDebounce, usePagination
├── services/        # API client + domain services (class, scheduling, progress, report, etc.)
├── test/            # Test setup
└── types/           # TypeScript types (api, domain, forms)
```

## Key Features by Role

### Teacher
- Class management with student enrollment
- Test score entry (manual + OCR upload)
- Per-test feedback with templates
- Weekly schedule management + one-off sessions
- Attendance marking (batch + individual)
- Progress reports with AI improvement plans

### Student
- View test scores and progress trends
- Upload test papers for OCR extraction
- Schedule view with RSVP (opt-out model)
- View improvement plans

### Parent
- View child's scores, progress, and reports
- Schedule view with RSVP on behalf of child
- Notification preferences

### Admin
- Full user/class/subject management
- Schedule overview across all classes
- All teacher capabilities for any class
