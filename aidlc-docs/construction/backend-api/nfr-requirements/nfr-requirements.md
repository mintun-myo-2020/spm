# Non-Functional Requirements - Backend API

## Overview
This document defines the non-functional requirements (NFRs) for the Student Progress Tracking System Backend API, covering scalability, performance, availability, security, reliability, and operational requirements.

---

## 1. Scalability Requirements

### 1.1 User Load Capacity

**Initial Deployment**:
- Support 5-10 teachers
- Support 50-100 parents
- Support 50-100 students
- Support 1-2 administrators
- Total: 100-200 users per tuition centre

**Growth Projections**:
- Annual growth rate: 20-30%
- 3-year target: 300-400 total users
- Peak concurrent users: 20-30% of total (60-120 concurrent users)
- Peak usage times: Weekday evenings (6-9 PM), weekend mornings

**Rationale**: Small to medium-sized tuition centres with steady growth patterns.

---

### 1.2 Data Volume Capacity

**Test Score Volume**:
- 4-6 tests per student per month
- 10-15 questions per test
- 2-3 sub-questions per question
- Estimated: 6,000-12,000 test scores per year per centre

**Data Retention**:
- Test scores: Retained indefinitely (permanent academic records)
- Archival: Data older than 5 years moved to cold storage
- Expected database size: 5-10 GB after 3 years

**Rationale**: Academic data requires long-term retention for historical records and compliance.

---

### 1.3 Scaling Strategy

**Approach**: Hybrid scaling (vertical first, then horizontal)

**Vertical Scaling**:
- Initial: 0.5 vCPU, 1 GB RAM (Fargate)
- Scale up to: 1 vCPU, 2 GB RAM as load increases
- Database: Start with db.t3.small, scale to db.t3.medium

**Horizontal Scaling**:
- Minimum: 2 ECS tasks for high availability
- Scale out: Add tasks when CPU > 70% or memory > 80% for 5 minutes
- Maximum: 10 tasks per centre (sufficient for 400 users)

**Auto-Scaling**:
- Automatic scaling with CloudWatch alarms
- Scale-out trigger: CPU > 70% or memory > 80% sustained for 5 minutes
- Scale-in trigger: CPU < 30% and memory < 40% sustained for 10 minutes

**Rationale**: Cost-effective approach that starts small and scales as needed. Horizontal scaling provides redundancy and handles traffic spikes.

---

## 2. Performance Requirements

### 2.1 API Response Time Requirements

**Critical Endpoints** (login, dashboard):
- Target: < 500ms (p95)
- Maximum: < 1000ms (p99)
- Examples: POST /api/v1/auth/login, GET /api/v1/auth/me

**Standard Endpoints** (list operations, view details):
- Target: < 1000ms (p95)
- Maximum: < 2000ms (p99)
- Examples: GET /api/v1/students, GET /api/v1/test-scores/{id}

**Heavy Endpoints** (reports, bulk operations):
- Target: < 5000ms (p95)
- Maximum: < 10000ms (p99)
- Examples: POST /api/v1/students/{id}/reports, POST /api/v1/users/students/bulk

**Rationale**: Response times aligned with user expectations for web applications. Critical paths prioritized for best user experience.

---

### 2.2 Database Performance Requirements

**Query Performance**:
- Simple queries (single table, indexed): < 100ms
- Complex queries (joins, aggregations): < 500ms
- Maximum acceptable query time: < 2000ms

**Write Performance**:
- Expected write operations: 5-10 writes/second during peak
- Acceptable write latency: < 200ms
- Batch operations: < 5000ms for 100 records

**Optimization Strategy**:
- Read-optimized (90% reads, 10% writes)
- Comprehensive indexing on foreign keys and query filters
- Read replica for reporting queries
- Connection pooling (HikariCP): 10-20 connections

**Rationale**: Read-heavy workload typical of educational systems. Most operations are viewing scores and progress, with occasional score entry.

---

### 2.3 Caching Strategy

**Approach**: Application-level caching with Caffeine

**Cached Data**:
- Subjects and topics (relatively static)
- User profiles (Teacher, Parent, Student, Admin)
- Class lists and enrollments
- Feedback templates

**Cache Configuration**:
- Subjects/Topics: TTL 1 hour, max 1000 entries
- User profiles: TTL 15 minutes, max 500 entries
- Class lists: TTL 30 minutes, max 200 entries
- Feedback templates: TTL 1 hour, max 100 entries

