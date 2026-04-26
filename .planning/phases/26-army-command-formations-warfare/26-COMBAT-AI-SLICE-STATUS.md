# Phase 26 Slice Status: Combat AI Overhaul (HYW Parity)

> Historical status note: this phase file is retained as execution history. All unfinished follow-up work has been consolidated into root `BANNERMOD_BACKLOG.md`; do not use this file as an active task queue.

## Scope

Phase 26 owns the military-side command, formation, morale, siege, and territory-war expansion that used to be spread across old Phases 27, 36–38, and 40–42. This slice narrows that scope to the **HYW-parity combat AI** work: smarter target selection, phalanx-style line cohesion, directional shield blocks, reach-weapon semantics, and HYW's unit-type damage counters. Player-facing UI and packet wiring for combat stances stay out of this slice.

## Status

**Landed on 2026-04-21.** Five atomic commits, all 129 tests in `com.talhanation.bannermod.ai.military.*` green. 12 new pure policy helpers, NBT-persisted combat stance, directional shield mitigation through Forge `ShieldBlockEvent`, second-rank poke through friendly LOS, per-unit attack cadence, and a full flank/cohesion/brace/counter-matrix layer.

## Follow-up Landed 2026-04-22

- Player-facing stance selection is now wired into the command screen through `MessageCombatStance`, reusing the selection-aware group command path so `LOOSE`, `LINE_HOLD`, and `SHIELD_WALL` are selectable in-game for the active groups.
- `CommandIntent` gained a `CombatStanceChange` variant and `CommandEvents.onCombatStanceCommand(...)`, keeping the stance change server-authoritative instead of baking it into GUI-only state mutation.
- `RecruitRangedBowAttackGoal` and `RecruitRangedCrossbowAttackGoal` now gate target-based auto-engagement through `CombatLeashPolicy` against `holdPos` when the recruit is holding formation, so the stance leash applies to ranged units too.
- New GameTest coverage in `BannerModCombatStanceCommandGameTests` proves both the group-authority targeting of the stance packet and the stance-to-ranged-leash behavior loop. `verifyGameTestStage` is now green with 39 required tests.

## Follow-up Landed 2026-04-24

- `CombatStance` is now synced through `SynchedEntityData` instead of being server-only runtime state, so client UI surfaces can read the live stance reliably.
- Added `MessageCombatStanceGui` for single-recruit stance changes in the recruit inventory path, reusing `CommandTargeting.forSingleRecruit(...)` rather than falling back to loose direct mutation.
- `RecruitInventoryScreen` now exposes a compact per-recruit stance cycle button, so the player can inspect and change `LOOSE` / `LINE_HOLD` / `SHIELD_WALL` without using the group command screen.
- `BannerModNetworkBootstrap.workerPacketOffset()` is now effectively `MILITARY_MESSAGES.length == 106` because the per-recruit stance packet extends the military catalog by one more message.

## Follow-up Landed 2026-04-25

- `RecruitMeleeAttackGoal.start()` now keeps recognized reach weapons equipped when melee begins, so spear/pike/sarissa-style troops do not silently switch away from the weapons that drive rank-2 poke, cadence, brace, and formation identity.
- `WeaponReach.effectiveReachForId(...)` now recognizes common polearm/lance aliases (`lance`, `glaive`, `partisan`, `billhook`, `bardiche`, `voulge`) as the pike reach bucket, which also routes them through pike cadence because `AttackCadence` consumes the same resolver.
- Added a reflection-only Better Combat reach metadata seam: `BetterCombatWeaponAttributes` reads `net.bettercombat.logic.WeaponRegistry#getAttributes(ItemStack)` and `net.bettercombat.api.WeaponAttributes#attackRange()` when present, then `WeaponReach.effectiveReachFor(ItemStack)` folds any range above the 5-block BannerMod baseline into the same extra-reach path used by `AttackUtil`.
- Better Combat is now the only compile-only optional combat compat input. Better Mob Combat is deliberately not a dependency or runtime adapter because its current recruit compat mixins target stale `com.talhanation.recruits.*` classes.
- Added `BetterCombatAttackBridge`: recruit melee now reads Better Combat weapon attacks directly through reflection, starts a BannerMod-owned pending upswing, applies Better Combat `upswing()` / `damageMultiplier()` / `angle()` / `hitbox()` metadata, and then calls `AbstractRecruitEntity.doHurtTarget(..., multiplier)` so BannerMod's server-side shield/flank/unit-counter damage rules remain authoritative.
- Added native Better Combat-style secondary AOE hits for eligible attacks. Secondary targets are selected server-side only, capped, arc/range/LOS checked, and filtered through `recruit.shouldAttack(...)` before damage so allies/same-owner/same-group/treaty-protected entities are not swept.
- Added BannerMod-owned synced attack presentation state and lightweight humanoid pose overlays for recruit human/villager renderers. This uses Better Combat attack shape metadata for presentation only; Better Combat's own player-only animation packet is not used as recruit combat authority.

