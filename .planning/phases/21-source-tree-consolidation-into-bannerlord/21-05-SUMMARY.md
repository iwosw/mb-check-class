---
phase: 21-source-tree-consolidation-into-bannerlord
plan: 05
subsystem: military-gameplay
tags: [bannermod, entities, ai, pathfinding, mixin, compat, migration, events, util, wave-5]

dependency_graph:
  requires:
    - phase: 21-04
      provides: "bannermod.events.* (13 classes), bannermod.commands.military.*, bannermod.config.RecruitsClientConfig/ServerConfig/WorkersServerConfig, BannerModMain wired"
  provides:
    - "bannermod.entity.military.** (21 classes): AbstractRecruitEntity + 20 unit/leader/capability types"
    - "bannermod.ai.military.** (55 classes): all recruit goals, controllers, navigation, async, villager/pillager/compat variants"
    - "bannermod.ai.pathfinding.** (10 classes): AsyncPath* pipeline + NodeEvaluator cache/generator"
    - "bannermod.mixin.** (10 classes) + src/main/resources/mixins.bannermod.json registered in build.gradle"
    - "bannermod.compat.** (11 classes, incl. compat/workers subpkg)"
    - "bannermod.migration.** (3 seam helpers)"
    - "bannermod.events.** (6 event data sub-classes appended to Wave-4 set, total 19)"
    - "bannermod.util.** (12 utility classes)"
  affects:
    - "21-06 -- military UI/persistence wave can reference outer-owned entity + AI types directly"
    - "21-09 -- compile-green gate must retire the old mixins.recruits.json gradle config entry and any remaining clone source-set residue"

tech-stack:
  added: []
  patterns:
    - "Per-directory package-line rewrite preserves nested subpackage paths (entity/military/, ai/military/controller/, compat/workers/, etc.)"
    - "Two-phase sed: Task 1 rewrites entities.ai|entities.|pathfinding. cross-refs; Task 2 rewrites mixin|compat|migration|events|util. cross-refs across full bannermod tree"
    - "Clone-side FQN rewrite applied to main/test/gametest sources so remaining clone callers point at new bannermod.* FQNs (per D-23 bookkeeping, compile-green still deferred to 21-09)"
    - "mixins.bannermod.json reissued with refmap name aligned to config basename (mixins.bannermod.refmap.json)"

key-files:
  created:
    - "src/main/java/com/talhanation/bannermod/entity/military/** (21 files)"
    - "src/main/java/com/talhanation/bannermod/ai/military/** (55 files)"
    - "src/main/java/com/talhanation/bannermod/ai/pathfinding/** (10 files)"
    - "src/main/java/com/talhanation/bannermod/mixin/** (10 files)"
    - "src/main/java/com/talhanation/bannermod/compat/** (11 files incl. compat/workers/IVillagerWorker.java)"
    - "src/main/java/com/talhanation/bannermod/migration/** (3 files)"
    - "src/main/java/com/talhanation/bannermod/events/{ClaimEvent,DiplomacyEvent,FactionEvent,RecruitEvent,RecruitsOnWriteSpawnEggEvent,SiegeEvent}.java (6 files)"
    - "src/main/java/com/talhanation/bannermod/util/** (12 files)"
    - "src/main/resources/mixins.bannermod.json"
  modified:
    - "build.gradle -- mixin block extended with mixins.bannermod.refmap.json + mixins.bannermod.json config entries"
    - "MERGE_NOTES.md -- Wave-5 section appended"

key-decisions:
  - "Entity/AI split: the plan explicitly maps recruits/entities/ai/** -> bannermod.ai.military.** and the rest of recruits/entities/** -> bannermod.entity.military.**. We follow the plan mapping even though the verification clause's `entity/military >=60` threshold was written assuming no split. The total file count is 76 (21 entity + 55 ai) which satisfies the substantive intent."
  - "Mixin config gradle registration: added mixins.bannermod.json alongside the existing mixins.recruits.json entry in build.gradle. The old entry is left dangling (the clone json was deleted) per plan instruction to let 21-09 retire it."
  - "Refmap filename: mixins.bannermod.refmap.json (matches config basename convention). Noted for 21-09 if shadow plugin needs manual refmap rules."
  - "Clone-side FQN rewrite applied to main/test/gametest so clone callers compile against new bannermod.* FQNs; compile-green still deferred to 21-09 per D-22."
  - "Test files in recruits/src/test/java/com/talhanation/recruits/pathfinding/ (AsyncPathProcessorTest, GlobalPathfindingControllerTest) retain their recruits.pathfinding test-package declaration (package path is under clone's own test source root). Imports within them were retargeted to bannermod.ai.pathfinding.*."

