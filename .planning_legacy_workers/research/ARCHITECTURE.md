# Architecture Patterns

**Domain:** Brownfield Minecraft Forge worker-villager automation mod revival
**Researched:** 2026-04-05
**Confidence:** MEDIUM-HIGH

## Recommended Architecture

Keep the existing **entity-centric** design. Do **not** redesign this into a generic service-oriented app. Instead, make the port and recovery manageable by inserting a few thin seams around the current high-risk classes:

```text
Forge bootstrap / registries
        ↓
Packet handlers + interaction hooks
        ↓
Server-side command/application layer   ← add here
        ↓
Worker entities / Work-area entities    ← keep as source of truth for persistent state
        ↓
Goal shells / planners / transfer rules ← extract pure logic here
        ↓
World mutation + storage + structure IO

Client screens/rendering stay thin and send intents only.
```

The recovery architecture should therefore be:

1. **Entities remain the persistence boundary** for workers and work areas.
2. **Packets become thin commands** that validate sender, resolve target, and delegate.
3. **AI goals become orchestration shells** over extractable planner/transfer helpers.
4. **Structure/build logic gets one canonical server-side implementation** for scan/rotate/material/entity-restore behavior.
5. **Tests split by seam**: pure Java tests for extracted logic, GameTests for in-world behavior, dedicated-server smoke tests for packet/GUI flows.

This is the lowest-churn path that still reduces rewrite risk.

## Component Boundaries

| Component | Responsibility | Communicates With |
|-----------|---------------|-------------------|
| Bootstrap + compat layer (`WorkersMain`, registries, config, mods.toml, Gradle) | Forge wiring, dependency versions, packet registration, lifecycle hooks | All other components |
| Domain entities (`AbstractWorkerEntity`, concrete workers, `AbstractWorkAreaEntity`, concrete areas) | Persistent gameplay state, NBT serialization, entity data, ownership, area identity | Goals, packet application layer, renderers |
| Packet application layer **(new seam)** | Resolve entity/work area safely, enforce authorization, reject unloaded/out-of-range targets, apply mutations through shared methods | Packet classes, entities, helper services |
| Goal shells (`BuilderWorkGoal`, `MinerWorkGoal`, etc.) | Tick-time orchestration, navigation timing, choosing when to plan/act/retry | Entities, planner helpers, transfer helpers |
| Planner/rule helpers **(new seam)** | Pure or mostly pure calculations: target selection, needed-items derivation, storage transfer plans, build rotation/material planning | Goal shells, build/storage code, tests |
| Structure/build subsystem (`StructureManager`, `BuildArea`) | Scan/load/save templates, rotate structure data, derive needed materials, restore embedded entities/work areas | Build screens, packet application layer, builder goal |
| Client UI/rendering | Show current state, collect input, preview only, never own truth | Packet layer, synced entity state |
| Test harness **(new)** | Unit tests for planners/serializers, GameTests for world behavior, runGameTestServer for CI gate | Planner helpers, entities, packets |

## Risky Seams in This Codebase

| Seam | Why Risky | Architectural Response |
|------|-----------|------------------------|
| Packet handlers mutating entities directly | Current handlers resolve by nearby UUID and mutate without strong ownership checks; this is both a multiplayer exploit risk and a regression risk | Introduce a shared server-side mutation layer (`WorkAreaCommands`, `MerchantCommands`, etc.) and move all permission + chunk-loaded checks there |
| Large AI state machines | `BuilderWorkGoal`, `MinerWorkGoal`, storage goals, etc. mix planning, pathing, inventory, world mutation, and user messaging | Split into goal shell + extracted planner/transfer helpers before major mechanic completion |
| Build pipeline spread across UI, `StructureManager`, `BuildArea`, and builder AI | Rotation/material/entity restoration logic can drift and is hard to test | Make `BuildArea`/shared helper the single canonical build-plan source; UI only selects template and dimensions |
| Recruits/pathfinding dependency | Mandatory dependency and custom path evaluator make the 1.21.1 port sensitive to upstream API changes | Treat Recruits integration as a dedicated compatibility boundary; do not intermingle new mechanic work with pathfinder-port edits |
| Client file/template loading | Local filesystem behavior and client-selected names can produce invalid inputs | Keep template discovery client-side, but sanitize and validate all names/paths before server use |
| Null-heavy APIs | Many methods return `null` for empty/absent values, making ports and refactors fragile | Normalize new extracted helpers to return empty collections/results; keep null adapters at old boundaries only |

## Data Flow

### Server-authoritative gameplay flow

1. Player interacts with worker/work-area or clicks a client GUI.
2. Client sends a **minimal intent packet**: target UUID + requested change.
3. Server packet handler enqueues work, resolves the target, verifies sender permission, checks chunk/entity presence, and delegates to a shared command/applicator method.
4. Applicator mutates the authoritative entity/work-area state.
5. Entities/goals consume that state during normal server ticks.
6. Vanilla/Forge entity sync plus explicit client-open packets update screens/rendering.

