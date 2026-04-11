---
phase: 11-large-battle-ai-profiling-baseline
plan: 01
subsystem: planning
tags: [planning, performance, profiling, ai, pathfinding, gametest, bannermod]
requires: []
provides:
  - canonical large-battle profiling scenario matrix for future AI and pathfinding phases
  - repeatable baseline capture procedure and acceptance rules
  - root verification reference separating baseline definition from later instrumentation work
affects: [planning, performance-roadmap, recruit-battle-profiling]
tech-stack:
  added: []
  patterns: [evidence-first performance work rooted in existing battle stress fixtures and current async target/pathfinding seams]
key-files:
  created:
    - .planning/phases/11-large-battle-ai-profiling-baseline/11-PROFILING-BASELINE.md
    - .planning/phases/11-large-battle-ai-profiling-baseline/11-VALIDATION.md
    - .planning/phases/11-large-battle-ai-profiling-baseline/11-large-battle-ai-profiling-baseline-01-SUMMARY.md
  modified:
    - .planning/VERIFICATION.md
key-decisions:
  - "Keep the mandatory baseline anchored to BattleStressFixtures.BASELINE_DENSE and HEAVY_DENSE instead of inventing a new benchmark map."
  - "Record async pathfinding and async target-finding config with every capture so later optimization results stay comparable."
  - "Reserve all new runtime counters or harness seams for Plan 11-02 and keep Plan 11-01 documentation-only."
patterns-established:
  - "Later performance phases can compare before/after evidence using one shared scenario id, counter vocabulary, and evidence bundle format."
requirements-completed: []
duration: pending
completed: 2026-04-11
---

# Phase 11 Plan 01: Large-Battle AI Profiling Baseline Summary

**BannerMod now has an explicit profiling contract for recruit-heavy battles, including the scenario matrix, hotspot inventory, required counters, and evidence package that later optimization phases must rerun.**

## Accomplishments

- Added `11-PROFILING-BASELINE.md` to define the mandatory stress scenarios, optional mixed-squad comparison leg, current AI/pathfinding hotspots, and normalized evidence bundle shape.
- Added `11-VALIDATION.md` to document the baseline capture procedure, config snapshot requirements, run order, sample expectations, and acceptance rules.
- Updated `.planning/VERIFICATION.md` so contributors can see that Plan 11-01 defines the profiling contract while Plan 11-02 will add any missing instrumentation.

## Files Created/Modified

- `.planning/phases/11-large-battle-ai-profiling-baseline/11-PROFILING-BASELINE.md` - Canonical large-battle scenario, hotspot, counter, and evidence-package specification.
- `.planning/phases/11-large-battle-ai-profiling-baseline/11-VALIDATION.md` - Repeatable baseline capture and acceptance procedure.
- `.planning/VERIFICATION.md` - Root verification reference for the new Phase 11 profiling baseline entrypoints.

## Decisions Made

- Kept the baseline grounded in existing recruit GameTest battle fixtures and current async seams.
- Required every future capture to record async toggles and worker-thread counts alongside profiler output.
- Left instrumentation implementation explicitly out of scope for this plan.

## Deviations From Plan

None - plan executed within the intended doc-only scope.

## Issues Encountered

- None in-scope.

## Next Phase Readiness

- Phase 11 now has a stable profiling contract, so Plan 11-02 can focus on the smallest instrumentation or harness changes needed to gather the missing counters without redefining the benchmark.
- No transition/state update was performed because execution was requested with `--no-transition`.

---
*Phase: 11-large-battle-ai-profiling-baseline*
*Completed: 2026-04-11*
