---
phase: 08-multiplayer-authority-conflict-validation
plan: 01
subsystem: testing
tags: [gametest, multiplayer, authority, recruits, workers, bannermod]
requires:
  - phase: 07-dedicated-server-authority-edge-validation
    provides: dedicated-server authority and reconnect validation helpers
provides:
  - live multiplayer owner-versus-outsider recruit command denial coverage
  - live multiplayer outsider worker recovery denial coverage
  - build-area mutation coverage through the real server-side authorization seam
affects: [08-02, authority, recruit-commands, worker-control, settlement-authoring]
tech-stack:
  added: []
  patterns: [multiplayer authority GameTests use distinct live fake players instead of detached-owner-only setup]
key-files:
  created:
    - src/gametest/java/com/talhanation/bannermod/BannerModMultiplayerAuthorityConflictGameTests.java
  modified:
    - src/gametest/java/com/talhanation/bannermod/BannerModDedicatedServerGameTestSupport.java
    - workers/src/main/java/com/talhanation/workers/network/MessageUpdateBuildArea.java
key-decisions:
  - "Exercise build-area mutation through MessageUpdateBuildArea's real authorization seam so outsider denial is proven at the server entrypoint, not only at pure helper level."
  - "Keep recruit conflict validation owner-versus-outsider only in this slice and defer same-team command expansion to Plan 08-02."
patterns-established:
  - "Contested multiplayer tests should keep the true owner online in-level so outsider denial is proven against live contention instead of offline-owner assumptions."
requirements-completed: []
duration: pending
completed: 2026-04-11
---

# Phase 08 Plan 01: Multiplayer Authority Conflict Validation Summary

**Root GameTests now prove a live outsider cannot commandeer another player's recruit group, recover another player's worker, or mutate an owned build area while the real owner remains online nearby.**

## Accomplishments

- Added `BannerModMultiplayerAuthorityConflictGameTests` to cover owner-versus-outsider recruit and settlement contention with two distinct live players in one GameTest runtime.
- Extended `MessageUpdateBuildArea` with a reusable server-dispatch helper so build-area mutation denial is exercised through the real runtime entrypoint.
- Added a reusable scoreboard-team helper in `BannerModDedicatedServerGameTestSupport` for the follow-up same-team multiplayer slice.

## Files Created/Modified

- `src/gametest/java/com/talhanation/bannermod/BannerModMultiplayerAuthorityConflictGameTests.java` - Live multiplayer outsider-denial GameTests for recruit commands, worker recovery, and build-area mutation.
- `src/gametest/java/com/talhanation/bannermod/BannerModDedicatedServerGameTestSupport.java` - Shared scoreboard-team helper for multiplayer setup.
- `workers/src/main/java/com/talhanation/workers/network/MessageUpdateBuildArea.java` - Real server-dispatch helper returning authoring decisions for testable build-area mutation authorization.

## Decisions Made

- Kept production behavior unchanged for worker recovery and work-area authoring because existing rules already matched the plan's outsider-denial contract.
- Added only the smallest build-area entrypoint seam needed to assert real server-side denial without fabricating a `NetworkEvent.Context` in GameTests.

## Deviations from Plan

None - plan executed within the intended scope.

## Issues Encountered

- None in-scope beyond the existing repository GameTest baseline.

## Next Phase Readiness

- Plan 08-02 can reuse the new multiplayer team helper and build-area dispatch seam to validate same-team cooperation without widening outsider permissions.

---
*Phase: 08-multiplayer-authority-conflict-validation*
*Completed: 2026-04-11*
