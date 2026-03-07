# Domain Entities - Backend API

## Overview
This document defines all domain entities for the Student Progress Tracking System, including their attributes, relationships, and lifecycle states.

## Architecture Notes
- **Single-Tenant Mode**: Each tuition centre has its own deployment and database
- **No centreId**: Since each deployment serves one centre, no centreId column is needed
- **Default Subjects**: Hardcoded in configuration files (Math, Science, English, Chinese with default topics)

---

## Core Entities

### 1. User
**Purpose**: Base user entity for authentication and common user attributes

**Attributes**:
- `id` (UUID, PK): Unique identifier
- `keycloakId` (String, Unique, Not Null): Keycloak user ID for OAuth2 integration
- `email` (String, Unique, Not Null): User email address
- `firstName` (String, Not Null): User first name
- `lastName` (String, Not Null): User last name
- `phoneNumber` (String, Nullable): Contact phone number
- `isActive` (Boolean, Not Null, Default: true): Account active status
- `createdAt` (Timestamp, Not Null): Account creation timestamp
- `updatedAt` (Timestamp, Not Null): Last update timestamp
- `deactivatedAt` (Timestamp, Nullable): Deactivation timestamp

**Relationships**:
- One-to-many with UserRole (a user can have multiple roles)

**Lifecycle States**:
- `ACTIVE`: User can log in and use the system
- `DEACTIVATED`: User cannot log in, data is retained

**Business Rules**:
- Email must be unique across all users
- Keycloak ID must match the OAuth2 provider
- Deactivated users retain all historical data
- Users can be reactivated by setting isActive=true and clearing deactivatedAt

---

### 2. UserRole
**Purpose**: Junction table supporting multiple roles per user (e.g., a teacher who is also an admin)

**Attributes**:
- `id` (UUID, PK): Unique identifier
- `userId` (UUID, FK → User, Not Null): Reference to user
- `role` (Enum, Not Null): Role type (TEACHER, PARENT, STUDENT, ADMIN)
- `createdAt` (Timestamp, Not Null): Role assignment timestamp

**Relationships**:
- Many-to-one with User

**Role Types**:
- `TEACHER`: Can manage classes, record scores, view assigned students
- `PARENT`: Can view own child's progress and scores
- `STUDENT`: Can view own progress and scores
- `ADMIN`: Can manage users, classes, subjects, and view/edit all data

**Business Rules**:
- A user can have multiple roles (e.g., TEACHER + ADMIN)
- Role assignments are permanent (no deletion, only user deactivation)
- At least one ADMIN user must exist in the system

---

### 3. Teacher
**Purpose**: Teacher-specific profile information

**Attributes**:
- `id` (UUID, PK): Unique identifier
- `userId` (UUID, FK → User, Unique, Not Null): Reference to user
- `specialization` (String, Nullable): Teaching specialization (e.g., "Mathematics")
- `bio` (Text, Nullable): Teacher biography
- `createdAt` (Timestamp, Not Null): Profile creation timestamp
- `updatedAt` (Timestamp, Not Null): Last update timestamp

**Relationships**:
- One-to-one with User
- One-to-many with Class (current classes)
- One-to-many with TeacherClassHistory (historical assignments)
- One-to-many with TestScore (created scores)
- One-to-many with Feedback (created feedback)

**Business Rules**:
- Each teacher profile must link to a user with TEACHER role
- Teacher profiles are retained when user is deactivated

---

### 4. Parent
**Purpose**: Parent-specific profile information

**Attributes**:
- `id` (UUID, PK): Unique identifier
- `userId` (UUID, FK → User, Unique, Not Null): Reference to user
- `preferredContactMethod` (Enum, Not Null, Default: EMAIL): Preferred contact method
- `emailNotificationsEnabled` (Boolean, Not Null, Default: true): Email notification preference
- `smsNotificationsEnabled` (Boolean, Not Null, Default: true): SMS notification preference
- `createdAt` (Timestamp, Not Null): Profile creation timestamp
- `updatedAt` (Timestamp, Not Null): Last update timestamp

