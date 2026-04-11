# Phase 11 Profiling Baseline

## Purpose

- Define the canonical large-battle profiling matrix for BannerMod before any AI or pathfinding optimization work lands.
- Keep Phase 11 evidence-first: this document names the scenarios, hotspots, counters, and evidence package, but does not add runtime instrumentation.
- Anchor the baseline to already-shipped merged-runtime seams so later phases rerun real recruit combat behavior instead of a detached benchmark harness.

## Baseline Sources Of Truth

- Battle density scenarios: `recruits/src/gametest/java/com/talhanation/recruits/gametest/battle/BattleStressFixtures.java`
- Stress assertions and timing windows: `recruits/src/gametest/java/com/talhanation/recruits/gametest/battle/BattleStressGameTests.java`
- Comparison battle harness: `recruits/src/gametest/java/com/talhanation/recruits/gametest/battle/MixedSquadBattleGameTests.java`
- Recruit target search cadence: `recruits/src/main/java/com/talhanation/recruits/entities/AbstractRecruitEntity.java`
- Recruit navigation and path creation seam: `recruits/src/main/java/com/talhanation/recruits/entities/ai/navigation/RecruitPathNavigation.java`
- Async path queue and completion seam: `recruits/src/main/java/com/talhanation/recruits/pathfinding/AsyncPathProcessor.java`
- Runtime toggles to record with every capture: `recruits/src/main/java/com/talhanation/recruits/config/RecruitsServerConfig.java`

## Scenario Matrix

| Scenario ID | Source | Why It Stays In Baseline | Expected Shape | Primary Hotspots |
| --- | --- | --- | --- | --- |
| `baseline_dense_battle` | `BattleStressFixtures.BASELINE_DENSE` via `spawnBaselineDenseScenario(...)` | Lowest-risk dense battle already validated in repo and intended as the default future comparison point | 6 west recruits vs 6 east recruits on `battle_density_field`; progress probe at tick 90; resolution deadline at tick 440 | server tick cost, per-recruit target search, navigation path creation |
| `heavy_dense_battle` | `BattleStressFixtures.HEAVY_DENSE` via `spawnHeavyDenseScenario(...)` | Existing heavier stress case that increases density and asymmetry without inventing a new harness | 8 west recruits vs 3 east recruits on `battle_density_field`; progress probe at tick 110; resolution deadline at tick 450 | server tick cost, async path queue pressure, path completion behavior |
| `mixed_squad_battle` | `MixedSquadBattleGameTests.representativeMixedSquadsResolveBoundedBattle(...)` | Existing comparison scenario with mixed unit types on the smaller `battle_harness_field`, useful to separate dense-battle cost from basic mixed-combat cost | west and east mixed squads on `battle_harness_field`; deterministic weakened east side; timeout 340 ticks | target-selection cadence, navigation entrypoint churn, baseline combat tick cost |

### Scenario Rules

- The two stress scenarios are mandatory for every later performance slice because they are the only current dense battle fixtures with explicit progress and resolution checkpoints.
- The mixed-squad scenario is the optional comparison leg of the baseline and should be captured whenever the same profiling setup can run it cheaply in the same session.
- Later phases may add new scenarios only if the repository gains a real runtime harness that broadens coverage; they must not replace these scenario ids when reporting before/after results.

## Hotspot Inventory

### 1. Recruit Tick And Target Search

- `AbstractRecruitEntity.tick()` calls `searchForTargets()` every 20 ticks when the recruit is alive and not in state `3`.
- `searchForTargets()` switches between `searchForTargetsAsync(...)` and `searchForTargetsSync(...)` based on `RecruitsServerConfig.UseAsyncTargetFinding`.
- Both target-search paths inflate a 40-block search box and evaluate nearby `LivingEntity` candidates, making target acquisition one of the first baseline costs to compare across dense battle runs.

### 2. Recruit Navigation And Path Creation

