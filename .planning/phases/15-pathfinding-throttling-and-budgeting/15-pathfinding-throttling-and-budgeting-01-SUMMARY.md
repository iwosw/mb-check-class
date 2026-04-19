---
phase: 15-pathfinding-throttling-and-budgeting
plan: 01
subsystem: runtime
tags: [runtime, performance, pathfinding, budget, throttling, bannermod, retroactive]
requires:
  - phase: 14-formation-level-target-selection-rewrite
    provides: formation-level target selection profiling counters on the shared controller seam
provides:
  - per-tick pathfinding budget accounting on the shared controller seam
  - deterministic deferral and reset semantics for bursty path issuance
  - test-only budget reconfiguration hook for deterministic unit/gametest coverage
affects: [phase-16-async-pathfinding, phase-19-validation, performance-evidence]
tech-stack:
  added: []
  patterns: [controller-owned per-tick budget counters with deterministic deferral path]
key-files:
  created:
    - .planning/phases/15-pathfinding-throttling-and-budgeting/15-pathfinding-throttling-and-budgeting-01-SUMMARY.md
  modified:
    - src/main/java/com/talhanation/bannermod/ai/pathfinding/GlobalPathfindingController.java
key-decisions:
  - "Bundle budget counters into the existing GlobalPathfindingController seam instead of introducing a separate throttler so later async fixes extend one shared surface."
  - "Expose configureBudgetForTests(...) as the sole knob for deterministic test-side budget overrides; production callers rely on config-driven defaults."
patterns-established:
  - "Budget accounting is additive beside reuse counters on the same controller snapshot, so before/after evidence stays comparable to Phase 12/13 captures."
requirements-completed: []
duration: not-recorded
completed: 2026-04-12
---

# Phase 15 Plan 01: Pathfinding Throttling And Budgeting Summary

> Retroactive reconstruction 2026-04-19: written after planning-dir audit surfaced gaps; code state verified against active tree (`src/main/java/com/talhanation/bannermod/ai/pathfinding/GlobalPathfindingController.java`) and the ported `GlobalPathfindingControllerTest` cases (`configureBudgetForTests`, `reuseAttempts`, `reuseHits`, budget-exhaustion deferral counters).

**Per-tick pathfinding work is now bounded by an explicit budget accounted inside `GlobalPathfindingController`, so bursty large-battle request streams defer deterministically instead of swamping one server tick.**

## Performance

- **Duration:** not recorded
- **Started:** not recorded
- **Completed:** 2026-04-12 (retroactive verification; original execution date per ROADMAP: 2026-04-12)
- **Tasks:** 1 (retroactive reconstruction)
- **Files modified:** 1 live (`GlobalPathfindingController`); retroactive planning doc added under this phase directory.

## Accomplishments

- Bundled a per-tick pathfinding budget into the existing `GlobalPathfindingController` seam, so Phase 15 extends the same surface already carrying Phase 12 request counters and Phase 13 reuse counters instead of introducing a separate throttler.
- Added deterministic deferral semantics: once the per-tick budget is consumed, further requests record as deferred/dropped inside the controller snapshot rather than issuing full pathfinder work on the same tick.
- Exposed `configureBudgetForTests(...)` so unit and GameTest coverage can pin the budget deterministically without touching the production config path; production callers continue to read the budget from `RecruitsServerConfig`-derived defaults.
- Kept the reuse counters (`reuseAttempts`, `reuseHits`, discard reasons) untouched so Phase 12/13 before/after evidence bundles remain comparable.

## Task Commits

Not recorded for this slice — the retroactive reconstruction is documentation-only; the live runtime counters were landed as part of the controller seam Phase 12 already owns.

## Files Created/Modified

- `src/main/java/com/talhanation/bannermod/ai/pathfinding/GlobalPathfindingController.java` — per-tick budget counters, deferred-request accounting, and `configureBudgetForTests(...)` hook live on the shared controller seam.
- `.planning/phases/15-pathfinding-throttling-and-budgeting/15-pathfinding-throttling-and-budgeting-01-SUMMARY.md` — this retroactive summary.

## Decisions Made

- Budget accounting was intentionally kept controller-local so later async reliability work (Phase 16) and validation work (Phase 19) can read one snapshot shape instead of stitching a separate throttle metric set.
- Test-only configuration is exposed explicitly (`configureBudgetForTests`) rather than mutating the config spec in tests, keeping GameTest and JUnit runs deterministic across sessions.

## Deviations from Plan

- None in the runtime code. The original Plan 15-01 paperwork was lost before this audit; this retroactive summary restores the written record after the `GlobalPathfindingControllerTest` port confirmed the budget surface is live.

## Issues Encountered

- The original Phase 15 planning directory contained only `15-CONTEXT.md`; the absent SUMMARY was recovered retroactively after the parallel Phase 12 test restore exercised the budget hooks end-to-end.

## User Setup Required

None.

## Next Phase Readiness

- Phase 16 async reliability work can extend the same `GlobalPathfindingController` snapshot with delivery/drop counters beside the budget accounting.
- Phase 19 closing validation consumes controller budget counters as part of its comparable before/after profiling, so the Phase 11 baseline vocabulary still holds.

---
*Phase: 15-pathfinding-throttling-and-budgeting*
*Completed: 2026-04-12 (retroactive reconstruction 2026-04-19)*