**Relationships**:
- One-to-one with User
- One-to-many with Student (children)

**Contact Methods**:
- `EMAIL`: Prefer email notifications
- `SMS`: Prefer SMS notifications
- `BOTH`: Receive both email and SMS

**Business Rules**:
- Each parent profile must link to a user with PARENT role
- One parent account per student (both parents share the same account)
- Parent profiles are retained when user is deactivated
- Display name format: "Parent of [Student Name]"

---

### 5. Student
**Purpose**: Student profile and enrollment information

**Attributes**:
- `id` (UUID, PK): Unique identifier
- `userId` (UUID, FK → User, Unique, Not Null): Reference to user
- `parentId` (UUID, FK → Parent, Not Null): Reference to parent
- `dateOfBirth` (Date, Nullable): Student date of birth
- `enrollmentDate` (Date, Not Null): Date enrolled in tuition centre
- `grade` (String, Nullable): Current grade level (e.g., "Grade 10")
- `createdAt` (Timestamp, Not Null): Profile creation timestamp
- `updatedAt` (Timestamp, Not Null): Last update timestamp

**Relationships**:
- One-to-one with User
- Many-to-one with Parent
- Many-to-many with Class (through ClassStudent)
- One-to-many with TestScore
- One-to-many with Feedback (received feedback)

**Business Rules**:
- Each student profile must link to a user with STUDENT role
- Each student must have exactly one parent account
- Student profiles are retained when user is deactivated
- Students can be enrolled in multiple classes simultaneously
- Students can be enrolled in multiple classes for the same subject

---

### 6. Admin
**Purpose**: Administrator-specific profile information

**Attributes**:
- `id` (UUID, PK): Unique identifier
- `userId` (UUID, FK → User, Unique, Not Null): Reference to user
- `createdAt` (Timestamp, Not Null): Profile creation timestamp
- `updatedAt` (Timestamp, Not Null): Last update timestamp

**Relationships**:
- One-to-one with User

**Business Rules**:
- Each admin profile must link to a user with ADMIN role
- At least one active admin must exist in the system
- Admins have full permissions to view and edit all data

---

## Academic Structure Entities

### 7. Subject
**Purpose**: Academic subjects (Math, Science, English, Chinese, custom subjects)

**Attributes**:
- `id` (UUID, PK): Unique identifier
- `name` (String, Not Null): Subject name
- `code` (String, Unique, Not Null): Subject code (e.g., "MATH", "SCI")
- `description` (Text, Nullable): Subject description
- `isDefault` (Boolean, Not Null, Default: false): Whether this is a default subject
- `isActive` (Boolean, Not Null, Default: true): Whether subject is active
- `createdAt` (Timestamp, Not Null): Creation timestamp
- `updatedAt` (Timestamp, Not Null): Last update timestamp

**Relationships**:
- One-to-many with Topic
- One-to-many with Class

**Default Subjects** (hardcoded in config):
- Mathematics (MATH)
- Science (SCI)
- English (ENG)
- Chinese (CHI)

**Business Rules**:
- Subject codes must be unique
- Default subjects are created on system initialization
- Centres can add custom subjects
- Centres can modify default subject names
- Centres can deactivate subjects (soft delete)
- Deactivated subjects are retained for historical data

---

### 8. Topic
**Purpose**: Topics within subjects (e.g., Algebra, Kinematics)

**Attributes**:
- `id` (UUID, PK): Unique identifier
- `subjectId` (UUID, FK → Subject, Not Null): Reference to subject
- `name` (String, Not Null): Topic name
- `code` (String, Not Null): Topic code (e.g., "ALG", "KIN")
- `description` (Text, Nullable): Topic description
- `isDefault` (Boolean, Not Null, Default: false): Whether this is a default topic
- `isActive` (Boolean, Not Null, Default: true): Whether topic is active
- `createdAt` (Timestamp, Not Null): Creation timestamp
- `updatedAt` (Timestamp, Not Null): Last update timestamp

