# Phase 12 Validation

## Purpose

- Verify that routing recruit path issuance through `GlobalPathfindingController` preserves current battle behavior.
- Capture Phase 12 evidence with the same scenario ids and async-config vocabulary defined by the Phase 11 baseline.
- Make before/after comparisons explicit without introducing new profiler harnesses or alternate scenarios.

## Before/After Sources

- **Before:** Phase 11 evidence and procedure in `11-PROFILING-BASELINE.md` and `11-VALIDATION.md`
- **After:** Phase 12 runs using the same scenarios plus the new controller counters emitted by `RecruitsBattleGameTestSupport.captureProfilingSnapshot(...)`

## Required Commands

Run from the repository root:

1. `./gradlew compileJava test --tests com.talhanation.recruits.pathfinding.GlobalPathfindingControllerTest`
2. `./gradlew compileGameTestJava verifyGameTestStage`

If the unit test step fails, Phase 12 is not ready for GameTest comparison. If `verifyGameTestStage` fails, keep the raw logs and treat the capture as invalid for comparison.

## Config Snapshot

Record the same Phase 11 config fields for every capture bundle:

- `UseAsyncPathfinding`
- `AsyncPathfindingThreadsCount`
- `UseAsyncTargetFinding`
- `AsyncTargetFindingThreadsCount`
- Java version
- JVM arguments
- Whether the run started from a clean server session

## Scenario Order

Run and compare scenarios in this order:

1. `baseline_dense_battle` *(mandatory)*
2. `heavy_dense_battle` *(mandatory)*
3. `mixed_squad_battle` *(optional comparison leg when the same runtime session can capture it cheaply)*

Do not rename or replace these scenario ids in reports.

## Reset Behavior

- `BattleStressGameTests` must reset `AbstractRecruitEntity`, `AsyncPathProcessor`, and `GlobalPathfindingController` profiling before each dense-battle scenario.
- `MixedSquadBattleGameTests` should start from `GlobalPathfindingController.resetProfiling()` before asserting controller activity.
- Treat runs without a reset as contaminated and exclude them from before/after comparison.

## Required Phase 12 Snapshot Fields

Every Phase 12 evidence bundle must include the existing Phase 11 counters plus these controller fields from `GlobalPathfindingController.profilingSnapshot()`:

- `controllerRequests`
- `controllerBlockRequests`
- `controllerEntityRequests`
- `controllerAsyncEnabled`
- `controllerAsyncDisabled`
- `controllerTargetPositions`

The stable log line emitted through `RecruitsBattleGameTestSupport.formatProfilingSnapshot(...)` is the canonical built-in snapshot format.

## Evidence Layout

Store captures under:

`/.planning/phases/12-global-pathfinding-control/evidence/<capture-id>/`

Minimum layout:

```text
.planning/phases/12-global-pathfinding-control/evidence/<capture-id>/
  metadata.md
  config.md
  summary.json
  notes.md
  raw/
```

### `summary.json` additions

Use the Phase 11 shape and add these controller-aware fields:

```json
{
  "phase": "12",
  "scenarioId": "baseline_dense_battle",
  "controllerRequests": 0,
  "controllerBlockRequests": 0,
  "controllerEntityRequests": 0,
  "controllerAsyncEnabled": 0,
  "controllerAsyncDisabled": 0,
  "controllerTargetPositions": 0
}
```

## Comparison Rules

- Compare Phase 12 runs against Phase 11 only when scenario id, template id, async toggles, and thread counts match.
- Correctness comes first: the mandatory GameTests must still pass before interpreting profiling deltas.
- Expect controller counts to be non-zero in both dense-battle scenarios and in the optional mixed-squad comparison when captured.
- A changed controller-request picture is acceptable in Phase 12 only if battle outcomes and bounded-resolution assertions still match the existing GameTest contract.

## Acceptance Truth

Phase 12 is validated when:

- `GlobalPathfindingControllerTest` passes.
- `verifyGameTestStage` passes with the mixed-squad and dense-battle scenarios still green.
- Dense-battle snapshot logs include the new controller counters beside the existing target-search and async-path counters.
- Evidence bundles clearly label Phase 11 as the before-state and Phase 12 as the after-state.
