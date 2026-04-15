---
phase: 21-source-tree-consolidation-into-bannerlord
plan: 04
subsystem: events-commands-config
tags: [bannermod, events, commands, config, utilities, wave-4]

dependency_graph:
  requires:
    - phase: 21-03
      provides: "Single @Mod(\"bannermod\") entrypoint BannerModMain; registry holders under bannermod.registry.{military,civilian}.*"
  provides:
    - "10 recruit event classes under bannermod.events.* (RecruitEvents, FactionEvents, ClaimEvents, CommandEvents, VillagerEvents, PillagerEvents, AssassinEvents, DamageEvent, DebugEvents, AttributeEvent)"
    - "3 workers event classes under bannermod.events.* (WorkersVillagerEvents, WorkersCommandEvents, WorkersAttributeEvent -- renamed to avoid collision)"
    - "Utilities bannermod.{UpdateChecker,WorkersUpdateChecker,Translatable}"
    - "Military commands under bannermod.commands.military.{PatrolSpawnCommand,RecruitsAdminCommands}"
    - "Config schemas under bannermod.config.{RecruitsClientConfig,RecruitsServerConfig,WorkersServerConfig}"
    - "BannerModMain: recruits config registered; commands wired; all event/config FQNs updated"
  affects:
    - "21-05..21-09 -- all waves have stable event/config/command import targets in bannermod.*"

tech-stack:
  added: []
  patterns:
    - "Collision rename convention: workers event classes that share names with recruits counterparts get Workers prefix (WorkersVillagerEvents, WorkersCommandEvents, WorkersAttributeEvent, WorkersUpdateChecker)"
    - "Bulk sed config import rewrite: com.talhanation.{recruits,workers}.config.* -> bannermod.config.* applied across full bannermod/ tree after copy"
    - "Clone callers left with stale imports per D-22: compile not required clean between waves"

key-files:
  created:
    - "src/main/java/com/talhanation/bannermod/events/RecruitEvents.java"
    - "src/main/java/com/talhanation/bannermod/events/FactionEvents.java"
    - "src/main/java/com/talhanation/bannermod/events/ClaimEvents.java"
    - "src/main/java/com/talhanation/bannermod/events/CommandEvents.java"
    - "src/main/java/com/talhanation/bannermod/events/VillagerEvents.java"
    - "src/main/java/com/talhanation/bannermod/events/PillagerEvents.java"
    - "src/main/java/com/talhanation/bannermod/events/AssassinEvents.java"
    - "src/main/java/com/talhanation/bannermod/events/DamageEvent.java"
    - "src/main/java/com/talhanation/bannermod/events/DebugEvents.java"
    - "src/main/java/com/talhanation/bannermod/events/AttributeEvent.java"
    - "src/main/java/com/talhanation/bannermod/events/WorkersVillagerEvents.java"
    - "src/main/java/com/talhanation/bannermod/events/WorkersCommandEvents.java"
    - "src/main/java/com/talhanation/bannermod/events/WorkersAttributeEvent.java"
    - "src/main/java/com/talhanation/bannermod/UpdateChecker.java"
    - "src/main/java/com/talhanation/bannermod/WorkersUpdateChecker.java"
    - "src/main/java/com/talhanation/bannermod/Translatable.java"
    - "src/main/java/com/talhanation/bannermod/commands/military/PatrolSpawnCommand.java"
    - "src/main/java/com/talhanation/bannermod/commands/military/RecruitsAdminCommands.java"
    - "src/main/java/com/talhanation/bannermod/config/RecruitsClientConfig.java"
    - "src/main/java/com/talhanation/bannermod/config/RecruitsServerConfig.java"
    - "src/main/java/com/talhanation/bannermod/config/WorkersServerConfig.java"
  modified:
    - "src/main/java/com/talhanation/bannermod/bootstrap/BannerModMain.java -- config registration added; commands wired; event/config imports retargeted"
    - "src/main/java/com/talhanation/bannermod/bootstrap/WorkersSubsystem.java -- VillagerEvents/CommandEvents imports retargeted to bannermod.events.Workers*"
    - "MERGE_NOTES.md -- Wave-4 migration section appended"
    - "Multiple bannermod/events/*.java -- recruits.config.* imports rewritten to bannermod.config.*"

