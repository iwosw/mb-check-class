---
phase: 17-ai-lod-and-tick-shedding
plan: 01
subsystem: ai
tags: [ai, performance, lod, tick-shedding, recruits, bannermod, retroactive]
requires:
  - phase: 16-async-pathfinding-reliability-fixes
    provides: hardened async callback hygiene beneath recruit target search
provides:
  - explicit recruit target-search level-of-detail policy
  - FULL / REDUCED / SHED tier classification with deterministic per-tier cadence
  - config-driven toggle plus test-friendly pure evaluation contract
affects: [phase-19-validation, performance-evidence, combat-readability]
tech-stack:
  added: []
  patterns: [pure policy record evaluating LOD tier and cadence from immutable per-recruit context]
key-files:
  created:
    - .planning/phases/17-ai-lod-and-tick-shedding/17-ai-lod-and-tick-shedding-01-SUMMARY.md
  modified:
    - src/main/java/com/talhanation/bannermod/ai/military/RecruitAiLodPolicy.java
    - src/main/java/com/talhanation/bannermod/entity/military/RecruitRuntimeLoop.java
key-decisions:
  - "Keep LOD policy pure (Context+Settings → Evaluation) so unit tests can pin tier selection without world state."
  - "Only the target-search cadence is LOD-aware in Plan 17-01; combat execution and ownership safety stay on full fidelity."
patterns-established:
  - "Recruit target search evaluates a LOD tier once per tick and records the tier counter beside earlier profiling seams."
requirements-completed: []
duration: not-recorded
completed: 2026-04-12
---

# Phase 17 Plan 01: AI LOD And Tick Shedding Summary

> Retroactive reconstruction 2026-04-19: written after planning-dir audit surfaced gaps; code state verified against active tree — the LOD policy lives in `src/main/java/com/talhanation/bannermod/ai/military/RecruitAiLodPolicy.java` and is wired from `src/main/java/com/talhanation/bannermod/entity/military/RecruitRuntimeLoop.java` (see `evaluateTargetSearchLod(...)` and `recordLodTier(...)`).

**Recruit target search now flows through an explicit `RecruitAiLodPolicy` seam that classifies each recruit into FULL / REDUCED / SHED tiers with deterministic per-tier cadence, so low-priority actors shed search work under large-battle load without changing combat execution or ownership safety behavior.**

## Performance

- **Duration:** not recorded
- **Started:** not recorded
- **Completed:** 2026-04-12 (retroactive verification; original execution date per ROADMAP: 2026-04-12)
- **Tasks:** 1 (retroactive reconstruction)
- **Files modified:** 2 live (policy + runtime loop); retroactive planning doc added under this phase directory.

## Accomplishments

- Introduced `RecruitAiLodPolicy` as a pure policy: it takes an immutable `Context` (recent-damage, live-target, distance, nearest-player distance, tick count, tick offset) plus `Settings` (enabled, proximity radii, per-tier cadence) and returns an `Evaluation` carrying the LOD tier, chosen search interval, and whether the current tick should run a search.
- Wired the policy from `RecruitRuntimeLoop.evaluateTargetSearchLod(...)` so every recruit resolves its LOD tier once per tick and records the tier through `TARGET_SEARCH_PROFILING.recordLodTier(...)` — tier counters live beside the Phase 11 target-search and Phase 12/13 controller counters in retained dense-battle and mixed-squad profiling snapshots.
- Kept the close-or-recently-engaged path on the default full-fidelity cadence (`DEFAULT_FULL_SEARCH_INTERVAL = 20`) so readability and combat accuracy remain unchanged near the player; only distant or uninvolved recruits downshift to REDUCED or SHED cadence.
- Left the LOD toggle operator-visible via config, and preserved a `Settings.enabled=false` fallback that forces every recruit back to FULL tier for debugging or tuning.

## Task Commits

Not recorded for this slice — the retroactive reconstruction is documentation-only; the live LOD policy and its wiring were already on disk before this audit.

## Files Created/Modified

- `src/main/java/com/talhanation/bannermod/ai/military/RecruitAiLodPolicy.java` — pure LOD policy with `LodTier`, `Settings`, `Context`, and `Evaluation` records and the `evaluate(...)` entrypoint.
- `src/main/java/com/talhanation/bannermod/entity/military/RecruitRuntimeLoop.java` — hook site: resolves the LOD tier per recruit, gates target search cadence, and publishes the tier counter into the existing target-search profiling snapshot.
- `.planning/phases/17-ai-lod-and-tick-shedding/17-ai-lod-and-tick-shedding-01-SUMMARY.md` — this retroactive summary.

## Decisions Made

- LOD decisions stay observable: recruits publish their tier into profiling counters so later tuning can compare skip rate against any target-acquisition or combat-readability regression.
- The policy is pure and world-free, which keeps unit tests deterministic and preserves the ability to exercise it from JUnit without a live server.

## Deviations from Plan

- None in the runtime code. The original Plan 17-01 paperwork was lost before this audit; this retroactive summary restores the written record against the live `RecruitAiLodPolicy` contract.

## Issues Encountered

- The original Phase 17 planning directory contained only `17-CONTEXT.md`; the absent SUMMARY was recovered retroactively on 2026-04-19 after confirming the policy class and hook site are both live.
- `RecruitAiLodPolicy` itself carries an internal javadoc note that the class was reconstructed during Phase 21 Wave 9 source consolidation (original bytecode lived only under `recruits/bin/main/`); this retroactive summary is consistent with that note.

## User Setup Required

None.

## Next Phase Readiness

- Phase 18 optional flow-field evaluation and Phase 19 closing validation can both read LOD tier counters beside controller and async counters from the same profiling snapshot.
- Later LOD expansion (beyond target search) can extend the same policy shape rather than forking a second tick-shedding surface.

---
*Phase: 17-ai-lod-and-tick-shedding*
*Completed: 2026-04-12 (retroactive reconstruction 2026-04-19)*
