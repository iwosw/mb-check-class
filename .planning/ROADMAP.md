# Merge Roadmap

## Phase 1: Workspace Bootstrap

- Establish one root Gradle entrypoint.
- Archive legacy planning trees.
- Create merge documentation and the active root `.planning/` context.

**Goal:** Root workspace bootstrap is explicit and executable: one root Gradle entrypoint, one active root planning context, preserved legacy archives, and documented merge truth/verification rules.

**Plans:** 2 plans

Plans:
- [x] 01-01-PLAN.md — Harden the single root build/workspace entrypoint and root source-of-truth docs.
- [x] 01-02-PLAN.md — Lock the verification baseline, merge conflict notes, and explicit bootstrap roadmap/state entries.

Planning artifacts live under `.planning/phases/01-workspace-bootstrap/` so Phase 1 bootstrap work stays explicit and reviewable.

Status: Complete (2/2 plans complete as of 2026-04-11)

## Phase 2: Runtime Unification Design

- Decide final mod id, artifact identity, and metadata strategy.
- Define namespace migration rules for `workers` assets, lang keys, and registry ids.
- Define packet, config, and save-data compatibility strategy.

**Goal:** BannerMod runtime-unification target is explicit and actionable: release identity, asset/lang namespace end-state, merged-runtime legacy-state boundary, and BannerMod-owned config direction are documented against current code seams.

**Requirements:** [BOOT-05]

**Plans:** 2 plans

Plans:
- [x] 02-01-PLAN.md — Publish the BannerMod-first release identity and `bannermod` namespace end-state.
- [x] 02-02-PLAN.md — Define the merged-runtime compatibility boundary and BannerMod-owned config direction.

Planning artifacts live under `.planning/phases/02-runtime-unification-design/` so the design contracts remain explicit before implementation phases consume them.

Status: Complete (2/2 plans complete as of 2026-04-11); runtime identity, bannermod namespace destination, compatibility boundary, and BannerMod-owned config direction are all explicit.

## Phase 3: Workers Subsystem Absorption

- Move worker entities and work areas behind the root runtime entrypoint.
- Fold `workers` registries, menus, packets, and client screens into the unified mod.
- Replace hard imports from `com.talhanation.recruits.*` with same-project integration seams where needed.

Status: Complete in practice; workers runtime ownership is already absorbed into the merged BannerMod runtime, with remaining cleanup and smoke validation tracked under Phase 5.

## Phase 4: Resource and Data Consolidation

- Merge assets and data paths under the final namespace strategy.
- Resolve duplicate GUI/resource names.
- Merge access transformers, mixin configs, and pack metadata.

Status: Complete in practice; active runtime resources, pack wiring, and workers registry-coupled namespaces now route through `bannermod`, while preserved `workers` assets/resources remain cleanup-only follow-up.

## Phase 5: Stabilization and Cleanup

- Remove transitional adapters that are no longer needed.
- Re-run tests and smoke validation from the unified root.
- Retire legacy source trees once all runtime ownership has moved.

**Goal:** Stabilization work turns the merged BannerMod runtime into a lower-risk root baseline: retained Workers regressions run from the active root workspace, high-risk merged-runtime mutation and legacy-listener seams are fenced or retired without violating the Phase 02 compatibility contract, and root docs tell the cleanup truth.

**Requirements:** [STAB-01, STAB-02, STAB-03, STAB-04]

**Plans:** 2 plans

Plans:
- [x] 05-01-PLAN.md — Expand the root regression pipeline to execute retained Workers JUnit suites.
- [x] 05-02-PLAN.md — Harden build-area update authoring so merged worker edits honor the existing access rules.
- [x] 05-03-PLAN.md — Retire live registration of duplicated legacy update-check listeners in the merged runtime.
- [x] 05-04-PLAN.md — Refresh roadmap/state/readiness docs so the stabilization baseline and remaining cleanup boundary stay truthful.

Planning artifacts live under `.planning/phases/05-stabilization-and-cleanup/` so stabilization work stays explicit before execution.

Status: Complete (4/4 plans complete as of 2026-04-11).

- Latest slice: workers registry-layer namespace unified under `bannermod`, root compile/resources/test revalidated, and legacy structure entity ids kept readable via migration fallback.
- Latest slice: added focused legacy `workers:*` compatibility hooks for world/registry remaps plus structure-scan/build NBT migration, then revalidated root compile/processResources/test.
- Latest slice: documented root verification entrypoints and merged codebase source-of-truth paths, and added lightweight Workers smoke/regression tests for merged runtime helpers and builder progress.
- Latest slice: routed retained Workers JUnit suites through the root `test` source set so merged regression coverage now runs from `./gradlew test`.
- Latest slice: fenced build-area mutation behind shared authoring rules and disabled duplicate legacy update-check listeners while keeping the Phase 02 compatibility boundary narrow.
- Latest slice: published the shared BannerMod system vocabulary in active planning docs and added a root smoke test that asserts the recruit-owned `bannermod` runtime and worker subsystem network seam stay aligned.
- Latest slice: introduced a shared BannerMod authority contract so worker authoring and worker-control recovery now use the same owner, same-team, admin, and forbidden relationship vocabulary without widening permissions.
- Latest slice: extended the shared BannerMod supply-status seam so recruit upkeep now joins build-area and worker-storage pressure in one passive logistics vocabulary for low-risk settlement-to-military coupling.
- Latest slice: unified the bootstrap onto BannerMod-owned `military`, `settlement`, and `client` config file surfaces, with explicit one-way migration from `bannermod-server.toml` and `workers-server.toml` only when the new target files do not yet exist.
- Latest slice: dedicated-server authority and reconnect validation now extend the root GameTest suite, and `verifyGameTestStage` is currently green with 32 required tests.

## Phase 6: Player-Cycle GameTest Validation

- Validate the integrated BannerMod gameplay loop as one player-facing system through root GameTests.
- Extend root GameTest coverage from isolated subsystem seams into small end-to-end player-cycle slices.
- Prefer additive validation rooted in `src/gametest/java/com/talhanation/bannermod/` unless a test exposes a real runtime defect.

**Goal:** Validate the integrated BannerMod gameplay loop as one player-facing system through root GameTests: prove player-owned recruit and worker coexistence, settlement labor participation, settlement-to-military upkeep flow, and a stitched authority-safe gameplay cycle under one merged runtime.

**Plans:** 2 plans

Plans:
- [x] 06-01-PLAN.md — Validate that one player can own recruit and worker actors in the same BannerMod runtime while shared ownership boundaries stay correct.
- [x] 06-02-PLAN.md — Validate that an owned worker can bind to settlement infrastructure and participate in a valid work area under the same authority rules.
- [x] 06-03-PLAN.md — Validate that settlement-side supply can satisfy recruit upkeep and readiness through a live runtime transition.
- [x] 06-04-PLAN.md — Validate the stitched full BannerMod player cycle with authority and recovery expectations in one root GameTest flow.

Planning artifacts live under `.planning/phases/06-player-cycle-gametest-validation/` so player-cycle validation work stays explicit and reviewable.

Status: Complete (4/4 plans complete as of 2026-04-11).

## Phase 7: Dedicated-Server Authority Edge Validation

