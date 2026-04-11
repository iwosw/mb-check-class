# Phase 09 Context

## Phase

- Number: 09
- Name: Settlement-Faction Binding Contract

## Goal

- Make settlement-to-faction binding explicit as a first-class BannerMod gameplay contract that downstream authority, logistics, and military slices can rely on.

## Why This Phase Exists

- The integrated architecture already describes settlements as faction-aligned operational hubs, but the active roadmap does not yet break that into executable follow-up work.
- Future legality, supply, and claim-loss behavior will stay ambiguous unless settlement binding rules are explicit first.
- The current architecture prefers derived settlement-plus-claim rules before any deep persistence rewrite, so the contract should be documented and scoped at that same level.

## Scope

- Publish the settlement-faction lifecycle vocabulary and binding rules in active planning-first form.
- Add the smallest runtime seam needed for faction-aware settlement legality or status queries if execution requires code support.
- Keep the phase contract-first and low-risk.

## Planned Execution Slices

- Plan 09-01: Publish the explicit settlement-faction binding rules and lifecycle vocabulary for the merged runtime.
- Plan 09-02: Add the smallest runtime seam needed so faction-aware settlement legality and status can be queried consistently.

## Constraints

- Treat settlements as derived operational footprints unless code proves a dedicated manager is necessary.
- Keep compatibility claims narrow and truthful.
- Avoid broad save-format or persistence rewrites in this phase.

## Evidence

- `.planning/ROADMAP.md`
- `.planning/STATE.md`
- `.planning/CODEBASE.md`
- `.planning/codebase/INTEGRATED_SYSTEM_ARCHITECTURE.md`
- `MERGE_NOTES.md`
