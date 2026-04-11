# Codebase Concerns

**Analysis Date:** 2026-04-11

## Tech Debt

**Merged test pipeline does not execute legacy workers tests:**
- Issue: The root `test` source set in `build.gradle` includes `src/test/java` and `recruits/src/test/java`, but excludes `workers/src/test/java` even though worker tests still exist there.
- Files: `build.gradle`, `workers/src/test/java/com/talhanation/workers/network/WorkAreaAuthoringRulesTest.java`, `workers/src/test/java/com/talhanation/workers/entities/WorkerBindingResumeTest.java`
- Impact: Worker-specific regressions can pass CI unnoticed because the root merged build does not run the legacy workers test suite.
- Fix approach: Either add `workers/src/test/java` to the root `test` source set in `build.gradle` or migrate those tests into `src/test/java` and delete the split test layout.

**Merged game-test pipeline covers recruits only:**
- Issue: The root `gametest` source set in `build.gradle` includes `src/gametest/java` and `recruits/src/gametest/java`, while `workers` has no merged gametest source set and `src/gametest/java` is effectively empty.
- Files: `build.gradle`, `recruits/src/gametest/java/com/talhanation/recruits/gametest/command/CommandAuthorityGameTests.java`, `src/gametest/java/.gitkeep`
- Impact: The `check` task runs `runGameTestServer`, but merged worker flows such as work-area authoring, merchant editing, and structure copying have no game-level verification in the active runtime.
- Fix approach: Add worker-focused game tests under `src/gametest/java` or include a `workers/src/gametest/java` source directory in the merged root build.

**Workers resources are merged through custom copy rules instead of first-class source sets:**
- Issue: `workers/src/main/resources` is excluded from `sourceSets.main.resources` and then selectively copied and renamed in `processResources`.
- Files: `build.gradle`, `workers/src/main/resources/assets/workers/**`, `recruits/src/main/resources/assets/bannermod/**`
- Impact: Asset and structure behavior depends on build-time remapping rules, so namespace or path changes are easy to miss and difficult to validate locally.
- Fix approach: Consolidate workers resources into merged `bannermod` paths directly or add explicit verification tests for the `processResources` remap output.

**Large brownfield classes centralize too many behaviors:**
- Issue: Several core classes are very large and mix unrelated responsibilities such as networking state, AI orchestration, persistence, rendering setup, and interaction rules.
- Files: `recruits/src/main/java/com/talhanation/recruits/entities/AbstractRecruitEntity.java`, `workers/src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java`, `workers/src/main/java/com/talhanation/workers/entities/ai/BuilderWorkGoal.java`, `recruits/src/main/java/com/talhanation/recruits/client/gui/worldmap/WorldMapScreen.java`
- Impact: Small changes have wide blast radius, code review is harder, and future 1.21.x migration work stays coupled to giant files.
- Fix approach: Extract pure decision helpers and packet/state adapters first, then shrink entity and screen classes behind tested helper types.

## Known Bugs

**Merged runtime still registers a recruits update checker that looks for the old mod id:**
- Symptoms: Login or server-start update checks can fail with an `Optional.get()` crash path because the code still queries `ModList` for `"recruits"` while the active merged mod id is `"bannermod"`.
- Files: `recruits/src/main/java/com/talhanation/recruits/UpdateChecker.java`, `recruits/src/main/java/com/talhanation/recruits/init/ModLifecycleRegistrar.java`, `recruits/src/main/java/com/talhanation/recruits/Main.java`
- Trigger: Any runtime path that fires `PlayerLoggedInEvent` or `ServerStartedEvent` after `ModLifecycleRegistrar.registerRuntimeListeners()` registers `new UpdateChecker()`.
- Workaround: Disable or remove the recruits update checker in merged builds until it resolves `Main.MOD_ID` instead of the legacy literal.

**Two update checkers are active in the merged runtime:**
- Symptoms: The merged mod can emit duplicate update-check behavior and mixed branding (`Villager Recruits` and `Villager Workers`) for one root runtime.
- Files: `recruits/src/main/java/com/talhanation/recruits/init/ModLifecycleRegistrar.java`, `workers/src/main/java/com/talhanation/workers/init/WorkersLifecycleRegistrar.java`, `recruits/src/main/java/com/talhanation/recruits/UpdateChecker.java`, `workers/src/main/java/com/talhanation/workers/UpdateChecker.java`
- Trigger: Normal login and server-start events in the merged `bannermod` runtime.
- Workaround: Keep only one merged update checker and retire the legacy-specific listener.

## Security Considerations

