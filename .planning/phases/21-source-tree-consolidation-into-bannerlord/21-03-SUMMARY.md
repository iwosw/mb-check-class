---
phase: 21-source-tree-consolidation-into-bannerlord
plan: 03
subsystem: bootstrap-network-registry
tags: [bannermod, bootstrap, network, registry, composition-root, deprecated-forwarders, wave-3]

dependency_graph:
  requires:
    - phase: 21-02
      provides: "Canonical shared-seam ownership under bannermod.shared.*; @Deprecated forwarders at legacy bannermod.{authority,settlement,logistics}"
  provides:
    - "Single @Mod(\"bannermod\") entrypoint at com.talhanation.bannermod.bootstrap.BannerModMain"
    - "Shared SimpleChannel owned by BannerModNetworkBootstrap: recruits [0..103], workers [104..123]"
    - "Registry holders under bannermod.registry.military.* (8 classes from recruits/init)"
    - "Registry holders under bannermod.registry.civilian.* (8 classes from workers/init)"
    - "bannermod.bootstrap.WorkersRuntime/Subsystem/MergedRuntimeCleanupPolicy as outer-repo canonical copies"
    - "@Deprecated forwarder shells at recruits.Main and workers.WorkersMain (no @Mod)"
    - "migrated init/Mod* deleted from both clones; clone bannerlord references retargeted to BannerModMain"
  affects:
    - "21-04 (events + commands + config) -- can register against BannerModMain lifecycle directly"
    - "21-05..21-08 -- all waves have stable bootstrap/network/registry import targets"
    - "workers.WorkersRuntime, workers.WorkersSubsystem -- forwarder shells now import BannerModMain"

tech-stack:
  added: []
  patterns:
    - "@Mod single entrypoint: BannerModMain is the only @Mod class in outer src/main/java/"
    - "Registry holder copy pattern: files copied from clone init/ to bannermod.registry.{military,civilian}/ with package + MOD_ID rewrite only; entity/block/item type imports left pointing at recruits/workers packages (later waves)"
    - "Runtime glue outer-repo copy: WorkersRuntime/Subsystem/MergedRuntimeCleanupPolicy copied verbatim with package rewrite; clone counterparts reduced to @Deprecated stubs"

key-files:
  created:
    - "src/main/java/com/talhanation/bannermod/bootstrap/BannerModMain.java"
    - "src/main/java/com/talhanation/bannermod/bootstrap/BannerModLifecycle.java"
    - "src/main/java/com/talhanation/bannermod/bootstrap/WorkersRuntime.java"
    - "src/main/java/com/talhanation/bannermod/bootstrap/WorkersSubsystem.java"
    - "src/main/java/com/talhanation/bannermod/bootstrap/MergedRuntimeCleanupPolicy.java"
    - "src/main/java/com/talhanation/bannermod/network/BannerModNetworkBootstrap.java"
    - "src/main/java/com/talhanation/bannermod/registry/military/ModBlocks.java"
    - "src/main/java/com/talhanation/bannermod/registry/military/ModEntityTypes.java"
    - "src/main/java/com/talhanation/bannermod/registry/military/ModItems.java"
    - "src/main/java/com/talhanation/bannermod/registry/military/ModPois.java"
    - "src/main/java/com/talhanation/bannermod/registry/military/ModProfessions.java"
    - "src/main/java/com/talhanation/bannermod/registry/military/ModScreens.java"
    - "src/main/java/com/talhanation/bannermod/registry/military/ModShortcuts.java"
    - "src/main/java/com/talhanation/bannermod/registry/military/ModSounds.java"
    - "src/main/java/com/talhanation/bannermod/registry/civilian/ModBlocks.java"
    - "src/main/java/com/talhanation/bannermod/registry/civilian/ModEntityTypes.java"
    - "src/main/java/com/talhanation/bannermod/registry/civilian/ModItems.java"
    - "src/main/java/com/talhanation/bannermod/registry/civilian/ModMenuTypes.java"
    - "src/main/java/com/talhanation/bannermod/registry/civilian/ModPois.java"
    - "src/main/java/com/talhanation/bannermod/registry/civilian/ModProfessions.java"
    - "src/main/java/com/talhanation/bannermod/registry/civilian/ModShortcuts.java"
    - "src/main/java/com/talhanation/bannermod/registry/civilian/ModSounds.java"
  modified:
    - "recruits/src/main/java/com/talhanation/recruits/Main.java -- @Deprecated added; BannerlordMain -> BannerModMain"
    - "recruits/src/main/java/com/talhanation/recruits/init/ModLifecycleRegistrar.java -- reduced to @Deprecated stub"
    - "recruits/src/main/java/com/talhanation/recruits/network/RecruitsNetworkRegistrar.java -- reduced to @Deprecated stub"
    - "workers/src/main/java/com/talhanation/workers/WorkersMain.java -- @Mod removed, @Deprecated added, body reduced"
    - "workers/src/main/java/com/talhanation/workers/WorkersRuntime.java -- BannerlordMain -> BannerModMain"
    - "workers/src/main/java/com/talhanation/workers/WorkersSubsystem.java -- bannerlord imports removed, @Deprecated"
    - "workers/src/main/java/com/talhanation/workers/init/WorkersLifecycleRegistrar.java -- reduced to @Deprecated stub"
    - "workers/src/main/java/com/talhanation/workers/network/WorkersNetworkRegistrar.java -- reduced to @Deprecated stub"
    - "MERGE_NOTES.md -- Wave 3 composition-root migration section appended"

