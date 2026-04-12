# Phase 20 Target Architecture

## Decision summary

- Canonical Java destination for the physical move is `src/main/java/com/talhanation/bannerlord/**`.
- Runtime identity does **not** move with the Java packages: the live Forge mod id remains `bannermod`, bootstrap still exposes one shared runtime, and workers remain an absorbed civilian subsystem inside that runtime.
- Phase 21 should execute as a controlled package-family re-home with temporary adapters where they lower migration risk, not as a blind rename of every `recruits`, `workers`, and `bannermod` class in one sweep.

## Architecture truths carried forward

1. `recruits/src/main/java/com/talhanation/recruits/Main.java` is the current composition root and the only `@Mod` entrypoint.
2. `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java` already treats workers as merged into the active `bannermod` runtime while preserving narrow legacy `workers:*` migration helpers.
3. Root `src/main/java/com/talhanation/bannermod/**` classes are cross-family seams, not an independent gameplay stack.
4. Worker code still depends heavily on recruit-owned bootstrap, entity base classes, pathfinding, GUI, and persistence vocabulary, so the target architecture must stay recruit-led during migration.

## Canonical Bannerlord package families

### `com.talhanation.bannerlord.bootstrap`

Owns the future runtime entrypoint, lifecycle registration, and composition of military + civilian registrars.

- Target residents: current `Main`, `ModLifecycleRegistrar`, shared bootstrap helpers, and the worker-subsystem composition boundary.
- Runtime rule: keep `MOD_ID = "bannermod"` and one shared `SimpleChannel`.
- Migration posture: move early, but only after shared adapter imports are ready so worker lifecycle wiring does not break mid-slice.

### `com.talhanation.bannerlord.shared`

Owns low-risk cross-family seams that already bridge recruit and worker code.

- Target residents: `BannerModAuthorityRules`, `BannerModSupplyStatus`, `BannerModSettlementBinding`, config filename helpers, and other shared utility-style seams discovered during Phase 21.
- Migration posture: first-wave move target because both recruit and worker code already depend on these classes.
- Adapter rule: temporary `com.talhanation.bannermod.*` forwarding wrappers are acceptable only where import churn would otherwise make Phase 21 unsafe.

### `com.talhanation.bannerlord.config`

Owns merged runtime config naming and registration.

- Target residents: `BannerModConfigFiles` plus merged runtime config registrars/helpers.
- Runtime rule: keep the existing `bannermod-military.toml`, `bannermod-settlement.toml`, and `bannermod-client.toml` contract stable.
- Migration posture: move in the first wave with shared seams because it is already BannerMod-shaped and low-risk.

### `com.talhanation.bannerlord.network`

Owns the shared packet channel, packet registration order, and compatibility offset policy.

- Target residents: recruit network bootstrap, worker network registrar/offset helpers, and any packet-id coordination utilities.
- Runtime rule: one channel created by bootstrap; worker packets continue to register after the current compatibility offset boundary.
- Migration posture: bootstrap-adjacent early move, but packet catalogs may remain temporarily split into `.network.military` and `.network.civilian` subpackages while registration consolidates.

### `com.talhanation.bannerlord.registry`

Owns deferred registers and runtime registration structure.

- Target residents: recruit and worker registry holders under explicit subpackages such as `.registry.military` and `.registry.civilian`.
- Runtime rule: preserve existing runtime ids and missing-mapping behavior.
- Migration posture: move after bootstrap/shared seams exist so registrations can import new common types without circular churn.

### `com.talhanation.bannerlord.entity`

Owns the live actor hierarchy.

- `com.talhanation.bannerlord.entity.military`: current recruit entities, leaders, combat units, command-oriented entities.
- `com.talhanation.bannerlord.entity.civilian`: current worker professions, work-area entities, and settlement labor actors.
- `com.talhanation.bannerlord.entity.shared`: any extracted base/entity-contract surfaces that both sides truly share.
- Migration posture: do **not** move worker entities ahead of the base class and pathfinding seams they import today.

### `com.talhanation.bannerlord.ai`

Owns pathfinding, combat AI, and profession goals.

- `com.talhanation.bannerlord.ai.pathfinding`: current recruit-owned pathfinding stack and related async control.
- `com.talhanation.bannerlord.ai.military`: recruit combat and command AI.
- `com.talhanation.bannerlord.ai.civilian`: worker profession AI and work goals.
- Migration posture: move pathfinding/shared AI seams before or with civilian AI to avoid a half-moved worker goal stack.

### `com.talhanation.bannerlord.persistence`

Owns claims, factions, groups, settlement binding helpers, structure/work-area serialization, and migration helpers.

- `com.talhanation.bannerlord.persistence.military`: recruit-owned world/save managers.
- `com.talhanation.bannerlord.persistence.civilian`: worker entity/work-area/world serialization.
- `com.talhanation.bannerlord.compat.workers`: legacy-id and structure migration helpers that must stay alive during transition.
- Migration posture: keep save-critical behavior stable first, rename packages second.

