---
phase: 05-stabilization-and-cleanup
plan: 01
subsystem: testing
tags: [gradle, junit5, workers, bannermod, verification]
requires:
  - phase: 02-runtime-unification-design
    provides: BannerMod compatibility boundary and root verification baseline
provides:
  - Root test source-set wiring for retained Workers JUnit suites
  - Active root regression coverage ownership for preserved Workers tests
affects: [phase-05, verification, gradle, workers-tests]
tech-stack:
  added: []
  patterns: [single root gradle test entrypoint, preserved workers suites routed through root source sets]
key-files:
  created: []
  modified:
    - build.gradle
key-decisions:
  - "Retained Workers JUnit suites run through the root test source set instead of a separate Workers-only entrypoint."
patterns-established:
  - "Merged regression coverage must stay reachable from repository-root ./gradlew test."
requirements-completed: [STAB-01]
duration: 8min
completed: 2026-04-11
---

# Phase 5 Plan 1: Stabilization and Cleanup Summary

**Root Gradle test wiring now includes retained Workers JUnit suites so merged regression coverage runs from the single bannermod workspace entrypoint**

## Performance

- **Duration:** 8 min
- **Started:** 2026-04-11T05:33:30Z
- **Completed:** 2026-04-11T05:41:59Z
- **Tasks:** 1
- **Files modified:** 1

## Accomplishments
- Added `workers/src/test/java` to the root `test` source set in `build.gradle`.
- Kept the merged root `main` and `gametest` source-set wiring unchanged.
- Verified the root `test` task executes both retained Workers network and entity JUnit suites successfully.

## Task Commits

Each task was committed atomically:

1. **Task 1: Expand the root test source set to include retained Workers suites** - `6de12a8` (chore)

**Plan metadata:** `ec253b5` (docs)

## Files Created/Modified
- `build.gradle` - Extends the root `test` source set to compile retained Workers JUnit classes from the merged workspace.

## Decisions Made
- Kept one root `test` entrypoint and routed preserved Workers regression coverage through it, matching the stabilization requirement to avoid hidden standalone verification paths.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- The root workspace now owns retained Workers JUnit coverage through `./gradlew test`.
- Phase 05 can continue with stabilization slices that assume preserved Workers regression suites are part of the active root baseline.

## Self-Check: PASSED

---
*Phase: 05-stabilization-and-cleanup*
*Completed: 2026-04-11*
