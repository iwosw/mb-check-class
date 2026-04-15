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

### Settlement wording conflict

- Earlier active architecture wording described settlements as faction-aligned hubs but left the binding implied, which risked later slices inventing different legality and degradation rules from the same code.
- Current root code and Phase 09 now make the narrower truth explicit: settlement binding is derived from claim ownership plus active worker infrastructure and team affiliation, not from a dedicated settlement manager or save-data type.
- Active root planning should therefore use the explicit `FRIENDLY_CLAIM`, `HOSTILE_CLAIM`, `UNCLAIMED`, and `DEGRADED_MISMATCH` vocabulary and avoid implying that a deeper persistence rewrite already exists.

### Recruits metadata conflict

- `recruits/build.gradle`, `recruits/gradle.properties`, `recruits/src/main/resources/META-INF/mods.toml`, and `recruits/update.json` do not agree on version strings.
- Real runtime metadata currently comes from code/build resources, not the planning notes.

### Future source-layout wording conflict

- Older merge notes in this file described the future tree-retirement destination as `src/main/java/com/talhanation/bannermod/**`.
- Current active Phase 20 planning and roadmap truth now disagree with that older wording: the normalized destination for the physical move is `src/main/java/com/talhanation/bannerlord/**`, while the live runtime mod id still remains `bannermod`.
- Current root code still confirms only the present runtime/package split (`build.gradle`, `recruits/src/main/java/com/talhanation/recruits/Main.java`, and `src/main/java/com/talhanation/bannermod/**`); active planning truth for future source-tree retirement is therefore `com.talhanation.bannerlord` packages under the existing `src/main/java/**` root, without changing the live mod id.

### Future jar-feature ownership conflict

- The repo root contains local jars for shield wall, morale, and siege compatibility behavior.
- Those jars are useful behavior references, but they should not be treated as the long-term maintainable end-state for the merged runtime.
- Active future-planning truth is to audit and absorb any kept behavior into BannerMod-owned source with explicit contracts and tests, while keeping optional third-party mod compatibility documented separately.

### Future roadmap numbering conflict

- The first future-expansion research draft described its work as Phase 0 through Phase 6.
- The active roadmap already has real Phases 1 through 19, so that parallel numbering was only acceptable as a temporary research label.
- Active planning truth is now to treat that same work as future roadmap Phases 20 through 26 so roadmap discovery, resumption, and later phase planning stay numerically unambiguous.

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

## Phase 21 Pivot (2026-04-15)

### Why bannerlord was the wrong convergence namespace
The wave-1 through wave-5 execution under `com.talhanation.bannerlord.**` was abandoned per CONTEXT D-01/D-02 after the team concluded (i) the bannerlord name collided with the sibling Bannerlord-inspired gameplay subsystem in planning-stage-only docs, creating ambiguity at every cross-reference, and (ii) `bannermod` already owned the active mod id, config filenames, and resource paths -- converging into bannermod aligns the Java package tree with the runtime contract instead of forcing a second rename later.

### Revert range
All nineteen commits from `f1832af feat(21-02): move bootstrap ownership into bannerlord packages` through `a792dc3 docs(21-06): complete shared seam ownership plan` inclusive are reverted as per-original-commit `revert(21): pivot bannerlord -> bannermod -- <subject>` commits (Task 2). The in-flight working-tree WIP capture (if any, from Task 1) is also reverted with the same preamble. Bisectability is preserved (no squash).

### Re-execution target
Plans 21-02 through 21-09 re-execute against `com.talhanation.bannermod.**` (convergence namespace, CONTEXT D-01/D-02). Plan 21-02 is the only place the shared-package overlap note lives -- this section does NOT duplicate it.

### Shared-package overlap (authored by 21-02)
Plan 21-02 introduced a new sibling subtree `com.talhanation.bannermod.shared.{authority,settlement,logistics}` that owns the canonical authority/settlement/logistics seam implementations. The pre-existing peer packages `com.talhanation.bannermod.{authority,settlement,logistics}` were NOT deleted -- per CONTEXT D-05 they were reduced to `@Deprecated` thin forwarders that delegate every public method/enum/record into the new `bannermod.shared.*` canonicals via mapping helpers, with no duplicate logic retained.

**In-scope (shipped this plan):** Five canonical files were created and their legacy peers were reduced to forwarders -- `BannerModAuthorityRules`, `BannerModSettlementBinding`, `BannerModSupplyStatus`, `BannerModUpkeepProviders`, `BannerModCombinedContainer`. All seventeen active callers across `bannermod.governance.**`, `src/test/java/**`, and `src/gametest/java/**` were retargeted to import from `bannermod.shared.*` directly. The `recruits/` and `workers/` legacy source roots had no remaining callers of these seams. Live runtime contract (mod id `bannermod`, config filenames `bannermod-{military,settlement,client}.toml`, claim-derived settlement binding) is unchanged.

