# Business Rules - Backend API

## Overview
This document defines all business rules, validation rules, constraints, and policies for the Student Progress Tracking System.

---

## 1. User Management Rules

### 1.1 User Account Rules

**UR-001: Email Uniqueness**
- Rule: Email addresses must be unique across all users
- Enforcement: Database unique constraint + application validation
- Error: "Email already exists"

**UR-002: Account Creation Authority**
- Rule: Only users with ADMIN or TEACHER roles can create accounts
- Enforcement: Authorization check in service layer
- Error: 403 Forbidden

**UR-003: No Self-Registration**
- Rule: Users cannot self-register; all accounts must be created by admin/teacher
- Enforcement: No public registration endpoint
- Error: N/A (endpoint does not exist)

**UR-004: Account Deactivation**
- Rule: Deactivated users cannot log in but all data is retained
- Enforcement: Keycloak user status + database isActive flag
- Error: "Account is deactivated"

**UR-005: Account Reactivation**
- Rule: Deactivated users can be reactivated by admins
- Enforcement: Admin-only endpoint
- Error: 403 Forbidden (non-admin)

**UR-006: Multiple Roles**
- Rule: A user can have multiple roles (e.g., TEACHER + ADMIN)
- Enforcement: UserRole junction table
- Error: N/A

---

### 1.2 Parent Account Rules

**PR-001: One Parent Per Student**
- Rule: Each student can have only one parent account (shared by both parents)
- Enforcement: Database constraint + application validation
- Error: "Student already has a parent account"

**PR-002: Parent Display Name**
- Rule: Parent accounts display as "Parent of [Student Name]"
- Enforcement: Application logic in UI and reports
- Error: N/A

**PR-003: Parent Cannot Edit Data**
- Rule: Parents have read-only access to their child's data
- Enforcement: Authorization checks on all write operations
- Error: 403 Forbidden

**PR-004: Parent Cannot Delete Account**
- Rule: Parents cannot delete their own accounts
- Enforcement: No delete endpoint for parent role
- Error: 403 Forbidden

---

### 1.3 Role-Based Access Rules

**RR-001: Teacher Class Ownership**
- Rule: Teachers can only view and manage their own classes
- Enforcement: Authorization filter on all class-related queries
- Error: 403 Forbidden

**RR-002: Teacher Cannot View Other Teachers' Data**
- Rule: Teachers cannot view other teachers' classes, students, scores, or feedback
- Enforcement: Authorization filter based on class ownership
- Error: 403 Forbidden

**RR-003: Parent Single Child Access**
- Rule: Parents can only view their own child's data
- Enforcement: Authorization filter based on parentId
- Error: 403 Forbidden

**RR-004: Student Self-Access Only**
- Rule: Students can only view their own data
- Enforcement: Authorization filter based on studentId
- Error: 403 Forbidden

**RR-005: Admin Full Access**
- Rule: Admins can view and edit all data
- Enforcement: Role check bypasses ownership filters
- Error: N/A

**RR-006: No Impersonation**
- Rule: No user (including admins) can impersonate other users
- Enforcement: No impersonation feature implemented
- Error: N/A

---

## 2. Class Management Rules

### 2.1 Class Creation Rules

**CR-001: Class Requires Teacher**
- Rule: A class cannot exist without an assigned teacher
- Enforcement: Database NOT NULL constraint on teacherId
- Error: "Teacher is required"

**CR-002: Class Can Have Zero Students**
- Rule: A class can be created with no students enrolled
- Enforcement: No minimum student constraint
- Error: N/A

**CR-003: Maximum Students Per Class**
- Rule: A class cannot exceed maxStudents (default: 100)
- Enforcement: Application validation on enrollment
- Error: "Class is at maximum capacity"

**CR-004: One Teacher Per Class**
- Rule: A class has exactly one current teacher (no co-teachers)
- Enforcement: Single foreign key relationship
- Error: N/A

**CR-005: Teacher Can Teach Multiple Classes**
- Rule: A teacher can be assigned to multiple classes
- Enforcement: One-to-many relationship
- Error: N/A

