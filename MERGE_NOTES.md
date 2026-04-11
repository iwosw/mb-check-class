# Merge Notes

## Goals of Both Mods

### Recruits

- Recruitable NPC army gameplay.
- Squad command, formations, patrols, claims, factions, diplomacy, and related persistence.
- Current planning focus before merge: stabilization, verification, and 1.21.1 migration prep.

### Workers

- Hireable worker villagers with profession-specific automation.
- Work areas, assignment/editing UI, merchant flows, and builder/template mechanics.
- Current planning focus before merge: recovery of incomplete gameplay loops, automated coverage, and release confidence.

## Architectural Reading

- Recruits is the core gameplay platform.
- Workers is an extension layer that reuses Recruits ownership, command, UI, pathfinding, and player-state systems.
- Final merged architecture should preserve that relationship explicitly: a shared core plus an internal workers subsystem.

## Decisions Taken In This Merge Bootstrap

- Chosen runtime base: `recruits`.
- Chosen final mod id: `bannermod`.
- Chosen integration model: absorb `workers` as a subsystem.
- Chosen root planning model: one active `.planning/` at repository root.
- Chosen archive model: move old planning trees to `.planning_legacy_recruits/` and `.planning_legacy_workers/`.
- Chosen truth policy: real code wins over old plans; disagreements must be recorded here.

## Legacy Roadmap / Backlog Carryover

### Recruits carryover

- `PORT-01`: full Minecraft `1.21.1` port remains future work.
- `PERF-01`: large-battle performance work remains future work.
- `CONT-01`: new gameplay content remains deferred.
- `COMP-01`: broader optional compatibility remains deferred.
- Pending todo carried from `recruits/.planning/STATE.md`:
  - profile and fix rendering cost for large recruit groups.
  - optimize target acquisition and pathfinding under large-scale combat.

### Workers carryover

- Explicit assignment of a worker to a specific work area remains a future requirement (`ASSIGN-01`).
- Courier/logistics worker remains future requirement (`LOGI-01`).
- Ownership/reassignment hardening remains future requirement (`OWNR-01`, `OWNR-02`).
- Full `1.21.1` port remains future requirement (`PORT-01`).

## Conflicts Between Legacy Plans and Code

### Active-doc historical wording conflict

- Older notes in this file still describe Workers deferred registers as staying under the historical `workers` namespace during transition.
- Current root code disagrees: `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java` resolves the active runtime mod id to `bannermod`, while `build.gradle` and the root bootstrap wire the shipped runtime around the single `bannermod` entrypoint.
- Active root truth for ongoing work is the newer migrated state documented below: runtime base stays `recruits`, the final active runtime id is `bannermod`, and any remaining `workers:*` handling is compatibility-only.

### Workers planning conflict

- `workers/.planning/PROJECT.md` frames `1.21.1` as the final target.
- `workers/.planning/STATE.md` simultaneously says Recruits compatibility and the `1.21.1` port were intentionally deferred from v1.
- Code reality agrees with the deferred interpretation: the code still targets Forge `1.20.1`, depends on Recruits, and is not ported.

### Recruits metadata conflict

- `recruits/build.gradle`, `recruits/gradle.properties`, `recruits/src/main/resources/META-INF/mods.toml`, and `recruits/update.json` do not agree on version strings.
- Real runtime metadata currently comes from code/build resources, not the planning notes.

### Workers metadata conflict

- The archived standalone Workers metadata/build files in `.planning_legacy_workers/standalone-build/` and `.planning_legacy_workers/standalone-metadata/` do not agree on version strings.
- The archived Workers `mods.toml` dependency range on Recruits is outdated relative to the actual current source tree.

## Current Root Layout Notes

- Root build compiles the Recruits base source tree plus the merged Workers runtime slice.
- Workers registries, client setup, menus, event listeners, and packets now bootstrap through the root `bannermod` runtime entrypoint.
- Legacy source trees remain physically present in `recruits/` and `workers/`.
- Legacy planning trees were moved to root archives.
- Archived standalone Workers build and metadata now live under `.planning_legacy_workers/standalone-build/` and `.planning_legacy_workers/standalone-metadata/`.

## Metadata Alignment Notes

