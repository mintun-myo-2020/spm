# API Contracts - Backend API

## Overview
This document defines the REST API contracts including request/response DTOs, error structures, pagination, and versioning strategy for the Student Progress Tracking System.

**Base URL**: `/api/v1`

**Authentication**: All endpoints require JWT Bearer token in Authorization header (except public endpoints)

---

## 1. Common DTOs

### 1.1 Error Response DTO

**ErrorResponseDTO**
```json
{
  "code": "string",           // Machine-readable error code
  "message": "string",         // Human-readable error message
  "details": {                 // Optional field-level details
    "field": "string",
    "value": "any",
    "reason": "string"
  },
  "timestamp": "string"        // ISO 8601 timestamp
}
```

**Error Codes**:
- `INVALID_INPUT`: Invalid request data
- `UNAUTHORIZED`: Authentication required
- `FORBIDDEN`: Insufficient permissions
- `NOT_FOUND`: Resource not found
- `CONFLICT`: Resource conflict (e.g., duplicate email)
- `INVALID_SCORE`: Score validation failure
- `INVALID_DATE`: Date validation failure
- `CLASS_FULL`: Class at maximum capacity
- `INTERNAL_ERROR`: Server error

---

### 1.2 Paged Response DTO

**PagedResponseDTO<T>**
```json
{
  "content": [],              // Array of items (type T)
  "page": 0,                  // Current page number (0-indexed)
  "size": 20,                 // Page size
  "totalElements": 100,       // Total number of items
  "totalPages": 5,            // Total number of pages
  "first": true,              // Is first page
  "last": false               // Is last page
}
```

---

### 1.3 API Response DTO

**ApiResponseDTO<T>**
```json
{
  "success": true,            // Operation success flag
  "data": {},                 // Response data (type T)
  "message": "string"         // Optional message
}
```

---

## 2. Authentication Endpoints

### 2.1 User Info

**GET** `/api/v1/auth/me`

**Description**: Get current user information

**Response**: `ApiResponseDTO<UserInfoDTO>`

**UserInfoDTO**
```json
{
  "id": "uuid",
  "email": "string",
  "firstName": "string",
  "lastName": "string",
  "roles": ["TEACHER", "ADMIN"],
  "profileId": "uuid",        // Teacher/Parent/Student/Admin profile ID
  "profileType": "TEACHER"    // TEACHER, PARENT, STUDENT, ADMIN
}
```

---

## 3. User Management Endpoints

### 3.1 Create Teacher

**POST** `/api/v1/users/teachers`

**Authorization**: ADMIN, TEACHER

**Request**: `CreateTeacherRequestDTO`
```json
{
  "email": "string",          // Required, unique
  "firstName": "string",      // Required
  "lastName": "string",       // Required
  "phoneNumber": "string",    // Optional
  "specialization": "string"  // Optional
}
```

**Response**: `ApiResponseDTO<TeacherDTO>`

**TeacherDTO**
```json
{
  "id": "uuid",
  "userId": "uuid",
  "email": "string",
  "firstName": "string",
  "lastName": "string",
  "phoneNumber": "string",
  "specialization": "string",
  "isActive": true,
  "createdAt": "string"       // ISO 8601
}
```

---

### 3.2 Create Parent

**POST** `/api/v1/users/parents`

**Authorization**: ADMIN, TEACHER

**Request**: `CreateParentRequestDTO`
```json
{
  "email": "string",          // Required, unique
  "firstName": "string",      // Required
  "lastName": "string",       // Required
  "phoneNumber": "string",    // Optional
  "studentId": "uuid"         // Required, student must exist
}
```

**Response**: `ApiResponseDTO<ParentDTO>`

**ParentDTO**
```json
{
  "id": "uuid",
  "userId": "uuid",
  "email": "string",
  "firstName": "string",
  "lastName": "string",
  "phoneNumber": "string",
  "studentId": "uuid",
  "studentName": "string",
  "preferredContactMethod": "EMAIL",
  "emailNotificationsEnabled": true,
  "smsNotificationsEnabled": true,
  "isActive": true,
  "createdAt": "string"
}
```

