---
phase: 03-battle-and-formation-regression-lockdown
plan: 04
subsystem: testing
tags: [forge, gametest, battle, formations, regression]
requires:
  - phase: 03-01
    provides: mixed-squad battle helpers and deterministic recruit spawn support
provides:
  - Dense battle stress fixtures with deterministic squad layouts and deadlines
  - Runtime Forge GameTests that fail on unresolved or broken dense-combat states
affects: [phase-03-verification, combat-regression-coverage, gametest-harness]
tech-stack:
  added: []
  patterns: [fixture-owned stress scenario matrices, bounded GameTest stability alarms]
key-files:
  created:
    - src/gametest/java/com/talhanation/recruits/gametest/battle/BattleStressFixtures.java
    - src/gametest/java/com/talhanation/recruits/gametest/battle/BattleStressGameTests.java
    - src/gametest/resources/data/recruits/structures/battle_density_field.nbt
  modified: []
key-decisions:
  - "Kept battle-density verification on a dedicated copied battlefield template so later stress layouts can diverge without disturbing the baseline harness."
  - "Used resolution deadlines, arena bounds, and stale-target checks as dense-combat alarms instead of exact casualty scripts."
patterns-established:
  - "Stress GameTests should read from fixture scenario records so squad counts, spawn pads, and deadlines stay declarative."
  - "Dense combat regressions should fail on bounded completion and broken-loop signals, not tick-perfect outcomes."
requirements-completed: [BATL-03, BATL-04]
duration: 4min
completed: 2026-04-06
---

# Phase 3 Plan 04: Battle density stress alarms Summary

**Dense battle Forge GameTests with declarative scenario matrices, dedicated battlefield coverage, and bounded stability alarms for battle-density regressions**

## Performance

- **Duration:** 4 min
- **Started:** 2026-04-06T11:54:21Z
- **Completed:** 2026-04-06T11:58:21Z
- **Tasks:** 2
- **Files modified:** 3

## Accomplishments
- Added reusable baseline and heavy dense-battle scenario fixtures with deterministic recruit layouts and deadlines.
- Added a dedicated battle-density battlefield template for stress verification.
- Added runtime GameTests that fail when dense battles do not resolve, leave the arena, or retain dead targets.

## Task Commits

Each task was committed atomically:

1. **Task 1: Define the stress scenario matrix and dedicated battlefield** - `6719173f` (feat)
2. **Task 2: Add runtime stress GameTests with bounded stability assertions** - `0673635b` (feat)

**Plan metadata:** Recorded in the final docs commit for execution state updates.

## Files Created/Modified
- `src/gametest/java/com/talhanation/recruits/gametest/battle/BattleStressFixtures.java` - Encodes dense battle scenarios, squad layouts, arena bounds, and resolve deadlines.
- `src/gametest/java/com/talhanation/recruits/gametest/battle/BattleStressGameTests.java` - Runs baseline and heavier dense-battle GameTests with stability alarms.
- `src/gametest/resources/data/recruits/structures/battle_density_field.nbt` - Dedicated copied battlefield template reserved for battle-density scenarios.

## Decisions Made
- Kept the stress battlefield as a dedicated copy so future density scenarios can evolve separately from the representative battle harness.
- Checked dense-battle regressions through completion bounds, arena containment, and stale-target cleanup instead of brittle exact-outcome assertions.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
- None.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Phase 3 now has deterministic dense-battle stress alarms in the canonical Forge GameTest flow.
- The copied battlefield and fixture matrix leave room for future stress variants without rewriting the earlier mixed-squad harness.

## Self-Check: PASSED

---
*Phase: 03-battle-and-formation-regression-lockdown*
*Completed: 2026-04-06*
