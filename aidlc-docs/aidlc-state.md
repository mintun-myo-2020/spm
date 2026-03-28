# AI-DLC State Tracking

## Project Information
- **Project Type**: Brownfield (Sprint 2)
- **Start Date**: 2026-03-08T00:00:00Z
- **Sprint 2 Start Date**: 2026-03-15T00:00:00Z
- **Current Stage**: CONSTRUCTION - Functional Design (Unit 2: OCR Test Upload) - IN PROGRESS

## Workspace State
- **Existing Code**: Yes (Sprint 1 complete)
- **Reverse Engineering Needed**: No
- **Workspace Root**: Current directory

## Code Location Rules
- **Application Code**: Workspace root (NEVER in aidlc-docs/)
- **Documentation**: aidlc-docs/ only
- **Structure patterns**: See code-generation.md Critical Rules

## Sprint 2 Execution Plan Summary
- **Total Stages to Execute**: 7
- **Unit Structure**: Per-feature (3 units)
- **INCEPTION Stages**: Workspace Detection ✓, Requirements Analysis ✓, Workflow Planning (in progress)
- **CONSTRUCTION Stages**: Functional Design (Units 2,3), Code Generation (Units 1,2,3), Build and Test
- **Stages Skipped**: User Stories, App Design, Units Gen, NFR Req (all), NFR Design (all), Infra Design (all), Functional Design (Unit 1)

## Extension Configuration
| Extension | Enabled | Decided At |
|---|---|---|
| Security Baseline | No | Requirements Analysis (Sprint 1) |

## Sprint 1 — COMPLETED
All INCEPTION and CONSTRUCTION stages complete for both units (Backend API + Frontend Application).

## Sprint 2 — Stage Progress

### INCEPTION PHASE
- [x] Workspace Detection - COMPLETED
- [x] Requirements Analysis - COMPLETED
- [x] Workflow Planning - IN PROGRESS
- [ ] User Stories - SKIP
- [ ] Application Design - SKIP
- [ ] Units Generation - SKIP

### CONSTRUCTION PHASE — Unit 1: Progress Report Content
- [x] Functional Design - SKIP
- [x] NFR Requirements - SKIP
- [x] NFR Design - SKIP
- [x] Infrastructure Design - SKIP
- [x] Code Generation - COMPLETED

### CONSTRUCTION PHASE — Unit 2: OCR Test Upload
- [x] Functional Design - COMPLETED
- [ ] NFR Requirements - SKIP
- [ ] NFR Design - SKIP
- [ ] Infrastructure Design - SKIP
- [x] Code Generation - COMPLETED

### CONSTRUCTION PHASE — Unit 3: Class Scheduling & Attendance
- [x] Functional Design - COMPLETED
- [ ] NFR Requirements - SKIP
- [ ] NFR Design - SKIP
- [ ] Infrastructure Design - SKIP
- [ ] Code Generation - IN PROGRESS

### Build and Test
- [ ] Build and Test - EXECUTE

## Current Status
- **Lifecycle Phase**: CONSTRUCTION
- **Current Stage**: CONSTRUCTION - Code Generation (Unit 3: Class Scheduling & Attendance) - IN PROGRESS
- **Next Stage**: Build and Test
- **Status**: Functional design complete for Unit 3. Starting code generation for scheduling module (backend + frontend).

## Sprint 3 — Report Plan Improvements

### Completed
- [x] Fix progress page to use percentages instead of raw scores (ProgressService, StudentProgressView)
- [x] Fix LLM stub returning empty plan (parseResponse fallback validation)
- [x] Switch LLM to Bedrock (.env, docker-compose.yml)
- [x] Consolidate BedrockConfig (single shared BedrockRuntimeClient)
- [x] LLM prompt fixes: gender-neutral language, no prescriptive advice, checkable action items
- [x] Action plan checklist: `completed` field on ActionItem, toggle endpoint
- [x] JSON plan storage: `plan_json` column on progress_reports, serialized on generation
- [x] Async report generation: fire-and-forget with IN_PROGRESS/COMPLETED/FAILED status
- [x] Transaction commit ordering fix for async dispatch
- [x] Pluggable job dispatcher abstraction (ReportJobDispatcher interface)
- [x] Config-driven environment swaps (dispatcher, storage, LLM, extraction)

### Remaining / Future
- [ ] Frontend report detail view — render planJson interactively with checkable action items
- [ ] SQS-based ReportJobDispatcher implementation (when needed for prod)
- [ ] S3-based ReportStorage implementation (when needed for prod)
