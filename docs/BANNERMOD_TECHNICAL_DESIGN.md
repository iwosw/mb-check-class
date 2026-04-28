# BannerMod Technical Design

## Purpose

This document describes the active BannerMod runtime in root `src/**` as it exists today, the major gameplay flows it implements, the invariants it depends on, and the architectural debt that still separates shipped code from the intended game experience.

Runtime truth lives in root `src/**` and `build.gradle`. Legacy `recruits/` and `workers/` trees are reference archives only.

## Runtime Identity

- Mod id: `bannermod`
- Primary entrypoint: `com.talhanation.bannermod.bootstrap.BannerModMain`
- Active gameplay domains:
  - army and recruit command
  - claims and protection
  - settlements, buildings, workers, logistics, and governance
  - war, politics, occupations, revolts, and battle windows
  - client UI, HUD, map, and multiplayer sync

## Design Goals

- Keep gameplay mutations server-authoritative.
- Represent core medieval-sandbox systems in world state, not only in menus or saved records.
- Preserve multiplayer authority rules across packets, UI, and persistence.
- Make player-facing systems understandable in-game without requiring markdown docs.
- Keep large world systems persistent through `SavedData` while using entity state for live behavior.

## High-Level Architecture

```text
Forge bootstrap
  -> BannerModMain
     -> registries, configs, event listeners, network bootstrap, client setup

Client input and UI
  -> keybinds, screens, HUD, world map, overlays
  -> sends packets through the shared channel

Server packet handlers
  -> resolve sender and targets
  -> validate authority
  -> mutate entities, saved state, or command queues

Server runtime loops
  -> recruit lifecycle and combat hooks
  -> claim protection and command queue ticks
  -> settlement snapshot refresh and orchestration
  -> war state updates and dirty snapshot broadcast

Client mirrors
  -> military or claim mirror
  -> war or politics mirror
  -> screens and HUD render current mirrored state
```

## Module Map

- `bootstrap/`: mod entrypoint, setup, merge-era runtime seams.
- `network/`: packet registration and message handlers for military, civilian, and war domains.
- `events/`: large event-driven orchestration surfaces.
- `army/`, `combat/`, `ai/`, `entity/`: recruit control, command intent, pathfinding, combat state, and entity behavior.
- `settlement/`, `citizen/`, `governance/`, `logistics/`: settlement snapshots, jobs, staffing, taxes, stockpiles, and governor logic.
- `war/`: political entities, wars, occupations, revolts, siege state, allies, battle windows, and client sync.
- `persistence/`: `SavedData` wrappers and world-save serialization.
- `client/`: screens, overlays, HUD, key input, render helpers, and client mirrors.
- `registry/`: deferred registers, menu types, entity or item registration, client screen bindings.
- `shared/`: cross-domain helpers, shared policy logic, binding and logistics support.

## Core Runtime Seams

### 1. Command Intent Pipeline

Army commands flow through packet handlers into `CommandIntent` objects and then through `CommandIntentDispatcher` into legacy command services.

Intent kinds currently include movement, face, attack, aggro, strategic fire, stance, and siege-machine related actions. Immediate and queued execution share the same intent vocabulary, but queue parity is not complete yet.

### 2. Settlement Snapshot and Orchestration Split

Settlement runtime is split into two major layers:

- snapshot building: derive a coherent settlement view from claims, workers, containers, buildings, staffing, projects, and logistics
- orchestration: advance work orders, staffing, home assignment, seller dispatch, growth projects, and resident jobs

This split is directionally correct, but each side still carries too many responsibilities.

### 3. War Runtime Facade

War and politics are stored across multiple `SavedData` structures. `WarRuntimeContext` acts as the main access seam that higher-level services use for declarations, occupations, allies, revolt resolution, and client broadcast.

### 4. Client Mirror Pattern

Two important client mirrors exist today:

- military or claim state in `ClientManager`
- war or politics state in `WarClientState`

This pattern keeps screens reactive to synced snapshots, but settlement UIs still rely more on ad hoc server requests than on a coherent mirror.

## State Model

### Persistent State

Persistent world state is mainly stored in `SavedData`:

- claims and claim ownership
- recruit groups and player unit counts
- settlement snapshots and validated buildings
- work orders, seller dispatch, home assignment, project state
- governor ledgers and treasury state
- political entities, wars, occupations, revolts, siege standards, ally invites, cooldowns

### Entity State

Live entities carry runtime behavior such as:

- recruit ownership, stance, aggro, follow or movement state, queue head behavior
- worker carried items, work assignment, route execution, and storage interaction
- citizen conversion and vacancy-filling behavior
- siege-standard health or control state

### Player State

Some command preferences live on `Player.PERSISTED_NBT_TAG`, including saved formation and active group selection.

## Authority Model

### Server Authority

The intended contract is that gameplay mutations happen only after server-side sender validation. Stronger newer paths already follow this model, especially around claim edits and some formation-map commands.

### Current Weak Spots

Authority is inconsistent across older packet handlers. Ownership transfer, teleport, and some civilian owner-mutation flows still need hardening so that the client cannot escalate privileges by forging payload data.

### Authority Roles

Current live logic mixes several concepts:

- direct owner
- same scoreboard team or town-level cooperation
- admin or operator override
- intended nation-level authority, which is only partially implemented

This hierarchy needs one explicit contract shared by UI, packet handlers, and tests.

## Gameplay Flows

### Army and Recruit Control

