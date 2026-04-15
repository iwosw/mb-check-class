---
phase: 21-source-tree-consolidation-into-bannerlord
plan: 08
subsystem: network-consolidation
tags: [bannermod, network, messages, military, civilian, wave-8]

dependency_graph:
  requires:
    - phase: 21-07
      provides: "bannermod.*.civilian.** (84 files); workers clone reduced to WorkersMain shim + network/"
  provides:
    - "bannermod.network.messages.military.** (109 files): all 109 recruits network message/utility classes"
    - "bannermod.network.messages.civilian.** (24 files): all 24 workers network message/utility classes"
    - "BannerModNetworkBootstrap: canonical MILITARY_MESSAGES[104] + CIVILIAN_MESSAGES[20] + workerPacketOffset()=104"
  affects:
    - "21-09 -- phase closure: source-root retirement, WorkersRuntime.bindChannel() removal, build.gradle cleanup"

tech-stack:
  added: []
  patterns:
    - "Class-level MILITARY_MESSAGES[] and CIVILIAN_MESSAGES[] constants in BannerModNetworkBootstrap preserve verbatim packet-ID ordering from legacy Main.setup() arrays"
    - "workerPacketOffset() returns MILITARY_MESSAGES.length -- compile-time derivation of offset proves must-have #4 from 21-03"
    - "createSharedChannel() loops over both arrays: military at [0..N), civilian at [N..N+M)"
    - "Multi-step FQN rewrite: package decl sed, network import sed, cross-subsystem entity/ai/client/persistence sed, top-level event class sed"

key-files:
  created:
    - "src/main/java/com/talhanation/bannermod/network/messages/military/** (109 files)"
    - "src/main/java/com/talhanation/bannermod/network/messages/civilian/** (24 files)"
  modified:
    - "src/main/java/com/talhanation/bannermod/network/BannerModNetworkBootstrap.java -- MILITARY_MESSAGES/CIVILIAN_MESSAGES canonical rewrite"
    - "src/main/java/com/talhanation/bannermod/client/civilian/gui/*.java -- workers.network.* FQN sweep (10 files)"
    - "src/main/java/com/talhanation/bannermod/client/military/** -- recruits.network.* FQN sweep (several files)"
    - "MERGE_NOTES.md -- Wave-8 section appended"

key-decisions:
  - "MILITARY_MESSAGES.length == 104 is the workerPacketOffset -- matches legacy WorkersRuntime.ROOT_NETWORK_ID_OFFSET = 104, no desync"
  - "CIVILIAN_MESSAGES.length == 20 -- 20 of 24 workers/network files are message classes; remaining 4 are utility types (WorkAreaAuthoringRules, WorkAreaRotation, BuildAreaUpdateAuthoring, WorkersNetworkRegistrar stub)"
  - "workers.config.WorkersServerConfig ref in MessageAddWorkArea.java left as-is -- config deferred to Wave 9 per D-22"
  - "workers.WorkersRuntime.bindChannel() call retained in createSharedChannel() -- Wave 9 source-root retirement scope"
  - "All 109 recruits network files migrated including non-message utility types (CommandTargeting, RecruitsNetworkRegistrar stub)"

requirements-completed:
  - SRCMOVE-02

duration: ~10 min
completed: 2026-04-15T00:00:00Z
---

# Phase 21 Plan 08: Wave 8 Network Consolidation Summary

**Migrated all 109 `recruits/network/` and 24 `workers/network/` Java files into `com.talhanation.bannermod.network.messages.{military,civilian}.**`, and rewrote `BannerModNetworkBootstrap` with canonical `MILITARY_MESSAGES[104]` + `CIVILIAN_MESSAGES[20]` class-level arrays so `workerPacketOffset() == MILITARY_MESSAGES.length == 104` is now compile-time provable.**

## Performance

- **Duration:** ~10 min
- **Completed:** 2026-04-15
- **Tasks:** 2 atomic task commits in outer repo + 2 clone removal commits
- **Outer files created:** 133 Java files (109 military + 24 civilian)
- **Outer files modified:** ~15 bannermod.* files (FQN sweep) + BannerModNetworkBootstrap.java + MERGE_NOTES.md

## Accomplishments

### Task 1: Bulk-migrate recruits/workers network packages (outer `6f3a4bd`, clone recruits `4138b81b`, clone workers `f4d1023`)

