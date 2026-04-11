# Phase 8 Port Inventory

This document is the canonical seam-grounded inventory for the later Minecraft 1.21.1 port. It converts the Phase 6 verification baseline and the Phase 7 seam work into one bounded source map so maintainers can start the port from known anchors instead of rediscovering version-sensitive code by hand.

## Inventory baseline

- **Primary seam references:** `.planning/phases/07-migration-ready-internal-seams/07-SEAM-INVENTORY.md`, `NetworkBootstrapSeams`, `StatePersistenceSeams`, and `CompatPathingSeams`
- **Behavior baseline to preserve:** the stabilized 1.20.1 gameplay and sync behavior locked by Phases 3-6
- **Verification evidence baseline:** `.planning/phases/06-full-surface-verification-and-safe-degradation/06-VERIFICATION.md` and the Phase 7 summary set
- **Accepted debt carried into interpretation:** the two inherited battle-density GameTest failures in `.planning/phases/06-full-surface-verification-and-safe-degradation/deferred-items.md`

## Networking

- **Current source anchors:** `src/main/java/com/talhanation/recruits/Main.java`, `src/main/java/com/talhanation/recruits/network/RecruitsNetworkRegistrar.java`, `src/main/java/com/talhanation/recruits/network/**`
- **Extracted seam/helper files:** `src/main/java/com/talhanation/recruits/migration/NetworkBootstrapSeams.java`, `src/main/java/com/talhanation/recruits/network/RecruitsNetworkRegistrar.java`, `src/test/java/com/talhanation/recruits/network/RecruitsNetworkRegistrarTest.java`
- **Why this surface is version-sensitive:** packet bootstrap, channel creation, and registration APIs are common Forge drift points during version moves, and the mod still depends on strict ordered registration against runtime networking types.
- **Behavior that must remain unchanged:** channel name remains `default`; packet ids stay contiguous and stable; message registration order stays identical; server/client side behavior for each message remains unchanged.
- **Current verification evidence:** `07-02-SUMMARY.md` records sentinel coverage for packet ids and order; `RecruitsNetworkRegistrarTest` locks stable ordering before the port starts.
- **Likely 1.21.1 touchpoints:** `SimpleChannel` setup, message builder or registration APIs, network event/context signatures, packet direction declarations, and any CoreLib interaction used during channel bootstrap.
- **Port note:** use `NetworkBootstrapSeams.MessageRegistration`, `OrderedMessageCatalog`, and `ChannelRegistrar` as the migration boundary so packet wire order can stay fixed while bootstrap code changes underneath.

## Registration glue

- **Current source anchors:** `src/main/java/com/talhanation/recruits/Main.java`, `src/main/java/com/talhanation/recruits/init/ModLifecycleRegistrar.java`, `src/main/java/com/talhanation/recruits/init/**`
- **Extracted seam/helper files:** `src/main/java/com/talhanation/recruits/migration/NetworkBootstrapSeams.java`, `src/main/java/com/talhanation/recruits/init/ModLifecycleRegistrar.java`
- **Why this surface is version-sensitive:** mod bootstrap wiring sits on Forge lifecycle hooks, deferred registers, event bus APIs, and client/common split points that often shift across Minecraft/Forge updates.
- **Behavior that must remain unchanged:** the same listeners still register in the same lifecycle phases; deferred registers still bind; creative-tab hooks and client-only gating still behave exactly as the stabilized branch expects.
- **Current verification evidence:** `07-02-SUMMARY.md` documents that `Main` now delegates to `ModLifecycleRegistrar` without changing gameplay bootstrap behavior.
- **Likely 1.21.1 touchpoints:** event-bus registration signatures, lifecycle event names/timing, deferred-register bootstrap APIs, mod initialization structure, and client bootstrap entrypoints.
- **Port note:** keep `Main` as a thin orchestration shell and adapt `NetworkBootstrapSeams.LifecycleBinder` implementations rather than re-spreading version-specific code back across the entrypoint.

## Persistence

- **Current source anchors:** `src/main/java/com/talhanation/recruits/world/RecruitsFactionManager.java`, `src/main/java/com/talhanation/recruits/world/RecruitsSavedDataFacade.java`, `src/main/java/com/talhanation/recruits/world/RecruitsTeamSaveData.java`, `src/main/java/com/talhanation/recruits/world/**`
- **Extracted seam/helper files:** `src/main/java/com/talhanation/recruits/migration/StatePersistenceSeams.java`, `src/main/java/com/talhanation/recruits/world/RecruitsSavedDataFacade.java`, `src/test/java/com/talhanation/recruits/world/RecruitsSavedDataFacadeTest.java`
- **Why this surface is version-sensitive:** `SavedData` load/save hooks, dirty-marking, and server-side broadcast timing are tightly coupled to world persistence APIs that are likely to drift during the port.
- **Behavior that must remain unchanged:** existing manager state reloads exactly as before; dirty-marking still happens before broadcast completion; no faction/team data is dropped, reordered, or silently defaulted away.
- **Current verification evidence:** `07-03-SUMMARY.md` states that callback ordering is covered in JVM tests; Phase 5 persistence work already locked save/reload behavior before the seam extraction.
- **Likely 1.21.1 touchpoints:** `SavedData` construction/loading APIs, NBT serialization helpers, server-level data access methods, and any lifecycle timing differences around save mutation and sync.
- **Port note:** treat `StatePersistenceSeams.SavedDataMutation` as the stable behavior contract, then only adapt persistence plumbing around it.

## Client state