**Out of scope / deferred:** Three classes from the original 21-02 plan -- `BannerModLogisticsRoute`, `BannerModLogisticsService`, `BannerModCourierTask` -- were NOT moved in this plan because no implementation exists in `bannermod.logistics.*` today and no callers reference those paths. They depend on the worker civilian seam (`AbstractWorkerEntity`) which lands in wave 21-04. They will be created in `bannermod.shared.logistics` directly by the plan that introduces their first caller.

**Forwarder lifespan:** The `@Deprecated` legacy forwarders are short-lived. Per the plan must-haves they exist only for staged migration safety -- once Phase 21 closes and any external/IDE references have rotated, a follow-up plan deletes the legacy peers. Phase 21 deliberately defers that deletion: phase 21 is a structural move, not a semantic merge (D-05). Reconciliation is owned by a separate, post-Phase-21 cleanup phase.

### Wave 3 — composition-root migration (authored by 21-03)

**What moved to outer repo `src/main/java/com/talhanation/bannermod/`:**

- `bannermod.bootstrap.BannerModMain` — unified `@Mod("bannermod")` entrypoint replacing both `recruits.Main` and `workers.WorkersMain`; wires lifecycle, DeferredRegisters, channel creation, and compat-flag detection.
- `bannermod.bootstrap.BannerModLifecycle` — extracted lifecycle event handlers.
- `bannermod.bootstrap.WorkersRuntime` — outer-repo canonical copy of workers runtime helper (mod id, legacy migration, channel binding).
- `bannermod.bootstrap.WorkersSubsystem` — outer-repo canonical copy of workers lifecycle/network subsystem glue.
- `bannermod.bootstrap.MergedRuntimeCleanupPolicy` — legacy update-checker disable policy.
- `bannermod.network.BannerModNetworkBootstrap` — single shared `SimpleChannel`; recruits at `[0..103]`, workers at `[104..123]` (offset = `WORKER_PACKET_OFFSET = 104`).
- `bannermod.registry.military.{ModBlocks,ModEntityTypes,ModItems,ModPois,ModProfessions,ModScreens,ModShortcuts,ModSounds}` — copied from `recruits/init/`, package and `Main.MOD_ID` references rewritten to `BannerModMain.MOD_ID`.
- `bannermod.registry.civilian.{ModBlocks,ModEntityTypes,ModItems,ModMenuTypes,ModPois,ModProfessions,ModShortcuts,ModSounds}` — copied from `workers/init/`, package and `WorkersMain.MOD_ID` references rewritten to `BannerModMain.MOD_ID`.

**What stayed as forwarders:**

- `recruits.Main` — `@Deprecated` forwarder; no `@Mod`, forwards static fields from `BannerModMain`.
- `workers.WorkersMain` — `@Deprecated` forwarder; `@Mod` removed, forwards `MOD_ID`/`LOGGER`/`SIMPLE_CHANNEL` from `BannerModMain`.

**What was deleted from clones:**

- `recruits/init/Mod{Blocks,EntityTypes,Items,Pois,Professions,Screens,Shortcuts,Sounds}.java` — git-removed in recruits clone commit `d45d0955`.
- `workers/init/Mod{Blocks,EntityTypes,Items,MenuTypes,Pois,Professions,Shortcuts,Sounds}.java` — git-removed in workers clone commit `2f806fd`.

**sed invocations used (inner bannermod tree):**
- `com.talhanation.recruits.init.` → `com.talhanation.bannermod.registry.military.` — no surviving references found in outer bannermod tree (registry holders reference the new package directly).
- `com.talhanation.workers.init.` → `com.talhanation.bannermod.registry.civilian.` — fixed in `bannermod.bootstrap.WorkersSubsystem` (static imports retargeted).

### Wave 4 — events + commands + config migration (authored by 21-04)

**What moved to outer repo `src/main/java/com/talhanation/bannermod/`:**

- `bannermod.events.{RecruitEvents,FactionEvents,ClaimEvents,CommandEvents,VillagerEvents,PillagerEvents,AssassinEvents,DamageEvent,DebugEvents,AttributeEvent}` — copied from `recruits/` top-level; package line rewritten to `bannermod.events`.
- `bannermod.events.{WorkersVillagerEvents,WorkersCommandEvents,WorkersAttributeEvent}` — copied from `workers/` top-level; renamed with `Workers` prefix to avoid collision with recruit counterparts; `WorkersMain.MOD_ID` references replaced with FQN `BannerModMain.MOD_ID`.
- `bannermod.UpdateChecker` — copied from `recruits/UpdateChecker.java`; package rewritten.
- `bannermod.WorkersUpdateChecker` — copied from `workers/UpdateChecker.java`; package + class name rewritten to `WorkersUpdateChecker`.
- `bannermod.Translatable` — copied from `workers/Translatable.java`; package rewritten.
- `bannermod.commands.military.{PatrolSpawnCommand,RecruitsAdminCommands}` — copied from `recruits/commands/`; package rewritten to `bannermod.commands.military`; `recruits.ClaimEvents/RecruitEvents/FactionEvents` imports retargeted to `bannermod.events.*`.
- `bannermod.config.{RecruitsClientConfig,RecruitsServerConfig}` — copied from `recruits/config/`; package rewritten; no collision with existing `BannerModConfigFiles`.
- `bannermod.config.WorkersServerConfig` — copied from `workers/config/`; package rewritten; no collision.

