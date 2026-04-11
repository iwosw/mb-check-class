# Phase 2: Worker Control & Recovery - Context

**Gathered:** 2026-04-05
**Status:** Ready for planning

<domain>
## Phase Boundary

Make existing worker command flows reliable, add one generic in-game recovery path for stuck workers, and surface clear blocked or idle reasons to the player without expanding Phase 2 into broader profession-loop or UI redesign work.

</domain>

<decisions>
## Implementation Decisions

### Recovery Actions
- Recovery should target a generic recall/reset path that restores control without introducing a broader new system.
- Recovery should be triggered through the existing worker command flow rather than a new command surface.
- If recovery cannot complete immediately, it should fail soft with a visible reason instead of silently doing nothing or forcing an unsafe reset.
- Phase 2 should provide one shared recovery behavior across workers rather than profession-specific recovery logic.

### Command Reliability
- A worker command counts as successful only when the player sees either an immediate worker-side reaction or an explicit failure reason.
- Command authority should stay server-authoritative with explicit player feedback, matching the existing packet-driven mutation pattern.
- Command failures should surface a concrete player-visible reason when known rather than a silent rejection.
- Phase 2 should preserve the current command UI/key flow and harden the behavior underneath it instead of redesigning the command surface.

### Blocked And Idle Reasons
- Blocked and idle reporting should use specific actionable reasons when known, especially around missing storage, missing items/tools, full inventories, or failed progression.
- These reasons should surface first through player-facing chat/system messages, reusing the current communication pattern.
- Reasons should emit on transition into a blocked condition, not every tick.
- Idle and blocked should stay distinct: idle means waiting by design, blocked means a missing requirement or failed progression.

### Scope Boundaries
- Profession logic may be touched only as needed to make commands, recovery, and blocked-state reporting reliable.
- If deeper profession defects are discovered, fix the control/recovery seam now and document the larger loop issue for its later phase.
- UI changes should stay minimal and reuse existing command/chat surfaces rather than adding new status panels or overlays.
- Execution proof should require end-to-end command and recovery smoke checks on at least one worker path, plus targeted regression coverage where practical.

### the agent's Discretion
- Choose which concrete command/recovery path provides the best cross-worker proof target for Phase 2.
- Choose the exact internal representation for blocked vs idle reasons, as long as it supports actionable player-facing feedback and anti-spam behavior.
- Choose the most practical regression seam for control/recovery behavior if one emerges naturally during implementation.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase Definition
- `.planning/ROADMAP.md` §Phase 2: Worker Control & Recovery — Defines the fixed goal and success criteria for command reliability, recovery, and blocked-state feedback.
- `.planning/REQUIREMENTS.md` §WORK-01, `WORK-02`, `WORK-03` — Defines the worker-control and recovery requirements this phase owns.

### Project Constraints
- `.planning/PROJECT.md` — Confirms existing code is the source of truth and that this is recovery work, not speculative redesign.
- `.planning/STATE.md` — Carries forward the active v1 recovery constraints and current phase focus.
- `.planning/phases/01-recovery-baseline/01-CONTEXT.md` — Carries forward the preference to preserve current surfaces and keep each phase tightly scoped.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `src/main/java/com/talhanation/workers/client/gui/WorkerCommandScreen.java`: Existing worker command surface already dispatches worker-related actions through the established command UI flow.
- `src/main/java/com/talhanation/workers/init/ModShortcuts.java`: Existing keyboard shortcut opens the command screen, so command recovery can reuse the current client entry point.
- `src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java`: Shared worker base class is the natural place to anchor generic control/recovery behavior that should apply across professions.
- `src/main/java/com/talhanation/workers/entities/ai/GetNeededItemsFromStorage.java` and `DepositItemsToStorage.java`: Existing goals already emit player-facing failure messages and use anti-spam flags that can inform blocked-reason reporting.

### Established Patterns
- Server-authoritative packet-per-action flows are the norm, so command/recovery fixes should preserve client request plus server mutation behavior.
- Player-facing operational feedback currently uses `sendSystemMessage()` more than dedicated UI components.
- Error handling typically uses guard clauses, retry loops, and soft failure instead of hard crashes or broad exception-driven flow.

### Integration Points
- Worker control changes will likely connect through `WorkerCommandScreen`, the worker entity hierarchy rooted at `AbstractWorkerEntity`, and any relevant network messages or Recruits command hooks.
- Blocked/idle reporting will likely connect inside profession and storage AI goals where the worker already knows why it cannot progress.
- Generic recovery behavior should plug into shared worker state/ownership paths rather than being reimplemented separately per profession.

</code_context>

<specifics>
## Specific Ideas

- Keep the control surface familiar and make it trustworthy rather than adding a new workflow.
- Prefer actionable player feedback over hidden state or debug-only diagnostics.
- Use one cross-worker recovery path first; defer profession-specific nuance unless it is required to make the generic path work.

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

</deferred>
