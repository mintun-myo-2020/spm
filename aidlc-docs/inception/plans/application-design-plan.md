# Application Design Plan

## Overview
This plan outlines the approach for designing the high-level application architecture for the Student Progress Tracking System.

---

## Design Questions

### Question 1: Component Organization Approach
How should the backend components be organized?

A) Layered architecture - Presentation layer, Service layer, Data Access layer, with clear separation
B) Feature-based modules - Organize by feature (Authentication, Student Management, Progress Tracking, etc.)
C) Domain-driven design - Organize by business domains with bounded contexts
D) Hybrid - Layered architecture with feature-based organization within each layer
X) Other (please describe after [Answer]: tag below)

[Answer]: Feature based organization with layers in each feature

### Question 2: Service Layer Granularity
What level of granularity should the service layer have?

A) Coarse-grained services - One service per major feature area (e.g., StudentService handles all student operations)
B) Fine-grained services - Multiple specialized services per feature (e.g., StudentProfileService, StudentEnrollmentService)
C) Mixed granularity - Coarse-grained for simple features, fine-grained for complex ones
D) Domain services - Services aligned with business domains and use cases
X) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 3: Frontend Component Structure
How should the React frontend be organized?

A) Feature-based - Components organized by feature (auth/, students/, progress/, etc.)
B) Atomic design - Atoms, molecules, organisms, templates, pages hierarchy
C) Role-based - Separate component trees for each user role (teacher/, parent/, student/, admin/)
D) Hybrid - Feature-based with shared component library
X) Other (please describe after [Answer]: tag below)

[Answer]: C

### Question 4: API Design Approach
What API design approach should be used?

A) RESTful API - Resource-based endpoints following REST principles
B) GraphQL API - Single endpoint with flexible queries
C) Hybrid - REST for most operations, GraphQL for complex queries
D) RPC-style - Action-based endpoints (e.g., /recordTestScore, /generateReport)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 5: Multi-Tenancy Implementation
How should multi-tenant data isolation be implemented?

A) Separate databases per centre - Complete physical isolation
B) Shared database with tenant ID column - Logical isolation via filtering
C) Separate schemas per centre - Physical isolation within same database
D) Hybrid - Shared infrastructure with row-level security policies
X) Other (please describe after [Answer]: tag below)

[Answer]: A. we can deploy them separately in AWS so each deployment will be for each centre

### Question 6: Authentication Integration
How should Keycloak integration be structured?

A) Direct integration - Application directly calls Keycloak APIs
B) Adapter pattern - Abstraction layer between application and Keycloak
C) Spring Security integration - Use Spring Security Keycloak adapter
D) Gateway-level authentication - API Gateway handles auth, passes user context
X) Other (please describe after [Answer]: tag below)

[Answer]: X: Springboot with Spring Security and keycloak as OAuth implementation. we want the jwt stuff. clarify if this works with react frontend and how?

### Question 7: Notification Service Design
How should the notification service be structured?

A) Single notification service - Handles both email and SMS
B) Separate services - EmailService and SMSService as independent components
C) Event-driven - Publish notification events, separate handlers for email/SMS
D) Queue-based - Notification requests queued, workers process asynchronously
X) Other (please describe after [Answer]: tag below)

[Answer]: C. initiates the request but some other service does the actual handling 

### Question 8: Data Access Pattern
What data access pattern should be used?

A) Repository pattern - Repository interfaces for each entity
B) DAO pattern - Data Access Objects for database operations
C) JPA/Hibernate entities - Direct entity management with Spring Data JPA
D) Query builders - Fluent API for building queries
X) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 9: State Management (Frontend)
How should frontend state be managed?

A) React Context API - Built-in context for global state
B) Redux - Centralized state management with actions and reducers
C) React Query - Server state management with caching
D) Hybrid - React Query for server state, Context for UI state
X) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 10: Error Handling Strategy
How should errors be handled across the application?

