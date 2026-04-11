---
gsd_state_version: 1.0
milestone: v1.0
milestone_name: milestone
status: complete
stopped_at: Completed milestone verification and closure
last_updated: "2026-04-09T13:38:08Z"
last_activity: 2026-04-09
progress:
  total_phases: 8
  completed_phases: 8
  total_plans: 28
  completed_plans: 28
  percent: 100
---

# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-04-09)

**Core value:** The dev branch must become a trustworthy base where the core NPC army mechanics behave predictably, are defended by tests, and are structurally ready for a near-term 1.21.1 migration.
**Current focus:** v1 milestone complete - stabilized branch and bounded 1.21.1 follow-up ready

## Current Position

Phase: 8 of 8 (1.21.1 Port Prep Inventory)
Plan: 2 of 2 in current phase
Status: Milestone complete — canonical verification green
Last activity: 2026-04-09

Progress: [██████████] 100%

## Performance Metrics

**Velocity:**

- Total phases completed: 8
- Total plans completed: 28
- Canonical milestone verification: `./gradlew check --continue` passed on 2026-04-09

**By Phase:**

| Phase | Plans | Total | Avg/Plan |
|-------|-------|-------|----------|
| 01-build-reproducibility-baseline | 3 | 27min | 9min |

**Recent Trend:**

- Last milestone actions: Phase 8 closed on 2026-04-08; milestone audit completed green on 2026-04-09
- Trend: Complete

| Phase 01-build-reproducibility-baseline P01 | 5min | 1 tasks | 2 files |
| Phase 01-build-reproducibility-baseline P02 | 14min | 2 tasks | 4 files |
| Phase 01-build-reproducibility-baseline P03 | 8min | 2 tasks | 3 files |
| Phase 02-layered-test-harness-foundations P01 | 572 | 2 tasks | 4 files |
| Phase 02-layered-test-harness-foundations P02 | 11 | 2 tasks | 8 files |
| Phase 02-layered-test-harness-foundations P03 | 4 | 2 tasks | 4 files |
| Phase 03-battle-and-formation-regression-lockdown P01 | 9 | 2 tasks | 6 files |
| Phase 03-battle-and-formation-regression-lockdown P02 | 14 | 2 tasks | 3 files |
| Phase 03 P04 | 4 | 2 tasks | 3 files |
| Phase 04-command-and-ai-state-stabilization P01 | 19 | 3 tasks | 4 files |
| Phase 04-command-and-ai-state-stabilization P02 | 85 | 2 tasks | 11 files |
| Phase 04-command-and-ai-state-stabilization P03 | 31 | 2 tasks | 6 files |
| Phase 04-command-and-ai-state-stabilization P04 | 42 | 2 tasks | 7 files |
| Phase 05 P01 | 12 | 2 tasks | 5 files |
| Phase 05 P02 | 12 | 2 tasks | 4 files |
| Phase 05 P03 | 15 | 2 tasks | 5 files |
| Phase 05 P04 | 28 | 2 tasks | 4 files |
| Phase 06-full-surface-verification-and-safe-degradation P01 | 121 | 2 tasks | 2 files |
| Phase 06-full-surface-verification-and-safe-degradation P02 | 363 | 2 tasks | 5 files |
| Phase 06-full-surface-verification-and-safe-degradation P03 | 913 | 2 tasks | 3 files |
| Phase 07-migration-ready-internal-seams P01 | 3 | 2 tasks | 4 files |
| Phase 07-migration-ready-internal-seams P02 | 6 | 2 tasks | 4 files |
| Phase 07-migration-ready-internal-seams P03 | 7 | 2 tasks | 6 files |
| Phase 07-migration-ready-internal-seams P04 | 7 | 2 tasks | 6 files |
| Phase 08-1-21-1-port-prep-inventory P01 | 3 | 2 tasks | 2 files |
| Phase 08-1-21-1-port-prep-inventory P02 | 2 | 2 tasks | 2 files |

## Accumulated Context

### Decisions

Decisions are logged in PROJECT.md Key Decisions table.
Recent decisions affecting current work:

- [Phase 1]: Start with reproducible builds and verification entrypoints before deeper stabilization.
- [Phases 3-5]: Split the highest-risk verification work into battles/formations, commands/AI, and persistence/networking.
- [Phases 7-8]: Defer migration-prep seam extraction and final port inventory until behavior is stabilized and covered.
- [Phase 01-build-reproducibility-baseline]: Pinned ForgeGradle to 6.0.52 and MixinGradle to 0.7.38 to stop canonical buildscript drift.
- [Phase 01-build-reproducibility-baseline]: Kept mavenLocal() available only behind -PallowLocalMaven so local artifacts stay out of the default workflow.
- [Phase 01-build-reproducibility-baseline]: Used a pure JVM mods.toml smoke test so the initial test baseline stays independent of Forge runtime bootstrapping.
- [Phase 01-build-reproducibility-baseline]: Kept check as the canonical verification entrypoint while adding helper tasks for build, unit-test, and game-test attribution.
- [Phase 01-build-reproducibility-baseline]: Made verifyGameTestStage succeed with a clear NO-SOURCE Phase 2 message until the GameTest source tree exists.
- [Phase 01-build-reproducibility-baseline]: Kept README.md compact and moved detailed troubleshooting plus override guidance into BUILDING.md.
- [Phase 01-build-reproducibility-baseline]: Documented ./gradlew check --continue as the practical verification invocation so later stages run when possible.
- [Phase 02-layered-test-harness-foundations]: Use a dedicated gametest source set mapped to src/gametest/java and src/gametest/resources instead of placeholder source detection.
- [Phase 02-layered-test-harness-foundations]: Keep verifyGameTestStage under check and route it directly to runGameTestServer so GameTests stay a first-class verification layer.
- [Phase 02-layered-test-harness-foundations]: Use a reusable empty harness template plus a namespaced smoke test as the baseline runtime contract for later gameplay tests.
- [Phase 02-layered-test-harness-foundations]: Use test-only fixture builders and round-trip assertions instead of moving helpers into src/main/java.
- [Phase 02-layered-test-harness-foundations]: Instantiate RecruitsClaim fixtures through its private UUID constructor so pure JVM tests avoid Forge config boot requirements.
- [Phase 02-layered-test-harness-foundations]: Add CoreLib's API artifact to testImplementation so codec tests can compile against existing Message classes.
- [Phase 02-layered-test-harness-foundations]: Keep recruit runtime helpers in the GameTest tree so later gameplay plans can reuse them without touching production code.
- [Phase 02-layered-test-harness-foundations]: Use a dedicated recruit_spawn_pad template so future recruit scenarios can evolve independently from the empty harness baseline.
- [Phase 02-layered-test-harness-foundations]: Use the canonical ./gradlew check --continue entrypoint for end-of-phase verification after the new GameTests land.
- [Phase 03-battle-and-formation-regression-lockdown]: Use helper-owned mixed-squad loadouts and owner assignment so later Phase 3 tests stay deterministic instead of relying on random recruit equipment.
- [Phase 03-battle-and-formation-regression-lockdown]: Assert formation behavior through public hold-position, follow-state, spacing, and anchor signals rather than private field inspection.
- [Phase 03-battle-and-formation-regression-lockdown]: Keep battle and recovery templates as dedicated copies now so later plans can evolve them independently from the recruit spawn baseline.
- [Phase 03-battle-and-formation-regression-lockdown]: Represent patrol-leader inputs as primitive ArmySnapshot values so tactic coverage stays pure JVM and Forge-free.
- [Phase 03-battle-and-formation-regression-lockdown]: Preserve the controller's outward messages and command methods while moving only the branching decision into BattleTacticDecider.
- [Phase 03]: Kept battle-density verification on a dedicated copied battlefield template so later stress layouts can diverge without disturbing the baseline harness.
- [Phase 03]: Used resolution deadlines, arena bounds, and stale-target checks as dense-combat alarms instead of exact casualty scripts.
- [Phase 04-command-and-ai-state-stabilization]: Keep command targeting pure by expressing packet inputs as RecruitSnapshot records and explicit selection result records.
- [Phase 04-command-and-ai-state-stabilization]: Preserve the legacy 100-block command radius as a named constant and surface invalid flows through Failure enums instead of silent behavior.
- [Phase 04-command-and-ai-state-stabilization]: Area command packets now validate against the actual server sender UUID instead of trusting packet payload UUIDs.
- [Phase 04-command-and-ai-state-stabilization]: Runtime authority coverage drives public packet dispatch helpers so GameTests exercise the same filtering logic as production packet execution.
- [Phase 04-command-and-ai-state-stabilization]: Leader and scout packets should validate the addressed entity through CommandTargeting.forSingleRecruit before mutating any state.
- [Phase 04-command-and-ai-state-stabilization]: Route assignment treats mismatched waypoint and wait payloads as invalid and degrades to a safe no-op.
- [Phase 04-command-and-ai-state-stabilization]: Command-to-AI normalization should live in a pure helper so move-arrival and patrol interruption semantics stay JVM-testable.
- [Phase 04-command-and-ai-state-stabilization]: Ownerless messenger recovery should restore listening state when it returns to its initial position instead of staying stuck in a silent idle drift.
- [Phase 04-command-and-ai-state-stabilization]: Phase 4 verification is accepted as complete when the only remaining canonical check failures are the already-accepted out-of-scope Phase 3 battle tests.
- [Phase 05]: Lock Phase 5 persistence behavior through pure JVM round-trip tests instead of runtime-only reload checks.
- [Phase 05]: Move static-map clearing from constructors into load paths so fresh instances do not erase already-loaded server state.
- [Phase 05]: Expose narrow manager-side persistence helpers so mutation behavior stays JVM-testable without a broad storage rewrite.
- [Phase 05]: ClientManager now owns one explicit synchronized-cache reset path while leaving the local route library intact.
- [Phase 05]: Runtime persistence coverage uses representative entity save-data round trips for patrol leaders instead of exhaustive world migration simulation.
- [Phase 06]: Use one repo-level verification matrix to classify subsystem coverage as deep automated, smoke/manual, or accepted deferred.
- [Phase 06]: Keep accepted Phase 3 battle failures visible in the matrix so canonical verification output is interpreted consistently.
- [Phase 06]: Verify representative optional compat absence through narrow helper seams instead of broad compat expansion.
- [Phase 06]: Make async fallback delivery testable through package-private executor and handoff helpers rather than rewriting pathfinding architecture.
- [Phase 06]: Treat the final Phase 6 pass as successful when canonical check is red only because of explicitly accepted inherited battle-density debt.
- [Phase 06]: Drop previously accepted mixed-squad debt from the remaining ledger when it no longer reproduces in the current full-surface pass.
- [Phase 07-migration-ready-internal-seams]: Map all six Phase 7 risk surfaces into three contract containers so later plans can refactor against explicit seam types instead of rediscovering inline Forge coupling.
- [Phase 07-migration-ready-internal-seams]: Keep the migration contracts record/interface-only with no bootstrap, SavedData, reflection, or executor side effects.
- [Phase 07-migration-ready-internal-seams]: Capture the current packet wire order with targeted sentinel tests before moving registration out of Main.
- [Phase 07-migration-ready-internal-seams]: Delegate common lifecycle and runtime listener wiring through ModLifecycleRegistrar while leaving gameplay behavior unchanged.
- [Phase 07-migration-ready-internal-seams]: Keep the client sync seam focused on route-preserving reset plus siege derivation instead of moving broader GUI cache logic.
- [Phase 07-migration-ready-internal-seams]: Model faction persistence orchestration as callback-driven apply, dirty, and broadcast steps so the seam stays JVM-testable.
- [Phase 07-migration-ready-internal-seams]: Use one reflective compat helper with injectable resolution instead of spreading raw Class.forName handling across optional-mod callers.
- [Phase 07-migration-ready-internal-seams]: Keep AsyncPathProcessor fallback semantics intact while delegating executor creation and delivery through PathProcessingRuntime.
- [Phase 08]: Anchor the port inventory to Phase 7 seam helpers and summaries instead of reopening repo-wide source discovery.
- [Phase 08]: Group the future 1.21.1 work into three bounded packages so verification debt and preserved behavior stay reviewable during the port.
- [Phase 08]: Use the Phase 8 work-package map as the checklist spine so the later port starts with ordered workstreams instead of ad hoc sequencing.
- [Phase 08]: Carry the Phase 6 accepted battle-density debt into the handoff brief so future verification distinguishes inherited failures from new migration regressions.

### Pending Todos

- Future: profile and fix recruit rendering so large recruit groups stop causing major FPS drops without breaking current visuals or behavior.
- Future: optimize target acquisition and pathfinding, including formation-vs-formation target distribution, non-formation search caching/alternatives, and lower-cost formation pathfinding that still avoids holes and obstacles even if formation cohesion must bend.

### Blockers/Concerns

- No active milestone blockers. Future work is the out-of-scope 1.21.1 port and deferred performance optimization tracked in `REQUIREMENTS.md` v2.

## Session Continuity

Last session: 2026-04-09T13:38:08Z
Stopped at: Completed milestone verification and closure
Resume file: None