### Rule to enforce

**Client screens may suggest state; only the server may decide state.**

That means:

- never trust client UUID ownership claims;
- never trust client-selected positions without chunk/presence validation;
- never let screens compute the final build/storage mutation;
- never let a packet directly edit multiple entities without a centralized permission check.

### Recommended packet pattern

```java
public final class WorkAreaCommands {
    public static void renameAndMove(ServerPlayer sender, AbstractWorkAreaEntity area, String name, Vec3 pos) {
        if (!WorkAreaAccess.canEdit(sender, area)) return;
        if (!sender.level().hasChunkAt(BlockPos.containing(pos))) return;

        area.setCustomName(Component.literal(name));
        area.moveTo(pos);
        area.rebuildAreaAndValidate();
    }
}
```

This keeps packet classes boring and reusable across the 1.21.1 port.

## Patterns to Follow

### Pattern 1: Entity as persistence boundary, helper as logic boundary
**What:** Keep NBT/synced data on entities, but move decision logic into helper classes with explicit inputs/outputs.
**When:** Any time a goal currently mixes “what should I do?” with “perform world action now.”
**Example targets:** needed item calculation, next block selection, storage withdraw/deposit decision, build rotation/material plan.

### Pattern 2: Goal shell / planner core split
**What:** Goals should manage tick cadence, cooldowns, navigation, and retries. Planner helpers should select targets and generate actions/results.
**When:** Refactoring `BuilderWorkGoal`, `MinerWorkGoal`, `DepositItemsToStorage`, `GetNeededItemsFromStorage`.
**Why:** Lets you test recovery work without spinning a full world for every branch.

### Pattern 3: One canonical build-plan source
**What:** Structure scan/load/rotate/material/entity-restore must be implemented in one shared path, not separately in screen code and builder code.
**When:** Before finishing builder mechanics or porting structure features.
**Why:** Build behavior is one of the most fragile existing seams.

### Pattern 4: Compatibility boundary for 1.21.1 changes
**What:** Isolate version-sensitive code in a small set of files: rendering registration, packet registration/wrappers, pathfinding hooks, menu/screen registration, data/resource path assumptions.
**When:** During the port branch.
**Why:** Minecraft 1.21 introduces notable API churn, especially around rendering/resource paths; keeping version churn localized prevents mechanic work from being mixed with port noise. Source: Forge 1.21 porting page + ChampionAsh 1.20.6→1.21 primer.

### Pattern 5: Test pyramid tailored to Forge mods
**What:**
- **Unit tests:** extracted planners, serializers, rotation/math, config wiring.
- **GameTests:** worker/work-area placement, storage transfer, build-area round-trip, packet permission outcomes that need world state.
- **Dedicated-server smoke tests:** manual scripted validation for login/open GUI/edit/build/hire flows.
**When:** Add unit tests as soon as helpers exist; add GameTests before port completion freeze.
**Why:** The project already has GameTest namespaces enabled in Gradle, so the build is partially prepared for this.

## Anti-Patterns to Avoid

### Anti-Pattern 1: Port and finish mechanics inside the same giant classes
**What:** Editing `BuilderWorkGoal`, `MinerWorkGoal`, network handlers, and rendering code at once during the 1.21.1 migration.
**Why bad:** You lose blameability. When behavior breaks, you cannot tell whether the cause is API migration, logic regression, or auth bugs.
**Instead:** First extract seams on 1.20.1, then port, then finish remaining mechanics behind tests.

### Anti-Pattern 2: Client-driven authority
**What:** Letting GUI packets remain the place where validation lives.
**Why bad:** Dedicated-server correctness and multiplayer security stay weak.
**Instead:** UI hides invalid actions, but server commands enforce them.

### Anti-Pattern 3: Replacing entity persistence with global saved data too early
**What:** Moving worker/work-area state into `SavedData` during recovery.
**Why bad:** High churn for little gain; current design already persists naturally via entities.
**Instead:** Use entities as the canonical state store. Only introduce `SavedData` for truly global indexes later if scaling demands it. Forge docs confirm `SavedData` is best for level-attached shared state, not mandatory replacement for entity-owned state.

### Anti-Pattern 4: Adding tests only after the port compiles
**What:** Waiting until 1.21.1 build issues are solved before writing any coverage.
**Why bad:** You will lock in wrong behavior or miss regressions from mechanic recovery.
**Instead:** Add seam tests on 1.20.1 first, then carry them forward.

## Suggested Build Order for Recovery + Migration

This project should be recovered in **two tracks with one gate**: stabilize seams on 1.20.1 first, then port to 1.21.1 once the riskiest logic is protected.

### Phase 0: Dependency and port gate
- Verify Forge 1.21.1 target, Recruits 1.21.1 availability/API shape, and any required dependency replacements.
- Remove obvious build hazards like `jcenter()` while still on the old branch.
- Outcome: a clear answer to “is 1.21.1 blocked by upstream dependency?”

