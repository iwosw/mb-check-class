# Phases 1-24 Audit Triage

Consolidated from 5 parallel read-only audit agents + targeted verification by orchestrator.
Individual reports: `phases-01-05.md`, `phases-06-10.md`, `phases-11-15.md`, `phases-16-20.md`, `phases-21-24.md`.

## TL;DR

- **Phases 1-10, 20, 21-24**: code and planning are consistent. Only cosmetic doc drift.
- **Phases 11-19**: serious planning/code mismatch. Several phase planning dirs are near-empty despite ROADMAP claiming "Complete N/N plans." A key seam (`GlobalPathfindingController`) the Phase 12/13/15/16 claims build on is NOT in the active `src/**` tree. Active pathfinding uses a different architecture (`AsyncPathProcessor`, `PathProcessingRuntime`) that does not expose the documented controller+counter API.

## Severity Key

- **CRITICAL**: runtime regression, silent correctness break, false security/build claim.
- **HIGH**: named class/artifact/test explicitly claimed exists but is absent; a "Complete" status that code contradicts.
- **MEDIUM**: stale doc text, drifted numbers/paths, missing paper trail with no runtime impact.
- **LOW**: wording.

## Confirmed Findings By Severity

### CRITICAL

_None confirmed at runtime level._ Agent 3 flagged Phase 12 as CRITICAL and agent 4 flagged Phase 19 missing artifacts. On re-check both are really HIGH (planning-vs-code truth drift, not live runtime regression — current runtime uses `AsyncPathProcessor`/`PathProcessingRuntime` architecture that works fine, just does not match the controller-seam plan).

### HIGH (actionable truth-drift)

#### H1. `GlobalPathfindingController` not in active tree (Phase 12 linchpin)
- **Claim**: `.planning/phases/12-.../12-global-pathfinding-control-01-SUMMARY.md:20` states class created at `recruits/src/main/java/com/talhanation/recruits/pathfinding/GlobalPathfindingController.java`. ROADMAP and STATE assert recruit path issuance routes through `GlobalPathfindingController`.
- **Reality**: `grep -rn GlobalPathfindingController /home/kaiserroman/bannermod/src` → 0 hits. Active `AsyncPathNavigation.java` (src/main/java/com/talhanation/bannermod/ai/pathfinding/) calls `this.pathFinder.findPath` directly, no controller indirection. Legacy `recruits/src/main/java/com/talhanation/recruits/pathfinding/` directory does not exist; only a zombie test file `recruits/src/test/java/.../GlobalPathfindingControllerTest.java` remains, referencing a class that cannot compile.
- **Root cause**: Phase 21 consolidation did not migrate the controller class into `bannermod.*`; active runtime was reshaped around `AsyncPathProcessor`/`PathProcessingRuntime` instead.
- **Blast radius**: Phase 13 (path reuse counters), Phase 15 (throttling/budget), Phase 16 (async hardening callback counters) all cite `GlobalPathfindingController.profilingSnapshot()`. None of those counters actually exist.
- **Recommended fix (truth-first)**: Rewrite Phase 12/13/15/16 status and summaries to truthfully describe the post-Phase-21 architecture (`AsyncPathProcessor` + `PathProcessingRuntime`) and mark the controller-seam narrative as superseded. Drop or dewrite inaccurate counter-name citations. Keep a small follow-up backlog item for "restore dense-battle profiling counters under the new runtime if needed."
- **Alternative (heavier)**: Reintroduce a thin `GlobalPathfindingController` wrapper in active tree, route `AsyncPathNavigation.createPath(...)` through it, add counters — then port the zombie legacy test. Only makes sense if the profiling work is truly needed soon.

#### H2. Phase 15/16/17/18/19 planning dirs stripped
- **Reality** (ls + verified):
  - `.planning/phases/15-pathfinding-throttling-and-budgeting/` → only `15-CONTEXT.md` (1577 bytes)
  - `.planning/phases/16-async-pathfinding-reliability-fixes/` → only `16-CONTEXT.md`
  - `.planning/phases/17-ai-lod-and-tick-shedding/` → only `17-CONTEXT.md`
  - `.planning/phases/18-optional-flow-field-navigation-evaluation/` → only `18-CONTEXT.md`
  - `.planning/phases/19-large-battle-performance-validation/` → only `19-CONTEXT.md`
