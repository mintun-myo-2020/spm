# NFR Requirements Plan - Backend API (UNIT-01)

## Overview
This plan outlines the non-functional requirements assessment for the Backend API unit, focusing on scalability, performance, availability, security, and tech stack selection.

## Unit Context
- **Unit ID**: UNIT-01
- **Unit Name**: Backend API
- **Technology**: Java 25, Spring Boot 4.x, Spring Security 6, PostgreSQL 18
- **Architecture**: Modular monolith with feature-based modules
- **Deployment**: Single-tenant per tuition centre (separate AWS account per centre)

## NFR Assessment Phases

### Phase 1: Scalability Requirements
- [x] Assess expected user load and growth patterns
- [x] Determine scaling triggers and capacity planning
- [x] Define horizontal vs vertical scaling strategy

### Phase 2: Performance Requirements
- [x] Define response time expectations
- [x] Determine throughput requirements
- [x] Establish performance benchmarks

### Phase 3: Availability Requirements
- [x] Define uptime expectations
- [x] Determine disaster recovery needs
- [x] Establish failover and backup strategies

### Phase 4: Security Requirements
- [x] Assess data protection requirements
- [x] Determine compliance needs
- [x] Define authentication and authorization requirements
- [x] Identify threat models and mitigation strategies

### Phase 5: Tech Stack Selection
- [x] Validate technology choices
- [x] Identify integration requirements
- [x] Determine infrastructure needs

### Phase 6: Reliability and Monitoring
- [x] Define error handling requirements
- [x] Determine monitoring and alerting needs
- [x] Establish logging and observability requirements

---

## NFR Assessment Questions

### Scalability Requirements

**Q1: Expected User Load**

What is the expected user load for a typical tuition centre deployment?

**Initial deployment**:
- Number of teachers: [Answer]: 5-10 teachers
- Number of parents: [Answer]: 50-100 parents
- Number of students: [Answer]: 50-100 students
- Number of admins: [Answer]: 1-2 admins

**Growth expectations (3 years)**:
- Expected growth rate per year: [Answer]: 20-30% annual growth
- Maximum expected users: [Answer]: 300-400 total users

**Concurrent users**:
- Peak concurrent users (% of total): [Answer]: 20-30% (60-120 concurrent users)
- Peak usage times (e.g., evenings, weekends): [Answer]: Weekday evenings (6-9 PM), weekend mornings 

---

**Q2: Data Volume**

What data volumes do you expect?

**Test scores**:
- Average tests per student per month: [Answer]: 4-6 tests per month
- Average questions per test: [Answer]: 10-15 questions
- Average sub-questions per question: [Answer]: 2-3 sub-questions

**Data retention**:
- How long should test score data be retained? [Answer]: Indefinitely (permanent academic records)
- Should old data be archived or deleted? [Answer]: Archived after 5 years to cold storage
- Expected database size after 3 years: [Answer]: 5-10 GB 

---

**Q3: Scaling Strategy**

How should the system scale as load increases?

A) Vertical scaling only (increase server resources)
B) Horizontal scaling (add more server instances)
C) Hybrid (vertical first, then horizontal)
D) No scaling needed (fixed capacity)

[Answer]: C (Hybrid - vertical first, then horizontal)

**Scaling triggers**:
- At what CPU/memory utilization should we scale? [Answer]: 70% CPU or 80% memory sustained for 5 minutes
- Should scaling be automatic or manual? [Answer]: Automatic with CloudWatch alarms 

---

### Performance Requirements

**Q4: API Response Time Requirements**

What are the acceptable response times for API endpoints?

**Critical endpoints** (e.g., login, dashboard):
- Target response time: [Answer]: < 500ms (p95)
- Maximum acceptable response time: [Answer]: < 1000ms (p99)

**Standard endpoints** (e.g., list students, view scores):
- Target response time: [Answer]: < 1000ms (p95)
- Maximum acceptable response time: [Answer]: < 2000ms (p99)

**Heavy endpoints** (e.g., generate report, bulk operations):
- Target response time: [Answer]: < 5000ms (p95)
- Maximum acceptable response time: [Answer]: < 10000ms (p99) 

