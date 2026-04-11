---
phase: 07-dedicated-server-authority-edge-validation
plan: 01
subsystem: testing
tags: [gametest, dedicated-server, authority, workers, recruits, bannermod]
requires:
  - phase: 06-player-cycle-gametest-validation
    provides: dedicated ownership and recovery GameTest slice baselines
provides:
  - dedicated-server fake-player and detached-ownership GameTest helpers
  - offline-owner recruit movement denial coverage on dedicated-server command paths
  - unresolved-owner worker recovery coverage that preserves owner-or-admin authority
affects: [07-02, authority, worker-control, recruit-commands]
tech-stack:
  added: []
  patterns: [dedicated-server GameTests use detached owner UUIDs instead of live owner entities]
key-files:
  created:
    - src/gametest/java/com/talhanation/bannermod/BannerModDedicatedServerGameTestSupport.java
    - src/gametest/java/com/talhanation/bannermod/BannerModDedicatedServerAuthorityGameTests.java
  modified: []
key-decisions:
  - "Create a dedicated-server helper seam now so later reconnect and persistence tests can reuse deterministic fake-player and detached-ownership setup."
  - "Model admin recovery with an explicit permission-granting fake player so offline-owner authority remains server-driven without requiring a live owner entity."
patterns-established:
  - "Dedicated-server authority GameTests should detach ownership by UUID and assert behavior without integrated local-player assumptions."
requirements-completed: [DSAUTH-01]
duration: 4min
completed: 2026-04-11
---

# Phase 07 Plan 01: Dedicated-Server Authority Edge Validation Summary

**Dedicated-server GameTests now prove offline-owned recruits ignore outsider movement commands and unresolved-owner workers remain recoverable only by the owner-or-admin authority contract.**

## Performance

- **Duration:** 4 min
- **Started:** 2026-04-11T10:56:00Z
- **Completed:** 2026-04-11T11:00:05Z
- **Tasks:** 2
- **Files modified:** 2

## Accomplishments
- Added `BannerModDedicatedServerGameTestSupport` with deterministic fake-player, detached-ownership, and save/load helpers for future dedicated-server validation slices.
- Added `BannerModDedicatedServerAuthorityGameTests` covering outsider recruit-command denial when the owner is offline.
- Proved unresolved-owner worker recovery still preserves claimed work areas for outsiders while allowing admin recovery without any production patch.

## Task Commits

Each task was committed atomically:

1. **Task 1: Create the dedicated-server GameTest helper contract** - `6f28445` (feat)
2. **Task 2: Add offline-owner and unresolved-owner dedicated-server authority tests** - `5f7b965` (test)

**Plan metadata:** pending

## Files Created/Modified
- `src/gametest/java/com/talhanation/bannermod/BannerModDedicatedServerGameTestSupport.java` - Dedicated-server helper seam for deterministic fake players, detached ownership, and save/load setup.
- `src/gametest/java/com/talhanation/bannermod/BannerModDedicatedServerAuthorityGameTests.java` - Root GameTests for offline-owner recruit command denial and unresolved-owner worker recovery authority.

## Decisions Made
- Added a dedicated-server-specific helper instead of extending existing root helpers so later persistence/reconnect plans can reuse a stable detached-owner seam.
- Kept runtime code unchanged because the new dedicated-server authority tests passed without exposing a real defect.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

- The helper seam could not call protected entity NBT hooks directly, so it used `saveWithoutId(...)` and `load(...)` to preserve the plan's save/load contract through the public entity API.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Plan 07-02 can reuse the new detached-owner and save/load helper seam for reconnect and persistence-safe ownership recovery coverage.
- No blocker is currently known; the main follow-up risk is keeping later persistence assertions aligned with the same UUID-only authority model.

## Self-Check

PASSED

---
*Phase: 07-dedicated-server-authority-edge-validation*
*Completed: 2026-04-11*
