# Architecture

**Analysis Date:** 2026-04-05

## Pattern Overview

**Overall:** Forge mod architecture with registry-driven bootstrap, entity-centric domain logic, and packet-mediated client/server UI updates.

**Key Characteristics:**
- `WorkersMain` in `src/main/java/com/talhanation/workers/WorkersMain.java` is the composition root for mod startup, registry wiring, config registration, event listeners, and network message registration.
- Runtime behavior is centered on custom entities in `src/main/java/com/talhanation/workers/entities/` and `src/main/java/com/talhanation/workers/entities/workarea/` rather than service classes.
- Client interaction flows are split between screens in `src/main/java/com/talhanation/workers/client/gui/` and packet handlers in `src/main/java/com/talhanation/workers/network/`.

## Layers

**Bootstrap and Forge integration:**
- Purpose: Initialize the mod, register content, attach event listeners, and create the shared packet channel.
- Location: `src/main/java/com/talhanation/workers/WorkersMain.java`, `src/main/java/com/talhanation/workers/AttributeEvent.java`, `src/main/java/com/talhanation/workers/VillagerEvents.java`
- Contains: `@Mod` entrypoint, Forge event subscribers, lifecycle listeners, creative tab population, attribute registration.
- Depends on: Forge lifecycle APIs, deferred registries in `src/main/java/com/talhanation/workers/init/`, config in `src/main/java/com/talhanation/workers/config/WorkersServerConfig.java`, packet classes in `src/main/java/com/talhanation/workers/network/`.
- Used by: Forge loader and Minecraft runtime.

**Registry layer:**
- Purpose: Declare all mod-owned blocks, items, POIs, professions, entity types, menus, sounds, and shortcuts.
- Location: `src/main/java/com/talhanation/workers/init/`
- Contains: Deferred registers in `ModBlocks.java`, `ModEntityTypes.java`, `ModItems.java`, `ModMenuTypes.java`, `ModPois.java`, and `ModProfessions.java`.
- Depends on: `WorkersMain.MOD_ID`, Forge registries, concrete entity/menu/item classes.
- Used by: `WorkersMain` during startup and client setup.

**Domain entity layer:**
- Purpose: Model workers and work areas as persistent world entities with their own state, AI goals, ownership, and serialization.
- Location: `src/main/java/com/talhanation/workers/entities/` and `src/main/java/com/talhanation/workers/entities/workarea/`
- Contains: Worker base class `AbstractWorkerEntity.java`, worker specializations like `FarmerEntity.java`, and area base class `AbstractWorkAreaEntity.java` with concrete types like `BuildArea.java` and `CropArea.java`.
- Depends on: Minecraft entity APIs, goal classes in `src/main/java/com/talhanation/workers/entities/ai/`, config in `src/main/java/com/talhanation/workers/config/WorkersServerConfig.java`, and helpers in `src/main/java/com/talhanation/workers/world/`.
- Used by: Registries, renderers, packet handlers, and event listeners.

**AI behavior layer:**
- Purpose: Encapsulate profession-specific work behavior and storage/inventory support logic.
- Location: `src/main/java/com/talhanation/workers/entities/ai/`
- Contains: Goal classes such as `FarmerWorkGoal.java`, `BuilderWorkGoal.java`, `MinerWorkGoal.java`, `DepositItemsToStorage.java`, and `GetNeededItemsFromStorage.java`.
- Depends on: Worker entities, work-area entities, pathfinding from the `recruits` dependency, and world helpers like `NeededItem.java`.
- Used by: `AbstractWorkerEntity.registerGoals()` and each concrete worker entity.

**Networking and UI orchestration layer:**
- Purpose: Bridge client screens and server-authoritative entity updates.
- Location: `src/main/java/com/talhanation/workers/network/`, `src/main/java/com/talhanation/workers/client/gui/`, `src/main/java/com/talhanation/workers/inventory/`
- Contains: Message classes such as `MessageAddWorkArea.java`, `MessageUpdateWorkArea.java`, `MessageUpdateBuildArea.java`; screens like `WorkAreaScreen.java` and `BuildAreaScreen.java`; menu/container classes for merchants.
- Depends on: Shared packet channel `WorkersMain.SIMPLE_CHANNEL`, entity lookup by UUID, Forge menu/screen APIs.
- Used by: Client button handlers, server packet execution, and entity interaction hooks.