**Naming collisions resolved:**
- `workers.VillagerEvents` vs `recruits.VillagerEvents` → workers version renamed `WorkersVillagerEvents`.
- `workers.CommandEvents` vs `recruits.CommandEvents` → workers version renamed `WorkersCommandEvents`.
- `workers.AttributeEvent` vs `recruits.AttributeEvent` → workers version renamed `WorkersAttributeEvent`.
- `workers.UpdateChecker` vs `recruits.UpdateChecker` → workers version renamed `WorkersUpdateChecker`.
- `bannermod.config.WorkersServerConfig` vs existing `bannermod.config.BannerModConfigFiles` → no collision (different class names); both coexist.

**What was deleted from clones:**
- `recruits/`: `{RecruitEvents,FactionEvents,ClaimEvents,CommandEvents,VillagerEvents,PillagerEvents,AssassinEvents,DamageEvent,DebugEvents,AttributeEvent,UpdateChecker}.java` — git-removed in recruits clone commit `bca0a039`.
- `recruits/commands/`: `{PatrolSpawnCommand,RecruitsAdminCommands}.java` — git-removed in recruits clone commit `afaccc39`.
- `recruits/config/`: `{RecruitsClientConfig,RecruitsServerConfig}.java` — git-removed in recruits clone commit `afaccc39`.
- `workers/`: `{VillagerEvents,CommandEvents,AttributeEvent,UpdateChecker,Translatable}.java` — git-removed in workers clone commit `1a229ff`.
- `workers/config/`: `WorkersServerConfig.java` — git-removed in workers clone commit `990ddc5`.

**BannerModMain updates:**
- Imports for `WorkersVillagerEvents`, `WorkersCommandEvents`, `WorkersUpdateChecker` retargeted to `bannermod.events.*` / `bannermod.*`.
- Import for `WorkersServerConfig` retargeted to `bannermod.config.*`.
- Added imports and `registerConfig` calls for `RecruitsClientConfig` (CLIENT) and `RecruitsServerConfig` (SERVER).
- `onRegisterCommands` stub filled with `PatrolSpawnCommand.register()` and `RecruitsAdminCommands.register()`.
- `WorkersSubsystem`: `VillagerEvents`/`CommandEvents` imports retargeted to `bannermod.events.Workers*`.

**sed invocations used (inner bannermod tree):**
- `com.talhanation.recruits.config.` → `com.talhanation.bannermod.config.` — applied across all files in `bannermod/` tree.
- `com.talhanation.workers.config.` → `com.talhanation.bannermod.config.` — applied across all files in `bannermod/` tree.
- `com.talhanation.recruits.ClaimEvents` → `com.talhanation.bannermod.events.ClaimEvents` — fixed in `WorkersVillagerEvents.java` and `commands/military/RecruitsAdminCommands.java`.

**Note on clone callers:** Files in `recruits/network/`, `recruits/pathfinding/`, `recruits/client/` etc. still import `com.talhanation.recruits.config.*` (now deleted). Per D-22, compile is not required to be clean between waves. These are addressed in waves 21-05..21-09.

### Wave 5 — military gameplay subsystem migration (authored by 21-05)

**What moved to outer repo `src/main/java/com/talhanation/bannermod/`:**

