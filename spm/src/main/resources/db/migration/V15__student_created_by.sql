-- V15: Add created_by to students for teacher data isolation
-- Teachers should only see students they created or students enrolled in their classes

ALTER TABLE students ADD COLUMN created_by UUID REFERENCES users(id);

-- Backfill: set created_by to the user_id of the teacher who owns the class the student is enrolled in
UPDATE students s SET created_by = (
    SELECT t.user_id FROM class_students cs
    JOIN classes c ON c.id = cs.class_id
    JOIN teachers t ON t.id = c.teacher_id
    WHERE cs.student_id = s.id
    LIMIT 1
) WHERE s.created_by IS NULL;

CREATE INDEX idx_students_created_by ON students(created_by);