**Client presentation layer:**
- Purpose: Register renderers, draw overlays, and provide client-only helpers.
- Location: `src/main/java/com/talhanation/workers/client/`
- Contains: Renderer registration in `client/events/ClientEvent.java`, overlay and drag handling in `client/events/ScreenEvents.java`, renderers in `client/render/`, and faction-claim gating in `client/WorkersClientManager.java`.
- Depends on: Client-side Forge events, render APIs, and client state from the `recruits` dependency.
- Used by: `WorkersMain.clientSetup()` and Forge MOD client events.

**World data and structure tooling:**
- Purpose: Represent scanned structures, merchant trades, required items, and file-backed build templates.
- Location: `src/main/java/com/talhanation/workers/world/`
- Contains: `StructureManager.java`, `BuildBlock.java`, `BuildBlockParse.java`, `ScannedBlock.java`, `NeededItem.java`, `WorkersMerchantTrade.java`.
- Depends on: NBT APIs, resource manager access, entity/work-area classes.
- Used by: `BuildArea.java`, `BuildAreaScreen.java`, merchant UI flows, and worker inventory logic.

## Data Flow

**Work area creation and editing:**

1. `WorkerCommandScreen` in `src/main/java/com/talhanation/workers/client/gui/WorkerCommandScreen.java` sends `MessageAddWorkArea` with a clicked `BlockPos` and a numeric type.
2. `MessageAddWorkArea` in `src/main/java/com/talhanation/workers/network/MessageAddWorkArea.java` instantiates the correct `AbstractWorkAreaEntity` subclass, applies owner/team/facing/default dimensions, checks overlap, and spawns the entity on the server.
3. `AbstractWorkAreaEntity.interact()` in `src/main/java/com/talhanation/workers/entities/workarea/AbstractWorkAreaEntity.java` sends `MessageToClientOpenWorkAreaScreen` back to the player when the area is interacted with.
4. `MessageToClientOpenWorkAreaScreen` in `src/main/java/com/talhanation/workers/network/MessageToClientOpenWorkAreaScreen.java` finds the nearby area by UUID and opens the concrete client screen returned by `AbstractWorkAreaEntity.getScreen()`.
5. Screen actions in `WorkAreaScreen.java` and specialized screens such as `BuildAreaScreen.java` send update packets like `MessageUpdateWorkArea`, `MessageRotateWorkArea`, or `MessageUpdateBuildArea` to mutate the authoritative server entity.

**Worker behavior loop:**

1. Worker entities such as `FarmerEntity` in `src/main/java/com/talhanation/workers/entities/FarmerEntity.java` extend `AbstractWorkerEntity` and register one profession goal plus shared storage goals.
2. `AbstractWorkerEntity` in `src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java` handles inventory pickup, needed-item tracking, deposit thresholds, and work-area distance checks.
3. Profession goals in `src/main/java/com/talhanation/workers/entities/ai/` consult worker state and the current `AbstractWorkAreaEntity` subtype to perform in-world actions.
4. Work areas persist ownership, dimensions, facing, and completion state through NBT in `AbstractWorkAreaEntity.java` and concrete subclasses like `BuildArea.java`.

**Build-template flow:**

1. `BuildAreaScreen` in `src/main/java/com/talhanation/workers/client/gui/BuildAreaScreen.java` either scans the selected world area or loads `.nbt` templates from the client-side `workers/scan` directory.
2. `StructureManager` in `src/main/java/com/talhanation/workers/world/StructureManager.java` scans blocks/entities into NBT, saves scans, loads scans, and copies bundled defaults from `src/main/resources/assets/workers/structures/`.
3. `MessageUpdateBuildArea` in `src/main/java/com/talhanation/workers/network/MessageUpdateBuildArea.java` updates the server-side `BuildArea` dimensions/template and triggers build or place actions.
4. `BuildArea` in `src/main/java/com/talhanation/workers/entities/workarea/BuildArea.java` converts stored NBT into placement/break stacks and can respawn embedded work-area entities when placing scanned structures.

**State Management:**
- Long-lived world state is stored directly on entities and serialized with NBT in files like `AbstractWorkerEntity.java`, `AbstractWorkAreaEntity.java`, and `BuildArea.java`.
- Client-only transient state lives inside screen instances such as `BuildAreaScreen.java` and helper widgets under `src/main/java/com/talhanation/workers/client/gui/widgets/`.
- Cross-session configuration is defined through Forge config values in `src/main/java/com/talhanation/workers/config/WorkersServerConfig.java`.

## Key Abstractions

**Worker base entity:**
- Purpose: Common inventory, ownership, pickup, deposit, and work-state behavior for all worker professions.
- Examples: `src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java`, `src/main/java/com/talhanation/workers/entities/FarmerEntity.java`, `src/main/java/com/talhanation/workers/entities/MerchantEntity.java`
- Pattern: Template-method style inheritance where subclasses supply `getCurrentWorkArea()`, inventory helpers, attributes, and profession goals.

