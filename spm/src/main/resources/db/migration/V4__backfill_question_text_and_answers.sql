-- V4: Backfill question text, student answers, and add MCQ examples to seed data
-- This populates the columns added in V3 so the UI has data to display.

-- ============================================================
-- MATH CLASS — Quiz 1 questions (all students share the same test)
-- Q1 questions across all Math Quiz 1 test scores → Algebra (OPEN)
-- Q2 questions across all Math Quiz 1 test scores → Convert to MCQ
-- ============================================================

-- Math Quiz 1 — Q1 (OPEN) for all students
UPDATE questions SET question_text = 'Solve for x: 3x + 7 = 22. Show your working.' WHERE id = '90000000-0000-0000-0001-000000000001';
UPDATE questions SET question_text = 'Solve for x: 3x + 7 = 22. Show your working.' WHERE id = '90000000-0000-0000-0002-000000000001';
UPDATE questions SET question_text = 'Solve for x: 3x + 7 = 22. Show your working.' WHERE id = '90000000-0000-0000-0003-000000000001';
UPDATE questions SET question_text = 'Solve for x: 3x + 7 = 22. Show your working.' WHERE id = '90000000-0000-0000-0004-000000000001';
UPDATE questions SET question_text = 'Solve for x: 3x + 7 = 22. Show your working.' WHERE id = '90000000-0000-0000-0005-000000000001';
UPDATE questions SET question_text = 'Solve for x: 3x + 7 = 22. Show your working.' WHERE id = '90000000-0000-0000-0006-000000000001';

-- Math Quiz 1 — Q2 → MCQ for all students
UPDATE questions SET question_text = 'What is the area of a circle with radius 5?',
  question_type = 'MCQ',
  mcq_options = '[{"key":"A","text":"25π"},{"key":"B","text":"10π"},{"key":"C","text":"50π"},{"key":"D","text":"5π"}]'
WHERE id IN (
  '90000000-0000-0000-0001-000000000002',
  '90000000-0000-0000-0002-000000000002',
  '90000000-0000-0000-0003-000000000002',
  '90000000-0000-0000-0004-000000000002',
  '90000000-0000-0000-0005-000000000002',
  '90000000-0000-0000-0006-000000000002'
);

-- Student answers for Math Quiz 1 Q1 (OPEN) sub-questions
-- Alice (good) — sub-q a & b
UPDATE sub_questions SET student_answer = '3x = 22 - 7 = 15, x = 5' WHERE id = 'a0000001-0001-0000-0000-000000000001';
UPDATE sub_questions SET student_answer = 'Used Pythagoras: a² + b² = c², so c = 13' WHERE id = 'a0000001-0001-0000-0000-000000000002';
-- Ben
UPDATE sub_questions SET student_answer = '3x + 7 = 22, 3x = 15, x = 5' WHERE id = 'a0000002-0001-0000-0000-000000000001';
UPDATE sub_questions SET student_answer = 'Applied theorem: hypotenuse = sqrt(25+144) = 13' WHERE id = 'a0000002-0001-0000-0000-000000000002';
-- Clara
UPDATE sub_questions SET student_answer = 'x = (22-7)/3 = 5' WHERE id = 'a0000003-0001-0000-0000-000000000001';
UPDATE sub_questions SET student_answer = 'c = sqrt(5² + 12²) = 13' WHERE id = 'a0000003-0001-0000-0000-000000000002';
-- Daniel
UPDATE sub_questions SET student_answer = '3x = 15 so x = 5' WHERE id = 'a0000004-0001-0000-0000-000000000001';
UPDATE sub_questions SET student_answer = 'sqrt(169) = 13' WHERE id = 'a0000004-0001-0000-0000-000000000002';
-- Ethan (struggling)
UPDATE sub_questions SET student_answer = 'x = 22/3 = 7.3' WHERE id = 'a0000005-0001-0000-0000-000000000001';
UPDATE sub_questions SET student_answer = 'I think c = 17?' WHERE id = 'a0000005-0001-0000-0000-000000000002';
-- Fiona (struggling)
UPDATE sub_questions SET student_answer = '3x = 29, x = 9.6' WHERE id = 'a0000006-0001-0000-0000-000000000001';
UPDATE sub_questions SET student_answer = 'Not sure, maybe 15' WHERE id = 'a0000006-0001-0000-0000-000000000002';

