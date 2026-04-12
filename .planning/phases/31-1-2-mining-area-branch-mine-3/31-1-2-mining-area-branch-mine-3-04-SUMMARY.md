---
phase: 31-1-2-mining-area-branch-mine-3
plan: 04
subsystem: workers-mining
tags: [workers, mining, claims, ai, settlement-binding]
requires:
  - phase: 29-1-3-3-2-branch-mining-strip-mining-3-21-26
    provides: segment-based tunnel and branch mining progress
  - phase: 09-settlement-faction-binding-contract
    provides: shared claim-status vocabulary for friendly, hostile, unclaimed, and degraded states
provides:
  - pure claim-safe excavation allow/deny helper for miner break targets
  - per-target mining scan filtering that skips hostile-claim blocks without changing the planner
affects: [workers-mining, workers-ai, settlement-binding]
tech-stack:
  added: []
  patterns: [pure excavation rule seam, per-target claim filtering, segment-progress preservation]
key-files:
  created:
    - .planning/phases/31-1-2-mining-area-branch-mine-3/31-1-2-mining-area-branch-mine-3-04-SUMMARY.md
    - workers/src/main/java/com/talhanation/workers/entities/ai/MiningClaimExcavationRules.java
    - src/test/java/com/talhanation/workers/MiningClaimExcavationRulesTest.java
  modified:
    - workers/src/main/java/com/talhanation/workers/entities/workarea/MiningArea.java
key-decisions:
  - "Keep hostile-claim excavation checks in MiningArea scan code so MiningPatternPlanner stays pure."
  - "Resolve claim status per target block with BannerModSettlementBinding.resolveFactionStatus instead of once at the work-area origin."
patterns-established:
  - "Claim Safety Pattern: world-aware claim checks live in scan/execution seams, while pure helpers only convert status to allow/deny."
  - "Progress Pattern: empty hostile-only pattern segments still rely on the existing CHECK -> advancePatternSegment path."
requirements-completed: [MINERSAFE-01]
duration: 3 min
completed: 2026-04-12
---

# Phase 31 Plan 04: Claim-safe miner excavation summary

**Miners now skip hostile-claim excavation targets with a pure status rule while keeping friendly and unclaimed mining on the existing segment-progress flow.**

## Performance

- **Duration:** 3 min
- **Started:** 2026-04-12T12:48:23Z
- **Completed:** 2026-04-12T12:51:55Z
- **Tasks:** 2
- **Files modified:** 3

## Accomplishments
- Added a pure `MiningClaimExcavationRules` helper that allows friendly and unclaimed targets while denying hostile and degraded claim states.
- Added focused RED/GREEN regression coverage for the claim excavation rule.
- Filtered mining-area break-target scans through per-target claim resolution so hostile-only segments empty out and continue through existing segment advancement.

## Task Commits

Each task was committed atomically:

1. **Task 1: Create the pure hostile-claim excavation rule** - root `3a93a2d` (test), workers `3b57775` (feat)
2. **Task 2: Filter miner targets through claim safety and preserve segment progress** - workers `422ea16` (feat)

**Plan metadata:** created after state and roadmap updates

_Note: Task 1 used TDD RED → GREEN across root test coverage and the nested `workers` implementation repo._

## Files Created/Modified
- `workers/src/main/java/com/talhanation/workers/entities/ai/MiningClaimExcavationRules.java` - Pure allow/deny helper for claim-aware excavation targets.
- `workers/src/main/java/com/talhanation/workers/entities/workarea/MiningArea.java` - Resolves claim status per target block before pushing break targets.
- `src/test/java/com/talhanation/workers/MiningClaimExcavationRulesTest.java` - Verifies friendly/unclaimed allow and hostile/degraded deny behavior.

## Decisions Made
- Keep the planner pure by resolving live claim state inside `MiningArea` scanning instead of in `MiningPatternPlanner`.
- Use the mining area's faction/team identity against each candidate target block so miners can still mine outside claims while skipping foreign-claim terrain.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Hostile claim blocks are filtered before miners add break targets, so follow-up runtime validation can focus on broader gameplay behavior instead of repeated foreign-claim targeting.
- Phase 31 now has 3 of 4 plans complete and is ready for the remaining claim-worker growth/runtime closure.

## Self-Check: PASSED

- FOUND: `.planning/phases/31-1-2-mining-area-branch-mine-3/31-1-2-mining-area-branch-mine-3-04-SUMMARY.md`
- FOUND: `3a93a2d`
- FOUND: `3b57775`
- FOUND: `422ea16`
