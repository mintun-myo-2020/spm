-- V3: Add question text, question type, MCQ options, and student answers

-- Question text and type on the question level
ALTER TABLE questions ADD COLUMN question_text TEXT;
ALTER TABLE questions ADD COLUMN question_type VARCHAR(10) NOT NULL DEFAULT 'OPEN';

-- MCQ options stored as JSON on the question level (e.g. [{"key":"A","text":"Photosynthesis"},{"key":"B","text":"Respiration"}])
ALTER TABLE questions ADD COLUMN mcq_options TEXT;

-- Student answer on the sub-question level
ALTER TABLE sub_questions ADD COLUMN student_answer TEXT;
