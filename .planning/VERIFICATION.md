# Root Verification Baseline

## What Exists Today

### Build Verification

- `compileJava` compiles the merged root source sets: `src/main/java`, `recruits/src/main/java`, and `workers/src/main/java`.
- `processResources` merges root, recruits, and workers resources into the single root artifact.
- `verifyBuildStage` wraps the build stage used by `check`.

### Unit / Regression Verification

- `test` runs root JUnit 5 coverage from `src/test/java`, `recruits/src/test/java`, and `workers/src/test/java`.
- Current root smoke/regression coverage includes:
  - BannerMod runtime identity plus worker subsystem seam alignment in one root merged-runtime smoke test
  - shared BannerMod authority vocabulary for inspect, modify, create, and recover-control decisions
  - shared BannerMod config-file taxonomy and legacy-file migration guardrails for military, settlement, and client surfaces
  - shared BannerMod supply-status vocabulary for build-material pressure, worker storage blockage, and recruit upkeep food/payment pressure
  - merged Workers runtime identity and asset-path helpers
  - merged Workers builder-progress flow helpers
  - retained Workers JUnit suites under `workers/src/test/java`
  - legacy `workers:*` id migration for known structure/build NBT paths
  - build-area authoring and cleanup-policy seams that fence merged worker mutation and legacy update-check registration
- `verifyUnitTestStage` wraps the unit test stage used by `check`.

### GameTest Verification

- Root `gametest` source set is wired in `build.gradle` and exposed through `runGameTestServer`.
- `verifyGameTestStage` depends on `runGameTestServer`.
- Root `gametest` now includes a split BannerMod validation suite under `src/gametest/java/com/talhanation/bannermod/`: `IntegratedRuntimeGameTests.java` for merged runtime smoke, `BannerModOwnershipCycleGameTests.java` for shared-ownership boundaries, `BannerModSettlementLaborGameTests.java` for owned work-area participation plus outsider recovery denial, `BannerModUpkeepFlowGameTests.java` for same-owner supply-to-upkeep transitions, `BannerModPlayerCycleGameTests.java` for one stitched ownership→labor→upkeep→recovery gameplay loop, `BannerModDedicatedServerAuthorityGameTests.java` for offline-owner and unresolved-owner authority denial, `BannerModDedicatedServerReconnectGameTests.java` for reconnect and persistence-safe ownership recovery, `BannerModMultiplayerAuthorityConflictGameTests.java` for live owner-versus-outsider contention, and `BannerModMultiplayerCooperationGameTests.java` for same-team cooperation with outsider regressions.
- `./gradlew verifyGameTestStage` is currently green with 36 required tests.

### Phase 11 Profiling Baseline

- Phase 11 baseline definition now lives in `.planning/phases/11-large-battle-ai-profiling-baseline/11-PROFILING-BASELINE.md`.
- Baseline capture procedure and acceptance rules now live in `.planning/phases/11-large-battle-ai-profiling-baseline/11-VALIDATION.md`.
- Plan 11-01 is documentation-only: it defines the canonical large-battle scenarios, hotspot counters, and evidence bundle shape rooted in the existing recruit battle fixtures and async seams.
- Plan 11-02 remains the implementation slice for any missing low-risk instrumentation or harness support needed to collect those counters directly.

## Recommended Default Validation For Stabilization Slices

Run from the repository root:

1. `./gradlew compileJava`
2. `./gradlew processResources`
3. `./gradlew test`

Phase 2 design outputs must preserve the existing migration helpers and compatibility seams documented in `.planning/phases/02-runtime-unification-design/02-runtime-compatibility-contract.md`.

Use `./gradlew runGameTestServer` or `./gradlew verifyGameTestStage` when a slice changes merged gameplay/runtime wiring or needs root-level smoke validation of recruit-plus-worker coexistence. `compileJava`, `processResources`, and `test` remain the default fast stabilization baseline, while GameTest now also covers dedicated ownership, live multiplayer authority contention, same-team cooperation, settlement-labor, upkeep-flow, and stitched player-cycle slices instead of only registry/runtime coexistence.

## Why This Baseline Exists

This merge workspace needs fast verification that the unified root build still compiles, resource merging remains stable, and the known Workers migration helpers do not regress, without promising broader save or integration compatibility than the active contract defines or requiring full Minecraft runtime E2E on every stabilization slice.
