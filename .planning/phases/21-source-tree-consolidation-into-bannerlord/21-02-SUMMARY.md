---
phase: 21-source-tree-consolidation-into-bannerlord
plan: 02
subsystem: shared-seam-ownership
tags: [bannermod, shared, authority, settlement, logistics, deprecation, forwarders, namespace-pivot]

dependency_graph:
  requires:
    - phase: 21-01
      provides: "Pivot reset (clean post-bannerlord-revert tree, pivot-era PLAN files restored, MERGE_NOTES Phase 21 Pivot section authored)"
  provides:
    - "Canonical shared-seam ownership under com.talhanation.bannermod.shared.{authority,settlement,logistics} for 5 in-scope classes"
    - "@Deprecated thin forwarders preserved at the legacy bannermod.{authority,settlement,logistics} paths for staged migration"
    - "All 17 active callers (governance, JUnit, gametest) retargeted at bannermod.shared.* directly"
    - "MERGE_NOTES.md shared-package overlap entry under the existing Phase 21 Pivot section"
  affects:
    - "21-03 (military control foundations) -- can import seams from bannermod.shared.* directly"
    - "21-04 (worker civilian ownership) -- introduces AbstractWorkerEntity, unblocks deferred Service/Route/CourierTask follow-up"
    - "21-05..21-09 -- all later moves now have a stable bannermod.shared.* import target"

tech-stack:
  added: []
  patterns:
    - "Deprecated thin forwarder: legacy package keeps original FQN, holds @Deprecated mirror types, delegates every method through shared <-> legacy mapping helpers (no duplicate logic)"
    - "Documented overlap (D-05): structural move + MERGE_NOTES entry, not a semantic dedup; reconciliation deferred to a separate post-Phase-21 cleanup phase"

key-files:
  created:
    - "src/main/java/com/talhanation/bannermod/shared/authority/BannerModAuthorityRules.java"
    - "src/main/java/com/talhanation/bannermod/shared/settlement/BannerModSettlementBinding.java"
    - "src/main/java/com/talhanation/bannermod/shared/logistics/BannerModSupplyStatus.java"
    - "src/main/java/com/talhanation/bannermod/shared/logistics/BannerModUpkeepProviders.java"
    - "src/main/java/com/talhanation/bannermod/shared/logistics/BannerModCombinedContainer.java"
  modified:
    - "src/main/java/com/talhanation/bannermod/authority/BannerModAuthorityRules.java"
    - "src/main/java/com/talhanation/bannermod/settlement/BannerModSettlementBinding.java"
    - "src/main/java/com/talhanation/bannermod/logistics/BannerModSupplyStatus.java"
    - "src/main/java/com/talhanation/bannermod/logistics/BannerModUpkeepProviders.java"
    - "src/main/java/com/talhanation/bannermod/logistics/BannerModCombinedContainer.java"
    - "src/main/java/com/talhanation/bannermod/governance/BannerModGovernorAuthority.java"
    - "src/main/java/com/talhanation/bannermod/governance/BannerModGovernorHeartbeat.java"
    - "src/main/java/com/talhanation/bannermod/governance/BannerModGovernorRules.java"
    - "src/main/java/com/talhanation/bannermod/governance/BannerModGovernorService.java"
    - "src/test/java/com/talhanation/bannermod/BannerModAuthorityRulesTest.java"
    - "src/test/java/com/talhanation/bannermod/BannerModSupplyStatusTest.java"
    - "src/test/java/com/talhanation/bannermod/governance/BannerModGovernorHeartbeatTest.java"
    - "src/test/java/com/talhanation/bannermod/governance/BannerModGovernorRulesTest.java"
    - "src/test/java/com/talhanation/workers/MiningClaimExcavationRulesTest.java"
    - "src/test/java/com/talhanation/workers/WorkerSettlementSpawnRulesTest.java"
    - "src/gametest/java/com/talhanation/bannermod/BannerModClaimWorkerGrowthGameTests.java"
    - "src/gametest/java/com/talhanation/bannermod/BannerModGovernorControlGameTests.java"
    - "src/gametest/java/com/talhanation/bannermod/BannerModPlayerCycleGameTests.java"
    - "src/gametest/java/com/talhanation/bannermod/BannerModSettlementFactionDegradationGameTests.java"
    - "src/gametest/java/com/talhanation/bannermod/BannerModSettlementFactionEnforcementGameTests.java"
    - "src/gametest/java/com/talhanation/bannermod/BannerModUpkeepFlowGameTests.java"
    - "src/gametest/java/com/talhanation/bannermod/BannerModWorkerBirthAndSettlementSpawnGameTests.java"
    - "MERGE_NOTES.md"

