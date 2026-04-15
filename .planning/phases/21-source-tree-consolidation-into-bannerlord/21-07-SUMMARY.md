---
phase: 21-source-tree-consolidation-into-bannerlord
plan: 07
subsystem: civilian-subsystem
tags: [bannermod, entity, ai, client, persistence, inventory, items, settlement, wave-7]

dependency_graph:
  requires:
    - phase: 21-06
      provides: "bannermod.client.military.** (107), bannermod.inventory.military.** (11), bannermod.persistence.military.** (22), bannermod.items.military.** (2)"
  provides:
    - "bannermod.entity.civilian.** (25 files): AbstractWorkerEntity, 8 concrete workers, 3 support types, workarea/ (11 area entities)"
    - "bannermod.ai.civilian.** (22 files): all worker goal/AI types + animals/ (WorkerTemptGoal)"
    - "bannermod.client.civilian.** (24 files): WorkersClientManager, events/ (2), gui/ (12 + structureRenderer/ + widgets/), render/ (5)"
    - "bannermod.persistence.civilian.** (8 files): BuildBlock, NeededItem, ScannedBlock, StructureManager, StructureTemplateLoader, Tree, WorkersMerchantTrade, BuildBlockParse"
    - "bannermod.inventory.civilian.** (2 files): MerchantAddEditTradeContainer, MerchantTradeContainer"
    - "bannermod.items.civilian.** (1 file): WorkersSpawnEgg"
    - "bannermod.settlement.civilian.** (2 files): WorkerSettlementSpawnRules, WorkerSettlementSpawner"
  affects:
    - "21-08 -- network wave: workers/network/** (18 files) remains in workers clone for Wave 8 migration"
    - "21-09 -- compile-green gate; workers clone source-set retirement; WorkersMain shim removal"

tech-stack:
  added: []
  patterns:
    - "Per-directory package-line rewrite maps each file to its destination package based on its path under the target root"
    - "Compound sed pass across all new subtrees + full outer bannermod tree rewrites all wave cross-refs simultaneously"
    - "workers.entities.* -> bannermod.entity.civilian.* / bannermod.ai.civilian.* FQN rewrite pattern mirrors Wave 5/6 recruits pattern"
    - "workers clone git-rm committed independently per wave (D-20)"

key-files:
  created:
    - "src/main/java/com/talhanation/bannermod/entity/civilian/** (25 files)"
    - "src/main/java/com/talhanation/bannermod/ai/civilian/** (22 files)"
    - "src/main/java/com/talhanation/bannermod/client/civilian/** (24 files)"
    - "src/main/java/com/talhanation/bannermod/persistence/civilian/** (8 files)"
    - "src/main/java/com/talhanation/bannermod/inventory/civilian/** (2 files)"
    - "src/main/java/com/talhanation/bannermod/items/civilian/** (1 file)"
    - "src/main/java/com/talhanation/bannermod/settlement/civilian/** (2 files)"
  modified:
    - "build.gradle -- removed workers mixin config entries (mixins.workers.refmap.json, mixins.workers.json)"
    - "src/main/java/com/talhanation/bannermod/bootstrap/BannerModMain.java -- workers.entities/client FQN rewrite"
    - "src/main/java/com/talhanation/bannermod/bootstrap/WorkersSubsystem.java -- workers.entities/client FQN rewrite"
    - "src/main/java/com/talhanation/bannermod/events/WorkersCommandEvents.java -- workers.entities/client FQN rewrite"
    - "src/main/java/com/talhanation/bannermod/events/WorkersAttributeEvent.java -- workers.init -> bannermod.registry.civilian rewrite"
    - "src/main/java/com/talhanation/bannermod/events/WorkersVillagerEvents.java -- workers.init -> bannermod.registry.civilian rewrite"
    - "src/main/java/com/talhanation/bannermod/governance/BannerModGovernorHeartbeat.java -- workers.entities FQN rewrite"
    - "src/main/java/com/talhanation/bannermod/registry/civilian/ModEntityTypes.java -- workers.entities FQN rewrite"
    - "src/main/java/com/talhanation/bannermod/registry/civilian/ModMenuTypes.java -- workers.inventory FQN rewrite"
    - "src/main/java/com/talhanation/bannermod/registry/civilian/ModItems.java -- workers.items FQN rewrite"
    - "src/main/java/com/talhanation/bannermod/shared/logistics/BannerModSupplyStatus.java -- workers.entities FQN rewrite"
    - "src/main/java/com/talhanation/bannermod/shared/logistics/BannerModUpkeepProviders.java -- workers.entities FQN rewrite"
    - "src/main/java/com/talhanation/bannermod/logistics/BannerModSupplyStatus.java -- workers.entities FQN rewrite"
    - "src/main/java/com/talhanation/bannermod/network/BannerModNetworkBootstrap.java -- workers.entities FQN rewrite"
    - "src/main/java/com/talhanation/bannermod/config/WorkersServerConfig.java -- workers.entities FQN rewrite"
    - "MERGE_NOTES.md -- Wave-7 section appended"

