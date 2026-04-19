# Phase Audit 1-24 — Truth-Fix Summary

Scope: planning-docs only. No code, gradle, settings, recruits/, or workers/ mutations.
Run date: 2026-04-19. Base branch: master, HEAD: f459ae6 (with existing in-flight planning edits preserved).

## A-item Outcomes

### A1 — Phase 15/16 ROADMAP status realignment: APPLIED
- Wrote retroactive `15-pathfinding-throttling-and-budgeting-01-SUMMARY.md` citing `GlobalPathfindingController` budget accounting (`configureBudgetForTests`, per-tick budget counters, reuseAttempts/reuseHits co-location). Marked retroactive.
- Wrote retroactive `16-async-pathfinding-reliability-fixes-01-SUMMARY.md` citing the active `AsyncPathProcessor.java`/`PathProcessingRuntime.java` async reliability surface. Marked retroactive.
- No 15-02 / 16-02 summaries written (validation-pass levels intentionally left absent per spec).
- ROADMAP 15/16 status left as "Complete" on the expectation the parallel pathfinding restore will land. If it does not, the caller should downgrade 12-16 to "Partial — controller restored, counters/validation captured on follow-up."

### A2 — Phase 17 retroactive summary: APPLIED
- Confirmed `src/main/java/com/talhanation/bannermod/ai/military/RecruitAiLodPolicy.java` exists and is wired from `src/main/java/com/talhanation/bannermod/entity/military/RecruitRuntimeLoop.java` (`evaluateTargetSearchLod(...)`, `recordLodTier(...)`).
- Wrote retroactive `17-ai-lod-and-tick-shedding-01-SUMMARY.md` citing the active class and hook sites. Marked retroactive.

### A3 — Phase 18 retroactive DECISION (drop): APPLIED
- Confirmed no flow-field class under `src/main/java/com/talhanation/bannermod/ai/` (glob `**/flow*` returns none).
- Wrote `18-DECISION.md` with a `drop` verdict consistent with the ROADMAP narrative (51 attempts, zero hits) and the absent runtime. Marked retroactive.

### A4 — Phase 19 status rewrite (no evidence): APPLIED
- Confirmed `.planning/phases/19-large-battle-performance-validation/evidence/` does not exist in the active tree.
- Wrote `19-VALIDATION.md` describing intended scope, what is in place (correctness gate, seams referenced), what is missing (no empirical capture, no RESULTS, no evidence bundles), status ("Plans declared, evidence deferred"), and the deferred follow-up slice.
- Updated ROADMAP Phase 19 Status block from the old "Complete … evidence now lives under …/evidence/" narrative to a truthful "Partial — validation doc written; empirical profiling evidence deferred; no `evidence/` bundles captured".

### A5 — Phase 14 plan count fix: APPLIED (restored, not downgraded)
- `14-04-PLAN.md` and `14-formation-level-target-selection-rewrite-04-SUMMARY.md` were found in git history under stash commit `29e5914` (`gsd-wave-hook-stash`).
- Both files restored to the active phase directory via `git show 29e5914:<path>`.
- ROADMAP Phase 14 already claims 4/4; no ROADMAP edit needed because the on-disk state now matches the claim.

### A6 — STATE.md stale cleanups: APPLIED
- Session stop line rewritten: `BannerModSettlementService` is verified as a `final` utility class with a private constructor and no `new BannerModSettlementService(...)` callers. The old "pre-existing constructor mismatch in compileJava" excuse is removed; compact-Phase-24 validation is noted as open on its own merits, not blocked by compile debt.
- `BannerModConfigFiles.java` confirmed present at `src/main/java/com/talhanation/bannermod/config/BannerModConfigFiles.java`, so the existing decision line stays as-is per spec.

### A7 — Stale build.gradle reference in Phase 01 verification doc: APPLIED
- Added a one-line post-Phase-21 note (dated 2026-04-15) at the top of `01-VERIFICATION.md`'s Goal Achievement section pointing readers to the current single-tree `build.gradle` layout, while preserving the historical Phase 1 citations of `build.gradle:48-68` wiring `recruits/src/main/java` and `workers/src/main/java` verbatim for audit continuity.