**Relationships**:
- Many-to-one with Subject
- One-to-many with SubQuestionTopic

**Default Topics** (hardcoded in config per subject):
- Math: Algebra, Geometry, Calculus, Statistics
- Science: Kinematics, Dynamics, Thermodynamics, Electricity
- English: Grammar, Vocabulary, Comprehension, Writing
- Chinese: Characters, Grammar, Comprehension, Composition

**Business Rules**:
- Topic codes must be unique within a subject
- Default topics are created on system initialization
- Centres can add custom topics to any subject
- Centres can modify default topic names
- Centres can deactivate topics (soft delete)
- Deactivated topics are retained for historical data

---

### 9. Class
**Purpose**: Classes that group students and teachers

**Attributes**:
- `id` (UUID, PK): Unique identifier
- `name` (String, Not Null): Class name (e.g., "Grade 10 Math A")
- `subjectId` (UUID, FK → Subject, Not Null): Reference to subject
- `teacherId` (UUID, FK → Teacher, Not Null): Current teacher
- `description` (Text, Nullable): Class description
- `maxStudents` (Integer, Not Null, Default: 100): Maximum students allowed
- `isActive` (Boolean, Not Null, Default: true): Whether class is active
- `createdAt` (Timestamp, Not Null): Creation timestamp
- `updatedAt` (Timestamp, Not Null): Last update timestamp

**Relationships**:
- Many-to-one with Subject
- Many-to-one with Teacher (current teacher)
- Many-to-many with Student (through ClassStudent)
- One-to-many with TeacherClassHistory

**Business Rules**:
- Class must have exactly one current teacher
- Class can have 0 to maxStudents students
- Class can exist with no students
- Class cannot exist without a teacher
- Maximum students per class: 100 (configurable)
- Minimum students per class: 0
- Classes can be deactivated (soft delete)

---

### 10. ClassStudent
**Purpose**: Junction table for class enrollment with history tracking

**Attributes**:
- `id` (UUID, PK): Unique identifier
- `classId` (UUID, FK → Class, Not Null): Reference to class
- `studentId` (UUID, FK → Student, Not Null): Reference to student
- `enrollmentDate` (Date, Not Null): Date student enrolled in class
- `withdrawalDate` (Date, Nullable): Date student withdrew from class
- `status` (Enum, Not Null, Default: ACTIVE): Enrollment status
- `createdAt` (Timestamp, Not Null): Record creation timestamp
- `updatedAt` (Timestamp, Not Null): Last update timestamp

**Relationships**:
- Many-to-one with Class
- Many-to-one with Student

**Enrollment Status**:
- `ACTIVE`: Currently enrolled
- `WITHDRAWN`: Withdrawn from class
- `COMPLETED`: Class completed

**Business Rules**:
- A student can be enrolled in multiple classes simultaneously
- A student can be enrolled in multiple classes for the same subject
- Enrollment history is tracked (enrollmentDate, withdrawalDate, status)
- Students can withdraw and re-enroll in the same class
- Withdrawal date must be after enrollment date
- Future enrollments are not supported (enrollmentDate cannot be in future)

---

### 11. TeacherClassHistory
**Purpose**: Historical record of teacher-class assignments

**Attributes**:
- `id` (UUID, PK): Unique identifier
- `teacherId` (UUID, FK → Teacher, Not Null): Reference to teacher
- `classId` (UUID, FK → Class, Not Null): Reference to class
- `startDate` (Date, Not Null): Date teacher started teaching class
- `endDate` (Date, Nullable): Date teacher stopped teaching class
- `createdAt` (Timestamp, Not Null): Record creation timestamp
- `updatedAt` (Timestamp, Not Null): Last update timestamp

**Relationships**:
- Many-to-one with Teacher
- Many-to-one with Class

**Business Rules**:
- History record is created when a teacher starts teaching a class
- History record is updated with endDate when teacher stops teaching class
- End date must be after start date
- Used for audit purposes only (not displayed in reports)

---

## Assessment Entities

