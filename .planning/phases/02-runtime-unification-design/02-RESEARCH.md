# Phase 02: Runtime Unification Design - Research

**Date:** 2026-04-11
**Status:** Complete

## Question

What do we need to know to plan the runtime-unification design phase well so downstream work can implement BannerMod-first identity, full `bannermod` namespace ownership, truthful legacy-state migration boundaries, and BannerMod-owned config behavior without reopening a second live Workers mod?

## Sources Read

- `.planning/phases/02-runtime-unification-design/02-CONTEXT.md`
- `.planning/PROJECT.md`
- `.planning/REQUIREMENTS.md`
- `.planning/ROADMAP.md`
- `.planning/STATE.md`
- `.planning/CODEBASE.md`
- `.planning/VERIFICATION.md`
- `.planning/codebase/STACK.md`
- `.planning/codebase/ARCHITECTURE.md`
- `.planning/codebase/CONVENTIONS.md`
- `MERGE_NOTES.md`
- `build.gradle`
- `recruits/src/main/resources/META-INF/mods.toml`
- `recruits/src/main/java/com/talhanation/recruits/init/ModLifecycleRegistrar.java`
- `recruits/src/main/java/com/talhanation/recruits/config/RecruitsClientConfig.java`
- `recruits/src/main/java/com/talhanation/recruits/config/RecruitsServerConfig.java`
- `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java`
- `workers/src/main/java/com/talhanation/workers/WorkersLegacyMappings.java`
- `workers/src/main/java/com/talhanation/workers/init/WorkersLifecycleRegistrar.java`
- `workers/src/main/java/com/talhanation/workers/config/WorkersServerConfig.java`
- `workers/src/main/java/com/talhanation/workers/world/StructureManager.java`
- `.planning/phases/01-workspace-bootstrap/01-workspace-bootstrap-01-SUMMARY.md`
- `.planning/phases/01-workspace-bootstrap/01-workspace-bootstrap-02-SUMMARY.md`

## Findings

### 1. Public identity is already technically `bannermod`, but the release-facing story is still mixed

- `mods.toml` already ships `modId="bannermod"` and `displayName="BannerMod"`.
- `build.gradle` still uses historical technical coordinates like `group = 'com.talhanation.recruits'` while the artifact base name is `bannermod-1.20.1`.
- `mods.toml` still points to Recruits-branded update/display URLs and keeps a Recruits-focused description.

**Planning implication:** Phase 2 should treat metadata cleanup as BannerMod-first release identity work per D-01 and D-02, while leaving package/group history alone unless the task explicitly needs to change technical coordinates.

### 2. Namespace migration is already halfway done through narrow, code-backed seams

- `WorkersRuntime.MOD_ID` resolves to `Main.MOD_ID`, so active runtime ids already publish under `bannermod`.
- `build.gradle` mirrors worker GUI/models/structures into `assets/bannermod/**` during `processResources`.
- `WorkersRuntime.ACTIVE_ASSET_NAMESPACE = "bannermod"` and helper methods like `mergedGuiTexture()` / `mergedStructureRoot()` already assume BannerMod-owned runtime assets.

**Planning implication:** The plan should not debate the destination namespace. It should codify the end-state as full `bannermod` ownership per D-03 and D-04, with `workers/**` treated only as preserved migration input during transition.

### 3. Legacy compatibility coverage is intentionally narrow today and must be documented honestly

- `WorkersLegacyMappings` remaps missing legacy `workers:*` registry ids for entity, item, block, POI, and profession registries.
- `WorkersRuntime.migrateStructureNbt(...)` and `StructureManager` migrate known structure/build NBT fields (`block`, `state.Name`, `entity_type`).
- No broader catch-all migration exists for arbitrary NBT, custom payloads, or unknown third-party integrations.

**Planning implication:** Phase 2 needs an explicit compatibility contract that says the merged runtime migrates its own known Workers-era state forward per D-05 and D-07, but does not promise a second live standalone `workers` mod contract per D-06.

### 4. Config ownership is still split, so the plan needs a target contract before implementation phases

- `ModLifecycleRegistrar` registers `RecruitsServerConfig` and `RecruitsClientConfig`, with the client file explicitly loaded from `bannermod-client.toml`.
- `WorkersLifecycleRegistrar` still registers a separate `WorkersServerConfig.SERVER`.
- Workers config values remain scoped and named as standalone Workers concepts.

**Planning implication:** Phase 2 should define the target as BannerMod-owned config naming/docs/behavior per D-08 and D-09, while allowing a transitional dual-read or dual-register seam during migration.

### 5. This phase is best executed as design-contract slices, not broad implementation rewrites

- The phase boundary explicitly says it clarifies target design and does not add new gameplay capability.
- Phase 1 established a pattern of explicit, reviewable slices with root docs as the active truth source.

**Planning implication:** Prefer small plans that publish design contracts and align active metadata/docs with current code seams, instead of trying to finish the full runtime migration inside the planning phase.

## Recommended Plan Shape

### Plan A — Release identity and namespace contract

Focus on:

- `recruits/src/main/resources/META-INF/mods.toml`
- `MERGE_NOTES.md`
- `.planning/phases/02-runtime-unification-design/02-runtime-identity-contract.md`

Expected outcome:

- BannerMod-first release identity is explicit in active metadata/docs.
- The end-state asset/lang namespace policy is written down as full `bannermod` ownership.

### Plan B — Legacy-state and config ownership contract

Focus on:

- `.planning/phases/02-runtime-unification-design/02-runtime-compatibility-contract.md`
- `.planning/CODEBASE.md`
- `.planning/VERIFICATION.md`

Expected outcome:

- Supported migration boundary for legacy Workers-era state is explicit and truthful.
- BannerMod-owned config direction is documented against the current Recruits/Workers registration seams.

## Do / Don’t

### Do

- Reference D-01 through D-09 directly in task actions.
- Use `WorkersRuntime`, `WorkersLegacyMappings`, `StructureManager`, `ModLifecycleRegistrar`, and `WorkersLifecycleRegistrar` as the current code-backed seams.
- Keep verification on the root `compileJava`, `processResources`, and `test` baseline.

### Don’t

- Don’t plan a revived second live `workers` mod identity.
- Don’t promise catch-all compatibility for arbitrary third-party `workers:*` references.
- Don’t turn this phase into a full package/refactor cleanup; it is a design-contract phase.

## Validation Architecture

### Quick feedback loop

- `./gradlew compileJava processResources`

### Full phase verification loop

- `./gradlew test`

### Why this is sufficient for Phase 2

- The phase primarily publishes design contracts and metadata/doc alignment.
- The fragile regressions are still root build/resource wiring and existing migration helper behavior.
- Gameplay/GameTest validation remains additive for later implementation phases that actually change runtime flows.

## Planning Constraints To Carry Forward

- Keep plan count small and file ownership clean so plans can run in parallel.
- Make each task name the exact files and decisions it implements.
- Treat `workers` compatibility seams as evidence for the migration contract, not as proof that broad compatibility is already done.
