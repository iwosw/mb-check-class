---
phase: 06-full-surface-verification-and-safe-degradation
plan: 01
subsystem: testing
tags: [verification, documentation, gradle, gametest, junit]
requires:
  - phase: 01-build-reproducibility-baseline
    provides: canonical build and check lifecycle commands
  - phase: 02-layered-test-harness-foundations
    provides: split JVM and GameTest verification layers
  - phase: 03-battle-and-formation-regression-lockdown
    provides: battle and formation automation evidence
  - phase: 04-command-and-ai-state-stabilization
    provides: command, AI, and deferred battle gap attribution
  - phase: 05-persistence-and-multiplayer-sync-hardening
    provides: persistence and sync verification evidence
provides:
  - repo-level verification matrix with deep, smoke/manual, and deferred coverage categories
  - canonical BUILDING.md link from maintainer workflow to verification interpretation
affects: [phase-06-verification, maintainer-workflow, documentation]
tech-stack:
  added: []
  patterns: [repo-level verification matrix tied to Gradle lifecycle commands, explicit accepted-gap attribution in docs]
key-files:
  created: [VERIFICATION_MATRIX.md]
  modified: [BUILDING.md]
key-decisions:
  - "Use one repo-level matrix document to classify subsystem coverage as deep automated, smoke/manual, or accepted deferred."
  - "Keep accepted Phase 3 battle failures visible in the matrix so canonical verification output is interpreted consistently."
patterns-established:
  - "Documentation pattern: point BUILDING.md at a dedicated verification matrix instead of duplicating subsystem-by-subsystem detail."
  - "Verification pattern: cite concrete JVM tests, GameTests, or explicit docs for every coverage claim."
requirements-completed: [TEST-04, STAB-01]
duration: 2min
completed: 2026-04-08
---

# Phase 6 Plan 01: Publish the repo-level verification matrix and wire it into the canonical build docs Summary

**Repo-level verification matrix tying Gradle `check --continue` to deep coverage, smoke/manual surfaces, and accepted battle-gap attribution**

## Performance

- **Duration:** 2 min
- **Started:** 2026-04-08T00:29:46Z
- **Completed:** 2026-04-08T00:31:55Z
- **Tasks:** 2
- **Files modified:** 2

## Accomplishments
- Added `VERIFICATION_MATRIX.md` as the single repo document explaining subsystem verification depth.
- Classified major mod surfaces into deep automated coverage, smoke/manual coverage, and accepted deferred gaps with concrete evidence.
- Linked `BUILDING.md` to the matrix without changing the canonical `./gradlew build` and `./gradlew check --continue` workflow.

## Task Commits

Each task was committed atomically:

1. **Task 1: Author the full-surface verification matrix** - `7d9d9af7` (chore)
2. **Task 2: Wire the matrix into the canonical maintainer workflow** - `225daa89` (chore)

**Plan metadata:** `f3465972` (docs)

## Files Created/Modified
- `VERIFICATION_MATRIX.md` - Maps subsystem verification depth, focused commands, and accepted external gaps.
- `BUILDING.md` - Routes maintainers from the canonical verification command to the new matrix.

## Decisions Made
- Used a dedicated matrix document instead of expanding `BUILDING.md` into a second subsystem inventory.
- Kept the three accepted Phase 3 battle failures explicit in the matrix so later `check --continue` output stays attributable.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
- The environment exposes `python3` rather than `python`, so automated verification snippets were run with `python3` while preserving the plan's assertion logic.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- The repo now has a truthful full-surface verification map for maintainers.
- Phase 06-02 can add safe-degradation regression coverage for compat and async-path fallback surfaces called out as smoke/manual today.

## Self-Check: PASSED

- Found `.planning/phases/06-full-surface-verification-and-safe-degradation/06-01-SUMMARY.md`.
- Verified task commits `7d9d9af7` and `225daa89` exist in git history.

---
*Phase: 06-full-surface-verification-and-safe-degradation*
*Completed: 2026-04-08*
