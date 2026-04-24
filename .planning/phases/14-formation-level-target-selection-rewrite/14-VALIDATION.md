# Phase 14 Validation

## Purpose

- Verify that formation-level target selection reduces repeated recruit target-search work without folding pathfinding or combat-behavior rewrites into the same slice.
- Keep the evidence directly comparable with the Phase 11 baseline, Phase 12 controller-only snapshots, and Phase 13 reuse-aware captures.
- Add explicit behavior checks for focus fire, retarget, and loss-of-target handling.

## Required Commands

Run from the repository root after Plan 14-01 lands:

1. `./gradlew test --tests com.talhanation.bannermod.ai.military.FormationTargetSelectionControllerTest`
2. `./gradlew compileGameTestJava verifyGameTestStage`

If the unit test command fails, the formation target-selection seam is not ready for evidence collection. If `verifyGameTestStage` fails, preserve the raw logs but treat the run as invalid phase evidence until every required GameTest is green.

## Scenario Order

Reuse the Phase 11 through Phase 13 scenario order exactly:

1. `baseline_dense_battle` *(mandatory)*
2. `heavy_dense_battle` *(mandatory)*
3. `mixed_squad_battle` *(optional comparison leg for evidence bundles, but still exercised during `verifyGameTestStage` correctness validation)*

Do not rename scenario ids or world templates.

## Config Snapshot

Record the same config vocabulary used by the earlier profiling phases:

- `UseAsyncPathfinding`
- `AsyncPathfindingThreadsCount`
- `UseAsyncTargetFinding`
- `AsyncTargetFindingThreadsCount`
- Java version
- JVM arguments
- Whether the run started from a clean server session

Treat config changes as a new comparison group rather than mixing them into one evidence set.

## Reset Rules

- `BattleStressGameTests` must reset target-search, async-path, `GlobalPathfindingController`, and `FormationTargetSelectionController` profiling before each dense-battle scenario.
- `MixedSquadBattleGameTests` must reset controller profiling before the mixed-squad comparison run.
- `FormationTargetSelectionGameTests` must reset formation-targeting profiling before the focused behavior scenarios.

## Required Phase 14 Snapshot Fields

Keep all Phase 11 through Phase 13 fields, then add the Phase 14 counters from `FormationTargetSelectionController.profilingSnapshot()`:

- `formationRequests`
- `formationComputations`
- `formationAssignments`
- `formationReuses`
- `formationInvalidations`
- `formationLocalFallbacks`

The active root dense-battle profiling harness remains the stable log formatter.

## Focused Behavior Checks

The dedicated Phase 14 GameTests cover:

- `focus fire` - `formationCohortFocusesFireOnOneSharedTarget`
- `retarget` - `formationCohortRetargetsAfterSharedEnemyDies`
- `loss-of-target` - `formationCohortClearsDeadTargetsWhenNoReplacementExists`

If any focused behavior GameTest reports stale targets, missing retargeting, or no shared target acquisition, reject the evidence bundle even if the dense-battle scenarios emit profiling logs.

## Evidence Interpretation

- Compare Phase 14 against Phase 11 when scenario id, template id, async toggles, and thread counts match.
- Compare Phase 14 against Phase 12 to measure how much work moved from per-entity searching into one shared formation seam.
- Compare Phase 14 against Phase 13 to ensure the new targeting counters sit beside the existing controller and reuse metrics without renaming prior fields.
- `formationRequests - formationLocalFallbacks` is the quickest headline for how often grouped recruits actually entered the shared selection seam.
- Treat large `formationInvalidations` counts as churn that needs explanation in notes, not as automatic success.

## Evidence Layout

Store captures under:

`/.planning/phases/14-formation-level-target-selection-rewrite/evidence/<capture-id>/`

Minimum layout:

```text
.planning/phases/14-formation-level-target-selection-rewrite/evidence/<capture-id>/
  metadata.md
  config.md
  summary.json
  notes.md
  raw/
```

## Acceptance Truth

Phase 14 validation is complete when:

- `FormationTargetSelectionControllerTest` passes.
- `verifyGameTestStage` passes with the mandatory dense-battle scenarios and the retained mixed-squad scenario still green.
- Dense-battle logs include the new formation counters beside the existing target-search, async-path, and controller counters.
- Reports call out the focused focus fire, retarget, and loss-of-target checks explicitly.