## Delivered

### Part A — Smarter target selection (commit `ebe813d`, part A)

- `RecruitCombatTargeting.resolveCombatTargetWithAssigneeSpread` scores candidates as `distSqr + assignees × 36` against the per-cohort `FormationTargetSelectionController` assignee registry (40-tick TTL). Replaces the old closest-first dogpile for formations.
- Round-robin threshold (≥3 assignees on a shared target) forces a local re-pick so shared cohort targets do not saturate.
- Reactive switching now uses a 3-block hysteresis plus a melee-reach override so recruits don't thrash between equidistant attackers.
- `AbstractRecruitEntity.lastTargetLossTick` + `RecruitRuntimeLoop` force the LOD FULL tier and drop the base gate to 5 ticks for 60 ticks after a kill. Closes the reported 15–20 s "blind spot" where recruits ignored the next enemy after killing three.

### Stage 1 — Line cohesion (commit `ebe813d`, part B)

- New `CombatStance` enum (`LOOSE` default / `LINE_HOLD` / `SHIELD_WALL`) on `AbstractRecruitEntity`, NBT-persisted via `RecruitPersistenceBridge`.
- `CombatLeashPolicy` pins stance-aware engagement: `LOOSE` = 13-block leash inside formation, `LINE_HOLD` = 5, `SHIELD_WALL` = 3. `RecruitMeleeAttackGoal.canContinueToUse` yanks chasers back when they drift off leash.
- `FormationSlotRegistry` tracks per-cohort `(slotIndex → ownerUuid, holdPos, ownerRotDeg)` state populated by `FormationLayoutPlanner`.
- `FormationGapFillPolicy.chooseGapSlot` + `FormationFallbackPlanner.tryFillForwardGap` migrate recruits into a forward-gap slot when their neighbour dies. 60-tick per-recruit cooldown, 20-tick staggered scan via `UUID.hashCode()`.
- `FormationYawPolicy.clampBodyYaw` clamps per-tick body-yaw to 10 °/6 ° while in formation under `LINE_HOLD`/`SHIELD_WALL`. Head yaw is left free so recruits can still look at threats.

### Stage 2 — Directional shield block (commit `2ff128c`)

- `ShieldBlockGeometry.isInFrontCone` computes attacker angle vs `yBodyRot` with a 120° front cone (60° half-angle).
- `ShieldMitigation.damageAfterBlock`: `LOOSE` remaining 0.55 / `LINE_HOLD` 0.45 / `SHIELD_WALL` 0.30. `RecruitShieldmanEntity` gets an extra ×0.9 remaining multiplier stacked on top. Stagger (`blockCoolDown > 0`) reduces absorption by 40 %.
- Non-blockable sources filtered (fall/drown/fire/freeze/magic/wither/`BYPASSES_SHIELD` tag); remaining sources pass.
- `RecruitShieldEvents` cancels Forge `ShieldBlockEvent` for recruits so vanilla does not zero-out damage and does not double-charge shield durability. `prepareIncomingDamage` is the single path.
- `UseShield.canUse` auto-raises the shield for `SHIELD_WALL` (8-block hostile radius) and `LINE_HOLD` (5-block). In-formation `SHIELD_WALL` units slowly pivot body-yaw toward the nearest hostile via `FormationYawPolicy.clampBodyYaw` (6°/tick).
- Blocked melee hits knock back the attacker with 0.5 strength.

### Stage 3 — Reach weapons + rank-2 poke + per-unit cadence (commit `33f86bf`)

- `WeaponReach.effectiveReachFor` returns per-item extra reach via registry-id heuristics and optional Better Combat weapon metadata: `sarissa`/`long_spear` +2.5, `pike`/`halberd`/`polearm`/common polearm aliases +2.0, `spear` +1.0, Better Combat `attackRange() - 5.0` when larger, plain melee +0. Folded additively into `AttackUtil.getAttackReachSqr` alongside Forge `ENTITY_REACH` and Epic Fight compat.
- `FriendlyLineOfSight.canReachThroughAllies` + `RecruitMeleeAttackGoal.hasReachLineOfSight`: reach holders (≥1 block extra) can strike through allied recruits between them and the target. World raycast still gates on blocks; allies don't break LOS for reach weapons.
- `AttackCadence.cooldownTicksFor` tunes post-hit cooldown per weapon: spear +2 tick windup, pike ×1.1 + 4 ticks, sarissa ×1.15 + 5 ticks, plain melee unchanged. Integrated in `AttackUtil.getAttackCooldown`.