-- Student answers for Math Quiz 1 Q2 (MCQ) — answer stored on sub-question
-- Good students picked A (correct), struggling picked wrong
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a0000001-0001-0000-0000-000000000003';
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a0000002-0001-0000-0000-000000000003';
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a0000003-0001-0000-0000-000000000003';
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a0000004-0001-0000-0000-000000000003';
UPDATE sub_questions SET student_answer = 'C' WHERE id = 'a0000005-0001-0000-0000-000000000003';
UPDATE sub_questions SET student_answer = 'B' WHERE id = 'a0000006-0001-0000-0000-000000000003';

-- ============================================================
-- MATH Midterm questions
-- Q1 (OPEN), Q2 (OPEN)
-- ============================================================
UPDATE questions SET question_text = 'Simplify the expression: 2(3x - 4) + 5x' WHERE id IN (
  '90000000-0000-0000-0001-000000000003', '90000000-0000-0000-0002-000000000003',
  '90000000-0000-0000-0003-000000000003', '90000000-0000-0000-0004-000000000003',
  '90000000-0000-0000-0005-000000000003', '90000000-0000-0000-0006-000000000003'
);
UPDATE questions SET question_text = 'A triangle has sides 8cm, 15cm and 17cm. Is it a right triangle? Explain.' WHERE id IN (
  '90000000-0000-0000-0001-000000000004', '90000000-0000-0000-0002-000000000004',
  '90000000-0000-0000-0003-000000000004', '90000000-0000-0000-0004-000000000004',
  '90000000-0000-0000-0005-000000000004', '90000000-0000-0000-0006-000000000004'
);

-- Midterm Q1 student answers
UPDATE sub_questions SET student_answer = '6x - 8 + 5x = 11x - 8' WHERE id = 'a0000001-0001-0000-0000-000000000005';
UPDATE sub_questions SET student_answer = '= 6x - 8 + 5x = 11x - 8' WHERE id = 'a0000002-0001-0000-0000-000000000005';
UPDATE sub_questions SET student_answer = '2(3x-4)+5x = 11x - 8' WHERE id = 'a0000003-0001-0000-0000-000000000005';
UPDATE sub_questions SET student_answer = '11x - 8' WHERE id = 'a0000004-0001-0000-0000-000000000005';
UPDATE sub_questions SET student_answer = '6x - 4 + 5x = 11x - 4' WHERE id = 'a0000005-0001-0000-0000-000000000005';
UPDATE sub_questions SET student_answer = '2*3x + 5x = 11x' WHERE id = 'a0000006-0001-0000-0000-000000000005';

-- Midterm Q1 sub-q b answers
UPDATE sub_questions SET student_answer = 'Expanded correctly, combined like terms' WHERE id = 'a0000001-0001-0000-0000-000000000006';
UPDATE sub_questions SET student_answer = 'Distributed and simplified' WHERE id = 'a0000002-0001-0000-0000-000000000006';
UPDATE sub_questions SET student_answer = 'Used distributive property' WHERE id = 'a0000003-0001-0000-0000-000000000006';
UPDATE sub_questions SET student_answer = 'Simplified step by step' WHERE id = 'a0000004-0001-0000-0000-000000000006';
UPDATE sub_questions SET student_answer = 'I multiplied 2 by 3x only' WHERE id = 'a0000005-0001-0000-0000-000000000006';
UPDATE sub_questions SET student_answer = 'Forgot the -4 part' WHERE id = 'a0000006-0001-0000-0000-000000000006';

