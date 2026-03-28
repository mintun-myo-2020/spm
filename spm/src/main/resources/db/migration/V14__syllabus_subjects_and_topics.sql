-- =============================================
-- V14: Replace generic subjects/topics with Singapore O-Level syllabus
-- Sources: Official Singapore-Cambridge GCE O-Level syllabuses
--   - 4052 Mathematics
--   - 4049 Additional Mathematics
--   - 6091 Physics
--   - 6092 Chemistry
--   - 6093 Biology
-- =============================================

-- ============================================================
-- STEP 1: Update existing subjects to match O-Level syllabus codes
-- ============================================================

-- Mathematics: keep same ID, update name/code/description
UPDATE subjects SET
  name = '4052 Mathematics',
  code = 'EMATH',
  description = 'Singapore-Cambridge GCE O-Level Mathematics'
WHERE id = 'a0000000-0000-0000-0000-000000000001';

-- Physics: keep same ID, update name/code/description
UPDATE subjects SET
  name = '6091 Physics',
  code = 'PHY',
  description = 'Singapore-Cambridge GCE O-Level Physics'
WHERE id = 'a0000000-0000-0000-0000-000000000006';

-- Delete generic subjects that don't map to O-Level syllabuses we have
-- (no classes or topics with FK references to these)
DELETE FROM topics   WHERE subject_id = 'a0000000-0000-0000-0000-000000000002'; -- Science topics
DELETE FROM subjects WHERE id = 'a0000000-0000-0000-0000-000000000002'; -- Science
DELETE FROM subjects WHERE id = 'a0000000-0000-0000-0000-000000000003'; -- English
DELETE FROM subjects WHERE id = 'a0000000-0000-0000-0000-000000000004'; -- History
DELETE FROM subjects WHERE id = 'a0000000-0000-0000-0000-000000000005'; -- ICT

-- ============================================================
-- STEP 2: Insert new subjects
-- ============================================================

INSERT INTO subjects (id, name, code, description, is_default, is_active) VALUES
  ('a0000000-0000-0000-0000-000000000007', '4049 Additional Mathematics', 'AMATH', 'Singapore-Cambridge GCE O-Level Additional Mathematics', true, true),
  ('a0000000-0000-0000-0000-000000000008', '6092 Chemistry',              'CHEM',  'Singapore-Cambridge GCE O-Level Chemistry',              true, true),
  ('a0000000-0000-0000-0000-000000000009', '6093 Biology',                'BIO',   'Singapore-Cambridge GCE O-Level Biology',                true, true);

-- ============================================================
-- STEP 3: Update existing Math topics to match O-Level syllabus
-- Existing: ALG(b..01), GEO(b..02), STAT(b..03), TRIG(b..04)
-- These map reasonably to syllabus topics, so update in-place
-- to preserve sub_question FK references.
-- ============================================================

UPDATE topics SET name = 'N5 Algebra',           code = 'EMATH-N5', description = 'Algebraic expressions and formulae: manipulation, factorisation, formulae'   WHERE id = 'b0000000-0000-0000-0000-000000000001';
UPDATE topics SET name = 'G1 Angles & Polygons', code = 'EMATH-G1', description = 'Angles, triangles and polygons: properties, constructions, symmetry'              WHERE id = 'b0000000-0000-0000-0000-000000000002';
UPDATE topics SET name = 'S1 Data Analysis',     code = 'EMATH-S1', description = 'Data handling and analysis: statistical representations, central tendency, spread'  WHERE id = 'b0000000-0000-0000-0000-000000000003';
UPDATE topics SET name = 'G4 Trigonometry',      code = 'EMATH-G4', description = 'Pythagoras theorem and trigonometry: trig ratios, sine/cosine rules, bearings'     WHERE id = 'b0000000-0000-0000-0000-000000000004';

-- ============================================================
-- STEP 4: Update existing Physics topics to match O-Level syllabus
-- Existing: MECH(b..07), THERM(b..08), WAVE(b..09), ELEC(b..0a)
-- ============================================================