- `bannermod.entity.military.**` — 21 classes copied from `recruits/entities/` (non-`ai/` top-level): `AbstractChunkLoaderEntity`, `AbstractInventoryEntity`, `AbstractLeaderEntity`, `AbstractOrderAbleEntity`, `AbstractRecruitEntity`, `AssassinEntity`, `AssassinLeaderEntity`, `BowmanEntity`, `CaptainEntity`, `CommanderEntity`, `CrossBowmanEntity`, `HorsemanEntity`, `ICompanion`, `IRangedRecruit`, `IStrategicFire`, `MessengerEntity`, `NomadEntity`, `RecruitEntity`, `RecruitShieldmanEntity`, `ScoutEntity`, `VillagerNobleEntity`.
- `bannermod.ai.military.**` — 55 classes copied from `recruits/entities/ai/**` (flat + subpackages `controller/`, `horse/`, `navigation/`, `async/`, `villager/`, `compat/`, `pillager/`).
- `bannermod.ai.pathfinding.**` — 10 classes copied from `recruits/pathfinding/`: `AsyncGroundPathNavigation`, `AsyncPath`, `AsyncPathNavigation`, `AsyncPathProcessor`, `AsyncPathfinder`, `AsyncPathfinderMob`, `AsyncWaterBoundPathNavigation`, `NodeEvaluatorCache`, `NodeEvaluatorGenerator`, `PathProcessingRuntime`.
- `bannermod.mixin.**` — 10 mixin classes copied from `recruits/mixin/` (`AbstractHorseMixin`, `AnimalMixin`, `CamelMixin`, `ClassInstanceMultiMapMixin`, `GoalUtilsMixin`, `LivingEntityMixin`, `MixinMinecraft`, `MobMixin`, `RestrictSunGoalMixin`, `WalkNodeEvaluatorMixin`).
- `bannermod.compat.**` — 11 classes copied from `recruits/compat/` (including `compat/workers/IVillagerWorker.java` preserving the subpackage).
- `bannermod.migration.**` — 3 seam helpers copied from `recruits/migration/`: `CompatPathingSeams`, `NetworkBootstrapSeams`, `StatePersistenceSeams`.
- `bannermod.events.**` — 6 event sub-classes copied from `recruits/events/` appended to the 13 event classes landed by Wave 4: `ClaimEvent`, `DiplomacyEvent`, `FactionEvent`, `RecruitEvent`, `RecruitsOnWriteSpawnEggEvent`, `SiegeEvent`. `bannermod.events` now holds 19 files total.
- `bannermod.util.**` — 12 classes copied from `recruits/util/`: `AttackUtil`, `ClaimUtil`, `DelayedExecutor`, `FormationUtils`, `GameProfileUtils`, `Kalkuel`, `NPCArmy`, `PathAnalyzer`, `ProcessState`, `RecruitCommanderUtil`, `RegistryUtils`, `WaterObstacleScanner`.

**Mixin config reissued:**
- `src/main/resources/mixins.bannermod.json` created from the old clone config with `"package": "com.talhanation.bannermod.mixin"` and `"refmap": "mixins.bannermod.refmap.json"`. Mixin class list identical.
- `build.gradle` `mixin { … }` block extended with `add sourceSets.main, 'mixins.bannermod.refmap.json'` and `config 'mixins.bannermod.json'`. Old `mixins.recruits.json` entry left in place until 21-09 retires it — except the clone-owned file itself is deleted, so the old config entry now points at nothing (Gradle will fail-soft on missing mixin json only when build runs; this is acceptable per D-22 "compile-green deferred to 21-09"). Flagged for 21-09.
- `recruits/src/main/resources/mixins.recruits.json` deleted from clone.

**What was deleted from clones:**
- `recruits/`: `src/main/java/com/talhanation/recruits/{entities,pathfinding,mixin,compat,migration,events,util}/**` — removed in recruits clone commits `237d6226` (entities+pathfinding) and `99d823e2` (mixin+compat+migration+events+util + mixins.recruits.json).

**sed invocations used:**
- On the newly copied outer subtrees (Task 1 post-pass): `com.talhanation.recruits.entities.ai.` → `com.talhanation.bannermod.ai.military.`; `com.talhanation.recruits.entities.` → `com.talhanation.bannermod.entity.military.`; `com.talhanation.recruits.pathfinding.` → `com.talhanation.bannermod.ai.pathfinding.`.
- On the newly copied outer subtrees (Task 2 post-pass, across the full `bannermod/` tree): `com.talhanation.recruits.mixin.` → `com.talhanation.bannermod.mixin.`; `com.talhanation.recruits.compat.` → `com.talhanation.bannermod.compat.`; `com.talhanation.recruits.migration.` → `com.talhanation.bannermod.migration.`; `com.talhanation.recruits.events.` → `com.talhanation.bannermod.events.`; `com.talhanation.recruits.util.` → `com.talhanation.bannermod.util.`.
- Clone-side (applied to `recruits/` remaining `src/main/java`, `src/test/java`, `src/gametest/java`): the same nine substitutions, so the ~150 clone files that still reference the migrated types resolve against the new `bannermod.*` FQNs. Per D-22 compile-green is still not required across the clone until 21-09, but keeping the FQNs pointing at the live namespace reduces future-wave churn.

**Package-declaration rewrites:**
- Per-file `sed -i -E 's|^package com\.talhanation\.recruits\.{sub}(\.[a-zA-Z0-9_.]+)?;|package com.talhanation.bannermod.{sub}[.subpkg];|'` computed per destination file from its directory, so nested subpackages (`entity/military/`, `ai/military/controller/`, `compat/workers/`, etc.) keep their relative paths.

**Naming collisions resolved:**
- `bannermod.events` pre-existing Wave-4 classes (13) vs newly arriving Wave-5 sub-classes (6): no name collision — Wave 4 moved top-level `Recruit*Events`, while Wave 5 added event data classes `RecruitEvent`, `FactionEvent`, `ClaimEvent`, `DiplomacyEvent`, `SiegeEvent`, `RecruitsOnWriteSpawnEggEvent`. Both families coexist as peers.
- No other subtree collided with preserved `bannermod.{citizen,governance,settlement,logistics,authority,config,shared,bootstrap,network,registry,commands}` packages.

