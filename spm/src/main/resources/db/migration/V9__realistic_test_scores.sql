-- V9: Replace seed test scores with realistic structure
-- - MCQs are 1 mark each
-- - Open-ended sub-questions vary: 2-5 marks depending on difficulty
-- - Tests do NOT all total 100
-- - Includes test_source (SCHOOL / CENTRE) and teacher_remarks
-- - Remarks reflect real teacher observations (misconceptions, careless errors, etc.)

-- ============================================================
-- STEP 1: Delete existing test data (cascade handles questions/sub_questions)
-- ============================================================
DELETE FROM sub_questions;
DELETE FROM questions;
DELETE FROM test_scores;

-- ============================================================
-- KEY:
--   Math topics: ALG=b..01, GEO=b..02, STAT=b..03, TRIG=b..04
--   Physics topics: MECH=b..07, THERM=b..08, WAVE=b..09, ELEC=b..0a
--   Math class (c1) = 60000000-...-000000000001, teacher Ms Lim (t1)
--   Physics class (c2) = 60000000-...-000000000002, teacher Mr Tan (t2)
-- ============================================================

-- ============================================================
-- MATH CLASS — GoodStudent Alice (student 01)
-- Test 1: Centre Quiz (40 marks), Test 2: School Midterm (60 marks), Test 3: Centre Quiz (35 marks)
-- ============================================================

-- Alice Test 1: Centre Math Quiz — 40 marks total
INSERT INTO test_scores (id, student_id, class_id, teacher_id, test_name, test_date, overall_score, max_score, test_source, created_by, updated_by) VALUES
  ('80000000-0000-0000-0001-000000000001', '50000000-0000-0000-0000-000000000001', '60000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001',
   'Centre Quiz 1', '2025-02-10', 34, 40, 'CENTRE', '10000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002');

-- Q1: MCQ section (4 MCQs, 1 mark each) — Algebra & Geometry
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type, mcq_options) VALUES
  ('90000000-0000-0000-0001-000000000001', '80000000-0000-0000-0001-000000000001', 'Q1', 4,
   'Answer all 4 multiple choice questions.', 'MCQ',
   '[{"key":"A","text":"x=5"},{"key":"B","text":"x=3"},{"key":"C","text":"x=7"},{"key":"D","text":"x=15"}]');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000001-0001-0000-0000-000000000001', '90000000-0000-0000-0001-000000000001', '1', 1, 1, 'b0000000-0000-0000-0000-000000000001', 'A', NULL),
  ('a0000001-0001-0000-0000-000000000002', '90000000-0000-0000-0001-000000000001', '2', 1, 1, 'b0000000-0000-0000-0000-000000000001', 'B', NULL),
  ('a0000001-0001-0000-0000-000000000003', '90000000-0000-0000-0001-000000000001', '3', 1, 1, 'b0000000-0000-0000-0000-000000000002', 'A', NULL),
  ('a0000001-0001-0000-0000-000000000004', '90000000-0000-0000-0001-000000000001', '4', 0, 1, 'b0000000-0000-0000-0000-000000000002', 'C', 'Chose obtuse angle instead of reflex — review angle types');

-- Q2: Open-ended Algebra (12 marks: 3 sub-parts)
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0001-000000000002', '80000000-0000-0000-0001-000000000001', 'Q2', 12,
   '(a) Solve 3x + 7 = 22. (b) Factorise x² + 5x + 6. (c) Solve the simultaneous equations: 2x + y = 7, x - y = 2.', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000001-0001-0000-0000-000000000005', '90000000-0000-0000-0001-000000000002', 'a', 3, 3, 'b0000000-0000-0000-0000-000000000001', '3x = 15, x = 5', NULL),
  ('a0000001-0001-0000-0000-000000000006', '90000000-0000-0000-0001-000000000002', 'b', 4, 4, 'b0000000-0000-0000-0000-000000000001', '(x+2)(x+3)', NULL),
  ('a0000001-0001-0000-0000-000000000007', '90000000-0000-0000-0001-000000000002', 'c', 4, 5, 'b0000000-0000-0000-0000-000000000001', 'x=3, y=1', 'Correct answer but did not show working for elimination step — lost 1 mark');

-- Q3: Open-ended Geometry (12 marks: 2 sub-parts)
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0001-000000000003', '80000000-0000-0000-0001-000000000001', 'Q3', 12,
   '(a) Calculate the area of a triangle with base 10cm and height 6cm. (b) Find the volume of a cylinder with radius 3cm and height 10cm.', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000001-0001-0000-0000-000000000008', '90000000-0000-0000-0001-000000000003', 'a', 4, 4, 'b0000000-0000-0000-0000-000000000002', '½ × 10 × 6 = 30 cm²', NULL),
  ('a0000001-0001-0000-0000-000000000009', '90000000-0000-0000-0001-000000000003', 'b', 7, 8, 'b0000000-0000-0000-0000-000000000002', 'V = π × 9 × 10 = 282.6 cm³', 'Correct method but rounded π to 3.14 instead of using exact value — minor precision loss');

-- Q4: Open-ended Statistics (12 marks: 3 sub-parts)
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0001-000000000004', '80000000-0000-0000-0001-000000000001', 'Q4', 12,
   '(a) Find the median of: 3, 7, 9, 12, 15. (b) Calculate the mean. (c) Draw a conclusion about the data distribution.', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000001-0001-0000-0000-00000000000a', '90000000-0000-0000-0001-000000000004', 'a', 3, 3, 'b0000000-0000-0000-0000-000000000003', 'Median = 9', NULL),
  ('a0000001-0001-0000-0000-00000000000b', '90000000-0000-0000-0001-000000000004', 'b', 3, 3, 'b0000000-0000-0000-0000-000000000003', 'Mean = 46/5 = 9.2', NULL),
  ('a0000001-0001-0000-0000-00000000000c', '90000000-0000-0000-0001-000000000004', 'c', 3, 6, 'b0000000-0000-0000-0000-000000000003', 'The data is slightly right-skewed because mean > median', 'Good observation but needs to mention the outlier effect of 15 — incomplete analysis');

-- Alice Test 2: School Midterm — 60 marks total
INSERT INTO test_scores (id, student_id, class_id, teacher_id, test_name, test_date, overall_score, max_score, test_source, created_by, updated_by) VALUES
  ('80000000-0000-0000-0001-000000000002', '50000000-0000-0000-0000-000000000001', '60000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001',
   'School Midterm', '2025-03-15', 38, 42, 'SCHOOL', '10000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002');

-- Q1: MCQ section (6 MCQs, 1 mark each) — mixed topics
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type, mcq_options) VALUES
  ('90000000-0000-0000-0001-000000000005', '80000000-0000-0000-0001-000000000002', 'Q1', 6,
   'Section A: Multiple Choice (6 marks)', 'MCQ',
   '[{"key":"A","text":"Option A"},{"key":"B","text":"Option B"},{"key":"C","text":"Option C"},{"key":"D","text":"Option D"}]');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000001-0002-0000-0000-000000000001', '90000000-0000-0000-0001-000000000005', '1', 1, 1, 'b0000000-0000-0000-0000-000000000001', 'A', NULL),
  ('a0000001-0002-0000-0000-000000000002', '90000000-0000-0000-0001-000000000005', '2', 1, 1, 'b0000000-0000-0000-0000-000000000002', 'B', NULL),
  ('a0000001-0002-0000-0000-000000000003', '90000000-0000-0000-0001-000000000005', '3', 1, 1, 'b0000000-0000-0000-0000-000000000003', 'A', NULL),
  ('a0000001-0002-0000-0000-000000000004', '90000000-0000-0000-0001-000000000005', '4', 1, 1, 'b0000000-0000-0000-0000-000000000004', 'C', NULL),
  ('a0000001-0002-0000-0000-000000000005', '90000000-0000-0000-0001-000000000005', '5', 0, 1, 'b0000000-0000-0000-0000-000000000001', 'D', 'Confused completing the square with factorisation'),
  ('a0000001-0002-0000-0000-000000000006', '90000000-0000-0000-0001-000000000005', '6', 1, 1, 'b0000000-0000-0000-0000-000000000003', 'B', NULL);

-- Q2: Open-ended Algebra + Trig (18 marks)
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0001-000000000006', '80000000-0000-0000-0001-000000000002', 'Q2', 18,
   '(a) Simplify 2(3x-4)+5x. (b) Solve x²-9=0. (c) Find sin(30°) and cos(60°). Explain why they are equal.', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000001-0002-0000-0000-000000000007', '90000000-0000-0000-0001-000000000006', 'a', 4, 4, 'b0000000-0000-0000-0000-000000000001', '6x - 8 + 5x = 11x - 8', NULL),
  ('a0000001-0002-0000-0000-000000000008', '90000000-0000-0000-0001-000000000006', 'b', 5, 5, 'b0000000-0000-0000-0000-000000000001', 'x² = 9, x = ±3', NULL),
  ('a0000001-0002-0000-0000-000000000009', '90000000-0000-0000-0001-000000000006', 'c', 7, 9, 'b0000000-0000-0000-0000-000000000004', 'sin30 = 0.5, cos60 = 0.5. They are equal.', 'Values correct but explanation is incomplete — needs to reference complementary angles (30+60=90)');

-- Q3: Open-ended Geometry + Statistics (18 marks)
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0001-000000000007', '80000000-0000-0000-0001-000000000002', 'Q3', 18,
   '(a) Find the perimeter of a rectangle 12cm × 7cm. (b) Calculate the mean of: 8, 12, 15, 9, 6. (c) A circle has diameter 14cm. Find its circumference.', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000001-0002-0000-0000-00000000000a', '90000000-0000-0000-0001-000000000007', 'a', 4, 4, 'b0000000-0000-0000-0000-000000000002', 'P = 2(12+7) = 38 cm', NULL),
  ('a0000001-0002-0000-0000-00000000000b', '90000000-0000-0000-0001-000000000007', 'b', 5, 5, 'b0000000-0000-0000-0000-000000000003', 'Mean = 50/5 = 10', NULL),
  ('a0000001-0002-0000-0000-00000000000c', '90000000-0000-0000-0001-000000000007', 'c', 8, 9, 'b0000000-0000-0000-0000-000000000002', 'C = πd = 3.14 × 14 = 43.96 cm', 'Correct approach but should state units and use π = 3.142 for better precision');

-- Alice Test 3: Centre Quiz 2 — 35 marks total
INSERT INTO test_scores (id, student_id, class_id, teacher_id, test_name, test_date, overall_score, max_score, test_source, created_by, updated_by) VALUES
  ('80000000-0000-0000-0001-000000000003', '50000000-0000-0000-0000-000000000001', '60000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001',
   'Centre Quiz 2', '2025-04-20', 30, 35, 'CENTRE', '10000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002');

-- Q1: MCQ (5 × 1 mark) — Trig & Stats
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type, mcq_options) VALUES
  ('90000000-0000-0000-0001-000000000008', '80000000-0000-0000-0001-000000000003', 'Q1', 5,
   'Section A: Multiple Choice (5 marks)', 'MCQ',
   '[{"key":"A","text":"Option A"},{"key":"B","text":"Option B"},{"key":"C","text":"Option C"},{"key":"D","text":"Option D"}]');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000001-0003-0000-0000-000000000001', '90000000-0000-0000-0001-000000000008', '1', 1, 1, 'b0000000-0000-0000-0000-000000000004', 'B', NULL),
  ('a0000001-0003-0000-0000-000000000002', '90000000-0000-0000-0001-000000000008', '2', 1, 1, 'b0000000-0000-0000-0000-000000000004', 'A', NULL),
  ('a0000001-0003-0000-0000-000000000003', '90000000-0000-0000-0001-000000000008', '3', 1, 1, 'b0000000-0000-0000-0000-000000000003', 'C', NULL),
  ('a0000001-0003-0000-0000-000000000004', '90000000-0000-0000-0001-000000000008', '4', 0, 1, 'b0000000-0000-0000-0000-000000000003', 'D', 'Confused interquartile range with range'),
  ('a0000001-0003-0000-0000-000000000005', '90000000-0000-0000-0001-000000000008', '5', 1, 1, 'b0000000-0000-0000-0000-000000000004', 'A', NULL);

-- Q2: Open-ended Algebra (15 marks)
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0001-000000000009', '80000000-0000-0000-0001-000000000003', 'Q2', 15,
   '(a) Expand and simplify (2x+3)(x-1). (b) Solve 5x - 3 = 2x + 9. (c) A rectangle has area 48cm² and width (x+2). Length is (x+6). Find x.', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000001-0003-0000-0000-000000000006', '90000000-0000-0000-0001-000000000009', 'a', 4, 4, 'b0000000-0000-0000-0000-000000000001', '2x² - 2x + 3x - 3 = 2x² + x - 3', NULL),
  ('a0000001-0003-0000-0000-000000000007', '90000000-0000-0000-0001-000000000009', 'b', 4, 4, 'b0000000-0000-0000-0000-000000000001', '3x = 12, x = 4', NULL),
  ('a0000001-0003-0000-0000-000000000008', '90000000-0000-0000-0001-000000000009', 'c', 5, 7, 'b0000000-0000-0000-0000-000000000001', '(x+2)(x+6)=48, x²+8x+12=48, x²+8x-36=0, x=4', 'Correct final answer but skipped showing the quadratic formula or factorisation step');

-- Q3: Open-ended Geometry (15 marks)
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0001-00000000000a', '80000000-0000-0000-0001-000000000003', 'Q3', 15,
   '(a) Find the area of a circle with radius 7cm. (b) A cone has radius 3cm and height 4cm. Find its slant height.', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000001-0003-0000-0000-000000000009', '90000000-0000-0000-0001-00000000000a', 'a', 5, 5, 'b0000000-0000-0000-0000-000000000002', 'A = πr² = 3.14 × 49 = 153.86 cm²', NULL),
  ('a0000001-0003-0000-0000-00000000000a', '90000000-0000-0000-0001-00000000000a', 'b', 8, 10, 'b0000000-0000-0000-0000-000000000002', 'l = √(3²+4²) = √25 = 5 cm', 'Correct! Well done applying Pythagoras to 3D shapes');

-- ============================================================
-- MATH CLASS — StrugglingStudent Ethan (student 05)
-- Test 1: Centre Quiz (40 marks), Test 2: School Midterm (60 marks), Test 3: Centre Quiz (35 marks)
-- ============================================================

