# Functional Design Plan - Backend API (UNIT-01)

## Overview
This plan outlines the functional design approach for the Backend API unit, focusing on business logic, domain models, and business rules for the Student Progress Tracking System.

## Unit Context
- **Unit ID**: UNIT-01
- **Unit Name**: Backend API
- **Technology**: Java 25, Spring Boot 4.x, Spring Security 6, PostgreSQL 18
- **Architecture**: Modular monolith with feature-based modules

## Functional Design Phases

### Phase 1: Domain Model Design
- [x] Define core domain entities and their relationships
- [x] Design entity attributes and data types
- [x] Define entity lifecycle states
- [x] Design entity validation rules
- [x] Create entity relationship diagrams

### Phase 2: Business Logic Modeling
- [x] Model authentication and authorization flows
- [x] Model user management workflows
- [x] Model student enrollment and class assignment workflows
- [x] Model test score recording workflows
- [x] Model progress calculation algorithms
- [x] Model notification triggering logic
- [x] Model report generation workflows

### Phase 3: Business Rules Definition
- [x] Define validation rules for all entities
- [x] Define authorization rules for each operation
- [x] Define data integrity constraints
- [x] Define business constraints (e.g., score ranges, date validations)
- [x] Define notification triggering rules
- [x] Define multi-tenant isolation rules

### Phase 4: API Contract Design
- [x] Define request/response DTOs for each endpoint
- [x] Define error response structures
- [x] Define pagination and filtering contracts
- [x] Define API versioning strategy

---

## Clarifying Questions

### Domain Model Questions

**Q1: User Entity Relationships**

The system has four user roles: Teacher, Parent, Student, and Centre Administrator. How should we model these in the database?

A) Single User table with a "role" field (simple, but less type-safe)
B) User base table + separate Teacher/Parent/Student/Admin tables (normalized, role-specific fields)
C) Separate tables for each role type (no shared User table)
D) User table + UserRole junction table (supports multiple roles per user)

[Answer]: D

**Rationale for your choice**:
[Answer]: teacher might be admin also

---

**Q2: Parent-Student Relationship**

How should we model the relationship between parents and students?

A) One-to-many (one parent can have multiple children, but each child has only one parent in the system)
B) Many-to-many (multiple parents can be linked to multiple children - supports both parents)
C) Fixed relationship (parent can have max 2-3 children, stored as foreign keys)

[Answer]: A

**Additional considerations** (e.g., should we track which parent is primary contact?):
[Answer]: parents use the same account.

---

**Q3: Class-Student Enrollment**

Students can be enrolled in multiple classes. Should we track enrollment history?

A) Current enrollment only (simple junction table: ClassStudent with classId, studentId)
B) Track enrollment history (add enrollmentDate, withdrawalDate, status fields)
C) Full audit trail (separate EnrollmentHistory table with all changes)

[Answer]: B

**Do we need to support**:
- Student withdrawal from classes? [Answer]: yes
- Re-enrollment in the same class? [Answer]: yes
- Enrollment effective dates (future enrollments)? [Answer]: no

---

**Q4: Test Score Structure**

How should we structure test scores and topic-level breakdowns?

A) Single TestScore table with JSON field for topic scores (flexible, less queryable)
B) TestScore table + TopicScore table (normalized, fully queryable)
C) TestScore table with columns for each topic (rigid, requires schema changes for new topics)

[Answer]: B

**Topic score details**:
- Should topic scores be mandatory or optional? [Answer]: mandatory
- Should topic scores sum to the overall score, or are they independent? [Answer]: topic scores sum to the overall score
- Can a test have scores for topics from different subjects? [Answer]: yes. topics are per-question (EACH question can also be multi-topic ie q1a is kinematics, q1b is dynamics)

---

**Q5: Subject and Topic Management**

The system has default subjects (Math, Science, English, Chinese) with default topics. How should we handle customization?

A) Shared default subjects + centre-specific overrides (copy-on-write pattern)
B) All subjects are centre-specific (each centre gets a copy of defaults on setup)
C) Shared subjects with centre-specific topic additions (hybrid approach)

[Answer]: B

**Customization scope**:
- Can centres add custom subjects not in defaults? [Answer]: yes
- Can centres modify default topic names? [Answer]: yes
- Can centres deactivate default topics? [Answer]: yes

---

**Q6: Teacher-Class Assignment**

How should we model the relationship between teachers and classes?

