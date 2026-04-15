# Merge Roadmap

## Phase 1: Workspace Bootstrap

- Establish one root Gradle entrypoint.
- Archive legacy planning trees.
- Create merge documentation and the active root `.planning/` context.

**Goal:** Root workspace bootstrap is explicit and executable: one root Gradle entrypoint, one active root planning context, preserved legacy archives, and documented merge truth/verification rules.

**Plans:** 3 plans

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

**Plans:** 3 plans

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

**Plans:** 4 plans

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

**Plans:** 4 plans

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

**Plans:** 4 plans

Plans:
- [x] 07-01-PLAN.md — Validate dedicated-server owner-offline and unresolved-owner authority denial for recruit and worker recovery flows.
- [x] 07-02-PLAN.md — Validate dedicated-server reconnect and persistence-safe ownership recovery across recruit, worker, and work-area state.

Planning artifacts live under `.planning/phases/07-dedicated-server-authority-edge-validation/` so dedicated-server follow-up stays explicit before implementation.

Status: Complete (2/2 plans complete as of 2026-04-11); `compileGameTestJava` and `verifyGameTestStage` now both pass, with all 32 required GameTests green.

## Phase 8: Multiplayer Authority Conflict Validation

- Validate contested multiplayer interactions with at least two distinct players in the same merged runtime.
- Prove same-team cooperation and outsider denial across recruit control, worker control, and settlement authoring seams.
- Keep slices narrow and deterministic so later plans can extend multiplayer coverage without broad scenario rewrites.

**Goal:** Prove BannerMod's shared authority contract remains correct under true multiplayer contention: cooperative players can use the intended same-team paths, outsiders stay denied, and server-owned state does not silently drift during concurrent recruit and settlement interactions.

**Plans:** 4 plans

Plans:
- [x] 08-01-PLAN.md — Validate contested multiplayer authority on shared recruit, worker, and work-area interactions with distinct owner and outsider players.
- [x] 08-02-PLAN.md — Validate same-team multiplayer cooperation paths without reopening outsider settlement or control access.

Planning artifacts live under `.planning/phases/08-multiplayer-authority-conflict-validation/` so multiplayer-specific validation remains reviewable and separate from dedicated-server edge coverage.

Status: Complete (2/2 plans complete as of 2026-04-11); `verifyGameTestStage` now passes with all 36 required GameTests green, including live multiplayer outsider-denial and same-team cooperation coverage.

## Phase 9: Settlement-Faction Binding Contract

- Make settlement-to-faction binding explicit as a first-class BannerMod gameplay contract rather than only an architectural implication.
- Keep the implementation boundary low-risk by preferring derived settlement-plus-claim rules over a new persistence manager unless the code proves one is necessary.
- Define how settlement legality, friendly use, and faction mismatch should be interpreted by worker and military systems.

**Goal:** BannerMod gains an explicit settlement-faction contract: settlements are faction-aligned operational footprints derived from claims plus active infrastructure, and that contract becomes actionable for authority, legality, and later logistics work without forcing an immediate deep persistence rewrite.

**Plans:** 4 plans

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

Status: Complete (2/2 plans complete as of 2026-04-12); root GameTests now prove friendly settlement placement and operation, hostile or unclaimed denial, and claim-loss or faction-mismatch degradation without silent ownership transfer.

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

Status: Complete (2/2 plans complete as of 2026-04-11); recruit path issuance now routes through `GlobalPathfindingController`, `GlobalPathfindingControllerTest` passes, and `verifyGameTestStage` is green with controller-aware mixed-squad and dense-battle profiling snapshots.

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

Status: Complete (2/2 plans complete as of 2026-04-11); recruit path requests now attempt controller-owned reuse with explicit hit/miss/drop counters, and `verifyGameTestStage` remains green with reuse-aware dense-battle and mixed-squad profiling snapshots.

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

Status: Complete (4/4 plans complete as of 2026-04-12); formation-aware target selection now reports dense-battle profiling counters, the optional retarget/loss-of-target checks are green on the retained recovery-field harness, and `verifyGameTestStage` passes with all 45 required tests green.

## Phase 15: Pathfinding Throttling And Budgeting

- Add explicit throttling and per-tick budgeting once global control, reuse, and formation targeting are in place.
- Prefer deterministic request deferral over hidden random slowdown so validation remains readable.
- Measure queue depth, deferred work, and recovery time under load.

