# Phase 2: Runtime Unification Design - Context

**Gathered:** 2026-04-11
**Status:** Ready for planning

<domain>
## Phase Boundary

Lock the intended merged runtime identity and migration policy for BannerMod: public metadata/branding, the end-state namespace for Workers-owned assets and translations, the forward-migration boundary for legacy Workers-era data/state, and the ownership model for config in the unified runtime. This phase clarifies the target design; it does not add new gameplay capability.

</domain>

<decisions>
## Implementation Decisions

### Public identity
- **D-01:** The merged mod should present itself publicly as `BannerMod` first. Downstream docs, planning, and metadata cleanup should treat `bannermod` as the intended release identity.
- **D-02:** Legacy Recruits-branded metadata that still exists in active files is transitional debt, not the target end-state.

### Workers namespace end-state
- **D-03:** The intended end-state is full migration of Workers assets and translation ownership into the active `bannermod` namespace.
- **D-04:** The current mixed state (`assets/workers/**` plus selective `assets/bannermod/**` mirrors) should be treated as transitional migration state, not as the long-term namespace policy.

### Legacy migration contract
- **D-05:** Phase 2 should define forward migration for the merged mod's own legacy Workers-era data/state into the unified `bannermod` runtime.
- **D-06:** The project does not guarantee compatibility with old standalone mods or outside integrations that depend on a separate live `workers` mod identity.
- **D-07:** The currently implemented narrow compatibility seam (registry remaps plus known structure/build NBT migration) is not the final target boundary; downstream agents should treat broader merged-runtime legacy-state migration as required design work rather than optional polish.

### Config ownership
- **D-08:** The target config surface is BannerMod-owned, not a permanently separate Workers config surface.
- **D-09:** Existing separate Workers config registration may remain temporarily as a migration seam, but downstream plans should converge toward BannerMod-facing config naming, docs, and behavior.

### the agent's Discretion
- Exact migration sequencing across metadata, assets, configs, and save-state paths.
- Whether each surface uses a dual-read transition seam or a direct one-step migration, as long as the decisions above remain true.

</decisions>

<specifics>
## Specific Ideas

- "BannerMod-first" is the intended release-facing presentation, not a temporary alias.
- "Aggressive full migration" applies to the merged mod's own legacy state, not to preserving a second live standalone Workers mod contract.
- Full `bannermod` asset ownership is the goal even if preserved `workers` resources remain during transition.

</specifics>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Active project context
- `.planning/PROJECT.md` — Merge stance, accepted foundational decisions, and active workspace constraints.
- `.planning/REQUIREMENTS.md` — What is already complete for runtime identity/namespace work and what compatibility guarantees remain unresolved.
- `.planning/ROADMAP.md` — Phase 2 boundary and adjacent phase sequencing.
- `.planning/STATE.md` — Current active status and latest planning/session checkpoint.

### Prior locked context
- `.planning/phases/01-workspace-bootstrap/01-CONTEXT.md` — Root truth policy, root planning authority, and verification baseline that Phase 2 must carry forward.

### Merge truth and readiness
- `MERGE_NOTES.md` — Current merge decisions, active-doc/code conflicts, namespace notes, and open runtime-unification questions.
- `.planning/CODEBASE.md` — Source-of-truth paths and open risks around remaining `workers:*` compatibility surfaces.
- `.planning/VERIFICATION.md` — Current validation baseline and what compatibility coverage already exists.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java`: central helper for active mod id, merged asset namespace, legacy-id migration, and worker packet offset.
- `workers/src/main/java/com/talhanation/workers/WorkersLegacyMappings.java`: focused Forge missing-mapping bridge for legacy `workers:*` registry ids.
- `workers/src/main/java/com/talhanation/workers/init/WorkersLifecycleRegistrar.java`: current Workers config-registration seam and runtime listener wiring.
- `recruits/src/main/resources/META-INF/mods.toml`: active shipped metadata source for the unified runtime, even though its release-facing copy is still legacy-skewed.

### Established Patterns
- The merged runtime has one active Forge bootstrap in `recruits/src/main/java/com/talhanation/recruits/Main.java`; Workers is integrated as a subsystem, not as a peer mod.
- Compatibility work currently uses targeted migration seams instead of broad catch-all rewriting.
- Resource migration is already selective: active GUI/structure/lang ownership has partly moved to `bannermod`, while preserved `workers` resources still exist as migration input.

### Integration Points
- Release identity changes connect through `recruits/src/main/resources/META-INF/mods.toml` and root build artifact settings in `build.gradle`.
- Asset and translation migration connect through `build.gradle` resource processing, `WorkersRuntime`, and the `recruits/src/main/resources/assets/bannermod/` plus `workers/src/main/resources/assets/workers/` trees.
- Legacy-state migration connects through `WorkersRuntime`, `WorkersLegacyMappings`, structure/NBT loaders such as `workers/src/main/java/com/talhanation/workers/world/StructureManager.java`, and config registration in `WorkersLifecycleRegistrar`.

</code_context>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

</deferred>

---

*Phase: 02-runtime-unification-design*
*Context gathered: 2026-04-11*
