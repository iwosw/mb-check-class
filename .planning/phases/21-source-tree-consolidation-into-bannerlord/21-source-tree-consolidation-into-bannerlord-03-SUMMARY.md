---
phase: 21-source-tree-consolidation-into-bannerlord
plan: 03
subsystem: infra
tags: [bannerlord, source-tree, entities, pathfinding, persistence, client]
requires:
  - phase: 21-source-tree-consolidation-into-bannerlord
    provides: wave-2 bannerlord bootstrap, network, and registry ownership
provides:
  - bannerlord-owned recruit entity, AI, persistence, and client foundation packages
  - bannerlord-owned pathfinding and military performance seam copies for later worker retargeting
  - updated wave-3 verification guidance tied to pathfinding and GameTest risk
affects: [phase-21-wave-4, workers, pathfinding, persistence, client]
tech-stack:
  added: []
  patterns: [bannerlord canonical copies with recruit compatibility left in place, shared client widgets under com.talhanation.bannerlord.client.shared]
key-files:
  created:
    - src/main/java/com/talhanation/bannerlord/entity/shared/AbstractRecruitEntity.java
    - src/main/java/com/talhanation/bannerlord/entity/military/RecruitEntity.java
    - src/main/java/com/talhanation/bannerlord/ai/pathfinding/GlobalPathfindingController.java
    - src/main/java/com/talhanation/bannerlord/ai/military/RecruitAiLodPolicy.java
    - src/main/java/com/talhanation/bannerlord/persistence/military/RecruitsClaim.java
    - src/main/java/com/talhanation/bannerlord/client/shared/gui/CommandScreen.java
  modified:
    - src/main/java/com/talhanation/bannerlord/bootstrap/BannerlordMain.java
    - src/main/java/com/talhanation/bannerlord/bootstrap/BannerlordLifecycleRegistrar.java
    - src/main/java/com/talhanation/bannermod/governance/BannerModGovernorHeartbeat.java
    - src/main/java/com/talhanation/bannermod/governance/BannerModGovernorService.java
    - src/gametest/java/com/talhanation/bannermod/BannerModGameTestSupport.java
    - .planning/VERIFICATION.md
key-decisions:
  - "Copy recruit-owned entity, AI, persistence, and client foundations into root bannerlord packages while leaving the old recruit packages as compatibility surfaces for the staged move."
  - "Treat verifyGameTestStage as the preferred follow-up gate for wave-3 ownership moves that touch retained pathfinding, persistence, and client seams."
patterns-established:
  - "Wave-3 ownership: canonical recruit control systems now live under com.talhanation.bannerlord.entity, .ai, .persistence, and .client."
  - "Brownfield package moves can copy canonical sources first and retarget root bannerlord callers before wave-4 worker relocation removes the old imports."
requirements-completed: []
duration: 1 min
completed: 2026-04-14
---

# Phase 21 Plan 03: Recruit-controlled foundations ownership summary

**Bannerlord-owned military entity, pathfinding, persistence, and shared client foundation packages copied into the root source tree with root runtime callers pointed at the new canonical homes.**

## Performance

- **Duration:** 1 min
- **Started:** 2026-04-14T13:43:51Z
- **Completed:** 2026-04-14T13:44:52Z
- **Tasks:** 2
- **Files modified:** 235

## Accomplishments

- Copied recruit-owned entity bases, military entities, pathfinding, military AI, persistence vocabulary, and client/widget surfaces into `src/main/java/com/talhanation/bannerlord/**`.
- Retargeted root bannerlord bootstrap, governance, and GameTest surfaces to the new wave-3 bannerlord packages.
- Updated verification guidance so wave-3 ownership moves explicitly call out the pathfinding/performance-sensitive follow-up gate and logged the unrelated compile blocker separately.

## Task Commits

Each task was committed atomically:

1. **Task 1: Re-home recruit-controlled foundations into `bannerlord` military/shared packages** - `eb53960` (`feat`)
2. **Task 2: Re-run the correctness and performance-sensitive validation surfaces that this move can disturb** - `99066ce` (`docs`)

## Files Created/Modified

- `src/main/java/com/talhanation/bannerlord/entity/shared/AbstractRecruitEntity.java` - canonical shared recruit base under bannerlord ownership
- `src/main/java/com/talhanation/bannerlord/entity/military/*.java` - canonical military recruit entities and command actors
- `src/main/java/com/talhanation/bannerlord/ai/pathfinding/*.java` - canonical async pathfinding and controller seams preserved from phases 11-19
- `src/main/java/com/talhanation/bannerlord/ai/military/*.java` - canonical military AI, LOD, formation, and navigation seams
- `src/main/java/com/talhanation/bannerlord/persistence/military/*.java` - canonical recruit claim, faction, group, route, and save-data vocabulary
- `src/main/java/com/talhanation/bannerlord/client/shared/**/*.java` - canonical shared recruit UI widgets, screens, overlays, and render helpers used by later worker relocation
- `.planning/VERIFICATION.md` - wave-3 validation guidance now calls out the pathfinding/persistence/client risk profile
- `.planning/phases/21-source-tree-consolidation-into-bannerlord/deferred-items.md` - recorded the unrelated worker logistics compile blocker discovered during validation

## Decisions Made

- Copied the recruit-owned foundations into root `bannerlord` packages instead of rewriting live recruit packages in place, keeping the staged move compatible with the existing brownfield source roots.
- Kept shared recruit UI surfaces under `com.talhanation.bannerlord.client.shared` so later worker screen moves can target one widget/screen-base package.
- Left the unrelated worker logistics compile breakage out of scope for this plan and documented it in `deferred-items.md` after verifying the new bannerlord packages themselves compiled cleanly.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

- `./gradlew compileJava` failed after the wave-3 move because pre-existing worker logistics sources reference missing `BannerModLogisticsService`, `BannerModLogisticsRoute`, and `BannerModCourierTask` classes. The new bannerlord wave-3 sources were fixed until only that unrelated blocker remained.
- `./gradlew processResources` succeeded.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Wave 3 is complete: canonical recruit-owned control systems now exist under root `bannerlord` packages for entity, AI, persistence, and client ownership.
- Wave 4 can now move worker civilian packages onto those bannerlord-owned foundations instead of dragging recruit-owned physical packages along.
- Before claiming full build green again, the pre-existing worker logistics compile blocker in `deferred-items.md` still needs resolution.

## Known Stubs

- `src/main/java/com/talhanation/bannerlord/client/shared/gui/faction/FactionInspectionScreen.java:127` - copied brownfield `TODO` about claim-map gating; inherited from recruit UI and not required for the ownership move.
- `src/main/java/com/talhanation/bannerlord/entity/military/CaptainEntity.java:183` - copied brownfield `TODO` about ranged pickup logic; existing gameplay debt, not introduced by this move.
- `src/main/java/com/talhanation/bannerlord/entity/military/CommanderEntity.java:86` - copied brownfield `TODO` about ranged pickup logic; existing gameplay debt, not introduced by this move.
- `src/main/java/com/talhanation/bannerlord/entity/military/ScoutEntity.java:126` - copied brownfield `TODO` about ranged combat handling; existing gameplay debt, not introduced by this move.
- `src/main/java/com/talhanation/bannerlord/entity/military/VillagerNobleEntity.java:102` - copied brownfield TODO cluster for village-center and villager interactions; existing backlog, not blocking this package move.

## Self-Check: PASSED

- FOUND: `.planning/phases/21-source-tree-consolidation-into-bannerlord/21-source-tree-consolidation-into-bannerlord-03-SUMMARY.md`
- FOUND commits: `eb53960`, `99066ce`
