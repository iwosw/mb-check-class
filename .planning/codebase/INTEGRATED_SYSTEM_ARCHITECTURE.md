# BannerMod Integrated System Architecture

**Status:** Proposed target architecture grounded in the current merged runtime on 2026-04-11

## Intent

BannerMod is one mod and one runtime built on the `recruits` gameplay base with `workers` absorbed as an in-process subsystem. The integrated design keeps Bannerlord-style army, faction, claim, patrol, siege, and command play as a first-class pillar while adding settlement labor, production, storage, and build execution as supporting world systems.

The target is not a MineColonies-style city builder. Settlements exist to support factions, armies, supply, territorial control, and player-led warbands rather than replacing them.

## Core Pillars

- Military command: recruitable units, formations, commands, patrols, officers, combat AI, siege pressure, and faction conflict remain the headline loop.
- Faction territory: claims, diplomacy, treaties, and contested land define where settlements can expand and what must be defended.
- Settlement production: workers convert claimed territory into food, raw materials, storage, trade, and construction capacity.
- War logistics: civilian output feeds military upkeep, replenishment, build projects, and campaign readiness.
- One runtime identity: `bannermod` owns the public bootstrap, config surface, network channel, UI namespace, and player-facing architecture.

## Shared System Vocabulary

- `BannerMod runtime`: the single active merged mod runtime exposed by `recruits.Main` under `bannermod`.
- `Strategic layer`: recruit-owned faction, claim, group, treaty, route, and command state that defines political and military authority.
- `Settlement layer`: the derived operational footprint formed by friendly claims plus active worker infrastructure inside the same runtime.
- `Settlement-faction contract`: the shared BannerMod vocabulary that names whether a queried settlement footprint is `FRIENDLY_CLAIM`, `HOSTILE_CLAIM`, `UNCLAIMED`, or `DEGRADED_MISMATCH`.
- `Military system`: recruit-derived combat units, officers, patrols, formations, and combat orders.
- `Civilian system`: worker-derived professions, work areas, build areas, storage flows, and merchant loops.
- `Logistics seam`: the shared BannerMod authority, packet, and asset surface where military and civilian systems meet without becoming separate runtimes.

Later slices should use these terms consistently in plans, tests, and user-facing integration notes instead of switching between separate recruits/workers terminology.

## Domain Model

## Strategic Layer

- `Faction`: persistent political owner of claims, diplomacy, treaties, and settlement footprint.
- `Claim`: territorial authority unit that gates legal placement, labor rights, and military presence.
- `Group`: player-directed command set for units, including both military squads and worker detachments.
- `Order`: explicit player or AI-issued intent such as move, hold, patrol, attack, recover control, or create/edit work area.

## Operational Layer

- `Settlement`: a faction-aligned operational hub formed by the overlap of claims, storage, work areas, markets, and military presence. It does not need a separate standalone runtime object on day one; it is explicitly derived from existing claim plus work-area state until a unified settlement manager is justified by real code pressure.
- `Work Area`: authored area entity that defines a bounded labor site and rule set for one profession or purpose.
- `Build Area`: specialized work area that consumes structure data and items to create or resume construction projects.
- `Storage Node`: storage area or approved chest network that acts as supply input/output for workers and nearby military support.
- `Market Node`: merchant-oriented trade point that converts surplus into exchangeable goods or faction wealth.

## Actor Layer

- `Military Unit`: recruit-derived combatant with owner/team/faction alignment and combat orders.
- `Civilian Worker`: worker-derived non-frontline specialist bound to a profession and usually to a work area.
- `Officer`: commander/captain/noble-style military coordinator able to project orders across groups.
- `Settlement Controller`: usually the player, but structurally represented through owner UUID, team string, and faction membership checks already used by recruits and workers.

## Ownership And Authority Model

- Server authority is the rule for all persistent state, world mutation, combat resolution, and authored-area edits.
- Player ownership is the first control boundary for units and work areas.
- Team membership is the second boundary for shared use and reassignment.
- Faction claim authority is the world-space boundary that determines where settlement assets may be placed and protected.
- Admin/creative access is a debug or override path, not a gameplay authority tier.

## Practical authority stack

1. Runtime/server validates the action.
2. Ownership/team rules validate who may issue it.
3. Claim/faction rules validate where it may happen.
4. Domain logic validates whether the target system can accept it.

