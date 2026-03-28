-- =============================================
-- V11: Class Scheduling & Attendance
-- =============================================

CREATE TABLE class_schedules (
    id UUID PRIMARY KEY,
    class_id UUID NOT NULL REFERENCES classes(id),
    day_of_week INT,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    location VARCHAR(255),
    is_recurring BOOLEAN NOT NULL DEFAULT true,
    effective_from DATE NOT NULL,
    effective_until DATE,
    created_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE class_sessions (
    id UUID PRIMARY KEY,
    schedule_id UUID REFERENCES class_schedules(id),
    class_id UUID NOT NULL REFERENCES classes(id),
    session_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    location VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    cancel_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE session_attendance (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES class_sessions(id),
    student_id UUID NOT NULL REFERENCES students(id),
    status VARCHAR(20) NOT NULL DEFAULT 'UNMARKED',
    student_rsvp VARCHAR(20) NOT NULL DEFAULT 'ATTENDING',
    rsvp_reason TEXT,
    marked_by UUID REFERENCES users(id),
    marked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (session_id, student_id)
);

-- Performance indexes
CREATE INDEX idx_class_schedules_class_id ON class_schedules(class_id);
CREATE INDEX idx_class_sessions_class_id ON class_sessions(class_id);
CREATE INDEX idx_class_sessions_schedule_id ON class_sessions(schedule_id);
CREATE INDEX idx_class_sessions_date ON class_sessions(session_date);
CREATE INDEX idx_class_sessions_status ON class_sessions(status);
CREATE INDEX idx_session_attendance_session_id ON session_attendance(session_id);
CREATE INDEX idx_session_attendance_student_id ON session_attendance(student_id);
