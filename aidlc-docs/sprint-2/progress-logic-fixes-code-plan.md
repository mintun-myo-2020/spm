# Code Generation Plan — Progress Logic Fixes

## Files Modified

### Backend
- [x] `spm/src/main/java/com/eggtive/spm/progress/service/ProgressService.java` — REQ-PF1, PF2, PF8
- [x] `spm/src/main/java/com/eggtive/spm/report/service/ReportDataAssembler.java` — REQ-PF1, PF4
- [x] `spm/src/main/java/com/eggtive/spm/report/service/ReportData.java` — REQ-PF4 (averageScore → averagePercentage)
- [x] `spm/src/main/java/com/eggtive/spm/report/service/TemplateReportContentGenerator.java` — REQ-PF4 (updated field reference)

### Steps

- [x] **Step 1 (REQ-PF8)**: Extract shared topic aggregation from `getTopicsProgressByClass` and `getAllTopicsProgress` into `aggregateTopicProgress(List<TestScore>)`.
- [x] **Step 2 (REQ-PF1)**: In extracted helper, `getClassSummary`, and `ReportDataAssembler.buildTopicSummaries`: compute topic average as `totalScore / totalMaxScore * 100` (sum-based). Trend still uses per-test percentages.
- [x] **Step 3 (REQ-PF2)**: In extracted helper, select `latestPct` by max `testDate` instead of relying on insertion order.
- [x] **Step 4 (REQ-PF4)**: In `ReportDataAssembler.buildOverallSummary`, convert `overallScore` to percentage before averaging/trend. Renamed `OverallSummary.averageScore` → `averagePercentage`. Updated `TemplateReportContentGenerator`.
- [ ] **Step 5**: Verify compilation with `getDiagnostics`.
