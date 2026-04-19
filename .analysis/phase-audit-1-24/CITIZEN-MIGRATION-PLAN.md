# Citizen-Base Migration Plan

**Goal**: retire the per-profession entity-subclass explosion and converge all NPCs (recruits + workers) on **one** registered `CitizenEntity` type whose active `CitizenProfession` controller is **swappable at runtime** — Manor Lords model.

## Today's surface

Existing Phase 22 foundation under `com.talhanation.bannermod.citizen.*`:

- `CitizenRole` — 2-value enum (`RECRUIT`, `WORKER`). Too coarse for profession granularity.
- `CitizenCore` interface — common NPC state (owner, inventory, hold/move/follow/work-area flags, runtime flags). Good shape; reuse verbatim.
- `CitizenRoleController` interface — lifecycle hooks only (`onCitizenReady`, `onRecoveredControl`, `onBoundWorkAreaRemembered`). No profession-switch semantic.
- `CitizenRoleContext` record — passed into controller hooks.
- `CitizenStateSnapshot` record + `Builder` — full persistence-bridge record with inventory copy/restore helpers.
- `CitizenPersistenceBridge` — static helpers for legacy recruit/worker NBT ↔ snapshot.

Existing per-profession entity subclasses (**15 concrete**):

Military (`bannermod.entity.military.*`):
- `AbstractRecruitEntity` (base)
- `RecruitEntity` (generic spear)
- `BowmanEntity`
- `CrossBowmanEntity`
- `HorsemanEntity`
- `NomadEntity`
- `ScoutEntity`
- `RecruitShieldmanEntity`
- `VillagerNobleEntity`
- `AbstractStrategicFireRecruitEntity` (+ subclasses — TBD inventory in Cit-02)

Civilian (`bannermod.entity.civilian.*`):
- `AbstractWorkerEntity` (base)
- `FarmerEntity`
- `LumberjackEntity`
- `MinerEntity`
- `AnimalFarmerEntity`
- `BuilderEntity`
- `MerchantEntity`
- `FishermanEntity`

Each has its own registered `EntityType`, spawn egg, renderer, NBT layout, hire/spawn flow.

## Target state

