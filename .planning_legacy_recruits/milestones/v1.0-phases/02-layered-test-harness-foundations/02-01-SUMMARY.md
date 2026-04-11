---
phase: 02-layered-test-harness-foundations
plan: 01
subsystem: testing
tags: [gradle, forge, gametest, junit]
requires:
  - phase: 01-build-reproducibility-baseline
    provides: staged build/unit/game-test verification under check
provides:
  - dedicated src/gametest source-set wiring for Forge GameTests
  - a runnable smoke GameTest and reusable empty structure template
affects: [phase-2-testing, battle-tests, persistence-tests, networking-tests]
tech-stack:
  added: []
  patterns: [split src/test and src/gametest harness, game tests routed through verifyGameTestStage]
key-files:
  created: [src/gametest/java/com/talhanation/recruits/gametest/HarnessSmokeGameTests.java, src/gametest/resources/data/recruits/structures/harness_empty.nbt]
  modified: [build.gradle, BUILDING.md]
key-decisions:
  - "Use a dedicated gametest source set mapped to src/gametest/java and src/gametest/resources instead of placeholder source detection."
  - "Keep verifyGameTestStage under check and route it directly to runGameTestServer so GameTests stay a first-class verification layer."
  - "Use a reusable empty harness template plus a namespaced smoke test as the baseline runtime contract for later gameplay tests."
patterns-established:
  - "GameTests live under src/gametest with resources in src/gametest/resources."
  - "Smoke runtime tests should use @GameTestHolder(Main.MOD_ID) and reusable templates before deeper scenario coverage is added."
requirements-completed: [TEST-01, TEST-02]
duration: 10min
completed: 2026-04-05
---

# Phase 2 Plan 01: Layered Test Harness Foundations Summary

**Forge GameTests now run from a dedicated src/gametest harness with a reusable empty structure and a passing namespaced smoke test.**

## Performance

- **Duration:** 10 min
- **Started:** 2026-04-05T14:22:53Z
- **Completed:** 2026-04-05T14:33:00Z
- **Tasks:** 2
- **Files modified:** 4

## Accomplishments
- Added a dedicated `gametest` source set and wired it into `runGameTestServer`.
- Documented the stable split between pure JVM tests and Forge runtime GameTests.
- Added a passing `HarnessSmokeGameTests` case backed by a reusable `harness_empty` structure template.

## Task Commits

1. **Task 1: Standardize the split JVM/GameTest source layout** - `ebceab33` (feat)
2. **Task 2: Add a smoke GameTest and reusable empty template** - `fb98e947` (feat)

## Files Created/Modified
- `build.gradle` - defines the `gametest` source set, classpaths, and direct `verifyGameTestStage` wiring.
- `BUILDING.md` - documents the split test layout and updated GameTest verification contract.
- `src/gametest/java/com/talhanation/recruits/gametest/HarnessSmokeGameTests.java` - first namespaced Forge smoke GameTest.
- `src/gametest/resources/data/recruits/structures/harness_empty.nbt` - reusable empty runtime template for future GameTests.

## Decisions Made
- Used a dedicated `gametest` source set so future gameplay tests land in one canonical location.
- Kept `verifyGameTestStage` as the visible `check` sub-stage instead of inventing a new wrapper workflow.
- Used an intentionally empty reusable template so later phases can add runtime cases without re-bootstraping the harness.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Moved GameTest source-set wiring ahead of Forge run configuration access**
- **Found during:** Task 1
- **Issue:** `gameTestServer` configuration referenced `sourceSets.gametest` before the source set existed.
- **Fix:** Declared the `gametest` source set and inherited classpaths before the `minecraft { runs { ... } }` block.
- **Files modified:** `build.gradle`
- **Verification:** `./gradlew tasks --all`
- **Committed in:** `ebceab33`

**2. [Rule 3 - Blocking] Corrected GameTest source-set classpath inheritance**
- **Found during:** Task 1
- **Issue:** Gradle evaluation failed because the build referenced a non-existent `configurations.main` container.
- **Fix:** Switched the `gametest` compile classpath to inherit from `sourceSets.main` outputs and classpaths.
- **Files modified:** `build.gradle`
- **Verification:** `./gradlew tasks --all`
- **Committed in:** `ebceab33`

**3. [Rule 3 - Blocking] Removed duplicate GameTest resource registration**
- **Found during:** Task 2
- **Issue:** `processGametestResources` failed because explicit `srcDir` additions duplicated the already-conventional `src/gametest/resources` path.
- **Fix:** Replaced additive source-dir calls with `setSrcDirs(...)` for both Java and resources.
- **Files modified:** `build.gradle`
- **Verification:** `./gradlew verifyGameTestStage`
- **Committed in:** `fb98e947`

**4. [Rule 3 - Blocking] Re-encoded the reusable template with Minecraft's expected NBT list format**
- **Found during:** Task 2
- **Issue:** The initial generated `harness_empty.nbt` used an invalid structure size encoding, so Forge could not load the template.
- **Fix:** Rebuilt the structure template with the expected list-based size tag so the smoke test can spawn it successfully.
- **Files modified:** `src/gametest/resources/data/recruits/structures/harness_empty.nbt`
- **Verification:** `./gradlew verifyGameTestStage`
- **Committed in:** `fb98e947`

---

**Total deviations:** 4 auto-fixed (4 blocking)
**Impact on plan:** All auto-fixes were required to make the planned GameTest harness runnable. No scope creep.

## Issues Encountered
- Forge GameTest annotation support uses `net.minecraftforge.gametest.PrefixGameTestTemplate` rather than the vanilla package, so the smoke test import had to be corrected during implementation.
- Minimal structure templates must use Minecraft's structure NBT shape exactly or the runtime harness refuses to load them.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- The repository now has stable JVM and Forge runtime test locations for future fixture work.
- Phase 2 Plan 02 can build shared JVM helpers and reference tests on top of the new harness without reworking Gradle wiring.

## Self-Check: PASSED
