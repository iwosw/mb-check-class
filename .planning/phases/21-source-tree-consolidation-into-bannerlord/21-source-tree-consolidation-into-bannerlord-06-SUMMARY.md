---
phase: 21-source-tree-consolidation-into-bannerlord
plan: 06
subsystem: infra
tags: [bannerlord, shared-seams, config, logistics, compatibility]
requires:
  - phase: 21-source-tree-consolidation-into-bannerlord
    provides: root-vendored source layout and wave-1 gap verification from plan 21-05
provides:
  - canonical bannerlord-owned shared seam implementations for authority, settlement, and temporary logistics types
  - canonical bannerlord-owned config helper implementation with stable bannermod file naming
  - deprecated bannermod forwarding wrappers plus retargeted active callers across bannerlord, recruits, and workers packages
affects: [phase-21-closeout, root-validation, compatibility-cleanup]
tech-stack:
  added: [bannerlord.shared authority seam, bannerlord.shared settlement seam, bannerlord.shared logistics seam]
  patterns: [canonical-implementation plus deprecated-forwarder bridge, dual-claim settlement compatibility during staged migration]
key-files:
  created:
    - src/main/java/com/talhanation/bannerlord/shared/authority/BannerModAuthorityRules.java
    - src/main/java/com/talhanation/bannerlord/shared/settlement/BannerModSettlementBinding.java
    - src/main/java/com/talhanation/bannerlord/shared/logistics/BannerModLogisticsService.java
  modified:
    - src/main/java/com/talhanation/bannerlord/config/BannerModConfigFiles.java
    - src/main/java/com/talhanation/bannermod/authority/BannerModAuthorityRules.java
    - src/main/java/com/talhanation/bannermod/settlement/BannerModSettlementBinding.java
    - src/main/java/com/talhanation/bannermod/logistics/BannerModLogisticsRoute.java
    - src/main/java/com/talhanation/bannerlord/entity/civilian/AbstractWorkerEntity.java
    - src/main/java/com/talhanation/recruits/RecruitEvents.java
key-decisions:
  - "Make bannerlord.shared and bannerlord.config the real seam owners while keeping deprecated bannermod wrappers only as temporary adapters."
  - "Let the canonical settlement seam accept both retained recruits claims and moved bannerlord claim lists so mixed-package callers can migrate without splitting the rules logic again."
  - "Preserve legacy logistics route accessor names through alias methods while route ownership moves, so existing UI and packet code keeps its contract stable."
patterns-established:
  - "Wave-1 seam migration: new runtime ownership lives under com.talhanation.bannerlord.shared/** and com.talhanation.bannerlord.config."
  - "Compatibility wrappers: old com.talhanation.bannermod seam classes delegate by enum/record mapping instead of keeping live logic."
requirements-completed: [SRCMOVE-01]
duration: 1 min
completed: 2026-04-15
---

# Phase 21 Plan 06: Shared Seam Ownership Summary

**Canonical authority, settlement, logistics, and config seam ownership now lives under bannerlord packages while bannermod classes are reduced to deprecated forwarders and active callers import the new homes directly.**

## Performance

- **Duration:** 1 min
- **Started:** 2026-04-15T02:26:42Z
- **Completed:** 2026-04-15T02:27:12Z
- **Tasks:** 2
- **Files modified:** 47

## Accomplishments
- Created the missing `com.talhanation.bannerlord.shared/**` family and moved the real authority, settlement, logistics, and combined-container behavior into it.
- Replaced the temporary `bannerlord.config.BannerModConfigFiles` wrapper with the real config implementation while keeping `bannermod-*` filenames and legacy migration behavior stable.
- Retargeted bannerlord, recruits, workers, and governance callers to the bannerlord-owned seams and shrank the old bannermod seam classes into deprecated forwarding wrappers.

## Task Commits

Each task was committed atomically:

1. **Task 1: Create canonical bannerlord shared and config implementations** - `264976b` (feat)
2. **Task 2: Retarget active callers and shrink legacy bannermod seams to forwarders** - `47d8562` (fix)

**Plan metadata:** pending

