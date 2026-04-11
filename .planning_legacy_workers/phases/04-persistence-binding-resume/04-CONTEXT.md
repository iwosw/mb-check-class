# Phase 4: Persistence & Binding Resume - Context

**Gathered:** 2026-04-06
**Status:** Ready for planning

<domain>
## Phase Boundary

Make the existing worker-to-area and storage or home-like bindings survive relog, restart, and chunk reload closely enough that workers resume their intended behavior after the world lifecycle interrupts them, without introducing a new assignment system or redesigning the authoring and command surfaces.

</domain>

<decisions>
## Implementation Decisions

### Binding Model
- Phase 4 should persist and restore the bindings already implied by the current codebase rather than introducing new explicit assignment systems.
- Existing worker-area links, last-used storage affinity, and home-like bindings should resume by persistent identifiers where possible.

### Restoration Strategy
- Binding restoration should prefer UUID-based entity resolution over proximity scans when resuming saved relationships.
- Broken resume behavior should be fixed near save/load and entity-lookup seams rather than by changing profession selection heuristics more broadly.

### Scope Boundaries
- Phase 4 should focus on persistence and resume, not deeper storage-loop correctness or profession-loop redesign.
- If a minimal fix is required to keep resumed workers from dropping their assignment immediately, that fix is in scope.
- New explicit assignment features remain deferred to v2.

### Feedback And Failure Handling
- Resume failures should degrade safely: workers should drop invalid references and remain controllable rather than corrupting state or hard-failing.
- Player-facing feedback may remain minimal unless a missing binding would otherwise be silent and confusing.

### Verification
- Prefer lightweight automated coverage around persistence and rebind seams where practical.
- Runtime proof should use one representative reload path that exercises both area assignment and storage or home binding continuity.

### the agent's Discretion
- Choose the smallest persistent identifier set needed to make current bindings reliable across reload.
- Choose the most representative worker path for the reload smoke proof.
- Decide whether resume fixes belong in worker save data, work-area save data, or shared rebind helpers based on the least invasive implementation.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase Definition
- `.planning/ROADMAP.md` §Phase 4: Persistence & Binding Resume — Defines the goal and success criteria for binding continuity across lifecycle events.
- `.planning/REQUIREMENTS.md` §`AREA-05`, `STOR-01` — Defines the persistence and intended binding requirements this phase owns.

### Project Constraints
- `.planning/PROJECT.md` — Confirms recovery-first scope and existing code as source of truth.
- `.planning/STATE.md` — Carries the current project position and v1 constraints.
- `.planning/phases/03-work-area-authoring/03-CONTEXT.md` — Carries forward the preference to preserve existing surfaces and keep mutation logic server-authoritative.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java`: Already persists `farmedItems` and `lastStorage`, making worker save data the natural anchor for storage affinity continuity.
- `src/main/java/com/talhanation/workers/entities/workarea/AbstractWorkAreaEntity.java`: Already persists ownership, dimensions, facing, team, and timing state, making work areas durable enough to support UUID-based rebinding.
- `src/main/java/com/talhanation/workers/entities/ai/AbstractChestGoal.java`: Already prioritizes `lastStorage` when searching storage areas, so restoring this field reliably should improve post-reload continuity with minimal churn.
- `src/main/java/com/talhanation/workers/network/MessageUpdateOwner.java` and the Phase 3 authoring packet changes: Existing UUID-based lookup patterns are now established and should be reused for resume logic.

### Established Patterns
- Current logic tolerates dropped references by nulling missing current areas and retrying selection rather than crashing.
- Binding and authoring state is stored directly on entities, not in an external manager service.
- Proximity scans still exist in profession selection loops, but Phase 4 should prefer direct persisted references where the relationship already exists.

### Integration Points
- Worker save/load and per-profession current-area fields are likely the main resume seam for `AREA-05`.
- `lastStorage`, storage-area UUIDs, and storage search prioritization are likely the main seam for `STOR-01` continuity.
- Any home-like resume behavior may connect through existing recruit/home fields inherited from the upstream dependency rather than a new Workers-owned binding surface.

</code_context>

<specifics>
## Specific Ideas

- Favor restoring known relationships over re-discovering them from scratch after every reload.
- If a saved UUID no longer resolves, clear it cleanly and fall back to normal selection rather than leaving a half-broken binding.
- Keep the phase centered on continuity of already-authored state, not richer assignment controls.

</specifics>

<deferred>
## Deferred Ideas

- Explicit worker-to-specific-area assignment stays deferred to v2.

</deferred>
