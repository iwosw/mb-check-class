# Merge Requirements

## Required

- [x] BOOT-01 — Root workspace has one active build entrypoint.
- [x] BOOT-02 — Root workspace has one active planning context.
- [x] BOOT-03 — Legacy planning context from both source mods remains preserved.
- [x] BOOT-04 — Merge conflicts between plans and code are documented.
- [x] BOOT-05 — Runtime merge work proceeds in explicit stages rather than hidden partial rewrites.

## Deferred Until Full Runtime Merge

- Unified save-data and packet compatibility guarantees.
- Retirement of `recruits/` and `workers/` as standalone source roots.

## Completed During Stabilization

- Final unified mod id and artifact identity are active as `bannermod`.
- Unified registry namespace for Workers content now routes through the active `bannermod` runtime namespace.
- Active runtime accepts legacy `workers:*` ids on confirmed critical compatibility paths (registry remaps and structure/build NBT migration) without reopening a second live namespace.
- Root workspace has a documented merged codebase baseline covering active runtime paths, legacy archives, verification entrypoints, and current open risks.
- Root workspace has lightweight smoke/regression coverage for merged Workers runtime helpers and build-flow progress helpers without requiring full runtime E2E.

## Active Stabilization Requirements

- [x] STAB-01 — Root merged verification runs retained Workers regression tests from the active root workspace.
- [x] STAB-02 — Build-area update packets enforce the same authoring access rules as the other merged worker modification packets.
- [x] STAB-03 — The merged runtime no longer live-registers duplicated legacy update checkers.
- [x] STAB-04 — Active roadmap/state/readiness docs truthfully describe the stabilization verification baseline and remaining cleanup boundary.

## Active Dedicated-Server Authority Validation Requirements

- [x] DSAUTH-01 — Dedicated-server validation proves outsiders cannot command offline-owned recruits or recover offline-owned workers while admin recovery still works when owner lookup is unresolved.
- [ ] DSAUTH-02 — Dedicated-server validation proves reconnect and persistence-safe ownership recovery stays deterministic across recruit, worker, and work-area state.

## Active Phase 31 Requirements

- [x] CLAIMGROW-01 — Friendly claims count as valid settlement context for worker growth without introducing a new settlement manager.
- [x] CLAIMGROW-02 — Claim worker growth counts existing workers, slows via an explicit diminishing cadence, seeds claim leader ownership/team defaults, and denies hostile or unclaimed territory.
- [x] MINERCFG-01 — Miner authoring exposes only tunnel and branch settings instead of the legacy generic mining box contract.
- [x] MINERSAFE-01 — Miner excavation skips hostile-claim blocks while preserving friendly-claim and unclaimed excavation.