key-decisions:
  - "Recruit network message catalog size = 104 (matches existing WorkersRuntime.ROOT_NETWORK_ID_OFFSET = 104); WORKER_PACKET_OFFSET constant locked at 104."
  - "Entity/block/item type imports in registry holders intentionally left pointing at recruits/workers packages -- later waves (21-05..21-07) will move those types; changing imports now would be premature."
  - "WorkersSubsystem and WorkersNetworkRegistrar/WorkersLifecycleRegistrar in clones reduced to @Deprecated stubs rather than deleted -- clone dev tests may still reference them transiently per D-24."
  - "BannerModMain.clientSetup omits WorkersClientManager.instance() registration -- WorkersClientManager was not visible from the workers source root at wave-3; deferred to wave 4+ when client event infrastructure moves."

requirements-completed:
  - SRCMOVE-02

duration: ~13min
completed: 2026-04-15T09:31:31Z
---

# Phase 21 Plan 03: Bootstrap + Network + Registry Composition-Root Migration Summary

**Unified `@Mod("bannermod")` entrypoint created at `BannerModMain`, shared `SimpleChannel` owned by `BannerModNetworkBootstrap` (recruits [0..103], workers [104..123]), and all `init/Mod*` registry holders copied into `bannermod.registry.{military,civilian}.*` with clone counterparts demoted or deleted.**

## Performance

- **Duration:** ~13 min
- **Completed:** 2026-04-15T09:31:31Z
- **Tasks:** 2 atomic task commits + 2 clone commits per task
- **Files modified:** 22 new outer-repo files + 8 clone file modifications + 16 clone file deletions + MERGE_NOTES

## Accomplishments

- Created `BannerModMain` as the single `@Mod("bannermod")` entrypoint replacing both `recruits.Main` and `workers.WorkersMain`. Wires lifecycle, DeferredRegisters from both registry subtrees, shared channel creation, compat flag detection, client setup, and creative tab entries.
- Created `BannerModNetworkBootstrap` with `createSharedChannel()` and `workerPacketOffset()`. Registers 104 recruit messages at `[0..103]` and 20 worker messages at `[104..123]` verbatim from the legacy arrays.
- Copied 8 military registry holders (`ModBlocks/EntityTypes/Items/Pois/Professions/Screens/Shortcuts/Sounds`) from `recruits/init/` into `bannermod.registry.military.*` with package and MOD_ID rewritten.
- Copied 8 civilian registry holders (`ModBlocks/EntityTypes/Items/MenuTypes/Pois/Professions/Shortcuts/Sounds`) from `workers/init/` into `bannermod.registry.civilian.*` with package and MOD_ID rewritten.
- Copied Phase-20 runtime glue (`WorkersRuntime`, `WorkersSubsystem`, `MergedRuntimeCleanupPolicy`) into `bannermod.bootstrap.*`; clone counterparts reduced to `@Deprecated` forwarder stubs with bannerlord imports removed.
- Demoted `recruits.Main` (added `@Deprecated`) and `workers.WorkersMain` (`@Mod` removed, `@Deprecated` added, body reduced to forwarder statics onto `BannerModMain`).
- `git rm`'d all 16 migrated `init/Mod*.java` files from both clones (8 in recruits, 8 in workers).
- Fixed all surviving `com.talhanation.{recruits,workers}.init.*` FQN references in outer `bannermod.*` tree (retargeted `bannermod.bootstrap.WorkersSubsystem` static imports to `bannermod.registry.civilian.*`).
- Appended Wave-3 migration record to `MERGE_NOTES.md`.

## Task Commits

**Outer repo:**
1. **Task 1: BannerModMain + network + registry holders** -- `875b030` (feat)
2. **Task 2: Demote forwarders + FQN fixes + MERGE_NOTES** -- `a43acdf` (feat)

**recruits/ clone:**
1. **fix: retarget from BannerlordMain to BannerModMain** -- `04d7898b`
2. **feat: demote Main to @Deprecated + rm init/Mod*** -- `d45d0955`

**workers/ clone:**
1. **fix: retarget from BannerlordMain to BannerModMain** -- `a3e0892`
2. **feat: demote WorkersMain + rm init/Mod*** -- `2f806fd`

## Files Created (22 outer-repo files)