---

**Q5: Database Performance**

What are the database performance requirements?

**Query performance**:
- Acceptable query response time: [Answer]: < 100ms for simple queries, < 500ms for complex queries
- Maximum acceptable query time: [Answer]: < 2000ms

**Write performance**:
- Expected write operations per second: [Answer]: 5-10 writes/second during peak
- Acceptable write latency: [Answer]: < 200ms

**Indexing strategy**:
- Should we optimize for read or write performance? [Answer]: Read-optimized (90% reads, 10% writes)
- Are complex queries expected (joins, aggregations)? [Answer]: Yes, for progress calculations and reporting 

---

**Q6: Caching Strategy**

Should we implement caching to improve performance?

A) No caching (always fetch from database)
B) Application-level caching (in-memory cache like Caffeine)
C) Distributed caching (Redis, Memcached)
D) Database query caching only

[Answer]: B (Application-level caching with Caffeine)

**If caching**:
- What data should be cached? (e.g., subjects, topics, user profiles) [Answer]: Subjects, topics, user profiles, class lists (relatively static data)
- Cache expiration strategy: [Answer]: TTL-based: 1 hour for subjects/topics, 15 minutes for user profiles
- Cache invalidation strategy: [Answer]: Event-driven invalidation on updates + TTL expiration 

---

### Availability Requirements

**Q7: Uptime Expectations**

What are the uptime requirements for the system?

A) 99% uptime (7.2 hours downtime per month)
B) 99.5% uptime (3.6 hours downtime per month)
C) 99.9% uptime (43 minutes downtime per month)
D) 99.99% uptime (4 minutes downtime per month)
E) Best effort (no SLA)

[Answer]: B (99.5% uptime - 3.6 hours downtime per month)

**Maintenance windows**:
- Are scheduled maintenance windows acceptable? [Answer]: Yes, during off-peak hours (2-4 AM local time)
- If yes, when and how often? [Answer]: Monthly, 2-hour window on Sunday 2-4 AM 

---

**Q8: Disaster Recovery**

What are the disaster recovery requirements?

**Recovery Time Objective (RTO)**:
- How quickly must the system be restored after a failure? [Answer]: 1-2 hours

**Recovery Point Objective (RPO)**:
- How much data loss is acceptable? [Answer]: Maximum 15 minutes of data loss

**Backup strategy**:
- How often should database backups be taken? [Answer]: Automated daily backups + continuous transaction log backups
- How long should backups be retained? [Answer]: 30 days for daily backups, 7 days for transaction logs
- Should backups be stored in a different region? [Answer]: Yes, cross-region backup for disaster recovery 

---

**Q9: High Availability**

Should the system be highly available with failover?

A) Single instance (no failover)
B) Active-passive (standby instance for failover)
C) Active-active (multiple instances with load balancing)
D) Multi-region deployment

[Answer]: B (Active-passive with standby instance for failover)

**If high availability**:
- Acceptable failover time: [Answer]: 5-10 minutes automatic failover
- Should database have read replicas? [Answer]: Yes, one read replica for reporting queries 

---

### Security Requirements

**Q10: Data Protection**

What data protection measures are required?

**Data at rest**:
- Should database be encrypted at rest? [Answer]: Yes, AES-256 encryption
- Should S3 reports be encrypted? [Answer]: Yes, S3 server-side encryption (SSE-S3)

**Data in transit**:
- HTTPS/TLS for all API calls? [Answer]: Yes, TLS 1.2+ required
- Database connections encrypted? [Answer]: Yes, SSL/TLS for database connections

**Sensitive data**:
- Should PII (email, phone) be encrypted in database? [Answer]: No, database-level encryption sufficient for MVP
- Should test scores be encrypted? [Answer]: No, database-level encryption sufficient 

---

**Q11: Compliance Requirements**

Are there any compliance requirements?