### Phase 1: Server-authority hardening on current version
- Centralize packet mutation paths.
- Add ownership/team/chunk-loaded validation for every mutating packet.
- Replace silent null/no-op hotspots with explicit failure returns/logging.
- Outcome: multiplayer-safe baseline before feature completion.

### Phase 2: Extract testable cores from high-risk mechanics
- Start with storage transfer and build planning, not every profession at once.
- Carve out helper classes from builder/miner/storage goals.
- Keep goal classes behaviorally equivalent while shrinking them.
- Outcome: stable seams for mechanic completion.

### Phase 3: Add automated tests before major behavior changes
- Unit tests for item-need math, structure rotation/material derivation, config wiring.
- GameTests for work-area placement/editing, build round-trip, storage deposit/withdraw, permission checks.
- Outcome: safety net for both recovery and porting.

### Phase 4: Complete missing mechanics on 1.20.1 baseline
- Finish the mechanics that are currently implied but incomplete using extracted helpers.
- Validate against dedicated-server flows.
- Outcome: behavior baseline is known before migration noise begins.

### Phase 5: Port infrastructure to 1.21.1
- Update Gradle/Forge/version metadata.
- Fix compile breaks in registries, packet setup, rendering, resources/tags/datapack paths, and version-specific API changes.
- Keep this phase focused on compatibility edits, not new mechanic behavior.
- Outcome: compiling/running 1.21.1 build with preserved tests.

### Phase 6: Port integration seams
- Reconcile Recruits/pathfinding hooks.
- Re-test menus/screens/renderers and client-only preview code.
- Fix any 1.21-specific behavior changes exposed by GameTests/manual play.
- Outcome: functional parity on 1.21.1.

### Phase 7: Performance and polish pass
- Throttle per-tick scans.
- Reduce repeated area rescans.
- Validate larger worker counts on dedicated server.
- Outcome: release-ready baseline.

### Why this order

- The current biggest risks are **logic regressions**, **packet authority bugs**, and **dependency/API churn**.
- Porting first would force behavior debugging inside unstable code.
- Extracting seams and tests first turns the port into mostly a compatibility exercise.
- Recruits compatibility is the only legitimate reason to stop early; everything else benefits from pre-port hardening.

## Migration Notes Specific to 1.21/1.21.1

- Forge 1.21 docs still recommend `DeferredRegister` and `SimpleChannel`-style packet handling; the current registry-driven bootstrap shape is still valid.
- Forge 1.21 GameTests remain supported and `runGameTestServer` exists, so the mod should lean on GameTests rather than trying to invent a custom in-world regression harness.
- The 1.20.6→1.21 primer indicates broad API churn in rendering/resource paths and some vanilla API signatures; keep client/rendering changes isolated from worker-mechanic logic.
- LOW confidence: exact 1.21.1-specific changes for the Recruits dependency were not verified from official upstream docs in this pass, so treat that compatibility seam as the first milestone gate.

## Scalability Considerations

| Concern | Small server (≤20 workers) | Mid server (20-100 workers) | Large server (100+ workers) |
|---------|-----------------------------|------------------------------|-----------------------------|
| Item pickup scans | Current approach is tolerable | Needs interval throttling | Needs throttling plus narrower triggers |
| Work-area rescans | Acceptable for small areas | Cache scan results per state change | Partition/cached jobs become necessary |
| Packet mutation safety | Mostly correctness concern | Multiplayer abuse matters | Must be centralized and auditable |
| Build-area planning | Manual verification possible | Requires GameTests | Requires cached plans and strict canonical logic |
| Pathfinding/custom evaluator | Debuggable by playtest | Profile hot spots | Reduce fork surface from Recruits/vanilla pathing |

## Sources

- Codebase architecture: `/home/kaiserroman/workers/.planning/codebase/ARCHITECTURE.md`
- Codebase concerns: `/home/kaiserroman/workers/.planning/codebase/CONCERNS.md`
- Project context: `/home/kaiserroman/workers/.planning/PROJECT.md`
- Forge docs, Registries: https://docs.minecraftforge.net/en/concepts/registries/ — HIGH
- Forge docs, SimpleImpl/networking: https://docs.minecraftforge.net/en/networking/simpleimpl/ — HIGH
- Forge docs, Game Tests: https://docs.minecraftforge.net/en/1.21.x/misc/gametest/ — HIGH
- Forge docs, Saved Data: https://docs.minecraftforge.net/en/1.21.x/datastorage/saveddata/ — HIGH
- Forge docs, Porting to Minecraft 1.21: https://docs.minecraftforge.net/en/legacy/porting/ — HIGH
- ChampionAsh5357 1.20.6 -> 1.21 migration primer (last active 2025-10-20): https://gist.github.com/ChampionAsh5357/d895a7b1a34341e19c80870720f9880f — MEDIUM