### `com.talhanation.bannerlord.client`

Owns shared widgets, screens, renderers, overlays, and client state.

- `com.talhanation.bannerlord.client.shared`: widget and screen-base surfaces currently reused by worker UI.
- `com.talhanation.bannerlord.client.military`: recruit UIs and client-only military helpers.
- `com.talhanation.bannerlord.client.civilian`: worker UIs and render flows.
- Migration posture: establish `.client.shared` before moving worker screens that still depend on recruit widgets.

## Package-move map

This package-move map is the canonical Phase 21 move order.

| Current family | Target family | Move wave | Adapter policy | Notes |
| --- | --- | --- | --- | --- |
| `com.talhanation.bannermod.authority` | `com.talhanation.bannerlord.shared.authority` | Wave 1 | Optional short-lived forwarder from old package | Already imported from worker and recruit code |
| `com.talhanation.bannermod.logistics` | `com.talhanation.bannerlord.shared.logistics` | Wave 1 | Optional short-lived forwarder | Shared passive seam; low-risk early move |
| `com.talhanation.bannermod.settlement` | `com.talhanation.bannerlord.shared.settlement` or `...persistence.settlement` | Wave 1 | Optional short-lived forwarder | Keep claim-dependent logic explicit; destination may tighten during execution |
| `com.talhanation.bannermod.config` | `com.talhanation.bannerlord.config` | Wave 1 | Avoid long-lived adapter if imports are cheap to update | Keep runtime config filenames unchanged |
| `com.talhanation.recruits.Main` + bootstrap wiring | `com.talhanation.bannerlord.bootstrap` | Wave 2 | No parallel second entrypoint; move as one composition slice | Preserve `@Mod`, `bannermod`, and worker subsystem composition |
| `com.talhanation.recruits.network` | `com.talhanation.bannerlord.network.military` | Wave 2 | Registration adapters allowed during transition | Shared channel remains bootstrap-owned |
| `com.talhanation.workers.network` + runtime registrars | `com.talhanation.bannerlord.network.civilian` and `...compat.workers` | Wave 2 | Keep `WorkersRuntime`-style bridge until legacy paths retire | Preserve offset and packet ordering contract |
| `com.talhanation.recruits.init` | `com.talhanation.bannerlord.registry.military` / `.bootstrap` | Wave 2 | Temporary imports acceptable | Split pure registries from lifecycle/bootstrap helpers |
| `com.talhanation.workers.init` | `com.talhanation.bannerlord.registry.civilian` | Wave 2 | Temporary imports acceptable | Preserve live ids and missing-mapping bridge |
| `com.talhanation.recruits.entities` | `com.talhanation.bannerlord.entity.military` | Wave 3 | Extract adapters only where worker inheritance blocks direct move | Move with or just before shared entity-base seams |
| worker entities/work areas | `com.talhanation.bannerlord.entity.civilian` | Wave 4 | Required temporary adapters if recruit inheritance is still not fully moved | Depends on entity base + pathfinding relocation |
| `com.talhanation.recruits.pathfinding` | `com.talhanation.bannerlord.ai.pathfinding` | Wave 3 | Avoid duplicate implementations; move the real seam | Worker AI depends on this stack today |
| recruit AI packages | `com.talhanation.bannerlord.ai.military` | Wave 3 | Minimal adapters | Keep performance seams intact |
| worker AI packages | `com.talhanation.bannerlord.ai.civilian` | Wave 4 | Adapter bridge acceptable while imports settle | Must follow pathfinding/base-entity move |
| `com.talhanation.recruits.world` | `com.talhanation.bannerlord.persistence.military` | Wave 3 | Minimal adapters for serialization lookups | Save vocabulary is recruit-owned today |
| worker world/structure packages | `com.talhanation.bannerlord.persistence.civilian` | Wave 4 | Keep compat helpers alive | Do not regress structure NBT migration |
| `com.talhanation.workers.WorkersRuntime` / `WorkersLegacyMappings` | `com.talhanation.bannerlord.compat.workers` | Wave 5 | These are the adapter layer | Remain until legacy ids are demonstrably no longer required |
| recruit client packages | `com.talhanation.bannerlord.client.military` and `.client.shared` | Wave 3 | Extract shared widgets first | Worker UI depends on recruit widgets |
| worker client packages | `com.talhanation.bannerlord.client.civilian` | Wave 4 | Temporary client shared adapters allowed | Follows `.client.shared` extraction |

## Phase 21 move order

### Wave 1: move shared seams first

1. Re-home `com.talhanation.bannermod` shared seam classes into `com.talhanation.bannerlord.shared/**` and `com.talhanation.bannerlord.config`.
2. Leave temporary forwarding adapters only where import churn would make later slices risky.
3. Confirm compile/test stability before moving bootstrap.

### Wave 2: re-home bootstrap, network, and registry composition

