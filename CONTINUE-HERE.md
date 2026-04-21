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
1. Read `.planning/phases/26-army-command-formations-warfare/26-COMBAT-AI-SLICE-STATUS.md` — it is the active reference for what landed in the combat AI overhaul.
2. Implement the next Phase 26 slice: player-facing stance commands/packets/UI so `LOOSE`/`LINE_HOLD`/`SHIELD_WALL` is selectable in-game, plus extending the stance leash into `RecruitRangedBowAttackGoal` and `RecruitRangedCrossbowAttackGoal`.
3. Add GameTest coverage for the stance → combat behavior loop once the command entrypoints exist.
4. When Phase 26 is stable, resume compact Phase 25 settlement work-order follow-ups or audit historical phases 1-24 against the current codebase — whichever the user prioritises next.

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
- Current main-tree HEAD commit: `fab08a4`
- HEAD commit message: `feat(ai-combat): stage 4 flank/cohesion/brace + HYW unit-type counters`
- Latest landed surface: **Phase 26 combat AI overhaul (2026-04-21)** — see the dedicated section below.
- Combat AI commit trail (newest first):
  - `fab08a4` `feat(ai-combat): stage 4 flank/cohesion/brace + HYW unit-type counters`
  - `33f86bf` `feat(ai-combat): stage 3 reach weapons + rank-2 poke + per-unit cadence`
  - `2ff128c` `feat(ai-combat): stage 2 directional shield block + stance auto-block`
  - `ebe813d` `feat(ai-combat): smarter targeting + stage 1 line cohesion`
- HYW-selection UI commits (earlier this day, 2026-04-21):
  - `cd5a19b` `feat(26-hyw): drag-box selection UI (slice 5b)`
  - `b5e57ac` `feat(26-hyw): recruit selection registry (slice 5a)`
  - `d80db39` `feat(26-hyw): debug intents chat command (slice 4)`
  - `b7f6da7` `feat(26-hyw): migrate remaining command packets to dispatcher (slice 3)`
- Earlier Phase 25 commits still in history:
  - `f7ce947` `feat(25-next): building-centric work-order migration seam`
  - `28e189a` `feat(25-next): wire growth hints and scheduled settlement jobs`
  - `6b8401b` `feat(25-next): enrich live settlement signals and refresh hooks`
- There are no uncommitted changes in the main tree after `fab08a4`. `AbstractRecruitEntity.java` currently carries Stage 1–4 fields (`lastTargetLossTick`, `combatStance`, `lastFormationGapFillTick`, `cachedCohesionTick`, `cachedCohesion`, `isBracing`) and the `tickHeadTurn` yaw clamp — these are all committed.
- Existing refactor commits from earlier this session:
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

### Latest session continuation after `6b8401b`

- The previously uncommitted reservation-aware Phase 25 integration slice was reviewed, re-verified in the main tree, and committed:
  - `6b8401b` `feat(25-next): enrich live settlement signals and refresh hooks`
- After that commit, one additional verified but still-uncommitted Phase 25 runtime-wiring slice was implemented in the main tree.

### What the uncommitted post-`6b8401b` slice added

- Added one shared settlement refresh helper so more real civilian mutation paths can trigger snapshot refresh without duplicating claim-manager wiring:
  - `BannerModSettlementRefreshSupport`
- Expanded immediate settlement refresh coverage to more settlement-critical mutation paths:
  - `MessageUpdateOwner`
  - `WorkerSettlementSpawner.spawnWorker(...)`
  - `BuilderWorkGoal` build completion
  - `MiningArea.tick()` self-removal on completion
- Extended live growth scoring so reservation-aware settlement hints are no longer snapshot-only:
  - `BannerModSettlementGrowthContext` now carries `tradeRouteHandoffSeed` and `supplySignalState`
  - `BannerModSettlementGrowthManager` now consumes those hints during project queue scoring
- Tightened live resident/job runtime behavior:
  - `BannerModSettlementOrchestrator` now runs job handlers only during scheduled `WorkResidentGoal` windows
  - handler `cooldownTicks()` are now respected via in-memory orchestrator state

### What was verified in the latest post-`6b8401b` slice

- `./gradlew compileJava --console=plain`
  - green on 2026-04-20
