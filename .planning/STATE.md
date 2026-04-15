---
gsd_state_version: 1.0
milestone: v1.0
milestone_name: milestone
status: Executing Phase 23
last_updated: "2026-04-15T16:18:36Z"
progress:
  total_phases: 31
  completed_phases: 15
  total_plans: 58
  completed_plans: 58
---

# Project State

- Current focus: Phase 23 is in progress. Plans 23-01 through 23-04 are complete and now provide the claim-keyed governor snapshot, pure legality rules, persistence manager, authority helper, snapshot-backed runtime assignment/revocation service, heartbeat-driven local tax/advisory output, and the live governor promotion/control UI path. The root test tree still has 39 deferred compile errors outside governance scope that block full root `./gradlew test`; Phase 23-05 is next.
- Runtime base: `recruits` (now merged in-place — single source root)
- Active runtime mod: `bannermod`
- Workers status: fully absorbed; civilian Java code lives under `bannermod.{entity,ai,client,inventory,items,persistence,registry,settlement}.civilian`; civilian network packets live under `bannermod.network.messages.civilian` at packet-ID offset = MILITARY_MESSAGES.length (104).
- Pending major work: Phase 22 onwards proceeds against the consolidated single-source-tree baseline.
- Primary references: `MERGE_PLAN.md`, `MERGE_NOTES.md`, `.planning/CODEBASE.md`, `.planning/VERIFICATION.md`
- Phase 06 planning artifacts: `.planning/phases/06-player-cycle-gametest-validation/`
- Phase 07 planning artifacts: `.planning/phases/07-dedicated-server-authority-edge-validation/`
- Phase 08 planning artifacts: `.planning/phases/08-multiplayer-authority-conflict-validation/`
- Phase 09 planning artifacts: `.planning/phases/09-settlement-faction-binding-contract/`
- Phase 10 planning artifacts: `.planning/phases/10-settlement-faction-enforcement-validation/`
- Phase 11 planning artifacts: `.planning/phases/11-large-battle-ai-profiling-baseline/`
- Phase 12 planning artifacts: `.planning/phases/12-global-pathfinding-control/`
- Phase 13 planning artifacts: `.planning/phases/13-path-reuse-for-cohesion-movement/`
- Phase 14 planning artifacts: `.planning/phases/14-formation-level-target-selection-rewrite/`
- Phase 15 planning artifacts: `.planning/phases/15-pathfinding-throttling-and-budgeting/`
- Phase 16 planning artifacts: `.planning/phases/16-async-pathfinding-reliability-fixes/`
- Phase 17 planning artifacts: `.planning/phases/17-ai-lod-and-tick-shedding/`
- Phase 18 planning artifacts: `.planning/phases/18-optional-flow-field-navigation-evaluation/`
- Phase 19 planning artifacts: `.planning/phases/19-large-battle-performance-validation/`
- Phase 20 planning artifacts: `.planning/phases/20-runtime-audit-and-bannerlord-target-architecture/`
- Phase 21 planning artifacts: `.planning/phases/21-source-tree-consolidation-into-bannerlord/`
- Phase 29 planning artifacts: `.planning/phases/29-1-3-3-2-branch-mining-strip-mining-3-21-26/`
- Phase 21-28 research summary: `.planning/phases/FUTURE-EXPANSION-PHASES.md`
- Phase 01 planning artifacts: `.planning/phases/01-workspace-bootstrap/`
- Latest execution summary: `.planning/phases/23-settlement-governance-and-governor-control/23-settlement-governance-and-governor-control-04-SUMMARY.md` (live governor promotion plus packet-synced control UI and bounded policy toggles)
- Latest planning artifacts: `.planning/phases/24-logistics-backbone-and-courier-worker/24-CONTEXT.md`, `.planning/phases/24-logistics-backbone-and-courier-worker/24-RESEARCH.md`, `.planning/phases/24-logistics-backbone-and-courier-worker/24-VALIDATION.md`, `.planning/phases/24-logistics-backbone-and-courier-worker/24-01-PLAN.md`, `.planning/phases/24-logistics-backbone-and-courier-worker/24-02-PLAN.md`, `.planning/phases/24-logistics-backbone-and-courier-worker/24-03-PLAN.md`, `.planning/phases/24-logistics-backbone-and-courier-worker/24-04-PLAN.md`, `.planning/phases/24-logistics-backbone-and-courier-worker/24-05-PLAN.md`

