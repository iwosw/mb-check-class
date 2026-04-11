<!-- GSD:project-start source:PROJECT.md -->
## Project

**Villager Recruits Stabilization and 1.21.1 Migration Prep**

Villager Recruits is a large brownfield Forge mod for Minecraft 1.20.1 focused on recruitable NPC armies, squad control, combat behavior, formations, patrols, faction systems, and related world interactions. This initiative is not about inventing new gameplay; it is about making the unstable dev branch dependable, covering the codebase with automated tests, and reshaping the internals so the actual port to 1.21.1 becomes a short, concrete next step.

**Core Value:** The dev branch must become a trustworthy base where the core NPC army mechanics behave predictably, are defended by tests, and are structurally ready for a near-term 1.21.1 migration.

### Constraints

- **Tech stack**: Stay within the existing Java 17 + Forge 1.20.1 + Gradle toolchain while preparing future compatibility with 1.21.1 — the current branch still has to build and run in its present environment
- **Behavioral stability**: External gameplay behavior should be preserved unless a change is clearly a bug fix or logic correction — this is stabilization work, not a redesign of player-facing mechanics
- **Testing**: Automated coverage must include both unit-style tests and game tests — the project is not complete with ad hoc manual checking alone
- **Scope**: Verification must cover the full mod surface, with deeper focus on battles, persistence, commands, AI, networking, and formations — these risk areas drive prioritization and roadmap structure
- **Migration prep**: Internal refactors may be substantial if they reduce migration risk or make version-sensitive code easier to isolate — the user explicitly allows bold refactoring in service of the future port
<!-- GSD:project-end -->

<!-- GSD:stack-start source:codebase/STACK.md -->
## Technology Stack

