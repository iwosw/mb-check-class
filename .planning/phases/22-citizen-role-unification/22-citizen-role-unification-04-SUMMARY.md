---
phase: 22-citizen-role-unification
plan: 04
subsystem: testing
tags: [citizen, workers, persistence, gametest]
requires:
  - phase: 22-citizen-role-unification
    provides: citizen role/controller seam and wrapper accessors
provides:
  - live worker persistence delegation through citizen state
  - worker-side regression coverage
  - worker-side root GameTest coverage
affects: [workers, gametest]
tech-stack:
  added: []
  patterns: [citizen-backed worker binding reload, worker recovery through citizen state]
key-files:
  created: [src/test/java/com/talhanation/workers/CitizenWorkerBridgeTest.java, src/gametest/java/com/talhanation/bannermod/BannerModCitizenWorkerGameTests.java]
  modified: [workers/src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java, workers/src/main/java/com/talhanation/workers/entities/WorkerBindingResume.java]
key-decisions:
  - "Move worker recovery and bound work-area persistence through the citizen seam before touching broader labor AI."
  - "Treat unrelated pre-existing root GameTest failures as deferred because they are outside Phase 22 worker scope."
patterns-established:
  - "Worker persistence helpers delegate legacy save keys to `CitizenPersistenceBridge`."
  - "Worker bound-area resume utilities can read from shared citizen-bound state."
requirements-completed: [CITIZEN-03, CITIZEN-04]
duration: not recorded
completed: 2026-04-13
---

# Phase 22 Plan 04: Citizen Role Unification Summary

**One live worker recovery and binding path now reloads through the citizen seam while the worker wrapper and current entrypoints remain unchanged.**

## Performance

- **Duration:** not recorded
- **Started:** 2026-04-13T00:00:00Z
- **Completed:** 2026-04-13T00:00:00Z
- **Tasks:** 2
- **Files modified:** 4

## Accomplishments
- Routed worker persistence helpers through `CitizenPersistenceBridge`.
- Added worker-side regression coverage for citizen-bound recovery and binding.
- Added a root GameTest that reloads a worker through citizen-backed persistence and recovery.

## Task Commits

1. **Task 1: Route one worker recovery/binding path through the citizen seam** - `e69315f`, `16f72b7` (test → feat multi-repo)
2. **Task 2: Add worker-side citizen GameTest coverage and revalidate the root suite** - `27af292` (feat)

## Files Created/Modified
- `workers/src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java` - citizen-backed worker persistence helpers
- `workers/src/main/java/com/talhanation/workers/entities/WorkerBindingResume.java` - citizen-aware binding resume helper
- `src/test/java/com/talhanation/workers/CitizenWorkerBridgeTest.java` - worker persistence regression coverage
- `src/gametest/java/com/talhanation/bannermod/BannerModCitizenWorkerGameTests.java` - worker citizen root GameTest

## Decisions Made
- Keep worker profession logic untouched while moving recovery and binding persistence first.
- Accept the retained root GameTest suite as the final gate even though one unrelated pre-existing required failure remains outside this slice.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Worker save could miss the current bound work-area if persistence ran before the next tick**
- **Found during:** Task 2
- **Issue:** The worker GameTest exposed that bound work-area state was not always remembered before save.
- **Fix:** Forced `addAdditionalSaveData()` to refresh the remembered work-area binding before persisting citizen-backed worker state.
- **Files modified:** `workers/src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java`
- **Verification:** `verifyGameTestStage` no longer reports `workerrecoveryandbindingsurvivecitizenbackedpersistence` as a failing required test.
- **Committed in:** `16f72b7`

## Issues Encountered
- `verifyGameTestStage` still reports the unrelated required failure `hostileorunclaimedsettlementsnevercreateworkers`; logged to `deferred-items.md` instead of widening Phase 22 scope.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Phase 22 is closed with both recruit and worker live paths proven against the citizen seam.

## Self-Check: PASSED

---
*Phase: 22-citizen-role-unification*
*Completed: 2026-04-13*
