# Phase 11 Validation

## Capture Procedure

Plan 11-02 now adds the minimum built-in counters and stress-harness snapshot path needed to collect repeatable baseline evidence without changing battle behavior.

### Prerequisites

Run from the repository root:

1. `./gradlew compileGameTestJava test`
2. Confirm the current recruit battle fixtures still compile and the root test baseline is green.
3. Prepare the profiling tool you plan to use for raw capture, such as Java Flight Recorder or another external sampler. The built-in counters complement these tools; they do not replace external tick or latency profiling.

### Config Snapshot

Before each profiling session, record:

- `UseAsyncPathfinding`
- `AsyncPathfindingThreadsCount`
- `UseAsyncTargetFinding`
- `AsyncTargetFindingThreadsCount`
- JVM version and JVM arguments
- Whether the run used a clean server start or a reused runtime session

If any value differs across runs, treat those captures as separate configuration groups instead of one baseline.

### Scenario Execution Order

Run scenarios in this order unless a later tool limitation forces a different order and the change is documented in `notes.md`:

1. `baseline_dense_battle`
2. `heavy_dense_battle`
3. `mixed_squad_battle` when the same profiler setup can capture it without changing runtime configuration

Reasoning:

- `baseline_dense_battle` is the default lowest-risk comparison point.
- `heavy_dense_battle` extends the same harness into the denser stress case.
- `mixed_squad_battle` is a secondary comparison leg that helps detect whether a result only helps the dense fixture.

### Measurement Window

- Start measurement only after a short settle/warmup window once the scenario has spawned and mutual targets have been assigned.
- Default warmup rule: `BattleStressFixtures.PROFILING_WARMUP_TICKS` (`40` ticks).
- Default measurement end: scenario success, scenario failure, or the existing GameTest timeout/resolution boundary, whichever happens first.
- Record the actual measured tick range in `metadata.md` and `summary.json`.
- Copy the two GameTest snapshot log lines per stress run into `metadata.md` or store the full raw log under `raw/`.

### Sample Expectation

- Collect at least 3 valid runs per mandatory stress scenario per configuration group.
- If run-to-run variance is visibly high, expand to 5 runs before drawing conclusions.
- The optional mixed-squad comparison should also be captured 3 times when included.

### Raw Artifact Storage

Store the outputs under:

`/.planning/phases/11-large-battle-ai-profiling-baseline/evidence/<capture-id>/`

Minimum layout:

```text
.planning/phases/11-large-battle-ai-profiling-baseline/evidence/<capture-id>/
  metadata.md
  config.md
  summary.json
  notes.md
  raw/
```

Keep raw profiler exports unmodified in `raw/`. Any post-processed tables or charts belong in `metadata.md` or separate derived files, not as replacements for the raw data.

### Built-In Snapshot Fields

The current stress harness emits or makes available these built-in fields per snapshot:

- `targetSearch.totalSearches`
- `targetSearch.asyncSearches`
- `targetSearch.syncSearches`
- `targetSearch.candidateEntitiesObserved`
- `targetSearch.targetsAssigned`
- `pathfinding.queueSubmissions`
- `pathfinding.syncFallbacks`
- `pathfinding.awaitCalls`
- `pathfinding.deliveredPaths`
- `pathfinding.deliveredNullPaths`

These fields come from `AbstractRecruitEntity.targetSearchProfilingSnapshot()` and `AsyncPathProcessor.profilingSnapshot()`, captured through `RecruitsBattleGameTestSupport.captureProfilingSnapshot(...)`.

## Acceptance Rules

A Phase 11 baseline capture is complete enough to compare against later phases only if all of the following are true:

- The run uses one of the canonical scenario ids from `11-PROFILING-BASELINE.md`.
- The evidence bundle records the four required async config values and the JVM/profiler context.
- The raw artifact directory is present and referenced from `summary.json`.
- The capture reports the built-in counter vocabulary above and explicitly marks any remaining external-only metrics, such as tick-time percentiles or path-completion latency, as external-profiler fields.
- The underlying GameTest run completed without changing AI behavior to make the profile easier to collect.
- At least 3 valid runs exist for each mandatory stress scenario in the same configuration group.

## Current Truth

- Plan 11-01 defined the profiling contract.
- Plan 11-02 now exposes resettable built-in counters for target-search and async path-processing activity and wires them into the dense-battle GameTest harness.
- Baseline collection still depends on external profiler outputs for server tick cost, GC pause analysis, and any latency-focused metrics not exposed by the built-in snapshot.
