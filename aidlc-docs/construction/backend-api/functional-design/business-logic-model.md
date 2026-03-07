# Business Logic Model - Backend API

## Overview
This document defines the core business logic, workflows, and algorithms for the Student Progress Tracking System.

---

## 1. Authentication and Authorization Flows

### 1.1 User Login Flow

**Trigger**: User clicks "Login" button

**Process**:
1. Frontend redirects user to Keycloak login page
2. User enters credentials or selects social login (Google/Facebook)
3. Keycloak authenticates user
4. Keycloak redirects back to frontend with authorization code
5. Frontend exchanges authorization code for JWT access token
6. Frontend stores JWT token
7. Frontend includes JWT in Authorization header for all API requests
8. Backend validates JWT using Spring Security OAuth2 Resource Server
9. Backend extracts user identity from JWT (keycloakId)
10. Backend loads user roles from database
11. Backend authorizes request based on roles and resource ownership

**Success Criteria**:
- User is authenticated and redirected to role-appropriate dashboard
- JWT token is valid and contains user identity
- User roles are loaded and cached

**Error Scenarios**:
- Invalid credentials → Show error message from Keycloak
- Network error → Show connection error message
- JWT validation failure → Return 401 Unauthorized

---

### 1.2 Role-Based Authorization Flow

**Trigger**: User attempts to access a protected resource

**Process**:
1. Extract JWT from Authorization header
2. Validate JWT signature and expiration
3. Extract keycloakId from JWT
4. Load User entity by keycloakId
5. Load UserRole entities for user
6. Check if user has required role for the operation
7. Check resource ownership (e.g., teacher can only access own classes)
8. Grant or deny access

**Authorization Rules**:

**Teacher**:
- Can view and manage own classes only
- Can view students enrolled in own classes only
- Can create/edit/delete test scores for own classes only
- Can create/edit feedback for own classes only
- Cannot view other teachers' classes or students
- Cannot view other teachers' feedback

**Parent**:
- Can view own child's data only (one child per parent account)
- Cannot view other students' data
- Cannot edit any data
- Cannot delete own account

**Student**:
- Can view own data only (scores, progress, feedback)
- Can view both raw scores and charts
- Cannot view class averages
- Cannot view other students' data
- Cannot edit any data

**Admin**:
- Can view all data (users, classes, students, scores, feedback)
- Can edit all data (users, classes, students, scores, feedback)
- Can create/deactivate/reactivate user accounts
- Can manage classes and enrollments
- Can manage subjects and topics
- Cannot impersonate other users (no impersonation feature)

**Success Criteria**:
- Authorized users can access resources
- Unauthorized users receive 403 Forbidden

---

### 1.3 User Logout Flow

**Trigger**: User clicks "Logout" button

**Process**:
1. Frontend calls Keycloak logout endpoint
2. Frontend clears JWT token from storage
3. Frontend redirects to login page
4. Backend invalidates any server-side session (if applicable)

**Success Criteria**:
- User is logged out and cannot access protected resources
- JWT token is cleared from frontend
- Back button does not allow access to protected pages

---

## 2. User Management Workflows

### 2.1 Create Teacher Account

**Trigger**: Admin or Teacher creates a new teacher account

**Process**:
1. Admin/Teacher submits teacher creation form (email, firstName, lastName, phoneNumber, specialization)
2. Backend validates input data
3. Backend creates user in Keycloak via Admin API
4. Backend receives keycloakId from Keycloak
5. Backend creates User entity with keycloakId
6. Backend creates UserRole entity with role=TEACHER
7. Backend creates Teacher entity with userId
8. Backend sends welcome email to teacher with login instructions
9. Backend returns success response

**Validation Rules**:
- Email must be unique
- Email must be valid format
- First name and last name are required
- Phone number is optional
- Specialization is optional

**Success Criteria**:
- Teacher account is created in Keycloak and database
- Teacher receives welcome email
- Teacher can log in with Keycloak credentials