### 12. TestScore
**Purpose**: Overall test score for a student

**Attributes**:
- `id` (UUID, PK): Unique identifier
- `studentId` (UUID, FK → Student, Not Null): Reference to student
- `classId` (UUID, FK → Class, Not Null): Reference to class
- `teacherId` (UUID, FK → Teacher, Not Null): Teacher who recorded the score
- `testName` (String, Not Null): Name of the test
- `testDate` (Date, Not Null): Date the test was taken
- `overallScore` (Decimal(5,2), Not Null): Overall test score (0.00 to 100.00)
- `maxScore` (Decimal(5,2), Not Null, Default: 100.00): Maximum possible score
- `createdAt` (Timestamp, Not Null): Record creation timestamp
- `updatedAt` (Timestamp, Not Null): Last update timestamp
- `createdBy` (UUID, FK → User, Not Null): User who created the record
- `updatedBy` (UUID, FK → User, Not Null): User who last updated the record

**Relationships**:
- Many-to-one with Student
- Many-to-one with Class
- Many-to-one with Teacher
- One-to-many with Question
- One-to-many with Feedback

**Business Rules**:
- Overall score must be between 0.00 and 100.00
- Decimal scores are allowed (e.g., 85.50)
- Negative scores are not allowed
- Test date cannot be in the future
- Test date can be any date in the past
- Multiple tests can have the same date
- Teachers can only create scores for their own classes
- Teachers can edit scores they created
- Admins can view and edit all scores
- Score updates trigger notifications to parents and students

---

### 13. Question
**Purpose**: Individual questions within a test

**Attributes**:
- `id` (UUID, PK): Unique identifier
- `testScoreId` (UUID, FK → TestScore, Not Null): Reference to test
- `questionNumber` (String, Not Null): Question number/label (e.g., "Q1", "Q2a")
- `maxScore` (Decimal(5,2), Not Null): Maximum score for this question
- `createdAt` (Timestamp, Not Null): Record creation timestamp
- `updatedAt` (Timestamp, Not Null): Last update timestamp

**Relationships**:
- Many-to-one with TestScore
- One-to-many with SubQuestion

**Business Rules**:
- Question numbers must be unique within a test
- Question max score must be greater than 0
- Sum of all question max scores should equal test max score

---

### 14. SubQuestion
**Purpose**: Sub-questions within a question (e.g., Q1a, Q1b)

**Attributes**:
- `id` (UUID, PK): Unique identifier
- `questionId` (UUID, FK → Question, Not Null): Reference to question
- `subQuestionLabel` (String, Not Null): Sub-question label (e.g., "a", "b", "i", "ii")
- `score` (Decimal(5,2), Not Null): Score achieved for this sub-question
- `maxScore` (Decimal(5,2), Not Null): Maximum score for this sub-question
- `createdAt` (Timestamp, Not Null): Record creation timestamp
- `updatedAt` (Timestamp, Not Null): Last update timestamp

**Relationships**:
- Many-to-one with Question
- One-to-many with SubQuestionTopic

**Business Rules**:
- Sub-question labels must be unique within a question
- Score must be between 0 and maxScore
- Sum of all sub-question max scores should equal question max score
- Sum of all sub-question scores should equal question score

---

### 15. SubQuestionTopic
**Purpose**: Links sub-questions to topics (a sub-question can test one topic)

**Attributes**:
- `id` (UUID, PK): Unique identifier
- `subQuestionId` (UUID, FK → SubQuestion, Not Null): Reference to sub-question
- `topicId` (UUID, FK → Topic, Not Null): Reference to topic
- `createdAt` (Timestamp, Not Null): Record creation timestamp

**Relationships**:
- Many-to-one with SubQuestion
- Many-to-one with Topic

**Business Rules**:
- Each sub-question must have exactly one topic
- The same topic can appear multiple times in one test (different sub-questions)
- Topic scores are calculated by summing all sub-question scores for that topic

---

### 16. Feedback
**Purpose**: Teacher feedback on test performance

