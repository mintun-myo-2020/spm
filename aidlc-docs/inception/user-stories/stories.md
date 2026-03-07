# User Stories

## Table of Contents
1. [Foundation: Authentication & Authorization](#foundation-authentication--authorization)
2. [Journey 1: Teacher Records Student Performance](#journey-1-teacher-records-student-performance)
3. [Journey 2: Parent Views Child's Progress](#journey-2-parent-views-childs-progress)
4. [Journey 3: Student Views Own Progress](#journey-3-student-views-own-progress)
5. [Journey 4: Centre Administrator Manages Centre](#journey-4-centre-administrator-manages-centre)

---

## Story Priority Legend
- **P0**: Critical - Must have for system to function
- **P1**: High - Core functionality
- **P2**: Medium - Important but not critical
- **P3**: Low - Nice to have
- **[MVP]**: Story is part of Minimum Viable Product (initial release)

---

# Foundation: Authentication & Authorization

## Story AUTH-1: User Login via Keycloak
**Priority**: P0 [MVP]  
**Persona**: All Users  
**Dependencies**: None

**Story**:  
As a user, I want to log in to the system using Keycloak authentication, so that I can securely access my account.

**Acceptance Criteria**:
- Given I am on the login page, when I click "Login", then I am redirected to Keycloak login
- Given I enter valid credentials, when I submit, then I am authenticated and redirected to my dashboard
- Given I enter invalid credentials, when I submit, then I see an error message
- Given I am authenticated, when I navigate to protected pages, then I can access them
- Given I am not authenticated, when I try to access protected pages, then I am redirected to login

---

## Story AUTH-2: Social Login Integration
**Priority**: P0 [MVP]  
**Persona**: All Users  
**Dependencies**: AUTH-1

**Story**:  
As a user, I want to log in using my Google or Facebook account, so that I don't need to create a new password.

**Acceptance Criteria**:
- Given I am on the login page, when I click "Login with Google", then I am redirected to Google authentication
- Given I am on the login page, when I click "Login with Facebook", then I am redirected to Facebook authentication
- Given I successfully authenticate with social provider, when I return to the app, then I am logged in
- Given I use social login for the first time, when I authenticate, then my account is created automatically

---

## Story AUTH-3: Role-Based Access Control
**Priority**: P0 [MVP]  
**Persona**: All Users  
**Dependencies**: AUTH-1

**Story**:  
As a system, I want to enforce role-based access control, so that users can only access features appropriate to their role.

**Acceptance Criteria**:
- Given I am a Teacher, when I log in, then I can access teacher features and my assigned classes
- Given I am a Parent, when I log in, then I can only access my own child's data
- Given I am a Student, when I log in, then I can only access my own data
- Given I am a Centre Administrator, when I log in, then I can access all centre management features
- Given I try to access unauthorized features, when I navigate to them, then I see an access denied message

---

## Story AUTH-4: User Logout
**Priority**: P0 [MVP]  
**Persona**: All Users  
**Dependencies**: AUTH-1

**Story**:  
As a user, I want to log out of the system, so that my account is secure when I'm done.

**Acceptance Criteria**:
- Given I am logged in, when I click "Logout", then I am logged out and redirected to login page
- Given I am logged out, when I try to access protected pages, then I am redirected to login
- Given I log out, when I try to use the back button, then I cannot access protected pages

---

# Journey 1: Teacher Records Student Performance

## Story TEACH-1: View My Classes
**Priority**: P1 [MVP]  
**Persona**: Teacher  
**Dependencies**: AUTH-3

**Story**:  
As a teacher, I want to view all my assigned classes, so that I can select which class to work with.

**Acceptance Criteria**:
- Given I am logged in as a teacher, when I navigate to my dashboard, then I see a list of my assigned classes
- Given I have multiple classes, when I view the list, then I see class name, subject, and student count for each
- Given I click on a class, when I select it, then I see the list of students in that class

---

## Story TEACH-2: View Students in Class
**Priority**: P1 [MVP]  
**Persona**: Teacher  
**Dependencies**: TEACH-1

**Story**:  
As a teacher, I want to view all students in a selected class, so that I can choose which student to record scores for.

**Acceptance Criteria**:
- Given I have selected a class, when I view the class details, then I see a list of all enrolled students
- Given I view the student list, when I see each student, then I see their name and recent performance summary
- Given I click on a student, when I select them, then I can view their detailed progress or record new scores

---

## Story TEACH-3: Record Test Score
**Priority**: P0 [MVP]  
**Persona**: Teacher  
**Dependencies**: TEACH-2

**Story**:  
As a teacher, I want to record a test score for a student, so that I can track their performance over time.

**Acceptance Criteria**:
- Given I have selected a student, when I click "Record Test Score", then I see a score entry form
- Given I am on the score entry form, when I enter test name, date, and overall score, then the data is saved
- Given I submit the form, when the save is successful, then I see a confirmation message
- Given I enter invalid data, when I submit, then I see validation error messages

---

## Story TEACH-4: Record Topic-Level Scores
**Priority**: P1 [MVP]  
**Persona**: Teacher  
**Dependencies**: TEACH-3

**Story**:  
As a teacher, I want to record topic-level breakdown of test scores, so that I can track which topics the student struggles with.

**Acceptance Criteria**:
- Given I am recording a test score, when I view the form, then I can add topic-level scores
- Given I select topics from the subject, when I enter scores for each topic, then the data is saved with the test
- Given I save topic-level scores, when I view the student's progress, then I see performance broken down by topic

---

## Story TEACH-5: Add Teacher Feedback
**Priority**: P1 [MVP]  
**Persona**: Teacher  
**Dependencies**: TEACH-3

**Story**:  
As a teacher, I want to add feedback notes when recording test scores, so that parents and students understand the context.

**Acceptance Criteria**:
- Given I am recording a test score, when I view the form, then I see a feedback text field
- Given I enter feedback text, when I save the test score, then the feedback is saved and associated with the test
- Given I save feedback, when parents or students view the test, then they can see my feedback

---

## Story TEACH-6: View Student Progress Charts
**Priority**: P1 [MVP]  
**Persona**: Teacher  
**Dependencies**: TEACH-3

**Story**:  
As a teacher, I want to view a student's progress charts, so that I can see their improvement trends over time.

**Acceptance Criteria**:
- Given a student has multiple test scores, when I view their profile, then I see a line chart of score trends
- Given I view the progress chart, when I look at the data, then I see test dates on X-axis and scores on Y-axis
- Given I view topic-level performance, when I select a topic, then I see that topic's score trend over time

---

# Journey 2: Parent Views Child's Progress

## Story PARENT-1: View My Children
**Priority**: P1 [MVP]  
**Persona**: Parent  
**Dependencies**: AUTH-3

**Story**:  
As a parent, I want to view my children enrolled in the tuition centre, so that I can select which child's progress to view.

**Acceptance Criteria**:
- Given I am logged in as a parent, when I navigate to my dashboard, then I see a list of my children
- Given I have multiple children, when I view the list, then I see each child's name and recent performance summary
- Given I click on a child, when I select them, then I see their detailed progress dashboard

---

## Story PARENT-2: View Child's Test Scores
**Priority**: P0 [MVP]  
**Persona**: Parent  
**Dependencies**: PARENT-1

**Story**:  
As a parent, I want to view my child's test scores, so that I can monitor their academic performance.

**Acceptance Criteria**:
- Given I have selected my child, when I view their dashboard, then I see a list of recent test scores
- Given I view test scores, when I see each test, then I see test name, date, overall score, and teacher feedback
- Given I click on a test, when I select it, then I see the detailed topic-level breakdown

---

## Story PARENT-3: View Child's Progress Charts
**Priority**: P1 [MVP]  
**Persona**: Parent  
**Dependencies**: PARENT-2

**Story**:  
As a parent, I want to view my child's progress charts, so that I can see if they are improving over time.

**Acceptance Criteria**:
- Given my child has multiple test scores, when I view their dashboard, then I see a line chart of score trends
- Given I view the progress chart, when I look at the data, then I see clear visualization of improvement or decline
- Given I view topic-level performance, when I select a topic, then I see how my child performs in that specific area

---

## Story PARENT-4: View Teacher Feedback
**Priority**: P1 [MVP]  
**Persona**: Parent  
**Dependencies**: PARENT-2

**Story**:  
As a parent, I want to view teacher feedback for my child's tests, so that I understand the teacher's assessment.

**Acceptance Criteria**:
- Given a test has teacher feedback, when I view the test details, then I see the feedback text
- Given I view my child's dashboard, when I see recent tests, then feedback is displayed alongside scores
- Given there is no feedback, when I view a test, then I see an indication that no feedback was provided

---

## Story PARENT-5: Receive Email Notifications
**Priority**: P1 [MVP]  
**Persona**: Parent  
**Dependencies**: PARENT-1

**Story**:  
As a parent, I want to receive email notifications when new test scores are recorded, so that I stay informed without checking the system constantly.

**Acceptance Criteria**:
- Given a teacher records a new test score for my child, when the score is saved, then I receive an email notification
- Given I receive an email, when I read it, then it contains the test name, score, and a link to view details
- Given a teacher adds feedback, when the feedback is saved, then I receive an email notification

---

## Story PARENT-6: Receive SMS Notifications
**Priority**: P1 [MVP]  
**Persona**: Parent  
**Dependencies**: PARENT-1

**Story**:  
As a parent, I want to receive SMS notifications for important updates, so that I am immediately informed of my child's progress.

**Acceptance Criteria**:
- Given a teacher records a new test score for my child, when the score is saved, then I receive an SMS notification
- Given I receive an SMS, when I read it, then it contains a brief summary and link to view full details
- Given I have notification preferences, when I configure them, then I can choose which events trigger SMS

---

## Story PARENT-7: Configure Notification Preferences
**Priority**: P2 [MVP]  
**Persona**: Parent  
**Dependencies**: PARENT-5, PARENT-6

**Story**:  
As a parent, I want to configure my notification preferences, so that I control how and when I receive updates.

**Acceptance Criteria**:
- Given I am logged in, when I navigate to settings, then I see notification preference options
- Given I view preferences, when I configure them, then I can enable/disable email and SMS notifications separately
- Given I save my preferences, when notifications are triggered, then they respect my settings

---

# Journey 3: Student Views Own Progress

## Story STUDENT-1: View My Test Scores
**Priority**: P1 [MVP]  
**Persona**: Student  
**Dependencies**: AUTH-3

**Story**:  
As a student, I want to view my test scores, so that I can track my own academic performance.

**Acceptance Criteria**:
- Given I am logged in as a student, when I navigate to my dashboard, then I see a list of my recent test scores
- Given I view test scores, when I see each test, then I see test name, date, overall score, and teacher feedback
- Given I click on a test, when I select it, then I see the detailed topic-level breakdown

---

## Story STUDENT-2: View My Progress Charts
**Priority**: P1 [MVP]  
**Persona**: Student  
**Dependencies**: STUDENT-1

**Story**:  
As a student, I want to view my progress charts, so that I can see if I am improving over time.

**Acceptance Criteria**:
- Given I have multiple test scores, when I view my dashboard, then I see a line chart of my score trends
- Given I view the progress chart, when I look at the data, then I can see my improvement or areas needing work
- Given I view topic-level performance, when I select a topic, then I see how I perform in that specific area

---

## Story STUDENT-3: View Teacher Feedback
**Priority**: P1 [MVP]  
**Persona**: Student  
**Dependencies**: STUDENT-1

**Story**:  
As a student, I want to view teacher feedback on my tests, so that I understand what I need to improve.

**Acceptance Criteria**:
- Given a test has teacher feedback, when I view the test details, then I see the feedback text
- Given I view my dashboard, when I see recent tests, then feedback is displayed alongside scores
- Given there is no feedback, when I view a test, then I see an indication that no feedback was provided

---

# Journey 4: Centre Administrator Manages Centre

## Story ADMIN-1: Manage Teachers
**Priority**: P2  
**Persona**: Centre Administrator  
**Dependencies**: AUTH-3

**Story**:  
As a centre administrator, I want to manage teacher accounts, so that I can control who has access to the system.

**Acceptance Criteria**:
- Given I am logged in as an administrator, when I navigate to teacher management, then I see a list of all teachers
- Given I view the teacher list, when I click "Add Teacher", then I can create a new teacher account
- Given I select a teacher, when I edit their details, then I can update their information or deactivate their account

---

## Story ADMIN-2: Manage Students
**Priority**: P2  
**Persona**: Centre Administrator  
**Dependencies**: AUTH-3

**Story**:  
As a centre administrator, I want to manage student profiles, so that I can maintain accurate student records.

**Acceptance Criteria**:
- Given I am logged in as an administrator, when I navigate to student management, then I see a list of all students
- Given I view the student list, when I click "Add Student", then I can create a new student profile
- Given I select a student, when I edit their details, then I can update their information or enrollment status

---

## Story ADMIN-3: Create and Manage Classes
**Priority**: P2  
**Persona**: Centre Administrator  
**Dependencies**: ADMIN-1, ADMIN-2

**Story**:  
As a centre administrator, I want to create and manage classes, so that I can organize students and teachers effectively.

**Acceptance Criteria**:
- Given I am logged in as an administrator, when I navigate to class management, then I see a list of all classes
- Given I view the class list, when I click "Create Class", then I can create a new class with name and subject
- Given I select a class, when I edit it, then I can assign teachers and enroll students

---

## Story ADMIN-4: Assign Students to Classes
**Priority**: P2  
**Persona**: Centre Administrator  
**Dependencies**: ADMIN-3

**Story**:  
As a centre administrator, I want to assign students to classes, so that teachers can track their progress.

**Acceptance Criteria**:
- Given I am managing a class, when I view the class details, then I can add or remove students
- Given I add a student to a class, when I save the changes, then the student appears in the teacher's class list
- Given a student is in multiple classes, when I view their profile, then I see all their class enrollments

---

## Story ADMIN-5: Manage Subjects and Topics
**Priority**: P2  
**Persona**: Centre Administrator  
**Dependencies**: AUTH-3

**Story**:  
As a centre administrator, I want to manage subjects and topics, so that teachers can use centre-specific curriculum structure.

**Acceptance Criteria**:
- Given I am logged in as an administrator, when I navigate to subject management, then I see default subjects and topics
- Given I view subjects, when I click "Add Subject", then I can create a custom subject for my centre
- Given I select a subject, when I edit it, then I can add, edit, or remove topics under that subject
- Given I edit default subjects, when I make changes, then they only affect my centre (not other centres)

---

## Story ADMIN-6: View Centre-Wide Reports
**Priority**: P3  
**Persona**: Centre Administrator  
**Dependencies**: AUTH-3

**Story**:  
As a centre administrator, I want to view centre-wide performance reports, so that I can assess overall centre effectiveness.

**Acceptance Criteria**:
- Given I am logged in as an administrator, when I navigate to reports, then I see centre-wide statistics
- Given I view reports, when I look at the data, then I see metrics like average scores, student count, and test frequency
- Given I want detailed insights, when I filter reports, then I can view data by class, subject, or time period

---

# Additional Supporting Stories

## Story DATA-1: Multi-Tenant Data Isolation
**Priority**: P0 [MVP]  
**Persona**: System  
**Dependencies**: AUTH-3

**Story**:  
As a system, I want to enforce data isolation between tuition centres, so that each centre's data remains private and secure.

**Acceptance Criteria**:
- Given I am a user from Centre A, when I access the system, then I can only see data from Centre A
- Given I try to access data from Centre B, when I make the request, then I receive an access denied error
- Given data is stored, when it is saved, then it is tagged with the correct centre identifier

---

## Story UI-1: Mobile-Responsive Design
**Priority**: P1 [MVP]  
**Persona**: All Users  
**Dependencies**: None

**Story**:  
As a user, I want the application to work on my mobile device, so that I can access it anywhere.

**Acceptance Criteria**:
- Given I access the app on a mobile browser, when I view pages, then they are properly formatted for mobile screens
- Given I use touch gestures, when I interact with the app, then buttons and links are easily tappable
- Given I view charts on mobile, when I see them, then they are readable and properly scaled

---

## Story REPORT-1: Generate Basic Progress Report
**Priority**: P2 [MVP]  
**Persona**: Teacher, Parent  
**Dependencies**: TEACH-6, PARENT-3

**Story**:  
As a teacher or parent, I want to generate a basic progress report, so that I can review comprehensive performance data.

**Acceptance Criteria**:
- Given I am viewing a student's progress, when I click "Generate Report", then I see a formatted report
- Given I view the report, when I read it, then it includes score history, trends, and teacher feedback summary
- Given I want to save the report, when I click "Download", then I can save it for my records

---

## Summary

**Total Stories**: 35
- **P0 (Critical)**: 7 stories
- **P1 (High)**: 17 stories
- **P2 (Medium)**: 9 stories
- **P3 (Low)**: 2 stories

**MVP Stories**: 27 stories marked with [MVP] tag

**Story Distribution by Journey**:
- Foundation (Authentication): 4 stories
- Journey 1 (Teacher): 6 stories
- Journey 2 (Parent): 7 stories
- Journey 3 (Student): 3 stories
- Journey 4 (Admin): 6 stories
- Supporting Stories: 9 stories

**Dependencies**: All stories include explicit dependency references to ensure proper implementation order.
