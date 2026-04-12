---
phase: 31-1-2-mining-area-branch-mine-3
plan: 02
subsystem: settlement
tags: [claims, workers, gametest, forge]
requires:
  - phase: 31-1-2-mining-area-branch-mine-3
    provides: claim worker growth rules, cooldown config, and profession pool inputs
provides:
  - server-side friendly-claim worker growth pass
  - claim-led worker ownership and team seeding at spawn time
  - root GameTest coverage for claim growth bounds and denial cases
affects: [phase-31-03, phase-31-04, claim-worker-growth]
tech-stack:
  added: []
  patterns: [server-side claim growth tick pass, deterministic profession seeding from claim coordinates]
key-files:
  created:
    - src/gametest/java/com/talhanation/bannermod/BannerModClaimWorkerGrowthGameTests.java
  modified:
    - workers/src/main/java/com/talhanation/workers/VillagerEvents.java
    - workers/src/main/java/com/talhanation/workers/settlement/WorkerSettlementSpawner.java
    - .planning/phases/31-1-2-mining-area-branch-mine-3/deferred-items.md
key-decisions:
  - "Run claim worker growth from one periodic server pass keyed by claim UUID cooldown timestamps."
  - "Expose the real VillagerEvents claim-growth helper to GameTests instead of mocking spawn outcomes."
patterns-established:
  - "Claim growth uses WorkerSettlementSpawnRules for allow/deny and WorkerSettlementSpawner for entity creation."
  - "Claim profession choice stays deterministic by seeding from claim chunk coordinates plus current worker count."
requirements-completed: [CLAIMGROW-02]
duration: 10 min
completed: 2026-04-12
---

# Phase 31 Plan 02: Claim Worker Growth Runtime Summary

**Server-side friendly-claim worker growth now runs through one bounded pass with claim-led ownership seeding and root GameTest coverage.**

## Performance

- **Duration:** 10 min
- **Started:** 2026-04-12T12:46:52Z
- **Completed:** 2026-04-12T12:57:26Z
- **Tasks:** 2
- **Files modified:** 4

## Accomplishments
- Added one periodic claim worker-growth runtime path in `VillagerEvents` that counts current workers and evaluates `WorkerSettlementSpawnRules` before spawning.
- Extended `WorkerSettlementSpawner` so claim growth and villager conversion both reuse the same ownership/team seeding path.
- Added root GameTests for friendly claim inheritance, diminishing cooldown gating, and hostile or unclaimed denial.

## Task Commits

Each task was committed atomically:

1. **Task 1: Add one claim worker-growth runtime path** - `workers@99c3343` (feat)
2. **Task 2: Add root GameTest coverage for bounded claim worker growth** - `a062e5a` (test)

**Plan metadata:** Recorded in the final docs commit after state updates.

## Files Created/Modified
- `workers/src/main/java/com/talhanation/workers/VillagerEvents.java` - runs the server-side claim growth pass, counts existing workers, and exposes deterministic helper entrypoints.
- `workers/src/main/java/com/talhanation/workers/settlement/WorkerSettlementSpawner.java` - reuses one worker creation path for villager conversion and claim growth while seeding claim leader ownership/team defaults.
- `src/gametest/java/com/talhanation/bannermod/BannerModClaimWorkerGrowthGameTests.java` - validates friendly claim inheritance, cooldown gating, and hostile or unclaimed denial.
- `.planning/phases/31-1-2-mining-area-branch-mine-3/deferred-items.md` - records the unrelated pre-existing GameTest failure blocking full-suite verification.

## Decisions Made
- Run claim worker growth from one server tick pass over claim-manager state instead of per-villager autonomous spawning.
- Keep GameTests on the real `VillagerEvents` helper seam so claim status, cooldown tracking, and spawner integration stay executable together.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
- `./gradlew verifyGameTestStage --console=plain` still fails on the pre-existing required GameTest `validleaderpacketsonlymutatetargetedleader`, which is outside this plan's scope. The issue was logged to `.planning/phases/31-1-2-mining-area-branch-mine-3/deferred-items.md` and not auto-fixed.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Live claim worker growth is wired and covered by dedicated root GameTests, so the remaining Phase 31 mining work can depend on a stable claim-growth seam.
- Full `verifyGameTestStage` still has an unrelated pre-existing blocker that should be cleared separately if future plans require a green global GameTest gate.

## Self-Check

PASSED

---
*Phase: 31-1-2-mining-area-branch-mine-3*
*Completed: 2026-04-12*
