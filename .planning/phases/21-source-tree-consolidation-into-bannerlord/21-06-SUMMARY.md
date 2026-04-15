---
phase: 21-source-tree-consolidation-into-bannerlord
plan: 06
subsystem: military-ui-persistence
tags: [bannermod, client, inventory, persistence, items, wave-6]

dependency_graph:
  requires:
    - phase: 21-05
      provides: "bannermod.entity.military.** (21), bannermod.ai.military.** (55), bannermod.ai.pathfinding.** (10), bannermod.mixin.**, bannermod.compat.**, bannermod.migration.**, bannermod.events.** (19), bannermod.util.** (12)"
  provides:
    - "bannermod.client.military.** (107 files): ClientManager, ClientSyncState, api/ (3), events/ (5), gui/** (82 across 9 subpackages), models/ (1), render/** (10)"
    - "bannermod.inventory.military.** (11 files): all recruit menu/container types"
    - "bannermod.persistence.military.** (22 files): all SavedData managers, faction/claim/group/diplomacy/route/treaty types"
    - "bannermod.items.military.** (2 files): HeldBannerItem, RecruitsSpawnEgg"
  affects:
    - "21-07 -- civilian wave can reference bannermod.client.military.* and bannermod.persistence.military.* directly"
    - "21-08 -- network wave: 40 remaining recruits.network.* imports in bannermod.client.military.gui/** will be resolved"
    - "21-09 -- compile-green gate; clone source-set retirement"

tech-stack:
  added: []
  patterns:
    - "Per-directory package-line rewrite maps each file to its destination package based on its path under the target root"
    - "Compound sed pass across all new subtrees + full outer bannermod tree rewrites all wave-1 through wave-5 cross-refs simultaneously"
    - "Clone-side FQN rewrite applied to recruits/src/{main,test}/java so remaining clone callers point at new bannermod.* FQNs (per D-23; compile-green still deferred to 21-09 per D-22)"
    - "BannerModMain.clientSetup() updated to reference bannermod.client.military.events.CommandCategoryManager"

key-files:
  created:
    - "src/main/java/com/talhanation/bannermod/client/military/** (107 files)"
    - "src/main/java/com/talhanation/bannermod/inventory/military/** (11 files)"
    - "src/main/java/com/talhanation/bannermod/persistence/military/** (22 files)"
    - "src/main/java/com/talhanation/bannermod/items/military/** (2 files)"
  modified:
    - "src/main/java/com/talhanation/bannermod/bootstrap/BannerModMain.java -- clientSetup() CommandCategoryManager import updated"
    - "src/main/java/com/talhanation/bannermod/bootstrap/WorkersSubsystem.java -- recruits.client.* FQN rewrite"
    - "src/main/java/com/talhanation/bannermod/events/*.java (13 files) -- recruits.world.* -> bannermod.persistence.military.* rewrites"
    - "src/main/java/com/talhanation/bannermod/entity/military/*.java (9 files) -- recruits.inventory.*, recruits.world.* FQN rewrites"
    - "src/main/java/com/talhanation/bannermod/governance/*.java (2 files) -- recruits.world.* FQN rewrites"
    - "src/main/java/com/talhanation/bannermod/migration/StatePersistenceSeams.java -- recruits.world.* FQN rewrite"
    - "src/main/java/com/talhanation/bannermod/settlement/BannerModSettlementBinding.java -- recruits.world.* FQN rewrite"
    - "src/main/java/com/talhanation/bannermod/shared/settlement/BannerModSettlementBinding.java -- recruits.world.* FQN rewrite"
    - "src/main/java/com/talhanation/bannermod/util/ClaimUtil.java -- recruits.world.* FQN rewrite"
    - "src/main/java/com/talhanation/bannermod/registry/military/ModItems.java -- recruits.items.* FQN rewrite"
    - "src/main/java/com/talhanation/bannermod/registry/military/ModScreens.java -- recruits.inventory.* FQN rewrite"
    - "MERGE_NOTES.md -- Wave-6 section appended"

key-decisions:
  - "40 recruits.network.* import references in bannermod.client.military.gui/** are left as-is -- network migration deferred to Wave 8 per D-22"
  - "Clone init/ModLifecycleRegistrar.java stub left in place -- it is a @Deprecated empty class from Wave 3; Wave 9 owns final clone source-set retirement"
  - "recruits.client.* FQN rewrite applied to full outer bannermod tree (not just the new client/military subtree) so BannerModMain.clientSetup() and WorkersSubsystem compile against the new package without manual hunt"

