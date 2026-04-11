# Merge Plan

## Analysis Summary

### `recruits`

- Forge `1.20.1-47.4.10`, Java 17, Gradle 8.8 wrapper.
- Main mod id: `recruits` in `recruits/src/main/resources/META-INF/mods.toml`.
- Entrypoint: `com.talhanation.recruits.Main`.
- Main packages: `entities`, `network`, `world`, `client`, `pathfinding`, `compat`, `init`, `config`.
- Active registries: blocks, items, entities, screens/menus, professions, POIs.
- Persistence-heavy systems already exist: factions, claims, groups, diplomacy, treaties, routes, player state.
- Strong verification baseline already exists: JVM tests in `recruits/src/test/java` and GameTests in `recruits/src/gametest/java`.
- Already contains a `compat/workers/IVillagerWorker` seam and explicit Workers hire-trade references in `RecruitsHireTradesRegistry`.

### `workers`

- Forge `1.20.1-47.4.1`, Java 17, Gradle 8.5 wrapper.
- Historical standalone mod id: `workers`, now preserved in `.planning_legacy_workers/standalone-metadata/src/main/resources/META-INF/mods.toml`.
- Entrypoint: `com.talhanation.workers.WorkersMain`.
- Main packages: `entities`, `entities.workarea`, `entities.ai`, `network`, `client`, `world`, `init`, `config`.
- Active registries: worker entities, spawn eggs, merchant menu types; blocks/professions/POIs are present mostly as scaffold.
- Functionally depends on Recruits across core types: `AbstractRecruitEntity`, `AbstractChunkLoaderEntity`, async pathfinding, recruit GUI widgets, command categories, client claim state, hire trades, player info.
- Has JVM test coverage but no GameTest source set.

## Base Project Choice

Use `recruits` as the base project.

### Final merged mod id

`bannermod`

### Why

- `workers` has a direct build/runtime dependency on `recruits` and imports its classes in dozens of files.
- `recruits` already owns the broader runtime foundation: ownership, command UI, persistent world state, networking infrastructure, pathfinding, and compatibility seams.
- `recruits` also has the more complete verification harness, including GameTests.
- In gameplay terms, Workers reads as an automation/profession subsystem layered on top of the recruit framework rather than the other way around.

## Intended Final Structure

```text
bannermod/
  build.gradle
  settings.gradle
  gradle.properties
  gradlew
  gradlew.bat
  CLAUDE.md
  MERGE_PLAN.md
  MERGE_NOTES.md
  .planning/
  .planning_legacy_recruits/
  .planning_legacy_workers/
  src/
    main/
      java/
      resources/
    test/
      java/
      resources/
    gametest/
      java/
      resources/
  recruits/
  workers/
```

### Runtime Target

- One root Gradle project.
- One runtime mod entrypoint with mod id `bannermod`.
- One `mods.toml`.
- One active namespace strategy.
- Workers content absorbed as an internal subsystem instead of a separate mod.

## What Becomes a Subsystem

`workers` should become a subsystem inside the final merged mod.

### Subsystem Boundaries

- Worker entities and profession loops.
- Work-area entities and authoring flows.
- Merchant/trade worker flows.
- Builder/template/structure tooling.
- Worker-specific client screens and packets.

## Pieces That Transfer Almost As-Is

- Most worker entity and AI logic under `workers/src/main/java/com/talhanation/workers/entities/**` and `entities/ai/**`.
- Work-area model/entity logic under `workers/src/main/java/com/talhanation/workers/entities/workarea/**`.
- Worker-specific client screens and textures, subject to namespace relocation.
- Workers JVM tests, after package/import adjustments.
- Workers structure assets under `workers/src/main/resources/assets/workers/structures/**`.

## Pieces That Need Adaptation

- `WorkersMain` bootstrap must be dissolved into the unified runtime bootstrap.
- `workers` mod metadata, mod id, and dependency declarations must be archived or merged.
- Worker registries must be re-homed under the final runtime registry strategy.
- Worker packets need a single shared registration plan with preserved id/order guarantees where compatibility matters.
- Worker GUI/widget imports currently point into Recruits packages and should become same-project shared UI code or moved classes.
- Worker assets/lang keys currently live under the `workers` namespace and will need a deliberate final namespace plan.
- Access transformers and mixin configs must be merged carefully.

## Conflicts and Risks

### Build Script Conflicts

- Forge version mismatch: `47.4.10` vs `47.4.1`.
- Gradle wrapper mismatch: `8.8` vs `8.5`.
- Foojay version mismatch: `0.7.0` vs `0.5.0`.
- Different repository policies: Recruits gates `mavenLocal()` behind `-PallowLocalMaven`; Workers enables it unconditionally.
- The archived standalone Workers build declares `implementation fg.deobf("curse.maven:recruits-523860:7374573")`; that dependency no longer belongs in the active merged project.

### Mod Id and Metadata Conflicts

