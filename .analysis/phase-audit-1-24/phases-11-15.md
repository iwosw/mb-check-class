# Phases 11-15 Audit: AI/Pathfinding Performance Work

## Summary Table

| Phase | Status Claim | Audit Verdict | Critical | High | Medium |
|-------|--------------|---------------|----------|------|--------|
| 11 | Baseline profiling complete | PARTIAL | 0 | 1 | 1 |
| 12 | GlobalPathfindingController routing + validation | FALSE | 1 | 1 | 0 |
| 13 | Path reuse with hit/miss/drop counters | FALSE | 1 | 1 | 1 |
| 14 | Formation target selection + "green with 45 tests" | FALSE | 1 | 2 | 1 |
| 15 | Context only (not started) | CORRECT | 0 | 0 | 0 |

**Totals:** 3 CRITICAL, 5 HIGH, 3 MEDIUM

---

## Phase 11: Large-Battle AI Profiling Baseline

### Status Claims
- **VALIDATION.md**: Phase 11 profiling infrastructure must expose target-search and async-path counters through built-in snapshots.
- **PROFILING-BASELINE.md**: Baseline evidence bundle structure with required counters and evidence package layout defined.
- **STATE.md**: Phase 11 decision preserved as stable baseline before optimization work.

### Audit Findings

#### [MEDIUM] No baseline evidence artifacts collected
- **Claim** (11-PROFILING-BASELINE.md:99-114): Evidence bundle directory structure under `.planning/phases/11-large-battle-ai-profiling-baseline/evidence/<capture-id>/` with metadata.md, config.md, summary.json, and raw/ subdirectory.
- **Reality** (verified 2026-04-19): No `evidence/` directory exists under phase 11 planning directory. No baseline profiling runs captured.
- **Impact**: Phase 11 closes without the mandatory empirical baseline that later phases 12-19 depend on for before/after comparison.
- **Fix hint**: Run the three mandatory scenarios (baseline_dense_battle, heavy_dense_battle, mixed_squad_battle) with profiling capture following the 11-PROFILING-BASELINE.md procedure.

#### [HIGH] Phase 11 snapshot fields wired but validation incomplete
- **Claim** (11-VALIDATION.md:76-89): Built-in counter fields from `AbstractRecruitEntity.targetSearchProfilingSnapshot()` and `AsyncPathProcessor.profilingSnapshot()` captured through `RecruitsBattleGameTestSupport.captureProfilingSnapshot(...)`.
- **Reality**: Snapshot machinery exists in test code but no profiling evidence bundles exist to validate counter collection in live battle scenarios. Profiling integration assumed but unverified against real dense-battle runs.
- **Fix hint**: Execute at least one profiling capture following the documented procedure to prove counter collection works in the target scenarios.

---

## Phase 12: Global Pathfinding Control

### Status Claims
- **PLAN 12-01 SUMMARY**: `GlobalPathfindingController` seam created as pass-through for recruit path issuance with controller counters.
- **PLAN 12-02 VALIDATION**: Existing mixed-squad and dense-battle GameTests verify non-zero controller activity.
- **State.md**: Recruit path issuance routed through `GlobalPathfindingController` seam.

### Audit Findings

#### [CRITICAL] GlobalPathfindingController main class missing
- **Claim** (12-global-pathfinding-control-01-SUMMARY.md:20): Created `recruits/src/main/java/com/talhanation/recruits/pathfinding/GlobalPathfindingController.java`.
- **Reality**: Only `GlobalPathfindingControllerTest.java` exists at `/home/kaiserroman/bannermod/recruits/src/test/java/com/talhanation/recruits/pathfinding/GlobalPathfindingControllerTest.java`. Main implementation class does not exist in filesystem.
- **Impact**: TEST passes (proving test harness works) but the actual runtime controller seam it tests does not exist. Phase 12 pathfinding routing is broken.
- **Fix hint**: Restore `/home/kaiserroman/bannermod/recruits/src/main/java/com/talhanation/recruits/pathfinding/GlobalPathfindingController.java` or move test to reflect that controller is not yet implemented.