## Decisions

- Phase 01 plan 01 keeps the repository anchored to one root Gradle project named `bannermod`.
- `.planning/` is the only active planning root; `.planning_legacy_recruits/` and `.planning_legacy_workers/` remain archive-only context.
- [Phase 01-workspace-bootstrap]: Keep the default bootstrap validation baseline at compileJava, processResources, and test until root GameTests become meaningful.
- [Phase 01-workspace-bootstrap]: Use MERGE_NOTES.md as the active log whenever legacy wording or archived plans disagree with root code and docs.
- [Phase 02-runtime-unification-design]: BannerMod remains the only active public runtime identity for the merged mod.
- [Phase 02-runtime-unification-design]: Workers-owned GUI, structure, and language assets now have an explicit bannermod namespace end-state.
- [Phase 02-runtime-unification-design]: The merged runtime only guarantees known Workers-era migration seams, not standalone workers mod compatibility.
- [Phase 02-runtime-unification-design]: BannerMod-owned config is the target end-state, while Workers config registration remains transitional only.
- [Phase 03-workers-subsystem-absorption]: Workers is treated as an absorbed in-process subsystem behind the merged BannerMod runtime rather than a separate live mod boundary.
- [Phase 04-resource-and-data-consolidation]: Active merged resources, pack wiring, and registry-coupled namespaces are already consolidated under `bannermod`; preserved `workers` assets remain migration or cleanup-only baggage.
- [Phase 05-stabilization-and-cleanup]: Retained Workers JUnit suites run through the root test source set instead of a separate Workers-only entrypoint.
- [Phase 05-stabilization-and-cleanup]: Build-area mutation is guarded by the shared work-area authoring rule boundary before server-side updates run.
- [Phase 05-stabilization-and-cleanup]: Legacy recruits and workers update-check listeners stay disabled until one merged release-feed contract exists.
- [Slice follow-up]: Shared planning vocabulary for strategic, settlement, military, civilian, and logistics layers lives in `.planning/codebase/INTEGRATED_SYSTEM_ARCHITECTURE.md` and `.planning/CODEBASE.md`.
- [Slice follow-up]: Root smoke coverage now asserts the recruit-owned `bannermod` runtime identity and the worker subsystem network offset in one merged-runtime regression test.
- [Slice follow-up]: Shared owner, same-team, admin, and forbidden authority vocabulary now lives in `src/main/java/com/talhanation/bannermod/authority/BannerModAuthorityRules.java` and backs both worker authoring rules and worker-control recovery.
- [Slice follow-up]: Shared supply-status vocabulary now lives in `src/main/java/com/talhanation/bannermod/logistics/BannerModSupplyStatus.java` and exposes build-project material pressure, worker storage blockage, and recruit upkeep food/payment pressure without changing current AI behavior.
- [Slice follow-up]: Shared config-file taxonomy now lives in `src/main/java/com/talhanation/bannermod/config/BannerModConfigFiles.java`, and the merged bootstrap resolves `bannermod-military.toml`, `bannermod-settlement.toml`, and `bannermod-client.toml` with explicit low-risk migration from `bannermod-server.toml` and `workers-server.toml`.
- [Slice follow-up]: Root `gametest` is no longer empty; `src/gametest/java/com/talhanation/bannermod/IntegratedRuntimeGameTests.java` now proves merged runtime coexistence and one live recruit-worker-crop-area interaction inside one BannerMod GameTest runtime.
- [Slice follow-up]: Root integrated gameplay validation now spawns a recruit, a farmer worker, and a crop area in one live BannerMod GameTest to prove shared-owner recruit friendly-fire protection, worker work-area authorization, and worker control recovery across the merged runtime seam.
- [Slice follow-up]: Root integrated supply validation now also spawns a recruit and an owned build area in one live BannerMod GameTest to prove settlement build-material pressure and a recruit upkeep transition from blocked to ready project through the same shared `BannerModSupplyStatus` seam.
- [Slice follow-up]: `./gradlew verifyGameTestStage` is currently green with 32 required tests after adding the dedicated-server authority and reconnect GameTests.
- [Phase 06-player-cycle-gametest-validation]: The next roadmap phase extends root GameTests from isolated merged-runtime seams into player-facing cycle validation for shared ownership, settlement labor, upkeep supply, and one stitched authority-safe gameplay loop.
- [Phase 06-player-cycle-gametest-validation]: Keep IntegratedRuntimeGameTests limited to merged runtime seam smoke coverage.
- [Phase 06-player-cycle-gametest-validation]: Move ownership assertions into a dedicated BannerModOwnershipCycleGameTests artifact so later Phase 06 slices can grow independently.
- [Phase 06-player-cycle-gametest-validation]: Model settlement-labor outsider checks with a distinct fake player identity so GameTest authority assertions stay deterministic.
- [Phase 06-player-cycle-gametest-validation]: Direct worker recovery must enforce the same owner-or-admin authority rule even when the owner player entity is not currently resolved in-level.
- [Phase 06-player-cycle-gametest-validation]: The full player-cycle GameTest should compose the earlier ownership, labor, and upkeep slice contracts rather than inventing a parallel setup path.
- [Post-Phase-06 roadmap]: Keep the next validation work split into separate dedicated-server and multiplayer phases so offline-owner and contested-player edge cases stay independently executable.
- [Post-Phase-06 roadmap]: Treat settlement-to-faction binding as a first-class gameplay contract that is explicit before later implementation slices expand settlement mechanics.
- [Post-Phase-06 roadmap]: Prefer derived settlement-plus-claim binding rules over a new deep persistence manager unless a later execution slice proves a dedicated manager is necessary.
- [Post-Phase-06 roadmap]: Validate faction-binding enforcement separately from contract-definition so each later `/gsd-plan-phase` slice stays small and reviewable.
- [Phase 09-settlement-faction-binding-contract]: Settlement-faction status now lives in one shared BannerMod seam with explicit `FRIENDLY_CLAIM`, `HOSTILE_CLAIM`, `UNCLAIMED`, and `DEGRADED_MISMATCH` vocabulary.
- [Phase 09-settlement-faction-binding-contract]: Keep settlement binding utility-shaped and claim-derived until later code proves a dedicated settlement manager is necessary.
- [Phase 10-settlement-faction-enforcement-validation]: Root GameTests now prove friendly settlement placement and operation, hostile or unclaimed denial, and claim-loss or faction-mismatch degradation through the shared settlement-binding seam.
- [Phase 10-settlement-faction-enforcement-validation]: `./gradlew verifyGameTestStage` remains green with all 45 required tests after the settlement-faction enforcement and degradation coverage is included in the root suite.
- [Performance roadmap]: Keep large-battle AI/performance work ordered as profiling baseline, global pathfinding control, path reuse, formation-level target selection, pathfinding throttling, async reliability fixes, AI LOD, optional flow-field evaluation, and closing performance validation.
- [Performance roadmap]: Require explicit profiling evidence before optimization, during each optimization slice, and after the full sequence so future tuning work can compare against one stable baseline.
- [Future roadmap phases 20-28]: Treat the post-performance structural/gameplay program as real roadmap phases after the active 10-19 queue, not as a side document outside the milestone.
- [Future roadmap phases 20-28]: Sequence the source-tree consolidation before broad gameplay expansion so later phases build on one canonical `bannermod` codebase.
- [Phase 11-large-battle-ai-profiling-baseline]: Derive the baseline scenario matrix from existing recruit battle stress fixtures and current target/pathfinding seams before adding any new instrumentation.
- [Phase 11-large-battle-ai-profiling-baseline]: Every baseline evidence bundle must record async pathfinding, async target-finding, and worker-thread config so later optimization results stay comparable.
- [Performance roadmap]: Treat flow-field navigation as optional and benchmark-gated rather than a mandatory rewrite of the current navigation stack.
- [Future expansion roadmap]: Sequence the full source-tree move ahead of broad gameplay expansion so entity, AI, registry, and client ownership are clear before deeper system rewrites begin.
- [Future expansion roadmap]: The one-entity target is a shared `Citizen` representation with role/job-driven behavior; phase planning must treat this as a major refactor because workers and recruits are currently concrete subclass families.
- [Future expansion roadmap]: Governor gameplay should sit above the existing settlement-binding and authority seams so tax collection, garrison guidance, and incident reporting do not fork ownership rules.
- [Future expansion roadmap]: Move workers onto the recruit-owned pathfinding stack and add explicit logistics reservations before shipping the courier worker.
- [Future expansion roadmap]: Treat taxes, treasury, army upkeep, supply, and trade as one layered economy program driven by heartbeat-style updates rather than per-tick full recomputation.
- [Future expansion roadmap]: Treat Medieval Siege Machines compatibility as optional external compatibility, while shield wall and morale improvements become BannerMod-owned combat systems backed by maintained source.
- [Phase 12-global-pathfinding-control]: Route recruit path issuance through one pass-through `GlobalPathfindingController` seam before adding path reuse, throttling, or async safety changes.
- [Phase 12-global-pathfinding-control]: Keep controller-aware profiling additive inside the existing mixed-squad and dense-battle GameTests so Phase 11 before/after comparisons stay aligned.
- [Phase 13-path-reuse-for-cohesion-movement]: Keep path reuse controller-owned and copy reused paths before application so nearby recruits do not share one mutable live `Path` instance.
- [Phase 13-path-reuse-for-cohesion-movement]: Treat reuse attempts as mandatory observability in dense scenarios, but judge hit rate only in the context of actual compatible movement opportunities.
- [Phase 14-formation-level-target-selection-rewrite]: Keep formation target selection utility-shaped and cohort-bounded so the rewrite stays on shared combat intent instead of introducing a persistent manager.
- [Phase 14-formation-level-target-selection-rewrite]: Preserve the retained dense-battle scenario contract by registering formation cohorts only where the original winner expectations stay stable, and keep harder retarget/loss-of-target checks optional until the brownfield arena setup is hardened.
- [Phase 14-formation-level-target-selection-rewrite]: Final target publication now revalidates candidate liveness, so stale async search results cannot republish dead or removed enemies during shared retarget/loss-of-target refresh.
- [Phase 14-formation-level-target-selection-rewrite]: `./gradlew verifyGameTestStage` is green with all 45 required tests after the final Phase 14 brownfield follow-up closed the optional retarget/loss-of-target gaps.
- [Phase 17-ai-lod-and-tick-shedding]: Keep the first LOD slice bounded to recruit-owned non-critical decision cadence, especially target-search work that is already observable, instead of skipping combat execution or ownership safety behavior.
- [Phase 17-ai-lod-and-tick-shedding]: Require explicit profiling for LOD runs, skips, and active tiers so later tuning can compare cost savings against any readability or target-acquisition regressions.
- [Phase 17-ai-lod-and-tick-shedding]: Recruit target search now runs through one explicit `RecruitAiLodPolicy` seam with operator-visible config, while close or combat-relevant recruits stay on the original full-fidelity cadence.
- [Phase 17-ai-lod-and-tick-shedding]: Retained dense-battle and mixed-squad GameTests now publish and assert LOD opportunity, skip, and tier counters, and `verifyGameTestStage` remains the correctness gate for accepting any AI LOD evidence.
- [Phase 18-optional-flow-field-navigation-evaluation]: The dedicated same-destination benchmark now exercises the optional prototype on a real in-scope path, but all 51 prototype attempts fell back with zero hits, so the final Phase 18 outcome is `drop`.
- [Phase 18-optional-flow-field-navigation-evaluation]: Keep the flow-field experiment documented as evaluated and rejected; continue Phase 19 on the existing pathfinding stack rather than expanding the optional prototype.
- [Phase 07-dedicated-server-authority-edge-validation]: Create a dedicated-server helper seam now so later reconnect and persistence tests can reuse deterministic fake-player and detached-ownership setup.
- [Phase 07-dedicated-server-authority-edge-validation]: Model admin recovery with an explicit permission-granting fake player so offline-owner authority remains server-driven without requiring a live owner entity.
- [Phase 07-dedicated-server-authority-edge-validation]: Dedicated-server reconnect tests should use a live per-call fake player entity, not the cached fake-player factory path, so same-UUID command recovery exercises the real nearby-selection code path.
- [Phase 07-dedicated-server-authority-edge-validation]: Reconnect persistence tests may reseed the transient recruit command-group state immediately before serialization when the plan is validating ownership round-trips rather than group-manager persistence.
- [Phase 08-multiplayer-authority-conflict-validation]: Live multiplayer outsider denial should be validated against a present in-level owner rather than inferred from offline-owner or detached-owner paths.
- [Phase 08-multiplayer-authority-conflict-validation]: Same-team recruit command cooperation is intentionally limited to the group-command targeting seam, while worker recovery remains owner-or-admin only even for allied players.
- [Phase 20-runtime-audit-and-bannerlord-target-architecture]: Treat the live merged runtime as recruit-led with workers absorbed through subsystem composition, not as two independent mods.
- [Phase 20-runtime-audit-and-bannerlord-target-architecture]: Carry forward only narrow save/runtime-critical workers compatibility seams during source-root retirement planning.
- [Phase 20-runtime-audit-and-bannerlord-target-architecture]: Keep the live `bannermod` mod id, shared channel, config filenames, and workers legacy-id migration helpers stable through the Phase 21 move.
- [Phase 21-source-tree-consolidation-into-bannerlord]: Phase 21 completed with one canonical production tree under `src/main/java/com/talhanation/bannermod/**` and one root-owned resource tree under `src/main/resources/**`.
- [Phase 21-source-tree-consolidation-into-bannerlord]: The only live `@Mod` entrypoint is `com.talhanation.bannermod.bootstrap.BannerModMain`, which initializes the shared `BannerModNetworkBootstrap` channel and preserves the worker packet offset contract.
- [Phase 21-source-tree-consolidation-into-bannerlord]: `recruits/` and `workers/` no longer contribute code or resources to the root build; they remain on disk only as archive/reference copies.
- [Phase 21-source-tree-consolidation-into-bannerlord]: Canonical authority, settlement, logistics, and config ownership now lives under active `bannermod` packages; temporary forwarders remain only where Phase 21 explicitly documented compatibility shims.
- [Phase 22-citizen-role-unification]: Keep the citizen seam additive and wrapper-owned; do not change live runtime ids, packet entrypoints, or screen entrypoints during the first recruit and worker conversions.
- [Phase 22-citizen-role-unification]: Route shared recruit and worker persistence through `CitizenPersistenceBridge` before widening live-path adoption beyond one recruit slice and one worker slice.
- [Phase 29-miner-excavation-recovery-and-builder-schematic-loading]: Mining behavior now uses one explicit `MiningPatternSettings` contract across UI, packets, persisted area state, and miner AI progress.
- [Phase 29-miner-excavation-recovery-and-builder-schematic-loading]: Tunnel mining must advance by planned diagonal segments and branch mining must advance by deterministic corridor/branch segments instead of generic scan-box inference.
- [Phase 29-miner-excavation-recovery-and-builder-schematic-loading]: Builder template import stays on one BuildArea `CompoundTag` contract through a unified `.nbt`/`.schem`/`.schematic` loader rather than introducing a parallel preview or build pipeline.
- [Phase 31-1-2-mining-area-branch-mine-3]: Claim worker growth evaluates BannerModSettlementBinding.Status directly. — This keeps the rules seam pure and independent from runtime world state.
- [Phase 31-1-2-mining-area-branch-mine-3]: Claim growth uses a dedicated immutable config snapshot. — The toggle, cooldown, cap, and profession pool stay explicit and testable for later runtime hooks.
- [Phase 31-1-2-mining-area-branch-mine-3]: Tunnel and branch mining now use one fixed internal segment budget instead of reusing legacy depth authoring.
- [Phase 31-1-2-mining-area-branch-mine-3]: The miner update packet and screen no longer expose z-size; only tunnel/branch settings remain player-authored.
- [Phase 31-1-2-mining-area-branch-mine-3]: Keep hostile-claim excavation checks in MiningArea scan code so MiningPatternPlanner stays pure.
- [Phase 31-1-2-mining-area-branch-mine-3]: Resolve claim status per target block with BannerModSettlementBinding.resolveFactionStatus instead of once at the work-area origin.
- [Phase 31-1-2-mining-area-branch-mine-3]: Run claim worker growth from one periodic server pass keyed by claim UUID cooldown timestamps.
- [Phase 31-1-2-mining-area-branch-mine-3]: Expose the real VillagerEvents claim-growth helper to GameTests instead of mocking spawn outcomes.
- [Phase 23]: Store heartbeat incidents and recommendations as compact token lists so later runtime and UI slices can reuse one snapshot seam.
- [Phase 24]: Keep the logistics backbone server-authoritative and service-shaped rather than introducing a settlement-wide logistics manager.
- [Phase 24]: Use authored entity-backed storage and work-area endpoints in the first slice instead of arbitrary chest discovery.
- [Phase 24]: Implement reservations as lightweight item intents with timeout and cleanup semantics, not deep slot-level locking.
- [Phase 24]: Courier task selection should be deterministic and priority-first; defer global optimization to later economy phases.
- [Phase 32]: Replace server-wide global chat with explicit faction and local channels; keep routing server-authoritative and operator-configurable.
- [Phase 33]: Unify citizen appearance and birth behind one building-capacity-aware settlement growth loop with overpopulation pressure.
- [Phase 33]: Prefer a narrow building-registration seam over a deep new settlement manager while MineColonies-style housing capacity is introduced.
- [Phase 34]: Government forms must extend the governor and authority stack instead of forking settlement control into a parallel power model.
- [Phase 35]: Clan state should be player-first, but keep NPC membership as a first-class extension seam so diplomacy and administration can converge later.
- [Phase 36]: Formation layout definitions should become datapack-authored content with strict schema validation and safe runtime fallback.
- [Phase 37]: Claimed faction territory should only permit wartime destruction through recognized siege-engine paths, not ordinary block breaking.
- [Phase 38]: Siege flow should be declaration-driven, camp-based, and center-capture-based, with hard attacker restrictions and defender build/break cooldowns during the assault window.
- [Phase 21-source-tree-consolidation-into-bannerlord]: Wave 3-8 ownership moves landed under `bannermod.{bootstrap,registry,events,commands,config,util,entity,ai,client,inventory,persistence,items,network}` with military/civilian splits preserved as package structure rather than clone boundaries. — This preserves the staged migration intent without carrying forward obsolete package-target assumptions.
- [Phase 21-source-tree-consolidation-into-bannerlord]: Treat `verifyGameTestStage` as the preferred follow-up gate for later pathfinding, persistence, and client changes that build on the consolidated tree. — Those seams still intersect the performance and correctness work from phases 11-19.
- [Phase 21-source-tree-consolidation-into-bannerlord]: Worker storage and courier code still compile against temporary BannerMod logistics compatibility types until the dedicated logistics backbone phase replaces them.
- [Phase 21-source-tree-consolidation-into-bannerlord]: The root build now vendors active Java, unit-test, GameTest, and resource inputs under `src/**`, and `build.gradle` no longer composes code or tests directly from `recruits/` or `workers/` source roots.
- [Phase 21-source-tree-consolidation-into-bannerlord]: Vendor remaining recruit and worker resources/tests into tracked root src trees so source-root retirement no longer depends on untracked nested repos.
- [Phase 21-source-tree-consolidation-into-bannerlord]: Keep recruits/ and workers/ on disk as archive-only reference copies until a later cleanup slice removes them without changing build truth.
- [Phase 21-source-tree-consolidation-into-bannerlord]: Do not claim Phase 21 complete until compileJava, test, and verifyGameTestStage run green from the retired root-only layout.
- [Phase 21-source-tree-consolidation-into-bannerlord]: Canonical shared seam ownership now lives under `com.talhanation.bannermod.shared/**` and `com.talhanation.bannermod.config`, while the old `com.talhanation.bannermod.{authority,settlement,logistics}` peers remain deprecated forwarders only where documented.
- [Phase 21-source-tree-consolidation-into-bannerlord]: The canonical settlement seam still accepts retained recruits claims alongside moved claim data so callers can migrate without splitting the rules logic again.
- [Phase 21-source-tree-consolidation-into-bannerlord]: Legacy logistics route accessor aliases stay in place where needed so existing UI and packet code keeps its contract stable.
- [Phase 21]: 21-02 narrowed Option A scope: only the 5 classes with extant legacy bannermod implementations were moved to bannermod.shared.*. Service/Route/CourierTask deferred -- no impl exists today, no callers, depends on AbstractWorkerEntity from wave 21-04.
- [Phase 21]: 21-02 forwarder lifespan: per D-05 the legacy bannermod.{authority,settlement,logistics} peers stay live as @Deprecated forwarders for the duration of Phase 21; deletion is owned by a separate post-Phase-21 cleanup phase.
- [Phase 21-source-tree-consolidation-into-bannerlord]: workerPacketOffset() now returns MILITARY_MESSAGES.length (compile-time=104), making must-have #4 from 21-03 provable
- [Phase 21-source-tree-consolidation-into-bannerlord]: Wave 9 closed Phase 21: outer src/main/resources owns shipped assets, build.gradle composes only outer src/{main,test,gametest}, ./gradlew compileJava is green, embedded recruits/ and workers/ clones retained on disk untracked (Option a).
- [Phase 23]: Persist governor state by claim UUID in one narrow SavedData manager instead of mutating claims or introducing a settlement manager.
- [Phase 23]: Keep legality in one pure rules helper driven by claim-derived settlement binding states before any runtime service or UI wiring.
- [Phase 23]: Governor assignment stays attached to an existing recruit UUID and owner UUID instead of introducing a GovernorEntity.
- [Phase 23]: Governor authority reuses shared owner/admin relationship rules and adds friendly-claim legality instead of forking a governance-only permission model.
- [Phase 23]: Runtime designation and revocation mutate BannerModGovernorSnapshot records in BannerModGovernorManager rather than storing governor state on live claims.
- [Phase 23]: Keep governor report payloads token-backed via enums so later snapshots and UI use one bounded vocabulary instead of free-form strings.
- [Phase 23]: Run claim heartbeat work only on server tick END phase with an existing coarse cadence to avoid double-firing and scope creep into per-tick simulation.
- [Phase 23-settlement-governance-and-governor-control]: Persist the three governor policy toggles on BannerModGovernorSnapshot so UI changes stay bounded and server authoritative.
- [Phase 23-settlement-governance-and-governor-control]: Keep governor promotion on MessagePromoteRecruit and branch profession id 6 into designation instead of adding a second entrypoint.

