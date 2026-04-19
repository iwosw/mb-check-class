---
phase: 19-large-battle-performance-validation
status: plans-declared-evidence-deferred
reconstructed: 2026-04-19
---

# Phase 19 Validation

> Retroactive reconstruction 2026-04-19: written after planning-dir audit surfaced that the Phase 19 directory only carried `19-CONTEXT.md` and no validation doc, despite ROADMAP previously claiming a completed closeout with evidence bundles. No `.planning/phases/19-large-battle-performance-validation/evidence/` directory exists in the active tree, so this document replaces any prior "Complete" narrative with a truthful status.

## Intended Scope

Phase 19 was intended to close the compact performance roadmap with comparable before/after large-battle profiling evidence against the Phase 11 baseline:

- Re-run the agreed Phase 11 dense-battle and mixed-squad scenarios against the optimized runtime (Phases 12–17 combined).
- Capture controller-aware snapshots including request counters, reuse hits/misses, per-tick budget accounting, async delivered/dropped counters, and LOD tier counters.
- Publish the before/after analysis, remaining hotspots, and the recommended next performance backlog.

## What Is Actually In Place Today

- **Correctness gate:** `verifyGameTestStage` continues to run the retained dense-battle and mixed-squad GameTests that publish controller, reuse, budget, async, and LOD counters in their profiling snapshots. This remains the live correctness-and-observability gate for Phases 12–17.
- **Runtime seams referenced by the closeout:** `GlobalPathfindingController` (request/reuse/budget counters), `AsyncPathProcessor` + `PathProcessingRuntime` (delivered/dropped async accounting), `RecruitAiLodPolicy` (LOD tier counters).
- **Retroactive Phase 15/16/17 summaries** were reconstructed on 2026-04-19 from the live tree and are consistent with the counters the Phase 19 closeout would consume.

## What Is Missing

- **No empirical profiling capture has been executed or published.** No `evidence/` directory exists under `.planning/phases/19-large-battle-performance-validation/`, no before/after bundles have been archived, and no `19-RESULTS.md` artifact is present in the active planning tree despite earlier ROADMAP text implying one.
- No raw Phase 11 baseline bundles have been located alongside optimized-runtime bundles for the side-by-side comparison the original closeout procedure required.
- No external profiler (JFR / async-profiler / etc.) output has been captured against the optimized runtime for residual-hotspot triage.

## Status

**Plans declared, evidence deferred.** The Phase 19 procedure described above (re-run Phase 11 scenarios, capture controller-aware snapshots, publish before/after comparison and residual-hotspot backlog) was never executed end-to-end. `verifyGameTestStage` remains the correctness gate, but no before/after performance evidence has been captured as of 2026-04-19.

Phase 19 should be treated as `Plans declared, evidence deferred` until a follow-up slice runs the capture procedure below. Do not mark Phase 19 complete in STATE/ROADMAP on the basis of paper docs alone.

## Deferred Follow-Up Slice

A future Phase 19 closeout slice must, at minimum:

1. Re-run the dense-battle and mixed-squad retained GameTest scenarios against the current HEAD optimized runtime, capturing the full controller / reuse / budget / async / LOD counter snapshot.
2. Re-run (or locate archived copies of) the Phase 11 baseline bundles to allow side-by-side comparison under the same scenario ids.
3. Archive both sets under `.planning/phases/19-large-battle-performance-validation/evidence/<scenario>/<baseline|optimized>/` with stable filenames that match the Phase 11 vocabulary.
4. Publish `19-RESULTS.md` with the before/after table, residual hotspots, and recommended next performance backlog.
5. Only then update ROADMAP Phase 19 status from `Partial — validation doc written; empirical profiling evidence deferred` to `Complete`.

## Caveats

- Any claim that Phase 19 is "Complete" predating 2026-04-19 referred only to the retained GameTest correctness gate being green, not to empirical before/after performance evidence.
- The Phase 18 `drop` decision for flow-field navigation stands independently and does not depend on Phase 19 closeout.

---
*Phase: 19-large-battle-performance-validation*
*Retroactive validation doc: 2026-04-19*
