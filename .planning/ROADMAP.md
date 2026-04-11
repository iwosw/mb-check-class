# Merge Roadmap

## Phase 1: Workspace Bootstrap

- Establish one root Gradle entrypoint.
- Archive legacy planning trees.
- Create merge documentation and the active root `.planning/` context.

**Goal:** Root workspace bootstrap is explicit and executable: one root Gradle entrypoint, one active root planning context, preserved legacy archives, and documented merge truth/verification rules.

**Plans:** 2 plans

Plans:
- [x] 01-01-PLAN.md — Harden the single root build/workspace entrypoint and root source-of-truth docs.
- [ ] 01-02-PLAN.md — Lock the verification baseline, merge conflict notes, and explicit bootstrap roadmap/state entries.

Status: In Progress (1/2 plans complete as of 2026-04-11)

## Phase 2: Runtime Unification Design

- Decide final mod id, artifact identity, and metadata strategy.
- Define namespace migration rules for `workers` assets, lang keys, and registry ids.
- Define packet, config, and save-data compatibility strategy.

Status: complete for mod id, root runtime entrypoint, and active workers registry namespace; broader compatibility cleanup still open.

## Phase 3: Workers Subsystem Absorption

- Move worker entities and work areas behind the root runtime entrypoint.
- Fold `workers` registries, menus, packets, and client screens into the unified mod.
- Replace hard imports from `com.talhanation.recruits.*` with same-project integration seams where needed.

Status: merged runtime baseline achieved.

## Phase 4: Resource and Data Consolidation

- Merge assets and data paths under the final namespace strategy.
- Resolve duplicate GUI/resource names.
- Merge access transformers, mixin configs, and pack metadata.

Status: mostly complete; active runtime resources and workers registry-coupled namespaces now route through `bannermod`, with legacy preservation cleanup still pending.

## Phase 5: Stabilization and Cleanup

- Remove transitional adapters that are no longer needed.
- Re-run tests and smoke validation from the unified root.
- Retire legacy source trees once all runtime ownership has moved.

Status: current active phase.

- Latest slice: workers registry-layer namespace unified under `bannermod`, root compile/resources/test revalidated, and legacy structure entity ids kept readable via migration fallback.
- Latest slice: added focused legacy `workers:*` compatibility hooks for world/registry remaps plus structure-scan/build NBT migration, then revalidated root compile/processResources/test.
- Latest slice: documented root verification entrypoints and merged codebase source-of-truth paths, and added lightweight Workers smoke/regression tests for merged runtime helpers and builder progress.
