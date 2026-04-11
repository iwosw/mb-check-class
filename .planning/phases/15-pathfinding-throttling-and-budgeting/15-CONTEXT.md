# Phase 15 Context

## Phase

- Number: 15
- Name: Pathfinding Throttling And Budgeting

## Goal

- Add explicit, deterministic pathfinding throttles and budgets so large-battle path work cannot burst unbounded in one tick.

## Why This Phase Exists

- After global control, reuse, and formation-level targeting are in place, BannerMod can safely add load-shedding without hiding the root workload behind random delays.
- The proposal orders throttling after those earlier structural changes so budgets can act on a cleaner request stream.
- Queue depth, deferral latency, and recovery behavior need to be measured explicitly or the throttle will be hard to tune later.

## Scope

- Add deterministic pathfinding budgets and request deferral at the shared control seam.
- Expose queue and deferral metrics needed to validate the budget behavior under load.
- Keep the slice focused on synchronous work control rather than async concurrency fixes.

## Planned Execution Slices

- Plan 15-01: Add deterministic pathfinding throttles and budget accounting to the shared control seam.
- Plan 15-02: Validate under load and profile queue depth, deferred-path latency, and tick-cost stability.

## Constraints

- Prefer observable and configurable budget behavior over hidden heuristics.
- Do not widen combat or ownership rules as part of load shedding.
- Preserve correctness when deferred work later resumes.

## Evidence

- `.planning/ROADMAP.md`
- `.planning/STATE.md`
- `.planning/VERIFICATION.md`
- `.planning/phases/14-formation-level-target-selection-rewrite/14-CONTEXT.md`
