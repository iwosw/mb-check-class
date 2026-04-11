# Phase 08 Context

## Phase

- Number: 08
- Name: Multiplayer Authority Conflict Validation

## Goal

- Validate the shared BannerMod authority contract under real multiplayer-style contention with multiple distinct players, same-team cooperation, and outsider denial.

## Why This Phase Exists

- Dedicated-server edge validation must be followed by contention validation so authority rules stay truthful outside single-owner slices.
- BannerMod already has a shared owner, same-team, admin, and forbidden vocabulary, but current root validation only lightly exercises outsider denial.
- Settlement and military interactions should stay consistent when two players touch the same live state.

## Scope

- Add focused root validation for owner versus outsider and same-team versus outsider interaction paths.
- Cover recruit control, worker control, and work-area authoring seams without broad gameplay rewrites.
- Keep scenarios deterministic so later phases can extend them safely.

## Planned Execution Slices

- Plan 08-01: Validate contested multiplayer authority on shared recruit, worker, and work-area interactions with distinct owner and outsider players.
- Plan 08-02: Validate same-team multiplayer cooperation paths without reopening outsider settlement or control access.

## Constraints

- Preserve the current shared authority vocabulary.
- Prefer additive GameTests or narrow regressions.
- Do not broaden permissions just to simplify multiplayer setup.

## Evidence

- `.planning/ROADMAP.md`
- `.planning/STATE.md`
- `.planning/CODEBASE.md`
- `.planning/VERIFICATION.md`
- `.planning/codebase/INTEGRATED_SYSTEM_ARCHITECTURE.md`
