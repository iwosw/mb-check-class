# Architecture

**Analysis Date:** 2026-04-05

## Pattern Overview

**Overall:** Forge mod with event-driven gameplay orchestration around entity-heavy domain logic.

**Key Characteristics:**
- `src/main/java/com/talhanation/recruits/Main.java` acts as the bootstrapper, wiring Forge lifecycle listeners, registries, configs, commands, and the global `SimpleChannel`.
- Gameplay logic is organized by feature domains rather than strict technical layers: recruits, factions, claims, commands, networking, and client UI each live in dedicated packages under `src/main/java/com/talhanation/recruits/`.
- Server state is persisted with Minecraft `SavedData` classes in `src/main/java/com/talhanation/recruits/world/`, while client state is mirrored into static caches in `src/main/java/com/talhanation/recruits/client/ClientManager.java`.

## Layers

**Bootstrap and registration:**
- Purpose: Start the mod, register Forge listeners, configs, registries, menus, and network messages.
- Location: `src/main/java/com/talhanation/recruits/Main.java`, `src/main/java/com/talhanation/recruits/init/`
- Contains: mod entrypoint, deferred registers, menu type registration, keybinding registration.
- Depends on: Forge lifecycle APIs, config classes, `de.maxhenkel.corelib.CommonRegistry`, feature event handlers.
- Used by: Forge mod loader and all runtime systems.

**Server event orchestration:**
- Purpose: React to world, server, player, combat, and tick events and dispatch feature behavior.
- Location: `src/main/java/com/talhanation/recruits/RecruitEvents.java`, `src/main/java/com/talhanation/recruits/FactionEvents.java`, `src/main/java/com/talhanation/recruits/ClaimEvents.java`, `src/main/java/com/talhanation/recruits/CommandEvents.java`, plus other root event handlers like `src/main/java/com/talhanation/recruits/VillagerEvents.java` and `src/main/java/com/talhanation/recruits/PillagerEvents.java`
- Contains: server startup/shutdown hooks, join/leave handling, tick loops, GUI opening, command execution helpers, feature broadcasts.
- Depends on: world managers, entity classes, network messages, config values.
- Used by: Forge event bus registrations in `src/main/java/com/talhanation/recruits/Main.java`.

**Domain entities and AI:**
- Purpose: Implement recruit units, companions, patrol leaders, combat behavior, navigation, and per-tick gameplay state.
- Location: `src/main/java/com/talhanation/recruits/entities/` and nested packages like `entities/ai/`, `entities/ai/controller/`, `entities/ai/navigation/`
- Contains: base classes such as `src/main/java/com/talhanation/recruits/entities/AbstractRecruitEntity.java`, concrete unit types like `RecruitEntity`, `BowmanEntity`, `CommanderEntity`, and goal/controller classes.
- Depends on: config, networking, utilities, world managers, custom pathfinding.
- Used by: entity registries, event handlers, menus, renderers, and network message handlers.

**Persistent world state:**
- Purpose: Store and load factions, claims, groups, diplomacy, treaties, routes, and player unit data.
- Location: `src/main/java/com/talhanation/recruits/world/`
- Contains: managers such as `RecruitsFactionManager`, `RecruitsClaimManager`, `RecruitsGroupsManager`; save data types such as `RecruitsTeamSaveData`, `RecruitsClaimSaveData`, `RecruitsGroupsSaveData`.
- Depends on: Minecraft `SavedData`, NBT serialization, network messages for client sync.
- Used by: root event handlers and client update messages.

**Network transport:**
- Purpose: Carry client commands to the server and broadcast state/UI updates back to clients.
- Location: `src/main/java/com/talhanation/recruits/network/`
- Contains: 100+ `Message*` classes implementing `de.maxhenkel.corelib.net.Message`, for example `MessageSaveTeamSettings.java` and `MessageToClientUpdateFactions.java`.
- Depends on: `Main.SIMPLE_CHANNEL`, domain models, event orchestrators, client manager.
- Used by: client screens, key handlers, entities, and server managers.

**Client presentation:**
- Purpose: Render entities, overlays, and feature-specific screens and keep client-side mirrors of server state.
- Location: `src/main/java/com/talhanation/recruits/client/`
- Contains: `ClientManager.java`, `client/events/`, `client/gui/`, `client/render/`, `client/models/`.
- Depends on: network-to-client messages, menu registrations, entity registries, static client caches.
- Used by: keybindings, menu opening, renderer registration, world map, faction/group/diplomacy UIs.

