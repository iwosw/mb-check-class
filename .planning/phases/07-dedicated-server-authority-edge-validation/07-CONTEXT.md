# Phase 07 Context

## Phase

- Number: 07
- Name: Dedicated-Server Authority Edge Validation

## Goal

- Validate merged BannerMod authority and ownership behavior under dedicated-server-only edge conditions where no integrated local-player assumptions are available.

## Why This Phase Exists

- Phase 06 proved player-cycle slices in root GameTests, but those tests still stop short of dedicated-server-specific owner resolution edges.
- The shared authority contract already states that server authority must remain the top rule even when owner lookup is absent or delayed.
- Later multiplayer and settlement-faction work needs a stable dedicated-server baseline first.

## Scope

- Add focused root validation for offline-owner, unresolved-owner, and reconnect-path authority behavior.
- Keep the work validation-first unless tests expose a real server-authority defect.
- Reuse the current ownership and player-cycle test seams rather than inventing a broad new framework.

## Planned Execution Slices

- Plan 07-01: Validate dedicated-server owner-offline and unresolved-owner authority denial for recruit and worker recovery flows.
- Plan 07-02: Validate dedicated-server reconnect and persistence-safe ownership recovery across recruit, worker, and work-area state.

## Constraints

- Preserve the existing owner-or-admin authority contract.
- Keep tests deterministic and narrow.
- Avoid deep persistence or packet rewrites unless a failing test proves they are required.

## Evidence

- `.planning/ROADMAP.md`
- `.planning/STATE.md`
- `.planning/CODEBASE.md`
- `.planning/VERIFICATION.md`
- `.planning/codebase/INTEGRATED_SYSTEM_ARCHITECTURE.md`
