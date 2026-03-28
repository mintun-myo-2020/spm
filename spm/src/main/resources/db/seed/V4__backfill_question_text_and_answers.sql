-- V4: Backfill question text, student answers, and MCQ examples
-- Each question has 2 sub-questions mapped to specific topics.
-- Sub-q text and answers are aligned to their topic.

-- ============================================================
-- MATH QUIZ 1 (all 6 students)
-- Q1: sub-a = Algebra (b..01), sub-b = Geometry (b..02)
-- Q2: sub-a = Statistics (b..03), sub-b = Trigonometry (b..04) → MCQ
-- ============================================================

-- Q1: Multi-part question (OPEN)
-- Part (a) tests Algebra, Part (b) tests Geometry
UPDATE questions SET question_text = '(a) Solve for x: 3x + 7 = 22. Show your working. (b) Calculate the area of a triangle with base 10cm and height 6cm.'
WHERE id IN (
  '90000000-0000-0000-0001-000000000001', '90000000-0000-0000-0002-000000000001',
  '90000000-0000-0000-0003-000000000001', '90000000-0000-0000-0004-000000000001',
  '90000000-0000-0000-0005-000000000001', '90000000-0000-0000-0006-000000000001'
);

-- Q2: MCQ covering Statistics (sub-a) and Trigonometry (sub-b)
UPDATE questions SET question_text = '(a) What is the median of: 3, 7, 9, 12, 15? (b) In a right triangle, sin(θ) = opposite/hypotenuse. If opposite = 3 and hypotenuse = 5, what is sin(θ)?',
  question_type = 'MCQ',
  mcq_options = '[{"key":"A","text":"Median=9, sin(θ)=0.6"},{"key":"B","text":"Median=7, sin(θ)=0.6"},{"key":"C","text":"Median=9, sin(θ)=0.8"},{"key":"D","text":"Median=12, sin(θ)=1.67"}]'
WHERE id IN (
  '90000000-0000-0000-0001-000000000002', '90000000-0000-0000-0002-000000000002',
  '90000000-0000-0000-0003-000000000002', '90000000-0000-0000-0004-000000000002',
  '90000000-0000-0000-0005-000000000002', '90000000-0000-0000-0006-000000000002'
);

-- Quiz 1 Q1 student answers: sub-a = Algebra answer, sub-b = Geometry answer
-- Alice (good)
UPDATE sub_questions SET student_answer = '3x = 22 - 7 = 15, x = 5' WHERE id = 'a0000001-0001-0000-0000-000000000001';
UPDATE sub_questions SET student_answer = 'Area = ½ × 10 × 6 = 30 cm²' WHERE id = 'a0000001-0001-0000-0000-000000000002';
-- Ben (good)
UPDATE sub_questions SET student_answer = '3x + 7 = 22, 3x = 15, x = 5' WHERE id = 'a0000002-0001-0000-0000-000000000001';
UPDATE sub_questions SET student_answer = '½ × base × height = ½ × 10 × 6 = 30 cm²' WHERE id = 'a0000002-0001-0000-0000-000000000002';
-- Clara (good)
UPDATE sub_questions SET student_answer = 'x = (22-7)/3 = 5' WHERE id = 'a0000003-0001-0000-0000-000000000001';
UPDATE sub_questions SET student_answer = '½ × 10 × 6 = 30 cm²' WHERE id = 'a0000003-0001-0000-0000-000000000002';
-- Daniel (good)
UPDATE sub_questions SET student_answer = '3x = 15 so x = 5' WHERE id = 'a0000004-0001-0000-0000-000000000001';
UPDATE sub_questions SET student_answer = 'Area = 0.5 × 10 × 6 = 30 cm²' WHERE id = 'a0000004-0001-0000-0000-000000000002';
-- Ethan (struggling)
UPDATE sub_questions SET student_answer = 'x = 22/3 = 7.3' WHERE id = 'a0000005-0001-0000-0000-000000000001';
UPDATE sub_questions SET student_answer = '10 × 6 = 60 cm²' WHERE id = 'a0000005-0001-0000-0000-000000000002';
-- Fiona (struggling)
UPDATE sub_questions SET student_answer = '3x = 29, x = 9.6' WHERE id = 'a0000006-0001-0000-0000-000000000001';
UPDATE sub_questions SET student_answer = 'base × height = 60' WHERE id = 'a0000006-0001-0000-0000-000000000002';