UPDATE topics SET name = '3 Dynamics',            code = 'PHY-03', description = 'Newton''s laws of motion, mass, weight, friction, free-body diagrams'       WHERE id = 'b0000000-0000-0000-0000-000000000007';
UPDATE topics SET name = '9 Thermal Properties',  code = 'PHY-09', description = 'Specific heat capacity, melting, boiling, latent heat, heating/cooling curves' WHERE id = 'b0000000-0000-0000-0000-000000000008';
UPDATE topics SET name = '10 Waves',              code = 'PHY-10', description = 'General properties of waves: transverse, longitudinal, wave equation'           WHERE id = 'b0000000-0000-0000-0000-000000000009';
UPDATE topics SET name = '14 Current Electricity', code = 'PHY-14', description = 'Electric current, EMF, potential difference, resistance, Ohm''s law'           WHERE id = 'b0000000-0000-0000-0000-00000000000a';


-- ============================================================
-- STEP 5: Insert remaining Mathematics (4052) topics
-- Already have: N5(b..01), G1(b..02), S1(b..03), G4(b..04)
-- ============================================================

INSERT INTO topics (id, subject_id, name, code, description, is_default, is_active) VALUES
  ('b0000000-0000-0000-0000-000000000101', 'a0000000-0000-0000-0000-000000000001', 'N1 Numbers',            'EMATH-N1', 'Numbers and their operations: primes, HCF/LCM, indices, standard form',        true, true),
  ('b0000000-0000-0000-0000-000000000102', 'a0000000-0000-0000-0000-000000000001', 'N2 Ratio & Proportion', 'EMATH-N2', 'Ratio and proportion: map scales, direct and inverse proportion',               true, true),
  ('b0000000-0000-0000-0000-000000000103', 'a0000000-0000-0000-0000-000000000001', 'N3 Percentage',         'EMATH-N3', 'Percentage: increase/decrease, reverse percentages, comparisons',               true, true),
  ('b0000000-0000-0000-0000-000000000104', 'a0000000-0000-0000-0000-000000000001', 'N4 Rate & Speed',       'EMATH-N4', 'Rate and speed: average rate, average speed, unit conversion',                  true, true),
  ('b0000000-0000-0000-0000-000000000105', 'a0000000-0000-0000-0000-000000000001', 'N6 Functions & Graphs', 'EMATH-N6', 'Functions and graphs: linear, quadratic, power, exponential, gradient',         true, true),
  ('b0000000-0000-0000-0000-000000000106', 'a0000000-0000-0000-0000-000000000001', 'N7 Equations',          'EMATH-N7', 'Equations and inequalities: linear, quadratic, simultaneous, fractional',       true, true),
  ('b0000000-0000-0000-0000-000000000107', 'a0000000-0000-0000-0000-000000000001', 'N8 Sets',               'EMATH-N8', 'Set language and notation: union, intersection, Venn diagrams',                 true, true),
  ('b0000000-0000-0000-0000-000000000108', 'a0000000-0000-0000-0000-000000000001', 'N9 Matrices',           'EMATH-N9', 'Matrices: operations, scalar multiplication, sum and product',                  true, true),
  ('b0000000-0000-0000-0000-000000000109', 'a0000000-0000-0000-0000-000000000001', 'G2 Similarity',         'EMATH-G2', 'Congruence and similarity: scale drawings, ratio of areas/volumes',             true, true),
  ('b0000000-0000-0000-0000-000000000110', 'a0000000-0000-0000-0000-000000000001', 'G3 Circles',            'EMATH-G3', 'Properties of circles: symmetry, angle, tangent properties',                    true, true),
  ('b0000000-0000-0000-0000-000000000111', 'a0000000-0000-0000-0000-000000000001', 'G5 Mensuration',        'EMATH-G5', 'Mensuration: area, volume, surface area, arc length, sector area, radians',    true, true),
  ('b0000000-0000-0000-0000-000000000112', 'a0000000-0000-0000-0000-000000000001', 'G6 Coord Geometry',     'EMATH-G6', 'Coordinate geometry: gradient, line segment length, equation of straight line', true, true),
  ('b0000000-0000-0000-0000-000000000113', 'a0000000-0000-0000-0000-000000000001', 'G7 Vectors',            'EMATH-G7', 'Vectors in two dimensions: notation, translation, magnitude, geometric problems', true, true),
  ('b0000000-0000-0000-0000-000000000114', 'a0000000-0000-0000-0000-000000000001', 'S2 Probability',        'EMATH-S2', 'Probability: single/combined events, tree diagrams, addition/multiplication rules', true, true);

-- ============================================================
-- STEP 6: Insert Additional Mathematics (4049) topics
-- ============================================================

