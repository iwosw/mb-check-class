# Phase 25 Slice Status: Settlement Runtime Bring-Up

> Historical status note: this phase file is retained as execution history. All unfinished follow-up work has been consolidated into root `BANNERMOD_BACKLOG.md`; do not use this file as an active task queue.

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
- Added `SettlementOrderWorkGoal` on `AbstractWorkerEntity` so every worker subclass now preempts its legacy zone goal when a claim is present, navigates to the order's target position, and executes HARVEST_CROP / BREAK_BLOCK / MINE_BLOCK / FELL_TREE plus simple placement-style TILL_SOIL / PLANT_CROP / REPLANT_TREE / BUILD_BLOCK actions before reporting completion back to the runtime. BUILD_BLOCK placement resolves the exact target state from the owning live BuildArea rather than guessing from the work-order target.
- Extended focused settlement JUnit coverage to verify candidate derivation and full snapshot persistence round-trip alongside the already-landed settlement seeds, and added `SettlementWorkOrderRuntimeTest` plus `HandlerClaimBehaviorTest` covering publish/claim/release/complete/expiry/purge semantics and handler claim filtering.
- Added a full Manor-Lords-style placement pipeline under `settlement/prefab/`: `BuildingPrefab` + `BuildingPrefabDescriptor` + `BuildingPrefabRegistry` + `BuildingPrefabCatalog` plus 10 concrete procedural prefabs (Farm, LumberCamp, Mine, Pasture, AnimalPen, FishingDock, MarketStall, Storage, House, Barracks) that embed the correct work-area entity for auto-staffing. Placement is driven by `MessageRequestPlaceBuilding` → `BuildingPlacementService`, which spawns a `BuildArea` preloaded with the prefab's STRUCTURE NBT so the existing builder worker executes the build and `spawnScannedEntities` fires the embedded work-area on completion.
- Added the player-facing wand: `BuildingPlacementWandItem` + `PlaceBuildingScreen` GUI. Right-click opens the prefab selector; right-click on a block submits a placement. Shift+right-click toggles to a VALIDATE mode where the player taps two corners + a center of a self-built structure.
- Added auto-staffing: `PrefabAutoStaffingRuntime` hooks into `BuildArea.tick()` completion and spawns the right worker/recruit for the prefab's profession, transfers owner UUID + team, and binds the worker to the freshly spawned work-area.
- Added player-built validation pipeline: `BuildingValidator`, `BuildingInspectionView`, `ArchitectureScorer` (0..100 heuristic: material diversity, decoration, coverage, vertical variance, symmetry, fill balance, cheap-block penalty), `ArchitectureTier` (HOVEL..MAJESTIC), `BuildingValidatorRegistry` with `DefaultBuildingValidator` fallback, and 10 per-prefab validators with real pass/fail rules. `BuildingValidationService` routes the wand's corner+center taps into a bounding box, runs the validator, emits chat feedback (BLOCKER/MAJOR/MINOR/INFO), and grants emerald rewards via `ValidationRewardService` scaled by architecture tier.
- Normalized settlement work-order publisher type matching so live building snapshots using namespaced ids like `bannermod:crop_area` or `bannermod:build_area` now match the crop/build/lumber/mining publishers instead of silently missing the publish seam.
- Added a bounded in-memory work-order execution receipt journal so completed settlement work leaves an observable runtime output after the order is removed from the active queue.
- Tightened growth/project prioritization: supply reservation hints now come only from concrete live reservations, shortage/reservation supply signals score above broad desired-good pressure, growth project ids are stable across ticks for the same profile, and the live project scheduler now keeps queues priority-ordered while retaining the highest-priority entries on overflow.
- Tightened runtime dirty semantics for in-memory/persisted Phase 25 runtimes: work-order restore, project scheduler restore/cancel/poll, home assignment restore/assign, and seller dispatch restore/advance now avoid marking SavedData dirty on identical/no-op state transitions.

## Not Delivered In This Slice

> Canonical tracking now lives in `BANNERMOD_BACKLOG.md` under `SETTLEMENT-*`, `WAR-*`, and `OPS-*`. The list below is historical evidence of what this slice did not ship.