---

### 2.2 Enrollment Rules

**ER-001: Student Multiple Class Enrollment**
- Rule: A student can be enrolled in multiple classes simultaneously
- Enforcement: Many-to-many relationship via ClassStudent
- Error: N/A

**ER-002: Student Multiple Classes Same Subject**
- Rule: A student can be enrolled in multiple classes for the same subject
- Enforcement: No unique constraint on (studentId, subjectId)
- Error: N/A

**ER-003: Enrollment Date Validation**
- Rule: Enrollment date cannot be in the future
- Enforcement: Application validation
- Error: "Enrollment date cannot be in the future"

**ER-004: Withdrawal Date After Enrollment**
- Rule: Withdrawal date must be after enrollment date
- Enforcement: Application validation
- Error: "Withdrawal date must be after enrollment date"

**ER-005: Re-enrollment Allowed**
- Rule: A student can withdraw and re-enroll in the same class
- Enforcement: Create new ClassStudent record with new enrollmentDate
- Error: N/A

**ER-006: No Future Enrollments**
- Rule: Future enrollments are not supported
- Enforcement: enrollmentDate validation
- Error: "Future enrollments are not supported"

---

### 2.3 Teacher Assignment Rules

**TR-001: Teacher History Tracking**
- Rule: Teacher-class assignment history must be tracked
- Enforcement: TeacherClassHistory table with startDate and endDate
- Error: N/A

**TR-002: Teacher Change Updates History**
- Rule: When a class teacher changes, the old teacher's history record is closed (endDate set) and a new record is created
- Enforcement: Application logic in teacher change workflow
- Error: N/A

**TR-003: Historical Scores Retain Original Teacher**
- Rule: Test scores retain the teacherId of the teacher who created them, even if class teacher changes
- Enforcement: TestScore.teacherId is immutable
- Error: N/A

---

## 3. Test Score Rules

### 3.1 Score Validation Rules

**SR-001: Score Range**
- Rule: Overall score must be between 0.00 and 100.00
- Enforcement: Database check constraint + application validation
- Error: "Score must be between 0 and 100"

**SR-002: Decimal Scores Allowed**
- Rule: Scores can have up to 2 decimal places (e.g., 85.50)
- Enforcement: Database DECIMAL(5,2) type
- Error: N/A

**SR-003: No Negative Scores**
- Rule: Scores cannot be negative
- Enforcement: Minimum value validation
- Error: "Score cannot be negative"

**SR-004: Test Date Not Future**
- Rule: Test date cannot be in the future
- Enforcement: Application validation
- Error: "Test date cannot be in the future"

**SR-005: Test Date Any Past Date**
- Rule: Test date can be any date in the past (no minimum)
- Enforcement: No minimum date validation
- Error: N/A

**SR-006: Multiple Tests Same Date**
- Rule: Multiple tests can have the same date
- Enforcement: No unique constraint on (studentId, testDate)
- Error: N/A

---

### 3.2 Topic Score Rules

**TS-001: Topic Scores Mandatory**
- Rule: Topic-level scores must be provided for all test scores
- Enforcement: Application validation (at least one sub-question required)
- Error: "Topic scores are required"

**TS-002: Topic Scores Must Sum to Overall**
- Rule: Sum of all sub-question scores must equal overall score
- Enforcement: Application validation
- Error: "Topic scores do not sum to overall score"

**TS-003: Topic Max Scores Must Sum to Test Max**
- Rule: Sum of all sub-question max scores must equal test max score (100.00)
- Enforcement: Application validation
- Error: "Topic max scores do not sum to test max score"

**TS-004: Topic Scores Cannot Exceed Max**
- Rule: Each sub-question score cannot exceed its max score
- Enforcement: Application validation
- Error: "Score exceeds maximum for sub-question"

**TS-005: Same Topic Multiple Times**
- Rule: The same topic can appear multiple times in one test (different sub-questions)
- Enforcement: No unique constraint on (testScoreId, topicId)
- Error: N/A

