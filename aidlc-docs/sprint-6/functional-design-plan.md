# Sprint 6 — Functional Design Plan — Multi-Tenant Data Segregation

## Overview
Design the detailed business logic, domain model changes, and business rules for adding multi-tenant center-level data segregation to the SPM application.

## Plan Steps

### Domain Model Changes
- [x] Step 1: Design `Tenant` entity and `tenants` table schema
- [x] Step 2: Design `TenantAwareBaseEntity` (extends BaseEntity with tenant_id)
- [x] Step 3: Document all entities that extend TenantAwareBaseEntity (full inventory)
- [x] Step 4: Design `TenantContext` holder (request-scoped tenant resolution)

### Business Logic Model
- [x] Step 5: Design tenant resolution flow (JWT → organization claim → tenant_id lookup)
- [x] Step 6: Design tenant validation filter/interceptor (reject requests with missing/invalid tenant)
- [x] Step 7: Design repository method changes (all queries gain tenant_id parameter)
- [x] Step 8: Design service layer changes (TenantContext injection into all service methods)
- [x] Step 9: Design `CurrentUserService.provisionFromToken()` updates (tenant-aware provisioning)
- [x] Step 10: Design email uniqueness constraint changes (unique per tenant, not globally)

### Business Rules
- [x] Step 11: Define tenant isolation rules (hard rules for data access)
- [x] Step 12: Define tenant provisioning rules (CLI script behavior)
- [x] Step 13: Define tenant resolution caching rules (cache strategy for org→tenant lookup)
- [x] Step 14: Define email-based user linking rules (tenant validation during linking)

### Frontend Changes
- [x] Step 15: Design `kc_org` login hint derivation from URL
- [x] Step 16: Design TenantContext React provider (org ID + name from JWT after login)
- [x] Step 17: Design Navbar center name display

### Database Migration
- [x] Step 18: Design Flyway migration (V16) — tenants table + tenant_id columns + indexes
- [x] Step 19: Design seed data strategy changes (per-tenant via provisioning script)

---

## Clarifying Questions

Before generating the functional design artifacts, the following questions need answers. Please see `sprint-6/functional-design-questions.md`.
