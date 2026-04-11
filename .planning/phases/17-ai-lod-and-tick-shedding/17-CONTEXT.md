# Phase 17 Context

## Phase

- Number: 17
- Name: AI LOD And Tick Shedding

## Goal

- Reduce large-battle AI cost with explicit level-of-detail and tick-shedding rules for lower-priority actors or decisions.

## Why This Phase Exists

- Once pathfinding and target-selection hot paths are bounded, BannerMod can safely decide which AI work is worth running at full fidelity under scale.
- The proposal places AI LOD after async fixes, which keeps core navigation correctness ahead of intentional behavior degradation.
- LOD changes are easy to over-apply, so the roadmap needs explicit validation and profiling expectations.

## Scope

- Add the smallest explicit LOD rules for low-priority AI loops under large-battle load.
- Measure the tick-cost tradeoff and verify that nearby or high-priority combat behavior remains readable.
- Keep the phase separate from optional flow-field research.

## Planned Execution Slices

- Plan 17-01: Add the smallest explicit AI LOD rules for low-priority actors and decision loops in large battles.
- Plan 17-02: Validate gameplay behavior and profile tick-cost reduction from the new LOD layer.

## Constraints

- Preserve authority and core combat outcomes.
- Make LOD policy explicit enough to tune later.
- Avoid hidden gameplay nerfs that only exist for performance reasons.

## Evidence

- `.planning/ROADMAP.md`
- `.planning/STATE.md`
- `.planning/VERIFICATION.md`
- `.planning/phases/16-async-pathfinding-reliability-fixes/16-CONTEXT.md`
