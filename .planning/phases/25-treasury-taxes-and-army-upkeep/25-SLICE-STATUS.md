# Phase 25 Slice Status: Settlement Runtime Bring-Up

## Scope

Compact Phase 25 is no longer only the persistence-seed bridge. The main tree now contains the original settlement aggregate seeds plus the first additive Millenaire-like runtime slices for resident scheduling, growth scoring, project queuing, seller dispatch, household/home assignment, and job-handler registration.

## Status

In progress. The seed layer is landed and several first-pass runtime packages now exist in `src/main/java/com/talhanation/bannermod/settlement/{goal,growth,project,dispatch,household,job}/`, but they are still partial runtime seams rather than a full end-to-end resident simulation.

## Delivered

- Added persisted `BannerModSettlementProjectCandidateSeed` so settlement snapshots now carry one compact candidate id, target building-profile hint, priority, governance/claim readiness flags, and stable driver ids.
- Updated `BannerModSettlementService` to derive `projectCandidateSeed` from existing building-profile counts, stockpile presence, desired-goods pressure, market readiness, and governor/claim settlement seams.
- Added `settlement.goal.BannerModResidentGoalScheduler` plus stock `Idle`, `Rest`, `Work`, `Socialise`, `Deliver`, and `Fetch` goals as the first resident-side scheduler seam over the already-landed `schedulePolicy` and `scheduleWindowSeed` records.
- Added `settlement.growth.BannerModSettlementGrowthManager` plus `PendingProject` scoring so settlements can derive a deterministic growth queue from the existing project/desire/market/building seeds.
- Added `settlement.project.BannerModSettlementProjectRuntime` plus scheduler/bridge seams so growth output can flow into a bounded project queue and optional BuildArea assignment bridge.
- Added `settlement.dispatch.BannerModSellerDispatchRuntime` plus advisor/goal seams so seller-dispatch seeds can animate through a bounded `READY -> MOVING_TO_STALL -> AT_STALL -> SELLING -> RETURNING -> RETURNED` runtime state machine.
- Added `settlement.household.BannerModHomeAssignmentRuntime` plus advisor/home goals so residents can hold a bounded in-memory home assignment and day/night home-ward intent.
- Added `settlement.job.JobHandlerRegistry` plus built-in handler stubs so resident job tokens are no longer only passive seeds.
- Added `BannerModSettlementOrchestrator` and wired it into `ClaimEvents` after snapshot refresh so the additive growth/project/home/seller/goal/job runtimes now execute over fresh `BannerModSettlementSnapshot` data on the governor tick.
- Added reservation-aware settlement signal enrichment by folding live authored logistics reservations into snapshot-owned `tradeRouteHandoffSeed` and `supplySignalState`, without introducing a second settlement persistence model.
- Expanded targeted settlement refresh coverage across additional civilian work-area update packets and successful merchant market trades, and made live seller dispatch runtime cancel/rebind stale sellers when the current snapshot seed changes markets.
- Extended `BannerModSettlementGrowthManager` so the live growth queue can consume reservation-aware `tradeRouteHandoffSeed` and `supplySignalState` hints instead of leaving them snapshot-only.
- Expanded targeted settlement refresh coverage again for remaining settlement-critical civilian mutation paths: work-area owner changes, claim worker spawning/seeding, build-area completion, and mining-area self-removal.
- Gated resident job execution behind scheduled work tasks inside `BannerModSettlementOrchestrator` and now honor handler `cooldownTicks()` so jobs no longer fire during home/rest paths or every eligible tick.
- Introduced a building-centric work-order layer under `src/main/java/com/talhanation/bannermod/settlement/workorder/`: `SettlementWorkOrder` record, `SettlementWorkOrderType` / `SettlementWorkOrderStatus` enums, `SettlementWorkOrderRuntime` per-level registry with claim/release/complete/expire/purge semantics, and a publisher interface plus registry.
- Added Manor-Lords-style publishers for all primary work-area kinds — `CropAreaWorkOrderPublisher`, `BuildAreaWorkOrderPublisher`, `LumberAreaWorkOrderPublisher`, `MiningAreaWorkOrderPublisher` — so building records now emit concrete block-level work orders each settlement tick.
- Replaced the stub `HarvestJobHandler` and `BuildJobHandler` implementations with real runtime-claiming handlers keyed to their respective settlement job seeds, so a resident in the work phase now claims a live order from the runtime instead of returning a no-op.
- Extended `BannerModSettlementOrchestrator` to own a per-level `SettlementWorkOrderRuntime` plus a `SettlementWorkOrderPublisherRegistry.defaults()` population, reclaim abandoned claims every tick, and publish fresh orders on every snapshot tick.
- Added `SettlementOrderWorkGoal` on `AbstractWorkerEntity` so every worker subclass now preempts its legacy zone goal when a claim is present, navigates to the order's target position, and executes HARVEST_CROP / BREAK_BLOCK / MINE_BLOCK / FELL_TREE actions before reporting completion back to the runtime.
- Extended focused settlement JUnit coverage to verify candidate derivation and full snapshot persistence round-trip alongside the already-landed settlement seeds, and added `SettlementWorkOrderRuntimeTest` plus `HandlerClaimBehaviorTest` covering publish/claim/release/complete/expiry/purge semantics and handler claim filtering.
- Added a full Manor-Lords-style placement pipeline under `settlement/prefab/`: `BuildingPrefab` + `BuildingPrefabDescriptor` + `BuildingPrefabRegistry` + `BuildingPrefabCatalog` plus 10 concrete procedural prefabs (Farm, LumberCamp, Mine, Pasture, AnimalPen, FishingDock, MarketStall, Storage, House, Barracks) that embed the correct work-area entity for auto-staffing. Placement is driven by `MessageRequestPlaceBuilding` → `BuildingPlacementService`, which spawns a `BuildArea` preloaded with the prefab's STRUCTURE NBT so the existing builder worker executes the build and `spawnScannedEntities` fires the embedded work-area on completion.
- Added the player-facing wand: `BuildingPlacementWandItem` + `PlaceBuildingScreen` GUI. Right-click opens the prefab selector; right-click on a block submits a placement. Shift+right-click toggles to a VALIDATE mode where the player taps two corners + a center of a self-built structure.
- Added auto-staffing: `PrefabAutoStaffingRuntime` hooks into `BuildArea.tick()` completion and spawns the right worker/recruit for the prefab's profession, transfers owner UUID + team, and binds the worker to the freshly spawned work-area.
- Added player-built validation pipeline: `BuildingValidator`, `BuildingInspectionView`, `ArchitectureScorer` (0..100 heuristic: material diversity, decoration, coverage, vertical variance, symmetry, fill balance, cheap-block penalty), `ArchitectureTier` (HOVEL..MAJESTIC), `BuildingValidatorRegistry` with `DefaultBuildingValidator` fallback, and 10 per-prefab validators with real pass/fail rules. `BuildingValidationService` routes the wand's corner+center taps into a bounding box, runs the validator, emits chat feedback (BLOCKER/MAJOR/MINOR/INFO), and grants emerald rewards via `ValidationRewardService` scaled by architecture tier.

