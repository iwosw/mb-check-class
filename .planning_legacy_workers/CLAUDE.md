<!-- GSD:project-start source:PROJECT.md -->
## Project

**Villager Workers Revival**

Villager Workers is an unreleased Minecraft Forge mod that adds worker villagers, work areas, and supporting UI so players can assign villagers to specialized jobs and automate in-world tasks. The current codebase already contains the core entity, AI, networking, GUI, and structure-handling foundations, but the mod is incomplete and still targets Minecraft 1.20.1. This project is to turn the existing implementation into a working 1.21.1 release-ready baseline for players running both client and dedicated server environments.

**Core Value:** The mod must reliably let players use the worker-villager mechanics already designed in the codebase without critical bugs or missing core loops.

### Constraints

- **Tech stack**: Stay within the existing Java/Gradle/Forge mod stack unless the 1.21.1 port requires targeted dependency or API changes — preserve the established codebase shape where possible
- **Compatibility**: Final target is Minecraft `1.21.1` with dedicated server support — this defines acceptance for the port
- **Dependency**: The mod currently has a mandatory dependency on Recruits per `src/main/resources/META-INF/mods.toml` — changes must account for its API and version availability on 1.21.1
- **Quality**: Core mechanics must be covered by automated tests where practical and by explicit gameplay verification for end-to-end flows — correctness matters more than raw speed of delivery
- **Scope control**: Existing code is the primary source of truth — avoid speculative redesigns or unrelated new systems
<!-- GSD:project-end -->

<!-- GSD:stack-start source:codebase/STACK.md -->
## Technology Stack

## Languages
- Java 17 - Main mod code under `src/main/java/com/talhanation/workers/`; Java 17 is enforced in `build.gradle` and `src/main/resources/mixins.workers.json`.
- Groovy DSL - Gradle build logic in `build.gradle` and `settings.gradle`.
- TOML - Forge mod metadata in `src/main/resources/META-INF/mods.toml`.
- JSON - Update metadata in `update.json`, mixin config in `src/main/resources/mixins.workers.json`, and game assets under `src/main/resources/assets/workers/`.
- CFG - Access transformer rules in `src/main/resources/META-INF/accesstransformer.cfg`.
## Runtime
- Minecraft Forge mod runtime for Minecraft 1.20.1 via `net.minecraftforge:forge:1.20.1-47.4.1` declared in `build.gradle`.
- Java 17 toolchain configured in `build.gradle`.
- Gradle 8.1.1 via the wrapper in `gradle/wrapper/gradle-wrapper.properties`.
- Lockfile: missing; dependency resolution is driven by `build.gradle` and the Gradle wrapper.
## Frameworks
- Minecraft Forge / JavaFML 47.x - Mod loader, registries, config, event bus, and networking; see `build.gradle`, `src/main/resources/META-INF/mods.toml`, and `src/main/java/com/talhanation/workers/WorkersMain.java`.
- Sponge Mixin / Mixingradle - Mixin processing is enabled in `build.gradle` and configured in `src/main/resources/mixins.workers.json`.
- Not detected. No JUnit, Mockito, GameTest source set, or dedicated test configuration files were found.
- ForgeGradle 6.x - Minecraft/Forge build pipeline in `build.gradle`.
- Shadow 7.1.0 - Shades and relocates CoreLib in `build.gradle`.
- CurseGradle 1.4.0 - Publishing-oriented Gradle plugin declared in `build.gradle`.
- Foojay toolchain resolver 0.5.0 - Java toolchain resolution in `settings.gradle`.
## Key Dependencies
- `net.minecraftforge:forge:1.20.1-47.4.1` - Core modding API and runtime; wired through `build.gradle` and used throughout `src/main/java/com/talhanation/workers/`.
- `de.maxhenkel.corelib:corelib:1.20.1-1.1.3` - Provides common registry, networking, containers, and screens; used in `src/main/java/com/talhanation/workers/WorkersMain.java`, `src/main/java/com/talhanation/workers/network/*.java`, and `src/main/java/com/talhanation/workers/inventory/*.java`.
- `curse.maven:recruits-523860:7374573` - Hard dependency for worker entities, pathfinding, GUI widgets, and player/group systems; required in `src/main/resources/META-INF/mods.toml` and imported across files such as `src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java` and `src/main/java/com/talhanation/workers/client/gui/WorkAreaScreen.java`.
- `org.spongepowered:mixin:0.8.5:processor` - Annotation processor configured in `build.gradle`.
- `curse.maven:architectury-api-419699:5137938` - Declared in `build.gradle`; direct source imports were not detected.
- `curse.maven:lets-do-herbal-brews-951221:6458889` - Compatibility item IDs appear in `src/main/java/com/talhanation/workers/config/WorkersServerConfig.java`.
- `curse.maven:supplementaries-412082:6615104` and `curse.maven:selene-499980:6681465` - Compatibility dependencies declared in `build.gradle`; `supplementaries:flax` is referenced in `src/main/java/com/talhanation/workers/config/WorkersServerConfig.java`.
- `curse.maven:farmers-delight-398521:6597298` - Declared in `build.gradle`; direct source imports were not detected.
- `curse.maven:worldedit-225608:4586218` - Declared in `build.gradle`; direct source imports were not detected.
- `curse.maven:debug-utils-forge-783008:5337491` - Declared in `build.gradle`; direct source imports were not detected.
## Configuration
- Runtime configuration is file-based, not environment-variable-based. Server config is registered in `src/main/java/com/talhanation/workers/WorkersMain.java` and defined with `ForgeConfigSpec` in `src/main/java/com/talhanation/workers/config/WorkersServerConfig.java`.
- Mod metadata and remote update metadata live in `src/main/resources/META-INF/mods.toml` and `update.json`.
- No `.env`, `.env.*`, `.nvmrc`, `tsconfig.json`, or `.python-version` files were detected at the repository root.
- `build.gradle` - dependencies, repositories, run configs, shading, mixins, and Java toolchain.
- `settings.gradle` - plugin repositories and Foojay toolchain resolver.
- `gradle.properties` - Minecraft/Forge version properties and template metadata values.
- `gradle/wrapper/gradle-wrapper.properties` - fixed Gradle distribution version.
- `src/main/resources/META-INF/mods.toml` - mod ID, version, update feed URL, and mandatory dependency metadata.
- `src/main/resources/META-INF/accesstransformer.cfg` - access transformation rules for the Forge runtime.
- `src/main/resources/mixins.workers.json` - mixin configuration; currently enabled with empty `mixins` and `client` arrays.
## Platform Requirements
- Java 17 and Gradle wrapper support are required to build and run dev tasks from `gradlew`.
- A Forge 1.20.1 development environment is configured in `build.gradle` with `client`, `server`, and `data` run targets.
- The mod expects the Recruits mod at runtime because `src/main/resources/META-INF/mods.toml` marks it as a mandatory dependency.
- Deployment target is a Forge mod JAR for Minecraft 1.20.1, published as `workers-1.20.1` per `build.gradle` and identified as mod `workers` in `src/main/resources/META-INF/mods.toml`.
<!-- GSD:stack-end -->