This matches the current code shape: recruit systems already own claims/factions/managers, while work areas already use owner UUID, team string, and authoring rules for modification access.

## Civilian Vs Military Roles

## Military Units

- Primary purpose: fight, escort, patrol, defend claims, raid, reinforce sieges, and hold formation.
- Expected state model: group membership, combat stance, formation/position data, faction allegiance, and persistence through recruit-side managers plus entity state.
- Resource relationship: consume upkeep, food, ammo-equivalent gear, mounts, and replacement equipment supplied by settlements.

## Civilian Workers

- Primary purpose: gather, farm, mine, fish, store, trade, breed livestock, and build.
- Expected state model: bound work area, needed items, storage interaction, blocked/idle reasons, and local entity NBT state.
- Combat posture: civilian-first. They avoid frontline roles by default, can be defended or escorted by military groups, and should not replace recruit combatants.

## Shared rules

- Both roles use the same high-level ownership and group-command ecosystem.
- Both roles can exist under one faction and one settlement footprint.
- Military protects civilian throughput; civilian throughput sustains military readiness.

## Settlement, Work Area, And Claim Relationship

- Claims define legal territorial envelope.
- Settlements are the faction-controlled activity clusters operating inside one or more claims.
- Work areas are the executable sites inside a settlement.
- Build areas are the settlement expansion mechanism.

## Relationship rules

- Work areas should default to requiring friendly claim coverage when `WorkersServerConfig.ShouldWorkAreaOnlyBeInFactionClaim` or its future merged equivalent is enabled.
- Settlement legality should use one explicit status vocabulary: `FRIENDLY_CLAIM` for matching claim coverage, `HOSTILE_CLAIM` for non-matching faction queries, `UNCLAIMED` for no covering claim, and `DEGRADED_MISMATCH` when an existing settlement footprint no longer matches current claim ownership.
- A settlement can span multiple adjacent claims of the same faction, but work areas remain concrete per-site entities.
- Military objects such as patrol routes, guard posts, and siege staging zones may reference the same settlement footprint without being modeled as work areas.
- Claim loss should degrade or disable civilian throughput before ownership is silently transferred.

## Storage, Supply, And Economy Loop

1. Workers gather or produce items inside profession work areas.
2. Workers deposit output into storage nodes.
3. Builders and other workers pull needed items from storage nodes.
4. Merchants convert selected surplus into trade output or economic value.
5. Military systems consume settlement output for upkeep, recruitment readiness, patrol endurance, and campaign preparation.

## Design rules

- Storage is the supply backbone, not just overflow inventory.
- Build projects should be item-backed unless an explicit creative/admin path is chosen.
- Economy should be local-first: settlements matter because distance, claim control, and storage availability affect military sustainability.
- Merchant and market flows complement faction logistics; they do not replace recruit command or warfare loops.

## Command And Order Model

BannerMod should converge on one order language with domain-specific executors.

## Order classes

- Strategic orders: faction management, diplomacy, claim expansion, patrol creation, siege start/end.
- Military orders: move, hold, follow, attack, defend, formation, mount, dismount, patrol, retreat.
- Civilian orders: add work area, edit work area, bind/rebind worker, start build, pause build, recover worker control, assign storage target.
- Logistics orders: designate storage, prioritize supply, route build materials, reserve stock for military or construction use.

## Model rule

- The command UI may stay category-based, but order authorization, serialization, and feedback should converge on shared patterns instead of separate recruit and worker conventions.

## UI Surface

- Shared command screen remains the primary tactical UI, with military categories plus the existing worker category.
- World overlays should show claims, military posture, and nearby work-area bounds as one layered battlefield-settlement view.
- Settlement interaction continues to use focused work-area screens for profession details.
- Faction and world-map screens remain the strategic overview surface.
- Build UI remains structure-centric, but should present supply status, required materials, and claim legality in one place.

## UI principle

Players should feel they are commanding one faction machine with military and civilian tabs, not switching between two unrelated mods.

## Config And Runtime Boundaries

## Runtime boundaries

