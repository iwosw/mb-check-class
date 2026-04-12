# Phase 20 Runtime Audit

## Runtime truth snapshot

- `build.gradle` is the authoritative merged build: one root Gradle project, one `main` source set, and one shipped Forge mod runtime named `bannermod`.
- `recruits/src/main/java/com/talhanation/recruits/Main.java` is still the only live `@Mod` entrypoint. The runtime is therefore recruit-led even though the physical codebase is split across root `src/`, `recruits/`, and `workers/`.
- `workers/src/main/java/com/talhanation/workers/WorkersSubsystem.java` shows workers already absorbed as an in-process subsystem: recruit bootstrap instantiates it, hands it the shared channel, and routes worker lifecycle registration through the same runtime.
- Root `src/main/java/com/talhanation/bannermod/**` classes are integration seams, not a full standalone gameplay family. They already bridge recruit and worker concerns for authority, supply, settlement binding, and config naming.
- Root-side jars still matter as behavior pressure even though they are outside maintained source: `shieldwall-1.0.1.jar`, `recruitsmoraleaddon-1.0.0.jar`, and `Recruits Siege Compatibility-2.1.0.jar` represent combat/compatibility expectations that Phase 21+ cannot ignore while retiring legacy roots.

## Build and bootstrap composition

### `build.gradle`

- `sourceSets.main.java.srcDirs = ['src/main/java', 'recruits/src/main/java', 'workers/src/main/java']` keeps three physical Java roots in one runtime artifact.
- All run configs expose only the `bannermod` namespace (`forge.enabledGameTestNamespaces`, `mods { 'bannermod' { ... } }`), which confirms there is no second active workers mod bootstrap.
- Mixin registration still names both `mixins.recruits.json` and `mixins.workers.json`, so package migration must preserve a merged runtime that can still host both patch inventories.
- `processResources` remains the single resource merge seam, including selective mirroring from `workers/src/main/resources` into active `assets/bannermod/**` paths such as `textures/gui/workers`.

### `recruits/src/main/java/com/talhanation/recruits/Main.java`

- `@Mod(Main.MOD_ID)` plus `MOD_ID = "bannermod"` makes recruit `Main` the only Forge composition root.
- `new WorkersSubsystem()` in the constructor and `workersSubsystem.registerNetwork(SIMPLE_CHANNEL)` in common setup prove workers runtime ownership already flows through recruit bootstrap rather than a separate workers `@Mod` entrypoint.
- The shared network channel is registered once through `CommonRegistry.registerChannel(Main.MOD_ID, "default")`; workers packets are appended to that same channel instead of opening a second transport.

### `recruits/src/main/java/com/talhanation/recruits/init/ModLifecycleRegistrar.java`

- Root config naming is already shared through `BannerModConfigFiles`, not separate recruit/worker file naming.
- `workersSubsystem.registerCommon(modEventBus)`, `workersSubsystem.registerRuntimeListeners()`, and `workersSubsystem.registerClientRuntimeListeners()` show recruit lifecycle owns when the worker subsystem enters the runtime.
- Runtime listeners still remain physically split after bootstrap, so the move problem is package ownership and source-root retirement, not runtime unification.

### `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java`

- `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java` is the worker-runtime identity and migration adapter inside the merged mod.
- `MOD_ID = Main.MOD_ID` keeps active runtime identity on `bannermod`, while `LEGACY_MOD_ID = "workers"` preserves only the compatibility namespace.
- `bindChannel(...)` and `networkIdOffset()` prove worker networking is layered onto the recruit-created shared channel rather than a standalone workers transport.

## Current physical ownership by surface

### Root `src/main/java/com/talhanation/bannermod/**`

Current role: shared integration seams.

- `BannerModAuthorityRules` centralizes owner/same-team/admin/forbidden decisions used by recruit and worker authority flows.
- `BannerModSupplyStatus` already mixes worker storage pressure with recruit upkeep pressure, which makes it a true cross-family seam.
- `BannerModSettlementBinding` is a root helper, but it still depends on recruit claim classes (`RecruitsClaim`, `RecruitsClaimManager`), so it is not yet independent of recruit-owned world state.

Migration implication: these classes are likely early move candidates or temporary adapters because both families already depend on them.

### `recruits/`

Current role: runtime authority and strategic/military base.

- Owns the live mod entrypoint, most registries, most event lifecycle, the root network channel, recruit entity hierarchies, claim/faction/save systems, command surfaces, and the current async pathfinding stack.
- Worker runtime still depends on recruit-owned classes for bootstrap (`Main`), config/client state, command-category wiring, GUI widgets, entity inheritance, path navigation, and saved-state vocabulary.

