# NFR Design Patterns - Backend API

## Overview
This document defines the design patterns and architectural approaches used to implement the non-functional requirements for the Student Progress Tracking System Backend API.

---

## 1. Resilience Patterns

### 1.1 Circuit Breaker Pattern

**Purpose**: Prevent cascading failures when external services are unavailable

**Implementation**: Resilience4j Circuit Breaker

**Applied To**:
- Keycloak authentication service
- AWS SES (email service)
- AWS SNS (SMS service)

**Configuration**:
```java
@CircuitBreaker(name = "keycloak", fallbackMethod = "keycloakFallback")
public UserInfo validateToken(String token) {
    return keycloakClient.getUserInfo(token);
}

private UserInfo keycloakFallback(String token, Exception ex) {
    log.error("Keycloak unavailable", ex);
    throw new ServiceUnavailableException("Authentication service temporarily unavailable");
}
```

**Circuit States**:
- **Closed**: Normal operation, requests pass through
- **Open**: Service failing, requests fail fast without calling service
- **Half-Open**: Testing if service recovered, limited requests allowed

**Thresholds**:
- Failure rate: 50% (5 failures in 10 requests)
- Wait duration in open state: 10 seconds
- Sliding window size: 10 requests

**Rationale**: Prevents system from repeatedly calling failing services, improves response time during outages, allows graceful degradation.

---

### 1.2 Retry Pattern

**Purpose**: Handle transient failures in external service calls

**Implementation**: Resilience4j Retry with exponential backoff

**Applied To**:
- Email sending (SES)
- SMS sending (SNS)
- Keycloak token validation (transient network issues)

**Configuration**:
```java
@Retry(name = "email", fallbackMethod = "emailFallback")
public void sendEmail(EmailRequest request) {
    sesClient.sendEmail(request);
}

private void emailFallback(EmailRequest request, Exception ex) {
    log.error("Email sending failed after retries", ex);
    notificationQueue.enqueue(request); // Queue for later retry
}
```

**Retry Strategy**:
- Maximum attempts: 3
- Initial wait: 1 second
- Backoff multiplier: 2 (exponential: 1s, 2s, 4s)
- Retry on: IOException, TimeoutException, TransientException

**Rationale**: Transient network issues are common, exponential backoff prevents overwhelming failing services, fallback ensures notifications aren't lost.

---

### 1.3 Timeout Pattern

**Purpose**: Prevent indefinite waiting for slow or unresponsive services

**Implementation**: Resilience4j TimeLimiter

**Applied To**:
- All external API calls
- Database queries
- Report generation

**Configuration**:
```java
@TimeLimiter(name = "external-api")
public CompletableFuture<Response> callExternalService() {
    return CompletableFuture.supplyAsync(() -> 
        externalClient.call()
    );
}
```

**Timeout Values**:
- External API calls: 10 seconds
- Database queries: 30 seconds
- Report generation: 60 seconds
- Health checks: 5 seconds

**Rationale**: Prevents thread exhaustion from hanging requests, ensures predictable response times, allows system to fail fast.

---

### 1.4 Bulkhead Pattern

**Purpose**: Isolate resources to prevent one failing component from exhausting all resources

**Implementation**: Thread pool isolation for different service types

**Applied To**:
- Notification sending (separate thread pool)
- Report generation (separate thread pool)
- API request handling (main thread pool)

**Configuration**:
```java
@Bulkhead(name = "notification", type = Bulkhead.Type.THREADPOOL)
public void sendNotification(Notification notification) {
    notificationService.send(notification);
}
```

**Thread Pool Sizes**:
- API requests: 50 threads (main pool)
- Notifications: 10 threads (isolated pool)
- Report generation: 5 threads (isolated pool)

**Rationale**: Prevents notification or report generation failures from blocking API requests, ensures system remains responsive even under load.

---

## 2. Scalability Patterns

### 2.1 Stateless Service Pattern

**Purpose**: Enable horizontal scaling without session affinity

**Implementation**: Stateless REST API with JWT authentication