**Attributes**:
- `id` (UUID, PK): Unique identifier
- `testScoreId` (UUID, FK → TestScore, Not Null): Reference to test score
- `teacherId` (UUID, FK → Teacher, Not Null): Teacher who provided feedback
- `studentId` (UUID, FK → Student, Not Null): Student receiving feedback
- `strengths` (Text, Nullable): Strengths category (optional)
- `areasForImprovement` (Text, Nullable): Areas for improvement category (optional)
- `recommendations` (Text, Nullable): Recommendations category (optional)
- `additionalNotes` (Text, Nullable): Free-form additional notes (optional)
- `isEdited` (Boolean, Not Null, Default: false): Whether feedback was edited after creation
- `createdAt` (Timestamp, Not Null): Record creation timestamp
- `updatedAt` (Timestamp, Not Null): Last update timestamp
- `createdBy` (UUID, FK → User, Not Null): User who created the feedback
- `updatedBy` (UUID, FK → User, Not Null): User who last updated the feedback

**Relationships**:
- Many-to-one with TestScore
- Many-to-one with Teacher
- Many-to-one with Student

**Business Rules**:
- Feedback is optional for test scores
- All structured categories (strengths, areasForImprovement, recommendations) are optional
- Additional notes are optional
- At least one field must be provided (not all null)
- Teachers can edit feedback after submission
- Editing sets isEdited flag to true (no edit history tracking)
- Feedback is visible to students and parents
- Feedback is not visible to other teachers
- Feedback updates trigger notifications to parents and students
- Teachers can only create feedback for their own classes

---

### 17. FeedbackTemplate
**Purpose**: Reusable feedback snippets/phrases

**Attributes**:
- `id` (UUID, PK): Unique identifier
- `teacherId` (UUID, FK → Teacher, Nullable): Teacher who created template (null for system-wide)
- `category` (Enum, Not Null): Template category
- `title` (String, Not Null): Template title
- `content` (Text, Not Null): Template content/snippet
- `isSystemWide` (Boolean, Not Null, Default: false): Whether template is system-wide
- `createdAt` (Timestamp, Not Null): Record creation timestamp
- `updatedAt` (Timestamp, Not Null): Last update timestamp

**Relationships**:
- Many-to-one with Teacher (nullable for system templates)

**Template Categories**:
- `STRENGTHS`: Strengths snippets
- `IMPROVEMENTS`: Areas for improvement snippets
- `RECOMMENDATIONS`: Recommendation snippets
- `GENERAL`: General feedback snippets

**Business Rules**:
- System-wide templates have teacherId = null
- Teacher-specific templates have teacherId set
- Teachers can create their own templates
- Teachers can use system-wide templates and their own templates
- Templates are snippets/phrases, not full feedback

---

## Notification Entities

### 18. Notification
**Purpose**: Notification records for email and SMS

**Attributes**:
- `id` (UUID, PK): Unique identifier
- `userId` (UUID, FK → User, Not Null): Recipient user
- `type` (Enum, Not Null): Notification type
- `channel` (Enum, Not Null): Delivery channel (EMAIL, SMS)
- `subject` (String, Not Null): Notification subject/title
- `message` (Text, Not Null): Notification message content
- `status` (Enum, Not Null, Default: PENDING): Delivery status
- `sentAt` (Timestamp, Nullable): Timestamp when sent
- `failureReason` (Text, Nullable): Reason for delivery failure
- `relatedEntityType` (String, Nullable): Type of related entity (e.g., "TestScore", "Feedback")
- `relatedEntityId` (UUID, Nullable): ID of related entity
- `createdAt` (Timestamp, Not Null): Record creation timestamp
- `updatedAt` (Timestamp, Not Null): Last update timestamp

**Relationships**:
- Many-to-one with User

**Notification Types**:
- `NEW_TEST_SCORE`: New test score recorded
- `TEST_SCORE_UPDATED`: Test score updated/corrected
- `NEW_FEEDBACK`: New teacher feedback added
- `FEEDBACK_UPDATED`: Teacher feedback updated

