# Ownership Matrix

Phase 21 target destination is `src/main/java/com/talhanation/bannerlord/**`. The matrix below records current ownership, runtime authority, dependencies, and move blockers by technical surface.

| Surface | Current source-root owner | Primary package family | Runtime authority | Critical dependencies / pressure | target destination | Phase 21 move wave | Blocker / adapter need |
| --- | --- | --- | --- | --- | --- | --- | --- |
| bootstrap/runtime identity | `recruits/` + root build | `com.talhanation.recruits` | Recruit `Main` is the only `@Mod`; shared runtime id is `bannermod` | `build.gradle`, `Main`, `ModLifecycleRegistrar`, `WorkersSubsystem` | `com.talhanation.bannerlord.bootstrap` | Wave 2 | Must preserve one `bannermod` mod id and keep workers composed through same runtime during move |
| registries | split: recruit registries in `recruits/`, worker registries in `workers/` | `com.talhanation.recruits.init`, `com.talhanation.workers.init` | Registered on one mod event bus via recruit-led lifecycle | Worker registry ids still rely on `WorkersRuntime` helpers and legacy remaps | `com.talhanation.bannerlord.registry.military` and `.registry.civilian` with shared bootstrap adapters | Wave 2 | Move order must not break current registry ids or `workers:*` missing-mapping bridge |
| entities | split, dominated by `recruits/` base classes and `workers/` civilian entities | `com.talhanation.recruits.entities`, `com.talhanation.workers.entities` | Recruit entity hierarchy provides shared base behavior used by workers | Workers import `AbstractRecruitEntity`; spawn eggs/renderers also depend on recruit classes | `com.talhanation.bannerlord.entity.military` and `.entity.civilian` | Waves 3-4 | Worker entities cannot move cleanly before shared entity base/inheritance seams are relocated or adapter-wrapped |
| AI/pathfinding | recruit-owned core with worker consumers | `com.talhanation.recruits.pathfinding`, worker `entities.ai` | Recruit async pathfinding stack remains authoritative | Worker entities import `AsyncGroundPathNavigation`; civilian goals assume recruit navigation types | `com.talhanation.bannerlord.ai.pathfinding` plus role-specific AI packages | Waves 3-4 | High blocker: workers are pathfinding-coupled to recruit-owned navigation |
| networking | recruit-created shared channel with split message catalogs | `com.talhanation.recruits.network`, `com.talhanation.workers.network` | `Main.SIMPLE_CHANNEL` is created once; workers register after offset 102 | `WorkersNetworkRegistrar`, packet ordering, message-id stability, shared channel bootstrap | `com.talhanation.bannerlord.network` | Wave 2 | Keep one shared channel and preserve packet ordering/compat seams while packages move |
| persistence/storage | recruit-owned world save vocabulary plus worker entity/work-area NBT | `com.talhanation.recruits.world`, `com.talhanation.workers.world` | Recruit managers own claims/factions/groups; workers own structure/build/work-area serialization | `BannerModSettlementBinding` depends on recruit claims; `WorkersRuntime.migrateStructureNbt` handles legacy `workers:*` structure ids | `com.talhanation.bannerlord.persistence` with military/civilian subpackages | Waves 3-5 | Must keep save-critical claim and structure migration behavior intact before source-root retirement |
| config | root seam plus split config specs | root `com.talhanation.bannermod.config`, recruit config, worker config | `BannerModConfigFiles` defines merged filenames; lifecycle registrars wire both configs | Shared file naming already bridges both families | `com.talhanation.bannerlord.config` | Wave 1 | Low blocker; likely early move candidate if runtime filenames stay stable |
| client/UI | split, but workers heavily reuse recruit widgets/screen bases | `com.talhanation.recruits.client`, `com.talhanation.workers.client` | Recruit client lifecycle triggers both families | Worker screens depend on `RecruitsScreenBase`, `CommandScreen`, `PlayersList`, `RecruitsCheckBox`, other recruit widgets | `com.talhanation.bannerlord.client.military`, `.client.civilian`, and `.client.shared` | Waves 3-4 | Needs shared client/widget package or temporary adapters before worker UI can move independently |
| assets/resources | root processResources with recruit roots and selective worker mirroring | active assets under `assets/bannermod/**`; some preserved worker assets remain under `assets/workers/**` | Root build decides shipped artifact paths | Worker GUI textures and structures already mirrored into active `bannermod` paths; some legacy namespaces remain migration inputs | `src/main/resources/assets/bannermod/**` stays runtime-facing; Java move points to `bannerlord` only | Continuous | Package move must not be confused with runtime asset namespace change |
| root `bannermod` integration seams | root `src/` | `com.talhanation.bannermod.authority`, `.logistics`, `.settlement` | Shared helpers already bridge military/civilian behavior | Still import recruit and worker packages; not yet standalone domain layer | `com.talhanation.bannerlord.shared` or focused domain packages | Wave 1 | Good first-move/adaptor candidates because both families already depend on them |
| legacy compatibility seams | mainly `workers/` plus root build/resource wiring | `com.talhanation.workers` runtime helpers | `WorkersRuntime` and `WorkersLegacyMappings` preserve narrow compatibility | `workers:*` missing-mapping remaps, structure-NBT migration, shared-channel offset, active mod id stays `bannermod` | `com.talhanation.bannerlord.compat.workers` | Wave 5 | Must survive the move until old ids are no longer needed for real save/runtime migration paths |
| root-side reference jars | repo root jars, outside maintained source | external local jars | none at source level, but they shape expected behavior | `shieldwall-1.0.1.jar`, `recruitsmoraleaddon-1.0.0.jar`, `Recruits Siege Compatibility-2.1.0.jar` | no direct Java move; future absorption or optional-compat decision | Deferred beyond Phase 21 | Source-root retirement does not resolve this pressure; later phases need explicit ownership decisions |

## Move-order notes

1. Move or adapter-wrap shared seams first: config naming, authority, supply, settlement binding, and other cross-family helpers.
2. Establish shared bootstrap/network/client bridge packages under `com.talhanation.bannerlord` before moving worker packages that currently depend on recruit internals.
3. Move recruit-owned pathfinding/entity-base vocabulary before or alongside worker entity and AI packages.
4. Keep compatibility helpers (`WorkersRuntime`, `WorkersLegacyMappings`, structure id migration) alive until save-critical legacy paths are proven retired.
5. Treat jar behavior as separate ownership work, not as a side effect of package relocation.

## Target alignment notes

- `20-TARGET-ARCHITECTURE.md` makes Wave 1 shared-seam re-homing the first mandatory Phase 21 slice.
- The matrix now treats worker package relocation as a dependent move, never the opening move.
- `com.talhanation.bannermod/**` surfaces are temporary physical homes only; their permanent destination is under `com.talhanation.bannerlord/**` unless a Phase 21 execution slice documents a narrower domain-specific landing zone.

## Highest blockers for Phase 21

- Worker entity and AI code is tightly coupled to recruit entity and pathfinding classes.
- Worker client/UI code is tightly coupled to recruit widget and command-screen infrastructure.
- Persistence and compatibility seams still rely on recruit-owned claim/save vocabulary plus worker legacy-id migration helpers.
- The runtime id must remain `bannermod` even while Java packages move toward `com.talhanation.bannerlord`.
