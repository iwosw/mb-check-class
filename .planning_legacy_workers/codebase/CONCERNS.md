# Codebase Concerns

**Analysis Date:** 2026-04-05

## Tech Debt

**Config drift and partially wired settings:**
- Issue: Several gameplay costs and toggles are defined in `WorkersServerConfig` but are bypassed or miswired in runtime code. `AnimalPenMaxAnimals` is declared with the config key `"LumberjackCost"`, so the intended setting is never exposed correctly. `BuilderActive` is declared but not read anywhere. Multiple worker entities hardcode hire costs instead of using config values.
- Files: `src/main/java/com/talhanation/workers/config/WorkersServerConfig.java`, `src/main/java/com/talhanation/workers/entities/BuilderEntity.java`, `src/main/java/com/talhanation/workers/entities/FarmerEntity.java`, `src/main/java/com/talhanation/workers/entities/FishermanEntity.java`, `src/main/java/com/talhanation/workers/entities/LumberjackEntity.java`, `src/main/java/com/talhanation/workers/entities/MinerEntity.java`, `src/main/java/com/talhanation/workers/entities/AnimalFarmerEntity.java`, `src/main/java/com/talhanation/workers/VillagerEvents.java`
- Impact: Server operators cannot trust config values to control gameplay balance. Changes to `BuilderCost` can unintentionally affect unrelated hire trades, while `AnimalPenMaxAnimals` and `BuilderActive` are effectively dead settings.
- Fix approach: Make every worker hire cost read from `WorkersServerConfig`, correct the `AnimalPenMaxAnimals` key name, either wire `BuilderActive` into spawn/trade registration or remove it, and add a startup validation pass that logs unused or duplicate config keys.

**Large state-machine classes with duplicated logic:**
- Issue: Core AI behavior is concentrated in very large classes with repeated selection, movement, and scan loops. `BuilderWorkGoal`, `MinerWorkGoal`, `FarmerWorkGoal`, `LumberjackWorkGoal`, `AnimalFarmerWorkGoal`, and `MerchantEntity` each mix pathing, inventory rules, world mutation, and UI-facing messaging in one class.
- Files: `src/main/java/com/talhanation/workers/entities/ai/BuilderWorkGoal.java`, `src/main/java/com/talhanation/workers/entities/ai/MinerWorkGoal.java`, `src/main/java/com/talhanation/workers/entities/ai/FarmerWorkGoal.java`, `src/main/java/com/talhanation/workers/entities/ai/LumberjackWorkGoal.java`, `src/main/java/com/talhanation/workers/entities/ai/AnimalFarmerWorkGoal.java`, `src/main/java/com/talhanation/workers/entities/MerchantEntity.java`, `src/main/java/com/talhanation/workers/entities/ai/RecruitsPathNodeEvaluator.java`
- Impact: Behavior changes require editing high-risk files with many branches and implicit state transitions. Regressions are likely because there are no smaller seam points for testing.
- Fix approach: Extract shared worker phases into reusable helpers, isolate scanning/pathing/material planning into dedicated services, and move player messaging out of AI state machines.

**Dead or placeholder code remains in production sources:**
- Issue: The repository includes stubs and debug-only code that are not integrated cleanly. `CommandEvents` is effectively empty, `DebugSyncWorkerPathNavigation` is marked debug-only, and `AbstractWorkerEntity` still contains commented debug navigation hooks and TODO markers.
- Files: `src/main/java/com/talhanation/workers/CommandEvents.java`, `src/main/java/com/talhanation/workers/entities/ai/DebugSyncWorkerPathNavigation.java`, `src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java`, `src/main/java/com/talhanation/workers/Translatable.java`
- Impact: Maintainers need to distinguish live code from abandoned experiments by inspection. That raises change risk and makes intent unclear.
- Fix approach: Remove obsolete classes/comments, or gate debugging utilities behind an explicit dev-only pathway.

## Known Bugs