## Languages
- Java 17 - Main gameplay, AI, networking, config, and persistence code in `src/main/java/com/talhanation/recruits/Main.java`, `src/main/java/com/talhanation/recruits/entities/`, `src/main/java/com/talhanation/recruits/network/`, and `src/main/java/com/talhanation/recruits/world/`.
- Groovy DSL (Gradle build scripts) - Build and dependency management in `build.gradle` and `settings.gradle`.
- TOML - Forge mod metadata and generated config schemas in `src/main/resources/META-INF/mods.toml`, `src/main/java/com/talhanation/recruits/config/RecruitsClientConfig.java`, and `src/main/java/com/talhanation/recruits/config/RecruitsServerConfig.java`.
- JSON - Update metadata and game resource definitions in `update.json`, `src/main/resources/mixins.recruits.json`, and `src/main/resources/assets/recruits/**`.
## Runtime
- JVM / Java 17 via Gradle toolchains in `build.gradle`.
- Minecraft 1.20.1 + Minecraft Forge 47.4.10 in `build.gradle` and `gradle.properties`.
- Gradle via wrapper.
- Wrapper distribution: Gradle 8.8 in `gradle/wrapper/gradle-wrapper.properties`.
- Lockfile: missing.
## Frameworks
- Minecraft Forge 47.4.10 - Mod runtime, event bus, registries, config, commands, and networking in `build.gradle` and `src/main/java/com/talhanation/recruits/Main.java`.
- Sponge Mixin / MixinGradle - Runtime patching and mixin processing in `build.gradle`, `src/main/resources/mixins.recruits.json`, and `src/main/java/com/talhanation/recruits/mixin/*.java`.
- Max Henkel CoreLib 1.20.1-1.1.3 - Shared registry and message helpers used by `src/main/java/com/talhanation/recruits/Main.java` and `src/main/java/com/talhanation/recruits/network/*.java`.
- Not detected. No `src/test/`, JUnit dependency, or dedicated test runner config was found under `/home/kaiserroman/recruits`.
- ForgeGradle 6.+ - Forge development workspace and run profiles in `build.gradle`.
- Shadow 7.1.0 - Shaded artifact creation and relocation of CoreLib in `build.gradle`.
- CurseGradle 1.4.0 - CurseForge publishing plugin declared in `build.gradle`.
- Foojay Toolchain Resolver 0.7.0 - Java toolchain resolution in `settings.gradle`.
## Key Dependencies
- `net.minecraftforge:forge:1.20.1-47.4.10` - Base game modding platform and API in `build.gradle`.
- `de.maxhenkel.corelib:corelib:1.20.1-1.1.3` - Provides `CommonRegistry.registerChannel()` and message helpers used in `src/main/java/com/talhanation/recruits/Main.java`.
- `org.spongepowered:mixin:0.8.5:processor` - Required for mixin annotation processing in `build.gradle`.
- `com.electronwill.nightconfig` transitively via Forge - Used for TOML config loading in `src/main/java/com/talhanation/recruits/config/RecruitsClientConfig.java` and `src/main/java/com/talhanation/recruits/config/RecruitsServerConfig.java`.
- `curse.maven:corpse-316582:5157034` - Optional corpse integration used by `src/main/java/com/talhanation/recruits/compat/Corpse.java`.
- `curse.maven:epic-knights-armor-and-weapons-509041:5254836` - Optional combat equipment compatibility checked in `src/main/java/com/talhanation/recruits/Main.java`.
- `curse.maven:architectury-api-419699:5137938` and `curse.maven:cloth-config-348521:4973441` - UI/config support dependencies declared in `build.gradle`.
- `curse.maven:ewewukeks-musket-mod-354562:5779561` - Optional ranged weapon compatibility implemented in `src/main/java/com/talhanation/recruits/compat/MusketWeapon.java` and related files.
- `curse.maven:worldedit-225608:4586218` - Runtime dependency declared in `build.gradle`.
- `curse.maven:small-ships-450659:5566900` - Optional ship compatibility implemented in `src/main/java/com/talhanation/recruits/compat/SmallShips.java`.
## Configuration
- Environment variables are not used. No `.env*` files were detected in `/home/kaiserroman/recruits`.
- Runtime configuration uses Forge `ModConfig` registration in `src/main/java/com/talhanation/recruits/Main.java`.
- Client config is loaded from the standard Forge config directory using `FMLPaths.CONFIGDIR` in `src/main/java/com/talhanation/recruits/Main.java` and `src/main/java/com/talhanation/recruits/config/RecruitsClientConfig.java`.
- Server config schema is defined in `src/main/java/com/talhanation/recruits/config/RecruitsServerConfig.java` and registered in `src/main/java/com/talhanation/recruits/Main.java`.
- Primary build file: `build.gradle`.
- Toolchain/plugin configuration: `settings.gradle`.
- Version and mod metadata properties: `gradle.properties`.
- Mod metadata: `src/main/resources/META-INF/mods.toml`.
- Gradle wrapper bootstrap: `gradle/wrapper/gradle-wrapper.properties`.
## Platform Requirements
- Java 17.
- Gradle wrapper (`./gradlew`) with network access to Forge, Sponge, Maven Central, Max Henkel, and CurseMaven repositories configured in `build.gradle` and `settings.gradle`.
- Minecraft Forge 1.20.1 development environment from the run configurations in `build.gradle` (`client`, `server`, and `data`).
- Deployment target is a Forge mod JAR for Minecraft 1.20.1, packaged as `recruits-1.20.1` in `build.gradle`.
- Runtime distribution targets mod marketplaces referenced by `src/main/resources/META-INF/mods.toml`, `update.json`, and `README.md`.
<!-- GSD:stack-end -->

<!-- GSD:conventions-start source:CONVENTIONS.md -->
## Conventions

