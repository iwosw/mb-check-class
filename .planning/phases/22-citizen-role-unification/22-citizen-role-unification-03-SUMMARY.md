---
phase: 22-citizen-role-unification
plan: 03
subsystem: testing
tags: [citizen, recruits, persistence, gametest]
requires:
  - phase: 22-citizen-role-unification
    provides: citizen role/controller seam and wrapper accessors
provides:
  - live recruit persistence delegation through citizen state
  - recruit-side regression coverage
  - recruit-side root GameTest coverage
affects: [recruits, gametest]
tech-stack:
  added: []
  patterns: [wrapper helper delegation, citizen-backed recruit reload coverage]
key-files:
  created: [src/test/java/com/talhanation/recruits/entities/CitizenRecruitBridgeTest.java, src/gametest/java/com/talhanation/bannermod/BannerModCitizenRecruitGameTests.java]
  modified: [recruits/src/main/java/com/talhanation/recruits/entities/AbstractRecruitEntity.java, src/main/java/com/talhanation/bannermod/citizen/CitizenStateSnapshot.java]
key-decisions:
  - "Move only recruit ownership and follow-state persistence through the citizen bridge in the first live military adoption slice."
  - "Use root GameTests to prove recruit reload behavior without changing recruit runtime identity."
patterns-established:
  - "Wrapper static helpers delegate legacy recruit persistence to `CitizenPersistenceBridge`."
  - "Citizen-backed recruit reloads are validated in both unit and root GameTest layers."
requirements-completed: [CITIZEN-03, CITIZEN-04]
duration: not recorded
completed: 2026-04-13
---

# Phase 22 Plan 03: Citizen Role Unification Summary

**One live recruit persistence path now reloads through the citizen seam while the recruit wrapper and external recruit identity stay intact.**

## Performance

- **Duration:** not recorded
- **Started:** 2026-04-13T00:00:00Z
- **Completed:** 2026-04-13T00:00:00Z
- **Tasks:** 2
- **Files modified:** 4

## Accomplishments
- Routed recruit persistence helpers through `CitizenPersistenceBridge`.
- Added recruit-side regression coverage for citizen persistence delegation.
- Added a root GameTest that reloads a recruit through citizen-backed persistence.

## Task Commits

1. **Task 1: Route one recruit ownership/persistence path through the citizen seam** - `f33f524`, `53fa64d`, `3c21afb5` (test → feat multi-repo)
2. **Task 2: Add recruit-side citizen GameTest coverage on top of the retained root suite** - `5b3d577` (feat)

## Files Created/Modified
- `recruits/src/main/java/com/talhanation/recruits/entities/AbstractRecruitEntity.java` - citizen-backed recruit persistence helpers
- `src/main/java/com/talhanation/bannermod/citizen/CitizenStateSnapshot.java` - inventory restore support for legacy recruit reloads
- `src/test/java/com/talhanation/recruits/entities/CitizenRecruitBridgeTest.java` - recruit persistence regression coverage
- `src/gametest/java/com/talhanation/bannermod/BannerModCitizenRecruitGameTests.java` - recruit citizen root GameTest

## Decisions Made
- Keep recruit AI, combat, and presentation untouched while persistence moves first.
- Validate live recruit adoption through reload behavior instead of widening the slice into combat logic.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Clearing recruit hold position did not clear the live vector cache**
- **Found during:** Task 1
- **Issue:** Citizen-applied state clears could leave `holdPosVec` stale even when the synced holder was emptied.
- **Fix:** Reset `holdPosVec` in `clearHoldPos()` before applying citizen-backed reloads.
- **Files modified:** `recruits/src/main/java/com/talhanation/recruits/entities/AbstractRecruitEntity.java`
- **Verification:** `CitizenRecruitBridgeTest` passes.
- **Committed in:** `3c21afb5`

**2. [Rule 2 - Missing Critical] Citizen inventory restore needed armor and hand slot support**
- **Found during:** Task 1
- **Issue:** Restoring only `Items` would have dropped armor and hand equipment on citizen-backed reloads.
- **Fix:** Extended `CitizenStateSnapshot.restoreInventory()` to repopulate armor and hand slot data from legacy tags.
- **Files modified:** `src/main/java/com/talhanation/bannermod/citizen/CitizenStateSnapshot.java`
- **Verification:** `CitizenRecruitBridgeTest` passes and the recruit GameTest reload slice stays green inside the root suite.
- **Committed in:** `53fa64d`

## Issues Encountered
- `verifyGameTestStage` still has unrelated pre-existing failures logged in `deferred-items.md`.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Worker recovery and work-area binding can now adopt the same citizen persistence seam.

## Self-Check: PASSED

---
*Phase: 22-citizen-role-unification*
*Completed: 2026-04-13*