---

### 3.3 Create Student

**POST** `/api/v1/users/students`

**Authorization**: ADMIN, TEACHER

**Request**: `CreateStudentRequestDTO`
```json
{
  "email": "string",          // Required, unique
  "firstName": "string",      // Required
  "lastName": "string",       // Required
  "dateOfBirth": "string",    // Optional, ISO 8601 date
  "grade": "string"           // Optional
}
```

**Response**: `ApiResponseDTO<StudentDTO>`

**StudentDTO**
```json
{
  "id": "uuid",
  "userId": "uuid",
  "email": "string",
  "firstName": "string",
  "lastName": "string",
  "dateOfBirth": "string",
  "grade": "string",
  "enrollmentDate": "string",
  "parentId": "uuid",         // Null if no parent assigned yet
  "parentName": "string",
  "isActive": true,
  "createdAt": "string"
}
```

---

### 3.4 Deactivate User

**PUT** `/api/v1/users/{userId}/deactivate`

**Authorization**: ADMIN

**Response**: `ApiResponseDTO<UserDTO>`

---

### 3.5 Reactivate User

**PUT** `/api/v1/users/{userId}/reactivate`

**Authorization**: ADMIN

**Response**: `ApiResponseDTO<UserDTO>`

---

### 3.6 Bulk Create Students

**POST** `/api/v1/users/students/bulk`

**Authorization**: ADMIN

**Request**: `multipart/form-data` with CSV file

**CSV Format**:
```
email,firstName,lastName,dateOfBirth,grade
student1@example.com,John,Doe,2010-01-15,Grade 10
student2@example.com,Jane,Smith,2011-03-20,Grade 9
```

**Response**: `ApiResponseDTO<BulkOperationResultDTO>`

**BulkOperationResultDTO**
```json
{
  "totalRows": 100,
  "successCount": 95,
  "failureCount": 5,
  "errors": [
    {
      "row": 3,
      "email": "duplicate@example.com",
      "error": "Email already exists"
    }
  ]
}
```

---

## 4. Class Management Endpoints

### 4.1 Create Class

**POST** `/api/v1/classes`

**Authorization**: ADMIN

**Request**: `CreateClassRequestDTO`
```json
{
  "name": "string",           // Required
  "subjectId": "uuid",        // Required
  "teacherId": "uuid",        // Required
  "description": "string",    // Optional
  "maxStudents": 100          // Optional, default 100
}
```

**Response**: `ApiResponseDTO<ClassDTO>`

**ClassDTO**
```json
{
  "id": "uuid",
  "name": "string",
  "subjectId": "uuid",
  "subjectName": "string",
  "teacherId": "uuid",
  "teacherName": "string",
  "description": "string",
  "maxStudents": 100,
  "currentStudentCount": 0,
  "isActive": true,
  "createdAt": "string"
}
```

---

### 4.2 Get Teacher's Classes

**GET** `/api/v1/classes/my-classes`

**Authorization**: TEACHER

**Query Parameters**:
- `page` (optional, default: 0)
- `size` (optional, default: 20)

**Response**: `PagedResponseDTO<ClassDTO>`

---

### 4.3 Get Class Details

**GET** `/api/v1/classes/{classId}`

**Authorization**: TEACHER (own class), ADMIN

**Response**: `ApiResponseDTO<ClassDetailDTO>`

**ClassDetailDTO**
```json
{
  "id": "uuid",
  "name": "string",
  "subjectId": "uuid",
  "subjectName": "string",
  "teacherId": "uuid",
  "teacherName": "string",
  "description": "string",
  "maxStudents": 100,
  "currentStudentCount": 25,
  "students": [
    {
      "id": "uuid",
      "name": "string",
      "email": "string",
      "enrollmentDate": "string",
      "status": "ACTIVE"
    }
  ],
  "isActive": true,
  "createdAt": "string"
}
```

---

### 4.4 Enroll Student

**POST** `/api/v1/classes/{classId}/students`

**Authorization**: ADMIN

**Request**: `EnrollStudentRequestDTO`
```json
{
  "studentId": "uuid"         // Required
}
```

