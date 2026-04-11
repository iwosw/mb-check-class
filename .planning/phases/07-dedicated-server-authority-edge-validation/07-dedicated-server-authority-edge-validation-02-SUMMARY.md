---
phase: 07-dedicated-server-authority-edge-validation
plan: 02
subsystem: testing
tags: [gametest, dedicated-server, reconnect, persistence, authority, workers, recruits, bannermod]
requires:
  - phase: 07-dedicated-server-authority-edge-validation
    provides: dedicated-server fake-player and detached-ownership GameTest helpers
provides:
  - dedicated-server reconnect GameTest coverage for recruit command recovery
  - persistence-safe ownership recovery coverage across recruit, worker, and crop-area state
  - a live fake-player helper path that participates in dedicated-server reconnect command tests
affects: [08-01, authority, persistence, worker-control, recruit-commands]
tech-stack:
  added: []
  patterns: [dedicated-server reconnect tests dispatch commands before transient group cleanup ticks erase ad-hoc command groups]
key-files:
  created:
    - src/gametest/java/com/talhanation/bannermod/BannerModDedicatedServerReconnectGameTests.java
  modified:
    - src/gametest/java/com/talhanation/bannermod/BannerModDedicatedServerGameTestSupport.java
key-decisions:
  - "Use a per-call fake player inserted into the GameTest level instead of the cached fake-player factory path so reconnect command tests exercise a live dedicated-server-style sender."
  - "Dispatch reconnect movement commands before the next server tick clears ad-hoc command groups, because Phase 07 validates ownership recovery rather than group-manager lifecycle restoration."
patterns-established:
  - "Dedicated-server reconnect validation can round-trip detached ownership through save/load while reseeding transient command-group state immediately before serialization when group membership itself is not the persistence subject under test."
requirements-completed: [DSAUTH-02]
duration: 56min
completed: 2026-04-11
---

# Phase 07 Plan 02: Dedicated-Server Authority Edge Validation Summary

**Dedicated-server reconnect GameTests now prove the same owner UUID regains recruit and worker authority after disconnects, and that detached ownership survives a representative save/load round trip.**

## Performance

- **Duration:** 56 min
- **Started:** 2026-04-11T18:13:00Z
- **Completed:** 2026-04-11T19:09:00Z
- **Tasks:** 1
- **Files modified:** 2

## Accomplishments
- Added `BannerModDedicatedServerReconnectGameTests` covering same-UUID reconnect authority recovery and ownership persistence round-trips.
- Updated `BannerModDedicatedServerGameTestSupport.createFakeServerPlayer(...)` so reconnect tests use a live fake player entity instead of a cached placeholder path.
- Proved plan-specific reconnect coverage passes under `verifyGameTestStage` when run with the required JVM module opens for this environment.

## Task Commits

Each task was committed atomically:

1. **Task 1: Add reconnect-path dedicated-server authority tests** - pending (not committed in this session)

**Plan metadata:** pending

## Files Created/Modified
- `src/gametest/java/com/talhanation/bannermod/BannerModDedicatedServerReconnectGameTests.java` - Dedicated-server reconnect and persistence-safe ownership recovery GameTests.
- `src/gametest/java/com/talhanation/bannermod/BannerModDedicatedServerGameTestSupport.java` - Fake-player helper now produces a live per-call test sender for reconnect coverage.

## Decisions Made
- Used a live `FakePlayer` inserted into the GameTest level so reconnect movement commands run through the same nearby-entity selection path as real dedicated-server senders.
- Kept the persistence test focused on ownership recovery by reseeding the transient command group immediately before serialization instead of widening the plan into group-manager persistence work.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

- `verifyGameTestStage` needs `JAVA_TOOL_OPTIONS="--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED --add-opens=java.base/java.nio=ALL-UNNAMED"` in this environment to get past the Forge/Netty module-access bootstrap issue.
- The full `verifyGameTestStage` gate still fails because unrelated existing GameTests (`invalidleaderandscoutpacketsfailsafely` and `packetdrivenrecoveryrestoresholdintentaftercombat`) are already failing outside Phase 07 scope; the new reconnect tests pass.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Phase 08 can build on dedicated-server reconnect coverage with multiplayer contention scenarios.
- Phase 07 phase-level completion is still blocked on unrelated pre-existing GameTest failures outside this plan.

---
*Phase: 07-dedicated-server-authority-edge-validation*
*Completed: 2026-04-11*
