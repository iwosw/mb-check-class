# Phase 22: Citizen Role Unification - Context

**Gathered:** 2026-04-13
**Status:** Ready for planning

<domain>
## Phase Boundary

Unify the recruit and worker runtime around one shared `Citizen` model with citizen-owned state and role/job-driven behavior, while keeping the migration incremental, compatibility-safe, and validated through live recruit and worker paths.

</domain>

<decisions>
## Implementation Decisions

### Rollout Shape
- Start with a shared `Citizen` core plus adapters instead of a one-shot replacement of every recruit and worker entity.
- Make shared citizen-owned state and controllers the first stable seam, with current recruit and worker classes reduced toward wrappers.
- Keep current entity ids and external save identities stable during Phase 22; change internals before touching runtime-facing ids.
- Do not retire legacy concrete classes in this phase; shrink them into thin compatibility wrappers first.

### Behavior Ownership
- Put role-specific behavior in role/job controllers attached to the shared citizen runtime instead of creating a new deep subclass tree.
- Move ownership, team and authority state, inventory access, persistence hooks, navigation handoff, and common runtime flags onto the shared core.
- Keep combat AI, labor AI, profession-specific work-area logic, and troop/job presentation role-specific for now.
- Treat the recruit-owned pathfinding stack as canonical and adapt workers onto it.

### Compatibility Boundary
- Read legacy recruit and worker persistence into citizen-owned data, but keep compatibility-safe write paths until wrappers are proven.
- Preserve current packet and screen entrypoints and redirect them through citizen-aware adapters instead of redesigning network and UI in the same phase.
- Allow internal API churn, but preserve the live mod id, registry identity, and the existing narrow compatibility seams.
- Keep existing recruit and worker GameTests as the main safety net and add focused citizen-seam regression coverage.

### First Live Conversion Scope
- Start live adoption with shared non-visual infrastructure plus one controlled recruit path and one controlled worker path, not every profession or troop at once.
- End Phase 22 with a working citizen core that both a recruit flow and a worker flow use in limited live paths while wrappers still exist.
- Slice execution into small plans around the citizen core, role controller seam, first live recruit adoption, and first live worker adoption with validation.
- Explicitly defer governor systems, logistics and economy expansion, broad UI redesign, and full class retirement out of Phase 22.

### the agent's Discretion
None. The rollout, ownership boundaries, compatibility stance, and first-live scope were all accepted during smart discuss.

</decisions>

<code_context>
## Existing Code Insights

### Reusable Assets
- The repository already favors shared seams for cross-domain behavior, including `src/main/java/com/talhanation/bannermod/authority/BannerModAuthorityRules.java`, `src/main/java/com/talhanation/bannermod/logistics/BannerModSupplyStatus.java`, and `src/main/java/com/talhanation/bannermod/settlement/BannerModSettlementBinding.java`.
- Recruit-side world managers and persistence vocabulary already live outside entities in `recruits/src/main/java/com/talhanation/recruits/world/**`, which is a useful model for moving state out of concrete entity subclasses.
- Worker automation is already expressed as worker-plus-work-area pairings and goal-driven execution under `workers/src/main/java/com/talhanation/workers/entities/**` and `workers/src/main/java/com/talhanation/workers/entities/ai/**`.

### Established Patterns
- The merged runtime is recruit-led, with workers absorbed through subsystem composition rather than parallel runtime ownership.
- Brownfield changes favor compatibility wrappers, guard-clause style handlers, and incremental seam extraction over one-shot rewrites.
- Existing roadmap decisions already treat the recruit-owned pathfinding stack as the canonical navigation base for future worker/logistics work.

### Integration Points
- Entity/runtime convergence will have to touch recruit entity families under `recruits/src/main/java/com/talhanation/recruits/entities/**` and worker entity families under `workers/src/main/java/com/talhanation/workers/entities/**`.
- Shared persistence and ownership logic already concentrates in recruit world managers and shared bannermod seam classes, which are the safest anchor points for citizen-owned data.
- Validation should extend the root GameTest pipeline under `src/gametest/java/com/talhanation/bannermod/**` rather than inventing a separate verification path.

</code_context>

<specifics>
## Specific Ideas

Use Phase 22 to replace inheritance sprawl with one citizen-owned runtime seam, but keep the first adoption narrow enough that one recruit path and one worker path can prove the model before wider conversion.

</specifics>

<deferred>
## Deferred Ideas

- Governor gameplay and settlement governance systems
- Logistics, courier, treasury, taxes, upkeep, trade, and other economy expansions
- Broad UI redesign tied to the future unified citizen model
- Full retirement of recruit and worker wrapper classes before the citizen seam is proven in live runtime paths

</deferred>