-- Quiz 1 Q2 (MCQ) answers: sub-a = Statistics, sub-b = Trigonometry
-- Correct answer is A (Median=9, sin(θ)=0.6)
-- Good students → A, struggling → wrong
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a0000001-0001-0000-0000-000000000003';
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a0000001-0001-0000-0000-000000000004';
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a0000002-0001-0000-0000-000000000003';
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a0000002-0001-0000-0000-000000000004';
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a0000003-0001-0000-0000-000000000003';
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a0000003-0001-0000-0000-000000000004';
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a0000004-0001-0000-0000-000000000003';
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a0000004-0001-0000-0000-000000000004';
UPDATE sub_questions SET student_answer = 'D' WHERE id = 'a0000005-0001-0000-0000-000000000003';
UPDATE sub_questions SET student_answer = 'D' WHERE id = 'a0000005-0001-0000-0000-000000000004';
UPDATE sub_questions SET student_answer = 'C' WHERE id = 'a0000006-0001-0000-0000-000000000003';
UPDATE sub_questions SET student_answer = 'C' WHERE id = 'a0000006-0001-0000-0000-000000000004';

-- ============================================================
-- MATH MIDTERM (all 6 students)
-- Q1: sub-a = Algebra (b..01), sub-b = Geometry (b..02)
-- Q2: sub-a = Statistics (b..03) / Algebra (b..01), sub-b = Trigonometry (b..04) / Statistics (b..03)
-- Note: V2 maps midterm Q1 subs to Algebra+Geometry, Q2 subs to Statistics/Algebra + Trig/Statistics
-- Actually checking V2: Midterm Q1 sub-a→Algebra, sub-b→Geometry; Q2 sub-a→Statistics, sub-b→Algebra
-- Let me re-check...
-- Alice midterm: Q1(a→ALG, b→GEO), Q2(a→STAT, b→ALG)
-- ============================================================

-- Midterm Q1: (a) Algebra, (b) Geometry
UPDATE questions SET question_text = '(a) Simplify: 2(3x - 4) + 5x. (b) Find the perimeter of a rectangle with length 12cm and width 7cm.'
WHERE id IN (
  '90000000-0000-0000-0001-000000000003', '90000000-0000-0000-0002-000000000003',
  '90000000-0000-0000-0003-000000000003', '90000000-0000-0000-0004-000000000003',
  '90000000-0000-0000-0005-000000000003', '90000000-0000-0000-0006-000000000003'
);

-- Midterm Q2: (a) Statistics, (b) Algebra
UPDATE questions SET question_text = '(a) Calculate the mean of: 8, 12, 15, 9, 6. (b) Factorise: x² + 5x + 6.'
WHERE id IN (
  '90000000-0000-0000-0001-000000000004', '90000000-0000-0000-0002-000000000004',
  '90000000-0000-0000-0003-000000000004', '90000000-0000-0000-0004-000000000004',
  '90000000-0000-0000-0005-000000000004', '90000000-0000-0000-0006-000000000004'
);

