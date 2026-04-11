---
gsd_state_version: 1.0
milestone: v1.0
milestone_name: milestone
status: Active
last_updated: "2026-04-11T19:09:00.000Z"
progress:
  total_phases: 19
  completed_phases: 6
  total_plans: 38
  completed_plans: 14
---

# Project State

- Current focus: Phase 07 Plan 02 is complete with dedicated-server reconnect and persistence-safe ownership recovery coverage; the remaining blocker before phase completion is unrelated existing GameTest failures outside Phase 07 scope, after which the next queued work is the Phase 08 through Phase 10 authority-and-contract backlog and the Phase 11 through Phase 19 large-battle AI/performance roadmap.
- Runtime base: `recruits`
- Active runtime mod: `bannermod`
- Workers status: absorbed into the active root runtime as a subsystem; registry-layer ids now publish under `bannermod` while legacy source/resources remain preserved under `workers/`
- Pending major work: dedicated-server and multiplayer-specific authority validation, explicit settlement-faction gameplay contract definition, focused settlement-faction enforcement coverage, large-battle AI/pathfinding optimization with explicit profiling, and any remaining optional non-critical/custom payload compatibility follow-up
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
- Phase 01 planning artifacts: `.planning/phases/01-workspace-bootstrap/`
- Latest execution summary: `.planning/phases/07-dedicated-server-authority-edge-validation/07-dedicated-server-authority-edge-validation-02-SUMMARY.md`

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
- [Slice follow-up]: `./gradlew verifyGameTestStage` is currently green with 26 required tests after adding the live settlement-to-military supply GameTest.
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
- [Performance roadmap]: Keep large-battle AI/performance work ordered as profiling baseline, global pathfinding control, path reuse, formation-level target selection, pathfinding throttling, async reliability fixes, AI LOD, optional flow-field evaluation, and closing performance validation.
- [Performance roadmap]: Require explicit profiling evidence before optimization, during each optimization slice, and after the full sequence so future tuning work can compare against one stable baseline.
- [Performance roadmap]: Treat flow-field navigation as optional and benchmark-gated rather than a mandatory rewrite of the current navigation stack.
- [Phase 07-dedicated-server-authority-edge-validation]: Create a dedicated-server helper seam now so later reconnect and persistence tests can reuse deterministic fake-player and detached-ownership setup.
- [Phase 07-dedicated-server-authority-edge-validation]: Model admin recovery with an explicit permission-granting fake player so offline-owner authority remains server-driven without requiring a live owner entity.
- [Phase 07-dedicated-server-authority-edge-validation]: Dedicated-server reconnect tests should use a live per-call fake player entity, not the cached fake-player factory path, so same-UUID command recovery exercises the real nearby-selection code path.
- [Phase 07-dedicated-server-authority-edge-validation]: Reconnect persistence tests may reseed the transient recruit command-group state immediately before serialization when the plan is validating ownership round-trips rather than group-manager persistence.

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

## Session

- Last updated: 2026-04-11T19:09:00Z
- Stopped at: Completed 07-02-PLAN.md; phase verification blocked by unrelated existing GameTest failures (`invalidleaderandscoutpacketsfailsafely`, `packetdrivenrecoveryrestoresholdintentaftercombat`)
- Resume file: .planning/phases/07-dedicated-server-authority-edge-validation/07-CONTEXT.md