- No persistent resident-runtime state for scheduler tasks, seller phases, household assignments, project queues, or work-order claims yet; the new runtime seams are still in-memory only.
- WATER_FIELD and specialist work-order types still fall through to legacy profession goals. BUILD_BLOCK execution is live only while the owning BuildArea can still resolve the target blueprint state; no standalone persisted block-state payload exists on SettlementWorkOrder yet.
- HAUL_RESOURCE / FETCH_INPUT remain analysis-only; they are not claimed/executed yet because the current work-order payload cannot safely carry source/destination/count/filter and the live courier-task adapter is still missing.
- Proposed settlement refresh hooks for prefab auto-staffing, worker death, container placement, and creative work-area discard remain deferred until GameTest or equivalent live-world coverage exists.
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
- `./gradlew compileJava test --tests com.talhanation.bannermod.settlement.workorder.SettlementWorkOrderPublisherRegistryTest --tests com.talhanation.bannermod.settlement.BannerModSettlementOrchestratorTest --console=plain` succeeded on 2026-04-24 after fixing namespaced building-type matching for the live work-order publishers.
- `git diff --check`, `./gradlew compileJava --console=plain`, and `./gradlew test --tests com.talhanation.bannermod.settlement.workorder.HandlerClaimBehaviorTest --tests com.talhanation.bannermod.settlement.workorder.SettlementWorkOrderRuntimeTest --tests com.talhanation.bannermod.settlement.workorder.SettlementWorkOrderPublisherRegistryTest --console=plain` succeeded on 2026-04-25 after enabling BuildJobHandler to claim BUILD_BLOCK again and adding BuildArea-state-backed BUILD_BLOCK execution in SettlementOrderWorkGoal.
- `./gradlew test --tests com.talhanation.bannermod.settlement.workorder.SettlementWorkOrderRuntimeTest --tests com.talhanation.bannermod.settlement.workorder.HandlerClaimBehaviorTest --tests com.talhanation.bannermod.settlement.growth.BannerModSettlementGrowthManagerTest --tests com.talhanation.bannermod.settlement.project.BannerModSettlementProjectSchedulerTest --tests com.talhanation.bannermod.settlement.BannerModSettlementServiceTest --console=plain` and `./gradlew compileJava --console=plain` succeeded on 2026-04-25 after the larger receipt/growth/project-priority batch.
- `./gradlew test --tests com.talhanation.bannermod.settlement.workorder.SettlementWorkOrderRuntimeTest --tests com.talhanation.bannermod.settlement.project.BannerModSettlementProjectSchedulerTest --tests com.talhanation.bannermod.settlement.household.BannerModHomeAssignmentRuntimeTest --tests com.talhanation.bannermod.settlement.dispatch.BannerModSellerDispatchRuntimeTest --console=plain` and `./gradlew compileJava --console=plain` succeeded on 2026-04-25 after the runtime dirty-semantics cleanup batch.

## Later Confirmed Closeouts

- Governor heartbeat accounting now derives live worker/recruit supply-upkeep state, and governor UI surfaces the persisted fiscal rollup instead of leaving it server-only.
- Controlled-worker settlement semantics were tightened for unassigned and missing-building cases.
- Settlement heuristics now prefer live sea-trade entrypoint sets where available, and targeted refresh hooks now run on storage updates plus worker work-area binding changes.
- Enum-load hardening improved for several resident and service seeds, but remaining raw `Enum.valueOf(...)` persistence paths mean this closeout is only partial today.

## Current Truth

- Phase 25 has moved past seed-only persistence work into runtime bring-up.
- The current runtime bring-up is real and test-backed, but still intentionally additive and partial.
- Millenaire-like settlement simulation is being built in slices on top of the existing BannerMod aggregate; it is not finished, but it is actively in flight in the main tree.
- The live work-order publish seam no longer depends on bare un-namespaced building ids; namespaced building records from `BannerModSettlementService.collectBuildings(...)` now route into the matching publishers correctly.
