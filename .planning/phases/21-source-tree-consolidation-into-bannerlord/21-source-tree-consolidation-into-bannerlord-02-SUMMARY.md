---
phase: 21-source-tree-consolidation-into-bannerlord
plan: 02
subsystem: infra
tags: [bannerlord, bootstrap, network, registry, forge]
requires:
  - phase: 21-source-tree-consolidation-into-bannerlord
    provides: wave-1 shared and config seams under com.talhanation.bannerlord
provides:
  - canonical bannerlord bootstrap entrypoint and lifecycle composition
  - bannerlord-owned shared channel registration with stable worker packet offset
  - bannerlord-owned registry composition docs for wave 2
affects: [phase-21-wave-3, bootstrap, networking, registry]
tech-stack:
  added: []
  patterns: [bannerlord-owned bootstrap with legacy shims, shared channel registration through canonical network bootstrap]
key-files:
  created:
    - src/main/java/com/talhanation/bannerlord/bootstrap/BannerlordMain.java
    - src/main/java/com/talhanation/bannerlord/bootstrap/BannerlordLifecycleRegistrar.java
    - src/main/java/com/talhanation/bannerlord/network/BannerlordNetworkBootstrap.java
    - src/main/java/com/talhanation/bannerlord/network/military/RecruitsNetworkRegistrar.java
    - src/main/java/com/talhanation/bannerlord/network/civilian/WorkersNetworkRegistrar.java
    - src/main/java/com/talhanation/bannerlord/registry/military/RecruitsRegistryRegistrar.java
    - src/main/java/com/talhanation/bannerlord/registry/civilian/WorkersLifecycleRegistrar.java
  modified:
    - recruits/src/main/java/com/talhanation/recruits/Main.java
    - recruits/src/main/java/com/talhanation/recruits/init/ModLifecycleRegistrar.java
    - recruits/src/main/java/com/talhanation/recruits/network/RecruitsNetworkRegistrar.java
    - workers/src/main/java/com/talhanation/workers/WorkersRuntime.java
    - workers/src/main/java/com/talhanation/workers/WorkersSubsystem.java
    - workers/src/main/java/com/talhanation/workers/init/WorkersLifecycleRegistrar.java
    - workers/src/main/java/com/talhanation/workers/network/WorkersNetworkRegistrar.java
    - .planning/VERIFICATION.md
key-decisions:
  - "Move the only live @Mod entrypoint to com.talhanation.bannerlord.bootstrap while keeping com.talhanation.recruits.Main as a compatibility shim."
  - "Register the shared SimpleChannel through BannerlordNetworkBootstrap and continue deriving the worker packet offset from the recruit packet catalog size."
patterns-established:
  - "Wave-2 package ownership: bootstrap, network, and registry composition now live under com.talhanation.bannerlord with old recruit/worker classes forwarding to them."
  - "Cross-repo migration safety: nested recruit and worker repos can keep thin adapters while the root repo owns the canonical implementation."
requirements-completed: []
duration: 18 min
completed: 2026-04-14
---

# Phase 21 Plan 02: Bootstrap, network, and registry ownership summary

**Bannerlord-owned Forge bootstrap, shared channel registration, and registry composition with recruit/worker compatibility shims preserving the live bannermod runtime contract.**

## Performance

- **Duration:** 18 min
- **Started:** 2026-04-14T13:05:00Z
- **Completed:** 2026-04-14T13:22:42Z
- **Tasks:** 2
- **Files modified:** 18

## Accomplishments

- Moved the only live `@Mod` entrypoint and lifecycle composition into `com.talhanation.bannerlord.bootstrap`.
- Moved shared channel ownership and recruit/worker packet registration catalogs into `com.talhanation.bannerlord.network` while preserving the worker offset boundary.
- Added bannerlord-owned registry composition seams and updated verification docs to describe the new wave-2 ownership truthfully.

## Task Commits

Each task was committed atomically:

1. **Task 1: Re-home the runtime composition root, lifecycle wiring, and packet registration structure** - `f1832af` (root), `e9da4fed` (recruits), `21a18e3` (workers) (`feat`)
2. **Task 2: Keep validation and docs aligned with the moved bootstrap surfaces** - `dc95147`, `a8415ed` (`docs`)