**Design Principles**:
- No server-side session storage
- All state in JWT tokens or database
- No sticky sessions required
- Any instance can handle any request

**Benefits**:
- Easy horizontal scaling (add/remove instances)
- Load balancer can distribute requests freely
- No session replication needed
- Simplified deployment and rollback

**Rationale**: Stateless design is fundamental for cloud-native applications, enables auto-scaling and high availability.

---

### 2.2 Database Connection Pooling

**Purpose**: Efficiently manage database connections and prevent connection exhaustion

**Implementation**: HikariCP (Spring Boot default)

**Configuration**:
```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

**Pool Sizing**:
- Minimum idle: 5 connections (always ready)
- Maximum pool: 20 connections (prevents database overload)
- Formula: connections = ((core_count * 2) + effective_spindle_count)

**Monitoring**:
- Active connections
- Idle connections
- Wait time for connections
- Connection creation time

**Rationale**: Connection pooling reduces overhead of creating connections, prevents connection exhaustion, improves performance.

---

### 2.3 Read Replica Pattern

**Purpose**: Offload read-heavy queries from primary database

**Implementation**: RDS read replica for reporting queries

**Read/Write Separation**:
- **Primary (write)**: All write operations, critical reads
- **Replica (read)**: Reporting queries, progress calculations, analytics

**Spring Data JPA Configuration**:
```java
@Transactional(readOnly = true)
@ReadOnlyRepository
public interface ReportRepository extends JpaRepository<Report, UUID> {
    // Queries routed to read replica
}
```

**Replication Lag Handling**:
- Asynchronous replication (< 1 second lag typically)
- Acceptable for reporting and analytics
- Critical reads use primary database

**Rationale**: 90% read workload benefits from read replica, reduces load on primary, improves query performance.

---

### 2.4 Auto-Scaling Pattern

**Purpose**: Automatically adjust capacity based on demand

**Implementation**: ECS Service Auto Scaling with CloudWatch metrics

**Scaling Policies**:
- **Scale Out**: CPU > 70% or Memory > 80% for 5 minutes
- **Scale In**: CPU < 30% and Memory < 40% for 10 minutes
- **Min tasks**: 2 (high availability)
- **Max tasks**: 10 (cost control)

**Metrics Monitored**:
- CPU utilization
- Memory utilization
- Request count
- Response time

**Rationale**: Auto-scaling ensures system handles traffic spikes, reduces costs during low usage, maintains performance SLAs.

---

## 3. Performance Patterns

### 3.1 Caching Pattern

**Purpose**: Reduce database load and improve response times for frequently accessed data

**Implementation**: Caffeine in-memory cache with Spring Cache abstraction

**Cache Strategy**: Cache-Aside (Lazy Loading)

**Cached Data**:
- Subjects and topics (1-hour TTL)
- User profiles (15-minute TTL)
- Class lists (30-minute TTL)
- Feedback templates (1-hour TTL)

**Cache Configuration**:
```java
@Cacheable(value = "subjects", key = "#id")
public Subject getSubject(UUID id) {
    return subjectRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Subject not found"));
}

@CacheEvict(value = "subjects", key = "#subject.id")
public Subject updateSubject(Subject subject) {
    return subjectRepository.save(subject);
}
```

**Cache Invalidation**:
- **Event-driven**: Invalidate on update/delete
- **TTL-based**: Automatic expiration
- **Manual**: Admin endpoint for cache clear

**Cache Metrics**:
- Hit rate
- Miss rate
- Eviction count
- Average load time

**Rationale**: Caching reduces database queries by 60-70%, improves response times, reduces infrastructure costs.

---

### 3.2 Database Indexing Strategy

**Purpose**: Optimize query performance

**Implementation**: Strategic indexes on frequently queried columns

**Index Types**:
- **Primary Key**: Clustered index on UUID
- **Foreign Key**: Non-clustered index on all foreign keys
- **Query Filters**: Indexes on commonly filtered columns

**Key Indexes**:
```sql
-- User lookups
CREATE INDEX idx_user_keycloak_id ON users(keycloak_id);
CREATE INDEX idx_user_email ON users(email);