- Root runtime identity is `bannermod`; any surviving Workers standalone metadata is archived and no longer lives in the active source tree.
- `workers/WorkersMain` is intentionally retained as a legacy-preserving subsystem bootstrap helper, while standalone Workers build/metadata files have been moved to the legacy archive.
- Phase 2 runtime identity and namespace truth is now locked in `.planning/phases/02-runtime-unification-design/02-runtime-identity-contract.md`: BannerMod is the only active public runtime identity, and full `bannermod` ownership of Workers-owned GUI/structure/lang assets is the target end-state.
- Any surviving `workers:*` compatibility handling or preserved `assets/workers/**` content should be treated as migration-only input, not as evidence for a second live runtime identity or a permanent mixed namespace policy.

## Runtime Merge Slice: Workers Lifecycle Routed Through BannerMod

- `Main` now instantiates the Workers subsystem bootstrap and invokes it from the root lifecycle registrar/common setup path.
- Workers deferred registers remain under the historical `workers` namespace, but they are now registered on the root mod event bus.
- Workers packets now register onto the shared root `bannermod` simple channel with a dedicated id range after the Recruits packet catalog.
- Client renderer/menu/keybinding wiring for Workers now runs from the root bootstrap instead of requiring a second active mod entrypoint.
- Root resources now include `assets/workers/**`, while legacy Workers `mods.toml` stays out of the active root artifact.

## Runtime Merge Slice: Workers Bootstrap Seam

- Workers runtime code now resolves its historical namespace, logger, and shared channel through `WorkersRuntime` instead of `WorkersMain` static bootstrap state.
- New `WorkersSubsystem` owns lifecycle and packet registration for the preserved Workers subsystem while still binding onto the root `bannermod` runtime.
- `WorkersMain` remains as a thin legacy adapter that delegates to the subsystem seam and mirrors the shared channel only for transitional compatibility.
- Historical worker registry ids and resource locations remain under the `workers` namespace, so this seam reduces bootstrap coupling without forcing a namespace migration yet.

## Runtime Merge Slice: Workers Resource Wiring Safety Notes

- Root runtime now uses a merged access transformer file under `src/main/resources/META-INF/accesstransformer.cfg` that preserves the Recruits rules and adds the unique Workers transformer entries once.
- Root mixin wiring now registers both `mixins.recruits.json` and the legacy-safe `mixins.workers.json`; the Workers config currently has no mixin classes, so this keeps runtime packaging truthful without introducing new patches.
- Standalone Workers metadata now lives only in the legacy archive; the shipped root artifact no longer has a live `workers/src/main/resources/META-INF/mods.toml` to exclude.
- Non-metadata Workers resources continue to flow into the root runtime primarily under the `assets/workers/**` namespace, which avoids direct path collisions with the active `bannermod` metadata slice.

## Runtime Merge Slice: Workers Content Re-Homed Under BannerMod Assets

- Workers GUI textures used by the merged runtime now resolve from the active `bannermod` asset namespace via `assets/bannermod/textures/gui/workers/**`.
- Workers bundled structure templates now resolve from the active `bannermod` asset namespace via `assets/bannermod/structures/workers/**`.
- This slice intentionally does not rename Workers registry ids or entity/item model namespaces yet; it only moves safe runtime-loaded content that does not affect world saves.

## Runtime Merge Slice: Workers UI/Lang Keys Routed Through BannerMod

- Safe user-facing Workers translation lookups now resolve through `gui.bannermod.workers.*`, `description.bannermod.workers.*`, and `chat.bannermod.workers.*` in the merged runtime.
- The historical `entity.workers.*`, `item.workers.*`, and `block.workers.*` keys remain untouched to avoid accidental registry-id, model, spawn egg, or save-data coupling changes during this slice.
- Active BannerMod lang files now carry the migrated Workers UI/chat/description entries so merged runtime screens and messages no longer depend on the legacy `assets/workers/lang/**` namespace for those safe strings.
- Legacy Workers lang files remain in place as a preservation layer while broader namespace and registry migration is still deferred.

## Runtime Merge Slice: Workers Registry Namespace Moved To BannerMod

- `WorkersRuntime.modId()` and `WorkersRuntime.id()` now resolve to the active `bannermod` namespace, so Workers deferred registers no longer publish a second live registry namespace.
- Workers entity, item, menu, POI, profession, and block deferred registers now attach their registry ids directly under the same active mod namespace as Recruits.
- Root resource processing now mirrors Workers spawn-egg models into `assets/bannermod/models/**`, and active BannerMod lang files now include the worker `entity.bannermod.*` and `item.bannermod.*` translation keys needed by the migrated registry ids.
- Structure placement keeps a compatibility seam: legacy scanned/template entity ids stored as `workers:*` are mapped forward to `bannermod:*` during resolution so older build-area content still loads.
- Validation for this slice was run from the root with `./gradlew compileJava processResources test`.