key-decisions:
  - "Workers event class collision resolution: Workers prefix applied to VillagerEvents, CommandEvents, AttributeEvent, UpdateChecker when recruit counterparts with identical names already landed in bannermod.events.* and bannermod.*"
  - "workers.CommandEvents top-level file (not a commands/ subpackage) treated as event class, migrated to bannermod.events.WorkersCommandEvents -- workers has no commands/ directory"
  - "Existing bannermod.config.BannerModConfigFiles preserved; three new config classes land as siblings with no overlap in class names"
  - "Clone callers (recruits/network, recruits/pathfinding, etc.) still import com.talhanation.recruits.config.* which no longer exists in the clone -- per D-22 compile not required clean; addressed in waves 21-05..21-09"

requirements-completed:
  - SRCMOVE-02

duration: ~18min
completed: 2026-04-15T10:15:00Z
---

# Phase 21 Plan 04: Events + Commands + Config Wave-4 Migration Summary

**All recruit and worker event classes, top-level utilities, military command registrars, and config schemas moved from clone working trees into `bannermod.events.*`, `bannermod.commands.military.*`, `bannermod.config.*`, and `bannermod.*`; `BannerModMain` updated to reference new FQNs throughout.**

## Performance

- **Duration:** ~18 min
- **Completed:** 2026-04-15T10:15:00Z
- **Tasks:** 2 atomic task commits in outer repo + 4 clone commits (2 recruits, 2 workers)
- **Files created:** 21 new outer-repo files
- **Files modified:** 2 outer-repo bootstrap files + 9 event files (config import rewrite) + MERGE_NOTES

## Accomplishments

### Task 1: Events + Top-level Utilities

- Copied 10 recruit event classes from `recruits/` top-level into `bannermod.events.*`; package line rewritten; `Main.MOD_ID` annotation reference in `AttributeEvent` replaced with FQN `BannerModMain.MOD_ID`.
- Copied 3 workers event classes into `bannermod.events.*` with `Workers` prefix to avoid name collision with recruit counterparts (`WorkersVillagerEvents`, `WorkersCommandEvents`, `WorkersAttributeEvent`); `WorkersMain.MOD_ID` references replaced with FQN `BannerModMain.MOD_ID`.
- Copied `recruits/UpdateChecker.java` → `bannermod.UpdateChecker`; `workers/UpdateChecker.java` → `bannermod.WorkersUpdateChecker`; `workers/Translatable.java` → `bannermod.Translatable`.
- `git rm`'d all 11 recruit event + utility files (clone commit `bca0a039`) and all 5 workers event + utility files (clone commit `1a229ff`).
- Updated `BannerModMain` and `WorkersSubsystem` to import from new `bannermod.events.Workers*` and `bannermod.WorkersUpdateChecker` FQNs.
- Fixed `WorkersVillagerEvents` import of `ClaimEvents` to target `bannermod.events.ClaimEvents`.

### Task 2: Commands + Config Schemas

- Copied `recruits/commands/{PatrolSpawnCommand,RecruitsAdminCommands}.java` → `bannermod.commands.military.*`; package rewritten; event imports retargeted to `bannermod.events.*`.
- Copied `recruits/config/{RecruitsClientConfig,RecruitsServerConfig}.java` → `bannermod.config.*`; package rewritten.
- Copied `workers/config/WorkersServerConfig.java` → `bannermod.config.*`; package rewritten.
- `git rm`'d recruits commands + configs (clone commit `afaccc39`) and workers config (clone commit `990ddc5`).
- Applied bulk sed across all `bannermod/` files: `com.talhanation.{recruits,workers}.config.*` → `com.talhanation.bannermod.config.*`.
- Updated `BannerModMain`: added `RecruitsClientConfig` and `RecruitsServerConfig` `registerConfig` calls; wired `PatrolSpawnCommand` and `RecruitsAdminCommands` into `onRegisterCommands`; retargeted `WorkersServerConfig` import to `bannermod.config`.
- Appended Wave-4 entry to `MERGE_NOTES.md` with full file inventory, collision resolution notes, and sed invocations.

