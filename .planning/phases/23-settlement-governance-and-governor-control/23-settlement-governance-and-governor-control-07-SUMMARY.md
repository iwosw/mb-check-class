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

`./gradlew compileJava compileGameTestJava verifyGameTestStage --console=plain`:

- `compileJava`: GREEN
- `compileGameTestJava`: GREEN
- `runGameTestServer`: 36 tests ran, 33 passed, 3 failed (non-governor)
- `BannerModGovernorControlGameTests` — all 3 @GameTest methods **passed** (GOV-04 closed)

The 3 failures are pre-existing non-governor tests:
- `claimlossdegradessettlementwithouttransferringownership`
- `factionmismatchdegradessettlementwithoutsilentrebinding`
- `sameteamcooperationallowscommandsandauthoringbutstilldeniesoutsiders`

Exit code 3 (non-zero due to 3 failing tests). GOV-04 runtime evidence is confirmed — governor tests execute and pass.

## Remaining debt

- 2 root `test` failures (`CitizenRecruitBridgeTest`, `CitizenWorkerBridgeTest`) — legacy archive path references, logged in `deferred-items.md`
- 3 gametest failures above — faction/claim degradation logic, separate from governor control
- `WorkersRuntime.ROOT_NETWORK_ID_OFFSET` stale constant (104 vs 107) — logged in `deferred-items.md`

## Deviations
None. Plan 23-07 was scoped exactly to the config guard fix.