Migration implication: Phase 21 cannot treat recruit code as just one peer package family; it is still the controlling runtime base.

### `workers/`

Current role: absorbed civilian subsystem with heavy recruit dependency.

- `WorkersSubsystem`, `WorkersRuntime`, `WorkersLifecycleRegistrar`, `WorkersLegacyMappings`, and `WorkersNetworkRegistrar` define the worker-side composition boundary.
- Workers still own their own deferred registers, entity types, menus, screens, work-area entities, profession AI, and structure/template helpers.
- `WorkersRuntime.MOD_ID = Main.MOD_ID` and `ACTIVE_ASSET_NAMESPACE = "bannermod"` prove active runtime identity is already merged even though package families remain preserved.

Migration implication: worker package movement can happen only with explicit adapters or a coordinated move of recruit-owned dependencies it imports today.

### Root-side reference jars

Current role: unmanaged behavior pressure.

- `shieldwall-1.0.1.jar` indicates external shield-wall combat behavior expectations remain relevant to future combat ownership planning.
- `recruitsmoraleaddon-1.0.0.jar` indicates morale is still represented outside maintained source, so future military/system architecture must decide whether to absorb, replace, or keep it optional.
- `Recruits Siege Compatibility-2.1.0.jar` indicates siege compatibility still exists as a local artifact rather than maintained merged source.

Migration implication: source-root retirement alone does not eliminate these behaviors; later phases need explicit contracts for whether they become BannerMod-owned source, optional compatibility, or documented exclusions.

## Material cross-package dependencies

## Worker -> recruit dependency pressure

The workers tree still imports recruit-owned classes broadly. Examples from live code:

- Bootstrap/runtime: `WorkersRuntime` imports `com.talhanation.recruits.Main`.
- Pathfinding/entity base: worker entities such as `FarmerEntity`, `BuilderEntity`, `MinerEntity`, `LumberjackEntity`, `FishermanEntity`, `AnimalFarmerEntity`, and `MerchantEntity` import `AbstractRecruitEntity` and `AsyncGroundPathNavigation`.
- Client/UI: worker screens import recruit widgets and bases like `RecruitsScreenBase`, `PlayersList`, `SelectedPlayerWidget`, `BlackShowingTextField`, `RecruitsCheckBox`, and `CommandScreen`.
- Saved-state/gameplay vocabulary: worker code imports recruit world classes such as `RecruitsPlayerInfo`, `RecruitsGroup`, `RecruitsHireTradesRegistry`, and claim handling from `ClaimEvents`.

This confirms the worker subsystem is not migration-ready as an isolated civilian package family.

## Shared config and compatibility seams

- `ModLifecycleRegistrar` and `WorkersLifecycleRegistrar` both register config through `BannerModConfigFiles`, so config naming is already a root-owned seam.
- `WorkersNetworkRegistrar` publishes 20 worker message types onto the shared channel starting at `WorkersRuntime.networkIdOffset()`.
- `WorkersRuntime.LEGACY_MOD_ID = "workers"`, `migrateLegacyId(...)`, and `WorkersLegacyMappings.onMissingMappings(...)` preserve the narrow compatibility contract for legacy `workers:*` registry ids.
- `WorkersRuntime.migrateStructureNbt(...)` shows structure/template NBT is one of the confirmed save-critical migration paths already covered.

## Live ownership conclusions

1. The repository is already one merged runtime, but it is not yet one merged physical code family.
2. Recruit code still owns runtime authority: entrypoint, lifecycle timing, network channel creation, and most shared world-state vocabulary.
3. Workers code is a real subsystem, not dead archive code, because it still owns civilian gameplay, registries, screens, work areas, and packets that ship in the live artifact.
4. Root `bannermod` classes are bridging seams only; they reduce risk today but do not yet replace either family.
5. Root-side jars are outside maintained source yet still represent compatibility and gameplay pressure that Phase 21+ must keep visible.

## Audit implications for the Bannerlord move

- The future move to `src/main/java/com/talhanation/bannerlord/**` must preserve one live `bannermod` runtime id and one shared channel even if package families move.
- The safest early move candidates are shared seams and adapter-worthy surfaces already consumed by both families.
- Worker package relocation requires either temporary adapters or parallel migration of recruit-owned widget, pathfinding, entity-base, and saved-state seams.
- Source-root retirement is blocked until every currently live ownership surface in root `src/`, `recruits/`, `workers/`, and the root-side jar pressure has an explicit disposition.