## Task Commits

**Outer repo:**
1. **Task 1: events + utilities** -- `eb61ab0` (feat)
2. **Task 2: commands + config** -- `f17eb97` (feat)

**recruits/ clone:**
1. **rm event classes + UpdateChecker** -- `bca0a039`
2. **rm commands + config** -- `afaccc39`

**workers/ clone:**
1. **rm events + utilities** -- `1a229ff`
2. **rm WorkersServerConfig** -- `990ddc5`

## Files Created (21 outer-repo files)

- `bannermod.events`: `RecruitEvents`, `FactionEvents`, `ClaimEvents`, `CommandEvents`, `VillagerEvents`, `PillagerEvents`, `AssassinEvents`, `DamageEvent`, `DebugEvents`, `AttributeEvent`, `WorkersVillagerEvents`, `WorkersCommandEvents`, `WorkersAttributeEvent`
- `bannermod`: `UpdateChecker`, `WorkersUpdateChecker`, `Translatable`
- `bannermod.commands.military`: `PatrolSpawnCommand`, `RecruitsAdminCommands`
- `bannermod.config`: `RecruitsClientConfig`, `RecruitsServerConfig`, `WorkersServerConfig`

## Files Deleted from Clones

**recruits/ (15 files):**
- `recruits/{RecruitEvents,FactionEvents,ClaimEvents,CommandEvents,VillagerEvents,PillagerEvents,AssassinEvents,DamageEvent,DebugEvents,AttributeEvent,UpdateChecker}.java`
- `recruits/commands/{PatrolSpawnCommand,RecruitsAdminCommands}.java`
- `recruits/config/{RecruitsClientConfig,RecruitsServerConfig}.java`

**workers/ (6 files):**
- `workers/{VillagerEvents,CommandEvents,AttributeEvent,UpdateChecker,Translatable}.java`
- `workers/config/WorkersServerConfig.java`

## Decisions Made

- **Workers event rename convention**: `Workers` prefix applied to all workers classes that collide with identically-named recruit classes: `VillagerEvents`, `CommandEvents`, `AttributeEvent`, `UpdateChecker`. This keeps both lineages visible in `bannermod.*` without shadowing.
- **workers.CommandEvents treated as event, not command**: The `workers/CommandEvents.java` top-level file is a bare event listener class (not a dispatcher registrar), so it migrates to `bannermod.events.WorkersCommandEvents` rather than `bannermod.commands.civilian.*`. Workers has no `commands/` subpackage directory.
- **No `bannermod.commands.civilian` created this wave**: Workers has no commands in a `commands/` subdirectory; only `CommandEvents.java` which is an event class. Civilian commands (if any) are deferred to wave 7.
- **Config overlap documented, not merged**: `bannermod.config` now contains 4 files (`BannerModConfigFiles`, `RecruitsClientConfig`, `RecruitsServerConfig`, `WorkersServerConfig`). No semantic naming collision. Per D-05 deduplication is deferred.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 2 - Missing critical functionality] Config imports in migrated event/command files needed immediate retarget**
- **Found during:** Task 2, after config files moved
- **Issue:** All 9 event files copied in Task 1 still imported `com.talhanation.recruits.config.*` which was removed from the clone in Task 2. Without rewriting imports, the outer bannermod tree would have unresolvable imports pointing at deleted clone files.
- **Fix:** Applied bulk `sed` across all `bannermod/` files to rewrite `recruits.config.` and `workers.config.` imports to `bannermod.config.` immediately after config move.
- **Files modified:** 9 event files + `UpdateChecker.java` + `commands/military/RecruitsAdminCommands.java`