-- Midterm Q2 student answers
UPDATE sub_questions SET student_answer = 'Yes: 8² + 15² = 64 + 225 = 289 = 17². Right triangle.' WHERE id = 'a0000001-0001-0000-0000-000000000007';
UPDATE sub_questions SET student_answer = '8²+15²=289=17², yes it is right-angled' WHERE id = 'a0000002-0001-0000-0000-000000000007';
UPDATE sub_questions SET student_answer = 'Check: 64+225=289=17². Yes.' WHERE id = 'a0000003-0001-0000-0000-000000000007';
UPDATE sub_questions SET student_answer = 'Pythagoras: 8²+15²=17². Right triangle.' WHERE id = 'a0000004-0001-0000-0000-000000000007';
UPDATE sub_questions SET student_answer = '8+15=23 which is not 17 so no' WHERE id = 'a0000005-0001-0000-0000-000000000007';
UPDATE sub_questions SET student_answer = 'I think no because 8+15 > 17' WHERE id = 'a0000006-0001-0000-0000-000000000007';

UPDATE sub_questions SET student_answer = 'Verified using Pythagorean theorem' WHERE id = 'a0000001-0001-0000-0000-000000000008';
UPDATE sub_questions SET student_answer = 'Used a²+b²=c² to verify' WHERE id = 'a0000002-0001-0000-0000-000000000008';
UPDATE sub_questions SET student_answer = 'Applied Pythagoras theorem' WHERE id = 'a0000003-0001-0000-0000-000000000008';
UPDATE sub_questions SET student_answer = 'Checked with theorem' WHERE id = 'a0000004-0001-0000-0000-000000000008';
UPDATE sub_questions SET student_answer = 'Added the sides together' WHERE id = 'a0000005-0001-0000-0000-000000000008';
UPDATE sub_questions SET student_answer = 'Used triangle inequality' WHERE id = 'a0000006-0001-0000-0000-000000000008';

-- ============================================================
-- MATH Quiz 2 questions
-- Q1 (OPEN), Q2 → MCQ
-- ============================================================
UPDATE questions SET question_text = 'Find the mean, median and mode of: 4, 7, 7, 9, 12, 15' WHERE id IN (
  '90000000-0000-0000-0001-000000000005', '90000000-0000-0000-0002-000000000005',
  '90000000-0000-0000-0003-000000000005', '90000000-0000-0000-0004-000000000005',
  '90000000-0000-0000-0005-000000000005', '90000000-0000-0000-0006-000000000005'
);

UPDATE questions SET question_text = 'Which trigonometric ratio equals opposite/hypotenuse?',
  question_type = 'MCQ',
  mcq_options = '[{"key":"A","text":"Cosine"},{"key":"B","text":"Tangent"},{"key":"C","text":"Sine"},{"key":"D","text":"Secant"}]'
WHERE id IN (
  '90000000-0000-0000-0001-000000000006', '90000000-0000-0000-0002-000000000006',
  '90000000-0000-0000-0003-000000000006', '90000000-0000-0000-0004-000000000006',
  '90000000-0000-0000-0005-000000000006', '90000000-0000-0000-0006-000000000006'
);

-- Quiz 2 Q1 answers
UPDATE sub_questions SET student_answer = 'Mean=9, Median=8, Mode=7' WHERE id = 'a0000001-0001-0000-0000-000000000009';
UPDATE sub_questions SET student_answer = 'Mean=(4+7+7+9+12+15)/6=9, Median=8, Mode=7' WHERE id = 'a0000002-0001-0000-0000-000000000009';
UPDATE sub_questions SET student_answer = 'Mean=9, Median=(7+9)/2=8, Mode=7' WHERE id = 'a0000003-0001-0000-0000-000000000009';
UPDATE sub_questions SET student_answer = 'Mean=54/6=9, Median=8, Mode=7' WHERE id = 'a0000004-0001-0000-0000-000000000009';
UPDATE sub_questions SET student_answer = 'Mean=54/6=9, Median=9, Mode=7' WHERE id = 'a0000005-0001-0000-0000-000000000009';
UPDATE sub_questions SET student_answer = 'Mean=10, Median=7, Mode=7' WHERE id = 'a0000006-0001-0000-0000-000000000009';

