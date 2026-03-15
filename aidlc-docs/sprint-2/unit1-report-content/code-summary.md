# Unit 1: Progress Report Content — Code Summary

## Architecture

```
ReportService
  └─> ReportDataAssembler.assemble(student, classId, startDate, endDate)
        └─> returns ReportData (scores, topics, feedback, question details)
  └─> ReportContentGenerator.generate(ReportData)
        └─> returns String (HTML)
        └─> impl: TemplateReportContentGenerator (@Primary, this sprint)
        └─> impl: AiReportContentGenerator (future — swap in via @Primary or @Qualifier)
```

## New Files Created

| File | Purpose |
|---|---|
| `report/service/ReportData.java` | Rich data record with nested records: StudentInfo, ClassInfo, OverallSummary, ScoreEntry, TopicSummary, FeedbackEntry, TestDetail, QuestionDetail, SubQuestionDetail |
| `report/service/ReportContentGenerator.java` | Interface — `String generate(ReportData data)` |
| `report/service/ReportDataAssembler.java` | Gathers data from TestScoreService, FeedbackRepository, ClassService into ReportData |
| `report/service/TemplateReportContentGenerator.java` | `@Service @Primary` — produces self-contained HTML with inline CSS |

## Modified Files

| File | Change |
|---|---|
| `report/service/ReportService.java` | Injected ReportDataAssembler + ReportContentGenerator, replaced stub HTML with real content pipeline |
| `report/dto/GenerateReportRequestDTO.java` | Added `@NotNull UUID classId`, made `startDate`/`endDate` `@NotNull` |
| `feedback/repository/FeedbackRepository.java` | Added `findRecentByStudentAndTeacher()` query (LIMIT 5, ordered by createdAt DESC) |
| `spm-frontend/src/types/forms.ts` | Updated `GenerateReportForm`: added `classId`, made `startDate`/`endDate` required, removed `selectedTestIds` |
| `spm-frontend/src/components/shared/ReportList.tsx` | Added class selector dropdown, date range inputs, validation to generate modal |

## Future AI Integration

To add AI-powered report generation:
1. Create `AiReportContentGenerator implements ReportContentGenerator`
2. Annotate with `@Service` (and `@Primary` to override template, or use `@Qualifier` for composition)
3. The `ReportData` record already includes question-level detail (question text, sub-questions with topic/score/maxScore/studentAnswer) — pass this as context to the LLM
4. No changes needed to `ReportService` or `ReportDataAssembler`
