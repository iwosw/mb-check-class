# Architecture Patterns

**Domain:** Brownfield Minecraft Forge NPC-army mod (Villager Recruits)
**Researched:** 2026-04-05

## Recommended Architecture

Reshape the mod into a **hexagonal-ish gameplay core with thin Forge adapters**, not a full rewrite. The goal is to move decision-heavy logic for commands, formations, faction state, persistence rules, and battle outcomes out of Forge event handlers, packet classes, and giant entity classes, while keeping entity rendering, registration, and lifecycle on the Forge side.

Recommended target structure:

```text
Forge bootstrap / registration
  -> feature adapters (events, packets, commands, menus, screens)
    -> application services / use-case handlers
      -> pure domain model + rules
      -> ports (persistence, entity access, clock, randomness, path jobs, sync)
        -> Forge implementations
```

That gives three benefits immediately:

1. **Stabilization:** logic becomes testable without booting Minecraft.
2. **GameTest focus:** GameTests can verify only integration boundaries instead of every rule branch.
3. **1.21.1 migration prep:** version-sensitive code is concentrated in adapters instead of smeared through gameplay logic.

## Component Boundaries

| Component | Responsibility | Communicates With |
|-----------|---------------|-------------------|
| Bootstrap & registration | Mod entrypoint, registries, config registration, packet registration, event-bus wiring | Forge APIs, adapter factories |
| Server adapters | Forge event listeners, Brigadier commands, packet handlers, menu openers; translate runtime events into use-case calls | Application services, sync gateway, persistence gateway |
| Application services | Orchestrate gameplay actions like hire, command squad, update formation, resolve diplomacy, save route, pay upkeep | Domain model, ports/gateways |
| Domain model | Factions, claims, groups, routes, recruit orders, battle state, formation rules, ownership rules, validation | Application services only |
| Entity façade layer | Narrow interface over `AbstractRecruitEntity` and related entities so services do not depend on full entity classes | Application services, Forge entity implementations |
| Persistence gateway | Load/save world state and snapshots; map domain objects to `SavedData`/NBT | Application services, Forge `SavedData` implementations |
| Network/sync gateway | Declare sync topics and command DTOs; map domain events to packets | Server adapters, client adapters |
| Client read model | Immutable-ish client state used by screens/overlays instead of mutable global bag | Client packet handlers, screens |
| AI/navigation executor | Tick-triggered execution of combat/formation/path jobs; isolate async pathing and mixin-dependent behavior | Entity façade, application services, pathfinding adapters |
| Platform compatibility layer | Optional mod compat, mixins, access transformers, vanilla-sensitive hooks | Bootstrap, entity/navigation adapters |

## Data Flow

### Command / UI flow

**Direction:** client input -> packet/menu adapter -> server use case -> domain mutation -> persistence/sync -> client read model

1. Client keybinding or screen creates a small command DTO.
2. Packet/menu adapter validates sender and loaded-chunk/entity preconditions.
3. Adapter calls one application service.
4. Service mutates domain state and/or issues entity façade commands.
5. Persistence gateway marks affected save state dirty.
6. Sync gateway emits targeted updates.
7. Client packet handlers update client read models.
8. Screens render from read model only.

### Server simulation flow

**Direction:** Forge tick event -> scheduler/executor -> use case / entity façade -> domain event -> sync/persist

1. Forge server tick invokes a small feature scheduler.
2. Scheduler selects work: upkeep, patrol updates, diplomacy timers, formation refresh, target search requests.
3. Executor runs logic through services or entity façades.
4. Results become domain events or state changes.
5. Persistence/sync adapters flush only changed aggregates.

### Persistence flow

**Direction:** services own mutation; `SavedData` only serializes snapshots

1. Managers/loaders rehydrate domain aggregates from `SavedData` at world start.
2. Runtime code mutates aggregate objects through services, not directly from screens/packets/entities.
3. Persistence gateway serializes aggregate snapshots back to `SavedData`.
4. `SavedData#setDirty()` is called only from the gateway.