### Stage 4 — Flank / cohesion / brace / unit-type counters (commit `fab08a4`)

- `FacingHitZone.classify` → `FRONT` (reuses 120° cone), `BACK` (rear 90° arc), else `SIDE`. `FlankDamage.multiplierFor`: FRONT ×1.0, SIDE ×1.15, BACK ×1.5. Applied in `prepareIncomingDamage` after shield mitigation.
- `FormationCohesion.isCohesive`: cohesive iff ≥2 other cohort-mates within 2 blocks sharing `LINE_HOLD`/`SHIELD_WALL`. Grants ×0.85 incoming damage. Result cached 10 ticks via new `cachedCohesionTick` / `cachedCohesion` fields.
- `BraceAgainstChargePolicy.shouldBrace`: stance ≠ LOOSE + shield/reach holder + mounted hostile within 10 blocks → `setShouldBlock(true)`, halt navigation, set `isBracing`. `UseShield.tick` attaches a transient `KNOCKBACK_RESISTANCE +0.5` attribute modifier while braced, clears it on release. Cavalry hits on a braced target get an extra ×0.7 damage remaining.
- `UnitTypeMatchup.classify` (`LIGHT` / `HEAVY` / `RANGED` / `CAVALRY` / `PIKE_INFANTRY`) + `damageMultiplier` ports HYW's counter matrix: light vs heavy ×0.8, heavy vs light ×1.2, cavalry vs light/ranged ×1.4, foot vs cavalry ×0.9, pike vs cavalry ×1.5. Applied in `RecruitCombatDecisions.doHurtTarget` only when the target is another recruit — PvE balance against players and monsters stays unchanged.

## Intentionally Deferred

> Canonical tracking now lives in `BANNERMOD_BACKLOG.md` under `COMBAT-*`, `UI-*`, and `TEST-*`. The list below is historical evidence of what this slice did not ship.

- **Spear/pike item classes.** BannerMod does not ship `SpearItem`/`PikeItem` yet; `WeaponReach` uses string heuristics. When real classes land, `WeaponReach.effectiveReachFor(Item)` is the single extension point for `instanceof` checks.
- **Velocity-aware charge detection in `BraceAgainstChargePolicy`.** Current implementation is proximity-only on mounted hostile — enough to trigger bracing; no predictive timing.
- **Morale-driven retreat.** Still environmental (`FleeTNT`/`FleeFire`/`FleeTarget`). No panic-threshold or formation-wide rout.

## Verification Update (2026-04-24)

- Re-ran `./gradlew compileJava compileTestJava compileGameTestJava --console=plain` — BUILD SUCCESSFUL.
- Re-ran `./gradlew verifyGameTestStage --console=plain` — BUILD SUCCESSFUL with all 39 required tests passing.
- Current Phase 26 stance-control follow-up is locally green across compile plus full root GameTest validation.

## Verification Update (2026-04-25)

- `git diff --check` — passed.
- `./gradlew test --tests com.talhanation.bannermod.ai.military.WeaponReachTest --tests com.talhanation.bannermod.ai.military.AttackCadenceTest --console=plain` — BUILD SUCCESSFUL after the polearm alias reach/cadence follow-up.
- `./gradlew compileJava --console=plain` — BUILD SUCCESSFUL after the same follow-up.
- `./gradlew test --tests com.talhanation.bannermod.compat.BetterCombatWeaponAttributesTest --tests com.talhanation.bannermod.ai.military.WeaponReachTest --tests com.talhanation.bannermod.ai.military.AttackCadenceTest --console=plain` — BUILD SUCCESSFUL after the Better Combat reach-metadata seam. One prior parallel run hit a local Gradle incremental output `NoSuchFileException`; a standalone rerun passed.
- `./gradlew compileJava processResources --console=plain` — BUILD SUCCESSFUL after replacing the BMC bridge with the native direct Better Combat attack adapter.
- `./gradlew test --tests com.talhanation.bannermod.compat.BetterCombatAttackBridgeTest --tests com.talhanation.bannermod.compat.BetterCombatWeaponAttributesTest --tests com.talhanation.bannermod.ai.military.WeaponReachTest --tests com.talhanation.bannermod.ai.military.AttackCadenceTest --console=plain` — BUILD SUCCESSFUL after the native Better Combat adapter.
- `./gradlew compileJava processResources --console=plain` — BUILD SUCCESSFUL after adding native Better Combat AOE and synced presentation pose state.
- `./gradlew test --tests com.talhanation.bannermod.compat.BetterCombatAttackBridgeTest --tests com.talhanation.bannermod.compat.BetterCombatWeaponAttributesTest --tests com.talhanation.bannermod.ai.military.WeaponReachTest --tests com.talhanation.bannermod.ai.military.AttackCadenceTest --console=plain` — BUILD SUCCESSFUL after the AOE/presentation follow-up.
- `./gradlew verifyGameTestStage --console=plain` with Better Combat present in the dev runtime started the Forge GameTest server and loaded Better Combat (`WeaponRegistry` encoded), but finished red on existing required GameTest `reconnectedownerrecoversauthorityafterownershiproundtrip`; no Better Combat/BMC mixin crash occurred in this live smoke.

