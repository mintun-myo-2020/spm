# SPM Frontend — Student Progress Management

React 19 + TypeScript + Vite 8 + Tailwind CSS 4 frontend for the Student Progress Management system.

## Prerequisites

- Node.js 20.19+ or 22.12+
- Backend API running at `http://localhost:8080`
- Keycloak instance running

## Setup

```bash
npm install
cp .env.example .env   # Edit with your Keycloak settings
```

## Environment Variables

| Variable | Description | Default |
|---|---|---|
| `VITE_API_BASE_URL` | Backend API base URL | `/api/v1` |
| `VITE_KEYCLOAK_URL` | Keycloak server URL | `http://localhost:9090` |
| `VITE_KEYCLOAK_REALM` | Keycloak realm name | `spm` |
| `VITE_KEYCLOAK_CLIENT_ID` | Keycloak client ID | `spm-frontend` |

## Scripts

```bash
npm run dev       # Start dev server on port 3000
npm run build     # TypeScript check + production build
npm run preview   # Preview production build
npm run test      # Run unit tests (single run)
npm run test:watch # Run tests in watch mode
npm run lint      # ESLint
```

## Project Structure

```
src/
├── components/
│   ├── admin/       # Admin dashboard, user/class/subject management
│   ├── auth/        # Login, ProtectedRoute, AccessDenied, NotFound
│   ├── parent/      # Parent dashboard, scores, progress, notifications
│   ├── shared/      # Layout, DataTable, Chart, Modal, Toast, etc.
│   ├── student/     # Student dashboard, scores, progress
│   └── teacher/     # Teacher dashboard, classes, score entry, feedback
├── context/         # AuthContext (Keycloak)
├── hooks/           # useAuth, useApi, useDebounce, usePagination
├── services/        # API client + domain service modules
├── test/            # Test setup
└── types/           # TypeScript types (api, domain, forms)
```