- `RecruitPathNavigation.createPathFinder(int range)` is the navigation seam that chooses the current pathfinder implementation.
- The constructor switches the supplier to `AsyncPathfinder` when `RecruitsServerConfig.UseAsyncPathfinding` is enabled; otherwise recruits remain on the vanilla-style `PathFinder` path.
- `RecruitPathNavigation.moveTo(double x, double y, double z, double speed)` drives actual path creation for recruit movement and is the baseline navigation entrypoint to watch when battles repeatedly reposition.

### 3. Async Path Queue And Completion

- `AsyncPathProcessor.start()` creates the worker pool from `RecruitsServerConfig.AsyncPathfindingThreadsCount`.
- `AsyncPathProcessor.queue(...)` is the current async path issuance seam and also exposes the synchronous fallback path when no executor is available.
- `AsyncPathProcessor.awaitProcessing(...)` is the completion handoff seam that determines when processed paths rejoin server-thread logic.

### 4. Async Target-Finding Worker Pool

- `AbstractRecruitEntity.searchForTargetsAsync(...)` submits work through `AsyncManager.executor`.
- `AsyncManager` sizes that pool from `RecruitsServerConfig.AsyncTargetFindingThreadsCount`.
- Every baseline evidence bundle must record the configured target-finding thread count even if no custom counter exists yet, because later optimization slices may move work between target selection and pathfinding.

## Counters

Phase 11 now exposes a small built-in counter set for recruit target search and async path processing. External profilers are still required for tick-time, GC, and latency-focused evidence.

### Required Counters

| Counter | What It Means | Baseline Source/Collection Intent |
| --- | --- | --- |
| `server_tick_ms` | Mean, p95, and max server tick duration during the measurement window | External profiler or server timing capture during each scenario run |
| `scenario_duration_ticks` | Ticks from scenario release to success/failure or timeout | GameTest scenario timing window and actual completion tick |
| `living_recruit_count` | Total living recruits at start, progress probe, and end of run | Scenario spawn counts plus progress/resolution observations |
| `target_search_total` | Total calls into `AbstractRecruitEntity.searchForTargets()` during the run | Built-in snapshot from `AbstractRecruitEntity.targetSearchProfilingSnapshot()` |
| `target_search_async` | Number of searches that used `searchForTargetsAsync(...)` | Built-in snapshot from `AbstractRecruitEntity.targetSearchProfilingSnapshot()` |
| `target_search_sync` | Number of searches that used `searchForTargetsSync(...)` | Built-in snapshot from `AbstractRecruitEntity.targetSearchProfilingSnapshot()` |
| `target_search_candidates_observed` | Aggregate nearby candidate count seen before filtering during target search | Built-in snapshot from `AbstractRecruitEntity.targetSearchProfilingSnapshot()` |
| `target_assignments` | Number of target-search passes that assigned a target | Built-in snapshot from `AbstractRecruitEntity.targetSearchProfilingSnapshot()` |
| `async_path_queue_submissions` | Number of async path jobs sent through `AsyncPathProcessor.queue(...)` | Built-in snapshot from `AsyncPathProcessor.profilingSnapshot()` |
| `async_path_sync_fallbacks` | Number of path jobs processed synchronously because no async executor was usable | Built-in snapshot from `AsyncPathProcessor.profilingSnapshot()` |
| `async_path_await_calls` | Number of `awaitProcessing(...)` handoff requests observed during the run | Built-in snapshot from `AsyncPathProcessor.profilingSnapshot()` |
| `async_path_deliveries` | Number of non-null path deliveries handed back after processing | Built-in snapshot from `AsyncPathProcessor.profilingSnapshot()` |
| `async_path_null_deliveries` | Number of null path deliveries handed back after processing | Built-in snapshot from `AsyncPathProcessor.profilingSnapshot()` |
| `path_completion_latency_ms` | Time from path request submission to `awaitProcessing(...)` completion handoff | Still requires external profiler/span data; not directly emitted by the built-in snapshot |
| `gc_pause_ms` | Any GC pause time observed during the capture window | JVM/Java Flight Recorder or equivalent external capture |

### Required Config Fields

Every evidence bundle must also record the exact runtime settings below, even when they are left at defaults.