## Files Created/Modified
- `src/main/java/com/talhanation/bannerlord/shared/authority/BannerModAuthorityRules.java` - canonical authority decision seam.
- `src/main/java/com/talhanation/bannerlord/shared/settlement/BannerModSettlementBinding.java` - canonical settlement-binding seam with mixed-claim compatibility.
- `src/main/java/com/talhanation/bannerlord/shared/logistics/*.java` - canonical temporary logistics records, service, and helper container.
- `src/main/java/com/talhanation/bannerlord/config/BannerModConfigFiles.java` - canonical config taxonomy ownership.
- `src/main/java/com/talhanation/bannermod/{authority,settlement,config,logistics}/**` - deprecated compatibility forwarders that map to bannerlord-owned seams.
- `src/main/java/com/talhanation/bannerlord/**`, `src/main/java/com/talhanation/recruits/**`, `src/main/java/com/talhanation/workers/**` - active callers retargeted to bannerlord-owned shared/config packages.

## Decisions Made
- Moved the real seam implementations instead of leaving bannerlord as another delegating layer, because Phase 21 verification explicitly required bannerlord to become the owner.
- Kept deprecated bannermod wrappers narrow and data-mapping only so later cleanup can delete them without moving behavior again.
- Added claim-list compatibility inside the canonical settlement seam rather than forking separate recruits-versus-bannerlord rule copies.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Added mixed-claim settlement overload handling in the canonical seam**
- **Found during:** Task 2 (Retarget active callers and shrink legacy bannermod seams to forwarders)
- **Issue:** `bannerlord.client` callers already hold moved `bannerlord.persistence.military.RecruitsClaim` data, while retained recruit callers still use `com.talhanation.recruits.world.RecruitsClaim`, so a straight package move would leave the canonical settlement seam unusable from one side.
- **Fix:** Broadened the canonical settlement-binding list handling and single-claim overloads to accept both retained recruit claims and moved bannerlord claims, then mirrored that bridge through the deprecated wrapper.
- **Files modified:** `src/main/java/com/talhanation/bannerlord/shared/settlement/BannerModSettlementBinding.java`, `src/main/java/com/talhanation/bannermod/settlement/BannerModSettlementBinding.java`
- **Verification:** Task 2 legacy-import scan passed, and `WorkersClientManager` now resolves the canonical shared settlement seam against bannerlord client claim state.
- **Committed in:** `47d8562`

**2. [Rule 3 - Blocking] Restored legacy logistics route accessor aliases during seam relocation**
- **Found during:** Task 2 (Retarget active callers and shrink legacy bannermod seams to forwarders)
- **Issue:** `StorageAreaScreen` still expects `minimumSourceStock()` and `desiredDestinationStock()` accessors even though the route record fields are named `minSourceCount` and `destinationThreshold` in the moved canonical type.
- **Fix:** Added alias accessors on both the canonical and deprecated logistics route records so the UI contract stays stable while ownership moves.
- **Files modified:** `src/main/java/com/talhanation/bannerlord/shared/logistics/BannerModLogisticsRoute.java`, `src/main/java/com/talhanation/bannermod/logistics/BannerModLogisticsRoute.java`
- **Verification:** Task 2 legacy-import scan passed, and the moved route type now exposes both the canonical fields and the existing UI-facing accessor names.
- **Committed in:** `47d8562`

---

**Total deviations:** 2 auto-fixed (2 blocking)
**Impact on plan:** Both fixes were migration blockers directly caused by restoring bannerlord ownership. They kept the move truthful without expanding scope beyond seam compatibility.

## Issues Encountered
- The plan’s sample verification command used `python`, but this environment only provides `python3`; verification was rerun with `python3` without changing repository code.
- Pre-existing Phase 21 compile debt outside this slice remains in the repo, but it was not required to complete this ownership move and was not expanded by the task commits.

## Known Stubs
- `src/main/java/com/talhanation/bannerlord/shared/logistics/BannerModLogisticsService.java:54` - `selectBestTask(...)` still returns `Optional.empty()` because the logistics backbone remains a temporary compatibility seam pending Phase 24.
- `src/main/java/com/talhanation/bannerlord/shared/logistics/BannerModLogisticsService.java:58` - `selectBlockedReason(...)` still returns `Optional.empty()` for the same temporary compatibility reason.

## Authentication Gates

None.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Wave-1 ownership truth is restored for the shared seam family: bannerlord packages now own the logic, and old bannermod classes are compatibility-only.
- The next Phase 21 closeout work can target the remaining compile and source-retirement mismatches against bannerlord-owned seams instead of split ownership.

---
*Phase: 21-source-tree-consolidation-into-bannerlord*
*Completed: 2026-04-15*

## Self-Check: PASSED