- **Claim (ROADMAP.md)**: Each of these is listed "Complete (2/2 plans complete)" (15, 16, 17, 19) or "Complete (4/4 plans complete)" (18). ROADMAP references `18-DECISION.md`, `19-VALIDATION.md`, `19-RESULTS.md`, and `19-.../evidence/` — none exist.
- **Code crosscheck**:
  - Phase 17 `RecruitAiLodPolicy` exists at `src/main/java/com/talhanation/bannermod/ai/military/RecruitAiLodPolicy.java` → CODE landed, planning paper-trail lost.
  - Phase 18 runtime: no flow-field code in `src/main/java/**/navigation/*` → drop decision is consistent with code but not documented.
  - Phase 15/16/19 deliverables: no counters, no validation docs, no evidence — only `AsyncPathProcessor` general runtime.
- **Recommended fix**: For each phase:
  - If code landed (Phase 17): write retroactive `17-SUMMARY.md` citing `RecruitAiLodPolicy.java` and current lodging in `AbstractRecruitEntity`/`RecruitCombatTargeting`. Keep "Complete" status.
  - If decision-only (Phase 18): write retroactive `18-DECISION.md` stating `drop` with reference to absent runtime flow-field code.
  - If claim unbacked (Phase 15/16/19): downgrade ROADMAP status from `Complete` to `Partial / Planning artifacts lost` or `Superseded by post-Phase-21 async path runtime`, whichever matches reality.

#### H3. Phase 14 missing 14-04-PLAN.md
- **Claim (ROADMAP.md:250-257)**: "Plans: 4 plans" and lists 14-04-PLAN.md as `[x]` complete.
- **Reality**: Only `14-01-PLAN.md`, `14-02-PLAN.md`, `14-03-PLAN.md` exist under `.planning/phases/14-formation-level-target-selection-rewrite/`.
- **Fix**: Either restore `14-04-PLAN.md` from git history or rewrite ROADMAP to 3/3 plans.

#### H4. Phase 11 baseline evidence never captured
- **Claim** (`11-PROFILING-BASELINE.md:99-114`): Required evidence under `.planning/phases/11-.../evidence/<capture-id>/`.
- **Reality**: No `evidence/` dir exists.
- **Fix**: Either mark Phase 11 baseline as "Plan documented, evidence capture deferred" in ROADMAP, or actually run the 3 mandatory scenarios and capture. For now, truth-fix is faster.

### MEDIUM (stale doc text)

#### M1. Phase 01-01 VERIFICATION references old `build.gradle:48-68` with legacy source paths
- **Claim** (`01-01-VERIFICATION.md`): `build.gradle:48-68` wires `recruits/src/main/java` and `workers/src/main/java`.
- **Reality**: Current `build.gradle` sourceSets only reference outer `src/{main,test,gametest}`.
- **Fix**: One-line edit in phase 01 verification doc, or leave with a "post-Phase-21" note.

#### M2. ROADMAP phase snapshots cite stale `verifyGameTestStage` counts
- Phase 07 mentions 32, Phase 08 → 36, Phase 10/14 → 45. STATE.md admits current is 37.
- **Fix**: These are historical; add a one-line "(snapshot at phase close; current count differs)" disclaimer or leave with a clear STATE.md override (already present).

#### M3. STATE.md Session section claims "focused treasury/governance JUnit execution blocked" by `BannerModSettlementService` constructor drift
- **Reality (agent 5 check)**: `BannerModSettlementService` looks clean today. Likely stale from an earlier moment in session.
- **Fix**: Rewrite STATE.md Session section to reflect the fiscal-rollup closeout being green on the latest targeted tests.

#### M4. STATE.md `BannerModConfigFiles` mentioned as target-taxonomy class
- **Claim**: line 71 decision says "`BannerModConfigFiles` documents the target config taxonomy."
- **Reality**: agent 1 did not find such class; agent 5 confirmed three configs actually registered with per-subsystem filenames. If `BannerModConfigFiles` does not exist, rewrite the decision to describe the live `BannerModMain.registerConfig(...)` triplet instead.

### LOW

- Phase 21-10 references `RecruitsClientConfig.RecruitsLookLikeVillagers.get()` as legacy; renamed guard helper now lives on abstract entity. Wording-only.
- Phase 22 citizen-seam claims two "live-path" conversions (one recruit, one worker). Agent 5 verified citizen bridge exists; precise live-path method-level citations in planning are unbacked but functionally fine.

## Clean Items Worth Preserving