- `UseAsyncPathfinding`
- `AsyncPathfindingThreadsCount`
- `UseAsyncTargetFinding`
- `AsyncTargetFindingThreadsCount`
- Java version
- JVM arguments used for profiling
- world/template used for the run (`battle_density_field` or `battle_harness_field`)

## Evidence Package

Store each real baseline capture under:

`/.planning/phases/11-large-battle-ai-profiling-baseline/evidence/<capture-id>/`

Suggested `capture-id` format:

`YYYY-MM-DDTHHMMSSZ-<operator>-<scenario-id>-<config-label>`

### Required Files

| File | Purpose |
| --- | --- |
| `metadata.md` | Human-readable summary of scenario, config, command, warmup, measurement window, result, and logged GameTest snapshot lines |
| `config.toml` or `config.md` | Snapshot of the relevant Recruits server config values used for the run |
| `summary.json` | Machine-readable normalized counter output for before/after comparison |
| `raw/` | Raw profiler exports, logs, spark outputs, JFR files, or other unmodified measurement artifacts |
| `notes.md` | Deviations, failed attempts, warmup anomalies, and any environment caveats |

### `summary.json` Minimum Fields

```json
{
  "phase": "11",
  "plan": "02-instrumented-baseline",
  "scenarioId": "baseline_dense_battle",
  "templateId": "battle_density_field",
  "captureId": "2026-04-11T000000Z-local-baseline_dense_battle-default",
  "measurementWindowTicks": 440,
  "warmupTicks": 40,
  "startEntityCount": 12,
  "endEntityCount": 3,
  "useAsyncPathfinding": true,
  "asyncPathfindingThreads": 1,
  "useAsyncTargetFinding": true,
  "asyncTargetFindingThreads": 1,
  "serverTickMsAvg": 0.0,
  "serverTickMsP95": 0.0,
  "serverTickMsMax": 0.0,
  "targetSearchTotal": 0,
  "targetSearchAsync": 0,
  "targetSearchSync": 0,
  "targetSearchCandidatesObserved": 0,
  "targetAssignments": 0,
  "asyncPathQueueSubmissions": 0,
  "asyncPathSyncFallbacks": 0,
  "asyncPathAwaitCalls": 0,
  "asyncPathDeliveries": 0,
  "asyncPathNullDeliveries": 0,
  "pathCompletionLatencyMsAvg": 0.0,
  "gcPauseMsTotal": 0.0,
  "result": "success",
  "rawCapturePath": "raw/"
}
```

## Capture Boundaries

- Warmup is part of the evidence contract. Unless a later slice proves a better rule, use a short settle/warmup period before measuring so entity spawn, world-start, and class-loading noise do not dominate the comparison.
- `BattleStressFixtures.PROFILING_WARMUP_TICKS` is now the canonical warmup constant and currently resolves to `40` ticks.
- The default measurement window should cover the scenario from post-spawn settle through the existing progress probe and out to success or resolution timeout.
- If a run fails the existing GameTest assertions, keep the raw artifacts but mark the evidence bundle invalid for baseline comparison.

## Implemented Capture Path

- `BattleStressGameTests` now resets profiling counters before each dense-battle scenario spawn.
- The stress harness captures one built-in snapshot at the progress probe and one at resolution using `RecruitsBattleGameTestSupport.captureProfilingSnapshot(...)`.
- The harness keeps the original combat assertions and adds only coarse profiling invariants: dense-battle runs must show non-zero target-search activity, observed target candidates, and async path activity when async pathfinding is enabled.
- Each snapshot is logged through `Main.LOGGER` using a stable field list so raw GameTest logs can be copied into the evidence bundle alongside external profiler artifacts.

## Acceptance Criteria For Later Phases

- Every optimization or instrumentation slice from Phases 12-19 must rerun at least the two mandatory stress scenarios against the same counter vocabulary.
- Before/after reports must compare the same scenario id, template id, async toggles, and thread-count settings.
- New counters may be added later, but the required counters and config fields in this document must remain present so long-term comparisons stay honest.
