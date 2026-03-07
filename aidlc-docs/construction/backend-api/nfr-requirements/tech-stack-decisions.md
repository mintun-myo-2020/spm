# Tech Stack Decisions - Backend API

## Overview
This document details all technology stack decisions for the Student Progress Tracking System Backend API, including rationale, alternatives considered, and implementation guidelines.

---

## 1. Core Application Stack

### 1.1 Programming Language

**Decision**: Java 25

**Rationale**:
- Latest LTS features and performance improvements
- Strong ecosystem for enterprise applications
- Excellent Spring Boot support
- Type safety and compile-time error detection
- Large talent pool and community support

**Alternatives Considered**:
- Java 21 LTS: More conservative, but Java 25 offers latest features
- Kotlin: Considered, but team familiarity with Java prioritized

---

### 1.2 Application Framework

**Decision**: Spring Boot 4.x

**Rationale**:
- Industry-standard for Java microservices and APIs
- Comprehensive ecosystem (Security, Data, Web, Actuator)
- Production-ready features (health checks, metrics, externalized config)
- Excellent OAuth2/OIDC support via Spring Security 6
- Auto-configuration reduces boilerplate
- Strong community and documentation

**Key Dependencies**:
- spring-boot-starter-web: REST API support
- spring-boot-starter-data-jpa: Database access
- spring-boot-starter-security: Security framework
- spring-boot-starter-oauth2-resource-server: JWT validation
- spring-boot-starter-oauth2-client: OAuth2 client
- spring-boot-starter-actuator: Monitoring and health checks
- spring-boot-starter-validation: Bean validation

**Alternatives Considered**:
- Quarkus: Faster startup, but less mature ecosystem
- Micronaut: Good performance, but smaller community

---

### 1.3 Build Tool

**Decision**: Gradle with Kotlin DSL

**Rationale**:
- Faster builds than Maven (incremental compilation, build cache)
- More flexible and expressive than Maven XML
- Kotlin DSL provides type-safe build scripts
- Better dependency management
- Excellent IDE support

**Configuration**:
```kotlin
plugins {
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.spring") version "1.9.0"
    kotlin("plugin.jpa") version "1.9.0"
}
```

**Alternatives Considered**:
- Maven: More widely used, but slower and more verbose

---

## 2. Database Stack

### 2.1 Database

**Decision**: PostgreSQL 18

**Rationale**:
- Open-source with no licensing costs
- ACID compliance for data integrity
- Excellent performance for read-heavy workloads
- Rich data types and indexing options
- Strong JSON support (future extensibility)
- Mature and stable

**Hosting**: AWS RDS PostgreSQL

**Configuration**:
- Instance: db.t3.small (2 vCPU, 2 GB RAM) initially
- Storage: 100 GB General Purpose SSD (gp3)
- Multi-AZ: Yes (automatic failover)
- Automated backups: Daily with 30-day retention
- Read replica: 1 replica for reporting queries
- Encryption: AES-256 at rest
- SSL/TLS: Required for connections

**Alternatives Considered**:
- MySQL: Considered, but PostgreSQL has better JSON support and advanced features
- Amazon Aurora PostgreSQL: More expensive, overkill for MVP

---

### 2.2 Database Access Layer

**Decision**: Spring Data JPA with Hibernate

**Rationale**:
- Abstraction over JDBC reduces boilerplate
- Type-safe queries with JPA Criteria API
- Automatic schema generation (development)
- Lazy loading and caching support
- Repository pattern for clean architecture

**Configuration**:
- Hibernate dialect: PostgreSQL18Dialect
- Show SQL: false (production), true (development)
- Format SQL: true (development)
- DDL auto: validate (production), update (development)

**Connection Pooling**: HikariCP (Spring Boot default)
- Minimum idle: 5
- Maximum pool size: 20
- Connection timeout: 30 seconds
- Idle timeout: 10 minutes

**Alternatives Considered**:
- jOOQ: More control, but more complex
- MyBatis: XML-based, less type-safe

---

### 2.3 Database Migrations

**Decision**: Flyway

**Rationale**:
- Simple and reliable versioned migrations
- SQL-based migrations (familiar to DBAs)
- Automatic execution on startup
- Rollback support
- Checksum validation prevents tampering

**Configuration**:
- Location: src/main/resources/db/migration
- Naming: V{version}__{description}.sql (e.g., V1__initial_schema.sql)
- Baseline on migrate: true
- Validate on migrate: true

**Migration Strategy**:
- Development: Automatic on startup
- Staging: Automatic on startup
- Production: Manual approval, then automatic

**Alternatives Considered**:
- Liquibase: More features, but more complex (XML/YAML)

