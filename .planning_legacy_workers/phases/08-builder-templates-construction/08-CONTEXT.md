# Phase 8: Builder Templates & Construction - Context

**Gathered:** 2026-04-06
**Status:** Ready for planning

<domain>
## Phase Boundary

Make the existing build-area and template mechanics reliable enough that players can start a builder job, watch the builder place the intended structure progressively, and have that construction loop survive normal interruptions closely enough to resume, without redesigning the current build UI or template workflow.

</domain>

<decisions>
## Implementation Decisions

### Builder Surface
- Phase 8 should preserve the existing build-area and template authoring flow.
- The phase should harden progressive construction behavior underneath the current UI rather than redesigning builder UX.

### Construction Loop Focus
- Phase 8 should focus on the actual builder placement loop, required-material handoff, and progress continuity.
- Template scanning/loading and build-area setup should only be touched where they directly block the intended construction loop.

### Resume Behavior
- Builder progress should survive normal interruptions closely enough to resume the current construction loop.
- If a minimal persistence or busy-state fix is needed to keep a partially completed structure resumable, it is in scope.

### Verification
- Prefer targeted automated coverage for build progress, material gating, and resume seams.
- Runtime proof should use one representative structure path rather than broad template-library coverage.

### the agent's Discretion
- Choose the smallest build-progress/resume seam(s) that best stabilize the builder loop without broad template-system churn.
- Choose the most representative structure/template path already supported by the current codebase.
- Decide whether fixes belong in `BuilderWorkGoal`, `BuildArea`, or structure-loading helpers based on the least invasive correct implementation.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase Definition
- `.planning/ROADMAP.md` §Phase 8: Builder Templates & Construction — Defines the goal and success criteria for build-area/template construction.
- `.planning/REQUIREMENTS.md` §`PROF-03` — Defines the builder profession requirement this phase owns.

### Project Constraints
- `.planning/PROJECT.md` — Confirms recovery-first scope and existing code as source of truth.
- `.planning/STATE.md` — Carries the current project position and v1 constraints.
- `.planning/phases/07-mining-merchant-flows/07-CONTEXT.md` — Carries forward the continuity-first approach for profession loops built on prior storage/recovery foundations.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `src/main/java/com/talhanation/workers/entities/ai/BuilderWorkGoal.java`: Existing builder loop already covers work-area selection, break/place passes, required-material calculation, multi-block handling, and end-of-build entity spawning.
- `src/main/java/com/talhanation/workers/entities/workarea/BuildArea.java`: Existing build-area entity already stores scanned blocks, required state lookups, done/progress flags, and placement helpers.
- `src/main/java/com/talhanation/workers/client/gui/BuildAreaScreen.java`: Existing UI already exposes the build/template flow and should be preserved.
- `src/main/java/com/talhanation/workers/world/StructureManager.java` and related build block classes: Existing template scan/load machinery should be reused rather than replaced.

### Established Patterns
- The builder is implemented as a long imperative state machine similar to other professions.
- Earlier phases already established continuity helpers for keeping a worker on the same assigned area across temporary interruptions; builder should follow the same spirit where appropriate.
- Construction logic mixes world-state validation, material checks, and placement progress directly in the goal, so the smallest correct fixes are likely near that loop rather than in UI code.

### Integration Points
- Builder stabilization will center on `BuilderWorkGoal`, `BuilderEntity`, `BuildArea`, and template/material helpers.
- Resume behavior will likely connect through build-area saved state plus worker current-area continuity.
- Runtime proof can reuse earlier work-area, storage, and control surfaces instead of introducing a new builder control flow.

</code_context>

<specifics>
## Specific Ideas

- Prioritize making a partial build continue correctly over polishing every template edge case equally.
- Keep material gating explicit and recoverable rather than letting the builder stall silently mid-structure.
- Use one representative structure as proof of end-to-end builder viability instead of attempting exhaustive template coverage in this phase.

</specifics>

<deferred>
## Deferred Ideas

- Broader template-library coverage and richer builder UX remain out of scope.

</deferred>