-- Midterm Q1 answers: sub-a = Algebra, sub-b = Geometry
-- Alice
UPDATE sub_questions SET student_answer = '6x - 8 + 5x = 11x - 8' WHERE id = 'a0000001-0001-0000-0000-000000000005';
UPDATE sub_questions SET student_answer = 'P = 2(12 + 7) = 38 cm' WHERE id = 'a0000001-0001-0000-0000-000000000006';
-- Ben
UPDATE sub_questions SET student_answer = '= 6x - 8 + 5x = 11x - 8' WHERE id = 'a0000002-0001-0000-0000-000000000005';
UPDATE sub_questions SET student_answer = '2 × 12 + 2 × 7 = 38 cm' WHERE id = 'a0000002-0001-0000-0000-000000000006';
-- Clara
UPDATE sub_questions SET student_answer = '2(3x-4)+5x = 11x - 8' WHERE id = 'a0000003-0001-0000-0000-000000000005';
UPDATE sub_questions SET student_answer = '2(12+7) = 38 cm' WHERE id = 'a0000003-0001-0000-0000-000000000006';
-- Daniel
UPDATE sub_questions SET student_answer = '11x - 8' WHERE id = 'a0000004-0001-0000-0000-000000000005';
UPDATE sub_questions SET student_answer = '12+7+12+7 = 38 cm' WHERE id = 'a0000004-0001-0000-0000-000000000006';
-- Ethan
UPDATE sub_questions SET student_answer = '6x - 4 + 5x = 11x - 4' WHERE id = 'a0000005-0001-0000-0000-000000000005';
UPDATE sub_questions SET student_answer = '12 × 7 = 84 cm' WHERE id = 'a0000005-0001-0000-0000-000000000006';
-- Fiona
UPDATE sub_questions SET student_answer = '2 × 3x + 5x = 11x' WHERE id = 'a0000006-0001-0000-0000-000000000005';
UPDATE sub_questions SET student_answer = '12 + 7 = 19 cm' WHERE id = 'a0000006-0001-0000-0000-000000000006';

-- Midterm Q2 answers: sub-a = Statistics, sub-b = Algebra
-- Alice
UPDATE sub_questions SET student_answer = 'Mean = (8+12+15+9+6)/5 = 50/5 = 10' WHERE id = 'a0000001-0001-0000-0000-000000000007';
UPDATE sub_questions SET student_answer = '(x+2)(x+3)' WHERE id = 'a0000001-0001-0000-0000-000000000008';
-- Ben
UPDATE sub_questions SET student_answer = '50 ÷ 5 = 10' WHERE id = 'a0000002-0001-0000-0000-000000000007';
UPDATE sub_questions SET student_answer = '(x+2)(x+3)' WHERE id = 'a0000002-0001-0000-0000-000000000008';
-- Clara
UPDATE sub_questions SET student_answer = 'Sum=50, Mean=10' WHERE id = 'a0000003-0001-0000-0000-000000000007';
UPDATE sub_questions SET student_answer = 'x² + 5x + 6 = (x+2)(x+3)' WHERE id = 'a0000003-0001-0000-0000-000000000008';
-- Daniel
UPDATE sub_questions SET student_answer = 'Mean = 50/5 = 10' WHERE id = 'a0000004-0001-0000-0000-000000000007';
UPDATE sub_questions SET student_answer = '(x+3)(x+2)' WHERE id = 'a0000004-0001-0000-0000-000000000008';
-- Ethan
UPDATE sub_questions SET student_answer = '8+12+15+9+6 = 50, mean = 50/4 = 12.5' WHERE id = 'a0000005-0001-0000-0000-000000000007';
UPDATE sub_questions SET student_answer = '(x+1)(x+6)' WHERE id = 'a0000005-0001-0000-0000-000000000008';
-- Fiona
UPDATE sub_questions SET student_answer = 'Mean = 15 (the middle number)' WHERE id = 'a0000006-0001-0000-0000-000000000007';
UPDATE sub_questions SET student_answer = 'x(x+5) + 6' WHERE id = 'a0000006-0001-0000-0000-000000000008';

-- ============================================================
-- MATH QUIZ 2 (all 6 students)
-- Q1: sub-a = Geometry (b..02), sub-b = Trigonometry (b..04)
-- Q2: sub-a = Algebra (b..01), sub-b = Statistics (b..03) → MCQ
-- ============================================================

-- Quiz 2 Q1: (a) Geometry, (b) Trigonometry
UPDATE questions SET question_text = '(a) Find the area of a circle with radius 7cm (use π ≈ 3.14). (b) In a right triangle with adjacent = 4 and hypotenuse = 5, find cos(θ).'
WHERE id IN (
  '90000000-0000-0000-0001-000000000005', '90000000-0000-0000-0002-000000000005',
  '90000000-0000-0000-0003-000000000005', '90000000-0000-0000-0004-000000000005',
  '90000000-0000-0000-0005-000000000005', '90000000-0000-0000-0006-000000000005'
);