---

## 3. Security Stack

### 3.1 Authentication Provider

**Decision**: Keycloak (self-hosted or managed)

**Rationale**:
- Open-source identity and access management
- OAuth2 and OpenID Connect support
- Social login integration (Google, Facebook)
- User federation and identity brokering
- Admin console for user management
- MFA support

**Integration**: Spring Security 6 OAuth2 Resource Server

**Configuration**:
- JWT validation: spring-boot-starter-oauth2-resource-server
- Issuer URI: Keycloak realm URL
- JWK Set URI: Keycloak JWKS endpoint
- Audience validation: Client ID

**Alternatives Considered**:
- AWS Cognito: Vendor lock-in, less flexible
- Auth0: SaaS, recurring costs
- Custom auth: Too much effort, security risks

---

### 3.2 Security Framework

**Decision**: Spring Security 6

**Rationale**:
- Industry-standard for Java applications
- Comprehensive security features (authentication, authorization, CSRF, XSS)
- OAuth2 Resource Server support
- Method-level security annotations
- Security filter chain customization

**Configuration**:
- JWT authentication with Bearer tokens
- Role-based access control (RBAC)
- CSRF protection for state-changing operations
- CORS configuration for frontend
- Security headers (HSTS, CSP, X-Frame-Options)

---

### 3.3 Secrets Management

**Decision**: AWS Secrets Manager

**Rationale**:
- Secure storage for sensitive credentials
- Automatic rotation support
- Integration with RDS and other AWS services
- Audit logging via CloudTrail
- Fine-grained IAM access control

**Secrets Stored**:
- Database credentials (RDS)
- Keycloak client secrets
- JWT signing keys
- AWS service credentials (SES, SNS)

**Alternatives Considered**:
- AWS Systems Manager Parameter Store: Less features, but cheaper
- HashiCorp Vault: More complex to operate

---

## 4. Application Deployment Stack

### 4.1 Containerization

**Decision**: Docker

**Rationale**:
- Consistent environments across dev, staging, production
- Isolation and portability
- Efficient resource utilization
- Easy local development setup

**Base Image**: eclipse-temurin:25-jre-alpine
- Official OpenJDK distribution
- Alpine Linux for smaller image size
- JRE-only (no JDK needed for runtime)

**Dockerfile Best Practices**:
- Multi-stage build (build stage + runtime stage)
- Non-root user for security
- Layer caching optimization
- Health check instruction

---

### 4.2 Container Orchestration

**Decision**: AWS ECS with Fargate

**Rationale**:
- Serverless containers (no EC2 management)
- Automatic scaling and load balancing
- Integration with AWS services (ALB, CloudWatch, Secrets Manager)
- Pay only for resources used
- Simpler than Kubernetes for small deployments

**Configuration**:
- Launch type: Fargate
- Task CPU: 0.5 vCPU (512 CPU units) initially
- Task memory: 1 GB initially
- Desired count: 2 tasks (high availability)
- Auto-scaling: Target tracking (CPU 70%, memory 80%)
- Health check: /actuator/health endpoint

**Alternatives Considered**:
- AWS EKS (Kubernetes): Overkill for MVP, more complex
- AWS Elastic Beanstalk: Less control, older platform
- AWS EC2: More management overhead

---

### 4.3 Load Balancing

**Decision**: AWS Application Load Balancer (ALB)

**Rationale**:
- Layer 7 load balancing (HTTP/HTTPS)
- Path-based routing
- Health checks with automatic target removal
- SSL/TLS termination
- Integration with ECS service discovery

**Configuration**:
- Scheme: Internet-facing
- Listeners: HTTPS (443) with SSL certificate
- Target group: ECS tasks on port 8080
- Health check: /actuator/health, 30s interval
- Stickiness: Disabled (stateless API)

---

### 4.4 Container Registry

**Decision**: AWS Elastic Container Registry (ECR)

**Rationale**:
- Fully managed Docker registry
- Integration with ECS and IAM
- Image scanning for vulnerabilities
- Lifecycle policies for image cleanup
- High availability and durability

**Configuration**:
- Repository: spm-backend-api
- Image tag immutability: Enabled
- Scan on push: Enabled
- Lifecycle policy: Keep last 10 images, delete untagged after 7 days

---

## 5. Notification Stack

### 5.1 Email Service

**Decision**: AWS Simple Email Service (SES)

**Rationale**:
- Cost-effective ($0.10 per 1000 emails)
- High deliverability rates
- Email templates support
- Bounce and complaint handling
- Integration with SNS for notifications

