# Merged Runtime Compatibility and Config Ownership Contract

## Status

- Active Phase 02 contract for D-05 through D-09.
- Defines the supported legacy Workers-era migration boundary for the merged `bannermod` runtime.

## Supported forward-migration boundary

- The merged runtime must migrate its own known Workers-era state forward into the active `bannermod` runtime.
- This required boundary is grounded in current code seams rather than broad compatibility promises.
- `workers/src/main/java/com/talhanation/workers/WorkersLegacyMappings.java` registers a `MissingMappingsEvent` bridge that remaps legacy `workers:*` registry ids for entities, items, blocks, POIs, and professions onto active `bannermod:*` targets when they exist.
- `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java` provides `migrateStructureNbt(...)`, `resolveRegistryId(...)`, and `resolveEntityType(...)` as the merged runtime helpers for known legacy ids and structure/build payload migration.
- `workers/src/main/java/com/talhanation/workers/world/StructureManager.java` applies `WorkersRuntime.migrateStructureNbt(...)` when loading scans and parsing structure NBT, so known `block`, `state.Name`, and `entity_type` fields move forward before reuse.

## What the merged runtime must support

- Legacy Workers-era state created by this merged mod's own prior behavior must continue to migrate forward on the known confirmed paths above.
- Registry remaps and known structure/build NBT migration are mandatory merged-runtime compatibility behavior, not optional polish.
- Later implementation phases may widen the migration boundary if new merged-runtime save or runtime seams are discovered, but they must remain explicit and code-backed.

## What is intentionally out of scope

- The project does not promise compatibility with an external standalone `workers` mod identity.
- The project does not promise catch-all migration for arbitrary third-party datapacks, custom payloads, or unknown integrations that still reference `workers:*` outside the confirmed seams.
- Preserved compatibility helpers must not be used to imply that a second live `workers` runtime contract is still supported.

## Config ownership direction

- BannerMod-owned config is the target end-state.
- `recruits/src/main/java/com/talhanation/recruits/init/ModLifecycleRegistrar.java` already registers root server/client config and explicitly loads `bannermod-client.toml`, establishing the active BannerMod-facing config surface.
- `workers/src/main/java/com/talhanation/workers/init/WorkersLifecycleRegistrar.java` still registers `WorkersServerConfig.SERVER`, which is a transitional seam rather than the desired final ownership model.
- `workers/src/main/java/com/talhanation/workers/config/WorkersServerConfig.java` remains valid migration input while Workers-specific settings are absorbed into BannerMod-owned naming, docs, and behavior.

## Transitional config migration policy

- A temporary dual-read or dual-register seam is allowed while config ownership converges.
- Transitional config compatibility should exist only to preserve the merged runtime's own known settings during migration.
- Downstream implementation work should move toward one BannerMod-owned config story rather than preserving a permanent split between Recruits and Workers config surfaces.

## Planning implications

- Root docs must distinguish supported merged-runtime migration from unsupported standalone or arbitrary third-party compatibility.
- Verification for Phase 2 design work should preserve the existing migration helpers and stay on the root compile/resources/test baseline unless a later slice changes gameplay/runtime flow.
- Later plans may extend compatibility coverage, but they must document the new boundary explicitly instead of implying broad magic support.