- `./gradlew test --tests com.talhanation.bannermod.settlement.growth.BannerModSettlementGrowthManagerTest --tests com.talhanation.bannermod.settlement.BannerModSettlementOrchestratorTest --console=plain`
  - green on 2026-04-20

### Planning files updated again in the latest continuation slice

- `.planning/ROADMAP.md`
  - now also states that Phase 25 growth scoring consumes reservation-aware hints and that broader real mutation refreshes plus scheduled job gating landed
- `.planning/STATE.md`
  - now states that reservation-aware hints feed live growth scoring and that resident jobs are gated to scheduled work windows
- `.planning/VERIFICATION.md`
  - now records the focused growth/orchestrator verification command from 2026-04-20
- `.planning/phases/25-treasury-taxes-and-army-upkeep/25-SLICE-STATUS.md`
  - now records reservation-aware growth scoring, additional refresh hooks, and scheduled job gating

### Exact currently modified files after `6b8401b`

- `.planning/ROADMAP.md`
- `.planning/STATE.md`
- `.planning/VERIFICATION.md`
- `.planning/phases/25-treasury-taxes-and-army-upkeep/25-SLICE-STATUS.md`
- `CONTINUE-HERE.md`
- `src/main/java/com/talhanation/bannermod/ai/civilian/BuilderWorkGoal.java`
- `src/main/java/com/talhanation/bannermod/entity/civilian/workarea/MiningArea.java`
- `src/main/java/com/talhanation/bannermod/network/messages/civilian/MessageUpdateOwner.java`
- `src/main/java/com/talhanation/bannermod/network/messages/civilian/WorkAreaMessageSupport.java`
- `src/main/java/com/talhanation/bannermod/settlement/BannerModSettlementOrchestrator.java`
- `src/main/java/com/talhanation/bannermod/settlement/civilian/WorkerSettlementSpawner.java`
- `src/main/java/com/talhanation/bannermod/settlement/growth/BannerModSettlementGrowthContext.java`
- `src/main/java/com/talhanation/bannermod/settlement/growth/BannerModSettlementGrowthManager.java`
- `src/main/java/com/talhanation/bannermod/shared/settlement/BannerModSettlementRefreshSupport.java`
- `src/test/java/com/talhanation/bannermod/settlement/BannerModSettlementOrchestratorTest.java`
- `src/test/java/com/talhanation/bannermod/settlement/growth/BannerModSettlementGrowthManagerTest.java`

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
- event-driven settlement refresh hooks now also cover work-area owner changes, claim worker spawn/seeding, build completion, and mining-area self-removal
- Phase 25 now has one live settlement orchestration seam on the governor tick
- reservation-aware settlement hints now feed live growth scoring
- resident jobs now only execute during scheduled work windows and respect handler cooldowns

### Step 2. Resume Phase 25 execution in new batches of 5

Updated next batch after the now-landed runtime-wiring slice:
1. continue resident/job runtime beyond scheduler-gated execution toward meaningful non-stub work output
2. decide whether merchant trade catalog intent should be projected into the current `tradeRouteHandoffSeed` only after defining one real item-to-good consumer contract
3. evaluate whether the live growth queue now needs a tighter reservation-shortage heuristic instead of the current broad hint consumption
4. audit whether any other settlement-critical mutation paths still bypass immediate refresh after the latest hook wave
5. shift active exploration toward HYW after Phase 25 stabilization

### Step 2.5. Commit the verified post-`6b8401b` Phase 25 slice

Before starting new feature work in the next session:

- review the currently modified files listed above
- confirm `CONTINUE-HERE.md` is included or intentionally excluded from the commit
- create one new non-amend commit for the latest Phase 25 runtime-wiring slice
- do **not** include junk/artifacts such as:
  - `run_gametest/`
  - jar files
  - `.analysis/**`
  - `recruits/`
  - `workers/`

Suggested commit scope:
- reservation-aware growth scoring
- broader settlement-critical refresh hooks
- scheduled resident job gating and cooldowns
- truthful planning/handoff updates

Suggested commit message shape:
- `feat(25-next): wire growth hints and scheduled settlement jobs`

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

## New Architectural Redirect: Kill Worker Zones, Move To Settlement-Centric Simulation

This was a deliberate product-direction change requested after more frustration with the old worker-area model.

### New explicit direction

