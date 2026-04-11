# Phase 8: 1.21.1 Port Prep Inventory - Context

**Gathered:** 2026-04-07
**Status:** Ready for planning
**Mode:** Auto-generated (infrastructure phase)

<domain>
## Phase Boundary

Produce the concrete migration-prep checklist and code inventory that turn the future 1.21.1 port into a bounded, well-scoped follow-up effort grounded in the seams and stabilization work completed in earlier phases.

</domain>

<decisions>
## Implementation Decisions

### the agent's Discretion
- All implementation choices are at the agent's discretion for this phase because it is a pure migration-preparation and documentation phase.
- Build directly on the seam inventory and stabilization outputs from earlier phases instead of re-exploring the repo from scratch.
- Favor concrete, reviewable checklists and code inventories over vague migration guidance.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase Definition
- `.planning/ROADMAP.md` — Phase 8 goal and success criteria for the final migration-prep inventory.
- `.planning/REQUIREMENTS.md` — `MIG-03`, which defines the requirement for a concrete checklist and code inventory that bounds the future 1.21.1 port.

### Prior Decisions
- `.planning/phases/07-migration-ready-internal-seams/07-CONTEXT.md` — migration-ready seam strategy that this inventory must summarize and operationalize.
- `.planning/phases/06-full-surface-verification-and-safe-degradation/06-CONTEXT.md` — current verification/deferred-gap framing that the future port plan must account for.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- Phase 7 already produced seam inventory and contracts that should anchor the final migration-prep checklist.
- Earlier phases now provide verified coverage areas, deferred-gap ledgers, and narrowed subsystem seams that define the real migration surface.

### Established Patterns
- This milestone prefers explicit, reviewable artifacts over implicit tribal knowledge.
- Migration-prep work should preserve stabilized behavior and use narrowed seams instead of reopening broad subsystem exploration.

### Integration Points
- `.planning/phases/07-migration-ready-internal-seams/**`
- `.planning/phases/06-full-surface-verification-and-safe-degradation/**`
- `src/main/java/com/talhanation/recruits/network/**`
- `src/main/java/com/talhanation/recruits/world/**`
- `src/main/java/com/talhanation/recruits/client/**`
- `src/main/java/com/talhanation/recruits/compat/**`
- `src/main/java/com/talhanation/recruits/pathfinding/**`
- `src/main/java/com/talhanation/recruits/init/**`

</code_context>

<specifics>
## Specific Ideas

No specific requirements — infrastructure phase.

</specifics>

<deferred>
## Deferred Ideas

None — infrastructure phase.

</deferred>

---

*Phase: 08-1-21-1-port-prep-inventory*
*Context gathered: 2026-04-07*