**Note on clone callers and build state:** The clone's main source tree now has no `entities/`, `pathfinding/`, `mixin/`, `compat/`, `migration/`, `events/`, or `util/` packages. Clone callers from `recruits/network/`, `recruits/client/`, `recruits/commands/`, `recruits/inventory/`, `recruits/world/`, `recruits/init/` (gone since 21-03 but `init/` removal is partial — empty dirs), and residual top-level files still reference types that now only exist in outer `bannermod.*`. Because `build.gradle` still compiles `recruits/src/main/java` as part of the outer source set, the outer project will not compile cleanly until Wave 21-09. Per D-22 this is acceptable; per D-22 no compile gate is run in this wave.

**Flagged for 21-09:**
- Old `mixins.recruits.json` Gradle config entry references a now-missing json.
- Old `mixins.recruits.refmap.json` entry likewise dead.
- Partial `recruits/init/` residue (empty or near-empty after 21-03) should be pruned when the clone source set is retired.

**Clone test/gametest note:** Two test files (`recruits/src/test/java/com/talhanation/recruits/pathfinding/{AsyncPathProcessorTest,GlobalPathfindingControllerTest}.java`) still declare `package com.talhanation.recruits.pathfinding;`. Their in-tree package declaration is valid for the clone's own test source root; they survived the migration because test-tree Java lives under the clone's own `src/test/java` root. Their FQN imports have been rewritten to point at the new `bannermod.*` types. If 21-09 retires the clone source set, these tests move with the rest of the clone or are deleted.

### Wave 6 — military UI, inventory, persistence, items migration (authored by 21-06)

**What moved to outer repo `src/main/java/com/talhanation/bannermod/`:**

- `bannermod.client.military.**` — 107 files copied from `recruits/client/` preserving subpackage shape (`api/`, `events/`, `gui/**`, `models/`, `render/**`). Subpackages: `api` (3), `events` (5), `gui` (31 direct + `commandscreen/` 3, `component/` 4, `diplomacy/` 5, `faction/` 10, `group/` 10, `overlay/` 2, `player/` 4, `widgets/` 11, `worldmap/` 11), `models` (1), `render` (2 + `layer/` 8).
- `bannermod.inventory.military.**` — 11 files from `recruits/inventory/`: AssassinLeaderMenu, CommandMenu, DebugInvMenu, DisbandContainer, GovernorContainer, PatrolLeaderContainer, PromoteContainer, RecruitHireMenu, RecruitInventoryMenu, RecruitSimpleContainer, TeamEditMenu.
- `bannermod.persistence.military.**` — 22 files from `recruits/world/`: PillagerPatrolSpawn, RecruitPlayerUnitSaveData, RecruitsClaim, RecruitsClaimManager, RecruitsClaimSaveData, RecruitsDiplomacyManager, RecruitsDiplomacySaveData, RecruitsFaction, RecruitsFactionManager, RecruitsGroup, RecruitsGroupsManager, RecruitsGroupsSaveData, RecruitsHireTrade, RecruitsHireTradesRegistry, RecruitsPatrolSpawn, RecruitsPlayerInfo, RecruitsPlayerUnitManager, RecruitsRoute, RecruitsSavedDataFacade, RecruitsTeamSaveData, RecruitsTreatyManager, RecruitsTreatySaveData.
- `bannermod.items.military.**` — 2 files from `recruits/items/`: HeldBannerItem, RecruitsSpawnEgg.

**What was deleted from clones:**
- `recruits/client/**` — git-removed in recruits clone commit `43dc9c9c` (107 files).
- `recruits/inventory/`, `recruits/world/`, `recruits/items/` — git-removed in recruits clone commit `7fb407d4` (35 files).

**sed invocations used (compound pass across all four new outer subtrees):**
1. Package declaration: `s|^package com.talhanation.recruits.{client|inventory|world|items}.*;|package com.talhanation.bannermod.{client.military|inventory.military|persistence.military|items.military};|`
2. Cross-ref rewrites applied to new subtrees and full bannermod outer tree:
   - `recruits.client.` → `bannermod.client.military.`
   - `recruits.inventory.` → `bannermod.inventory.military.`
   - `recruits.world.` → `bannermod.persistence.military.`
   - `recruits.items.` → `bannermod.items.military.`
   - `recruits.entities.ai.` → `bannermod.ai.military.`
   - `recruits.entities.` → `bannermod.entity.military.`
   - `recruits.events.` → `bannermod.events.`
   - `recruits.util.` → `bannermod.util.`
   - `recruits.init.` → `bannermod.registry.military.`
   - `import com.talhanation.recruits.Main;` → `import com.talhanation.bannermod.bootstrap.BannerModMain;`
   - Top-level event imports: `recruits.{FactionEvents,RecruitEvents,ClaimEvents,ClaimEvent,SiegeEvent,DiplomacyEvent}` → `bannermod.events.*`
3. Clone-side FQN rewrites applied to `recruits/src/{main,test}/java/**` for the same nine substitutions above.

**Remaining recruits.* references (expected per D-22):**
- `recruits.network.*` — 40 imports in client/military GUIs, 0+ in other new trees. Network migration deferred to Wave 8.