requirements-completed:
  - SRCMOVE-02

duration: ~4 min
completed: 2026-04-15T00:00:00Z
---

# Phase 21 Plan 06: Wave 6 Military UI, Inventory, Persistence, Items Migration Summary

**Bulk-migrated the remaining recruits-side non-network subsystems (142 Java files across client UI, inventory menus, world managers/SavedData, and items) from the `recruits/` clone into `com.talhanation.bannermod.{client.military,inventory.military,persistence.military,items.military}`, reducing the clone's `src/main/java` to only the `@Deprecated Main.java` shim, one legacy init stub, and the `network/` package (Wave 8 scope).**

## Performance

- **Duration:** ~4 min
- **Completed:** 2026-04-15
- **Tasks:** 2 atomic task commits in outer repo + 2 clone removal commits
- **Outer files created:** 142 Java files
- **Outer files modified:** 32 previously-migrated bannermod.* files (FQN sweep) + MERGE_NOTES.md

## Accomplishments

### Task 1: recruits/client/** -> bannermod.client.military.** (outer `dd2925e`, clone `43dc9c9c`)

- Copied 107 files from `recruits/client/` preserving full subpackage shape (api/, events/, gui/ with 9 nested subpackages, models/, render/ with layer/).
- Per-directory `sed` rewrote each destination's `package` line based on its path under the new root.
- Compound `sed` pass across the 107 new files rewrote all prior-wave cross-refs: `recruits.entities.ai.` → `bannermod.ai.military.`, `recruits.entities.` → `bannermod.entity.military.`, `recruits.client.` → `bannermod.client.military.`, `recruits.events.` → `bannermod.events.`, `recruits.util.` → `bannermod.util.`, `recruits.init.` → `bannermod.registry.military.`, `recruits.Main` → `BannerModMain`, `recruits.inventory.` → `bannermod.inventory.military.`, `recruits.world.` → `bannermod.persistence.military.`, `recruits.items.` → `bannermod.items.military.`.
- Top-level event class imports fixed: `recruits.{FactionEvents,RecruitEvents,ClaimEvents}` → `bannermod.events.*`.
- Static import `recruits.client.gui.*` → `bannermod.client.military.gui.*`.
- Applied same `recruits.client.*` → `bannermod.client.military.*` rewrite across full outer bannermod tree (hits `BannerModMain.clientSetup()`, `WorkersSubsystem`, `CommandEvents`, `ModScreens`).
- Clone-side FQN rewrite applied to `recruits/src/{main,test}/java/**`.
- `git rm -rf recruits/client/` staged and committed in clone.

### Task 2: inventory + world + items migration (outer `72e692c`, clone `7fb407d4`)

- Copied 11 inventory files → `bannermod/inventory/military/`.
- Copied 22 world files → `bannermod/persistence/military/`.
- Copied 2 items files → `bannermod/items/military/`.
- Package declarations rewritten per destination root.
- Compound `sed` pass rewrote all ten cross-ref substitutions across all three new subtrees.
- FQN sweep applied to the full outer bannermod tree for the three new substitutions (`recruits.inventory.*`, `recruits.world.*`, `recruits.items.*`), touching 32 already-migrated files (events/, entity/military/, governance/, migration/, settlement/, shared/, util/, registry/military/).
- Clone-side FQN rewrites applied to `recruits/src/{main,test}/java/**`.
- `git rm -rf` for inventory/, world/, items/ staged and committed in clone.
- Wave-6 section appended to `MERGE_NOTES.md` with file counts, sed invocations, and final clone tree inventory.

## Task Commits

**Outer repo (`/home/kaiserroman/bannermod`, branch `master`):**
1. `dd2925e` -- `feat(21-06): migrate recruits client into bannermod.client.military` (111 files +15850 -5)
2. `72e692c` -- `feat(21-06): migrate recruits inventory/world/items into bannermod` (66 files +5187 -57)

**recruits/ clone (branch `main`):**
1. `43dc9c9c` -- `refactor(21-06): remove migrated client subsystem` (107 files)
2. `7fb407d4` -- `refactor(21-06): remove migrated inventory, world, items subsystems` (35 files)