- Root bootstrap: `recruits.Main` remains the sole `@Mod` entrypoint.
- Recruits-owned runtime: factions, claims, treaties, groups, command categories, client strategic state, and army systems.
- Workers-owned runtime: profession entities, work areas, build templates, storage logic, market logic, and worker-specific screens.
- Shared seam: one `SimpleChannel`, one mod id, one player-facing asset namespace, and one authority model.

## Config boundaries

- Recruits config currently owns broader military/client settings.
- Workers config currently owns profession costs and work-area restrictions.
- Target end-state: one BannerMod config taxonomy with sections for `military`, `faction`, `settlement`, `economy`, and `ui`.
- Active low-risk file taxonomy: `bannermod-military.toml` for recruit-led server settings, `bannermod-settlement.toml` for absorbed workers server settings, and `bannermod-client.toml` for shared client settings.
- Transitional rule: keep existing config classes working behind BannerMod-owned file names, and only migrate known legacy files when the new BannerMod target file does not already exist.

## Migration Strategy From Current Seams

## Current seams to preserve short-term

- `recruits` remains the bootstrap and persistent strategic manager base.
- `workers` remains a preserved source subtree compiled as a subsystem.
- Legacy `workers:*` compatibility stays limited to the already-documented critical migration paths.

## Target consolidation moves

- Unify player-facing command semantics before moving packages.
- Introduce a settlement-oriented strategic facade over claims plus work areas before creating new deep persistence types.
- Route worker/client sync through shared BannerMod state conventions where practical, while keeping entity NBT for local worker state.
- Merge config naming and docs before attempting save-format unification.
- Only move packages or collapse trees after behavior, tests, and compatibility seams are explicit.

## Non-goals for this slice

- Do not replace army gameplay with a city-builder loop.
- Do not promise broad legacy standalone Workers compatibility beyond current contracts.
- Do not force immediate rewrites of every worker entity into recruit-side inheritance or managers.

## Phased Implementation Slices

## Slice 1: Shared Architecture Vocabulary

- Document settlement as the derived operational layer over claims plus work areas.
- Normalize docs, UI text, and future tickets around civilian, military, logistics, and settlement language.
- Add gameplay smoke tests that cover one military flow and one worker flow in the same world.

## Slice 2: Unified Authority And Order Contracts

- Standardize order validation around owner, team, faction-claim, and server authority checks.
- Expose worker actions through the same command-state vocabulary used by recruit commands.
- Add explicit worker assignment and reassignment hardening from the carried Workers backlog.

## Slice 3: Settlement Supply Backbone

- Make storage nodes and build material pulls visible and testable at the BannerMod level.
- Add settlement-level supply status feedback for blocked workers and stalled builders.
- Define how military upkeep and replenishment read local settlement supply without removing existing recruit behavior.

## Slice 4: Military-Settlement Coupling

- Connect claims, patrols, and siege behavior to settlement support values such as food, materials, and labor availability.
- Add guard and escort expectations around civilian zones.
- Allow settlement loss or blockade to affect military readiness in controlled, testable ways.
- Current low-risk implementation step: recruit upkeep now publishes passive food/payment pressure through `BannerModSupplyStatus`, so settlement supply readers can observe one shared military-readiness seam before any deeper patrol, siege, or persistence coupling.

## Slice 5: Persistence And Tree Cleanup

- Decide whether settlement aggregation needs a dedicated `SavedData` manager or can remain derived.
- Merge config file ownership under BannerMod-owned naming before deeper class or save-format rewrites.
- Retire or narrow preserved source-tree seams only after compatibility and test coverage are truthful.

## Slice 6: Root Integrated Gameplay Validation

- Populate the root `gametest` source set with at least one low-risk BannerMod-owned runtime smoke test.
- Prove a recruit-side capability and a worker-side capability survive in the same live `bannermod` GameTest runtime before deeper cross-system world interactions are added.
- Keep the first validation narrow: prefer merged runtime identity, shared packet-channel seams, and root-source-set execution over broad world-behavior assertions.

## End-State Summary

BannerMod should play as a faction-and-warband sandbox where armies win territory, settlements exploit and hold that territory, logistics sustain campaigns, and civilian systems deepen warfare instead of replacing it. The merged architecture should therefore remain recruit-led, server-authoritative, claim-aware, and combat-explicit while using the workers subsystem to supply production, construction, trade, and local settlement identity inside the same runtime.
