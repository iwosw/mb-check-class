---
phase: 04-command-and-ai-state-stabilization
plan: 02
subsystem: testing
tags: [commands, gametest, networking, authority, validation]
requires:
  - phase: 04-command-and-ai-state-stabilization
    provides: CommandTargeting shared authority seam
provides:
  - Shared authority filtering for movement, formation, attack, and shields packets
  - JVM codec coverage for attack and shields payloads
  - Runtime GameTests for area command authority filtering and safe no-op behavior
affects: [04-03-PLAN, 04-04-PLAN, command-packets]
tech-stack:
  added: []
  patterns: [server-sender UUID validation, packet dispatch helpers for GameTests, packet-path authority GameTests]
key-files:
  created:
    - src/test/java/com/talhanation/recruits/network/MessageAttackCodecTest.java
    - src/test/java/com/talhanation/recruits/network/MessageShieldsCodecTest.java
    - src/gametest/java/com/talhanation/recruits/gametest/command/CommandAuthorityGameTests.java
    - src/gametest/java/com/talhanation/recruits/gametest/support/RecruitsCommandGameTestSupport.java
  modified:
    - src/main/java/com/talhanation/recruits/network/MessageMovement.java
    - src/main/java/com/talhanation/recruits/network/MessageFormationFollowMovement.java
    - src/main/java/com/talhanation/recruits/network/MessageAttack.java
    - src/main/java/com/talhanation/recruits/network/MessageShields.java
    - src/main/java/com/talhanation/recruits/CommandEvents.java
    - src/test/java/com/talhanation/recruits/testsupport/RecruitsFixtures.java
    - src/test/java/com/talhanation/recruits/testsupport/MessageCodecAssertions.java
key-decisions:
  - "Area command packets now validate against the actual server sender UUID instead of trusting packet payload UUIDs."
  - "Runtime authority coverage drives public packet dispatch helpers so GameTests exercise the same filtering logic as production packet execution."
patterns-established:
  - "Area command packets should resolve nearby owned recruits through CommandTargeting before mutating entity state."
  - "Command authority regressions should mix JVM codec tests with packet-path GameTests."
requirements-completed: [CMD-01, CMD-03, CMD-04]
duration: 85min
completed: 2026-04-07
---

# Phase 4 Plan 2: Area Command Authority Summary

**Server-authoritative movement, formation, attack, and shields packets with shared targeting filters plus runtime GameTests for bystander safety and out-of-radius no-ops.**

## Performance

- **Duration:** 85 min
- **Started:** 2026-04-07T00:20:00Z
- **Completed:** 2026-04-07T07:25:16Z
- **Tasks:** 2
- **Files modified:** 11

## Accomplishments
- Routed movement, formation, attack, and shields packets through the shared command-targeting seam.
- Added attack/shields codec tests and packet-path GameTests for movement, formation, attack, shield, and out-of-radius scenarios.
- Preserved safe no-op behavior for invalid sender or empty selection flows while keeping bystanders unchanged.

## Task Commits

1. **Task 1: Rewire area command packets through shared authority validation** - `6022391e` (test), `b71c4b68` (feat)
2. **Task 2: Add runtime authority regression scenarios for area commands** - `555240e0` (test), `731eef08` (feat)

**Plan metadata:** pending final docs commit

## Files Created/Modified
- `src/main/java/com/talhanation/recruits/network/MessageMovement.java` - Server-sender validation and shared target selection for movement packets.
- `src/main/java/com/talhanation/recruits/network/MessageFormationFollowMovement.java` - Shared target selection for formation packets.
- `src/main/java/com/talhanation/recruits/network/MessageAttack.java` - Shared target selection plus packet-path dispatch helper for attack commands.
- `src/main/java/com/talhanation/recruits/network/MessageShields.java` - Shared target selection plus packet-path dispatch helper for shield commands.
- `src/main/java/com/talhanation/recruits/CommandEvents.java` - Player-compatible shield mutation overload used by packet-path GameTests.
- `src/test/java/com/talhanation/recruits/network/MessageAttackCodecTest.java` - Attack codec coverage and dispatch helper contract.
- `src/test/java/com/talhanation/recruits/network/MessageShieldsCodecTest.java` - Shields codec coverage and dispatch helper contract.
- `src/gametest/java/com/talhanation/recruits/gametest/command/CommandAuthorityGameTests.java` - Runtime authority scenarios for movement, formation, attack, and shields packets.
- `src/gametest/java/com/talhanation/recruits/gametest/support/RecruitsCommandGameTestSupport.java` - Deterministic area-command GameTest fixtures.

## Decisions Made
- Used the actual server sender UUID as the packet authority source so spoofed payload UUIDs degrade to safe no-ops.
- Added player-based attack/shield dispatch overloads so GameTests can exercise packet-path logic even though `GameTestHelper.makeMockPlayer()` is not a `ServerPlayer`.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Added non-`ServerPlayer` dispatch path for attack and shield GameTests**
- **Found during:** Task 2 (Add runtime authority regression scenarios for area commands)
- **Issue:** `GameTestHelper.makeMockPlayer()` returns a mock `Player`, not a `ServerPlayer`, so direct packet-path GameTests for attack/shields could not call the production dispatch helpers.
- **Fix:** Added player-based attack/shield dispatch overloads and a matching `CommandEvents.onShieldsCommand(Player, ...)` overload.
- **Files modified:** `src/main/java/com/talhanation/recruits/network/MessageAttack.java`, `src/main/java/com/talhanation/recruits/network/MessageShields.java`, `src/main/java/com/talhanation/recruits/CommandEvents.java`
- **Verification:** `./gradlew runGameTestServer --continue`
- **Committed in:** `731eef08`

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** The deviation kept runtime coverage on the real packet path without broadening scope.

## Issues Encountered
- `./gradlew runGameTestServer --continue` and `./gradlew check --continue` still report pre-existing out-of-scope failures in `BattleStressGameTests`; those failures were logged to `deferred-items.md` instead of being changed during the command-authority plan.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Explicit-entity leader/scout packets can reuse the same sender-authority and safe no-op validation approach.
- Phase 4 still has unrelated deferred GameTest instability in battle stress coverage outside the area-command packet scope.

## Self-Check: PASSED

- Verified the 04-02 summary exists on disk.
- Verified task commits `6022391e`, `b71c4b68`, `555240e0`, and `731eef08` exist in git history.
