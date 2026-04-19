# Legacy Workers Compatibility Slice Summary

> Historical summary only. This file documents an earlier compatibility slice and should not be treated as the current roadmap or runtime truth.

- Audited the real post-merge `workers:*` compatibility surface and limited fixes to confirmed critical paths: Forge missing registry mappings during world/inventory/profession load, plus Workers structure scan/build NBT fields that still persist raw legacy ids.
- Added a focused runtime remap bridge for legacy Workers entity/item/block/POI/profession ids and in-memory migration for structure `entity_type`, `block`, and block-state `Name` fields before scans/build plans are parsed or replayed.
- Added regression tests for the migration helpers and revalidated the root build with `./gradlew compileJava`, `./gradlew processResources`, and `./gradlew test`.

## Changed Files

- `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java`
- `workers/src/main/java/com/talhanation/workers/WorkersLegacyMappings.java`
- `workers/src/main/java/com/talhanation/workers/init/WorkersLifecycleRegistrar.java`
- `workers/src/main/java/com/talhanation/workers/world/StructureManager.java`
- `workers/src/main/java/com/talhanation/workers/entities/workarea/BuildArea.java`
- `src/test/java/com/talhanation/workers/WorkersRuntimeLegacyIdMigrationTest.java`
- `MERGE_NOTES.md`
- `.planning/ROADMAP.md`
- `.planning/REQUIREMENTS.md`
- `.planning/STATE.md`

## Critical Paths Covered

1. **Forge missing registry mappings during save/world load**
   - Legacy `workers:*` ids for Workers-owned entities, items, blocks, POIs, and professions are remapped to same-path `bannermod:*` ids when the active target exists.
2. **Workers structure scan/build NBT**
   - `entity_type`
   - block `block`
   - block-state `Name`
   - Migration runs when scan NBT is loaded, when structure NBT is attached to `BuildArea`, and before parsed structure previews are consumed.

## Verification

- `./gradlew compileJava` ✅
- `./gradlew processResources` ✅
- `./gradlew test` ✅

## Residual Risks

- This slice does not rewrite arbitrary third-party datapack content or unknown custom NBT schemas that may still hardcode `workers:*`; only confirmed critical runtime/save paths were covered.
- Missing-mapping remaps depend on modern same-path `bannermod:*` targets existing; any legacy id with no active replacement will still require a deliberate compatibility decision.
- No workspace-root git repository is initialized, so the requested per-task execution commits could not be created from `/home/kaiserroman/bannermod`.