```text
Player input
  -> command screen, map, or hotkey
  -> packet to server
  -> server resolves actors and authority
  -> packet translated into CommandIntent
  -> dispatcher executes immediately or enqueues
  -> recruit entity state changes
  -> client receives resulting synced state or visual feedback
```

Key expectations:

- formation commands are server-authoritative
- queue mode and immediate mode must preserve the same command semantics
- ownership and team authority must be enforced before mutation

### Settlement Founding and Building Registration

```text
Player claims land or selects settlement space
  -> surveyor validates world zones and building shape
  -> settlement bootstrap creates or binds initial records
  -> validated buildings publish staffing vacancies
  -> citizens or workers fill roles and begin work loops
  -> settlement snapshots and orchestration drive ongoing jobs and growth
```

Key expectations:

- founding depends on real world blocks and zone validation
- starter settlement and auto-bootstrap paths must converge on one coherent lifecycle
- building registration should lead to real staffing and real work, not paper-only records

### Workers, Logistics, and Economy

```text
Settlement state identifies needs
  -> work orders and logistics tasks are published
  -> workers path to storage or work areas
  -> real inventories and containers are mutated
  -> settlement snapshot refreshes supply state
  -> governor or logistics UI surfaces outcomes
```

Key expectations:

- containers and carried items are part of the real simulation
- work-order release, assignment, and failure states remain persistent
- sea trade should eventually move from summary math to materially meaningful flow

### Politics, War, Occupation, and Revolt

```text
Player creates or manages political entity
  -> war declaration validated against cooldowns and authority
  -> battle window, allies, and siege state evolve
  -> occupation or outcomes mutate territory and ledgers
  -> revolts resolve against objectives during battle windows
  -> client mirrors update War Room and HUD
```

Key expectations:

- declarations, outcomes, and revolt state stay server-authoritative
- player UI must expose the real supported flows
- high-level political systems should create visible gameplay consequences, not only saved records

## System Invariants

### Army Invariants

- Server sender identity overrides client-claimed identity.
- Group commands cannot expand beyond server-resolved eligible recruits.
- Formation-sensitive movement uses one authoritative movement-state mapping.
- Queued commands must either execute with the same semantics as immediate commands or be rejected before enqueue.

### Claim Invariants

- Claim protection is Overworld-only.
- Claim mutation requires server-side authority validation.
- Players cannot claim already-owned chunks or exceed configured limits.
- Delete and update flows must resolve persisted claim state server-side.

### Settlement Invariants

- Starter settlements require valid founding geometry.
- Non-starter building registration requires an existing settlement context.
- Staffing and work-order state must survive save or load.
- Friendly claim or settlement binding is a prerequisite for normal civilian operation.

### War Invariants

- Political entity names are validated and unique.
- War declarations respect cooldown, authority, and status gates.
- Occupation requires a different occupier and occupied entity and a non-empty chunk set.
- Pending revolts resolve only through the supported battle-window objective path.

### Sync Invariants

- Client mirrors are disposable caches of server state, not sources of truth.
- Login, logout, and refresh events must clear or repopulate mirrors deterministically.
- Waiting-for-sync, stale-data, and empty-data UI states must stay distinct.

## UI and UX Model

### What Works Well Today

- recruit command surfaces are relatively strong
- War Room and political lists expose a large part of war-state control
- surveyor and building-wand flows at least have dedicated tooltips and message feedback

### Current UX Debt

- too many multi-step flows rely on chat or system messages
- some documented hotkeys or actions are not truly wired end to end
- several screens still expose raw UUIDs, enum internals, or hardcoded English
- some mechanics exist in code and docs but still lack coherent in-game affordances

The target UX is that a player should discover and complete supported gameplay loops in-game, with docs acting as reinforcement rather than as the primary source of truth.

## Performance Notes

- async pathfinding already uses scheduler capacity checks and stale-result rejection
- large recruit crowds still need deeper render optimization beyond current LOD skipping
- formation-map snapshot generation needs bounded scaling under large recruit populations and multiple viewers

## Test Strategy

BannerMod uses a mix of unit tests and GameTests.

- unit tests are best for pure policy, serialization, small runtime helpers, and deterministic state transitions
- GameTests are the primary confidence layer for multiplayer authority, packet paths, claim protection, settlement growth, logistics, and other gameplay scenarios

Green status is not enough by itself. High-risk changes need direct scenario coverage that exercises the real packet or gameplay path, not only helper methods or scenario counts.

## Major Architectural Debt

- oversized event hub classes such as `RecruitEvents` and `ClaimEvents`
- oversized settlement snapshot and orchestration services
- giant shared network bootstrap catalog
- global mutable static runtime managers
- partially retired bootstrap or merge-residue classes
- legacy worker assignment state that competes with newer validated-building records
- incomplete authority normalization across old and new packet handlers

## Target End State

BannerMod should converge on the following shape:

- every player-visible mechanic is actually operable in-game through coherent UI, world interaction, or both
- every high-risk mutation path is server-authoritative and covered by focused tests
- settlements, war, and economy systems produce visible consequences in world play rather than only ledger or registry updates
- client mirrors exist for every major player-facing state surface that needs continuous refresh
- architectural seams are organized by bounded domain rather than by giant event classes or monolithic registration files

## Working Rules For Future Changes

- prefer small, local, verifiable changes over large speculative rewrites
- when adding or changing mechanics, update code, tests, player docs, and backlog acceptance together
- do not mark backlog work done until every acceptance item is backed by explicit verification evidence
- preserve the root `src/**` runtime as the source of truth when docs or legacy trees disagree