**Response**: `ApiResponseDTO<EnrollmentDTO>`

**EnrollmentDTO**
```json
{
  "id": "uuid",
  "classId": "uuid",
  "studentId": "uuid",
  "studentName": "string",
  "enrollmentDate": "string",
  "status": "ACTIVE"
}
```

---

### 4.5 Withdraw Student

**PUT** `/api/v1/classes/{classId}/students/{studentId}/withdraw`

**Authorization**: ADMIN

**Response**: `ApiResponseDTO<EnrollmentDTO>`

---

### 4.6 Change Class Teacher

**PUT** `/api/v1/classes/{classId}/teacher`

**Authorization**: ADMIN

**Request**: `ChangeTeacherRequestDTO`
```json
{
  "newTeacherId": "uuid"      // Required
}
```

**Response**: `ApiResponseDTO<ClassDTO>`

---

### 4.7 Bulk Enroll Students

**POST** `/api/v1/classes/enrollments/bulk`

**Authorization**: ADMIN

**Request**: `multipart/form-data` with CSV file

**CSV Format**:
```
studentEmail,className
student1@example.com,Grade 10 Math A
student2@example.com,Grade 9 Science B
```

**Response**: `ApiResponseDTO<BulkOperationResultDTO>`

---

## 5. Test Score Endpoints

### 5.1 Create Test Score

**POST** `/api/v1/test-scores`

**Authorization**: TEACHER (own class), ADMIN

**Request**: `CreateTestScoreRequestDTO`
```json
{
  "studentId": "uuid",        // Required
  "classId": "uuid",          // Required
  "testName": "string",       // Required
  "testDate": "string",       // Required, ISO 8601 date
  "overallScore": 85.50,      // Required, 0.00-100.00
  "maxScore": 100.00,         // Optional, default 100.00
  "questions": [
    {
      "questionNumber": "Q1",
      "maxScore": 20.00,
      "subQuestions": [
        {
          "label": "a",
          "score": 8.00,
          "maxScore": 10.00,
          "topicId": "uuid"
        },
        {
          "label": "b",
          "score": 9.00,
          "maxScore": 10.00,
          "topicId": "uuid"
        }
      ]
    }
  ]
}
```

**Response**: `ApiResponseDTO<TestScoreDTO>`

**TestScoreDTO**
```json
{
  "id": "uuid",
  "studentId": "uuid",
  "studentName": "string",
  "classId": "uuid",
  "className": "string",
  "teacherId": "uuid",
  "teacherName": "string",
  "testName": "string",
  "testDate": "string",
  "overallScore": 85.50,
  "maxScore": 100.00,
  "questions": [
    {
      "id": "uuid",
      "questionNumber": "Q1",
      "maxScore": 20.00,
      "subQuestions": [
        {
          "id": "uuid",
          "label": "a",
          "score": 8.00,
          "maxScore": 10.00,
          "topicId": "uuid",
          "topicName": "Algebra"
        }
      ]
    }
  ],
  "createdAt": "string",
  "updatedAt": "string"
}
```

---

### 5.2 Get Student Test Scores

**GET** `/api/v1/students/{studentId}/test-scores`

**Authorization**: TEACHER (own class), PARENT (own child), STUDENT (self), ADMIN

**Query Parameters**:
- `page` (optional, default: 0)
- `size` (optional, default: 20)
- `startDate` (optional, ISO 8601 date)
- `endDate` (optional, ISO 8601 date)
- `subjectId` (optional, UUID)
- `classId` (optional, UUID)
- `minScore` (optional, decimal)
- `maxScore` (optional, decimal)
- `sortBy` (optional, default: "testDate")
- `sortOrder` (optional, default: "DESC")

**Response**: `PagedResponseDTO<TestScoreDTO>`

---

### 5.3 Get Test Score Details

**GET** `/api/v1/test-scores/{testScoreId}`

**Authorization**: TEACHER (own class), PARENT (own child), STUDENT (self), ADMIN

**Response**: `ApiResponseDTO<TestScoreDetailDTO>`