- **One** registered `EntityType<CitizenEntity>` (`bannermod:citizen`).
- `CitizenEntity extends PathfinderMob implements CitizenCore` — single entity class.
- `CitizenProfession` enum covers all 15 profession variants + `NONE`.
- `CitizenProfessionController` interface (evolved from today's `CitizenRoleController`) owns goal installation, loadout, inventory size, hiring rules, renderer hints.
- `CitizenEntity.switchProfession(newProfession)` uninstalls old controller's goals + installs new one without entity respawn.
- Save-migration: on NBT load, any legacy `bannermod:bowman` / `bannermod:farmer` / etc. entity → construct `CitizenEntity` with matching profession.
- Legacy per-profession EntityType registrations remain present but marked `@Deprecated`, kept around only so existing save chunks load cleanly until a later GC slice retires them.

## Slice breakdown

### Cit-01 — expand role/profession vocabulary + evolve controller interface (foundation)

Additive, zero behavior change. Lays the contract all further slices bind to.

Files (all under `com.talhanation.bannermod.citizen.*`):
- Expand `CitizenRole` from 2 values to the coarse-category set: `CIVILIAN_RESIDENT`, `CONTROLLED_WORKER`, `CONTROLLED_RECRUIT`, `MILITIA`, `NOBLE`. Keep old `RECRUIT`/`WORKER` as `@Deprecated` aliases mapped via `static fromLegacy(...)` so existing callers don't break.
- NEW `CitizenProfession` enum: 16 values (`NONE`, `FARMER`, `LUMBERJACK`, `MINER`, `ANIMAL_FARMER`, `BUILDER`, `MERCHANT`, `FISHERMAN`, `RECRUIT_SPEAR`, `RECRUIT_BOWMAN`, `RECRUIT_CROSSBOWMAN`, `RECRUIT_HORSEMAN`, `RECRUIT_NOMAD`, `RECRUIT_SCOUT`, `RECRUIT_SHIELDMAN`, `NOBLE`). Each carries metadata: coarse `CitizenRole`, `String legacyEntityId` (for save migration), bounded attributes.
- NEW `CitizenProfessionController` interface extending today's `CitizenRoleController` with profession-switch hooks:
  - `CitizenProfession profession()`
  - `default void installGoals(CitizenCore citizen)`
  - `default void uninstallGoals(CitizenCore citizen)`
  - `default void onProfessionAssigned(CitizenRoleContext context)`
  - `default void onProfessionReplaced(CitizenRoleContext context, CitizenProfession old)`
  - `default int preferredInventorySize()` → `27` default
- NEW `CitizenProfessionRegistry` — static registry mapping `CitizenProfession` → `CitizenProfessionController`. 16 default bindings to `noop(profession)` stub controllers; real controllers install themselves in later slices.
- Unit test `CitizenProfessionRegistryTest` — locks default bindings, lookup by profession, lookup by legacy entity id.

**Commit shape**: `feat(cit-01): expand CitizenRole + add CitizenProfession + controller registry`. ~400 lines additive. No existing class modified.

### Cit-02 — introduce `CitizenEntity` (single entity type)

Heavier. One new entity class + registration.

Files:
- `com.talhanation.bannermod.entity.citizen.CitizenEntity extends PathfinderMob implements CitizenCore`. ~600 lines. Composes:
  - `CitizenCore` state (owner, inventory, flags)
  - `CitizenProfession activeProfession` (mutable)
  - `CitizenProfessionController activeController` (mutable; resolved from registry on profession change)
  - `switchProfession(CitizenProfession newProfession)`: calls `activeController.uninstallGoals(this)`, rebinds registry lookup, calls `activeController.installGoals(this)`, fires `onProfessionReplaced`.
  - `additionalSaveData / readAdditionalSaveData`: persists `CitizenStateSnapshot` via `CitizenPersistenceBridge` PLUS profession name.
- New `EntityType<CitizenEntity>` registration under `bannermod.registry.civilian.ModEntityTypes` (or wherever ModEntityTypes lives) — needs a `/bannermod:citizen` id.
- New spawn egg `bannermod.items.civilian.CitizenSpawnEgg`.
- Minimal client-side renderer stub (`CitizenRenderer`) that delegates to a per-profession model lookup. For Cit-02, just a "default humanoid" renderer so compile works and the entity is visually present. Per-profession visual polish is Cit-04.
- GameTest that spawns a `CitizenEntity` with profession=FARMER and confirms `switchProfession(LUMBERJACK)` works without respawn.

**Commit shape**: `feat(cit-02): CitizenEntity + registration`. ~800 lines. No existing entity deleted yet.

### Cit-03 — concrete profession controllers (port goal sets)

Extract the 15 profession behavior blocks from today's subclasses into controller implementations. One commit per controller (atomic, reviewable).

Per profession:
- Read the existing `FooEntity.registerGoals()` or equivalent
- Extract the Goal-registration block into `FooProfessionController.installGoals(core)` that calls back into the `CitizenEntity`'s `goalSelector` via a narrow adapter
- Leave the old entity class in place — it still compiles, still works

Expected order of port (easiest first, so failures early lose less work):
1. `NoneProfessionController` (no-op baseline) — already exists in Cit-01 as stub
2. `FarmerProfessionController`
3. `LumberjackProfessionController`
4. `FishermanProfessionController`
5. `MinerProfessionController`
6. `AnimalFarmerProfessionController`
7. `MerchantProfessionController`
8. `BuilderProfessionController`
9. `NobleProfessionController`
10. `RecruitSpearProfessionController` (i.e. generic `RecruitEntity` goals)
11. `BowmanProfessionController`
12. `CrossBowmanProfessionController`
13. `ScoutProfessionController`
14. `ShieldmanProfessionController`
15. `HorsemanProfessionController`
16. `NomadProfessionController`

Each controller is a commit of roughly 100-250 lines including its own unit test. ~15 commits in this slice family.

### Cit-04 — renderers + profession-aware visuals

- Per-profession `EntityModel` bindings under `CitizenRenderer.dispatchModel(profession)`.
- Lang keys for profession names.
- Loot tables if per-entity-type loot tables today differ.

### Cit-05 — save-migration: legacy `bannermod:farmer` et al. → `bannermod:citizen`

- Forge `EntityType` mixin or world-load event handler that maps legacy entity ids at load time.
- Reads legacy NBT via `CitizenPersistenceBridge.fromWorkerLegacy()` / `.fromRecruitLegacy()`, constructs `CitizenEntity` with matching profession, spawns in-world at the legacy entity's position.
- GameTest proving that a world-save containing legacy `bannermod:bowman` loads as a `CitizenEntity` with `profession=RECRUIT_BOWMAN` on the next tick.

### Cit-06 — runtime profession-switching UX

- Server-side `MessageRequestCitizenProfession` packet. Admin / owner can request profession change via a GUI button.
- Reuses `CitizenEntity.switchProfession(newProfession)`.
- Validation: check ownership, cooldown, prerequisites (e.g., can't switch to militia without claim-safe context).

### Cit-07 — retire legacy entity subclasses

Once Cit-05 has proven save-migration works for every profession, delete the 15 legacy concrete subclasses. Keep abstract bases (`AbstractRecruitEntity`, `AbstractWorkerEntity`) only if they carry non-migrated behavior — likely they can be deleted too once controllers extract everything.

- Commit one delete per profession for reviewability.
- Legacy `EntityType` registrations → keep as `@Deprecated noop` for one release cycle, then GC.

## Risks + mitigations

1. **Per-entity-type AttributeSupplier** — vanilla attaches attributes per EntityType. One CitizenEntity means one attribute set. Profession-specific buffs must apply via attribute MODIFIERS at `switchProfession` time, not base-attribute differences. Tracked in Cit-02 design notes.
2. **Animations/models** — per-profession models exist today via per-entity renderer. Cit-04 needs explicit mapping; absent mapping falls back to a generic humanoid model.
3. **Save-backward-compat** — legacy entity ids MUST still deserialize. Cit-05 handles this via event-driven migration. Rollback safety: a feature flag `EnableCitizenMigration` (default OFF in Cit-02..04, flipped ON in Cit-05).
4. **GameTest coverage** — retained suite has ~37 required tests tied to recruit/worker entity ids. Some will need updating to use `CitizenEntity` spawn + profession switch instead. Track under Cit-05/06.
5. **Packets** — many `MessageFoo` packets are typed against `AbstractRecruitEntity` or `AbstractWorkerEntity`. After Cit-03 those bases remain; after Cit-07 they don't. Packet types will need retargeting at that point.

## Estimated slice count

- Cit-01: 1 commit (foundation)
- Cit-02: 1 commit (entity + reg + test)
- Cit-03: ~16 commits (one per profession controller)
- Cit-04: 2-3 commits (renderer + model + lang)
- Cit-05: 2 commits (migration path + GameTest)
- Cit-06: 1-2 commits (profession-switch UX)
- Cit-07: ~15 commits (one per legacy-class delete)

Total ≈ 40 atomic commits. Not a single-session job. Cit-01 lands this session; remainder proceeds via dedicated sessions with context focus on one slice family at a time.