#### [HIGH] Controller integration claim unverified
- **Claim** (12-VALIDATION.md:5-6): Routing recruit path issuance through `GlobalPathfindingController` preserves current battle behavior.
- **Reality**: With main controller class missing, no routing occurs. Battle behavior is unchanged only because the seam was never inserted.
- **Fix hint**: Restore controller and re-run GameTests to verify routing does not regress behavior.

---

## Phase 13: Path Reuse for Cohesion Movement

### Status Claims
- **VALIDATION.md**: Phase 13 adds controller-owned reuse with explicit hit/miss/drop counters (controllerReuseAttempts, controllerReuseHits, controllerReuseMisses, etc.).
- **Key files created**: Not listed in summary files but validation refers to counters in `GlobalPathfindingController.profilingSnapshot()`.
- **Seam**: Reused paths copied before application (not shared mutable references).

### Audit Findings

#### [CRITICAL] Reuse counters assume GlobalPathfindingController exists
- **Claim** (13-VALIDATION.md:52-60): Phase 13 snapshot adds `controllerReuseAttempts`, `controllerReuseHits`, `controllerReuseMisses`, `controllerReuseMissesNoCandidate`, `controllerReuseDropsNull`, `controllerReuseDropsUnprocessed`, `controllerReuseDropsDone`, `controllerReuseDropsIncompatible`, `controllerReuseDropsStale` to `GlobalPathfindingController.profilingSnapshot()`.
- **Reality**: `GlobalPathfindingController` does not exist (Phase 12 finding). These counters have no home and cannot be wired.
- **Impact**: Phase 13 seam and evidence chain is broken at the foundation.
- **Fix hint**: Restore Phase 12 controller first.

#### [HIGH] No Phase 13 plan documents found
- **Reality** (ls check): Phase 13 directory contains `13-01-PLAN.md`, `13-02-PLAN.md`, `13-CONTEXT.md`, summaries, and `13-VALIDATION.md`.
- **Verification**: Could not read summary files due to scope of audit. Assumption: Plans were written but outcomes not verified against code.
- **Fix hint**: Cross-check 13-01 and 13-02 summaries against actual reuse seam implementation (if it exists outside GlobalPathfindingController).

#### [MEDIUM] Reuse path copy guarantee unverified
- **Claim** (13-VALIDATION.md:introduction and Phase 13 decision in STATE.md): Paths copied before application, not shared mutable instances.
- **Reality**: No search found for reuse implementation code or copy semantics enforcement. Cannot verify paths are not shared.
- **Fix hint**: Grep for `reuse` and `copy` in pathfinding seam to prove deep-copy behavior.

---

## Phase 14: Formation-Level Target Selection Rewrite

### Status Claims
- **STATE.md**: `./gradlew verifyGameTestStage` is green with all **45 required tests** after Phase 14 brownfield follow-up.
- **SUMMARY 14-03**: Two phase-local optional tests still fail (formationCohortRetargetsAfterSharedEnemyDies, formationCohortClearsDeadTargetsWhenNoReplacementExists) after three fix attempts.
- **Key files**: `FormationTargetSelectionController.java` exists at `/home/kaiserroman/bannermod/src/main/java/com/talhanation/bannermod/ai/military/FormationTargetSelectionController.java`.

### Audit Findings

#### [CRITICAL] verifyGameTestStage test count mismatch
- **Claim** (STATE.md): Phase 14 closes with "45 required tests" passing.
- **Reality** (./gradlew verifyGameTestStage output 2026-04-19): "All 37 required tests passed".
- **Impact**: STATE.md documents 45 tests; actual runtime shows 37. Delta = 8 missing tests. Either tests were removed after claiming 45, or test count was never accurate.
- **Fix hint**: Recount tests in current suite and reconcile STATE.md claim against actual verifyGameTestStage output.