key-decisions:
  - "Option A narrowed scope: only the 5 classes with extant legacy implementations were moved (Authority, Settlement, SupplyStatus, UpkeepProviders, CombinedContainer). The 3 classes named in the original 21-02 plan but with no implementation in bannermod.logistics (Service, Route, CourierTask) were deferred."
  - "Forwarders mirror enums/records literal-for-literal and delegate via shared <-> legacy mapping helpers; no duplicate behavior is retained, and forwarder lifespan is intentionally short (deletion deferred to a post-Phase-21 cleanup phase per D-05)."
  - "Per D-05/D-17 the shared-package overlap is documented in MERGE_NOTES.md under the existing 21-01-authored Phase 21 Pivot section, not in a new top-level section, to keep the pivot record contiguous."

patterns-established:
  - "Shared-seam canonical FQN convention: com.talhanation.bannermod.shared.{authority,settlement,logistics}.* (sibling subtree alongside the preserved bannermod.{citizen,governance,settlement,logistics,authority,config} domain services per CONTEXT D-04)."
  - "Legacy-peer forwarder pattern: keep original FQN @Deprecated, delegate every public surface through name-based mappers, document the overlap in MERGE_NOTES instead of deduplicating in-phase."

requirements-completed:
  - SRCMOVE-01

duration: ~25min
completed: 2026-04-15
---

# Phase 21 Plan 02: Bannermod Shared Seam Ownership Summary

**Canonical shared seam ownership for 5 authority/settlement/logistics classes landed under `com.talhanation.bannermod.shared.*`, with the legacy bannermod peers reduced to `@Deprecated` thin forwarders and all 17 active callers retargeted in-phase.**

## Performance

- **Duration:** ~25 min (resumed across 2 sessions; first session created canonicals, second session committed forwarders + retarget + MERGE_NOTES + finalize)
- **Started:** prior session (canonicals commit `e4177f1`)
- **Completed:** 2026-04-15T08:24:09Z
- **Tasks:** 4 atomic commits (plus final metadata commit)
- **Files modified:** 23 source/test files + MERGE_NOTES.md + 5 new canonicals

## Accomplishments

- Created the 5 canonical shared-seam files under `bannermod.shared.{authority,settlement,logistics}` (Authority/Settlement/SupplyStatus/UpkeepProviders/CombinedContainer).
- Reduced the 5 legacy bannermod peers to `@Deprecated` thin forwarders that delegate every public method/enum/record into the canonical classes via mapping helpers, with no duplicate behavior retained.
- Retargeted all 17 active in-tree callers (4 governance services, 6 JUnit tests, 7 gametest scenarios) to import from `bannermod.shared.*` directly. Verified `recruits/` and `workers/` legacy source roots had zero remaining callers.
- Recorded the documented shared-package overlap and forwarder-lifespan policy in `MERGE_NOTES.md` under the existing `## Phase 21 Pivot (2026-04-15)` section authored by 21-01.
- Live runtime contract preserved: mod id `bannermod`, config filenames `bannermod-{military,settlement,client}.toml`, claim-derived settlement binding all unchanged.

## Task Commits