**Build-area update packets lack the access checks used by other worker authoring packets:**
- Risk: Any nearby client that knows a `BuildArea` UUID can change dimensions, replace structure NBT, start building, cancel a build, or request creative-mode building without owner/team/admin validation.
- Files: `workers/src/main/java/com/talhanation/workers/network/MessageUpdateBuildArea.java`, `workers/src/main/java/com/talhanation/workers/network/MessageRotateWorkArea.java`, `workers/src/main/java/com/talhanation/workers/network/MessageUpdateWorkArea.java`, `workers/src/main/java/com/talhanation/workers/network/MessageUpdateOwner.java`
- Current mitigation: Other work-area packets use `workArea.getAuthoringAccess(player)` and `WorkAreaAuthoringRules.modifyDecision(...)`, but `MessageUpdateBuildArea` does not.
- Recommendations: Apply the same authorization gate in `MessageUpdateBuildArea.executeServerSide()`, reject unauthorized requests before deserializing or mutating NBT, and add regression tests beside `workers/src/test/java/com/talhanation/workers/network/WorkAreaAuthoringRulesTest.java`.

**Client-provided compressed NBT is accepted with weak validation:**
- Risk: The server accepts arbitrary compressed structure payloads from `FriendlyByteBuf` and writes them directly into `BuildArea` state.
- Files: `workers/src/main/java/com/talhanation/workers/network/MessageUpdateBuildArea.java`, `workers/src/main/java/com/talhanation/workers/entities/workarea/BuildArea.java`
- Current mitigation: IO failures fall back to an empty `CompoundTag`, but there is no schema validation, size cap, or ownership check in the packet handler.
- Recommendations: Enforce payload size and schema bounds, validate expected keys before assignment, and reject malformed data instead of silently storing a fallback tag.

## Performance Bottlenecks

**Worker idle and loot handling performs per-entity scans on hot ticks:**
- Problem: `AbstractWorkerEntity.aiStep()` searches for nearby item entities every server tick, and multiple worker goals also scan 64-block areas to rediscover work areas or storage.
- Files: `workers/src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java`, `workers/src/main/java/com/talhanation/workers/entities/ai/FarmerWorkGoal.java`, `workers/src/main/java/com/talhanation/workers/entities/ai/LumberjackWorkGoal.java`, `workers/src/main/java/com/talhanation/workers/entities/ai/MinerWorkGoal.java`, `workers/src/main/java/com/talhanation/workers/entities/ai/BuilderWorkGoal.java`, `workers/src/main/java/com/talhanation/workers/entities/ai/AbstractChestGoal.java`
- Cause: Repeated `getEntitiesOfClass(...)` queries run from entity tick logic rather than cached area assignment or lower-frequency refresh loops.
- Improvement path: Cache current work targets/storage references, throttle rediscovery scans, and profile worker-heavy villages before adding more behavior.

**Recruit combat and command logic also relies on broad repeated entity scans:**
- Problem: Recruit AI and leader logic repeatedly search wide bounding boxes, including 32-100 block scans in core entity classes and goals.
- Files: `recruits/src/main/java/com/talhanation/recruits/entities/AbstractRecruitEntity.java`, `recruits/src/main/java/com/talhanation/recruits/entities/AbstractLeaderEntity.java`, `recruits/src/main/java/com/talhanation/recruits/entities/ai/RecruitProtectEntityGoal.java`, `recruits/src/main/java/com/talhanation/recruits/entities/ai/RecruitUpkeepEntityGoal.java`
- Cause: Search-heavy AI decisions are embedded directly inside tick-driven gameplay code.
- Improvement path: Centralize target selection caches and reduce duplicate spatial queries for nearby allies, enemies, and upkeep targets.

## Fragile Areas

**Structure file handling mixes client-only APIs, migration logic, and filesystem writes:**
- Files: `workers/src/main/java/com/talhanation/workers/world/StructureManager.java`, `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java`
- Why fragile: `StructureManager` depends on `Minecraft.getInstance()`, local filesystem paths, resource-copy bootstrap, and legacy-id migration in one class; failures are mostly `printStackTrace()` or silent ignores.
- Safe modification: Change one path at a time and verify scan save/load, default structure copy, and legacy-id migration together.
- Test coverage: No root tests currently exercise `StructureManager.saveStructureToFile()`, `loadScanNbt()`, or `copyDefaultStructuresIfMissing()`.

**Merged networking depends on implicit packet ordering and shared channel offsets:**
- Files: `recruits/src/main/java/com/talhanation/recruits/Main.java`, `recruits/src/main/java/com/talhanation/recruits/network/RecruitsNetworkRegistrar.java`, `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java`, `workers/src/main/java/com/talhanation/workers/network/WorkersNetworkRegistrar.java`
- Why fragile: Recruits and workers share one channel, with workers relying on a hard-coded offset from `WorkersRuntime.networkIdOffset()`.
- Safe modification: Treat packet registration order and offsets as compatibility contracts; add tests before inserting, deleting, or reordering network registrations.
- Test coverage: Root tests only smoke-test the offset constant in `src/test/java/com/talhanation/workers/WorkersRuntimeSmokeTest.java`.