- Validate merged BannerMod ownership and recovery behavior under dedicated-server execution, not only local single-runtime assumptions.
- Cover edge cases where the owner player is offline, unresolved, or reconnecting while recruit, worker, and work-area authority must remain deterministic.
- Keep the slice validation-first and rooted in root GameTests or narrow regressions unless a test exposes a real server-authority defect.

**Goal:** Prove the merged BannerMod authority model survives dedicated-server edge conditions: owner resolution can disappear and return without widening permissions, and recruit-plus-worker control remains server-authoritative when no integrated local-player assumptions exist.

**Requirements:** [DSAUTH-01, DSAUTH-02]

**Plans:** 2 plans

Plans:
- [x] 07-01-PLAN.md — Validate dedicated-server owner-offline and unresolved-owner authority denial for recruit and worker recovery flows.
- [x] 07-02-PLAN.md — Validate dedicated-server reconnect and persistence-safe ownership recovery across recruit, worker, and work-area state.

Planning artifacts live under `.planning/phases/07-dedicated-server-authority-edge-validation/` so dedicated-server follow-up stays explicit before implementation.

Status: Complete (2/2 plans complete as of 2026-04-11); `compileGameTestJava` and `verifyGameTestStage` now both pass, with all 32 required GameTests green (historical snapshot at phase close; current suite is 37 per STATE.md).

## Phase 8: Multiplayer Authority Conflict Validation

- Validate contested multiplayer interactions with at least two distinct players in the same merged runtime.
- Prove same-team cooperation and outsider denial across recruit control, worker control, and settlement authoring seams.
- Keep slices narrow and deterministic so later plans can extend multiplayer coverage without broad scenario rewrites.

**Goal:** Prove BannerMod's shared authority contract remains correct under true multiplayer contention: cooperative players can use the intended same-team paths, outsiders stay denied, and server-owned state does not silently drift during concurrent recruit and settlement interactions.

**Plans:** 2 plans

Plans:
- [x] 08-01-PLAN.md — Validate contested multiplayer authority on shared recruit, worker, and work-area interactions with distinct owner and outsider players.
- [x] 08-02-PLAN.md — Validate same-team multiplayer cooperation paths without reopening outsider settlement or control access.

Planning artifacts live under `.planning/phases/08-multiplayer-authority-conflict-validation/` so multiplayer-specific validation remains reviewable and separate from dedicated-server edge coverage.

Status: Complete (2/2 plans complete as of 2026-04-11); `verifyGameTestStage` now passes with all 36 required GameTests green, including live multiplayer outsider-denial and same-team cooperation coverage (historical snapshot at phase close; current suite is 37 per STATE.md).

## Phase 9: Settlement-Faction Binding Contract

- Make settlement-to-faction binding explicit as a first-class BannerMod gameplay contract rather than only an architectural implication.
- Keep the implementation boundary low-risk by preferring derived settlement-plus-claim rules over a new persistence manager unless the code proves one is necessary.
- Define how settlement legality, friendly use, and faction mismatch should be interpreted by worker and military systems.

**Goal:** BannerMod gains an explicit settlement-faction contract: settlements are faction-aligned operational footprints derived from claims plus active infrastructure, and that contract becomes actionable for authority, legality, and later logistics work without forcing an immediate deep persistence rewrite.

**Plans:** 2 plans

Plans:
- [x] 09-01-PLAN.md — Publish the explicit settlement-faction binding rules and lifecycle vocabulary for the merged runtime.
- [x] 09-02-PLAN.md — Add the smallest runtime seam needed so faction-aware settlement legality and status can be queried consistently.

Planning artifacts live under `.planning/phases/09-settlement-faction-binding-contract/` so the new gameplay contract is explicit before validation and downstream implementation slices consume it.

Status: Complete (2/2 plans complete as of 2026-04-11); settlement-faction lifecycle vocabulary is now explicit in active planning docs, and the merged runtime exposes one shared settlement-binding status seam for placement, client legality hints, and work-area participation.

## Phase 10: Settlement-Faction Enforcement Validation

- Validate the new settlement-faction contract through focused runtime tests instead of broad settlement rewrites.
- Cover legal friendly binding, hostile or unclaimed denial, and claim-loss or faction-mismatch degradation behavior.
- Keep the slices small so later `/gsd-plan-phase` work can execute them independently.

**Goal:** Prove the first-class settlement-faction contract behaves in live BannerMod validation: friendly claims permit settlement participation, hostile or missing faction authority blocks it, and claim/faction loss degrades civilian throughput before any silent transfer can occur.

**Plans:** 2 plans

Plans:
- [x] 10-01-PLAN.md — Validate friendly-claim settlement binding and hostile or unclaimed denial in root GameTests.
- [x] 10-02-PLAN.md — Validate settlement degradation on claim loss or faction mismatch without silent ownership transfer.

Planning artifacts live under `.planning/phases/10-settlement-faction-enforcement-validation/` so faction-binding validation stays isolated from the contract-definition phase.

Status: Complete (2/2 plans complete as of 2026-04-12); root GameTests now prove friendly settlement placement and operation, hostile or unclaimed denial, and claim-loss or faction-mismatch degradation without silent ownership transfer (45-required-test count at phase close is a historical snapshot; current suite is 37 per STATE.md).

## Phase 11: Large-Battle AI Profiling Baseline

- Establish a reproducible profiling baseline for large-battle AI and pathfinding cost before changing runtime behavior.
- Make benchmark scenarios, counters, and pass/fail evidence explicit so later optimization slices can be measured honestly.
- Keep the slice instrumentation-first unless profiling exposes an immediately obvious defect that must be fenced.

**Goal:** BannerMod has a shared large-battle performance baseline: the hot paths, measurement scenarios, and profiling outputs for recruit-heavy combat are documented and repeatable before optimization work starts.

**Plans:** 2 plans

Plans:
- [x] 11-01-PLAN.md — Define the large-battle profiling scenarios, counters, and evidence format for AI and pathfinding work.
- [x] 11-02-PLAN.md — Add the smallest instrumentation or harness seams needed to capture baseline measurements without changing AI behavior.

Planning artifacts live under `.planning/phases/11-large-battle-ai-profiling-baseline/` so performance work starts from explicit evidence instead of anecdotal lag reports.

Status: Complete (2/2 plans complete as of 2026-04-11); the dense-battle baseline now has canonical profiling docs, resettable target-search and async-path counters, and GameTest snapshot capture wired into the live stress harness.

## Phase 12: Global Pathfinding Control

- Introduce a global control seam for pathfinding issuance so large groups stop acting like independent unbounded callers.
- Prefer one shared scheduler or budget boundary over scattered local guards.
- Validate that the new control point preserves current behavior in small battles before later optimization layers depend on it.

**Goal:** Path requests now flow through one explicit BannerMod control seam, giving later reuse, throttling, and async fixes a stable runtime boundary instead of entity-by-entity ad hoc pathfinding.

**Plans:** 2 plans

Plans:
- [x] 12-01-PLAN.md — Add the shared global pathfinding control seam and route the current high-volume callers through it.
- [x] 12-02-PLAN.md — Validate correctness and capture before/after profiling for the new global control boundary.

Planning artifacts live under `.planning/phases/12-global-pathfinding-control/` so the first optimization seam is explicit before more invasive AI changes land.

