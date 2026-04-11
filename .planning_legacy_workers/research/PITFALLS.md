# Domain Pitfalls

**Domain:** Minecraft Forge villager-worker automation mod revival for 1.21.1
**Researched:** 2026-04-05

## Critical Pitfalls

Mistakes that are likely to cause rewrites, multiplayer breakage, or a "compiles but is not actually shippable" result.

### Pitfall 1: Treating the port as a compile-only upgrade
**What goes wrong:** The mod is updated from 1.20.1 to 1.21.1 just enough to compile, but hidden API and content-format changes break runtime behavior in entities, rendering, resources, and data-driven content.
**Why it happens:** 1.21 changed common migration surfaces such as `ResourceLocation` construction, tag/registry folder names, rendering APIs, and multiple vanilla signatures. The current build also still targets Forge `1.20.1-47.4.1`, uses deprecated `jcenter()`, and depends on CurseMaven artifacts plus mandatory Recruits compatibility.
**Consequences:** Long tail of runtime crashes, missing assets/data, broken menus/renderers, and blocked builds because one dependency in the stack is not actually ready for 1.21.1.
**Warning signs:**
- Port work is measured by "number of compile errors fixed"
- First successful launch still shows missing tags/resources or renderer/menu crashes
- Recruits/CoreLib/other transitive mods do not have a verified 1.21.1 path early
- Data packs/resources silently stop loading after folder-name changes
**Prevention strategy:**
- Start with a dependency-and-bootstrap phase, not gameplay fixes
- Verify 1.21.1 availability for Recruits and every required library before deeper refactors
- Replace deprecated repositories (`jcenter()`) and pin maintained artifact sources early
- Run a porting checklist against the 1.20.6→1.21 primer and Forge 1.21 docs before touching gameplay logic
- Smoke-test client launch, dedicated server launch, entity spawn, menu open, and one packet round-trip before broader feature work
**Phase should address it:** Phase 1 - Port/bootstrap and dependency validation

### Pitfall 2: Client-only assumptions leaking into dedicated-server paths
**What goes wrong:** GUI, renderer, or client utility references are left in common/server code, or server logic depends on singleplayer behavior. The mod works in dev client but crashes or desyncs on a dedicated server.
**Why it happens:** This mod is heavily entity/UI driven, and Forge explicitly warns against crossing logical/physical sides. Brownfield mods often "accidentally work" in singleplayer because both sides share a JVM.
**Consequences:** Dedicated-server startup crashes, ghost state, menus that only work locally, packet handlers mutating the wrong side, and bugs that only appear in multiplayer.
**Warning signs:**
- Code paths use `Minecraft.getInstance()` or client classes from entity/goal/network/common classes
- Screens are used as state holders instead of server-authoritative data sources
- Features work in integrated server tests but fail on a headless dedicated server
- Static fields are used to bridge client/server state
**Prevention strategy:**
- Audit every GUI/network/entity interaction for side boundaries before feature completion
- Keep game logic on the logical server; use packets only for client↔server communication
- Gate client-only registration with client setup / `DistExecutor` patterns
- Add a dedicated-server validation checklist for worker hire, area edit, merchant edit, and structure build flows
**Phase should address it:** Phase 2 - Server-authority and side-safety hardening

### Pitfall 3: Trusting client packets for ownership, selection, or world access
**What goes wrong:** Nearby players can edit/delete work areas, mutate merchant state, or force unsafe world lookups because packet handlers trust client UUIDs, positions, or proximity alone.
**Why it happens:** Current concerns already show mutating packet handlers that do not consistently enforce ownership/authorization. Forge networking docs also warn that packet handlers must be defensive and avoid unsafe chunk access.
**Consequences:** Multiplayer griefing, dupes/desync, arbitrary chunk loads, and a mod that is unusable on real servers even if the core loop works.
**Warning signs:**
- Packet handlers accept UUID/entity references from the client and immediately mutate server state
- Ownership checks exist only in GUI visibility, not server handlers
- Handlers query world state from client-provided positions without `hasChunkAt`/loaded checks
- Bug reports mention "other players can edit my worker/trades/areas"
**Prevention strategy:**
- Build a centralized authorization helper for all mutating packets
- Re-resolve target entities/work areas server-side from trusted context, then verify owner/team/interaction distance
- Reject edits when the target chunk/entity is not loaded instead of forcing loads
- Add packet-focused tests or GameTests for non-owner edit attempts and unloaded-target cases
**Phase should address it:** Phase 2 - Server-authority and packet hardening

