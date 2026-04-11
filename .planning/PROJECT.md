# BannerMod Merge Workspace

## What This Is

This workspace merges `recruits/` and `workers/` into one staged Forge project. The root build, active planning context, and shipped runtime have been unified under `bannermod`; the remaining work is cleanup, namespace strategy, and validation rather than maintaining two active mods.
The merged workspace has one active root `.planning/` context and one active root runtime/build entrypoint centered on `bannermod`.

## Core Value

One maintainable mod workspace with preserved history, explicit merge decisions, and no loss of either codebase's existing functionality or planning context.

## Current Base

- Runtime/build base: `recruits`
- Final merged mod id: `bannermod`
- Workers subsystem status: absorbed into the active root runtime, with some historical namespaces intentionally preserved
- Legacy context archives: `.planning_legacy_recruits/`, `.planning_legacy_workers/`
- Readiness references: `.planning/CODEBASE.md`, `.planning/VERIFICATION.md`, `MERGE_NOTES.md`

## Constraints

- Keep the code truthful: the shipped runtime is merged, but some historical namespaces and legacy metadata/build tails are still intentionally preserved.
- Preserve existing source trees during the staged merge.
- Active root docs and root code win on conflict with historical planning artifacts; record the disagreement in `MERGE_NOTES.md`.
- Prefer real code over legacy planning artifacts when the two disagree.
- Keep the build on Minecraft Forge `1.20.1` / Java 17 until the merge stabilizes.

## Initial Decisions

| Decision | Rationale | Status |
|----------|-----------|--------|
| Use `recruits` as the root build base | `workers` has a hard dependency on `recruits` classes, UI, pathfinding, and gameplay state | Accepted |
| Treat `workers` as a subsystem, not a peer runtime mod, in the final target | Workers adds profession/work-area automation on top of recruit ownership, control, navigation, and UI foundations | Accepted |
| Archive both old `.planning` trees at root instead of deleting them | Merge work needs historical context without leaving two active GSD roots in place | Accepted |
| Keep registry-id migration separate from runtime merge | Allows one active mod/runtime without forcing risky save-data and registry-id changes in the same slice | Accepted |