-- Test score queries
CREATE INDEX idx_test_score_student_date ON test_scores(student_id, test_date DESC);
CREATE INDEX idx_test_score_class ON test_scores(class_id);

-- Enrollment queries
CREATE INDEX idx_class_student_status ON class_students(class_id, status);

-- Notification queries
CREATE INDEX idx_notification_user_status ON notifications(user_id, status, created_at DESC);
```

**Index Maintenance**:
- Monitor index usage with pg_stat_user_indexes
- Remove unused indexes
- Rebuild fragmented indexes periodically

**Rationale**: Proper indexing reduces query time from seconds to milliseconds, critical for performance SLAs.

---

### 3.3 Pagination Pattern

**Purpose**: Limit result set size and improve response times

**Implementation**: Offset-based pagination with Spring Data

**API Design**:
```java
@GetMapping("/api/v1/test-scores")
public PagedResponseDTO<TestScoreDTO> getTestScores(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    @RequestParam(defaultValue = "testDate") String sortBy,
    @RequestParam(defaultValue = "DESC") String sortOrder
) {
    Pageable pageable = PageRequest.of(page, size, 
        Sort.by(Sort.Direction.fromString(sortOrder), sortBy));
    Page<TestScore> scores = testScoreRepository.findAll(pageable);
    return PagedResponseDTO.from(scores);
}
```

**Pagination Limits**:
- Default page size: 20
- Maximum page size: 100
- Fetch all option: size=-1 (use with caution)

**Rationale**: Pagination prevents large result sets from overwhelming clients and servers, improves perceived performance.

---

### 3.4 Asynchronous Processing Pattern

**Purpose**: Offload long-running tasks from request threads

**Implementation**: Spring @Async with thread pools

**Applied To**:
- Notification sending (email/SMS)
- Report generation
- Bulk operations

**Configuration**:
```java
@Async("notificationExecutor")
public CompletableFuture<Void> sendNotificationAsync(Notification notification) {
    notificationService.send(notification);
    return CompletableFuture.completedFuture(null);
}

@Bean(name = "notificationExecutor")
public Executor notificationExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("notification-");
    executor.initialize();
    return executor;
}
```

**Thread Pools**:
- Notification executor: 5-10 threads, 100 queue capacity
- Report executor: 2-5 threads, 50 queue capacity

**Rationale**: Async processing prevents blocking API requests, improves throughput, enables better resource utilization.

---

## 4. Security Patterns

### 4.1 OAuth2 Resource Server Pattern

**Purpose**: Secure API endpoints with JWT token validation

**Implementation**: Spring Security 6 OAuth2 Resource Server

**Authentication Flow**:
1. Client obtains JWT from Keycloak
2. Client includes JWT in Authorization header
3. Spring Security validates JWT signature and claims
4. Request proceeds if valid, 401 if invalid

**Configuration**:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/**").authenticated()
            );
        return http.build();
    }
}
```

**JWT Validation**:
- Signature verification (RSA public key from Keycloak)
- Expiration check
- Issuer validation
- Audience validation

**Rationale**: OAuth2 is industry standard, JWT tokens are stateless, Keycloak handles complex auth logic.

---

### 4.2 Role-Based Access Control (RBAC) Pattern

**Purpose**: Enforce authorization based on user roles

**Implementation**: Spring Security method-level security

**Authorization Levels**:
- **Method-level**: @PreAuthorize annotations
- **Service-level**: Custom authorization logic
- **Data-level**: Filter queries by ownership

**Examples**:
```java
@PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
public void createStudent(CreateStudentRequest request) {
    // Only admins and teachers can create students
}

@PreAuthorize("@authService.canAccessStudent(#studentId)")
public StudentDTO getStudent(UUID studentId) {
    // Custom authorization logic checks ownership
}
```