- Historical conflict was two `@Mod` entrypoints: `recruits` and `workers`; active runtime now has only `bannermod`.
- Historical conflict was two `mods.toml` files; active runtime now uses only the root `bannermod` metadata, while Workers standalone metadata is archived.
- Version drift inside both projects.
- Workers dependency range on Recruits is stale: `[1.14, 1.14.5)`.

### Registry and Init-Order Conflicts

- Duplicate bootstrap class basenames exist across both mods: `ModBlocks`, `ModEntityTypes`, `ModItems`, `ModPois`, `ModProfessions`, `ModShortcuts`, `AttributeEvent`, `ClientEvent`, `VillagerEvents`, `UpdateChecker`, `CommandEvents`.
- Workers currently assumes Recruits systems are already initialized.
- Packet registration in both mods is order-sensitive.

### Package Structure Conflicts

- Packages are separate today: `com.talhanation.recruits.*` and `com.talhanation.workers.*`.
- Workers currently imports Recruits packages directly in 50+ places.
- Final package policy must decide between preserving `com.talhanation.workers.*` as an internal package subtree or relocating to a shared root.

### Resource and Asset Path Conflicts

- Duplicate resource basenames already exist: `command_gui.png`, `hire_gui.png`, `logo.png`, `mods.toml`, `pack.mcmeta`, multiple lang files, and many shared entity texture names.
- Namespace-safe today because assets are split between `assets/recruits` and `assets/workers`.
- If final namespace becomes single, many assets must be renamed or reorganized.

### Persistence and Compatibility Risks

- Recruits uses multiple `SavedData` managers; Workers stores more state on entities/NBT.
- Worker logic depends on claim, player, and hire-trade state from Recruits.
- Packet compatibility and save migration need explicit treatment before runtime unification is declared complete.

### GSD-State Conflicts

- Both mods had their own completed `.planning/` trees with different project goals.
- Recruits prioritized stabilization and 1.21.1 prep.
- Workers prioritized recovery/completion/testing, while deferring the 1.21.1 port to v2 despite `PROJECT.md` still calling out 1.21.1 as the final target.

## Merge Order

1. Create one root build entrypoint and one active root `.planning/` context.
2. Archive both legacy `.planning/` trees without deleting content.
3. Keep `recruits` as the active runtime base while Workers stays preserved as a legacy subsystem.
4. Merge bootstrap and metadata next: finish propagating the chosen final mod id `bannermod`, final `mods.toml`, and final versioning policy across the remaining subsystem slices.
5. Move shared UI/helpers that Workers imports from Recruits into stable shared locations only where needed.
6. Fold Workers registries into the base runtime in slices: entities/items, menus/screens, packets, events.
7. Merge assets/data/configs with explicit namespace and file-renaming rules.
8. Re-run tests from the root and add integration coverage for merged worker flows.
9. Retire or archive the remaining standalone `workers` tails once cleanup goals are satisfied.

## Transitional Adapters to Keep Temporarily

- `compat/workers/IVillagerWorker` in Recruits.
- `workers/WorkersMain.java` as a legacy-preserving subsystem bootstrap helper while the remaining merge slices are absorbed.
- `workers/WorkersRuntime.java` and `workers/WorkersSubsystem.java` as the staging seam that preserves the historical `workers` namespace/channel contract without keeping `WorkersMain` as the quasi-entrypoint.
- Existing Recruits runtime metadata until Workers registries and assets are genuinely attached to the same mod.

## Immediate Practical Outcome of This Step

- Root build exists.
- Root `.planning/` exists.
- Legacy GSD contexts are preserved at root.
- Merge documentation exists.
- Workers lifecycle bootstrap is routed through the shipped root runtime while legacy metadata still remains transitional.

## Current Staging Slice Guardrails

- `workers/src/main/java/com/talhanation/workers/WorkersMain.java` remains in place as a legacy-preserving subsystem bootstrap helper and constant holder, not as a second active `@Mod` root entrypoint.
- Workers runtime, registry, and screen code should prefer the subsystem seam for namespace and channel access instead of reaching through `WorkersMain` static state directly.
- Standalone Workers build and metadata are now archived under `.planning_legacy_workers/standalone-build/` and `.planning_legacy_workers/standalone-metadata/`; they are no longer part of the active source tree.
- The authoritative runtime artifact name, root project name, and active mod metadata stay centered on `bannermod` at repository root.
- Workers registries and item/entity namespaces remain preserved in place until later slices deliberately re-home them, even though their lifecycle wiring now runs through the root runtime.
- Safe runtime-loaded content can already move under the active `bannermod` asset namespace ahead of registry-id migration; current examples are Workers GUI textures and bundled structure templates.
- Safe user-facing Workers lang keys can also move early when they are limited to GUI/chat/description strings; historical entity/item/block translation keys stay pinned to the `workers` namespace until registry migration is explicitly planned.