**TestScoreDetailDTO** (extends TestScoreDTO with feedback)
```json
{
  ...TestScoreDTO fields,
  "feedback": {
    "id": "uuid",
    "strengths": "string",
    "areasForImprovement": "string",
    "recommendations": "string",
    "additionalNotes": "string",
    "isEdited": false,
    "createdAt": "string",
    "updatedAt": "string"
  }
}
```

---

### 5.4 Update Test Score

**PUT** `/api/v1/test-scores/{testScoreId}`

**Authorization**: TEACHER (own class), ADMIN

**Request**: `UpdateTestScoreRequestDTO` (same structure as CreateTestScoreRequestDTO)

**Response**: `ApiResponseDTO<TestScoreDTO>`

---

### 5.5 Delete Test Score

**DELETE** `/api/v1/test-scores/{testScoreId}`

**Authorization**: TEACHER (own class), ADMIN

**Response**: `ApiResponseDTO<Void>`

---

## 6. Progress Tracking Endpoints

### 6.1 Get Overall Progress

**GET** `/api/v1/students/{studentId}/progress/overall`

**Authorization**: TEACHER (own class), PARENT (own child), STUDENT (self), ADMIN

**Response**: `ApiResponseDTO<OverallProgressDTO>`

**OverallProgressDTO**
```json
{
  "studentId": "uuid",
  "studentName": "string",
  "trendData": [
    {
      "testDate": "string",
      "testName": "string",
      "score": 85.50
    }
  ],
  "averageScore": 82.30,
  "improvementVelocity": {
    "improvement": 5.20,
    "velocityPerMonth": 2.60,
    "firstAverage": 78.00,
    "recentAverage": 83.20
  }
}
```

---

### 6.2 Get Topic Progress

**GET** `/api/v1/students/{studentId}/progress/topics/{topicId}`

**Authorization**: TEACHER (own class), PARENT (own child), STUDENT (self), ADMIN

**Response**: `ApiResponseDTO<TopicProgressDTO>`

**TopicProgressDTO**
```json
{
  "studentId": "uuid",
  "topicId": "uuid",
  "topicName": "string",
  "trendData": [
    {
      "testDate": "string",
      "testName": "string",
      "topicScore": 17.00,
      "topicMaxScore": 20.00,
      "percentage": 85.00
    }
  ],
  "averagePercentage": 82.50
}
```

---

### 6.3 Get All Topics Progress

**GET** `/api/v1/students/{studentId}/progress/topics`

**Authorization**: TEACHER (own class), PARENT (own child), STUDENT (self), ADMIN

**Response**: `ApiResponseDTO<List<TopicProgressSummaryDTO>>`

**TopicProgressSummaryDTO**
```json
{
  "topicId": "uuid",
  "topicName": "string",
  "testCount": 5,
  "averagePercentage": 82.50,
  "latestPercentage": 85.00,
  "trend": "IMPROVING"         // IMPROVING, DECLINING, STABLE
}
```

---

## 7. Feedback Endpoints

### 7.1 Create Feedback

**POST** `/api/v1/test-scores/{testScoreId}/feedback`

**Authorization**: TEACHER (own class), ADMIN

**Request**: `CreateFeedbackRequestDTO`
```json
{
  "strengths": "string",              // Optional
  "areasForImprovement": "string",    // Optional
  "recommendations": "string",        // Optional
  "additionalNotes": "string"         // Optional
}
```

**Response**: `ApiResponseDTO<FeedbackDTO>`

**FeedbackDTO**
```json
{
  "id": "uuid",
  "testScoreId": "uuid",
  "teacherId": "uuid",
  "teacherName": "string",
  "studentId": "uuid",
  "strengths": "string",
  "areasForImprovement": "string",
  "recommendations": "string",
  "additionalNotes": "string",
  "isEdited": false,
  "createdAt": "string",
  "updatedAt": "string"
}
```

---

### 7.2 Update Feedback

**PUT** `/api/v1/feedback/{feedbackId}`

**Authorization**: TEACHER (own class), ADMIN

**Request**: `UpdateFeedbackRequestDTO` (same as CreateFeedbackRequestDTO)

