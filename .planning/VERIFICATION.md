# Root Verification Baseline

## What Exists Today

### Build Verification

- `compileJava` compiles only the active root source set: `src/main/java`.
- `processResources` packages the active root resources from `src/main/resources` and `src/generated/resources` into the single shipped artifact.
- `verifyBuildStage` wraps the build stage used by `check`.

### Unit / Regression Verification

- `test` runs root JUnit 5 coverage from `src/test/java`.
- Current root smoke/regression coverage includes:
  - BannerMod runtime identity plus worker subsystem seam alignment in one root merged-runtime smoke test
  - shared BannerMod authority vocabulary for inspect, modify, create, and recover-control decisions
  - shared BannerMod config-file taxonomy and legacy-file migration guardrails for military, settlement, and client surfaces
  - shared BannerMod supply-status vocabulary for build-material pressure, worker storage blockage, and recruit upkeep food/payment pressure
  - merged Workers runtime identity and asset-path helpers
  - merged Workers builder-progress flow helpers
  - worker-origin regression coverage now vendored into the root `src/test/java` tree
  - legacy `workers:*` id migration for known structure/build NBT paths
  - build-area authoring and cleanup-policy seams that fence merged worker mutation and legacy update-check registration
- `verifyUnitTestStage` wraps the unit test stage used by `check`.
- Current compact-Phase-25 settlement aggregate verification is `./gradlew test --tests com.talhanation.bannermod.settlement.BannerModSettlementManagerTest --tests com.talhanation.bannermod.settlement.BannerModSettlementResidentRecordTest --tests com.talhanation.bannermod.settlement.BannerModSettlementServiceTest --console=plain`; it succeeded on 2026-04-19 after the resident job-definition seed slice landed.
- Current compact-Phase-25 settlement runtime bring-up verification is `./gradlew test --tests com.talhanation.bannermod.settlement.goal.BannerModResidentGoalSchedulerTest --tests com.talhanation.bannermod.settlement.growth.BannerModSettlementGrowthManagerTest --tests com.talhanation.bannermod.settlement.dispatch.BannerModSellerDispatchRuntimeTest --tests com.talhanation.bannermod.settlement.household.BannerModHomeAssignmentRuntimeTest --tests com.talhanation.bannermod.settlement.household.HouseholdGoalsTest --tests com.talhanation.bannermod.settlement.project.BannerModBuildAreaProjectBridgeTest --tests com.talhanation.bannermod.settlement.job.JobHandlerRegistryTest --console=plain`; it succeeded on 2026-04-19.
- Latest governance/settlement bugfix closeout verification kept `./gradlew compileJava` green, and `./gradlew test --tests com.talhanation.bannermod.governance.BannerModGovernorHeartbeatTest --tests com.talhanation.bannermod.settlement.BannerModSettlementServiceTest --tests com.talhanation.bannermod.settlement.BannerModSettlementResidentRecordTest --console=plain` remained green after the helper overload fix in `BannerModSettlementService.summarizeStockpiles(...)`.
- Latest live-orchestration verification kept `./gradlew compileJava` green, and `./gradlew test --tests com.talhanation.bannermod.settlement.BannerModSettlementOrchestratorTest --tests com.talhanation.bannermod.settlement.goal.BannerModResidentGoalSchedulerTest --tests com.talhanation.bannermod.settlement.project.BannerModBuildAreaProjectBridgeTest --tests com.talhanation.bannermod.settlement.job.JobHandlerRegistryTest --console=plain` succeeded on 2026-04-19.
- Latest reservation-aware Phase 25 integration verification kept `./gradlew compileJava --console=plain` green, and `./gradlew test --tests com.talhanation.bannermod.logistics.BannerModLogisticsServiceTest --tests com.talhanation.bannermod.settlement.BannerModSettlementServiceTest --tests com.talhanation.bannermod.settlement.BannerModSettlementOrchestratorTest --tests com.talhanation.bannermod.settlement.BannerModSettlementManagerTest --console=plain` succeeded on 2026-04-19.
- Latest Phase 25 runtime-wiring verification kept `./gradlew compileJava --console=plain` green, and `./gradlew test --tests com.talhanation.bannermod.settlement.growth.BannerModSettlementGrowthManagerTest --tests com.talhanation.bannermod.settlement.BannerModSettlementOrchestratorTest --console=plain` succeeded on 2026-04-20 after wiring reservation-aware growth hints, broader refresh hooks, and scheduled job gating.
- Building-centric migration verification kept `./gradlew compileJava --console=plain` green and `./gradlew test --console=plain` green on 2026-04-20 after landing the work-order runtime, crop/build/lumber/mining publishers, real claim-behavior job handlers, orchestrator publish wiring, and the `SettlementOrderWorkGoal` preemption goal on `AbstractWorkerEntity`; new focused tests live at `src/test/java/com/talhanation/bannermod/settlement/workorder/`.
- Compact Phase 26 combat AI verification kept `./gradlew compileJava --console=plain` and `./gradlew compileTestJava --console=plain` green, and `./gradlew test --tests "com.talhanation.bannermod.ai.military.*" --console=plain` succeeded on 2026-04-21 across 16 suites, 129 tests, 0 failures, 0 errors. Covered suites: `CombatLeashPolicyTest` 10, `FormationGapFillPolicyTest` 8, `FormationSlotRegistryTest` 7, `FormationYawPolicyTest` 9, `ShieldBlockGeometryTest` 9, `ShieldMitigationTest` 9, `WeaponReachTest` 7, `FriendlyLineOfSightTest` 8, `AttackCadenceTest` 7, `FacingHitZoneTest` 10, `FlankDamageTest` 4, `FormationCohesionTest` 9, `BraceAgainstChargePolicyTest` 10, `UnitTypeMatchupTest` 15, `FormationTargetSelectionControllerTest` 4, and `controller.BattleTacticDeciderTest` 3.
- Compact Phase 26 stance-command follow-up verification was green on 2026-04-22: `./gradlew compileJava compileTestJava compileGameTestJava --console=plain`, `./gradlew test --tests com.talhanation.bannermod.army.command.CommandIntentShapeTest --console=plain`, and `./gradlew verifyGameTestStage --console=plain` all passed after adding `BannerModCombatStanceCommandGameTests` for group-authority stance targeting plus ranged-goal leash behavior.
- Latest rerun on 2026-04-24 kept `./gradlew compileJava compileTestJava compileGameTestJava --console=plain` green and `./gradlew verifyGameTestStage --console=plain` green after adding synced per-recruit stance control in `RecruitInventoryScreen` plus `MessageCombatStanceGui`. Full GameTest stage passed with 39 required tests.
- Latest Phase 25 work-order seam fix kept `./gradlew compileJava test --tests com.talhanation.bannermod.settlement.workorder.SettlementWorkOrderPublisherRegistryTest --tests com.talhanation.bannermod.settlement.BannerModSettlementOrchestratorTest --console=plain` green on 2026-04-24 after normalizing namespaced building-type matching for the crop/build/lumber/mining work-order publishers.
- Latest Phase 25 BUILD_BLOCK execution follow-up kept `git diff --check`, `./gradlew compileJava --console=plain`, and `./gradlew test --tests com.talhanation.bannermod.settlement.workorder.HandlerClaimBehaviorTest --tests com.talhanation.bannermod.settlement.workorder.SettlementWorkOrderRuntimeTest --tests com.talhanation.bannermod.settlement.workorder.SettlementWorkOrderPublisherRegistryTest --console=plain` green on 2026-04-25 after BuildJobHandler claim support was restored and SettlementOrderWorkGoal began resolving construction placement state from the owning live BuildArea.
- Latest larger Phase 25 settlement batch kept `./gradlew test --tests com.talhanation.bannermod.settlement.workorder.SettlementWorkOrderRuntimeTest --tests com.talhanation.bannermod.settlement.workorder.HandlerClaimBehaviorTest --tests com.talhanation.bannermod.settlement.growth.BannerModSettlementGrowthManagerTest --tests com.talhanation.bannermod.settlement.project.BannerModSettlementProjectSchedulerTest --tests com.talhanation.bannermod.settlement.BannerModSettlementServiceTest --console=plain` and `./gradlew compileJava --console=plain` green on 2026-04-25 after adding work-order completion receipts, concrete reservation-only supply hints, shortage/reservation growth scoring, stable growth project IDs, and priority-ordered project queues.
- Latest Phase 25 persistence-churn cleanup kept `./gradlew test --tests com.talhanation.bannermod.settlement.workorder.SettlementWorkOrderRuntimeTest --tests com.talhanation.bannermod.settlement.project.BannerModSettlementProjectSchedulerTest --tests com.talhanation.bannermod.settlement.household.BannerModHomeAssignmentRuntimeTest --tests com.talhanation.bannermod.settlement.dispatch.BannerModSellerDispatchRuntimeTest --console=plain` and `./gradlew compileJava --console=plain` green on 2026-04-25 after suppressing dirty marks for identical/no-op restore, assign, advance, cancel, and empty-queue transitions in the Phase 25 runtimes.
- Latest Phase 26 battle-realism follow-up kept `git diff --check`, `./gradlew test --tests com.talhanation.bannermod.ai.military.WeaponReachTest --tests com.talhanation.bannermod.ai.military.AttackCadenceTest --console=plain`, and `./gradlew compileJava --console=plain` green on 2026-04-25 after adding common polearm/lance id aliases to the reach and cadence resolver.
- Latest Phase 26 Better Combat reach-metadata follow-up kept `git diff --check`, `./gradlew compileJava --console=plain`, and `./gradlew test --tests com.talhanation.bannermod.compat.BetterCombatWeaponAttributesTest --tests com.talhanation.bannermod.ai.military.WeaponReachTest --tests com.talhanation.bannermod.ai.military.AttackCadenceTest --console=plain` green on 2026-04-25. One first parallel targeted-test attempt failed in `compileJava` with a local Gradle incremental output `NoSuchFileException`; the standalone rerun passed.
- Latest Phase 26 native Better Combat server-flow follow-up kept `./gradlew compileJava processResources --console=plain` and `./gradlew test --tests com.talhanation.bannermod.compat.BetterCombatAttackBridgeTest --tests com.talhanation.bannermod.compat.BetterCombatWeaponAttributesTest --tests com.talhanation.bannermod.ai.military.WeaponReachTest --tests com.talhanation.bannermod.ai.military.AttackCadenceTest --console=plain` green on 2026-04-25 after removing the Better Mob Combat adapter/dependency and routing recruit melee through a BannerMod-owned Better Combat upswing/damage-multiplier bridge.
- Latest Phase 26 Better Combat AOE/presentation follow-up kept `./gradlew compileJava processResources --console=plain` and `./gradlew test --tests com.talhanation.bannermod.compat.BetterCombatAttackBridgeTest --tests com.talhanation.bannermod.compat.BetterCombatWeaponAttributesTest --tests com.talhanation.bannermod.ai.military.WeaponReachTest --tests com.talhanation.bannermod.ai.military.AttackCadenceTest --console=plain` green on 2026-04-25 after adding server-authoritative Better Combat-style secondary arc hits plus synced recruit attack pose presentation. `./gradlew verifyGameTestStage --console=plain` with Better Combat in the dev runtime started the GameTest server and loaded Better Combat, but failed on existing required GameTest `reconnectedownerrecoversauthorityafterownershiproundtrip`; no BMC or Better Combat mixin crash occurred in that live smoke.

