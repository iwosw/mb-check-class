---
phase: 04-command-and-ai-state-stabilization
plan: 03
subsystem: testing
tags: [commands, leader, scout, gametest, validation]
requires:
  - phase: 04-command-and-ai-state-stabilization
    provides: shared single-target authority seam for explicit recruit selection
provides:
  - Validated patrol leader and scout packet paths
  - JVM and GameTest coverage for explicit entity command authority and payload checks
affects: [04-04-PLAN, leader-packets, scout-packets]
tech-stack:
  added: []
  patterns: [single-target authority validation, packet-local payload sanity checks, explicit-entity command GameTests]
key-files:
  created:
    - src/test/java/com/talhanation/recruits/network/LeaderScoutCommandValidationTest.java
    - src/gametest/java/com/talhanation/recruits/gametest/command/LeaderScoutCommandGameTests.java
    - src/gametest/java/com/talhanation/recruits/gametest/support/RecruitsLeaderScoutGameTestSupport.java
  modified:
    - src/main/java/com/talhanation/recruits/network/MessagePatrolLeaderSetPatrolState.java
    - src/main/java/com/talhanation/recruits/network/MessagePatrolLeaderSetRoute.java
    - src/main/java/com/talhanation/recruits/network/MessageScoutTask.java
key-decisions:
  - "Leader and scout packets should validate the addressed entity through CommandTargeting.forSingleRecruit before mutating any state."
  - "Route assignment treats mismatched waypoint and wait payloads as invalid and degrades to a safe no-op."
patterns-established:
  - "Explicit entity commands should expose packet-path dispatch helpers so GameTests can exercise the same authority rules as production packets."
requirements-completed: [CMD-01, CMD-03, CMD-04]
duration: 31min
completed: 2026-04-07
---

# Phase 4 Plan 3: Leader and Scout Command Summary

**Server-authoritative patrol leader and scout packet validation with JVM checks for malformed payloads and runtime proof that only the targeted owned entity mutates.**

## Performance

- **Duration:** 31 min
- **Started:** 2026-04-07T07:05:00Z
- **Completed:** 2026-04-07T07:36:00Z
- **Tasks:** 2
- **Files modified:** 6

## Accomplishments
- Routed patrol-state, patrol-route, and scout-task packets through the shared single-target authority seam.
- Added JVM validation coverage for invalid targets, invalid state indices, and malformed route payloads.
- Added runtime GameTests proving valid leader and scout packets mutate only the addressed nearby owned entity.

## Task Commits

1. **Task 1: Validate leader and scout packets before mutating state** - `b08bee19` (test), `9db13b54` (feat)
2. **Task 2: Add runtime patrol/scout command regression scenarios** - included in `b08bee19` with packet-path GameTests

**Plan metadata:** pending final docs commit

## Files Created/Modified
- `src/main/java/com/talhanation/recruits/network/MessagePatrolLeaderSetPatrolState.java` - Validates patrol leader authority and state index before mutation.
- `src/main/java/com/talhanation/recruits/network/MessagePatrolLeaderSetRoute.java` - Validates authority plus route payload structure before loading waypoints.
- `src/main/java/com/talhanation/recruits/network/MessageScoutTask.java` - Validates authority and scout task state before starting the mission.
- `src/test/java/com/talhanation/recruits/network/LeaderScoutCommandValidationTest.java` - JVM regression coverage for invalid target and malformed payload handling.
- `src/gametest/java/com/talhanation/recruits/gametest/command/LeaderScoutCommandGameTests.java` - Runtime leader/scout packet-path scenarios.

## Decisions Made
- Reused `CommandTargeting.forSingleRecruit` so patrol leader and scout packets obey the same ownership and radius contract as area-command packets.
- Treated mismatched route waypoint and wait-time lists as invalid command payloads instead of partially loading route state.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
- Phase verification still reports out-of-scope failures in older battle GameTests; these were logged to `deferred-items.md` and not changed during explicit-entity command work.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Command-to-AI transition stabilization can build on validated explicit-command packet paths without revisiting leader/scout authority rules.

## Self-Check: PASSED

- Verified summary file creation for 04-03.
- Verified commits `b08bee19` and `9db13b54` exist in git history.
