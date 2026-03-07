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
- [ ] Assess expected user load and growth patterns
- [ ] Determine scaling triggers and capacity planning
- [ ] Define horizontal vs vertical scaling strategy

### Phase 2: Performance Requirements
- [ ] Define response time expectations
- [ ] Determine throughput requirements
- [ ] Establish performance benchmarks

### Phase 3: Availability Requirements
- [ ] Define uptime expectations
- [ ] Determine disaster recovery needs
- [ ] Establish failover and backup strategies

### Phase 4: Security Requirements
- [ ] Assess data protection requirements
- [ ] Determine compliance needs
- [ ] Define authentication and authorization requirements
- [ ] Identify threat models and mitigation strategies

### Phase 5: Tech Stack Selection
- [ ] Validate technology choices
- [ ] Identify integration requirements
- [ ] Determine infrastructure needs

### Phase 6: Reliability and Monitoring
- [ ] Define error handling requirements
- [ ] Determine monitoring and alerting needs
- [ ] Establish logging and observability requirements

---

## NFR Assessment Questions

### Scalability Requirements

**Q1: Expected User Load**

What is the expected user load for a typical tuition centre deployment?

**Initial deployment**:
- Number of teachers: [Answer]: 
- Number of parents: [Answer]: 
- Number of students: [Answer]: 
- Number of admins: [Answer]: 

**Growth expectations (3 years)**:
- Expected growth rate per year: [Answer]: 
- Maximum expected users: [Answer]: 

**Concurrent users**:
- Peak concurrent users (% of total): [Answer]: 
- Peak usage times (e.g., evenings, weekends): [Answer]: 

---

**Q2: Data Volume**

What data volumes do you expect?

**Test scores**:
- Average tests per student per month: [Answer]: 
- Average questions per test: [Answer]: 
- Average sub-questions per question: [Answer]: 

**Data retention**:
- How long should test score data be retained? [Answer]: 
- Should old data be archived or deleted? [Answer]: 
- Expected database size after 3 years: [Answer]: 

---

**Q3: Scaling Strategy**

How should the system scale as load increases?

A) Vertical scaling only (increase server resources)
B) Horizontal scaling (add more server instances)
C) Hybrid (vertical first, then horizontal)
D) No scaling needed (fixed capacity)

[Answer]: 

**Scaling triggers**:
- At what CPU/memory utilization should we scale? [Answer]: 
- Should scaling be automatic or manual? [Answer]: 

---

### Performance Requirements

**Q4: API Response Time Requirements**

What are the acceptable response times for API endpoints?

**Critical endpoints** (e.g., login, dashboard):
- Target response time: [Answer]: 
- Maximum acceptable response time: [Answer]: 

**Standard endpoints** (e.g., list students, view scores):
- Target response time: [Answer]: 
- Maximum acceptable response time: [Answer]: 

**Heavy endpoints** (e.g., generate report, bulk operations):
- Target response time: [Answer]: 
- Maximum acceptable response time: [Answer]: 

---

**Q5: Database Performance**

What are the database performance requirements?

**Query performance**:
- Acceptable query response time: [Answer]: 
- Maximum acceptable query time: [Answer]: 

**Write performance**:
- Expected write operations per second: [Answer]: 
- Acceptable write latency: [Answer]: 

**Indexing strategy**:
- Should we optimize for read or write performance? [Answer]: 
- Are complex queries expected (joins, aggregations)? [Answer]: 

---

**Q6: Caching Strategy**

Should we implement caching to improve performance?

A) No caching (always fetch from database)
B) Application-level caching (in-memory cache like Caffeine)
C) Distributed caching (Redis, Memcached)
D) Database query caching only

[Answer]: 

**If caching**:
- What data should be cached? (e.g., subjects, topics, user profiles) [Answer]: 
- Cache expiration strategy: [Answer]: 
- Cache invalidation strategy: [Answer]: 

---

### Availability Requirements

**Q7: Uptime Expectations**

What are the uptime requirements for the system?

A) 99% uptime (7.2 hours downtime per month)
B) 99.5% uptime (3.6 hours downtime per month)
C) 99.9% uptime (43 minutes downtime per month)
D) 99.99% uptime (4 minutes downtime per month)
E) Best effort (no SLA)

[Answer]: 

**Maintenance windows**:
- Are scheduled maintenance windows acceptable? [Answer]: 
- If yes, when and how often? [Answer]: 

---

**Q8: Disaster Recovery**

What are the disaster recovery requirements?

**Recovery Time Objective (RTO)**:
- How quickly must the system be restored after a failure? [Answer]: 

**Recovery Point Objective (RPO)**:
- How much data loss is acceptable? [Answer]: 

**Backup strategy**:
- How often should database backups be taken? [Answer]: 
- How long should backups be retained? [Answer]: 
- Should backups be stored in a different region? [Answer]: 

---