- Stop treating manual worker zones as the primary gameplay/control model.
- Replace zone-driven worker logic with **building-centric settlement simulation** closer to **Manor Lords / Millenaire**.
- Target model:
  - `Settlement -> Buildings -> JobSlots -> WorkOrders -> Workers`
- Desired player experience:
  - player places buildings, homes, storage, and infrastructure
  - player configures policy/priorities/radii/limits
  - villagers/workers take tasks because settlement needs them, not because of a painted invisible rectangle

### Why the old worker-zone model is now considered wrong

The user explicitly rejected the current zone model because it has the usual bad properties:

1. player paints invisible rectangles instead of building a settlement
2. NPCs work because GUI markers told them to, not because they belong to a workplace inside a village economy
3. economy feels fake because zones magically imply production
4. the village feels like scripts attached to markers rather than a living organism

### What the replacement should feel like

Worker control should become **settlement-based AI**, not zone-based AI.

Core ideas to preserve from the request:

- every production building becomes a center that publishes work
- residents have homes, work affiliations, schedules, inventory/carry limits, and priorities
- resources move through stores/stockpiles/warehouses instead of spawning from zone logic
- fields are parcels / plots, not generic worker rectangles
- forestry and extraction should be building-radius / resource-node / parcel based, not direct “go work in this cuboid” authoring
- player should mostly manage policy:
  - specialization
  - production focus
  - max radius
  - seasonal behavior
  - resource caps / limits
  - do-not-harvest / decorative protection rules

### Concrete target design requested by user

The user’s requested design direction, summarized faithfully:

#### Settlement structure

- `Village` / settlement-level state owns:
  - buildings
  - population
  - stores / stockpiles
  - seasonal needs
  - development / construction priorities
  - local economy and logistics

#### Buildings

- each building has:
  - type / specialization
  - storage needs / inputs / outputs
  - worker slots / job slots
  - task generation rules
  - optional service radius / parcel / linked nodes

#### Work model

- buildings publish `WorkOrders` / contracts such as:
  - fetch resource
  - deliver resource
  - produce item
  - plant / till / harvest
  - cut / haul / replant
  - build / repair
  - refuel / restock

#### Resident model

- residents/workers should eventually have:
  - home
  - workplace affiliation or profession
  - schedule / day rhythm
  - inventory / carrying capacity
  - skill / preference / priority weights

#### Task choice model

- worker picks jobs by weighted scoring, e.g.:
  - priority
  - distance
  - profession fit
  - congestion
  - current settlement shortage / urgency

### Specific per-domain replacements requested

#### Farming

- replace generic zones with **field parcels / field plots**
- field should be a settlement object with:
  - boundaries
  - crop
  - fertility / state / season
  - owner building / farm affiliation
  - worker demand
- expected tasks:
  - till
  - sow
  - weed
  - harvest
  - haul to barn / storage

#### Forestry

- replace generic lumber zone with **building service radius** around woodcutter camp / forestry building
- building should publish tasks:
  - cut mature tree
  - haul logs
  - replant
- possible future policy:
  - avoid village-core decorative trees
  - prefer mature trees
  - enable / disable replanting

#### Mining / stone / clay / extraction

- do **not** keep cuboid “mine here” as the main mechanic
- preferred replacements:
  - resource nodes near a mine/quarry building
  - or parcel-like quarry/extraction site marker that belongs to settlement infrastructure, not generic worker zoning

#### Logistics / production

- buildings should publish needs and contracts
- storage / hauling must be first-class work
- no more “zone exists, therefore magic production happens”

### Strong migration rule from the user

Do **not** try to fix the zone UX further as the long-term answer.

The direction is:

- worker zones become fallback / temporary compatibility only
- new development should favor settlement/building/task architecture
- the end state should make zones either optional rare tools or removable entirely

## Current Code Reality Relevant To This Redirect

This was inspected before the redirect was captured.

### Old zone-centric runtime still active

Current worker execution is still heavily tied to `AbstractWorkAreaEntity` and area-specific entities like:

- `CropArea`
- `LumberArea`
- `MiningArea`
- `BuildArea`
- `StorageArea`
- `MarketArea`
- `AnimalPenArea`

Worker AI still consumes these directly in area-centric goal loops such as:

- farmer current crop-area binding
- lumberjack current lumber-area behavior
- miner current mining-area behavior
- builder current build-area behavior