**Response**: `ApiResponseDTO<FeedbackDTO>`

---

### 7.3 Get Feedback Templates

**GET** `/api/v1/feedback/templates`

**Authorization**: TEACHER, ADMIN

**Query Parameters**:
- `category` (optional, enum: STRENGTHS, IMPROVEMENTS, RECOMMENDATIONS, GENERAL)

**Response**: `ApiResponseDTO<List<FeedbackTemplateDTO>>`

**FeedbackTemplateDTO**
```json
{
  "id": "uuid",
  "category": "STRENGTHS",
  "title": "string",
  "content": "string",
  "isSystemWide": true,
  "teacherId": "uuid"         // Null for system-wide templates
}
```

---

### 7.4 Create Feedback Template

**POST** `/api/v1/feedback/templates`

**Authorization**: TEACHER, ADMIN

**Request**: `CreateFeedbackTemplateRequestDTO`
```json
{
  "category": "STRENGTHS",    // Required
  "title": "string",          // Required
  "content": "string"         // Required
}
```

**Response**: `ApiResponseDTO<FeedbackTemplateDTO>`

---

## 8. Report Generation Endpoints

### 8.1 Generate Progress Report

**POST** `/api/v1/students/{studentId}/reports`

**Authorization**: TEACHER (own class), PARENT (own child), STUDENT (self), ADMIN

**Request**: `GenerateReportRequestDTO`
```json
{
  "reportType": "BASIC_PROGRESS",     // Required
  "startDate": "string",              // Optional, ISO 8601 date
  "endDate": "string",                // Optional, ISO 8601 date
  "selectedTestIds": ["uuid"]         // Optional
}
```

**Response**: `ApiResponseDTO<ProgressReportDTO>`

**ProgressReportDTO**
```json
{
  "id": "uuid",
  "studentId": "uuid",
  "reportType": "BASIC_PROGRESS",
  "startDate": "string",
  "endDate": "string",
  "reportUrl": "string",              // Pre-signed S3 URL
  "generatedAt": "string",
  "expiresAt": "string"
}
```

---

### 8.2 Get Report

**GET** `/api/v1/reports/{reportId}`

**Authorization**: TEACHER (own class), PARENT (own child), STUDENT (self), ADMIN

**Response**: `ApiResponseDTO<ProgressReportDTO>`

---

### 8.3 List Student Reports

**GET** `/api/v1/students/{studentId}/reports`

**Authorization**: TEACHER (own class), PARENT (own child), STUDENT (self), ADMIN

**Query Parameters**:
- `page` (optional, default: 0)
- `size` (optional, default: 20)

**Response**: `PagedResponseDTO<ProgressReportDTO>`

---

## 9. Subject and Topic Endpoints

### 9.1 List Subjects

**GET** `/api/v1/subjects`

**Authorization**: All authenticated users

**Query Parameters**:
- `includeInactive` (optional, default: false)

**Response**: `ApiResponseDTO<List<SubjectDTO>>`

**SubjectDTO**
```json
{
  "id": "uuid",
  "name": "string",
  "code": "string",
  "description": "string",
  "isDefault": true,
  "isActive": true,
  "topicCount": 4
}
```

---

### 9.2 Get Subject with Topics

**GET** `/api/v1/subjects/{subjectId}`

**Authorization**: All authenticated users

**Response**: `ApiResponseDTO<SubjectDetailDTO>`

**SubjectDetailDTO**
```json
{
  "id": "uuid",
  "name": "string",
  "code": "string",
  "description": "string",
  "isDefault": true,
  "isActive": true,
  "topics": [
    {
      "id": "uuid",
      "name": "string",
      "code": "string",
      "description": "string",
      "isDefault": true,
      "isActive": true
    }
  ]
}
```

---

### 9.3 Create Subject

**POST** `/api/v1/subjects`

**Authorization**: ADMIN

**Request**: `CreateSubjectRequestDTO`
```json
{
  "name": "string",           // Required
  "code": "string",           // Required, unique
  "description": "string"     // Optional
}
```

**Response**: `ApiResponseDTO<SubjectDTO>`