**Error Scenarios**:
- Duplicate email → Return 409 Conflict
- Keycloak API failure → Return 500 Internal Server Error
- Invalid input → Return 400 Bad Request with field errors

---

### 2.2 Create Parent Account

**Trigger**: Admin or Teacher creates a new parent account

**Process**:
1. Admin/Teacher submits parent creation form (email, firstName, lastName, phoneNumber, studentId)
2. Backend validates input data
3. Backend checks that student exists and doesn't already have a parent
4. Backend creates user in Keycloak via Admin API
5. Backend receives keycloakId from Keycloak
6. Backend creates User entity with keycloakId
7. Backend creates UserRole entity with role=PARENT
8. Backend creates Parent entity with userId
9. Backend updates Student entity with parentId
10. Backend sends welcome email to parent with login instructions
11. Backend returns success response

**Validation Rules**:
- Email must be unique
- Email must be valid format
- First name and last name are required
- Phone number is optional
- Student must exist
- Student must not already have a parent account

**Success Criteria**:
- Parent account is created in Keycloak and database
- Parent is linked to student
- Parent receives welcome email
- Parent can log in and view child's data

**Error Scenarios**:
- Student already has parent → Return 409 Conflict
- Student not found → Return 404 Not Found
- Duplicate email → Return 409 Conflict

---

### 2.3 Create Student Account

**Trigger**: Admin or Teacher creates a new student account

**Process**:
1. Admin/Teacher submits student creation form (email, firstName, lastName, dateOfBirth, grade)
2. Backend validates input data
3. Backend creates user in Keycloak via Admin API
4. Backend receives keycloakId from Keycloak
5. Backend creates User entity with keycloakId
6. Backend creates UserRole entity with role=STUDENT
7. Backend creates Student entity with userId, enrollmentDate=today
8. Backend returns success response (parent account created separately)

**Validation Rules**:
- Email must be unique
- Email must be valid format
- First name and last name are required
- Date of birth is optional
- Grade is optional
- Enrollment date defaults to today

**Success Criteria**:
- Student account is created in Keycloak and database
- Student can log in (credentials provided to parent)

**Error Scenarios**:
- Duplicate email → Return 409 Conflict
- Invalid input → Return 400 Bad Request

---

### 2.4 Deactivate User Account

**Trigger**: Admin deactivates a user account

**Process**:
1. Admin submits deactivation request for userId
2. Backend validates that user exists and is active
3. Backend sets User.isActive = false
4. Backend sets User.deactivatedAt = now
5. Backend deactivates user in Keycloak (optional)
6. Backend returns success response

**Business Rules**:
- Deactivated users cannot log in
- All historical data is retained (scores, feedback, enrollments)
- Deactivated users can be reactivated

**Success Criteria**:
- User cannot log in
- User data is retained

---

### 2.5 Reactivate User Account

**Trigger**: Admin reactivates a deactivated user account

**Process**:
1. Admin submits reactivation request for userId
2. Backend validates that user exists and is deactivated
3. Backend sets User.isActive = true
4. Backend sets User.deactivatedAt = null
5. Backend reactivates user in Keycloak (optional)
6. Backend returns success response

**Success Criteria**:
- User can log in again
- User retains all historical data

---

## 3. Class and Enrollment Workflows

### 3.1 Create Class

**Trigger**: Admin creates a new class

**Process**:
1. Admin submits class creation form (name, subjectId, teacherId, description, maxStudents)
2. Backend validates input data
3. Backend validates that subject exists
4. Backend validates that teacher exists and has TEACHER role
5. Backend creates Class entity
6. Backend creates TeacherClassHistory record (teacherId, classId, startDate=today)
7. Backend returns success response

**Validation Rules**:
- Class name is required
- Subject must exist
- Teacher must exist and have TEACHER role
- Max students defaults to 100
- Class can be created with no students

