# Phase 7 Migration Seam Inventory

This inventory names the highest-risk version-sensitive boundaries that Phase 7 will narrow before the later 1.21.1 port begins. It is intentionally scoped to seam identification and adapter shape only; it is not a full migration checklist.

## Networking

- **Current source anchors:** `src/main/java/com/talhanation/recruits/Main.java`, `src/main/java/com/talhanation/recruits/network/**`
- **Why this is version-sensitive:** `Main.setup()` owns a giant ordered packet list, channel creation, and inline registration against Forge/CoreLib networking APIs that commonly shift across Minecraft and Forge updates.
- **Intended seam / adapter shape:** move ordered packet metadata and channel registration behind `NetworkBootstrapSeams.MessageRegistration` plus a `ChannelRegistrar` contract so later work can preserve wire order while changing bootstrap details in one place.
- **Implemented by later plan:** `07-02-PLAN.md`
- **Behavior that must stay unchanged:** channel name stays `default`, packet ids stay contiguous and stable, and message-side behavior remains exactly as it is now.

## Persistence

- **Current source anchors:** `src/main/java/com/talhanation/recruits/world/RecruitsFactionManager.java`, `src/main/java/com/talhanation/recruits/world/RecruitsTeamSaveData.java`, `src/main/java/com/talhanation/recruits/world/**`
- **Why this is version-sensitive:** manager load/save flows are coupled directly to `SavedData`, dirty-marking, and broadcast side effects, all of which become noisy to re-audit when world-data APIs or serialization hooks change during a port.
- **Intended seam / adapter shape:** capture load/save mutations as `StatePersistenceSeams.SavedDataMutation` contracts so a narrow facade can own apply/dirty/broadcast orchestration without changing faction behavior.
- **Implemented by later plan:** `07-03-PLAN.md`
- **Behavior that must stay unchanged:** team data still reloads into the same manager state, dirty-marking still occurs before broadcast flows complete, and no saved faction state is lost or reordered.

## Client state

- **Current source anchors:** `src/main/java/com/talhanation/recruits/client/ClientManager.java`, `src/main/java/com/talhanation/recruits/client/events/ClientSyncLifecycleEvents.java`
- **Why this is version-sensitive:** `ClientManager` keeps synchronized multiplayer caches in static mutable fields, mixing reset, siege derivation, and route-preservation behavior directly into one Forge-facing client holder.
- **Intended seam / adapter shape:** define `StatePersistenceSeams.ClientSyncReset` as the boundary for resetting synchronized caches while preserving local route data, with derived siege state rebuilt through a focused helper instead of ad hoc map mutation.
- **Implemented by later plan:** `07-03-PLAN.md`
- **Behavior that must stay unchanged:** synchronized remote caches still clear on session boundaries, `routesMap` remains preserved, and active siege state still derives from claim data.

## Compat

- **Current source anchors:** `src/main/java/com/talhanation/recruits/compat/SmallShips.java`, `src/main/java/com/talhanation/recruits/compat/MusketWeapon.java`, `src/main/java/com/talhanation/recruits/compat/**`
- **Why this is version-sensitive:** optional integrations rely on repeated reflection and mod-presence checks whose target class names, methods, and field layouts are likely to drift across future loader and version updates.
- **Intended seam / adapter shape:** centralize optional class discovery behind `CompatPathingSeams.ReflectiveLookup` so compat callers consume one safe-degradation lookup contract instead of open-coded reflection.
- **Implemented by later plan:** `07-04-PLAN.md`
- **Behavior that must stay unchanged:** absent or incompatible optional mods still degrade to null/false/no-op behavior without destabilizing the core mod.

## Pathfinding

- **Current source anchors:** `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathProcessor.java`, `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathfinder.java`
- **Why this is version-sensitive:** async executor startup, fallback processing, and callback handoff sit directly on Minecraft/Forge runtime types and threading assumptions that are risky to touch during a version move.
- **Intended seam / adapter shape:** isolate executor creation and callback delivery behind `CompatPathingSeams.PathRuntime` so future migration work can adjust runtime handoff behavior without rewriting queue and fallback semantics.
- **Implemented by later plan:** `07-04-PLAN.md`
- **Behavior that must stay unchanged:** synchronous fallback still occurs when no executor is available, async processing still hands results back correctly, and Phase 6 safe-degradation guarantees remain intact.

## Registration glue

- **Current source anchors:** `src/main/java/com/talhanation/recruits/Main.java`, `src/main/java/com/talhanation/recruits/init/**`
- **Why this is version-sensitive:** common lifecycle listeners, deferred-register binding, and client/common bootstrap glue are concentrated in `Main`, making even small API migration changes require edits across a large Forge-heavy entrypoint.
- **Intended seam / adapter shape:** define `NetworkBootstrapSeams.LifecycleBinder` as the shared contract for common lifecycle binding so `Main` can delegate setup concerns to narrower helpers.
- **Implemented by later plan:** `07-02-PLAN.md`
- **Behavior that must stay unchanged:** the same listeners, deferred registers, creative-tab hooks, and client-only gating remain registered in the same lifecycle phases.

## Phase 7 Contract Map

- `NetworkBootstrapSeams` covers **Networking** and **Registration glue**.
- `StatePersistenceSeams` covers **Persistence** and **Client state**.
- `CompatPathingSeams` covers **Compat** and **Pathfinding**.

These contracts give later Phase 7 plans one explicit place to implement migration-ready seams instead of rediscovering the current Forge-heavy coupling from scratch.
