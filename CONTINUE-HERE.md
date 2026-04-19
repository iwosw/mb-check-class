# Continue Here

## Primary Prompt For Next Session

Use this prompt to continue work in a fresh session:

```md
Continue work in `/home/kaiserroman/bannermod` from `CONTINUE-HERE.md`.

Critical workflow rules:
- First read `CONTINUE-HERE.md` fully and treat it as the active handoff.
- Use subagents first for every non-trivial task.
- For implementation work, the default flow is: `subagent plans + edits -> main agent reviews changed files -> main agent runs verification -> only then update planning/docs truthfully`.
- Do not trust a subagent's "done" status by itself. Always verify its code in the main session before merging, summarizing, or marking a slice complete.
- When running multiple independent tasks, run them in parallel.
- If multiple subagents may touch the same files, DO NOT run them in the same worktree. Create separate `git worktree` copies first and only merge/reapply changes after review.
- Do not use direct GSD slash commands. Emulate GSD workflow manually: `plan -> execute -> verify -> update planning truthfully`.
- For active planning work, operate on `.planning/**` directly.
- For refactor or stabilization work, the default fast gate is `./gradlew compileJava`.
- Use hard timeouts on Gradle commands. If Gradle stalls on daemon/configure/dependency fetching, treat that as external stall and continue instead of blocking.
- Do not block on `runGameTestServer` unless explicitly needed.
- If you update planning artifacts, keep them truthful. Never mark a slice complete unless verification actually supports it.

Immediate objectives, in order:
1. Update `.planning` to reflect the latest confirmed 24-25 bugfix closeouts if not already done.
2. Resume compact Phase 25 execution in batches of 5 slices at a time, using subagents in parallel.
3. Audit historical phases 1-24 against the current codebase and fix any real implementation/planning mismatches you find.

When executing Phase 25 slices:
- Group 5 independent slices per batch.
- For each slice, have a subagent do the implementation work first, then in the main session explicitly review the produced files and rerun verification yourself before touching `.planning`.
- After each batch, run aggregate verification and normalize `.planning` if any subagent overwrote shared planning files incorrectly.

When multiple phase slices touch the same settlement/planning files:
- Create separate git worktrees under `/home/kaiserroman/worktrees/`.
- Run those subagents inside separate worktrees.
- Review outputs before applying/merging back to main tree.

Always preserve these constraints:
- Active code is root `src/**`.
- Active planning root is `.planning/**`.
- Legacy `recruits/` and `workers/` trees are reference only.
- Prefer a Bannerlord-like runtime experience: server-authoritative command/state seams, less ad hoc glue, clearer army/settlement/economy/politics layers.
```

## Session Snapshot

- Repo: `/home/kaiserroman/bannermod`
- Current main-tree HEAD commit: `329edf1`
- HEAD commit message: `refactor(runtime): split legacy systems and wire settlement loop`
- There is one newer verified but still-uncommitted Phase 25 settlement slice on top of that commit.
- Existing refactor commits already created earlier in session:
  - `cc986b4` `refactor(recruits): split recruit runtime and formation commands`
  - `009df5f` `refactor(events): extract claim faction and villager runtimes`
  - `f459ae6` `refactor(shared): remove legacy forwarders and worker glue`
  - `329edf1` `refactor(runtime): split legacy systems and wire settlement loop`

## Absolute Truths

- Active runtime/build root is `src/**`.
- Active planning root is `.planning/**`.
- Root build is the only build that matters.
- Legacy `recruits/` and `workers/` directories are archive/reference trees only.
- Do not trust old planning text over real code.

## Critical Workflow Lessons From This Session

### 1. Do not let parallel subagents touch the same files in one worktree

This caused real damage/races in this session.

Observed problem:
- Parallel batch-6 subagents touched the same settlement/planning files.
- They overwrote each other and produced misleading planning status updates.
- At least one shared file, `.planning/phases/25-treasury-taxes-and-army-upkeep/25-SLICE-STATUS.md`, was overwritten by the last finisher rather than reflecting all slices.

Rule now:
- If two agents might touch the same files, create isolated worktrees first.
- Worktrees used in this session:
  - `/home/kaiserroman/worktrees/bannermod-b6-jobtarget`
  - `/home/kaiserroman/worktrees/bannermod-b6-shortage`
  - `/home/kaiserroman/worktrees/bannermod-b6-routehandoff`
- Treat worktree-only outputs as experimental until reviewed and explicitly merged.

### 2. Time-box Gradle commands

Real issue encountered repeatedly:
- `compileJava`, `compileTestJava`, `runGameTestServer`, and `verifyGameTestStage` sometimes appeared to hang.
- Some hangs were external/network/daemon/configure stalls, not code regressions.

Operational rule:
- Always run Gradle with a timeout.
- If it stalls during daemon/configure/dependency fetching, do not block the whole wave.
- Record the stall and continue with code/planning work.

### 3. `runGameTestServer` hang root cause found earlier

Earlier in session:
- `gsd-next` kept hanging around `compileTestJava` / `verifyGameTestStage` / `runGameTestServer`.
- Investigation showed dependency resolution/network issues around Forge/Minecraft artifacts, not just runtime logic.
- Later another GameTest blocker was due to reuse of a dirty GameTest world.

Result:
- `build.gradle` was changed so `runGameTestServer` uses isolated `run_gametest`.
- The GameTest world is cleaned before launch.
- This removed the stale-world crash and got `verifyGameTestStage` green.

### 4. Planning files must be normalized after parallel work

Observed problem:
- Subagents updated `ROADMAP.md`, `STATE.md`, `VERIFICATION.md`, and slice-status files in parallel.
- Some notes became stale or overstated completion.

Rule now:
- After each batch, read `.planning` changes directly and normalize them manually if needed.
- Never trust the last-writing agent blindly.

## What Was Refactored Earlier In This Session

Large refactor work already landed in the main tree before later phase work started.

### `AbstractRecruitEntity` decomposition

Extracted helpers/services include:
- `RecruitCombatOverrideService`
- `RecruitCitizenBridge`
- `RecruitSpawnService`
- `RecruitPersistenceBridge`
- `RecruitEquipmentLoadoutService`
- `RecruitRuntimeLoop`
- `RecruitStateAccess`
- `RecruitCommandAccess`
- `RecruitOwnershipAccess`
- `RecruitUpkeepAccess`
- `RecruitEquipmentAccess`
- `RecruitProgressionAccess`

### `AbstractWorkerEntity` decomposition

Extracted helpers/services include:
- `WorkerCitizenBridge`
- `WorkerCourierService`
- `WorkerSupplyRuntime`
- `WorkerRecoveryService`
- `WorkerInventoryService`
- `WorkerStatusRuntime`
- `WorkerBlockBreakService`
- `WorkerPersistenceBridge`
- `WorkerRuntimeLoop`
- `WorkerStateAccess`
- `WorkerControlAccess`
- `WorkerLogisticsAccess`

### `AbstractLeaderEntity` decomposition

Extracted helpers/services include:
- `PatrolRouteRuntime`
- `LeaderCombatRuntime`
- `LeaderUpkeepRuntime`

### Event/runtime decomposition

Extracted/thinned areas include:
- `ClaimEvents`:
  - `ClaimProtectionPolicy`
  - `ClaimAccessQueries`
  - `ClaimInteractionTargetResolver`
  - `ClaimSiegeRuntime`
- `RecruitCombatRuntime`:
  - `RecruitAttackPolicy`
  - `RecruitDiplomacyPolicy`
- `Villager/guard` runtime:
  - `VillageGuardRecruitFactory`
  - `VillageGuardSquadFactory`
  - `IronGolemRecruitReplacementFactory`
- `Faction` runtime/events:
  - `FactionLifecycleService`
  - `FactionTeamCommandBridge`
  - `FactionRecruitTeamService`
  - `FactionRuntimeSyncService`
  - `FactionTeamJoinService`
  - `FactionTeamTransactionService`
  - `FactionTeamModificationService`
  - `FactionMenuService`
  - removed redundant `FactionTeamRuntime`
- `Villager`/worker event areas:
  - `WorkerSettlementEventService`
  - `WorkerSettlementSpawnRuntime`
  - `WorkerSettlementClaimPolicy`
  - `WorkerTradeBootstrap`
  - `WorkerAnimalGoalInjector`
  - `WorkerMarketAreaAccess`
  - removed redundant `VillagerRecruitRuntime`
- `RecruitEvents` split further via:
  - `RecruitGovernorWorkflow`
  - `RecruitWorldLifecycleService`

### Commands/UI/worldmap decomposition

- `CommandEvents` -> `MovementFormationCommandService`, `RecruitCommandActionService`
- `FormationUtils` -> `FormationLayoutPlanner`, `FormationPatternBuilder`, `FormationFallbackPlanner`
- `RecruitsAdminCommands` split into:
  - `ClaimManagerAdminCommands`
  - `DiplomacyManagerAdminCommands`
  - `FactionManagerAdminCommands`
  - `UnitsManagerAdminCommands`
  - `DebugManagerAdminCommands`
  - `TreatyAdminHelper`
  - `RecruitOwnerTeleportHelper`
- worldmap / patrol UI extracted pieces:
  - `WorldMapClaimController`
  - `WorldMapClaimMenuActions`
  - `WorldMapRouteMutationController`
  - `WorldMapRoutePopupController`
  - `WorldMapRouteToolbar`
  - `WorldMapWaypointInteractionController`
  - `WorldMapRouteSelectionUiController`
  - `PatrolRouteAssignmentController`
  - `PatrolLeaderControlController`

### Shared-domain cleanup

Removed or migrated old forwarders around:
- authority
- logistics
- settlement

Canonical shared seams now live under `shared/**` instead of compatibility-forwarders.

## Phase / Planning Work Already Performed In This Session

Compact roadmap was restructured earlier in the session.

### Current compact roadmap shape

- Phase 24: `Logistics Backbone, Courier Validation, And Economy Foundation`
- Phase 25: `Settlement Economy, Governance, And Resident Simulation`
- Phase 26: `Army Command, Formations, And Warfare`
- Phase 27: `Read Models, UI, And Player Operations`
- Phase 28: `Architecture Integration, Telemetry, Balance, And Safe Rollout`

Historical note:
- Phases `29-31` remain historical completed branches.
- Future old `32-49` were folded into compact `24-28`.

### Important planning clarifications added earlier

- Phase 25 explicitly owns the staged replacement of vanilla-village-dependent settlement gameplay with custom BannerMod settlement simulation.
- `24-05` courier closeout is now truly green and marked complete.
- Phase 24 remains active only because old `25-26` economy-foundation work was folded into it.

## Phase Batches Already Executed In This Session

These were done manually via subagents with `plan -> execute -> verify -> update planning`.

### Batch 1

Executed slices:
1. historical `24-05` courier validation closeout
2. treasury ledger + governor heartbeat tax deposit
3. army wage-food drain accounting seed
4. sea-trade entrypoint/port substrate
5. settlement aggregate opener

Important outcome:
- Initially partial because `compileTestJava` and `verifyGameTestStage` had unrelated blockers.
- Later closed after blocker fixes.

### Batch 2

Fix + advancement batch:
- fixed `compileTestJava` blocker
- fixed `Duplicate id value for 16!`
- fixed dirty GameTest world issue
- reran `verifyGameTestStage`

Outcome:
- `compileJava`, `compileTestJava`, `compileGameTestJava`, and `verifyGameTestStage` all green
- `24-05` fully closed

### Batch 3

Executed slices:
1. treasury/accounting flow enrichment
2. sea-trade runtime step
3. settlement assignment semantics enrichment
4. resident role/schedule seed
5. controlled-worker vs settlement-resident bridge

Important landed artifacts include:
- `BannerModTreasuryLedgerSnapshot` enriched use
- `BannerModSettlementManager`
- `BannerModSettlementService`
- `BannerModSettlementResidentRecord`
- assignment semantics
- service contracts and modes

### Batch 4

Executed slices:
1. stockpile summary seed
2. building category/profile seed
3. resident runtime role seed
4. market state seed
5. seller dispatch seed

Important landed artifacts include:
- `BannerModSettlementStockpileSummary`
- `BannerModSettlementBuildingCategory`
- `BannerModSettlementBuildingProfileSeed`
- `BannerModSettlementResidentRuntimeRoleSeed`
- `BannerModSettlementMarketState`
- `BannerModSettlementMarketRecord`
- `BannerModSettlementSellerDispatchState`
- `BannerModSettlementSellerDispatchRecord`

### Batch 5

Executed slices:
1. resident role profile / lightweight role registry seed
2. schedule window seed
3. job definition / job handler seed
4. desired goods / stock rules seed
5. seller dispatch seed continuation

Important landed artifacts include:
- `BannerModSettlementResidentRoleProfile`
- `BannerModSettlementResidentScheduleWindowSeed`
- `BannerModSettlementResidentJobDefinition`
- `BannerModSettlementJobHandlerSeed`
- `BannerModSettlementDesiredGoodSeed`
- `BannerModSettlementDesiredGoodsSeed`

### Batch 6

Attempted slices:
1. schedule policy projection
2. job target selection seed
3. growth/project candidate seed
4. shortage/reservation signal seed
5. seller-dispatch to trade-route handoff seed

Important warning:
- Batch 6 was the one where parallel subagents clobbered each other.
- Do NOT assume every batch-6 planning update is trustworthy.
- Re-review all batch-6 claims before merging more from worktrees.

Worktree isolation was introduced after this failure.

## Worktree Situation

Three worktrees were created to isolate colliding batch-6 tasks:
- `/home/kaiserroman/worktrees/bannermod-b6-jobtarget`
- `/home/kaiserroman/worktrees/bannermod-b6-shortage`
- `/home/kaiserroman/worktrees/bannermod-b6-routehandoff`

Status:
- These were created specifically because agents were overwriting each other.
- Their outputs must be reviewed before any merge/reapply.
- Do not blindly cherry-pick or copy their `.planning` files into main.

## Confirmed Bugs Found In Landed 24-25 Slices

These were found by auditing already-landed compact `24-25` work in the main tree.

### Already fixed in this session

1. `MarketArea` free slot counting was wrong
- Fixed.

2. `BannerModGovernorHeartbeat` used dummy supply/upkeep state
- Fixed.

3. Fiscal rollup persisted but did not reach governor UI
- Fixed.

4. Ownerless controlled worker could fall into contradictory settlement state
- Fixed.

5. Settlement enum persistence was brittle due to raw `Enum.valueOf(...)`
- Partially improved with safe fallback parsing, but remaining raw enum loads still need follow-up.

6. Settlement trade heuristics over-relied on raw port/route flags
- Partially improved by using live sea-trade entrypoint sets in settlement summaries.

7. Stale governor/treasury state after claim removal
- Fixed via cleanup in heartbeat.

8. Settlement snapshots refreshed only on heartbeat in some important civil paths
- Improved by targeted refresh hooks:
  - storage update
  - worker bound work-area changes

### Still not fully solved

1. Sea-trade still lacks a richer production consumer loop.
2. Settlement refresh is more event-driven now, but not all civil mutation paths are hooked.
3. Some batch-6 work still needs manual review because of worktree conflicts.

## Files Changed Recently For The Confirmed Bugfixes

Key files from the last bugfix wave:
- `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorHeartbeat.java`
- `src/main/java/com/talhanation/bannermod/governance/BannerModTreasuryManager.java`
- `src/main/java/com/talhanation/bannermod/network/messages/military/MessageToClientUpdateGovernorScreen.java`
- `src/main/java/com/talhanation/bannermod/client/military/gui/GovernorScreen.java`
- `src/main/java/com/talhanation/bannermod/settlement/BannerModSettlementResidentRole.java`
- `src/main/java/com/talhanation/bannermod/settlement/BannerModSettlementResidentMode.java`
- `src/main/java/com/talhanation/bannermod/settlement/BannerModSettlementResidentAssignmentState.java`
- `src/main/java/com/talhanation/bannermod/settlement/BannerModSettlementServiceActorState.java`
- `src/main/java/com/talhanation/bannermod/settlement/BannerModSettlementJobHandlerSeed.java`
- `src/main/java/com/talhanation/bannermod/settlement/BannerModSettlementResidentRuntimeRoleSeed.java`
- `src/main/java/com/talhanation/bannermod/settlement/BannerModSettlementJobTargetSelectionMode.java`
- `src/main/java/com/talhanation/bannermod/settlement/BannerModSettlementResidentServiceContract.java`
- `src/main/java/com/talhanation/bannermod/settlement/BannerModSettlementResidentJobDefinition.java`
- `src/main/java/com/talhanation/bannermod/settlement/BannerModSettlementResidentJobTargetSelectionSeed.java`
- `src/main/java/com/talhanation/bannermod/settlement/BannerModSettlementResidentRoleProfile.java`
- `src/main/java/com/talhanation/bannermod/settlement/BannerModSettlementResidentRecord.java`
- `src/main/java/com/talhanation/bannermod/settlement/BannerModSettlementService.java`
- `src/main/java/com/talhanation/bannermod/network/messages/civilian/MessageUpdateStorageArea.java`
- `src/main/java/com/talhanation/bannermod/entity/civilian/WorkerControlAccess.java`
- `src/main/java/com/talhanation/bannermod/entity/civilian/workarea/MarketArea.java`

## Current Verification Truth

At different points this session, the following were confirmed green:
- `./gradlew compileJava`
- `./gradlew compileTestJava`
- `./gradlew compileGameTestJava`
- `./gradlew verifyGameTestStage`

Important verified fact:
- `verifyGameTestStage` was green with **37 required tests**.

Also, after the latest confirmed bugfixes:
- `./gradlew compileJava` is green.
- targeted settlement/governance tests were green after fixing the helper overload issue in `BannerModSettlementService.summarizeStockpiles(...)`.

One transient Gradle issue encountered:
- incremental/build-state glitch around `RecruitMoreScreen.class` caused one targeted test run to fail in `:compileJava` despite clean code.
- this looked like local Gradle state/caching noise, not a regression in the actual bugfixes.

## Planning Files Already Updated Earlier In Session

These were already normalized earlier:
- `.planning/phases/24-logistics-backbone-and-courier-worker/24-VALIDATION.md`
  - `24-05` now marked passed/complete
- `.planning/ROADMAP.md`
  - Phase 24 historical `5/5 plans complete`
  - compact Phase 24 remains active only as folded economy foundation
- `.planning/STATE.md`
  - no longer claims `24-05` is still open
- `.planning/VERIFICATION.md`
  - `verifyGameTestStage` count corrected to `37 required tests`

Still to do immediately in next session:
- update planning docs again to reflect the latest bugfix closeouts from the final governance/settlement refresh wave, if not already mentioned.

## New Session Truth (Current Session After Claude Log Recovery)

- Claude Code session `44ac036a-48d3-4af1-a9ad-79b2c95f7a59` did leave useful work in the codebase and log, even though the handoff text was incomplete.
- That log was manually re-read in this session and used as the source of truth for what Phase 25 Millenaire work already landed.
- The previously written `MILLENAIRE-PHASE-25-MAPPING.md` in `.analysis/phase-audit-1-24/` is real and was used as the continuation map.

### Phase 25 runtime truth recovered from code + log

- Compact Phase 25 is no longer only a persistence-seed phase.
- The main tree currently contains these additive Millenaire-style runtime packages:
  - `src/main/java/com/talhanation/bannermod/settlement/goal/`
  - `src/main/java/com/talhanation/bannermod/settlement/growth/`
  - `src/main/java/com/talhanation/bannermod/settlement/project/`
  - `src/main/java/com/talhanation/bannermod/settlement/dispatch/`
  - `src/main/java/com/talhanation/bannermod/settlement/household/`
  - `src/main/java/com/talhanation/bannermod/settlement/job/`
- These slices are real, compile, and have targeted JUnit coverage, but they are still partial and mostly in-memory runtime seams rather than a full end-to-end village simulation.

### What was verified in this session

- `./gradlew test --tests com.talhanation.bannermod.settlement.goal.BannerModResidentGoalSchedulerTest --tests com.talhanation.bannermod.settlement.growth.BannerModSettlementGrowthManagerTest --tests com.talhanation.bannermod.settlement.dispatch.BannerModSellerDispatchRuntimeTest --tests com.talhanation.bannermod.settlement.household.BannerModHomeAssignmentRuntimeTest --tests com.talhanation.bannermod.settlement.household.HouseholdGoalsTest --tests com.talhanation.bannermod.settlement.project.BannerModBuildAreaProjectBridgeTest --tests com.talhanation.bannermod.settlement.job.JobHandlerRegistryTest`
  - green on 2026-04-19
- `./gradlew compileJava`
  - green on 2026-04-19

### Planning files updated in this session

- `.planning/phases/25-treasury-taxes-and-army-upkeep/25-SLICE-STATUS.md`
  - no longer claims Phase 25 is only `projectCandidateSeed`
- `.planning/ROADMAP.md`
  - Phase 25 status now states that runtime bring-up exists in `goal/growth/project/dispatch/household/job`
- `.planning/STATE.md`
  - current focus now reflects Phase 25 runtime bring-up, not only seed-layer work
- `.planning/VERIFICATION.md`
  - now records targeted verification for the new Phase 25 runtime packages

### Code changes made in this session

- `BannerModResidentGoalScheduler` now has an overload that composes the already-landed household and seller-dispatch runtime seams instead of only the original 6 stock stub goals.
- `BannerModResidentGoalSchedulerTest` now pins two integration facts:
  - home-bound residents at night pick `GoHomeResidentGoal`
  - market-service residents with `READY` dispatch pick `SellerResidentGoal`

### CitizenCore clarification

- `CitizenCore` is **not** the Millenaire runtime.
- It belongs to the older Phase 22 citizen-unification track.
- Purpose:
  - provide one shared NPC state contract (`owner`, `team`, `inventory`, `follow`, `hold/move`, `bound work area`, runtime flags)
  - support the experimental unified `CitizenEntity`
  - move the project toward one citizen/profession-driven NPC model instead of separate recruit/worker subclass forests
- Relevant files:
  - `src/main/java/com/talhanation/bannermod/citizen/CitizenCore.java`
  - `src/main/java/com/talhanation/bannermod/citizen/CitizenCoreState.java`
  - `src/main/java/com/talhanation/bannermod/entity/citizen/CitizenEntity.java`
- Treat it as adjacent enabling architecture, not as the current Phase 25 settlement-simulation owner.

### Latest session continuation after `329edf1`

- The user asked to commit the current verified state first, then finish the remaining broad Phase 25 settlement slice, then transition toward HYW.
- That commit was created:
  - `329edf1` `refactor(runtime): split legacy systems and wire settlement loop`
- After that commit, one additional broad-but-local Phase 25 slice was implemented and verified in the main tree but has **not** been committed yet.

### What the uncommitted post-`329edf1` slice added

- Added read-only logistics reservation visibility:
  - `BannerModLogisticsService.listReservations()`
- Folded live reservation state into snapshot-owned settlement signals:
  - `BannerModSettlementTradeRouteHandoffSeed` now also carries:
    - `activeReservationCount`
    - `reservedUnitCount`
  - `BannerModSettlementService` now derives reservation-aware:
    - `tradeRouteHandoffSeed`
    - `supplySignalState`
- Expanded event-driven settlement refresh coverage for more civilian mutation packets:
  - `MessageUpdateCropArea`
  - `MessageUpdateLumberArea`
  - `MessageUpdateMiningArea`
  - `MessageUpdateBuildArea`
  - `MessageUpdateAnimalPenArea`
- Successful merchant trades now refresh the local settlement snapshot:
  - `MerchantEntity.doTrade(...)`
- The live settlement orchestrator now reconciles stale seller dispatch runtime against the current snapshot seed instead of letting sellers drift across market changes:
  - `BannerModSettlementOrchestrator`

### What was verified in the latest continuation slice

- `./gradlew compileJava --console=plain`
  - green on 2026-04-19
- `./gradlew test --tests com.talhanation.bannermod.logistics.BannerModLogisticsServiceTest --tests com.talhanation.bannermod.settlement.BannerModSettlementServiceTest --tests com.talhanation.bannermod.settlement.BannerModSettlementOrchestratorTest --tests com.talhanation.bannermod.settlement.BannerModSettlementManagerTest --console=plain`
  - green on 2026-04-19

### Planning files updated again in the latest continuation slice

- `.planning/ROADMAP.md`
  - now states that Phase 25 also folds live logistics reservations into snapshot-owned trade/supply signalling
- `.planning/STATE.md`
  - now states that broader civilian packet refresh hooks and merchant-trade refresh are in place
- `.planning/VERIFICATION.md`
  - now records the reservation-aware focused verification command
- `.planning/phases/25-treasury-taxes-and-army-upkeep/25-SLICE-STATUS.md`
  - now records reservation-aware signalling, broader refresh hooks, and stale seller dispatch reconciliation

### Exact currently modified files after `329edf1`

- `.planning/ROADMAP.md`
- `.planning/STATE.md`
- `.planning/VERIFICATION.md`
- `.planning/phases/25-treasury-taxes-and-army-upkeep/25-SLICE-STATUS.md`
- `CONTINUE-HERE.md`
- `src/main/java/com/talhanation/bannermod/entity/civilian/MerchantEntity.java`
- `src/main/java/com/talhanation/bannermod/network/messages/civilian/MessageUpdateAnimalPenArea.java`
- `src/main/java/com/talhanation/bannermod/network/messages/civilian/MessageUpdateBuildArea.java`
- `src/main/java/com/talhanation/bannermod/network/messages/civilian/MessageUpdateCropArea.java`
- `src/main/java/com/talhanation/bannermod/network/messages/civilian/MessageUpdateLumberArea.java`
- `src/main/java/com/talhanation/bannermod/network/messages/civilian/MessageUpdateMiningArea.java`
- `src/main/java/com/talhanation/bannermod/settlement/BannerModSettlementOrchestrator.java`
- `src/main/java/com/talhanation/bannermod/settlement/BannerModSettlementService.java`
- `src/main/java/com/talhanation/bannermod/settlement/BannerModSettlementTradeRouteHandoffSeed.java`
- `src/main/java/com/talhanation/bannermod/shared/logistics/BannerModLogisticsService.java`
- `src/test/java/com/talhanation/bannermod/logistics/BannerModLogisticsServiceTest.java`
- `src/test/java/com/talhanation/bannermod/settlement/BannerModSettlementManagerTest.java`
- `src/test/java/com/talhanation/bannermod/settlement/BannerModSettlementOrchestratorTest.java`
- `src/test/java/com/talhanation/bannermod/settlement/BannerModSettlementServiceTest.java`

### HYW transition truth recovered this session

- `.analysis/hyw/**` already contains real raw/extracted/decompiled HYW reference material.
- The highest-value next HYW step is **not** pathing/UI first.
- The recommended opener is a bounded Phase 26 seam:
  - audit current recruit command flags and command packet mutation paths
  - then add one additive `CommandIntent` / `ArmyAction` seam over the legacy recruit command stack
- Most likely first-touch files for that HYW opener:
  - `src/main/java/com/talhanation/bannermod/entity/military/AbstractRecruitEntity.java`
  - `src/main/java/com/talhanation/bannermod/entity/military/RecruitCommandAccess.java`
  - `src/main/java/com/talhanation/bannermod/events/CommandEvents.java`
  - `src/main/java/com/talhanation/bannermod/events/RecruitCommandActionService.java`
  - `src/main/java/com/talhanation/bannermod/network/messages/military/**`

## Exact Next Steps Recommended

### Step 1. Planning normalization

This is now done in the main tree.

Current planning truth now reflects:
- governor heartbeat uses real supply/upkeep states
- governor UI surfaces fiscal rollup
- controlled-worker mode contradiction fix
- settlement enum hardening is only partial, not complete
- settlement heuristics use live sea-trade entrypoint sets more honestly
- event-driven settlement refresh hooks exist for storage updates and worker binding changes
- Phase 25 now has one live settlement orchestration seam on the governor tick

### Step 2. Resume Phase 25 execution in new batches of 5

Updated next batch after the now-landed orchestration seam:
1. Phase 25 already ported the low-risk shortage-worktree idea: logistics reservation visibility now feeds snapshot-owned settlement supply/trade hints, so the next step is deeper runtime use of those hints rather than another signal model
2. continue expanding event-driven settlement refresh to the remaining real civil mutation paths not yet covered by work-area packet hooks or merchant trade refresh
3. continue resident/job runtime toward non-stub execution using the now-live scheduler seam
4. decide whether merchant trade catalog intent should be projected into the current `tradeRouteHandoffSeed` without introducing a parallel persistence model
5. shift active exploration toward HYW after Phase 25 stabilization

### Step 2.5. Commit the verified post-`329edf1` Phase 25 slice

Before starting new feature work in the next session:

- review the currently modified files listed above
- confirm `CONTINUE-HERE.md` is included or intentionally excluded from the commit
- create one new non-amend commit for the reservation-aware Phase 25 integration slice
- do **not** include junk/artifacts such as:
  - `run_gametest/`
  - jar files
  - `.analysis/**`
  - `recruits/`
  - `workers/`

Suggested commit scope:
- reservation-aware settlement signalling
- broader civilian refresh hooks
- merchant trade refresh
- stale seller dispatch reconciliation
- truthful planning/handoff updates

Suggested commit message shape:
- `feat(25-next): enrich live settlement signals and refresh hooks`

Important:
- Run each potentially overlapping slice in separate worktrees if they touch the same settlement/planning files.
- If a subagent implements any of those slices, the main session must then:
  - read the files it changed,
  - run targeted tests/compile under timeout,
  - only then update `.planning` and the handoff.

### Step 3. Audit historical phases 1-24

The user explicitly asked for this after finishing steps 1 and 2.

Do this as a dedicated audit:
- compare phase claims vs current codebase
- report findings ordered by severity
- fix only confirmed mismatches, not speculative ones

Suggested audit scope:
- Phase 1-24 only
- focus on code/planning mismatches, missing verification, stale completion claims, and obvious integration holes

### Step 4. Start HYW opener after the Phase 25 commit

Do this with isolated worktree + subagent first.

Recommended first HYW task:
- audit the current recruit command state model and military packet mutation paths
- produce one bounded implementation seam for additive command intent / action routing

Do **not** jump straight to:
- formation/pathing rewrite
- selection UI rewrite
- broad battlefield AI rewrite

## User Preferences / Style To Preserve

- User wants aggressive pace.
- User wants many subagents in parallel.
- User explicitly wants batching of phases, `5 at a time`.
- User wants GSD workflow emulated manually, not by direct slash commands.
- User is annoyed by weak pacing and by agents overwriting each other.
- User explicitly asked that parallel subagents use worktrees when they can collide.
- User is fine with rough language; respond directly and efficiently.
- User wants minimal blocking on Gradle stalls.

## Final Reminder

Do not restart from abstract planning.

In the next session:
1. read this file,
2. launch subagents for implementation slices, but treat them as code producers rather than final authorities,
3. explicitly review and verify every subagent result in the main session before updating planning,
4. continue Phase 25 runtime integration from the already-landed Millenaire packages,
5. then audit historical phases 1-24.