## Ownership Concerns

| Concern | Recommended Owner | Why |
|--------|--------------------|-----|
| Faction / claim / group truth | Server-side domain aggregates | Prevent client cache drift and direct packet-side mutation |
| Recruit transient combat state | Entity façade + entity internals | Remains close to ticking simulation |
| Recruit command intent | Domain command/order objects | Makes commands testable without entity ticks |
| Persistence serialization | Persistence gateway | Keeps NBT churn away from gameplay rules |
| Client display state | Client read model | Replaces `ClientManager` static sprawl |
| Packet encoding | Network gateway/adapters | Prevents packet classes from becoming business logic containers |
| Version quirks / optional mod logic | Platform compatibility layer | Localizes migration breakage |

## Patterns to Follow

### Pattern 1: Use-case handler per gameplay action
**What:** Replace packet/event classes that directly mutate world state with explicit handlers.
**When:** For command, faction, diplomacy, formation, route, and upkeep actions.
**Example:**
```typescript
interface CommandSquadUseCase {
  execute(CommandSquadRequest request): CommandSquadResult;
}

interface RecruitUnitPort {
  setFollowState(UUID recruitId, FollowState state): void;
  setHoldPosition(UUID recruitId, BlockPos pos): void;
}
```

### Pattern 2: Aggregate + repository/gateway for persistent features
**What:** Treat factions, claims, groups, treaties, and routes as aggregate roots with one persistence adapter each.
**When:** Anywhere `SavedData` currently mixes storage, config application, lookup helpers, and broadcasting.
**Example:**
```typescript
interface FactionRepository {
  FactionState load(ServerLevelRef level);
  void save(ServerLevelRef level, FactionState state);
}
```

### Pattern 3: Entity façade, not direct entity reach-through
**What:** Wrap `AbstractRecruitEntity` behavior behind narrow interfaces.
**When:** Before writing tests for combat, follow/hold/mount logic, formations, and payment behavior.
**Example:**
```typescript
interface RecruitUnit {
  UUID id();
  UUID ownerId();
  boolean isAlive();
  void assignGroup(@Nullable UUID groupId);
  void applyOrder(RecruitOrder order);
}
```

### Pattern 4: Client read model with reducers
**What:** Replace mutable public statics in `ClientManager` with feature-scoped state objects updated by packet handlers.
**When:** For factions, claims, treaties, routes, leader screens, and world map data.
**Why:** This is the cheapest major win for determinism and UI testability.

### Pattern 5: Version seam packages
**What:** Create explicit packages such as `platform/forge120`, `platform/shared`, and later `platform/forge121` or equivalent adapter namespaces.
**When:** Before the migration branch starts.
**Why:** You want a known list of porting files, not a repo-wide scavenger hunt.

## Anti-Patterns to Avoid

### Anti-Pattern 1: Packet-per-action with embedded business logic
**What:** `Message*` classes decoding, validating, mutating state, and broadcasting in one place.
**Why bad:** Hard to unit test, duplicates validation, and increases migration surface.
**Instead:** Packet -> DTO -> use-case handler -> sync event.

### Anti-Pattern 2: Static client state as shared mutable source of truth
**What:** `ClientManager` as a global writable bag.
**Why bad:** Easy stale-state bugs, hidden dependencies, side-only fragility.
**Instead:** Feature read models with explicit update methods and reset hooks.

### Anti-Pattern 3: Entity god class
**What:** `AbstractRecruitEntity` mixes synced data, AI, payment, combat, targeting, UI opening, inventory, routing, and team/group updates.
**Why bad:** Tests are expensive, bugs hide in tick interactions, port risk is extreme.
**Instead:** Peel out command state, combat policy, upkeep policy, and formation policy first.