**Regulations**:
- GDPR (EU data protection)? [Answer]: No, Singapore-based deployment
- PDPA (Singapore data protection)? [Answer]: Yes, comply with PDPA requirements
- COPPA (children's privacy)? [Answer]: Yes, students are minors
- Other regulations? [Answer]: None initially

**Data residency**:
- Must data be stored in a specific region/country? [Answer]: Yes, Singapore region (ap-southeast-1)
- Are there cross-border data transfer restrictions? [Answer]: No cross-border transfers needed 

---

**Q12: Authentication and Authorization**

What are the authentication and authorization requirements?

**Password policy** (if not using social login):
- Minimum password length: [Answer]: 12 characters
- Password complexity requirements: [Answer]: At least one uppercase, lowercase, number, special character
- Password expiration: [Answer]: No expiration (modern security practice)

**Session management**:
- JWT token expiration time: [Answer]: 1 hour for access token
- Refresh token support needed? [Answer]: Yes, 7-day refresh token
- Maximum concurrent sessions per user: [Answer]: 3 concurrent sessions

**Multi-factor authentication (MFA)**:
- Should MFA be supported? [Answer]: Yes, optional for MVP
- If yes, for which roles? [Answer]: Admin role required, optional for teachers 

---

**Q13: Security Threats**

What security threats should we protect against?

**Common threats**:
- SQL injection protection needed? [Answer]: Yes, use parameterized queries (JPA/Hibernate)
- XSS (Cross-Site Scripting) protection needed? [Answer]: Yes, Spring Security XSS protection
- CSRF (Cross-Site Request Forgery) protection needed? [Answer]: Yes, CSRF tokens for state-changing operations
- Rate limiting to prevent abuse? [Answer]: Yes, 100 requests per minute per user

**API security**:
- Should we implement API key authentication (in addition to JWT)? [Answer]: No, JWT sufficient for MVP
- Should we log all API access for audit? [Answer]: Yes, log all authenticated requests
- Should we implement IP whitelisting? [Answer]: No, not needed for MVP 

---

### Tech Stack Selection

**Q14: Database Technology**

You've chosen PostgreSQL 18. Any specific requirements?

**Database features**:
- Do we need full-text search? [Answer]: No, not for MVP (simple LIKE queries sufficient)
- Do we need JSON/JSONB support? [Answer]: No, normalized relational model
- Do we need database-level encryption? [Answer]: Yes, RDS encryption at rest

**Database hosting**:
- AWS RDS PostgreSQL? [Answer]: Yes, RDS PostgreSQL
- Self-managed PostgreSQL on EC2? [Answer]: No
- Other managed service? [Answer]: No

**Database configuration**:
- Expected database instance size (e.g., db.t3.medium): [Answer]: db.t3.small for MVP, scale to db.t3.medium as needed
- Multi-AZ deployment for high availability? [Answer]: Yes, Multi-AZ for production 

---

**Q15: Application Server**

How should the Spring Boot application be deployed?

A) AWS Elastic Beanstalk (managed platform)
B) AWS ECS (containerized with Docker)
C) AWS EKS (Kubernetes)
D) AWS EC2 (self-managed)
E) AWS Lambda (serverless)

[Answer]: B (AWS ECS with Docker containers)

**If containerized**:
- Docker image registry (ECR, Docker Hub)? [Answer]: AWS ECR (Elastic Container Registry)
- Container orchestration preferences? [Answer]: ECS with Fargate for serverless containers

**Server sizing**:
- Expected instance type (e.g., t3.medium, m5.large): [Answer]: Fargate: 0.5 vCPU, 1 GB RAM initially, scale to 1 vCPU, 2 GB RAM
- Number of instances for load balancing: [Answer]: 2 tasks minimum for high availability 

---

**Q16: Email and SMS Services**

What services should we use for notifications?

**Email service**:
A) AWS SES (Simple Email Service)
B) SendGrid
C) Mailgun
D) Other

[Answer]: A (AWS SES)

**SMS service**:
A) AWS SNS (Simple Notification Service)
B) Twilio
C) Nexmo
D) Other

[Answer]: A (AWS SNS)

**Email/SMS requirements**:
- Expected email volume per day: [Answer]: 50-200 emails per day
- Expected SMS volume per day: [Answer]: 20-100 SMS per day
- Need email templates? [Answer]: Yes, HTML email templates for notifications 

