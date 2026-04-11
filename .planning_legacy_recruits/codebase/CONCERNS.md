# Codebase Concerns

**Analysis Date:** 2026-04-05

## Tech Debt

**Monolithic recruit entity logic:**
- Issue: Combat, inventory, targeting, persistence, payment, movement, and command handling are concentrated in very large classes instead of smaller feature-focused modules.
- Files: `src/main/java/com/talhanation/recruits/entities/AbstractRecruitEntity.java`, `src/main/java/com/talhanation/recruits/entities/AbstractLeaderEntity.java`, `src/main/java/com/talhanation/recruits/entities/AbstractInventoryEntity.java`
- Impact: Small behavior changes have wide blast radius, review cost is high, and bug fixes are likely to regress unrelated recruit systems.
- Fix approach: Split entity behavior into dedicated helpers/components by concern (targeting, upkeep, inventory, orders, persistence) and keep entity classes as orchestration points.

**Menu registration duplicates lookup and error handling:**
- Issue: Every menu factory repeats UUID lookup, null returns, and manual logging; failures are handled by returning `null`, which hides root causes from callers.
- Files: `src/main/java/com/talhanation/recruits/init/ModScreens.java`
- Impact: GUI failures are hard to diagnose, and menu open behavior stays inconsistent across recruit, hire, debug, promote, disband, and patrol flows.
- Fix approach: Centralize entity lookup and menu creation validation into shared helpers that return explicit error states and structured logs.

**Partially implemented gameplay systems remain wired into production code:**
- Issue: Scout, commander, captain, noble villager, and faction inspection flows contain TODO-backed stubs rather than finished feature boundaries.
- Files: `src/main/java/com/talhanation/recruits/entities/ScoutEntity.java`, `src/main/java/com/talhanation/recruits/entities/CommanderEntity.java`, `src/main/java/com/talhanation/recruits/entities/CaptainEntity.java`, `src/main/java/com/talhanation/recruits/entities/VillagerNobleEntity.java`, `src/main/java/com/talhanation/recruits/client/gui/faction/FactionInspectionScreen.java`, `src/main/java/com/talhanation/recruits/Main.java`
- Impact: Unsupported feature combinations are reachable in normal gameplay and create behavior gaps that look like bugs to players.
- Fix approach: Gate unfinished features behind config checks/UI guards or finish the missing ranged combat, noble AI, claim gating, and compatibility version handling paths.

## Known Bugs

**Recruit-related menus fail when the target entity is outside a 10-block search box:**
- Symptoms: Inventory, hire, debug, disband, promote, and patrol leader screens can fail to open and return `null` even when the recruit UUID is valid.
- Files: `src/main/java/com/talhanation/recruits/init/ModScreens.java`
- Trigger: `getRecruitByUUID()` and `getAssassinByUUID()` only scan a local `AABB` with radius `10D`; any recruit farther away is treated as missing.
- Workaround: Open menus only while standing near the recruit.

**Claim map button visibility ignores the server claiming toggle:**
- Symptoms: Team leaders can see the claim-map entry point even when claiming is disabled by config; the actual map later blocks actions.
- Files: `src/main/java/com/talhanation/recruits/client/gui/faction/FactionInspectionScreen.java`, `src/main/java/com/talhanation/recruits/client/ClientManager.java`, `src/main/java/com/talhanation/recruits/client/gui/worldmap/WorldMapScreen.java`
- Trigger: `claimMapButton.visible = isTeamLeader` is missing the `ClientManager.configValueIsClaimingAllowed` check.
- Workaround: Use the world map only on servers where claiming is enabled.

**Small Ships compatibility check is version-string specific:**
- Symptoms: Compatible `smallships` builds outside the hard-coded substrings are treated as incompatible; integration features are disabled even when APIs still match.
- Files: `src/main/java/com/talhanation/recruits/Main.java`, `src/main/java/com/talhanation/recruits/compat/SmallShips.java`
- Trigger: `Main.setup()` only accepts versions containing `2.0.0-b1.3` or `2.0.0-b1.4`.
- Workaround: Use one of the accepted versions or patch the version guard.

## Security Considerations

**Automatic update checks create unsolicited outbound network traffic:**
- Risk: Client login and server startup trigger version checks and expose mod usage metadata to the update source; players/admins do not get an in-game privacy prompt.
- Files: `src/main/java/com/talhanation/recruits/UpdateChecker.java`, `src/main/java/com/talhanation/recruits/config/RecruitsClientConfig.java`, `src/main/java/com/talhanation/recruits/config/RecruitsServerConfig.java`
- Current mitigation: The behavior is configurable through `UpdateCheckerClientside` and `UpdateCheckerServerside`.
- Recommendations: Document the network behavior explicitly, default it off in privacy-sensitive distributions, and keep the toggle surfaced in release notes/admin docs.

