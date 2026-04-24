# Phase 13 Validation

## Purpose

- Verify that the Phase 13 path-reuse seam preserves battle correctness before any profiling claim is accepted.
- Keep the evidence directly comparable with the Phase 11 baseline and Phase 12 controller-only captures.
- Make reuse observability explicit through hit, miss, and invalidation churn counters on the shared controller seam.

## Required Commands

Run from the repository root after Plan 13-01 lands:

1. `./gradlew test --tests com.talhanation.bannermod.ai.pathfinding.GlobalPathfindingControllerTest`
2. `./gradlew compileGameTestJava verifyGameTestStage`

If the unit test step fails, the reuse seam is not ready for evidence collection. If `verifyGameTestStage` fails, keep the raw logs but treat the run as invalid evidence even if some profiling lines were emitted.

## Scenario Order

Reuse the existing Phase 11 and Phase 12 scenario order exactly:

1. `baseline_dense_battle` *(mandatory)*
2. `heavy_dense_battle` *(mandatory)*
3. `mixed_squad_battle` *(optional comparison leg for evidence bundles, but still covered during `verifyGameTestStage` correctness validation)*

Do not rename the scenario ids. Phase-to-phase comparison depends on the same scenario identity and world templates.

## Config Snapshot

Record the same config vocabulary already required by Phases 11 and 12:

- `UseAsyncPathfinding`
- `AsyncPathfindingThreadsCount`
- `UseAsyncTargetFinding`
- `AsyncTargetFindingThreadsCount`
- Java version
- JVM arguments
- Whether the run started from a clean server session

Treat config changes as a new comparison group instead of mixing them into one report.

## Reset Rules

- `BattleStressGameTests` must reset target-search, async-path, and `GlobalPathfindingController` profiling before each dense-battle scenario.
- `MixedSquadBattleGameTests` must begin from `GlobalPathfindingController.resetProfiling()` before asserting controller observability.
- Evidence from runs that skipped reset is contaminated and should not be compared against Phase 11 or Phase 12 captures.

## Required Phase 13 Snapshot Fields

Keep all Phase 11 and Phase 12 snapshot fields, then add the Phase 13 reuse metrics from `GlobalPathfindingController.profilingSnapshot()`:

- `controllerReuseAttempts`
- `controllerReuseHits`
- `controllerReuseMisses`
- `controllerReuseMissesNoCandidate`
- `controllerReuseDropsNull`
- `controllerReuseDropsUnprocessed`
- `controllerReuseDropsDone`
- `controllerReuseDropsIncompatible`
- `controllerReuseDropsStale`

The active root dense-battle profiling harness remains the canonical stable log format.

## Evidence Interpretation

Correctness first:

- A run with dead-target retention, broken resolution timing, or any other GameTest failure is invalid evidence.
- Reuse hit rate is only meaningful after the mandatory correctness checks pass.

Comparison guidance:

- Compare Phase 13 against Phase 11 only when scenario id, template id, async toggles, and thread counts match.
- Compare Phase 13 against Phase 12 to see whether reuse reduced duplicate controller work or simply shifted misses into churn.
- Use `controllerReuseHits / controllerReuseAttempts` as the hit-rate headline.
- Use `controllerReuseMisses / controllerReuseAttempts` as the miss-rate headline.
- Watch invalidation churn through the drop counters, especially stale and incompatible drops.
- Zero reuse hits are acceptable in a scenario only when the scenario genuinely offered no compatible reuse opportunities; zero reuse attempts are not acceptable for the mandatory dense scenarios once the seam is active.

## Evidence Layout

Store captures under:

`/.planning/phases/13-path-reuse-for-cohesion-movement/evidence/<capture-id>/`

Minimum layout:

```text
.planning/phases/13-path-reuse-for-cohesion-movement/evidence/<capture-id>/
  metadata.md
  config.md
  summary.json
  notes.md
  raw/
```

## Acceptance Truth

Phase 13 is validated when:

- `GlobalPathfindingControllerTest` passes.
- `verifyGameTestStage` passes with `baseline_dense_battle`, `heavy_dense_battle`, and the existing mixed-squad correctness flow still green.
- Dense-battle logs include the reuse counters beside the existing target-search, async-path, and controller counters.
- Reports clearly identify Phase 11 as the baseline, Phase 12 as the controller-only before-state for reuse, and Phase 13 as the reuse-aware after-state.