requirements-completed:
  - SRCMOVE-02

duration: ~15min
completed: 2026-04-15T00:00:00Z
---

# Phase 21 Plan 05: Wave 5 Military Gameplay Subsystem Migration Summary

**Bulk-migrated the recruits military gameplay subsystem (128 Java files across entities, AI goals, pathfinding, mixins, compat, migration, events sub-classes, and utilities) from the `recruits/` clone into `com.talhanation.bannermod.{entity.military,ai.military,ai.pathfinding,mixin,compat,migration,events,util}`, reissued the mixin config as `mixins.bannermod.json`, and retargeted remaining clone callers via FQN rewrite.**

## Performance

- **Duration:** ~15 min
- **Completed:** 2026-04-15
- **Tasks:** 2 atomic task commits in outer repo + 2 clone removal commits
- **Outer files created:** 128 Java files + 1 resource (`mixins.bannermod.json`)
- **Outer files modified:** `build.gradle`, `MERGE_NOTES.md`, plus 17 already-migrated `bannermod.{ai.military,ai.pathfinding}` files whose cross-subtree FQNs were swept in Task 2

## Accomplishments

### Task 1: Entities + AI + Pathfinding (outer `3082cfe`, clone `237d6226`)

- Copied `recruits/entities/` (21 non-`ai/` Java files) into `src/main/java/com/talhanation/bannermod/entity/military/**`.
- Copied `recruits/entities/ai/**` (55 files in 7 subpackages: flat, `controller/`, `horse/`, `navigation/`, `async/`, `villager/`, `compat/`, `pillager/`) into `src/main/java/com/talhanation/bannermod/ai/military/**`.
- Copied `recruits/pathfinding/` (10 files) into `src/main/java/com/talhanation/bannermod/ai/pathfinding/**`.
- Per-file `sed` rewrote each destination's `package` line based on its directory under the new target root.
- Deterministic post-pass `sed` on the three new subtrees rewrote cross-subtree FQNs: `com.talhanation.recruits.entities.ai.` -> `com.talhanation.bannermod.ai.military.`, `com.talhanation.recruits.entities.` -> `com.talhanation.bannermod.entity.military.`, `com.talhanation.recruits.pathfinding.` -> `com.talhanation.bannermod.ai.pathfinding.`.
- `git rm -r recruits/src/main/java/com/talhanation/recruits/{entities,pathfinding}` in the clone, then applied the same three `sed` substitutions to every remaining `.java` file under `recruits/src/{main,test,gametest}/java/**` so clone-side callers now reference the new FQNs.

### Task 2: Mixin + Compat + Migration + Events + Util (outer `f5645e0`, clone `99d823e2`)

- Copied five clone subtrees into outer: `recruits/{mixin,compat,migration,events,util}/` -> `bannermod/{mixin,compat,migration,events,util}/` (10 + 11 + 3 + 6 + 12 = 42 files). Nested subpackages preserved (e.g. `compat/workers/IVillagerWorker.java`).
- Per-file `sed` rewrote `package` lines per destination directory.
- Global post-pass `sed` across the full outer `bannermod/` tree rewrote `com.talhanation.recruits.{mixin,compat,migration,events,util}.` -> `com.talhanation.bannermod.{same}.`. This also updated already-migrated `bannermod.ai.military.*` and `bannermod.ai.pathfinding.*` files that cross-reference the newly migrated subtrees (17 modifications).
- **Mixin config reissue:** `recruits/src/main/resources/mixins.recruits.json` copied to `src/main/resources/mixins.bannermod.json`; `sed` rewrote `"package": "com.talhanation.recruits.mixin"` -> `"com.talhanation.bannermod.mixin"` and `"refmap": "mixins.recruits.refmap.json"` -> `"mixins.bannermod.refmap.json"`. Mixin class list and injector defaults preserved.
- **build.gradle update:** `mixin { … }` block extended with `add sourceSets.main, 'mixins.bannermod.refmap.json'` and `config 'mixins.bannermod.json'`. Old `mixins.recruits.json` config entry left in place per plan instructions (21-09 retires it).
- Deleted `recruits/src/main/resources/mixins.recruits.json` and all five clone Java subtrees via `git rm -r`.
- Applied the same five `sed` substitutions to `recruits/src/{main,test,gametest}/java/**` so the clone's remaining source tree references the new FQNs.
- Appended Wave-5 section to `MERGE_NOTES.md` with full inventory, `sed` invocations, collision resolution, and 21-09 follow-ups.