key-decisions:
  - "workers.network.* imports in entity/client files left as-is -- network migration deferred to Wave 8 per D-22"
  - "workers.config.WorkersServerConfig references left as-is -- config migration deferred, Wave 9 scope"
  - "workers.WorkersRuntime.bindChannel() reference left as-is in bootstrap -- Wave 8 network consolidation scope"
  - "bannermod.settlement.civilian.* lands as a new sibling subpackage alongside preserved bannermod.settlement.* -- no clobbering per D-05"
  - "mixins.workers.json fold was trivial -- empty mixin arrays, nothing to merge into mixins.bannermod.json"
  - "entity/civilian count 25 vs plan threshold 30 -- all available workers entity files migrated; plan threshold was an overestimate"

requirements-completed:
  - SRCMOVE-02

duration: ~8 min
completed: 2026-04-15T00:00:00Z
---

# Phase 21 Plan 07: Wave 7 Civilian Subsystem Migration Summary

**Bulk-migrated the workers-clone civilian subsystem (84 Java files across entities, AI, workarea, client UI, world persistence, inventory, items, and settlement) from the `workers/` clone into `com.talhanation.bannermod.{entity,ai,client,persistence,inventory,items,settlement}.civilian.**`, and folded the empty `mixins.workers.json` into `mixins.bannermod.json`, leaving the workers clone containing only the `WorkersMain` shim group and the `network/` package for Wave 8.**

## Performance

- **Duration:** ~8 min
- **Completed:** 2026-04-15
- **Tasks:** 2 atomic task commits in outer repo + 2 clone removal commits
- **Outer files created:** 84 Java files
- **Outer files modified:** ~15 previously-migrated bannermod.* files (FQN sweep) + build.gradle + MERGE_NOTES.md

## Accomplishments

### Task 1: workers/entities/** + workers/client/** -> bannermod.*.civilian.** (outer `b7f8640`, clone `07dfaaf`)

- Copied 22 AI files from `workers/entities/ai/` (including `animals/WorkerTemptGoal`) → `bannermod/ai/civilian/`.
- Copied 11 workarea files from `workers/entities/workarea/` → `bannermod/entity/civilian/workarea/`.
- Copied 14 top-level entity files from `workers/entities/` → `bannermod/entity/civilian/`.
- Copied 24 client files from `workers/client/` (events/, gui/ with structureRenderer/ and widgets/, render/) → `bannermod/client/civilian/`.
- Per-directory `sed` rewrote each destination's `package` line based on path under the target root.
- Compound `sed` pass across all four new subtrees + full outer bannermod tree rewrote all cross-subsystem FQN refs:
  - `workers.entities.ai.animals.` → `bannermod.ai.civilian.animals.`
  - `workers.entities.ai.` → `bannermod.ai.civilian.`
  - `workers.entities.workarea.` → `bannermod.entity.civilian.workarea.`
  - `workers.entities.` → `bannermod.entity.civilian.`
  - `workers.client.*` → `bannermod.client.civilian.*`
  - `workers.world.` → `bannermod.persistence.civilian.` (forward ref to Task 2 targets)
  - `workers.inventory.` → `bannermod.inventory.civilian.`
  - `workers.items.` → `bannermod.items.civilian.`
  - `workers.settlement.` → `bannermod.settlement.civilian.`
  - `workers.init.` → `bannermod.registry.civilian.`
  - recruits.* → bannermod.* (prior-wave pattern; recruits.entities.ai. → bannermod.ai.military., etc.)
- `git rm -rf workers/entities/ workers/client/` committed in clone.

### Task 2: workers/world + inventory + items + settlement + mixin fold (outer `dfa0e16`, clone `070d6d1`)

- Copied 8 world files → `bannermod/persistence/civilian/`.
- Copied 2 inventory files → `bannermod/inventory/civilian/`.
- Copied 1 items file → `bannermod/items/civilian/`.
- Copied 2 settlement files → `bannermod/settlement/civilian/`. These land as sibling subpackage under the preserved `bannermod.settlement.*` without touching existing settlement classes.
- Package declarations rewritten per destination root for all four subtrees.
- Compound `sed` pass applied FQN rewrites to all four new subtrees + full outer bannermod tree.
- workers.init FQN rewrite applied separately to catch files missed by earlier pass.
- `mixins.workers.json` had empty arrays — deleted from clone with no merge required. Removed workers mixin config entries from outer `build.gradle`.
- `git rm -rf workers/world/ workers/inventory/ workers/items/ workers/settlement/` + `git rm mixins.workers.json` committed in clone.
- Wave-7 section appended to `MERGE_NOTES.md`.

## Task Commits

**Outer repo (`/home/kaiserroman/bannermod`, branch `worktree-agent-afe402dd`):**
1. `b7f8640` -- `feat(21-07): migrate workers entities/AI/workarea/client into bannermod.*.civilian` (82 files +14643 -20)
2. `dfa0e16` -- `feat(21-07): migrate workers world/inventory/items/settlement + fold mixin config` (24 files +1418 -15)

