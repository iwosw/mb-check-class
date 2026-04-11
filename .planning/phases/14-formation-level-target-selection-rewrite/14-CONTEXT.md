# Phase 14 Context

## Phase

- Number: 14
- Name: Formation-Level Target Selection Rewrite

## Goal

- Replace the highest-cost per-entity target acquisition path with formation-level or squad-level selection and assignment.

## Why This Phase Exists

- Large-battle AI cost is not only pathfinding; repeated target acquisition across many actors also scales poorly.
- The proposal places the target-selection rewrite after path reuse and before pathfinding throttling, keeping movement optimization and combat-intent changes separate but ordered.
- A formation-aware target seam can lower search cost without requiring a full combat AI rewrite.

## Scope

- Rewrite target selection at the formation or squad level where group behavior currently duplicates search work.
- Keep the phase limited to target discovery and assignment rather than navigation or damage rules.
- Include correctness checks for retargeting, target loss, and focus-fire behavior.

## Planned Execution Slices

- Plan 14-01: Replace the highest-cost per-entity target acquisition path with a formation-level selection and assignment seam.
- Plan 14-02: Validate combat behavior and profile target-selection cost after the formation rewrite.

## Constraints

- Preserve readable combat behavior for players.
- Avoid bundling path budgets, async safety, or LOD policy into the rewrite.
- Keep the new formation contract explicit enough for later validation.

## Evidence

- `.planning/ROADMAP.md`
- `.planning/STATE.md`
- `.planning/VERIFICATION.md`
- `.planning/phases/13-path-reuse-for-cohesion-movement/13-CONTEXT.md`