## Task Commits

**Outer repo (`/home/kaiserroman/bannermod`, branch `master`):**
1. `3082cfe` -- `feat(21-05): migrate recruits military subsystem into bannermod` (86 files +16184)
2. `f5645e0` -- `feat(21-05): migrate mixin/compat/migration/events/util; reissue mixins.bannermod.json` (73 files +5280 -42)

**recruits/ clone (branch `main`):**
1. `237d6226` -- `refactor(21-05): remove migrated military subsystem source` (464 files)
2. `99d823e2` -- `refactor(21-05): remove migrated mixin/compat/migration/events/util + mixins.recruits.json` (59 files)

## File Counts Per Package

| Package | Files |
|---|---|
| `bannermod.entity.military.**` | 21 |
| `bannermod.ai.military.**` | 55 |
| `bannermod.ai.pathfinding.**` | 10 |
| `bannermod.mixin.**` | 10 |
| `bannermod.compat.**` | 11 |
| `bannermod.migration.**` | 3 |
| `bannermod.events.**` (newly appended) | 6 (total now 19) |
| `bannermod.util.**` | 12 |
| **Total new Java** | **128** |
| `src/main/resources/mixins.bannermod.json` | 1 |

## Decisions Made

- **Entity/AI split per plan mapping** -- plan explicitly routes `entities/ai/**` to `ai.military.**` and the remaining `entities/**` to `entity.military.**`. This produces 21 entity files and 55 AI files. The verification clause's `entity/military >=60` threshold was written against a non-split expectation; substantive intent (all 76 `entities/**` files relocated) is met.
- **Refmap filename convention** -- new mixin config uses `mixins.bannermod.refmap.json`, matching the config basename. If the shadow/MixinGradle plugin needs a manual refmap include rule in the shade/jar task (lines 186/201 of `build.gradle` currently include `mixins.recruits.refmap.json`), that's 21-09 work per plan.
- **Clone-side FQN rewrite scope** -- applied to clone's `src/main/java`, `src/test/java`, `src/gametest/java` even though compile-green isn't required between waves (D-22). Keeping FQNs aligned reduces churn in 21-06..21-09 and aids grep-based discovery.

## Deviations from Plan

### [Rule 2 - Missing critical functionality] Clone-side FQN rewrite for tests and gametests

- **Found during:** Task 1 after removing clone main sources.
- **Issue:** Plan's `<action>` only explicitly mentions rewriting FQNs on the destination tree and in the "clone" (implicitly the clone's main sources). The `recruits/` clone also has `src/test/java` and `src/gametest/java` source roots that `build.gradle` includes in the outer project's test/gametest sourceSets.
- **Fix:** Extended the clone-side `sed` invocations to cover `src/test/java` and `src/gametest/java` in both tasks so the outer build's test/gametest compilation has a chance of succeeding and so downstream waves don't inherit stale FQNs. Per D-22 compile-green is still deferred, but this prevents accidentally-hidden stale references.
- **Files modified:** ~80 clone test/gametest files (Task 1) + ~5 additional (Task 2).
- **Commit:** `237d6226`, `99d823e2` (clone).

### [Note] Two clone test files retain recruits.pathfinding package declaration

- **Found during:** Task 1 verification grep.
- **Issue:** `recruits/src/test/java/com/talhanation/recruits/pathfinding/AsyncPathProcessorTest.java` and `GlobalPathfindingControllerTest.java` self-declare `package com.talhanation.recruits.pathfinding;`. The clone's test source root's directory structure still uses the old package path.
- **Fix:** No action needed -- their `package` line is valid for files physically located under `recruits/src/test/java/com/talhanation/recruits/pathfinding/`. Their imports of the migrated types were retargeted to `bannermod.ai.pathfinding.*` via the clone-side `sed`. 21-09 owns final disposition (move to outer test tree or delete with the clone source set).
- **No commit impact.**