**Goal:** BannerMod can cap and defer pathfinding work during large battles with an explicit, measurable budget instead of allowing bursty navigation spikes to dominate server ticks.

**Plans:** 2/2 plans complete

Plans:
- [x] 15-01-PLAN.md — Add deterministic pathfinding throttles and budget accounting to the shared control seam.
- [x] 15-02-PLAN.md — Validate under load and profile queue depth, deferred-path latency, and tick-cost stability.

Planning artifacts live under `.planning/phases/15-pathfinding-throttling-and-budgeting/` so budget control stays separate from targeting and async remediation work.

Status: Complete (2/2 plans complete as of 2026-04-12); pathfinding requests now run through an explicit per-tick budget with bounded deferred-work accounting, retained battle GameTests expose queue/defer/drop pressure in stable profiling snapshots, and Phase 15 has one explicit correctness-and-evidence procedure for later async comparisons.

## Phase 16: Async Pathfinding Reliability Fixes

- Fix the known async-pathfinding safety and correctness issues only after the synchronous control surfaces are explicit.
- Keep the scope on correctness, race prevention, stale-result handling, and safe handoff boundaries.
- Require validation that async gains do not reintroduce authority drift, stuck entities, or invalid path application.

**Goal:** Async pathfinding becomes predictable and safe under the new global control model: stale or unsafe results are fenced, handoff points are explicit, and large-battle path work no longer depends on fragile concurrency assumptions.

**Plans:** 2 plans

Plans:
- [x] 16-01-PLAN.md — Harden async pathfinding handoff, cancellation, and stale-result handling against the current large-battle failure modes.
- [x] 16-02-PLAN.md — Validate async correctness and profile whether the fixed async path still improves large-battle cost.

Planning artifacts live under `.planning/phases/16-async-pathfinding-reliability-fixes/` so concurrency fixes remain a focused slice after earlier control work.

Status: Complete (2/2 plans complete as of 2026-04-12); async path ownership and invalidation are now explicit, retained battle GameTests expose delivered-versus-dropped callback accounting, and Phase 16 has one correctness-first async reliability validation procedure.

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

Status: Complete (2/2 plans complete as of 2026-04-12); the retained required GameTest gate is green again, optimized-runtime evidence now lives under `.planning/phases/19-large-battle-performance-validation/evidence/`, and the final closeout is published in `19-VALIDATION.md` and `19-RESULTS.md` with explicit caveats about missing Phase 11 raw bundles and external profiler data.

## Phase 20: Runtime Audit And Bannerlord Target Architecture

- Audit what still lives only in `recruits/`, `workers/`, or root-side jars.
- Map entity, AI, registry, networking, storage, settlement, and config ownership that must survive the physical source-tree merge.
- Normalize the requested destination into the valid Java package path `src/main/java/com/talhanation/bannerlord/**` before package moves begin.

**Goal:** Turn the current future-feature wishlist into a code-backed baseline with explicit migration, compatibility, and package-move boundaries.

**Plans:** 2/2 plans complete

Plans:
- [x] 20-01-PLAN.md — Audit the active runtime ownership surfaces and publish the source-root, package-family, and jar-reference matrix that must survive consolidation.
- [x] 20-02-PLAN.md — Publish the Bannerlord target architecture, package-move map, compatibility boundary, and migration risk register for Phase 21 execution.

Planning artifacts live under `.planning/phases/20-runtime-audit-and-bannerlord-target-architecture/`, while roadmap-shaping research remains in `.planning/FUTURE_EXPANSION_RESEARCH.md` and `.planning/phases/FUTURE-EXPANSION-PHASES.md`.

Status: Complete (2/2 plans complete as of 2026-04-12); runtime ownership is audited in `20-RUNTIME-AUDIT.md`, the move-order matrix is published in `20-OWNERSHIP-MATRIX.md`, and the Phase 21 handoff contract now lives in `20-TARGET-ARCHITECTURE.md`.

## Phase 21: Source Tree Consolidation Into BannerMod

- Move active Java ownership out of `recruits/` and `workers/` into `src/main/java/com/talhanation/bannermod/**` in controlled slices.
- Re-home merged recruit and worker code, AI, registries, networking, and client packages into the canonical root tree.
- Retire legacy source roots only after full root validation is green.

**Goal:** BannerMod becomes one physical codebase instead of one root build that still composes legacy source trees.

**Plans:** 7/9 plans executed