**Configuration**:
- Region: Singapore (ap-southeast-1)
- Verified domain: tuitioncentre.com
- Configuration set: Track opens, clicks, bounces
- Templates: HTML templates for notifications

**Expected Volume**: 50-200 emails per day per centre

**Alternatives Considered**:
- SendGrid: SaaS, recurring costs
- Mailgun: SaaS, recurring costs

---

### 5.2 SMS Service

**Decision**: AWS Simple Notification Service (SNS)

**Rationale**:
- Pay-as-you-go pricing
- Global SMS coverage
- Delivery status tracking
- Integration with CloudWatch

**Configuration**:
- SMS type: Transactional (high priority)
- Default sender ID: TuitionCtr
- Monthly spend limit: $100 per centre

**Expected Volume**: 20-100 SMS per day per centre

**Alternatives Considered**:
- Twilio: More features, but higher cost
- Nexmo: Similar to Twilio

---

## 6. Storage Stack

### 6.1 File Storage

**Decision**: AWS S3

**Rationale**:
- Highly durable (99.999999999%)
- Scalable and cost-effective
- Lifecycle policies for automatic tiering
- Server-side encryption
- Pre-signed URLs for secure access

**Configuration**:
- Bucket: spm-reports-{centre-id}
- Region: Singapore (ap-southeast-1)
- Encryption: SSE-S3 (AES-256)
- Versioning: Disabled
- Lifecycle policy:
  - Standard storage: 0-365 days
  - Infrequent Access: 365-730 days
  - Delete: After 730 days (2 years)

**Access Control**:
- Private bucket (no public access)
- Pre-signed URLs with 1-hour expiration
- IAM role for ECS tasks

---

## 7. Monitoring and Logging Stack

### 7.1 Application Monitoring

**Decision**: AWS CloudWatch + Application Insights

**Rationale**:
- Native AWS integration
- Automatic metric collection
- Custom metrics support
- Dashboards and alarms
- Application Insights for APM (traces, errors, performance)

**Metrics Collected**:
- ECS task metrics (CPU, memory, network)
- ALB metrics (request count, latency, errors)
- RDS metrics (connections, CPU, IOPS, storage)
- Custom business metrics (test scores created, logins)

**Dashboards**:
- System health dashboard (CPU, memory, errors)
- API performance dashboard (latency, throughput)
- Business metrics dashboard (user activity, test scores)

---

### 7.2 Logging

**Decision**: AWS CloudWatch Logs

**Rationale**:
- Centralized log aggregation
- Log groups per service
- Log retention policies
- Log Insights for querying
- Integration with CloudWatch alarms

**Configuration**:
- Log group: /ecs/spm-backend-api
- Retention: 30 days in CloudWatch
- Archive: Export to S3 after 30 days (90-day retention)
- Long-term: S3 Glacier after 90 days (1-year retention)

**Log Format**: JSON structured logging
```json
{
  "timestamp": "2026-03-08T12:00:00Z",
  "level": "INFO",
  "logger": "com.tuitioncentre.spm.controller",
  "message": "API request processed",
  "requestId": "abc-123",
  "userId": "user-456",
  "endpoint": "/api/v1/test-scores",
  "method": "POST",
  "statusCode": 201,
  "duration": 245
}
```

---

### 7.3 Alerting

**Decision**: CloudWatch Alarms + SNS + PagerDuty

**Rationale**:
- CloudWatch Alarms for metric-based alerts
- SNS for notification routing
- PagerDuty for on-call escalation

**Alert Channels**:
- Email: DevOps team (warnings)
- Slack: #alerts channel (warnings and critical)
- PagerDuty: On-call engineer (critical only)

**Alert Rules**:
- Error rate > 5%: Warning
- Error rate > 10%: Critical
- p95 latency > 2s: Warning
- p95 latency > 5s: Critical
- CPU > 80%: Warning
- CPU > 90%: Critical
- Memory > 85%: Warning
- Memory > 95%: Critical
- Database connections > 80% pool: Warning

---

## 8. CI/CD Stack

### 8.1 Source Control

**Decision**: Git with GitHub

**Rationale**:
- Industry-standard version control
- GitHub Actions for CI/CD
- Pull request workflow
- Code review tools
- Branch protection rules