-- Quiz 2 Q2: MCQ — (a) Algebra, (b) Statistics
UPDATE questions SET question_text = 'Which of the following is correct?',
  question_type = 'MCQ',
  mcq_options = '[{"key":"A","text":"2x + 3x = 5x AND mean of {2,4,6} = 4"},{"key":"B","text":"2x + 3x = 6x AND mean of {2,4,6} = 4"},{"key":"C","text":"2x + 3x = 5x AND mean of {2,4,6} = 6"},{"key":"D","text":"2x × 3x = 5x AND mean of {2,4,6} = 3"}]'
WHERE id IN (
  '90000000-0000-0000-0001-000000000006', '90000000-0000-0000-0002-000000000006',
  '90000000-0000-0000-0003-000000000006', '90000000-0000-0000-0004-000000000006',
  '90000000-0000-0000-0005-000000000006', '90000000-0000-0000-0006-000000000006'
);

-- Quiz 2 Q1 answers: sub-a = Geometry, sub-b = Trigonometry
-- Alice
UPDATE sub_questions SET student_answer = 'A = π × 7² = 3.14 × 49 = 153.86 cm²' WHERE id = 'a0000001-0001-0000-0000-000000000009';
UPDATE sub_questions SET student_answer = 'cos(θ) = adjacent/hypotenuse = 4/5 = 0.8' WHERE id = 'a0000001-0001-0000-0000-00000000000a';
-- Ben
UPDATE sub_questions SET student_answer = 'πr² = 3.14 × 49 = 153.86 cm²' WHERE id = 'a0000002-0001-0000-0000-000000000009';
UPDATE sub_questions SET student_answer = 'cos(θ) = 4/5 = 0.8' WHERE id = 'a0000002-0001-0000-0000-00000000000a';
-- Clara
UPDATE sub_questions SET student_answer = '3.14 × 49 = 153.86 cm²' WHERE id = 'a0000003-0001-0000-0000-000000000009';
UPDATE sub_questions SET student_answer = 'cos = adj/hyp = 4/5 = 0.8' WHERE id = 'a0000003-0001-0000-0000-00000000000a';
-- Daniel
UPDATE sub_questions SET student_answer = 'Area = 3.14 × 7 × 7 = 153.86 cm²' WHERE id = 'a0000004-0001-0000-0000-000000000009';
UPDATE sub_questions SET student_answer = '4/5 = 0.8' WHERE id = 'a0000004-0001-0000-0000-00000000000a';
-- Ethan
UPDATE sub_questions SET student_answer = '2 × 3.14 × 7 = 43.96 cm²' WHERE id = 'a0000005-0001-0000-0000-000000000009';
UPDATE sub_questions SET student_answer = 'cos = 5/4 = 1.25' WHERE id = 'a0000005-0001-0000-0000-00000000000a';
-- Fiona
UPDATE sub_questions SET student_answer = '3.14 × 7 = 21.98 cm²' WHERE id = 'a0000006-0001-0000-0000-000000000009';
UPDATE sub_questions SET student_answer = 'cos = opposite/hypotenuse = 3/5' WHERE id = 'a0000006-0001-0000-0000-00000000000a';

-- Quiz 2 Q2 (MCQ) answers — A is correct
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a0000001-0001-0000-0000-00000000000b';
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a0000001-0001-0000-0000-00000000000c';
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a0000002-0001-0000-0000-00000000000b';
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a0000002-0001-0000-0000-00000000000c';
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a0000003-0001-0000-0000-00000000000b';
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a0000003-0001-0000-0000-00000000000c';
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a0000004-0001-0000-0000-00000000000b';
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a0000004-0001-0000-0000-00000000000c';
UPDATE sub_questions SET student_answer = 'B' WHERE id = 'a0000005-0001-0000-0000-00000000000b';
UPDATE sub_questions SET student_answer = 'B' WHERE id = 'a0000005-0001-0000-0000-00000000000c';
UPDATE sub_questions SET student_answer = 'D' WHERE id = 'a0000006-0001-0000-0000-00000000000b';
UPDATE sub_questions SET student_answer = 'D' WHERE id = 'a0000006-0001-0000-0000-00000000000c';