**Animal pen limit config never applies:**
- Symptoms: Changing the animal pen cap in config has no effect.
- Files: `src/main/java/com/talhanation/workers/config/WorkersServerConfig.java`
- Trigger: Set `AnimalPenMaxAnimals` in config and restart.
- Workaround: None in code. The value is never referenced, and its config key is misdeclared as `"LumberjackCost"`.

**Fisherman and animal farmer trades use builder cost:**
- Symptoms: Hire prices for fisherman and animal farmer follow `BuilderCost` instead of dedicated values.
- Files: `src/main/java/com/talhanation/workers/VillagerEvents.java`
- Trigger: Server startup trade registration.
- Workaround: Adjust `BuilderCost` if matching those prices is acceptable; there is no separate config path for those trades.

**Several worker APIs expose null instead of empty values:**
- Symptoms: Callers must rely on convention to avoid `NullPointerException`. Examples include `inventoryInputHelp()` and `getAllowedItems()` returning `null` in multiple entities, and utility lookups returning `null` on normal control flow.
- Files: `src/main/java/com/talhanation/workers/entities/BuilderEntity.java`, `src/main/java/com/talhanation/workers/entities/FarmerEntity.java`, `src/main/java/com/talhanation/workers/entities/FishermanEntity.java`, `src/main/java/com/talhanation/workers/entities/LumberjackEntity.java`, `src/main/java/com/talhanation/workers/entities/MinerEntity.java`, `src/main/java/com/talhanation/workers/entities/AnimalFarmerEntity.java`, `src/main/java/com/talhanation/workers/entities/MerchantEntity.java`, `src/main/java/com/talhanation/workers/world/NeededItem.java`, `src/main/java/com/talhanation/workers/world/StructureManager.java`, `src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java`
- Trigger: Any new caller that assumes collection-returning methods are non-null, or code paths that forget to null-check structure/material lookups.
- Workaround: Guard every call site manually. Current codebase already does this inconsistently.

**Menu factories can return null containers/screens:**
- Symptoms: Invalid merchant/menu resolution results in `null` being returned from menu constructors and screen registration wrappers.
- Files: `src/main/java/com/talhanation/workers/init/ModMenuTypes.java`
- Trigger: Open a merchant screen when the target entity is missing or outside the hardcoded lookup radius.
- Workaround: Stay within range and keep the merchant loaded. There is no graceful fallback screen or error container.

## Security Considerations

**Server packet handlers trust proximity but do not enforce ownership/authorization:**
- Risk: A nearby player can attempt to modify or delete work areas and merchant data without proving they own the target object. Several handlers only search nearby entities by UUID and then mutate them directly.
- Files: `src/main/java/com/talhanation/workers/network/MessageUpdateOwner.java`, `src/main/java/com/talhanation/workers/network/MessageUpdateWorkArea.java`, `src/main/java/com/talhanation/workers/network/MessageRotateWorkArea.java`, `src/main/java/com/talhanation/workers/network/MessageUpdateBuildArea.java`, `src/main/java/com/talhanation/workers/network/MessageUpdateMerchant.java`, `src/main/java/com/talhanation/workers/network/MessageUpdateMerchantTrade.java`, `src/main/java/com/talhanation/workers/network/MessageMoveMerchantTrade.java`, `src/main/java/com/talhanation/workers/network/MessageUpdateCropArea.java`, `src/main/java/com/talhanation/workers/network/MessageUpdateLumberArea.java`, `src/main/java/com/talhanation/workers/network/MessageUpdateMiningArea.java`, `src/main/java/com/talhanation/workers/network/MessageUpdateStorageArea.java`, `src/main/java/com/talhanation/workers/network/MessageUpdateAnimalPenArea.java`, `src/main/java/com/talhanation/workers/network/MessageUpdateMarketArea.java`
- Current mitigation: Some UI code hides controls from non-owners, and `MessageOpenMerchantEditTradeScreen` at least verifies the sender UUID matches the encoded player UUID.
- Recommendations: Validate owner UUID or team membership in every server-side mutating packet before applying changes. Treat client-sent UUIDs as untrusted input.