## Runtime Merge Slice: Legacy Workers Id Compatibility Hooks

- Critical persisted/runtime paths that could still encounter raw `workers:*` ids were narrowed to two categories: Forge registry remaps during world/inventory/profession load, and Workers structure scan/build NBT (`entity_type`, `block`, and block-state `Name`).
- The merged runtime now registers a focused `MissingMappingsEvent` bridge that remaps legacy `workers:*` entity, item, block, POI, and villager profession ids onto same-path `bannermod:*` entries when those modern targets exist.
- Workers structure templates/scans and saved `BuildArea` structure NBT now migrate those known legacy id fields forward in-memory before parsing or replaying them, so existing scans can be reopened and rebuilt without keeping broad namespace-magic elsewhere.
- This slice intentionally does not add catch-all rewriting for unrelated arbitrary NBT/resource payloads; only confirmed save/runtime critical paths were covered.
- Validation for this slice was run from the root with `./gradlew compileJava`, `./gradlew processResources`, and `./gradlew test`.

## Runtime Merge Slice: Root Readiness And Smoke Verification Baseline

- Root verification entrypoints are now documented explicitly in `.planning/VERIFICATION.md`, covering the current compile, resource-merge, JUnit, and optional GameTest hooks exposed by `build.gradle`.
- Root merged-project/source-of-truth guidance is now documented in `.planning/CODEBASE.md`, so follow-up GSD mapping/new-project flows see one active merged workspace, preserved legacy archives, active runtime paths, and current risks from the start.
- Lightweight root smoke/regression tests now cover merged Workers runtime helper invariants (`bannermod` runtime id, merged asset paths, packet id offset) plus builder-progress helper behavior used by resumed build flows.
- This slice intentionally stops short of adding full runtime gameplay E2E or mandatory GameTest coverage; the root `gametest` source set remains an explicitly documented future expansion point.
- Validation for this slice was run from the root with `./gradlew compileJava`, `./gradlew processResources`, and `./gradlew test`.

## Runtime Merge Slice: Phase 05 Stabilization Hardening

- Retained Workers JUnit suites now run from the active root `./gradlew test` path via `workers/src/test/java`, so preserved worker regressions are no longer hidden behind a separate test entrypoint.
- `MessageUpdateBuildArea` now uses the shared work-area authoring decision boundary through `BuildAreaUpdateAuthoring` and translated `WorkAreaAuthoringRules` denial messages before mutating a `BuildArea`.
- The merged `bannermod` runtime now keeps both legacy recruits/workers update-check listeners disabled behind `MergedRuntimeCleanupPolicy` until a single merged release-feed contract exists.
- These stabilization slices preserve the Phase 02 compatibility contract: they harden root verification and cleanup seams without reviving standalone `workers` compatibility or claiming legacy-tree retirement.
- Validation for these slices was run from the root with `./gradlew compileJava`, `./gradlew processResources`, and `./gradlew test`.

## Open Questions

- Which packet/save compatibility guarantees matter for existing worlds during the transition?
- Should final packages preserve `com.talhanation.workers.*` as internal subsystem packages or be moved under a shared merged root?

Asset/lang namespace and release identity are no longer open questions for active planning:

- BannerMod is the release-facing identity for the merged runtime.
- Full `bannermod` ownership is the target end-state for Workers-owned GUI, structure, and language assets.
- Preserved `workers` namespaces remain migration seams only unless a later plan documents a narrower compatibility exception.

## Next Merge Tasks

1. Audit any remaining non-structure custom payloads or external datapack content for raw `workers:*` references if real compatibility reports surface after the current critical-path remaps.
2. Continue replacing any remaining direct Workers bootstrap/static assumptions with shared merged seams where useful.
3. Merge shared UI/widget and packet infrastructure where Workers currently imports Recruits internals.
4. Add root-level verification steps that cover merged recruit and worker gameplay flows, not just compilation.
5. Decide how much further to clean the preserved `workers/` tree itself versus keeping it as a source archive now that the standalone build/metadata tails have been moved out.