**Q9: High Availability**

Should the system be highly available with failover?

A) Single instance (no failover)
B) Active-passive (standby instance for failover)
C) Active-active (multiple instances with load balancing)
D) Multi-region deployment

[Answer]: 

**If high availability**:
- Acceptable failover time: [Answer]: 
- Should database have read replicas? [Answer]: 

---

### Security Requirements

**Q10: Data Protection**

What data protection measures are required?

**Data at rest**:
- Should database be encrypted at rest? [Answer]: 
- Should S3 reports be encrypted? [Answer]: 

**Data in transit**:
- HTTPS/TLS for all API calls? [Answer]: 
- Database connections encrypted? [Answer]: 

**Sensitive data**:
- Should PII (email, phone) be encrypted in database? [Answer]: 
- Should test scores be encrypted? [Answer]: 

---

**Q11: Compliance Requirements**

Are there any compliance requirements?

**Regulations**:
- GDPR (EU data protection)? [Answer]: 
- PDPA (Singapore data protection)? [Answer]: 
- COPPA (children's privacy)? [Answer]: 
- Other regulations? [Answer]: 

**Data residency**:
- Must data be stored in a specific region/country? [Answer]: 
- Are there cross-border data transfer restrictions? [Answer]: 

---

**Q12: Authentication and Authorization**

What are the authentication and authorization requirements?

**Password policy** (if not using social login):
- Minimum password length: [Answer]: 
- Password complexity requirements: [Answer]: 
- Password expiration: [Answer]: 

**Session management**:
- JWT token expiration time: [Answer]: 
- Refresh token support needed? [Answer]: 
- Maximum concurrent sessions per user: [Answer]: 

**Multi-factor authentication (MFA)**:
- Should MFA be supported? [Answer]: 
- If yes, for which roles? [Answer]: 

---

**Q13: Security Threats**

What security threats should we protect against?

**Common threats**:
- SQL injection protection needed? [Answer]: 
- XSS (Cross-Site Scripting) protection needed? [Answer]: 
- CSRF (Cross-Site Request Forgery) protection needed? [Answer]: 
- Rate limiting to prevent abuse? [Answer]: 

**API security**:
- Should we implement API key authentication (in addition to JWT)? [Answer]: 
- Should we log all API access for audit? [Answer]: 
- Should we implement IP whitelisting? [Answer]: 

---

### Tech Stack Selection

**Q14: Database Technology**

You've chosen PostgreSQL 18. Any specific requirements?

**Database features**:
- Do we need full-text search? [Answer]: 
- Do we need JSON/JSONB support? [Answer]: 
- Do we need database-level encryption? [Answer]: 

**Database hosting**:
- AWS RDS PostgreSQL? [Answer]: 
- Self-managed PostgreSQL on EC2? [Answer]: 
- Other managed service? [Answer]: 

**Database configuration**:
- Expected database instance size (e.g., db.t3.medium): [Answer]: 
- Multi-AZ deployment for high availability? [Answer]: 

---

**Q15: Application Server**

How should the Spring Boot application be deployed?

A) AWS Elastic Beanstalk (managed platform)
B) AWS ECS (containerized with Docker)
C) AWS EKS (Kubernetes)
D) AWS EC2 (self-managed)
E) AWS Lambda (serverless)

[Answer]: 

**If containerized**:
- Docker image registry (ECR, Docker Hub)? [Answer]: 
- Container orchestration preferences? [Answer]: 

**Server sizing**:
- Expected instance type (e.g., t3.medium, m5.large): [Answer]: 
- Number of instances for load balancing: [Answer]: 

---

**Q16: Email and SMS Services**

What services should we use for notifications?

**Email service**:
A) AWS SES (Simple Email Service)
B) SendGrid
C) Mailgun
D) Other

[Answer]: 

**SMS service**:
A) AWS SNS (Simple Notification Service)
B) Twilio
C) Nexmo
D) Other

[Answer]: 

**Email/SMS requirements**:
- Expected email volume per day: [Answer]: 
- Expected SMS volume per day: [Answer]: 
- Need email templates? [Answer]: 

---

**Q17: File Storage**

You've chosen S3 for report storage. Any specific requirements?

**S3 configuration**:
- S3 bucket per centre or shared bucket with prefixes? [Answer]: 
- S3 storage class (Standard, Infrequent Access, Glacier)? [Answer]: 
- S3 lifecycle policy (auto-delete after 2 years)? [Answer]: 

**File access**:
- Pre-signed URL expiration time: [Answer]: 
- Should reports be publicly accessible or private? [Answer]: 

---

**Q18: Build and Deployment**

What build and deployment tools should we use?

**Build tool**:
A) Maven
B) Gradle

[Answer]: 

**CI/CD pipeline**:
A) AWS CodePipeline + CodeBuild + CodeDeploy
B) GitHub Actions
C) GitLab CI/CD
D) Jenkins
E) Other