<!-- GSD:conventions-start source:CONVENTIONS.md -->
## Conventions

## Naming Patterns
- Use PascalCase for Java types, with one top-level type per file under `src/main/java/com/talhanation/workers/**`; examples: `src/main/java/com/talhanation/workers/WorkersMain.java`, `src/main/java/com/talhanation/workers/entities/FarmerEntity.java`, `src/main/java/com/talhanation/workers/client/gui/WorkAreaScreen.java`.
- Use descriptive suffixes by concern: `*Entity.java` in `src/main/java/com/talhanation/workers/entities/`, `*WorkGoal.java` in `src/main/java/com/talhanation/workers/entities/ai/`, `*Area.java` in `src/main/java/com/talhanation/workers/entities/workarea/`, `Message*.java` in `src/main/java/com/talhanation/workers/network/`, and `Mod*.java` in `src/main/java/com/talhanation/workers/init/`.
- Use lowerCamelCase for methods; examples: `setup()` and `addCreativeTabs()` in `src/main/java/com/talhanation/workers/WorkersMain.java`, `scanBreakArea()` in `src/main/java/com/talhanation/workers/entities/workarea/MiningArea.java`, and `updateWorkArea()` in `src/main/java/com/talhanation/workers/network/MessageUpdateWorkArea.java`.
- Boolean methods read as predicates; examples: `isWorking()` in `src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java`, `shouldIgnore()` in `src/main/java/com/talhanation/workers/entities/workarea/MiningArea.java`, and `isBuildingAreaAvailable()` in `src/main/java/com/talhanation/workers/entities/ai/BuilderWorkGoal.java`.
- Use lowerCamelCase for fields and locals; examples: `currentCropArea` in `src/main/java/com/talhanation/workers/entities/FarmerEntity.java`, `bufferSource` in `src/main/java/com/talhanation/workers/client/events/ScreenEvents.java`, and `playerInfo` in `src/main/java/com/talhanation/workers/client/gui/WorkAreaScreen.java`.
- Public mutable fields are common in gameplay classes and message DTOs; examples: `neededItems` in `src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java`, `uuid` in `src/main/java/com/talhanation/workers/network/MessageRotateWorkArea.java`, and `errorMessageDone` in `src/main/java/com/talhanation/workers/entities/ai/BuilderWorkGoal.java`.
- Use UPPER_SNAKE_CASE for shared constants and synched data accessors; examples: `MOD_ID` and `LOGGER` in `src/main/java/com/talhanation/workers/WorkersMain.java`, `HEIGHT_OFFSET` and `CLOSE_FLOOR` in `src/main/java/com/talhanation/workers/entities/workarea/MiningArea.java`, and many translation constants in `src/main/java/com/talhanation/workers/Translatable.java`.
- Use nested enums for worker or area state machines; examples: `BuilderWorkGoal.State` in `src/main/java/com/talhanation/workers/entities/ai/BuilderWorkGoal.java` and `MiningArea.MiningMode` in `src/main/java/com/talhanation/workers/entities/workarea/MiningArea.java`.
- Records are used sparingly for compact value carriers; example: `ScannedBlock` in `src/main/java/com/talhanation/workers/world/ScannedBlock.java`.
## Code Style
- No formatter configuration file is detected at repository root: no `/.prettierrc*`, `/prettier.config.*`, or `/biome.json`.
- No Java formatter configuration is detected: no Spotless or Checkstyle configuration is present in `/build.gradle`.
- Current style in `src/main/java/com/talhanation/workers/**` uses 4-space indentation, braces on the same line, and blank lines between logical sections; examples: `src/main/java/com/talhanation/workers/WorkersMain.java` and `src/main/java/com/talhanation/workers/entities/workarea/MiningArea.java`.
- Inline comments are used as lightweight markers rather than formal documentation; examples: `// ModSounds.SOUNDS.register(modEventBus);` in `src/main/java/com/talhanation/workers/WorkersMain.java` and `// Test the rotated area before committing` in `src/main/java/com/talhanation/workers/network/MessageRotateWorkArea.java`.
- No lint configuration is detected: no `/eslint.config.*`, `/.eslintrc*`, or Java lint plugin setup in `/build.gradle`.
- Static analysis is implicit through Java annotations and Forge APIs rather than enforced repository rules; examples: `@Nullable` and `@NotNull` in `src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java` and `src/main/java/com/talhanation/workers/inventory/MerchantAddEditTradeContainer.java`.
## Import Organization
- Not applicable. The codebase uses standard Java packages rooted at `com.talhanation.workers`; examples: `com.talhanation.workers.entities`, `com.talhanation.workers.client.gui`, and `com.talhanation.workers.network` under `src/main/java/com/talhanation/workers/**`.
## Error Handling
- Use guard clauses to exit early on invalid runtime state; examples: `if(player == null) return;` in `src/main/java/com/talhanation/workers/network/MessageRotateWorkArea.java` and `if(this.getCommandSenderWorld().isClientSide()) return;` in `src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java`.
- Prefer silent no-op fallback over throwing in network and UI flows; examples: `updateWorkArea()` in `src/main/java/com/talhanation/workers/network/MessageUpdateWorkArea.java` reverts invalid moves, and `rotate()` in `src/main/java/com/talhanation/workers/network/MessageRotateWorkArea.java` restores the original facing on overlap.
- Use try/catch around IO and screen instantiation. Failures are logged or printed and execution continues; examples: `registerMenu()` in `src/main/java/com/talhanation/workers/init/ModMenuTypes.java` and file operations in `src/main/java/com/talhanation/workers/world/StructureManager.java`.
- Throw explicit exceptions only for invalid enum mapping or impossible states; example: `MiningMode.fromIndex()` in `src/main/java/com/talhanation/workers/entities/workarea/MiningArea.java` throws `IllegalArgumentException`.
## Logging
- Reuse a shared mod logger from `src/main/java/com/talhanation/workers/WorkersMain.java` via `WorkersMain.LOGGER` or a local `LogManager.getLogger(WorkersMain.MOD_ID)`; examples: `src/main/java/com/talhanation/workers/UpdateChecker.java`, `src/main/java/com/talhanation/workers/init/ModMenuTypes.java`, `src/main/java/com/talhanation/workers/init/ModPois.java`, and `src/main/java/com/talhanation/workers/init/ModProfessions.java`.
- Use `sendSystemMessage()` for player-facing operational feedback instead of logs; examples: `src/main/java/com/talhanation/workers/entities/MerchantEntity.java`, `src/main/java/com/talhanation/workers/entities/ai/GetNeededItemsFromStorage.java`, and `src/main/java/com/talhanation/workers/entities/ai/DepositItemsToStorage.java`.
- Some IO paths still use `e.printStackTrace()` rather than structured logging; examples: `src/main/java/com/talhanation/workers/world/StructureManager.java`, `src/main/java/com/talhanation/workers/network/MessageUpdateBuildArea.java`, and `src/main/java/com/talhanation/workers/client/gui/widgets/ScrollDropDownMenuWithFolders.java`.
## Comments
- Comment edge cases, temporary workarounds, and intent around Forge behavior; examples: `// Only edit below this line...` in `/build.gradle`, `//ONLY FOR BUILDING AREA WILL REMOVE IT` in `src/main/java/com/talhanation/workers/entities/ai/BuilderWorkGoal.java`, and `// Test the rotated area before committing` in `src/main/java/com/talhanation/workers/network/MessageRotateWorkArea.java`.
- TODO comments exist but are sparse; examples: `src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java` and `src/main/java/com/talhanation/workers/Translatable.java`.
- Not applicable.
- JavaDoc usage is minimal. A small explanatory block appears above `registerMenu()` in `src/main/java/com/talhanation/workers/init/ModMenuTypes.java`, but most methods rely on naming and inline comments instead of formal JavaDoc.
## Function Design
- Small helper methods are common for serialization and getters; examples: `toBytes()`/`fromBytes()` in `src/main/java/com/talhanation/workers/network/MessageRotateWorkArea.java` and `isOre()` in `src/main/java/com/talhanation/workers/entities/workarea/MiningArea.java`.
- Long imperative methods are also common in AI and UI orchestration. Keep stateful gameplay logic in a single method when matching existing code; examples: `tick()` in `src/main/java/com/talhanation/workers/entities/ai/BuilderWorkGoal.java` and `setButtons()` in `src/main/java/com/talhanation/workers/client/gui/WorkAreaScreen.java`.
- Use concrete game objects directly instead of wrapper DTOs; examples: `BuilderWorkGoal(BuilderEntity builderEntity)` in `src/main/java/com/talhanation/workers/entities/ai/BuilderWorkGoal.java` and `getScreen(Player player)` in `src/main/java/com/talhanation/workers/entities/workarea/MiningArea.java`.
- Network message constructors accept raw serializable values and store them on public fields; example: `MessageUpdateWorkArea(UUID uuid, String name, Vec3 vec3, boolean destroy)` in `src/main/java/com/talhanation/workers/network/MessageUpdateWorkArea.java`.
- `null` is an accepted return value in several APIs. Preserve existing null-aware patterns when extending similar code; examples: `getRecruitByUUID()` in `src/main/java/com/talhanation/workers/init/ModMenuTypes.java`, `getMatchingItem()` in `src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java`, and `getAllowedItems()` / `inventoryInputHelp()` in `src/main/java/com/talhanation/workers/entities/FarmerEntity.java`.
- Methods that mutate state often return `void` or booleans for “still working / continue” control flow; examples: `mineBlocks()` in `src/main/java/com/talhanation/workers/entities/ai/BuilderWorkGoal.java` and `updateWorkArea()` in `src/main/java/com/talhanation/workers/network/MessageUpdateWorkArea.java`.
## Module Design
- Not applicable in the Java module sense. The codebase uses public classes under package directories rather than explicit module descriptors.
- Shared entry points and registries are centralized in static holder classes; examples: `src/main/java/com/talhanation/workers/WorkersMain.java`, `src/main/java/com/talhanation/workers/init/ModBlocks.java`, `src/main/java/com/talhanation/workers/init/ModItems.java`, and `src/main/java/com/talhanation/workers/init/ModMenuTypes.java`.
- Not applicable. There are no barrel exports; package organization under `src/main/java/com/talhanation/workers/**` is the main discovery mechanism.
<!-- GSD:conventions-end -->

