# Architecture

**Analysis Date:** 2026-04-11

## Pattern Overview

**Overall:** Merged Forge mod monolith with a single root runtime entrypoint and a preserved workers subsystem.

**Key Characteristics:**
- Use `recruits/src/main/java/com/talhanation/recruits/Main.java` as the only active `@Mod` bootstrap; the merged runtime mod id is `bannermod`.
- Compose the runtime through lifecycle registrars and subsystem wrappers instead of separate Gradle subprojects; see `recruits/src/main/java/com/talhanation/recruits/init/ModLifecycleRegistrar.java` and `workers/src/main/java/com/talhanation/workers/WorkersSubsystem.java`.
- Build one runtime from multiple source trees via `build.gradle`, which merges `src/main/java`, `recruits/src/main/java`, and `workers/src/main/java` into the root `main` source set.

## Layers

**Root Build Composition:**
- Purpose: Assemble one Forge runtime and one test/gametest pipeline from merged source trees.
- Location: `build.gradle`, `settings.gradle`, `gradle.properties`
- Contains: merged source-set declarations, Forge run configs, shared resource processing, game test source set, root verification tasks.
- Depends on: `recruits/` and `workers/` source/resource trees.
- Used by: every local build, run, test, and datagen workflow.

**Runtime Bootstrap Layer:**
- Purpose: Start the mod, bind Forge lifecycle listeners, create the shared network channel, and hand off to both domain registrars.
- Location: `recruits/src/main/java/com/talhanation/recruits/Main.java`, `recruits/src/main/java/com/talhanation/recruits/init/ModLifecycleRegistrar.java`
- Contains: `@Mod` entrypoint, config registration, deferred register hookup, command registration, client setup, creative tab population.
- Depends on: Forge lifecycle APIs, `recruits/src/main/java/com/talhanation/recruits/network/RecruitsNetworkRegistrar.java`, `workers/src/main/java/com/talhanation/workers/WorkersSubsystem.java`.
- Used by: Forge mod loading.

**Recruits Domain Layer:**
- Purpose: Own the primary gameplay runtime for recruitable units, factions, claims, patrols, diplomacy, commands, and client UX.
- Location: `recruits/src/main/java/com/talhanation/recruits/entities/`, `recruits/src/main/java/com/talhanation/recruits/world/`, `recruits/src/main/java/com/talhanation/recruits/client/`, `recruits/src/main/java/com/talhanation/recruits/commands/`
- Contains: entity inheritance tree, AI, managers backed by `SavedData`, network messages, screens, overlays, renderers, and command handlers.
- Depends on: Forge/Minecraft APIs, `Main.SIMPLE_CHANNEL`, registry classes in `recruits/src/main/java/com/talhanation/recruits/init/`.
- Used by: the active `bannermod` runtime and by workers code that imports recruit systems.

**Workers Subsystem Layer:**
- Purpose: Preserve and expose worker-villager mechanics inside the merged `bannermod` runtime.
- Location: `workers/src/main/java/com/talhanation/workers/WorkersSubsystem.java`, `workers/src/main/java/com/talhanation/workers/init/WorkersLifecycleRegistrar.java`, `workers/src/main/java/com/talhanation/workers/entities/`
- Contains: subsystem composition, worker entities, work areas, profession AI goals, worker screens, worker network messages, legacy id migration helpers.
- Depends on: the shared `SimpleChannel`, recruit-side client command category APIs in `recruits/src/main/java/com/talhanation/recruits/client/events/CommandCategoryManager.java`, and recruit-side base entity/pathing classes.
- Used by: `recruits/src/main/java/com/talhanation/recruits/Main.java` through `WorkersSubsystem`.

**Persistence and Sync Layer:**
- Purpose: Keep long-lived world state on the server and mirror selected slices to clients.
- Location: `recruits/src/main/java/com/talhanation/recruits/world/`, `recruits/src/main/java/com/talhanation/recruits/client/ClientManager.java`, `workers/src/main/java/com/talhanation/workers/entities/workarea/AbstractWorkAreaEntity.java`
- Contains: `SavedData` implementations for factions/claims/groups/treaties, client cache state, and NBT-backed entity/work-area state.
- Depends on: server lifecycle events in `RecruitEvents.java`, `FactionEvents.java`, and `ClaimEvents.java`; packet handlers under `recruits/src/main/java/com/talhanation/recruits/network/` and `workers/src/main/java/com/talhanation/workers/network/`.
- Used by: commands, screens, tick handlers, and entity interactions.

