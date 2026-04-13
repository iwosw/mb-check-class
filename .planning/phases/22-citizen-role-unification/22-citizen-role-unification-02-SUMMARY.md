---
phase: 22-citizen-role-unification
plan: 02
subsystem: runtime
tags: [citizen, roles, wrappers, delegation]
requires:
  - phase: 22-citizen-role-unification
    provides: citizen core contracts and persistence bridge
provides:
  - citizen role vocabulary
  - role-controller seam for wrappers
  - recruit and worker citizen accessors
affects: [recruits, workers, gametest]
tech-stack:
  added: []
  patterns: [wrapper-owned citizen accessors, role-controller hooks]
key-files:
  created: [src/main/java/com/talhanation/bannermod/citizen/CitizenRole.java, src/main/java/com/talhanation/bannermod/citizen/CitizenRoleController.java, src/main/java/com/talhanation/bannermod/citizen/CitizenRoleContext.java, src/test/java/com/talhanation/bannermod/citizen/CitizenRoleControllerTest.java]
  modified: [recruits/src/main/java/com/talhanation/recruits/entities/AbstractRecruitEntity.java, workers/src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java]
key-decisions:
  - "Expose citizen state through wrapper-owned accessors instead of introducing a new shared entity base class."
  - "Use no-op role controllers by default so wrapper identity stays stable while later plans adopt live hooks narrowly."
patterns-established:
  - "Wrapper classes expose `getCitizenCore`, `getCitizenRole`, and `getCitizenRoleController` accessors."
  - "Worker recovery and work-area binding events flow through the same role-controller seam later live paths can reuse."
requirements-completed: [CITIZEN-01, CITIZEN-02]
duration: not recorded
completed: 2026-04-13
---

# Phase 22 Plan 02: Citizen Role Unification Summary

**Recruit and worker wrappers now publish one shared citizen core plus role-controller metadata without changing their live class identities.**

## Performance

- **Duration:** not recorded
- **Started:** 2026-04-13T00:00:00Z
- **Completed:** 2026-04-13T00:00:00Z
- **Tasks:** 2
- **Files modified:** 6

## Accomplishments
- Added `CitizenRole`, `CitizenRoleController`, and `CitizenRoleContext`.
- Exposed citizen accessors on recruit and worker wrappers.
- Routed worker recovery and remembered work-area binding through the role-controller seam.

## Task Commits

1. **Task 1: Define the citizen role vocabulary and controller contracts** - `9bc49ff` (feat)
2. **Task 2: Add citizen accessors to recruit and worker wrappers without changing their entrypoints** - `8ec1774`, `3311b389`, `837a766` (test → feat multi-repo)

## Files Created/Modified
- `src/main/java/com/talhanation/bannermod/citizen/CitizenRole.java` - shared recruit/worker role enum
- `src/main/java/com/talhanation/bannermod/citizen/CitizenRoleController.java` - narrow role-controller contract
- `src/main/java/com/talhanation/bannermod/citizen/CitizenRoleContext.java` - typed controller context
- `src/test/java/com/talhanation/bannermod/citizen/CitizenRoleControllerTest.java` - wrapper accessor regression coverage
- `recruits/src/main/java/com/talhanation/recruits/entities/AbstractRecruitEntity.java` - recruit citizen accessors
- `workers/src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java` - worker citizen accessors and controller hooks

## Decisions Made
- Keep controller hooks narrow: ready, recovery, and remembered work-area binding.
- Implement wrapper accessors inside existing classes so runtime ids and packet entrypoints stay untouched.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Nested recruit and worker git repos were not declared in init metadata**
- **Found during:** Task 2
- **Issue:** Wrapper changes lived in nested repos, so the standard root-only commit flow could not capture task work atomically.
- **Fix:** Committed wrapper changes in the `recruits` and `workers` repos separately while keeping the root test commit in the main repo.
- **Files modified:** `recruits/src/main/java/com/talhanation/recruits/entities/AbstractRecruitEntity.java`, `workers/src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java`
- **Verification:** `CitizenRoleControllerTest` passes.
- **Committed in:** `3311b389`, `837a766`

## Issues Encountered
- Nested repo layout required multi-repo task commits.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Live recruit and worker persistence paths can now delegate through wrapper-owned citizen accessors.

## Self-Check: PASSED

---
*Phase: 22-citizen-role-unification*
*Completed: 2026-04-13*