**Cache Invalidation**:
- Event-driven invalidation on updates (Spring Events)
- TTL-based expiration as fallback
- Manual cache clear endpoint for admins

**Rationale**: Application-level caching is simpler and sufficient for single-tenant deployments. Avoids complexity of distributed caching (Redis) for MVP.

---

## 3. Availability Requirements

### 3.1 Uptime and SLA

**Target Uptime**: 99.5% (3.6 hours downtime per month)

**Maintenance Windows**:
- Scheduled maintenance: Monthly, 2-hour window
- Timing: Sunday 2-4 AM local time (Singapore)
- Notification: 48 hours advance notice to users

**Downtime Budget**:
- Planned maintenance: 2 hours/month
- Unplanned outages: 1.6 hours/month maximum

**Rationale**: 99.5% uptime balances cost and reliability for educational systems. Maintenance windows during off-peak hours minimize user impact.

---

### 3.2 Disaster Recovery

**Recovery Time Objective (RTO)**: 1-2 hours
- Time to restore service after catastrophic failure

**Recovery Point Objective (RPO)**: Maximum 15 minutes data loss
- Acceptable data loss window

**Backup Strategy**:
- Automated daily snapshots (RDS automated backups)
- Continuous transaction log backups (point-in-time recovery)
- Retention: 30 days for daily backups, 7 days for transaction logs
- Cross-region backup: Yes, replicate to secondary region

**Disaster Recovery Plan**:
1. Detect failure via monitoring alerts
2. Assess impact and determine recovery path
3. Restore from latest backup or failover to standby
4. Verify data integrity and system functionality
5. Resume normal operations
6. Post-mortem and corrective actions

**Rationale**: 1-2 hour RTO acceptable for educational systems (not life-critical). 15-minute RPO ensures minimal data loss.

---

### 3.3 High Availability

**Approach**: Active-passive with automatic failover

**Application Layer**:
- Minimum 2 ECS tasks across multiple availability zones
- Application Load Balancer with health checks
- Automatic task replacement on failure

**Database Layer**:
- RDS Multi-AZ deployment (automatic failover)
- Synchronous replication to standby instance
- Automatic failover time: 1-2 minutes
- Read replica for reporting queries (asynchronous replication)

**Failover Behavior**:
- Application: ALB routes traffic to healthy tasks (< 30 seconds)
- Database: RDS automatic failover (1-2 minutes)
- Total failover time: 5-10 minutes

**Rationale**: Active-passive provides good balance of availability and cost. Multi-AZ RDS ensures database availability without manual intervention.

---

## 4. Security Requirements

### 4.1 Data Protection

**Data at Rest**:
- Database: AES-256 encryption (RDS encryption)
- S3 reports: Server-side encryption (SSE-S3)
- Secrets: AWS Secrets Manager with automatic rotation

**Data in Transit**:
- API: HTTPS/TLS 1.2+ required (no HTTP)
- Database: SSL/TLS connections enforced
- Internal services: VPC private subnets

**Sensitive Data Handling**:
- PII (email, phone): Database-level encryption sufficient for MVP
- Test scores: Database-level encryption sufficient
- Passwords: Managed by Keycloak (bcrypt hashing)

**Rationale**: Encryption at rest and in transit meets PDPA requirements. Database-level encryption simpler than field-level for MVP.

---

### 4.2 Compliance Requirements

