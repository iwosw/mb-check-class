# Phase 19 Context

## Phase

- Number: 19
- Name: Large-Battle Performance Validation

## Goal

- Close the current AI/performance proposal with comparable before/after large-battle profiling evidence and an explicit record of remaining hotspots.

## Why This Phase Exists

- Optimization work is incomplete without a closing pass that compares results against the original baseline.
- The roadmap needs an explicit validation phase so profiling remains a deliverable, not just an implementation aid.
- A final evidence pass will show whether optional follow-up work is still needed after the ordered optimization slices land.

## Scope

- Re-run the agreed Phase 11 scenarios after the preceding optimization slices complete.
- Capture before/after comparisons, remaining hotspots, and recommended follow-up backlog.
- Keep the phase evidence-first rather than adding fresh optimization experiments.

## Planned Execution Slices

- Plan 19-01: Re-run the agreed large-battle profiling scenarios against the optimized runtime and capture comparable evidence.
- Plan 19-02: Publish the before/after analysis, residual hotspots, and recommended next performance backlog based on measured results.

## Constraints

- Use the same baseline scenarios where possible.
- Report residual debt honestly even if some hotspots remain.
- Do not reopen the implementation sequence during the closing validation phase.

## Evidence

- `.planning/ROADMAP.md`
- `.planning/STATE.md`
- `.planning/VERIFICATION.md`
- `.planning/phases/11-large-battle-ai-profiling-baseline/11-CONTEXT.md`
