-- Sprint 4: Add structured notes fields to class_sessions
ALTER TABLE class_sessions ADD COLUMN topic_covered TEXT;
ALTER TABLE class_sessions ADD COLUMN homework_given TEXT;
ALTER TABLE class_sessions ADD COLUMN common_weaknesses TEXT;
ALTER TABLE class_sessions ADD COLUMN additional_notes TEXT;
