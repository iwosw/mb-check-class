---
phase: 05-persistence-and-multiplayer-sync-hardening
plan: 01
subsystem: testing
tags: [junit5, nbt, saveddata, persistence, forge]
requires:
  - phase: 02-02
    provides: reusable JVM fixtures and round-trip assertion patterns for persistence coverage
provides:
  - JVM regression coverage for team, group, diplomacy, treaty, and player-unit SavedData round trips
  - stable reload behavior for static diplomacy and player-unit maps during load/setup
affects: [phase-05, persistence, saveddata, multiplayer-sync]
tech-stack:
  added: []
  patterns: [fixture-first JVM save-data coverage, explicit load-clears-static-state pattern]
key-files:
  created:
    - src/test/java/com/talhanation/recruits/testsupport/PersistenceRoundTripAssertions.java
    - src/test/java/com/talhanation/recruits/world/RecruitsWorldSaveDataSerializationTest.java
  modified:
    - src/main/java/com/talhanation/recruits/world/RecruitsTeamSaveData.java
    - src/main/java/com/talhanation/recruits/world/RecruitsDiplomacySaveData.java
    - src/main/java/com/talhanation/recruits/world/RecruitPlayerUnitSaveData.java
key-decisions:
  - "Lock Phase 5 persistence behavior through pure JVM round-trip tests instead of runtime-only reload checks."
  - "Move static-map clearing from constructors into load paths so fresh instances do not erase already-loaded server state."
patterns-established:
  - "SavedData round-trip tests should assert observable collection contents, not just non-null reloads."
  - "Static persistence caches must be reset explicitly in load methods, not incidental constructors."
requirements-completed: [DATA-01, DATA-02]
duration: 12min
completed: 2026-04-07
---

# Phase 5 Plan 01: Save Data Round-Trip Summary

**JVM save-data tests now lock teams, groups, diplomacy, treaties, and player-unit counts against restart corruption and constructor-driven state wipes.**

## Performance

- **Duration:** 12 min
- **Started:** 2026-04-07T12:27:31Z
- **Completed:** 2026-04-07T12:39:31Z
- **Tasks:** 2
- **Files modified:** 5

## Accomplishments
- Added reusable JVM round-trip assertions for Phase 5 SavedData coverage.
- Added focused regression tests for high-risk world-state payloads and restart-sensitive setup behavior.
- Fixed missing faction-member persistence and constructor-driven static-map drift in diplomacy and player-unit save data.

## Task Commits

Each task was committed atomically:

1. **Task 1: Write save-data round-trip regression tests** - `b76efbb8` (test)
2. **Task 2: Fix save-data serialization and default-load drift** - `0b573e53` (fix)

**Plan metadata:** pending

_Note: TDD tasks may have multiple commits (test → feat → refactor)_

## Files Created/Modified
- `src/test/java/com/talhanation/recruits/testsupport/PersistenceRoundTripAssertions.java` - shared assertions for SavedData round-trip equality checks.
- `src/test/java/com/talhanation/recruits/world/RecruitsWorldSaveDataSerializationTest.java` - JVM regression coverage for prioritized Phase 5 save payloads.
- `src/main/java/com/talhanation/recruits/world/RecruitsTeamSaveData.java` - persists and restores faction member lists alongside existing team fields.
- `src/main/java/com/talhanation/recruits/world/RecruitsDiplomacySaveData.java` - clears static diplomacy state only during load, not construction.
- `src/main/java/com/talhanation/recruits/world/RecruitPlayerUnitSaveData.java` - clears static recruit counts only during load, not construction.

## Decisions Made
- Kept the new persistence coverage pure JVM so restart-contract failures surface before any Forge runtime GameTest runs.
- Preserved the existing SavedData key structure and made only the narrow serialization/load fixes the tests exposed.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Restored faction member persistence in team save data**
- **Found during:** Task 2 (Fix save-data serialization and default-load drift)
- **Issue:** `RecruitsTeamSaveData` serialized join requests but dropped faction members on save/load.
- **Fix:** Added `Members` list serialization and reload via `RecruitsPlayerInfo` NBT helpers.
- **Files modified:** `src/main/java/com/talhanation/recruits/world/RecruitsTeamSaveData.java`
- **Verification:** `./gradlew test --tests "com.talhanation.recruits.world.RecruitsWorldSaveDataSerializationTest"`
- **Committed in:** `0b573e53` (part of task commit)

**2. [Rule 1 - Bug] Stopped fresh save-data instances from wiping loaded static maps**
- **Found during:** Task 2 (Fix save-data serialization and default-load drift)
- **Issue:** `RecruitsDiplomacySaveData` and `RecruitPlayerUnitSaveData` constructors cleared loaded static state as a side effect.
- **Fix:** Removed constructor clears and made load paths perform the explicit reset before repopulating data.
- **Files modified:** `src/main/java/com/talhanation/recruits/world/RecruitsDiplomacySaveData.java`, `src/main/java/com/talhanation/recruits/world/RecruitPlayerUnitSaveData.java`
- **Verification:** `./gradlew test --tests "com.talhanation.recruits.world.RecruitsWorldSaveDataSerializationTest"`
- **Committed in:** `0b573e53` (part of task commit)

---

**Total deviations:** 2 auto-fixed (2 bug fixes)
**Impact on plan:** Both fixes were directly required to make the new persistence round-trip contract correct. No scope creep.

## Issues Encountered
- The first RED pass was too weak and passed unexpectedly, so the tests were tightened to assert faction member persistence and constructor-side static-state corruption explicitly.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Phase 5 now has a trusted JVM baseline for high-risk save payloads before mutation and sync hardening expands.
- The next persistence plan can focus on dirty-marking and manager mutation flows without re-litigating raw serialization behavior.

## Self-Check: PASSED

---
*Phase: 05-persistence-and-multiplayer-sync-hardening*
*Completed: 2026-04-07*
