# SPM — Student Progress Management

A tuition centre management system for tracking student progress, test scores, feedback, scheduling, and report generation.

## Features

- Multi-role access: Admin, Teacher, Student, Parent
- Test score entry with per-question topic mapping
- OCR test paper upload (Tesseract local / AWS Textract)
- Progress tracking with trend analysis and topic breakdowns
- AI-powered improvement plans (AWS Bedrock)
- Async progress report generation with HTML export
- Class scheduling with weekly recurrence and one-off sessions
- Attendance tracking with batch marking and student RSVP
- Feedback templates and per-test feedback
- Notification system (email/SMS stubs)

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 25, Spring Boot 4.x, Spring Security OAuth2 |
| Database | PostgreSQL 18, Flyway migrations |
| Frontend | React 19, TypeScript, Vite 8, Tailwind CSS 4, Flowbite React |
| Auth | Keycloak 26 (OIDC/OAuth2) — swappable via standard OIDC |
| AI/LLM | AWS Bedrock (Nova) — behind interface, stub available |
| OCR | Tesseract (local) / AWS Textract — behind interface |
| Storage | Local filesystem / AWS S3 — behind interface |

## Quick Start (Local Dev)

### Prerequisites

- Java 25 (e.g., via SDKMAN: `sdk install java 25-open`)
- Node.js 20.19+ or 22.12+
- Docker & Docker Compose

### 1. Start infrastructure

```bash
cd spm
cp .env.example .env        # Edit passwords if desired
docker compose up -d         # Starts PostgreSQL, Keycloak, Tesseract OCR
```

Wait for Keycloak to be healthy (~60s). Check: `docker compose ps`

### 2. Start backend

```bash
cd spm
./gradlew bootRun
```

Backend runs on http://localhost:8080. Flyway auto-migrates the DB and seeds demo data.

### 3. Start frontend

```bash
cd spm-frontend
npm install
cp .env.example .env         # Defaults work for local dev
npm run dev
```

Frontend runs on http://localhost:5173.

### 4. Login

Open http://localhost:5173. Keycloak login page appears. Use the demo accounts from the realm import:

| Role | Email | Password |
|---|---|---|
| Admin | admin@spm.local | admin |
| Teacher (Math) | teacher1@spm.local | teacher1 |
| Teacher (Physics) | teacher2@spm.local | teacher2 |
| Parent | parent1@spm.local | parent1 |
| Student | student1@spm.local | student1 |

## Project Structure

```
spm/                          # Backend (Spring Boot)
  src/main/java/.../
    auth/                     # Security, JWT, role conversion
    classmanagement/          # Classes, enrollments
    common/                   # Shared DTOs, enums, exceptions, base entity
    feedback/                 # Teacher feedback on test scores
    notification/             # Notification system
    progress/                 # Progress calculation, trends
    report/                   # Async report generation, LLM plans
    scheduling/               # Class schedules, sessions, attendance
    subject/                  # Subjects and topics
    testpaper/                # OCR upload, extraction
    testscore/                # Test score entry, questions
    user/                     # Users, teachers, students, parents
  src/main/resources/
    db/migration/             # Flyway SQL migrations (V1-V12)
    application.yml           # Spring config

spm-frontend/                 # Frontend (React + TypeScript)
  src/
    components/
      admin/                  # Admin views
      auth/                   # Login, protected routes
      parent/                 # Parent views
      shared/                 # Reusable components
      student/                # Student views
      teacher/                # Teacher views
    services/                 # API client + domain services
    types/                    # TypeScript types

deployment/                   # Deployment guides (EC2, ECS Fargate)
aidlc-docs/                   # Design documentation (requirements, architecture, plans)
```

## Environment Configuration

All integrations are config-driven and swappable via `application.yml` / `.env`:

| Integration | Config Key | Options |
|---|---|---|
| LLM | `app.llm.type` | `stub` (default), `bedrock` |
| OCR | `app.ocr.type` | `stub`, `tesseract` (default), `textract` |
| File Storage | `app.storage.type` | `local` (default), `s3` |
| Report Storage | `app.report.storage-type` | `stub` (default), `s3` |
| Job Dispatch | `app.report.dispatcher-type` | `sync` (default), `sqs` |
| Extraction | `app.extraction.type` | `stub` (default), `bedrock` |

## Deployment

See [deployment/deployment-guide.md](deployment/deployment-guide.md) for:
- EC2 + Docker Compose (simple, low cost)
- ECS Fargate (recommended for production)

## Vendor Lock-in

The system is designed to minimize vendor lock-in:
- Auth: Spring Security OAuth2 Resource Server (any OIDC provider works)
- Storage/OCR/LLM: All behind interfaces with local dev stubs
- Database: JPA/Hibernate (PostgreSQL, but portable)

See `aidlc-docs/aidlc-state.md` for the full vendor lock-in assessment.