**Authorization Service**:
```java
@Service
public class AuthorizationService {
    public boolean canAccessStudent(UUID studentId) {
        User currentUser = getCurrentUser();
        if (currentUser.hasRole("ADMIN")) return true;
        if (currentUser.hasRole("TEACHER")) {
            return teacherOwnsStudent(currentUser.getId(), studentId);
        }
        if (currentUser.hasRole("PARENT")) {
            return parentOwnsStudent(currentUser.getId(), studentId);
        }
        if (currentUser.hasRole("STUDENT")) {
            return currentUser.getStudentId().equals(studentId);
        }
        return false;
    }
}
```

**Rationale**: RBAC provides fine-grained access control, prevents unauthorized data access, enforces business rules.

---

### 4.3 Input Validation Pattern

**Purpose**: Prevent injection attacks and ensure data integrity

**Implementation**: Bean Validation (JSR-380) with custom validators

**Validation Layers**:
1. **DTO validation**: @Valid annotations on controller parameters
2. **Business validation**: Service layer validation
3. **Database constraints**: NOT NULL, UNIQUE, CHECK constraints

**Example**:
```java
public class CreateTestScoreRequest {
    @NotNull(message = "Student ID is required")
    private UUID studentId;
    
    @NotBlank(message = "Test name is required")
    @Size(max = 255, message = "Test name too long")
    private String testName;
    
    @NotNull(message = "Test date is required")
    @PastOrPresent(message = "Test date cannot be in future")
    private LocalDate testDate;
    
    @NotNull(message = "Overall score is required")
    @DecimalMin(value = "0.0", message = "Score cannot be negative")
    @DecimalMax(value = "100.0", message = "Score cannot exceed 100")
    @Digits(integer = 3, fraction = 2, message = "Invalid score format")
    private BigDecimal overallScore;
    
    @NotEmpty(message = "Questions are required")
    @Valid
    private List<QuestionRequest> questions;
}
```

**Custom Validators**:
- Score sum validation (topic scores sum to overall)
- Date range validation
- Business rule validation

**Rationale**: Multi-layer validation prevents bad data, protects against injection attacks, provides clear error messages.

---

### 4.4 Encryption Pattern

**Purpose**: Protect sensitive data at rest and in transit

**Implementation**: Multi-layer encryption strategy

**Encryption Layers**:
1. **Transport**: TLS 1.2+ for all API calls
2. **Database**: RDS encryption at rest (AES-256)
3. **Storage**: S3 server-side encryption (SSE-S3)
4. **Secrets**: AWS Secrets Manager encryption

**TLS Configuration**:
```yaml
server:
  ssl:
    enabled: true
    protocol: TLS
    enabled-protocols: TLSv1.2,TLSv1.3
    ciphers: TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,...
```

