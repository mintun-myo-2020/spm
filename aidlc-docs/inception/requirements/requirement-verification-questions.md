# Requirements Clarification Questions

Please answer the following questions to help clarify the requirements for the Student Progress Tracking System.

---

## Question 1: Security Extensions
Should security extension rules be enforced for this project?

A) Yes — enforce all SECURITY rules as blocking constraints (recommended for production-grade applications)
B) No — skip all SECURITY rules (suitable for PoCs, prototypes, and experimental projects)
X) Other (please describe after [Answer]: tag below)

[Answer]: B

---

## Question 2: Application Type
What type of application should this be?

A) Web application only (accessible via browser)
B) Mobile application only (iOS/Android native apps)
C) Both web and mobile applications
D) Progressive Web App (PWA - web app with mobile-like features)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Question 3: Technology Stack Preference
Do you have a preferred technology stack for this project?

A) Node.js with React/Next.js frontend
B) Python with Django/Flask
C) Ruby on Rails
D) Java/Spring Boot
E) No preference - recommend based on requirements
X) Other (please describe after [Answer]: tag below)

[Answer]: D with React frontend

---

## Question 4: Database Technology
What database technology should be used?

A) Relational database (PostgreSQL, MySQL)
B) NoSQL document database (MongoDB, DynamoDB)
C) Hybrid approach (relational + NoSQL)
D) No preference - recommend based on requirements
X) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Question 5: User Authentication
How should users authenticate?

A) Email and password with password reset
B) Email and password with multi-factor authentication (MFA)
C) Social login (Google, Facebook)
D) Single Sign-On (SSO) integration
X) Other (please describe after [Answer]: tag below)

[Answer]: C (KEYCLOACK PREFERRED)

---

## Question 6: User Roles
Which user roles should the system support?

A) Teachers and Parents only
B) Teachers, Parents, and Students
C) Teachers, Parents, Students, and Centre Administrators
D) Teachers, Parents, Students, Centre Administrators, and Super Admins
X) Other (please describe after [Answer]: tag below)

[Answer]: X. C

---

## Question 7: Test Score Input Method
How should teachers upload test scores?

A) Manual entry form (one student at a time)
B) Bulk upload via CSV/Excel file
C) Both manual entry and bulk upload
D) OCR/photo upload of test papers
X) Other (please describe after [Answer]: tag below)

[Answer]: A and D

---

## Question 8: Progress Chart Types
What types of progress visualizations are needed?

A) Line charts showing score trends over time
B) Bar charts comparing performance across subjects/topics
C) Both line and bar charts with multiple views
D) Advanced analytics with percentile rankings and peer comparisons
X) Other (please describe after [Answer]: tag below)

[Answer]: A 

---

## Question 9: Parent Notification System
Should parents receive notifications about their child's progress?

A) No notifications - parents check dashboard manually
B) Email notifications for new test scores and feedback
C) Email and SMS notifications
D) In-app notifications with email/SMS options
X) Other (please describe after [Answer]: tag below)

[Answer]: C

---

## Question 10: Data Privacy and Access Control
How should student data privacy be handled?

A) Parents can only view their own child's data
B) Parents can view their child's data and compare with class averages (anonymized)
C) Teachers can view all students in their classes, parents only their child
D) Role-based access with configurable permissions per centre
X) Other (please describe after [Answer]: tag below)

[Answer]: C. 

---

## Question 11: Multi-Tenancy
Should the system support multiple tuition centres?

A) Single tuition centre only
B) Multiple tuition centres with shared infrastructure (multi-tenant SaaS)
C) Multiple tuition centres with isolated data (separate deployments)
D) Start with single centre, design for multi-tenancy later
X) Other (please describe after [Answer]: tag below)

[Answer]: C

---

## Question 12: Subject and Topic Structure
How should subjects and topics be organized?

A) Predefined subject list (Math, Science, English, etc.) with fixed topics
B) Customizable subjects and topics per tuition centre
C) Hierarchical structure (Subject > Topic > Subtopic)
D) Flexible tagging system for topics
X) Other (please describe after [Answer]: tag below)

[Answer]: B but it should have some default list also

---

## Question 13: Performance Tracking Granularity
What level of detail should be tracked for student performance?

A) Overall test scores only
B) Test scores with topic-level breakdown
C) Test scores, topic-level scores, and question-level analysis
D) Comprehensive tracking including class participation, homework, and attendance
X) Other (please describe after [Answer]: tag below)

[Answer]: B

---

## Question 14: Reporting Features
What reporting capabilities are needed?

A) Basic progress reports (score history, trends)
B) Progress reports with weak topic identification
C) Comprehensive reports with recommendations and learning paths
D) Customizable report templates with export to PDF
X) Other (please describe after [Answer]: tag below)

[Answer]: A to start. leave B and C in the backlog for future implementation

---

## Question 15: Deployment Environment
Where should this application be deployed?

A) Cloud platform (AWS, Azure, GCP)
B) On-premises servers
C) Hybrid (some components cloud, some on-premises)
D) No preference - recommend based on requirements
X) Other (please describe after [Answer]: tag below)

[Answer]: AWS

---

## Question 16: Initial Scale
What is the expected initial scale of the system?

A) Small (1-2 centres, <100 students)
B) Medium (3-10 centres, 100-1000 students)
C) Large (10+ centres, 1000+ students)
D) Start small but design for large scale
X) Other (please describe after [Answer]: tag below)

[Answer]: B

---

## Question 17: AI Features Scope
You mentioned focusing on core progress tracking first. Should the initial version include ANY AI features?

A) No AI features in initial version - pure progress tracking only
B) Basic weak-topic detection using rule-based logic (no ML)
C) Simple ML-based weak-topic detection
D) Defer decision until after core features are complete
X) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Question 18: Class Management
How should classes and student enrollment be managed?

A) Simple student list per teacher
B) Class-based organization (students belong to specific classes)
C) Flexible grouping (students can be in multiple classes/groups)
D) Full scheduling system with class sessions and attendance
X) Other (please describe after [Answer]: tag below)

[Answer]: C

---

## Question 19: Historical Data Migration
Will there be existing student data to migrate?

A) No - starting fresh with new data
B) Yes - migrate from Excel spreadsheets
C) Yes - migrate from another system (specify in Other)
D) Not initially, but need to support data import later
X) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Question 20: Mobile Responsiveness
If building a web application, what level of mobile support is needed?

A) Desktop-only (no mobile optimization)
B) Mobile-responsive design (works on mobile browsers)
C) Mobile-first design (optimized for mobile, works on desktop)
D) Not applicable - building native mobile apps
X) Other (please describe after [Answer]: tag below)

[Answer]: B

---

**Instructions**: Please fill in your answer choice (A, B, C, D, E, or X) after each [Answer]: tag. If you choose X (Other), please provide additional details after the tag. Let me know when you've completed all questions.
