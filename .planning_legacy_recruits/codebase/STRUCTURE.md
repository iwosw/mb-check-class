# Codebase Structure

**Analysis Date:** 2026-04-05

## Directory Layout

```text
[project-root]/
├── src/main/java/com/talhanation/recruits/   # Mod source code grouped by gameplay feature
├── src/main/resources/                       # Forge metadata, mixin config, assets, blockstates, lang files
├── gradle/                                   # Gradle wrapper support files
├── build.gradle                              # Build, Forge, mixin, dependency, and run configuration
├── settings.gradle                           # Gradle plugin management
├── gradle.properties                         # Gradle and JVM properties
├── README.md                                 # Minimal project overview
└── .planning/codebase/                       # Generated architecture/reference docs
```

## Directory Purposes

**`src/main/java/com/talhanation/recruits/`:**
- Purpose: Primary Java package root for gameplay logic and mod bootstrap.
- Contains: `Main.java`, root event handlers, plus subpackages for `client`, `commands`, `compat`, `config`, `entities`, `events`, `init`, `inventory`, `items`, `mixin`, `network`, `pathfinding`, `util`, and `world`.
- Key files: `src/main/java/com/talhanation/recruits/Main.java`, `RecruitEvents.java`, `FactionEvents.java`, `ClaimEvents.java`, `CommandEvents.java`.

**`src/main/java/com/talhanation/recruits/client/`:**
- Purpose: Client-only cache, events, rendering, and screen code.
- Contains: state cache in `ClientManager.java`, key and render event listeners, GUI packages for commands, diplomacy, faction, group, player, widgets, overlays, and world map.
- Key files: `src/main/java/com/talhanation/recruits/client/ClientManager.java`, `client/events/ClientEvent.java`, `client/events/KeyEvents.java`, `client/gui/worldmap/WorldMapScreen.java`.

**`src/main/java/com/talhanation/recruits/commands/`:**
- Purpose: Brigadier command definitions.
- Contains: admin and patrol-spawn commands.
- Key files: `src/main/java/com/talhanation/recruits/commands/RecruitsAdminCommands.java`, `PatrolSpawnCommand.java`.

**`src/main/java/com/talhanation/recruits/compat/`:**
- Purpose: Optional integration helpers for other mods and alternate weapon types.
- Contains: compatibility adapters and weapon abstractions.
- Key files: `src/main/java/com/talhanation/recruits/compat/SmallShips.java`, `IWeapon.java`, `MusketWeapon.java`, `Corpse.java`.

**`src/main/java/com/talhanation/recruits/config/`:**
- Purpose: Forge config definitions.
- Contains: server and client config specs.
- Key files: `src/main/java/com/talhanation/recruits/config/RecruitsServerConfig.java`, `RecruitsClientConfig.java`.

**`src/main/java/com/talhanation/recruits/entities/`:**
- Purpose: Recruit, companion, and leader entity implementations.
- Contains: base entity classes, concrete unit classes, and nested AI/navigation/controller packages.
- Key files: `src/main/java/com/talhanation/recruits/entities/AbstractRecruitEntity.java`, `AbstractLeaderEntity.java`, `CommanderEntity.java`, `BowmanEntity.java`, `MessengerEntity.java`.

**`src/main/java/com/talhanation/recruits/entities/ai/`:**
- Purpose: Entity behavior goals and combat/navigation helpers.
- Contains: goal classes and nested packages for `async`, `compat`, `controller`, `horse`, `navigation`, `pillager`, and `villager`.
- Key files: `src/main/java/com/talhanation/recruits/entities/ai/RecruitFollowOwnerGoal.java`, `entities/ai/controller/PatrolLeaderAttackController.java`, `entities/ai/navigation/RecruitPathNavigation.java`.

**`src/main/java/com/talhanation/recruits/events/`:**
- Purpose: Domain event type definitions rather than active listeners.
- Contains: custom event classes posted onto `MinecraftForge.EVENT_BUS`.
- Key files: `src/main/java/com/talhanation/recruits/events/FactionEvent.java`, `ClaimEvent.java`, `RecruitEvent.java`, `SiegeEvent.java`, `DiplomacyEvent.java`.

**`src/main/java/com/talhanation/recruits/init/`:**
- Purpose: Deferred registration and bootstrap helpers.
- Contains: blocks, entities, items, POIs, professions, screens, shortcuts, sounds.
- Key files: `src/main/java/com/talhanation/recruits/init/ModEntityTypes.java`, `ModItems.java`, `ModBlocks.java`, `ModScreens.java`, `ModShortcuts.java`.