**Asynchronous server callbacks are scheduled outside the main server executor:**
- Risk: `DelayedExecutor` runs faction updates on its own scheduler thread, but the scheduled tasks mutate and broadcast server-side state.
- Files: `src/main/java/com/talhanation/recruits/util/DelayedExecutor.java`, `src/main/java/com/talhanation/recruits/FactionEvents.java`
- Current mitigation: No thread handoff back to `MinecraftServer#execute` is present in these delayed callbacks.
- Recommendations: Replace `DelayedExecutor.runLater()` usage with server-thread scheduling or wrap delayed work in `server.execute(...)` before touching world/team state.

## Performance Bottlenecks

**Target acquisition scans nearby entities for every recruit on a fixed cadence:**
- Problem: Each recruit searches a 40-block inflated box and sorts candidate targets every 20 ticks.
- Files: `src/main/java/com/talhanation/recruits/entities/AbstractRecruitEntity.java`
- Cause: `searchForTargets()` calls `serverLevel.getEntitiesOfClass(...)`, filters with `targetingConditions`, sorts by distance, and selects from the top results.
- Improvement path: Cache candidates per chunk/tick, reduce scan frequency based on state, and short-circuit when current targets remain valid.

**World map tile generation performs repeated disk I/O while the screen is open:**
- Problem: The map updates the current tile every second and a neighbor tile every 500 ms, merging and saving PNG data on each cycle.
- Files: `src/main/java/com/talhanation/recruits/client/events/ClientPlayerEvents.java`, `src/main/java/com/talhanation/recruits/client/gui/worldmap/ChunkTileManager.java`, `src/main/java/com/talhanation/recruits/client/gui/worldmap/ChunkTile.java`
- Cause: Tile refresh uses `readAllBytes`, image merge loops, `writeToFile`, and per-chunk image regeneration on the render path of an open screen.
- Improvement path: Batch writes, debounce saves until screen close, and add an eviction/cache policy for `loadedTiles`.

**Async pathfinding falls back to synchronous execution when the queue is saturated:**
- Problem: Heavy path load can spill work back onto the caller thread instead of shedding load.
- Files: `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathProcessor.java`
- Cause: The rejection handler runs `task.run()` directly when the executor queue is full and not shut down.
- Improvement path: Record saturation metrics and prefer dropping/retrying low-priority path requests over running them synchronously on gameplay threads.

## Fragile Areas

**Reflection-heavy compatibility shims:**
- Files: `src/main/java/com/talhanation/recruits/compat/SmallShips.java`, `src/main/java/com/talhanation/recruits/compat/BlunderbussWeapon.java`, `src/main/java/com/talhanation/recruits/compat/MusketWeapon.java`, `src/main/java/com/talhanation/recruits/compat/PistolWeapon.java`
- Why fragile: Integration code depends on exact class names, fields, and method signatures from external mods; many failures degrade to logs plus `null` returns.
- Safe modification: Keep compatibility work isolated, validate the target mod version before reflection, and add explicit fallback behavior for missing methods/fields.
- Test coverage: No automated compatibility tests are present under `src/test/`.

**Async pathfinding and target-finding concurrency:**
- Files: `src/main/java/com/talhanation/recruits/pathfinding/AsyncPath.java`, `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathNavigation.java`, `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathfinder.java`, `src/main/java/com/talhanation/recruits/entities/ai/async/AsyncManager.java`, `src/main/java/com/talhanation/recruits/entities/ai/async/AsyncTaskWithCallback.java`
- Why fragile: Behavior depends on thread-group checks, volatile state, post-processing callbacks, and executor configuration; some failures are logged with `printStackTrace()` or converted to `null` paths.
- Safe modification: Change one async subsystem at a time, keep callbacks on the server thread, and replace silent/null fallbacks with typed failure states.
- Test coverage: No concurrency regression tests are present under `src/test/`.

**World map resource lifecycle:**
- Files: `src/main/java/com/talhanation/recruits/client/gui/worldmap/WorldMapScreen.java`, `src/main/java/com/talhanation/recruits/client/gui/worldmap/ChunkTileManager.java`, `src/main/java/com/talhanation/recruits/client/gui/worldmap/ChunkTile.java`, `src/main/java/com/talhanation/recruits/client/gui/worldmap/ChunkImage.java`
- Why fragile: Texture/image cleanup is manual, several exceptions are ignored, and tile caching is unbounded until screen/world close.
- Safe modification: Add deterministic ownership rules for image/texture objects and verify all close paths before changing rendering behavior.
- Test coverage: No client UI or resource lifecycle tests are present under `src/test/`.

