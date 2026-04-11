# Phase 01: Workspace Bootstrap - Research

**Date:** 2026-04-11
**Status:** Complete

## Question

What do we need to know to plan the workspace bootstrap phase well without reopening already-settled merge decisions?

## Sources Read

- `.planning/phases/01-workspace-bootstrap/01-CONTEXT.md`
- `.planning/PROJECT.md`
- `.planning/REQUIREMENTS.md`
- `.planning/ROADMAP.md`
- `.planning/STATE.md`
- `.planning/CODEBASE.md`
- `.planning/VERIFICATION.md`
- `.planning/codebase/STACK.md`
- `.planning/codebase/STRUCTURE.md`
- `.planning/codebase/ARCHITECTURE.md`
- `.planning/codebase/CONVENTIONS.md`
- `MERGE_NOTES.md`
- `MERGE_PLAN.md`
- `build.gradle`
- `settings.gradle`

## Findings

### 1. The bootstrap foundation already has strong concrete targets

- The single root build entrypoint already lives in `build.gradle` and `settings.gradle`.
- `build.gradle` already composes `src/main/java`, `recruits/src/main/java`, and `workers/src/main/java` into one root runtime, and the root test/gametest source sets are also merged.
- `settings.gradle` already sets `rootProject.name = 'bannermod'`, matching the active merged runtime identity.

**Planning implication:** Phase 1 should be planned as a hardening/documentation phase, not as greenfield infrastructure work.

### 2. User decisions lock a layered source-of-truth model

- Per D-01 and D-02, downstream work must read the active root planning docs first, then merge-readiness docs.
- Per D-03 and D-04, root code and active root docs beat legacy plans, and `MERGE_PLAN.md` is historical rationale only.
- Per D-08 through D-10, legacy planning archives are background only unless active root docs explicitly point to them.

**Planning implication:** Tasks must reinforce the read order and conflict policy in the active docs instead of reviving legacy trees as peers.

### 3. Verification for this phase should stay root-first and fast

- D-05 sets the current default baseline to `./gradlew compileJava`, `./gradlew processResources`, and `./gradlew test`.
- D-06 makes `check` the future stricter baseline only after root GameTests become meaningful.
- D-07 says `runGameTestServer` is additive when gameplay/runtime behavior changes; bootstrap work is primarily build/docs work.

**Planning implication:** Every executable plan in this phase should verify with the root compile/resources/test trio, not require GameTest by default.

### 4. The biggest risk is documentation drift, not missing code hooks

- `build.gradle`, `settings.gradle`, `.planning/CODEBASE.md`, `.planning/VERIFICATION.md`, and `MERGE_NOTES.md` already express most of the intended bootstrap truths.
- `.planning/codebase/STRUCTURE.md` still frames root `src/` as mostly merge support, which is more conservative than D-12 through D-15.
- `ROADMAP.md` contains the phase bullets but does not yet contain generated plan counts/listing for Phase 1.

**Planning implication:** Phase 1 plans should include explicit doc alignment/hardening work so active root docs describe the target root-owned architecture and current verification truth.

## Recommended Plan Shape

### Plan A — Root entrypoint and workspace topology

Focus on the files that prove there is one active build/workspace root:

- `build.gradle`
- `settings.gradle`
- `.planning/PROJECT.md`
- `.planning/CODEBASE.md`

Expected outcome:

- One root Gradle identity and merged source-set entrypoint remain explicit.
- Active docs clearly state that root `.planning/` is the only live planning context and legacy trees are archives.

### Plan B — Verification + merge-truth docs

Focus on the files that define how follow-up agents reason about the merged repo:

- `.planning/VERIFICATION.md`
- `MERGE_NOTES.md`
- `.planning/ROADMAP.md`
- `.planning/STATE.md`

Expected outcome:

- Validation baseline is explicit and phase-appropriate.
- Merge conflicts and current bootstrap decisions are documented in the active root notes.
- Roadmap is updated with concrete plan entries.

## Do / Don’t

### Do

- Treat Phase 1 as establishing the active root contract for the rest of the merge.
- Reference D-01 through D-05, D-08 through D-15 directly in plan actions.
- Keep verification commands under 60 seconds when possible, using root Gradle tasks.

### Don’t

- Don’t plan gameplay expansion, packet/save compatibility redesign, or worker namespace migrations here; D-11 defers that to later phases.
- Don’t treat `.planning_legacy_recruits/` or `.planning_legacy_workers/` as active requirement sources.
- Don’t require `./gradlew check` as the default verification baseline yet.

## Validation Architecture

### Quick feedback loop

- `./gradlew compileJava`
- `./gradlew processResources`

### Full phase verification loop

- `./gradlew test`

### Why this is sufficient for Phase 1

- The phase is about root workspace/build/docs truth, not new gameplay loops.
- The root build already exposes these three commands as the currently recommended baseline.
- `runGameTestServer` remains optional unless a task changes runtime behavior, which the bootstrap plans should avoid.

## Planning Constraints To Carry Forward

- Use the active root docs as canonical references in every plan context block.
- Prefer two parallel plans with non-overlapping file ownership.
- Keep tasks concrete enough that an executor can update docs/build files without re-discovering the repository.
