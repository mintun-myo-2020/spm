# Teacher Data Isolation Analysis

## Current State

### Already Isolated (teacher sees only their own)
| Data | Endpoint | Scoping |
|------|----------|---------|
| Classes | `GET /my-classes` | `findByTeacherIdAndIsActiveTrue` |
| Class details | `GET /classes/{id}` | Verifies teacher owns class |
| Students in class | via ClassDetailDTO | Scoped through class enrollment |
| Test scores | `GET /test-scores` | Scoped through class ownership |
| Schedules | `GET /sessions/upcoming` | `findUpcomingForTeacher(teacherId)` |
| Attendance | `POST /sessions/{id}/attendance` | Verifies teacher owns class |
| Progress | `GET /classes/{id}/progress` | Verifies teacher owns class |

### NOT Isolated (teacher sees all users globally)
| Data | Endpoint | Problem |
|------|----------|---------|
| Student list | `GET /users/students` | `studentRepository.findAll()` — returns ALL students |
| Teacher list | `GET /users/teachers` | Returns ALL teachers |
| Enroll modal | Uses `GET /users/students` | Teacher sees students created by other teachers |

## Impact

In a shared instance with multiple private tutors:
- Tutor A can see Tutor B's students in the enroll dropdown (names + emails visible)
- Tutor A could enroll Tutor B's student into their own class
- Once enrolled, Tutor A can see that student's data in their class context
- Tutor A cannot see Tutor B's classes, scores, or schedules (those are properly scoped)

## Proposed Fix

### Option 1: Scope student list to teacher's own students (recommended)
When a TEACHER calls `GET /users/students`, only return students who are enrolled in that teacher's classes (or were created by that teacher).

Implementation:
- Add `created_by` column to `students` table (tracks which user created the student)
- For TEACHER role: `GET /users/students` returns students where `created_by = current_teacher_user_id` OR student is enrolled in one of the teacher's classes
- For ADMIN role: returns all students (unchanged)

### Option 2: Track student creator
Simpler — just filter by who created the student.
- Add `created_by UUID` to students table
- Teacher sees only students they created
- Problem: if admin creates a student and enrolls them in a teacher's class, the teacher can't see them in the enroll dropdown

### Option 3: Filter by class enrollment only
- Teacher sees students enrolled in any of their classes + unenrolled students they created
- Most accurate but more complex query

## Recommendation

Option 1 is the right balance. The query would be:
```sql
SELECT DISTINCT s.* FROM students s
LEFT JOIN class_students cs ON cs.student_id = s.id
LEFT JOIN classes c ON c.id = cs.class_id
WHERE s.created_by = :currentUserId
   OR c.teacher_id = :currentTeacherId
```

This ensures:
- Teacher sees students they created (even if not yet enrolled)
- Teacher sees students enrolled in their classes (even if created by admin)
- Teacher does NOT see students created by other teachers

## Effort Estimate
- Migration: add `created_by` column to students + backfill — 30 min
- Update `UserService.createStudent()` to set `created_by` — 10 min
- New repository query for teacher-scoped students — 30 min
- Update `UserController.getStudents()` to use scoped query for TEACHER role — 20 min
- Total: ~1.5 hours