-- Ethan Test 1: Centre Quiz 1 — 40 marks total, scored 16/40
INSERT INTO test_scores (id, student_id, class_id, teacher_id, test_name, test_date, overall_score, max_score, test_source, created_by, updated_by) VALUES
  ('80000000-0000-0000-0005-000000000001', '50000000-0000-0000-0000-000000000005', '60000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001',
   'Centre Quiz 1', '2025-02-10', 16, 40, 'CENTRE', '10000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002');

-- Q1: MCQ (4 × 1 mark)
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type, mcq_options) VALUES
  ('90000000-0000-0000-0005-000000000001', '80000000-0000-0000-0005-000000000001', 'Q1', 4,
   'Answer all 4 multiple choice questions.', 'MCQ',
   '[{"key":"A","text":"x=5"},{"key":"B","text":"x=3"},{"key":"C","text":"x=7"},{"key":"D","text":"x=15"}]');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000005-0001-0000-0000-000000000001', '90000000-0000-0000-0005-000000000001', '1', 1, 1, 'b0000000-0000-0000-0000-000000000001', 'A', NULL),
  ('a0000005-0001-0000-0000-000000000002', '90000000-0000-0000-0005-000000000001', '2', 0, 1, 'b0000000-0000-0000-0000-000000000001', 'D', 'Did not isolate variable — divided both sides incorrectly'),
  ('a0000005-0001-0000-0000-000000000003', '90000000-0000-0000-0005-000000000001', '3', 0, 1, 'b0000000-0000-0000-0000-000000000002', 'C', 'Confused area with perimeter formula'),
  ('a0000005-0001-0000-0000-000000000004', '90000000-0000-0000-0005-000000000001', '4', 0, 1, 'b0000000-0000-0000-0000-000000000002', 'D', 'Does not know the difference between acute and obtuse angles');

-- Q2: Open-ended Algebra (12 marks)
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0005-000000000002', '80000000-0000-0000-0005-000000000001', 'Q2', 12,
   '(a) Solve 3x + 7 = 22. (b) Factorise x² + 5x + 6. (c) Solve the simultaneous equations: 2x + y = 7, x - y = 2.', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000005-0001-0000-0000-000000000005', '90000000-0000-0000-0005-000000000002', 'a', 1, 3, 'b0000000-0000-0000-0000-000000000001', 'x = 22/3 = 7.3', 'Forgot to subtract 7 first — fundamental misconception about solving linear equations'),
  ('a0000005-0001-0000-0000-000000000006', '90000000-0000-0000-0005-000000000002', 'b', 0, 4, 'b0000000-0000-0000-0000-000000000001', 'x(x+5) + 6', 'Does not understand factorisation — just pulled x out. Needs to revise factor pairs'),
  ('a0000005-0001-0000-0000-000000000007', '90000000-0000-0000-0005-000000000002', 'c', 1, 5, 'b0000000-0000-0000-0000-000000000001', 'x = 5, y = 3', 'Attempted substitution but made arithmetic error. Got 1 mark for correct method setup');

-- Q3: Open-ended Geometry (12 marks)
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0005-000000000003', '80000000-0000-0000-0005-000000000001', 'Q3', 12,
   '(a) Calculate the area of a triangle with base 10cm and height 6cm. (b) Find the volume of a cylinder with radius 3cm and height 10cm.', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000005-0001-0000-0000-000000000008', '90000000-0000-0000-0005-000000000003', 'a', 2, 4, 'b0000000-0000-0000-0000-000000000002', '10 × 6 = 60 cm²', 'Forgot to halve — area of triangle = ½bh, not bh'),
  ('a0000005-0001-0000-0000-000000000009', '90000000-0000-0000-0005-000000000003', 'b', 3, 8, 'b0000000-0000-0000-0000-000000000002', 'V = 3 × 3 × 10 = 90', 'Used r×r×h instead of πr²h — missing π entirely. Got marks for correct r² step');

-- Q4: Open-ended Statistics (12 marks)
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0005-000000000004', '80000000-0000-0000-0005-000000000001', 'Q4', 12,
   '(a) Find the median of: 3, 7, 9, 12, 15. (b) Calculate the mean. (c) Draw a conclusion about the data distribution.', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000005-0001-0000-0000-00000000000a', '90000000-0000-0000-0005-000000000004', 'a', 3, 3, 'b0000000-0000-0000-0000-000000000003', 'Median = 9', NULL),
  ('a0000005-0001-0000-0000-00000000000b', '90000000-0000-0000-0005-000000000004', 'b', 2, 3, 'b0000000-0000-0000-0000-000000000003', 'Mean = 46/4 = 11.5', 'Divided by 4 instead of 5 — careless counting error'),
  ('a0000005-0001-0000-0000-00000000000c', '90000000-0000-0000-0005-000000000004', 'c', 3, 6, 'b0000000-0000-0000-0000-000000000003', 'The numbers go up', 'Very vague — needs to use statistical terms like skew, spread, central tendency');

-- Ethan Test 2: School Midterm — 60 marks total, scored 28/60
INSERT INTO test_scores (id, student_id, class_id, teacher_id, test_name, test_date, overall_score, max_score, test_source, created_by, updated_by) VALUES
  ('80000000-0000-0000-0005-000000000002', '50000000-0000-0000-0000-000000000005', '60000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001',
   'School Midterm', '2025-03-15', 19, 42, 'SCHOOL', '10000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002');

-- Q1: MCQ (6 × 1 mark)
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type, mcq_options) VALUES
  ('90000000-0000-0000-0005-000000000005', '80000000-0000-0000-0005-000000000002', 'Q1', 6,
   'Section A: Multiple Choice (6 marks)', 'MCQ',
   '[{"key":"A","text":"Option A"},{"key":"B","text":"Option B"},{"key":"C","text":"Option C"},{"key":"D","text":"Option D"}]');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000005-0002-0000-0000-000000000001', '90000000-0000-0000-0005-000000000005', '1', 1, 1, 'b0000000-0000-0000-0000-000000000001', 'A', NULL),
  ('a0000005-0002-0000-0000-000000000002', '90000000-0000-0000-0005-000000000005', '2', 0, 1, 'b0000000-0000-0000-0000-000000000002', 'C', 'Mixed up supplementary and complementary angles'),
  ('a0000005-0002-0000-0000-000000000003', '90000000-0000-0000-0005-000000000005', '3', 1, 1, 'b0000000-0000-0000-0000-000000000003', 'B', NULL),
  ('a0000005-0002-0000-0000-000000000004', '90000000-0000-0000-0005-000000000005', '4', 0, 1, 'b0000000-0000-0000-0000-000000000004', 'A', 'Guessed — no understanding of basic trig ratios'),
  ('a0000005-0002-0000-0000-000000000005', '90000000-0000-0000-0005-000000000005', '5', 0, 1, 'b0000000-0000-0000-0000-000000000001', 'D', NULL),
  ('a0000005-0002-0000-0000-000000000006', '90000000-0000-0000-0005-000000000005', '6', 1, 1, 'b0000000-0000-0000-0000-000000000003', 'B', NULL);

-- Q2: Open-ended Algebra + Trig (18 marks)
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0005-000000000006', '80000000-0000-0000-0005-000000000002', 'Q2', 18,
   '(a) Simplify 2(3x-4)+5x. (b) Solve x²-9=0. (c) Find sin(30°) and cos(60°). Explain why they are equal.', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000005-0002-0000-0000-000000000007', '90000000-0000-0000-0005-000000000006', 'a', 2, 4, 'b0000000-0000-0000-0000-000000000001', '6x - 4 + 5x = 11x - 4', 'Distributed the 2 to 3x but forgot to multiply -4 by 2. Common bracket expansion error'),
  ('a0000005-0002-0000-0000-000000000008', '90000000-0000-0000-0005-000000000006', 'b', 3, 5, 'b0000000-0000-0000-0000-000000000001', 'x² = 9, x = 3', 'Only found positive root — forgot x = -3. Needs to remember ± when square rooting'),
  ('a0000005-0002-0000-0000-000000000009', '90000000-0000-0000-0005-000000000006', 'c', 2, 9, 'b0000000-0000-0000-0000-000000000004', 'sin30 = 0.5. I dont know cos60.', 'Knows sin30 from memory but cannot derive cos60. No understanding of complementary angle relationship');

-- Q3: Open-ended Geometry + Statistics (18 marks)
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0005-000000000007', '80000000-0000-0000-0005-000000000002', 'Q3', 18,
   '(a) Find the perimeter of a rectangle 12cm × 7cm. (b) Calculate the mean of: 8, 12, 15, 9, 6. (c) A circle has diameter 14cm. Find its circumference.', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000005-0002-0000-0000-00000000000a', '90000000-0000-0000-0005-000000000007', 'a', 2, 4, 'b0000000-0000-0000-0000-000000000002', '12 + 7 = 19 cm', 'Only added two sides — forgot perimeter means ALL sides. P = 2(l+w)'),
  ('a0000005-0002-0000-0000-00000000000b', '90000000-0000-0000-0005-000000000007', 'b', 3, 5, 'b0000000-0000-0000-0000-000000000003', '8+12+15+9+6 = 50, mean = 50/5 = 10', NULL),
  ('a0000005-0002-0000-0000-00000000000c', '90000000-0000-0000-0005-000000000007', 'c', 4, 9, 'b0000000-0000-0000-0000-000000000002', 'C = 2 × 3.14 × 14 = 87.92', 'Used diameter as radius — C = πd, not 2πd. Doubled the answer. Careless reading of the question');

-- Ethan Test 3: Centre Quiz 2 — 35 marks total, scored 15/35
INSERT INTO test_scores (id, student_id, class_id, teacher_id, test_name, test_date, overall_score, max_score, test_source, created_by, updated_by) VALUES
  ('80000000-0000-0000-0005-000000000003', '50000000-0000-0000-0000-000000000005', '60000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001',
   'Centre Quiz 2', '2025-04-20', 12, 35, 'CENTRE', '10000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002');

-- Q1: MCQ (5 × 1 mark)
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type, mcq_options) VALUES
  ('90000000-0000-0000-0005-000000000008', '80000000-0000-0000-0005-000000000003', 'Q1', 5,
   'Section A: Multiple Choice (5 marks)', 'MCQ',
   '[{"key":"A","text":"Option A"},{"key":"B","text":"Option B"},{"key":"C","text":"Option C"},{"key":"D","text":"Option D"}]');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000005-0003-0000-0000-000000000001', '90000000-0000-0000-0005-000000000008', '1', 1, 1, 'b0000000-0000-0000-0000-000000000004', 'B', NULL),
  ('a0000005-0003-0000-0000-000000000002', '90000000-0000-0000-0005-000000000008', '2', 0, 1, 'b0000000-0000-0000-0000-000000000004', 'C', 'Confused tan with sin ratio'),
  ('a0000005-0003-0000-0000-000000000003', '90000000-0000-0000-0005-000000000008', '3', 0, 1, 'b0000000-0000-0000-0000-000000000003', 'A', 'Does not understand cumulative frequency'),
  ('a0000005-0003-0000-0000-000000000004', '90000000-0000-0000-0005-000000000008', '4', 1, 1, 'b0000000-0000-0000-0000-000000000003', 'D', NULL),
  ('a0000005-0003-0000-0000-000000000005', '90000000-0000-0000-0005-000000000008', '5', 0, 1, 'b0000000-0000-0000-0000-000000000004', 'B', NULL);

-- Q2: Open-ended Algebra (15 marks)
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0005-000000000009', '80000000-0000-0000-0005-000000000003', 'Q2', 15,
   '(a) Expand and simplify (2x+3)(x-1). (b) Solve 5x - 3 = 2x + 9. (c) A rectangle has area 48cm² and width (x+2). Length is (x+6). Find x.', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000005-0003-0000-0000-000000000006', '90000000-0000-0000-0005-000000000009', 'a', 2, 4, 'b0000000-0000-0000-0000-000000000001', '2x² + 3x - 1', 'Missing the -2x term from FOIL — only multiplied first and last terms correctly'),
  ('a0000005-0003-0000-0000-000000000007', '90000000-0000-0000-0005-000000000009', 'b', 2, 4, 'b0000000-0000-0000-0000-000000000001', '5x - 2x = 9 - 3, 3x = 6, x = 2', 'Careless sign error: moved -3 as -3 instead of +3. Should be 3x = 12, x = 4'),
  ('a0000005-0003-0000-0000-000000000008', '90000000-0000-0000-0005-000000000009', 'c', 1, 7, 'b0000000-0000-0000-0000-000000000001', 'x + 2 + x + 6 = 48, 2x + 8 = 48, x = 20', 'Added instead of multiplied — does not understand area = length × width for algebraic expressions');

-- Q3: Open-ended Geometry (15 marks)
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0005-00000000000a', '80000000-0000-0000-0005-000000000003', 'Q3', 15,
   '(a) Find the area of a circle with radius 7cm. (b) A cone has radius 3cm and height 4cm. Find its slant height.', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000005-0003-0000-0000-000000000009', '90000000-0000-0000-0005-00000000000a', 'a', 3, 5, 'b0000000-0000-0000-0000-000000000002', '2 × 3.14 × 7 = 43.96', 'Used circumference formula (2πr) instead of area formula (πr²) — needs to distinguish between the two'),
  ('a0000005-0003-0000-0000-00000000000a', '90000000-0000-0000-0005-00000000000a', 'b', 2, 10, 'b0000000-0000-0000-0000-000000000002', 'l = 3 + 4 = 7', 'Added radius and height instead of using Pythagoras theorem. Fundamental gap in understanding 3D geometry');

-- ============================================================
-- MATH CLASS — GoodStudent Ben (student 02)
-- Test 1: 33/40 Centre, Test 2: 50/60 School, Test 3: 30/35 Centre
-- ============================================================