- **Current source anchors:** `src/main/java/com/talhanation/recruits/client/ClientManager.java`, `src/main/java/com/talhanation/recruits/client/ClientSyncState.java`, `src/main/java/com/talhanation/recruits/client/events/ClientSyncLifecycleEvents.java`, `src/main/java/com/talhanation/recruits/client/**`
- **Extracted seam/helper files:** `src/main/java/com/talhanation/recruits/migration/StatePersistenceSeams.java`, `src/main/java/com/talhanation/recruits/client/ClientSyncState.java`, `src/test/java/com/talhanation/recruits/client/ClientSyncStateTest.java`
- **Why this surface is version-sensitive:** the synchronized multiplayer caches still live in client static state and depend on runtime client lifecycle and packet-driven reset timing that can shift with newer client APIs.
- **Behavior that must remain unchanged:** remote synchronized caches clear on session boundaries; `routesMap` remains preserved; active sieges are rebuilt from claims; the seam does not widen into GUI redesign work.
- **Current verification evidence:** `07-03-SUMMARY.md` records route-preserving reset and active-siege derivation coverage; Phase 5 established the original client reset contract.
- **Likely 1.21.1 touchpoints:** client connection lifecycle events, packet receive timing, client cache invalidation hooks, and any API changes around screen/client state bootstrap.
- **Port note:** keep the later port centered on `StatePersistenceSeams.ClientSyncReset` and `ActiveSiegeTracker` instead of editing many client screens directly.

## Compat

- **Current source anchors:** `src/main/java/com/talhanation/recruits/compat/SmallShips.java`, `src/main/java/com/talhanation/recruits/compat/MusketWeapon.java`, `src/main/java/com/talhanation/recruits/compat/**`
- **Extracted seam/helper files:** `src/main/java/com/talhanation/recruits/migration/CompatPathingSeams.java`, `src/main/java/com/talhanation/recruits/compat/ReflectiveCompatAccess.java`, `src/test/java/com/talhanation/recruits/compat/ReflectiveCompatAccessTest.java`
- **Why this surface is version-sensitive:** optional mod integrations depend on class names, methods, and fields discovered through reflection, all of which may drift independently across loader and game-version updates.
- **Behavior that must remain unchanged:** absent or incompatible mods still degrade to null/false/no-op behavior; compat checks must not destabilize the core mod; this phase does not expand compat scope.
- **Current verification evidence:** Phase 6 added representative safe-degradation coverage; `07-04-SUMMARY.md` records extracted reflective lookup tests and helper rewiring.
- **Likely 1.21.1 touchpoints:** reflected class names, method handles, field names, mod loader availability checks, and any changed optional integration APIs.
- **Port note:** route compat lookup through `CompatPathingSeams.ReflectiveLookup` and `ReflectiveCompatAccess` first, then patch only the drifted reflective details.

## Pathfinding

- **Current source anchors:** `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathProcessor.java`, `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathfinder.java`, `src/main/java/com/talhanation/recruits/pathfinding/PathProcessingRuntime.java`
- **Extracted seam/helper files:** `src/main/java/com/talhanation/recruits/migration/CompatPathingSeams.java`, `src/main/java/com/talhanation/recruits/pathfinding/PathProcessingRuntime.java`, `src/test/java/com/talhanation/recruits/pathfinding/AsyncPathProcessorTest.java`
- **Why this surface is version-sensitive:** async executor creation, fallback delivery, and runtime callback handoff depend on Minecraft path types and thread assumptions that are easy to break during a port.
- **Behavior that must remain unchanged:** synchronous fallback still occurs when no executor is available; async processing still hands results back correctly; Phase 6 safe-degradation guarantees still hold.
- **Current verification evidence:** Phase 6 added async fallback coverage; `07-04-SUMMARY.md` records that runtime extraction preserved queue and fallback semantics.
- **Likely 1.21.1 touchpoints:** path object types, navigation internals, executor handoff expectations, nullable callback signatures, and server-thread/client-thread delivery assumptions.
- **Port note:** use `CompatPathingSeams.PathRuntime` and `PathProcessingRuntime` to adapt runtime details while preserving the existing fallback contract.

## Build, test, and documentation touchpoints

- **Current anchors:** `build.gradle`, `settings.gradle`, `gradle.properties`, `README.md`, `BUILDING.md`, `VERIFICATION_MATRIX.md`, `.planning/phases/06-full-surface-verification-and-safe-degradation/06-VERIFICATION.md`
- **Why these matter to the port:** buildscript versions, Forge dependencies, run configurations, and verification docs define the operational boundary for the later migration and tell maintainers how to interpret failures.
- **Behavior that must remain unchanged:** the port-prep docs do not authorize new gameplay scope; verification interpretation must continue to distinguish real regressions from the already accepted inherited battle-density debt until that debt is explicitly resolved.
- **Current verification evidence:** Phase 1 standardized the canonical Gradle flow, Phase 6 established the matrix/report/deferred-ledger pattern, and Phase 7 summaries identify which seams must preserve behavior during migration.
- **Likely 1.21.1 touchpoints:** Forge coordinates, mappings/toolchain compatibility, run configurations, GameTest execution wiring, and documentation updates for any new canonical commands or accepted-failure interpretation.
- **Port note:** revisit build/test/docs only as support work for the six seam surfaces above; do not widen scope into extra features, extra compat targets, or unrelated cleanup.

## Carry-forward constraints for the later port

1. Preserve the stabilized gameplay and synchronization behavior from Phases 3-6 unless a change is clearly a bug fix forced by the version move.
2. Preserve the seam boundaries introduced in Phase 7 instead of collapsing them back into `Main`, manager classes, or optional-mod callers.
3. Treat `.planning/phases/06-full-surface-verification-and-safe-degradation/deferred-items.md` as interpretation debt, not license to ignore new regressions.
4. Keep the future 1.21.1 effort bounded to version-sensitive migration work; new mechanics, broad compat expansion, and cosmetic redesign remain out of scope.
