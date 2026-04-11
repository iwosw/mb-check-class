# Phase 13 Context

## Phase

- Number: 13
- Name: Path Reuse For Cohesion Movement

## Goal

- Reduce duplicate navigation work by reusing compatible paths for nearby or formation-cohesive actors where safe.

## Why This Phase Exists

- Once pathfinding requests flow through one control seam, the next clear win is to stop recomputing equivalent paths for actors sharing movement intent.
- The proposal puts path reuse ahead of target-selection and throttling work, which keeps this slice focused on duplicate path cost rather than broader AI policy.
- Reuse needs explicit hit-rate and invalidation evidence or it can silently trade CPU cost for stuck-movement bugs.

## Scope

- Add the narrowest safe path-reuse seam for compatible group movement.
- Track reuse eligibility, hit rate, and invalidation behavior as part of validation.
- Avoid bundling target acquisition or async-path concurrency changes into this phase.

## Planned Execution Slices

- Plan 13-01: Implement the smallest safe path-reuse seam for nearby or formation-cohesive movement.
- Plan 13-02: Validate path correctness and profile reuse hit rate, invalidation churn, and large-battle impact.

## Constraints

- Reuse must stay correctness-first; stale or incompatible paths should be discarded rather than forced.
- Keep the feature observable in profiling output.
- Do not assume flow fields or LOD are available yet.

## Evidence

- `.planning/ROADMAP.md`
- `.planning/STATE.md`
- `.planning/VERIFICATION.md`
- `.planning/phases/12-global-pathfinding-control/12-CONTEXT.md`
