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

## Active Phase 22 Requirements

- [x] CITIZEN-01 — One shared citizen core owns common authority, team, inventory, persistence, navigation-handoff, and runtime-flag state while recruit and worker wrappers preserve current runtime ids and external save identities.
- [x] CITIZEN-02 — Recruit and worker specialization routes through explicit citizen role/job controller seams instead of adding a new deep inheritance tree.
- [x] CITIZEN-03 — One controlled recruit path and one controlled worker path use the citizen seam live without changing the live mod id, registry identity, packet entrypoints, screen entrypoints, or current narrow save-compatibility seams.
- [x] CITIZEN-04 — Existing root GameTests plus targeted new regression coverage prove the citizen seam keeps recruit and worker behavior compatibility-safe during the incremental rollout.

## Active Phase 21 Requirements

- [x] SRCMOVE-01 — Canonical shared seam ownership lives under `com.talhanation.bannermod.shared/**` and `com.talhanation.bannermod.config`, and retained legacy `com.talhanation.bannermod.{authority,settlement,logistics}` seam classes are deprecated compatibility forwarders only. (Plan 21-02; narrowed Option A scope -- 5 of 8 named classes shipped, deferred 3 documented in MERGE_NOTES.md and SUMMARY)
- [x] SRCMOVE-02 — Active recruit military/shared gameplay AND worker civilian gameplay (entities, AI, persistence, client flows) are re-homed onto canonical `com.talhanation.bannermod.{entity,ai,pathfinding,persistence,client}/{shared,military,civilian}/**` packages, replacing the legacy `com.talhanation.recruits/**` and `com.talhanation.workers/**` owners of that behavior.
- [x] SRCMOVE-03 — Remaining `com.talhanation.recruits/**` files are reduced to documented compatibility surfaces (deprecated forwarders into canonical `bannermod.*` homes, or explicitly enumerated event/adapter/mixin seams in `MERGE_NOTES.md`); no recruit-package file still owns live military/shared gameplay behavior.
- [x] SRCMOVE-04 — Remaining `com.talhanation.workers/**` files are reduced to the enumerated narrow compat surface (bootstrap glue, packet registrar, legacy `workers:*` id remap, structure NBT migration, Forge-bus event subscribers, i18n helpers) with canonical civilian ownership living exclusively under `com.talhanation.bannermod.*.civilian/**` and the narrow worker compat under `com.talhanation.bannermod.compat.workers/**`.

## Active Phase 23 Requirements

- [x] GOV-01 — Settlement governance state exists as one explicit persisted claim-keyed seam with pure legality rules before live governor assignment, heartbeat, or UI wiring lands.
- [x] GOV-02 — Governor designation and revocation route through authority-safe runtime services over existing recruit or citizen identities.
- [x] GOV-03 — Governed claims publish bounded local tax, incident, and recommendation output without widening into treasury or logistics rewrites.
- [ ] GOV-04 — Governor control is validated through focused GameTests and player-facing UI flows over the real server-side governance snapshot.

## Active Phase 31 Requirements

- [x] CLAIMGROW-01 — Friendly claims count as valid settlement context for worker growth without introducing a new settlement manager.
- [x] CLAIMGROW-02 — Claim worker growth counts existing workers, slows via an explicit diminishing cadence, seeds claim leader ownership/team defaults, and denies hostile or unclaimed territory.
- [x] MINERCFG-01 — Miner authoring exposes only tunnel and branch settings instead of the legacy generic mining box contract.
- [x] MINERSAFE-01 — Miner excavation skips hostile-claim blocks while preserving friendly-claim and unclaimed excavation.
