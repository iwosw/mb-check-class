---
phase: 23
plan: 06
subsystem: gametest-tree
tags: [test-tree, consolidation, settlement-binding, supply-status, gametest, phase-21-followup]
key-files:
  vendored:
    - src/gametest/java/com/talhanation/bannermod/gametest/support/RecruitsBattleGameTestSupport.java
    - src/gametest/java/com/talhanation/bannermod/gametest/support/RecruitsCommandGameTestSupport.java
    - src/gametest/resources/data/bannermod/structures/harness_empty.nbt
  modified:
    - src/gametest/java/com/talhanation/bannermod/BannerModSettlementFactionEnforcementGameTests.java
    - src/gametest/java/com/talhanation/bannermod/BannerModSettlementFactionDegradationGameTests.java
    - src/gametest/java/com/talhanation/bannermod/BannerModClaimWorkerGrowthGameTests.java
    - src/gametest/java/com/talhanation/bannermod/BannerModUpkeepFlowGameTests.java
    - src/gametest/java/com/talhanation/bannermod/BannerModPlayerCycleGameTests.java
    - src/gametest/java/com/talhanation/bannermod/IntegratedRuntimeGameTests.java
    - src/gametest/java/com/talhanation/bannermod/BannerModMultiplayerCooperationGameTests.java
    - src/gametest/java/com/talhanation/bannermod/BannerModMultiplayerAuthorityConflictGameTests.java
    - src/test/java/com/talhanation/bannermod/BannerModIntegratedRuntimeSmokeTest.java
gates:
  compileJava: GREEN
  compileGameTestJava: GREEN
  test: RED (pre-existing, out-of-scope — see Deferred Issues)
  verifyGameTestStage: RED (pre-existing production bug surfaced by runtime gate — escalated to deferred-items.md)
---

# Phase 23 Plan 06: Gametest Tree Consolidation Cleanup Summary

Completes the phase-21 source-tree consolidation by repairing every gametest source file that still referenced pre-consolidation packages, message-bus helpers, and accessor methods that no longer exist on the merged bannermod runtime. Restores `compileGameTestJava` to green and vendors the missing `harness_empty.nbt` fixture so `runGameTestServer` can advance past template loading. The remaining `verifyGameTestStage` runtime failure is a pre-existing production bug in `AbstractRecruitEntity.getHurtSound`, escalated.

## Outcome

- **`compileGameTestJava`: GREEN** (was RED with 21 errors in 8 files at plan start)
- **`compileJava`: GREEN** (unchanged — no production code modified)
- **`verifyGameTestStage`: RED** — runtime crash in production code `AbstractRecruitEntity.getHurtSound` (`Cannot get config value before config is loaded`). Escalated; out of scope.
- **`./gradlew test`: RED** — same two pre-existing root test failures (`CitizenRecruitBridgeTest`, `CitizenWorkerBridgeTest`) already documented in `deferred-items.md`. Untouched by this plan.

## Commits

| # | Hash      | Message |
|---|-----------|---------|
| 1 | `199b152` | chore(23-06): vendor recruits battle and command gametest support into bannermod namespace |
| 2 | `50c03f3` | feat(23-06): trim unused profiling surface from vendored battle support |
| 3 | `1485c2f` | feat(23-06): sweep stale FQNs across sibling gametest files |
| 4 | `37d9fd0` | feat(23-06): sweep stale FQNs across root test tree |
| 5 | `1e7287b` | fix(23-06): repoint smoke test to consolidated network bootstrap api |
| 6 | `b99ccbc` | feat(23-06): repoint settlement faction enforcement gametests to consolidated binding seam |
| 7 | `496b4c5` | feat(23-06): repoint settlement degradation gametests to consolidated binding seam |
| 8 | `af93fea` | feat(23-06): repoint claim worker growth gametests to consolidated settlement binding |
| 9 | `466e3ef` | feat(23-06): repoint upkeep flow gametest to consolidated supply status seam |
| 10 | `f2c97fe` | feat(23-06): repoint integrated player cycle gametest to consolidated supply status seam |
| 11 | `ea10cd3` | feat(23-06): repoint integrated runtime gametest to consolidated network bootstrap |
| 12 | `75b7946` | feat(23-06): repoint multiplayer cooperation gametest to consolidated build-area authoring seam |
| 13 | `04e17d0` | feat(23-06): repoint multiplayer authority conflict gametest to consolidated build-area authoring seam |
| 14 | `ba4d183` | test(23-06): vendor harness_empty gametest structure into live src tree |

## What Was Done

