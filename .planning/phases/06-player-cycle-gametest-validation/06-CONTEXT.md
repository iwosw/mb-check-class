# Phase 06 Context

## Phase

- Number: 06
- Name: Player-Cycle GameTest Validation

## Goal

- Validate the integrated BannerMod gameplay loop as one player-facing system through root GameTests, not only through isolated subsystem seams.

## Why This Phase Exists

- The merged runtime is now stable and the root GameTest layer is green.
- Current coverage proves runtime coexistence, shared ownership behavior, work-area authorization, and one supply transition.
- What is still missing is a player-perspective gameplay cycle: own units, establish settlement labor, feed military upkeep, preserve authority rules, and keep the loop coherent under one runtime.

## Scope

- Add root GameTests that validate the integrated gameplay cycle in small slices.
- Prefer additive runtime validation over production-code rewrites unless a test reveals a real bug.
- Keep all validation rooted in `src/gametest/java/com/talhanation/bannermod/` when feasible.

## Planned Validation Slices

- Plan 06-01: Player can own recruit and worker in the same BannerMod runtime and shared ownership boundaries behave correctly.
- Plan 06-02: Owned worker can bind to settlement infrastructure and perform valid work-area participation under the same authority model.
- Plan 06-03: Settlement-side supply can satisfy recruit upkeep/readiness in a live runtime transition.
- Plan 06-04: Full BannerMod player-cycle GameTest orchestrates the stitched loop with authority and recovery expectations.

## Constraints

- Preserve current gameplay behavior unless a failing GameTest exposes a real defect.
- Keep tests deterministic and low-risk.
- Avoid deep persistence rewrites during validation-first slices.
- Use the already-green `verifyGameTestStage` baseline as the gate.

## Evidence

- `.planning/CODEBASE.md`
- `.planning/VERIFICATION.md`
- `.planning/codebase/INTEGRATED_SYSTEM_ARCHITECTURE.md`
- `src/gametest/java/com/talhanation/bannermod/IntegratedRuntimeGameTests.java`
