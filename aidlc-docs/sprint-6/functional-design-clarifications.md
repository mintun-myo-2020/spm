# Sprint 6 — Functional Design Clarifications

Two answers need a quick follow-up.

## Clarification 1: FeedbackTemplate system-wide scope (Q3)

To explain: `FeedbackTemplate` is a feature from Sprint 1 where teachers can save reusable feedback snippets (e.g., "Needs to practice algebra more", "Excellent improvement in comprehension"). The `isSystemWide = true` flag means the template is visible to ALL teachers, not just the one who created it — like a shared library of common feedback phrases.

With multi-tenancy, the question is: should "system-wide" templates be shared across ALL tenants (truly global), or only shared within a single tenant?

A) Tenant-scoped — each tenant has their own "system-wide" templates (Center A's shared templates are invisible to Center B)
B) Truly global — one set of system-wide templates shared across all tenants
C) Other (please describe after [Answer]: tag below)

[Answer]: A

## Clarification 2: Background notification processing (Q4)

You said "why add complexity? isn't it just add 1 filter" — I want to confirm: you're saying the background job should process ALL pending notifications across all tenants in one pass (option A from the original question), since adding a tenant filter to the background job would be unnecessary complexity?

Or are you saying adding a tenant filter IS simple (just one WHERE clause) so we should do per-tenant filtering (option B)?

A) Cross-tenant — single background job processes all pending notifications regardless of tenant (no filter needed)
B) Per-tenant — add tenant_id filter to background notification queries (it's just one extra WHERE clause, worth doing for consistency)
C) Other (please describe after [Answer]: tag below)

[Answer]: B
