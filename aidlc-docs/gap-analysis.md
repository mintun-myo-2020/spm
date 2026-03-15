# Gap Analysis: PRFAQ & Requirements vs Current Implementation

**Date**: 2026-03-15
**Compared against**: `PRFAQ/mvp_prfaq.md`, `aidlc-docs/inception/requirements/requirements.md`

---

## Implemented (Working)

| Requirement | Status | Notes |
|---|---|---|
| FR-1.1 Keycloak authentication | ✅ Done | OAuth2/OIDC, JWT, role-based routing |
| FR-1.2 Four user roles | ✅ Done | Teacher, Parent, Student, Admin — all have dashboards |
| FR-1.3 Role-based access control | ✅ Done | @PreAuthorize + StudentAccessService for data isolation |
| FR-3.1 Student profile management | ✅ Done | Create, view, deactivate/reactivate |
| FR-3.2 Students in multiple classes | ✅ Done | ClassStudent join table with enrollment status |
| FR-4.1 Default subjects and topics | ✅ Done | Seed data in V2 migration |
| FR-4.2 Admin custom subjects/topics | ✅ Done | Full CRUD + deactivation |
| FR-4.3 Class management | ✅ Done | Create, enroll, withdraw, re-enroll, change teacher |
| FR-5.1 Test score recording | ✅ Done | Test name, date, overall score, topic-level breakdown, question text, MCQ support |
| FR-5.2 One-student-at-a-time entry | ✅ Done | Single student form |
| FR-6.1 Two-level performance tracking | ✅ Done | Overall scores + topic-level sub-question scores |
| FR-6.2 Historical performance data | ✅ Done | All scores timestamped, topic trends, feedback linked |
| FR-7.1 Teacher feedback | ✅ Done | Free-text (strengths, improvements, recommendations), templates, edit tracking |
| FR-8.1 Progress charts | ✅ Done | Line charts for score trends, bar charts for topic performance |
| FR-8.2 Chart access by role | ✅ Done | All four roles can view appropriate charts |
| FR-10.1 Parent dashboard | ✅ Done | Recent scores, progress charts, feedback, child selector |
| FR-10.2 Parent historical data | ✅ Done | Full trend data and topic drill-down |
| FR-12.1 Data isolation | ✅ Done | Parents see own child only, teachers see own classes only |
| FR-12.2 No peer comparison | ✅ Done | Not implemented (as intended) |
| NFR-1.1 Java + Spring Boot | ✅ Done | |
| NFR-1.2 React frontend | ✅ Done | Flowbite-react UI, react-hook-form, recharts |
| NFR-1.3 PostgreSQL | ✅ Done | Flyway migrations |
| NFR-1.4 Keycloak auth | ✅ Done | |
| NFR-3.2 Basic security | ✅ Done | Keycloak, RBAC, data isolation, rate limiting |
| NFR-7.1 Code best practices | ✅ Done | Modular package structure, DTOs, services, controllers |

---

## Gaps (Missing or Stubbed)

| Requirement | Status | Impact | Details |
|---|---|---|---|
| FR-2.1 Multi-tenancy | ❌ Not implemented | HIGH | No tenant/centre concept in DB schema. No data isolation between centres. Currently single-tenant only. This was a core requirement: "support multiple tuition centres with isolated data". |
| FR-2.2 Centre admin settings | ❌ Not implemented | HIGH | Depends on multi-tenancy. No centre-specific configuration. |
| FR-11.1 Email/SMS notifications | ❌ Stub only | MEDIUM | Notification entity and preferences exist in DB. No actual email or SMS sending service. Notifications stay in PENDING status forever. No JavaMailSender, no SMS provider integration. |
| FR-11.2 Notification preferences | ⚠️ Partial | LOW | Parent can set preferences (email/SMS/both) in UI, but since sending doesn't work, it's decorative. |
| FR-9.1 Progress report content | ⚠️ Stub only | MEDIUM | Report generation endpoint exists. S3 storage works. But generated content is placeholder HTML: `<html><body>Progress Report</body></html>`. No actual score history, charts, feedback summary, or topic breakdown in the report. |
| FR-1.1 Social login (Google/Facebook) | ⚠️ Config needed | LOW | Keycloak is integrated but realm config needs Google/Facebook identity providers added. This is a Keycloak admin config task, not a code change. |
| NFR-2.1 AWS deployment | ❌ Not done | MEDIUM | App runs locally via Docker Compose. No AWS infrastructure (ECS, RDS, etc.) provisioned. |
| NFR-4.1 Mobile responsiveness | ⚠️ Untested | LOW | Flowbite-react provides responsive defaults, but no explicit mobile optimization or testing has been done. |
| NFR-6.2 Audit logs | ❌ Not implemented | LOW | No audit trail for data modifications (e.g., who edited a test score, when). |

---

## Explicitly Deferred (Out of Scope per Requirements)

These are NOT gaps — they were intentionally excluded from the prototype:

- AI-powered weak topic detection
- OCR/photo upload of test papers
- Bulk CSV/Excel upload of scores
- PDF export of reports
- Customizable report templates
- Class scheduling and attendance tracking
- Homework management
- Class participation scoring
- Peer comparison and percentile rankings
- Historical data migration tools
- Native mobile apps

---

## Priority Recommendations

1. **Multi-tenancy** — Biggest gap. Adding a `centre_id` to all major tables and filtering all queries by it is a significant schema + service change. Should be tackled before any real deployment.
2. **Notification sending** — Implement an EmailService (Spring Mail or SES) and wire it into test score creation and feedback creation flows. SMS can use SNS or Twilio.
3. **Report content generation** — Replace the stub HTML with actual report content pulling from ProgressService data. Could generate HTML or PDF with score tables, trend charts, and feedback summaries.
4. **AWS deployment** — Terraform/CDK for RDS, ECS, S3, and Keycloak hosting.
