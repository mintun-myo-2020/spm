-- =============================================
-- V1: Complete initial schema
-- =============================================

-- Users and roles
CREATE TABLE users (
    id UUID PRIMARY KEY,
    keycloak_id VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT true,
    deactivated_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role)
);

CREATE TABLE teachers (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id),
    specialization VARCHAR(255),
    bio TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE parents (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id),
    preferred_contact_method VARCHAR(20) NOT NULL DEFAULT 'EMAIL',
    email_notifications_enabled BOOLEAN NOT NULL DEFAULT true,
    sms_notifications_enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE students (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id),
    parent_id UUID REFERENCES parents(id),
    date_of_birth DATE,
    enrollment_date DATE NOT NULL,
    grade VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE admins (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Subjects and topics
CREATE TABLE subjects (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    is_default BOOLEAN NOT NULL DEFAULT false,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE topics (
    id UUID PRIMARY KEY,
    subject_id UUID NOT NULL REFERENCES subjects(id),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    description TEXT,
    is_default BOOLEAN NOT NULL DEFAULT false,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (subject_id, code)
);

-- Classes
CREATE TABLE classes (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    subject_id UUID NOT NULL REFERENCES subjects(id),
    teacher_id UUID NOT NULL REFERENCES teachers(id),
    description TEXT,
    max_students INT NOT NULL DEFAULT 100,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE class_students (
    id UUID PRIMARY KEY,
    class_id UUID NOT NULL REFERENCES classes(id),
    student_id UUID NOT NULL REFERENCES students(id),
    enrollment_date DATE NOT NULL,
    withdrawal_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (class_id, student_id)
);

-- Test scores
CREATE TABLE test_scores (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES students(id),
    class_id UUID NOT NULL REFERENCES classes(id),
    teacher_id UUID NOT NULL REFERENCES teachers(id),
    test_name VARCHAR(255) NOT NULL,
    test_date DATE NOT NULL,
    overall_score NUMERIC(5,2) NOT NULL,
    max_score NUMERIC(5,2) NOT NULL DEFAULT 100.00,
    created_by UUID NOT NULL REFERENCES users(id),
    updated_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE questions (
    id UUID PRIMARY KEY,
    test_score_id UUID NOT NULL REFERENCES test_scores(id) ON DELETE CASCADE,
    question_number VARCHAR(20) NOT NULL,
    max_score NUMERIC(5,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE sub_questions (
    id UUID PRIMARY KEY,
    question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    sub_question_label VARCHAR(20) NOT NULL,
    score NUMERIC(5,2) NOT NULL,
    max_score NUMERIC(5,2) NOT NULL,
    topic_id UUID NOT NULL REFERENCES topics(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Feedback
CREATE TABLE feedback (
    id UUID PRIMARY KEY,
    test_score_id UUID NOT NULL REFERENCES test_scores(id),
    teacher_id UUID NOT NULL REFERENCES teachers(id),
    student_id UUID NOT NULL REFERENCES students(id),
    strengths TEXT,
    areas_for_improvement TEXT,
    recommendations TEXT,
    additional_notes TEXT,
    is_edited BOOLEAN NOT NULL DEFAULT false,
    created_by UUID NOT NULL REFERENCES users(id),
    updated_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE feedback_templates (
    id UUID PRIMARY KEY,
    teacher_id UUID REFERENCES teachers(id),
    category VARCHAR(30) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    is_system_wide BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Notifications
CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    type VARCHAR(30) NOT NULL,
    channel VARCHAR(10) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    sent_at TIMESTAMPTZ,
    failure_reason TEXT,
    related_entity_type VARCHAR(50),
    related_entity_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Progress reports
CREATE TABLE progress_reports (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES students(id),
    generated_by UUID NOT NULL REFERENCES users(id),
    report_type VARCHAR(30) NOT NULL,
    start_date DATE,
    end_date DATE,
    s3_key VARCHAR(500) NOT NULL,
    s3_bucket VARCHAR(255) NOT NULL,
    generated_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Performance indexes
CREATE INDEX idx_users_keycloak_id ON users(keycloak_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_students_parent_id ON students(parent_id);
CREATE INDEX idx_classes_teacher_id ON classes(teacher_id);
CREATE INDEX idx_classes_subject_id ON classes(subject_id);
CREATE INDEX idx_class_students_class_id ON class_students(class_id);
CREATE INDEX idx_class_students_student_id ON class_students(student_id);
CREATE INDEX idx_test_scores_student_id ON test_scores(student_id);
CREATE INDEX idx_test_scores_class_id ON test_scores(class_id);
CREATE INDEX idx_test_scores_test_date ON test_scores(test_date);
CREATE INDEX idx_feedback_student_id ON feedback(student_id);
CREATE INDEX idx_feedback_test_score_id ON feedback(test_score_id);
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_progress_reports_student_id ON progress_reports(student_id);
CREATE INDEX idx_topics_subject_id ON topics(subject_id);
CREATE INDEX idx_sub_questions_topic_id ON sub_questions(topic_id);