### But compact Phase 25 already created useful replacement seams

The important thing is that the codebase is **not** starting from zero.

Phase 25 already has additive settlement runtime pieces that can become the new authority:

- settlement snapshots
- building records and building profile seeds
- resident records
- resident role profiles
- resident schedule seeds / policies
- resident service contracts
- resident job definitions and target-selection seeds
- `BannerModSettlementOrchestrator`
- `BannerModResidentGoalScheduler`
- `JobHandlerRegistry`
- `BuildJobHandler`
- `HarvestJobHandler`
- household/home-assignment runtime
- seller-dispatch runtime
- growth/project runtime

This means the likely migration path is **not** “rewrite everything from scratch.”

It is:

- move authority away from work-area entities
- let settlement/building/runtime seams publish and resolve jobs
- leave old work areas as compatibility adapters until no longer needed

## Recommended First Migration Seam

Do **not** try to delete all zones in one move.

The narrowest useful first seam is:

### Building-owned work orders over existing settlement records

Start by introducing one additive concept over the current Phase 25 runtime:

- `BuildingWorkOrder` or `SettlementWorkOrder`
- `BuildingJobSlot` / `WorkplaceDemand`
- building-scoped task publication from settlement building records

And then route resident job execution through that instead of direct work-area ownership.

### Why this is the right first seam

Because the code already has:

- building records
- resident job definitions
- a scheduler
- an orchestrator
- job handlers

What is still missing is the middle layer that says:

- this building currently needs X done
- here is the target / count / urgency / workplace
- residents take that order from the settlement runtime

That is the bridge from:

- `Zone -> NPC hardcoded loop`

to:

- `Settlement -> Building -> Published work -> Resident takes task`

## Practical Migration Stages To Preserve

The user explicitly wanted a staged migration, not a giant instant rewrite.

### Stage 1. Kill zones as required authority

- keep work areas only as fallback compatibility
- introduce settlement/building/runtime authority for work publication
- stop making new features depend on manual area rectangles

### Stage 2. Move simple professions first

First candidates:

- farmer
- woodcutter
- hauler / carrier
- miner

These should become building-driven before more complex professions.

### Stage 3. Make storage/logistics real

This is mandatory for “living village” feel:

- central/local storage
- reservation
- hauling as explicit work
- delivery / fetch as explicit tasks

### Stage 4. Home and day-rhythm matter

- resident should be tied to home and schedule
- not just job type

### Stage 5. Replace micromanagement with policy

Player should set:

- building specialization
- max range
- production priority
- seasonal mode
- limits / caps / bans

not draw worker rectangles as the primary gameplay loop.

## Files / Areas Most Likely To Matter First

These are the highest-value current files for the migration opener:

- `src/main/java/com/talhanation/bannermod/settlement/BannerModSettlementOrchestrator.java`
- `src/main/java/com/talhanation/bannermod/settlement/BannerModSettlementService.java`
- `src/main/java/com/talhanation/bannermod/settlement/BannerModSettlementResidentJobDefinition.java`
- `src/main/java/com/talhanation/bannermod/settlement/BannerModSettlementResidentJobTargetSelectionSeed.java`
- `src/main/java/com/talhanation/bannermod/settlement/BannerModSettlementResidentServiceContract.java`
- `src/main/java/com/talhanation/bannermod/settlement/BannerModSettlementBuildingRecord.java`
- `src/main/java/com/talhanation/bannermod/settlement/job/JobHandlerRegistry.java`
- `src/main/java/com/talhanation/bannermod/settlement/job/BuildJobHandler.java`
- `src/main/java/com/talhanation/bannermod/settlement/job/HarvestJobHandler.java`
- likely new files under `src/main/java/com/talhanation/bannermod/settlement/job/` or `settlement/building/`

Legacy/fallback worker-area files that will eventually be bypassed rather than expanded:

- `src/main/java/com/talhanation/bannermod/entity/civilian/workarea/**`
- old worker AI loops under `src/main/java/com/talhanation/bannermod/ai/civilian/**`

## Exact Next Session Recommendation For This Redirect

When resuming this task, do this in order:

1. Do **not** spend the next session polishing zone size UX as the long-term answer.
2. Use subagents first to inspect current settlement/job/building seams in isolated worktrees if overlap risk appears.
3. Design and implement the first additive building-centric work publication seam.
4. Keep worker zones only as compatibility/fallback until one profession is truly running building-first.
5. Verify with compile + focused settlement/job tests before updating planning.