1. **Task 1: Create canonical bannermod.shared.* implementations** -- `e4177f1` (feat) -- prior session
2. **Task 2: Reduce legacy bannermod seams to @Deprecated forwarders** -- `6b46d19` (refactor)
3. **Task 3: Retarget seam callers from legacy bannermod to bannermod.shared** -- `b376da5` (fix)
4. **Task 4: Record bannermod.shared overlap in merge notes** -- `188d2c1` (docs)

**Plan metadata commit:** to be recorded after this SUMMARY commits.

## Files Created/Modified

### Created (5 canonical shared-seam classes, prior commit e4177f1)
- `src/main/java/com/talhanation/bannermod/shared/authority/BannerModAuthorityRules.java` -- Authority decision seam (Relationship/Decision enums + resolvers)
- `src/main/java/com/talhanation/bannermod/shared/settlement/BannerModSettlementBinding.java` -- Settlement binding seam (Status enum, Binding record, resolveFactionStatus/resolveSettlementStatus/allowsWorkAreaPlacement overloads)
- `src/main/java/com/talhanation/bannermod/shared/logistics/BannerModSupplyStatus.java` -- Build/recruit/worker supply status calculators
- `src/main/java/com/talhanation/bannermod/shared/logistics/BannerModUpkeepProviders.java` -- Block/entity upkeep target validation + container resolution
- `src/main/java/com/talhanation/bannermod/shared/logistics/BannerModCombinedContainer.java` -- Multi-source container aggregator

### Modified (5 legacy peers reduced to forwarders, commit 6b46d19)
- `src/main/java/com/talhanation/bannermod/authority/BannerModAuthorityRules.java` -- @Deprecated, delegates via Relationship/Decision name-based mappers
- `src/main/java/com/talhanation/bannermod/settlement/BannerModSettlementBinding.java` -- @Deprecated, delegates all 6 resolver overloads + allowsWorkAreaPlacement + allowsSettlementOperation
- `src/main/java/com/talhanation/bannermod/logistics/BannerModSupplyStatus.java` -- @Deprecated, delegates buildProjectStatus/workerSupplyStatus/recruitSupplyStatus
- `src/main/java/com/talhanation/bannermod/logistics/BannerModUpkeepProviders.java` -- @Deprecated, delegates 4 static helpers
- `src/main/java/com/talhanation/bannermod/logistics/BannerModCombinedContainer.java` -- @Deprecated, thin subclass of canonical (inherits behavior, only forwards `of(List)` factory)

### Modified (17 caller files retargeted, commit b376da5)
- `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorAuthority.java`
- `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorHeartbeat.java`
- `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorRules.java`
- `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorService.java`
- `src/test/java/com/talhanation/bannermod/BannerModAuthorityRulesTest.java`
- `src/test/java/com/talhanation/bannermod/BannerModSupplyStatusTest.java`
- `src/test/java/com/talhanation/bannermod/governance/BannerModGovernorHeartbeatTest.java`
- `src/test/java/com/talhanation/bannermod/governance/BannerModGovernorRulesTest.java`
- `src/test/java/com/talhanation/workers/MiningClaimExcavationRulesTest.java`
- `src/test/java/com/talhanation/workers/WorkerSettlementSpawnRulesTest.java`
- `src/gametest/java/com/talhanation/bannermod/BannerModClaimWorkerGrowthGameTests.java`
- `src/gametest/java/com/talhanation/bannermod/BannerModGovernorControlGameTests.java`
- `src/gametest/java/com/talhanation/bannermod/BannerModPlayerCycleGameTests.java`
- `src/gametest/java/com/talhanation/bannermod/BannerModSettlementFactionDegradationGameTests.java`
- `src/gametest/java/com/talhanation/bannermod/BannerModSettlementFactionEnforcementGameTests.java`
- `src/gametest/java/com/talhanation/bannermod/BannerModUpkeepFlowGameTests.java`
- `src/gametest/java/com/talhanation/bannermod/BannerModWorkerBirthAndSettlementSpawnGameTests.java`

