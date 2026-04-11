# Phase 10 Context

## Phase

- Number: 10
- Name: Settlement-Faction Enforcement Validation

## Goal

- Validate that the explicit settlement-faction contract behaves correctly in live BannerMod runtime checks and GameTests.

## Why This Phase Exists

- Contract work alone is not enough; later gameplay slices need proof that friendly binding, hostile denial, and claim-loss degradation are enforced in runtime behavior.
- The integrated architecture already states that claim loss should degrade civilian throughput before ownership is silently transferred.
- Separating enforcement validation from contract definition keeps each phase small and executable.

## Scope

- Add focused validation for faction-aligned settlement use, hostile or unclaimed denial, and claim-loss or faction-mismatch degradation.
- Prefer additive root GameTests or narrow regression checks.
- Keep the slice bounded to settlement-faction enforcement rather than broad logistics or warfare coupling.

## Planned Execution Slices

- Plan 10-01: Validate friendly-claim settlement binding and hostile or unclaimed denial in root GameTests.
- Plan 10-02: Validate settlement degradation on claim loss or faction mismatch without silent ownership transfer.

## Constraints

- Preserve server authority and existing ownership checks.
- Keep settlement-faction behavior explicit before any deeper military coupling slices.
- Avoid broad rewrites of claims, factions, or worker persistence during validation-first work.

## Evidence

- `.planning/ROADMAP.md`
- `.planning/STATE.md`
- `.planning/CODEBASE.md`
- `.planning/VERIFICATION.md`
- `.planning/codebase/INTEGRATED_SYSTEM_ARCHITECTURE.md`
