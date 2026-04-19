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
- Extended focused settlement JUnit coverage to verify candidate derivation and full snapshot persistence round-trip alongside the already-landed settlement seeds.

## Not Delivered In This Slice

- No persistent resident-runtime state for scheduler tasks, seller phases, household assignments, or project queues yet; the new runtime seams are still in-memory only.
- No fully wired resident AI loop yet; current worker gameplay is not replaced by the new scheduler/job/home/seller slices.
- No real construction execution, building upgrade lifecycle, household economy, inter-settlement trade, or settlement culture/reputation system yet.
- No broad civil-path integration yet beyond the governor tick seam; `BannerModSettlementService` still seeds the aggregate, and the new runtime packages are not yet orchestrated from every important civil mutation or a dedicated settlement runtime loop.

## Verification Notes

- `./gradlew test --tests com.talhanation.bannermod.settlement.BannerModSettlementManagerTest --tests com.talhanation.bannermod.settlement.BannerModSettlementResidentRecordTest --tests com.talhanation.bannermod.settlement.BannerModSettlementServiceTest --console=plain` succeeded on 2026-04-19.
- `./gradlew test --tests com.talhanation.bannermod.settlement.goal.BannerModResidentGoalSchedulerTest --tests com.talhanation.bannermod.settlement.growth.BannerModSettlementGrowthManagerTest --tests com.talhanation.bannermod.settlement.dispatch.BannerModSellerDispatchRuntimeTest --tests com.talhanation.bannermod.settlement.household.BannerModHomeAssignmentRuntimeTest --tests com.talhanation.bannermod.settlement.household.HouseholdGoalsTest --tests com.talhanation.bannermod.settlement.project.BannerModBuildAreaProjectBridgeTest --tests com.talhanation.bannermod.settlement.job.JobHandlerRegistryTest --console=plain` succeeded on 2026-04-19.
- `./gradlew compileJava` succeeded on 2026-04-19 with the runtime bring-up packages present in the main tree.
- `./gradlew test --tests com.talhanation.bannermod.settlement.BannerModSettlementOrchestratorTest --tests com.talhanation.bannermod.settlement.goal.BannerModResidentGoalSchedulerTest --tests com.talhanation.bannermod.settlement.project.BannerModBuildAreaProjectBridgeTest --tests com.talhanation.bannermod.settlement.job.JobHandlerRegistryTest --console=plain` succeeded on 2026-04-19 after wiring the live orchestration seam.
- `./gradlew test --tests com.talhanation.bannermod.logistics.BannerModLogisticsServiceTest --tests com.talhanation.bannermod.settlement.BannerModSettlementServiceTest --tests com.talhanation.bannermod.settlement.BannerModSettlementOrchestratorTest --tests com.talhanation.bannermod.settlement.BannerModSettlementManagerTest --console=plain` succeeded on 2026-04-19 after adding reservation-aware signalling and broader civil refresh hooks.
- `./gradlew compileJava --console=plain` succeeded on 2026-04-20 after wiring reservation-aware growth hints, broader refresh hooks, and scheduled job gating.
- `./gradlew test --tests com.talhanation.bannermod.settlement.growth.BannerModSettlementGrowthManagerTest --tests com.talhanation.bannermod.settlement.BannerModSettlementOrchestratorTest --console=plain` succeeded on 2026-04-20.

## Later Confirmed Closeouts

- Governor heartbeat accounting now derives live worker/recruit supply-upkeep state, and governor UI surfaces the persisted fiscal rollup instead of leaving it server-only.
- Controlled-worker settlement semantics were tightened for unassigned and missing-building cases.
- Settlement heuristics now prefer live sea-trade entrypoint sets where available, and targeted refresh hooks now run on storage updates plus worker work-area binding changes.
- Enum-load hardening improved for several resident and service seeds, but remaining raw `Enum.valueOf(...)` persistence paths mean this closeout is only partial today.

## Current Truth

- Phase 25 has moved past seed-only persistence work into runtime bring-up.
- The current runtime bring-up is real and test-backed, but still intentionally additive and partial.
- Millenaire-like settlement simulation is being built in slices on top of the existing BannerMod aggregate; it is not finished, but it is actively in flight in the main tree.