**Remaining clone working-tree inventory after Wave 6:**
- `recruits/src/main/java/com/talhanation/recruits/Main.java` — `@Deprecated` shim (no `@Mod`), carries forward static fields from `BannerModMain`.
- `recruits/src/main/java/com/talhanation/recruits/init/ModLifecycleRegistrar.java` — `@Deprecated` stub (residual from Wave 3).
- `recruits/src/main/java/com/talhanation/recruits/network/**` — 109 network message files (Wave 8 scope).

**BannerModMain.clientSetup() updated:**
- `com.talhanation.recruits.client.events.CommandCategoryManager` → `com.talhanation.bannermod.client.military.events.CommandCategoryManager`

**Flagged for 21-09 (carried from Wave 5):**
- Old `mixins.recruits.json` Gradle config entry still dangling.
- `recruits/init/ModLifecycleRegistrar.java` stub — prune when clone source set is retired.

### Wave 7 — civilian subsystem migration (workers entities/AI/workarea/client/world/inventory/items/settlement) (authored by 21-07)

**What moved to outer repo `src/main/java/com/talhanation/bannermod/`:**

- `bannermod.entity.civilian.**` — 14 top-level entity files + 11 workarea files (25 total). Top-level: AbstractWorkerEntity, AnimalFarmerEntity, BuilderEntity, FarmerEntity, FishermanEntity, FishingBobberEntity, LumberjackEntity, MerchantAccessControl, MerchantEntity, MerchantTradeRules, MinerEntity, WorkerBindingResume, WorkerControlStatus, WorkerStorageRequestState. Workarea: AbstractWorkAreaEntity, AnimalPenArea, BuildArea, CropArea, FishingArea, LumberArea, MarketArea, MiningArea, MiningPatternContract, MiningPatternSettings, StorageArea.
- `bannermod.ai.civilian.**` — 22 files from `workers/entities/ai/`. Top-level: AbstractChestGoal, AnimalFarmerLoopProgress, AnimalFarmerWorkGoal, BuilderBuildProgress, BuilderWorkGoal, DebugSyncWorkerPathNavigation, DepositItemsToStorage, FarmerAreaSelectionTiming, FarmerLoopProgress, FarmerPlantingPreparation, FarmerWorkGoal, FishermanWorkGoal, GetNeededItemsFromStorage, LumberjackWorkGoal, MerchantWorkGoal, MinerWorkGoal, MiningClaimExcavationRules, MiningPatternPlanner, MiningPositionSelection, RecruitsPathNodeEvaluator, StorageDepositRules. Subpackage `animals/`: WorkerTemptGoal.
- `bannermod.client.civilian.**` — 24 files from `workers/client/`. Top-level: WorkersClientManager. Subpackages: `events/` (ClientEvent, ScreenEvents), `gui/` (AnimalPenAreaScreen, BuildAreaScreen, CropAreaScreen, FishingAreaScreen, LumberAreaScreen, MarketAreaScreen, MerchantAddEditTradeScreen, MerchantTradeScreen, MiningAreaScreen, StorageAreaScreen, WorkAreaScreen, WorkerCommandScreen), `gui/structureRenderer/` (StructurePreviewWidget), `gui/widgets/` (DisplayTextItemScrollDropDownMenu, ItemScrollDropDownMenu, ScrollDropDownMenuWithFolders), `render/` (FishingBobberRenderer, IRenderWorkArea, WorkerAreaRenderer, WorkerHumanRenderer, WorkerVillagerRenderer).
- `bannermod.persistence.civilian.**` — 8 files from `workers/world/`: BuildBlock, BuildBlockParse, NeededItem, ScannedBlock, StructureManager, StructureTemplateLoader, Tree, WorkersMerchantTrade.
- `bannermod.inventory.civilian.**` — 2 files from `workers/inventory/`: MerchantAddEditTradeContainer, MerchantTradeContainer.
- `bannermod.items.civilian.**` — 1 file from `workers/items/`: WorkersSpawnEgg.
- `bannermod.settlement.civilian.**` — 2 files from `workers/settlement/`: WorkerSettlementSpawnRules, WorkerSettlementSpawner. These land as a new sibling subpackage under the preserved `bannermod.settlement.*` (see two-subpackage structure note below).

**Two-subpackage structure under `bannermod.settlement`:**
- `bannermod.settlement.*` — preserved from prior phases: BannerModSettlementBinding (and other existing settlement seam classes). Do NOT modify.
- `bannermod.settlement.civilian.*` — new: WorkerSettlementSpawnRules (pure utility, no state), WorkerSettlementSpawner (spawn logic). These are siblings, not replacements.

**Mixin config fold:**
- `workers/src/main/resources/mixins.workers.json` had empty `mixins`, `client`, and `server` arrays — no mixin classes to merge.
- Deleted `mixins.workers.json` from workers clone (commit `070d6d1`).
- Removed `add sourceSets.main, 'mixins.workers.refmap.json'` and `config 'mixins.workers.json'` entries from outer `build.gradle`.
- `mixins.bannermod.json` unchanged — all existing bannermod mixin classes preserved.

