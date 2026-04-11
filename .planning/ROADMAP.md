# Merge Roadmap

## Phase 1: Workspace Bootstrap

- Establish one root Gradle entrypoint.
- Archive legacy planning trees.
- Create merge documentation and the active root `.planning/` context.

**Goal:** Root workspace bootstrap is explicit and executable: one root Gradle entrypoint, one active root planning context, preserved legacy archives, and documented merge truth/verification rules.

**Plans:** 2 plans

Plans:
- [x] 01-01-PLAN.md — Harden the single root build/workspace entrypoint and root source-of-truth docs.
- [x] 01-02-PLAN.md — Lock the verification baseline, merge conflict notes, and explicit bootstrap roadmap/state entries.

Planning artifacts live under `.planning/phases/01-workspace-bootstrap/` so Phase 1 bootstrap work stays explicit and reviewable.

Status: Complete (2/2 plans complete as of 2026-04-11)

## Phase 2: Runtime Unification Design

- Decide final mod id, artifact identity, and metadata strategy.
- Define namespace migration rules for `workers` assets, lang keys, and registry ids.
- Define packet, config, and save-data compatibility strategy.

**Goal:** BannerMod runtime-unification target is explicit and actionable: release identity, asset/lang namespace end-state, merged-runtime legacy-state boundary, and BannerMod-owned config direction are documented against current code seams.

**Requirements:** [BOOT-05]

**Plans:** 2 plans

Plans:
- [x] 02-01-PLAN.md — Publish the BannerMod-first release identity and `bannermod` namespace end-state.
- [x] 02-02-PLAN.md — Define the merged-runtime compatibility boundary and BannerMod-owned config direction.

Planning artifacts live under `.planning/phases/02-runtime-unification-design/` so the design contracts remain explicit before implementation phases consume them.

Status: Complete (2/2 plans complete as of 2026-04-11); runtime identity, bannermod namespace destination, compatibility boundary, and BannerMod-owned config direction are all explicit.

## Phase 3: Workers Subsystem Absorption

- Move worker entities and work areas behind the root runtime entrypoint.
- Fold `workers` registries, menus, packets, and client screens into the unified mod.
- Replace hard imports from `com.talhanation.recruits.*` with same-project integration seams where needed.

Status: Complete in practice; workers runtime ownership is already absorbed into the merged BannerMod runtime, with remaining cleanup and smoke validation tracked under Phase 5.

## Phase 4: Resource and Data Consolidation

- Merge assets and data paths under the final namespace strategy.
- Resolve duplicate GUI/resource names.
- Merge access transformers, mixin configs, and pack metadata.

Status: Complete in practice; active runtime resources, pack wiring, and workers registry-coupled namespaces now route through `bannermod`, while preserved `workers` assets/resources remain cleanup-only follow-up.

## Phase 5: Stabilization and Cleanup

- Remove transitional adapters that are no longer needed.
- Re-run tests and smoke validation from the unified root.
- Retire legacy source trees once all runtime ownership has moved.

**Goal:** Stabilization work turns the merged BannerMod runtime into a lower-risk root baseline: retained Workers regressions run from the active root workspace, high-risk merged-runtime mutation and legacy-listener seams are fenced or retired without violating the Phase 02 compatibility contract, and root docs tell the cleanup truth.

**Requirements:** [STAB-01, STAB-02, STAB-03, STAB-04]

**Plans:** 4 plans

Plans:
- [x] 05-01-PLAN.md — Expand the root regression pipeline to execute retained Workers JUnit suites.
- [x] 05-02-PLAN.md — Harden build-area update authoring so merged worker edits honor the existing access rules.
- [x] 05-03-PLAN.md — Retire live registration of duplicated legacy update-check listeners in the merged runtime.
- [x] 05-04-PLAN.md — Refresh roadmap/state/readiness docs so the stabilization baseline and remaining cleanup boundary stay truthful.

Planning artifacts live under `.planning/phases/05-stabilization-and-cleanup/` so stabilization work stays explicit before execution.

Status: Complete (4/4 plans complete as of 2026-04-11).

- Latest slice: workers registry-layer namespace unified under `bannermod`, root compile/resources/test revalidated, and legacy structure entity ids kept readable via migration fallback.
- Latest slice: added focused legacy `workers:*` compatibility hooks for world/registry remaps plus structure-scan/build NBT migration, then revalidated root compile/processResources/test.
- Latest slice: documented root verification entrypoints and merged codebase source-of-truth paths, and added lightweight Workers smoke/regression tests for merged runtime helpers and builder progress.
- Latest slice: routed retained Workers JUnit suites through the root `test` source set so merged regression coverage now runs from `./gradlew test`.
- Latest slice: fenced build-area mutation behind shared authoring rules and disabled duplicate legacy update-check listeners while keeping the Phase 02 compatibility boundary narrow.
- Latest slice: published the shared BannerMod system vocabulary in active planning docs and added a root smoke test that asserts the recruit-owned `bannermod` runtime and worker subsystem network seam stay aligned.
- Latest slice: introduced a shared BannerMod authority contract so worker authoring and worker-control recovery now use the same owner, same-team, admin, and forbidden relationship vocabulary without widening permissions.
- Latest slice: extended the shared BannerMod supply-status seam so recruit upkeep now joins build-area and worker-storage pressure in one passive logistics vocabulary for low-risk settlement-to-military coupling.
- Latest slice: unified the bootstrap onto BannerMod-owned `military`, `settlement`, and `client` config file surfaces, with explicit one-way migration from `bannermod-server.toml` and `workers-server.toml` only when the new target files do not yet exist.
- Latest slice: deepened root GameTest coverage from runtime coexistence to one live recruit-worker-crop-area shared-ownership interaction plus one live build-area-to-recruit-upkeep resupply transition, and `verifyGameTestStage` is currently green with 26 required tests.

## Phase 6: Player-Cycle GameTest Validation

- Validate the integrated BannerMod gameplay loop as one player-facing system through root GameTests.
- Extend root GameTest coverage from isolated subsystem seams into small end-to-end player-cycle slices.
- Prefer additive validation rooted in `src/gametest/java/com/talhanation/bannermod/` unless a test exposes a real runtime defect.

**Goal:** Validate the integrated BannerMod gameplay loop as one player-facing system through root GameTests: prove player-owned recruit and worker coexistence, settlement labor participation, settlement-to-military upkeep flow, and a stitched authority-safe gameplay cycle under one merged runtime.

**Plans:** 4 plans

Plans:
- [x] 06-01-PLAN.md — Validate that one player can own recruit and worker actors in the same BannerMod runtime while shared ownership boundaries stay correct.
- [ ] 06-02-PLAN.md — Validate that an owned worker can bind to settlement infrastructure and participate in a valid work area under the same authority rules.
- [ ] 06-03-PLAN.md — Validate that settlement-side supply can satisfy recruit upkeep and readiness through a live runtime transition.
- [ ] 06-04-PLAN.md — Validate the stitched full BannerMod player cycle with authority and recovery expectations in one root GameTest flow.

Planning artifacts live under `.planning/phases/06-player-cycle-gametest-validation/` so player-cycle validation work stays explicit and reviewable.

Status: In progress (1/4 plans complete as of 2026-04-11).