**TS-006: Topic Scores Summed for Trends**
- Rule: When calculating topic trends, sum all sub-question scores for that topic
- Enforcement: Application logic in progress calculation
- Error: N/A

---

### 3.3 Question Structure Rules

**QS-001: Test Has Questions**
- Rule: A test must have at least one question
- Enforcement: Application validation
- Error: "Test must have at least one question"

**QS-002: Question Has Sub-Questions**
- Rule: Each question must have at least one sub-question
- Enforcement: Application validation
- Error: "Question must have at least one sub-question"

**QS-003: Sub-Question Has One Topic**
- Rule: Each sub-question must have exactly one topic
- Enforcement: Application validation + database constraint
- Error: "Sub-question must have exactly one topic"

**QS-004: Question Numbers Tracked**
- Rule: Question numbers/labels (e.g., "Q1", "Q2a") must be stored
- Enforcement: Database fields for questionNumber and subQuestionLabel
- Error: N/A

**QS-005: Sub-Question Scores Sum to Question Score**
- Rule: Sum of sub-question scores should equal question score
- Enforcement: Application validation (optional, for data integrity)
- Error: Warning (not blocking)

---

### 3.4 Score Authorization Rules

**SA-001: Teacher Can Only Score Own Classes**
- Rule: Teachers can only create test scores for students in their own classes
- Enforcement: Authorization check on classId ownership
- Error: 403 Forbidden

**SA-002: Teacher Can Edit Own Scores**
- Rule: Teachers can edit test scores they created for their own classes
- Enforcement: Authorization check on teacherId and classId
- Error: 403 Forbidden

**SA-003: Teacher Can Delete Own Scores**
- Rule: Teachers can delete test scores for their own classes
- Enforcement: Authorization check on classId ownership
- Error: 403 Forbidden

**SA-004: Admin Can Edit All Scores**
- Rule: Admins can view, edit, and delete all test scores
- Enforcement: Role check bypasses ownership filters
- Error: N/A

