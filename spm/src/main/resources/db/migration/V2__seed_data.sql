-- =============================================
-- V2: Seed data for development / demo
-- =============================================

-- Default subjects
INSERT INTO subjects (id, name, code, description, is_default, is_active, created_at, updated_at) VALUES
  ('a0000000-0000-0000-0000-000000000001', 'Mathematics',  'MATH',    'Core mathematics curriculum',       true, true, NOW(), NOW()),
  ('a0000000-0000-0000-0000-000000000002', 'Science',      'SCI',     'General science curriculum',        true, true, NOW(), NOW()),
  ('a0000000-0000-0000-0000-000000000003', 'English',      'ENG',     'English language and literature',   true, true, NOW(), NOW()),
  ('a0000000-0000-0000-0000-000000000004', 'History',      'HIST',    'World and local history',           true, true, NOW(), NOW()),
  ('a0000000-0000-0000-0000-000000000005', 'ICT',          'ICT',     'Information and communication technology', true, true, NOW(), NOW());

-- Default topics for Mathematics
INSERT INTO topics (id, subject_id, name, code, description, is_default, is_active, created_at, updated_at) VALUES
  ('b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001', 'Algebra',    'MATH-ALG',  'Algebraic expressions and equations', true, true, NOW(), NOW()),
  ('b0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000001', 'Geometry',   'MATH-GEO',  'Shapes, angles, and spatial reasoning', true, true, NOW(), NOW()),
  ('b0000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000001', 'Statistics', 'MATH-STAT', 'Data analysis and probability', true, true, NOW(), NOW());

-- Default topics for Science
INSERT INTO topics (id, subject_id, name, code, description, is_default, is_active, created_at, updated_at) VALUES
  ('b0000000-0000-0000-0000-000000000004', 'a0000000-0000-0000-0000-000000000002', 'Physics',   'SCI-PHY',  'Mechanics, energy, and waves', true, true, NOW(), NOW()),
  ('b0000000-0000-0000-0000-000000000005', 'a0000000-0000-0000-0000-000000000002', 'Chemistry', 'SCI-CHEM', 'Matter, reactions, and elements', true, true, NOW(), NOW()),
  ('b0000000-0000-0000-0000-000000000006', 'a0000000-0000-0000-0000-000000000002', 'Biology',   'SCI-BIO',  'Living organisms and ecosystems', true, true, NOW(), NOW());