**Success Criteria**:
- Class is created and assigned to teacher
- Teacher can view class in their class list
- Teacher history is recorded

---

### 3.2 Enroll Student in Class

**Trigger**: Admin enrolls a student in a class

**Process**:
1. Admin submits enrollment request (classId, studentId)
2. Backend validates that class exists and is active
3. Backend validates that student exists and is active
4. Backend checks that class has not reached maxStudents
5. Backend checks if student is already enrolled (active enrollment)
6. Backend creates ClassStudent entity (enrollmentDate=today, status=ACTIVE)
7. Backend returns success response

**Validation Rules**:
- Class must exist and be active
- Student must exist and be active
- Class must not be at max capacity
- Student can be enrolled in multiple classes simultaneously
- Student can be enrolled in multiple classes for the same subject

**Success Criteria**:
- Student is enrolled in class
- Teacher can see student in class list
- Student can see class in their dashboard

**Error Scenarios**:
- Class at max capacity → Return 409 Conflict
- Student already enrolled → Return 409 Conflict

---

### 3.3 Withdraw Student from Class

**Trigger**: Admin withdraws a student from a class

**Process**:
1. Admin submits withdrawal request (classId, studentId)
2. Backend validates that enrollment exists and is active
3. Backend updates ClassStudent entity (withdrawalDate=today, status=WITHDRAWN)
4. Backend returns success response

**Business Rules**:
- Withdrawal date must be after enrollment date
- Historical enrollment data is retained
- Student can re-enroll in the same class later

**Success Criteria**:
- Student is withdrawn from class
- Teacher no longer sees student in active class list
- Enrollment history is retained

---

### 3.4 Change Class Teacher

**Trigger**: Admin assigns a new teacher to a class

**Process**:
1. Admin submits teacher change request (classId, newTeacherId)
2. Backend validates that class exists
3. Backend validates that new teacher exists and has TEACHER role
4. Backend updates TeacherClassHistory for current teacher (endDate=today)
5. Backend updates Class entity (teacherId=newTeacherId)
6. Backend creates new TeacherClassHistory record (newTeacherId, classId, startDate=today)
7. Backend returns success response

**Business Rules**:
- Teacher history is tracked for audit purposes
- Historical test scores retain original teacherId
- New teacher can view all historical scores for the class

**Success Criteria**:
- Class is assigned to new teacher
- New teacher can view and manage class
- Old teacher can no longer access class
- Teacher history is recorded

---

## 4. Test Score Recording Workflows

### 4.1 Record Test Score with Topic Breakdown

**Trigger**: Teacher records a test score for a student

**Process**:
1. Teacher submits test score form:
   - studentId, classId, testName, testDate, overallScore
   - questions: [{questionNumber, maxScore, subQuestions: [{label, score, maxScore, topicId}]}]
2. Backend validates input data
3. Backend validates authorization (teacher owns class)
4. Backend validates that student is enrolled in class (active enrollment)
5. Backend creates TestScore entity
6. Backend creates Question entities for each question
7. Backend creates SubQuestion entities for each sub-question
8. Backend creates SubQuestionTopic entities linking sub-questions to topics
9. Backend validates that sum of sub-question scores equals overall score
10. Backend validates that sum of sub-question max scores equals test max score
11. Backend commits transaction
12. Backend triggers notification event (NEW_TEST_SCORE)
13. Backend returns success response

**Validation Rules**:
- Overall score: 0.00 to 100.00, decimals allowed
- Test date: cannot be in future, any past date allowed
- Test name: required
- Questions: at least one question required
- Sub-questions: at least one sub-question per question
- Each sub-question must have exactly one topic
- Sum of sub-question scores must equal overall score
- Sum of sub-question max scores must equal test max score (100.00)
- Score cannot exceed max score for any sub-question

**Success Criteria**:
- Test score is recorded with full topic breakdown
- Parent receives notification (email and/or SMS)
- Student receives notification
- Score appears in student's test history
- Topic-level scores are calculated correctly