**2. [Rule 2 - Missing critical functionality] WorkersSubsystem still referenced deleted workers event classes**
- **Found during:** Task 1, after updating BannerModMain
- **Issue:** `bannermod.bootstrap.WorkersSubsystem` imported `com.talhanation.workers.VillagerEvents` and `CommandEvents` — both deleted from workers clone.
- **Fix:** Retargeted both imports and usages to `bannermod.events.WorkersVillagerEvents` and `WorkersCommandEvents`.
- **Files modified:** `WorkersSubsystem.java`

**3. [Rule 1 - Bug] WorkersVillagerEvents still imported deleted recruits.ClaimEvents**
- **Found during:** Task 1, import scan after copy
- **Issue:** Copied `WorkersVillagerEvents.java` imported `com.talhanation.recruits.ClaimEvents` which no longer exists in recruits clone.
- **Fix:** Retargeted to `com.talhanation.bannermod.events.ClaimEvents`.
- **Files modified:** `events/WorkersVillagerEvents.java`

## Known Stubs

- **Clone callers (recruits/network, recruits/pathfinding, recruits/client/gui, etc.)**: These files still import `com.talhanation.recruits.config.{RecruitsClientConfig,RecruitsServerConfig}` which no longer exists in the clone. Per D-22 compile is not required clean between waves. These are addressed when those subsystems move in waves 21-05..21-09.
- **Entity/block/item type imports in event files**: `RecruitEvents`, `FactionEvents`, etc. still import `com.talhanation.recruits.entities.*`, `com.talhanation.recruits.world.*`, etc. These types haven't moved yet (waves 21-05..21-06). Expected per D-22.
- **`bannermod.config.WorkersServerConfig`** still imports `com.talhanation.workers.settlement.WorkerSettlementSpawnRules` — that type moves in wave 21-07.

## Threat Flags

None. No new network endpoints or auth paths. Config schemas are Forge ForgeConfigSpec definitions, not network-exposed.

## Self-Check

- FOUND: `src/main/java/com/talhanation/bannermod/events/RecruitEvents.java`
- FOUND: `src/main/java/com/talhanation/bannermod/events/WorkersVillagerEvents.java` (13 events total)
- FOUND: `src/main/java/com/talhanation/bannermod/UpdateChecker.java`
- FOUND: `src/main/java/com/talhanation/bannermod/WorkersUpdateChecker.java`
- FOUND: `src/main/java/com/talhanation/bannermod/Translatable.java`
- FOUND: `src/main/java/com/talhanation/bannermod/commands/military/PatrolSpawnCommand.java`
- FOUND: `src/main/java/com/talhanation/bannermod/commands/military/RecruitsAdminCommands.java`
- FOUND: `src/main/java/com/talhanation/bannermod/config/RecruitsClientConfig.java`
- FOUND: `src/main/java/com/talhanation/bannermod/config/RecruitsServerConfig.java`
- FOUND: `src/main/java/com/talhanation/bannermod/config/WorkersServerConfig.java`
- FOUND: `src/main/java/com/talhanation/bannermod/config/BannerModConfigFiles.java` (preserved)
- VERIFIED: all events have `package com.talhanation.bannermod.events;`
- VERIFIED: all commands have `package com.talhanation.bannermod.commands.military;`
- VERIFIED: all configs have `package com.talhanation.bannermod.config;`
- VERIFIED: no `com.talhanation.recruits.config.*` or `com.talhanation.workers.config.*` imports remain in bannermod/ tree
- VERIFIED: recruits clone commands/ and config/ dirs gone
- VERIFIED: workers clone config/ dir gone
- VERIFIED: MERGE_NOTES.md has Wave-4 entry
- COMMITS: eb61ab0, f17eb97 in outer repo

## Self-Check: PASSED

---
*Phase: 21-source-tree-consolidation-into-bannerlord*
*Completed: 2026-04-15*
