---
phase: 12-global-pathfinding-control
plan: 01
subsystem: runtime
tags: [runtime, performance, pathfinding, ai, profiling, junit, bannermod]
requires:
  - phase: 11-large-battle-ai-profiling-baseline
    provides: baseline profiling counters and stress-battle evidence capture
provides:
  - shared global controller seam for recruit path issuance
  - controller-side request counters for block and entity path requests
  - routed async recruit navigation through one pass-through controller API
affects: [phase-12-validation, pathfinding-budgeting, path-reuse, async-pathfinding]
tech-stack:
  added: []
  patterns: [pass-through global path request controller with resettable profiling snapshot]
key-files:
  created:
    - .planning/phases/12-global-pathfinding-control/12-global-pathfinding-control-01-SUMMARY.md
    - recruits/src/main/java/com/talhanation/recruits/pathfinding/GlobalPathfindingController.java
    - recruits/src/test/java/com/talhanation/recruits/pathfinding/GlobalPathfindingControllerTest.java
  modified:
    - recruits/src/main/java/com/talhanation/recruits/pathfinding/AsyncPathNavigation.java
    - recruits/src/main/java/com/talhanation/recruits/entities/ai/navigation/RecruitPathNavigation.java
key-decisions:
  - "Keep the new controller strictly pass-through in Phase 12 so later optimization phases can extend one stable seam without changing current navigation behavior."
  - "Track both request kind and async-enabled state inside the controller snapshot so later profiling and budgeting work can compare caller shape without rediscovering path hooks."
patterns-established:
  - "Shared recruit path issuance now enters through GlobalPathfindingController before pathfinder work begins."
requirements-completed: []
duration: not-recorded
completed: 2026-04-11
---

# Phase 12 Plan 01: Global Pathfinding Control Summary

**Recruit path creation now flows through one pass-through global controller that records request shape and async state without changing existing small-battle behavior.**

## Performance

- **Duration:** not recorded
- **Started:** not recorded
- **Completed:** 2026-04-11T15:24:22Z
- **Tasks:** 2
- **Files modified:** 5

## Accomplishments

- Added `GlobalPathfindingController` as the shared runtime seam for recruit path issuance.
- Added narrow JUnit coverage proving the controller returns the supplier result unchanged and resets its counters cleanly.
- Routed block-target and entity-target path creation in `AsyncPathNavigation` through the new controller while preserving the existing path-building logic.

## Task Commits

Not created — execution was requested without git commits.

## Files Created/Modified

- `recruits/src/main/java/com/talhanation/recruits/pathfinding/GlobalPathfindingController.java` - Pass-through path-request controller and profiling snapshot.
- `recruits/src/test/java/com/talhanation/recruits/pathfinding/GlobalPathfindingControllerTest.java` - Regression coverage for pass-through behavior and reset semantics.
- `recruits/src/main/java/com/talhanation/recruits/pathfinding/AsyncPathNavigation.java` - Routes block and entity path requests through the shared controller.
- `recruits/src/main/java/com/talhanation/recruits/entities/ai/navigation/RecruitPathNavigation.java` - Keeps recruit-specific target setup on the shared navigation path.

## Decisions Made

- Kept the controller static and utility-shaped to match the existing async pathfinding seams.
- Counted target positions as part of the snapshot so later queueing or budgeting work can measure request volume without changing callers again.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

- Running the plan-01 unit test inside the standalone `recruits/` subdirectory failed because that sub-build cannot resolve merged `bannermod` and `workers` classes; the repository-root Gradle command succeeded and is the truthful verification path for this workspace.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Phase 12-02 can now validate the controller activity inside the existing mixed-squad and dense-battle GameTests.
- Later pathfinding-budgeting and reuse phases can extend one shared request seam instead of patching per-entity callers.

---
*Phase: 12-global-pathfinding-control*
*Completed: 2026-04-11*