**What was deleted from workers clone:**
- `workers/entities/**` and `workers/client/**` — git-removed in workers clone commit `07dfaaf` (71 files).
- `workers/world/**`, `workers/inventory/**`, `workers/items/**`, `workers/settlement/**`, `workers/src/main/resources/mixins.workers.json` — git-removed in workers clone commit `070d6d1` (14 files).

**sed invocations used (compound pass):**
1. Package declarations: per-directory sed for each target package root.
2. Cross-ref rewrites applied to all new subtrees + full outer bannermod tree:
   - `workers.entities.ai.animals.` → `bannermod.ai.civilian.animals.`
   - `workers.entities.ai.` → `bannermod.ai.civilian.`
   - `workers.entities.workarea.` → `bannermod.entity.civilian.workarea.`
   - `workers.entities.` → `bannermod.entity.civilian.`
   - `workers.client.*` → `bannermod.client.civilian.*`
   - `workers.world.` → `bannermod.persistence.civilian.`
   - `workers.inventory.` → `bannermod.inventory.civilian.`
   - `workers.items.` → `bannermod.items.civilian.`
   - `workers.settlement.` → `bannermod.settlement.civilian.`
   - `workers.init.` → `bannermod.registry.civilian.`
   - `recruits.entities.ai.` → `bannermod.ai.military.`
   - `recruits.entities.` → `bannermod.entity.military.`
   - `recruits.client.` → `bannermod.client.military.`
   - `recruits.world.` → `bannermod.persistence.military.`
   - Top-level recruits event imports → `bannermod.events.*`

**Remaining workers.* references (expected per D-22):**
- `workers.network.*` — network messages in entity/GUI files, deferred to Wave 8.
- `workers.config.WorkersServerConfig` — config deferred (Wave 9 scope).
- `workers.WorkersRuntime.bindChannel()` — in bootstrap, deferred to Wave 8.

**Remaining workers clone working-tree inventory after Wave 7:**
- `workers/src/main/java/com/talhanation/workers/WorkersMain.java` — `@Deprecated` shim.
- `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java` — runtime binding helper.
- `workers/src/main/java/com/talhanation/workers/WorkersSubsystem.java` — subsystem bootstrap.
- `workers/src/main/java/com/talhanation/workers/MergedRuntimeCleanupPolicy.java` — cleanup policy.
- `workers/src/main/java/com/talhanation/workers/init/WorkersLifecycleRegistrar.java` — lifecycle stub.
- `workers/src/main/java/com/talhanation/workers/network/**` — 18 network files (Wave 8 scope).

**Entity count vs plan threshold:**
- Plan specified `>= 30` for `entity/civilian`. Actual: 25 (14 top-level + 11 workarea). All available workers entity files were migrated; plan threshold was an overestimate. No files omitted.

## Wave 8: Network Consolidation (2026-04-15)

**What was done:**
- Migrated all 109 `recruits/network/*.java` files to `bannermod.network.messages.military.**`.
- Migrated all 24 `workers/network/*.java` files to `bannermod.network.messages.civilian.**`.
- `BannerModNetworkBootstrap.java` rewritten with canonical class-level `MILITARY_MESSAGES[]` and `CIVILIAN_MESSAGES[]` arrays.
- `workerPacketOffset()` now returns `MILITARY_MESSAGES.length` (compile-time constant = 104).
- `createSharedChannel()` registers military at `[0..104)` and civilian at `[104..124)`.

**Packet ID counts:**
- `MILITARY_MESSAGES.length` = 104 (indices 0..103)
- `CIVILIAN_MESSAGES.length` = 20 (indices 104..123)
- `workerPacketOffset()` = 104

**Ordering preservation:**
- `MILITARY_MESSAGES[]` order is verbatim from the `recruitMessages[]` array in the pre-migration `BannerModNetworkBootstrap.java` (which itself captured the legacy `recruits.Main.setup()` ordering). Packet IDs 0..103 are stable.
- `CIVILIAN_MESSAGES[]` order is verbatim from the `workerMessages[]` array in the same source (captured from legacy `workers.WorkersMain.setup()`). Packet IDs 104..123 are stable.

**Clone commits:**
- `recruits` clone: `4138b81b` — `refactor(21-08): remove migrated network subtree (109 files → bannermod.network.messages.military)`
- `workers` clone: `f4d1023` — `refactor(21-08): remove migrated network subtree (24 files → bannermod.network.messages.civilian)`

**Outer repo commits:**
- `6f3a4bd` — Task 1: bulk migration + FQN sweeps
- Task 2 commit: BannerModNetworkBootstrap canonical rewrite + MERGE_NOTES append