### Mechanical FQN sweep (commits 1-5)
Vendored `RecruitsBattleGameTestSupport` / `RecruitsCommandGameTestSupport` from the legacy `recruits/src/gametest/` archive into the consolidated `src/gametest/java/com/talhanation/bannermod/gametest/support/` namespace, swept stale `com.talhanation.recruits.*` / `com.talhanation.workers.*` / `Main.MOD_ID` references throughout sibling gametest files and the root test tree, and rebuilt the smoke test against `BannerModNetworkBootstrap.workerPacketOffset()`.

### Targeted test-body rewrites (commits 6-13, Option Y scope extension — see Deviations)
Replaced removed APIs with calls into the post-consolidation surface that production code already uses:

- **`shared.settlement.BannerModSettlementBinding` → `bannermod.settlement.BannerModSettlementBinding`** (the non-shared variant accepted by `WorkersVillagerEvents.attemptClaimWorkerGrowth` and used by `MiningArea.allowsExcavation`). Applied across enforcement, degradation, and claim-worker-growth gametests.
- **`CropArea.getSettlementBinding()` (removed)** → explicit `BannerModSettlementBinding.resolveFactionStatus(...)` for placement enforcement and `BannerModSettlementBinding.resolveSettlementStatus(...)` for degradation, sourcing the live `RecruitsClaimManager` from `ClaimEvents.recruitsClaimManager` (mirrors `MiningArea.allowsExcavation`).
- **`shared.logistics.BannerModSupplyStatus` → `bannermod.logistics.BannerModSupplyStatus`** (the non-shared variant returned by `AbstractRecruitEntity.getSupplyStatus(Container)`). Applied to upkeep-flow and integrated player-cycle gametests.
- **`BuildArea.getSupplyStatus()` (removed)** → explicit `BannerModSupplyStatus.buildProjectStatus(buildArea.hasStructureTemplate(), buildArea.hasPendingBuildWork(), buildArea.getRequiredMaterials())`.
- **`MessageAddWorkArea.executeForPlayer(player) -> boolean` (removed)** → in-test `attemptWorkAreaPlacement` helper that mirrors `MessageAddWorkArea.executeServerSide` (faction-claim gate via `WorkAreaAuthoringRules.createDecision` + `level.addFreshEntity` on allow). No production code added.
- **`MessageUpdateBuildArea.dispatchToServer(player, msg) -> Decision` (removed)** → in-test `dispatchBuildAreaUpdate` helper that calls the real `BuildAreaUpdateAuthoring.authorize(true, buildArea.getAuthoringAccess(player))` gate and applies `MessageUpdateBuildArea.update(buildArea)` on allow, mirroring `MessageUpdateBuildArea.executeServerSide`. Same approach in both multiplayer gametest files.
- **`RecruitsNetworkRegistrar.orderedMessageTypes()` / `WorkersSubsystem.networkMessageCount()` (removed dead introspection)** → assertion against `BannerModNetworkBootstrap.MILITARY_MESSAGES.length` / `workerPacketOffset()` / `CIVILIAN_MESSAGES.length` (mirrors the smoke test pattern from commit 5).

### Test-tree resource fix (commit 14)
Vendored `harness_empty.nbt` from `recruits/src/gametest/resources/data/bannermod/structures/` into `src/gametest/resources/data/bannermod/structures/`. Phase-21 moved gametest java sources but never vendored the gametest resources, so `runGameTestServer` always crashed before any test executed. Discovered when running `verifyGameTestStage` for the first time end-to-end after Option Y completed.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 — Plan materially underspecified] Option 1 (mechanical FQN sweep) — applied during commits 1-5**
- **Found during:** Initial plan execution (prior agent session, before this resume).
- **Issue:** Plan 23-06 originally scoped only ~3 mechanical edits, but `compileGameTestJava` actually had 21 errors across 8 files plus one missing test resource.
- **Fix:** Pivoted to full sweep across the gametest tree and root test tree.
- **Approval:** Approved by user mid-execution before agent stop.

**2. [Rule 3 — Plan materially underspecified] Option Y (targeted test-body rewrites) — applied during commits 6-13**
- **Found during:** Resume of plan 23-06 in this session.
- **Issue:** After Option 1 cleared mechanical errors, 21 deeper errors remained in 8 gametest files referencing pre-consolidation APIs (`getSettlementBinding`, `getSupplyStatus`, `executeForPlayer`, `dispatchToServer`, `orderedMessageTypes`, `networkMessageCount`) that no longer exist on the consolidated bannermod runtime.
- **Fix:** Rewrote test bodies against the real post-consolidation API surface — used the same accessors production code (`MiningArea.allowsExcavation`, `MessageAddWorkArea.executeServerSide`, `MessageUpdateBuildArea.executeServerSide`, `AbstractRecruitEntity.getSupplyStatus`, `BannerModNetworkBootstrap`) already uses. Added one small in-test mirror helper per file when needed (`attemptWorkAreaPlacement`, `dispatchBuildAreaUpdate`); no new production accessors were added.
- **Approval:** Approved by user before this session continued.
- **Files modified:** 8 gametest files (commits 6-13).
- **Hard constraints honoured:** No edits to `src/main/java/com/talhanation/bannermod/governance/**`. No edits to the three 23-05 gametest files. No production accessors added. Original test semantic intent preserved everywhere.

