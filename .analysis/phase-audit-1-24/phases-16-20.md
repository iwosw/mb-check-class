# Phases 16-20 Audit

## Summary table

| Phase | Status claim | Audit verdict | Critical/High count |
|-------|--------------|---------------|-------------------|
| 16 | Complete (2/2 plans); async reliability, delivered-vs-dropped counters | PARTIAL MISMATCH: class present, async handoff/cancellation logic exists, but **no delivered-vs-dropped counters found** | 1 HIGH |
| 17 | Complete (2/2 plans); RecruitAiLodPolicy present, config knobs, LOD skip/tier counters | PASS: class found, wired into recruit loop, settings-based config, LOD tiers and skip counting present | 0 |
| 18 | Complete (4/4 plans); drop decision documented in `18-DECISION.md`, zero flow-field code merged | **CRITICAL MISMATCH: `18-DECISION.md` does NOT exist**, despite explicit ROADMAP claim; flow-field runtime code absent (PASS) | 1 HIGH |
| 19 | Complete (2/2 plans); validation/results published, evidence directory with profiling | **CRITICAL MISMATCH: No `19-01-PLAN.md`, `19-02-PLAN.md`, `19-VALIDATION.md`, `19-RESULTS.md`, or evidence/ directory found** | 2 CRITICAL |
| 20 | Complete (2/2 plans); three artifacts present; bannerlord→bannermod crosswalk documented | PASS: all three artifacts (`20-RUNTIME-AUDIT.md`, `20-OWNERSHIP-MATRIX.md`, `20-TARGET-ARCHITECTURE.md`) present; convergence explicitly documented | 0 |

---

## Findings

### Phase 16: Async Pathfinding Reliability Fixes

**[HIGH]** Delivered-vs-dropped callback counters not found in runtime
- **Claim** (ROADMAP): "retained battle GameTests expose delivered-versus-dropped callback accounting"
- **Reality** (`AsyncPath.java`, `AsyncPathProcessor.java`): Class hierarchy manages post-processing callbacks with exception handling; no visible counters for delivered/dropped tracking found in codebase search
- **Impact**: Claim of explicit delivered-vs-dropped telemetry cannot be verified; audit passes on async safety (stale result guards, cancellation, handoff via `AsyncPathProcessor`) but fails on counter evidence
- **Fix hint**: Either implement `delivered`/`dropped` counters in async callback delivery path or update ROADMAP to correct the claim

---

### Phase 17: AI LOD and Tick Shedding

**[PASS]** All audit criteria met
- `RecruitAiLodPolicy.java` class present and fully implemented with three LOD tiers (FULL/REDUCED/SHED)
- Policy wired into `RecruitRuntimeLoop::evaluateTargetSearchLod()` and called every base search tick
- Config integration: `RecruitAiLodPolicy.settingsFromConfig()` retrieves settings (enabled flag, proximity threshold, distance thresholds, search intervals)
- LOD counting: `TargetSearchProfilingCounters` tracks LOD tiers, skip opportunities, and actual skips via `recordLodTier()`, `recordSearchOpportunity()`, `recordLodSkip()` methods
- Recruits on FULL cadence preserved for close/recently-damaged/combat scenarios (lines 109-111 of RecruitAiLodPolicy.java)
- GameTests call `targetSearchProfilingSnapshot()` to verify counters
- No HIGH/CRITICAL issues

---

### Phase 18: Optional Flow-Field Navigation Evaluation

**[CRITICAL]** Decision artifact missing despite explicit ROADMAP claim

- **Claim** (ROADMAP): "Phase 18 closes with `drop` in `18-DECISION.md`"
- **Reality**: File `/home/kaiserroman/bannermod/.planning/phases/18-optional-flow-field-navigation-evaluation/18-DECISION.md` does not exist
  - Directory contains only `18-CONTEXT.md`
  - ROADMAP explicitly claims the decision file exists and contains the drop verdict
- **Runtime evidence**: No FlowField, flow-field, or flow_field classes found anywhere in `src/main/java/**` (verified with glob and grep)
- **Verdict**: Flow-field code is absent from runtime (PASS), but the documented decision artifact is missing (HIGH/CRITICAL severity)
- **Fix hint**: Either create `18-DECISION.md` with the drop rationale or update ROADMAP to remove the reference

---

### Phase 19: Large-Battle Performance Validation

**[CRITICAL]** Multiple artifacts missing despite "Complete (2/2 plans)" claim

- **Claim** (ROADMAP line 103): "Status: Complete (2/2 plans complete as of 2026-04-12); retained required GameTest gate is green again, optimized-runtime evidence now lives under `.planning/phases/19-large-battle-performance-validation/evidence/`, and the final closeout is published in `19-VALIDATION.md` and `19-RESULTS.md`"
- **Reality** (verified with find): 
  - Directory `/home/kaiserroman/bannermod/.planning/phases/19-large-battle-performance-validation/` contains **only** `19-CONTEXT.md`
  - Missing: `19-01-PLAN.md`, `19-02-PLAN.md`, `19-VALIDATION.md`, `19-RESULTS.md`
  - Missing: `evidence/` subdirectory (ROADMAP explicitly claims "optimized-runtime evidence now lives under `.../evidence/`")