**SA-005: Score Updates Trigger Notifications**
- Rule: Creating or updating a test score triggers notifications to parent and student
- Enforcement: Event-driven notification system
- Error: N/A (notification failures are logged but don't block score creation)

---

## 4. Feedback Rules

### 4.1 Feedback Structure Rules

**FR-001: Feedback Is Optional**
- Rule: Feedback is optional for test scores
- Enforcement: Nullable foreign key relationship
- Error: N/A

**FR-002: Hybrid Feedback Structure**
- Rule: Feedback can have structured categories (strengths, improvements, recommendations) and/or free-form notes
- Enforcement: All fields are nullable
- Error: N/A

**FR-003: At Least One Field Required**
- Rule: At least one feedback field must be provided (not all null)
- Enforcement: Application validation
- Error: "At least one feedback field is required"

**FR-004: No Character Limits**
- Rule: Feedback fields have no character limits
- Enforcement: Database TEXT type
- Error: N/A

**FR-005: All Categories Optional**
- Rule: Structured categories (strengths, improvements, recommendations) are all optional
- Enforcement: Nullable fields
- Error: N/A

---

### 4.2 Feedback Visibility Rules

**FV-001: Feedback Visible to Students**
- Rule: Students can view feedback on their own test scores
- Enforcement: Authorization allows student self-access
- Error: N/A

**FV-002: Feedback Visible to Parents**
- Rule: Parents can view feedback on their child's test scores
- Enforcement: Authorization allows parent access to child's data
- Error: N/A

**FV-003: Feedback Not Visible to Other Teachers**
- Rule: Teachers cannot view other teachers' feedback
- Enforcement: Authorization filter based on class ownership
- Error: 403 Forbidden

**FV-004: Admins Can View All Feedback**
- Rule: Admins can view all feedback
- Enforcement: Role check bypasses ownership filters
- Error: N/A

---

### 4.3 Feedback Edit Rules

**FE-001: Teacher Can Edit Own Feedback**
- Rule: Teachers can edit feedback they created
- Enforcement: Authorization check on teacherId
- Error: 403 Forbidden

**FE-002: Edit Flag Set on Update**
- Rule: When feedback is edited, isEdited flag is set to true
- Enforcement: Application logic on update
- Error: N/A

**FE-003: No Edit History Tracking**
- Rule: Edit history is not tracked (only isEdited flag)
- Enforcement: No audit table for feedback changes
- Error: N/A

**FE-004: Feedback Updates Trigger Notifications**
- Rule: Updating feedback triggers notifications to parent and student
- Enforcement: Event-driven notification system
- Error: N/A

---

### 4.4 Feedback Template Rules

**FT-001: Templates Are Snippets**
- Rule: Feedback templates are snippets/phrases, not full feedback
- Enforcement: Application UI design
- Error: N/A

**FT-002: System-Wide Templates**
- Rule: System-wide templates are available to all teachers
- Enforcement: FeedbackTemplate.isSystemWide = true, teacherId = null
- Error: N/A

**FT-003: Teacher-Specific Templates**
- Rule: Teachers can create their own templates
- Enforcement: FeedbackTemplate.teacherId = current user
- Error: N/A

**FT-004: Templates Not Saved as References**
- Rule: Template content is inserted into feedback form, not saved as a reference
- Enforcement: Application logic (copy content, not link)
- Error: N/A

---

## 5. Subject and Topic Rules

### 5.1 Subject Management Rules

**SM-001: Default Subjects Hardcoded**
- Rule: Default subjects (Math, Science, English, Chinese) are hardcoded in configuration
- Enforcement: Configuration file loaded on system initialization
- Error: N/A

**SM-002: Subject Code Uniqueness**
- Rule: Subject codes must be unique
- Enforcement: Database unique constraint
- Error: "Subject code already exists"

**SM-003: Centres Can Add Custom Subjects**
- Rule: Centres can add custom subjects beyond defaults
- Enforcement: Admin-only endpoint for subject creation
- Error: N/A

**SM-004: Centres Can Modify Subject Names**
- Rule: Centres can modify default subject names
- Enforcement: Admin-only endpoint for subject update
- Error: N/A

**SM-005: Centres Can Deactivate Subjects**
- Rule: Centres can deactivate subjects (soft delete)
- Enforcement: isActive flag
- Error: N/A

**SM-006: Deactivated Subjects Retained**
- Rule: Deactivated subjects are retained for historical data
- Enforcement: Soft delete (isActive = false)
- Error: N/A

---

### 5.2 Topic Management Rules

**TM-001: Default Topics Hardcoded**
- Rule: Default topics for each subject are hardcoded in configuration
- Enforcement: Configuration file loaded on system initialization
- Error: N/A

**TM-002: Topic Code Unique Within Subject**
- Rule: Topic codes must be unique within a subject
- Enforcement: Database unique constraint on (subjectId, code)
- Error: "Topic code already exists for this subject"

**TM-003: Centres Can Add Custom Topics**
- Rule: Centres can add custom topics to any subject
- Enforcement: Admin-only endpoint for topic creation
- Error: N/A

**TM-004: Centres Can Modify Topic Names**
- Rule: Centres can modify default topic names
- Enforcement: Admin-only endpoint for topic update
- Error: N/A

**TM-005: Centres Can Deactivate Topics**
- Rule: Centres can deactivate topics (soft delete)
- Enforcement: isActive flag
- Error: N/A

**TM-006: Deactivated Topics Retained**
- Rule: Deactivated topics are retained for historical data
- Enforcement: Soft delete (isActive = false)
- Error: N/A

---

## 6. Notification Rules

### 6.1 Notification Triggering Rules

**NR-001: Immediate Notifications**
- Rule: Notifications are sent immediately when events occur (not batched)
- Enforcement: Event-driven architecture with synchronous/asynchronous processing
- Error: N/A

**NR-002: New Score Triggers Notification**
- Rule: Creating a test score triggers notifications to parent and student
- Enforcement: Event published on TestScore creation
- Error: N/A

**NR-003: Score Update Triggers Notification**
- Rule: Updating a test score triggers notifications to parent and student
- Enforcement: Event published on TestScore update
- Error: N/A

**NR-004: New Feedback Triggers Notification**
- Rule: Creating feedback triggers notifications to parent and student
- Enforcement: Event published on Feedback creation
- Error: N/A

**NR-005: Feedback Update Triggers Notification**
- Rule: Updating feedback triggers notifications to parent and student
- Enforcement: Event published on Feedback update
- Error: N/A

**NR-006: Respect Notification Preferences**
- Rule: Notifications respect user preferences (email/SMS enabled flags)
- Enforcement: Check preferences before creating notification records
- Error: N/A

**NR-007: Failed Notifications Don't Block Operations**
- Rule: Notification failures are logged but do not block score/feedback creation
- Enforcement: Asynchronous notification processing with error handling
- Error: N/A

---

### 6.2 Notification Channel Rules

**NC-001: Email and SMS Channels**
- Rule: Notifications can be sent via email and/or SMS
- Enforcement: Notification.channel enum (EMAIL, SMS)
- Error: N/A

**NC-002: Same Rules for Email and SMS**
- Rule: Email and SMS notifications follow the same triggering rules
- Enforcement: Same event handlers for both channels
- Error: N/A

**NC-003: Parent Preferences Control Channels**
- Rule: Parent notification preferences control which channels are used
- Enforcement: Check emailNotificationsEnabled and smsNotificationsEnabled
- Error: N/A

**NC-004: Student Preferences Control Channels**
- Rule: Student notification preferences control which channels are used (if implemented)
- Enforcement: Check student notification preferences (future enhancement)
- Error: N/A

---

## 7. Progress Calculation Rules

### 7.1 Overall Progress Rules

**PR-001: Simple Average Calculation**
- Rule: Overall progress is calculated as simple average of all test scores
- Enforcement: Application logic in progress calculation
- Error: N/A

**PR-002: All Tests Included**
- Rule: All test scores are included in progress calculation (no filtering by class or subject)
- Enforcement: Query all test scores for student
- Error: N/A

**PR-003: Chronological Ordering**
- Rule: Progress trends are ordered by test date ascending
- Enforcement: ORDER BY testDate ASC
- Error: N/A

---

### 7.2 Topic Progress Rules

**TP-001: Topic Scores Summed**
- Rule: Topic scores are calculated by summing all sub-question scores for that topic
- Enforcement: Application logic in topic trend calculation
- Error: N/A

**TP-002: Topic Can Appear Multiple Times**
- Rule: The same topic can appear multiple times in one test (sum all occurrences)
- Enforcement: SUM(score) GROUP BY topicId, testScoreId
- Error: N/A

**TP-003: Topic Percentage Calculation**
- Rule: Topic percentage = (sum of topic scores / sum of topic max scores) * 100
- Enforcement: Application logic
- Error: N/A

---

### 7.3 Improvement Velocity Rules

**VR-001: Minimum Two Tests Required**
- Rule: Improvement velocity requires at least 2 tests
- Enforcement: Return null if fewer than 2 tests
- Error: N/A

**VR-002: First vs Recent Average**
- Rule: Velocity compares average of first 3 tests to average of last 3 tests
- Enforcement: Application logic
- Error: N/A

**VR-003: Velocity Per Month**
- Rule: Velocity is expressed as points per month
- Enforcement: improvement / (timeSpan / 30)
- Error: N/A

---

## 8. Report Generation Rules

### 8.1 Report Content Rules

**RC-001: All-Time Default Period**
- Rule: Reports default to all-time period if no date range specified
- Enforcement: Application logic (no date filter if startDate and endDate are null)
- Error: N/A

**RC-002: Date Range Filtering**
- Rule: Reports can be filtered by date range (startDate, endDate)
- Enforcement: WHERE testDate BETWEEN startDate AND endDate
- Error: N/A

**RC-003: Test Selection**
- Rule: Reports can include only selected tests (selectedTestIds)
- Enforcement: WHERE testScoreId IN (selectedTestIds)
- Error: N/A

**RC-004: Include Feedback**
- Rule: Reports include teacher feedback for all included tests
- Enforcement: JOIN Feedback table
- Error: N/A

**RC-005: Include Topic Breakdown**
- Rule: Reports include topic-level performance breakdown
- Enforcement: Calculate topic trends for all topics
- Error: N/A

**RC-006: No Class Average Comparison**
- Rule: Reports do not include comparison to class average
- Enforcement: No class average calculation
- Error: N/A

---

### 8.2 Report Storage Rules

**RS-001: HTML Format Only**
- Rule: Reports are generated in HTML format only
- Enforcement: Report generator produces HTML
- Error: N/A

**RS-002: Generated On-Demand**
- Rule: Reports are generated on-demand, not pre-generated
- Enforcement: Report generation triggered by user request
- Error: N/A

**RS-003: Stored in S3**
- Rule: Reports are stored in S3 for 2 years
- Enforcement: Upload to S3 after generation, set expiresAt = now + 2 years
- Error: N/A

**RS-004: Historical Snapshots**
- Rule: Reports are historical snapshots (not regenerated if data changes)
- Enforcement: Report content is static HTML
- Error: N/A

**RS-005: Access Previously Generated Reports**
- Rule: Users can access previously generated reports via reportId
- Enforcement: Pre-signed S3 URL generation
- Error: N/A

**RS-006: Expired Reports Return 410**
- Rule: Expired reports (expiresAt < now) return 410 Gone
- Enforcement: Check expiresAt before generating pre-signed URL
- Error: 410 Gone

---

### 8.3 Report Authorization Rules

**RA-001: Teacher Can Generate for Own Students**
- Rule: Teachers can generate reports for students in their classes
- Enforcement: Authorization check on class ownership
- Error: 403 Forbidden

**RA-002: Parent Can Generate for Own Child**
- Rule: Parents can generate reports for their own child
- Enforcement: Authorization check on parentId
- Error: 403 Forbidden

**RA-003: Student Can Generate for Self**
- Rule: Students can generate reports for themselves
- Enforcement: Authorization check on studentId
- Error: 403 Forbidden

**RA-004: Admin Can Generate for Any Student**
- Rule: Admins can generate reports for any student
- Enforcement: Role check bypasses ownership filters
- Error: N/A

---

## 9. Multi-Tenant Rules

### 9.1 Single-Tenant Deployment Rules

**MT-001: Separate Deployment Per Centre**
- Rule: Each tuition centre has its own deployment and database
- Enforcement: Infrastructure configuration (separate AWS accounts)
- Error: N/A

**MT-002: No centreId Column**
- Rule: Since each deployment serves one centre, no centreId column is needed
- Enforcement: Database schema does not include centreId
- Error: N/A

**MT-003: No Cross-Centre Constraints**
- Rule: No database constraints are needed to enforce cross-centre isolation
- Enforcement: Physical database separation
- Error: N/A

---

## 10. API Rules

### 10.1 Pagination Rules

**PG-001: Offset-Based Pagination**
- Rule: List endpoints use offset-based pagination (page number + page size)
- Enforcement: Query parameters: page (default: 0), size (default: 20)
- Error: N/A

**PG-002: Default Page Size 20**
- Rule: Default page size is 20 items
- Enforcement: Application default
- Error: N/A

**PG-003: Maximum Page Size 100**
- Rule: Maximum page size is 100 items
- Enforcement: Application validation
- Error: "Page size cannot exceed 100"

**PG-004: Fetch All Option**
- Rule: Endpoints support "fetch all" option (size=-1 or size=0)
- Enforcement: Special handling for size=-1
- Error: N/A

---

### 10.2 Error Response Rules

**ER-001: Structured Error Format**
- Rule: Errors return structured error object (code, message, details)
- Enforcement: Global exception handler
- Format:
```json
{
  "code": "INVALID_SCORE",
  "message": "Score must be between 0 and 100",
  "details": {
    "field": "overallScore",
    "value": 150
  },
  "timestamp": "2026-03-08T12:00:00Z"
}
```

**ER-002: Error Codes**
- Rule: All errors include a machine-readable error code
- Enforcement: Enum of error codes
- Error: N/A

**ER-003: Field-Level Validation Errors**
- Rule: Validation errors include field-level details
- Enforcement: details object with field and value
- Error: N/A

**ER-004: Timestamp Included**
- Rule: All errors include a timestamp
- Enforcement: Global exception handler adds timestamp
- Error: N/A

**ER-005: No Request ID (MVP)**
- Rule: Request IDs for tracing are not included in MVP
- Enforcement: Future enhancement
- Error: N/A

---

### 10.3 Filtering and Sorting Rules

**FS-001: Date Range Filtering**
- Rule: Test score endpoints support date range filtering (startDate, endDate)
- Enforcement: Query parameters: startDate, endDate
- Error: N/A

**FS-002: Subject Filtering**
- Rule: Test score endpoints support subject filtering
- Enforcement: Query parameter: subjectId
- Error: N/A

**FS-003: Score Range Filtering**
- Rule: Test score endpoints support score range filtering (minScore, maxScore)
- Enforcement: Query parameters: minScore, maxScore
- Error: N/A

**FS-004: Class Filtering**
- Rule: Test score endpoints support class filtering
- Enforcement: Query parameter: classId
- Error: N/A

**FS-005: Sorting Options**
- Rule: List endpoints support sorting by date, score, and name
- Enforcement: Query parameters: sortBy, sortOrder (ASC/DESC)
- Error: N/A

**FS-006: Default Sort Order**
- Rule: Default sort order depends on specific API (e.g., test scores default to date DESC)
- Enforcement: Application default per endpoint
- Error: N/A

---

### 10.4 API Versioning Rules

**AV-001: URL Versioning**
- Rule: API versioning uses URL path (e.g., /api/v1/students)
- Enforcement: Controller path mapping
- Error: N/A

**AV-002: Multiple Versions Supported**
- Rule: System supports multiple API versions simultaneously
- Enforcement: Separate controller classes per version
- Error: N/A

**AV-003: Two Version Maintenance**
- Rule: System maintains 2 versions (current + previous)
- Enforcement: Deprecation policy
- Error: N/A

---

## 11. Batch Operation Rules

### 11.1 CSV Import Rules

**CSV-001: Bulk Student Creation**
- Rule: Admins can create multiple students via CSV import
- Enforcement: Admin-only endpoint
- Error: 403 Forbidden

**CSV-002: Bulk Enrollment**
- Rule: Admins can enroll multiple students via CSV import
- Enforcement: Admin-only endpoint
- Error: 403 Forbidden

**CSV-003: Partial Success Allowed**
- Rule: CSV imports allow partial success (some rows succeed, some fail)
- Enforcement: Process all rows, return summary with errors
- Error: N/A

**CSV-004: No Batch Score Entry**
- Rule: Batch test score entry is not supported in MVP
- Enforcement: No batch endpoint
- Error: N/A

---

## Summary

This document defines 150+ business rules across:
- User management (account creation, roles, authorization)
- Class management (creation, enrollment, teacher assignment)
- Test scores (validation, topic breakdown, authorization)
- Feedback (structure, visibility, editing, templates)
- Subjects and topics (defaults, customization, deactivation)
- Notifications (triggering, channels, preferences)
- Progress calculation (trends, velocity)
- Report generation (content, storage, authorization)
- Multi-tenant deployment (single-tenant mode)
- API design (pagination, errors, filtering, versioning)
- Batch operations (CSV import)

All rules include enforcement mechanisms and error handling.

---

**Document Version**: 1.0  
**Created**: 2026-03-08  
**Status**: Draft
