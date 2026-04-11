---
phase: 14-formation-level-target-selection-rewrite
plan: 03
subsystem: testing
tags: [testing, gametest, ai, targeting, formations, bannermod]
requires:
  - phase: 14-formation-level-target-selection-rewrite
    provides: formation-aware target selection seam from plans 14-01 and 14-02
provides:
  - deterministic recovery-cohort harness adjustments for the Phase 14 retarget and loss-of-target checks
  - same-tick shared-target invalidation that publishes replacement or null results through the formation controller seam
affects: [phase-14-validation, future-targeting-followup]
tech-stack:
  added: []
  patterns: [same-tick null shared-target publication through the existing formation controller vocabulary]
key-files:
  created:
    - .planning/phases/14-formation-level-target-selection-rewrite/14-formation-level-target-selection-rewrite-03-SUMMARY.md
  modified:
    - recruits/src/gametest/java/com/talhanation/recruits/gametest/battle/FormationTargetSelectionGameTests.java
    - recruits/src/gametest/java/com/talhanation/recruits/gametest/support/RecruitsBattleGameTestSupport.java
    - recruits/src/main/java/com/talhanation/recruits/entities/AbstractRecruitEntity.java
    - recruits/src/main/java/com/talhanation/recruits/entities/ai/FormationTargetSelectionController.java
    - recruits/src/test/java/com/talhanation/recruits/entities/ai/FormationTargetSelectionControllerTest.java
key-decisions:
  - Use the existing `formation_recovery_field` template plus helper-side cohort cleanup instead of creating a new Phase 14 structure.
  - Publish same-tick null shared-target resolutions as a reused controller decision so later cohort members clear stale targets instead of reusing dead assignments.
requirements-completed: []
duration: not-recorded
completed: 2026-04-11
---

# Phase 14 Plan 03: Formation-Level Target Selection Rewrite Summary

**Phase 14 now hardens the shared-target invalidation seam and narrows the retarget/loss-of-target GameTest harness, but the two phase-local optional GameTests still fail in brownfield verification and were deferred after the fix-attempt limit.**

## Performance

- **Duration:** not recorded
- **Started:** not recorded
- **Completed:** 2026-04-11T23:42:00Z
- **Tasks:** 2
- **Files modified:** 5

## Accomplishments

- Moved the gap-closure scenarios onto the existing recovery-field harness and added helper support for explicit shared-target setup and local cohort cleanup.
- Added a RED controller test for same-tick null shared-target publication.
- Updated the runtime selection seam so reused shared decisions can clear stale targets when a cohort has no valid replacement on the current tick.

## Task Commits

- `recruits@272115a0` - `test(14-03): harden formation retarget gametests`
- `recruits@f8a19731` - `test(14-03): add failing test for shared target clearing`
- `recruits@8e1e5a28` - `fix(14-03): clear dead shared formation targets deterministically`

## Files Created/Modified

- `recruits/src/gametest/java/com/talhanation/recruits/gametest/battle/FormationTargetSelectionGameTests.java` - recovery-field harness tuning for focus-fire/retarget/clear scenarios.
- `recruits/src/gametest/java/com/talhanation/recruits/gametest/support/RecruitsBattleGameTestSupport.java` - shared-target and local-cohort cleanup helpers for the focused harness.
- `recruits/src/main/java/com/talhanation/recruits/entities/AbstractRecruitEntity.java` - applies reused null shared selections so stale targets are cleared immediately.
- `recruits/src/main/java/com/talhanation/recruits/entities/ai/FormationTargetSelectionController.java` - records same-tick null shared resolutions and reuses them across the cohort.
- `recruits/src/test/java/com/talhanation/recruits/entities/ai/FormationTargetSelectionControllerTest.java` - regression coverage for same-tick null shared-target publication.

## Decisions Made

- Reused the pre-existing `formation_recovery_field` template instead of adding a new structure file.
- Kept the Phase 14 profiling vocabulary unchanged while extending controller behavior to represent “shared clear” as a same-tick reused decision with a null target.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking Issue] Root verification had to run from the merged workspace instead of the nested `recruits/` Gradle entrypoint**
- **Found during:** Task 2 RED verification
- **Issue:** The nested repo compile path could not resolve merged BannerMod classes, so the planned unit-test command failed before the new RED assertion ran.
- **Fix:** Switched verification to the merged root Gradle build, which is the active runtime entrypoint for this workspace.
- **Files modified:** none
- **Committed in:** not committed

## Deferred Issues

- `./gradlew test --tests com.talhanation.recruits.entities.ai.FormationTargetSelectionControllerTest` passes.
- `./gradlew compileGameTestJava` passes.
- `./gradlew verifyGameTestStage` still reports the two phase-local optional failures `formationCohortRetargetsAfterSharedEnemyDies` and `formationCohortClearsDeadTargetsWhenNoReplacementExists` after three fix attempts on the brownfield harness.
- The run also still reports the pre-existing reconnect authority failure and other unrelated brownfield failures outside this plan’s scope.

## Issues Encountered

- The focused recovery-field scenarios remain unstable in full `verifyGameTestStage` even after narrowing the cohort harness and publishing same-tick null shared-target results.
- Brownfield `verifyGameTestStage` continues to include unrelated reconnect/leader packet failures that obscure end-to-end Phase 14 closure.

## User Setup Required

None.

## Next Phase Readiness

- The controller/unit seam now has direct regression coverage for same-tick shared-target clearing.
- Phase 14 still needs one more focused follow-up on the brownfield GameTest arena before the two optional retarget/loss-of-target checks can be called green.

## Self-Check: PASSED

- Summary file exists.
- Recruits task commits exist: `272115a0`, `f8a19731`, and `8e1e5a28`.
- Root unit test command passed.
- Root `compileGameTestJava` passed.
- Remaining verification failures are documented above.

---
*Phase: 14-formation-level-target-selection-rewrite*
*Completed: 2026-04-11*