**`src/main/java/com/talhanation/recruits/inventory/`:**
- Purpose: Server-side menu/container implementations.
- Contains: menu classes backing recruit, command, hire, faction, promote, and patrol leader UIs.
- Key files: `src/main/java/com/talhanation/recruits/inventory/RecruitInventoryMenu.java`, `CommandMenu.java`, `PatrolLeaderContainer.java`, `PromoteContainer.java`.

**`src/main/java/com/talhanation/recruits/items/`:**
- Purpose: Custom item classes.
- Contains: spawn egg and banner item classes.
- Key files: `src/main/java/com/talhanation/recruits/items/RecruitsSpawnEgg.java`, `HeldBannerItem.java`.

**`src/main/java/com/talhanation/recruits/mixin/`:**
- Purpose: Sponge mixin patches to vanilla behavior.
- Contains: targeted mixins for horses, mobs, animals, pathfinding, and client Minecraft behavior.
- Key files: `src/main/java/com/talhanation/recruits/mixin/WalkNodeEvaluatorMixin.java`, `MobMixin.java`, `MixinMinecraft.java`.

**`src/main/java/com/talhanation/recruits/network/`:**
- Purpose: Packet classes for server actions and client sync.
- Contains: 105 `Message*.java` classes.
- Key files: `src/main/java/com/talhanation/recruits/network/MessageMovement.java`, `MessageSaveTeamSettings.java`, `MessageToClientUpdateClaims.java`, `MessageToClientUpdateFactions.java`.

**`src/main/java/com/talhanation/recruits/pathfinding/`:**
- Purpose: Async pathfinding implementation.
- Contains: async path, path processor, pathfinder, navigation wrappers, node evaluator cache/generator.
- Key files: `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathProcessor.java`, `AsyncPathfinder.java`, `AsyncGroundPathNavigation.java`.

**`src/main/java/com/talhanation/recruits/util/`:**
- Purpose: Shared utility helpers.
- Contains: formation, claim, attack, registry, profile, and delayed execution helpers.
- Key files: `src/main/java/com/talhanation/recruits/util/FormationUtils.java`, `ClaimUtil.java`, `RegistryUtils.java`, `DelayedExecutor.java`.

**`src/main/java/com/talhanation/recruits/world/`:**
- Purpose: Persistent domain models and managers.
- Contains: factions, claims, groups, routes, diplomacy, treaties, patrol spawns, save data types, and player info/unit managers.
- Key files: `src/main/java/com/talhanation/recruits/world/RecruitsFactionManager.java`, `RecruitsClaimManager.java`, `RecruitsTeamSaveData.java`, `RecruitsRoute.java`.

**`src/main/resources/`:**
- Purpose: Non-code mod resources.
- Contains: Forge metadata, access transformer, mixin config, data pack tags, asset models, textures, blockstates, and translations.
- Key files: `src/main/resources/META-INF/mods.toml`, `META-INF/accesstransformer.cfg`, `mixins.recruits.json`, `assets/recruits/lang/en_us.json`.

## Key File Locations

**Entry Points:**
- `src/main/java/com/talhanation/recruits/Main.java`: Forge `@Mod` entrypoint and global bootstrap.
- `src/main/java/com/talhanation/recruits/client/events/ClientEvent.java`: client renderer and layer registration entrypoint.
- `src/main/resources/META-INF/mods.toml`: mod metadata consumed by Forge.

**Configuration:**
- `build.gradle`: build, run configs, dependencies, mixin plugin setup.
- `settings.gradle`: Gradle plugin repositories.
- `src/main/java/com/talhanation/recruits/config/RecruitsServerConfig.java`: server config schema.
- `src/main/java/com/talhanation/recruits/config/RecruitsClientConfig.java`: client config schema.
- `src/main/resources/mixins.recruits.json`: mixin declaration file.

**Core Logic:**
- `src/main/java/com/talhanation/recruits/entities/AbstractRecruitEntity.java`: shared recruit state, ticking, targeting, commands, and networking hooks.
- `src/main/java/com/talhanation/recruits/FactionEvents.java`: faction creation/join/leave/orchestration.
- `src/main/java/com/talhanation/recruits/ClaimEvents.java`: siege and claim processing.
- `src/main/java/com/talhanation/recruits/CommandEvents.java`: movement, formation, and command execution.
- `src/main/java/com/talhanation/recruits/world/RecruitsFactionManager.java`: faction persistence and broadcasts.