## Accumulated Context

### Roadmap Evolution

- Phase 29 added: Майнер не прокапывается к шахте, а просто статично стоит. 1. Добавь майнеру задачу копать тоннель 3х3 (или произвольной ширины) вниз по диагонали (ни в коем случае не под себя) 2. Добавь майнеру задачу копать по тактике branch mining (strip mining) заданной высоты 3. Добавь загрузку схематик билдерам. Задача высочайшего приоритета, сдвинь фазы 21-26 вправо, а это впихни прямо сейчас
- Phase 30 added: Worker Birth And Claim-Based Settlement Spawn
- Phase 31 added: Добавляем приоритетную новую фазу. 1. Клеймы считаются поселениями 2. Mining Area майнера - убрать, оставить чисто настройки копания тоннеля по диагонали вниз и branch mine. Настроек как далеко можно копать можно не делать, но сделать так чтобы не пытался всё время сломать блок чужого клейма, если рядом с клеймом чужим к нему не копает (копать вне клейма майнер может) 3. Делаем генерацию воркеров в клеймах, но считаем сколько уже есть, и по убывающей прогрессии добавляем новых.
- Phase 32 added: Чат фракции и локала, глобал вырубить.
- Phase 33 added: Исправить рождаемость воркеров: система регистраций зданий, появление citizens как в MineColonies, рождаемость и давление перенаселения.
- Phase 34 added: Внедрить различные формы правления и формы устройства государства с административным вопросом.
- Phase 35 added: Внедрить систему кланов для игроков и при необходимости NPC.
- Phase 36 added: Переработать формации на datapack-driven.
- Phase 37 added: Отключить поломку блоков во фракциях всем кроме осаждающих, и только осадными орудиями.
- Phase 38 added: Исправить осады на манер SiegeWar: осадный лагерь, 48 часов до старта, центральный чанк, осадные орудия, ограничения интерактов и кулдаун защитников.