INSERT INTO topics (id, subject_id, name, code, description, is_default, is_active) VALUES
  ('b0000000-0000-0000-0000-000000000201', 'a0000000-0000-0000-0000-000000000007', 'A1 Quadratics',          'AMATH-A1', 'Quadratic functions: completing the square, max/min, always positive/negative',    true, true),
  ('b0000000-0000-0000-0000-000000000202', 'a0000000-0000-0000-0000-000000000007', 'A2 Equations',           'AMATH-A2', 'Equations and inequalities: discriminant, simultaneous, quadratic inequalities',    true, true),
  ('b0000000-0000-0000-0000-000000000203', 'a0000000-0000-0000-0000-000000000007', 'A3 Surds',               'AMATH-A3', 'Surds: operations, rationalising denominators, solving surd equations',             true, true),
  ('b0000000-0000-0000-0000-000000000204', 'a0000000-0000-0000-0000-000000000007', 'A4 Polynomials',         'AMATH-A4', 'Polynomials and partial fractions: remainder/factor theorems, cubic equations',     true, true),
  ('b0000000-0000-0000-0000-000000000205', 'a0000000-0000-0000-0000-000000000007', 'A5 Binomial',            'AMATH-A5', 'Binomial expansions: binomial theorem, nCr notation, general term',                true, true),
  ('b0000000-0000-0000-0000-000000000206', 'a0000000-0000-0000-0000-000000000007', 'A6 Exp & Log',           'AMATH-A6', 'Exponential and logarithmic functions: laws of logarithms, change of base',        true, true),
  ('b0000000-0000-0000-0000-000000000207', 'a0000000-0000-0000-0000-000000000007', 'G1 Trig Functions',      'AMATH-G1', 'Trigonometric functions, identities and equations: R-formula, trig graphs',        true, true),
  ('b0000000-0000-0000-0000-000000000208', 'a0000000-0000-0000-0000-000000000007', 'G2 Coord Geometry',      'AMATH-G2', 'Coordinate geometry in two dimensions: circles, linear law transformation',        true, true),
  ('b0000000-0000-0000-0000-000000000209', 'a0000000-0000-0000-0000-000000000007', 'G3 Plane Geometry',      'AMATH-G3', 'Proofs in plane geometry: midpoint theorem, tangent-chord theorem',                true, true),
  ('b0000000-0000-0000-0000-000000000210', 'a0000000-0000-0000-0000-000000000007', 'C1 Calculus',            'AMATH-C1', 'Differentiation and integration: derivatives, chain rule, stationary points, area under curve', true, true);


-- ============================================================
-- STEP 7: Insert remaining Physics (6091) topics
-- Already have: 3 Dynamics(b..07), 9 Thermal Properties(b..08),
--               10 General Properties of Waves(b..09), 14 Current of Electricity(b..0a)
-- ============================================================

