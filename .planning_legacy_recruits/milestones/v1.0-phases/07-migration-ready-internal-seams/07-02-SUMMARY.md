---
phase: 07-migration-ready-internal-seams
plan: 02
subsystem: infra
tags: [migration, bootstrap, networking, packet-order, lifecycle]
requires:
  - phase: 07-migration-ready-internal-seams
    provides: seam inventory and NetworkBootstrapSeams contract types
provides:
  - Ordered packet-registration seam outside Main
  - Lifecycle/deferred-register helper that thins Main bootstrap wiring
affects: [07-03-PLAN, 07-04-PLAN, bootstrap, networking]
tech-stack:
  added: []
  patterns: [wire-contract regression tests, helper-owned bootstrap registration, thinner Main orchestration shell]
key-files:
  created:
    - src/test/java/com/talhanation/recruits/network/RecruitsNetworkRegistrarTest.java
    - src/main/java/com/talhanation/recruits/network/RecruitsNetworkRegistrar.java
    - src/main/java/com/talhanation/recruits/init/ModLifecycleRegistrar.java
  modified:
    - src/main/java/com/talhanation/recruits/Main.java
key-decisions:
  - "Capture the current packet wire order with targeted sentinel tests before moving registration out of Main."
  - "Delegate common lifecycle and runtime listener wiring through ModLifecycleRegistrar while leaving gameplay behavior unchanged."
patterns-established:
  - "Network registration seams should expose orderedMessageTypes plus registerAll so packet ids stay reviewable and testable."
  - "Main should orchestrate version-sensitive bootstrap helpers rather than owning long inline Forge registration blocks."
requirements-completed: [MIG-02]
duration: 6min
completed: 2026-04-08
---

# Phase 7 Plan 2: Networking and lifecycle bootstrap seam Summary

**A dedicated packet registrar and lifecycle binding helper that preserve wire order while turning Main into a thinner bootstrap shell.**

## Performance

- **Duration:** 6 min
- **Started:** 2026-04-08T01:11:40Z
- **Completed:** 2026-04-08T01:17:32Z
- **Tasks:** 2
- **Files modified:** 4

## Accomplishments
- Added JVM regression coverage that locks packet ids, order, and sentinel positions to the existing wire contract.
- Moved packet registration into `RecruitsNetworkRegistrar` with explicit ordered seam data.
- Moved common/bootstrap listener wiring into `ModLifecycleRegistrar`, leaving `Main` as a smaller orchestration entrypoint.

## Task Commits

Each task was committed atomically:

1. **Task 1: Lock down packet registration order behind a dedicated registrar** - `74ca9e50`, `174807cb` (test, feat)
2. **Task 2: Delegate Main bootstrap wiring to lifecycle helpers** - `7a8d1226` (feat)

**Plan metadata:** pending final docs commit

## Files Created/Modified
- `src/test/java/com/talhanation/recruits/network/RecruitsNetworkRegistrarTest.java` - Captures stable packet ids, order, uniqueness, and sentinel positions.
- `src/main/java/com/talhanation/recruits/network/RecruitsNetworkRegistrar.java` - Owns the ordered message catalog and channel registration loop.
- `src/main/java/com/talhanation/recruits/init/ModLifecycleRegistrar.java` - Owns common lifecycle, deferred-register, and runtime listener bindings.
- `src/main/java/com/talhanation/recruits/Main.java` - Delegates bootstrap work to the new registrar helpers.

## Decisions Made
- Preserved the exact packet registration sequence by copying the current ordered list into a dedicated registrar instead of deriving it dynamically.
- Kept lifecycle extraction narrow: the helper only absorbed bootstrap registration responsibilities, not command logic or gameplay setup.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
- The first sentinel assertions used off-by-one packet positions; correcting the test indices brought the captured contract in line with the existing `Main` ordering.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- `07-03` can now follow the same seam style for client-state and persistence orchestration.
- `07-04` can build compat/path helpers without further expanding `Main` bootstrap responsibilities.

## Self-Check: PASSED

- Verified `RecruitsNetworkRegistrar`, `ModLifecycleRegistrar`, and `07-02-SUMMARY.md` exist on disk.
- Verified task commits `74ca9e50`, `174807cb`, and `7a8d1226` exist in git history.

---
*Phase: 07-migration-ready-internal-seams*
*Completed: 2026-04-08*