**Error Scenarios**:
- Unauthorized teacher → Return 403 Forbidden
- Student not in class → Return 400 Bad Request
- Score validation failure → Return 400 Bad Request with field errors
- Sum mismatch → Return 400 Bad Request "Scores do not sum correctly"

---

### 4.2 Update Test Score

**Trigger**: Teacher or Admin updates an existing test score

**Process**:
1. User submits update request with testScoreId and updated data
2. Backend validates that test score exists
3. Backend validates authorization (teacher owns class OR user is admin)
4. Backend updates TestScore entity
5. Backend updates Question, SubQuestion, SubQuestionTopic entities as needed
6. Backend validates score sums
7. Backend commits transaction
8. Backend triggers notification event (TEST_SCORE_UPDATED)
9. Backend returns success response

**Business Rules**:
- Teachers can only update scores for their own classes
- Admins can update any score
- Score updates trigger notifications
- All validation rules from creation apply

**Success Criteria**:
- Test score is updated
- Notifications are sent
- Updated score appears in student's history

---

### 4.3 Delete Test Score

**Trigger**: Teacher or Admin deletes a test score

**Process**:
1. User submits delete request with testScoreId
2. Backend validates that test score exists
3. Backend validates authorization (teacher owns class OR user is admin)
4. Backend deletes SubQuestionTopic entities
5. Backend deletes SubQuestion entities
6. Backend deletes Question entities
7. Backend deletes associated Feedback entities
8. Backend deletes TestScore entity
9. Backend commits transaction
10. Backend returns success response

**Business Rules**:
- Teachers can only delete scores for their own classes
- Admins can delete any score
- Deleting a test score also deletes associated feedback
- No notifications are sent for deletions

**Success Criteria**:
- Test score and all related data are deleted
- Score no longer appears in student's history

---

## 5. Progress Calculation Algorithms

### 5.1 Overall Score Trend Calculation

**Purpose**: Calculate student's overall score trend over time

**Algorithm**:
1. Retrieve all test scores for student, ordered by testDate ascending
2. For each test score, extract (testDate, overallScore)
3. Return array of data points: [{date, score}]

**Calculation Method**: Simple chronological list (no averaging or weighting)

**Output**: Array of {date, score} for charting

**Business Rules**:
- Include all test scores regardless of class or subject
- Order by test date ascending (oldest first)
- No filtering by date range (all-time by default)

---

### 5.2 Topic-Specific Trend Calculation

**Purpose**: Calculate student's performance trend for a specific topic

**Algorithm**:
1. Retrieve all test scores for student
2. For each test score:
   a. Find all SubQuestionTopic records for the given topicId
   b. Sum the scores for that topic
   c. Sum the max scores for that topic
   d. Calculate percentage: (sum of scores / sum of max scores) * 100
3. Return array of data points: [{testDate, topicScore, topicMaxScore, percentage}]

**Calculation Method**: Sum all sub-question scores for the topic per test

**Output**: Array of {date, topicScore, topicMaxScore, percentage} for charting

**Business Rules**:
- A topic can appear multiple times in one test (sum all occurrences)
- Only include tests where the topic appears
- Order by test date ascending

---

### 5.3 Improvement Rate (Velocity) Calculation

**Purpose**: Calculate rate of improvement over time

**Algorithm**:
1. Retrieve all test scores for student, ordered by testDate ascending
2. If fewer than 2 tests, return null (insufficient data)
3. Calculate first test average: average of first 3 tests (or all if < 3)
4. Calculate recent test average: average of last 3 tests (or all if < 3)
5. Calculate improvement: recentAverage - firstAverage
6. Calculate time span: days between first and last test
7. Calculate velocity: improvement / (timeSpan / 30) [improvement per month]

**Output**: {improvement, velocity, firstAverage, recentAverage}

**Business Rules**:
- Requires at least 2 tests
- Velocity is expressed as points per month
- Positive velocity = improving, negative = declining