Status: Partial (as of 2026-04-19 audit + restoration wave) — `GlobalPathfindingController` seam class and `GlobalPathfindingControllerTest` live again under active `com.talhanation.bannermod.ai.pathfinding.*` (commits `accf542`, `ad812b7`). The seam was lost in the bannerlord→bannermod pivot `cc178aa` and never migrated until the 2026-04-19 restoration wave; the 2026-04-11 "Complete" claim was paper-only. `AsyncPathNavigation` integration (wrapping `createPath(...)` through `GlobalPathfindingController.requestPath(...)`) is the remaining follow-up slice: current active `AsyncPathNavigation.java` still calls `this.pathFinder.findPath` directly. Controller-aware GameTest profiling snapshots remain deferred.

## Phase 13: Path Reuse For Cohesion Movement

- Reuse compatible paths where nearby or formation-linked actors are currently recomputing equivalent navigation work.
- Keep the reuse rules narrow and observable so invalid-path churn does not silently increase.
- Profile reuse hit rate and path invalidation behavior as part of the phase, not as follow-up.

**Goal:** BannerMod reduces duplicate path computation in group movement by reusing valid navigation work where actors share compatible movement intent, with explicit profiling to prove the reuse is helping.

**Requirements:** [PATHREUSE-01, PATHREUSE-02]

**Plans:** 2 plans

Plans:
- [x] 13-01-PLAN.md — Implement the smallest safe path-reuse seam for nearby or formation-cohesive movement.
- [x] 13-02-PLAN.md — Validate path correctness and profile reuse hit rate, invalidation churn, and large-battle impact.

Planning artifacts live under `.planning/phases/13-path-reuse-for-cohesion-movement/` so path reuse remains a distinct, measurable slice.

Status: Partial (as of 2026-04-19 restoration wave) — Reuse logic lives inside the restored `GlobalPathfindingController` (commit `accf542`): `tryReuse()` with `NO_CANDIDATE` / `NULL_CANDIDATE` / `UNPROCESSED_CANDIDATE` / `DONE_CANDIDATE` / `INCOMPATIBLE_CANDIDATE` / `STALE_CANDIDATE` drop counters, `copyPath(...)` guaranteeing reused paths are copied before return (no shared mutable `Path` references). Contract locked by `GlobalPathfindingControllerTest`. The 2026-04-11 "Complete" claim was paper-only; code was absent until restoration. Runtime wiring of reuse into `AsyncPathNavigation` is gated on the Phase 12 AsyncPathNavigation integration follow-up.

## Phase 14: Formation-Level Target Selection Rewrite

- Rewrite target selection around formation-level or squad-level intent so large groups stop paying full per-entity acquisition cost every cycle.
- Keep the rewrite bounded to selection and assignment rather than bundling navigation or combat-behavior rewrites into the same phase.
- Make correctness validation explicit for focus fire, retargeting, and loss-of-target behavior.

**Goal:** Large-group combat target acquisition becomes formation-aware instead of purely per-entity, reducing repeated target-search work while keeping combat intent understandable and testable.

**Requirements:** [TARGETSEL-01, TARGETSEL-02]

**Plans:** 4 plans

Plans:
- [x] 14-01-PLAN.md — Replace the highest-cost per-entity target acquisition path with a formation-level selection and assignment seam.
- [x] 14-02-PLAN.md — Validate combat behavior and profile target-selection cost after the formation rewrite.
- [x] 14-03-PLAN.md — Close the remaining retarget and loss-of-target GameTest gaps without widening the rewrite or reopening unrelated reconnect work.
- [x] 14-04-PLAN.md — Apply the smallest live-candidate filtering and brownfield harness fix needed to turn the remaining optional retarget/loss-of-target checks green.

Planning artifacts live under `.planning/phases/14-formation-level-target-selection-rewrite/` so targeting work stays isolated from later movement-budget changes.

Status: Complete (4/4 plans complete as of 2026-04-12); formation-aware target selection now reports dense-battle profiling counters, the optional retarget/loss-of-target checks are green on the retained recovery-field harness, and `verifyGameTestStage` passes with all 45 required tests green (historical snapshot at phase close; current suite is 37 per STATE.md).

## Phase 15: Pathfinding Throttling And Budgeting

- Add explicit throttling and per-tick budgeting once global control, reuse, and formation targeting are in place.
- Prefer deterministic request deferral over hidden random slowdown so validation remains readable.
- Measure queue depth, deferred work, and recovery time under load.

**Goal:** BannerMod can cap and defer pathfinding work during large battles with an explicit, measurable budget instead of allowing bursty navigation spikes to dominate server ticks.

**Plans:** 1 retroactive summary + 1 deferred validation

Plans:
- [x] 15-01-SUMMARY (retroactive 2026-04-19) — Per-tick budget + deferred-backlog accounting lives inside `GlobalPathfindingController` with `PathfindingRequestBudgetPerTick` / `PathfindingMaxDeferredBacklog` / `PathfindingMaxDeferredTicks` config and explicit `BACKLOG_CAP` / `MAX_AGE` / `INVALIDATED` drop reasons. Evidence: `.planning/phases/15-pathfinding-throttling-and-budgeting/15-pathfinding-throttling-and-budgeting-01-SUMMARY.md`, controller source under `bannermod.ai.pathfinding.*`, JUnit contract in `GlobalPathfindingControllerTest`.
- [ ] 15-02-PLAN (not written, not executed) — Dense-battle GameTest validation capturing queue/defer/drop counters under load remains deferred; controller counters are ready for capture but no evidence bundle has been produced.

Planning artifacts live under `.planning/phases/15-pathfinding-throttling-and-budgeting/` so budget control stays separate from targeting and async remediation work.

Status: Partial — budget seam restored in active `bannermod.ai.pathfinding.GlobalPathfindingController` (commit `accf542`, 2026-04-19) together with the Phase 12/13 counters; empirical load-validation capture deferred. Historical 2026-04-12 "Complete (2/2 plans complete)" claim was paper-only (original controller was lost in the bannerlord→bannermod pivot `cc178aa` and never migrated into active `src/**` until this restoration).

## Phase 16: Async Pathfinding Reliability Fixes

- Fix the known async-pathfinding safety and correctness issues only after the synchronous control surfaces are explicit.
- Keep the scope on correctness, race prevention, stale-result handling, and safe handoff boundaries.
- Require validation that async gains do not reintroduce authority drift, stuck entities, or invalid path application.

**Goal:** Async pathfinding becomes predictable and safe under the new global control model: stale or unsafe results are fenced, handoff points are explicit, and large-battle path work no longer depends on fragile concurrency assumptions.

**Plans:** 1 retroactive summary + 1 deferred validation

Plans:
- [x] 16-01-SUMMARY (retroactive 2026-04-19) — `AsyncPathProcessor` now carries the Phase 16 hardening: 4-arg `deliverProcessedPath(..., BooleanSupplier shouldDeliver, ...)` and 4-arg `awaitProcessing(..., BooleanSupplier, ...)` drop stale callbacks explicitly, with `ProfilingSnapshot` exposing `queueSubmissions` / `syncFallbacks` / `awaitCalls` / `deliveredPaths` / `deliveredNullPaths` / `droppedCallbacks`. Evidence: `.planning/phases/16-async-pathfinding-reliability-fixes/16-async-pathfinding-reliability-fixes-01-SUMMARY.md`, source `bannermod.ai.pathfinding.AsyncPathProcessor`, JUnit `AsyncPathProcessorTest` (restored 2026-04-19).
- [ ] 16-02-PLAN (not written, not executed) — Battle-GameTest delivered-vs-dropped accounting under load remains deferred; counters are wired but no evidence capture ran.