### Best first implementation slice

Recommended bounded first slice:

- introduce building-owned published work demand inside the settlement runtime
- let `BannerModSettlementOrchestrator` resolve resident work from building demand rather than only direct area-style bindings
- start with one low-risk profession path, likely **harvest/farm** or **construction/build**, because the code already has job handlers and project/build seams

### What not to do first

- do not attempt full pathing rewrite first
- do not attempt full village UI rewrite first
- do not delete all work-area entities in one patch
- do not claim the migration is complete after only adding data classes

### Success condition for the first real migration slice

One concrete profession path should be able to say:

- a building / settlement record published the work
- a resident got a task because of settlement/building demand
- the path no longer fundamentally depends on a hand-authored worker rectangle as primary authority

That is the real beginning of the Manor Lords / Millenaire shift.

## Session 2026-04-21: Phase 26 Combat AI Overhaul

This session delivered a compact HYW-parity combat AI overhaul across four stages plus a smarter-targeting prelude. All landed on `master`. The authoritative slice status is `.planning/phases/26-army-command-formations-warfare/26-COMBAT-AI-SLICE-STATUS.md` — read that first.

### Why this work landed

User reported four concrete combat problems:
- A. Target finding was weak.
- B. Switching between multiple attackers was poor.
- C. After killing 3 enemies a recruit could ignore the 4th for 15–20 seconds.
- D. Whole squads dogpiled one enemy instead of spreading.

Plus a product-direction ask: "make formations with shields solid, like in HYW". Audit of HYW and our mod showed **neither** actually implemented real shield-wall semantics. So the work is "HYW parity, then go beyond where HYW stops short". Written down as four stages that ship additively behind a new `CombatStance` contract.

### Landed seams (newest first)

`fab08a4` — Stage 4 flank / cohesion / brace / unit-type counters:
- `FacingHitZone.classify` returns `FRONT` (120° cone, reuses `ShieldBlockGeometry`), `BACK` (rear 90° arc), else `SIDE`. `FlankDamage.multiplierFor`: FRONT ×1.0, SIDE ×1.15, BACK ×1.5.
- `FormationCohesion.isCohesive`: true when ≥2 other cohort-mates within 2 blocks share `LINE_HOLD`/`SHIELD_WALL`. Grants ×0.85 remaining damage. Cached 10 ticks via `cachedCohesionTick`/`cachedCohesion` on `AbstractRecruitEntity`.
- `BraceAgainstChargePolicy.shouldBrace`: stance ≠ LOOSE + shield/reach holder + mounted hostile ≤10 blocks → `setShouldBlock(true)`, stop navigation, set `isBracing`. `UseShield.tick` attaches a transient `KNOCKBACK_RESISTANCE +0.5` modifier while braced. Cavalry damage against braced targets gets ×0.7 remaining.
- `UnitTypeMatchup.classify` (LIGHT/HEAVY/RANGED/CAVALRY/PIKE_INFANTRY) + `damageMultiplier` ports HYW counters: light vs heavy ×0.8, heavy vs light ×1.2, cavalry vs light/ranged ×1.4, foot vs cavalry ×0.9, pike vs cavalry ×1.5. Applied in `RecruitCombatDecisions.doHurtTarget` only when target is another recruit — PvE balance vs players/monsters is intact.

`33f86bf` — Stage 3 reach weapons + rank-2 poke + per-unit cadence:
- `WeaponReach.effectiveReachFor` returns per-item extra reach via registry-id heuristics: sarissa/long_spear +2.5, pike/halberd/polearm +2.0, spear +1.0. Fold-in point in `AttackUtil.getAttackReachSqr`. BannerMod does not ship `SpearItem`/`PikeItem` yet — the `Item`-taking overload in `WeaponReach` is the single extension point when those classes land.
- `FriendlyLineOfSight.canReachThroughAllies` + `RecruitMeleeAttackGoal.hasReachLineOfSight`: reach holders (≥1 block extra) can attack through allied recruits in front of them. Block LOS still gates. Leash still holds rank-2 spearmen in their slot.
- `AttackCadence.cooldownTicksFor` tunes post-hit cooldown per weapon: spear +2 tick windup, pike ×1.1 + 4 ticks, sarissa ×1.15 + 5 ticks, plain melee unchanged.