**Work area base entity:**
- Purpose: Shared ownership, dimensions, area math, interaction permissions, and client-screen dispatch for all placeable work zones.
- Examples: `src/main/java/com/talhanation/workers/entities/workarea/AbstractWorkAreaEntity.java`, `src/main/java/com/talhanation/workers/entities/workarea/CropArea.java`, `src/main/java/com/talhanation/workers/entities/workarea/BuildArea.java`
- Pattern: Base entity with synchronized entity data accessors and per-type `getRenderItem()`/`getScreen()` implementations.

**Packet-per-action command model:**
- Purpose: Represent each client action as a dedicated network message class.
- Examples: `src/main/java/com/talhanation/workers/network/MessageAddWorkArea.java`, `src/main/java/com/talhanation/workers/network/MessageUpdateMerchantTrade.java`, `src/main/java/com/talhanation/workers/network/MessageUpdateStorageArea.java`
- Pattern: Fine-grained message classes implementing `de.maxhenkel.corelib.net.Message` with side-specific execution.

**Registry facade classes:**
- Purpose: Centralize each Forge registry type instead of scattering registration logic across feature files.
- Examples: `src/main/java/com/talhanation/workers/init/ModEntityTypes.java`, `src/main/java/com/talhanation/workers/init/ModItems.java`, `src/main/java/com/talhanation/workers/init/ModMenuTypes.java`
- Pattern: Static `DeferredRegister` holders plus `RegistryObject` declarations.

## Entry Points

**Mod bootstrap:**
- Location: `src/main/java/com/talhanation/workers/WorkersMain.java`
- Triggers: Forge loads the `@Mod("workers")` class.
- Responsibilities: Register config, registries, event listeners, client setup hooks, creative-tab entries, and all network messages.

**Attribute registration:**
- Location: `src/main/java/com/talhanation/workers/AttributeEvent.java`
- Triggers: Forge MOD-bus entity attribute event.
- Responsibilities: Attach attribute sets for every custom worker entity type.

**World and gameplay events:**
- Location: `src/main/java/com/talhanation/workers/VillagerEvents.java`
- Triggers: Server starting, player join, animal join, and player block interaction events.
- Responsibilities: Register recruit hire trades, push config to clients, alter animal temptation goals, and restrict interactions inside `MarketArea` bounds.

**Client renderer registration:**
- Location: `src/main/java/com/talhanation/workers/client/events/ClientEvent.java`
- Triggers: Forge MOD client renderer registration event.
- Responsibilities: Bind custom renderers for work areas, workers, and the fishing bobber entity.

**Client screen/overlay events:**
- Location: `src/main/java/com/talhanation/workers/client/events/ScreenEvents.java`
- Triggers: Screen drag events and world render stage events.
- Responsibilities: Drive build-preview drag behavior and draw wireframe boxes for nearby work areas.

## Error Handling

**Strategy:** Minimal defensive checks with early returns, null guards, and silent rejection of invalid operations.

**Patterns:**
- Packet handlers typically abort when sender lookup fails or entity lookup misses, as in `MessageAddWorkArea.java` and `MessageUpdateWorkArea.java`.
- Menu creation returns `null` when packet payloads are invalid in `src/main/java/com/talhanation/workers/init/ModMenuTypes.java`.
- `ModMenuTypes.registerMenu()` wraps screen construction in a `try/catch` and logs exceptions instead of crashing.
- File and NBT operations in `StructureManager.java` catch `IOException` and either log stack traces or return `null`.

## Cross-Cutting Concerns

**Logging:** `WorkersMain.LOGGER` exists in `src/main/java/com/talhanation/workers/WorkersMain.java`, and explicit error logging is concentrated in `src/main/java/com/talhanation/workers/init/ModMenuTypes.java`. Broader structured logging is not a major architectural layer.

**Validation:** Validation is embedded near mutations: overlap checks in `AbstractWorkAreaEntity.isAreaOverlapping()` and `MessageAddWorkArea.java`, ownership checks in `AbstractWorkAreaEntity.canPlayerSee()`, and screen-side gating through `WorkersClientManager.isInFactionClaim()`.

**Authentication:** There is no external auth system. Authorization is in-world ownership/team/admin permission logic implemented in `AbstractWorkAreaEntity.java`, `WorkAreaScreen.java`, and `VillagerEvents.java`.

---

*Architecture analysis: 2026-04-05*