**Branching Strategy**: GitFlow
- main: Production-ready code
- develop: Integration branch
- feature/*: Feature branches
- hotfix/*: Emergency fixes

---

### 8.2 CI/CD Pipeline

**Decision**: GitHub Actions

**Rationale**:
- Native GitHub integration
- YAML-based configuration
- Matrix builds for multiple environments
- Secrets management
- Free for public repos, affordable for private

**Pipeline Stages**:
1. Build: Compile code, run unit tests
2. Test: Run integration tests with Testcontainers
3. Security: SonarQube scan, dependency check
4. Package: Build Docker image
5. Push: Push image to ECR
6. Deploy: Update ECS service (rolling deployment)

**Deployment Strategy**: Rolling deployment
- Deploy to staging automatically on develop branch
- Deploy to production manually on main branch (approval required)
- Health checks before marking deployment successful
- Automatic rollback on health check failure

**Alternatives Considered**:
- AWS CodePipeline: More complex, less flexible
- GitLab CI/CD: Would require GitLab migration
- Jenkins: Self-hosted, more maintenance

---

## 9. Testing Stack

### 9.1 Unit Testing

**Frameworks**:
- JUnit 5: Test framework
- Mockito: Mocking framework
- AssertJ: Fluent assertions

**Coverage Tool**: JaCoCo
- Minimum coverage: 80% line, 70% branch
- Reports: HTML and XML for CI integration

---

### 9.2 Integration Testing

**Frameworks**:
- Spring Boot Test: Test context and MockMvc
- Testcontainers: PostgreSQL container for tests
- REST Assured: API testing (alternative to MockMvc)

**Test Database**: Testcontainers PostgreSQL
- Isolated test database per test run
- Automatic cleanup after tests
- Same database version as production

---

### 9.3 Performance Testing

**Tool**: Gatling

**Rationale**:
- Scala-based, high performance
- Expressive DSL for scenarios
- Detailed HTML reports
- CI/CD integration

**Test Scenarios**:
- Load test: 100 concurrent users, 10 minutes
- Stress test: Ramp up to 200 users
- Spike test: Sudden traffic spike
- Endurance test: 50 users, 1 hour

---

### 9.4 Security Testing

**Tools**:
- SonarQube: Static code analysis
- OWASP Dependency-Check: Vulnerability scanning
- Snyk: Dependency vulnerability scanning (alternative)

**Penetration Testing**: Annual third-party assessment

---

## 10. Development Tools

### 10.1 IDE

**Recommended**: IntelliJ IDEA Ultimate

**Rationale**:
- Best-in-class Java IDE
- Excellent Spring Boot support
- Database tools
- Docker integration
- Git integration

**Alternatives**: Eclipse, VS Code with Java extensions

---

### 10.2 API Development

**Tool**: Postman or Insomnia

**Usage**:
- API testing during development
- Collection sharing among team
- Environment variables for different deployments

---

### 10.3 Database Management

**Tool**: DBeaver or pgAdmin

**Usage**:
- Database schema exploration
- Query development and testing
- Data inspection

---

## 11. Caching Stack

### 11.1 Application Cache

**Decision**: Caffeine

**Rationale**:
- High-performance in-memory cache
- Spring Cache abstraction support
- Automatic eviction policies
- Statistics and monitoring
- No external dependencies

**Configuration**:
```java
@EnableCaching
@Configuration
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .recordStats());
        return cacheManager;
    }
}
```

**Alternatives Considered**:
- Redis: Overkill for single-tenant, adds complexity
- Ehcache: Less performant than Caffeine

---

## 12. Resilience Stack

### 12.1 Resilience Library

**Decision**: Resilience4j

**Rationale**:
- Lightweight resilience library
- Circuit breaker, retry, rate limiter, bulkhead
- Spring Boot integration
- Metrics and monitoring

**Patterns Implemented**:
- Circuit Breaker: For Keycloak, SES, SNS
- Retry: For transient failures
- Rate Limiter: For API endpoints
- Timeout: For external calls

**Configuration**:
```yaml
resilience4j:
  circuitbreaker:
    instances:
      keycloak:
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
        slidingWindowSize: 10
  retry:
    instances:
      email:
        maxAttempts: 3
        waitDuration: 1s
        exponentialBackoffMultiplier: 2
```

**Alternatives Considered**:
- Hystrix: Deprecated by Netflix
- Spring Retry: Less features than Resilience4j

---

## Summary

This tech stack provides:
- Modern Java platform (Java 25, Spring Boot 4)
- Scalable infrastructure (ECS Fargate, RDS Multi-AZ)
- Comprehensive security (Spring Security 6, Keycloak, encryption)
- Reliable operations (CloudWatch, Flyway, Resilience4j)
- Developer productivity (Gradle, GitHub Actions, Testcontainers)
- Cost-effective deployment ($200-400/month per centre)

All technology choices prioritize:
- Production readiness
- Developer experience
- Operational simplicity
- Cost effectiveness
- Security and compliance

---

**Document Version**: 1.0  
**Created**: 2026-03-08  
**Status**: Draft