INSERT INTO topics (id, subject_id, name, code, description, is_default, is_active) VALUES
  ('b0000000-0000-0000-0000-000000000301', 'a0000000-0000-0000-0000-000000000006', '1 Physical Quantities, Units and Measurement', 'PHY-01', 'SI units, prefixes, scalars and vectors, measuring instruments',                true, true),
  ('b0000000-0000-0000-0000-000000000302', 'a0000000-0000-0000-0000-000000000006', '2 Kinematics',                                 'PHY-02', 'Speed, velocity, acceleration, distance-time and speed-time graphs, free fall', true, true),
  ('b0000000-0000-0000-0000-000000000303', 'a0000000-0000-0000-0000-000000000006', '4 Turning Effect of Forces',                    'PHY-04', 'Moments, centre of gravity, stability, equilibrium conditions',                true, true),
  ('b0000000-0000-0000-0000-000000000304', 'a0000000-0000-0000-0000-000000000006', '5 Pressure',                                    'PHY-05', 'Pressure in solids, liquids and gases, atmospheric pressure, manometers',      true, true),
  ('b0000000-0000-0000-0000-000000000305', 'a0000000-0000-0000-0000-000000000006', '6 Energy',                                      'PHY-06', 'Forms of energy, work, power, efficiency, energy conversion and conservation', true, true),
  ('b0000000-0000-0000-0000-000000000306', 'a0000000-0000-0000-0000-000000000006', '7 Kinetic Particle Model of Matter',             'PHY-07', 'States of matter, Brownian motion, kinetic model, gas pressure',               true, true),
  ('b0000000-0000-0000-0000-000000000307', 'a0000000-0000-0000-0000-000000000006', '8 Thermal Processes',                            'PHY-08', 'Conduction, convection, radiation, thermal energy transfer',                   true, true),
  ('b0000000-0000-0000-0000-000000000308', 'a0000000-0000-0000-0000-000000000006', '11 Electromagnetic Spectrum',                    'PHY-11', 'Properties and applications of electromagnetic waves',                         true, true),
  ('b0000000-0000-0000-0000-000000000309', 'a0000000-0000-0000-0000-000000000006', '12 Light',                                       'PHY-12', 'Reflection, refraction, thin lenses, total internal reflection',               true, true),
  ('b0000000-0000-0000-0000-000000000310', 'a0000000-0000-0000-0000-000000000006', '13 Static Electricity',                          'PHY-13', 'Laws of electrostatics, electric field, charging and discharging',             true, true),
  ('b0000000-0000-0000-0000-000000000311', 'a0000000-0000-0000-0000-000000000006', '15 D.C. Circuits',                               'PHY-15', 'Series and parallel circuits, potential divider, combined resistance',         true, true),
  ('b0000000-0000-0000-0000-000000000312', 'a0000000-0000-0000-0000-000000000006', '16 Practical Electricity',                       'PHY-16', 'Electrical energy, power, cost of electricity, safety features',               true, true),
  ('b0000000-0000-0000-0000-000000000313', 'a0000000-0000-0000-0000-000000000006', '17 Magnetism',                                   'PHY-17', 'Magnetic properties, magnetic field, permanent magnets',                       true, true),
  ('b0000000-0000-0000-0000-000000000314', 'a0000000-0000-0000-0000-000000000006', '18 Electromagnetism',                            'PHY-18', 'Magnetic effect of current, force on current-carrying conductor, d.c. motor',  true, true),
  ('b0000000-0000-0000-0000-000000000315', 'a0000000-0000-0000-0000-000000000006', '19 Electromagnetic Induction',                   'PHY-19', 'Faraday''s law, Lenz''s law, a.c. generator, transformer',                    true, true),
  ('b0000000-0000-0000-0000-000000000316', 'a0000000-0000-0000-0000-000000000006', '20 Radioactivity',                               'PHY-20', 'Radioactive emissions, half-life, nuclear reactions, safety',                  true, true);

-- ============================================================
-- STEP 8: Insert Chemistry (6092) topics
-- ============================================================

INSERT INTO topics (id, subject_id, name, code, description, is_default, is_active) VALUES
  ('b0000000-0000-0000-0000-000000000401', 'a0000000-0000-0000-0000-000000000008', '1 Experimental Chemistry',            'CHEM-01', 'Experimental design, methods of purification and analysis',                     true, true),
  ('b0000000-0000-0000-0000-000000000402', 'a0000000-0000-0000-0000-000000000008', '2 The Particulate Nature of Matter',   'CHEM-02', 'Kinetic particle theory, atomic structure, isotopes',                          true, true),
  ('b0000000-0000-0000-0000-000000000403', 'a0000000-0000-0000-0000-000000000008', '3 Chemical Bonding and Structure',     'CHEM-03', 'Ionic, covalent, metallic bonding, structure and properties',                  true, true),
  ('b0000000-0000-0000-0000-000000000404', 'a0000000-0000-0000-0000-000000000008', '4 Chemical Calculations',              'CHEM-04', 'Mole concept, reacting masses, volumes of gases, concentration',               true, true),
  ('b0000000-0000-0000-0000-000000000405', 'a0000000-0000-0000-0000-000000000008', '5 Acid-Base Chemistry',                'CHEM-05', 'Acids, bases, salts, indicators, pH, neutralisation',                         true, true),
  ('b0000000-0000-0000-0000-000000000406', 'a0000000-0000-0000-0000-000000000008', '6 Qualitative Analysis',               'CHEM-06', 'Identification of cations, anions, gases',                                    true, true),
  ('b0000000-0000-0000-0000-000000000407', 'a0000000-0000-0000-0000-000000000008', '7 Redox Chemistry',                    'CHEM-07', 'Oxidation and reduction, reactivity series, electrochemistry',                true, true),
  ('b0000000-0000-0000-0000-000000000408', 'a0000000-0000-0000-0000-000000000008', '8 Patterns in the Periodic Table',     'CHEM-08', 'Periodic table trends, group properties, transition elements',                true, true),
  ('b0000000-0000-0000-0000-000000000409', 'a0000000-0000-0000-0000-000000000008', '9 Chemical Energetics',                'CHEM-09', 'Exothermic/endothermic reactions, energy level diagrams, bond energy',         true, true),
  ('b0000000-0000-0000-0000-000000000410', 'a0000000-0000-0000-0000-000000000008', '10 Rate of Reactions',                 'CHEM-10', 'Factors affecting rate, collision theory, catalysts',                          true, true),
  ('b0000000-0000-0000-0000-000000000411', 'a0000000-0000-0000-0000-000000000008', '11 Organic Chemistry',                 'CHEM-11', 'Alkanes, alkenes, alcohols, carboxylic acids, polymers, macromolecules',       true, true),
  ('b0000000-0000-0000-0000-000000000412', 'a0000000-0000-0000-0000-000000000008', '12 Maintaining Air Quality',           'CHEM-12', 'Composition of air, pollutants, carbon cycle, greenhouse effect',              true, true);