**Remote update check introduces external network dependency at login/startup:**
- Risk: `UpdateChecker` queries Forge version metadata when players join and when the server starts. Availability or latency of the remote version source can affect player-facing behavior and log noise.
- Files: `src/main/java/com/talhanation/workers/UpdateChecker.java`, `src/main/java/com/talhanation/workers/WorkersMain.java`
- Current mitigation: Failed checks only log errors.
- Recommendations: Make update checks opt-in or cache results per session so the server does not re-run network-dependent logic on every player login.

**Client-side file loading/saving trusts local path names:**
- Risk: Structure save/load uses plain filenames and local filesystem access under the Minecraft game directory. The current UI constrains selection through `ScrollDropDownMenuWithFolders`, but `StructureManager.loadScanNbt()` still accepts a relative path string and constructs a path directly.
- Files: `src/main/java/com/talhanation/workers/world/StructureManager.java`, `src/main/java/com/talhanation/workers/client/gui/BuildAreaScreen.java`, `src/main/java/com/talhanation/workers/client/gui/widgets/ScrollDropDownMenuWithFolders.java`
- Current mitigation: The load path originates from the in-game dropdown widget rather than arbitrary network input.
- Recommendations: Normalize and validate that resolved paths stay under `workers/scan`, and reject path traversal tokens before opening files.

## Performance Bottlenecks

**Per-worker entity scans run every tick:**
- Problem: `AbstractWorkerEntity.aiStep()` scans a 5.5-block inflated box for `ItemEntity` instances every server tick for every worker that can pick up loot.
- Files: `src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java`
- Cause: Item pickup logic calls `getEntitiesOfClass()` unconditionally inside `aiStep()`.
- Improvement path: Throttle scans to a larger interval, skip scans while pathing/working states do not need pickups, or maintain a narrower pickup trigger.

**Work-area rescans repeatedly traverse large volumes:**
- Problem: Mining, building, crop, and storage workflows repeatedly rescan full areas or chest lists while progressing through state machines.
- Files: `src/main/java/com/talhanation/workers/entities/workarea/BuildArea.java`, `src/main/java/com/talhanation/workers/entities/workarea/MiningArea.java`, `src/main/java/com/talhanation/workers/entities/workarea/CropArea.java`, `src/main/java/com/talhanation/workers/entities/workarea/StorageArea.java`, `src/main/java/com/talhanation/workers/entities/ai/BuilderWorkGoal.java`, `src/main/java/com/talhanation/workers/entities/ai/MinerWorkGoal.java`, `src/main/java/com/talhanation/workers/entities/ai/GetNeededItemsFromStorage.java`, `src/main/java/com/talhanation/workers/entities/ai/DepositItemsToStorage.java`
- Cause: `scanBreakArea()`, `scanForOresOnWalls()`, `scanFloorArea()`, and `scanStorageBlocks()` rebuild stacks/maps from world state instead of incrementally updating them.
- Improvement path: Cache scan results per work area, invalidate only on block changes or state transitions, and avoid full rescans after every partial progress update.

**Raycast-heavy block visibility checks in miner/builder selection:**
- Problem: Candidate mining/build blocks are filtered with repeated `level.clip()` visibility checks.
- Files: `src/main/java/com/talhanation/workers/entities/ai/MinerWorkGoal.java`, `src/main/java/com/talhanation/workers/entities/ai/BuilderWorkGoal.java`
- Cause: `canSeeBlock()` is called while pruning candidate stacks; `MinerWorkGoal` already labels this path "PERFORMANCE HEAVY DO NOT USE FREQUENTLY".
- Improvement path: Cache visibility decisions for the active target set, reduce reevaluation frequency, or use cheaper heuristic ordering before raycasts.