Plans:
- [x] 21-01-PLAN.md — Reset Phase 21 executed state: revert the prior-namespace wave-1..5 work (commit range `f1832af..a792dc3`), delete stale executed-plan summaries, and record the convergence-namespace pivot (now `bannermod`) in roadmap and merge notes.
- [x] 21-02-PLAN.md — Establish canonical shared seam ownership under `com.talhanation.bannermod.shared.{authority,settlement,logistics}` and reduce legacy `com.talhanation.bannermod.{authority,settlement,logistics}` peers to `@Deprecated` thin forwarders. (Option A narrowed scope: 5 of 8 originally-named classes shipped; deferred 3 documented in MERGE_NOTES.)
- [x] 21-03-PLAN.md — Move recruit-owned controlling systems into `bannermod` military/shared packages before worker code follows them.
- [x] 21-04-PLAN.md — Move worker civilian entities, AI, persistence, and client flows onto the new `bannermod` base while isolating the compatibility layer.
- [x] 21-05-PLAN.md — Retire legacy Java source roots, refresh root docs, and close the phase only after full root validation is green.
- [x] 21-06-PLAN.md — Migrate remaining recruits-side subsystems (client UI, inventory menus, world managers/SavedData, items) into `bannermod.{client,inventory,persistence,items}.military`, emptying the recruits clone down to deprecated `Main.java` shim.
- [x] 21-07-PLAN.md — Gap-closure slice for `bannermod` consolidation per the re-planned wave-7 contract.
- [ ] 21-08-PLAN.md — Gap-closure slice for `bannermod` consolidation per the re-planned wave-8 contract.
- [ ] 21-09-PLAN.md — Final phase closeout for `bannermod` consolidation: full root validation green and all prior plan checkboxes re-confirmed at true closeout.

Planning artifacts live under the original Phase 21 directory (`.planning/phases/21-source-tree-consolidation-into-<prior-namespace>/`, directory name retained verbatim for git history continuity; re-execution targets the `bannermod` convergence namespace per 21-CONTEXT.md D-01/D-02).

Status: In progress (6/9 as of 2026-04-15); waves 1-6 complete under the `bannermod` convergence namespace. Recruits clone working tree is reduced to `Main.java` shim plus `init/ModLifecycleRegistrar.java` and `network/**` (Wave 8 scope). Plans 21-07 through 21-09 remain.

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

**Plans:** 5 plans

Plans:
- [ ] 23-01-PLAN.md — Define the claim-keyed governor snapshot, pure rules, and persistence boundary before live runtime wiring begins.
- [ ] 23-02-PLAN.md — Implement governor designation and revocation as authority-safe runtime services over existing recruit/citizen identities.
- [ ] 23-03-PLAN.md — Add the bounded governor heartbeat for local tax state, incidents, and settlement recommendations without widening into treasury or logistics rewrites.
- [ ] 23-04-PLAN.md — Activate the dormant governor promotion path and add a dedicated governor control screen fed by live governance snapshots.
- [ ] 23-05-PLAN.md — Close Phase 23 with reusable GameTest helpers and live governor designation/reporting validation.

Planning artifacts live under `.planning/phases/23-settlement-governance-and-governor-control/` once planned.

Status: Planned (5 plans defined on 2026-04-13).

## Phase 24: Logistics Backbone And Courier Worker

- Add shared storage-node routing, reservations, filters, stock thresholds, and failure handling.
- Implement the courier worker so it travels from source chest to destination chest transporting resources.
- Surface route authoring, priority, and blocked-state feedback to the player.

**Goal:** BannerMod gains an explicit logistics layer and courier profession instead of isolated profession-local item movement.

Planning artifacts live under `.planning/phases/24-logistics-backbone-and-courier-worker/` once planned.

Status: Planned (new decomposition defined as of 2026-04-12).

## Phase 25: Treasury, Taxes, And Army Upkeep

- Add treasury and accounting vocabulary tied to settlement identity and faction ownership.
- Add heartbeat-based tax collection instead of per-tick recomputation.
- Add army wage and upkeep drains plus starvation/unpaid penalties that can feed morale and discipline systems.

**Goal:** Settlements and armies become fiscally legible through one shared treasury and upkeep model.

Planning artifacts live under `.planning/phases/25-treasury-taxes-and-army-upkeep/` once planned.

Status: Planned (new decomposition defined as of 2026-04-12).

## Phase 26: Supply Radius, Trade Routes, And Ports

