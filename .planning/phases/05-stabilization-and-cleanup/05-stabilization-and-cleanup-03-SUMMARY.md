---
phase: 05-stabilization-and-cleanup
plan: 03
subsystem: runtime
tags: [workers, recruits, lifecycle, cleanup, update-check]
requires:
  - phase: 02-runtime-unification-design
    provides: bannermod-first runtime identity and compatibility boundary
provides:
  - Explicit merged-runtime cleanup policy for legacy update checkers
  - Guarded recruits lifecycle registration for legacy update checker
  - Guarded workers lifecycle registration for legacy update checker
affects: [phase-05, runtime-cleanup, lifecycle, release-feed]
tech-stack:
  added: []
  patterns: [explicit merged cleanup policy seams, lifecycle registration guarded by policy]
key-files:
  created:
    - workers/src/main/java/com/talhanation/workers/MergedRuntimeCleanupPolicy.java
  modified:
    - recruits/src/main/java/com/talhanation/recruits/init/ModLifecycleRegistrar.java
    - workers/src/main/java/com/talhanation/workers/init/WorkersLifecycleRegistrar.java
    - src/test/java/com/talhanation/workers/MergedRuntimeCleanupPolicyTest.java
key-decisions:
  - "The merged bannermod runtime disables both legacy update-check listeners until one merged release-feed contract exists."
patterns-established:
  - "Legacy runtime listeners that conflict with merged branding should be gated behind explicit policy seams instead of left implicitly active."
requirements-completed: [STAB-03]
duration: 6min
completed: 2026-04-11
---

# Phase 5 Plan 3: Stabilization and Cleanup Summary

**Merged runtime cleanup policy now keeps both legacy update-check listeners disabled and makes each lifecycle registrar honor that policy explicitly**

## Performance

- **Duration:** 6 min
- **Started:** 2026-04-11T05:44:30Z
- **Completed:** 2026-04-11T05:50:29Z
- **Tasks:** 2
- **Files modified:** 4

## Accomplishments
- Added `MergedRuntimeCleanupPolicy.enableLegacyUpdateCheckers()` as an explicit merged-runtime seam that returns `false`.
- Added root regression coverage locking the merged runtime to keep legacy update checkers disabled.
- Updated both recruits and workers lifecycle registrars so the old update checkers no longer live-register unconditionally.

## Task Commits

Each task was committed atomically:

1. **Task 1: Create an explicit merged-runtime cleanup policy for legacy update checkers** - `4717c98` (root test), `workers@6fee2e8` (feat)
2. **Task 2: Guard both lifecycle registrars so the legacy update checkers stop live-registering** - `recruits@25a90bd0` (fix), `workers@d5a9a94` (fix)

**Plan metadata:** pending final docs commit

## Files Created/Modified
- `src/test/java/com/talhanation/workers/MergedRuntimeCleanupPolicyTest.java` - Root regression coverage for the merged update-check cleanup policy.
- `workers/src/main/java/com/talhanation/workers/MergedRuntimeCleanupPolicy.java` - Explicit merged-runtime switch keeping legacy update checkers disabled.
- `recruits/src/main/java/com/talhanation/recruits/init/ModLifecycleRegistrar.java` - Guards recruits update-check registration behind the cleanup policy.
- `workers/src/main/java/com/talhanation/workers/init/WorkersLifecycleRegistrar.java` - Guards workers update-check registration behind the cleanup policy.

## Decisions Made
- Left release-feed behavior intentionally unresolved and disabled rather than guessing at a merged URL or mixed-branding update story.
- Preserved all other runtime listeners, including `WorkersLegacyMappings`, while retiring only the duplicated update-check seam.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Routed lifecycle commits through the nested file-owning repos**
- **Found during:** Task 1 and Task 2
- **Issue:** The root workspace uses nested `workers/` and `recruits/` git histories, so the plan's file set spans multiple repos.
- **Fix:** Committed the root test in the root repo and the lifecycle/policy code in the nested repos that own those files.
- **Files modified:** `src/test/java/com/talhanation/workers/MergedRuntimeCleanupPolicyTest.java`, `workers/src/main/java/com/talhanation/workers/MergedRuntimeCleanupPolicy.java`, `recruits/src/main/java/com/talhanation/recruits/init/ModLifecycleRegistrar.java`, `workers/src/main/java/com/talhanation/workers/init/WorkersLifecycleRegistrar.java`
- **Verification:** `./gradlew test --tests "com.talhanation.workers.MergedRuntimeCleanupPolicyTest"`
- **Committed in:** `4717c98`, `6fee2e8`, `25a90bd0`, `d5a9a94`

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** Only commit routing changed; runtime cleanup behavior matched the plan exactly.

## Issues Encountered

- The nested repo layout required separate commits for root tests, recruits lifecycle code, and workers lifecycle code.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Active docs can now describe legacy update-check listeners as intentionally disabled in the merged runtime.
- Later release-feed work can add one explicit merged contract without needing to undo hidden legacy listener registrations first.

## Self-Check: PASSED

---
*Phase: 05-stabilization-and-cleanup*
*Completed: 2026-04-11*