**Extension and Compatibility Layer:**
- Purpose: Patch or adapt behavior that cannot be modeled cleanly through normal Forge hooks.
- Location: `recruits/src/main/java/com/talhanation/recruits/mixin/`, `recruits/src/main/resources/mixins.recruits.json`, `recruits/src/main/java/com/talhanation/recruits/compat/`, `workers/src/main/java/com/talhanation/workers/WorkersLegacyMappings.java`, `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java`
- Contains: mixins, optional compat wrappers, namespace migration helpers, registry remaps for legacy `workers` ids.
- Depends on: Sponge Mixin, Forge missing-mapping events, optional runtime mods.
- Used by: entity AI, navigation, and legacy-world/resource compatibility paths.

## Data Flow

**Merged Runtime Startup:**

1. Forge loads `recruits/src/main/java/com/talhanation/recruits/Main.java` via `@Mod(Main.MOD_ID)`.
2. `Main` creates `WorkersSubsystem`, `ModLifecycleRegistrar`, and `RecruitsNetworkRegistrar`.
3. `ModLifecycleRegistrar.registerCommon()` registers recruit registries and then delegates to `workers/src/main/java/com/talhanation/workers/WorkersSubsystem.java` for worker registries.
4. `Main.setup()` creates the shared channel with `CommonRegistry.registerChannel(...)`, registers recruit packets, then registers worker packets with an offset from `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java`.

**Server State and Client Sync:**

1. Server lifecycle handlers in `recruits/src/main/java/com/talhanation/recruits/RecruitEvents.java`, `FactionEvents.java`, and `ClaimEvents.java` load manager state from `SavedData` classes in `recruits/src/main/java/com/talhanation/recruits/world/`.
2. Gameplay changes mutate manager-owned maps or entity-owned state.
3. Managers save back through `save(server.overworld())` calls and broadcast updates through message classes in `recruits/src/main/java/com/talhanation/recruits/network/`.
4. Client handlers update static caches in `recruits/src/main/java/com/talhanation/recruits/client/ClientManager.java`, which are then consumed directly by screens and overlays.

**Worker Authoring and Execution:**

1. A player interacts with a work-area entity derived from `workers/src/main/java/com/talhanation/workers/entities/workarea/AbstractWorkAreaEntity.java`.
2. The server checks `WorkAreaAuthoringRules` in `workers/src/main/java/com/talhanation/workers/network/WorkAreaAuthoringRules.java` and sends `MessageToClientOpenWorkAreaScreen` over the shared channel.
3. Client screens under `workers/src/main/java/com/talhanation/workers/client/gui/` send targeted update packets back to the server.
4. Worker entities in `workers/src/main/java/com/talhanation/workers/entities/` consume work-area state and AI goals in `workers/src/main/java/com/talhanation/workers/entities/ai/` to perform world actions.

**Worker Structure Asset Flow:**

1. Root `processResources` in `build.gradle` copies worker structures from `workers/src/main/resources/assets/workers/structures` into merged assets under `assets/bannermod/structures/workers`.
2. `workers/src/main/java/com/talhanation/workers/world/StructureManager.java` copies those default structures into the local game directory if `workers/scan` is empty.
3. Build-area logic reads and migrates structure NBT through `WorkersRuntime.migrateStructureNbt(...)` before placement or preview.

**State Management:**
- Use server-authoritative state.
- Persist recruit/meta systems with `SavedData` in `recruits/src/main/java/com/talhanation/recruits/world/`.
- Persist worker work areas and workers as entity NBT in files like `workers/src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java` and `workers/src/main/java/com/talhanation/workers/entities/workarea/AbstractWorkAreaEntity.java`.
- Mirror client UI state into mutable static caches in `recruits/src/main/java/com/talhanation/recruits/client/ClientManager.java`.

## Key Abstractions

**Lifecycle Registrar / Subsystem Composition:**
- Purpose: Keep the root entrypoint small while still merging two brownfield runtimes.
- Examples: `recruits/src/main/java/com/talhanation/recruits/init/ModLifecycleRegistrar.java`, `workers/src/main/java/com/talhanation/workers/init/WorkersLifecycleRegistrar.java`, `workers/src/main/java/com/talhanation/workers/WorkersSubsystem.java`
- Pattern: bootstrap composition object delegates registry and runtime listener wiring.

**Recruit World Managers:**
- Purpose: Isolate persistent world features away from individual entity classes.
- Examples: `recruits/src/main/java/com/talhanation/recruits/world/RecruitsFactionManager.java`, `RecruitsClaimManager.java`, `RecruitsGroupsManager.java`, `RecruitsTreatyManager.java`
- Pattern: manager + `SavedData` pair, loaded on server start and saved on stop/world-save.