- Copied 109 `recruits/network/*.java` → `bannermod/network/messages/military/`.
- Copied 24 `workers/network/*.java` → `bannermod/network/messages/civilian/`.
- Per-file `sed` rewrote `package com.talhanation.recruits.network` → `package com.talhanation.bannermod.network.messages.military` (and workers equivalent).
- Import rewrite: `recruits.network.*` → `bannermod.network.messages.military.*`; `workers.network.*` → `bannermod.network.messages.civilian.*`.
- Cross-subsystem compound `sed` pass across both new subtrees + full outer `bannermod` tree:
  - `recruits.entities.ai.` → `bannermod.ai.military.`
  - `recruits.entities.` → `bannermod.entity.military.`
  - `recruits.world.` → `bannermod.persistence.military.`
  - `workers.entities.ai.animals.` → `bannermod.ai.civilian.animals.`
  - `workers.entities.ai.` → `bannermod.ai.civilian.`
  - `workers.entities.workarea.` → `bannermod.entity.civilian.workarea.`
  - `workers.entities.` → `bannermod.entity.civilian.`
  - `workers.client.*` → `bannermod.client.civilian.*`
  - `workers.world.` → `bannermod.persistence.civilian.`
  - `workers.inventory.` → `bannermod.inventory.civilian.`
  - `workers.items.` → `bannermod.items.civilian.`
  - `workers.settlement.` → `bannermod.settlement.civilian.`
  - `workers.init.` → `bannermod.registry.civilian.`
  - Top-level recruits event classes (RecruitEvents, FactionEvents, CommandEvents, ClaimEvents, VillagerEvents, DebugEvents) → `bannermod.events.*`
  - `recruits.init.ModItems` → `bannermod.registry.military.ModItems`
  - `recruits.config.RecruitsServerConfig` → `bannermod.config.RecruitsServerConfig`
  - `recruits.Main` → `bannermod.bootstrap.BannerModMain`
- Full outer `bannermod` tree sweep: all remaining `recruits.network.*` / `workers.network.*` FQNs updated.
- `git rm -rf recruits/network/` committed in recruits clone (`4138b81b`, 109 files).
- `git rm -rf workers/network/` committed in workers clone (`f4d1023`, 24 files).

### Task 2: Rewrite BannerModNetworkBootstrap with canonical shared-channel + offset contract (outer `a1b7745`)

- Moved `recruitMessages[]` and `workerMessages[]` locals to class-level `MILITARY_MESSAGES[]` and `CIVILIAN_MESSAGES[]` public constants.
- `workerPacketOffset()` now returns `MILITARY_MESSAGES.length` (compile-time = 104) instead of a hardcoded int constant.
- `createSharedChannel()` registers both arrays in loops: military at `[0..MILITARY_MESSAGES.length)`, civilian at `[MILITARY_MESSAGES.length..+CIVILIAN_MESSAGES.length)`.
- Imports updated to `bannermod.network.messages.military.*` and `bannermod.network.messages.civilian.*`.
- Wave-8 section appended to `MERGE_NOTES.md` with full packet counts, ordering provenance, and remaining deferrals.

## Task Commits

**Outer repo (`/home/kaiserroman/bannermod`, branch `master`):**
1. `6f3a4bd` -- `feat(21-08): migrate recruits/workers network packages to bannermod.network.messages.{military,civilian}` (195 files +8667 -90)
2. `a1b7745` -- `feat(21-08): rewrite BannerModNetworkBootstrap with MILITARY_MESSAGES/CIVILIAN_MESSAGES + offset contract` (2 files +214 -146)

**recruits/ clone (branch `main`):**
1. `4138b81b` -- `refactor(21-08): remove migrated network subtree (109 files → bannermod.network.messages.military)` (109 files, 6868 deletions)

**workers/ clone (branch `V2-1.20.1`):**
1. `f4d1023` -- `refactor(21-08): remove migrated network subtree (24 files → bannermod.network.messages.civilian)` (24 files, 1709 deletions)

## File Counts Per Package

| Package | Files |
|---|---|
| `bannermod.network.messages.military.**` | 109 |
| `bannermod.network.messages.civilian.**` | 24 |
| **Total new Java** | **133** |

**Packet ID Catalog:**

| Array | Count | Channel Indices |
|---|---|---|
| `MILITARY_MESSAGES` | 104 message classes | [0..103] |
| `CIVILIAN_MESSAGES` | 20 message classes | [104..123] |
| `workerPacketOffset()` | **104** | compile-time |

Note: The military directory has 109 files total (104 message classes + 5 non-message utility types: `CommandTargeting`, `RecruitsNetworkRegistrar`, and similar supporting classes). The civilian directory has 24 files total (20 message classes + 4 utility types: `WorkAreaAuthoringRules`, `WorkAreaRotation`, `BuildAreaUpdateAuthoring`, `WorkersNetworkRegistrar`).

## Decisions Made