## Naming Patterns
- Use PascalCase class-per-file names in `src/main/java/com/talhanation/recruits/**`, such as `src/main/java/com/talhanation/recruits/Main.java`, `src/main/java/com/talhanation/recruits/entities/RecruitEntity.java`, and `src/main/java/com/talhanation/recruits/client/gui/faction/FactionInspectionScreen.java`.
- Prefix registry/bootstrap files with `Mod` in `src/main/java/com/talhanation/recruits/init/`, for example `src/main/java/com/talhanation/recruits/init/ModBlocks.java` and `src/main/java/com/talhanation/recruits/init/ModScreens.java`.
- Prefix network message classes with `Message` in `src/main/java/com/talhanation/recruits/network/`, for example `src/main/java/com/talhanation/recruits/network/MessageWriteSpawnEgg.java` and `src/main/java/com/talhanation/recruits/network/MessageToClientUpdateClaim.java`.
- Keep package names lowercase and domain-oriented, such as `com.talhanation.recruits.entities`, `com.talhanation.recruits.client.gui.faction`, and `com.talhanation.recruits.pathfinding`.
- Use lowerCamelCase method names, for example `registerMenus()` in `src/main/java/com/talhanation/recruits/init/ModScreens.java`, `fillRecruitsInfo()` in `src/main/java/com/talhanation/recruits/network/MessageWriteSpawnEgg.java`, and `awaitProcessing()` in `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathProcessor.java`.
- Name boolean predicates as verbs or state checks: `wantsToPickUp()`, `canHoldItem()`, `isLoaded()`, `isGun()`, and `canMelee()` in `src/main/java/com/talhanation/recruits/entities/RecruitEntity.java` and `src/main/java/com/talhanation/recruits/compat/MusketWeapon.java`.
- Use event-handler names that state the trigger, such as `onRegisterCommands()` in `src/main/java/com/talhanation/recruits/Main.java` and `entityRenderersEvent()` in `src/main/java/com/talhanation/recruits/client/events/ClientEvent.java`.
- Use lowerCamelCase for fields and locals, such as `modEventBus` in `src/main/java/com/talhanation/recruits/Main.java`, `workerId` in `src/main/java/com/talhanation/recruits/init/ModScreens.java`, and `buttonY` in `src/main/java/com/talhanation/recruits/client/gui/faction/FactionInspectionScreen.java`.
- Use `UPPER_SNAKE_CASE` for constants and registry singletons, such as `MOD_ID` and `LOGGER` in `src/main/java/com/talhanation/recruits/Main.java`, `BLOCKS` and `RECRUIT_BLOCK` in `src/main/java/com/talhanation/recruits/init/ModBlocks.java`, and `CLAIM_BUTTON` in `src/main/java/com/talhanation/recruits/client/gui/faction/FactionInspectionScreen.java`.
- Expect occasional inconsistent local naming in brownfield code, such as `player_uuid` in `src/main/java/com/talhanation/recruits/init/ModScreens.java`; prefer the dominant lowerCamelCase style for new code.
- Use PascalCase for classes, interfaces, nested event types, and enums, such as `RecruitEntity`, `AsyncPathProcessor`, `RecruitEvent.Hired`, and `RecruitsRoute.WaypointAction.Type` in `src/main/java/com/talhanation/recruits/entities/RecruitEntity.java`, `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathProcessor.java`, `src/main/java/com/talhanation/recruits/events/RecruitEvent.java`, and `src/main/java/com/talhanation/recruits/world/RecruitsRoute.java`.
## Code Style
- No formatter configuration is detected at the project root: no `.editorconfig`, Checkstyle, Spotless, or formatter XML files are present under `/home/kaiserroman/recruits`.
- Follow the repository’s de facto Java style from `src/main/java/com/talhanation/recruits/Main.java` and `src/main/java/com/talhanation/recruits/entities/RecruitEntity.java`: 4-space indentation, opening braces on the same line, and chained calls split across lines.
- Keep short guard clauses inline when that matches nearby code, for example `if (executor == null || executor.isShutdown()) return;` in `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathProcessor.java`.
- Use multiline lambdas for Forge/Gradle registration code, as seen in `src/main/java/com/talhanation/recruits/init/ModBlocks.java` and `src/main/java/com/talhanation/recruits/init/ModScreens.java`.
- No linting tool or lint config is detected in `/home/kaiserroman/recruits`; `build.gradle` does not configure Checkstyle, Spotless, PMD, or Error Prone.
- Preserve existing style manually when editing files like `src/main/java/com/talhanation/recruits/commands/RecruitsAdminCommands.java` and `src/main/java/com/talhanation/recruits/client/gui/faction/FactionInspectionScreen.java`.
## Import Organization
- Not applicable in this Java codebase; imports use full package names such as `com.talhanation.recruits.network.*` in `src/main/java/com/talhanation/recruits/Main.java` and `net.minecraft.*` / `net.minecraftforge.*` types throughout `src/main/java/com/talhanation/recruits/**`.
## Error Handling
- Use guard clauses and sentinel returns (`null`, `false`, `0`) instead of custom exception hierarchies. Examples: `return null;` in `src/main/java/com/talhanation/recruits/init/ModScreens.java`, `return false;` in `src/main/java/com/talhanation/recruits/compat/MusketWeapon.java`, and `return 0;` in `src/main/java/com/talhanation/recruits/commands/RecruitsAdminCommands.java`.
- Wrap Forge GUI/container construction in `try/catch` and log before returning `null`, as done repeatedly in `src/main/java/com/talhanation/recruits/init/ModScreens.java`.
- For compatibility/reflection code, catch specific reflection exceptions and degrade gracefully, as in `src/main/java/com/talhanation/recruits/compat/MusketWeapon.java`.
- Throw `IllegalArgumentException` for invalid enum/index state instead of silent coercion in logic-heavy entity classes, as indicated by matches in `src/main/java/com/talhanation/recruits/entities/ScoutEntity.java`, `src/main/java/com/talhanation/recruits/entities/MessengerEntity.java`, and `src/main/java/com/talhanation/recruits/entities/AbstractLeaderEntity.java`.
- Swallow exceptions only in narrowly defensive code paths, for example `catch (Exception ignored) {}` in `src/main/java/com/talhanation/recruits/world/RecruitsRoute.java`.
## Logging
- Use the shared mod logger `Main.LOGGER` for cross-cutting code in `src/main/java/com/talhanation/recruits/Main.java`, `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathProcessor.java`, and `src/main/java/com/talhanation/recruits/compat/MusketWeapon.java`.
- Use file-local `logger` instances in registry/bootstrap classes such as `src/main/java/com/talhanation/recruits/init/ModScreens.java` and `src/main/java/com/talhanation/recruits/init/ModPois.java`.
- Log operational milestones with `info`, degraded behavior with `warn`, and failures with `error`; examples include `logger.info("MenuScreens registered")` in `src/main/java/com/talhanation/recruits/init/ModScreens.java` and `Main.LOGGER.warn("AsyncPathProcessor shutdown interrupted")` in `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathProcessor.java`.
- Match the existing string-heavy style when editing older files, but prefer parameterized logging where a file already uses it, such as `Main.LOGGER.error("No node evaluator generator present for Mob {}", p_77429_);` in `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathfinder.java`.
## Comments
- Use inline comments to label sections or explain Forge-specific intent, such as `//ATTRIBUTES` in `src/main/java/com/talhanation/recruits/entities/RecruitEntity.java`, `//COMPANIONS` in `src/main/java/com/talhanation/recruits/client/events/ClientEvent.java`, and `//TeamCommand` in `src/main/java/com/talhanation/recruits/commands/RecruitsAdminCommands.java`.
- Keep existing TODO comments in place when they document gaps, for example in `src/main/java/com/talhanation/recruits/client/gui/faction/FactionInspectionScreen.java` and `src/main/java/com/talhanation/recruits/Main.java`.
- Not applicable.
- JavaDoc is used selectively for API-like event classes and helper methods. Follow the style in `src/main/java/com/talhanation/recruits/events/RecruitEvent.java` and the method comment above `registerMenu()` in `src/main/java/com/talhanation/recruits/init/ModScreens.java` when documenting extension points or non-obvious behavior.
## Function Design
- Expect large orchestration methods in bootstrap and command files, such as `setup()` in `src/main/java/com/talhanation/recruits/Main.java` and `register()` in `src/main/java/com/talhanation/recruits/commands/RecruitsAdminCommands.java`.
- Prefer small focused methods for entity overrides and utilities when extending existing types, as in `src/main/java/com/talhanation/recruits/entities/RecruitEntity.java` and `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathProcessor.java`.
- Pass explicit Minecraft/Forge types rather than wrapper DTOs, for example `FriendlyByteBuf`, `NetworkEvent.Context`, `ServerPlayer`, and `Player` in `src/main/java/com/talhanation/recruits/network/MessageWriteSpawnEgg.java` and `src/main/java/com/talhanation/recruits/init/ModScreens.java`.
- Use nullable annotations where null is part of the contract, as shown by `@Nullable` usage in `src/main/java/com/talhanation/recruits/events/RecruitEvent.java`, `src/main/java/com/talhanation/recruits/world/RecruitsRoute.java`, and `src/main/java/com/talhanation/recruits/client/events/ClientEvent.java`.
- Return concrete domain objects when lookup succeeds and `null` when the surrounding Forge API expects nullable construction or lookup, as in `getRecruitByUUID()` in `src/main/java/com/talhanation/recruits/init/ModScreens.java` and `getByResourceLocation()` in `src/main/java/com/talhanation/recruits/world/RecruitsHireTradesRegistry.java`.
- Use fluent/chained builders for registration and config assembly, as in `src/main/java/com/talhanation/recruits/init/ModBlocks.java` and `src/main/java/com/talhanation/recruits/config/RecruitsServerConfig.java`.
## Module Design
- Use one public top-level class per file under `src/main/java/com/talhanation/recruits/**`.
- Favor static registries and utility-style modules for global game hooks, such as `src/main/java/com/talhanation/recruits/init/ModBlocks.java`, `src/main/java/com/talhanation/recruits/world/RecruitsHireTradesRegistry.java`, and `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathProcessor.java`.
- Use nested public event types to model related extension points in a single file, as in `src/main/java/com/talhanation/recruits/events/RecruitEvent.java`.
- Not used. There are no Java barrel/export aggregator files beyond normal package organization and wildcard imports such as `com.talhanation.recruits.init.*` in `src/main/java/com/talhanation/recruits/Main.java`.
<!-- GSD:conventions-end -->