**Database Connection**:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://host:5432/db?ssl=true&sslmode=require
```

**Rationale**: Defense in depth, meets compliance requirements (PDPA), protects against data breaches.

---

## 5. Observability Patterns

### 5.1 Structured Logging Pattern

**Purpose**: Enable efficient log searching and analysis

**Implementation**: JSON structured logging with Logback

**Log Structure**:
```json
{
  "timestamp": "2026-03-08T12:00:00.000Z",
  "level": "INFO",
  "logger": "com.tuitioncentre.spm.controller.TestScoreController",
  "thread": "http-nio-8080-exec-1",
  "message": "Test score created",
  "requestId": "abc-123-def-456",
  "userId": "user-789",
  "studentId": "student-101",
  "endpoint": "/api/v1/test-scores",
  "method": "POST",
  "statusCode": 201,
  "duration": 245,
  "context": {
    "testScoreId": "score-202",
    "className": "Grade 10 Math A"
  }
}
```

**Key Fields**:
- **requestId**: Trace requests across services
- **userId**: Audit user actions
- **duration**: Performance monitoring
- **context**: Business-specific data

**Rationale**: Structured logs enable powerful queries, facilitate troubleshooting, support compliance auditing.

---

### 5.2 Health Check Pattern

**Purpose**: Enable load balancer and monitoring to detect unhealthy instances

**Implementation**: Spring Boot Actuator health endpoint

**Health Checks**:
```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        try {
            dataSource.getConnection().close();
            return Health.up()
                .withDetail("database", "PostgreSQL")
                .withDetail("status", "Connected")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

**Health Endpoint**: `/actuator/health`

**Health Indicators**:
- Database connectivity (required)
- Disk space (required)
- Keycloak availability (optional, degraded if down)

**Load Balancer Configuration**:
- Health check path: /actuator/health
- Interval: 30 seconds
- Healthy threshold: 2 successes
- Unhealthy threshold: 3 failures

**Rationale**: Health checks enable automatic instance replacement, prevent routing to unhealthy instances, improve availability.

---

### 5.3 Metrics Collection Pattern

**Purpose**: Monitor application performance and business metrics

**Implementation**: Micrometer with CloudWatch backend

**Metric Types**:
- **Counters**: Request count, error count, test scores created
- **Gauges**: Active connections, memory usage, cache size
- **Timers**: Request duration, database query time
- **Distribution summaries**: Request size, response size

**Custom Metrics**:
```java
@Service
public class TestScoreService {
    private final Counter testScoreCounter;
    private final Timer testScoreCreationTimer;
    
    public TestScoreService(MeterRegistry registry) {
        this.testScoreCounter = registry.counter("test.scores.created");
        this.testScoreCreationTimer = registry.timer("test.scores.creation.time");
    }
    
    public TestScore createTestScore(CreateTestScoreRequest request) {
        return testScoreCreationTimer.record(() -> {
            TestScore score = // ... create test score
            testScoreCounter.increment();
            return score;
        });
    }
}
```

**Rationale**: Metrics enable proactive monitoring, identify performance bottlenecks, track business KPIs.

---

## 6. Data Management Patterns

### 6.1 Repository Pattern

**Purpose**: Abstract data access logic and provide clean API

**Implementation**: Spring Data JPA repositories

**Repository Hierarchy**:
```java
public interface TestScoreRepository extends JpaRepository<TestScore, UUID> {
    Page<TestScore> findByStudentId(UUID studentId, Pageable pageable);
    
    @Query("SELECT ts FROM TestScore ts WHERE ts.studentId = :studentId " +
           "AND ts.testDate BETWEEN :startDate AND :endDate")
    List<TestScore> findByStudentAndDateRange(
        @Param("studentId") UUID studentId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
```

**Benefits**:
- Clean separation of concerns
- Testable data access layer
- Type-safe queries
- Automatic CRUD operations

**Rationale**: Repository pattern provides abstraction over data access, enables easy testing, follows clean architecture principles.

---

### 6.2 Database Migration Pattern

**Purpose**: Version control database schema changes

**Implementation**: Flyway versioned migrations

**Migration Structure**:
```
src/main/resources/db/migration/
├── V1__initial_schema.sql
├── V2__add_feedback_templates.sql
├── V3__add_notification_preferences.sql
└── V4__add_indexes.sql
```

**Migration Example**:
```sql
-- V1__initial_schema.sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    keycloak_id VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_keycloak_id ON users(keycloak_id);
CREATE INDEX idx_user_email ON users(email);
```

**Migration Strategy**:
- Development: Automatic on startup
- Production: Manual approval, then automatic
- Rollback: Manual rollback scripts for critical changes

**Rationale**: Versioned migrations ensure consistent database state, enable safe deployments, provide audit trail of schema changes.

---

## Summary

These NFR design patterns provide:
- **Resilience**: Circuit breaker, retry, timeout, bulkhead patterns
- **Scalability**: Stateless services, connection pooling, read replicas, auto-scaling
- **Performance**: Caching, indexing, pagination, async processing
- **Security**: OAuth2, RBAC, input validation, encryption
- **Observability**: Structured logging, health checks, metrics
- **Data Management**: Repository pattern, database migrations

All patterns are production-proven, align with Spring Boot best practices, and support the NFR requirements defined for the system.

---

**Document Version**: 1.0  
**Created**: 2026-03-08  
**Status**: Draft