---

**Q17: File Storage**

You've chosen S3 for report storage. Any specific requirements?

**S3 configuration**:
- S3 bucket per centre or shared bucket with prefixes? [Answer]: Separate bucket per centre (single-tenant deployment)
- S3 storage class (Standard, Infrequent Access, Glacier)? [Answer]: Standard for first year, lifecycle to Infrequent Access after 1 year
- S3 lifecycle policy (auto-delete after 2 years)? [Answer]: Yes, auto-delete after 2 years

**File access**:
- Pre-signed URL expiration time: [Answer]: 1 hour
- Should reports be publicly accessible or private? [Answer]: Private, pre-signed URLs only 

---

**Q18: Build and Deployment**

What build and deployment tools should we use?

**Build tool**:
A) Maven
B) Gradle

[Answer]: B (Gradle with Kotlin DSL)

**CI/CD pipeline**:
A) AWS CodePipeline + CodeBuild + CodeDeploy
B) GitHub Actions
C) GitLab CI/CD
D) Jenkins
E) Other

[Answer]: B (GitHub Actions)

**Deployment strategy**:
A) Blue-green deployment
B) Rolling deployment
C) Canary deployment
D) Simple deployment (downtime acceptable)

[Answer]: B (Rolling deployment with health checks) 

---

### Reliability and Monitoring

**Q19: Error Handling**

What error handling and resilience patterns should we implement?

**Retry logic**:
- Should we retry failed external API calls (Keycloak, email, SMS)? [Answer]: Yes, with exponential backoff
- Maximum retry attempts: [Answer]: 3 attempts
- Retry backoff strategy (exponential, linear): [Answer]: Exponential backoff (1s, 2s, 4s)

**Circuit breaker**:
- Should we implement circuit breaker for external services? [Answer]: Yes, using Resilience4j
- Circuit breaker threshold (failures before opening): [Answer]: 5 failures in 10 seconds

**Fallback behavior**:
- What should happen if email service fails? [Answer]: Log error, queue for retry, continue operation
- What should happen if SMS service fails? [Answer]: Log error, queue for retry, continue operation
- What should happen if Keycloak is unavailable? [Answer]: Return 503 Service Unavailable, cannot proceed without auth 

---

**Q20: Monitoring and Alerting**

What monitoring and alerting should we implement?

**Application monitoring**:
A) AWS CloudWatch (basic metrics)
B) AWS CloudWatch + Application Insights (detailed APM)
C) Datadog
D) New Relic
E) Prometheus + Grafana
F) Other

[Answer]: B (AWS CloudWatch + Application Insights)

**Metrics to monitor**:
- API response times? [Answer]: Yes, p50, p95, p99 latencies
- Error rates? [Answer]: Yes, 4xx and 5xx error rates
- Database performance? [Answer]: Yes, connection pool, query times, slow queries
- Memory and CPU usage? [Answer]: Yes, container/instance metrics
- Custom business metrics (e.g., test scores created per day)? [Answer]: Yes, test scores, user logins, notifications sent

**Alerting**:
- Who should receive alerts? [Answer]: DevOps team, on-call engineer
- Alert channels (email, SMS, Slack, PagerDuty)? [Answer]: Email + Slack for warnings, PagerDuty for critical
- Alert thresholds (e.g., error rate > 5%, response time > 2s)? [Answer]: Error rate > 5%, p95 latency > 2s, CPU > 80%, memory > 85% 

---

**Q21: Logging**

What logging strategy should we implement?

**Log levels**:
- Default log level (INFO, DEBUG, WARN): [Answer]: INFO for production, DEBUG for development
- Should we log all API requests? [Answer]: Yes, with request ID, user ID, endpoint, response time
- Should we log all database queries? [Answer]: No, only slow queries (> 1s)

**Log aggregation**:
A) AWS CloudWatch Logs
B) ELK Stack (Elasticsearch, Logstash, Kibana)
C) Splunk
D) Datadog Logs
E) Other

[Answer]: A (AWS CloudWatch Logs)

