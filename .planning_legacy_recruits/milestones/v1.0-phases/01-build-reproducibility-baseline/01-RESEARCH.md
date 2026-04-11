# Phase 1 Research — Build Reproducibility Baseline

## RESEARCH COMPLETE

**Date:** 2026-04-05
**Phase:** 01-build-reproducibility-baseline

## Current State

- `./gradlew clean build` succeeds on a clean sequential run with the current wrapper and toolchain setup.
- `./gradlew check` currently ends with `test NO-SOURCE`, so the verification path does not yet distinguish unit-test and game-test coverage in a meaningful way.
- `build.gradle` still has three reproducibility hazards called out in context:
  - `net.minecraftforge.gradle:ForgeGradle:6.+`
  - `org.spongepowered:mixingradle:0.7-SNAPSHOT`
  - unconditional `mavenLocal()`
- `README.md` does not document a clean-checkout maintainer workflow.

## External Confirmation

- ForgeGradle metadata confirms **`6.0.52`** is the latest stable 6.0 release, so the current dynamic `6.+` can be pinned safely.
- Sponge MixinGradle metadata confirms **`0.7.38`** is the latest stable release, so the current `0.7-SNAPSHOT` can be replaced with a fixed version.
- Forge 1.20.x GameTest docs confirm `gradlew runGameTestServer` is the supported Gradle entrypoint once a `gameTestServer` run configuration is present, and that the task exits non-zero when required game tests fail.

## Recommended Phase 1 Shape

### 1. Lock the canonical build inputs first

Use Phase 1 to make the default path deterministic:

- Pin ForgeGradle to `6.0.52`
- Pin MixinGradle to `0.7.38`
- Move `mavenLocal()` behind an explicit opt-in property such as `-PallowLocalMaven=true`
- Keep Gradle wrapper `8.8` and Foojay toolchain resolution unchanged
- Add dependency locking so the canonical branch does not drift on later clean checkouts

This directly addresses D-05 and D-06 from `01-CONTEXT.md`.

### 2. Make verification stages visible without inventing a wrapper command

The user explicitly wants Gradle lifecycle commands retained, so the safest implementation is:

- Keep `./gradlew build` as the documented build command
- Keep `check` as the lifecycle verification entrypoint
- Add stage-specific helper tasks with explicit names for:
  - build / assemble
  - unit tests
  - game tests
- Document `./gradlew check --continue` as the day-to-day invocation so Gradle attempts later stages when practical per D-04

This preserves D-01 and D-02 while still giving maintainers layer-by-layer output.

### 3. Add the thinnest possible real test baseline

Phase 2 will build the real harness, but Phase 1 still benefits from a tiny smoke baseline:

- Add JUnit 5 to the project so `test` is a real stage instead of `NO-SOURCE`
- Add one pure JVM smoke test that inspects build metadata/resources without booting Minecraft runtime
- Add the `gameTestServer` run configuration and a `gameTest` lifecycle alias now, even if the initial GameTest stage is allowed to report `NO-SOURCE` until Phase 2 adds actual cases

This keeps Phase 1 small while still creating the reproducible verification skeleton later phases build on.

### 4. Document the workflow in two places

Per D-08, the phase should land both:

- a concise README section for first-time maintainers
- a dedicated build-focused repo document with prerequisites, commands, override path, and failure interpretation

## Planning Implications

Recommended plan split:

1. **Plan 01** — harden Gradle/plugin/repository reproducibility
2. **Plan 02** — wire visible build/unit/game-test stages into `check`
3. **Plan 03** — document the canonical maintainer workflow and failure interpretation

This keeps file ownership clear and makes the docs plan depend on the final task names from the verification plan.

## Risks To Avoid

- Do **not** replace `build` or `check` with a bespoke top-level wrapper task; helper tasks are fine, replacing the lifecycle command is not.
- Do **not** leave `mavenLocal()` active in the canonical path.
- Do **not** depend on manual GameTest launching from the client/server run configs; use a dedicated `gameTestServer` path.
- Do **not** document local-override behavior as the normal path.

## Validation Architecture

- **Quick feedback command:** `./gradlew buildEnvironment`
- **Task-level validation commands:** use the task-specific commands embedded in each PLAN
- **Full verification command after Phase 1:** `./gradlew clean build && ./gradlew check --continue`
- **Expected stage labels in output:** build / unit test / game test
- **Known acceptable interim state:** the GameTest stage may report `NO-SOURCE` in Phase 1, but it must still be a named, callable Gradle stage with documented behavior