### Anti-Pattern 4: Save manager that also broadcasts and enforces gameplay rules
**What:** Current managers load/save/configure/broadcast/validate in one class.
**Why bad:** Persistence changes can break gameplay and vice versa.
**Instead:** split into aggregate state + service + serializer + sync publisher.

## High-Risk Version-Sensitive Areas to Isolate First

### 1. Navigation, pathfinding, and mixins — **highest risk**
`AsyncPathfinder`, custom navigation, and mixins against vanilla classes are the first isolation target. These depend on unstable internals and are the most likely 1.21.1 breakpoints.

### 2. Rendering and client-only classes
Minecraft 1.21 changed significant rendering APIs; keep renderers, models, overlays, and client event hooks out of gameplay logic. Any leak from screens/renderers into domain code increases migration cost. Source confidence: **MEDIUM/HIGH** from official Forge docs plus the 1.20.6 -> 1.21 primer.

### 3. Packet registration and network transport
Forge still documents `SimpleChannel`, but recommends a dedicated packet handler class. The current channel is built inside `Main` via CoreLib helpers and 100+ message classes are registered from a monolithic list. Isolate protocol mapping from gameplay logic now. Source confidence: **HIGH** for dedicated network handler recommendation from Forge docs.

### 4. Resource and registry-facing code
Registration itself is stable conceptually, but resource naming and vanilla registry details shifted in 1.21 (for example `ResourceLocation` construction and some datapack folder names in the primer). Keep registry/resource creation in dedicated adapter packages. Source confidence: **MEDIUM** because porting primer is not Forge docs, but it is linked from official Forge porting docs.

### 5. Entity internals and synced data
Large recruit classes depend on vanilla entity methods, attributes, damage/combat APIs, and synced data accessors. Isolate decision logic from direct calls so that API signature churn stays in façade implementations.

### 6. Optional mod compatibility
`smallships`, musket, corpse, armor/weapons compat should sit behind tiny strategy interfaces. Optional integrations are frequent migration tail-risk multipliers.

## Suggested Build Order

This is the safest decomposition order for a brownfield Forge mod.

### Step 1: Establish test seams before moving logic
- Add unit test source set and Forge GameTests.
- Start with snapshot/contract tests around current faction, claims, group, command, and recruit behaviors.
- Add harnesses for packet handling, `SavedData` round-trips, and command use cases.

### Step 2: Extract persistent feature cores
- Refactor factions, claims, groups, treaties, and routes first.
- These have clearer boundaries than combat AI and give immediate persistence tests.
- End state: services own rule changes; serializers own NBT.

### Step 3: Replace direct packet logic with handlers
- Keep packet classes thin.
- Introduce one package of command/query DTOs and one package of handlers.
- This gives the largest stability gain for networking-heavy features without touching rendering.

### Step 4: Replace `ClientManager` with client read models
- Move one feature at a time: factions -> claims -> routes -> diplomacy -> leader/UI detail screens.
- This reduces debugging noise and side-coupling before migration.

### Step 5: Peel logic out of `AbstractRecruitEntity`
- Extract policies in this order: command state, upkeep/payment, group/team syncing, formation logic, target selection/combat policy.
- Keep raw animation, attributes, navigation hooks, and physical world interaction on the entity side.

### Step 6: Isolate pathfinding/mixin/compat platform code
- Create narrow interfaces for path job submission, node evaluation, and vanilla patch assumptions.
- Move compat checks out of `Main` and entity classes.

### Step 7: Create explicit port layer for 1.21.1
- After the previous steps, most changes should be confined to platform adapters, render code, resource/registry code, and some entity façade implementations.

## Safe Decomposition by Feature Area