**Platform adaptation:**
- Purpose: Extend or patch base game behavior for navigation and compatibility.
- Location: `src/main/java/com/talhanation/recruits/pathfinding/`, `src/main/java/com/talhanation/recruits/mixin/`, `src/main/java/com/talhanation/recruits/compat/`
- Contains: async pathfinding pipeline, Sponge mixins declared in `src/main/resources/mixins.recruits.json`, optional compatibility wrappers for mods like Small Ships and musket weapons.
- Depends on: Minecraft internals, Forge, mixin runtime, optional mod APIs.
- Used by: entities, AI controllers, and feature logic that needs compatibility-specific behavior.

## Data Flow

**Gameplay command flow:**

1. Client input starts in `src/main/java/com/talhanation/recruits/client/events/KeyEvents.java` or in a screen under `src/main/java/com/talhanation/recruits/client/gui/`.
2. The client sends a `Message*` packet through `Main.SIMPLE_CHANNEL`, for example `src/main/java/com/talhanation/recruits/network/MessageSaveTeamSettings.java`.
3. The server-side message handler calls a feature coordinator such as `src/main/java/com/talhanation/recruits/FactionEvents.java` or `src/main/java/com/talhanation/recruits/CommandEvents.java`.
4. Coordinators update entities or persistent managers in `src/main/java/com/talhanation/recruits/world/`.
5. Managers broadcast fresh state with `MessageToClient*` packets such as `src/main/java/com/talhanation/recruits/network/MessageToClientUpdateFactions.java`.
6. Client handlers update static caches in `src/main/java/com/talhanation/recruits/client/ClientManager.java`, and screens read those caches for rendering.

**Persistent state flow:**

1. Startup hooks in `src/main/java/com/talhanation/recruits/RecruitEvents.java`, `FactionEvents.java`, and `ClaimEvents.java` instantiate managers.
2. Managers load `SavedData` objects such as `src/main/java/com/talhanation/recruits/world/RecruitsTeamSaveData.java` from `server.overworld()`.
3. Runtime logic mutates manager-owned domain objects like `RecruitsFaction`, `RecruitsClaim`, and `RecruitsGroup`.
4. Save hooks on world save and server stop persist the current manager maps back through `SavedData#setDirty()`.

**Entity AI flow:**

1. Forge registers entity types in `src/main/java/com/talhanation/recruits/init/ModEntityTypes.java`.
2. Concrete entities inherit shared state and tick behavior from `src/main/java/com/talhanation/recruits/entities/AbstractRecruitEntity.java` or leader/inventory base classes.
3. Goal classes under `src/main/java/com/talhanation/recruits/entities/ai/` and controllers under `entities/ai/controller/` mutate follow, attack, patrol, and formation state.
4. Navigation delegates to custom classes such as `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathfinder.java` and `entities/ai/navigation/RecruitPathNavigation.java`.

**State Management:**
- Server authority is the default. Long-lived state lives in manager maps plus `SavedData` classes under `src/main/java/com/talhanation/recruits/world/`.
- Client state is cache-based. `src/main/java/com/talhanation/recruits/client/ClientManager.java` stores factions, claims, groups, diplomacy, routes, and config mirrors as mutable static fields.
- Entity-local runtime state is stored on the entity itself using synced data accessors, as shown in `src/main/java/com/talhanation/recruits/entities/AbstractRecruitEntity.java`.

## Key Abstractions

**Recruit entity hierarchy:**
- Purpose: Represent all recruitable units and companions through shared base behavior.
- Examples: `src/main/java/com/talhanation/recruits/entities/AbstractInventoryEntity.java`, `AbstractOrderAbleEntity.java`, `AbstractRecruitEntity.java`, `AbstractLeaderEntity.java`, `CommanderEntity.java`, `CaptainEntity.java`
- Pattern: Deep inheritance tree with capability-style interfaces such as `ICompanion`, `IRangedRecruit`, and `IStrategicFire`.

**World manager + save pair:**
- Purpose: Isolate collection management, persistence, and client broadcasting for feature state.
- Examples: `src/main/java/com/talhanation/recruits/world/RecruitsFactionManager.java` + `RecruitsTeamSaveData.java`, `RecruitsClaimManager.java` + `RecruitsClaimSaveData.java`
- Pattern: manager owns maps in memory; `SavedData` serializes NBT; event handler owns lifecycle.

**Message-per-action transport:**
- Purpose: Keep each networked action or sync payload isolated in its own class.
- Examples: `src/main/java/com/talhanation/recruits/network/MessageMovement.java`, `MessageSaveTeamSettings.java`, `MessageToClientUpdateClaims.java`, `MessageToClientOpenNobleTradeScreen.java`
- Pattern: one packet class per interaction, with explicit `executeServerSide` or `executeClientSide` method.

