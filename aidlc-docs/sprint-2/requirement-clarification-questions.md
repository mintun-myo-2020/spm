# Sprint 2 — Clarification Questions

I found a couple of spots where I can give you guidance, and one answer that needs a quick clarification.

---

## Guidance 1: Local Dev File Storage (Q3)

Here's the breakdown:

| Option | Pros | Cons |
|---|---|---|
| A) `spm/uploads/` under project root | Easy to find/inspect during dev, survives restarts | Risk of accidentally committing to git, clutters project dir |
| B) System temp dir (`java.io.tmpdir`) | Clean, OS-managed, no project clutter | Files disappear on restart, hard to inspect |
| C) Configurable via `application.yml` | Most flexible, can point anywhere | Slightly more setup |

**My recommendation**: Option C (configurable) with a sensible default of `./uploads/` in the project root. This gives you the best of both worlds — easy to inspect during dev, and you can override it per environment. We'd add `uploads/` to `.gitignore`.

A) Accept recommendation — configurable path, default `./uploads/`, added to `.gitignore`
B) Use system temp dir instead
C) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Guidance 2: File Size Limit (Q5)

For ~20 pages of smartphone photos:
- A typical smartphone photo (12-50MP) is 3-8 MB as JPEG
- 20 photos = 60-160 MB total if uploaded individually
- But the requirement says upload a single test paper image/PDF, not 20 separate files
- A single scanned page photo: 3-8 MB
- A multi-page PDF scan (20 pages): 20-80 MB depending on compression

Since the requirement is "upload an image (JPEG, PNG) or PDF of a test paper":

A) 10 MB — covers single-page photos comfortably, keeps things snappy
B) 25 MB — covers multi-page PDFs of ~5-10 pages
C) 50 MB — covers larger multi-page PDFs up to ~20 pages
D) Other (please describe after [Answer]: tag below)

[Answer]: C i need it to be able to upload multiple photos / 1pdf of multiple pages. each exam is obviously more than 1 photo/1page in a pdf.

---

## Clarification 1: Schedule Deletion vs Student Enrollment (Q7)

Quick clarification — deleting a schedule removes the recurring class time slot (e.g., "Monday 4-6pm"), which affects ALL students in that class, not just one student. It's a class-level action, not a student-level one.

If a single student leaves the class, that's handled by removing them from the class enrollment (which already exists in Sprint 1).

With that context, does your answer change?

A) Keep answer B — hard delete schedule + cancel all future sessions (use case: class is discontinued entirely)
B) Change to A — soft delete (mark inactive), keep existing session records for history
C) Change to C — deactivate by setting end date (most flexible, preserves history)
D) Other (please describe after [Answer]: tag below)

[Answer]: C.

---

## Clarification 2: Recurrence Patterns (Q6 expanded)

Your answer to Q6 mentioned wanting flexible recurrence patterns: "x times a week, weekly, monthly, with end date." This is more complex than the original requirement (which only had weekly recurring + one-off).

How far do you want to go with recurrence?

A) Simple — weekly only (e.g., every Monday 4-6pm). One-off sessions for anything else. This is what the original requirement describes.
B) Moderate — weekly + bi-weekly + specific days of week (e.g., Mon+Wed 4-6pm). End date supported.
C) Full — weekly, bi-weekly, monthly, custom patterns. Basically a calendar-grade recurrence engine.
D) Other (please describe after [Answer]: tag below)

[Answer]: A with end date supported.