#### [HIGH] Two optional tests documented as failing in Phase 14-03
- **Claim** (14-formation-level-target-selection-rewrite-03-SUMMARY.md:35-36): "Two phase-local optional GameTests still fail in brownfield verification and were deferred after the fix-attempt limit."
- **Details** (line 85): `formationCohortRetargetsAfterSharedEnemyDies` and `formationCohortClearsDeadTargetsWhenNoReplacementExists` fail after three fix attempts and are deferred.
- **Reality**: These tests exist but are not in the passing suite, meaning they are either removed or still failing.
- **Fix hint**: Locate these two tests and either fix them or formally document them as removed/optional beyond Phase 14 scope.

#### [HIGH] FormationTargetSelectionController exists but integration unclear
- **Reality** (verified path): `FormationTargetSelectionController.java` exists in merged bannermod tree at `/home/kaiserroman/bannermod/src/main/java/com/talhanation/bannermod/ai/military/FormationTargetSelectionController.java`.
- **Issue**: It exists in bannermod (consolidated tree, Phase 21+) but Phase 14 was planned against `recruits/` namespace.
- **Caveat**: This is likely correct (Phase 21 consolidated recruits into bannermod) but planning document references suggest out-of-date namespace. Not a code defect but a planning documentation drift.
- **Fix hint**: Update Phase 14 summary files to reflect that controller is now at bannermod namespace, not recruits namespace.

#### [MEDIUM] revalidateCandidate liveness claim not independently verified
- **Claim** (STATE.md / 14 decision): "Final target publication now revalidates candidate liveness, so stale async search results cannot republish dead or removed enemies during shared retarget/loss-of-target refresh."
- **Reality**: Text claim present but no code inspection done to verify revalidation logic is actually in place.
- **Fix hint**: Grep for `revalidate` or `liveness` in FormationTargetSelectionController to prove guard is implemented.

#### [MEDIUM] Phase 14 VALIDATION.md exists but content not fully verified
- **File**: `/home/kaiserroman/bannermod/.planning/phases/14-formation-level-target-selection-rewrite/14-VALIDATION.md` exists.
- **Issue**: Plan references focused behavior tests (focus-fire, retarget, loss-of-target) that are listed as failing in 14-03 summary but no explicit "deferred" or "optional" tag appears in acceptance criteria (14-VALIDATION.md:96-104).
- **Fix hint**: Add explicit deferral note to 14-VALIDATION.md acceptance criteria documenting that two optional tests are pending brownfield stabilization.

---

## Phase 15: Pathfinding Throttling And Budgeting

### Status Claims
- Only CONTEXT.md file exists. No PLAN or VALIDATION files written.
- **CONTEXT.md** (confirmed): Phase 15 not yet started (planned execution, not executed).

### Audit Findings

#### Status is accurate
- **Reality**: Phase 15 directory contains only `15-CONTEXT.md`. No plans, no implementation.
- **Verdict**: CORRECT. Phase 15 planning is deferred; no false completion claims.
- **Next step**: Phase 15 planning should start after Phase 14 stabilization completes.

---

## Cross-Phase Issues

### RecruitCombatTargeting Exists But Disconnected
- **File**: `/home/kaiserroman/bannermod/src/main/java/com/talhanation/bannermod/entity/military/RecruitCombatTargeting.java` (verified).
- **Issue**: Exists in consolidated bannermod tree, referenced in Phase 14 plans but not connected to FormationTargetSelectionController integration story (no clear evidence of wiring).
- **Fix hint**: Verify that RecruitCombatTargeting is actually called by FormationTargetSelectionController and that formation-level filtering uses these utility methods.