## Performance Metrics

| Phase | Plan | Duration | Tasks | Files |
| ----- | ---- | -------- | ----- | ----- |
| 01-workspace-bootstrap | 01 | 1 min | 2 | 4 |
| Phase 01-workspace-bootstrap P02 | 11 min | 2 tasks | 4 files |
| Phase 02-runtime-unification-design P01 | 12 min | 2 tasks | 4 files |
| Phase 02-runtime-unification-design P02 | 9 min | 2 tasks | 4 files |
| Phase 05-stabilization-and-cleanup P01 | 8 min | 1 tasks | 1 files |
| Phase 05-stabilization-and-cleanup P02 | 6 min | 2 tasks | 3 files |
| Phase 05-stabilization-and-cleanup P03 | 6 min | 2 tasks | 4 files |
| Phase 05-stabilization-and-cleanup P04 | 4 min | 2 tasks | 6 files |
| Phase 06-player-cycle-gametest-validation P01 | 5 min | 2 tasks | 3 files |
| Phase 06-player-cycle-gametest-validation P02 | 5 min | 1 tasks | 2 files |
| Phase 06-player-cycle-gametest-validation P03 | 2 min | 1 tasks | 1 files |
| Phase 06-player-cycle-gametest-validation P04 | 2 min | 1 tasks | 1 files |
| Phase 07-dedicated-server-authority-edge-validation P01 | 4 min | 2 tasks | 2 files |
| Phase 07-dedicated-server-authority-edge-validation P02 | 56 min | 1 tasks | 2 files |
| Phase 12-global-pathfinding-control P01 | not recorded | 2 tasks | 5 files |
| Phase 12-global-pathfinding-control P02 | not recorded | 2 tasks | 5 files |
| Phase 13-path-reuse-for-cohesion-movement P01 | not recorded | 2 tasks | 3 files |
| Phase 13-path-reuse-for-cohesion-movement P02 | not recorded | 2 tasks | 4 files |
| Phase 14-formation-level-target-selection-rewrite P01 | not recorded | 2 tasks | 4 files |
| Phase 14-formation-level-target-selection-rewrite P02 | not recorded | 2 tasks | 7 files |
| Phase 14-formation-level-target-selection-rewrite P03 | not recorded | 2 tasks | 5 files |
| Phase 14-formation-level-target-selection-rewrite P04 | not recorded | 2 tasks | 1 files |
| Phase 20-runtime-audit-and-bannerlord-target-architecture P01 | 7 min | 2 tasks | 3 files |
| Phase 20-runtime-audit-and-bannerlord-target-architecture P02 | 4 min | 2 tasks | 4 files |
| Phase 29-miner-excavation-recovery-and-builder-schematic-loading P01 | not recorded | 2 tasks | 6 files |
| Phase 29-miner-excavation-recovery-and-builder-schematic-loading P02 | not recorded | 2 tasks | 4 files |
| Phase 29-miner-excavation-recovery-and-builder-schematic-loading P03 | not recorded | 2 tasks | 4 files |
| Phase 29-miner-excavation-recovery-and-builder-schematic-loading P04 | not recorded | 2 tasks | 5 files |
| Phase 31-1-2-mining-area-branch-mine-3 P01 | 3 min | 2 tasks | 3 files |
| Phase 31-1-2-mining-area-branch-mine-3 P03 | 14 min | 2 tasks | 5 files |
| Phase 31-1-2-mining-area-branch-mine-3 P04 | 3 min | 2 tasks | 3 files |
| Phase 31-1-2-mining-area-branch-mine-3 P02 | 10 min | 2 tasks | 4 files |
| Phase 22-citizen-role-unification P01 | not recorded | 2 tasks | 4 files |
| Phase 22-citizen-role-unification P02 | not recorded | 2 tasks | 6 files |
| Phase 22-citizen-role-unification P03 | not recorded | 2 tasks | 4 files |
| Phase 22-citizen-role-unification P04 | not recorded | 2 tasks | 4 files |
| Phase 23-settlement-governance-and-governor-control P01 | not recorded | 2 tasks | 4 files |
| Phase 21-source-tree-consolidation-into-bannerlord P02 | 18 min | 2 tasks | 18 files |
| Phase 21-source-tree-consolidation-into-bannerlord P03 | 1 min | 2 tasks | 235 files |
| Phase 21-source-tree-consolidation-into-bannerlord P04 | 1 min | 2 tasks | 122 files |
| Phase 21-source-tree-consolidation-into-bannerlord P05 | 4 min | 3 tasks | 861 files |
| Phase 21-source-tree-consolidation-into-bannerlord P06 | 1 min | 2 tasks | 47 files |
| Phase 21 P02 | 25min | 4 tasks | 29 files |
| Phase 21-source-tree-consolidation-into-bannerlord P08 | 10 min | 2 tasks | 133 files |
| Phase 21-source-tree-consolidation-into-bannerlord P09 | 25 min | 2 tasks | 313 files |
| Phase 23-settlement-governance-and-governor-control P01 | 2 min | 2 tasks | 4 files |
| Phase 23-settlement-governance-and-governor-control P02 | 13 min | 2 tasks | 4 files |
| Phase 23-settlement-governance-and-governor-control P03 | 7 min | 2 tasks | 5 files |
| Phase 23-settlement-governance-and-governor-control P04 | 8 min | 2 tasks | 11 files |

### Quick Tasks Completed

| # | Description | Date | Commit | Directory |
|---|-------------|------|--------|-----------|
| 260413-a9x | Убедись, что claims рекрутов защищают от грифа (лавакаст, кража из сундуков, джамплаг) | 2026-04-13 | 5b807e7 | [260413-a9x-claims](./quick/260413-a9x-claims/) |

## Session

- Last updated: 2026-04-15T16:18:36Z
- Stopped at: Completed 23-04-PLAN.md
- Resume file: None
