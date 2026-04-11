---
phase: 01-build-reproducibility-baseline
plan: 02
subsystem: testing
tags: [gradle, junit5, gametest, verification, forge]
requires:
  - phase: 01-01
    provides: pinned canonical Gradle and plugin resolution
provides:
  - real JVM unit-test stage with JUnit 5 smoke coverage
  - named build, unit-test, and game-test verification tasks under check
  - gameTestServer entrypoint with explicit Phase 2 no-source fallback
affects: [phase-1-plan-03, phase-2, verification-workflow]
tech-stack:
  added: [junit-bom-5.10.2, junit-jupiter]
  patterns: [tdd-red-green, named-verification-stage-tasks, phase-gated-gametest-fallback]
key-files:
  created: [src/test/java/com/talhanation/recruits/build/BuildBaselineTest.java, .planning/phases/01-build-reproducibility-baseline/01-02-SUMMARY.md]
  modified: [build.gradle, .gitignore]
key-decisions:
  - "Used a pure JVM mods.toml smoke test so the initial test baseline stays independent of Forge runtime bootstrapping."
  - "Kept check as the canonical verification entrypoint while adding helper tasks for build, unit-test, and game-test attribution."
  - "Made verifyGameTestStage succeed with a clear NO-SOURCE Phase 2 message until the GameTest source tree exists."
patterns-established:
  - "Verification layers should be visible as named Gradle tasks under check."
  - "Phase 1 test baselines should avoid Minecraft runtime startup unless the plan explicitly requires it."
requirements-completed: [BLD-02, BLD-04]
duration: 14min
completed: 2026-04-05
---

# Phase 1 Plan 2: Build Reproducibility Baseline Summary

**JUnit 5 smoke coverage and named build/unit/game-test Gradle stages wired under the canonical `check` lifecycle.**

## Performance

- **Duration:** 14 min
- **Started:** 2026-04-05T14:02:00Z
- **Completed:** 2026-04-05T14:16:00Z
- **Tasks:** 2
- **Files modified:** 3

## Accomplishments
- Added a real `test` stage with JUnit 5 and a pure JVM smoke test that validates `mods.toml` contains the `recruits` mod id.
- Added a `gameTestServer` run configuration plus `verifyBuildStage`, `verifyUnitTestStage`, and `verifyGameTestStage` helper tasks.
- Verified `./gradlew tasks --all` and `./gradlew check --continue` now expose distinct verification layers with a clear Phase 2 `NO-SOURCE` message for GameTests.

## Task Commits

Each task was committed atomically:

1. **Task 1: Add a real unit-test baseline** - `92542975` (test), `4123ad3b` (feat)
2. **Task 2: Expose build, unit-test, and game-test stages through `check`** - `d39bb2d0` (feat)

**Plan metadata:** Pending

## Files Created/Modified
- `src/test/java/com/talhanation/recruits/build/BuildBaselineTest.java` - Pure JVM smoke test that reads `mods.toml` without booting Forge runtime.
- `build.gradle` - Adds JUnit 5 dependencies, enables JUnit Platform, defines `gameTestServer`, and registers named verification stage tasks.
- `.gitignore` - Restricts root build/bin ignores so nested `src/**/build` packages can be tracked.
- `.planning/phases/01-build-reproducibility-baseline/01-02-SUMMARY.md` - Records plan execution, deviations, and verification results.

## Decisions Made
- Used JUnit 5 BOM + Jupiter to make `./gradlew test` a standard Gradle/JVM stage with minimal setup.
- Kept the game-test layer behind the generated `runGameTestServer` task instead of inventing a custom top-level command.
- Allowed `verifyGameTestStage` to pass with a lifecycle message when no GameTest source tree exists yet, matching the Phase 1 interim contract from research.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Fixed `.gitignore` so nested `build` packages can be versioned**
- **Found during:** Task 1 (Add a real unit-test baseline)
- **Issue:** The existing `.gitignore` entry `build` ignored `src/test/java/com/talhanation/recruits/build/BuildBaselineTest.java`, blocking the TDD RED commit.
- **Fix:** Anchored the root ignore entries to `/build` and `/bin` so only generated top-level directories stay ignored.
- **Files modified:** `.gitignore`
- **Verification:** `git status --short --untracked-files=all` surfaced `BuildBaselineTest.java` after the change.
- **Committed in:** `92542975`

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** The fix was required to track the planned test source file and did not expand scope beyond execution needs.

## Issues Encountered
- `./gradlew test` initially failed during the TDD RED step because JUnit 5 was not yet configured; this was the expected failing test state before the GREEN implementation commit.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Phase 1 Plan 03 can now document `./gradlew build` and `./gradlew check --continue` against the actual task names and baseline outputs.
- Phase 2 can add real Forge GameTests behind the existing `gameTestServer` and `verifyGameTestStage` entrypoints.

## Self-Check: PASSED
- FOUND: `.planning/phases/01-build-reproducibility-baseline/01-02-SUMMARY.md`
- FOUND: `92542975`
- FOUND: `4123ad3b`
- FOUND: `d39bb2d0`

---
*Phase: 01-build-reproducibility-baseline*
*Completed: 2026-04-05*