### Pitfall 4: Null- and distance-based menu/entity resolution breaking packet-driven GUIs
**What goes wrong:** Merchant/work-area screens open against entities that later unload, move, or fail radius lookup; constructors return `null` or silently no-op, leaving broken screens and hard-to-reproduce desyncs.
**Why it happens:** The current menu path already has null-return behavior and short-radius UUID lookups. Forge menu docs assume server-authoritative menu opening and explicit client constructor data; this mod currently has a more fragile packet-driven approach.
**Consequences:** Random GUI failures, lost edits, crashes when entities are absent, and UX that feels flaky under lag or multiplayer.
**Warning signs:**
- Menu factories/screens can return `null`
- Screens depend on nearby entity scans instead of durable server-opened menu context
- Edits fail only when the villager moved, chunk unloaded, or player stepped away
- Multiplayer reports say "screen opened but nothing saved"
**Prevention strategy:**
- Replace null-return paths with explicit error handling and player feedback
- Prefer server-opened menus or server-authoritative entity resolution with stable IDs
- Sync only minimal validated state to the client; do not let the screen be the source of truth
- Test menu open/edit/close while moving away, relogging, and unloading/reloading the worker
**Phase should address it:** Phase 3 - GUI/menu stabilization

### Pitfall 5: Porting custom entities without revalidating sync and persistence
**What goes wrong:** Workers appear with wrong state after relog/chunk reload, visual/client state diverges from server state, or spawn-time data is missing after the 1.21 port.
**Why it happens:** The mod has multiple custom worker entities, profession states, ownership, work-area bindings, and GUI-visible fields. Forge 1.21 entity sync still relies on correct `SynchedEntityData`, spawn data, and save/load discipline; incomplete or duplicated state logic is easy to break in a brownfield port.
**Consequences:** Workers forget jobs, ownership, targets, or inventories; players see stale data; bug reports become impossible to triage because symptoms appear only after reload.
**Warning signs:**
- State only exists in transient fields or only in client screens
- Different code paths serialize the same concept differently
- Freshly spawned workers behave differently from reloaded workers
- Chunk reloads or server restarts reset assignments or area links
**Prevention strategy:**
- Inventory every worker state field: server-only, synced, persistent, or derived
- Use `SynchedEntityData` only for client-visible dynamic state; keep authoritative logic on the server
- Verify save/load and spawn packet behavior for each worker type before balancing AI
- Add regression tests for spawn, save/reload, and reassignment flows
**Phase should address it:** Phase 3 - Entity state and persistence audit

### Pitfall 6: AI-goal regressions hidden inside giant state machines
**What goes wrong:** Small port fixes in one goal break unrelated worker behavior: miners stall, builders loop forever, farmers drop items incorrectly, or storage retrieval duplicates/fails.
**Why it happens:** Current AI is concentrated in large classes that mix pathing, scanning, inventory mutation, and messaging. There is almost no automated coverage, so regressions are discovered only in live play.
**Consequences:** Endless bug-fix churn, "one profession fixed, another broken" releases, and inability to confidently finish the mod.
**Warning signs:**
- Goals contain many booleans/branch flags with implicit transitions
- Fixes require editing multiple worker classes in parallel
- Workers get stuck in retry loops or spam status messages
- Similar logic exists in builder/miner/farmer/lumberjack variants with small drift
**Prevention strategy:**
- Stabilize one profession loop at a time instead of editing all workers at once
- Extract shared storage/pathing/selection helpers before broad gameplay tuning
- Add GameTests or focused logic tests for state transitions, especially storage retrieval/deposit and target completion
- Keep a manual regression matrix per profession before merge
**Phase should address it:** Phase 4 - AI/state-machine stabilization