**Custom pathfinding evaluator is large and always-on for worker navigation:**
- Problem: Path cost calculation in `RecruitsPathNodeEvaluator` is extensive and runs inside movement-critical code.
- Files: `src/main/java/com/talhanation/workers/entities/ai/RecruitsPathNodeEvaluator.java`, `src/main/java/com/talhanation/workers/entities/ai/DebugSyncWorkerPathNavigation.java`
- Cause: The mod maintains a custom copy/fork of substantial `WalkNodeEvaluator` behavior with additional terrain scoring.
- Improvement path: Profile hot paths, isolate only the custom rules that differ from vanilla, and reduce per-node block lookups where possible.

**Client debug rendering scales with nearby work areas:**
- Problem: When hitboxes are enabled, the client renders a line box for every nearby work area within 100 blocks every frame.
- Files: `src/main/java/com/talhanation/workers/client/events/ScreenEvents.java`
- Cause: `onRenderLevel()` iterates `getEntitiesOfClass(AbstractWorkAreaEntity.class, mc.player.getBoundingBox().inflate(100))` each render stage.
- Improvement path: Cache the active list while the debug overlay is open, or reduce the search radius/render frequency.

## Fragile Areas

**Build pipeline around scanned structures and entity spawning:**
- Files: `src/main/java/com/talhanation/workers/world/StructureManager.java`, `src/main/java/com/talhanation/workers/entities/workarea/BuildArea.java`, `src/main/java/com/talhanation/workers/entities/ai/BuilderWorkGoal.java`, `src/main/java/com/talhanation/workers/client/gui/BuildAreaScreen.java`
- Why fragile: Structure scanning, rotation, material derivation, filesystem persistence, and post-build entity spawning are spread across client UI, shared world utilities, and server AI. `BuildArea` and `BuilderWorkGoal` both implement entity spawn restoration logic, which invites drift.
- Safe modification: Keep block rotation, relative-position math, and work-area entity restoration in one shared implementation before changing scan/build behavior.
- Test coverage: No automated tests detected under `src/test/` or matching `*.test.*` / `*.spec.*` patterns.

**Merchant screen and trade synchronization path:**
- Files: `src/main/java/com/talhanation/workers/entities/MerchantEntity.java`, `src/main/java/com/talhanation/workers/init/ModMenuTypes.java`, `src/main/java/com/talhanation/workers/client/gui/MerchantTradeScreen.java`, `src/main/java/com/talhanation/workers/network/MessageUpdateMerchant.java`, `src/main/java/com/talhanation/workers/network/MessageUpdateMerchantTrade.java`, `src/main/java/com/talhanation/workers/network/MessageMoveMerchantTrade.java`
- Why fragile: UI, container lookup, and trade mutation all depend on local entity resolution by UUID within short radii. Failures degrade into null returns or silent no-ops instead of recoverable errors.
- Safe modification: Preserve server-authoritative merchant resolution and replace null returns with explicit failure handling before extending trade features.
- Test coverage: No automated tests detected.

**Storage deposit/retrieval state machines:**
- Files: `src/main/java/com/talhanation/workers/entities/ai/GetNeededItemsFromStorage.java`, `src/main/java/com/talhanation/workers/entities/ai/DepositItemsToStorage.java`, `src/main/java/com/talhanation/workers/entities/workarea/StorageArea.java`
- Why fragile: Inventory mutation, chest interaction timing, retry loops, and player-facing error states are tightly interleaved. Small ordering changes can duplicate item movement or strand workers in retry states.
- Safe modification: Separate pathing/chest-open timing from item transfer rules, and favor immutable transfer results over in-place list mutation.
- Test coverage: No automated tests detected.

## Scaling Limits

**Worker count scales linearly with repeated area/entity scans:**
- Current capacity: Not explicitly bounded in code.
- Limit: Server cost grows with the number of active workers because each worker can scan nearby items, nearby work areas, and storage areas independently.
- Scaling path: Introduce shared registries for nearby work areas/storage, interval-based updates, and lower-frequency background scans.