Planning artifacts live under `.planning/phases/16-async-pathfinding-reliability-fixes/` so concurrency fixes remain a focused slice after earlier control work.

Status: Partial — async handoff hardening + stale-result guard + profiling counters restored in active `bannermod.ai.pathfinding.AsyncPathProcessor` (commit `a5a4b0f`, 2026-04-19); empirical validation capture deferred. Historical 2026-04-12 "Complete (2/2 plans complete)" claim was paper-only (the 4-arg delivery path and validator were dropped in the bannerlord→bannermod pivot `cc178aa`; active `AsyncPathProcessor` had been stripped to a 3-arg delivery without counters until this restoration).

## Phase 17: AI LOD And Tick Shedding

- Introduce AI level-of-detail rules after the pathing and targeting hot paths are already bounded.
- Degrade update frequency or decision richness based on distance, relevance, or visibility without breaking authority or battle readability.
- Make profiling and behavior validation explicit so LOD stays a controlled tradeoff rather than a hidden nerf.

**Goal:** BannerMod can shed non-critical AI work under scale using explicit LOD rules, reducing background decision cost while preserving important combat and ownership behavior close to the player.

**Plans:** 2 plans

Plans:
- [x] 17-01-PLAN.md — Add the smallest explicit AI LOD rules for low-priority actors and decision loops in large battles.
- [x] 17-02-PLAN.md — Validate gameplay behavior and profile tick-cost reduction from the new LOD layer.

Planning artifacts live under `.planning/phases/17-ai-lod-and-tick-shedding/` so AI degradation policy remains explicit and reviewable.

Status: Complete (2/2 plans complete as of 2026-04-12); recruit target search now uses one explicit AI LOD policy with operator-visible cadence knobs, retained dense-battle and mixed-squad GameTests publish LOD skip and tier counters beside earlier profiling seams, and `verifyGameTestStage` remains green as the correctness gate for Phase 17 evidence.

## Phase 18: Optional Flow-Field Navigation Evaluation

- Treat flow-field navigation as optional, benchmark-gated follow-up rather than a mandatory rewrite.
- Start with feasibility, boundary conditions, and a guarded prototype in the largest movement cases that still justify the added complexity.
- Require side-by-side profiling against the prior phases before expanding the approach.

**Goal:** BannerMod has an explicit decision on whether flow-field navigation is worth carrying: the idea is either proven by focused prototype evidence or rejected without destabilizing the core roadmap.

**Plans:** 4 plans

Plans:
- [x] 18-01-PLAN.md — Define the narrow movement scenarios where an optional flow-field prototype is allowed and how it will be isolated.
- [x] 18-02-PLAN.md — Build the guarded prototype or spike and benchmark it against the existing pathfinding stack.
- [x] 18-03-PLAN.md — Add one same-destination benchmark path, capture side-by-side evidence, and publish the final keep-or-drop decision.
- [x] 18-04-PLAN.md — Refresh roadmap, state, and retained summaries so they match the completed Phase 18 outcome.

Planning artifacts live under `.planning/phases/18-optional-flow-field-navigation-evaluation/` so optional navigation research does not blur into the required optimization backlog.

Status: Complete (4/4 plans complete as of 2026-04-12); the optional prototype was exercised on `same_destination_flow_field_lane`, but all 51 prototype attempts fell back with zero hits, so Phase 18 closes with `drop` in `18-DECISION.md`.

## Phase 19: Large-Battle Performance Validation

- Re-run the full large-battle profiling matrix after the earlier AI/pathfinding slices land.
- Compare results against the Phase 11 baseline and document remaining hotspots honestly.
- Keep the phase evidence-first so future performance work can branch from measured residual problems instead of assumptions.

**Goal:** BannerMod closes the current AI/performance optimization proposal with explicit end-to-end validation: the large-battle baseline is rerun, gains are documented against the original measurements, and the remaining performance debt is visible.

**Plans:** 2 plans

Plans:
- [x] 19-01-PLAN.md — Re-run the agreed large-battle profiling scenarios against the optimized runtime and capture comparable evidence.
- [x] 19-02-PLAN.md — Publish the before/after analysis, residual hotspots, and recommended next performance backlog based on measured results.

Planning artifacts live under `.planning/phases/19-large-battle-performance-validation/` so the optimization proposal ends with explicit proof rather than only implementation slices.

Status: Partial — validation doc written; empirical profiling evidence deferred; no `evidence/` bundles captured (re-audited 2026-04-19). The retained required GameTest gate (`verifyGameTestStage`) still runs green and remains the correctness gate, but the original closeout narrative overstated completeness: no `.planning/phases/19-large-battle-performance-validation/evidence/` bundles exist in the active tree, and no `19-RESULTS.md` has been published. `19-VALIDATION.md` now records the truthful status and the deferred follow-up slice required to promote Phase 19 back to Complete.

## Phase 20: Runtime Audit And Bannerlord Target Architecture

- Audit what still lives only in `recruits/`, `workers/`, or root-side jars.
- Map entity, AI, registry, networking, storage, settlement, and config ownership that must survive the physical source-tree merge.
- Phase 20 originally investigated a `bannerlord` destination, but the realized Phase 21 convergence target is the active `src/main/java/com/talhanation/bannermod/**` tree.

**Goal:** Turn the current future-feature wishlist into a code-backed baseline with explicit migration, compatibility, and package-move boundaries.

**Plans:** 2/2 plans complete

Plans:
- [x] 20-01-PLAN.md — Audit the active runtime ownership surfaces and publish the source-root, package-family, and jar-reference matrix that must survive consolidation.
- [x] 20-02-PLAN.md — Publish the Bannerlord target architecture, package-move map, compatibility boundary, and migration risk register for Phase 21 execution.

Planning artifacts live under `.planning/phases/20-runtime-audit-and-bannerlord-target-architecture/`.

Status: Complete (2/2 plans complete as of 2026-04-12); runtime ownership is audited in `20-RUNTIME-AUDIT.md`, the move-order matrix is published in `20-OWNERSHIP-MATRIX.md`, and the Phase 21 handoff contract now lives in `20-TARGET-ARCHITECTURE.md`.

## Phase 21: Source Tree Consolidation Into BannerMod

- Move active Java ownership out of `recruits/` and `workers/` into `src/main/java/com/talhanation/bannermod/**` in controlled slices.
- Re-home merged recruit and worker code, AI, registries, networking, and client packages into the canonical root tree.
- Retire legacy source roots only after full root validation is green.

**Goal:** BannerMod becomes one physical codebase instead of one root build that still composes legacy source trees.

**Plans:** 13/13 plans complete