### Pitfall 7: Structure scanning/build restoration drifting across client, world utils, and AI
**What goes wrong:** Scan/save/load/rotate/build round-trips stop matching, material lists are wrong, or post-build entities/work areas restore inconsistently.
**Why it happens:** The structure pipeline already spans client GUI, filesystem access, world utilities, `BuildArea`, and `BuilderWorkGoal`, with duplicated restoration logic noted in concerns. 1.21 resource/path changes raise the risk further.
**Consequences:** Builders consume wrong items, rotate structures incorrectly, create broken templates, or rebuild incomplete/invalid work areas.
**Warning signs:**
- A structure works unrotated but fails after rotation
- Saved scans behave differently after reload
- Entity restoration logic exists in more than one place
- Build bugs only appear with certain dimensions/orientations
**Prevention strategy:**
- Consolidate scan/build/restore math into one shared implementation before new feature completion
- Normalize and validate file paths under the scan directory only
- Add round-trip tests: scan → save → load → rotate → build
- Treat template/entity restoration as a separate milestone, not a side effect of builder AI work
**Phase should address it:** Phase 5 - Structure pipeline hardening

### Pitfall 8: Server performance collapsing once multiple workers are active
**What goes wrong:** The mod feels fine with one worker in dev, then tanks TPS on real servers when several workers run scans, raycasts, pathfinding, and storage checks every tick.
**Why it happens:** Current concerns already show per-tick item scans, full-area rescans, repeated raycasts, and a heavy custom path evaluator. Automation mods often fail at concurrency, not at single-worker correctness.
**Consequences:** Poor server TPS, rubber-banding workers, abandoned chunks staying hot, and admins disabling the mod despite liking the concept.
**Warning signs:**
- Performance problems appear only after adding more workers or larger work areas
- Builders/miners cause spikes during scans or target selection
- TPS degradation scales roughly with worker count or area size
- Profiling shows pathfinding, `getEntitiesOfClass`, or scan methods dominating tick time
**Prevention strategy:**
- Set explicit worker-count and area-size test scenarios early
- Throttle scans, cache area results, and invalidate incrementally instead of full rescans
- Profile before and after pathfinding or scan changes
- Add server config guardrails for expensive features until the implementation is proven stable
**Phase should address it:** Phase 6 - Performance and scaling pass

## Moderate Pitfalls

### Pitfall 9: Config drift making gameplay feel random or untrustworthy
**What goes wrong:** Hire costs, feature toggles, and limits do not match config values, so players and server admins cannot predict balance.
**Prevention:** Fix miswired config keys first, route all worker costs/toggles through config, and add startup validation for duplicate/unused keys.
**Warning signs:**
- Changing config values does nothing
- Multiple professions share the wrong cost
- README/config comments disagree with in-game behavior
**Phase should address it:** Phase 3 - Config and rules audit

### Pitfall 10: Gameplay deadlocks from missing-path, missing-item, or missing-storage edge cases
**What goes wrong:** Workers are technically alive but practically useless: they idle forever, spam retry states, or consume player attention because one inaccessible chest/block path halts the loop.
**Prevention:** Define explicit failure recovery states, timeouts, and player-visible reasons; treat unreachable targets and empty storage as first-class states, not exceptional cases.
**Warning signs:**
- Workers stand still with no obvious message
- Same failed target is retried forever
- Storage loops reopen the same chest/path repeatedly
**Phase should address it:** Phase 4 - Core loop reliability

### Pitfall 11: Dependency lock-in to the Recruits ecosystem blocking release timing
**What goes wrong:** The mod is mostly ready, but release is gated by Recruits/API/version compatibility or behavioral changes in the dependency chain.
**Prevention:** Verify 1.21.1 dependency versions at the start, isolate integration seams, and keep a compatibility matrix for exact supported dependency versions.
**Warning signs:**
- Port blockers come from dependency API mismatch rather than Workers code
- "Works in IDE" depends on locally cached artifacts
- Runtime behavior changes when dependency minor versions change
**Phase should address it:** Phase 1 - Dependency validation, then revisit in release prep