### Modified (documentation, commit 188d2c1)
- `MERGE_NOTES.md` -- Appended `### Shared-package overlap (authored by 21-02)` subsection under existing `## Phase 21 Pivot (2026-04-15)` section

## Decisions Made

- **Option A narrowed scope:** Moved only the 5 classes with extant implementations in `bannermod.{authority,settlement,logistics}` (Authority, Settlement, SupplyStatus, UpkeepProviders, CombinedContainer). The other 3 classes named in the original plan body -- `BannerModLogisticsService`, `BannerModLogisticsRoute`, `BannerModCourierTask` -- have no implementation in `bannermod.logistics` today, no callers anywhere in the tree, and depend on the worker civilian seam (`AbstractWorkerEntity`) that lands in wave 21-04. They are deferred to be created in `bannermod.shared.logistics` directly by the plan that introduces their first caller.
- **Per D-05 do-not-dedup policy:** The legacy peers are kept live as @Deprecated forwarders, not deleted. Reconciliation (forwarder deletion) is deferred to a separate post-Phase-21 cleanup phase. The structural move + MERGE_NOTES entry IS the deliverable for this plan; the semantic merge is out of scope.
- **MERGE_NOTES placement:** Appended the shared-overlap subsection under the existing `## Phase 21 Pivot (2026-04-15)` section authored by 21-01, rather than creating a new top-level section, to keep the pivot record contiguous (per CONTEXT D-17).

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 4 -> Rule 1 - Scope correction] Plan body listed 8 files; only 5 had legacy implementations**
- **Found during:** Task 1 (canonical creation), prior session
- **Issue:** The 21-02 plan body listed 8 canonical files to create (Authority, Settlement, plus 6 logistics: SupplyStatus, UpkeepProviders, LogisticsService, LogisticsRoute, CourierTask, CombinedContainer). On inspection of `src/main/java/com/talhanation/bannermod/logistics/`, only SupplyStatus, UpkeepProviders, and CombinedContainer had real implementations there. LogisticsService, LogisticsRoute, and CourierTask had no source files in `bannermod.logistics` to "move from" -- they had been targeted for the bannerlord-era namespace which 21-01 reverted, and have no callers in the current tree.
- **Fix:** Narrowed scope to the 5 classes with extant legacy implementations. The 3 deferred classes will be created directly under `bannermod.shared.logistics` by the plan that introduces their first caller (expected wave 21-04 since they depend on `AbstractWorkerEntity`).
- **Files affected:** Plan was implicitly narrowed; actual files created/modified match the new scope.
- **Verification:** `git status` shows exactly the 5 canonical creations + 5 forwarder reductions + 17 caller retargets, no stub/empty files for the deferred 3.
- **Committed in:** Decision applied in `e4177f1` (canonicals) and `6b46d19` (forwarders); documented here and in MERGE_NOTES (`188d2c1`).

**2. [Rule 2 - Critical functionality] MERGE_NOTES placement aligned with D-17 ownership boundary**
- **Found during:** Task 4 (MERGE_NOTES append)
- **Issue:** The original plan body for Task 2 says "append to MERGE_NOTES.md a subsection" but does not specify where. CONTEXT D-17 states 21-01 owns the pivot rationale and revert range; 21-02 owns only the shared-overlap note. The 21-01 SUMMARY confirmed the `## Phase 21 Pivot (2026-04-15)` top-level section was already authored and explicitly says "Plan 21-02 is the only place the shared-package overlap note lives -- this section does NOT duplicate it."
- **Fix:** Appended `### Shared-package overlap (authored by 21-02)` as a new H3 subsection under the existing 21-01-authored H2 section, immediately after the `### Re-execution target` subsection. Did not duplicate pivot rationale or revert range.
- **Files affected:** `MERGE_NOTES.md` (commit `188d2c1`).
- **Verification:** Plan automated check `grep -q 'Phase 21 Pivot' MERGE_NOTES.md && grep -q 'bannermod.shared' MERGE_NOTES.md` passes.
- **Committed in:** `188d2c1`.

