# Functional Design Clarification Questions - Backend API

## Overview
After reviewing your answers to the functional design plan, I've identified several areas that need clarification to ensure we build the system correctly.

---

## Clarification 1: Multi-Topic Questions (Q4 Follow-up)

You mentioned that each question can be multi-topic (e.g., "q1a is kinematics, q1b is dynamics"). This is important for the data model.

**How should we model this?**

A) Test has questions, each question has one or more topics with scores
   - TestScore → Question → QuestionTopicScore (questionId, topicId, score)
   
B) Test has topics directly, questions are just labels/references in the topic score
   - TestScore → TopicScore (topicId, score, questionReference like "Q1a")
   
C) Flat structure - just topic scores with optional question labels
   - TestScore → TopicScore (topicId, score, questionLabel)

[Answer]: test has questions. question has one or more sub questions. each question has a topic

**Additional details**:
- Should we track question numbers/labels (Q1a, Q1b) in the database? [Answer]: yes
- Can the same topic appear multiple times in one test (e.g., Q1a and Q3b both test kinematics)? [Answer]: yes
- If yes to above, should we sum those scores or keep them separate? [Answer]: sum 

---

## Clarification 2: Parent Account Sharing (Q2 Follow-up)

You said "parents use the same account" for multiple parents of one child. This needs clarification.

**What exactly do you mean?**

A) Both parents share one login credential (same username/password)
B) Each parent has their own account, but they're linked to the same student
C) Only one parent account per student is allowed in the system
D) Parents can have a "family account" that both can access

[Answer]: C

**If parents share credentials**:
- How do we track which parent performed actions (e.g., changed notification preferences)? [Answer]: n/a
- Should we show "logged in as Parent of [Student Name]" instead of individual parent names? [Answer]: yes

---

## Clarification 3: Teacher History Tracking (Q6 Follow-up)

You want to track teaching history, but chose "One teacher per class" (Option A).

**How should we track history if the relationship is just a foreign key?**

A) Add a TeacherClassHistory table to track past assignments (teacherId, classId, startDate, endDate)
B) Keep current teacher as foreign key, but add audit fields (previousTeacherId, teacherChangedDate)
C) Use a junction table (TeacherClass) with status and date fields to support history
D) Don't track history in the database, rely on audit logs

[Answer]: A. update this when a teacher starts and stops teaching the class.

**History tracking details**:
- Do we need to know which teacher created which test scores historically? [Answer]: no
- Should reports show "Teacher: [Name] (2024-2025)" for historical context? [Answer]: no

---

## Clarification 4: Feedback Structure (Q13 Contradiction)

You answered "yes" to both "Free-form text only?" AND "Structured feedback with categories?". These are contradictory.

**Which approach do you want?**

A) Free-form text only (single text field)
B) Structured categories only (separate fields for strengths, improvements, etc.)
C) Hybrid - structured categories + optional free-form notes
D) Flexible - teacher chooses between free-form or structured per feedback

[Answer]: C

**If structured or hybrid**:
- What categories should we have? (e.g., Strengths, Areas for Improvement, Recommendations) [Answer]:  Strengths, Areas for Improvement, Recommendations
- Are categories mandatory or optional? [Answer]: optional

---

## Clarification 5: Feedback Templates (Q13 Follow-up)

You want predefined feedback templates. How should these work?

**Template functionality**:
- Should templates be centre-specific or system-wide? [Answer]: system-wide
- Can teachers create their own templates? [Answer]: yes
- Should templates be for entire feedback or just snippets/phrases? [Answer]: snippets/phrases
- Example template format you envision: [Answer]: recommend something

---

## Clarification 6: Multi-Tenant Deployment (Q9 Follow-up)

You said "every centre is a different deployment (separate AWS account)". This changes the architecture significantly.

**Clarification needed**:
- If each centre is a separate deployment, do we still need centreId in tables? [Answer]: yes
- Should the codebase support single-tenant mode (no centreId) or keep multi-tenant structure? [Answer]: single-tenant mode
- How do we handle default subjects if they're "hardcoded" - in code or in database? [Answer]: in code under some config file somewhere

**Database constraints question**:
- You asked "is this applicable" - if each centre has its own database, then no cross-centre constraints are needed. Correct? [Answer]: yes each centre has its own db

---

## Clarification 7: Self-Registration (Q11 Confusion)

For "Should we support self-registration for any role?", you answered "admin/teacher". This doesn't make sense.

**What did you mean?**

A) Only admins and teachers can self-register (no parent/student self-registration)
B) Self-registration is not supported (only admins/teachers can create accounts for others)
C) Something else

[Answer]: B

**Account creation clarification**:
- Can parents create their own accounts? [Answer]: no, admin/teacher creates the account and parents receive it
- Can students create their own accounts? [Answer]: no, admin/teacher creates the account and parents receive it
- Or must all accounts be created by admin/teacher? [Answer]: yes

---

## Clarification 8: Admin Permissions (Q15 Surprising Answers)

You said admins CANNOT view/edit test scores or impersonate users. This is unusual for an admin role.

**What is the admin role for then?**

- What CAN admins do? [Answer]: ok, yes they can view/edit.
- Should there be a "Super Admin" role with more permissions? [Answer]: no
- Who can fix data issues if admins can't edit scores? [Answer]: they can. also teachers can

---

## Clarification 9: Pagination Defaults (Q16 Follow-up)

You said pagination defaults "depend on specific api". We need concrete defaults.

**Please specify defaults for key endpoints**:
- Student list (for teachers): Default [Answer]:  / Max [Answer]: 20 or recommend something that makes sense
- Test score list: Default [Answer]:  / Max [Answer]: 20 or recommend something that makes sense
- Class list: Default [Answer]:  / Max [Answer]: 20 or recommend something that makes sense
- Notification list: Default [Answer]:  / Max [Answer]: 20 or recommend something that makes sense

---

## Clarification 10: Report Storage (Q14 Follow-up)

You said reports should be "generated on demand and stored". 

**Storage details**:
- How long should we keep stored reports? [Answer]: 2 years
- Should we regenerate if data changes, or keep historical snapshots? [Answer]: keep snapshots. 
- Where should reports be stored (database, S3, etc.)? [Answer]: s3
- Should users be able to access previously generated reports? [Answer]: yes

---

## Summary

Please answer all clarification questions above. Once complete, I'll proceed with generating the functional design artifacts.

**Status**: Awaiting Clarifications  
**Created**: 2026-03-08