### A8 — ROADMAP test-count disclaimers: APPLIED
- Phase 07, Phase 08, Phase 10, and Phase 14 status blocks now trail each historical test-count (32/36/45/45) with a qualifier such as "(historical snapshot at phase close; current suite is 37 per STATE.md)" rather than rewriting the historical figure. Kept inline and compact.

## Files Created

- `/home/kaiserroman/bannermod/.planning/phases/15-pathfinding-throttling-and-budgeting/15-pathfinding-throttling-and-budgeting-01-SUMMARY.md`
- `/home/kaiserroman/bannermod/.planning/phases/16-async-pathfinding-reliability-fixes/16-async-pathfinding-reliability-fixes-01-SUMMARY.md`
- `/home/kaiserroman/bannermod/.planning/phases/17-ai-lod-and-tick-shedding/17-ai-lod-and-tick-shedding-01-SUMMARY.md`
- `/home/kaiserroman/bannermod/.planning/phases/18-optional-flow-field-navigation-evaluation/18-DECISION.md`
- `/home/kaiserroman/bannermod/.planning/phases/19-large-battle-performance-validation/19-VALIDATION.md`
- `/home/kaiserroman/bannermod/.planning/phases/14-formation-level-target-selection-rewrite/14-04-PLAN.md` (restored from git stash `29e5914`)
- `/home/kaiserroman/bannermod/.planning/phases/14-formation-level-target-selection-rewrite/14-formation-level-target-selection-rewrite-04-SUMMARY.md` (restored from git stash `29e5914`)
- `/home/kaiserroman/bannermod/.analysis/phase-audit-1-24/TRUTHFIX-SUMMARY.md` (this file)

## Files Edited

- `/home/kaiserroman/bannermod/.planning/ROADMAP.md`
  - Phase 07 Status: appended historical-snapshot disclaimer for the 32-test count.
  - Phase 08 Status: appended historical-snapshot disclaimer for the 36-test count.
  - Phase 10 Status: appended historical-snapshot disclaimer for the 45-test count.
  - Phase 14 Status: appended historical-snapshot disclaimer for the 45-test count.
  - Phase 19 Status: rewrote from "Complete … evidence under …/evidence/, closeout in 19-RESULTS.md" to truthful "Partial — validation doc written; empirical profiling evidence deferred".
- `/home/kaiserroman/bannermod/.planning/STATE.md`
  - Session "Stopped at" line rewritten to reflect that `BannerModSettlementService` has no constructor mismatch in the active tree.
- `/home/kaiserroman/bannermod/.planning/phases/01-workspace-bootstrap/01-VERIFICATION.md`
  - Added post-Phase-21 disclaimer block at the top of Goal Achievement pointing at the current single-tree `build.gradle` layout; historical `48-68 / recruits/src/main/java / workers/src/main/java` citations retained as-is.

## Consciously Skipped

- **15-02 and 16-02 retroactive summaries**: skipped per A1 spec — those levels correspond to validation passes that never happened. Directories intentionally contain only `15-CONTEXT.md` + `15-01-SUMMARY.md` and `16-CONTEXT.md` + `16-01-SUMMARY.md`.
- **ROADMAP Phase 15/16 demotion from 2/2 to 1/2**: skipped — spec says only to demote if the parallel worktree pathfinding restore does not land. That gate is owned by the caller, not by this run.
- **STATE.md BannerModConfigFiles rewrite**: skipped — file exists on disk, spec says leave the line as-is in that case.
- **ROADMAP Phase 14 "4 plans" downgrade to "3 plans"**: skipped — A5 is resolved by restoration instead, keeping ROADMAP's existing 4/4 claim truthful.
- **No git add / commit / push**: per spec, the caller will review and commit the aggregated planning-doc diff.

## Notes For The Caller

- The parallel pathfinding restore worktree was not inspected or interacted with; its landing or non-landing does not affect any file written in this run.
- All new files are marked retroactive and cite 2026-04-19 so they are easy to grep and to promote or retire later.
- Phase 21's `21-VERIFICATION.md` and `21-UAT.md` showed as modified in git status at the start of this run; those were left untouched — they are owned by the Phase 21 closeout track, not by this audit fix.