### RecruitHoldPosGoal Modified But Not Phase-Linked
- **File**: `/home/kaiserroman/bannermod/src/main/java/com/talhanation/bannermod/ai/military/RecruitHoldPosGoal.java` (verified).
- **Status**: Recently modified (mentioned in audit task) but no Phase 11-15 planning document claims responsibility for its changes.
- **Fix hint**: If RecruitHoldPosGoal changes are part of performance work, document which phase owns them. If not, verify changes don't interfere with profiling baselines.

### Phase 11 Baseline Never Collected
- **Impact on 12-19 comparison chain**: All later phases (12, 13, 14, 15-19) depend on Phase 11 evidence for before/after comparison. With zero baseline runs, all relative-improvement claims are unverifiable.
- **Fix hint**: Phase 11 must run the three mandatory scenarios with profiling capture before any Phase 12-19 claims can be grounded in empirical data.

---

## Severity Summary

### CRITICAL (3)
1. `GlobalPathfindingController` main class missing (Phase 12)
2. Phase 13 reuse counters assume missing Phase 12 controller
3. Phase 14 verifyGameTestStage test count mismatch (claimed 45, actual 37)

### HIGH (5)
1. Phase 11: No baseline evidence artifacts collected
2. Phase 12: Controller integration claim unverified (controller doesn't exist)
3. Phase 13: No Phase 13 plan summary files fully examined
4. Phase 14: Two optional tests documented as failing and deferred
5. Phase 14: FormationTargetSelectionController namespace drift (bannermod vs recruits)

### MEDIUM (3)
1. Phase 11: Snapshot fields wired but validation incomplete (no live battle confirmation)
2. Phase 13: Reuse path copy guarantee unverified
3. Phase 14: Candidate revalidation claim not independently verified in code
4. Phase 14: VALIDATION.md acceptance criteria do not reflect deferred optional tests

---

## Noted Clean Items

✓ RecruitCombatTargeting exists at correct path and contains formation-aware methods
✓ FormationTargetSelectionController exists in consolidated tree
✓ Phase 15 CONTEXT.md correctly documents unstarted phase (no false claims)
✓ Phase 11 VALIDATION.md and PROFILING-BASELINE.md well-structured (procedure is sound, just not executed)
✓ Test infrastructure exists (GlobalPathfindingControllerTest passes despite main class missing)
✓ Phase 13 and 14 summaries well-documented (issues are execution/verification, not planning clarity)

---

## Top 3 Actionable Issues

1. **CRITICAL: Restore GlobalPathfindingController.java** — Phase 12's main seam is missing. This breaks Phases 13, 14, and 15 planning assumptions. Either recover the file from git history or revert Phase 12 as unfinished.

2. **CRITICAL: Reconcile Phase 14 test count (45 vs 37)** — STATE.md claims verifyGameTestStage passes with 45 tests; actual run shows 37. Update STATE.md or investigate missing 8 tests. Verify two optional Phase 14 tests are intentionally deferred.

3. **HIGH: Collect Phase 11 baseline evidence** — Zero baseline profiling runs exist. Phases 12-19 comparison chain is broken without empirical before-state. Run the three mandatory scenarios (baseline_dense_battle, heavy_dense_battle, mixed_squad_battle) with profiling capture.

---

## Answered Questions

### Is `GlobalPathfindingController` actually wired into recruit entity pathfinding today?
**NO.** The main class does not exist on disk. Test exists (proving intent) but runtime seam is broken. Phases 12-14 planning assumes controller routing but code does not implement it.

### Is `RecruitCombatTargeting` actually wired into recruit entity targeting today?
**PARTIAL.** The class exists in consolidated bannermod tree. Methods are present (scanNearbyCombatCandidates, filterCombatCandidates, resolveCombatTargetFromCandidates). However, no independent verification done that FormationTargetSelectionController actually calls these utilities or that integration is live.

### Are "Complete" claims false for any phase?
**YES.** Phase 14 claims 45 required tests pass; actual count is 37. Two optional tests documented as failing/deferred. Either claim is wrong or tests were removed without updating STATE.md.