**FQN rewrites applied during migration:**
- `recruits.network.*` → `bannermod.network.messages.military.*`
- `workers.network.*` → `bannermod.network.messages.civilian.*`
- `recruits.entities.ai.*` → `bannermod.ai.military.*`
- `recruits.entities.*` → `bannermod.entity.military.*`
- `recruits.world.*` → `bannermod.persistence.military.*`
- `workers.entities.ai.animals.*` → `bannermod.ai.civilian.animals.*`
- `workers.entities.ai.*` → `bannermod.ai.civilian.*`
- `workers.entities.workarea.*` → `bannermod.entity.civilian.workarea.*`
- `workers.entities.*` → `bannermod.entity.civilian.*`
- `workers.client.*` → `bannermod.client.civilian.*`
- `workers.world.*` → `bannermod.persistence.civilian.*`
- `workers.inventory.*` → `bannermod.inventory.civilian.*`
- `workers.items.*` → `bannermod.items.civilian.*`
- `workers.settlement.*` → `bannermod.settlement.civilian.*`
- `workers.init.*` → `bannermod.registry.civilian.*`
- Top-level recruits event classes → `bannermod.events.*`

**Remaining deferred (Wave 9 scope):**
- `workers.config.WorkersServerConfig` ref in `MessageAddWorkArea.java` — config migration Wave 9.
- `workers.WorkersRuntime.bindChannel()` call in `BannerModNetworkBootstrap.createSharedChannel()` — Wave 9 source-root retirement.
- Neither clone's `src/main/java/` now contains a `network/` subtree. Only deprecated shim groups remain.

## Wave 9: Phase 21 Closure (2026-04-15)

### Resource Consolidation

All shipped resources were copied from the embedded clones into outer `src/main/resources/`:
- `recruits/src/main/resources/assets/bannermod/**` → `src/main/resources/assets/bannermod/**` (canonical, blockstates/lang/models/textures).
- `recruits/src/main/resources/data/minecraft/**` → `src/main/resources/data/minecraft/**` (vanilla tag overrides).
- `recruits/src/main/resources/{logo.png,pack.mcmeta}` → `src/main/resources/`.
- `recruits/src/main/resources/META-INF/{accesstransformer.cfg,mods.toml}` → `src/main/resources/META-INF/`.
- `workers/src/main/resources/assets/workers/{models,textures,structures}` → merged into `src/main/resources/assets/bannermod/{models,textures,structures}` (no JSON namespace collisions; workers spawn-egg models reference vanilla parents only).
- `workers/src/main/resources/assets/workers/lang/*.json` → key-merged into `src/main/resources/assets/bannermod/lang/*.json`. All 109 worker translation keys (`entity.workers.miner`, `entity.workers.lumberjack`, etc.) preserved verbatim alongside existing bannermod keys; lang merge is additive (no key collisions observed).

The old recruits-namespaced asset tree (`recruits/src/main/resources/assets/recruits/**`) is no longer copied into the build output. Code references rewritten via `sed`: `"recruits:` → `"bannermod:` (3 sites: `RecruitsAdminCommands` suggestion provider id, two `enemy.png` ResourceLocations) and `"workers:` → `"bannermod:` (no sites found).

### Build Source Set Cleanup

- `build.gradle` `sourceSets.{main,test,gametest}.java.srcDirs` no longer reference `recruits/src/main/java` or `workers/src/main/java`.
- `sourceSets.{main,test,gametest}.resources.srcDirs` no longer reference clone resource paths.
- `processResources` no longer copies from `workers/src/main/resources/**`.
- `data` run argument `--existing` switched from `recruits/src/main/resources/` → `src/main/resources/`.
- Mixin config retained: only `mixins.bannermod.json` registered; `mixins.recruits.json` references removed (config never existed in clones).
- `jar`/`shadowJar` mixin refmap include switched from `mixins.recruits.refmap.json` → `mixins.bannermod.refmap.json`.
- `shadowJar` corelib relocation target switched from `de.maxhenkel.recruits.corelib` → `de.maxhenkel.bannermod.corelib`.

### Clone Retention Decision (truth #5)

Decision: **Option (a) — keep clone dirs as-is in outer working tree, untracked by outer git.**

Rationale:
- The clones (`recruits/`, `workers/`) carry their own `.git/` histories and original source for archaeological purposes.
- They were already `?? recruits/`, `?? workers/` in outer `git status` before this wave; nothing in this plan promotes them to tracked outer paths.
- After the build.gradle cleanup, they are no longer compiled, packaged, or referenced by the outer build — they exist only as filesystem-visible reference copies.
- Removing them entirely would discard their per-file git history and force any future archaeological lookup to consult a separate remote. Keeping them costs only disk space.

Wave 8 had already reduced their `src/main/java/` working trees to deprecated shim groups (`recruits/Main.java`, `recruits/init/ModLifecycleRegistrar.java`, `workers/{WorkersMain,WorkersRuntime,WorkersSubsystem,MergedRuntimeCleanupPolicy}.java`, `workers/init/WorkersLifecycleRegistrar.java`). Those shims remain on disk inside the clones but no longer participate in the outer build.