## File Counts Per Package

| Package | Files |
|---|---|
| `bannermod.client.military.**` | 107 |
| `bannermod.inventory.military.**` | 11 |
| `bannermod.persistence.military.**` | 22 |
| `bannermod.items.military.**` | 2 |
| **Total new Java** | **142** |

## Decisions Made

- **40 network imports deferred** -- `bannermod.client.military.gui/**` retains `com.talhanation.recruits.network.*` imports for 40 network message types. These are correct per D-22; Wave 8 moves them.
- **BannerModMain.clientSetup() call site updated** -- `com.talhanation.recruits.client.events.CommandCategoryManager` updated to `bannermod.client.military.events.CommandCategoryManager`. This is the only line in BannerModMain referencing a now-migrated client type, so it was fixed inline as part of the outer-tree FQN sweep.
- **Clone init/ stub left** -- `recruits/init/ModLifecycleRegistrar.java` is a `@Deprecated` empty class created in Wave 3. Wave 9 owns its removal when the clone source set is retired.

## Deviations from Plan

### [Rule 1 - Bug] git rm required -f flag due to modified working tree

- **Found during:** Task 1 clone deletion step.
- **Issue:** The clone-side FQN rewrite modified the files before `git rm` was called, making them "modified" in the working tree. Plain `git rm` refused to remove modified files.
- **Fix:** Used `git rm -rf` with force flag to stage deletions of the modified-then-removed clone files.
- **Files modified:** 107 clone files (Task 1), 35 clone files (Task 2).
- **No commit impact** — removal committed cleanly.

## Known Stubs

None. All migrated types are complete source files. No placeholder text, empty returns wired to UI, or TODO-gated logic was introduced in this wave.

## Residual Work (for 21-07..21-09)

- `recruits/network/**` -- 109 network message files, Wave 8.
- `recruits/init/ModLifecycleRegistrar.java` -- `@Deprecated` stub, Wave 9.
- `recruits/Main.java` -- `@Deprecated` shim, Wave 9.
- 40 `recruits.network.*` import references in `bannermod.client.military.gui/**` -- Wave 8.
- `build.gradle` mixin block dangling `mixins.recruits.json` entries -- Wave 9.
- Workers civilian wave (`workers/{entities,client,world,inventory,items,settlement}/**`) -- Wave 7.

## Threat Flags

None. This wave relocates pure-code files (GUI screens, menus, SavedData managers, items) without introducing new network endpoints, auth paths, file access patterns, or schema changes. Existing persistence surface (`RecruitsFactionManager`, `RecruitsClaimManager`, etc.) is unchanged — package declarations rewritten but behavior preserved.

## Self-Check

**Created files:**
- FOUND: `src/main/java/com/talhanation/bannermod/client/military/ClientManager.java`
- FOUND: `src/main/java/com/talhanation/bannermod/client/military/events/CommandCategoryManager.java`
- FOUND: `src/main/java/com/talhanation/bannermod/client/military/gui/worldmap/WorldMapScreen.java`
- FOUND: `src/main/java/com/talhanation/bannermod/inventory/military/RecruitInventoryMenu.java`
- FOUND: `src/main/java/com/talhanation/bannermod/persistence/military/RecruitsClaimManager.java`
- FOUND: `src/main/java/com/talhanation/bannermod/persistence/military/RecruitsFactionManager.java`
- FOUND: `src/main/java/com/talhanation/bannermod/items/military/HeldBannerItem.java`
- FOUND: `src/main/java/com/talhanation/bannermod/items/military/RecruitsSpawnEgg.java`

**Verified clone state:**
- VERIFIED: `recruits/src/main/java/com/talhanation/recruits/{client,inventory,world,items}` all removed
- VERIFIED: remaining clone tree is `{Main.java, init/ModLifecycleRegistrar.java, network/**}`

**Verified commits:**
- FOUND: outer `dd2925e` (Task 1 feat)
- FOUND: outer `72e692c` (Task 2 feat)
- FOUND: clone `43dc9c9c` (rm client)
- FOUND: clone `7fb407d4` (rm inventory+world+items)

**Verified docs:**
- FOUND: `MERGE_NOTES.md` Wave-6 section appended

## Self-Check: PASSED

---
*Phase: 21-source-tree-consolidation-into-bannerlord*
*Wave: 6*
*Completed: 2026-04-15*