- **Impact**: 
  - No plan execution records for either slice
  - No validation or results documentation
  - No profiling evidence bundles
  - Audit cannot verify before/after comparisons or remaining hotspot analysis
- **Severity**: CRITICAL — plan completion is claimed but core deliverables are absent
- **Fix hint**: Either execute and deliver the missing plans/evidence or mark phase as incomplete in STATE.md

---

### Phase 20: Runtime Audit And Bannerlord Target Architecture

**[PASS]** All required artifacts present and coherent

- ✓ `20-RUNTIME-AUDIT.md` (present, 113 lines): documents boot, ownership surfaces, cross-package dependencies, migration implications
- ✓ `20-OWNERSHIP-MATRIX.md` (present, 40 lines): explicit Wave 1-5 move plan with target `com.talhanation.bannerlord.**` and blocker/adapter notes
- ✓ `20-TARGET-ARCHITECTURE.md` (present, 218 lines): canonical bannerlord package families, move-order waves, compatibility boundary, source-root retirement preconditions
- **Bannerlord→bannermod crosswalk**: Clearly documented in `20-TARGET-ARCHITECTURE.md` lines 5-8:
  - "Canonical Java destination for the physical move is `src/main/java/com/talhanation/bannerlord/**`"
  - "Runtime identity does **not** move with Java packages: the live Forge mod id remains `bannermod`"
  - "Phase 21 should execute as a controlled package-family re-home with temporary adapters"
- Phase 21 confirmed in STATE.md line 126: "Phase 21 completed with one canonical production tree under `src/main/java/com/talhanation/bannermod/**`" (note: bannermod, not bannerlord — the move was to an intermediate staging tree, not the originally-planned bannerlord namespace)
- No HIGH/CRITICAL issues

---

## Noted clean items

- **Phase 16 async runtime safety**: `AsyncPath.process()` with synchronized guard, `postProcessing` callback list, exception safety, stale-result guards on accessor methods (`ensureProcessed()` checks), `AsyncPathProcessor` with executor lifecycle management — async infrastructure is sound
- **Phase 17 LOD policy purity**: Class is pure (no world/entity state held), operates on immutable Context + Settings, returns explicit Evaluation tuple — design is correct even if counters in Phase 16 are missing
- **Phase 18 flow-field absence**: Zero flow-field implementation found anywhere in runtime tree under `src/main/java/**` — the "drop" decision is respected at the code level
- **Phase 20 source consolidation**: Phase 21 successfully completed tree consolidation; no split runtime traces remain active in build or source config

---

## Critical summary

| Severity | Count | Issues |
|----------|-------|--------|
| CRITICAL | 2 | Phase 19: four plan/evidence artifacts missing; Phase 18: decision document missing |
| HIGH | 2 | Phase 16: delivered-vs-dropped counters claimed but not found; Phase 18: ROADMAP claims decision file that doesn't exist |
| MEDIUM | 0 | |
| LOW | 0 | |

---

## Top 3 actionable issues

1. **Phase 19 evidence gap** — ROADMAP claims completion with profiling evidence, validation, and results files that do not exist. Either execute the missing plans and capture evidence, or correct STATE.md to mark phase as incomplete. This blocks future performance tuning work that depends on measured baselines.

2. **Phase 18 decision artifact** — ROADMAP explicitly references `18-DECISION.md` as the source of the drop verdict, but the file is absent. Create the artifact or remove the reference from ROADMAP to keep planning truth current.

3. **Phase 16 counter verification** — ROADMAP claims "delivered-versus-dropped callback accounting" is exposed in retained GameTests, but no such counters are visible in the async pathfinding classes or test references. Verify whether counters exist elsewhere in test infrastructure or if this claim should be revised.

---

## Phase completion claims verification

- **Phase 16**: Marked Complete; async safety infrastructure present; counter claim unverified → PARTIAL
- **Phase 17**: Marked Complete; all criteria met → VERIFIED
- **Phase 18**: Marked Complete; decision file missing → INCOMPLETE DOCUMENTATION
- **Phase 19**: Marked Complete; core plan, validation, results, and evidence artifacts all missing → INCOMPLETE (false claim)
- **Phase 20**: Marked Complete; all artifacts present and coherent → VERIFIED

---

## Flow-field evaluation status

**Phase 18 runtime verdict**: DROPPED from active runtime ✓
- No FlowField, flow-field, or flow_field classes anywhere in `src/main/java/**`
- No configuration or guarding flags for optional flow-field paths
- Conclusion: Drop decision was applied to the runtime build, but the decision artifact itself is missing from `.planning/`

