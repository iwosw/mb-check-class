---
phase: 02-runtime-unification-design
plan: 02
subsystem: docs
tags: [bannermod, compatibility, config, workers, verification]
requires:
  - phase: 02-runtime-unification-design
    provides: BannerMod-first runtime identity and namespace contract
provides:
  - Explicit merged-runtime legacy compatibility boundary
  - BannerMod-owned config direction grounded in current registrars
  - Root readiness docs aligned to the documented migration scope
affects: [phase-03, phase-04, compatibility, config-migration]
tech-stack:
  added: []
  patterns: [code-backed compatibility contracts, compile-processResources-test verification baseline]
key-files:
  created:
    - .planning/phases/02-runtime-unification-design/02-runtime-compatibility-contract.md
  modified:
    - .planning/CODEBASE.md
    - .planning/VERIFICATION.md
key-decisions:
  - "The merged runtime only guarantees known Workers-era migration seams, not standalone workers mod compatibility."
  - "BannerMod-owned config is the target end-state, while Workers config registration remains transitional only."
patterns-established:
  - "Compatibility claims must be tied to explicit code seams such as WorkersLegacyMappings and WorkersRuntime.migrateStructureNbt."
  - "Design-only slices stay on compileJava, processResources, and test unless runtime gameplay flows actually change."
requirements-completed: [BOOT-05]
duration: 9min
completed: 2026-04-11
---

# Phase 2 Plan 2: Runtime Unification Design Summary

**Merged-runtime compatibility contract with BannerMod-owned config direction and root docs scoped to known workers migration seams**

## Performance

- **Duration:** 9 min
- **Started:** 2026-04-11T04:55:30Z
- **Completed:** 2026-04-11T05:04:34Z
- **Tasks:** 2
- **Files modified:** 3

## Accomplishments
- Added an explicit compatibility and config ownership contract for D-05 through D-09.
- Updated `.planning/CODEBASE.md` so remaining `workers:*` risk is framed as uncovered cases outside the known merged-runtime migration seams.
- Updated `.planning/VERIFICATION.md` so design work preserves migration helpers while keeping compile, processResources, and test as the default baseline.

## Task Commits

Each task was committed atomically:

1. **Task 1: Write the merged-runtime compatibility and config ownership contract** - `f5b5719` (feat)
2. **Task 2: Publish the compatibility boundary in active readiness docs** - `855d280` (chore)

**Plan metadata:** pending

## Files Created/Modified
- `.planning/phases/02-runtime-unification-design/02-runtime-compatibility-contract.md` - Documents supported Workers-era migration seams and BannerMod-owned config direction.
- `.planning/CODEBASE.md` - Narrows remaining `workers:*` risk to uncovered custom payload/datapack cases outside the contract.
- `.planning/VERIFICATION.md` - Aligns verification guidance to preserve existing migration helpers without overstating GameTest requirements.

## Decisions Made
- Supported compatibility is limited to known merged-runtime migration seams such as `WorkersLegacyMappings` remaps and structure/build NBT migration.
- External standalone `workers` identity compatibility and arbitrary third-party `workers:*` payload support are out of scope unless a later plan makes them explicit.
- BannerMod-owned config is the end-state, with split Workers config registration treated as a transitional migration seam.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None.

## User Setup Required

None - no external service configuration required.

## Known Stubs

- `.planning/VERIFICATION.md:24` - Existing note says root GameTest directories are "placeholders only" because active GameTest coverage is still intentionally deferred to later runtime-validation work.

## Next Phase Readiness
- Phase 02 is complete: runtime identity, namespace destination, compatibility boundary, and config ownership direction are now explicit.
- Later implementation phases can absorb remaining workers save/config/resource seams against one documented compatibility boundary instead of inferred behavior.

## Self-Check: PASSED

---
*Phase: 02-runtime-unification-design*
*Completed: 2026-04-11*