INSERT INTO test_scores (id, student_id, class_id, teacher_id, test_name, test_date, overall_score, max_score, test_source, created_by, updated_by) VALUES
  ('80000000-0000-0000-0002-000000000001', '50000000-0000-0000-0000-000000000002', '60000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'Centre Quiz 1',  '2025-02-10', 33, 40, 'CENTRE', '10000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002'),
  ('80000000-0000-0000-0002-000000000002', '50000000-0000-0000-0000-000000000002', '60000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'School Midterm', '2025-03-15', 36, 42, 'SCHOOL', '10000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002'),
  ('80000000-0000-0000-0002-000000000003', '50000000-0000-0000-0000-000000000002', '60000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'Centre Quiz 2',  '2025-04-20', 31, 35, 'CENTRE', '10000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002');

-- Ben Test 1 questions
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type, mcq_options) VALUES
  ('90000000-0000-0000-0002-000000000001', '80000000-0000-0000-0002-000000000001', 'Q1', 4, 'MCQ Section', 'MCQ', '[{"key":"A","text":"A"},{"key":"B","text":"B"},{"key":"C","text":"C"},{"key":"D","text":"D"}]');
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0002-000000000002', '80000000-0000-0000-0002-000000000001', 'Q2', 12, '(a) Solve 3x+7=22. (b) Factorise x²+5x+6. (c) Simultaneous equations.', 'OPEN'),
  ('90000000-0000-0000-0002-000000000003', '80000000-0000-0000-0002-000000000001', 'Q3', 12, '(a) Triangle area. (b) Cylinder volume.', 'OPEN'),
  ('90000000-0000-0000-0002-000000000004', '80000000-0000-0000-0002-000000000001', 'Q4', 12, '(a) Median. (b) Mean. (c) Data conclusion.', 'OPEN');

INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000002-0001-0000-0000-000000000001', '90000000-0000-0000-0002-000000000001', '1', 1, 1, 'b0000000-0000-0000-0000-000000000001', 'A', NULL),
  ('a0000002-0001-0000-0000-000000000002', '90000000-0000-0000-0002-000000000001', '2', 1, 1, 'b0000000-0000-0000-0000-000000000001', 'B', NULL),
  ('a0000002-0001-0000-0000-000000000003', '90000000-0000-0000-0002-000000000001', '3', 1, 1, 'b0000000-0000-0000-0000-000000000002', 'A', NULL),
  ('a0000002-0001-0000-0000-000000000004', '90000000-0000-0000-0002-000000000001', '4', 1, 1, 'b0000000-0000-0000-0000-000000000002', 'B', NULL),
  ('a0000002-0001-0000-0000-000000000005', '90000000-0000-0000-0002-000000000002', 'a', 3, 3, 'b0000000-0000-0000-0000-000000000001', '3x=15, x=5', NULL),
  ('a0000002-0001-0000-0000-000000000006', '90000000-0000-0000-0002-000000000002', 'b', 4, 4, 'b0000000-0000-0000-0000-000000000001', '(x+2)(x+3)', NULL),
  ('a0000002-0001-0000-0000-000000000007', '90000000-0000-0000-0002-000000000002', 'c', 3, 5, 'b0000000-0000-0000-0000-000000000001', 'x=3, y=1', 'Correct but working is messy — hard to follow the elimination steps'),
  ('a0000002-0001-0000-0000-000000000008', '90000000-0000-0000-0002-000000000003', 'a', 4, 4, 'b0000000-0000-0000-0000-000000000002', '½ × 10 × 6 = 30 cm²', NULL),
  ('a0000002-0001-0000-0000-000000000009', '90000000-0000-0000-0002-000000000003', 'b', 6, 8, 'b0000000-0000-0000-0000-000000000002', 'V = π × 9 × 10 = 282.7', 'Correct method, minor rounding difference'),
  ('a0000002-0001-0000-0000-00000000000a', '90000000-0000-0000-0002-000000000004', 'a', 3, 3, 'b0000000-0000-0000-0000-000000000003', 'Median = 9', NULL),
  ('a0000002-0001-0000-0000-00000000000b', '90000000-0000-0000-0002-000000000004', 'b', 3, 3, 'b0000000-0000-0000-0000-000000000003', 'Mean = 9.2', NULL),
  ('a0000002-0001-0000-0000-00000000000c', '90000000-0000-0000-0002-000000000004', 'c', 3, 6, 'b0000000-0000-0000-0000-000000000003', 'Data is spread out, mean close to median so roughly symmetric', 'Reasonable but should mention specific values and range');

-- Ben Test 2 questions
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type, mcq_options) VALUES
  ('90000000-0000-0000-0002-000000000005', '80000000-0000-0000-0002-000000000002', 'Q1', 6, 'MCQ Section A', 'MCQ', '[{"key":"A","text":"A"},{"key":"B","text":"B"},{"key":"C","text":"C"},{"key":"D","text":"D"}]');
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0002-000000000006', '80000000-0000-0000-0002-000000000002', 'Q2', 18, '(a) Simplify. (b) Solve quadratic. (c) Trig identities.', 'OPEN'),
  ('90000000-0000-0000-0002-000000000007', '80000000-0000-0000-0002-000000000002', 'Q3', 18, '(a) Perimeter. (b) Mean. (c) Circumference.', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000002-0002-0000-0000-000000000001', '90000000-0000-0000-0002-000000000005', '1', 1, 1, 'b0000000-0000-0000-0000-000000000001', 'A', NULL),
  ('a0000002-0002-0000-0000-000000000002', '90000000-0000-0000-0002-000000000005', '2', 1, 1, 'b0000000-0000-0000-0000-000000000002', 'B', NULL),
  ('a0000002-0002-0000-0000-000000000003', '90000000-0000-0000-0002-000000000005', '3', 1, 1, 'b0000000-0000-0000-0000-000000000003', 'A', NULL),
  ('a0000002-0002-0000-0000-000000000004', '90000000-0000-0000-0002-000000000005', '4', 0, 1, 'b0000000-0000-0000-0000-000000000004', 'C', 'Mixed up sin and cos for standard angles'),
  ('a0000002-0002-0000-0000-000000000005', '90000000-0000-0000-0002-000000000005', '5', 1, 1, 'b0000000-0000-0000-0000-000000000001', 'B', NULL),
  ('a0000002-0002-0000-0000-000000000006', '90000000-0000-0000-0002-000000000005', '6', 1, 1, 'b0000000-0000-0000-0000-000000000003', 'A', NULL),
  ('a0000002-0002-0000-0000-000000000007', '90000000-0000-0000-0002-000000000006', 'a', 4, 4, 'b0000000-0000-0000-0000-000000000001', '11x - 8', NULL),
  ('a0000002-0002-0000-0000-000000000008', '90000000-0000-0000-0002-000000000006', 'b', 5, 5, 'b0000000-0000-0000-0000-000000000001', 'x = ±3', NULL),
  ('a0000002-0002-0000-0000-000000000009', '90000000-0000-0000-0002-000000000006', 'c', 6, 9, 'b0000000-0000-0000-0000-000000000004', 'sin30=0.5, cos60=0.5. Equal because complementary.', 'Good — could elaborate more on the unit circle relationship'),
  ('a0000002-0002-0000-0000-00000000000a', '90000000-0000-0000-0002-000000000007', 'a', 4, 4, 'b0000000-0000-0000-0000-000000000002', '2(12+7) = 38 cm', NULL),
  ('a0000002-0002-0000-0000-00000000000b', '90000000-0000-0000-0002-000000000007', 'b', 5, 5, 'b0000000-0000-0000-0000-000000000003', 'Mean = 10', NULL),
  ('a0000002-0002-0000-0000-00000000000c', '90000000-0000-0000-0002-000000000007', 'c', 7, 9, 'b0000000-0000-0000-0000-000000000002', 'C = πd = 43.96 cm', 'Correct but forgot to state d=14 explicitly in working');

-- Ben Test 3 questions
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type, mcq_options) VALUES
  ('90000000-0000-0000-0002-000000000008', '80000000-0000-0000-0002-000000000003', 'Q1', 5, 'MCQ Section', 'MCQ', '[{"key":"A","text":"A"},{"key":"B","text":"B"},{"key":"C","text":"C"},{"key":"D","text":"D"}]');
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0002-000000000009', '80000000-0000-0000-0002-000000000003', 'Q2', 15, '(a) Expand. (b) Solve linear. (c) Area problem.', 'OPEN'),
  ('90000000-0000-0000-0002-00000000000a', '80000000-0000-0000-0002-000000000003', 'Q3', 15, '(a) Circle area. (b) Cone slant height.', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000002-0003-0000-0000-000000000001', '90000000-0000-0000-0002-000000000008', '1', 1, 1, 'b0000000-0000-0000-0000-000000000004', 'B', NULL),
  ('a0000002-0003-0000-0000-000000000002', '90000000-0000-0000-0002-000000000008', '2', 1, 1, 'b0000000-0000-0000-0000-000000000004', 'A', NULL),
  ('a0000002-0003-0000-0000-000000000003', '90000000-0000-0000-0002-000000000008', '3', 1, 1, 'b0000000-0000-0000-0000-000000000003', 'C', NULL),
  ('a0000002-0003-0000-0000-000000000004', '90000000-0000-0000-0002-000000000008', '4', 1, 1, 'b0000000-0000-0000-0000-000000000003', 'D', NULL),
  ('a0000002-0003-0000-0000-000000000005', '90000000-0000-0000-0002-000000000008', '5', 1, 1, 'b0000000-0000-0000-0000-000000000004', 'A', NULL),
  ('a0000002-0003-0000-0000-000000000006', '90000000-0000-0000-0002-000000000009', 'a', 4, 4, 'b0000000-0000-0000-0000-000000000001', '2x²+x-3', NULL),
  ('a0000002-0003-0000-0000-000000000007', '90000000-0000-0000-0002-000000000009', 'b', 4, 4, 'b0000000-0000-0000-0000-000000000001', 'x = 4', NULL),
  ('a0000002-0003-0000-0000-000000000008', '90000000-0000-0000-0002-000000000009', 'c', 5, 7, 'b0000000-0000-0000-0000-000000000001', 'x²+8x+12=48, x=4', 'Correct answer, showed quadratic setup but skipped factoring step'),
  ('a0000002-0003-0000-0000-000000000009', '90000000-0000-0000-0002-00000000000a', 'a', 5, 5, 'b0000000-0000-0000-0000-000000000002', '153.86 cm²', NULL),
  ('a0000002-0003-0000-0000-00000000000a', '90000000-0000-0000-0002-00000000000a', 'b', 8, 10, 'b0000000-0000-0000-0000-000000000002', 'l = √(9+16) = 5 cm', 'Correct application of Pythagoras');

-- ============================================================
-- MATH CLASS — GoodStudent Clara (student 03): 31/40, 48/60, 28/35
-- ============================================================
INSERT INTO test_scores (id, student_id, class_id, teacher_id, test_name, test_date, overall_score, max_score, test_source, created_by, updated_by) VALUES
  ('80000000-0000-0000-0003-000000000001', '50000000-0000-0000-0000-000000000003', '60000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'Centre Quiz 1',  '2025-02-10', 31, 40, 'CENTRE', '10000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002'),
  ('80000000-0000-0000-0003-000000000002', '50000000-0000-0000-0000-000000000003', '60000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'School Midterm', '2025-03-15', 35, 42, 'SCHOOL', '10000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002'),
  ('80000000-0000-0000-0003-000000000003', '50000000-0000-0000-0000-000000000003', '60000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'Centre Quiz 2',  '2025-04-20', 28, 35, 'CENTRE', '10000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002');

INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type, mcq_options) VALUES
  ('90000000-0000-0000-0003-000000000001', '80000000-0000-0000-0003-000000000001', 'Q1', 4, 'MCQ Section', 'MCQ', '[{"key":"A","text":"A"},{"key":"B","text":"B"},{"key":"C","text":"C"},{"key":"D","text":"D"}]');
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0003-000000000002', '80000000-0000-0000-0003-000000000001', 'Q2', 12, 'Algebra section', 'OPEN'),
  ('90000000-0000-0000-0003-000000000003', '80000000-0000-0000-0003-000000000001', 'Q3', 12, 'Geometry section', 'OPEN'),
  ('90000000-0000-0000-0003-000000000004', '80000000-0000-0000-0003-000000000001', 'Q4', 12, 'Statistics section', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000003-0001-0000-0000-000000000001', '90000000-0000-0000-0003-000000000001', '1', 1, 1, 'b0000000-0000-0000-0000-000000000001', 'A', NULL),
  ('a0000003-0001-0000-0000-000000000002', '90000000-0000-0000-0003-000000000001', '2', 0, 1, 'b0000000-0000-0000-0000-000000000001', 'C', 'Sign error in inequality'),
  ('a0000003-0001-0000-0000-000000000003', '90000000-0000-0000-0003-000000000001', '3', 1, 1, 'b0000000-0000-0000-0000-000000000002', 'B', NULL),
  ('a0000003-0001-0000-0000-000000000004', '90000000-0000-0000-0003-000000000001', '4', 1, 1, 'b0000000-0000-0000-0000-000000000002', 'A', NULL),
  ('a0000003-0001-0000-0000-000000000005', '90000000-0000-0000-0003-000000000002', 'a', 3, 3, 'b0000000-0000-0000-0000-000000000001', 'x = 5', NULL),
  ('a0000003-0001-0000-0000-000000000006', '90000000-0000-0000-0003-000000000002', 'b', 3, 4, 'b0000000-0000-0000-0000-000000000001', '(x+2)(x+3)', 'Correct but no working shown'),
  ('a0000003-0001-0000-0000-000000000007', '90000000-0000-0000-0003-000000000002', 'c', 3, 5, 'b0000000-0000-0000-0000-000000000001', 'x=3, y=1', 'Correct answer, working partially shown'),
  ('a0000003-0001-0000-0000-000000000008', '90000000-0000-0000-0003-000000000003', 'a', 4, 4, 'b0000000-0000-0000-0000-000000000002', '30 cm²', NULL),
  ('a0000003-0001-0000-0000-000000000009', '90000000-0000-0000-0003-000000000003', 'b', 5, 8, 'b0000000-0000-0000-0000-000000000002', 'V = 282.6 cm³', 'Correct value but should show πr²h formula explicitly'),
  ('a0000003-0001-0000-0000-00000000000a', '90000000-0000-0000-0003-000000000004', 'a', 3, 3, 'b0000000-0000-0000-0000-000000000003', '9', NULL),
  ('a0000003-0001-0000-0000-00000000000b', '90000000-0000-0000-0003-000000000004', 'b', 3, 3, 'b0000000-0000-0000-0000-000000000003', '9.2', NULL),
  ('a0000003-0001-0000-0000-00000000000c', '90000000-0000-0000-0003-000000000004', 'c', 4, 6, 'b0000000-0000-0000-0000-000000000003', 'Roughly symmetric, mean ≈ median', 'Good start but needs to discuss spread/range');

-- Clara Test 2 & 3 (abbreviated — same structure)
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type, mcq_options) VALUES
  ('90000000-0000-0000-0003-000000000005', '80000000-0000-0000-0003-000000000002', 'Q1', 6, 'MCQ Section A', 'MCQ', '[{"key":"A","text":"A"},{"key":"B","text":"B"},{"key":"C","text":"C"},{"key":"D","text":"D"}]');
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0003-000000000006', '80000000-0000-0000-0003-000000000002', 'Q2', 18, 'Algebra + Trig', 'OPEN'),
  ('90000000-0000-0000-0003-000000000007', '80000000-0000-0000-0003-000000000002', 'Q3', 18, 'Geometry + Stats', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000003-0002-0000-0000-000000000001', '90000000-0000-0000-0003-000000000005', '1', 1, 1, 'b0000000-0000-0000-0000-000000000001', 'A', NULL),
  ('a0000003-0002-0000-0000-000000000002', '90000000-0000-0000-0003-000000000005', '2', 1, 1, 'b0000000-0000-0000-0000-000000000002', 'B', NULL),
  ('a0000003-0002-0000-0000-000000000003', '90000000-0000-0000-0003-000000000005', '3', 1, 1, 'b0000000-0000-0000-0000-000000000003', 'A', NULL),
  ('a0000003-0002-0000-0000-000000000004', '90000000-0000-0000-0003-000000000005', '4', 1, 1, 'b0000000-0000-0000-0000-000000000004', 'C', NULL),
  ('a0000003-0002-0000-0000-000000000005', '90000000-0000-0000-0003-000000000005', '5', 0, 1, 'b0000000-0000-0000-0000-000000000001', 'D', NULL),
  ('a0000003-0002-0000-0000-000000000006', '90000000-0000-0000-0003-000000000005', '6', 1, 1, 'b0000000-0000-0000-0000-000000000003', 'B', NULL),
  ('a0000003-0002-0000-0000-000000000007', '90000000-0000-0000-0003-000000000006', 'a', 4, 4, 'b0000000-0000-0000-0000-000000000001', '11x - 8', NULL),
  ('a0000003-0002-0000-0000-000000000008', '90000000-0000-0000-0003-000000000006', 'b', 4, 5, 'b0000000-0000-0000-0000-000000000001', 'x = 3, x = -3', 'Correct but should write x² - 9 = (x-3)(x+3) = 0'),
  ('a0000003-0002-0000-0000-000000000009', '90000000-0000-0000-0003-000000000006', 'c', 6, 9, 'b0000000-0000-0000-0000-000000000004', 'sin30=0.5, cos60=0.5, complementary angles', 'Good understanding'),
  ('a0000003-0002-0000-0000-00000000000a', '90000000-0000-0000-0003-000000000007', 'a', 4, 4, 'b0000000-0000-0000-0000-000000000002', '38 cm', NULL),
  ('a0000003-0002-0000-0000-00000000000b', '90000000-0000-0000-0003-000000000007', 'b', 5, 5, 'b0000000-0000-0000-0000-000000000003', '10', NULL),
  ('a0000003-0002-0000-0000-00000000000c', '90000000-0000-0000-0003-000000000007', 'c', 7, 9, 'b0000000-0000-0000-0000-000000000002', 'C = 43.96 cm', 'Correct, minor precision issue');

INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type, mcq_options) VALUES
  ('90000000-0000-0000-0003-000000000008', '80000000-0000-0000-0003-000000000003', 'Q1', 5, 'MCQ Section', 'MCQ', '[{"key":"A","text":"A"},{"key":"B","text":"B"},{"key":"C","text":"C"},{"key":"D","text":"D"}]');
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0003-000000000009', '80000000-0000-0000-0003-000000000003', 'Q2', 15, 'Algebra', 'OPEN'),
  ('90000000-0000-0000-0003-00000000000a', '80000000-0000-0000-0003-000000000003', 'Q3', 15, 'Geometry', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000003-0003-0000-0000-000000000001', '90000000-0000-0000-0003-000000000008', '1', 1, 1, 'b0000000-0000-0000-0000-000000000004', 'B', NULL),
  ('a0000003-0003-0000-0000-000000000002', '90000000-0000-0000-0003-000000000008', '2', 1, 1, 'b0000000-0000-0000-0000-000000000004', 'A', NULL),
  ('a0000003-0003-0000-0000-000000000003', '90000000-0000-0000-0003-000000000008', '3', 0, 1, 'b0000000-0000-0000-0000-000000000003', 'B', 'Confused mode with median'),
  ('a0000003-0003-0000-0000-000000000004', '90000000-0000-0000-0003-000000000008', '4', 1, 1, 'b0000000-0000-0000-0000-000000000003', 'D', NULL),
  ('a0000003-0003-0000-0000-000000000005', '90000000-0000-0000-0003-000000000008', '5', 1, 1, 'b0000000-0000-0000-0000-000000000004', 'A', NULL),
  ('a0000003-0003-0000-0000-000000000006', '90000000-0000-0000-0003-000000000009', 'a', 4, 4, 'b0000000-0000-0000-0000-000000000001', '2x²+x-3', NULL),
  ('a0000003-0003-0000-0000-000000000007', '90000000-0000-0000-0003-000000000009', 'b', 4, 4, 'b0000000-0000-0000-0000-000000000001', 'x = 4', NULL),
  ('a0000003-0003-0000-0000-000000000008', '90000000-0000-0000-0003-000000000009', 'c', 4, 7, 'b0000000-0000-0000-0000-000000000001', 'x = 4', 'Correct but no quadratic working shown — lost marks for method'),
  ('a0000003-0003-0000-0000-000000000009', '90000000-0000-0000-0003-00000000000a', 'a', 5, 5, 'b0000000-0000-0000-0000-000000000002', '153.86 cm²', NULL),
  ('a0000003-0003-0000-0000-00000000000a', '90000000-0000-0000-0003-00000000000a', 'b', 7, 10, 'b0000000-0000-0000-0000-000000000002', 'l = 5 cm', 'Correct answer, showed Pythagoras');

-- ============================================================
-- MATH CLASS — GoodStudent Daniel (student 04): 30/40, 46/60, 27/35
-- ============================================================
INSERT INTO test_scores (id, student_id, class_id, teacher_id, test_name, test_date, overall_score, max_score, test_source, created_by, updated_by) VALUES
  ('80000000-0000-0000-0004-000000000001', '50000000-0000-0000-0000-000000000004', '60000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'Centre Quiz 1',  '2025-02-10', 30, 40, 'CENTRE', '10000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002'),
  ('80000000-0000-0000-0004-000000000002', '50000000-0000-0000-0000-000000000004', '60000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'School Midterm', '2025-03-15', 33, 42, 'SCHOOL', '10000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002'),
  ('80000000-0000-0000-0004-000000000003', '50000000-0000-0000-0000-000000000004', '60000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'Centre Quiz 2',  '2025-04-20', 27, 35, 'CENTRE', '10000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002');

INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type, mcq_options) VALUES
  ('90000000-0000-0000-0004-000000000001', '80000000-0000-0000-0004-000000000001', 'Q1', 4, 'MCQ', 'MCQ', '[{"key":"A","text":"A"},{"key":"B","text":"B"},{"key":"C","text":"C"},{"key":"D","text":"D"}]');
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0004-000000000002', '80000000-0000-0000-0004-000000000001', 'Q2', 12, 'Algebra', 'OPEN'),
  ('90000000-0000-0000-0004-000000000003', '80000000-0000-0000-0004-000000000001', 'Q3', 12, 'Geometry', 'OPEN'),
  ('90000000-0000-0000-0004-000000000004', '80000000-0000-0000-0004-000000000001', 'Q4', 12, 'Statistics', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000004-0001-0000-0000-000000000001', '90000000-0000-0000-0004-000000000001', '1', 1, 1, 'b0000000-0000-0000-0000-000000000001', 'A', NULL),
  ('a0000004-0001-0000-0000-000000000002', '90000000-0000-0000-0004-000000000001', '2', 1, 1, 'b0000000-0000-0000-0000-000000000001', 'B', NULL),
  ('a0000004-0001-0000-0000-000000000003', '90000000-0000-0000-0004-000000000001', '3', 0, 1, 'b0000000-0000-0000-0000-000000000002', 'D', 'Careless — chose diameter formula instead of radius'),
  ('a0000004-0001-0000-0000-000000000004', '90000000-0000-0000-0004-000000000001', '4', 1, 1, 'b0000000-0000-0000-0000-000000000002', 'A', NULL),
  ('a0000004-0001-0000-0000-000000000005', '90000000-0000-0000-0004-000000000002', 'a', 3, 3, 'b0000000-0000-0000-0000-000000000001', 'x = 5', NULL),
  ('a0000004-0001-0000-0000-000000000006', '90000000-0000-0000-0004-000000000002', 'b', 3, 4, 'b0000000-0000-0000-0000-000000000001', '(x+2)(x+3)', 'Correct but trial-and-error approach — should learn systematic method'),
  ('a0000004-0001-0000-0000-000000000007', '90000000-0000-0000-0004-000000000002', 'c', 3, 5, 'b0000000-0000-0000-0000-000000000001', 'x=3, y=1', 'Correct, working shown but messy'),
  ('a0000004-0001-0000-0000-000000000008', '90000000-0000-0000-0004-000000000003', 'a', 4, 4, 'b0000000-0000-0000-0000-000000000002', '30 cm²', NULL),
  ('a0000004-0001-0000-0000-000000000009', '90000000-0000-0000-0004-000000000003', 'b', 5, 8, 'b0000000-0000-0000-0000-000000000002', '282.6 cm³', 'Correct, used calculator well'),
  ('a0000004-0001-0000-0000-00000000000a', '90000000-0000-0000-0004-000000000004', 'a', 3, 3, 'b0000000-0000-0000-0000-000000000003', '9', NULL),
  ('a0000004-0001-0000-0000-00000000000b', '90000000-0000-0000-0004-000000000004', 'b', 3, 3, 'b0000000-0000-0000-0000-000000000003', '9.2', NULL),
  ('a0000004-0001-0000-0000-00000000000c', '90000000-0000-0000-0004-000000000004', 'c', 3, 6, 'b0000000-0000-0000-0000-000000000003', 'Data is balanced', 'Too vague — needs statistical vocabulary');

-- Daniel Test 2 & 3 (same structure as other good students)
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type, mcq_options) VALUES
  ('90000000-0000-0000-0004-000000000005', '80000000-0000-0000-0004-000000000002', 'Q1', 6, 'MCQ', 'MCQ', '[{"key":"A","text":"A"},{"key":"B","text":"B"},{"key":"C","text":"C"},{"key":"D","text":"D"}]');
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0004-000000000006', '80000000-0000-0000-0004-000000000002', 'Q2', 18, 'Algebra + Trig', 'OPEN'),
  ('90000000-0000-0000-0004-000000000007', '80000000-0000-0000-0004-000000000002', 'Q3', 18, 'Geometry + Stats', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000004-0002-0000-0000-000000000001', '90000000-0000-0000-0004-000000000005', '1', 1, 1, 'b0000000-0000-0000-0000-000000000001', 'A', NULL),
  ('a0000004-0002-0000-0000-000000000002', '90000000-0000-0000-0004-000000000005', '2', 1, 1, 'b0000000-0000-0000-0000-000000000002', 'B', NULL),
  ('a0000004-0002-0000-0000-000000000003', '90000000-0000-0000-0004-000000000005', '3', 1, 1, 'b0000000-0000-0000-0000-000000000003', 'A', NULL),
  ('a0000004-0002-0000-0000-000000000004', '90000000-0000-0000-0004-000000000005', '4', 0, 1, 'b0000000-0000-0000-0000-000000000004', 'B', 'Confused tan and cos'),
  ('a0000004-0002-0000-0000-000000000005', '90000000-0000-0000-0004-000000000005', '5', 1, 1, 'b0000000-0000-0000-0000-000000000001', 'A', NULL),
  ('a0000004-0002-0000-0000-000000000006', '90000000-0000-0000-0004-000000000005', '6', 1, 1, 'b0000000-0000-0000-0000-000000000003', 'B', NULL),
  ('a0000004-0002-0000-0000-000000000007', '90000000-0000-0000-0004-000000000006', 'a', 4, 4, 'b0000000-0000-0000-0000-000000000001', '11x - 8', NULL),
  ('a0000004-0002-0000-0000-000000000008', '90000000-0000-0000-0004-000000000006', 'b', 4, 5, 'b0000000-0000-0000-0000-000000000001', 'x = ±3', 'Good'),
  ('a0000004-0002-0000-0000-000000000009', '90000000-0000-0000-0004-000000000006', 'c', 5, 9, 'b0000000-0000-0000-0000-000000000004', 'sin30=0.5, cos60=0.5', 'Values correct but no explanation of why they are equal'),
  ('a0000004-0002-0000-0000-00000000000a', '90000000-0000-0000-0004-000000000007', 'a', 4, 4, 'b0000000-0000-0000-0000-000000000002', '38 cm', NULL),
  ('a0000004-0002-0000-0000-00000000000b', '90000000-0000-0000-0004-000000000007', 'b', 5, 5, 'b0000000-0000-0000-0000-000000000003', '10', NULL),
  ('a0000004-0002-0000-0000-00000000000c', '90000000-0000-0000-0004-000000000007', 'c', 6, 9, 'b0000000-0000-0000-0000-000000000002', 'C = 43.96 cm', 'Correct');

INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type, mcq_options) VALUES
  ('90000000-0000-0000-0004-000000000008', '80000000-0000-0000-0004-000000000003', 'Q1', 5, 'MCQ', 'MCQ', '[{"key":"A","text":"A"},{"key":"B","text":"B"},{"key":"C","text":"C"},{"key":"D","text":"D"}]');
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0004-000000000009', '80000000-0000-0000-0004-000000000003', 'Q2', 15, 'Algebra', 'OPEN'),
  ('90000000-0000-0000-0004-00000000000a', '80000000-0000-0000-0004-000000000003', 'Q3', 15, 'Geometry', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000004-0003-0000-0000-000000000001', '90000000-0000-0000-0004-000000000008', '1', 1, 1, 'b0000000-0000-0000-0000-000000000004', 'B', NULL),
  ('a0000004-0003-0000-0000-000000000002', '90000000-0000-0000-0004-000000000008', '2', 0, 1, 'b0000000-0000-0000-0000-000000000004', 'C', NULL),
  ('a0000004-0003-0000-0000-000000000003', '90000000-0000-0000-0004-000000000008', '3', 1, 1, 'b0000000-0000-0000-0000-000000000003', 'C', NULL),
  ('a0000004-0003-0000-0000-000000000004', '90000000-0000-0000-0004-000000000008', '4', 1, 1, 'b0000000-0000-0000-0000-000000000003', 'D', NULL),
  ('a0000004-0003-0000-0000-000000000005', '90000000-0000-0000-0004-000000000008', '5', 1, 1, 'b0000000-0000-0000-0000-000000000004', 'A', NULL),
  ('a0000004-0003-0000-0000-000000000006', '90000000-0000-0000-0004-000000000009', 'a', 4, 4, 'b0000000-0000-0000-0000-000000000001', '2x²+x-3', NULL),
  ('a0000004-0003-0000-0000-000000000007', '90000000-0000-0000-0004-000000000009', 'b', 4, 4, 'b0000000-0000-0000-0000-000000000001', 'x = 4', NULL),
  ('a0000004-0003-0000-0000-000000000008', '90000000-0000-0000-0004-000000000009', 'c', 4, 7, 'b0000000-0000-0000-0000-000000000001', 'x = 4', 'Correct but no working for quadratic'),
  ('a0000004-0003-0000-0000-000000000009', '90000000-0000-0000-0004-00000000000a', 'a', 5, 5, 'b0000000-0000-0000-0000-000000000002', '153.86 cm²', NULL),
  ('a0000004-0003-0000-0000-00000000000a', '90000000-0000-0000-0004-00000000000a', 'b', 6, 10, 'b0000000-0000-0000-0000-000000000002', 'l = √25 = 5', 'Correct but should show 3²+4² = 25 step');

-- ============================================================
-- MATH CLASS — StrugglingStudent Fiona (student 06): 14/40, 22/60, 13/35
-- ============================================================
INSERT INTO test_scores (id, student_id, class_id, teacher_id, test_name, test_date, overall_score, max_score, test_source, created_by, updated_by) VALUES
  ('80000000-0000-0000-0006-000000000001', '50000000-0000-0000-0000-000000000006', '60000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'Centre Quiz 1',  '2025-02-10', 14, 40, 'CENTRE', '10000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002'),
  ('80000000-0000-0000-0006-000000000002', '50000000-0000-0000-0000-000000000006', '60000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'School Midterm', '2025-03-15', 16, 42, 'SCHOOL', '10000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002'),
  ('80000000-0000-0000-0006-000000000003', '50000000-0000-0000-0000-000000000006', '60000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'Centre Quiz 2',  '2025-04-20', 9, 35, 'CENTRE', '10000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002');

INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type, mcq_options) VALUES
  ('90000000-0000-0000-0006-000000000001', '80000000-0000-0000-0006-000000000001', 'Q1', 4, 'MCQ', 'MCQ', '[{"key":"A","text":"A"},{"key":"B","text":"B"},{"key":"C","text":"C"},{"key":"D","text":"D"}]');
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0006-000000000002', '80000000-0000-0000-0006-000000000001', 'Q2', 12, 'Algebra', 'OPEN'),
  ('90000000-0000-0000-0006-000000000003', '80000000-0000-0000-0006-000000000001', 'Q3', 12, 'Geometry', 'OPEN'),
  ('90000000-0000-0000-0006-000000000004', '80000000-0000-0000-0006-000000000001', 'Q4', 12, 'Statistics', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000006-0001-0000-0000-000000000001', '90000000-0000-0000-0006-000000000001', '1', 0, 1, 'b0000000-0000-0000-0000-000000000001', 'C', 'Random guess'),
  ('a0000006-0001-0000-0000-000000000002', '90000000-0000-0000-0006-000000000001', '2', 1, 1, 'b0000000-0000-0000-0000-000000000001', 'B', NULL),
  ('a0000006-0001-0000-0000-000000000003', '90000000-0000-0000-0006-000000000001', '3', 0, 1, 'b0000000-0000-0000-0000-000000000002', 'D', NULL),
  ('a0000006-0001-0000-0000-000000000004', '90000000-0000-0000-0006-000000000001', '4', 0, 1, 'b0000000-0000-0000-0000-000000000002', 'A', NULL),
  ('a0000006-0001-0000-0000-000000000005', '90000000-0000-0000-0006-000000000002', 'a', 1, 3, 'b0000000-0000-0000-0000-000000000001', '3x = 29, x = 9.6', 'Added 7 instead of subtracting — does not understand inverse operations'),
  ('a0000006-0001-0000-0000-000000000006', '90000000-0000-0000-0006-000000000002', 'b', 0, 4, 'b0000000-0000-0000-0000-000000000001', 'x² + 5x + 6 = x(x+5+6)', 'Fundamental misunderstanding of factorisation'),
  ('a0000006-0001-0000-0000-000000000007', '90000000-0000-0000-0006-000000000002', 'c', 0, 5, 'b0000000-0000-0000-0000-000000000001', 'I dont know', 'Left blank — needs intensive revision on simultaneous equations'),
  ('a0000006-0001-0000-0000-000000000008', '90000000-0000-0000-0006-000000000003', 'a', 2, 4, 'b0000000-0000-0000-0000-000000000002', 'base × height = 60', 'Forgot to halve for triangle area'),
  ('a0000006-0001-0000-0000-000000000009', '90000000-0000-0000-0006-000000000003', 'b', 2, 8, 'b0000000-0000-0000-0000-000000000002', '3 × 10 = 30', 'Used radius × height only — missing π and r² concept entirely'),
  ('a0000006-0001-0000-0000-00000000000a', '90000000-0000-0000-0006-000000000004', 'a', 3, 3, 'b0000000-0000-0000-0000-000000000003', '9', NULL),
  ('a0000006-0001-0000-0000-00000000000b', '90000000-0000-0000-0006-000000000004', 'b', 2, 3, 'b0000000-0000-0000-0000-000000000003', 'Mean = 15 (the middle number)', 'Confused mean with median — critical gap'),
  ('a0000006-0001-0000-0000-00000000000c', '90000000-0000-0000-0006-000000000004', 'c', 3, 6, 'b0000000-0000-0000-0000-000000000003', 'The numbers are different', 'No statistical reasoning at all');

-- Fiona Test 2 & 3 (abbreviated)
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type, mcq_options) VALUES
  ('90000000-0000-0000-0006-000000000005', '80000000-0000-0000-0006-000000000002', 'Q1', 6, 'MCQ', 'MCQ', '[{"key":"A","text":"A"},{"key":"B","text":"B"},{"key":"C","text":"C"},{"key":"D","text":"D"}]');
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0006-000000000006', '80000000-0000-0000-0006-000000000002', 'Q2', 18, 'Algebra + Trig', 'OPEN'),
  ('90000000-0000-0000-0006-000000000007', '80000000-0000-0000-0006-000000000002', 'Q3', 18, 'Geometry + Stats', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000006-0002-0000-0000-000000000001', '90000000-0000-0000-0006-000000000005', '1', 1, 1, 'b0000000-0000-0000-0000-000000000001', 'A', NULL),
  ('a0000006-0002-0000-0000-000000000002', '90000000-0000-0000-0006-000000000005', '2', 0, 1, 'b0000000-0000-0000-0000-000000000002', 'D', NULL),
  ('a0000006-0002-0000-0000-000000000003', '90000000-0000-0000-0006-000000000005', '3', 0, 1, 'b0000000-0000-0000-0000-000000000003', 'C', NULL),
  ('a0000006-0002-0000-0000-000000000004', '90000000-0000-0000-0006-000000000005', '4', 0, 1, 'b0000000-0000-0000-0000-000000000004', 'A', NULL),
  ('a0000006-0002-0000-0000-000000000005', '90000000-0000-0000-0006-000000000005', '5', 0, 1, 'b0000000-0000-0000-0000-000000000001', 'B', NULL),
  ('a0000006-0002-0000-0000-000000000006', '90000000-0000-0000-0006-000000000005', '6', 1, 1, 'b0000000-0000-0000-0000-000000000003', 'B', NULL),
  ('a0000006-0002-0000-0000-000000000007', '90000000-0000-0000-0006-000000000006', 'a', 1, 4, 'b0000000-0000-0000-0000-000000000001', '2 × 3x + 5x = 11x', 'Dropped the -4 entirely — incomplete distribution'),
  ('a0000006-0002-0000-0000-000000000008', '90000000-0000-0000-0006-000000000006', 'b', 2, 5, 'b0000000-0000-0000-0000-000000000001', 'x = 3', 'Only positive root — same issue as Ethan, needs ± reminder'),
  ('a0000006-0002-0000-0000-000000000009', '90000000-0000-0000-0006-000000000006', 'c', 0, 9, 'b0000000-0000-0000-0000-000000000004', 'I dont know trig', 'No attempt — trig is a major weakness'),
  ('a0000006-0002-0000-0000-00000000000a', '90000000-0000-0000-0006-000000000007', 'a', 2, 4, 'b0000000-0000-0000-0000-000000000002', '12 + 7 = 19 cm', 'Only added two sides — same error as before'),
  ('a0000006-0002-0000-0000-00000000000b', '90000000-0000-0000-0006-000000000007', 'b', 5, 5, 'b0000000-0000-0000-0000-000000000003', '50/5 = 10', 'Good — mean calculation is improving'),
  ('a0000006-0002-0000-0000-00000000000c', '90000000-0000-0000-0006-000000000007', 'c', 4, 9, 'b0000000-0000-0000-0000-000000000002', 'C = 3.14 × 14 = 43.96', 'Used πd correctly this time — improvement from last test');

INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type, mcq_options) VALUES
  ('90000000-0000-0000-0006-000000000008', '80000000-0000-0000-0006-000000000003', 'Q1', 5, 'MCQ', 'MCQ', '[{"key":"A","text":"A"},{"key":"B","text":"B"},{"key":"C","text":"C"},{"key":"D","text":"D"}]');
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0006-000000000009', '80000000-0000-0000-0006-000000000003', 'Q2', 15, 'Algebra', 'OPEN'),
  ('90000000-0000-0000-0006-00000000000a', '80000000-0000-0000-0006-000000000003', 'Q3', 15, 'Geometry', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000006-0003-0000-0000-000000000001', '90000000-0000-0000-0006-000000000008', '1', 0, 1, 'b0000000-0000-0000-0000-000000000004', 'C', NULL),
  ('a0000006-0003-0000-0000-000000000002', '90000000-0000-0000-0006-000000000008', '2', 0, 1, 'b0000000-0000-0000-0000-000000000004', 'A', NULL),
  ('a0000006-0003-0000-0000-000000000003', '90000000-0000-0000-0006-000000000008', '3', 1, 1, 'b0000000-0000-0000-0000-000000000003', 'D', NULL),
  ('a0000006-0003-0000-0000-000000000004', '90000000-0000-0000-0006-000000000008', '4', 0, 1, 'b0000000-0000-0000-0000-000000000003', 'B', NULL),
  ('a0000006-0003-0000-0000-000000000005', '90000000-0000-0000-0006-000000000008', '5', 0, 1, 'b0000000-0000-0000-0000-000000000004', 'A', NULL),
  ('a0000006-0003-0000-0000-000000000006', '90000000-0000-0000-0006-000000000009', 'a', 1, 4, 'b0000000-0000-0000-0000-000000000001', '2x + 3x - 1 = 5x - 1', 'Did not use FOIL — treated as simple addition'),
  ('a0000006-0003-0000-0000-000000000007', '90000000-0000-0000-0006-000000000009', 'b', 2, 4, 'b0000000-0000-0000-0000-000000000001', '5x - 2x = 9 + 3, x = 4', 'Correct this time — sign handling improved'),
  ('a0000006-0003-0000-0000-000000000008', '90000000-0000-0000-0006-000000000009', 'c', 0, 7, 'b0000000-0000-0000-0000-000000000001', 'x = 48 - 2 - 6 = 40', 'No understanding of forming quadratic from area equation'),
  ('a0000006-0003-0000-0000-000000000009', '90000000-0000-0000-0006-00000000000a', 'a', 3, 5, 'b0000000-0000-0000-0000-000000000002', '3.14 × 7 = 21.98', 'Used πr instead of πr² — forgot to square the radius'),
  ('a0000006-0003-0000-0000-00000000000a', '90000000-0000-0000-0006-00000000000a', 'b', 2, 10, 'b0000000-0000-0000-0000-000000000002', 'l = 3 + 4 = 7', 'Added instead of using Pythagoras — same error as Ethan');

-- ============================================================
-- PHYSICS CLASS — GoodStudent Grace (student 07): 38/45, 52/60, 33/40
-- ============================================================
INSERT INTO test_scores (id, student_id, class_id, teacher_id, test_name, test_date, overall_score, max_score, test_source, created_by, updated_by) VALUES
  ('80000000-0000-0000-0007-000000000001', '50000000-0000-0000-0000-000000000007', '60000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', 'Centre Quiz 1',    '2025-02-12', 38, 45, 'CENTRE', '10000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003'),
  ('80000000-0000-0000-0007-000000000002', '50000000-0000-0000-0000-000000000007', '60000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', 'School Midterm',   '2025-03-18', 51, 60, 'SCHOOL', '10000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003'),
  ('80000000-0000-0000-0007-000000000003', '50000000-0000-0000-0000-000000000007', '60000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', 'Centre Quiz 2',    '2025-04-22', 33, 40, 'CENTRE', '10000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003');

-- Grace Test 1: 45 marks (5 MCQ + 2 open)
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type, mcq_options) VALUES
  ('90000000-0000-0000-0007-000000000001', '80000000-0000-0000-0007-000000000001', 'Q1', 5, 'MCQ Section', 'MCQ', '[{"key":"A","text":"A"},{"key":"B","text":"B"},{"key":"C","text":"C"},{"key":"D","text":"D"}]');
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0007-000000000002', '80000000-0000-0000-0007-000000000001', 'Q2', 20, '(a) Calculate final velocity. (b) Convert 100°C to Kelvin. (c) Explain heat vs temperature.', 'OPEN'),
  ('90000000-0000-0000-0007-000000000003', '80000000-0000-0000-0007-000000000001', 'Q3', 20, '(a) Wave speed calculation. (b) Ohms law problem.', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000007-0001-0000-0000-000000000001', '90000000-0000-0000-0007-000000000001', '1', 1, 1, 'b0000000-0000-0000-0000-000000000007', 'B', NULL),
  ('a0000007-0001-0000-0000-000000000002', '90000000-0000-0000-0007-000000000001', '2', 1, 1, 'b0000000-0000-0000-0000-000000000008', 'A', NULL),
  ('a0000007-0001-0000-0000-000000000003', '90000000-0000-0000-0007-000000000001', '3', 1, 1, 'b0000000-0000-0000-0000-000000000009', 'C', NULL),
  ('a0000007-0001-0000-0000-000000000004', '90000000-0000-0000-0007-000000000001', '4', 0, 1, 'b0000000-0000-0000-0000-00000000000a', 'D', 'Confused series and parallel resistance formulas'),
  ('a0000007-0001-0000-0000-000000000005', '90000000-0000-0000-0007-000000000001', '5', 1, 1, 'b0000000-0000-0000-0000-000000000007', 'A', NULL),
  ('a0000007-0001-0000-0000-000000000006', '90000000-0000-0000-0007-000000000002', 'a', 6, 6, 'b0000000-0000-0000-0000-000000000007', 'v = u + at = 0 + 2(8) = 16 m/s', NULL),
  ('a0000007-0001-0000-0000-000000000007', '90000000-0000-0000-0007-000000000002', 'b', 4, 4, 'b0000000-0000-0000-0000-000000000008', 'K = 100 + 273 = 373 K', NULL),
  ('a0000007-0001-0000-0000-000000000008', '90000000-0000-0000-0007-000000000002', 'c', 8, 10, 'b0000000-0000-0000-0000-000000000008', 'Heat is energy transfer; temperature measures average KE of particles', 'Good definition but could give an example like phase change'),
  ('a0000007-0001-0000-0000-000000000009', '90000000-0000-0000-0007-000000000003', 'a', 8, 8, 'b0000000-0000-0000-0000-000000000009', 'v = fλ = 50 × 0.4 = 20 m/s', NULL),
  ('a0000007-0001-0000-0000-00000000000a', '90000000-0000-0000-0007-000000000003', 'b', 8, 12, 'b0000000-0000-0000-0000-00000000000a', 'V = IR, so I = V/R = 12/4 = 3A', 'Correct calculation but did not state units clearly in working');

-- Grace Test 2: 60 marks
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type, mcq_options) VALUES
  ('90000000-0000-0000-0007-000000000004', '80000000-0000-0000-0007-000000000002', 'Q1', 8, 'MCQ Section (8 marks)', 'MCQ', '[{"key":"A","text":"A"},{"key":"B","text":"B"},{"key":"C","text":"C"},{"key":"D","text":"D"}]');
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0007-000000000005', '80000000-0000-0000-0007-000000000002', 'Q2', 16, 'Mechanics: KE and Newtons Laws', 'OPEN'),
  ('90000000-0000-0000-0007-000000000006', '80000000-0000-0000-0007-000000000002', 'Q3', 16, 'Waves and Electricity', 'OPEN'),
  ('90000000-0000-0000-0007-000000000007', '80000000-0000-0000-0007-000000000002', 'Q4', 20, 'Thermodynamics extended', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000007-0002-0000-0000-000000000001', '90000000-0000-0000-0007-000000000004', '1', 1, 1, 'b0000000-0000-0000-0000-000000000007', 'B', NULL),
  ('a0000007-0002-0000-0000-000000000002', '90000000-0000-0000-0007-000000000004', '2', 1, 1, 'b0000000-0000-0000-0000-000000000008', 'A', NULL),
  ('a0000007-0002-0000-0000-000000000003', '90000000-0000-0000-0007-000000000004', '3', 1, 1, 'b0000000-0000-0000-0000-000000000009', 'C', NULL),
  ('a0000007-0002-0000-0000-000000000004', '90000000-0000-0000-0007-000000000004', '4', 1, 1, 'b0000000-0000-0000-0000-00000000000a', 'B', NULL),
  ('a0000007-0002-0000-0000-000000000005', '90000000-0000-0000-0007-000000000004', '5', 1, 1, 'b0000000-0000-0000-0000-000000000007', 'A', NULL),
  ('a0000007-0002-0000-0000-000000000006', '90000000-0000-0000-0007-000000000004', '6', 0, 1, 'b0000000-0000-0000-0000-000000000008', 'D', 'Confused latent heat with specific heat capacity'),
  ('a0000007-0002-0000-0000-000000000007', '90000000-0000-0000-0007-000000000004', '7', 1, 1, 'b0000000-0000-0000-0000-000000000009', 'A', NULL),
  ('a0000007-0002-0000-0000-000000000008', '90000000-0000-0000-0007-000000000004', '8', 1, 1, 'b0000000-0000-0000-0000-00000000000a', 'C', NULL),
  ('a0000007-0002-0000-0000-000000000009', '90000000-0000-0000-0007-000000000005', 'a', 6, 6, 'b0000000-0000-0000-0000-000000000007', 'KE = ½mv² = ½ × 5 × 16 = 40 J', NULL),
  ('a0000007-0002-0000-0000-00000000000a', '90000000-0000-0000-0007-000000000005', 'b', 8, 10, 'b0000000-0000-0000-0000-000000000007', 'F = ma. Net force = mass × acceleration.', 'Correct formula and statement but needs a worked example'),
  ('a0000007-0002-0000-0000-00000000000b', '90000000-0000-0000-0007-000000000006', 'a', 6, 8, 'b0000000-0000-0000-0000-000000000009', 'Convex lens: parallel rays converge at focal point, forming inverted real image', 'Good description, could mention magnification'),
  ('a0000007-0002-0000-0000-00000000000c', '90000000-0000-0000-0007-000000000006', 'b', 6, 8, 'b0000000-0000-0000-0000-00000000000a', 'P = IV = 3 × 12 = 36 W', NULL),
  ('a0000007-0002-0000-0000-00000000000d', '90000000-0000-0000-0007-000000000007', 'a', 10, 10, 'b0000000-0000-0000-0000-000000000008', 'Q = mcΔT = 2 × 4200 × 30 = 252000 J', NULL),
  ('a0000007-0002-0000-0000-00000000000e', '90000000-0000-0000-0007-000000000007', 'b', 8, 10, 'b0000000-0000-0000-0000-000000000008', 'Energy is conserved. Heat flows from hot to cold until thermal equilibrium.', 'Correct principle, could mention entropy');

-- Grace Test 3: 40 marks
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type, mcq_options) VALUES
  ('90000000-0000-0000-0007-000000000008', '80000000-0000-0000-0007-000000000003', 'Q1', 4, 'MCQ', 'MCQ', '[{"key":"A","text":"A"},{"key":"B","text":"B"},{"key":"C","text":"C"},{"key":"D","text":"D"}]');
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0007-000000000009', '80000000-0000-0000-0007-000000000003', 'Q2', 18, 'Mechanics + Waves', 'OPEN'),
  ('90000000-0000-0000-0007-00000000000a', '80000000-0000-0000-0007-000000000003', 'Q3', 18, 'Thermo + Electricity', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000007-0003-0000-0000-000000000001', '90000000-0000-0000-0007-000000000008', '1', 1, 1, 'b0000000-0000-0000-0000-000000000007', 'B', NULL),
  ('a0000007-0003-0000-0000-000000000002', '90000000-0000-0000-0007-000000000008', '2', 1, 1, 'b0000000-0000-0000-0000-000000000009', 'A', NULL),
  ('a0000007-0003-0000-0000-000000000003', '90000000-0000-0000-0007-000000000008', '3', 0, 1, 'b0000000-0000-0000-0000-00000000000a', 'C', 'Confused AC and DC current properties'),
  ('a0000007-0003-0000-0000-000000000004', '90000000-0000-0000-0007-000000000008', '4', 1, 1, 'b0000000-0000-0000-0000-000000000008', 'A', NULL),
  ('a0000007-0003-0000-0000-000000000005', '90000000-0000-0000-0007-000000000009', 'a', 8, 8, 'b0000000-0000-0000-0000-000000000007', 'PE = mgh = 2 × 10 × 10 = 200 J', NULL),
  ('a0000007-0003-0000-0000-000000000006', '90000000-0000-0000-0007-000000000009', 'b', 7, 10, 'b0000000-0000-0000-0000-000000000009', 'Diffraction is bending of waves around obstacles. More diffraction when gap ≈ wavelength.', 'Good but should mention Huygens principle'),
  ('a0000007-0003-0000-0000-000000000007', '90000000-0000-0000-0007-00000000000a', 'a', 8, 8, 'b0000000-0000-0000-0000-000000000008', 'Efficiency = useful output / total input × 100', NULL),
  ('a0000007-0003-0000-0000-000000000008', '90000000-0000-0000-0007-00000000000a', 'b', 7, 10, 'b0000000-0000-0000-0000-00000000000a', 'Total R = R1 + R2 = 4 + 6 = 10Ω, I = 12/10 = 1.2A', 'Correct for series. Should also mention what changes in parallel');


-- ============================================================
-- PHYSICS CLASS — GoodStudent Henry (student 08): 36/45, 50/60, 34/40
-- ============================================================
INSERT INTO test_scores (id, student_id, class_id, teacher_id, test_name, test_date, overall_score, max_score, test_source, created_by, updated_by) VALUES
  ('80000000-0000-0000-0008-000000000001', '50000000-0000-0000-0000-000000000008', '60000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', 'Centre Quiz 1',  '2025-02-12', 37, 45, 'CENTRE', '10000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003'),
  ('80000000-0000-0000-0008-000000000002', '50000000-0000-0000-0000-000000000008', '60000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', 'School Midterm', '2025-03-18', 51, 60, 'SCHOOL', '10000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003'),
  ('80000000-0000-0000-0008-000000000003', '50000000-0000-0000-0000-000000000008', '60000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', 'Centre Quiz 2',  '2025-04-22', 34, 40, 'CENTRE', '10000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003');

-- Henry Test 1: 45 marks (5 MCQ + 2 open)
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type, mcq_options) VALUES
  ('90000000-0000-0000-0008-000000000001', '80000000-0000-0000-0008-000000000001', 'Q1', 5, 'MCQ Section', 'MCQ', '[{"key":"A","text":"A"},{"key":"B","text":"B"},{"key":"C","text":"C"},{"key":"D","text":"D"}]');
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0008-000000000002', '80000000-0000-0000-0008-000000000001', 'Q2', 20, '(a) Calculate final velocity. (b) Convert 100°C to Kelvin. (c) Explain heat vs temperature.', 'OPEN'),
  ('90000000-0000-0000-0008-000000000003', '80000000-0000-0000-0008-000000000001', 'Q3', 20, '(a) Wave speed calculation. (b) Ohms law problem.', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000008-0001-0000-0000-000000000001', '90000000-0000-0000-0008-000000000001', '1', 1, 1, 'b0000000-0000-0000-0000-000000000007', 'B', NULL),
  ('a0000008-0001-0000-0000-000000000002', '90000000-0000-0000-0008-000000000001', '2', 1, 1, 'b0000000-0000-0000-0000-000000000008', 'A', NULL),
  ('a0000008-0001-0000-0000-000000000003', '90000000-0000-0000-0008-000000000001', '3', 0, 1, 'b0000000-0000-0000-0000-000000000009', 'D', 'Confused transverse and longitudinal wave examples'),
  ('a0000008-0001-0000-0000-000000000004', '90000000-0000-0000-0008-000000000001', '4', 1, 1, 'b0000000-0000-0000-0000-00000000000a', 'B', NULL),
  ('a0000008-0001-0000-0000-000000000005', '90000000-0000-0000-0008-000000000001', '5', 1, 1, 'b0000000-0000-0000-0000-000000000007', 'A', NULL),
  ('a0000008-0001-0000-0000-000000000006', '90000000-0000-0000-0008-000000000002', 'a', 6, 6, 'b0000000-0000-0000-0000-000000000007', 'v = u + at = 0 + 2(8) = 16 m/s', NULL),
  ('a0000008-0001-0000-0000-000000000007', '90000000-0000-0000-0008-000000000002', 'b', 4, 4, 'b0000000-0000-0000-0000-000000000008', 'K = 100 + 273 = 373 K', NULL),
  ('a0000008-0001-0000-0000-000000000008', '90000000-0000-0000-0008-000000000002', 'c', 7, 10, 'b0000000-0000-0000-0000-000000000008', 'Heat is energy transferred due to temperature difference. Temperature is a measure of how hot something is.', 'Correct but temperature definition should reference average kinetic energy of particles'),
  ('a0000008-0001-0000-0000-000000000009', '90000000-0000-0000-0008-000000000003', 'a', 8, 8, 'b0000000-0000-0000-0000-000000000009', 'v = fλ = 50 × 0.4 = 20 m/s', NULL),
  ('a0000008-0001-0000-0000-00000000000a', '90000000-0000-0000-0008-000000000003', 'b', 8, 12, 'b0000000-0000-0000-0000-00000000000a', 'V = IR, I = 12/4 = 3A. P = IV = 3 × 12 = 36W', 'Good — went beyond the question to calculate power too');

-- Henry Test 2: 60 marks (8 MCQ + 3 open)
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type, mcq_options) VALUES
  ('90000000-0000-0000-0008-000000000004', '80000000-0000-0000-0008-000000000002', 'Q1', 8, 'MCQ Section (8 marks)', 'MCQ', '[{"key":"A","text":"A"},{"key":"B","text":"B"},{"key":"C","text":"C"},{"key":"D","text":"D"}]');
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0008-000000000005', '80000000-0000-0000-0008-000000000002', 'Q2', 16, 'Mechanics: KE and Newtons Laws', 'OPEN'),
  ('90000000-0000-0000-0008-000000000006', '80000000-0000-0000-0008-000000000002', 'Q3', 16, 'Waves and Electricity', 'OPEN'),
  ('90000000-0000-0000-0008-000000000007', '80000000-0000-0000-0008-000000000002', 'Q4', 20, 'Thermodynamics extended', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000008-0002-0000-0000-000000000001', '90000000-0000-0000-0008-000000000004', '1', 1, 1, 'b0000000-0000-0000-0000-000000000007', 'B', NULL),
  ('a0000008-0002-0000-0000-000000000002', '90000000-0000-0000-0008-000000000004', '2', 1, 1, 'b0000000-0000-0000-0000-000000000008', 'A', NULL),
  ('a0000008-0002-0000-0000-000000000003', '90000000-0000-0000-0008-000000000004', '3', 1, 1, 'b0000000-0000-0000-0000-000000000009', 'C', NULL),
  ('a0000008-0002-0000-0000-000000000004', '90000000-0000-0000-0008-000000000004', '4', 1, 1, 'b0000000-0000-0000-0000-00000000000a', 'B', NULL),
  ('a0000008-0002-0000-0000-000000000005', '90000000-0000-0000-0008-000000000004', '5', 1, 1, 'b0000000-0000-0000-0000-000000000007', 'A', NULL),
  ('a0000008-0002-0000-0000-000000000006', '90000000-0000-0000-0008-000000000004', '6', 1, 1, 'b0000000-0000-0000-0000-000000000008', 'B', NULL),
  ('a0000008-0002-0000-0000-000000000007', '90000000-0000-0000-0008-000000000004', '7', 0, 1, 'b0000000-0000-0000-0000-000000000009', 'D', 'Confused reflection and refraction conditions'),
  ('a0000008-0002-0000-0000-000000000008', '90000000-0000-0000-0008-000000000004', '8', 1, 1, 'b0000000-0000-0000-0000-00000000000a', 'C', NULL),
  ('a0000008-0002-0000-0000-000000000009', '90000000-0000-0000-0008-000000000005', 'a', 6, 6, 'b0000000-0000-0000-0000-000000000007', 'KE = ½mv² = ½ × 5 × 16 = 40 J', NULL),
  ('a0000008-0002-0000-0000-00000000000a', '90000000-0000-0000-0008-000000000005', 'b', 8, 10, 'b0000000-0000-0000-0000-000000000007', 'F = ma. If 10N acts on 2kg, a = 5 m/s².', 'Good worked example, could also mention Newton third law'),
  ('a0000008-0002-0000-0000-00000000000b', '90000000-0000-0000-0008-000000000006', 'a', 7, 8, 'b0000000-0000-0000-0000-000000000009', 'Convex lens converges light to focal point. Image is real and inverted.', 'Correct — could add ray diagram description'),
  ('a0000008-0002-0000-0000-00000000000c', '90000000-0000-0000-0008-000000000006', 'b', 6, 8, 'b0000000-0000-0000-0000-00000000000a', 'P = IV = 3 × 12 = 36 W', 'Correct but did not show I = V/R step first'),
  ('a0000008-0002-0000-0000-00000000000d', '90000000-0000-0000-0008-000000000007', 'a', 10, 10, 'b0000000-0000-0000-0000-000000000008', 'Q = mcΔT = 2 × 4200 × 30 = 252000 J', NULL),
  ('a0000008-0002-0000-0000-00000000000e', '90000000-0000-0000-0008-000000000007', 'b', 7, 10, 'b0000000-0000-0000-0000-000000000008', 'Heat flows from hot to cold. Energy is conserved in a closed system.', 'Correct principle but should mention thermal equilibrium explicitly');

-- Henry Test 3: 40 marks (4 MCQ + 2 open)
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type, mcq_options) VALUES
  ('90000000-0000-0000-0008-000000000008', '80000000-0000-0000-0008-000000000003', 'Q1', 4, 'MCQ', 'MCQ', '[{"key":"A","text":"A"},{"key":"B","text":"B"},{"key":"C","text":"C"},{"key":"D","text":"D"}]');
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0008-000000000009', '80000000-0000-0000-0008-000000000003', 'Q2', 18, 'Mechanics + Waves', 'OPEN'),
  ('90000000-0000-0000-0008-00000000000a', '80000000-0000-0000-0008-000000000003', 'Q3', 18, 'Thermo + Electricity', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000008-0003-0000-0000-000000000001', '90000000-0000-0000-0008-000000000008', '1', 1, 1, 'b0000000-0000-0000-0000-000000000007', 'B', NULL),
  ('a0000008-0003-0000-0000-000000000002', '90000000-0000-0000-0008-000000000008', '2', 1, 1, 'b0000000-0000-0000-0000-000000000009', 'A', NULL),
  ('a0000008-0003-0000-0000-000000000003', '90000000-0000-0000-0008-000000000008', '3', 1, 1, 'b0000000-0000-0000-0000-00000000000a', 'B', NULL),
  ('a0000008-0003-0000-0000-000000000004', '90000000-0000-0000-0008-000000000008', '4', 1, 1, 'b0000000-0000-0000-0000-000000000008', 'A', NULL),
  ('a0000008-0003-0000-0000-000000000005', '90000000-0000-0000-0008-000000000009', 'a', 8, 8, 'b0000000-0000-0000-0000-000000000007', 'PE = mgh = 2 × 10 × 10 = 200 J', NULL),
  ('a0000008-0003-0000-0000-000000000006', '90000000-0000-0000-0008-000000000009', 'b', 8, 10, 'b0000000-0000-0000-0000-000000000009', 'Diffraction occurs when waves pass through a gap similar to their wavelength. The wave spreads out.', 'Good explanation — could mention single-slit vs double-slit patterns'),
  ('a0000008-0003-0000-0000-000000000007', '90000000-0000-0000-0008-00000000000a', 'a', 8, 8, 'b0000000-0000-0000-0000-000000000008', 'Efficiency = useful output / total input × 100 = 300/400 × 100 = 75%', NULL),
  ('a0000008-0003-0000-0000-000000000008', '90000000-0000-0000-0008-00000000000a', 'b', 6, 10, 'b0000000-0000-0000-0000-00000000000a', 'Series: R = R1 + R2 = 10Ω. I = V/R = 12/10 = 1.2A', 'Correct for series but did not attempt parallel calculation as asked');

-- ============================================================
-- PHYSICS CLASS — StrugglingStudent Irene (student 09): 15/45, 19/60, 13/40
-- ============================================================
INSERT INTO test_scores (id, student_id, class_id, teacher_id, test_name, test_date, overall_score, max_score, test_source, created_by, updated_by) VALUES
  ('80000000-0000-0000-0009-000000000001', '50000000-0000-0000-0000-000000000009', '60000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', 'Centre Quiz 1',  '2025-02-12', 15, 45, 'CENTRE', '10000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003'),
  ('80000000-0000-0000-0009-000000000002', '50000000-0000-0000-0000-000000000009', '60000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', 'School Midterm', '2025-03-18', 18, 60, 'SCHOOL', '10000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003'),
  ('80000000-0000-0000-0009-000000000003', '50000000-0000-0000-0000-000000000009', '60000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', 'Centre Quiz 2',  '2025-04-22', 13, 40, 'CENTRE', '10000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003');

-- Irene Test 1: 45 marks (5 MCQ + 2 open)
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type, mcq_options) VALUES
  ('90000000-0000-0000-0009-000000000001', '80000000-0000-0000-0009-000000000001', 'Q1', 5, 'MCQ Section', 'MCQ', '[{"key":"A","text":"A"},{"key":"B","text":"B"},{"key":"C","text":"C"},{"key":"D","text":"D"}]');
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0009-000000000002', '80000000-0000-0000-0009-000000000001', 'Q2', 20, '(a) Calculate final velocity. (b) Convert 100°C to Kelvin. (c) Explain heat vs temperature.', 'OPEN'),
  ('90000000-0000-0000-0009-000000000003', '80000000-0000-0000-0009-000000000001', 'Q3', 20, '(a) Wave speed calculation. (b) Ohms law problem.', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000009-0001-0000-0000-000000000001', '90000000-0000-0000-0009-000000000001', '1', 1, 1, 'b0000000-0000-0000-0000-000000000007', 'B', NULL),
  ('a0000009-0001-0000-0000-000000000002', '90000000-0000-0000-0009-000000000001', '2', 0, 1, 'b0000000-0000-0000-0000-000000000008', 'C', 'Does not know Kelvin conversion formula'),
  ('a0000009-0001-0000-0000-000000000003', '90000000-0000-0000-0009-000000000001', '3', 0, 1, 'b0000000-0000-0000-0000-000000000009', 'A', 'Guessed — no understanding of wave properties'),
  ('a0000009-0001-0000-0000-000000000004', '90000000-0000-0000-0009-000000000001', '4', 0, 1, 'b0000000-0000-0000-0000-00000000000a', 'D', NULL),
  ('a0000009-0001-0000-0000-000000000005', '90000000-0000-0000-0009-000000000001', '5', 0, 1, 'b0000000-0000-0000-0000-000000000007', 'C', 'Confused velocity and acceleration'),
  ('a0000009-0001-0000-0000-000000000006', '90000000-0000-0000-0009-000000000002', 'a', 2, 6, 'b0000000-0000-0000-0000-000000000007', 'v = 2 × 8 = 16', 'Got correct number by luck but used wrong formula — wrote v = a × t, missing initial velocity concept'),
  ('a0000009-0001-0000-0000-000000000007', '90000000-0000-0000-0009-000000000002', 'b', 2, 4, 'b0000000-0000-0000-0000-000000000008', 'K = 100 - 273 = -173', 'Subtracted instead of adding — fundamental sign error in Kelvin conversion'),
  ('a0000009-0001-0000-0000-000000000008', '90000000-0000-0000-0009-000000000002', 'c', 3, 10, 'b0000000-0000-0000-0000-000000000008', 'Heat makes things hot. Temperature is how hot it is.', 'Circular definition — does not distinguish between energy transfer and thermal property'),
  ('a0000009-0001-0000-0000-000000000009', '90000000-0000-0000-0009-000000000003', 'a', 3, 8, 'b0000000-0000-0000-0000-000000000009', 'v = 50 + 0.4 = 50.4', 'Added frequency and wavelength instead of multiplying — does not know v = fλ'),
  ('a0000009-0001-0000-0000-00000000000a', '90000000-0000-0000-0009-000000000003', 'b', 4, 12, 'b0000000-0000-0000-0000-00000000000a', 'V = I × R, so I = 12 × 4 = 48', 'Multiplied instead of dividing — knows the formula letters but not how to rearrange');

-- Irene Test 2: 60 marks (8 MCQ + 3 open)
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type, mcq_options) VALUES
  ('90000000-0000-0000-0009-000000000004', '80000000-0000-0000-0009-000000000002', 'Q1', 8, 'MCQ Section (8 marks)', 'MCQ', '[{"key":"A","text":"A"},{"key":"B","text":"B"},{"key":"C","text":"C"},{"key":"D","text":"D"}]');
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0009-000000000005', '80000000-0000-0000-0009-000000000002', 'Q2', 16, 'Mechanics: KE and Newtons Laws', 'OPEN'),
  ('90000000-0000-0000-0009-000000000006', '80000000-0000-0000-0009-000000000002', 'Q3', 16, 'Waves and Electricity', 'OPEN'),
  ('90000000-0000-0000-0009-000000000007', '80000000-0000-0000-0009-000000000002', 'Q4', 20, 'Thermodynamics extended', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000009-0002-0000-0000-000000000001', '90000000-0000-0000-0009-000000000004', '1', 1, 1, 'b0000000-0000-0000-0000-000000000007', 'B', NULL),
  ('a0000009-0002-0000-0000-000000000002', '90000000-0000-0000-0009-000000000004', '2', 0, 1, 'b0000000-0000-0000-0000-000000000008', 'D', NULL),
  ('a0000009-0002-0000-0000-000000000003', '90000000-0000-0000-0009-000000000004', '3', 0, 1, 'b0000000-0000-0000-0000-000000000009', 'A', NULL),
  ('a0000009-0002-0000-0000-000000000004', '90000000-0000-0000-0009-000000000004', '4', 0, 1, 'b0000000-0000-0000-0000-00000000000a', 'C', 'Confused parallel and series circuit diagrams'),
  ('a0000009-0002-0000-0000-000000000005', '90000000-0000-0000-0009-000000000004', '5', 1, 1, 'b0000000-0000-0000-0000-000000000007', 'A', NULL),
  ('a0000009-0002-0000-0000-000000000006', '90000000-0000-0000-0009-000000000004', '6', 0, 1, 'b0000000-0000-0000-0000-000000000008', 'A', NULL),
  ('a0000009-0002-0000-0000-000000000007', '90000000-0000-0000-0009-000000000004', '7', 0, 1, 'b0000000-0000-0000-0000-000000000009', 'B', NULL),
  ('a0000009-0002-0000-0000-000000000008', '90000000-0000-0000-0009-000000000004', '8', 0, 1, 'b0000000-0000-0000-0000-00000000000a', 'D', NULL),
  ('a0000009-0002-0000-0000-000000000009', '90000000-0000-0000-0009-000000000005', 'a', 2, 6, 'b0000000-0000-0000-0000-000000000007', 'KE = mv² = 5 × 16 = 80 J', 'Forgot the ½ in KE formula — common error'),
  ('a0000009-0002-0000-0000-00000000000a', '90000000-0000-0000-0009-000000000005', 'b', 3, 10, 'b0000000-0000-0000-0000-000000000007', 'Force makes things move', 'Too vague — needs to state F=ma and give a numerical example'),
  ('a0000009-0002-0000-0000-00000000000b', '90000000-0000-0000-0009-000000000006', 'a', 2, 8, 'b0000000-0000-0000-0000-000000000009', 'Light goes through lens and makes image', 'No mention of focal point, convergence, or real/virtual image'),
  ('a0000009-0002-0000-0000-00000000000c', '90000000-0000-0000-0009-000000000006', 'b', 3, 8, 'b0000000-0000-0000-0000-00000000000a', 'P = V × R = 12 × 4 = 48', 'Used wrong formula — P = IV not VR. Does not understand power equation'),
  ('a0000009-0002-0000-0000-00000000000d', '90000000-0000-0000-0009-000000000007', 'a', 4, 10, 'b0000000-0000-0000-0000-000000000008', 'Q = m × c × T = 2 × 4200 × 30 = 252000', 'Got correct answer but wrote T instead of ΔT — does not understand change in temperature concept'),
  ('a0000009-0002-0000-0000-00000000000e', '90000000-0000-0000-0009-000000000007', 'b', 2, 10, 'b0000000-0000-0000-0000-000000000008', 'Hot things cool down', 'No mention of energy conservation, thermal equilibrium, or direction of heat flow');

-- Irene Test 3: 40 marks (4 MCQ + 2 open)
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type, mcq_options) VALUES
  ('90000000-0000-0000-0009-000000000008', '80000000-0000-0000-0009-000000000003', 'Q1', 4, 'MCQ', 'MCQ', '[{"key":"A","text":"A"},{"key":"B","text":"B"},{"key":"C","text":"C"},{"key":"D","text":"D"}]');
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-0009-000000000009', '80000000-0000-0000-0009-000000000003', 'Q2', 18, 'Mechanics + Waves', 'OPEN'),
  ('90000000-0000-0000-0009-00000000000a', '80000000-0000-0000-0009-000000000003', 'Q3', 18, 'Thermo + Electricity', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a0000009-0003-0000-0000-000000000001', '90000000-0000-0000-0009-000000000008', '1', 0, 1, 'b0000000-0000-0000-0000-000000000007', 'C', NULL),
  ('a0000009-0003-0000-0000-000000000002', '90000000-0000-0000-0009-000000000008', '2', 1, 1, 'b0000000-0000-0000-0000-000000000009', 'A', NULL),
  ('a0000009-0003-0000-0000-000000000003', '90000000-0000-0000-0009-000000000008', '3', 0, 1, 'b0000000-0000-0000-0000-00000000000a', 'D', NULL),
  ('a0000009-0003-0000-0000-000000000004', '90000000-0000-0000-0009-000000000008', '4', 0, 1, 'b0000000-0000-0000-0000-000000000008', 'B', 'Confused boiling and evaporation'),
  ('a0000009-0003-0000-0000-000000000005', '90000000-0000-0000-0009-000000000009', 'a', 3, 8, 'b0000000-0000-0000-0000-000000000007', 'PE = m × g × h = 2 × 10 = 20', 'Forgot to include height — only multiplied mass × g'),
  ('a0000009-0003-0000-0000-000000000006', '90000000-0000-0000-0009-000000000009', 'b', 2, 10, 'b0000000-0000-0000-0000-000000000009', 'Diffraction is when light bends', 'Incomplete — no mention of gap size, wavelength relationship, or wave spreading'),
  ('a0000009-0003-0000-0000-000000000007', '90000000-0000-0000-0009-00000000000a', 'a', 3, 8, 'b0000000-0000-0000-0000-000000000008', 'Efficiency = output / input = 300/400 = 0.75', 'Correct ratio but forgot to multiply by 100 for percentage'),
  ('a0000009-0003-0000-0000-000000000008', '90000000-0000-0000-0009-00000000000a', 'b', 4, 10, 'b0000000-0000-0000-0000-00000000000a', 'R = 4 + 6 = 10. V = 12. I = 12/10 = 1.2A', 'Correct for series — some improvement. Still did not attempt parallel');

-- ============================================================
-- PHYSICS CLASS — StrugglingStudent Jack (student 10): 16/45, 22/60, 14/40
-- ============================================================
INSERT INTO test_scores (id, student_id, class_id, teacher_id, test_name, test_date, overall_score, max_score, test_source, created_by, updated_by) VALUES
  ('80000000-0000-0000-000a-000000000001', '50000000-0000-0000-0000-000000000010', '60000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', 'Centre Quiz 1',  '2025-02-12', 16, 45, 'CENTRE', '10000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003'),
  ('80000000-0000-0000-000a-000000000002', '50000000-0000-0000-0000-000000000010', '60000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', 'School Midterm', '2025-03-18', 19, 60, 'SCHOOL', '10000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003'),
  ('80000000-0000-0000-000a-000000000003', '50000000-0000-0000-0000-000000000010', '60000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', 'Centre Quiz 2',  '2025-04-22', 14, 40, 'CENTRE', '10000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003');

-- Jack Test 1: 45 marks (5 MCQ + 2 open)
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type, mcq_options) VALUES
  ('90000000-0000-0000-000a-000000000001', '80000000-0000-0000-000a-000000000001', 'Q1', 5, 'MCQ Section', 'MCQ', '[{"key":"A","text":"A"},{"key":"B","text":"B"},{"key":"C","text":"C"},{"key":"D","text":"D"}]');
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-000a-000000000002', '80000000-0000-0000-000a-000000000001', 'Q2', 20, '(a) Calculate final velocity. (b) Convert 100°C to Kelvin. (c) Explain heat vs temperature.', 'OPEN'),
  ('90000000-0000-0000-000a-000000000003', '80000000-0000-0000-000a-000000000001', 'Q3', 20, '(a) Wave speed calculation. (b) Ohms law problem.', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a000000a-0001-0000-0000-000000000001', '90000000-0000-0000-000a-000000000001', '1', 0, 1, 'b0000000-0000-0000-0000-000000000007', 'D', NULL),
  ('a000000a-0001-0000-0000-000000000002', '90000000-0000-0000-000a-000000000001', '2', 1, 1, 'b0000000-0000-0000-0000-000000000008', 'A', NULL),
  ('a000000a-0001-0000-0000-000000000003', '90000000-0000-0000-000a-000000000001', '3', 0, 1, 'b0000000-0000-0000-0000-000000000009', 'B', NULL),
  ('a000000a-0001-0000-0000-000000000004', '90000000-0000-0000-000a-000000000001', '4', 1, 1, 'b0000000-0000-0000-0000-00000000000a', 'B', NULL),
  ('a000000a-0001-0000-0000-000000000005', '90000000-0000-0000-000a-000000000001', '5', 0, 1, 'b0000000-0000-0000-0000-000000000007', 'C', NULL),
  ('a000000a-0001-0000-0000-000000000006', '90000000-0000-0000-000a-000000000002', 'a', 2, 6, 'b0000000-0000-0000-0000-000000000007', 'v = 8/2 = 4 m/s', 'Divided acceleration by time instead of multiplying — does not understand v = u + at'),
  ('a000000a-0001-0000-0000-000000000007', '90000000-0000-0000-000a-000000000002', 'b', 4, 4, 'b0000000-0000-0000-0000-000000000008', 'K = 100 + 273 = 373 K', NULL),
  ('a000000a-0001-0000-0000-000000000008', '90000000-0000-0000-000a-000000000002', 'c', 2, 10, 'b0000000-0000-0000-0000-000000000008', 'They are the same thing', 'Fundamental misconception — cannot distinguish heat (energy) from temperature (measure)'),
  ('a000000a-0001-0000-0000-000000000009', '90000000-0000-0000-000a-000000000003', 'a', 2, 8, 'b0000000-0000-0000-0000-000000000009', 'v = 50/0.4 = 125', 'Divided instead of multiplying — does not know v = fλ formula'),
  ('a000000a-0001-0000-0000-00000000000a', '90000000-0000-0000-000a-000000000003', 'b', 4, 12, 'b0000000-0000-0000-0000-00000000000a', 'I = V/R = 12/4 = 3A', 'Correct Ohms law application — one of the few formulas he remembers');

-- Jack Test 2: 60 marks (8 MCQ + 3 open)
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type, mcq_options) VALUES
  ('90000000-0000-0000-000a-000000000004', '80000000-0000-0000-000a-000000000002', 'Q1', 8, 'MCQ Section (8 marks)', 'MCQ', '[{"key":"A","text":"A"},{"key":"B","text":"B"},{"key":"C","text":"C"},{"key":"D","text":"D"}]');
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-000a-000000000005', '80000000-0000-0000-000a-000000000002', 'Q2', 16, 'Mechanics: KE and Newtons Laws', 'OPEN'),
  ('90000000-0000-0000-000a-000000000006', '80000000-0000-0000-000a-000000000002', 'Q3', 16, 'Waves and Electricity', 'OPEN'),
  ('90000000-0000-0000-000a-000000000007', '80000000-0000-0000-000a-000000000002', 'Q4', 20, 'Thermodynamics extended', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a000000a-0002-0000-0000-000000000001', '90000000-0000-0000-000a-000000000004', '1', 1, 1, 'b0000000-0000-0000-0000-000000000007', 'B', NULL),
  ('a000000a-0002-0000-0000-000000000002', '90000000-0000-0000-000a-000000000004', '2', 0, 1, 'b0000000-0000-0000-0000-000000000008', 'A', NULL),
  ('a000000a-0002-0000-0000-000000000003', '90000000-0000-0000-000a-000000000004', '3', 0, 1, 'b0000000-0000-0000-0000-000000000009', 'D', 'Cannot identify wave types from diagrams'),
  ('a000000a-0002-0000-0000-000000000004', '90000000-0000-0000-000a-000000000004', '4', 1, 1, 'b0000000-0000-0000-0000-00000000000a', 'B', NULL),
  ('a000000a-0002-0000-0000-000000000005', '90000000-0000-0000-000a-000000000004', '5', 0, 1, 'b0000000-0000-0000-0000-000000000007', 'C', NULL),
  ('a000000a-0002-0000-0000-000000000006', '90000000-0000-0000-000a-000000000004', '6', 0, 1, 'b0000000-0000-0000-0000-000000000008', 'D', NULL),
  ('a000000a-0002-0000-0000-000000000007', '90000000-0000-0000-000a-000000000004', '7', 1, 1, 'b0000000-0000-0000-0000-000000000009', 'A', NULL),
  ('a000000a-0002-0000-0000-000000000008', '90000000-0000-0000-000a-000000000004', '8', 0, 1, 'b0000000-0000-0000-0000-00000000000a', 'C', NULL),
  ('a000000a-0002-0000-0000-000000000009', '90000000-0000-0000-000a-000000000005', 'a', 3, 6, 'b0000000-0000-0000-0000-000000000007', 'KE = ½ × 5 × 4 = 10 J', 'Used v instead of v² — does not understand squaring in KE formula'),
  ('a000000a-0002-0000-0000-00000000000a', '90000000-0000-0000-000a-000000000005', 'b', 2, 10, 'b0000000-0000-0000-0000-000000000007', 'Newton said force = mass × speed', 'Confused acceleration with speed — fundamental gap in mechanics'),
  ('a000000a-0002-0000-0000-00000000000b', '90000000-0000-0000-000a-000000000006', 'a', 3, 8, 'b0000000-0000-0000-0000-000000000009', 'Lens makes things bigger', 'No physics terminology — no mention of focal point, real/virtual, convergence'),
  ('a000000a-0002-0000-0000-00000000000c', '90000000-0000-0000-000a-000000000006', 'b', 3, 8, 'b0000000-0000-0000-0000-00000000000a', 'P = I × V. I = 3. P = 3 × 12 = 36W', 'Correct answer but did not show how I was obtained from V/R'),
  ('a000000a-0002-0000-0000-00000000000d', '90000000-0000-0000-000a-000000000007', 'a', 4, 10, 'b0000000-0000-0000-0000-000000000008', 'Q = 2 × 4200 × 30 = 252000 J', 'Correct calculation but did not write Q = mcΔT formula first — lost method marks'),
  ('a000000a-0002-0000-0000-00000000000e', '90000000-0000-0000-000a-000000000007', 'b', 1, 10, 'b0000000-0000-0000-0000-000000000008', 'Energy goes away', 'Contradicts conservation of energy — major misconception');

-- Jack Test 3: 40 marks (4 MCQ + 2 open)
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type, mcq_options) VALUES
  ('90000000-0000-0000-000a-000000000008', '80000000-0000-0000-000a-000000000003', 'Q1', 4, 'MCQ', 'MCQ', '[{"key":"A","text":"A"},{"key":"B","text":"B"},{"key":"C","text":"C"},{"key":"D","text":"D"}]');
INSERT INTO questions (id, test_score_id, question_number, max_score, question_text, question_type) VALUES
  ('90000000-0000-0000-000a-000000000009', '80000000-0000-0000-000a-000000000003', 'Q2', 18, 'Mechanics + Waves', 'OPEN'),
  ('90000000-0000-0000-000a-00000000000a', '80000000-0000-0000-000a-000000000003', 'Q3', 18, 'Thermo + Electricity', 'OPEN');
INSERT INTO sub_questions (id, question_id, sub_question_label, score, max_score, topic_id, student_answer, teacher_remarks) VALUES
  ('a000000a-0003-0000-0000-000000000001', '90000000-0000-0000-000a-000000000008', '1', 1, 1, 'b0000000-0000-0000-0000-000000000007', 'B', NULL),
  ('a000000a-0003-0000-0000-000000000002', '90000000-0000-0000-000a-000000000008', '2', 0, 1, 'b0000000-0000-0000-0000-000000000009', 'C', NULL),
  ('a000000a-0003-0000-0000-000000000003', '90000000-0000-0000-000a-000000000008', '3', 0, 1, 'b0000000-0000-0000-0000-00000000000a', 'A', 'Does not understand resistance in parallel circuits'),
  ('a000000a-0003-0000-0000-000000000004', '90000000-0000-0000-000a-000000000008', '4', 0, 1, 'b0000000-0000-0000-0000-000000000008', 'D', NULL),
  ('a000000a-0003-0000-0000-000000000005', '90000000-0000-0000-000a-000000000009', 'a', 3, 8, 'b0000000-0000-0000-0000-000000000007', 'PE = mgh = 2 × 10 × 10 = 200 J', 'Correct — memorised this formula well'),
  ('a000000a-0003-0000-0000-000000000006', '90000000-0000-0000-000a-000000000009', 'b', 2, 10, 'b0000000-0000-0000-0000-000000000009', 'Waves go around things', 'Knows the basic idea of diffraction but no detail on wavelength or gap size'),
  ('a000000a-0003-0000-0000-000000000007', '90000000-0000-0000-000a-00000000000a', 'a', 4, 8, 'b0000000-0000-0000-0000-000000000008', 'Efficiency = 300/400 = 0.75 = 75%', 'Correct — improved from last test'),
  ('a000000a-0003-0000-0000-000000000008', '90000000-0000-0000-000a-00000000000a', 'b', 4, 10, 'b0000000-0000-0000-0000-00000000000a', 'R total = 4 + 6 = 10. I = 12/10 = 1.2A', 'Correct for series. Did not attempt parallel — consistent weakness');
