# Student Progress Tracking System - Requirements Document

## Intent Analysis Summary

**User Request**: Build a student progress tracking application for tuition centres based on the provided PRFAQ, focusing on core progress tracking functionality first (AI features deferred).

**Request Type**: New Project (Greenfield)

**Scope Estimate**: System-wide - Multi-component web application with authentication, data management, visualization, and reporting

**Complexity Estimate**: Moderate - Standard web application with multiple user roles, data visualization, and integration requirements

---

## Project Overview

The Student Progress Tracking System is a web-based platform designed for tuition centres in Singapore to track student performance, manage test scores, and provide parents with visibility into their child's academic progress. The initial version focuses on core progress tracking capabilities without AI-powered features.

---

## Functional Requirements

### FR-1: User Management and Authentication

**FR-1.1**: The system shall integrate with Keycloak for authentication and authorization
- Support social login (Google, Facebook) via Keycloak
- Keycloak integration required from day one

**FR-1.2**: The system shall support four user roles:
- **Teachers**: Can view and manage students in their classes, record performance, upload test scores
- **Parents**: Can view their own child's progress and data only
- **Students**: Can view their own progress and performance data
- **Centre Administrators**: Can manage teachers, students, classes, and centre-wide settings

**FR-1.3**: The system shall enforce role-based access control:
- Teachers can view all students in their assigned classes
- Parents can only view their own child's data
- Students can only view their own data
- Centre Administrators have full access to their centre's data

### FR-2: Multi-Tenancy and Centre Management

**FR-2.1**: The system shall support multiple tuition centres with isolated data
- Each centre's data is completely separated
- No cross-centre data visibility

**FR-2.2**: Centre Administrators shall be able to:
- Configure centre-specific settings
- Manage subjects and topics for their centre
- Manage teacher and student accounts

### FR-3: Student Profile Management

**FR-3.1**: Teachers and Centre Administrators shall be able to create and manage student profiles containing:
- Basic information (name, contact details)
- Parent/guardian information
- Enrollment status
- Class assignments

**FR-3.2**: Students can belong to multiple classes/groups simultaneously

### FR-4: Class and Subject Management

**FR-4.1**: The system shall provide default subjects and topics:
- Default list includes common subjects (Math, Science, English, etc.)
- Default topics organized under each subject

**FR-4.2**: Centre Administrators shall be able to:
- Add custom subjects and topics
- Edit or remove default subjects and topics
- Organize topics hierarchically under subjects

**FR-4.3**: Teachers shall be able to create and manage classes:
- Assign students to classes
- Students can be in multiple classes
- Track which subjects are taught in each class

### FR-5: Test Score Recording

**FR-5.1**: Teachers shall be able to manually record test scores for students:
- Enter test name/title
- Enter test date
- Enter overall score
- Enter topic-level breakdown of scores
- Tag which topics were covered in the test

**FR-5.2**: The system shall support one-student-at-a-time manual entry (no bulk upload in initial version)

**FR-5.3**: OCR/photo upload of test papers is deferred to future version

### FR-6: Performance Tracking

**FR-6.1**: The system shall track student performance at two levels:
- Overall test scores
- Topic-level score breakdown

**FR-6.2**: The system shall maintain historical performance data:
- All test scores with timestamps
- Topic-level performance over time
- Teacher feedback associated with each test

### FR-7: Teacher Feedback

**FR-7.1**: Teachers shall be able to add short feedback notes for each test or class session:
- Free-text feedback
- Associated with specific student and date
- Visible to parents and students

### FR-8: Progress Visualization

**FR-8.1**: The system shall provide line charts showing:
- Score trends over time for each student
- Overall test score progression
- Topic-level score progression

**FR-8.2**: Charts shall be accessible to:
- Teachers (for all students in their classes)
- Parents (for their own child only)
- Students (for their own data only)
- Centre Administrators (for all students in their centre)

### FR-9: Progress Reports

**FR-9.1**: The system shall generate basic progress reports containing:
- Test score history (tabular and chart format)
- Score trends and patterns
- Teacher feedback summary
- Topic-level performance summary

**FR-9.2**: Advanced reporting features are deferred to future versions:
- Weak topic identification (future)
- Personalized learning recommendations (future)
- AI-powered insights (future)

### FR-10: Parent Dashboard

**FR-10.1**: Parents shall have access to a dashboard showing:
- Their child's recent test scores
- Progress charts
- Teacher feedback
- Upcoming tests or assignments (if applicable)