UPDATE sub_questions SET student_answer = 'Calculated each measure correctly' WHERE id = 'a0000001-0001-0000-0000-00000000000a';
UPDATE sub_questions SET student_answer = 'Showed all working for each' WHERE id = 'a0000002-0001-0000-0000-00000000000a';
UPDATE sub_questions SET student_answer = 'Ordered data first then found median' WHERE id = 'a0000003-0001-0000-0000-00000000000a';
UPDATE sub_questions SET student_answer = 'Sorted and computed' WHERE id = 'a0000004-0001-0000-0000-00000000000a';
UPDATE sub_questions SET student_answer = 'Picked middle number without sorting' WHERE id = 'a0000005-0001-0000-0000-00000000000a';
UPDATE sub_questions SET student_answer = 'Guessed the mean' WHERE id = 'a0000006-0001-0000-0000-00000000000a';

-- Quiz 2 Q2 (MCQ) answers — C is correct (Sine)
UPDATE sub_questions SET student_answer = 'C' WHERE id = 'a0000001-0001-0000-0000-00000000000b';
UPDATE sub_questions SET student_answer = 'C' WHERE id = 'a0000002-0001-0000-0000-00000000000b';
UPDATE sub_questions SET student_answer = 'C' WHERE id = 'a0000003-0001-0000-0000-00000000000b';
UPDATE sub_questions SET student_answer = 'C' WHERE id = 'a0000004-0001-0000-0000-00000000000b';
UPDATE sub_questions SET student_answer = 'B' WHERE id = 'a0000005-0001-0000-0000-00000000000b';
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a0000006-0001-0000-0000-00000000000b';

UPDATE sub_questions SET student_answer = 'C' WHERE id = 'a0000001-0001-0000-0000-00000000000c';
UPDATE sub_questions SET student_answer = 'C' WHERE id = 'a0000002-0001-0000-0000-00000000000c';
UPDATE sub_questions SET student_answer = 'C' WHERE id = 'a0000003-0001-0000-0000-00000000000c';
UPDATE sub_questions SET student_answer = 'C' WHERE id = 'a0000004-0001-0000-0000-00000000000c';
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a0000005-0001-0000-0000-00000000000c';
UPDATE sub_questions SET student_answer = 'D' WHERE id = 'a0000006-0001-0000-0000-00000000000c';

-- ============================================================
-- PHYSICS CLASS questions
-- Quiz 1: Q1 (OPEN), Q2 → MCQ
-- Midterm: Q1 (OPEN), Q2 (OPEN)
-- Quiz 2: Q1 → MCQ, Q2 (OPEN)
-- ============================================================

-- Physics Quiz 1 Q1 (OPEN)
UPDATE questions SET question_text = 'A car accelerates from rest at 2 m/s². Calculate its velocity after 8 seconds.' WHERE id IN (
  '90000000-0000-0000-0007-000000000001', '90000000-0000-0000-0008-000000000001',
  '90000000-0000-0000-0009-000000000001', '90000000-0000-0000-000a-000000000001'
);

-- Physics Quiz 1 Q2 → MCQ
UPDATE questions SET question_text = 'Which law states that every action has an equal and opposite reaction?',
  question_type = 'MCQ',
  mcq_options = '[{"key":"A","text":"Newton''s First Law"},{"key":"B","text":"Newton''s Second Law"},{"key":"C","text":"Newton''s Third Law"},{"key":"D","text":"Law of Conservation of Energy"}]'
WHERE id IN (
  '90000000-0000-0000-0007-000000000002', '90000000-0000-0000-0008-000000000002',
  '90000000-0000-0000-0009-000000000002', '90000000-0000-0000-000a-000000000002'
);

-- Physics Quiz 1 Q1 answers
UPDATE sub_questions SET student_answer = 'v = u + at = 0 + 2(8) = 16 m/s' WHERE id = 'a0000007-0001-0000-0000-000000000001';
UPDATE sub_questions SET student_answer = 'v = 0 + 2×8 = 16 m/s' WHERE id = 'a0000008-0001-0000-0000-000000000001';
UPDATE sub_questions SET student_answer = 'v = 2 × 8 = 16' WHERE id = 'a0000009-0001-0000-0000-000000000001';
UPDATE sub_questions SET student_answer = 'v = 2 + 8 = 10 m/s' WHERE id = 'a000000a-0001-0000-0000-000000000001';