- `bannermod.bootstrap`: `BannerModMain`, `BannerModLifecycle`, `WorkersRuntime`, `WorkersSubsystem`, `MergedRuntimeCleanupPolicy`
- `bannermod.network`: `BannerModNetworkBootstrap`
- `bannermod.registry.military`: `ModBlocks`, `ModEntityTypes`, `ModItems`, `ModPois`, `ModProfessions`, `ModScreens`, `ModShortcuts`, `ModSounds`
- `bannermod.registry.civilian`: `ModBlocks`, `ModEntityTypes`, `ModItems`, `ModMenuTypes`, `ModPois`, `ModProfessions`, `ModShortcuts`, `ModSounds`

## Files Deleted from Clones (16 total)

- `recruits/init/Mod{Blocks,EntityTypes,Items,Pois,Professions,Screens,Shortcuts,Sounds}.java`
- `workers/init/Mod{Blocks,EntityTypes,Items,MenuTypes,Pois,Professions,Shortcuts,Sounds}.java`

## Decisions Made

- **WORKER_PACKET_OFFSET = 104**: matches the pre-existing `WorkersRuntime.ROOT_NETWORK_ID_OFFSET = 104` constant established in Phase 20; recruit catalog count verified as 104 matching message classes in `recruits/network/`.
- **Entity/block/item imports not rewritten**: type imports in registry holders still point at `recruits.*` and `workers.*` entity/block/item classes — those types stay in the clones until waves 21-05..21-07 move them. Rewriting imports now would create dangling references. Per D-22, compile is not required to be clean between waves.
- **Clone stubs retained, not deleted**: `WorkersLifecycleRegistrar`, `WorkersNetworkRegistrar`, `RecruitsNetworkRegistrar`, `ModLifecycleRegistrar` reduced to `@Deprecated` stubs rather than deleted. Clone-internal dev tests may reference them transiently (D-24).

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Clone files referenced reverted `BannerlordMain` (bannerlord namespace reverted in 21-01)**
- **Found during:** Task 1, reading source files
- **Issue:** `workers.WorkersRuntime`, `workers.WorkersSubsystem`, `workers.WorkersNetworkRegistrar`, `workers.init.WorkersLifecycleRegistrar`, `recruits.Main`, `recruits.init.ModLifecycleRegistrar`, `recruits.network.RecruitsNetworkRegistrar` all imported or extended `com.talhanation.bannerlord.*` classes that were fully reverted in 21-01 and no longer exist. The clones were left in a broken state after the revert.
- **Fix:** Retargeted all `BannerlordMain` imports to `BannerModMain`; reduced classes that extended reverted abstract `bannerlord.*` bases to `@Deprecated` stub shells; fixed `WorkersSubsystem` to not use `bannerlord.network/registry` packages.
- **Files modified:** 7 clone files across recruits/ and workers/
- **Commits:** `04d7898b` (recruits), `a3e0892` (workers)

**2. [Rule 2 - Missing critical functionality] `mods.toml` already correct — no change needed**
- **Found during:** Task 1 verification
- **Issue:** Plan action step 7 said to set `modId="bannermod"` in `src/main/resources/META-INF/mods.toml`. On inspection the file does not exist at that path — the active `mods.toml` is in `recruits/src/main/resources/META-INF/mods.toml` and already has `modId="bannermod"`. No action required.
- **Fix:** No change; documented here.

## Known Stubs

- **`BannerModLifecycle.onRegisterCommands`**: empty body, marked as "filled in by wave 4". Not flow-blocking — commands registered in wave 4 (21-04).
- **Entity/block/item type imports in registry holders**: still point at clone packages (`com.talhanation.recruits.*`, `com.talhanation.workers.*`). These will compile fine as long as the clone source trees are in `build.gradle` sourceSets (they are, per D-22). Waves 21-05..21-07 will move the types and retarget imports.

## Threat Flags

None. No new network endpoints beyond the packet catalog already established in prior phases. The `BannerModNetworkBootstrap` registers exactly the same message set as the two legacy `setup()` methods, at the same indices.

## Self-Check

- FOUND: `src/main/java/com/talhanation/bannermod/bootstrap/BannerModMain.java`
- FOUND: `@Mod("bannermod")` in BannerModMain.java
- FOUND: `src/main/java/com/talhanation/bannermod/network/BannerModNetworkBootstrap.java`
- FOUND: `workerPacketOffset` in BannerModNetworkBootstrap.java
- FOUND: `src/main/java/com/talhanation/bannermod/registry/military/` directory
- FOUND: `src/main/java/com/talhanation/bannermod/registry/civilian/` directory
- VERIFIED: @Mod count in outer src/main/java/ = 1
- VERIFIED: @Deprecated on recruits.Main
- VERIFIED: @Deprecated on workers.WorkersMain; @Mod removed
- VERIFIED: recruits init/Mod*.java deleted (ModLifecycleRegistrar retained — not a registry holder)
- VERIFIED: workers init/Mod*.java deleted
- VERIFIED: no com.talhanation.recruits.init.* or com.talhanation.workers.init.* FQN refs in bannermod/
- COMMITS: 875b030, a43acdf in outer repo

## Self-Check: PASSED

---
*Phase: 21-source-tree-consolidation-into-bannerlord*
*Completed: 2026-04-15*