**Screen + menu pairing:**
- Purpose: Bind server-side containers to client-side screens for recruit, command, faction, and promotion UIs.
- Examples: `src/main/java/com/talhanation/recruits/inventory/RecruitInventoryMenu.java` + `src/main/java/com/talhanation/recruits/client/gui/RecruitInventoryScreen.java`, `PatrolLeaderContainer.java` + `PatrolLeaderScreen.java`
- Pattern: menu types are registered in `src/main/java/com/talhanation/recruits/init/ModScreens.java`, then mapped to screen constructors during client setup.

**Client cache facade:**
- Purpose: Give many screens a single shared read/write location for synchronized data.
- Examples: `src/main/java/com/talhanation/recruits/client/ClientManager.java`, read by screens like `client/gui/faction/FactionInspectionScreen.java` and `client/gui/worldmap/WorldMapScreen.java`
- Pattern: mutable static collections updated by network handlers and consumed directly by screens.

**Mixin-based engine patches:**
- Purpose: Alter Minecraft behavior where Forge hooks are not sufficient.
- Examples: `src/main/resources/mixins.recruits.json`, `src/main/java/com/talhanation/recruits/mixin/WalkNodeEvaluatorMixin.java`
- Pattern: targeted mixins against vanilla classes to support recruit-specific navigation and behavior.

## Entry Points

**Mod bootstrap:**
- Location: `src/main/java/com/talhanation/recruits/Main.java`
- Triggers: Forge loads `@Mod(Main.MOD_ID)`.
- Responsibilities: register configs, deferred registers, event handlers, commands, network messages, client listeners, and creative tab entries.

**Server lifecycle feature bootstrap:**
- Location: `src/main/java/com/talhanation/recruits/RecruitEvents.java`, `FactionEvents.java`, `ClaimEvents.java`
- Triggers: Registered on `MinecraftForge.EVENT_BUS` during `Main.setup()`.
- Responsibilities: initialize world managers, load/save persistent state, tick patrols and sieges, broadcast sync state, and start/shutdown async processors.

**Client renderer bootstrap:**
- Location: `src/main/java/com/talhanation/recruits/client/events/ClientEvent.java`
- Triggers: `@Mod.EventBusSubscriber(..., value = Dist.CLIENT)` mod-bus events.
- Responsibilities: register entity renderers and layer definitions based on client config.

**Client menu bootstrap:**
- Location: `src/main/java/com/talhanation/recruits/init/ModScreens.java`
- Triggers: `Main.clientSetup()` enqueues `ModScreens::registerMenus`.
- Responsibilities: bind `MenuType` registrations to screen constructors and create server-side containers from packet buffer payloads.

**Commands:**
- Location: `src/main/java/com/talhanation/recruits/commands/RecruitsAdminCommands.java`, `src/main/java/com/talhanation/recruits/commands/PatrolSpawnCommand.java`
- Triggers: `RegisterCommandsEvent` in `Main.onRegisterCommands()`.
- Responsibilities: expose admin and patrol management operations through Brigadier.

**World map UI:**
- Location: `src/main/java/com/talhanation/recruits/client/gui/worldmap/WorldMapScreen.java`
- Triggers: keybinding in `src/main/java/com/talhanation/recruits/client/events/KeyEvents.java`.
- Responsibilities: display claims, routes, chunk overlays, route editing popups, and context menu actions.

## Error Handling

**Strategy:** Local try/catch around Forge and screen/container boundaries, with log-and-return fallbacks instead of centralized exception handling.

**Patterns:**
- `src/main/java/com/talhanation/recruits/init/ModScreens.java` wraps container and screen creation in try/catch and returns `null` on failure to avoid silent Forge UI crashes.
- Async pathfinding in `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathfinder.java` catches exceptions inside deferred work and returns `null` paths.
- Many broadcast and update paths use guard clauses (`if (player == null) return;`, `if (claim == null) return;`) as the primary defensive style, for example in `RecruitsFactionManager.java` and `RecruitsClaimManager.java`.

## Cross-Cutting Concerns

**Logging:** `src/main/java/com/talhanation/recruits/Main.java` exposes `LOGGER`, and classes like `src/main/java/com/talhanation/recruits/init/ModScreens.java` and `src/main/java/com/talhanation/recruits/FactionEvents.java` log setup and notable state changes.

**Validation:** Validation is feature-local and imperative. Examples include team creation checks in `src/main/java/com/talhanation/recruits/FactionEvents.java`, entity lookup guards in `ModScreens.java`, and claim existence checks in `RecruitsClaimManager.java`.

**Authentication:** There is no external auth layer. Authorization uses Minecraft server primitives: `ServerPlayer`, scoreboard team membership, permission level checks in `src/main/java/com/talhanation/recruits/commands/RecruitsAdminCommands.java`, and ownership/team checks inside entities and event handlers.

---

*Architecture analysis: 2026-04-05*
