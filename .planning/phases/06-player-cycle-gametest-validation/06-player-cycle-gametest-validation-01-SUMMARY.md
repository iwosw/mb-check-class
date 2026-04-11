---
phase: 06-player-cycle-gametest-validation
plan: 01
subsystem: testing
tags: [gametest, forge, bannermod, ownership, runtime-smoke]
requires:
  - phase: 05-stabilization-and-cleanup
    provides: root BannerMod GameTest baseline and merged runtime seam coverage
provides:
  - shared BannerMod GameTest helper methods for owned worker and work-area setup
  - dedicated ownership-cycle GameTest coverage separated from runtime smoke coverage
  - trimmed runtime smoke class focused on merged runtime seam assertions
affects: [06-02, 06-03, 06-04, gametest-validation]
tech-stack:
  added: []
  patterns: [shared root GameTest helper surface, one slice class per gameplay concern]
key-files:
  created:
    - src/gametest/java/com/talhanation/bannermod/BannerModGameTestSupport.java
    - src/gametest/java/com/talhanation/bannermod/BannerModOwnershipCycleGameTests.java
  modified:
    - src/gametest/java/com/talhanation/bannermod/IntegratedRuntimeGameTests.java
key-decisions:
  - "Keep IntegratedRuntimeGameTests limited to merged runtime seam smoke coverage."
  - "Move ownership assertions into a dedicated BannerModOwnershipCycleGameTests artifact so later Phase 06 slices can grow independently."
patterns-established:
  - "Root GameTest helpers live in BannerModGameTestSupport and expose exact reusable spawn/build contracts."
  - "Player-cycle coverage is split into dedicated GameTest classes instead of expanding one monolithic integrated test file."
requirements-completed: []
duration: 5min
completed: 2026-04-11
---

# Phase 06 Plan 01: Ownership Helper and Slice Split Summary

**Shared BannerMod GameTest helpers plus a dedicated ownership-cycle test now isolate player ownership validation from the merged runtime smoke baseline.**

## Performance

- **Duration:** 5 min
- **Started:** 2026-04-11T09:53:34Z
- **Completed:** 2026-04-11T09:58:12Z
- **Tasks:** 2
- **Files modified:** 3

## Accomplishments
- Added `BannerModGameTestSupport` with reusable owned farmer, crop area, storage area, build area, build-template, and entity spawn helpers.
- Kept `IntegratedRuntimeGameTests` focused on the merged runtime identity and packet seam smoke assertion only.
- Created `BannerModOwnershipCycleGameTests` to prove shared ownership disables hostility, enables crop-area participation, and that divergent ownership restores rejection/hostility.

## Task Commits

Each task was committed atomically:

1. **Task 1: Create the shared BannerMod root GameTest support contract** - `19f8cde` (feat)
2. **Task 2: Split ownership validation into a dedicated root GameTest while retaining runtime smoke** - `1e52a2d` (feat)

**Plan metadata:** pending

## Files Created/Modified
- `src/gametest/java/com/talhanation/bannermod/BannerModGameTestSupport.java` - Shared helper surface for later Phase 06 GameTest slices.
- `src/gametest/java/com/talhanation/bannermod/BannerModOwnershipCycleGameTests.java` - Dedicated ownership-cycle GameTest for shared-owner and divergent-owner behavior.
- `src/gametest/java/com/talhanation/bannermod/IntegratedRuntimeGameTests.java` - Trimmed runtime smoke class that preserves only merged runtime seam validation.

## Decisions Made
- Kept runtime smoke isolated in `IntegratedRuntimeGameTests` so future gameplay-slice plans do not repeatedly edit the same root smoke artifact.
- Established `BannerModGameTestSupport` as the shared helper entrypoint for Phase 06 instead of copying worker/work-area setup code across tests.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Phase 06 now has a stable helper contract for owned worker and work-area setup.
- Later plans can add settlement labor, upkeep, and stitched player-cycle tests without re-expanding the runtime smoke class.

## Self-Check

PASSED

---
*Phase: 06-player-cycle-gametest-validation*
*Completed: 2026-04-11*