---

**Total deviations:** 2 documented (1 Rule-4-style scope narrowing, 1 Rule-2-style placement correction)
**Impact on plan:** Both deviations align the plan body with the post-revert reality of the codebase. The narrowed scope is strictly safer (no empty / stub canonicals shipped), and the MERGE_NOTES placement aligns with CONTEXT D-17 ownership. No success criteria are weakened.

## Issues Encountered

- **Resume across rate-limit boundary:** Task 1 (canonical creation) committed in prior session as `e4177f1`; the agent was interrupted by a rate limit while Task 2 (forwarders) was in working tree but uncommitted. Continuation agent verified the 5 modified-file set was exactly the forwarder reduction (no duplicate logic, all `@Deprecated`, all delegate into `bannermod.shared.*`) before committing. No re-do needed.

## Next Phase Readiness

- **Plan 21-03 (military control foundations):** Can import `BannerModAuthorityRules` from `bannermod.shared.authority` directly. No legacy seam coupling.
- **Plan 21-04 (worker civilian ownership):** Should also create the deferred `bannermod.shared.logistics.{BannerModLogisticsService, BannerModLogisticsRoute, BannerModCourierTask}` when the first caller is introduced (`AbstractWorkerEntity` is the expected dependency).
- **Plans 21-05..21-09:** All can import from `bannermod.shared.*` directly.
- **Forwarder lifespan:** The `@Deprecated` legacy peers stay live for the duration of Phase 21 to absorb any external/IDE references. Deletion is owned by a separate post-Phase-21 cleanup phase, not by any plan within Phase 21.

## Known Stubs

None. All 5 canonical files contain real implementations (moved from the legacy peers, not stubbed). All 5 forwarders delegate to working canonical methods, not to TODO placeholders. The 3 deferred classes (Service/Route/CourierTask) were intentionally NOT created as stubs -- they will be created with real implementations by the plan that introduces their first caller.

## Threat Flags

None. No new network endpoints, auth paths, file access patterns, or trust-boundary schema changes. Authority decision logic, settlement binding logic, and supply-status logic moved package-only with mapping helpers preserving identical semantics. Live runtime contract (mod id, config filenames, settlement vocabulary) is unchanged.

## Self-Check

- **Files created (5):**
  - FOUND: src/main/java/com/talhanation/bannermod/shared/authority/BannerModAuthorityRules.java
  - FOUND: src/main/java/com/talhanation/bannermod/shared/settlement/BannerModSettlementBinding.java
  - FOUND: src/main/java/com/talhanation/bannermod/shared/logistics/BannerModSupplyStatus.java
  - FOUND: src/main/java/com/talhanation/bannermod/shared/logistics/BannerModUpkeepProviders.java
  - FOUND: src/main/java/com/talhanation/bannermod/shared/logistics/BannerModCombinedContainer.java
- **Files modified (23 + MERGE_NOTES):** verified via git log per commit
- **Commits (4):**
  - FOUND: e4177f1 feat(21-02): create bannermod.shared seam ownership (narrowed scope)
  - FOUND: 6b46d19 refactor(21-02): reduce legacy bannermod seams to @Deprecated forwarders
  - FOUND: b376da5 fix(21-02): retarget seam callers from legacy bannermod to bannermod.shared
  - FOUND: 188d2c1 docs(21-02): record bannermod.shared overlap in merge notes
- **Plan automated verification:** PASS (no legacy imports outside legacy package itself; MERGE_NOTES contains both `Phase 21 Pivot` and `bannermod.shared`; all forwarders contain `@Deprecated`)

## Self-Check: PASSED

---
*Phase: 21-source-tree-consolidation-into-bannerlord*
*Completed: 2026-04-15*