UPDATE sub_questions SET student_answer = 'Used v=u+at formula correctly' WHERE id = 'a0000007-0001-0000-0000-000000000002';
UPDATE sub_questions SET student_answer = 'Applied kinematics equation' WHERE id = 'a0000008-0001-0000-0000-000000000002';
UPDATE sub_questions SET student_answer = 'Forgot units' WHERE id = 'a0000009-0001-0000-0000-000000000002';
UPDATE sub_questions SET student_answer = 'Added instead of multiplied' WHERE id = 'a000000a-0001-0000-0000-000000000002';

-- Physics Quiz 1 Q2 (MCQ) answers — C is correct
UPDATE sub_questions SET student_answer = 'C' WHERE id = 'a0000007-0001-0000-0000-000000000003';
UPDATE sub_questions SET student_answer = 'C' WHERE id = 'a0000008-0001-0000-0000-000000000003';
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a0000009-0001-0000-0000-000000000003';
UPDATE sub_questions SET student_answer = 'B' WHERE id = 'a000000a-0001-0000-0000-000000000003';

UPDATE sub_questions SET student_answer = 'C' WHERE id = 'a0000007-0001-0000-0000-000000000004';
UPDATE sub_questions SET student_answer = 'C' WHERE id = 'a0000008-0001-0000-0000-000000000004';
UPDATE sub_questions SET student_answer = 'D' WHERE id = 'a0000009-0001-0000-0000-000000000004';
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a000000a-0001-0000-0000-000000000004';

-- Physics Midterm Q1 (OPEN)
UPDATE questions SET question_text = 'Explain the difference between heat and temperature. Give one example.' WHERE id IN (
  '90000000-0000-0000-0007-000000000003', '90000000-0000-0000-0008-000000000003',
  '90000000-0000-0000-0009-000000000003', '90000000-0000-0000-000a-000000000003'
);
-- Physics Midterm Q2 (OPEN)
UPDATE questions SET question_text = 'A wave has frequency 50 Hz and wavelength 0.4 m. Calculate its speed.' WHERE id IN (
  '90000000-0000-0000-0007-000000000004', '90000000-0000-0000-0008-000000000004',
  '90000000-0000-0000-0009-000000000004', '90000000-0000-0000-000a-000000000004'
);

-- Physics Midterm Q1 answers
UPDATE sub_questions SET student_answer = 'Heat is energy transfer, temperature is a measure of average kinetic energy. E.g. boiling water transfers heat but thermometer reads temperature.' WHERE id = 'a0000007-0001-0000-0000-000000000005';
UPDATE sub_questions SET student_answer = 'Heat = energy flow, temp = how hot. Example: ice melting absorbs heat but stays at 0°C.' WHERE id = 'a0000008-0001-0000-0000-000000000005';
UPDATE sub_questions SET student_answer = 'Heat is hot and temperature is cold' WHERE id = 'a0000009-0001-0000-0000-000000000005';
UPDATE sub_questions SET student_answer = 'They are the same thing measured differently' WHERE id = 'a000000a-0001-0000-0000-000000000005';

UPDATE sub_questions SET student_answer = 'Clearly distinguished with correct example' WHERE id = 'a0000007-0001-0000-0000-000000000006';
UPDATE sub_questions SET student_answer = 'Good explanation with phase change example' WHERE id = 'a0000008-0001-0000-0000-000000000006';
UPDATE sub_questions SET student_answer = 'Confused the two concepts' WHERE id = 'a0000009-0001-0000-0000-000000000006';
UPDATE sub_questions SET student_answer = 'Incorrect — they are different quantities' WHERE id = 'a000000a-0001-0000-0000-000000000006';

-- Physics Midterm Q2 answers
UPDATE sub_questions SET student_answer = 'v = fλ = 50 × 0.4 = 20 m/s' WHERE id = 'a0000007-0001-0000-0000-000000000007';
UPDATE sub_questions SET student_answer = 'Speed = 50 × 0.4 = 20 m/s' WHERE id = 'a0000008-0001-0000-0000-000000000007';
UPDATE sub_questions SET student_answer = 'v = 50/0.4 = 125 m/s' WHERE id = 'a0000009-0001-0000-0000-000000000007';
UPDATE sub_questions SET student_answer = 'v = 50 + 0.4 = 50.4' WHERE id = 'a000000a-0001-0000-0000-000000000007';

