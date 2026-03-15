# Unit 1: Progress Report Content — Code Generation Plan

## Unit Context
- **Unit**: Progress Report Content (FR-9)
- **Scope**: Modify existing report module to produce real HTML content instead of placeholder
- **Dependencies**: ReportService, ProgressService, TestScoreService, FeedbackService (all existing)
- **No new tables or entities**
- **Key Design Goal**: Abstraction for future AI-powered report generation

## Architecture — Abstraction Strategy

The report content generation is behind a `ReportContentGenerator` interface. This sprint ships a `TemplateReportContentGenerator` that builds HTML from structured data. In a future sprint, an `AiReportContentGenerator` can be swapped in (or composed with the template one) to produce AI-enhanced reports with study plans, weakness analysis, etc.

The data assembly is separated from content rendering:
- `ReportDataAssembler` — gathers all raw data (scores, topics, feedback, question-level detail) into a `ReportData` record. This is reusable regardless of how the report is rendered.
- `ReportContentGenerator` interface — takes `ReportData` and produces HTML. Implementations can be template-based, AI-based, or a hybrid.

```
ReportService
  └─> ReportDataAssembler.assemble(student, classId, startDate, endDate)
        └─> returns ReportData (scores, topics, feedback, question details)
  └─> ReportContentGenerator.generate(ReportData)
        └─> returns String (HTML)
        └─> impl: TemplateReportContentGenerator (this sprint)
        └─> impl: AiReportContentGenerator (future — calls LLM with ReportData as context)
```

## Existing Files to Modify
- `ReportService.java` — inject ReportDataAssembler + ReportContentGenerator, replace stub
- `GenerateReportRequestDTO.java` — add classId, make startDate/endDate required
- `FeedbackRepository.java` — add query for feedback by student+teacher within date range
- `ReportList.tsx` — add date range inputs and class selector to generate modal
- `forms.ts` — update GenerateReportForm

## New Files to Create
- `ReportData.java` — rich data record with all report inputs (scores, topics, feedback, questions)
- `ReportDataAssembler.java` — service that gathers data from existing services into ReportData
- `ReportContentGenerator.java` — interface for content generation
- `TemplateReportContentGenerator.java` — HTML template implementation

---

## Steps

### Step 1: Create ReportData record and ReportContentGenerator interface
- [x] Create `ReportData.java` in `report/service/` — record containing:
  - Student info (name, id)
  - Class info (name, subject name, teacher name)
  - Date range (startDate, endDate)
  - List of score entries (test name, date, score, maxScore)
  - List of topic summaries (topic name, average %, trend, question count)
  - List of feedback entries (date, strengths, areas for improvement, recommendations)
  - Question-level detail per test (question text, sub-questions with topic, score, maxScore) — included now so AI can use it later
- [x] Create `ReportContentGenerator.java` interface in `report/service/`:
  ```java
  public interface ReportContentGenerator {
      String generate(ReportData data);
  }
  ```

### Step 2: Create ReportDataAssembler
- [x] Create `ReportDataAssembler.java` in `report/service/`
- [x] Inject TestScoreService, FeedbackRepository, ClassService (or ClassRepository)
- [x] Method: `assemble(Student student, UUID classId, LocalDate startDate, LocalDate endDate)` returns `ReportData`
- [x] Fetch test scores for student in class within date range
- [x] Compute topic summaries from sub-question data
- [x] Fetch most recent 5 feedback entries from the class teacher within date range
- [x] Include question-level detail (question text, sub-questions, topics, scores)
- [x] Return fully populated ReportData

### Step 3: Create TemplateReportContentGenerator
- [x] Create `TemplateReportContentGenerator.java` in `report/service/`
- [x] Annotated with `@Service` and `@Primary` (so it's the default, future AI impl can override)
- [x] Implements `ReportContentGenerator`
- [x] Produces self-contained HTML with inline CSS
- [x] Sections: header, overall summary, score trend table, topic performance, feedback summary
- [x] Clean, readable HTML suitable for browser viewing

### Step 4: Update FeedbackRepository
- [x] Add query method to find recent feedback by student + teacher + date range
- [x] `List<Feedback> findTop5ByStudentIdAndTeacherIdAndCreatedAtBetweenOrderByCreatedAtDesc(UUID studentId, UUID teacherId, Instant from, Instant to)`
- [x] Or use `@Query` with `LIMIT 5` if method name is too long

### Step 5: Update GenerateReportRequestDTO
- [x] Add `@NotNull UUID classId`
- [x] Add `@NotNull` to `startDate` and `endDate`

### Step 6: Update ReportService.generateReport()
- [x] Inject `ReportDataAssembler` and `ReportContentGenerator`
- [x] Replace stub HTML with:
  1. Look up class to get teacher context
  2. Call `assembler.assemble(student, classId, startDate, endDate)`
  3. Call `generator.generate(reportData)`
  4. Upload generated HTML to storage
- [x] Keep existing S3 upload, entity creation, and DTO mapping logic

### Step 7: Update Frontend — GenerateReportForm and ReportList
- [x] Update `GenerateReportForm` in `forms.ts`: add `classId: string`, make `startDate`/`endDate` required
- [x] Update `ReportList.tsx` generate modal:
  - Add start date and end date inputs
  - Add class selector dropdown (only shown when `canGenerate` is true)
  - Fetch classes for the current teacher/admin via classService
  - Pass classId, startDate, endDate in the generate request
  - Validate dates before submission

### Step 8: Documentation
- [x] Create `aidlc-docs/sprint-2/unit1-report-content/code-summary.md`
- [x] Document the abstraction strategy and how to add AI implementation later

---

## Completion Criteria
- `ReportContentGenerator` interface exists — future AI impl just needs to implement this
- `ReportData` record contains rich data including question-level detail (ready for AI consumption)
- `ReportDataAssembler` cleanly separates data gathering from rendering
- `TemplateReportContentGenerator` produces self-contained HTML with all 5 sections
- Reports scoped to requesting teacher's class
- Date range is teacher-specified (required)
- Frontend generate modal includes date range + class selector
- Existing report list/view functionality unchanged