`2ff128c` — Stage 2 directional shield block + stance auto-block:
- `ShieldBlockGeometry.isInFrontCone` computes attacker angle vs `yBodyRot` with a 120° front cone. Flank/back bypass the shield entirely (Stage 4 then applies the flank multiplier).
- `ShieldMitigation.damageAfterBlock`: LOOSE remaining 0.55 / LINE_HOLD 0.45 / SHIELD_WALL 0.30. `RecruitShieldmanEntity` gets an extra ×0.9 remaining stacked on top. Stagger (`blockCoolDown > 0`) reduces absorption by 40 %.
- `RecruitCombatOverrideService.prepareIncomingDamage` now: (shield mitigation if front-cone + up) → (flank multiplier) → (brace cavalry ×0.7) → (cohesion ×0.85). Single damage-flow path.
- `RecruitShieldEvents` cancels Forge `ShieldBlockEvent` for `AbstractRecruitEntity`. **Critical** — without this, vanilla `LivingEntity.hurt` would zero damage AND double-charge shield durability after our mitigation. Our `prepareIncomingDamage` is now the only shield path. Registered in `BannerModMain.setup`.
- `UseShield.canUse` auto-raises shield for SHIELD_WALL (8-block hostile radius) or LINE_HOLD (5-block). In-formation SHIELD_WALL units also slowly pivot `yBodyRot` toward nearest hostile via `FormationYawPolicy.clampBodyYaw` (6°/tick).
- Blocked melee hits knock attacker back with 0.5 strength.

`ebe813d` — Targeting + Stage 1 line cohesion (one bundled commit because the two sets interleaved on `AbstractRecruitEntity`):
- **Targeting part:**
  - `RecruitCombatTargeting.resolveCombatTargetWithAssigneeSpread` scores candidates `distSqr + assignees × 36` against the new per-cohort `FormationTargetSelectionController` assignee registry (40-tick TTL). Replaces closest-first dogpile.
  - REUSED-shared path checks the registry for pile-on (≥3 assignees) and falls through to local scan if the shared target is saturated. Compute paths record the assignee after picking.
  - Reactive `assignReactiveCombatTarget` uses a 3-block hysteresis (`newDistSqr + 9 < currentDistSqr`) with a melee-reach override so hits at equal distance no longer toggle targets.
  - `AbstractRecruitEntity.lastTargetLossTick` stamped when `setTarget(null)` fires for a dead/removed target. `RecruitAiLodPolicy.Context.recentlyLostTarget` forces FULL tier; `RecruitRuntimeLoop.isBaseTargetSearchTick` drops the base gate to 5 ticks for 60 ticks. Closes symptom C (15–20s blind spot).
- **Stage 1 part:**
  - New `CombatStance` enum (LOOSE default / LINE_HOLD / SHIELD_WALL). NBT-persisted via `RecruitPersistenceBridge`.
  - `CombatLeashPolicy` stance-aware leash: LOOSE 13 blocks (formation) / 20 (free), LINE_HOLD 5, SHIELD_WALL 3. `RecruitMeleeAttackGoal.canContinueToUse` breaks engagement when drifted off stance leash.
  - `FormationSlotRegistry` tracks per-cohort `(slotIndex → ownerUuid, holdPos, ownerRotDeg)` populated by `FormationLayoutPlanner` (both `assignAndApplySlots` and `applySequentialSlots`).
  - `FormationGapFillPolicy` + `FormationFallbackPlanner.tryFillForwardGap`: on neighbour death, rear-rank recruits in LINE_HOLD/SHIELD_WALL migrate to the forward-gap slot. 60-tick per-recruit cooldown via `lastFormationGapFillTick`, 20-tick staggered scan via `UUID.hashCode()`.
  - `FormationYawPolicy.clampBodyYaw` clamps body-yaw to 10°/6° per tick in formation under LINE_HOLD/SHIELD_WALL. Head yaw free.

### File map

New (ai/military): `CombatStance`, `CombatLeashPolicy`, `FormationSlotRegistry`, `FormationGapFillPolicy`, `FormationYawPolicy`, `ShieldBlockGeometry`, `ShieldMitigation`, `WeaponReach`, `FriendlyLineOfSight`, `AttackCadence`, `FacingHitZone`, `FlankDamage`, `FormationCohesion`, `BraceAgainstChargePolicy`, `UnitTypeMatchup`.

