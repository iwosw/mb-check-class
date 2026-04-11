# Phase 11 Context

## Phase

- Number: 11
- Name: Large-Battle AI Profiling Baseline

## Goal

- Establish a reproducible large-battle AI and pathfinding profiling baseline before runtime optimization work starts.

## Why This Phase Exists

- The merged workspace already carries `PERF-01` as future work, but the active planning tree does not yet turn that into executable, evidence-first slices.
- Later optimization work needs shared benchmark scenarios and counters or it will drift into anecdotal tuning.
- The proposal explicitly starts with structural pathfinding control work, so a baseline must exist first to judge whether those changes help.

## Scope

- Define the representative large-battle scenarios, counters, and profiling outputs used by the rest of the performance roadmap.
- Add only the smallest instrumentation or harness support needed to collect repeatable baseline evidence.
- Keep the phase measurement-first rather than changing AI behavior.

## Planned Execution Slices

- Plan 11-01: Define the large-battle profiling scenarios, counters, and evidence format for AI and pathfinding work.
- Plan 11-02: Add the smallest instrumentation or harness seams needed to capture baseline measurements without changing AI behavior.

## Constraints

- Do not bundle optimization work into the baseline phase.
- Prefer measurements that can be rerun at the end of the roadmap without reinterpretation.
- Keep any instrumentation low-risk and easy to remove or disable.

## Evidence

- `.planning/ROADMAP.md`
- `.planning/STATE.md`
- `.planning/VERIFICATION.md`
- `MERGE_NOTES.md`
