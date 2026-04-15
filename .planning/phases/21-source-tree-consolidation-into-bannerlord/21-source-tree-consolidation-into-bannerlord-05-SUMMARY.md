---
phase: 21-source-tree-consolidation-into-bannerlord
plan: 05
subsystem: infra
tags: [bannerlord, source-tree, root-build, validation, retirement]
requires:
  - phase: 21-source-tree-consolidation-into-bannerlord
    provides: wave-4 bannerlord civilian ownership and the narrowed compat.workers boundary
provides:
  - root-vendored Java, test, GameTest, and resource inputs for source-root retirement
  - truthful roadmap, state, merge-note, and verification status for the retired root-only layout
  - explicit validation evidence showing processResources is green while compile-driven gates remain blocked
affects: [phase-21-closeout, root-validation, compatibility-cleanup]
tech-stack:
  added: [root-vendored recruit resources, root-vendored worker resources, root-vendored recruit and worker test suites]
  patterns: [root-only Gradle source-set ownership, legacy trees preserved as archive-only references, validation docs record failed gates explicitly]
key-files:
  created:
    - src/main/java/com/talhanation/bannerlord/config/BannerModConfigFiles.java
    - src/main/resources/META-INF/mods.toml
    - src/gametest/java/com/talhanation/recruits/gametest/HarnessSmokeGameTests.java
    - src/test/java/com/talhanation/recruits/pathfinding/GlobalPathfindingControllerTest.java
  modified:
    - build.gradle
    - .planning/ROADMAP.md
    - .planning/STATE.md
    - .planning/VERIFICATION.md
    - MERGE_NOTES.md
key-decisions:
  - "Vendor the remaining recruit and worker resources/tests into tracked root src trees so source-root retirement stops depending on untracked nested repos."
  - "Keep the legacy recruits/ and workers/ directories as archive-only references outside the active Gradle source sets instead of pretending they are already deleted from disk."
  - "Document plan 21-05 as structurally landed but not phase-closing because compileJava, test, and verifyGameTestStage still fail on retained recruit↔bannerlord type mismatches."
patterns-established:
  - "Root-only build inputs: Gradle sourceSets now resolve Java, resources, tests, and GameTests from src/** only."
  - "Truthful retirement reporting: failed post-retirement gates are recorded explicitly instead of being described as deferred green validation."
requirements-completed: []
duration: 4 min
completed: 2026-04-15
---

# Phase 21 Plan 05: Source-Root Retirement Summary