---

## 6. Notification Triggering Logic

### 6.1 New Test Score Notification

**Trigger**: TestScore entity is created

**Process**:
1. Event: NEW_TEST_SCORE published with testScoreId
2. Notification handler receives event
3. Load TestScore, Student, Parent entities
4. Check Parent notification preferences (emailNotificationsEnabled, smsNotificationsEnabled)
5. If email enabled, create Notification entity (type=NEW_TEST_SCORE, channel=EMAIL, userId=parent.userId)
6. If SMS enabled, create Notification entity (type=NEW_TEST_SCORE, channel=SMS, userId=parent.userId)
7. Create Notification entities for Student (email and SMS if enabled)
8. Notification sender processes pending notifications asynchronously
9. Send email via AWS SES (or configured email service)
10. Send SMS via AWS SNS (or configured SMS service)
11. Update Notification status to SENT or FAILED

**Notification Content**:
- Subject: "New Test Score: [testName] for [studentName]"
- Message: "[studentName] received a score of [overallScore]/100 on [testName] taken on [testDate]. View details: [link]"

**Business Rules**:
- Notifications sent immediately (not batched)
- Respect parent notification preferences
- Respect student notification preferences
- Failed notifications are logged but do not block score creation

---

### 6.2 Test Score Updated Notification

**Trigger**: TestScore entity is updated

**Process**: Same as 6.1, but with type=TEST_SCORE_UPDATED

**Notification Content**:
- Subject: "Test Score Updated: [testName] for [studentName]"
- Message: "[studentName]'s score for [testName] has been updated to [overallScore]/100. View details: [link]"

---

### 6.3 New Feedback Notification

**Trigger**: Feedback entity is created

**Process**: Same as 6.1, but with type=NEW_FEEDBACK

**Notification Content**:
- Subject: "New Teacher Feedback: [testName] for [studentName]"
- Message: "Teacher [teacherName] has provided feedback for [studentName]'s [testName]. View feedback: [link]"

---

### 6.4 Feedback Updated Notification

**Trigger**: Feedback entity is updated

**Process**: Same as 6.1, but with type=FEEDBACK_UPDATED

**Notification Content**:
- Subject: "Teacher Feedback Updated: [testName] for [studentName]"
- Message: "Teacher [teacherName] has updated feedback for [studentName]'s [testName]. View feedback: [link]"

---

## 7. Report Generation Workflows

### 7.1 Generate Basic Progress Report

**Trigger**: User requests a progress report for a student