**Core worker control and build flows are split across entity state, packets, and UI without end-to-end merged tests:**
- Files: `workers/src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java`, `workers/src/main/java/com/talhanation/workers/entities/workarea/BuildArea.java`, `workers/src/main/java/com/talhanation/workers/network/MessageUpdateBuildArea.java`, `workers/src/main/java/com/talhanation/workers/client/gui/BuildAreaScreen.java`
- Why fragile: Server-authoritative ownership, screen state, structure payloads, and build execution are coupled but validated mostly by isolated helper tests.
- Safe modification: Change packet contracts and `BuildArea` state transitions together, then verify from UI action through server mutation.
- Test coverage: There are helper-level tests such as `workers/src/test/java/com/talhanation/workers/network/WorkAreaAuthoringRulesTest.java`, but the merged root build does not execute that directory.

## Scaling Limits

**Entity-heavy villages scale poorly with worker and recruit counts:**
- Current capacity: Not formally bounded in config or code.
- Limit: Tick cost rises with population because both recruits and workers repeatedly scan nearby entities, items, work areas, or targets from per-entity tick logic.
- Scaling path: Add sampling/profiling around the scan-heavy files above, then replace repeated local searches with cached ownership/work-area indexes.

## Dependencies at Risk

**`jcenter()` is still present in the root buildscript repositories:**
- Risk: JCenter is deprecated and can fail unpredictably for new environments or dependency resolution changes.
- Impact: Fresh development setup and CI reproducibility depend on a retired repository path in `build.gradle`.
- Migration plan: Remove `jcenter()` from `build.gradle` once all plugins and transitive dependencies resolve from `mavenCentral()`, Forge, Sponge, or explicit vendor repositories.

**CurseMaven-hosted binary dependencies remain a reproducibility risk:**
- Risk: The root build depends on opaque CurseMaven artifact IDs for runtime mods and compatibility libraries.
- Impact: If upstream artifacts disappear or are republished, local rebuilds of `build.gradle` can break without source changes.
- Migration plan: Mirror critical dependencies or document exact replacement coordinates for packages declared in `build.gradle` such as `curse.maven:small-ships-450659:5566900` and `curse.maven:ewewukeks-musket-mod-354562:5779561`.

## Missing Critical Features

**No merged automated coverage for worker filesystem and structure-bootstrap flows:**
- Problem: The active merged runtime depends on structure migration and copied default worker structures, but no root test suite covers those behaviors.
- Blocks: Safe refactoring of `workers/src/main/java/com/talhanation/workers/world/StructureManager.java` and `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java`.

## Test Coverage Gaps

**Unauthorized build-area mutation is not regression-tested in the active root suite:**
- What's not tested: Owner/team/admin enforcement for `workers/src/main/java/com/talhanation/workers/network/MessageUpdateBuildArea.java`.
- Files: `workers/src/main/java/com/talhanation/workers/network/MessageUpdateBuildArea.java`, `workers/src/test/java/com/talhanation/workers/network/WorkAreaAuthoringRulesTest.java`, `src/test/java/com/talhanation/workers/WorkersRuntimeSmokeTest.java`
- Risk: Access-control regressions can ship because the helper authorization tests do not cover the packet that currently bypasses them.
- Priority: High

**Merged build does not validate worker legacy tests or worker-side regression helpers:**
- What's not tested: The active root `test` task does not execute worker helper and network tests stored under `workers/src/test/java`.
- Files: `build.gradle`, `workers/src/test/java/com/talhanation/workers/entities/ai/BuilderBuildProgressTest.java`, `workers/src/test/java/com/talhanation/workers/network/MessageRecoverWorkerControlTest.java`
- Risk: Worker regressions appear only after manual playtesting or after moving tests into the merged root tree.
- Priority: High

**IO failure handling is untested and currently degrades inconsistently:**
- What's not tested: `printStackTrace()` and ignored-exception paths during structure save/load and build-area packet decode.
- Files: `workers/src/main/java/com/talhanation/workers/world/StructureManager.java`, `workers/src/main/java/com/talhanation/workers/network/MessageUpdateBuildArea.java`, `workers/src/main/java/com/talhanation/workers/client/gui/widgets/ScrollDropDownMenuWithFolders.java`, `recruits/src/main/java/com/talhanation/recruits/pathfinding/AsyncPathfinder.java`
- Risk: Filesystem or decode failures can surface as silent corruption, empty state, or noisy logs without predictable player-facing recovery.
- Priority: Medium

---

*Concerns audit: 2026-04-11*