<!-- GSD:architecture-start source:ARCHITECTURE.md -->
## Architecture

## Pattern Overview
- `src/main/java/com/talhanation/recruits/Main.java` acts as the bootstrapper, wiring Forge lifecycle listeners, registries, configs, commands, and the global `SimpleChannel`.
- Gameplay logic is organized by feature domains rather than strict technical layers: recruits, factions, claims, commands, networking, and client UI each live in dedicated packages under `src/main/java/com/talhanation/recruits/`.
- Server state is persisted with Minecraft `SavedData` classes in `src/main/java/com/talhanation/recruits/world/`, while client state is mirrored into static caches in `src/main/java/com/talhanation/recruits/client/ClientManager.java`.
## Layers
- Purpose: Start the mod, register Forge listeners, configs, registries, menus, and network messages.
- Location: `src/main/java/com/talhanation/recruits/Main.java`, `src/main/java/com/talhanation/recruits/init/`
- Contains: mod entrypoint, deferred registers, menu type registration, keybinding registration.
- Depends on: Forge lifecycle APIs, config classes, `de.maxhenkel.corelib.CommonRegistry`, feature event handlers.
- Used by: Forge mod loader and all runtime systems.
- Purpose: React to world, server, player, combat, and tick events and dispatch feature behavior.
- Location: `src/main/java/com/talhanation/recruits/RecruitEvents.java`, `src/main/java/com/talhanation/recruits/FactionEvents.java`, `src/main/java/com/talhanation/recruits/ClaimEvents.java`, `src/main/java/com/talhanation/recruits/CommandEvents.java`, plus other root event handlers like `src/main/java/com/talhanation/recruits/VillagerEvents.java` and `src/main/java/com/talhanation/recruits/PillagerEvents.java`
- Contains: server startup/shutdown hooks, join/leave handling, tick loops, GUI opening, command execution helpers, feature broadcasts.
- Depends on: world managers, entity classes, network messages, config values.
- Used by: Forge event bus registrations in `src/main/java/com/talhanation/recruits/Main.java`.
- Purpose: Implement recruit units, companions, patrol leaders, combat behavior, navigation, and per-tick gameplay state.
- Location: `src/main/java/com/talhanation/recruits/entities/` and nested packages like `entities/ai/`, `entities/ai/controller/`, `entities/ai/navigation/`
- Contains: base classes such as `src/main/java/com/talhanation/recruits/entities/AbstractRecruitEntity.java`, concrete unit types like `RecruitEntity`, `BowmanEntity`, `CommanderEntity`, and goal/controller classes.
- Depends on: config, networking, utilities, world managers, custom pathfinding.
- Used by: entity registries, event handlers, menus, renderers, and network message handlers.
- Purpose: Store and load factions, claims, groups, diplomacy, treaties, routes, and player unit data.
- Location: `src/main/java/com/talhanation/recruits/world/`
- Contains: managers such as `RecruitsFactionManager`, `RecruitsClaimManager`, `RecruitsGroupsManager`; save data types such as `RecruitsTeamSaveData`, `RecruitsClaimSaveData`, `RecruitsGroupsSaveData`.
- Depends on: Minecraft `SavedData`, NBT serialization, network messages for client sync.
- Used by: root event handlers and client update messages.
- Purpose: Carry client commands to the server and broadcast state/UI updates back to clients.
- Location: `src/main/java/com/talhanation/recruits/network/`
- Contains: 100+ `Message*` classes implementing `de.maxhenkel.corelib.net.Message`, for example `MessageSaveTeamSettings.java` and `MessageToClientUpdateFactions.java`.
- Depends on: `Main.SIMPLE_CHANNEL`, domain models, event orchestrators, client manager.
- Used by: client screens, key handlers, entities, and server managers.
- Purpose: Render entities, overlays, and feature-specific screens and keep client-side mirrors of server state.
- Location: `src/main/java/com/talhanation/recruits/client/`
- Contains: `ClientManager.java`, `client/events/`, `client/gui/`, `client/render/`, `client/models/`.
- Depends on: network-to-client messages, menu registrations, entity registries, static client caches.
- Used by: keybindings, menu opening, renderer registration, world map, faction/group/diplomacy UIs.
- Purpose: Extend or patch base game behavior for navigation and compatibility.
- Location: `src/main/java/com/talhanation/recruits/pathfinding/`, `src/main/java/com/talhanation/recruits/mixin/`, `src/main/java/com/talhanation/recruits/compat/`
- Contains: async pathfinding pipeline, Sponge mixins declared in `src/main/resources/mixins.recruits.json`, optional compatibility wrappers for mods like Small Ships and musket weapons.
- Depends on: Minecraft internals, Forge, mixin runtime, optional mod APIs.
- Used by: entities, AI controllers, and feature logic that needs compatibility-specific behavior.
## Data Flow
- Server authority is the default. Long-lived state lives in manager maps plus `SavedData` classes under `src/main/java/com/talhanation/recruits/world/`.
- Client state is cache-based. `src/main/java/com/talhanation/recruits/client/ClientManager.java` stores factions, claims, groups, diplomacy, routes, and config mirrors as mutable static fields.
- Entity-local runtime state is stored on the entity itself using synced data accessors, as shown in `src/main/java/com/talhanation/recruits/entities/AbstractRecruitEntity.java`.
## Key Abstractions
- Purpose: Represent all recruitable units and companions through shared base behavior.
- Examples: `src/main/java/com/talhanation/recruits/entities/AbstractInventoryEntity.java`, `AbstractOrderAbleEntity.java`, `AbstractRecruitEntity.java`, `AbstractLeaderEntity.java`, `CommanderEntity.java`, `CaptainEntity.java`
- Pattern: Deep inheritance tree with capability-style interfaces such as `ICompanion`, `IRangedRecruit`, and `IStrategicFire`.
- Purpose: Isolate collection management, persistence, and client broadcasting for feature state.
- Examples: `src/main/java/com/talhanation/recruits/world/RecruitsFactionManager.java` + `RecruitsTeamSaveData.java`, `RecruitsClaimManager.java` + `RecruitsClaimSaveData.java`
- Pattern: manager owns maps in memory; `SavedData` serializes NBT; event handler owns lifecycle.
- Purpose: Keep each networked action or sync payload isolated in its own class.
- Examples: `src/main/java/com/talhanation/recruits/network/MessageMovement.java`, `MessageSaveTeamSettings.java`, `MessageToClientUpdateClaims.java`, `MessageToClientOpenNobleTradeScreen.java`
- Pattern: one packet class per interaction, with explicit `executeServerSide` or `executeClientSide` method.
- Purpose: Bind server-side containers to client-side screens for recruit, command, faction, and promotion UIs.
- Examples: `src/main/java/com/talhanation/recruits/inventory/RecruitInventoryMenu.java` + `src/main/java/com/talhanation/recruits/client/gui/RecruitInventoryScreen.java`, `PatrolLeaderContainer.java` + `PatrolLeaderScreen.java`
- Pattern: menu types are registered in `src/main/java/com/talhanation/recruits/init/ModScreens.java`, then mapped to screen constructors during client setup.
- Purpose: Give many screens a single shared read/write location for synchronized data.
- Examples: `src/main/java/com/talhanation/recruits/client/ClientManager.java`, read by screens like `client/gui/faction/FactionInspectionScreen.java` and `client/gui/worldmap/WorldMapScreen.java`
- Pattern: mutable static collections updated by network handlers and consumed directly by screens.
- Purpose: Alter Minecraft behavior where Forge hooks are not sufficient.
- Examples: `src/main/resources/mixins.recruits.json`, `src/main/java/com/talhanation/recruits/mixin/WalkNodeEvaluatorMixin.java`
- Pattern: targeted mixins against vanilla classes to support recruit-specific navigation and behavior.
## Entry Points
- Location: `src/main/java/com/talhanation/recruits/Main.java`
- Triggers: Forge loads `@Mod(Main.MOD_ID)`.
- Responsibilities: register configs, deferred registers, event handlers, commands, network messages, client listeners, and creative tab entries.
- Location: `src/main/java/com/talhanation/recruits/RecruitEvents.java`, `FactionEvents.java`, `ClaimEvents.java`
- Triggers: Registered on `MinecraftForge.EVENT_BUS` during `Main.setup()`.
- Responsibilities: initialize world managers, load/save persistent state, tick patrols and sieges, broadcast sync state, and start/shutdown async processors.
- Location: `src/main/java/com/talhanation/recruits/client/events/ClientEvent.java`
- Triggers: `@Mod.EventBusSubscriber(..., value = Dist.CLIENT)` mod-bus events.
- Responsibilities: register entity renderers and layer definitions based on client config.
- Location: `src/main/java/com/talhanation/recruits/init/ModScreens.java`
- Triggers: `Main.clientSetup()` enqueues `ModScreens::registerMenus`.
- Responsibilities: bind `MenuType` registrations to screen constructors and create server-side containers from packet buffer payloads.
- Location: `src/main/java/com/talhanation/recruits/commands/RecruitsAdminCommands.java`, `src/main/java/com/talhanation/recruits/commands/PatrolSpawnCommand.java`
- Triggers: `RegisterCommandsEvent` in `Main.onRegisterCommands()`.
- Responsibilities: expose admin and patrol management operations through Brigadier.
- Location: `src/main/java/com/talhanation/recruits/client/gui/worldmap/WorldMapScreen.java`
- Triggers: keybinding in `src/main/java/com/talhanation/recruits/client/events/KeyEvents.java`.
- Responsibilities: display claims, routes, chunk overlays, route editing popups, and context menu actions.
## Error Handling
- `src/main/java/com/talhanation/recruits/init/ModScreens.java` wraps container and screen creation in try/catch and returns `null` on failure to avoid silent Forge UI crashes.
- Async pathfinding in `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathfinder.java` catches exceptions inside deferred work and returns `null` paths.
- Many broadcast and update paths use guard clauses (`if (player == null) return;`, `if (claim == null) return;`) as the primary defensive style, for example in `RecruitsFactionManager.java` and `RecruitsClaimManager.java`.
## Cross-Cutting Concerns
<!-- GSD:architecture-end -->

<!-- GSD:workflow-start source:GSD defaults -->
## GSD Workflow Enforcement

Before using Edit, Write, or other file-changing tools, start work through a GSD command so planning artifacts and execution context stay in sync.

Use these entry points:
- `/gsd:quick` for small fixes, doc updates, and ad-hoc tasks
- `/gsd:debug` for investigation and bug fixing
- `/gsd:execute-phase` for planned phase work

Do not make direct repo edits outside a GSD workflow unless the user explicitly asks to bypass it.
<!-- GSD:workflow-end -->



<!-- GSD:profile-start -->
## Developer Profile

> Profile not yet configured. Run `/gsd:profile-user` to generate your developer profile.
> This section is managed by `generate-claude-profile` -- do not edit manually.
<!-- GSD:profile-end -->
