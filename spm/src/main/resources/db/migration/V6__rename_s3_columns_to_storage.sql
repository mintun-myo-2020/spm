-- Rename S3-specific column names to storage-agnostic names
ALTER TABLE test_paper_pages RENAME COLUMN s3_bucket TO storage_location;
ALTER TABLE test_paper_pages RENAME COLUMN s3_key TO storage_key;

ALTER TABLE progress_reports RENAME COLUMN s3_bucket TO storage_location;
ALTER TABLE progress_reports RENAME COLUMN s3_key TO storage_key;