UPDATE sub_questions SET student_answer = 'Used wave equation correctly' WHERE id = 'a0000007-0001-0000-0000-000000000008';
UPDATE sub_questions SET student_answer = 'Applied v=fλ' WHERE id = 'a0000008-0001-0000-0000-000000000008';
UPDATE sub_questions SET student_answer = 'Divided instead of multiplied' WHERE id = 'a0000009-0001-0000-0000-000000000008';
UPDATE sub_questions SET student_answer = 'Added frequency and wavelength' WHERE id = 'a000000a-0001-0000-0000-000000000008';

-- Physics Quiz 2 Q1 → MCQ
UPDATE questions SET question_text = 'What is the SI unit of electric current?',
  question_type = 'MCQ',
  mcq_options = '[{"key":"A","text":"Volt"},{"key":"B","text":"Ampere"},{"key":"C","text":"Ohm"},{"key":"D","text":"Watt"}]'
WHERE id IN (
  '90000000-0000-0000-0007-000000000005', '90000000-0000-0000-0008-000000000005',
  '90000000-0000-0000-0009-000000000005', '90000000-0000-0000-000a-000000000005'
);

-- Physics Quiz 2 Q2 (OPEN)
UPDATE questions SET question_text = 'Describe how a convex lens forms a real image. Include a ray diagram description.' WHERE id IN (
  '90000000-0000-0000-0007-000000000006', '90000000-0000-0000-0008-000000000006',
  '90000000-0000-0000-0009-000000000006', '90000000-0000-0000-000a-000000000006'
);

-- Physics Quiz 2 Q1 (MCQ) answers — B is correct
UPDATE sub_questions SET student_answer = 'B' WHERE id = 'a0000007-0001-0000-0000-000000000009';
UPDATE sub_questions SET student_answer = 'B' WHERE id = 'a0000008-0001-0000-0000-000000000009';
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a0000009-0001-0000-0000-000000000009';
UPDATE sub_questions SET student_answer = 'C' WHERE id = 'a000000a-0001-0000-0000-000000000009';

UPDATE sub_questions SET student_answer = 'B' WHERE id = 'a0000007-0001-0000-0000-00000000000a';
UPDATE sub_questions SET student_answer = 'B' WHERE id = 'a0000008-0001-0000-0000-00000000000a';
UPDATE sub_questions SET student_answer = 'D' WHERE id = 'a0000009-0001-0000-0000-00000000000a';
UPDATE sub_questions SET student_answer = 'A' WHERE id = 'a000000a-0001-0000-0000-00000000000a';

-- Physics Quiz 2 Q2 answers
UPDATE sub_questions SET student_answer = 'Parallel ray refracts through focal point, central ray passes straight. They converge to form inverted real image beyond lens.' WHERE id = 'a0000007-0001-0000-0000-00000000000b';
UPDATE sub_questions SET student_answer = 'Light converges after passing through lens, forming real inverted image at focal point.' WHERE id = 'a0000008-0001-0000-0000-00000000000b';
UPDATE sub_questions SET student_answer = 'The lens makes the image bigger' WHERE id = 'a0000009-0001-0000-0000-00000000000b';
UPDATE sub_questions SET student_answer = 'Light goes through and makes a picture' WHERE id = 'a000000a-0001-0000-0000-00000000000b';

UPDATE sub_questions SET student_answer = 'Drew correct ray diagram with principal axis' WHERE id = 'a0000007-0001-0000-0000-00000000000c';
UPDATE sub_questions SET student_answer = 'Showed two rays converging' WHERE id = 'a0000008-0001-0000-0000-00000000000c';
UPDATE sub_questions SET student_answer = 'No diagram drawn' WHERE id = 'a0000009-0001-0000-0000-00000000000c';
UPDATE sub_questions SET student_answer = 'Drew a circle for the lens' WHERE id = 'a000000a-0001-0000-0000-00000000000c';