A) Exception hierarchy - Custom exception classes with global exception handler
B) Result pattern - Return success/failure objects instead of throwing exceptions
C) Error codes - Standardized error codes with error response DTOs
D) Hybrid - Exceptions for unexpected errors, result objects for business validation
X) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Application Design Execution Plan

Once the above questions are answered, the following steps will be executed:

### Phase 1: Component Identification
- [x] Analyze requirements and user stories to identify functional areas
- [x] Define backend components (controllers, services, repositories, entities)
- [x] Define frontend components (pages, features, shared components)
- [x] Identify integration components (Keycloak adapter, notification service, etc.)
- [x] Document component purposes and responsibilities

### Phase 2: Component Methods Definition
- [x] Define method signatures for each backend service
- [x] Define API endpoints for each controller
- [x] Define props and interfaces for frontend components
- [x] Document high-level purpose of each method
- [x] Note: Detailed business rules will be defined in Functional Design stage

### Phase 3: Service Layer Design
- [x] Define service interfaces and responsibilities
- [x] Design service orchestration patterns
- [x] Identify transaction boundaries
- [x] Define service-to-service communication patterns
- [x] Document service dependencies

### Phase 4: Component Dependencies
- [x] Create dependency matrix showing component relationships
- [x] Define communication patterns (synchronous, asynchronous, event-driven)
- [x] Identify data flow between components
- [x] Document integration points with external systems
- [x] Validate no circular dependencies exist

### Phase 5: Design Validation
- [x] Verify all user stories map to components
- [x] Check component responsibilities are clear and single-purpose
- [x] Ensure proper separation of concerns
- [x] Validate scalability and maintainability
- [x] Review for potential design issues

### Phase 6: Documentation
- [x] Create components.md with component catalog
- [x] Create component-methods.md with method signatures
- [x] Create services.md with service definitions
- [x] Create component-dependency.md with dependency diagrams
- [x] Create consolidated application-design.md

---

## Component Categories (Based on Requirements)

The following component categories will be designed:

### Backend Components

1. **Authentication & Authorization**
   - Keycloak integration adapter
   - Role-based access control service
   - Session management

2. **User Management**
   - Teacher management
   - Parent management
   - Student management
   - Centre administrator management

3. **Student & Class Management**
   - Student profile service
   - Class management service
   - Enrollment service
   - Subject and topic management

4. **Test Score Management**
   - Test score recording service
   - Topic-level score service
   - Score validation service

5. **Progress Tracking**
   - Progress calculation service
   - Chart data generation service
   - Historical data service

6. **Feedback Management**
   - Teacher feedback service
   - Feedback retrieval service

7. **Reporting**
   - Report generation service
   - Data aggregation service

8. **Notifications**
   - Email notification service
   - SMS notification service
   - Notification preference service

9. **Multi-Tenancy**
   - Tenant context service
   - Data isolation service

### Frontend Components

1. **Authentication**
   - Login page
   - Logout handler
   - Protected route wrapper

2. **Teacher Dashboard**
   - Class list view
   - Student list view
   - Score entry form
   - Progress chart view

3. **Parent Dashboard**
   - Child selector
   - Progress overview
   - Test score history
   - Notification preferences

4. **Student Dashboard**
   - Personal progress view
   - Test score history
   - Feedback view

5. **Admin Dashboard**
   - Teacher management
   - Student management
   - Class management
   - Subject/topic management

6. **Shared Components**
   - Chart components
   - Form components
   - Navigation components
   - Layout components

---

## Mandatory Artifacts

The following artifacts will be generated:

1. **components.md** - Component catalog with responsibilities
2. **component-methods.md** - Method signatures and interfaces
3. **services.md** - Service layer design
4. **component-dependency.md** - Dependency relationships and communication patterns
5. **application-design.md** - Consolidated design document

---

**Instructions**: Please answer all questions above by filling in your choice (A, B, C, D, or X) after each [Answer]: tag. Once all questions are answered, I will proceed with generating the application design artifacts.