Plans:
- [x] 21-01-PLAN.md — Reset Phase 21 executed state: revert the prior-namespace wave-1..5 work (commit range `f1832af..a792dc3`), delete stale executed-plan summaries, and record the convergence-namespace pivot (now `bannermod`) in roadmap and merge notes.
- [x] 21-02-PLAN.md — Establish canonical shared seam ownership under `com.talhanation.bannermod.shared.{authority,settlement,logistics}` and reduce legacy `com.talhanation.bannermod.{authority,settlement,logistics}` peers to `@Deprecated` thin forwarders. (Option A narrowed scope: 5 of 8 originally-named classes shipped; deferred 3 documented in MERGE_NOTES.)
- [x] 21-03-PLAN.md — Move recruit-owned bootstrap, init, and registry packages into `com.talhanation.bannermod.bootstrap/registry` while routing the shared `SimpleChannel` through `BannerModNetworkBootstrap`.
- [x] 21-04-PLAN.md — Move recruit events, commands, top-level config, and utilities into `bannermod.{events,commands,config,util}` so the recruits clone is reduced to `Main.java` plus subsystem subtrees.
- [x] 21-05-PLAN.md — Migrate recruits military gameplay (entities, AI, mixin, compat, migration, util) into `bannermod.{entity,ai,mixin,compat,migration,util}.military`.
- [x] 21-06-PLAN.md — Migrate recruits client UI, inventory menus, world managers/SavedData, and items into `bannermod.{client,inventory,persistence,items}.military`.
- [x] 21-07-PLAN.md — Migrate workers civilian subsystem (entities, client, world, inventory, items, settlement) into `bannermod.{entity,client,persistence,inventory,items,settlement}.civilian`.
- [x] 21-08-PLAN.md — Network consolidation: migrate `recruits/network` (109 files) and `workers/network` (24 files) into `bannermod.network.messages.{military,civilian}` and rewrite `BannerModNetworkBootstrap` with canonical `MILITARY_MESSAGES`/`CIVILIAN_MESSAGES` arrays so `workerPacketOffset() == MILITARY_MESSAGES.length` is compile-time provable.
- [x] 21-09-PLAN.md — Phase closure: consolidate resources under outer `src/main/resources/{assets,data}/bannermod/`, retire embedded clone source sets from `build.gradle`, scrub `mods.toml` to single bannermod entry, complete cross-tree FQN sweep, and pass the `./gradlew compileJava` compile-green gate.
- [x] 21-10-PLAN.md — Plan 10: Post-UAT config conflict fix (gap closure). Switch `BannerModMain` to the 3-arg `ModLoadingContext.registerConfig(Type, Spec, fileName)` overload with distinct filenames (`bannermod-recruits-client.toml`, `bannermod-recruits-server.toml`, `bannermod-workers-server.toml`) to resolve the `Config conflict detected!` crash reported in 21-UAT.md test 2, and document the one-time operator config filename migration in MERGE_NOTES.md. Follow-up commit 14e7684 also defers `RecruitsClientConfig.RecruitsLookLikeVillagers.get()` out of `EntityRenderersEvent.RegisterRenderers` into the renderer provider lambda to fix a downstream `IllegalStateException` surfaced post-fix.
- [x] 21-11-PLAN.md — Plan 11: Post-UAT recruits-side handler registration fix (gap closure). Register the seven recruits-side `@SubscribeEvent` handler classes (`RecruitEvents`, `ClaimEvents`, `FactionEvents`, `CommandEvents`, `DamageEvent`, `PillagerEvents`, `VillagerEvents`) on `MinecraftForge.EVENT_BUS` inside `BannerModMain.setup(FMLCommonSetupEvent)`. Closes the right-click-to-hire NPE (`recruitsPlayerUnitManager is null`) and the latent NPE class on sibling static manager fields (`recruitsClaimManager`, `recruitsFactionManager`, `recruitsDiplomacyManager`, `recruitsTreatyManager`, `recruitsGroupsManager`).
- [x] 21-12-PLAN.md — Plan 12: Post-UAT client-side handler registration fix (gap closure). Same defect class as 21-11, applied to the three client-only handler classes (`KeyEvents`, `ClientPlayerEvents`, `ClaimOverlayManager`) that were never registered after the consolidation. Closes the R/U/M hotkey gap (Command/Faction/Map screens not opening) and restores the claim overlay HUD.
- [x] 21-13-PLAN.md — Plan 13: Post-UAT lang-file merge (gap closure). Merge recruits-side UI keys (`gui.recruits.*`, `key.recruits.*`, `category.recruits`, `chat.recruits.*`, `description.recruits.*`, `subtitles.recruits.*`, `recruits.*`, `gui.multiLineEditBox.*`) from `recruits/src/main/resources/assets/recruits/lang/<locale>.json` into the matching `src/main/resources/assets/bannermod/lang/<locale>.json` for en_us, ru_ru, de_de, ja_jp, tr_tr; seed es_es from en_us. Wave 9 only migrated entity / item / block keys; UI keys were left referencing the legacy `recruits` namespace and rendered as raw key strings.

Planning artifacts live under the original Phase 21 directory (`.planning/phases/21-source-tree-consolidation-into-<prior-namespace>/`, directory name retained verbatim for git history continuity per CONTEXT D-16; the realized convergence namespace is `bannermod` per 21-CONTEXT.md D-01/D-02).

Status: Complete (13/13 plans complete as of 2026-04-15). The original 9/9 closure (Waves 1–9) still holds at the source-tree-consolidation level: outer build composes only `src/{main,test,gametest}/{java,resources}`, clones remain as untracked archive copies (Option a per Wave 9 retention decision), `./gradlew compileJava` is green, and the 39 deferred test-tree errors remain documented in MERGE_NOTES.md (D-05 package overlap + smoke-test symbol drift). UAT (21-UAT.md) surfaced four post-consolidation defects, all closed by gap-closure plans 21-10..21-13:
- **21-10 (done)** — `Config conflict detected!` at `BannerModMain.<init>` (two `ModConfig.Type.SERVER` specs colliding on default filename). Fixed by 3-arg `registerConfig` overload with explicit per-subsystem filenames; follow-up commit 14e7684 also deferred a `RecruitsClientConfig.RecruitsLookLikeVillagers.get()` call out of `EntityRenderersEvent.RegisterRenderers` to fix a downstream `IllegalStateException`.
- **21-11 (done)** — Server-side NPE on right-click-to-hire (`recruitsPlayerUnitManager is null`) because seven recruits-side `@SubscribeEvent` handler classes were never registered on EVENT_BUS after `recruits/Main.java` was deprecated to a no-op shim. Fixed by registering all seven in `BannerModMain.setup(FMLCommonSetupEvent)`.
- **21-12 (done)** — R/U/M hotkeys now open Command / Faction / Map correctly because `KeyEvents`, `ClientPlayerEvents`, and `ClaimOverlayManager` are registered again in `BannerModMain.clientSetup(FMLClientSetupEvent)`. Follow-up fix `21-12.1` also restored the missing Combat / Movement / Other command categories.
- **21-13 (done)** — Recruits UI strings no longer render as raw translation keys; legacy recruits UI keys were merged into the six active `assets/bannermod/lang/*.json` files.

Phase 21 is structurally closed. Remaining follow-up dirt is tracked outside phase completion: the deferred test-tree compile errors in MERGE_NOTES.md and the optional UAT re-attempt for test 8.

## Phase 22: Citizen Role Unification