**Process**:
1. User submits report request (studentId, reportType=BASIC_PROGRESS, startDate, endDate, selectedTestIds)
2. Backend validates authorization (teacher owns student's class, parent owns student, student is self, or user is admin)
3. Backend retrieves test scores for student (filtered by date range if provided, or all-time)
4. Backend retrieves feedback for test scores
5. Backend calculates overall score trend
6. Backend calculates topic-specific trends for all topics
7. Backend calculates improvement velocity
8. Backend generates HTML report with:
   - Student information
   - Test score list with dates and scores
   - Overall score trend chart (line chart)
   - Topic-level breakdown chart (line chart per topic)
   - Teacher feedback summary
   - Improvement velocity metrics
9. Backend uploads HTML report to S3
10. Backend creates ProgressReport entity (s3Key, s3Bucket, expiresAt=now+2years)
11. Backend returns report URL and reportId

**Report Content**:
- Student name and grade
- Report period (date range or "All Time")
- Test score table (testName, testDate, overallScore, feedback summary)
- Overall score trend line chart
- Topic-level performance charts (one chart per topic)
- Improvement velocity: "[+/-X] points per month"
- Teacher feedback excerpts

**Business Rules**:
- Reports are generated on-demand (not pre-generated)
- Reports are stored in S3 for 2 years
- Reports are historical snapshots (not updated if data changes)
- Users can access previously generated reports via reportId
- Default report period: all-time
- Can filter by date range (startDate, endDate)
- Can select specific tests (selectedTestIds)

**Success Criteria**:
- Report is generated and stored in S3
- Report URL is returned to user
- User can view report in browser
- Report is accessible for 2 years

---

### 7.2 Access Previously Generated Report

**Trigger**: User requests access to a previously generated report

**Process**:
1. User submits request with reportId
2. Backend validates that report exists
3. Backend validates authorization (same rules as generation)
4. Backend checks if report has expired (expiresAt < now)
5. If expired, return 410 Gone
6. Backend generates pre-signed S3 URL (valid for 1 hour)
7. Backend returns pre-signed URL

**Success Criteria**:
- User can access report via pre-signed URL
- Expired reports return 410 Gone

---

## 8. Feedback Management Workflows

### 8.1 Create Feedback

**Trigger**: Teacher creates feedback for a test score

**Process**:
1. Teacher submits feedback form (testScoreId, strengths, areasForImprovement, recommendations, additionalNotes)
2. Backend validates that test score exists
3. Backend validates authorization (teacher owns class)
4. Backend validates that at least one field is provided
5. Backend creates Feedback entity
6. Backend triggers notification event (NEW_FEEDBACK)
7. Backend returns success response

**Validation Rules**:
- At least one field must be provided (not all null)
- All fields are optional
- No character limits

**Success Criteria**:
- Feedback is created and linked to test score
- Notifications are sent to parent and student
- Feedback is visible to student and parent

---

### 8.2 Update Feedback

**Trigger**: Teacher updates existing feedback

**Process**:
1. Teacher submits update request (feedbackId, updated fields)
2. Backend validates that feedback exists
3. Backend validates authorization (teacher owns class)
4. Backend updates Feedback entity
5. Backend sets isEdited = true
6. Backend triggers notification event (FEEDBACK_UPDATED)
7. Backend returns success response

**Business Rules**:
- Teachers can edit their own feedback
- Editing sets isEdited flag (no edit history tracking)
- Feedback updates trigger notifications

**Success Criteria**:
- Feedback is updated
- isEdited flag is set
- Notifications are sent

---

### 8.3 Use Feedback Template

**Trigger**: Teacher selects a feedback template while creating/editing feedback

**Process**:
1. Teacher requests template list (category filter optional)
2. Backend returns system-wide templates + teacher's own templates
3. Teacher selects template
4. Frontend inserts template content into appropriate field
5. Teacher can edit the inserted content before saving

**Business Rules**:
- Templates are snippets/phrases, not full feedback
- Teachers can use system-wide templates and their own templates
- Template content is inserted into the form, not saved as a reference

---

### 8.4 Create Feedback Template

**Trigger**: Teacher creates a new feedback template

**Process**:
1. Teacher submits template form (category, title, content)
2. Backend validates input data
3. Backend creates FeedbackTemplate entity (teacherId=current user, isSystemWide=false)
4. Backend returns success response

**Validation Rules**:
- Category must be valid enum value
- Title and content are required

**Success Criteria**:
- Template is created and available for teacher to use
- Template appears in teacher's template list

---

## 9. Subject and Topic Management Workflows

### 9.1 Initialize Default Subjects and Topics

**Trigger**: System initialization (first deployment)

**Process**:
1. Backend reads default subjects from configuration file
2. For each default subject:
   a. Create Subject entity (isDefault=true)
   b. For each default topic:
      - Create Topic entity (isDefault=true)
3. Backend commits transaction

**Default Configuration** (hardcoded in config file):
```yaml
subjects:
  - name: Mathematics
    code: MATH
    topics:
      - {name: Algebra, code: ALG}
      - {name: Geometry, code: GEO}
      - {name: Calculus, code: CALC}
      - {name: Statistics, code: STAT}
  - name: Science
    code: SCI
    topics:
      - {name: Kinematics, code: KIN}
      - {name: Dynamics, code: DYN}
      - {name: Thermodynamics, code: THERMO}
      - {name: Electricity, code: ELEC}
  - name: English
    code: ENG
    topics:
      - {name: Grammar, code: GRAM}
      - {name: Vocabulary, code: VOCAB}
      - {name: Comprehension, code: COMP}
      - {name: Writing, code: WRITE}
  - name: Chinese
    code: CHI
    topics:
      - {name: Characters, code: CHAR}
      - {name: Grammar, code: GRAM}
      - {name: Comprehension, code: COMP}
      - {name: Composition, code: COMPO}
```

**Success Criteria**:
- Default subjects and topics are created on first deployment
- Centres can customize subjects and topics after initialization

---

### 9.2 Add Custom Subject

**Trigger**: Admin adds a custom subject

**Process**:
1. Admin submits subject form (name, code, description)
2. Backend validates that code is unique
3. Backend creates Subject entity (isDefault=false)
4. Backend returns success response

**Success Criteria**:
- Custom subject is created
- Subject is available for class creation

---

### 9.3 Add Custom Topic

**Trigger**: Admin adds a custom topic to a subject

**Process**:
1. Admin submits topic form (subjectId, name, code, description)
2. Backend validates that subject exists
3. Backend validates that code is unique within subject
4. Backend creates Topic entity (isDefault=false)
5. Backend returns success response

**Success Criteria**:
- Custom topic is created
- Topic is available for test score recording

---

### 9.4 Deactivate Subject or Topic

**Trigger**: Admin deactivates a subject or topic

**Process**:
1. Admin submits deactivation request (subjectId or topicId)
2. Backend validates that entity exists
3. Backend sets isActive = false
4. Backend returns success response

**Business Rules**:
- Deactivated subjects/topics are retained for historical data
- Deactivated subjects/topics are not available for new classes/tests
- Existing classes/tests with deactivated subjects/topics are unaffected

**Success Criteria**:
- Subject/topic is deactivated
- Historical data is retained

---

## 10. Batch Operations

### 10.1 Bulk Create Students via CSV

**Trigger**: Admin uploads CSV file with student data

**Process**:
1. Admin uploads CSV file (columns: email, firstName, lastName, dateOfBirth, grade)
2. Backend validates CSV format
3. Backend validates each row (email uniqueness, required fields)
4. Backend creates users in Keycloak (batch API if available)
5. Backend creates User, UserRole, Student entities for each row
6. Backend returns success response with summary (created count, failed count, errors)

**Validation Rules**:
- CSV must have required columns
- Each row must pass individual validation
- Duplicate emails within CSV are rejected
- Partial success is allowed (some rows succeed, some fail)

**Success Criteria**:
- Valid students are created
- Invalid rows are reported with error messages
- Admin receives summary of results

---

### 10.2 Bulk Enroll Students via CSV

**Trigger**: Admin uploads CSV file with enrollment data

**Process**:
1. Admin uploads CSV file (columns: studentEmail, className)
2. Backend validates CSV format
3. Backend validates each row (student exists, class exists)
4. Backend creates ClassStudent entities for each row
5. Backend returns success response with summary

**Success Criteria**:
- Valid enrollments are created
- Invalid rows are reported with error messages

---

## Summary

This business logic model defines:
- Authentication and authorization flows using Keycloak OAuth2
- User management workflows (create, deactivate, reactivate)
- Class and enrollment workflows (create, enroll, withdraw, change teacher)
- Test score recording with topic breakdown
- Progress calculation algorithms (trends, velocity)
- Notification triggering logic (immediate, event-driven)
- Report generation and storage (S3, 2-year retention)
- Feedback management (create, update, templates)
- Subject and topic management (defaults, customization)
- Batch operations (CSV import)

All workflows include validation rules, authorization checks, and error handling.

---

**Document Version**: 1.0  
**Created**: 2026-03-08  
**Status**: Draft
