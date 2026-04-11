# Merged Codebase Readiness

## Active Runtime

- Root Gradle project is the only active build entrypoint.
- Active shipped mod/runtime id: `bannermod`.
- Runtime base: `recruits/`.
- Workers is an in-process subsystem compiled into the same root runtime from `workers/src/main/java`.

## Source-of-Truth Paths

- Planning source of truth: `.planning/`
- Merge decisions and plan/code conflicts: `MERGE_NOTES.md`
- Shared system vocabulary and integrated target architecture: `.planning/codebase/INTEGRATED_SYSTEM_ARCHITECTURE.md`
- Root build/runtime wiring: `build.gradle`, `settings.gradle`, `src/main/resources/`
- Active root runtime code: `src/main/java/`, `recruits/src/main/java/`, `workers/src/main/java/`
- Root regression coverage: `src/test/java/`
- Root gametest source set: `src/gametest/` and `recruits/src/gametest/`
- Active root docs describe the intended root-owned architecture even while preserved source trees remain in place.

## Shared Vocabulary Baseline

- `BannerMod runtime` means the one shipped `bannermod` mod identity rooted in `recruits.Main`.
- `Strategic layer` means recruit-owned faction, claim, group, route, and command state.
- `Settlement layer` means the derived claim-plus-work-area operational footprint inside the same BannerMod runtime.
- `Settlement-faction contract` means the explicit derived binding between a settlement footprint and the covering faction claim, with shared `FRIENDLY_CLAIM`, `HOSTILE_CLAIM`, `UNCLAIMED`, and `DEGRADED_MISMATCH` status vocabulary.
- `Military system` means recruit-derived combat actors and orders.
- `Civilian system` means worker-derived professions, work areas, storage, market, and build execution.
- `Logistics seam` means the shared mod id, packet channel, and asset namespace used by workers inside the recruit-led runtime.
- `Authority contract` means the shared owner, same-team, admin, and forbidden relationship model used to validate merged orders and worker authoring actions.
- `Supply status seam` means the shared BannerMod snapshot of build-material pressure, worker storage blockage, and recruit upkeep pressure exposed without rewriting existing worker AI or recruit upkeep systems.

## Legacy Archives

- Recruits planning archive: `.planning_legacy_recruits/`
- Workers planning archive: `.planning_legacy_workers/`
- Legacy archives are preserved for historical context only and are not the active execution root.
- Follow-up agents must not treat archived planning files as active requirements unless a root doc explicitly points to them.

## Verification Entry Points

- Fast compile check: `./gradlew compileJava`
- Resource merge check: `./gradlew processResources`
- Root regression check: `./gradlew test`
- Optional runtime gametest hook: `./gradlew runGameTestServer`

## Current Coverage Baseline

- Root JUnit coverage now includes retained Workers suites routed through the root build plus lightweight merged-runtime smoke checks.
- Build-area mutation now follows the shared authoring-rule boundary through `BuildAreaUpdateAuthoring` before `MessageUpdateBuildArea` mutates server state.
- Worker authoring and worker-control recovery now share one BannerMod authority vocabulary through `BannerModAuthorityRules`.
- Build areas and workers now expose passive supply snapshots through `BannerModSupplyStatus` so later settlement and logistics slices can read one shared status vocabulary.
- Settlement legality and participation now also have one shared status seam through `BannerModSettlementBinding`, keeping the settlement layer explicit without introducing a dedicated settlement manager.
- Recruit upkeep now also projects passive food/payment pressure through `BannerModSupplyStatus`, so settlement-side supply readers can observe one low-risk military readiness seam without changing recruit AI or persistence.
- Legacy recruits/workers update-check listeners are intentionally disabled in the merged runtime until one merged release-feed contract exists.
- Root gametest source set now includes live root-owned integrated validation in `src/gametest/java/com/talhanation/bannermod/IntegratedRuntimeGameTests.java`.
- Full gameplay E2E coverage is still deferred, but root GameTest coverage now materially proves merged runtime coexistence, shared-owner recruit-worker-crop-area interaction, live settlement-to-military supply resupply, and multiplayer authority contention/cooperation under one `bannermod` runtime.

## Open Risks

- Root GameTest coverage is still intentionally bounded: it now proves merged runtime coexistence, shared-owner and dedicated-server authority recovery, live multiplayer outsider denial, same-team recruit/work-area cooperation, and one shared-owner build-area-to-recruit-upkeep resupply transition, but not yet deeper cross-system world interactions like settlement logistics dynamically changing military behavior over longer simulations.
- Some preserved legacy namespaces and source trees remain intentionally present during stabilization; future cleanup must keep save/runtime compatibility truthful.
- Remaining `workers:*` compatibility risk is intentionally narrow: uncovered custom payloads, datapacks, or other third-party references outside the known merged-runtime migration seams may still fail.
- The active scope boundary for that risk now lives in `.planning/phases/02-runtime-unification-design/02-runtime-compatibility-contract.md`, which limits required support to known merged-runtime registry remaps and structure/build NBT migration rather than broad standalone Workers compatibility.
