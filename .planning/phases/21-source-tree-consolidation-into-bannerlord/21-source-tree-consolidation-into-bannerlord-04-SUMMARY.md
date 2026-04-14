---
phase: 21-source-tree-consolidation-into-bannerlord
plan: 04
subsystem: civilian
tags: [bannerlord, workers, civilian, compatibility, migration]
requires:
  - phase: 21-source-tree-consolidation-into-bannerlord
    provides: wave-3 recruit-owned entity, pathfinding, persistence, and shared client foundations under bannerlord
provides:
  - worker civilian entities, AI, persistence helpers, and client flows under bannerlord package ownership
  - explicit worker compatibility helpers under bannerlord.compat.workers
  - updated worker regression imports and phase-21 verification guidance for the civilian move
affects: [phase-21-wave-5, worker-runtime-validation, source-root-retirement]
tech-stack:
  added: [temporary BannerMod logistics compatibility records/service]
  patterns: [worker gameplay classes live under bannerlord civilian packages, worker migration helpers stay isolated under compat.workers]
key-files:
  created:
    - src/main/java/com/talhanation/bannerlord/entity/civilian/AbstractWorkerEntity.java
    - src/main/java/com/talhanation/bannerlord/ai/civilian/CourierWorkGoal.java
    - src/main/java/com/talhanation/bannerlord/persistence/civilian/StructureManager.java
    - src/main/java/com/talhanation/bannerlord/client/civilian/gui/WorkAreaScreen.java
    - src/main/java/com/talhanation/bannerlord/compat/workers/WorkersRuntime.java
  modified:
    - src/main/java/com/talhanation/bannerlord/registry/civilian/WorkersLifecycleRegistrar.java
    - src/main/java/com/talhanation/bannerlord/network/BannerlordNetworkBootstrap.java
    - src/main/java/com/talhanation/bannermod/logistics/BannerModSupplyStatus.java
    - src/test/java/com/talhanation/bannermod/BannerModIntegratedRuntimeSmokeTest.java
    - .planning/VERIFICATION.md
key-decisions:
  - "Worker civilian ownership now lives under src/main/java/com/talhanation/bannerlord/{entity,ai,persistence,client}/civilian/** while workers/src/main/java/com/talhanation/workers/** is reduced to registrars, network, config, and other compatibility-facing surfaces."
  - "Legacy worker runtime helpers moved into src/main/java/com/talhanation/bannerlord/compat/workers/**, with only a thin deprecated workers.WorkersRuntime adapter retained to keep remaining workers-package registrars compiling during source-root retirement."
  - "Worker storage and courier code now compiles against narrow temporary BannerMod logistics compatibility types until the dedicated logistics backbone phase replaces them."
patterns-established:
  - "Worker gameplay ownership: bannerlord.entity/ai/persistence/client.civilian packages are the canonical worker code homes."
  - "Compatibility isolation: workers legacy-id remaps and structure migration helpers belong under bannerlord.compat.workers, not mixed into gameplay packages."
requirements-completed: []
duration: 1 min
completed: 2026-04-14
---

# Phase 21 Plan 04: Worker Civilian Ownership Summary

**Worker civilian entities, profession AI, work-area persistence, and client flows now live under bannerlord civilian packages with worker migration helpers isolated under compat.workers.**

## Performance

- **Duration:** 1 min
- **Started:** 2026-04-14T17:25:55Z
- **Completed:** 2026-04-14T17:26:36Z
- **Tasks:** 2
- **Files modified:** 122

## Accomplishments
- Re-homed worker entities, work areas, AI goals, world helpers, renderers, screens, and widgets into `src/main/java/com/talhanation/bannerlord/**/civilian`.
- Moved `WorkersRuntime` and `WorkersLegacyMappings` into `src/main/java/com/talhanation/bannerlord/compat/workers/**` and retargeted lifecycle/network/test imports.
- Updated worker-focused tests, GameTests, and `.planning/VERIFICATION.md` so Phase 21 explicitly checks worker runtime identity, mining/build regressions, and legacy migration helpers after the move.

## Task Commits

Each task was committed atomically:

1. **Task 1: Re-home worker civilian code and isolate the compatibility layer** - `1ec78c4` (feat)
2. **Task 2: Keep worker regression coverage and runtime validation aligned with the move** - `f9ecd0a` (docs)

**Plan metadata:** pending

## Files Created/Modified
- `src/main/java/com/talhanation/bannerlord/entity/civilian/**` - canonical worker entities and work-area entity classes.
- `src/main/java/com/talhanation/bannerlord/ai/civilian/**` - canonical worker profession AI and mining/build helper logic.
- `src/main/java/com/talhanation/bannerlord/persistence/civilian/**` - worker structure, block-scan, item-need, and work-area persistence helpers.
- `src/main/java/com/talhanation/bannerlord/client/civilian/**` - worker screens, widgets, renderers, and client events.
- `src/main/java/com/talhanation/bannerlord/compat/workers/**` - legacy worker runtime identity and missing-mapping migration helpers.
- `src/main/java/com/talhanation/bannermod/logistics/BannerModLogistics*.java` - temporary compatibility types required by the moved storage/courier sources.
- `src/test/java/com/talhanation/**` and `src/gametest/java/com/talhanation/bannermod/**` - updated imports so worker validation targets bannerlord civilian ownership.
- `.planning/VERIFICATION.md` - wave-4 ownership and regression guidance.

## Decisions Made
- Worker civilian gameplay classes are now bannerlord-owned even though some worker bootstrap, config, menu, and packet surfaces remain in `workers/` as transitional compatibility code.
- Compatibility-critical worker runtime helpers now have a dedicated `compat.workers` home instead of living beside gameplay classes.
- A thin deprecated `workers.WorkersRuntime` adapter is temporarily acceptable because the remaining workers-package registrars still need the shared runtime helper while Phase 21 source-root retirement is unfinished.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Added narrow logistics compatibility types for moved worker storage/courier code**
- **Found during:** Task 1 (Re-home worker civilian code and isolate the compatibility layer)
- **Issue:** The moved `StorageArea`, `CourierWorkGoal`, and storage-request flows referenced `BannerModLogisticsService`, `BannerModLogisticsRoute`, and `BannerModCourierTask`, but those classes were missing from the tracked root source tree.
- **Fix:** Added temporary compatibility record/service classes under `src/main/java/com/talhanation/bannermod/logistics/` so the civilian move can compile against one explicit seam while the later logistics-backbone phase owns the full implementation.
- **Files modified:** `src/main/java/com/talhanation/bannermod/logistics/BannerModCourierTask.java`, `src/main/java/com/talhanation/bannermod/logistics/BannerModLogisticsRoute.java`, `src/main/java/com/talhanation/bannermod/logistics/BannerModLogisticsService.java`
- **Verification:** `./gradlew compileJava` advanced past the missing logistics-type errors and then failed only on pre-existing wave-3 recruit/bannerlord type mismatches logged to `deferred-items.md`.
- **Committed in:** `1ec78c4`

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** The deviation was required to keep the worker civilian move buildable against the current tree; it did not widen the plan beyond compatibility needed by the moved sources.

## Issues Encountered
- `./gradlew compileJava` is still blocked by pre-existing wave-3 recruit-to-bannerlord type mismatches in military/shared code outside this plan's scope. Logged to `.planning/phases/21-source-tree-consolidation-into-bannerlord/deferred-items.md` and not fixed here.

## Authentication Gates

None.

## Known Stubs

- `src/main/java/com/talhanation/bannermod/logistics/BannerModLogisticsService.java` - `selectBestTask()` and `selectBlockedReason()` currently return `Optional.empty()` as a temporary compatibility shim until the dedicated logistics backbone phase provides the real service behavior.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Wave 4 physical ownership is in place, so phase 21-05 can focus on shrinking the remaining compatibility boundary and retiring legacy source roots.
- Before phase 21 can claim full root validation, the deferred wave-3 recruit/bannerlord type mismatches need a dedicated cleanup slice or prior fix so `compileJava` and the broader validation gate can run end-to-end again.

---
*Phase: 21-source-tree-consolidation-into-bannerlord*
*Completed: 2026-04-14*

## Self-Check: PASSED