- **workerPacketOffset derived from array length** -- `workerPacketOffset()` now returns `MILITARY_MESSAGES.length` rather than a hardcoded `104`. This makes the must-have from 21-03 ("worker packet IDs at offset = recruit catalog size") compile-time provable.
- **Ordering verbatim** -- `MILITARY_MESSAGES[]` and `CIVILIAN_MESSAGES[]` bodies copy the exact class sequence from the pre-migration `BannerModNetworkBootstrap.java`, which itself captured the legacy `Main.setup()` and `WorkersMain.setup()` ordering. No desync risk.
- **workers.config.WorkersServerConfig deferred** -- `MessageAddWorkArea.java` still imports `workers.config.WorkersServerConfig`. This is 1 reference, consistent with Wave 7 precedent for config migration deferral to Wave 9.
- **workers.WorkersRuntime.bindChannel() retained** -- The compatibility bind call remains in `createSharedChannel()` until Wave 9 retires the workers clone source root.
- **Utility types migrated as-is** -- `CommandTargeting`, `WorkAreaAuthoringRules`, `WorkAreaRotation`, `BuildAreaUpdateAuthoring`, and the deprecated registrar stubs are now under `bannermod.network.messages.*`. They are utility/helper types, not message classes — they do not appear in the registration arrays.

## Deviations from Plan

### [Rule 1 - Bug] FQN sweep required multiple passes for top-level recruits event classes

- **Found during:** Task 1 — after the primary network-import rewrite pass, `grep` revealed remaining `recruits.FactionEvents`, `recruits.CommandEvents`, `recruits.RecruitEvents`, `recruits.VillagerEvents`, `recruits.DebugEvents`, `recruits.ClaimEvents`, `recruits.init.ModItems`, `recruits.config.RecruitsServerConfig`, `recruits.Main` references in the migrated files.
- **Issue:** The initial cross-subsystem sed pass only covered `recruits.entities.*`, `recruits.world.*` etc. — not bare top-level `recruits.ClassName` imports that packet handlers use for their business logic.
- **Fix:** Additional targeted sed pass replaced all bare top-level recruits class refs with their Wave-4-migrated `bannermod.events.*` / `bannermod.registry.*` / `bannermod.config.*` / `bannermod.bootstrap.*` equivalents.
- **Files affected:** Multiple military network message files (MessageHire, MessageOpenTeamEditScreen, MessageStrategicFire, MessageDismount, MessageSendJoinRequestTeam, and others).
- **Committed in:** `6f3a4bd`.

## Known Stubs

None. All migrated network message classes are complete source files implementing the existing packet encode/decode/handle logic. No placeholder returns or TODO-gated handlers introduced.

## Residual Work (for 21-09)

- `workers.config.WorkersServerConfig` import in `bannermod.network.messages.civilian.MessageAddWorkArea` — config migration.
- `workers.WorkersRuntime.bindChannel()` call in `BannerModNetworkBootstrap.createSharedChannel()` — source-root retirement.
- `workers/` clone remaining files: `WorkersMain.java`, `WorkersRuntime.java`, `WorkersSubsystem.java`, `MergedRuntimeCleanupPolicy.java`, `init/WorkersLifecycleRegistrar.java`.
- `recruits/` clone remaining files: `Main.java` and other shim group.
- `build.gradle` source-set cleanup: retire embedded clone source roots.
- Final compile-green gate.

## Threat Flags

None. This wave relocates pure packet handler classes (encode/decode/handle logic) without introducing new network endpoints, auth paths, or schema changes. The channel structure and packet IDs are identical to the pre-migration state.

## Self-Check

**Created files verified:**
- FOUND: MessageMovement.java (military)
- FOUND: CommandTargeting.java (military)
- FOUND: RecruitsNetworkRegistrar.java (military)
- FOUND: MessageAddWorkArea.java (civilian)
- FOUND: WorkAreaAuthoringRules.java (civilian)
- FOUND: WorkersNetworkRegistrar.java (civilian)
- FOUND: BannerModNetworkBootstrap.java (rewritten)

**Commits verified:**
- FOUND: outer `6f3a4bd` (Task 1 feat)
- FOUND: outer `a1b7745` (Task 2 feat)
- FOUND: recruits clone `4138b81b` (rm network)
- FOUND: workers clone `f4d1023` (rm network)

**Clone state verified:**
- recruits/network/: GONE
- workers/network/: GONE
- recruits/Main.java: PRESENT (shim)
- workers/WorkersMain.java: PRESENT (shim)

**Packet count verified:**
- MILITARY_MESSAGES array entries: 104 (matching WORKER_PACKET_OFFSET = 104)
- CIVILIAN_MESSAGES array entries: 20

## Self-Check: PASSED

---
*Phase: 21-source-tree-consolidation-into-bannerlord*
*Wave: 8*
*Completed: 2026-04-15*
