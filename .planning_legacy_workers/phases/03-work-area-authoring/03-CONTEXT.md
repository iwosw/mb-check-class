# Phase 3: Work Area Authoring - Context

**Gathered:** 2026-04-06
**Status:** Ready for planning

<domain>
## Phase Boundary

Make the existing work-area creation, editing, and inspection flows reliable enough that players can create, move, rotate, rename, inspect, and trust work areas through the current in-game command and screen interactions, without redesigning the UI or pulling persistence-resume work from later phases.

</domain>

<decisions>
## Implementation Decisions

### Authoring Surface
- Phase 3 should preserve the existing authoring flow: create from the current worker command screen, then inspect and edit through the existing work-area screens.
- The phase should harden behavior under the current surfaces rather than redesign the command or work-area UI.

### Authority And Mutation
- Create, edit, rotate, rename, owner change, and destroy behavior should remain server-authoritative.
- Client behavior may stay optimistic only where the existing screens already preview movement or rotation, but the server result remains canonical.

### Validation And Feedback
- Overlap and authorization validation should stay close to the server-side mutation points.
- Invalid edits and unauthorized access should fail explicitly rather than silently corrupting area state.
- Player-facing feedback should reuse the current screen/chat patterns instead of introducing new status panels.

### Scope Boundaries
- Phase 3 should cover creation, editing, validation, and inspection only.
- Persistence/resume work stays in Phase 4 unless a minimal fix is required to prevent immediate authoring corruption.
- Deeper profession behavior stays out of scope unless it directly blocks trusting the authored area.

### Verification
- Prefer lightweight automated coverage around authoring rules and packet-side validation seams where practical.
- Runtime proof should stay focused on one or two representative authoring flows rather than every area type.

### the agent's Discretion
- Choose the most representative area type(s) for proof if one flow naturally exercises shared authoring logic.
- Choose the smallest validation seam(s) to test automatically, especially for overlap, authorization, or UUID/entity lookup behavior.
- Decide where explicit failure feedback belongs, as long as it stays within current screen/chat surfaces.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase Definition
- `.planning/ROADMAP.md` §Phase 3: Work Area Authoring — Defines the goal and success criteria for creation, editing, validation, and inspection.
- `.planning/REQUIREMENTS.md` §`AREA-01` through `AREA-04` — Defines the work-area authoring requirements this phase owns.

### Project Constraints
- `.planning/PROJECT.md` — Confirms brownfield recovery scope and existing code as source of truth.
- `.planning/STATE.md` — Captures the active phase position and ongoing v1 constraints.
- `.planning/phases/02-worker-control-recovery/02-CONTEXT.md` — Carries forward the preference to preserve current surfaces and favor minimal hardening over redesign.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `src/main/java/com/talhanation/workers/client/gui/WorkerCommandScreen.java`: Existing entry point for creating new work areas through the in-game command flow.
- `src/main/java/com/talhanation/workers/network/MessageAddWorkArea.java`: Current server-side creation path already selects area type, applies defaults, sets ownership/team metadata, and rejects overlaps.
- `src/main/java/com/talhanation/workers/entities/workarea/AbstractWorkAreaEntity.java`: Shared area entity base already owns authorization checks, area math, overlap detection, screen dispatch, and area serialization fields.
- `src/main/java/com/talhanation/workers/client/gui/WorkAreaScreen.java`: Shared edit screen already supports movement, rotation, destroy, and owner reassignment flows.
- `src/main/java/com/talhanation/workers/network/MessageUpdateWorkArea.java` and `MessageRotateWorkArea.java`: Existing packet-driven edit paths are the main server-authoritative mutation seams.
- `src/main/java/com/talhanation/workers/network/MessageToClientOpenWorkAreaScreen.java`: Existing screen-opening flow depends on resolving the authored area reliably on the client.

### Established Patterns
- Authoring uses packet-per-action mutations with server-side guards and client-side previews.
- Validation is embedded near mutation points rather than centralized in a separate service layer.
- Unauthorized or invalid operations currently tend to no-op; Phase 3 should preserve that defensive style while making failure outcomes clearer.

### Integration Points
- Creation reliability will likely center on `WorkerCommandScreen` plus `MessageAddWorkArea`.
- Editing reliability will likely center on `WorkAreaScreen`, `MessageUpdateWorkArea`, `MessageRotateWorkArea`, and the shared area math in `AbstractWorkAreaEntity`.
- Inspection and trustworthiness will likely involve `AbstractWorkAreaEntity.interact()`, `MessageToClientOpenWorkAreaScreen`, and the existing screen/box-visualization behavior.

</code_context>

<specifics>
## Specific Ideas

- If one area type exercises the shared authoring path well, use it as the main proof target before fixing type-specific edge cases.
- Prefer explicit rejection of bad edits over attempting broad silent correction that can hide state drift.
- Fix lookup and validation seams close to packets/entities first before changing presentation code.

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

</deferred>
