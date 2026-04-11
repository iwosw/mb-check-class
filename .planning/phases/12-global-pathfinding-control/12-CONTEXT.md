# Phase 12 Context

## Phase

- Number: 12
- Name: Global Pathfinding Control

## Goal

- Introduce one explicit BannerMod control seam for high-volume pathfinding requests so later optimization work can act on shared budgets and queues instead of per-entity ad hoc calls.

## Why This Phase Exists

- Large-battle lag cannot be managed well while every actor behaves like an independent pathfinding caller.
- Path reuse, throttling, and async safety all depend on a stable global control boundary first.
- The proposal orders this seam ahead of the rest of the navigation optimization work, so the roadmap must preserve that dependency.

## Scope

- Add one shared pathfinding control boundary and route the current highest-volume callers through it.
- Keep behavior changes minimal beyond the new control point itself.
- Include correctness validation and before/after profiling as part of the phase.

## Planned Execution Slices

- Plan 12-01: Add the shared global pathfinding control seam and route the current high-volume callers through it.
- Plan 12-02: Validate correctness and capture before/after profiling for the new global control boundary.

## Constraints

- Preserve small-scale navigation behavior unless profiling proves a guardrail is required.
- Prefer one shared seam over many local feature flags.
- Do not mix in formation targeting or LOD policy yet.

## Evidence

- `.planning/ROADMAP.md`
- `.planning/STATE.md`
- `.planning/VERIFICATION.md`
- `.planning/phases/11-large-battle-ai-profiling-baseline/11-CONTEXT.md`