## Files Added / Modified

### New (24 files)

Main: `ai/military/CombatStance.java`, `CombatLeashPolicy.java`, `FormationSlotRegistry.java`, `FormationGapFillPolicy.java`, `FormationYawPolicy.java`, `ShieldBlockGeometry.java`, `ShieldMitigation.java`, `WeaponReach.java`, `FriendlyLineOfSight.java`, `AttackCadence.java`, `FacingHitZone.java`, `FlankDamage.java`, `FormationCohesion.java`, `BraceAgainstChargePolicy.java`, `UnitTypeMatchup.java`; `events/RecruitShieldEvents.java`.

Tests (pure, framework-free): `ai/military/CombatLeashPolicyTest.java`, `FormationGapFillPolicyTest.java`, `FormationSlotRegistryTest.java`, `FormationYawPolicyTest.java`, `ShieldBlockGeometryTest.java`, `ShieldMitigationTest.java`, `WeaponReachTest.java`, `FriendlyLineOfSightTest.java`, `AttackCadenceTest.java`, `FacingHitZoneTest.java`, `FlankDamageTest.java`, `FormationCohesionTest.java`, `BraceAgainstChargePolicyTest.java`, `UnitTypeMatchupTest.java`.

### Modified

- `ai/military/FormationTargetSelectionController.java` (+ assignee registry)
- `ai/military/RecruitAiLodPolicy.java` (+ `recentlyLostTarget` context)
- `ai/military/RecruitMeleeAttackGoal.java` (leash + reach LOS)
- `ai/military/RecruitHoldPosGoal.java` (gap-fill scan)
- `ai/military/UseShield.java` (auto-block + brace + facing bias)
- `entity/military/AbstractRecruitEntity.java` (stance, fields, `tickHeadTurn` clamp)
- `entity/military/RecruitCombatOverrideService.java` (shield mitigation + flank + cohesion + brace chain)
- `entity/military/RecruitCombatTargeting.java` (round-robin + reactive hysteresis)
- `entity/military/RecruitRuntimeLoop.java` (recent-loss boost + cohort scorer plumbing)
- `entity/military/RecruitPersistenceBridge.java` (stance NBT)
- `entity/military/RecruitCombatDecisions.java` (unit-type counter on outgoing damage)
- `util/AttackUtil.java` (weapon reach + cadence)
- `util/FormationLayoutPlanner.java`, `util/FormationFallbackPlanner.java` (slot registry + gap fill)
- `bootstrap/BannerModMain.java` (register `RecruitShieldEvents`)

## Verification (2026-04-21)

- `./gradlew compileJava --console=plain` — BUILD SUCCESSFUL after every commit.
- `./gradlew compileTestJava --console=plain` — BUILD SUCCESSFUL.
- `./gradlew test --tests "com.talhanation.bannermod.ai.military.*" --console=plain` — 16 suites, **129 tests, 0 failures, 0 errors**.
- Did NOT run `verifyGameTestStage` in this slice — combat-AI changes are server-tick heuristics over existing seams, and new logic is backed by pure-helper unit coverage rather than new GameTests. A future UI/stance-command slice that exposes this through packets should add GameTest coverage.

## Commit Trail

- `ebe813d` — `feat(ai-combat): smarter targeting + stage 1 line cohesion`
- `2ff128c` — `feat(ai-combat): stage 2 directional shield block + stance auto-block`
- `33f86bf` — `feat(ai-combat): stage 3 reach weapons + rank-2 poke + per-unit cadence`
- `fab08a4` — `feat(ai-combat): stage 4 flank/cohesion/brace + HYW unit-type counters`