**Worker + Work Area Pairing:**
- Purpose: Model automation as a worker entity bound to an authored area entity.
- Examples: `workers/src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java`, `workers/src/main/java/com/talhanation/workers/entities/workarea/AbstractWorkAreaEntity.java`, `workers/src/main/java/com/talhanation/workers/entities/WorkerBindingResume.java`
- Pattern: entity inheritance + UUID-based work-area binding + goal-driven execution.

**Per-Action Packet Classes:**
- Purpose: Keep network behavior explicit and side-specific.
- Examples: `recruits/src/main/java/com/talhanation/recruits/network/RecruitsNetworkRegistrar.java`, `workers/src/main/java/com/talhanation/workers/network/WorkersNetworkRegistrar.java`, message files under both `network/` packages.
- Pattern: one message class per action or sync payload, registered in a fixed order on one shared channel.

**Legacy Namespace Migration:**
- Purpose: Keep old `workers` worlds/assets functional inside the merged `bannermod` runtime.
- Examples: `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java`, `workers/src/main/java/com/talhanation/workers/WorkersLegacyMappings.java`
- Pattern: translate ids from `workers:*` to `bannermod:*` during missing-mapping and NBT/resource handling.

## Entry Points

**Merged Mod Entrypoint:**
- Location: `recruits/src/main/java/com/talhanation/recruits/Main.java`
- Triggers: Forge loads the root mod.
- Responsibilities: instantiate registrars, bind the shared network channel, register commands, client setup, and creative-tab entries.

**Recruit Runtime Events:**
- Location: `recruits/src/main/java/com/talhanation/recruits/RecruitEvents.java`, `recruits/src/main/java/com/talhanation/recruits/FactionEvents.java`, `recruits/src/main/java/com/talhanation/recruits/ClaimEvents.java`
- Triggers: `MinecraftForge.EVENT_BUS` runtime events.
- Responsibilities: load/save managers, tick patrols and sieges, broadcast sync payloads, manage async pathing lifecycle.

**Workers Runtime Registration:**
- Location: `workers/src/main/java/com/talhanation/workers/WorkersSubsystem.java`, `workers/src/main/java/com/talhanation/workers/init/WorkersLifecycleRegistrar.java`
- Triggers: delegated calls from `Main` and `ModLifecycleRegistrar`.
- Responsibilities: register worker content, client hooks, runtime listeners, and worker packets within the merged runtime.

**Client Screen Binding:**
- Location: `recruits/src/main/java/com/talhanation/recruits/init/ModScreens.java`, `workers/src/main/java/com/talhanation/workers/init/ModMenuTypes.java`
- Triggers: client setup enqueued from `Main.clientSetup()` and `WorkersSubsystem.clientSetup()`.
- Responsibilities: map menu types to screen constructors and resolve nearby entities by UUID from packet buffers.

**Command Surface:**
- Location: `recruits/src/main/java/com/talhanation/recruits/commands/RecruitsAdminCommands.java`, `recruits/src/main/java/com/talhanation/recruits/commands/PatrolSpawnCommand.java`
- Triggers: `RegisterCommandsEvent` handled by `Main.onRegisterCommands()`.
- Responsibilities: expose admin, patrol, and management flows to Brigadier.

## Error Handling

**Strategy:** Guard-clause and no-crash fallback handling for Forge runtime flows.

**Patterns:**
- Return `null` from menu factories when entity lookup or payload decoding fails; see `recruits/src/main/java/com/talhanation/recruits/init/ModScreens.java` and `workers/src/main/java/com/talhanation/workers/init/ModMenuTypes.java`.
- Catch client/screen instantiation failures and log rather than crashing the game; see `registerMenu(...)` in both menu registration files.
- Abort packet-driven worker actions when the sender or target entity is missing; the worker packet package is `workers/src/main/java/com/talhanation/workers/network/`.

## Cross-Cutting Concerns

**Logging:** Use shared mod loggers from `recruits/src/main/java/com/talhanation/recruits/Main.java` and `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java`.
**Validation:** Gate worker area editing through `workers/src/main/java/com/talhanation/workers/network/WorkAreaAuthoringRules.java`; use guard clauses in event/message handlers across both domains.
**Authentication:** Use in-game ownership/team checks rather than external auth; examples are `AbstractRecruitEntity` owner checks in `recruits/src/main/java/com/talhanation/recruits/entities/` and authoring checks in `workers/src/main/java/com/talhanation/workers/entities/workarea/AbstractWorkAreaEntity.java`.

---

*Architecture analysis: 2026-04-11*