-- ============================================================
-- PHYSICS QUIZ 1 (4 students: Grace, Henry, Irene, Jack)
-- Q1: sub-a = Mechanics (b..07), sub-b = Thermodynamics (b..08)
-- Q2: sub-a = Waves (b..09), sub-b = Electricity (b..0a) → MCQ
-- ============================================================

-- Physics Quiz 1 Q1: (a) Mechanics, (b) Thermodynamics
UPDATE questions SET question_text = '(a) A car accelerates from rest at 2 m/s² for 8 seconds. Calculate its final velocity. (b) Convert 100°C to Kelvin.'
WHERE id IN (
  '90000000-0000-0000-0007-000000000001', '90000000-0000-0000-0008-000000000001',
  '90000000-0000-0000-0009-000000000001', '90000000-0000-0000-000a-000000000001'
);

-- Physics Quiz 1 Q2: MCQ — (a) Waves, (b) Electricity
UPDATE questions SET question_text = 'Which statement is correct about waves and circuits?',
  question_type = 'MCQ',
  mcq_options = '[{"key":"A","text":"Sound waves are transverse AND V=IR"},{"key":"B","text":"Sound waves are longitudinal AND V=IR"},{"key":"C","text":"Sound waves are longitudinal AND V=I/R"},{"key":"D","text":"Light waves are longitudinal AND V=IR"}]'
WHERE id IN (
  '90000000-0000-0000-0007-000000000002', '90000000-0000-0000-0008-000000000002',
  '90000000-0000-0000-0009-000000000002', '90000000-0000-0000-000a-000000000002'
);

-- Physics Quiz 1 Q1 answers
-- Grace (good)
UPDATE sub_questions SET student_answer = 'v = u + at = 0 + 2(8) = 16 m/s' WHERE id = 'a0000007-0001-0000-0000-000000000001';
UPDATE sub_questions SET student_answer = 'K = 100 + 273 = 373 K' WHERE id = 'a0000007-0001-0000-0000-000000000002';
-- Henry (good)
UPDATE sub_questions SET student_answer = 'v = 0 + 2×8 = 16 m/s' WHERE id = 'a0000008-0001-0000-0000-000000000001';
UPDATE sub_questions SET student_answer = '100 + 273 = 373 K' WHERE id = 'a0000008-0001-0000-0000-000000000002';
-- Irene (struggling)
UPDATE sub_questions SET student_answer = 'v = 2 × 8 = 16' WHERE id = 'a0000009-0001-0000-0000-000000000001';
UPDATE sub_questions SET student_answer = '100 × 273 = 27300' WHERE id = 'a0000009-0001-0000-0000-000000000002';
-- Jack (struggling)
UPDATE sub_questions SET student_answer = 'v = 2 + 8 = 10 m/s' WHERE id = 'a000000a-0001-0000-0000-000000000001';
UPDATE sub_questions SET student_answer = '100 - 273 = -173 K' WHERE id = 'a000000a-0001-0000-0000-000000000002';

-- Physics Quiz 1 Q2 (MCQ) — B is correct
UPDATE sub_questions SET student_answer = 'B' WHERE id = 'a0000007-0001-0000-0000-000000000003';
UPDATE sub_questions SET student_answer = 'B' WHERE id = 'a0000007-0001-0000-0000-000000000004';
UPDATE sub_questions SET student_answer = 'B' WHERE id = 'a0000008-0001-0000-0000-000000000003';
UPDATE sub_questions SET student_answer = 'B' WHERE id = 'a0000008-0001-0000-0000-000000000004';
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a0000009-0001-0000-0000-000000000003';
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a0000009-0001-0000-0000-000000000004';
UPDATE sub_questions SET student_answer = 'D' WHERE id = 'a000000a-0001-0000-0000-000000000003';
UPDATE sub_questions SET student_answer = 'D' WHERE id = 'a000000a-0001-0000-0000-000000000004';