New (events): `RecruitShieldEvents`.

Tests added (all pure, framework-free, under `src/test/java/com/talhanation/bannermod/ai/military/`): `CombatLeashPolicyTest`, `FormationGapFillPolicyTest`, `FormationSlotRegistryTest`, `FormationYawPolicyTest`, `ShieldBlockGeometryTest`, `ShieldMitigationTest`, `WeaponReachTest`, `FriendlyLineOfSightTest`, `AttackCadenceTest`, `FacingHitZoneTest`, `FlankDamageTest`, `FormationCohesionTest`, `BraceAgainstChargePolicyTest`, `UnitTypeMatchupTest`.

Modified: `FormationTargetSelectionController`, `RecruitAiLodPolicy`, `RecruitMeleeAttackGoal`, `RecruitHoldPosGoal`, `UseShield`, `AbstractRecruitEntity`, `RecruitCombatOverrideService`, `RecruitCombatTargeting`, `RecruitRuntimeLoop`, `RecruitPersistenceBridge`, `RecruitCombatDecisions`, `AttackUtil`, `FormationLayoutPlanner`, `FormationFallbackPlanner`, `BannerModMain` (just one `EVENT_BUS.register` line for `RecruitShieldEvents`).

### Verification (2026-04-21)

- `./gradlew compileJava --console=plain` — green after every commit.
- `./gradlew compileTestJava --console=plain` — green.
- `./gradlew test --tests "com.talhanation.bannermod.ai.military.*" --console=plain` — 16 suites, **129 tests, 0 failures, 0 errors**.
- `verifyGameTestStage` was NOT re-run this slice. All new logic is server-tick heuristics over existing seams with pure-helper unit coverage. The next stance-command/UI slice should add GameTest coverage when player-facing entrypoints land.

### What is deliberately NOT done yet

- **Player-facing stance commands / packets / UI.** Stance is programmatic-only today — `recruit.setCombatStance(CombatStance.SHIELD_WALL)`. The next Phase 26 slice owns this.
- **Stance leash for ranged goals.** `RecruitRangedBowAttackGoal` and `RecruitRangedCrossbowAttackGoal` still use `movePos` not `holdPos`, so stance leash does not yet extend there. Same next slice should handle it.
- **Real `SpearItem`/`PikeItem` classes.** `WeaponReach` uses string heuristics because those classes don't exist. When they land, swap in `instanceof` checks in `WeaponReach.effectiveReachFor(Item)`.
- **Velocity-aware brace timing.** `BraceAgainstChargePolicy` is proximity-only on mounted hostile.
- **Morale-driven retreat / rout.** Still environmental flees only.
- **Shield durability discount during brace.** Not implemented — shield still takes mitigated damage.

### Next-session entry points

For the next developer continuing this track:

1. `.planning/phases/26-army-command-formations-warfare/26-COMBAT-AI-SLICE-STATUS.md` is the authoritative slice status.
2. For implementing the stance command/UI slice:
   - Existing packet path: look at `src/main/java/com/talhanation/bannermod/network/messages/military/` for packet patterns. Dispatcher refactor landed earlier this day in `b7f6da7`.
   - `CombatStance` values are already NBT-persisted via `RecruitPersistenceBridge`. Just need a `MessageSetCombatStance` packet that calls `recruit.setCombatStance(...)` server-side.
   - UI entrypoint most likely under `src/main/java/com/talhanation/bannermod/client/military/gui/`.
   - Selection registry from `b5e57ac` lets you apply commands across a group — reuse it.
3. For extending leash into ranged goals:
   - `RecruitRangedBowAttackGoal.canUse` checks `canAttackMovePos` which uses `movePos`, not `holdPos`. You need to consult `CombatLeashPolicy.canEngage` against `holdPos` when the recruit `isInFormation`.
4. For GameTest coverage:
   - Existing pattern lives under `src/gametest/java/com/talhanation/bannermod/`. Spawn a squad in formation, apply `SHIELD_WALL` stance, inject hostile, assert the line holds (recruits don't leave leash) and shields raise.
5. Keep the sticky workflow: subagents produce, main session verifies, only then write planning.
