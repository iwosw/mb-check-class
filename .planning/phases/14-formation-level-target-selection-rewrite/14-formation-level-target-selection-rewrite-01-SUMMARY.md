---
phase: 14-formation-level-target-selection-rewrite
plan: 01
subsystem: runtime
tags: [runtime, ai, targeting, formations, profiling, junit, bannermod]
requires:
  - phase: 13-path-reuse-for-cohesion-movement
    provides: reuse-aware controller profiling and dense-battle comparison seam
provides:
  - formation-level target-selection controller with resettable profiling
  - recruit target-search wiring that checks shared formation targeting before legacy local search
  - unit regression coverage for shared selection, invalidation, and fallback rules
affects: [phase-14-validation, phase-15-budgeting, large-battle-ai]
tech-stack:
  added: []
  patterns: [utility-shaped formation target-selection state with explicit request, reuse, invalidation, and fallback counters]
key-files:
  created:
    - .planning/phases/14-formation-level-target-selection-rewrite/14-formation-level-target-selection-rewrite-01-SUMMARY.md
    - recruits/src/main/java/com/talhanation/recruits/entities/ai/FormationTargetSelectionController.java
    - recruits/src/test/java/com/talhanation/recruits/entities/ai/FormationTargetSelectionControllerTest.java
  modified:
    - recruits/src/main/java/com/talhanation/recruits/entities/AbstractRecruitEntity.java
key-decisions:
  - "Keep formation selection utility-shaped and in-memory so Phase 14 stays scoped to target assignment rather than introducing persistent managers."
  - "Use the shared formation seam before legacy local search, but keep ungrouped or non-eligible recruits on the existing fallback behavior."
patterns-established:
  - "Formation target selection enters through FormationTargetSelectionController, then falls back to the preserved local candidate scan only when the recruit is not eligible for sharing."
requirements-completed: [TARGETSEL-01]
duration: not-recorded
completed: 2026-04-11
---

# Phase 14 Plan 01: Formation-Level Target Selection Rewrite Summary

**Grouped recruits now share one formation-target selection seam with explicit profiling, while ineligible recruits still use the preserved local search path.**

## Performance

- **Duration:** not recorded
- **Started:** not recorded
- **Completed:** 2026-04-11T23:03:53Z
- **Tasks:** 2
- **Files modified:** 4

## Accomplishments

- Added `FormationTargetSelectionController` with shared assignment, reuse, invalidation, and local-fallback accounting.
- Routed `AbstractRecruitEntity.searchForTargets()` through the formation-aware seam before the preserved local candidate scan.
- Added focused JUnit coverage for cohort reuse, invalidation, age-out behavior, and profiling counters.

## Task Commits

Not created — execution was requested without git commits.

## Files Created/Modified

- `recruits/src/main/java/com/talhanation/recruits/entities/ai/FormationTargetSelectionController.java` - Utility-shaped shared target-selection contract and profiling snapshot.
- `recruits/src/main/java/com/talhanation/recruits/entities/AbstractRecruitEntity.java` - Formation-aware target-selection wiring with preserved fallback search behavior.
- `recruits/src/test/java/com/talhanation/recruits/entities/ai/FormationTargetSelectionControllerTest.java` - Regression coverage for reuse, invalidation, fallback, and counter semantics.

## Decisions Made

- Kept the controller keyed by owner-plus-group cohort so later validation can reason about exactly why sharing did or did not happen.
- Allowed the async branch to compute shared formation assignments inline when the recruit is eligible, while preserving the old async task path for true local-fallback searches.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Prevented GameTest cohorts from being stripped by group validation**
- **Found during:** Task 2
- **Issue:** Test-only group UUIDs were being cleared by the existing group-manager validation seam before formation selection could activate.
- **Fix:** Registered GameTest formation cohorts as real `RecruitsGroup` entries before assigning recruit group ids.
- **Files modified:** recruits/src/gametest/java/com/talhanation/recruits/gametest/support/RecruitsBattleGameTestSupport.java
- **Verification:** `./gradlew verifyGameTestStage` dense-battle snapshots now report non-zero Phase 14 formation counters.
- **Committed in:** not committed

---

**Total deviations:** 1 auto-fixed (1 bug)
**Impact on plan:** Necessary to make the planned formation cohort behavior observable under the existing brownfield group validation rules.

## Issues Encountered

- The first GameTest runs showed zero formation assignments because the brownfield group manager cleared unregistered test group ids; registering real test groups resolved that issue.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Plan 14-02 can now capture dense-battle formation counters next to the Phase 11-13 profiling vocabulary.
- Focus-fire behavior is covered, but retarget and loss-of-target GameTests remain optional and still need follow-up hardening.

## Self-Check: PASSED

- Summary file exists.
- Verification command `./gradlew test --tests com.talhanation.recruits.entities.ai.FormationTargetSelectionControllerTest` passed.
- No git commit check was performed because the user explicitly requested no commits.

---
*Phase: 14-formation-level-target-selection-rewrite*
*Completed: 2026-04-11*
