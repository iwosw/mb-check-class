# Phase 16 Context

## Phase

- Number: 16
- Name: Async Pathfinding Reliability Fixes

## Goal

- Make async pathfinding safe and predictable under the new global control model by fixing race, cancellation, and stale-result issues.

## Why This Phase Exists

- Async pathfinding can only be repaired cleanly once the synchronous control points and budgets are explicit.
- The proposal places async fixes after throttling, which keeps concurrency remediation from obscuring earlier structural gains.
- Large-battle performance work should not trade CPU cost for invalid path application, stuck entities, or authority drift.

## Scope

- Harden async handoff, cancellation, and stale-result handling around the current large-battle failure modes.
- Validate correctness under load and capture whether the fixed async path still provides measurable value.
- Avoid bundling LOD policy or optional navigation-model experiments into the phase.

## Planned Execution Slices

- Plan 16-01: Harden async pathfinding handoff, cancellation, and stale-result handling against the current large-battle failure modes.
- Plan 16-02: Validate async correctness and profile whether the fixed async path still improves large-battle cost.

## Constraints

- Correctness comes before throughput.
- Keep thread-handoff boundaries explicit and reviewable.
- Do not assume optional flow-field work will happen later.

## Evidence

- `.planning/ROADMAP.md`
- `.planning/STATE.md`
- `.planning/VERIFICATION.md`
- `.planning/phases/15-pathfinding-throttling-and-budgeting/15-CONTEXT.md`
