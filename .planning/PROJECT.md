# BannerMod Merge Workspace

## What This Is

This workspace is the realized merged Forge project created from the historical `recruits/` and `workers/` mods. The root build, active planning context, and shipped runtime are unified under `bannermod`; the remaining work is stabilization, architecture cleanup, and gameplay repair rather than maintaining two active mods.
The merged workspace has one active root `.planning/` context and one active root runtime/build entrypoint centered on `bannermod`.

## Core Value

One maintainable mod workspace with preserved history, explicit merge decisions, and no loss of either codebase's existing functionality or planning context.

## Current Base

- Runtime/build base: root `src/**` under `com.talhanation.bannermod`
- Final merged mod id: `bannermod`
- Workers subsystem status: absorbed into the active root runtime, with some historical namespaces intentionally preserved
- Historical source archives: `recruits/`, `workers/` (reference only; not root build inputs)
- Readiness references: `.planning/CODEBASE.md`, `.planning/VERIFICATION.md`, `docs/STATUS.md`, `docs/CONTRIBUTING.md`

## Constraints

- Keep the code truthful: the shipped runtime is merged, but some historical namespaces and legacy metadata/build tails are still intentionally preserved.
- Preserve existing source trees as archives unless a root plan explicitly scopes archive cleanup.
- Active root docs and root code win on conflict with historical planning artifacts; record material disagreements in `.planning/STATE.md` or `docs/STATUS.md`.
- Prefer real code over legacy planning artifacts when the two disagree.
- Keep the build on Minecraft Forge `1.20.1` / Java 17 until the merge stabilizes.

## Initial Decisions

| Decision | Rationale | Status |
|----------|-----------|--------|
| Use root `src/**` as the active build/runtime base | Phase 21 completed the in-place merge into `com.talhanation.bannermod`; archive trees are reference only | Accepted |
| Treat `workers` as an absorbed subsystem, not a peer runtime mod | Workers profession/work-area automation now lives in the root BannerMod runtime | Accepted |
| Archive both old `.planning` trees at root instead of deleting them | Merge work needs historical context without leaving two active GSD roots in place | Accepted |
| Keep registry-id migration separate from runtime merge | Allows one active mod/runtime without forcing risky save-data and registry-id changes in the same slice | Accepted |