- Replace profession- and troop-specific entity sprawl with one shared `Citizen` representation plus role/job-driven behavior seams.
- Move shared state out of concrete entity subclasses and into citizen-owned data, controllers, or role definitions.
- Keep the migration incremental so recruit combat, worker labor, ownership, and inventory behavior stay testable.

**Goal:** Civilian and military actors converge on one coherent runtime model instead of separate inheritance forests.

**Requirements:** [CITIZEN-01, CITIZEN-02, CITIZEN-03, CITIZEN-04]

**Plans:** 4/4 plans complete

Plans:
- [x] 22-01-PLAN.md — Define the citizen core contracts and compatibility-safe persistence bridge before live wrappers change.
- [x] 22-02-PLAN.md — Add the citizen role/controller seam and wrapper adapters to recruit and worker bases without changing entrypoints.
- [x] 22-03-PLAN.md — Convert one recruit live path onto the citizen seam and pin it with targeted regressions plus retained GameTests.
- [x] 22-04-PLAN.md — Convert one worker live path onto the citizen seam and revalidate the merged runtime through targeted regressions plus retained GameTests.

Planning artifacts live under `.planning/phases/22-citizen-role-unification/`.

Status: Complete (4/4 plans complete as of 2026-04-13); recruit and worker wrappers now share a citizen core and role/controller seam, and one live recruit plus one live worker path reload through citizen-backed persistence while runtime ids and entrypoints stay stable.

## Phase 23: Settlement Governance And Governor Control

- Add a real governor role that rules a settlement instead of a placeholder promotion slot.
- Let governors collect taxes from bound citizens, coordinate garrison recommendations, suggest fortification actions, and report incidents or shortages.
- Keep governor authority layered on top of settlement binding and faction/claim legality.

**Goal:** Settlement governance becomes a first-class gameplay system rather than an implied side effect of ownership and work areas.

**Requirements:** [GOV-01, GOV-02, GOV-03, GOV-04]

**Plans:** 7/7 plans complete

Plans:
- [x] 23-01-PLAN.md — Define the claim-keyed governor snapshot, pure rules, and persistence boundary before live runtime wiring begins.
- [x] 23-02-PLAN.md — Implement governor designation and revocation as authority-safe runtime services over existing recruit/citizen identities.
- [x] 23-03-PLAN.md — Add the bounded governor heartbeat for local tax state, incidents, and settlement recommendations without widening into treasury or logistics rewrites.
- [x] 23-04-PLAN.md — Activate the dormant governor promotion path and add a dedicated governor control screen fed by live governance snapshots.
- [x] 23-05-PLAN.md — Close the original phase scope with reusable GameTest helpers and live governor designation/reporting validation.
- [x] 23-06-PLAN.md — Close the delayed GOV-04 compile/gametest gate by repairing stale root test and GameTest tree references after Phase 21 consolidation.
- [x] 23-07-PLAN.md — Guard early-tick config reads so the governor control GameTests execute under the harness without config-load crashes.

Planning artifacts live under `.planning/phases/23-settlement-governance-and-governor-control/`.

Status: Complete (7/7 plans complete as of 2026-04-15); the original governor feature slice landed in 23-01..23-05, and the post-phase validation debt was closed by 23-06..23-07 so GOV-04 now has real harness evidence instead of a stale blocked-gate note.

## Phase 24: Logistics Backbone, Courier Validation, And Economy Foundation

- Keep the existing logistics-backbone and courier-worker execution work as the active phase.
- Close the remaining live-delivery validation gap from historical plan `24-05`.
- Treat treasury, taxes, upkeep, supply, trade routes, and ports as the same lower-layer economy substrate instead of separate tiny follow-on phases.

**Goal:** BannerMod finishes the logistics backbone already in flight and turns it into the shared foundation for treasury, supply, and trade work instead of growing three adjacent half-phases.

**Plans:** 5/5 historical plans complete

Plans:
- [x] 24-01-PLAN.md — Define the shared logistics contracts for authored storage nodes, routes, lightweight reservations, and blocked-state vocabulary.
- [x] 24-02-PLAN.md — Implement the server-authoritative route/runtime seam with deterministic task selection and reservation cleanup.
- [x] 24-03-PLAN.md — Add the courier worker execution loop over the shared logistics backbone.
- [x] 24-04-PLAN.md — Surface route authoring, priority/filter intent, and blocked-state feedback through existing BannerMod UI/message patterns.
- [x] 24-05-PLAN.md — Close historical Phase 24 with unit and GameTest validation for reservations, authored routes, and live courier delivery.

Planning artifacts currently live under `.planning/phases/24-logistics-backbone-and-courier-worker/`. Keep using the existing Phase 24 directory and plan numbering for the open closeout rather than renumbering in-flight work.

Status: In Progress (5/5 historical plans complete as of 2026-04-19); the original logistics/courier Phase 24 scope is now fully verified, and old Phases 25 and 26 remain folded into this still-active economy foundation for treasury, upkeep, trade routes, and ports.

- Latest slice: governor heartbeat tax output now deposits into a persisted claim-keyed treasury ledger with faction identity carried from live claim ownership, and the same ledger can now record bounded army-upkeep debits from shared unpaid-upkeep accounting without going negative.
- Latest slice: treasury heartbeat accounting now applies tax credit plus bounded upkeep debit in one claim-local write, exposes a compact fiscal rollup with next-cycle projection from the ledger, and persists that rollup onto governor snapshots so active governor state can read balance and net treasury movement without a new economy manager.
- Latest slice: the shared logistics runtime can now publish sea-trade import/export entrypoints directly from live server-side `StorageArea` entities, while filtering out authored routes whose storage endpoints are not actually present; the retained courier GameTest now exercises that live publication path and stale-route suppression. `BannerModLogisticsServiceTest`, `compileGameTestJava`, and the current focused verification baseline are green.
- Latest slice: governor follow-through now uses live worker/recruit supply-upkeep state in heartbeat accounting, clears stale governor or treasury state after claim loss, and keeps the client governor view aligned by surfacing the persisted fiscal rollup instead of leaving it server-only.

## Phase 25: Settlement Economy, Governance, And Resident Simulation

- Expand the post-Phase-24 economy upward into a BannerMod-owned settlement aggregate with resident records, building-driven economy, stockpile/market state, resident roles, goal scheduling, growth/projects, and village-life loops.
- Build on completed governor, citizen, and claim-growth foundations and the Millenaire-like settlement-simulation design input instead of treating civil economy and resident simulation as many disconnected micro-phases.
- Keep settlement ownership, legality, growth, and de-vanillaged village logic replacement under one coherent civil-side program, with phased retirement of vanilla-village-dependent gameplay rather than layering parallel dependencies on top forever.
- Phase 25 owns the settlement runtime/model replacement; downstream Phase 26 consumes it for settlement-defense behavior and Phase 27 consumes it for settlement-chief and aggregate settlement UI.

**Goal:** BannerMod's next civil expansion becomes one settlement-facing economy and simulation phase that replaces vanilla-village-dependent settlement gameplay with custom BannerMod settlement simulation instead of leaving treasury, resident, growth, and politics placeholders disconnected.

Planning artifacts currently live under `.planning/phases/25-treasury-taxes-and-army-upkeep/`; keep that directory truthful while compact Phase 25 execution continues in isolated batches.

