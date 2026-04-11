# Phase 18 Context

## Phase

- Number: 18
- Name: Optional Flow-Field Navigation Evaluation

## Goal

- Decide whether flow-field navigation is worth carrying as an optional large-group movement optimization, based on focused prototype evidence instead of assumption.

## Why This Phase Exists

- The proposal explicitly marks flow-field navigation as optional rather than mandatory.
- By waiting until after the earlier pathfinding, targeting, async, and LOD slices, BannerMod can evaluate flow fields against a much healthier baseline instead of using them as a first resort.
- Optional research still needs a tracked backlog slot so it does not quietly become an unbounded rewrite.

## Scope

- Define the narrow scenarios where a guarded flow-field prototype is allowed.
- Prototype or spike only enough to compare the idea against the existing optimized stack.
- End the phase with an evidence-backed keep-or-drop decision.

## Planned Execution Slices

- Plan 18-01: Define the narrow movement scenarios where an optional flow-field prototype is allowed and how it will be isolated.
- Plan 18-02: Build the guarded prototype or spike and benchmark it against the existing pathfinding stack.

## Constraints

- Treat the work as optional and benchmark-gated.
- Do not destabilize the primary navigation stack just to support the prototype.
- Keep any experiment easy to disable or abandon.

## Evidence

- `.planning/ROADMAP.md`
- `.planning/STATE.md`
- `.planning/VERIFICATION.md`
- `.planning/phases/17-ai-lod-and-tick-shedding/17-CONTEXT.md`