- Phase 1-5 workspace/design/stabilization: code matches plan.
- Phase 6-10 GameTest coverage: every claimed class exists at claimed path. 55 `@GameTest` methods counted in `src/gametest/java/com/talhanation/bannermod/**`.
- Phase 9 `BannerModSettlementBinding` with `FRIENDLY_CLAIM`/`HOSTILE_CLAIM`/`UNCLAIMED`/`DEGRADED_MISMATCH` vocabulary — verified at code.
- Phase 20 has full artifact set: `20-RUNTIME-AUDIT.md`, `20-OWNERSHIP-MATRIX.md`, `20-TARGET-ARCHITECTURE.md`.
- Phase 21 event handler registration: all 7 server + 3 client handlers wired in `BannerModMain`.
- Phase 21 config registration: all three filenames (`bannermod-recruits-client.toml`, `bannermod-recruits-server.toml`, `bannermod-workers-server.toml`) present.
- Phase 21 `BannerModNetworkBootstrap`: MILITARY_MESSAGES + CIVILIAN_MESSAGES arrays with `workerPacketOffset() == MILITARY_MESSAGES.length` (104).
- Phase 21 `build.gradle`: sourceSets are root-only, no legacy paths.
- Phase 23 fiscal chain: heartbeat → treasury ledger → snapshot rollup → client message → governor screen — all links present.
- Phase 23-07 early-tick config guard implemented in `AbstractRecruitEntity`/`AbstractWorkerEntity`.
- Phase 24 courier/logistics runtime: `BannerModLogisticsRuntime`, `BannerModLogisticsService`, `BannerModSupplyStatus` all in shared package.

## Proposed Fix Plan (subject to user approval)

Each fix = one worktree, one subagent, reviewed before merge.

### Wave A: Truth-fixing (low-risk planning-only edits)

Scope: rewrite planning docs to match reality. Zero code impact.

1. **A1 — Phase 12/13/15/16 narrative realignment**
   - Rewrite ROADMAP phase 12/13/15/16 status from "Complete" to "Superseded by post-Phase-21 async path runtime" OR write retroactive summaries citing `AsyncPathProcessor`/`PathProcessingRuntime` as the delivered architecture.
   - Remove/rewrite STATE.md decisions that cite `GlobalPathfindingController`.
   - Mark zombie test `recruits/src/test/java/.../GlobalPathfindingControllerTest.java` as archive-only (or delete — but that touches legacy tree, user may prefer to keep archive intact).

2. **A2 — Phase 17/18/19 retroactive artifacts**
   - Write `17-SUMMARY.md` citing live `RecruitAiLodPolicy` wiring.
   - Write `18-DECISION.md` with the `drop` verdict confirmed by absence of flow-field runtime.
   - For Phase 19: either downgrade ROADMAP status to "Plans not executed" or write `19-VALIDATION.md` + `19-RESULTS.md` stubs that explicitly acknowledge missing evidence (user preference).

3. **A3 — Phase 14 cleanup**
   - Restore `14-04-PLAN.md` from git history if it exists, or rewrite ROADMAP Phase 14 plan count to 3/3.

4. **A4 — Stale doc cleanups**
   - STATE.md Session section: drop stale "blocked by constructor mismatch" line.
   - STATE.md decision on `BannerModConfigFiles`: replace with actual `BannerModMain.registerConfig` triplet.
   - ROADMAP test-count snapshots: add `(snapshot at close; current count differs)` disclaimer.
   - Phase 01-01 verification: scrub legacy `build.gradle:48-68` line.

### Wave B: Code restoration (optional, heavier)

Only if user explicitly wants Phase 12 profiling counters back in the runtime. Single worktree, single fix.

5. **B1 — Restore `GlobalPathfindingController` seam** (if wanted)
   - Create `src/main/java/com/talhanation/bannermod/ai/pathfinding/GlobalPathfindingController.java` (pass-through + counter snapshot).
   - Route `AsyncPathNavigation.createPath(...)` through it.
   - Port legacy test into `src/test/java/com/talhanation/bannermod/ai/pathfinding/GlobalPathfindingControllerTest.java`.
   - Compile gate: `./gradlew compileJava && ./gradlew test --tests GlobalPathfindingControllerTest`.

### Wave C: Run the missing Phase 11 / Phase 19 profiling (most expensive)

6. **C1 — Capture Phase 11 baseline and Phase 19 optimized runs** (optional)
   - Follow `11-PROFILING-BASELINE.md` procedure.
   - Only if user wants real before/after data.

## Recommended immediate action

Authorize **Wave A (A1-A4 truth-fix)**. That is cheap, purely planning, and closes the biggest truth gap. Each of A1-A4 is independent and can run in its own worktree in parallel.

Defer Wave B and C until user decides whether the profiling seam is worth restoring. Current runtime works without it — the consolidation effectively replaced the controller-snapshot story with `AsyncPathProcessor`/`PathProcessingRuntime`, which is a valid (if different) architecture.
