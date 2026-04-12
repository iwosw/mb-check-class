---
phase: 30-worker-birth-and-claim-based-settlement-spawn
plan: 02
subsystem: api
tags: [workers, settlement, claims, spawning, runtime]
requires:
  - phase: 30-worker-birth-and-claim-based-settlement-spawn
    provides: claim-aware rule seam and config views
provides:
  - runtime worker spawner for claim settlements
  - villager birth and autonomous settlement spawn event wiring
affects: [30-03, workers]
tech-stack:
  added: []
  patterns: [single runtime spawn seam, claim-derived cooldown map]
key-files:
  created: [workers/src/main/java/com/talhanation/workers/settlement/WorkerSettlementSpawner.java]
  modified: [workers/src/main/java/com/talhanation/workers/VillagerEvents.java, workers/src/main/java/com/talhanation/workers/config/WorkersServerConfig.java]
key-decisions:
  - "Worker creation stays in one WorkerSettlementSpawner seam so ownership and team seeding are not duplicated in events."
  - "Villager team membership, when present, is treated as the settlement-side faction context for hostile denial."
patterns-established:
  - "VillagerEvents delegates birth and autonomous settlement conversion through direct helper entrypoints."
requirements-completed: [WBSP-01, WBSP-02]
duration: 45min
completed: 2026-04-12
---

# Phase 30 Plan 02: Worker Birth And Claim-Based Settlement Spawn Summary

**Friendly-claim villager conversion and autonomous settlement spawning routed through one owned worker spawner seam**

## Performance
- **Duration:** 45 min
- **Started:** 2026-04-12T12:25:00Z
- **Completed:** 2026-04-12T13:10:00Z
- **Tasks:** 2
- **Files modified:** 4

## Accomplishments
- Added `WorkerSettlementSpawner` as the only runtime entity-construction seam for Phase 30 workers.
- Wired villager birth/maturation and autonomous settlement conversion into `VillagerEvents` with claim-aware cooldown handling.
- Seeded worker owner/team defaults from the claim faction leader before the villager replacement path completes.

## Task Commits
1. **Task 1: Add one runtime spawner for settlement-born workers** - `2f7bf10` (feat)
2. **Task 2: Wire villager birth and claim-based settlement spawn into server events** - `e7200ae` (feat), `47e2b54` (fix)

## Files Created/Modified
- `workers/src/main/java/com/talhanation/workers/settlement/WorkerSettlementSpawner.java` - Runtime worker creation, inventory/name carryover, and faction-team seeding.
- `workers/src/main/java/com/talhanation/workers/VillagerEvents.java` - Birth, maturation, and autonomous settlement worker conversion entrypoints.
- `workers/src/main/java/com/talhanation/workers/config/WorkersServerConfig.java` - Cooldown tick helper used by runtime wiring.

## Decisions Made
- Autonomous settlement spawning reuses an existing villager as the conversion source so Phase 30 stays helper-shaped and avoids a new settlement manager.
- Cooldown tracking is claim-scoped and in-memory, keyed by claim UUID.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Restored hostile-claim denial for team-marked villagers**
- **Found during:** Task 2
- **Issue:** Settlement binding always reused the claim owner faction, making hostile villager-team scenarios impossible.
- **Fix:** `VillagerEvents` now resolves settlement binding from the villager team when present, while still falling back to the claim owner for neutral villagers.
- **Files modified:** `workers/src/main/java/com/talhanation/workers/VillagerEvents.java`
- **Verification:** `./gradlew compileJava --console=plain`
- **Committed in:** `47e2b54`

## Issues Encountered
- Existing nested `workers` repository history required separate worker-repo commits for runtime-source changes.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- GameTests can now exercise live birth and autonomous settlement worker conversion through deterministic `VillagerEvents` entrypoints.

## Self-Check: PASSED