**Log retention**:
- How long should logs be retained? [Answer]: 30 days in CloudWatch, 90 days in S3
- Should logs be archived for compliance? [Answer]: Yes, archive to S3 for 1 year 

---

**Q22: Testing Requirements**

What testing requirements should we meet?

**Unit testing**:
- Target code coverage: [Answer]: 80% line coverage, 70% branch coverage
- Testing framework (JUnit 5, TestNG): [Answer]: JUnit 5 with Mockito

**Integration testing**:
- Should we use Testcontainers for database tests? [Answer]: Yes, Testcontainers for PostgreSQL
- Should we test external integrations (Keycloak, email, SMS)? [Answer]: Yes, with mocks/stubs for external services

**Performance testing**:
- Should we conduct load testing? [Answer]: Yes, before production release
- If yes, what tools (JMeter, Gatling, k6)? [Answer]: Gatling for load testing
- Performance test scenarios: [Answer]: 100 concurrent users, 1000 requests/minute sustained for 10 minutes

**Security testing**:
- Should we conduct security scans (OWASP ZAP, SonarQube)? [Answer]: Yes, SonarQube for static analysis, OWASP dependency check
- Penetration testing required? [Answer]: Yes, annual penetration testing 

---

**Q23: Documentation Requirements**

What documentation should we create?

**API documentation**:
- OpenAPI/Swagger specification? [Answer]: Yes, OpenAPI 3.0 specification
- Interactive API documentation (Swagger UI)? [Answer]: Yes, Swagger UI for development/staging

**Code documentation**:
- Javadoc for all public methods? [Answer]: Yes, Javadoc for public APIs and complex logic
- Architecture decision records (ADRs)? [Answer]: Yes, document major architectural decisions

**Operational documentation**:
- Deployment runbook? [Answer]: Yes, step-by-step deployment guide
- Troubleshooting guide? [Answer]: Yes, common issues and resolutions
- Monitoring and alerting guide? [Answer]: Yes, dashboard setup and alert response procedures 

---

**Q24: Operational Requirements**

What operational requirements should we consider?

**Database migrations**:
- Migration tool (Flyway, Liquibase): [Answer]: Flyway for database migrations
- Migration strategy (automatic on startup, manual): [Answer]: Automatic on startup for dev/staging, manual approval for production

**Configuration management**:
- Environment-specific configs (dev, staging, prod): [Answer]: Yes, Spring profiles (dev, staging, prod)
- Secrets management (AWS Secrets Manager, Parameter Store, HashiCorp Vault): [Answer]: AWS Secrets Manager for database credentials, API keys

**Health checks**:
- Health check endpoint for load balancer? [Answer]: Yes, /actuator/health endpoint
- What should health check verify (database, Keycloak, email service)? [Answer]: Database connectivity, Keycloak availability (optional), disk space 

---

**Q25: Cost Optimization**

What cost optimization strategies should we consider?

**Infrastructure costs**:
- Budget per centre per month: [Answer]: $200-400 USD per month (RDS, ECS, S3, SES, SNS, CloudWatch)
- Should we use reserved instances for cost savings? [Answer]: Yes, 1-year reserved instances for RDS after 3 months
- Should we use spot instances for non-critical workloads? [Answer]: No, not for MVP (all workloads are critical)

**Data transfer costs**:
- Expected data transfer volume: [Answer]: 10-20 GB per month
- Should we use CloudFront CDN for static assets? [Answer]: No, frontend handles static assets

**Storage costs**:
- Expected S3 storage costs: [Answer]: $5-10 per month (reports storage)
- Should we use S3 Intelligent-Tiering? [Answer]: No, use lifecycle policies (Standard → IA → Delete) 

---

## Plan Completion Checklist

- [x] All questions answered by user
- [x] All ambiguities resolved
- [x] Scalability requirements defined
- [x] Performance requirements defined
- [x] Availability requirements defined
- [x] Security requirements defined
- [x] Tech stack decisions finalized
- [x] Reliability and monitoring requirements defined
- [x] NFR requirements artifacts generated
- [ ] User approval received

---

**Plan Status**: Awaiting User Approval  
**Created**: 2026-03-08  
**Unit**: UNIT-01 (Backend API)