**FR-10.2**: Parents shall be able to view historical data and trends

### FR-11: Notifications

**FR-11.1**: The system shall send notifications via email and SMS when:
- New test scores are recorded
- Teacher adds feedback
- Important updates from the centre

**FR-11.2**: Parents shall be able to configure notification preferences

### FR-12: Data Privacy

**FR-12.1**: The system shall enforce strict data isolation:
- Parents can only access their own child's data
- No cross-student data visibility for parents
- Teachers can only access students in their assigned classes

**FR-12.2**: The system shall not provide peer comparison or class average features in the initial version

---

## Non-Functional Requirements

### NFR-1: Technology Stack

**NFR-1.1**: Backend shall be built using Java with Spring Boot framework

**NFR-1.2**: Frontend shall be built using React

**NFR-1.3**: Database shall be a relational database (PostgreSQL recommended)

**NFR-1.4**: Authentication shall use Keycloak with social login integration

### NFR-2: Deployment and Infrastructure

**NFR-2.1**: The system shall be deployed on AWS cloud platform

**NFR-2.2**: The system shall be designed for medium scale:
- Support 3-10 tuition centres initially
- Support 100-1000 students initially
- Architecture should allow for future scaling

### NFR-3: Security

**NFR-3.1**: Security extension rules are NOT enforced for this project (prototype/MVP approach)

**NFR-3.2**: Basic security measures shall still be implemented:
- Secure authentication via Keycloak
- Role-based access control
- Data isolation between centres
- HTTPS for all communications

### NFR-4: Usability

**NFR-4.1**: The web application shall be mobile-responsive:
- Works on mobile browsers
- Optimized for desktop, functional on mobile
- Touch-friendly interface elements

**NFR-4.2**: The system shall be intuitive for non-technical users:
- Simple forms for data entry
- Clear navigation
- Helpful error messages

### NFR-5: Performance

**NFR-5.1**: The system shall load dashboards and charts within 3 seconds under normal load

**NFR-5.2**: The system shall support concurrent access by multiple users without degradation

### NFR-6: Data Integrity

**NFR-6.1**: All test scores and feedback shall be permanently stored

**NFR-6.2**: The system shall maintain audit logs of data modifications

### NFR-7: Maintainability

**NFR-7.1**: Code shall follow standard Java and React best practices

**NFR-7.2**: The system shall be modular to allow future feature additions:
- AI features can be added later
- OCR capabilities can be integrated
- Advanced reporting can be extended

---

## Out of Scope (Future Versions)

The following features are explicitly deferred to future versions:

1. **AI-Powered Features**:
   - Weak topic detection using ML
   - Personalized learning recommendations
   - Predictive analytics

2. **Advanced Input Methods**:
   - OCR/photo upload of test papers
   - Bulk CSV/Excel upload

3. **Advanced Reporting**:
   - Weak topic identification
   - Learning path recommendations
   - Customizable report templates
   - PDF export

4. **Additional Features**:
   - Class scheduling and attendance tracking
   - Homework management
   - Class participation scoring
   - Peer comparison and percentile rankings
   - Historical data migration tools

---

## User Stories Reference

User stories will be created in a separate stage to detail specific user interactions and acceptance criteria.

---

## Success Criteria

The initial version will be considered successful when:

1. Teachers can create student profiles and record test scores with topic breakdowns
2. Parents can log in and view their child's progress charts and feedback
3. The system supports multiple tuition centres with isolated data
4. Keycloak authentication with social login is functional
5. Email and SMS notifications are sent for new scores and feedback
6. The application is deployed on AWS and accessible via web browsers
7. Mobile-responsive design works on common mobile devices

---

## Assumptions and Constraints

**Assumptions**:
- Tuition centres have internet connectivity
- Parents and teachers have email addresses for notifications
- Mobile phone numbers are available for SMS notifications
- Users have access to modern web browsers

**Constraints**:
- Initial version focuses on core tracking only (no AI)
- Manual test score entry only (no bulk upload)
- Web application only (no native mobile apps)
- Medium scale initially (3-10 centres, 100-1000 students)

---

## Extension Configuration

| Extension | Enabled | Decided At |
|---|---|---|
| Security Baseline | No | Requirements Analysis |

---

**Document Version**: 1.0  
**Last Updated**: 2026-03-08  
**Status**: Draft - Pending Approval