_Note: Task 2 required a follow-up docs commit to narrow the recorded diff back to only the wave-2 verification update after a brownfield file carried unrelated local edits._

## Files Created/Modified

- `src/main/java/com/talhanation/bannerlord/bootstrap/BannerlordMain.java` - canonical merged Forge entrypoint under bannerlord ownership
- `src/main/java/com/talhanation/bannerlord/bootstrap/BannerlordLifecycleRegistrar.java` - canonical lifecycle/config registration seam for the merged runtime
- `src/main/java/com/talhanation/bannerlord/network/BannerlordNetworkBootstrap.java` - one shared channel bootstrap that preserves recruit-first then worker packet ordering
- `src/main/java/com/talhanation/bannerlord/network/military/RecruitsNetworkRegistrar.java` - canonical recruit packet catalog under bannerlord ownership
- `src/main/java/com/talhanation/bannerlord/network/civilian/WorkersNetworkRegistrar.java` - canonical worker packet catalog under bannerlord ownership
- `src/main/java/com/talhanation/bannerlord/registry/military/RecruitsRegistryRegistrar.java` - canonical recruit registry composition seam
- `src/main/java/com/talhanation/bannerlord/registry/civilian/WorkersLifecycleRegistrar.java` - canonical worker lifecycle/registry seam
- `recruits/src/main/java/com/talhanation/recruits/Main.java` - compatibility shim exposing legacy static runtime fields from the bannerlord bootstrap
- `recruits/src/main/java/com/talhanation/recruits/init/ModLifecycleRegistrar.java` - bannerlord lifecycle adapter
- `recruits/src/main/java/com/talhanation/recruits/network/RecruitsNetworkRegistrar.java` - bannerlord military registrar adapter
- `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java` - worker runtime now reads mod identity from bannerlord bootstrap ownership
- `workers/src/main/java/com/talhanation/workers/WorkersSubsystem.java` - worker subsystem now instantiates bannerlord-owned lifecycle/network seams
- `workers/src/main/java/com/talhanation/workers/init/WorkersLifecycleRegistrar.java` - bannerlord civilian lifecycle adapter
- `workers/src/main/java/com/talhanation/workers/network/WorkersNetworkRegistrar.java` - bannerlord civilian registrar adapter
- `src/test/java/com/talhanation/bannermod/BannerModIntegratedRuntimeSmokeTest.java` - smoke coverage now checks the canonical bannerlord bootstrap id
- `src/gametest/java/com/talhanation/bannermod/IntegratedRuntimeGameTests.java` - GameTest smoke coverage now points at the canonical bannerlord bootstrap/network seam
- `recruits/src/test/java/com/talhanation/recruits/network/RecruitsNetworkRegistrarTest.java` - recruit packet order assertions now run against the canonical bannerlord registrar
- `.planning/VERIFICATION.md` - root verification guidance now describes wave-2 package ownership

## Decisions Made

- Used `BannerlordMain` as the only live `@Mod` entrypoint and kept `com.talhanation.recruits.Main` as a thin static shim so the package move does not force immediate import churn across the whole runtime.
- Centralized channel creation in `BannerlordNetworkBootstrap` and continued deriving the worker packet offset from the recruit catalog size so the packet ordering contract stays stable.
- Split registry composition into military and civilian bannerlord seams instead of moving every concrete registry holder in this slice, keeping wave 2 focused on ownership rather than broad feature relocation.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

- `.planning/VERIFICATION.md` already had unrelated local edits in the brownfield workspace. I used a follow-up docs commit to keep the net plan-owned documentation change limited to the wave-2 ownership lines.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Wave 2 is complete: later Phase 21 slices can target `com.talhanation.bannerlord.bootstrap`, `.network`, and `.registry` directly.
- The worker packet offset, one shared `SimpleChannel`, and one live `bannermod` runtime identity remain intact for wave 3 entity/pathfinding moves.

## Known Stubs

None.

## Self-Check: PASSED

- FOUND: `.planning/phases/21-source-tree-consolidation-into-bannerlord/21-source-tree-consolidation-into-bannerlord-02-SUMMARY.md`
- FOUND commits: `f1832af`, `e9da4fed`, `21a18e3`, `dc95147`, `a8415ed`
