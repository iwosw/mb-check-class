---
phase: 03-battle-and-formation-regression-lockdown
plan: 03
subsystem: testing
tags: [forge-gametest, battle, formations, command-path, networking]
requires:
  - phase: 03-01
    provides: reusable mixed-squad battle fixtures and dedicated battle/recovery templates
provides:
  - deterministic mixed-squad battle GameTests
  - packet-driven formation recovery GameTests
  - player-safe command and message dispatch seams usable by runtime tests
affects: [phase-03, phase-04, gametest, commands]
tech-stack:
  added: [battle and recovery runtime regression classes]
  patterns: [message-dispatch-backed GameTests, player-safe formation APIs, outcome-level battle assertions]
key-files:
  created:
    - src/gametest/java/com/talhanation/recruits/gametest/battle/MixedSquadBattleGameTests.java
    - src/gametest/java/com/talhanation/recruits/gametest/battle/FormationRecoveryGameTests.java
  modified:
    - src/main/java/com/talhanation/recruits/CommandEvents.java
    - src/main/java/com/talhanation/recruits/network/MessageMovement.java
    - src/main/java/com/talhanation/recruits/network/MessageFormationFollowMovement.java
    - src/main/java/com/talhanation/recruits/util/FormationUtils.java
key-decisions:
  - "Generalize formation command helpers from ServerPlayer to Player so GameTests can drive the real packet-to-command path with mock players."
  - "Use message dispatch helper methods in GameTests instead of fabricating NetworkEvent contexts, keeping runtime coverage close to the production path."
patterns-established:
  - "Runtime command-path tests can call packet-class dispatch helpers while still validating the same recruit filtering and CommandEvents flow as production."
  - "Battle GameTests should assert bounded resolution and surviving ownership rather than exact casualty choreography."
requirements-completed: [BATL-01, BATL-02, BATL-03]
duration: 19min
completed: 2026-04-06
---

# Phase 3 Plan 03: Battle and Formation Recovery Summary

**Deterministic mixed-squad battle GameTests and packet-driven formation recovery checks now lock down the main Phase 3 combat and command-path contracts.**

## Performance

- **Duration:** 19 min
- **Started:** 2026-04-06T01:40:00Z
- **Completed:** 2026-04-06T01:59:32Z
- **Tasks:** 2
- **Files modified:** 6

## Accomplishments
- Added mixed-squad runtime battle coverage that asserts bounded resolution and surviving owned recruits.
- Added formation recovery GameTests that drive the existing message-to-command path with mock players.
- Made formation command helpers and packet dispatch code Player-safe so GameTests can exercise the production command flow directly.

## Task Commits

Each task was committed atomically:

1. **Task 1: Add deterministic mixed-squad ground battle GameTests** - `38868c73` (test)
2. **Task 2: Drive formation recovery through the packet-to-command path and fix exposed logic faults** - `885be52b` (feat)

**Plan metadata:** pending

## Files Created/Modified
- `src/gametest/java/com/talhanation/recruits/gametest/battle/MixedSquadBattleGameTests.java` - deterministic mixed-squad battle regression coverage
- `src/gametest/java/com/talhanation/recruits/gametest/battle/FormationRecoveryGameTests.java` - packet-driven formation application and recovery checks
- `src/main/java/com/talhanation/recruits/network/MessageMovement.java` - reusable dispatch helper for movement-command runtime tests
- `src/main/java/com/talhanation/recruits/network/MessageFormationFollowMovement.java` - reusable dispatch helper for formation-command runtime tests
- `src/main/java/com/talhanation/recruits/CommandEvents.java` - player-safe command signatures for production and GameTest use
- `src/main/java/com/talhanation/recruits/util/FormationUtils.java` - player-safe formation entrypoints reused by command-path tests

## Decisions Made
- Kept packet-path coverage close to production by introducing tiny dispatch helpers on the message classes instead of mocking `NetworkEvent.Context`.
- Generalized command and formation entrypoints to `Player` because the logic only depends on shared player APIs, not `ServerPlayer`-specific behavior.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

- GameTest mock players are not `ServerPlayer` instances, so the command-path seam had to be widened to the shared `Player` API before the runtime tests could drive it.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Phase 3 now has direct runtime coverage for representative battles, formation commands, and post-combat recovery behavior.
- The remaining stress plan can reuse the same battle helper layer and runtime verification flow for denser scenarios.

## Self-Check: PASSED

---
*Phase: 03-battle-and-formation-regression-lockdown*
*Completed: 2026-04-06*