A) One teacher per class (simple foreign key on Class table)
B) Multiple teachers per class (junction table: ClassTeacher)
C) Primary teacher + assistant teachers (Class has primaryTeacherId + ClassTeacher junction for assistants)

[Answer]: A

**Teaching scenarios**:
- Can a teacher teach multiple classes? [Answer]: yes
- Can a class have co-teachers with equal responsibility? [Answer]: no
- Do we need to track teaching history (past assignments)? [Answer]: yes

---

### Business Logic Questions

**Q7: Progress Calculation Algorithm**

How should we calculate student progress and trends?

A) Simple average of all test scores (easy, but doesn't show improvement)
B) Weighted average (recent tests weighted more heavily)
C) Linear regression trend line (shows improvement trajectory)
D) Comparison to class average (relative performance)
E) Combination of multiple metrics

[Answer]: A

**Progress metrics to include**:
- Overall score trend? [Answer]: yes
- Topic-specific trends? [Answer]: yes
- Comparison to class average? [Answer]: no
- Improvement rate (velocity)? [Answer]: yes

---

**Q8: Notification Triggering Rules**

When should the system send notifications to parents?

A) Every time a test score is recorded (immediate, high volume)
B) Daily digest of all new scores (batched, lower volume)
C) Only for significant events (low scores, big improvements)
D) Configurable per parent (parent chooses frequency)

[Answer]: A

**Notification scenarios**:
- Should we notify when teacher adds feedback without a new score? [Answer]: yes
- Should we notify students as well as parents? [Answer]: yes
- Should we notify for score updates/corrections? [Answer]: yes
- Should we have different rules for email vs SMS? [Answer]: no

---

**Q9: Multi-Tenant Data Isolation**

How should we implement data isolation between tuition centres?

A) Database-level (separate database per centre)
B) Schema-level (separate schema per centre in same database)
C) Row-level (centreId column on all tables, enforced in queries)
D) Application-level (centreId in JWT, filtered in service layer)

[Answer]: every centre is a different deployment (separate aws account)

**Isolation enforcement**:
- Should centreId be in every table? [Answer]: yes
- Should we use database constraints to enforce isolation? [Answer]: is this applicable
- How do we handle shared reference data (e.g., default subjects)? [Answer]: default subjects are hardcoded

---

**Q10: Test Score Validation Rules**

What validation rules should we enforce for test scores?

**Score range**:
- Minimum score: [Answer]: 0
- Maximum score: [Answer]: 100
- Allow negative scores? [Answer]: no
- Allow decimal scores (e.g., 85.5)? [Answer]: yes

**Date validation**:
- Can test date be in the future? [Answer]: no
- How far back can test dates go? [Answer]: any
- Can multiple tests have the same date? [Answer]: yes

**Topic score validation**:
- Must topic scores be provided? [Answer]: yes
- Must topic scores sum to overall score? [Answer]: yes
- Can topic scores exceed overall score? [Answer]: no

---

**Q11: User Management Workflows**

How should user account creation and management work?

**Account creation**:
- Who can create teacher accounts? [Answer]: admin/teacher
- Who can create parent accounts? [Answer]: admin/teacher
- Who can create student accounts? [Answer]: admin/teacher
- Should we support self-registration for any role? [Answer]: admin/teacher

**Account lifecycle**:
- Should we support account deactivation (soft delete)? [Answer]: yes
- Should we support account reactivation? [Answer]: yes
- What happens to data when a user is deactivated? [Answer]: keep

---

**Q12: Class Management Rules**

What are the business rules for class creation and management?

**Class constraints**:
- Minimum students per class: [Answer]: 0
- Maximum students per class: [Answer]: 100
- Can a class exist with no students? [Answer]: yes 
- Can a class exist with no teacher? [Answer]: no

**Class lifecycle**:
- Should we support class archival (end of term)? [Answer]: no
- Should we track class start/end dates? [Answer]: no
- Can students be in multiple classes for the same subject? [Answer]: yes

---

**Q13: Feedback Management**

How should teacher feedback be structured and managed?

**Feedback structure**:
- Free-form text only? [Answer]: yes
- Structured feedback with categories (e.g., strengths, areas for improvement)? [Answer]: yes
- Predefined feedback templates? [Answer]: yes
- Character limit for feedback? [Answer]: no

**Feedback visibility**:
- Should feedback be visible to students? [Answer]: yes
- Should feedback be visible to other teachers? [Answer]: no
- Can feedback be edited after submission? [Answer]: yes
- Should we track feedback edit history? [Answer]: no, but it should show "edited" flag