1. Move the `@Mod` composition root into `com.talhanation.bannerlord.bootstrap`.
2. Re-home shared channel/bootstrap registration into `com.talhanation.bannerlord.network/**`.
3. Re-home registry/lifecycle packages into explicit military/civilian destinations while preserving runtime ids.

### Wave 3: move recruit-owned controlling systems

1. Re-home recruit entity base classes, military entities, pathfinding, client shared widgets, and recruit persistence vocabulary.
2. Keep behavior unchanged; this wave exists to move the controlling seams worker code still imports.

### Wave 4: move worker civilian packages onto the new base

1. Re-home worker entities, AI, world/work-area logic, and UI after their imported bases already live under `bannerlord`.
2. Remove temporary imports/adapters that are no longer needed.

### Wave 5: shrink compatibility and retire source roots

1. Keep only the narrow compatibility adapters that remain necessary for save/runtime migration.
2. Retire `recruits/` and `workers/` source roots only after validation proves all live ownership moved.

## Transitional adapter policy

Temporary adapters are allowed only when they directly reduce migration risk during Phase 21.

Allowed:

- Package-forwarding wrappers from old `com.talhanation.bannermod` seam classes to new `com.talhanation.bannerlord.shared` homes.
- Compatibility shims around `WorkersRuntime`, legacy id remaps, structure-NBT migration, and packet registration ordering.
- Shared client/widget adapter classes while worker screens still import recruit-era types.

Not allowed:

- A second runtime entrypoint or alternate mod id.
- Long-lived duplicate implementations of the same system in both old and new packages.
- Broad promises that every old `workers` or `recruits` package path stays supported indefinitely.

## Compatibility boundary

The compatibility boundary for the move is intentionally narrow and code-backed.

### Must remain stable through Phase 21

- Active Forge mod id stays `bannermod`.
- Active runtime asset namespace stays `bannermod`.
- Bootstrap still creates one shared network channel for the merged runtime.
- Worker packet registration keeps the established offset/ordering contract unless Phase 21 updates all affected registrations in one verified slice.
- `BannerModConfigFiles` runtime-facing config filenames remain stable, including existing migration from legacy server config names.
- `workers:*` missing-mapping remaps remain live through the adapter boundary.
- Save-critical migration helpers such as structure-NBT id migration stay active until legacy saves are proven safe without them.

### Explicitly not promised

- Indefinite support for old Java package names at runtime.
- Restoration of workers as a standalone second Forge mod.
- Blanket compatibility for unknown third-party references to old package names or non-audited payloads.
- Asset namespace reversal back to a live `workers` runtime namespace.

## source-root retirement preconditions

Legacy source roots can be retired only when all of the following are true:

1. `build.gradle` no longer needs `recruits/src/main/java` or `workers/src/main/java` in `sourceSets.main.java.srcDirs`.
2. The only active Java ownership lives under `src/main/java/com/talhanation/bannerlord/**` plus any intentionally retained root non-legacy support packages.
3. The runtime still boots as one `bannermod` mod with one shared channel.
4. Required validation (`compileJava`, `processResources`, `test`, and any plan-required GameTests) is green after the move.
5. Legacy `workers:*` remaps and structure migration paths either still exist in `com.talhanation.bannerlord.compat.workers` or are explicitly retired by evidence-backed follow-up planning.
6. Temporary adapters left behind from old `bannermod`, `recruits`, or `workers` package paths are either removed or documented as the only remaining intentional compatibility layer.
7. Resource processing still ships worker GUI/structure assets into their active `assets/bannermod/**` destinations.

## Migration risk register

| Risk | Why it matters | Mitigation |
| --- | --- | --- |
| Bootstrap split-brain | Moving bootstrap incorrectly could create two perceived runtimes or break worker composition | Move the `@Mod` entrypoint as one explicit slice; do not create a parallel bootstrap |
| Worker entity/pathfinding coupling | Worker entities and AI import recruit-owned bases and navigation | Move entity base/pathfinding seams before worker entities/AI |
| Client/UI coupling | Worker screens depend on recruit widgets and command-screen infrastructure | Establish `com.talhanation.bannerlord.client.shared` before worker UI relocation |
| Save/mapping regression | Structure ids, missing mappings, or claim-backed persistence could break old saves | Keep `compat.workers` migration helpers alive until explicit retirement evidence exists |
| Packet id drift | Reordered registration can break compatibility or internal assumptions | Preserve the shared channel and current offset contract during migration |
| Package move mistaken for runtime rename | Java destination could be confused with mod id or asset namespace changes | Keep docs and execution slices explicit: package move only, runtime identity stable |
| Root-side jar pressure ignored | Shield wall, morale, and siege compatibility pressure still exists outside maintained source | Keep those jars documented as later ownership work, not solved by Phase 21 |

## Phase 21 execution handoff

Phase 21 should treat this document as the move contract:

- move shared seams first,
- move the recruit-led runtime base second,
- move worker civilian packages only after their imported bases are re-homed,
- and preserve only the narrow compatibility boundary documented above.