- Add `SupplyPool`, carried stock, supply sources/sinks, and out-of-supply penalties.
- Add inland trade routes, convoy abstractions, tariffs, throughput, and route-risk rules.
- Extend the same stack to ports and maritime routes only after inland logistics is stable.

**Goal:** Supply, trade, and port value all reuse one layered logistics model rather than fragmenting into separate systems.

Planning artifacts live under `.planning/phases/26-supply-radius-trade-routes-and-ports/` once planned.

Status: Planned (new decomposition defined as of 2026-04-12).

## Phase 27: Shield Wall, Morale, And Medieval Siege Compatibility

- Promote shield wall into a maintained BannerMod combat system instead of a jar-side reference behavior.
- Expand morale from the current recruit-centric seam into broader formation and discipline hooks.
- Port Medieval Siege Machines compatibility into maintained source and integrate it more cleanly than the current reference jar baseline.

**Goal:** Combat extensions and siege compatibility stop living in opaque sidecars and become code-backed BannerMod systems.

Planning artifacts live under `.planning/phases/27-shield-wall-morale-and-medieval-siege-compatibility/` once planned.

Status: Planned (new decomposition defined as of 2026-04-12).

## Phase 28: Telemetry, Balance, And Economic Validation

- Add metrics for faction income, army upkeep, supply loss, route usage, richest/poorest settlements, and economy tick cost.
- Use the metrics to tune taxes, upkeep, logistics, governor behavior, and trade.
- Close the expansion program with explicit validation instead of anecdotal gameplay reports.

**Goal:** The future expansion block ends with measurable evidence and balancing feedback, not just feature checkboxes.

Planning artifacts live under `.planning/phases/28-telemetry-balance-and-economic-validation/` once planned.

Status: Planned (new decomposition defined as of 2026-04-12).

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

### Phase 30: Worker Birth And Claim-Based Settlement Spawn

**Goal:** BannerMod settlements can grow worker populations through explicit worker birth and claim-backed settlement spawning, with friendly-claim authority, bounded cadence, and automated validation instead of ad hoc village-only spawning.

**Requirements:** [WBSP-01, WBSP-02, WBSP-03]
**Depends on:** Phase 29
**Plans:** 3/3 plans complete

Plans:
- [ ] 30-01-PLAN.md — Define the pure claim-aware rules and config contract for worker birth plus settlement spawning.
- [ ] 30-02-PLAN.md — Wire villager birth and autonomous settlement worker spawning through one runtime claim-aware spawner.
- [ ] 30-03-PLAN.md — Add root GameTest coverage for friendly worker birth, bounded claim spawn, and hostile or unclaimed denial.

Planning artifacts live under `.planning/phases/30-worker-birth-and-claim-based-settlement-spawn/`.

Status: Planned (new decomposition defined as of 2026-04-12).

### Phase 31: Добавляем приоритетную новую фазу. 1. Клеймы считаются поселениями  2. Mining Area майнера - убрать, оставить чисто настройки копания тоннеля по диагонали вниз и branch mine. Настроек как далеко можно копать можно не делать, но сделать так чтобы не пытался всё время сломать блок чужого клейма, если рядом с клеймом чужим к нему не копает (копать вне клейма майнер может)  3. Делаем генерацию воркеров в клеймах, но считаем сколько уже есть, и по убывающей прогрессии добавляем новых.

**Goal:** BannerMod treats friendly claims as settlement-capable worker-growth surfaces, miner authoring exposes only tunnel and branch settings, and miners skip hostile-claim excavation targets without losing friendly or unclaimed mining.
**Requirements:** [CLAIMGROW-01, CLAIMGROW-02, MINERCFG-01, MINERSAFE-01]
**Depends on:** Phase 30
**Plans:** 4/4 plans complete

Plans:
- [x] 31-01-PLAN.md — Extend the pure worker-growth rules/config seam so friendly claims count as settlement growth surfaces with diminishing cadence.
- [x] 31-02-PLAN.md — Wire bounded live claim worker growth and root GameTest validation through one claim-aware runtime spawn path.
- [x] 31-03-PLAN.md — Remove the miner's legacy box-style authoring contract and keep only tunnel/branch settings in the packet and screen.
- [x] 31-04-PLAN.md — Add hostile-claim excavation guardrails so miners skip foreign-claim blocks while preserving friendly and unclaimed mining.

Planning artifacts live under `.planning/phases/31-1-2-mining-area-branch-mine-3/`.

Status: Complete (4/4 plans complete as of 2026-04-12).
