# Root Verification Baseline

## What Exists Today

### Build Verification

- `compileJava` compiles the merged root source sets: `src/main/java`, `recruits/src/main/java`, and `workers/src/main/java`.
- `processResources` merges root, recruits, and workers resources into the single root artifact.
- `verifyBuildStage` wraps the build stage used by `check`.

### Unit / Regression Verification

- `test` runs root JUnit 5 coverage from `src/test/java`, `recruits/src/test/java`, and `workers/src/test/java`.
- Current root smoke/regression coverage includes:
  - merged Workers runtime identity and asset-path helpers
  - merged Workers builder-progress flow helpers
  - retained Workers JUnit suites under `workers/src/test/java`
  - legacy `workers:*` id migration for known structure/build NBT paths
  - build-area authoring and cleanup-policy seams that fence merged worker mutation and legacy update-check registration
- `verifyUnitTestStage` wraps the unit test stage used by `check`.

### GameTest Verification

- Root `gametest` source set is wired in `build.gradle` and exposed through `runGameTestServer`.
- `verifyGameTestStage` depends on `runGameTestServer`.
- Current root gametest directories are placeholders only; no active root GameTest classes are present yet.

## Recommended Default Validation For Stabilization Slices

Run from the repository root:

1. `./gradlew compileJava`
2. `./gradlew processResources`
3. `./gradlew test`

Phase 2 design outputs must preserve the existing migration helpers and compatibility seams documented in `.planning/phases/02-runtime-unification-design/02-runtime-compatibility-contract.md`.

Use `./gradlew runGameTestServer` only when a later execution slice changes actual gameplay or runtime flow while root GameTests remain sparse. It remains additive validation rather than the default stabilization baseline.

## Why This Baseline Exists

This merge workspace needs fast verification that the unified root build still compiles, resource merging remains stable, and the known Workers migration helpers do not regress, without promising broader save or integration compatibility than the active contract defines or requiring full Minecraft runtime E2E on every stabilization slice.