Status: In Progress; opener slices landed a persisted claim-keyed settlement aggregate seeded from current governor, villager/worker citizen, and work-area seams, and the next additive runtime bring-up now also exists in main-tree code: `settlement.goal` provides a first resident scheduler seam, `settlement.growth` scores pending projects, `settlement.project` holds a bounded project queue/BuildArea bridge, `settlement.dispatch` animates seller dispatch state, `settlement.household` tracks home assignment intent, and `settlement.job` exposes a first job-handler registry. These seams are real and test-backed, but still partial and mostly in-memory rather than a finished end-to-end village simulation.

- Latest slice: added one persisted settlement `stockpileSummary` and matching building-local stockpile seed metadata derived from authored `StorageArea` state plus its scanned storage footprint, so later building-driven economy slices can read storage capacity, route, port, and storage-type hints without introducing a stockpile manager or full market simulation.
- Latest slice: added one persisted building `buildingCategory` plus `buildingProfileSeed` derived from live authored work-area types and defaulted legacy loads from `buildingTypeId`, so later growth, market, and resident slices can branch on stable building semantics without claiming a full building economy already exists.
- Latest slice: enriched the settlement aggregate with explicit worker assignment semantics so controlled workers now persist local-vs-unassigned-vs-missing-building assignment state, while buildings also retain assigned resident UUIDs and aggregate assigned/unassigned/missing worker totals remain additive over existing worker bindings instead of introducing housing, scheduling, or stockpile simulation.
- Latest slice: added one persisted resident `runtimeRoleSeed` classification derived from role, schedule seed, resident mode, and assignment state so later scheduling/job logic can consume lightweight village-life, governance, and labor intent directly from the settlement layer without a real scheduler yet.
- Latest slice: added one persisted resident `serviceContract` projection for projected controlled workers, joining assignment-state output with local building records so the settlement layer can read local, floating, and orphaned service actors without changing worker gameplay behavior.
- Latest slice: added one persisted resident `roleProfile` descriptor derived from `role`, `runtimeRoleSeed`, `residentMode`, and `assignmentState`, so later scheduler/job logic can consume one stable profile id, goal domain, and local-building preference without faking a full resident role system.
- Latest slice: added one persisted resident `scheduleWindowSeed` derived from `scheduleSeed` and `runtimeRoleSeed`, carrying daylight-flex, labor-day, and civic-day active/rest tick windows so later village-life timing can consume server-authored schedule intent without introducing a scheduler.
- Latest slice: added a persisted `marketState` seed to the settlement aggregate so each claim can retain per-market open/name/storage-slot snapshots plus aggregate market capacity totals from authored `MarketArea` entities, without yet introducing merchant runtime, pricing, or trade-route decision logic.
- Latest slice: added a persisted seller-dispatch seed to `marketState`, projecting market-profile local service workers into `READY` vs `MARKET_CLOSED` dispatch records keyed by resident and market UUID so later merchant behavior can consume one compact settlement-facing dispatch view without changing the current merchant loop.
- Latest slice: added one persisted resident `jobDefinition` seed that derives a compact handler plus optional target-building UUID/type/category/profile from `runtimeRoleSeed`, `serviceContract`, and local building records, so later resident/job runtime can consume one settlement-local task token without replacing the current worker AI.
- Latest slice: added one persisted resident `schedulePolicy` projection that derives a compact policy seed plus scheduler-facing schedule/window and role-profile hints from `scheduleSeed`, `scheduleWindowSeed`, `runtimeRoleSeed`, and `roleProfile`, so later resident scheduling logic can consume one stable settlement-local policy view without building a full scheduler yet.
- Latest slice: added one persisted settlement `projectCandidateSeed` derived from building profiles, `stockpileSummary`, `desiredGoodsSeed`, `marketState`, and governor/claim readiness seams so later project and growth logic can consume one compact next-build hint without introducing a settlement project manager yet.
- Latest slice: controlled-worker settlement semantics are now more consistent for unassigned and missing-building cases, settlement refresh now re-runs on storage updates and worker binding changes in targeted civil paths, and settlement stockpile/trade heuristics prefer live sea-trade entrypoint sets where available.
- Latest slice: settlement enum-load hardening improved across key resident and service seeds with safe fallback parsing, but remaining raw `Enum.valueOf(...)` persistence paths still mean this closeout is partial rather than complete.
- Latest slice: compact Phase 25 moved beyond persistence-only seeds into runtime bring-up: `BannerModResidentGoalScheduler`, `BannerModSettlementGrowthManager`, `BannerModSettlementProjectRuntime`, `BannerModSellerDispatchRuntime`, `BannerModHomeAssignmentRuntime`, and `JobHandlerRegistry` now exist in the main tree with targeted JUnit coverage, while still remaining additive and only partially integrated into the live settlement loop.
- Latest slice: `ClaimEvents` now calls a new `BannerModSettlementOrchestrator` after settlement refresh, so refreshed claim snapshots feed the additive Phase 25 growth/project/home/seller/goal/job runtimes on the governor tick without changing entity behavior or persistence shape.

## Phase 26: Army Command, Formations, And Warfare

- Consolidate battlefield command, formation runtime, tactical doctrine, morale, shield-wall improvement, siege behavior, and territory-war rules into one military-side program.
- Reuse the completed pathfinding/performance stack and current authority/faction seams instead of reopening them as separate prerequisite queues.
- Keep combat extensions, siege compatibility, army-control rework, and the settlement-defense bridge that consumes Phase 25 settlement state in one coherent military roadmap slice.

**Goal:** BannerMod's military expansion proceeds as one command-and-warfare phase instead of separate formation, doctrine, morale, siege, and territory subprojects competing for sequence.

Planning artifacts will be created under a new compact Phase 26 when Phase 25 is stable enough to branch the military-side work.

Status: Planned.

## Phase 27: Read Models, UI, And Player Operations

- Add the aggregate read models, command tooling, communication surfaces, settlement-chief UI, and operator-facing views needed to make the larger settlement and army systems usable.
- Fold faction/local chat, army-command tooling, settlement overview screens, and debug/operator surfaces into one player-facing integration phase.
- Keep UI work downstream of the model/runtime phases so screens consume stable aggregates instead of scraping brownfield state directly.

**Goal:** BannerMod gets one explicit player-operations phase that turns the larger settlement and army systems into usable tools and read models.

Planning artifacts will be created under a new compact Phase 27 when the Phase 25-26 model seams are explicit enough for UI planning.

Status: Planned.

## Phase 28: Architecture Integration, Telemetry, Balance, And Safe Rollout

- Close the compact roadmap with the cross-domain architecture contracts, telemetry, migration safety, balance loops, and staged rollout controls.
- Use the earlier phases' real runtime seams as the truth source for final integration instead of maintaining a separate sprawling triple-merge track.
- End the active roadmap with explicit validation and rollout evidence, not only design notes.

**Goal:** BannerMod's remaining expansion closes as one integration-and-rollout phase that makes the larger civil and military program measurable, migratable, and safe to enable.

Planning artifacts will be created under a new compact Phase 28 when earlier compact phases have executable seams to integrate and validate.

Status: Planned.

## Phase 29: Miner Excavation Recovery And Builder Schematic Loading