**Root Gradle inputs now live under src/** with vendored recruit and worker resources/tests, while post-retirement validation truthfully reports compile-driven blockers in the remaining recruit↔bannerlord compatibility seam.**

## Performance

- **Duration:** 4 min
- **Started:** 2026-04-15T01:16:00Z
- **Completed:** 2026-04-15T01:20:12Z
- **Tasks:** 3
- **Files modified:** 861

## Accomplishments
- Retired active Gradle source-set ownership away from `recruits/` and `workers/` by vendoring the remaining Java compatibility copies, resources, JUnit suites, and recruit GameTest harness assets into tracked root `src/**` paths.
- Updated roadmap, state, verification, and merge notes so they describe the root-only build truth and do not claim Phase 21 is complete yet.
- Ran the required root validation commands and recorded the exact result: `processResources` passes, while `compileJava`, `test`, and `verifyGameTestStage` fail from the same retained recruit↔bannerlord type mismatches.

## Task Commits

Each task was committed atomically:

1. **Task 1: Remove legacy source-root ownership from the active build** - `96c9563` (feat)
2. **Task 2: Refresh roadmap, state, verification, and merge notes to record the post-consolidation truth** - `da9ddc2` (docs)
3. **Task 3: Run the full root validation gate required for source-root retirement** - `5c47295` (docs)

**Plan metadata:** pending

## Files Created/Modified
- `build.gradle` - removes legacy source-set wiring and points all build inputs at root `src/**`.
- `src/main/java/com/talhanation/recruits/**` and `src/main/java/com/talhanation/workers/**` - tracked root copies of the remaining compatibility-facing Java surfaces.
- `src/main/resources/**` - vendored recruit and worker runtime assets, metadata, mixin configs, and structures needed by the root artifact.
- `src/test/java/com/talhanation/recruits/**` and `src/test/java/com/talhanation/workers/**` - vendored retained JUnit coverage.
- `src/gametest/java/com/talhanation/recruits/**` and `src/gametest/resources/**` - vendored recruit GameTest harness coverage and structures.
- `.planning/ROADMAP.md`, `.planning/STATE.md`, `.planning/VERIFICATION.md`, `MERGE_NOTES.md` - updated with the truthful retirement and validation status.

## Decisions Made
- Vendored the remaining legacy resources/tests into tracked root state as requested by the user-approved option B, because source-root retirement could not be truthful while Gradle still depended on untracked nested repos for runtime assets and retained test coverage.
- Treated the physical `recruits/` and `workers/` directories as archive/reference copies, not active build owners.
- Kept Phase 21 marked in progress because the validation gate is materially blocked, not deferred by choice.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Vendored legacy resources and retained tests into root src trees**
- **Found during:** Task 1 (Remove legacy source-root ownership from the active build)
- **Issue:** Removing the legacy source-root wiring would have left the active build dependent on untracked nested-repo resources, JUnit suites, and recruit GameTest harness files.
- **Fix:** Copied the remaining recruit and worker resources plus retained test/GameTest inputs into tracked root `src/main/resources`, `src/test`, and `src/gametest` paths, then pointed Gradle source sets at those root-owned locations only.
- **Files modified:** `build.gradle`, `src/main/resources/**`, `src/test/**`, `src/gametest/**`
- **Verification:** `./gradlew processResources` succeeds from the root-only layout, and `build.gradle` no longer references `recruits/src/**` or `workers/src/**` in active source sets.
- **Committed in:** `96c9563`

**2. [Rule 3 - Blocking] Added a bannerlord config wrapper for root-only bootstrap wiring**
- **Found during:** Task 1 (Remove legacy source-root ownership from the active build)
- **Issue:** The root-only compile path referenced `com.talhanation.bannerlord.config.BannerModConfigFiles`, but only the older `com.talhanation.bannermod.config` implementation existed in tracked source.
- **Fix:** Added `src/main/java/com/talhanation/bannerlord/config/BannerModConfigFiles.java` as a thin delegating wrapper over the existing bannermod config helper.
- **Files modified:** `src/main/java/com/talhanation/bannerlord/config/BannerModConfigFiles.java`
- **Verification:** The missing-package error for `bannerlord.config.BannerModConfigFiles` no longer appears in the post-retirement compile output.
- **Committed in:** `96c9563`

---

**Total deviations:** 2 auto-fixed (2 blocking)
**Impact on plan:** Both fixes were required to make the retirement attempt truthful. They expanded only the root-vendoring work needed to remove active dependency on legacy source roots.

## Issues Encountered
- `./gradlew compileJava` still fails from the retired layout because moved `bannerlord` military/shared classes and retained `recruits` compatibility classes disagree on core types and method signatures. Representative failures include `RecruitEvents.assignGovernor(...)`, `bannerlord.entity.shared.AbstractInventoryEntity` using `RecruitSimpleContainer`, and `bannerlord.entity.shared.AbstractRecruitEntity` still calling old recruit-only event/helper signatures.
- Because `test` and `verifyGameTestStage` depend on successful compilation, both commands fail on the same blocker instead of reaching independent runtime assertions.

## Authentication Gates

None.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- The repository now has one active root build input tree under `src/**`; no active Java, test, or resource source-set wiring remains pointed at `recruits/` or `workers/`.
- Phase 21 still needs a compatibility-cleanup follow-up to reconcile the retained `recruits` wrappers with the moved `bannerlord` military/shared classes, then rerun `compileJava`, `test`, and `verifyGameTestStage` before the phase can honestly close.

---
*Phase: 21-source-tree-consolidation-into-bannerlord*
*Completed: 2026-04-15*

## Self-Check: PASSED