-- ============================================================
-- PHYSICS MIDTERM (4 students)
-- Q1: sub-a = Mechanics (b..07), sub-b = Thermodynamics (b..08)
-- Q2: sub-a = Waves (b..09), sub-b = Mechanics (b..07)
-- ============================================================

-- Physics Midterm Q1: (a) Mechanics, (b) Thermodynamics
UPDATE questions SET question_text = '(a) Calculate the kinetic energy of a 5kg object moving at 4 m/s. (b) Explain the difference between heat and temperature.'
WHERE id IN (
  '90000000-0000-0000-0007-000000000003', '90000000-0000-0000-0008-000000000003',
  '90000000-0000-0000-0009-000000000003', '90000000-0000-0000-000a-000000000003'
);

-- Physics Midterm Q2: (a) Waves, (b) Mechanics
UPDATE questions SET question_text = '(a) A wave has frequency 50 Hz and wavelength 0.4 m. Calculate its speed. (b) State Newton''s Second Law and give the formula.'
WHERE id IN (
  '90000000-0000-0000-0007-000000000004', '90000000-0000-0000-0008-000000000004',
  '90000000-0000-0000-0009-000000000004', '90000000-0000-0000-000a-000000000004'
);

-- Midterm Q1 answers
-- Grace
UPDATE sub_questions SET student_answer = 'KE = ½mv² = ½ × 5 × 16 = 40 J' WHERE id = 'a0000007-0001-0000-0000-000000000005';
UPDATE sub_questions SET student_answer = 'Heat is energy transfer between objects; temperature measures average kinetic energy of particles.' WHERE id = 'a0000007-0001-0000-0000-000000000006';
-- Henry
UPDATE sub_questions SET student_answer = '½ × 5 × 4² = 40 J' WHERE id = 'a0000008-0001-0000-0000-000000000005';
UPDATE sub_questions SET student_answer = 'Heat = energy flow, temperature = measure of hotness. E.g. ice at 0°C absorbs heat but temp stays same.' WHERE id = 'a0000008-0001-0000-0000-000000000006';
-- Irene
UPDATE sub_questions SET student_answer = 'KE = mv = 5 × 4 = 20 J' WHERE id = 'a0000009-0001-0000-0000-000000000005';
UPDATE sub_questions SET student_answer = 'Heat is hot and temperature is cold' WHERE id = 'a0000009-0001-0000-0000-000000000006';
-- Jack
UPDATE sub_questions SET student_answer = 'KE = m × v = 5 × 4 = 20' WHERE id = 'a000000a-0001-0000-0000-000000000005';
UPDATE sub_questions SET student_answer = 'They are the same thing' WHERE id = 'a000000a-0001-0000-0000-000000000006';

-- Midterm Q2 answers
-- Grace
UPDATE sub_questions SET student_answer = 'v = fλ = 50 × 0.4 = 20 m/s' WHERE id = 'a0000007-0001-0000-0000-000000000007';
UPDATE sub_questions SET student_answer = 'F = ma. Force equals mass times acceleration.' WHERE id = 'a0000007-0001-0000-0000-000000000008';
-- Henry
UPDATE sub_questions SET student_answer = 'Speed = 50 × 0.4 = 20 m/s' WHERE id = 'a0000008-0001-0000-0000-000000000007';
UPDATE sub_questions SET student_answer = 'Newton''s 2nd: F = ma' WHERE id = 'a0000008-0001-0000-0000-000000000008';
-- Irene
UPDATE sub_questions SET student_answer = 'v = 50/0.4 = 125 m/s' WHERE id = 'a0000009-0001-0000-0000-000000000007';
UPDATE sub_questions SET student_answer = 'Every action has equal reaction' WHERE id = 'a0000009-0001-0000-0000-000000000008';
-- Jack
UPDATE sub_questions SET student_answer = 'v = 50 + 0.4 = 50.4' WHERE id = 'a000000a-0001-0000-0000-000000000007';
UPDATE sub_questions SET student_answer = 'F = mv' WHERE id = 'a000000a-0001-0000-0000-000000000008';