**Large build/mining zones scale with block volume:**
- Current capacity: UI clamps build dimensions to 3-32 blocks per axis in `BuildAreaScreen`, but existing scans still traverse full volumes and wall-inflated regions.
- Limit: Full volume scans in `BuildArea.scanBreakArea()` and `MiningArea.scanBreakArea()` / `scanFloorArea()` become increasingly expensive as dimensions rise.
- Scaling path: Partition large work areas into chunks or incremental jobs and persist partial scan progress.

## Dependencies at Risk

**Forked/customized pathfinding maintenance burden:**
- Risk: `RecruitsPathNodeEvaluator` duplicates a large portion of vanilla pathfinding behavior with local modifications.
- Impact: Minecraft or Forge pathfinding changes are difficult to merge safely, and bug fixes must be ported manually.
- Migration plan: Reduce the fork surface to a decorator or targeted overrides around the exact custom heuristics.

**Build uses deprecated repository `jcenter()`:**
- Risk: The Gradle build still references `jcenter()`, which is deprecated and increasingly unreliable.
- Impact: Dependency resolution can break for new environments or mirrors.
- Migration plan: Remove `jcenter()` from `build.gradle` and keep dependencies on maintained repositories only.

## Missing Critical Features

**Automated regression test suite:**
- Problem: No unit, integration, or gametest sources are present in `src/test/`, and no `*.test.*` or `*.spec.*` files were detected.
- Blocks: Safe refactoring of AI, packet authorization, structure scanning, and merchant trade workflows.

**Centralized server-side authorization layer for packet mutations:**
- Problem: Ownership/team checks are scattered or absent across network handlers.
- Blocks: Secure extension of work-area editing, merchant management, and multiplayer permission features.

## Test Coverage Gaps

**AI state machines:**
- What's not tested: Worker transitions for mining, building, farming, animal handling, storage deposit, and storage retrieval.
- Files: `src/main/java/com/talhanation/workers/entities/ai/BuilderWorkGoal.java`, `src/main/java/com/talhanation/workers/entities/ai/MinerWorkGoal.java`, `src/main/java/com/talhanation/workers/entities/ai/FarmerWorkGoal.java`, `src/main/java/com/talhanation/workers/entities/ai/LumberjackWorkGoal.java`, `src/main/java/com/talhanation/workers/entities/ai/AnimalFarmerWorkGoal.java`, `src/main/java/com/talhanation/workers/entities/ai/GetNeededItemsFromStorage.java`, `src/main/java/com/talhanation/workers/entities/ai/DepositItemsToStorage.java`
- Risk: State regressions can ship unnoticed and typically surface only in live gameplay.
- Priority: High

**Network permission boundaries:**
- What's not tested: Whether non-owners can mutate work areas or merchant state through packets.
- Files: `src/main/java/com/talhanation/workers/network/MessageUpdateOwner.java`, `src/main/java/com/talhanation/workers/network/MessageUpdateWorkArea.java`, `src/main/java/com/talhanation/workers/network/MessageRotateWorkArea.java`, `src/main/java/com/talhanation/workers/network/MessageUpdateMerchant.java`, `src/main/java/com/talhanation/workers/network/MessageUpdateMerchantTrade.java`
- Risk: Multiplayer abuse can remain undetected until reported by players.
- Priority: High

**Structure scan/build round-trip:**
- What's not tested: Scan to NBT, save/load, rotation, material derivation, and spawned work-area restoration.
- Files: `src/main/java/com/talhanation/workers/world/StructureManager.java`, `src/main/java/com/talhanation/workers/entities/workarea/BuildArea.java`, `src/main/java/com/talhanation/workers/entities/ai/BuilderWorkGoal.java`, `src/main/java/com/talhanation/workers/client/gui/BuildAreaScreen.java`
- Risk: Rotation bugs, missing entities, or corrupted structure files can regress silently.
- Priority: High

**Dependency and deployment concerns not directly evidenced in source:**
- What's not tested: No additional dependency-risk monitoring or deployment validation code was detected beyond Gradle configuration.
- Files: `build.gradle`
- Risk: Build breakage from repository changes is caught late.
- Priority: Medium

---

*Concerns audit: 2026-04-05*