**Testing:**
- Not detected. No `src/test/`, `src/test/java/`, `*.test.*`, or `*.spec.*` files were found under `/home/kaiserroman/recruits` during this architecture pass.

## Naming Conventions

**Files:**
- Java classes use PascalCase filenames matching class names: `src/main/java/com/talhanation/recruits/entities/CommanderEntity.java`.
- Packet classes are `Message*`: `src/main/java/com/talhanation/recruits/network/MessageToClientUpdateFactions.java`.
- Registry helpers are `Mod*`: `src/main/java/com/talhanation/recruits/init/ModEntityTypes.java`.
- Manager classes are `Recruits*Manager` or `*SaveData`: `src/main/java/com/talhanation/recruits/world/RecruitsClaimManager.java`, `RecruitsTeamSaveData.java`.
- Resource files use lowercase snake_case: `src/main/resources/assets/recruits/lang/en_us.json`, `assets/recruits/models/item/recruit_spawn_egg.json`.

**Directories:**
- Java packages are lowercase and domain-oriented: `src/main/java/com/talhanation/recruits/network/`, `client/gui/faction/`, `entities/ai/navigation/`.
- GUI directories are subdivided by feature area rather than screen type: `client/gui/diplomacy/`, `client/gui/group/`, `client/gui/worldmap/`.

## Where to Add New Code

**New Feature:**
- Primary server logic: add a root event/orchestrator class under `src/main/java/com/talhanation/recruits/` if the feature is world-level, or extend an existing manager under `src/main/java/com/talhanation/recruits/world/` if it owns persistent state.
- Persistent models: place them in `src/main/java/com/talhanation/recruits/world/` beside the corresponding manager/save pair.
- Client sync: add `Message*` classes in `src/main/java/com/talhanation/recruits/network/` and register them through `src/main/java/com/talhanation/recruits/Main.java`.
- Tests: Not applicable; no test directory is established.

**New Component/Module:**
- New entity type: implement under `src/main/java/com/talhanation/recruits/entities/`, register in `src/main/java/com/talhanation/recruits/init/ModEntityTypes.java`, and add renderer/menu support in `client/events/ClientEvent.java` or `init/ModScreens.java` as needed.
- New screen: place under the nearest feature package in `src/main/java/com/talhanation/recruits/client/gui/` and back it with a container in `src/main/java/com/talhanation/recruits/inventory/` if it needs a server menu.
- New custom event type: place in `src/main/java/com/talhanation/recruits/events/` to match `FactionEvent.java`, `ClaimEvent.java`, and `RecruitEvent.java`.

**Utilities:**
- Shared helpers: add to `src/main/java/com/talhanation/recruits/util/` when the code is stateless and reused across features.
- Pathfinding-specific helpers: keep them in `src/main/java/com/talhanation/recruits/pathfinding/` instead of `util/`.
- Compatibility helpers: place optional-mod logic in `src/main/java/com/talhanation/recruits/compat/`.

## Special Directories

**`src/main/resources/assets/recruits/`:**
- Purpose: Textures, models, blockstates, and language files for the mod.
- Generated: No.
- Committed: Yes.

**`src/main/resources/data/`:**
- Purpose: Datapack-style game data such as POI tags.
- Generated: Partially supported by the Gradle `data` run in `build.gradle`, but current checked-in files are committed resources.
- Committed: Yes.

**`src/main/java/com/talhanation/recruits/network/`:**
- Purpose: High-volume message directory for client/server packet classes.
- Generated: No.
- Committed: Yes.

**`src/main/java/com/talhanation/recruits/client/gui/worldmap/`:**
- Purpose: Standalone world map subsystem with chunk imagery, claim overlays, context menus, and route editing.
- Generated: No.
- Committed: Yes.

**`src/main/java/com/talhanation/recruits/events/`:**
- Purpose: Custom event definitions even though their package declarations remain `package com.talhanation.recruits;`.
- Generated: No.
- Committed: Yes.

**`src/generated/resources/`:**
- Purpose: Data-generator output configured in `build.gradle`.
- Generated: Yes, when the Gradle `data` run is executed.
- Committed: Not detected in the current checkout.

---

*Structure analysis: 2026-04-05*
