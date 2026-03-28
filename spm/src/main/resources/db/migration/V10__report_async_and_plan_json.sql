-- Add status for async report generation and plan_json for structured plan storage
ALTER TABLE progress_reports
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    ADD COLUMN plan_json TEXT,
    ADD COLUMN error_message TEXT;

-- Make storage_key nullable (not yet known when report is IN_PROGRESS)
ALTER TABLE progress_reports ALTER COLUMN storage_key DROP NOT NULL;
ALTER TABLE progress_reports ALTER COLUMN storage_location DROP NOT NULL;
