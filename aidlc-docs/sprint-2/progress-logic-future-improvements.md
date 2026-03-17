# Future Improvements — Progress & Trend Logic

Items deferred from the post-review of commit `3211242`. These are not bugs but would improve accuracy and UX as the product matures.

## 1. Time-Weighted Trend Calculation
**What**: Apply recency weighting so recent tests influence the trend more than older ones (e.g., exponential decay or sliding window).
**Why**: Currently a test from 6 months ago counts the same as yesterday's test. For a progress tracker, recent performance is more indicative of current ability. The `calculateVelocity` method already exists but isn't used for trend — could be leveraged.

## 2. Trend Stability for Small Sample Sizes
**What**: Refine the first-half/second-half split logic for small N. With 2 tests, trend is just "did the second test score 2+ points higher?" With 3 tests, the split is asymmetric (`[0:1]` vs `[1:3]`).
**Why**: Small sample sizes produce jumpy, potentially misleading trends. Options: require a minimum of 3+ data points before showing a trend (instead of 2), or use a different algorithm for N < 5 (e.g., simple delta between first and last).

## 3. Topic Coverage Indicator
**What**: Show how many of the student's total tests actually covered a given topic (e.g., "Geometry: 2 of 10 tests").
**Why**: Currently the UI shows "2 tests, 5 questions" but doesn't distinguish between "2 tests out of 2 total" vs "2 tests out of 10 total." A topic with low coverage may have unreliable averages, and the teacher should know that.

## 4. Clarify Question Count Label in UI
**What**: Change the UI label from "12 questions" to "12 questions across 3 tests" or show average questions per test.
**Why**: "12 questions" is ambiguous — a user might think it means 12 questions per test. Adding "across X tests" removes the ambiguity without adding clutter.
