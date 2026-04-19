---
phase: 18-optional-flow-field-navigation-evaluation
decision: drop
decided: 2026-04-12
reconstructed: 2026-04-19
---

# Phase 18 Decision: Drop Optional Flow-Field Navigation

> Retroactive reconstruction 2026-04-19: written after planning-dir audit surfaced that the Phase 18 directory only carried `18-CONTEXT.md` and no published verdict, despite ROADMAP and STATE already recording the `drop` outcome.

## Verdict

**Drop.** Flow-field navigation is rejected as a carried optional large-group movement layer. The merged BannerMod runtime continues to rely on the existing `AsyncPathNavigation` / `AsyncPathProcessor` / `PathProcessingRuntime` stack hardened in Phases 12–16, plus the `RecruitAiLodPolicy` tick-shedding layer from Phase 17.

## Evidence

- ROADMAP narrative for Phase 18 records the prototype outcome as **51 attempts, zero hits** on the dedicated `same_destination_flow_field_lane` benchmark. With no measurable hit rate on an in-scope movement scenario, the prototype cannot justify the complexity or maintenance cost of a second navigation model.
- A scan of the active runtime tree under `src/main/java/com/talhanation/bannermod/ai/` finds no flow-field navigation class (no `**/flow*` sources): the prototype was never merged into the active stack, which is consistent with the `drop` outcome.
- STATE.md already carries the decision: *"all 51 prototype attempts fell back with zero hits, so the final Phase 18 outcome is `drop`"* and *"Keep the flow-field experiment documented as evaluated and rejected; continue Phase 19 on the existing pathfinding stack rather than expanding the optional prototype."*

## Scope Of The Rejection

- The `drop` verdict applies to adopting flow-field navigation as an optional, benchmark-gated large-group movement layer in the current roadmap window.
- It does **not** forbid later re-evaluation if future scenario counters or residual Phase 19 hotspots justify re-opening the question against a different baseline.
- It does **not** retroactively block any existing pathfinding work: the controller seam (Phase 12), reuse (Phase 13), budget/throttle (Phase 15), async reliability (Phase 16), and LOD (Phase 17) remain in place.

## Active Runtime Surface

- Async pathfinding continues to flow through `src/main/java/com/talhanation/bannermod/ai/pathfinding/AsyncPathNavigation.java` → `AsyncPathProcessor.java` → `PathProcessingRuntime.java`, all routed through `GlobalPathfindingController` for request accounting, reuse, and per-tick budget deferral.
- Recruit target-search tick cadence continues to flow through `RecruitAiLodPolicy` (Phase 17).

## Follow-Up

- Phase 19 closing validation proceeds on the existing stack per the compact performance roadmap.
- Any future flow-field spike must re-open this decision explicitly, capture side-by-side evidence against the then-current baseline, and cite a measured motivation rather than anticipated scale.

---
*Phase: 18-optional-flow-field-navigation-evaluation*
*Decision recorded: 2026-04-12 (retroactive reconstruction 2026-04-19)*