### [Flag for 21-09] Dangling `mixins.recruits.json` gradle config entry

- **Found during:** Task 2 build.gradle edit.
- **Issue:** The clone's `mixins.recruits.json` was deleted in this wave (plan instruction), but `build.gradle` still references it at lines 32, 34, 186, 201. Gradle will fail at mixin processing / shadow/jar time until 21-09 removes these lines.
- **Fix:** Not fixing in this wave -- plan explicitly says "leave old entry for now (21-09 retires it)."
- **Recorded in:** `MERGE_NOTES.md` Wave-5 "Flagged for 21-09" section.

## Known Stubs

None from this wave. All migrated types are real, complete source files (no placeholders introduced).

## Residual Work (for 21-06..21-09)

- Remaining clone `src/main/java` subsystems: `client/**`, `inventory/**`, `items/**`, `world/**`, `network/**`, `init/**` (partial residue), top-level `Main.java` forwarder. These are owned by Waves 21-06 (UI + persistence), 21-07 (workers civilian), 21-08 (network), 21-09 (closure).
- `build.gradle` mixin block still references the removed `mixins.recruits.json` / `mixins.recruits.refmap.json` -- 21-09.
- Shadow `include 'mixins.recruits.refmap.json'` at lines 186 and 201 -- 21-09 must add `mixins.bannermod.refmap.json` or replace entirely.
- `recruits/` and `workers/` source sets in `build.gradle` still compile as part of the outer mod; 21-09 closure decides final retirement.
- Wave 5 does NOT attempt compile-green. Per D-22 this is acceptable and intentional.

## Threat Flags

None. This wave relocates pure-code files (entities, AI, mixins, compat shims, utilities) without introducing new network endpoints, auth paths, file access patterns, or schema changes. Existing mixin surface is unchanged -- the `mixins.bannermod.json` config lists the exact same mixin classes as the retired `mixins.recruits.json`, only the Java package prefix moved.

## Self-Check

**Created files:**
- FOUND: `src/main/java/com/talhanation/bannermod/entity/military/AbstractRecruitEntity.java`
- FOUND: `src/main/java/com/talhanation/bannermod/entity/military/RecruitEntity.java` (21 total entity files)
- FOUND: `src/main/java/com/talhanation/bannermod/ai/military/RecruitMeleeAttackGoal.java` (55 total ai/military files)
- FOUND: `src/main/java/com/talhanation/bannermod/ai/pathfinding/AsyncPathProcessor.java` (10 total pathfinding files)
- FOUND: `src/main/java/com/talhanation/bannermod/mixin/LivingEntityMixin.java` (10 total)
- FOUND: `src/main/java/com/talhanation/bannermod/compat/MusketWeapon.java` (11 total incl. workers/IVillagerWorker)
- FOUND: `src/main/java/com/talhanation/bannermod/migration/NetworkBootstrapSeams.java` (3 total)
- FOUND: `src/main/java/com/talhanation/bannermod/events/RecruitEvent.java` (6 new sub-classes; 19 events total in bannermod.events)
- FOUND: `src/main/java/com/talhanation/bannermod/util/Kalkuel.java` (12 total)
- FOUND: `src/main/resources/mixins.bannermod.json` (package `com.talhanation.bannermod.mixin`, refmap `mixins.bannermod.refmap.json`)

**Verified clone state:**
- VERIFIED: `recruits/src/main/java/com/talhanation/recruits/{entities,pathfinding,mixin,compat,migration,events,util}` removed (0 Java files each)
- VERIFIED: `recruits/src/main/resources/mixins.recruits.json` deleted
- VERIFIED: no stale `com.talhanation.recruits.{entities,pathfinding,mixin,compat,migration,events,util}.` imports in the outer `bannermod/` tree

**Verified commits:**
- FOUND: outer `3082cfe` (feat Task 1)
- FOUND: outer `f5645e0` (feat Task 2)
- FOUND: clone `237d6226` (rm Task 1)
- FOUND: clone `99d823e2` (rm Task 2)

**Verified docs:**
- FOUND: `MERGE_NOTES.md` Wave-5 section appended

## Self-Check: PASSED

---
*Phase: 21-source-tree-consolidation-into-bannerlord*
*Wave: 5*
*Completed: 2026-04-15*
