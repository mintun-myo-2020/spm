# Sprint 2 — Progress Logic Fixes (Post-Review)

## Context
Code review of commit `3211242` (update performance and trend logic) identified correctness and maintainability issues in the progress/trend calculation logic.

## Requirements

### REQ-PF1: Sum-Based Topic Average (Every Question Counts Equally)
**Problem**: Topic average is computed by averaging per-test percentages. A test with 1 question on a topic has equal influence to a test with 10 questions. E.g. 1/1 (100%) and 8/10 (80%) averages to 90%, but the student actually got 9/11 = 81.8%.
**Fix**: Compute topic average as `totalScore / totalMaxScore * 100` across ALL sub-questions for that topic, regardless of which test they came from. Trend still uses per-test percentages (chronological) to detect improvement/decline over time. Applies to `ProgressService` (extracted helper + `getClassSummary`) and `ReportDataAssembler.buildTopicSummaries`.

### REQ-PF2: Trend Compares Latest vs Previous Average
**Problem**: Trend uses a first-half/second-half split which is arbitrary and asymmetric for small sample sizes. The "earlier" group includes the latest test, muddying the comparison.
**Fix**: Trend compares the latest test's percentage against the average of all previous tests (excluding the latest). "Am I doing better than my track record?" With 1 test → INSUFFICIENT_DATA. Applies to `SimpleProgressCalculator.determineTrend`. Also select `latestPct` by max date (not insertion order) in `ProgressService`.

### REQ-PF4: Overall Report Trend Uses Percentages (Not Raw Scores)
**Problem**: `ReportDataAssembler.buildOverallSummary` computes trend from raw `overallScore` values. Topic trends use percentages. If tests have different `maxScore`, the overall trend can contradict topic trends.
**Fix**: Convert each test's `overallScore` to a percentage (`overallScore * 100 / maxScore`) before computing the overall average and trend in `buildOverallSummary`.

### REQ-PF8: Extract Shared Topic Aggregation Logic
**Problem**: `getTopicsProgressByClass` and `getAllTopicsProgress` contain nearly identical aggregation code (~40 lines duplicated).
**Fix**: Extract the shared per-test-per-topic aggregation into a private helper method that both methods call.
