# Unit 2: OCR Test Upload — Functional Design Clarifications

## Context
Several answers significantly change the scope and behavior of the OCR feature from what was originally specified. Need to clarify before generating design artifacts.

---

## Clarification 1: Auto-population scope (Q3 + Q5 combined)

Your Q3 answer says the purpose is to extract questions (question text + student's answer), MCQ options, and marks (per question/sub-question). Your Q5 answer says the extracted data should be auto-populated into the test score form, not just shown as read-only text.

This is a much larger scope than the original requirement (FR-13), which was: "extracted text displayed in a read-only panel beside the test score form so the teacher can reference it while filling in scores manually."

Auto-populating means the system needs to:
1. Parse raw OCR text into structured data (question numbers, question text, student answers, marks per question)
2. Map that structured data to the TestScoreForm fields (questions, sub-questions, scores)
3. Pre-fill the form with this data

This is essentially AI-powered document understanding, not just OCR text extraction. AWS Textract `DetectDocumentText` gives you raw text blocks — it doesn't understand question/answer structure.

**Options:**

- A) **Keep original scope for this sprint** — OCR extracts raw text, shown in a side panel. Teacher manually enters scores while referencing the text. Auto-population is a future sprint feature (when AI integration is added). This is what the requirements say.
- B) **Expand scope this sprint** — Build the full pipeline: OCR → structured parsing → auto-populate form. This requires significant additional logic to parse exam paper formats (which vary widely between teachers/subjects). High risk of poor accuracy.
- C) **Hybrid** — OCR extracts raw text AND attempts basic structured parsing (question numbers, marks). Auto-populate what can be confidently parsed, show raw text for the rest. Teacher validates and corrects. Accept that parsing accuracy will be imperfect.

[Answer]: C for now, we might want to expand this with LLMs in future so we need the abstraction super important

---

## Clarification 2: Upload cardinality (Q3)

Your Q3 answer implies a test paper is typically 1 page (score is just 1 page). But Q7 says multi-file upload. If a test paper is 1 page, why multi-file?

- A) One file per upload — teacher uploads a single image/PDF of the test paper. Multi-file from Q7 was about uploading papers for multiple students at once (batch).
- B) Multiple files per upload — teacher might upload front + back of a page, or multiple pages of a longer exam. Still one test score.
- C) One file per upload, but the teacher might upload for multiple students in sequence (one at a time, different students).

[Answer]:Q3 SAYS SCORE is 1 page (front page will show total score, teacher can add this themselves). the questions and subquestions are obviously many more pages.

---

## Clarification 3: Student self-upload (Q9)

You said students can also upload for themselves. This raises questions:

- A) Students upload their own test papers, and the extracted text is shown to them (read-only reference). The teacher still creates the test score entry and enters marks. Student upload is just for convenience (teacher doesn't have to scan it).
- B) Students upload their own test papers, and the system auto-populates a test score draft that the teacher then reviews and approves.
- C) Students upload for reference only — they can see the extracted text of their own papers but it doesn't feed into test score creation.

[Answer]: B. IT ALSO auto populates the question/subquestion that teacher reviews and approves

---

## Clarification 4: Multi-file upload semantics (Q7)

Given the clarification on Q3 above, what does multi-file upload mean?

- A) Teacher selects multiple files (e.g., 3 images) in one upload request. All files belong to the same test paper / same student. OCR runs on each file, results are concatenated.
- B) Teacher selects multiple files in one upload request. Each file is a different student's test paper. Each gets its own upload record and OCR extraction.
- C) Single file per request (revise Q7 answer). Teacher uploads one file at a time.

[Answer]: multiple files are for same test paper / same student because the paper can have MANY PAGES.

---

**Document Version**: 1.0
**Last Updated**: 2026-03-15
**Status**: Awaiting Clarification Answers