-- ============================================================
-- PHYSICS QUIZ 2 (4 students)
-- Q1: sub-a = Thermodynamics (b..08), sub-b = Electricity (b..0a) → MCQ
-- Q2: sub-a = Mechanics (b..07), sub-b = Waves (b..09)
-- ============================================================

-- Physics Quiz 2 Q1: MCQ — (a) Thermodynamics, (b) Electricity
UPDATE questions SET question_text = 'Which statement about energy is correct?',
  question_type = 'MCQ',
  mcq_options = '[{"key":"A","text":"Specific heat capacity is measured in J/(kg·K) AND current is measured in Amperes"},{"key":"B","text":"Specific heat capacity is measured in Watts AND current is measured in Amperes"},{"key":"C","text":"Specific heat capacity is measured in J/(kg·K) AND current is measured in Volts"},{"key":"D","text":"Specific heat capacity is measured in Joules AND current is measured in Ohms"}]'
WHERE id IN (
  '90000000-0000-0000-0007-000000000005', '90000000-0000-0000-0008-000000000005',
  '90000000-0000-0000-0009-000000000005', '90000000-0000-0000-000a-000000000005'
);

-- Physics Quiz 2 Q2: (a) Mechanics, (b) Waves
UPDATE questions SET question_text = '(a) A 2kg ball is dropped from 10m. Calculate its potential energy at the top (g=10 m/s²). (b) Describe how a convex lens forms a real image.'
WHERE id IN (
  '90000000-0000-0000-0007-000000000006', '90000000-0000-0000-0008-000000000006',
  '90000000-0000-0000-0009-000000000006', '90000000-0000-0000-000a-000000000006'
);

-- Quiz 2 Q1 (MCQ) — A is correct
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a0000007-0001-0000-0000-000000000009';
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a0000007-0001-0000-0000-00000000000a';
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a0000008-0001-0000-0000-000000000009';
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a0000008-0001-0000-0000-00000000000a';
UPDATE sub_questions SET student_answer = 'B' WHERE id = 'a0000009-0001-0000-0000-000000000009';
UPDATE sub_questions SET student_answer = 'B' WHERE id = 'a0000009-0001-0000-0000-00000000000a';
UPDATE sub_questions SET student_answer = 'D' WHERE id = 'a000000a-0001-0000-0000-000000000009';
UPDATE sub_questions SET student_answer = 'D' WHERE id = 'a000000a-0001-0000-0000-00000000000a';

-- Quiz 2 Q2 answers: sub-a = Mechanics, sub-b = Waves
-- Grace
UPDATE sub_questions SET student_answer = 'PE = mgh = 2 × 10 × 10 = 200 J' WHERE id = 'a0000007-0001-0000-0000-00000000000b';
UPDATE sub_questions SET student_answer = 'Parallel ray refracts through focal point, central ray passes straight. They converge to form inverted real image.' WHERE id = 'a0000007-0001-0000-0000-00000000000c';
-- Henry
UPDATE sub_questions SET student_answer = 'mgh = 2 × 10 × 10 = 200 J' WHERE id = 'a0000008-0001-0000-0000-00000000000b';
UPDATE sub_questions SET student_answer = 'Light converges after passing through lens, forming real inverted image at focal point.' WHERE id = 'a0000008-0001-0000-0000-00000000000c';
-- Irene
UPDATE sub_questions SET student_answer = 'PE = mg = 2 × 10 = 20 J' WHERE id = 'a0000009-0001-0000-0000-00000000000b';
UPDATE sub_questions SET student_answer = 'The lens makes the image bigger' WHERE id = 'a0000009-0001-0000-0000-00000000000c';
-- Jack
UPDATE sub_questions SET student_answer = 'PE = m + g + h = 22' WHERE id = 'a000000a-0001-0000-0000-00000000000b';
UPDATE sub_questions SET student_answer = 'Light goes through and makes a picture' WHERE id = 'a000000a-0001-0000-0000-00000000000c';