- Restore miner progression by replacing the current idle-at-mine behavior with a diagonal tunnel pattern that moves away from the worker instead of straight under them.
- Add a configurable branch/strip-mining mode so miners can harvest a main corridor plus side branches at authored height/spacing.
- Let build areas load schematic templates through the existing preview/build pipeline, not only locally scanned `.nbt` structures.

**Goal:** Workers recover two blocked core automation loops: miners can excavate authored tunnel and branch-mining patterns without getting stuck, and builders can load schematic templates into the existing BuildArea workflow.

**Requirements:** [MINING-01, MINING-02, MINING-03, BUILD-01]

**Depends on:** Current merged BannerMod runtime baseline; execute before returning to the deferred Phase 21+ structural roadmap.

**Plans:** 4 plans

Plans:
- [x] 29-01-PLAN.md — Define the explicit mining-area settings contract and persist it across UI, packet, and entity state.
- [x] 29-02-PLAN.md — Implement the diagonal tunnel planner and wire miners to excavate it without digging under themselves.
- [x] 29-03-PLAN.md — Extend miner execution to deterministic branch/strip mining at configured height, spacing, and branch length.
- [x] 29-04-PLAN.md — Add schematic template loading to BuildArea load mode while preserving the current NBT scan flow.

Planning artifacts live under `.planning/phases/29-1-3-3-2-branch-mining-strip-mining-3-21-26/`.

Status: Complete (priority override executed on 2026-04-12 before returning to the deferred Phase 21+ structural roadmap).

### Phase 30: Claim Settlement Growth, Worker Birth, And Miner Claim Safety

**Goal:** BannerMod treats friendly claims as real settlement growth surfaces through one coherent worker-growth block: worker birth, autonomous claim spawning, diminishing growth cadence, miner authoring cleanup, and hostile-claim excavation safety all live in one claim-aware rules/runtime story instead of two overlapping priority phases.

**Requirements:** [WBSP-01, WBSP-02, WBSP-03, CLAIMGROW-01, CLAIMGROW-02, MINERCFG-01, MINERSAFE-01]
**Depends on:** Phase 29
**Plans:** 7/7 historical plans complete

Plans:
- [x] 30-01-PLAN.md — Define the pure claim-aware rules and config contract for worker birth plus settlement spawning.
- [x] 30-02-PLAN.md — Wire villager birth and autonomous settlement worker spawning through one runtime claim-aware spawner.
- [x] 30-03-PLAN.md — Add root GameTest coverage for friendly worker birth, bounded claim spawn, and hostile or unclaimed denial.
- [x] 31-01-SUMMARY (no PLAN doc) — Extend the same growth rules/config seam so friendly claims count as settlement growth surfaces with diminishing cadence. Evidence: `.planning/phases/31-1-2-mining-area-branch-mine-3/31-1-2-mining-area-branch-mine-3-01-SUMMARY.md` (commit `8ba19ec`).
- [x] 31-02-SUMMARY (no PLAN doc) — Wire bounded live claim worker growth and root GameTest validation through the claim-aware runtime spawn path. Evidence: `31-1-2-mining-area-branch-mine-3-02-SUMMARY.md` (commit `f0987cf`).
- [x] 31-03-SUMMARY (no PLAN doc) — Remove the miner's legacy box-style authoring contract and keep only tunnel/branch settings in the packet and screen. Evidence: `31-1-2-mining-area-branch-mine-3-03-SUMMARY.md` (commit `2546b32`).
- [x] 31-04-SUMMARY (no PLAN doc) — Add hostile-claim excavation guardrails so miners skip foreign-claim blocks while preserving friendly and unclaimed mining. Evidence: `31-1-2-mining-area-branch-mine-3-04-SUMMARY.md` (commit `e24b73f`).

Planning artifacts live under `.planning/phases/30-worker-birth-and-claim-based-settlement-spawn/` and `.planning/phases/31-1-2-mining-area-branch-mine-3/`. Phase 31 is kept on disk as historical execution evidence, but active roadmap ownership is consolidated here because both branches touched the same claim growth/runtime seam.

**Paper-trail note (2026-04-19 audit)**: The four 31-XX slices went plan→execute→summary without an intermediate `31-XX-PLAN.md` artifact (confirmed via `git log --all -- '**/31-*-PLAN*'` returning zero results; commits `8ba19ec`/`f0987cf`/`2546b32`/`e24b73f` only ever added SUMMARY files). Treat the SUMMARY docs as authoritative evidence for this phase's closure.

Status: Complete (7/7 historical plans complete as of 2026-04-12); the former Phase 31 priority override has been folded back into this larger claim-growth block so normal development has one source of truth instead of two competing adjacent phases.

### Phase 31: Historical Branch Merged Into Phase 30

- The old “claims count as settlements + miner claim safety + worker growth” priority branch was executed and then folded back into Phase 30.
- Keep `.planning/phases/31-1-2-mining-area-branch-mine-3/` for historical summaries only.
- Do not treat Phase 31 as a separate active roadmap phase anymore.

## Old-To-New Future Phase Crosswalk

- Historical completed branches: Phases 29-31 stay as completed historical execution evidence in their existing directories; they are not folded into compact Phases 24-28.
- Folded future work: old Phases 32-49 no longer exist as separate queued roadmap phases and now feed the compact future program in Phases 24-28.

- Old 24 stays New 24 (historical in-flight execution directory and open `24-05` closeout remain authoritative).
- Old 25 (`Treasury, Taxes, And Army Upkeep`) folds into New 24.
- Old 26 (`Supply Radius, Trade Routes, And Ports`) folds into New 24.
- Old 27 (`Shield Wall, Morale, And Medieval Siege Compatibility`) folds into New 26.
- Old 28 (`Telemetry, Balance, And Economic Validation`) folds into New 28.
- Old 32 (`Faction And Local Chat`) folds into New 27.
- Old 33 (`Worker Birth / Building Capacity / Citizen Growth follow-up`) folds into New 25.
- Old 34 (`Forms Of Government`) folds into New 25.
- Old 35 (`Clans`) folds into New 25.
- Old 36 (`Datapack-Driven Formations`) folds into New 26.
- Old 37 (`Faction Territory Destruction Restrictions`) folds into New 26.
- Old 38 (`SiegeWar-Style Siege Rework`) folds into New 26.
- Old 39 (`Triple-Merge Domain Contracts And Migration Boundaries`) folds into New 28.
- Old 40 (`Army Command Action Layer And Battlefield Selection`) folds into New 26.
- Old 41 (`Formation Runtime And Banner Group Pathing`) folds into New 26.
- Old 42 (`Tactical Doctrine, Target Policy, And Combat Support Orders`) folds into New 26.
- Old 43 (`Settlement Aggregate And Resident Persistence`) folds into New 25.
- Old 44 (`Resident Roles, Schedules, And Job Handler Runtime`) folds into New 25.
- Old 45 (`Settlement Economy, Goods, Markets, And Trade Integration`) folds into New 25.
- Old 46 (`Settlement Projects, Building Growth, And Village Life Loops`) folds into New 25.
- Old 47 (`Politics, Defense, And War-Economy Integration`) folds into New 25.
- Old 48 (`Aggregate Read Models, UI, And Command Tooling`) folds into New 27.
- Old 49 (`Triple-Merge Migration, Telemetry, And Safe Rollout`) folds into New 28.

Historical executed phase directories stay on disk unchanged even where the active planning surface above now folds or renumbers future work.