**3. [Rule 3 — Test-tree blocker] Vendored missing `harness_empty.nbt` fixture (commit 14)**
- **Found during:** First end-to-end `verifyGameTestStage` run after Option Y.
- **Issue:** `runGameTestServer` crashed with `Could not find structure file gameteststructures/harness_empty.snbt`. The fixture exists only in `recruits/src/gametest/resources/data/bannermod/structures/`, never copied into the live `src/gametest/resources/` during phase-21 consolidation.
- **Fix:** Copied the single 1.1 KB fixture into `src/gametest/resources/data/bannermod/structures/`. All `@GameTest(template = "harness_empty")` declarations resolve.
- **Files modified:** `src/gametest/resources/data/bannermod/structures/harness_empty.nbt` (vendored).

## Deferred Issues

### Category C runtime blocker — escalation required

**`AbstractRecruitEntity.getHurtSound` reads `ForgeConfigSpec.ConfigValue.get()` before config is loaded.** After the fixture vendoring fix, `runGameTestServer` advances past template loading and starts ticking spawned recruit entities. The first recruit `hurt()` call into `playHurtSound → getHurtSound` (`src/main/java/com/talhanation/bannermod/entity/military/AbstractRecruitEntity.java:1089`) crashes the server with:

```
java.lang.IllegalStateException: Cannot get config value before config is loaded.
This error is currently only thrown in the development environment, to avoid breaking published mods.
```

This is a **production-code bug** that pre-dates plan 23-06 — it was hidden because no one had successfully executed `verifyGameTestStage` end-to-end after the phase-21 consolidation. Fixing it requires editing `AbstractRecruitEntity` (production governance/entity code) to either guard the `ConfigValue.get()` call, cache a default at class init, or override `getHurtSound` for the gametest harness. **Out of scope for plan 23-06's hard constraint** ("Do NOT modify production governance code; prefer test-body rewrites over adding production accessors"). Escalated to `deferred-items.md` plan 23-06 follow-up section.

### Pre-existing root test failures (untouched by this plan)
- `CitizenRecruitBridgeTest` and `CitizenWorkerBridgeTest` reference legacy `recruits/src/main/java/...` and `workers/src/main/java/...` paths via `Path.of(...)`, fail at runtime with `NoSuchFileException`. Already documented in `deferred-items.md` from earlier plan iterations. No relation to plan 23-06.

## Gate Verification

```
./gradlew compileJava test compileGameTestJava verifyGameTestStage --console=plain
```

- `compileJava`: UP-TO-DATE (GREEN)
- `test`: FAILED on the two pre-existing `CitizenRecruitBridgeTest` / `CitizenWorkerBridgeTest` `NoSuchFileException` failures only. No new failures introduced by this plan.
- `compileGameTestJava`: SUCCESS (GREEN — was 21 errors at plan start)
- `verifyGameTestStage / runGameTestServer`: FAILED on the production `AbstractRecruitEntity.getHurtSound` crash described above. The three `BannerModGovernorControlGameTests` `@GameTest` methods could not execute because the entity-tick exception killed the harness before `BannerModGovernorControlGameTests` was scheduled.

Gate logs: `/tmp/phase23-gate.log` (full pipeline) and `/tmp/phase23-gate2.log` (gametest-only after fixture fix).

## Self-Check

- Vendored fixture present: `src/gametest/resources/data/bannermod/structures/harness_empty.nbt` — FOUND.
- All 14 commits exist on master — FOUND.
- `compileGameTestJava` exit code: 0 — VERIFIED (last successful run timestamp prior to the runtime gate attempt).
- Production governance code (`src/main/java/com/talhanation/bannermod/governance/**`) untouched — VERIFIED via `git log -- src/main/java/com/talhanation/bannermod/governance/` showing no commits in this plan.
- The three 23-05 gametest files untouched — VERIFIED.

## Self-Check: PARTIAL

Plan-level success criteria met:
- ✅ All gametest source files compile against the consolidated bannermod runtime.
- ✅ All deviations documented (Option 1, Option Y, fixture vendor).
- ✅ Hard constraints honoured (no production governance edits, no 23-05 file edits, no production accessors added).
- ❌ Cannot demonstrate the three `BannerModGovernorControlGameTests` `@GameTest` methods executing successfully under `verifyGameTestStage` — blocked by the pre-existing `AbstractRecruitEntity.getHurtSound` production bug, which requires a separate plan/escalation to fix.
