# Pathfinding Restoration Summary

Branch: `restore-pathfinding`, based on `master` HEAD `f459ae6`.

## Commits

| Hash | Subject |
|------|---------|
| `accf542` | feat(12): restore GlobalPathfindingController seam + pathfinding budget config |
| `ad812b7` | test(12-13-15): port GlobalPathfindingControllerTest |
| `a5a4b0f` | feat(16): restore AsyncPathProcessor profiling counters + stale-callback guard |
| `f88486b` | test(14): port FormationTargetSelectionControllerTest + BattleTacticDeciderTest |

## What landed

### Phase 12 (`GlobalPathfindingController` seam)

- `src/main/java/com/talhanation/bannermod/ai/pathfinding/GlobalPathfindingController.java` — 604-line pass-through controller with:
  - `PathRequest` / `PathRequestResult` / `RequestStatus` / `RequestKind` types
  - `ReuseContext` / `FlowFieldPrototypeRequest` / `DeferredPathTicket` / `DeferredDropReason` types
  - `ProfilingSnapshot` record exposing 31 counters
  - `requestPath(...)` / `discardDeferred(...)` / `resetProfiling()` / `profilingSnapshot()`
  - Test hooks: `configureBudgetForTests(...)`, `clearBudgetOverrideForTests()`, `rememberCandidateForTests(...)`, `tryReuseForTests(...)`, `tryFlowFieldPrototypeForTests(...)`

### Phase 13 (path reuse)

- Reuse logic lives inside the controller (part of the 710-line restoration):
  - `tryReuse()` with NO_CANDIDATE, NULL_CANDIDATE, UNPROCESSED_CANDIDATE, DONE_CANDIDATE, INCOMPATIBLE_CANDIDATE, STALE_CANDIDATE drop counters
  - `copyPath(...)` — reused paths are copied before return (no shared mutable references)

### Phase 15 (throttling/budget)

- Budget accounting lives inside the controller:
  - `BudgetSettings` record pulled from `RecruitsServerConfig.PathfindingRequestBudgetPerTick/MaxDeferredBacklog/MaxDeferredTicks`
  - `currentDeferredQueueDepth`, `maxDeferredQueueDepth`, `totalDeferredLatencyTicks`, `maxDeferredLatencyTicks` counters
  - Explicit deferred-drop reasons: `BACKLOG_CAP`, `MAX_AGE`, `INVALIDATED`
- `src/main/java/com/talhanation/bannermod/config/RecruitsServerConfig.java` — three new worldRestart fields (defaults 32/128/20).

### Phase 16 (async hardening)

- `src/main/java/com/talhanation/bannermod/ai/pathfinding/AsyncPathProcessor.java` — restored 4-arg `deliverProcessedPath(..., BooleanSupplier shouldDeliver, ...)` and 4-arg `awaitProcessing(..., BooleanSupplier, ...)`, plus `ProfilingSnapshot` with `queueSubmissions`, `syncFallbacks`, `awaitCalls`, `deliveredPaths`, `deliveredNullPaths`, `droppedCallbacks`. 3-arg overloads preserved, so existing `AsyncPathNavigation` callers don't change.

### Tests ported

| File | Source |
|------|--------|
| `src/test/java/com/talhanation/bannermod/ai/pathfinding/GlobalPathfindingControllerTest.java` | 355 lines — full controller contract (reuse, budget, deferred, flow-field fallback, snapshot reset) |
| `src/test/java/com/talhanation/bannermod/ai/pathfinding/AsyncPathProcessorTest.java` | 111 lines — delivery validator, executor handoff, sync-fallback, profiling reset |
| `src/test/java/com/talhanation/bannermod/ai/military/FormationTargetSelectionControllerTest.java` | 170 lines — cohort reuse/invalidation/fallback semantics |
| `src/test/java/com/talhanation/bannermod/ai/military/controller/BattleTacticDeciderTest.java` | 38 lines — three representative tactic decisions |

All four were ported verbatim with only the package line swapped.

## Gate results

- `./gradlew compileJava` — GREEN (only pre-existing deprecation warnings; no errors).
- `./gradlew compileTestJava` — RED but only due to pre-existing D-05 brownfield debt in `BannerModSettlementBindingTest.java` (documented in `MERGE_NOTES.md`). None of the 21 errors touch any file in this branch.
- Running tests in isolation (`--tests com.talhanation.bannermod.ai.pathfinding.*`) was not attempted because `compileTestJava` must first pass, and that is blocked by D-05 test debt outside this slice's scope.

## Explicitly not done in this slice

1. **AsyncPathNavigation integration** — current active `AsyncPathNavigation.java` (297 lines) is much slimmer than the pre-pivot version (604 lines). Routing path requests through `GlobalPathfindingController.requestPath(...)` requires a careful merge against the refactored active file. Left as a follow-up slice so each change stays reviewable.
2. **D-05 test-tree cleanup** — needs a separate test-stabilization slice that removes the deprecated `com.talhanation.bannermod.settlement.*` forwarder or fixes imports in `BannerModSettlementBindingTest.java`. Out of pathfinding scope.
3. **Phase 11 / Phase 19 empirical profiling capture** — counters now exist but no actual baseline or optimized-runtime measurement runs were executed in this slice.

## Merge-back direction

When ready, merge `restore-pathfinding` into `master` with `git merge --ff-only` or rebase. Four commits are atomic and can be landed individually if preferred.