| Feature Area | Decompose Early? | Why |
|-------------|------------------|-----|
| Factions / claims / groups / treaties | Yes | Persistent aggregates with clear inputs/outputs; easiest high-value test targets |
| Commands / networking | Yes | Large surface, but mostly adapter-to-service extraction |
| Client screens / read state | Yes | Removes global mutable state and side leaks |
| Formations / squad orders | Yes | Important gameplay core; benefits from pure rule extraction |
| Combat / target selection | Medium | Valuable, but entangled with entities and navigation |
| Async pathfinding / mixins | After seam creation, before port | Too risky to rewrite blindly; isolate, then stabilize |
| Rendering polish | Late | Migration-sensitive but not core stabilization work |

## Testability Strategy by Boundary

| Boundary | Best Test Type | Notes |
|---------|----------------|-------|
| Pure command, faction, diplomacy, formation rules | Unit tests | No Minecraft runtime required |
| NBT serialization of aggregates | Unit tests | Golden snapshot tests catch migration regressions |
| Packet decode/validation/dispatch | Unit tests | Assert handler selection and rejection behavior |
| Server/client sync contracts | GameTests + focused integration tests | Verify end-to-end updates, not every branch |
| Entity behavior with real ticks/pathing | GameTests | Especially for battle, formation, mount, patrol behavior |
| Mixins and compat | Manual + targeted GameTests | High fragility, narrow coverage |

## Scalability Considerations

| Concern | At current stabilization scope | At heavy automated test coverage | At 1.21.1 migration phase |
|---------|-------------------------------|----------------------------------|---------------------------|
| Change blast radius | Too wide due to god classes | Reduced by adapters and handlers | Mostly confined to platform seams |
| Persistence correctness | Hard to reason about | Snapshot tests make regressions visible | Serializer changes become explicit |
| Networking correctness | Message sprawl | DTO/handler split improves coverage | Transport can be ported separately |
| AI determinism | Tick interactions are opaque | Policy extraction enables unit tests + GameTests | Port focuses on API deltas, not intent |
| Side safety | Client/server leaks likely | Read-model split reduces leaks | Client migration isolated from server logic |

## Architecture Recommendation

Do **not** start with entities or mixins. Start with **persistent world systems + packet handling + client cache replacement**, because that creates clean seams cheaply and gives immediate stabilization leverage. Then extract recruit command/formation/combat policies out of `AbstractRecruitEntity`. Only after those seams exist should the project isolate pathfinding and platform patches for the actual 1.21.1 port.

If this order is followed, the eventual migration becomes a bounded platform exercise instead of a full-system rewrite.

## Sources

- Existing project architecture and stack docs: `/home/kaiserroman/recruits/.planning/codebase/ARCHITECTURE.md`, `/home/kaiserroman/recruits/.planning/codebase/STACK.md`
- Project scope/context: `/home/kaiserroman/recruits/.planning/PROJECT.md`
- Code inspection: `Main.java`, `ClientManager.java`, `AbstractRecruitEntity.java`, `AsyncPathfinder.java`, `RecruitsFactionManager.java`
- Forge Game Tests docs: https://docs.minecraftforge.net/en/1.20.x/misc/gametest/ (**HIGH**)
- Forge Registries docs: https://docs.minecraftforge.net/en/1.20.x/concepts/registries/ (**HIGH**)
- Forge SavedData docs: https://docs.minecraftforge.net/en/1.20.x/datastorage/saveddata/ (**HIGH**)
- Forge Structuring Your Mod docs: https://docs.minecraftforge.net/en/1.21.x/gettingstarted/structuring/ (**HIGH**)
- Forge SimpleImpl docs: https://docs.minecraftforge.net/en/1.21.x/networking/simpleimpl/ (**HIGH**)
- Forge porting page linking current primers: https://docs.minecraftforge.net/en/1.21.x/legacy/porting/ (**HIGH**)
- ChampionAsh5357 1.20.6 -> 1.21 primer: https://gist.github.com/ChampionAsh5357/d895a7b1a34341e19c80870720f9880f (**MEDIUM**, linked from Forge docs but not official docs itself)