---

**Q14: Report Generation Logic**

What should be included in the basic progress report?

**Report content**:
- Time period for report (last 30 days, last term, all time)? [Answer]: all time
- Include all tests or only selected tests? [Answer]: all tests by default, able to select specific tests
- Include teacher feedback in report? [Answer]: yes
- Include comparison to class average? [Answer]: no
- Include topic-level breakdown? [Answer]: yes

**Report format**:
- PDF, HTML, or both? [Answer]: html
- Should reports be stored or generated on-demand? [Answer]: generated on demand and stored
- Should we support report customization? [Answer]: no

---

**Q15: Authorization Rules**

What are the detailed authorization rules for each operation?

**Teacher permissions**:
- Can teachers view students from other classes? [Answer]: no
- Can teachers edit scores they didn't create? [Answer]: teachers can only view and edit their own classes
- Can teachers delete test scores? [Answer]: teachers can only view and edit their own classes
- Can teachers view other teachers' feedback? [Answer]: teachers can only view and edit their own classes

**Parent permissions**:
- Can parents view other children's data (siblings)? [Answer]: no
- Can parents edit any data? [Answer]: no
- Can parents delete their own account? [Answer]: no

**Student permissions**:
- Can students view their raw scores or only charts? [Answer]: both
- Can students view class averages? [Answer]: no
- Can students view other students' data? [Answer]: no

**Admin permissions**:
- Can admins view all test scores? [Answer]: no 
- Can admins edit test scores? [Answer]: no 
- Can admins impersonate other users? [Answer]: no 

---

### API Design Questions

**Q16: Pagination Strategy**

How should we handle pagination for list endpoints?

A) Offset-based pagination (page number + page size)
B) Cursor-based pagination (cursor token for next page)
C) Keyset pagination (last seen ID)
D) No pagination (return all results)

[Answer]: A

**Pagination defaults**:
- Default page size: [Answer]: depends on specific api
- Maximum page size: [Answer]: depends on specific api
- Should we support "fetch all" option? [Answer]: yes

---

**Q17: Error Response Format**

How should we structure error responses?

A) Simple message string
B) Structured error object (code, message, details)
C) RFC 7807 Problem Details format
D) Custom format with field-level errors

[Answer]: B

**Error details to include**:
- Error code (e.g., "INVALID_SCORE")? [Answer]: yes
- Field-level validation errors? [Answer]: yes
- Timestamp? [Answer]: yes
- Request ID for tracing? [Answer]: not yet

---

**Q18: Filtering and Sorting**

What filtering and sorting capabilities should list endpoints support?

**Test scores filtering**:
- Filter by date range? [Answer]: yes
- Filter by subject? [Answer]: yes
- Filter by score range? [Answer]: yes
- Filter by class? [Answer]: yes

**Sorting options**:
- Sort by date (ascending/descending)? [Answer]: yes
- Sort by score? [Answer]: yes
- Sort by student name? [Answer]: yes
- Default sort order: [Answer]: depends on specific api

---

**Q19: Batch Operations**

Should we support batch operations for efficiency?

**Batch score entry**:
- Should teachers be able to enter scores for multiple students at once? [Answer]: no
- Should we support CSV import for test scores? [Answer]: no
- Maximum batch size: [Answer]: no

**Batch user management**:
- Should admins be able to create multiple students at once? [Answer]: yes
- Should we support CSV import for student enrollment? [Answer]: yes

---

**Q20: API Versioning**

How should we handle API versioning for future changes?

A) URL versioning (e.g., /api/v1/students)
B) Header versioning (e.g., Accept: application/vnd.api+json;version=1)
C) No versioning (breaking changes require migration)
D) Query parameter versioning (e.g., /api/students?version=1)

[Answer]: A

**Version strategy**:
- Should we support multiple versions simultaneously? [Answer]: yes
- How long should we maintain old versions? [Answer]: 2 versions

---

## Plan Completion Checklist

- [x] All questions answered by user
- [x] All ambiguities resolved
- [x] Domain model design complete
- [x] Business logic modeling complete
- [x] Business rules definition complete
- [x] API contract design complete
- [x] Functional design artifacts generated
- [ ] User approval received

---

**Plan Status**: Awaiting User Approval  
**Created**: 2026-03-08  
**Unit**: UNIT-01 (Backend API)
