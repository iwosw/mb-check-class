# Root Verification Baseline

## What Exists Today

### Build Verification

- `compileJava` now compiles the retired root source set only: `src/main/java`.
- `processResources` now reads from vendored root resources under `src/main/resources` and `src/generated/resources`, including the copied recruit and worker assets that used to live only under legacy source roots.
- `verifyBuildStage` wraps the build stage used by `check`.

### Unit / Regression Verification

- `test` now runs root JUnit 5 coverage from `src/test/java`, which includes the vendored recruit and worker regression suites.
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
- Phase 21 wave 1 now has canonical shared seam homes under `src/main/java/com/talhanation/bannerlord/shared/**` and `src/main/java/com/talhanation/bannerlord/config/**`, while `com.talhanation.bannermod/**` remains as a temporary forwarding surface during import churn.
- Phase 21 wave 2 now keeps the one live `@Mod` entrypoint, shared `SimpleChannel`, and registry/lifecycle composition under `src/main/java/com/talhanation/bannerlord/bootstrap/**`, `.../network/**`, and `.../registry/**`, with recruit and worker package surfaces reduced to temporary compatibility shims where needed.
- Phase 21 wave 3 now has canonical recruit-owned controlling seams under `src/main/java/com/talhanation/bannerlord/entity/**`, `.../ai/**`, `.../persistence/**`, and `.../client/**`, with worker imports able to target bannerlord-owned entity bases, pathfinding, and shared client widgets while old recruit packages remain as brownfield compatibility surfaces during the staged move.
- Phase 21 wave 4 now places worker civilian entities, profession AI, work-area persistence helpers, and worker client flows under `src/main/java/com/talhanation/bannerlord/entity/civilian/**`, `.../ai/civilian/**`, `.../persistence/civilian/**`, and `.../client/civilian/**`, while legacy `workers:*` remaps, structure/build-area NBT migration, and shared-channel helpers live under `src/main/java/com/talhanation/bannerlord/compat/workers/**`.

### GameTest Verification

- Root `gametest` source set is wired in `build.gradle` and now resolves from vendored root paths under `src/gametest/**` only.
- `verifyGameTestStage` depends on `runGameTestServer`.
- Root `gametest` now includes a split BannerMod validation suite under `src/gametest/java/com/talhanation/bannermod/`: `IntegratedRuntimeGameTests.java` for merged runtime smoke, `BannerModOwnershipCycleGameTests.java` for shared-ownership boundaries, `BannerModSettlementLaborGameTests.java` for owned work-area participation plus outsider recovery denial, `BannerModUpkeepFlowGameTests.java` for same-owner supply-to-upkeep transitions, `BannerModPlayerCycleGameTests.java` for one stitched ownership→labor→upkeep→recovery gameplay loop, `BannerModDedicatedServerAuthorityGameTests.java` for offline-owner and unresolved-owner authority denial, `BannerModDedicatedServerReconnectGameTests.java` for reconnect and persistence-safe ownership recovery, `BannerModMultiplayerAuthorityConflictGameTests.java` for live owner-versus-outsider contention, and `BannerModMultiplayerCooperationGameTests.java` for same-team cooperation with outsider regressions.
- The last fully green pre-retirement GameTest baseline was `./gradlew verifyGameTestStage` with 36 required tests before plan 21-05 removed the legacy source-root wiring.

### Current Phase 21 Retirement Gate Status

- `build.gradle` no longer composes Java, test, or resource inputs from `recruits/` or `workers/` source roots.
- The root tree now vendors the recruit and worker resources, retained JUnit suites, and retained recruit GameTest harness assets under `src/**`.
- Validation run from the retired layout on 2026-04-15 produced: `./gradlew processResources` ✅, `./gradlew compileJava` ❌, `./gradlew test` ❌, and `./gradlew verifyGameTestStage` ❌.
- The failing commands all stop at the same retained recruit↔bannerlord compatibility mismatches in moved military/shared classes (for example `AbstractInventoryEntity`, `AbstractRecruitEntity`, `FactionEvents`, and `RecruitEvents`, plus related persistence/client seams). Until those type mismatches are fixed, treat `test` and `verifyGameTestStage` as blocked by the compile step rather than as independently evaluated green gates.

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

When a Phase 21 slice changes pathfinding, persistence, or shared client ownership, also treat `./gradlew verifyGameTestStage` as the preferred follow-up gate once the fast compile/test baseline is available. Those package moves can disturb retained performance and correctness seams proven by `GlobalPathfindingControllerTest`, `AsyncPathProcessorTest`, and the root BannerMod ownership / upkeep / multiplayer GameTests.

When a Phase 21 slice changes worker civilian ownership, also keep the worker-specific regression set explicit: root smoke coverage for worker runtime identity (`BannerModIntegratedRuntimeSmokeTest`, `IntegratedRuntimeGameTests`), worker build/mining serialization helpers (`BuilderBuildProgressSmokeTest`, `StructureTemplateLoaderTest`, `MessageUpdateMiningAreaCodecTest`, `MiningPatternSettingsTest`, `MiningPatternPlanner*Test`, `MiningClaimExcavationRulesTest`), and legacy migration helpers (`WorkersRuntime.migrateLegacyId`, `WorkersRuntime.migrateStructureNbt`, `WorkersLegacyMappings`).

Phase 2 design outputs must preserve the existing migration helpers and compatibility seams documented in `.planning/phases/02-runtime-unification-design/02-runtime-compatibility-contract.md`.

Use `./gradlew runGameTestServer` or `./gradlew verifyGameTestStage` when a slice changes merged gameplay/runtime wiring or needs root-level smoke validation of recruit-plus-worker coexistence. `compileJava`, `processResources`, and `test` remain the default fast stabilization baseline, while GameTest now also covers dedicated ownership, live multiplayer authority contention, same-team cooperation, settlement-labor, upkeep-flow, and stitched player-cycle slices instead of only registry/runtime coexistence.

## Why This Baseline Exists

This merge workspace needs fast verification that the unified root build still compiles, resource merging remains stable, and the known Workers migration helpers do not regress, without promising broader save or integration compatibility than the active contract defines or requiring full Minecraft runtime E2E on every stabilization slice.
