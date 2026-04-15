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
  - explicit worker mining settings contract plus mining-area packet/state codec coverage for tunnel and branch modes
  - pure worker mining planner coverage for diagonal tunnel geometry and deterministic branch/strip patterns
  - unified worker structure-template loader coverage for `.nbt`, `.schem`, and `.schematic` import through one BuildArea contract
  - merged Workers runtime identity and asset-path helpers
  - merged Workers builder-progress flow helpers
  - retained Workers JUnit suites under `workers/src/test/java`
  - legacy `workers:*` id migration for known structure/build NBT paths
  - build-area authoring and cleanup-policy seams that fence merged worker mutation and legacy update-check registration
- `verifyUnitTestStage` wraps the unit test stage used by `check`.
- Phase 21 wave 1 now has canonical shared seam homes under `src/main/java/com/talhanation/bannerlord/shared/**` and `src/main/java/com/talhanation/bannerlord/config/**`, while `com.talhanation.bannermod/**` remains as a temporary forwarding surface during import churn.
- Phase 21 wave 2 now keeps the one live `@Mod` entrypoint, shared `SimpleChannel`, and registry/lifecycle composition under `src/main/java/com/talhanation/bannerlord/bootstrap/**`, `.../network/**`, and `.../registry/**`, with recruit and worker package surfaces reduced to temporary compatibility shims where needed.

### Phase 29 Validation

- `./gradlew test --tests com.talhanation.workers.MiningPatternSettingsTest --tests com.talhanation.workers.MessageUpdateMiningAreaCodecTest --tests com.talhanation.workers.MiningPatternPlannerTunnelTest --tests com.talhanation.workers.MiningPatternPlannerBranchTest --tests com.talhanation.workers.StructureTemplateLoaderTest --console=plain` passed during the 2026-04-12 Phase 29 closeout run.
- `./gradlew compileJava --console=plain` also passed during the 2026-04-12 Phase 29 closeout run.

### GameTest Verification

- Root `gametest` source set is wired in `build.gradle` and exposed through `runGameTestServer`.
- `verifyGameTestStage` depends on `runGameTestServer`.
- Root `gametest` now includes a split BannerMod validation suite under `src/gametest/java/com/talhanation/bannermod/`: `IntegratedRuntimeGameTests.java` for merged runtime smoke, `BannerModOwnershipCycleGameTests.java` for shared-ownership boundaries, `BannerModSettlementLaborGameTests.java` for owned work-area participation plus outsider recovery denial, `BannerModSettlementFactionEnforcementGameTests.java` for friendly settlement placement plus hostile or unclaimed denial, `BannerModSettlementFactionDegradationGameTests.java` for claim-loss and faction-mismatch degradation without silent ownership transfer, `BannerModUpkeepFlowGameTests.java` for same-owner supply-to-upkeep transitions, `BannerModPlayerCycleGameTests.java` for one stitched ownership→labor→upkeep→recovery gameplay loop, `BannerModDedicatedServerAuthorityGameTests.java` for offline-owner and unresolved-owner authority denial, `BannerModDedicatedServerReconnectGameTests.java` for reconnect and persistence-safe ownership recovery, `BannerModMultiplayerAuthorityConflictGameTests.java` for live owner-versus-outsider contention, and `BannerModMultiplayerCooperationGameTests.java` for same-team cooperation with outsider regressions.
- `./gradlew verifyGameTestStage` is currently green in this workspace, including the retained dense-battle profiling snapshots, the Phase 16 async reliability accounting assertions, and the Phase 17 AI LOD run/skip/tier telemetry checks.

### Phase 11 Profiling Baseline

- Phase 11 baseline definition now lives in `.planning/phases/11-large-battle-ai-profiling-baseline/11-PROFILING-BASELINE.md`.
- Baseline capture procedure and acceptance rules now live in `.planning/phases/11-large-battle-ai-profiling-baseline/11-VALIDATION.md`.
- Plan 11-01 defines the canonical large-battle scenarios, hotspot counters, and evidence bundle shape rooted in the existing recruit battle fixtures and async seams.
- Plan 11-02 added the current resettable target-search and async-path counters plus dense-battle GameTest snapshot capture, so Phase 11 evidence can now be collected directly from the live stress harness.

### Phase 19 Large-Battle Performance Validation

- Phase 19 validation procedure now lives in `.planning/phases/19-large-battle-performance-validation/19-VALIDATION.md`.
- Final closeout report now lives in `.planning/phases/19-large-battle-performance-validation/19-RESULTS.md`.
- Accepted optimized-runtime evidence bundles now live under `.planning/phases/19-large-battle-performance-validation/evidence/`.
- `./gradlew test --tests com.talhanation.recruits.pathfinding.GlobalPathfindingControllerTest --console=plain` passed during the 2026-04-12 closeout run.
- `./gradlew compileGameTestJava verifyGameTestStage --console=plain` also passed during that run with all `46` required GameTests green; two optional GameTests still failed outside the Phase 19 acceptance gate.
- Caveat: no Phase 11 raw evidence bundles or external-profiler captures are stored in this workspace, so Phase 19 comparison is strongest on retained scenario/counter continuity and weaker on numeric before/after tick-cost deltas.

## Recommended Default Validation For Stabilization Slices

Run from the repository root:

1. `./gradlew compileJava`
2. `./gradlew processResources`
3. `./gradlew test`

Phase 2 design outputs must preserve the existing migration helpers and compatibility seams documented in `.planning/phases/02-runtime-unification-design/02-runtime-compatibility-contract.md`.

Use `./gradlew runGameTestServer` or `./gradlew verifyGameTestStage` when a slice changes merged gameplay/runtime wiring or needs root-level smoke validation of recruit-plus-worker coexistence. `compileJava`, `processResources`, and `test` remain the default fast stabilization baseline, while GameTest now also covers dedicated ownership, live multiplayer authority contention, same-team cooperation, settlement-labor, upkeep-flow, and stitched player-cycle slices instead of only registry/runtime coexistence.

## Why This Baseline Exists

This merge workspace needs fast verification that the unified root build still compiles, resource merging remains stable, and the known Workers migration helpers do not regress, without promising broader save or integration compatibility than the active contract defines or requiring full Minecraft runtime E2E on every stabilization slice.
