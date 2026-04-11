---
phase: 01-build-reproducibility-baseline
plan: 01
subsystem: infra
tags: [gradle, forgegradle, mixingradle, reproducibility, repositories]
requires: []
provides:
  - pinned ForgeGradle and MixinGradle buildscript versions for canonical builds
  - opt-in local Maven override path outside the default maintainer workflow
affects: [phase-1-plan-02, phase-1-plan-03, build-verification]
tech-stack:
  added: []
  patterns: [pinned-buildscript-versions, opt-in-local-repository-overrides]
key-files:
  created: [.planning/phases/01-build-reproducibility-baseline/01-01-SUMMARY.md]
  modified: [build.gradle]
key-decisions:
  - "Pinned ForgeGradle to 6.0.52 and MixinGradle to 0.7.38 to stop canonical buildscript drift."
  - "Kept mavenLocal() available only behind -PallowLocalMaven so local artifacts stay out of the default workflow."
patterns-established:
  - "Canonical Gradle resolution uses fixed plugin coordinates instead of version ranges or snapshots."
  - "Machine-specific repository inputs must be explicit opt-in properties, not default repositories."
requirements-completed: [BLD-01, BLD-03]
duration: 5min
completed: 2026-04-05
---

# Phase 1 Plan 1: Build Reproducibility Baseline Summary

**Pinned ForgeGradle and MixinGradle resolution with an explicit local Maven escape hatch for non-canonical builds.**

## Performance

- **Duration:** 5 min
- **Started:** 2026-04-05T13:56:00Z
- **Completed:** 2026-04-05T14:00:58Z
- **Tasks:** 1
- **Files modified:** 1

## Accomplishments
- Replaced drifting ForgeGradle and MixinGradle coordinates with fixed stable versions.
- Removed default local Maven participation from canonical dependency resolution.
- Verified `./gradlew buildEnvironment` resolves ForgeGradle `6.0.52` and MixinGradle `0.7.38` successfully.

## Task Commits

Each task was committed atomically:

1. **Task 1: Pin canonical Gradle and plugin resolution** - `10195489` (feat)

**Plan metadata:** Pending

## Files Created/Modified
- `build.gradle` - Pins buildscript dependencies and gates `mavenLocal()` behind `allowLocalMaven`.
- `.planning/phases/01-build-reproducibility-baseline/01-01-SUMMARY.md` - Records execution details and verification results for the plan.

## Decisions Made
- Pinned `net.minecraftforge.gradle:ForgeGradle` to `6.0.52` and `org.spongepowered:mixingradle` to `0.7.38` to keep clean-checkout builds deterministic.
- Preserved the existing Gradle wrapper and Foojay toolchain configuration, limiting this plan to reproducibility hazards called out in phase context.
- Kept `mavenLocal()` as an explicit `-PallowLocalMaven` override so local experimentation remains possible without affecting the canonical workflow.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
- `./gradlew buildEnvironment` generated local IDE/cache noise (`.factorypath`, `.gradle/buildOutputCleanup/cache.properties`); these were cleaned from the working tree before task commit.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Phase 1 Plan 02 can now wire named verification stages on top of a pinned, reproducible Gradle baseline.
- The canonical build path no longer depends on local Maven cache state unless a maintainer opts in explicitly.

## Self-Check: PASSED
- FOUND: `.planning/phases/01-build-reproducibility-baseline/01-01-SUMMARY.md`
- FOUND: `10195489`

---
*Phase: 01-build-reproducibility-baseline*
*Completed: 2026-04-05*