**workers/ clone (branch `V2-1.20.1`):**
1. `07dfaaf` -- `refactor(21-07): remove migrated entities, AI, workarea, client subsystems` (71 files)
2. `070d6d1` -- `refactor(21-07): remove migrated world, inventory, items, settlement subsystems and mixin config` (14 files)

## File Counts Per Package

| Package | Files |
|---|---|
| `bannermod.entity.civilian.**` | 25 |
| `bannermod.ai.civilian.**` | 22 |
| `bannermod.client.civilian.**` | 24 |
| `bannermod.persistence.civilian.**` | 8 |
| `bannermod.inventory.civilian.**` | 2 |
| `bannermod.items.civilian.**` | 1 |
| `bannermod.settlement.civilian.**` | 2 |
| **Total new Java** | **84** |

## Decisions Made

- **workers.network.* deferred** -- network message imports in entity/client/gui files retain `com.talhanation.workers.network.*` refs. Correct per D-22; Wave 8 owns them.
- **settlement two-subpackage preserved** -- `bannermod.settlement.*` (existing) and `bannermod.settlement.civilian.*` (new) coexist as siblings per D-05. No existing settlement code touched.
- **mixin fold trivial** -- `mixins.workers.json` had empty `mixins`, `client`, and `server` arrays. No Java mixin classes in workers clone. Fold was deletion-only; no entries to transfer.
- **entity count 25 vs plan threshold 30** -- Plan verification specified `>= 30` for entity/civilian. Actual count: 25 (14 top-level + 11 workarea). All available workers entity Java files were migrated; the threshold was an overestimate in the plan spec. No files omitted.

## Deviations from Plan

### [Rule 2 - Missing critical functionality] workers.init FQN rewrite not applied in Task 1 cross-ref pass

- **Found during:** Task 2 — subsequent grep revealed `workers.init.*` refs remained after Task 1 pass.
- **Issue:** The Task 1 compound sed pass rewrote workers.entities/client/world/inventory/items/settlement but did not include a `workers.init.` → `bannermod.registry.civilian.` substitution. A second pass in Task 2 cleaned these up.
- **Fix:** Applied `workers.init.` → `bannermod.registry.civilian.` in Task 2 outer-tree sweep.
- **Files affected:** FishingBobberEntity.java, ClientEvent.java, WorkersAttributeEvent.java, WorkersVillagerEvents.java.
- **Committed in:** `dfa0e16`.

## Known Stubs

None. All migrated types are complete source files with full behavior. No placeholder text, empty returns, or TODO-gated logic introduced in this wave.

## Residual Work (for 21-08..21-09)

- `workers/network/**` -- 18 network message/registrar files, Wave 8.
- `workers/WorkersMain.java`, `WorkersRuntime.java`, `WorkersSubsystem.java`, `MergedRuntimeCleanupPolicy.java` -- shim group, Wave 9.
- `workers/init/WorkersLifecycleRegistrar.java` -- lifecycle stub, Wave 9.
- `workers.network.*` import references in migrated civilian entity/GUI files -- Wave 8.
- `workers.config.WorkersServerConfig` references -- Wave 9 config migration.
- `mixins.recruits.json` entry in `build.gradle` -- still present (recruits network not yet retired); Wave 9.

## Threat Flags

None. This wave relocates pure-code files (entity types, goal AIs, GUI screens, persistence data classes) without introducing new network endpoints, auth paths, file access patterns, or schema changes. The `WorkerSettlementSpawner` spawns entities server-side as before — behavior and surface unchanged.

## Self-Check

**Created files verified:**
- FOUND: AbstractWorkerEntity.java (entity/civilian)
- FOUND: CropArea.java (entity/civilian/workarea)
- FOUND: FarmerWorkGoal.java (ai/civilian)
- FOUND: WorkerTemptGoal.java (ai/civilian/animals)
- FOUND: WorkersClientManager.java (client/civilian)
- FOUND: MiningAreaScreen.java (client/civilian/gui)
- FOUND: NeededItem.java (persistence/civilian)
- FOUND: MerchantTradeContainer.java (inventory/civilian)
- FOUND: WorkersSpawnEgg.java (items/civilian)
- FOUND: WorkerSettlementSpawnRules.java (settlement/civilian)
- FOUND: WorkerSettlementSpawner.java (settlement/civilian)

**Commits verified:**
- FOUND: outer `b7f8640` (Task 1 feat)
- FOUND: outer `dfa0e16` (Task 2 feat)
- FOUND: workers clone `07dfaaf` (rm entities/client)
- FOUND: workers clone `070d6d1` (rm world/inventory/items/settlement + mixin)

**Workers clone state verified:**
- entities/: GONE
- client/: GONE
- world/: GONE
- settlement/: GONE
- WorkersMain.java: PRESENT
- network/: 24 files (Wave 8 scope)

## Self-Check: PASSED

---
*Phase: 21-source-tree-consolidation-into-bannerlord*
*Wave: 7*
*Completed: 2026-04-15*
