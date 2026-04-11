# Phase 7: Migration-Ready Internal Seams - Context

**Gathered:** 2026-04-07
**Status:** Ready for planning
**Mode:** Auto-generated (infrastructure phase)

<domain>
## Phase Boundary

Create narrower, migration-ready seams around the highest-risk version-sensitive subsystems so future 1.21.1 port work can be done through smaller adapters and boundaries instead of deep edits across Forge-heavy runtime paths.

</domain>

<decisions>
## Implementation Decisions

### the agent's Discretion
- All implementation choices are at the agent's discretion for this phase because it is a pure infrastructure and migration-preparation phase.
- Preserve stabilized outward behavior from earlier phases while extracting narrower seams around networking, persistence, client state, compat, pathfinding, and registration glue.
- Favor narrow adapters and isolating boundaries over broad rewrites.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase Definition
- `.planning/ROADMAP.md` — Phase 7 goal and success criteria for migration-ready seams.
- `.planning/REQUIREMENTS.md` — `MIG-01` and `MIG-02`, which define version-sensitive seam identification and narrower adapter-style change surfaces.

### Prior Decisions
- `.planning/phases/06-full-surface-verification-and-safe-degradation/06-CONTEXT.md` — current verification and deferred-gap framing that seam extraction must preserve.
- `.planning/phases/04-command-and-ai-state-stabilization/04-CONTEXT.md` and `.planning/phases/05-persistence-and-multiplayer-sync-hardening/05-CONTEXT.md` — recent seam extraction preferences and subsystem boundaries already established.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- The codebase now has multiple extracted test seams and support helpers from Phases 3-5 that can guide how new migration-oriented seams should be shaped.
- Phase 6 documented the high-risk surfaces and verification matrix that identify where seam extraction matters most.

### Established Patterns
- Prefer extracting narrow, JVM-testable or boundary-focused helpers over broad subsystem rewrites.
- Preserve observable behavior and verification contracts while reducing deep coupling.

### Integration Points
- `src/main/java/com/talhanation/recruits/network/**`
- `src/main/java/com/talhanation/recruits/world/**`
- `src/main/java/com/talhanation/recruits/client/**`
- `src/main/java/com/talhanation/recruits/compat/**`
- `src/main/java/com/talhanation/recruits/pathfinding/**`
- `src/main/java/com/talhanation/recruits/init/**`
- `src/main/java/com/talhanation/recruits/Main.java`

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

*Phase: 07-migration-ready-internal-seams*
*Context gathered: 2026-04-07*