[Answer]: 

**Deployment strategy**:
A) Blue-green deployment
B) Rolling deployment
C) Canary deployment
D) Simple deployment (downtime acceptable)

[Answer]: 

---

### Reliability and Monitoring

**Q19: Error Handling**

What error handling and resilience patterns should we implement?

**Retry logic**:
- Should we retry failed external API calls (Keycloak, email, SMS)? [Answer]: 
- Maximum retry attempts: [Answer]: 
- Retry backoff strategy (exponential, linear): [Answer]: 

**Circuit breaker**:
- Should we implement circuit breaker for external services? [Answer]: 
- Circuit breaker threshold (failures before opening): [Answer]: 

**Fallback behavior**:
- What should happen if email service fails? [Answer]: 
- What should happen if SMS service fails? [Answer]: 
- What should happen if Keycloak is unavailable? [Answer]: 

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

[Answer]: 

**Metrics to monitor**:
- API response times? [Answer]: 
- Error rates? [Answer]: 
- Database performance? [Answer]: 
- Memory and CPU usage? [Answer]: 
- Custom business metrics (e.g., test scores created per day)? [Answer]: 

**Alerting**:
- Who should receive alerts? [Answer]: 
- Alert channels (email, SMS, Slack, PagerDuty)? [Answer]: 
- Alert thresholds (e.g., error rate > 5%, response time > 2s)? [Answer]: 

---

**Q21: Logging**

What logging strategy should we implement?

**Log levels**:
- Default log level (INFO, DEBUG, WARN): [Answer]: 
- Should we log all API requests? [Answer]: 
- Should we log all database queries? [Answer]: 

**Log aggregation**:
A) AWS CloudWatch Logs
B) ELK Stack (Elasticsearch, Logstash, Kibana)
C) Splunk
D) Datadog Logs
E) Other

[Answer]: 

**Log retention**:
- How long should logs be retained? [Answer]: 
- Should logs be archived for compliance? [Answer]: 

---

**Q22: Testing Requirements**

What testing requirements should we meet?

**Unit testing**:
- Target code coverage: [Answer]: 
- Testing framework (JUnit 5, TestNG): [Answer]: 

**Integration testing**:
- Should we use Testcontainers for database tests? [Answer]: 
- Should we test external integrations (Keycloak, email, SMS)? [Answer]: 

**Performance testing**:
- Should we conduct load testing? [Answer]: 
- If yes, what tools (JMeter, Gatling, k6)? [Answer]: 
- Performance test scenarios: [Answer]: 

**Security testing**:
- Should we conduct security scans (OWASP ZAP, SonarQube)? [Answer]: 
- Penetration testing required? [Answer]: 

---

**Q23: Documentation Requirements**

What documentation should we create?

**API documentation**:
- OpenAPI/Swagger specification? [Answer]: 
- Interactive API documentation (Swagger UI)? [Answer]: 

**Code documentation**:
- Javadoc for all public methods? [Answer]: 
- Architecture decision records (ADRs)? [Answer]: 

**Operational documentation**:
- Deployment runbook? [Answer]: 
- Troubleshooting guide? [Answer]: 
- Monitoring and alerting guide? [Answer]: 

---

**Q24: Operational Requirements**

What operational requirements should we consider?

**Database migrations**:
- Migration tool (Flyway, Liquibase): [Answer]: 
- Migration strategy (automatic on startup, manual): [Answer]: 

**Configuration management**:
- Environment-specific configs (dev, staging, prod): [Answer]: 
- Secrets management (AWS Secrets Manager, Parameter Store, HashiCorp Vault): [Answer]: 

**Health checks**:
- Health check endpoint for load balancer? [Answer]: 
- What should health check verify (database, Keycloak, email service)? [Answer]: 

---

**Q25: Cost Optimization**

What cost optimization strategies should we consider?

**Infrastructure costs**:
- Budget per centre per month: [Answer]: 
- Should we use reserved instances for cost savings? [Answer]: 
- Should we use spot instances for non-critical workloads? [Answer]: 

**Data transfer costs**:
- Expected data transfer volume: [Answer]: 
- Should we use CloudFront CDN for static assets? [Answer]: 

**Storage costs**:
- Expected S3 storage costs: [Answer]: 
- Should we use S3 Intelligent-Tiering? [Answer]: 

---

## Plan Completion Checklist

- [ ] All questions answered by user
- [ ] All ambiguities resolved
- [ ] Scalability requirements defined
- [ ] Performance requirements defined
- [ ] Availability requirements defined
- [ ] Security requirements defined
- [ ] Tech stack decisions finalized
- [ ] Reliability and monitoring requirements defined
- [ ] NFR requirements artifacts generated
- [ ] User approval received

---

**Plan Status**: Awaiting User Input  
**Created**: 2026-03-08  
**Unit**: UNIT-01 (Backend API)