**Regulations**:
- PDPA (Singapore Personal Data Protection Act): Yes
- COPPA (Children's Online Privacy Protection): Yes (students are minors)
- GDPR: No (Singapore-based deployment)

**Data Residency**:
- Region: Singapore (ap-southeast-1)
- No cross-border data transfers
- All data stored and processed in Singapore

**Compliance Measures**:
- Data access logging and audit trails
- User consent for data collection
- Data retention and deletion policies
- Privacy policy and terms of service
- Parental consent for student data

**Rationale**: PDPA compliance mandatory for Singapore operations. COPPA considerations for student privacy.

---

### 4.3 Authentication and Authorization

**Password Policy** (Keycloak-managed):
- Minimum length: 12 characters
- Complexity: At least one uppercase, lowercase, number, special character
- No password expiration (modern security practice)
- Password history: Prevent reuse of last 5 passwords

**Session Management**:
- Access token: 1-hour expiration (JWT)
- Refresh token: 7-day expiration
- Maximum concurrent sessions: 3 per user
- Automatic logout on token expiration

**Multi-Factor Authentication (MFA)**:
- Support: Yes, optional for MVP
- Admin role: MFA required
- Teacher role: MFA optional
- Parent/Student: MFA optional

**Rationale**: Strong password policy and MFA for admins balances security and usability. Short-lived access tokens limit exposure.

---

### 4.4 Security Threat Mitigation

**Common Threats**:
- SQL Injection: Parameterized queries (JPA/Hibernate)
- XSS: Spring Security XSS protection, content security policy
- CSRF: CSRF tokens for state-changing operations
- Rate Limiting: 100 requests per minute per user (Spring Cloud Gateway or Bucket4j)

**API Security**:
- Authentication: JWT Bearer tokens required
- Authorization: Role-based access control (RBAC)
- API logging: All authenticated requests logged with user ID
- IP whitelisting: Not implemented for MVP

**Security Headers**:
- Strict-Transport-Security (HSTS)
- X-Content-Type-Options: nosniff
- X-Frame-Options: DENY
- Content-Security-Policy

**Rationale**: Standard security practices for web applications. Rate limiting prevents abuse and DoS attacks.

---

## 5. Reliability Requirements

### 5.1 Error Handling and Resilience

**Retry Logic**:
- External API calls: 3 retry attempts with exponential backoff (1s, 2s, 4s)
- Applies to: Keycloak, SES (email), SNS (SMS)
- Circuit breaker: Resilience4j with 5 failures in 10 seconds threshold

**Fallback Behavior**:
- Email service failure: Log error, queue for retry, continue operation
- SMS service failure: Log error, queue for retry, continue operation
- Keycloak unavailable: Return 503 Service Unavailable (cannot proceed)

**Timeout Configuration**:
- External API calls: 10-second timeout
- Database queries: 30-second timeout
- Report generation: 60-second timeout

**Rationale**: Resilience patterns ensure system continues operating despite external service failures. Notifications are non-blocking.

---

### 5.2 Monitoring and Observability

**Application Monitoring**: AWS CloudWatch + Application Insights

**Metrics Collected**:
- API response times: p50, p95, p99 latencies per endpoint
- Error rates: 4xx and 5xx error counts and percentages
- Database performance: Connection pool usage, query times, slow queries
- Resource usage: CPU, memory, disk, network per container
- Business metrics: Test scores created, user logins, notifications sent

**Alerting Configuration**:
- Recipients: DevOps team, on-call engineer
- Channels: Email + Slack for warnings, PagerDuty for critical
- Thresholds:
  - Error rate > 5% (warning), > 10% (critical)
  - p95 latency > 2s (warning), > 5s (critical)
  - CPU > 80% (warning), > 90% (critical)
  - Memory > 85% (warning), > 95% (critical)
  - Database connections > 80% pool (warning)

**Rationale**: Comprehensive monitoring enables proactive issue detection and rapid incident response.

---

### 5.3 Logging Strategy

**Log Levels**:
- Production: INFO level
- Development: DEBUG level
- Staging: INFO level

**Log Content**:
- All API requests: Request ID, user ID, endpoint, method, response time, status code
- Errors: Stack traces, context, user ID, request ID
- Security events: Login attempts, authorization failures, suspicious activity
- Slow queries: Database queries > 1 second

**Log Aggregation**: AWS CloudWatch Logs

**Log Retention**:
- CloudWatch: 30 days (searchable)
- S3 archive: 90 days (compliance)
- Long-term archive: 1 year in S3 Glacier

**Structured Logging**:
- Format: JSON with consistent fields
- Correlation: Request ID for tracing across services
- Sensitive data: Mask PII in logs

**Rationale**: Structured logging enables efficient troubleshooting and compliance auditing.

---

## 6. Testing Requirements

### 6.1 Unit Testing

**Coverage Targets**:
- Line coverage: 80% minimum
- Branch coverage: 70% minimum
- Critical paths: 90% coverage

**Framework**: JUnit 5 with Mockito

**Scope**:
- All service layer methods
- All business logic and validation
- All utility functions
- Mock external dependencies

---

### 6.2 Integration Testing

**Approach**: Testcontainers for PostgreSQL

**Scope**:
- Repository layer with real database
- API endpoints (MockMvc)
- External service integrations (mocked)

**Test Data**:
- Test fixtures for common scenarios
- Database seeding for integration tests
- Cleanup after each test

---

### 6.3 Performance Testing

**Tool**: Gatling

**Scenarios**:
- 100 concurrent users
- 1000 requests per minute sustained for 10 minutes
- Ramp-up: 0 to 100 users over 2 minutes
- Steady state: 100 users for 10 minutes
- Ramp-down: 100 to 0 users over 2 minutes

**Success Criteria**:
- p95 latency < 2s for all endpoints
- Error rate < 1%
- No memory leaks or resource exhaustion

---

### 6.4 Security Testing

**Static Analysis**: SonarQube
- Code quality and security vulnerabilities
- OWASP Top 10 checks
- Dependency vulnerability scanning (OWASP Dependency-Check)

**Penetration Testing**:
- Annual penetration testing by third party
- Scope: API security, authentication, authorization, data protection

---

## 7. Operational Requirements

### 7.1 Database Migrations

**Tool**: Flyway

**Strategy**:
- Development/Staging: Automatic on startup
- Production: Manual approval required
- Versioning: Sequential version numbers (V1__initial_schema.sql)
- Rollback: Manual rollback scripts for critical migrations

---

### 7.2 Configuration Management

**Environment Profiles**: Spring profiles (dev, staging, prod)

**Secrets Management**: AWS Secrets Manager
- Database credentials
- Keycloak client secrets
- AWS service credentials (SES, SNS)
- JWT signing keys

**Configuration Sources**:
- application.yml: Non-sensitive defaults
- application-{profile}.yml: Environment-specific overrides
- AWS Secrets Manager: Sensitive credentials
- Environment variables: Deployment-specific settings

---

### 7.3 Health Checks

**Endpoint**: /actuator/health

**Checks**:
- Database connectivity (required)
- Disk space (required)
- Keycloak availability (optional, degraded if unavailable)

**Load Balancer Configuration**:
- Health check interval: 30 seconds
- Healthy threshold: 2 consecutive successes
- Unhealthy threshold: 3 consecutive failures
- Timeout: 5 seconds

---

## 8. Documentation Requirements

### 8.1 API Documentation

- OpenAPI 3.0 specification (auto-generated from code)
- Swagger UI for interactive documentation (dev/staging only)
- API versioning documented
- Authentication and authorization requirements per endpoint

### 8.2 Code Documentation

- Javadoc for all public APIs
- Complex business logic documented inline
- Architecture Decision Records (ADRs) for major decisions

### 8.3 Operational Documentation

- Deployment runbook with step-by-step instructions
- Troubleshooting guide for common issues
- Monitoring and alerting guide with dashboard setup
- Incident response procedures

---

## 9. Cost Optimization

### 9.1 Infrastructure Budget

**Target**: $200-400 USD per month per centre

**Cost Breakdown**:
- RDS PostgreSQL (db.t3.small Multi-AZ): $60-80/month
- ECS Fargate (2 tasks, 0.5 vCPU, 1 GB): $30-40/month
- Application Load Balancer: $20-25/month
- S3 storage (reports): $5-10/month
- SES (email): $1-5/month
- SNS (SMS): $10-20/month
- CloudWatch (logs, metrics): $10-20/month
- Data transfer: $10-20/month
- Secrets Manager: $1-2/month
- Backup storage: $5-10/month

**Cost Optimization Strategies**:
- Reserved instances for RDS after 3 months (30% savings)
- S3 lifecycle policies (Standard → IA → Delete)
- CloudWatch log retention policies
- Right-sizing based on actual usage

---

## Summary

These non-functional requirements define:
- Scalability for 100-400 users with hybrid scaling
- Performance targets (< 1s for standard operations)
- 99.5% uptime with 1-2 hour RTO
- Comprehensive security (encryption, PDPA compliance, MFA)
- Resilience patterns (retry, circuit breaker, fallback)
- Monitoring and logging for observability
- 80% test coverage with performance and security testing
- Operational excellence (Flyway, Secrets Manager, health checks)
- Cost-effective infrastructure ($200-400/month per centre)

---

**Document Version**: 1.0  
**Created**: 2026-03-08  
**Status**: Draft