-- ============================================================
-- STEP 9: Insert Biology (6093) topics
-- ============================================================

INSERT INTO topics (id, subject_id, name, code, description, is_default, is_active) VALUES
  ('b0000000-0000-0000-0000-000000000501', 'a0000000-0000-0000-0000-000000000009', '1 Cell Structure and Organisation',                              'BIO-01', 'Plant/animal cells, organelles, cell specialisation',                          true, true),
  ('b0000000-0000-0000-0000-000000000502', 'a0000000-0000-0000-0000-000000000009', '2 Movement of Substances',                                       'BIO-02', 'Diffusion, osmosis, active transport',                                        true, true),
  ('b0000000-0000-0000-0000-000000000503', 'a0000000-0000-0000-0000-000000000009', '3 Biological Molecules',                                         'BIO-03', 'Carbohydrates, fats, proteins, enzymes, food tests',                          true, true),
  ('b0000000-0000-0000-0000-000000000504', 'a0000000-0000-0000-0000-000000000009', '4 Nutrition in Humans',                                          'BIO-04', 'Balanced diet, digestive system, digestion and absorption',                    true, true),
  ('b0000000-0000-0000-0000-000000000505', 'a0000000-0000-0000-0000-000000000009', '5 Transport in Humans',                                          'BIO-05', 'Circulatory system, heart, blood, blood vessels',                             true, true),
  ('b0000000-0000-0000-0000-000000000506', 'a0000000-0000-0000-0000-000000000009', '6 Respiration in Humans',                                        'BIO-06', 'Aerobic/anaerobic respiration, respiratory system, gas exchange',              true, true),
  ('b0000000-0000-0000-0000-000000000507', 'a0000000-0000-0000-0000-000000000009', '7 Excretion in Humans',                                          'BIO-07', 'Excretory system, kidney structure and function, dialysis',                    true, true),
  ('b0000000-0000-0000-0000-000000000508', 'a0000000-0000-0000-0000-000000000009', '8 Homeostasis, Co-ordination and Response in Humans',             'BIO-08', 'Nervous system, hormones, homeostasis, eye, skin',                            true, true),
  ('b0000000-0000-0000-0000-000000000509', 'a0000000-0000-0000-0000-000000000009', '9 Infectious Diseases in Humans',                                'BIO-09', 'Pathogens, transmission, body defences, immunity, vaccination',               true, true),
  ('b0000000-0000-0000-0000-000000000510', 'a0000000-0000-0000-0000-000000000009', '10 Nutrition and Transport in Flowering Plants',                  'BIO-10', 'Photosynthesis, leaf structure, mineral nutrition, transport in plants',       true, true),
  ('b0000000-0000-0000-0000-000000000511', 'a0000000-0000-0000-0000-000000000009', '11 Organisms and their Environment',                             'BIO-11', 'Ecosystems, food chains/webs, carbon/water cycles, human impact',             true, true),
  ('b0000000-0000-0000-0000-000000000512', 'a0000000-0000-0000-0000-000000000009', '12 Molecular Genetics',                                          'BIO-12', 'DNA structure, protein synthesis, genetic engineering',                        true, true),
  ('b0000000-0000-0000-0000-000000000513', 'a0000000-0000-0000-0000-000000000009', '13 Reproduction',                                                'BIO-13', 'Asexual/sexual reproduction, human reproductive system, menstrual cycle',     true, true),
  ('b0000000-0000-0000-0000-000000000514', 'a0000000-0000-0000-0000-000000000009', '14 Inheritance',                                                 'BIO-14', 'Mendelian genetics, monohybrid crosses, co-dominance, sex-linked inheritance', true, true);
