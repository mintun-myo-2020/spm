-- Test paper uploads and pages for OCR feature
CREATE TABLE test_paper_uploads (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    test_score_id UUID REFERENCES test_scores(id),
    student_id UUID NOT NULL REFERENCES students(id),
    class_id UUID NOT NULL REFERENCES classes(id),
    status VARCHAR(25) NOT NULL DEFAULT 'UPLOADED',
    uploaded_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE test_paper_pages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    upload_id UUID NOT NULL REFERENCES test_paper_uploads(id) ON DELETE CASCADE,
    page_number INT NOT NULL,
    s3_bucket VARCHAR(255) NOT NULL,
    s3_key VARCHAR(500) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    extracted_text TEXT,
    parsed_result TEXT,
    ocr_confidence REAL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (upload_id, page_number)
);

-- Add isDraft to test_scores
ALTER TABLE test_scores ADD COLUMN is_draft BOOLEAN NOT NULL DEFAULT false;

-- Indexes
CREATE INDEX idx_test_paper_uploads_student ON test_paper_uploads(student_id);
CREATE INDEX idx_test_paper_uploads_class ON test_paper_uploads(class_id);
CREATE INDEX idx_test_paper_uploads_test_score ON test_paper_uploads(test_score_id);
CREATE INDEX idx_test_paper_pages_upload ON test_paper_pages(upload_id);