## Scaling Limits

**Recruit combat/AI density:**
- Current capacity: `MaxRecruitsForPlayer` defaults to `100` and `MaxNPCsInFaction` defaults to `500` in `src/main/java/com/talhanation/recruits/config/RecruitsServerConfig.java`.
- Limit: Per-entity target scans, pathfinding, and large monolithic tick methods amplify CPU cost sharply as recruit counts approach config ceilings.
- Scaling path: Lower scan frequency, partition AI by distance/activity, and add coarse-grained caps per loaded area instead of only per-player/per-faction totals.

**World map memory footprint:**
- Current capacity: `ChunkTileManager` keeps all visited tiles in `loadedTiles` for the lifetime of the map session.
- Limit: Long map sessions across many regions accumulate `ChunkTile` images/textures without eviction.
- Scaling path: Add LRU eviction plus background save/close behavior keyed to last access time.

## Dependencies at Risk

**Build relies on floating and snapshot tooling versions:**
- Risk: `ForgeGradle` uses `6.+` and `mixingradle` uses `0.7-SNAPSHOT`, which makes builds non-reproducible and susceptible to upstream breakage.
- Impact: Clean environment builds can fail or change behavior without any repository changes.
- Migration plan: Pin exact plugin versions in `build.gradle` and document a known-good toolchain.

**Deprecated/unreliable repository usage:**
- Risk: `jcenter()` remains in the buildscript repositories.
- Impact: Dependency resolution depends on an effectively frozen repository and complicates long-term build maintenance.
- Migration plan: Remove `jcenter()` from `build.gradle` after confirming all artifacts resolve from active repositories.

## Missing Critical Features

**Automated regression suite:**
- Problem: The repository has no committed automated tests under `src/test/`, and `build.gradle` does not define a project-specific test stack.
- Blocks: Safe refactoring of recruit AI, GUI/menu flows, async pathfinding, compatibility layers, and faction/claim logic.

**Completed ranged-combat support for leader/scout variants:**
- Problem: Multiple entity classes explicitly reject bows/crossbows with TODO markers instead of implementing full behavior.
- Blocks: Consistent item pickup/equipment behavior in `src/main/java/com/talhanation/recruits/entities/ScoutEntity.java`, `src/main/java/com/talhanation/recruits/entities/CommanderEntity.java`, and `src/main/java/com/talhanation/recruits/entities/CaptainEntity.java`.

## Test Coverage Gaps

**Async AI and navigation:**
- What's not tested: Path queue saturation, callback ordering, null-path fallbacks, and server-thread handoff behavior.
- Files: `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathProcessor.java`, `src/main/java/com/talhanation/recruits/pathfinding/AsyncPath.java`, `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathNavigation.java`, `src/main/java/com/talhanation/recruits/entities/ai/async/AsyncManager.java`
- Risk: Deadlocks, race conditions, and hard-to-reproduce AI stalls can ship unnoticed.
- Priority: High

**GUI entity lookup and world-map flows:**
- What's not tested: Menu opening with distant recruits, claim-map visibility rules, route persistence, and map tile save/load behavior.
- Files: `src/main/java/com/talhanation/recruits/init/ModScreens.java`, `src/main/java/com/talhanation/recruits/client/gui/faction/FactionInspectionScreen.java`, `src/main/java/com/talhanation/recruits/client/gui/worldmap/WorldMapScreen.java`, `src/main/java/com/talhanation/recruits/world/RecruitsRoute.java`
- Risk: Players hit null menus, stale UI state, or corrupted client-side route/map data without automated detection.
- Priority: High

**Cross-mod compatibility layer:**
- What's not tested: Reflection-backed integrations with Small Ships, Musket Mod, Corpse, and other optional dependencies.
- Files: `src/main/java/com/talhanation/recruits/compat/SmallShips.java`, `src/main/java/com/talhanation/recruits/compat/BlunderbussWeapon.java`, `src/main/java/com/talhanation/recruits/compat/MusketWeapon.java`, `src/main/java/com/talhanation/recruits/compat/PistolWeapon.java`, `build.gradle`
- Risk: Upstream mod updates break integrations at runtime instead of during CI.
- Priority: Medium

---

*Concerns audit: 2026-04-05*