**Delivery Channels**:
- `EMAIL`: Email notification
- `SMS`: SMS notification

**Delivery Status**:
- `PENDING`: Queued for delivery
- `SENT`: Successfully sent
- `FAILED`: Delivery failed

**Business Rules**:
- Notifications are triggered immediately when events occur
- Parents receive notifications for their children's scores and feedback
- Students receive notifications for their own scores and feedback
- Notification preferences are respected (email/SMS enabled flags)
- Failed notifications are retained for troubleshooting
- Notifications are sent for: new scores, score updates, new feedback, feedback updates

---

## Report Entities

### 19. ProgressReport
**Purpose**: Generated progress reports stored in S3

**Attributes**:
- `id` (UUID, PK): Unique identifier
- `studentId` (UUID, FK → Student, Not Null): Student the report is for
- `generatedBy` (UUID, FK → User, Not Null): User who generated the report
- `reportType` (Enum, Not Null): Type of report
- `startDate` (Date, Nullable): Report period start date (null for all-time)
- `endDate` (Date, Nullable): Report period end date (null for all-time)
- `s3Key` (String, Not Null): S3 object key for stored report
- `s3Bucket` (String, Not Null): S3 bucket name
- `format` (Enum, Not Null, Default: HTML): Report format
- `generatedAt` (Timestamp, Not Null): Report generation timestamp
- `expiresAt` (Timestamp, Not Null): Report expiration timestamp (2 years from generation)
- `createdAt` (Timestamp, Not Null): Record creation timestamp

**Relationships**:
- Many-to-one with Student
- Many-to-one with User (generatedBy)

**Report Types**:
- `BASIC_PROGRESS`: Basic progress report with scores and trends
- `DETAILED_PROGRESS`: Detailed report with topic breakdowns
- `CUSTOM`: Custom report with selected tests

**Report Formats**:
- `HTML`: HTML format

**Business Rules**:
- Reports are generated on-demand
- Reports are stored in S3 for 2 years
- Reports are historical snapshots (not regenerated if data changes)
- Users can access previously generated reports
- Teachers can generate reports for their students
- Parents can generate reports for their children
- Students can generate reports for themselves
- Admins can generate reports for any student
- Report includes: test scores, trends, teacher feedback, topic-level breakdown

---

## Entity Relationship Summary

### User Hierarchy
```
User (base)
├── UserRole (junction, many-to-many)
├── Teacher (one-to-one)
├── Parent (one-to-one)
├── Student (one-to-one)
└── Admin (one-to-one)
```

### Academic Structure
```
Subject
└── Topic
    └── SubQuestionTopic
        └── SubQuestion
            └── Question
                └── TestScore
```

### Class Structure
```
Class
├── Teacher (current, many-to-one)
├── TeacherClassHistory (historical, one-to-many)
└── ClassStudent (enrollment, many-to-many with Student)
```

### Assessment Structure
```
TestScore
├── Question
│   └── SubQuestion
│       └── SubQuestionTopic → Topic
└── Feedback
```

### Supporting Entities
```
FeedbackTemplate (reusable snippets)
Notification (email/SMS records)
ProgressReport (stored reports)
```

---

## Database Indexes

**Recommended indexes for performance**:
- User: `keycloakId`, `email`, `isActive`
- UserRole: `userId`, `role`
- Student: `parentId`, `userId`
- Class: `teacherId`, `subjectId`, `isActive`
- ClassStudent: `classId`, `studentId`, `status`
- TestScore: `studentId`, `classId`, `teacherId`, `testDate`
- Question: `testScoreId`
- SubQuestion: `questionId`
- SubQuestionTopic: `subQuestionId`, `topicId`
- Feedback: `testScoreId`, `studentId`, `teacherId`
- Notification: `userId`, `status`, `createdAt`
- ProgressReport: `studentId`, `generatedAt`, `expiresAt`

---

**Document Version**: 1.0  
**Created**: 2026-03-08  
**Status**: Draft