## Minor Pitfalls

### Pitfall 12: Leaving debug/placeholder code in the release path
**What goes wrong:** Dev-only hooks, commented experiments, and placeholder classes confuse maintainers and can accidentally affect runtime behavior.
**Prevention:** Remove or explicitly gate debug utilities before content freeze.
**Warning signs:**
- Commented-out navigation hooks remain in live classes
- "temporary" or TODO-only files are still on the main runtime path
**Phase should address it:** Phase 6 - Cleanup and release hardening

### Pitfall 13: Over-scoping the revival into a redesign
**What goes wrong:** The team starts adding new worker systems or redesigning architecture before the original core loop is stable.
**Prevention:** Use the existing code as source of truth; only add glue required to complete intended mechanics.
**Warning signs:**
- New professions/systems are proposed before current ones are stable
- Refactors are justified by taste rather than a proven bug/porting need
**Phase should address it:** All phases - scope control checkpoint

## Phase-Specific Warnings

| Phase Topic | Likely Pitfall | Mitigation |
|-------------|---------------|------------|
| Port/bootstrap | Dependency/version dead-end | Validate Forge 1.21.1 + Recruits/toolchain first; replace deprecated repos early |
| Server-authority | Client packet trust | Centralize ownership, distance, and loaded-chunk checks in packet handlers |
| GUI/menu stabilization | Null entity/menu resolution | Open menus from server authority and fail explicitly, never with `null` |
| Entity persistence | Reload/desync bugs | Audit every synced/persistent worker field and test chunk reload + relog |
| AI stabilization | State-machine regressions | Freeze one profession at a time and add transition-focused regression tests |
| Structure pipeline | Rotation/restore drift | Add scan→save→load→rotate→build round-trip tests |
| Performance | Worker-count TPS collapse | Profile with many workers; throttle scans and cache work-area results |
| Release prep | "Works on client only" false confidence | Run dedicated-server gameplay checklist before release |

## Sources

- `/home/kaiserroman/workers/.planning/PROJECT.md` — project scope, dedicated-server requirement, dependency constraint (**HIGH**)
- `/home/kaiserroman/workers/.planning/codebase/CONCERNS.md` — current fragility, security, performance, and scaling evidence (**HIGH**)
- `/home/kaiserroman/workers/.planning/codebase/TESTING.md` — current lack of automated tests and available GameTest hook in build (**HIGH**)
- Forge 1.21 docs: Sides — https://docs.minecraftforge.net/en/1.21.x/concepts/sides/ (**HIGH**)
- Forge 1.21 docs: SimpleImpl — https://docs.minecraftforge.net/en/1.21.x/networking/simpleimpl/ (**HIGH**)
- Forge 1.21 docs: Synchronizing Entities — https://docs.minecraftforge.net/en/1.21.x/networking/entities/ (**HIGH**)
- Forge 1.21 docs: Menus — https://docs.minecraftforge.net/en/1.21.x/gui/menus/ (**HIGH**)
- Forge 1.21 docs: Screens — https://docs.minecraftforge.net/en/1.21.x/gui/screens/ (**HIGH**)
- Forge 1.21 docs: Saved Data — https://docs.minecraftforge.net/en/1.21.x/datastorage/saveddata/ (**HIGH**)
- Forge 1.21 docs: Game Tests — https://docs.minecraftforge.net/en/1.21.x/misc/gametest/ (**HIGH**)
- Forge 1.21 docs: Update Checker — https://docs.minecraftforge.net/en/1.21.x/misc/updatechecker/ (**HIGH**)
- Forge legacy porting index — https://docs.minecraftforge.net/en/1.21.x/legacy/porting/ (**HIGH**)
- ChampionAsh5357 1.20.6 → 1.21 migration primer — https://gist.github.com/ChampionAsh5357/d895a7b1a34341e19c80870720f9880f (**MEDIUM**; respected community source, not official Forge docs)
