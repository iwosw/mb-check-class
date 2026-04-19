# Merged Baseline Readiness Summary

> Historical summary only. This file predates the current root GameTest and planning cleanup work; use `.planning/ROADMAP.md`, `.planning/STATE.md`, and `.planning/VERIFICATION.md` as the active source of truth.

- Investigated the root verification surface from the actual merged build: JUnit is active at `src/test/java`, the root `gametest` source set exists but is still a placeholder, and build verification is anchored on `compileJava`, `processResources`, and the wrapper tasks declared in `build.gradle`.
- Added lightweight regression coverage for merged Workers stabilization without requiring runtime E2E: smoke tests for merged runtime helper invariants and builder-progress flow helpers, while keeping the existing legacy-id migration regression coverage in place.
- Added readiness docs so future GSD mapping/orchestration sees one merged project with explicit source-of-truth paths, preserved legacy archives, verification entrypoints, and open risks.

## Changed Files

- `src/test/java/com/talhanation/workers/BuilderBuildProgressSmokeTest.java`
- `src/test/java/com/talhanation/workers/WorkersRuntimeSmokeTest.java`
- `.planning/CODEBASE.md`
- `.planning/VERIFICATION.md`
- `.planning/PROJECT.md`
- `.planning/ROADMAP.md`
- `.planning/REQUIREMENTS.md`
- `.planning/STATE.md`
- `MERGE_NOTES.md`

## Verification

- `./gradlew compileJava`
- `./gradlew processResources`
- `./gradlew test`

## Residual Risks

- Root GameTest wiring exists but there are still no active root GameTest classes, so gameplay validation remains limited to compile plus JUnit smoke coverage unless a future slice adds runtime tests.
- Legacy `workers:*` compatibility coverage is still intentionally focused on confirmed critical paths, not arbitrary third-party datapack or custom NBT payloads.
- No workspace-root git repository is initialized, so task-by-task execution commits could not be created from `/home/kaiserroman/bannermod`.

## Self-Check

PASSED - readiness docs, new smoke tests, and the local execution summary file all exist in the expected root paths.
