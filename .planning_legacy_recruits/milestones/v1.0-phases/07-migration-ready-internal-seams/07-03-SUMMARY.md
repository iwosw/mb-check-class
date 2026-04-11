---
phase: 07-migration-ready-internal-seams
plan: 03
subsystem: infra
tags: [migration, client-state, persistence, saveddata, jvm-tests]
requires:
  - phase: 05-persistence-and-multiplayer-sync-hardening
    provides: Phase 5 client-reset and persistence behavior contracts that must remain stable
  - phase: 07-migration-ready-internal-seams
    provides: StatePersistenceSeams contracts and seam inventory guidance
provides:
  - Explicit client sync seam for reset and active siege derivation
  - Explicit faction SavedData facade for load/save orchestration
affects: [07-04-PLAN, migration-prep, persistence, client-sync]
tech-stack:
  added: []
  patterns: [helper-owned client cache derivation, callback-based save orchestration, JVM seam regression tests]
key-files:
  created:
    - src/test/java/com/talhanation/recruits/client/ClientSyncStateTest.java
    - src/test/java/com/talhanation/recruits/world/RecruitsSavedDataFacadeTest.java
    - src/main/java/com/talhanation/recruits/client/ClientSyncState.java
    - src/main/java/com/talhanation/recruits/world/RecruitsSavedDataFacade.java
  modified:
    - src/main/java/com/talhanation/recruits/client/ClientManager.java
    - src/main/java/com/talhanation/recruits/world/RecruitsFactionManager.java
key-decisions:
  - "Keep the client sync seam focused on route-preserving reset plus siege derivation instead of moving broader GUI cache logic."
  - "Model faction persistence orchestration as callback-driven apply/dirty/broadcast steps so the seam stays JVM-testable."
patterns-established:
  - "Client synchronized state should be rebuilt from helper-returned data instead of ad hoc map mutation in ClientManager."
  - "Manager SavedData save/load logic should delegate through narrow facades that own ordering of apply, dirty, and broadcast work."
requirements-completed: [MIG-01, MIG-02]
duration: 7min
completed: 2026-04-08
---

# Phase 7 Plan 3: Client sync and SavedData seam Summary

**Route-preserving client sync reset and faction SavedData orchestration now flow through focused helpers with JVM regression coverage.**

## Performance

- **Duration:** 7 min
- **Started:** 2026-04-08T01:15:30Z
- **Completed:** 2026-04-08T01:22:24Z
- **Tasks:** 2
- **Files modified:** 6

## Accomplishments
- Added JVM tests that capture the client reset, active siege, and faction save/load orchestration contracts.
- Added `ClientSyncState` and `RecruitsSavedDataFacade` as narrow migration-ready seam helpers.
- Rewired `ClientManager` and `RecruitsFactionManager` to delegate through the new helpers without changing Phase 5 behavior.

## Task Commits

Each task was committed atomically:

1. **Task 1: Lock down client-sync and persistence seam behavior in JVM tests** - `9ffa4ab6`, `b6cca9d4` (test, feat)
2. **Task 2: Rewire ClientManager and faction persistence through the new seams** - `cf7b92ea` (feat)

**Plan metadata:** pending final docs commit

## Files Created/Modified
- `src/test/java/com/talhanation/recruits/client/ClientSyncStateTest.java` - Tests route-preserving resets and siege derivation behavior.
- `src/test/java/com/talhanation/recruits/world/RecruitsSavedDataFacadeTest.java` - Tests callback ordering for team save/load orchestration.
- `src/main/java/com/talhanation/recruits/client/ClientSyncState.java` - Helper for reset and active-siege derivation seams.
- `src/main/java/com/talhanation/recruits/world/RecruitsSavedDataFacade.java` - Helper for manager-side team save/load sequencing.
- `src/main/java/com/talhanation/recruits/client/ClientManager.java` - Delegates synchronized reset and siege updates to the seam helper.
- `src/main/java/com/talhanation/recruits/world/RecruitsFactionManager.java` - Delegates team load/save orchestration to the facade.

## Decisions Made
- Preserved `routesMap` by keeping it outside synchronized remote cache clearing and threading it through the new reset helper.
- Kept the persistence seam narrow by passing existing `RecruitsTeamSaveData` operations and broadcasts as callbacks rather than expanding into a broader manager rewrite.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
- None.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- `07-04` can now apply the same seam-extraction style to compat reflection and async path runtime behavior.
- Phase 7 now has dedicated seams across bootstrap, client-state, and persistence before the final compat/path extraction.

## Self-Check: PASSED

- Verified `ClientSyncState`, `RecruitsSavedDataFacade`, and `07-03-SUMMARY.md` exist on disk.
- Verified task commits `9ffa4ab6`, `b6cca9d4`, and `cf7b92ea` exist in git history.

---
*Phase: 07-migration-ready-internal-seams*
*Completed: 2026-04-08*
