---
phase: 23-settlement-governance-and-governor-control
plan: 07
type: summary
status: complete
gap_closure: true
commits:
  - hash: dcec4c8
    message: "fix(23-07): guard entity sound config reads against gametest harness early-tick"
files_changed:
  - src/main/java/com/talhanation/bannermod/entity/military/AbstractRecruitEntity.java
  - src/main/java/com/talhanation/bannermod/entity/civilian/AbstractWorkerEntity.java
---

# Plan 23-07 Summary

## Objective
Guard `RecruitsClientConfig.RecruitsLookLikeVillagers.get()` calls in entity sound/cosmetic methods against the config-not-loaded state exposed by the gametest harness during `LivingEntity.baseTick`.

## What was done

Added a `recruitsLookLikeVillagers()` helper in both `AbstractRecruitEntity` and `AbstractWorkerEntity` that wraps the `ForgeConfigSpec.ConfigValue.get()` call in a try-catch, falling back to `true` (the config default) on `IllegalStateException`. Replaced all 6 bare `.get()` call sites with the guarded helper:

| File | Method | Line |
|------|--------|------|
| `AbstractRecruitEntity.java` | `getHurtSound` | 1097 |
| `AbstractRecruitEntity.java` | `getDeathSound` | 1103 |
| `AbstractRecruitEntity.java` | `makeLevelUpSound` | ~1999 |
| `AbstractRecruitEntity.java` | `makeHireSound` | ~2004 |
| `AbstractWorkerEntity.java` | `getHurtSound` | ~519 |
| `AbstractWorkerEntity.java` | `getDeathSound` | ~528 |

## Gate result

Historical note: this summary captured the first point where the dedicated governor GameTests themselves executed and passed, even though the full root GameTest suite still had unrelated failures at that time.

Current repo truth has moved on further:

- the active root GameTest gate is now green again
- `verifyGameTestStage` currently passes with 39 required tests in the active tree
- the old non-governor failures listed below are no longer the current blocking story for Phase 23

So this summary remains useful as historical execution evidence for the config-guard fix, but should not be read as the current phase-level verification state. `23-VERIFICATION.md` is the current source of truth.

## Remaining debt

- 2 root `test` failures (`CitizenRecruitBridgeTest`, `CitizenWorkerBridgeTest`) — legacy archive path references, logged in `deferred-items.md`
- 3 gametest failures above — faction/claim degradation logic, separate from governor control
- `WorkersRuntime` network offset drift was logged here historically, but that seam has since been corrected to follow the shared bootstrap offset dynamically.

## Deviations
None. Plan 23-07 was scoped exactly to the config guard fix.