### GameTest Verification

- Root `gametest` source set is wired in `build.gradle` and exposed through `runGameTestServer`.
- `verifyGameTestStage` depends on `runGameTestServer`.
- Root `gametest` now includes a split BannerMod validation suite under `src/gametest/java/com/talhanation/bannermod/`: `IntegratedRuntimeGameTests.java` for merged runtime smoke, `BannerModOwnershipCycleGameTests.java` for shared-ownership boundaries, `BannerModSettlementLaborGameTests.java` for owned work-area participation plus outsider recovery denial, `BannerModUpkeepFlowGameTests.java` for same-owner supply-to-upkeep transitions, `BannerModPlayerCycleGameTests.java` for one stitched ownership→labor→upkeep→recovery gameplay loop, `BannerModDedicatedServerAuthorityGameTests.java` for offline-owner and unresolved-owner authority denial, `BannerModDedicatedServerReconnectGameTests.java` for reconnect and persistence-safe ownership recovery, `BannerModMultiplayerAuthorityConflictGameTests.java` for live owner-versus-outsider contention, and `BannerModMultiplayerCooperationGameTests.java` for same-team cooperation with outsider regressions.
- `./gradlew verifyGameTestStage` was green on 2026-04-24 with 39 required tests after the per-recruit stance-control follow-up. A later 2026-04-25 Better Combat-present smoke started the GameTest server and loaded Better Combat, but failed on the existing required `reconnectedownerrecoversauthorityafterownershiproundtrip` GameTest; do not claim the current dirty tree is full-GameTest green until that is fixed or quarantined and rerun.

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

## Source-of-Truth Reminder

- Active build truth is root `src/**` plus root `build.gradle`.
- `recruits/` and `workers/` are retained archive/reference trees, not active build inputs.
- If docs disagree with code, trust `build.gradle`, `src/main/java/com/talhanation/bannermod/bootstrap/BannerModMain.java`, and the root `src/**` tree.