<!-- GSD:architecture-start source:ARCHITECTURE.md -->
## Architecture

## Pattern Overview
- `WorkersMain` in `src/main/java/com/talhanation/workers/WorkersMain.java` is the composition root for mod startup, registry wiring, config registration, event listeners, and network message registration.
- Runtime behavior is centered on custom entities in `src/main/java/com/talhanation/workers/entities/` and `src/main/java/com/talhanation/workers/entities/workarea/` rather than service classes.
- Client interaction flows are split between screens in `src/main/java/com/talhanation/workers/client/gui/` and packet handlers in `src/main/java/com/talhanation/workers/network/`.
## Layers
- Purpose: Initialize the mod, register content, attach event listeners, and create the shared packet channel.
- Location: `src/main/java/com/talhanation/workers/WorkersMain.java`, `src/main/java/com/talhanation/workers/AttributeEvent.java`, `src/main/java/com/talhanation/workers/VillagerEvents.java`
- Contains: `@Mod` entrypoint, Forge event subscribers, lifecycle listeners, creative tab population, attribute registration.
- Depends on: Forge lifecycle APIs, deferred registries in `src/main/java/com/talhanation/workers/init/`, config in `src/main/java/com/talhanation/workers/config/WorkersServerConfig.java`, packet classes in `src/main/java/com/talhanation/workers/network/`.
- Used by: Forge loader and Minecraft runtime.
- Purpose: Declare all mod-owned blocks, items, POIs, professions, entity types, menus, sounds, and shortcuts.
- Location: `src/main/java/com/talhanation/workers/init/`
- Contains: Deferred registers in `ModBlocks.java`, `ModEntityTypes.java`, `ModItems.java`, `ModMenuTypes.java`, `ModPois.java`, and `ModProfessions.java`.
- Depends on: `WorkersMain.MOD_ID`, Forge registries, concrete entity/menu/item classes.
- Used by: `WorkersMain` during startup and client setup.
- Purpose: Model workers and work areas as persistent world entities with their own state, AI goals, ownership, and serialization.
- Location: `src/main/java/com/talhanation/workers/entities/` and `src/main/java/com/talhanation/workers/entities/workarea/`
- Contains: Worker base class `AbstractWorkerEntity.java`, worker specializations like `FarmerEntity.java`, and area base class `AbstractWorkAreaEntity.java` with concrete types like `BuildArea.java` and `CropArea.java`.
- Depends on: Minecraft entity APIs, goal classes in `src/main/java/com/talhanation/workers/entities/ai/`, config in `src/main/java/com/talhanation/workers/config/WorkersServerConfig.java`, and helpers in `src/main/java/com/talhanation/workers/world/`.
- Used by: Registries, renderers, packet handlers, and event listeners.
- Purpose: Encapsulate profession-specific work behavior and storage/inventory support logic.
- Location: `src/main/java/com/talhanation/workers/entities/ai/`
- Contains: Goal classes such as `FarmerWorkGoal.java`, `BuilderWorkGoal.java`, `MinerWorkGoal.java`, `DepositItemsToStorage.java`, and `GetNeededItemsFromStorage.java`.
- Depends on: Worker entities, work-area entities, pathfinding from the `recruits` dependency, and world helpers like `NeededItem.java`.
- Used by: `AbstractWorkerEntity.registerGoals()` and each concrete worker entity.
- Purpose: Bridge client screens and server-authoritative entity updates.
- Location: `src/main/java/com/talhanation/workers/network/`, `src/main/java/com/talhanation/workers/client/gui/`, `src/main/java/com/talhanation/workers/inventory/`
- Contains: Message classes such as `MessageAddWorkArea.java`, `MessageUpdateWorkArea.java`, `MessageUpdateBuildArea.java`; screens like `WorkAreaScreen.java` and `BuildAreaScreen.java`; menu/container classes for merchants.
- Depends on: Shared packet channel `WorkersMain.SIMPLE_CHANNEL`, entity lookup by UUID, Forge menu/screen APIs.
- Used by: Client button handlers, server packet execution, and entity interaction hooks.
- Purpose: Register renderers, draw overlays, and provide client-only helpers.
- Location: `src/main/java/com/talhanation/workers/client/`
- Contains: Renderer registration in `client/events/ClientEvent.java`, overlay and drag handling in `client/events/ScreenEvents.java`, renderers in `client/render/`, and faction-claim gating in `client/WorkersClientManager.java`.
- Depends on: Client-side Forge events, render APIs, and client state from the `recruits` dependency.
- Used by: `WorkersMain.clientSetup()` and Forge MOD client events.
- Purpose: Represent scanned structures, merchant trades, required items, and file-backed build templates.
- Location: `src/main/java/com/talhanation/workers/world/`
- Contains: `StructureManager.java`, `BuildBlock.java`, `BuildBlockParse.java`, `ScannedBlock.java`, `NeededItem.java`, `WorkersMerchantTrade.java`.
- Depends on: NBT APIs, resource manager access, entity/work-area classes.
- Used by: `BuildArea.java`, `BuildAreaScreen.java`, merchant UI flows, and worker inventory logic.
## Data Flow
- Long-lived world state is stored directly on entities and serialized with NBT in files like `AbstractWorkerEntity.java`, `AbstractWorkAreaEntity.java`, and `BuildArea.java`.
- Client-only transient state lives inside screen instances such as `BuildAreaScreen.java` and helper widgets under `src/main/java/com/talhanation/workers/client/gui/widgets/`.
- Cross-session configuration is defined through Forge config values in `src/main/java/com/talhanation/workers/config/WorkersServerConfig.java`.
## Key Abstractions
- Purpose: Common inventory, ownership, pickup, deposit, and work-state behavior for all worker professions.
- Examples: `src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java`, `src/main/java/com/talhanation/workers/entities/FarmerEntity.java`, `src/main/java/com/talhanation/workers/entities/MerchantEntity.java`
- Pattern: Template-method style inheritance where subclasses supply `getCurrentWorkArea()`, inventory helpers, attributes, and profession goals.
- Purpose: Shared ownership, dimensions, area math, interaction permissions, and client-screen dispatch for all placeable work zones.
- Examples: `src/main/java/com/talhanation/workers/entities/workarea/AbstractWorkAreaEntity.java`, `src/main/java/com/talhanation/workers/entities/workarea/CropArea.java`, `src/main/java/com/talhanation/workers/entities/workarea/BuildArea.java`
- Pattern: Base entity with synchronized entity data accessors and per-type `getRenderItem()`/`getScreen()` implementations.
- Purpose: Represent each client action as a dedicated network message class.
- Examples: `src/main/java/com/talhanation/workers/network/MessageAddWorkArea.java`, `src/main/java/com/talhanation/workers/network/MessageUpdateMerchantTrade.java`, `src/main/java/com/talhanation/workers/network/MessageUpdateStorageArea.java`
- Pattern: Fine-grained message classes implementing `de.maxhenkel.corelib.net.Message` with side-specific execution.
- Purpose: Centralize each Forge registry type instead of scattering registration logic across feature files.
- Examples: `src/main/java/com/talhanation/workers/init/ModEntityTypes.java`, `src/main/java/com/talhanation/workers/init/ModItems.java`, `src/main/java/com/talhanation/workers/init/ModMenuTypes.java`
- Pattern: Static `DeferredRegister` holders plus `RegistryObject` declarations.
## Entry Points
- Location: `src/main/java/com/talhanation/workers/WorkersMain.java`
- Triggers: Forge loads the `@Mod("workers")` class.
- Responsibilities: Register config, registries, event listeners, client setup hooks, creative-tab entries, and all network messages.
- Location: `src/main/java/com/talhanation/workers/AttributeEvent.java`
- Triggers: Forge MOD-bus entity attribute event.
- Responsibilities: Attach attribute sets for every custom worker entity type.
- Location: `src/main/java/com/talhanation/workers/VillagerEvents.java`
- Triggers: Server starting, player join, animal join, and player block interaction events.
- Responsibilities: Register recruit hire trades, push config to clients, alter animal temptation goals, and restrict interactions inside `MarketArea` bounds.
- Location: `src/main/java/com/talhanation/workers/client/events/ClientEvent.java`
- Triggers: Forge MOD client renderer registration event.
- Responsibilities: Bind custom renderers for work areas, workers, and the fishing bobber entity.
- Location: `src/main/java/com/talhanation/workers/client/events/ScreenEvents.java`
- Triggers: Screen drag events and world render stage events.
- Responsibilities: Drive build-preview drag behavior and draw wireframe boxes for nearby work areas.
## Error Handling
- Packet handlers typically abort when sender lookup fails or entity lookup misses, as in `MessageAddWorkArea.java` and `MessageUpdateWorkArea.java`.
- Menu creation returns `null` when packet payloads are invalid in `src/main/java/com/talhanation/workers/init/ModMenuTypes.java`.
- `ModMenuTypes.registerMenu()` wraps screen construction in a `try/catch` and logs exceptions instead of crashing.
- File and NBT operations in `StructureManager.java` catch `IOException` and either log stack traces or return `null`.
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