## Not Delivered In This Slice

- No persistent resident-runtime state for scheduler tasks, seller phases, household assignments, project queues, or work-order claims yet; the new runtime seams are still in-memory only.
- The `SettlementOrderWorkGoal` currently executes only break/mine style orders (HARVEST_CROP, BREAK_BLOCK, MINE_BLOCK, FELL_TREE). Placement-style orders (PLANT_CROP, BUILD_BLOCK, REPLANT_TREE, TILL_SOIL, WATER_FIELD) still fall through to the legacy zone-driven profession goals.
- No real construction execution, building upgrade lifecycle, household economy, inter-settlement trade, or settlement culture/reputation system yet.
- No broad civil-path integration yet beyond the governor tick seam; `BannerModSettlementService` still seeds the aggregate, and the new runtime packages are not yet orchestrated from every important civil mutation or a dedicated settlement runtime loop.
- Legacy profession goals (`FarmerWorkGoal`, `BuilderWorkGoal`, `LumberjackWorkGoal`, `MinerWorkGoal`, ...) still exist as compatibility fallback and still own the full zone-scanning / state-machine logic when no settlement-published claim is available.

## Verification Notes

- `./gradlew test --tests com.talhanation.bannermod.settlement.BannerModSettlementManagerTest --tests com.talhanation.bannermod.settlement.BannerModSettlementResidentRecordTest --tests com.talhanation.bannermod.settlement.BannerModSettlementServiceTest --console=plain` succeeded on 2026-04-19.
- `./gradlew test --tests com.talhanation.bannermod.settlement.goal.BannerModResidentGoalSchedulerTest --tests com.talhanation.bannermod.settlement.growth.BannerModSettlementGrowthManagerTest --tests com.talhanation.bannermod.settlement.dispatch.BannerModSellerDispatchRuntimeTest --tests com.talhanation.bannermod.settlement.household.BannerModHomeAssignmentRuntimeTest --tests com.talhanation.bannermod.settlement.household.HouseholdGoalsTest --tests com.talhanation.bannermod.settlement.project.BannerModBuildAreaProjectBridgeTest --tests com.talhanation.bannermod.settlement.job.JobHandlerRegistryTest --console=plain` succeeded on 2026-04-19.
- `./gradlew compileJava` succeeded on 2026-04-19 with the runtime bring-up packages present in the main tree.
- `./gradlew test --tests com.talhanation.bannermod.settlement.BannerModSettlementOrchestratorTest --tests com.talhanation.bannermod.settlement.goal.BannerModResidentGoalSchedulerTest --tests com.talhanation.bannermod.settlement.project.BannerModBuildAreaProjectBridgeTest --tests com.talhanation.bannermod.settlement.job.JobHandlerRegistryTest --console=plain` succeeded on 2026-04-19 after wiring the live orchestration seam.
- `./gradlew test --tests com.talhanation.bannermod.logistics.BannerModLogisticsServiceTest --tests com.talhanation.bannermod.settlement.BannerModSettlementServiceTest --tests com.talhanation.bannermod.settlement.BannerModSettlementOrchestratorTest --tests com.talhanation.bannermod.settlement.BannerModSettlementManagerTest --console=plain` succeeded on 2026-04-19 after adding reservation-aware signalling and broader civil refresh hooks.
- `./gradlew compileJava --console=plain` succeeded on 2026-04-20 after wiring reservation-aware growth hints, broader refresh hooks, and scheduled job gating.
- `./gradlew test --tests com.talhanation.bannermod.settlement.growth.BannerModSettlementGrowthManagerTest --tests com.talhanation.bannermod.settlement.BannerModSettlementOrchestratorTest --console=plain` succeeded on 2026-04-20.
- `./gradlew compileJava --console=plain` and `./gradlew test --console=plain` (full suite) succeeded on 2026-04-20 after the building-centric work-order layer landed: runtime, publishers, real claim-behavior job handlers, orchestrator publish wiring, and `SettlementOrderWorkGoal` on `AbstractWorkerEntity`.

## Later Confirmed Closeouts

- Governor heartbeat accounting now derives live worker/recruit supply-upkeep state, and governor UI surfaces the persisted fiscal rollup instead of leaving it server-only.
- Controlled-worker settlement semantics were tightened for unassigned and missing-building cases.
- Settlement heuristics now prefer live sea-trade entrypoint sets where available, and targeted refresh hooks now run on storage updates plus worker work-area binding changes.
- Enum-load hardening improved for several resident and service seeds, but remaining raw `Enum.valueOf(...)` persistence paths mean this closeout is only partial today.

## Current Truth

- Phase 25 has moved past seed-only persistence work into runtime bring-up.
- The current runtime bring-up is real and test-backed, but still intentionally additive and partial.
- Millenaire-like settlement simulation is being built in slices on top of the existing BannerMod aggregate; it is not finished, but it is actively in flight in the main tree.