---

### 9.4 Create Topic

**POST** `/api/v1/subjects/{subjectId}/topics`

**Authorization**: ADMIN

**Request**: `CreateTopicRequestDTO`
```json
{
  "name": "string",           // Required
  "code": "string",           // Required, unique within subject
  "description": "string"     // Optional
}
```

**Response**: `ApiResponseDTO<TopicDTO>`

---

### 9.5 Deactivate Subject

**PUT** `/api/v1/subjects/{subjectId}/deactivate`

**Authorization**: ADMIN

**Response**: `ApiResponseDTO<SubjectDTO>`

---

### 9.6 Deactivate Topic

**PUT** `/api/v1/topics/{topicId}/deactivate`

**Authorization**: ADMIN

**Response**: `ApiResponseDTO<TopicDTO>`

---

## 10. Notification Endpoints

### 10.1 Get My Notifications

**GET** `/api/v1/notifications/my-notifications`

**Authorization**: All authenticated users

**Query Parameters**:
- `page` (optional, default: 0)
- `size` (optional, default: 20)
- `status` (optional, enum: PENDING, SENT, FAILED)

**Response**: `PagedResponseDTO<NotificationDTO>`

**NotificationDTO**
```json
{
  "id": "uuid",
  "type": "NEW_TEST_SCORE",
  "channel": "EMAIL",
  "subject": "string",
  "message": "string",
  "status": "SENT",
  "sentAt": "string",
  "relatedEntityType": "TestScore",
  "relatedEntityId": "uuid",
  "createdAt": "string"
}
```

---

### 10.2 Update Notification Preferences

**PUT** `/api/v1/users/me/notification-preferences`

**Authorization**: PARENT, STUDENT

**Request**: `UpdateNotificationPreferencesRequestDTO`
```json
{
  "emailNotificationsEnabled": true,
  "smsNotificationsEnabled": true,
  "preferredContactMethod": "EMAIL"
}
```

**Response**: `ApiResponseDTO<NotificationPreferencesDTO>`

---

## 11. Pagination and Filtering

### 11.1 Pagination Query Parameters

All list endpoints support:
- `page`: Page number (0-indexed, default: 0)
- `size`: Page size (default: 20, max: 100, -1 for all)

### 11.2 Sorting Query Parameters

Sortable endpoints support:
- `sortBy`: Field name to sort by
- `sortOrder`: ASC or DESC (default varies by endpoint)

### 11.3 Filtering Query Parameters

Endpoints support specific filters as documented per endpoint.

---

## 12. API Versioning

### 12.1 Versioning Strategy

- **URL Versioning**: `/api/v1/...`, `/api/v2/...`
- **Multiple Versions**: System supports 2 versions simultaneously (current + previous)
- **Deprecation**: Old versions deprecated 6 months before removal

### 12.2 Version Headers

Optional version header for future use:
```
API-Version: 1
```

---

## 13. HTTP Status Codes

### 13.1 Success Codes

- `200 OK`: Successful GET, PUT, DELETE
- `201 Created`: Successful POST
- `204 No Content`: Successful DELETE with no response body

### 13.2 Client Error Codes

- `400 Bad Request`: Invalid input data
- `401 Unauthorized`: Authentication required
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `409 Conflict`: Resource conflict (duplicate, constraint violation)
- `410 Gone`: Resource expired (e.g., report expired)

### 13.3 Server Error Codes

- `500 Internal Server Error`: Unexpected server error
- `503 Service Unavailable`: Service temporarily unavailable

---

## 14. Rate Limiting

**MVP**: No rate limiting implemented

**Future**: Rate limiting headers
```
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1234567890
```

---

## Summary

This API contract defines:
- 50+ REST endpoints across 11 functional areas
- Comprehensive DTO structures for all requests and responses
- Standardized error response format
- Pagination and filtering contracts
- URL-based versioning strategy
- Authorization requirements per endpoint
- HTTP status code conventions

All endpoints follow RESTful principles and return JSON responses.

---

**Document Version**: 1.0  
**Created**: 2026-03-08  
**Status**: Draft
