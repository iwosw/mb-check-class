# Phase 8 Version Surface Map

This map turns `08-PORT-INVENTORY.md` into bounded future-port work packages. It is not the port itself. It is the reviewable breakdown maintainers can use to execute the later Minecraft 1.21.1 migration without reopening full-repo exploration.

## Framing assumptions

- The future port starts from the stabilized 1.20.1 behavior and seam boundaries documented in Phase 6 and Phase 7.
- `08-PORT-INVENTORY.md` is the source inventory for all work packages below.
- `.planning/phases/06-full-surface-verification-and-safe-degradation/06-VERIFICATION.md` remains the interpretation baseline for automated results.
- `.planning/phases/06-full-surface-verification-and-safe-degradation/deferred-items.md` continues to define the accepted inherited battle-density debt that must not be mistaken for new port regressions.

## Work package 1

### Scope

**Networking and registration bootstrap migration**

- **Related inventory sections:** `## Networking`, `## Registration glue` in `08-PORT-INVENTORY.md`
- **Expected files/packages to touch:** `src/main/java/com/talhanation/recruits/Main.java`, `src/main/java/com/talhanation/recruits/network/RecruitsNetworkRegistrar.java`, `src/main/java/com/talhanation/recruits/network/**`, `src/main/java/com/talhanation/recruits/init/ModLifecycleRegistrar.java`, `src/main/java/com/talhanation/recruits/migration/NetworkBootstrapSeams.java`
- **Likely API drift category:** Forge mod bootstrap and networking registration drift
- **Behavior/verification constraints to preserve:** packet ids and order remain stable; channel naming remains stable; client/common lifecycle registration timing remains unchanged from the stabilized branch; `RecruitsNetworkRegistrarTest` should continue to define the wire-order contract.
- **Port execution focus:** adapt event-bus, lifecycle, deferred-register, and channel registration calls behind existing registrar helpers rather than rewriting packet behavior or scattering version-specific code back into `Main`.
- **Bounded completion signal:** bootstrap compiles and routes through the existing seam helpers with packet-order tests still matching the old contract.
- **Accepted debt interpretation:** the Phase 6 battle-density debt does **not** directly block this package, but red canonical runs must still be checked against `.planning/phases/06-full-surface-verification-and-safe-degradation/deferred-items.md` before treating failures as new.

## Work package 2

### Scope

**Persistence and client synchronization migration**

- **Related inventory sections:** `## Persistence`, `## Client state` in `08-PORT-INVENTORY.md`
- **Expected files/packages to touch:** `src/main/java/com/talhanation/recruits/world/RecruitsFactionManager.java`, `src/main/java/com/talhanation/recruits/world/RecruitsSavedDataFacade.java`, `src/main/java/com/talhanation/recruits/world/RecruitsTeamSaveData.java`, `src/main/java/com/talhanation/recruits/world/**`, `src/main/java/com/talhanation/recruits/client/ClientManager.java`, `src/main/java/com/talhanation/recruits/client/ClientSyncState.java`, `src/main/java/com/talhanation/recruits/client/events/ClientSyncLifecycleEvents.java`, `src/main/java/com/talhanation/recruits/migration/StatePersistenceSeams.java`
- **Likely API drift category:** world persistence and client session lifecycle drift
- **Behavior/verification constraints to preserve:** `routesMap` remains preserved across synchronized resets; active sieges still derive from claim data; saved team/faction state still reloads without dropped or reordered data; dirty-marking and broadcast ordering remain intact; Phase 5 persistence behavior remains the contract baseline.
- **Port execution focus:** adapt `SavedData` and client lifecycle plumbing around `StatePersistenceSeams` and the extracted helpers instead of changing manager semantics or widening into GUI redesign.
- **Bounded completion signal:** seam helper tests still pass conceptually, and runtime migration changes remain localized to persistence/client sync plumbing rather than spreading across unrelated gameplay files.
- **Accepted debt interpretation:** Phase 6 battle-density debt is mostly orthogonal here, but if canonical verification stays red only on the known stress scenarios from `06-VERIFICATION.md`, this package should still be interpreted against that accepted baseline.

## Work package 3

### Scope

**Compat, path runtime, and support-surface migration**

- **Related inventory sections:** `## Compat`, `## Pathfinding`, `## Build, test, and documentation touchpoints` in `08-PORT-INVENTORY.md`
- **Expected files/packages to touch:** `src/main/java/com/talhanation/recruits/compat/ReflectiveCompatAccess.java`, `src/main/java/com/talhanation/recruits/compat/SmallShips.java`, `src/main/java/com/talhanation/recruits/compat/MusketWeapon.java`, `src/main/java/com/talhanation/recruits/compat/**`, `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathProcessor.java`, `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathfinder.java`, `src/main/java/com/talhanation/recruits/pathfinding/PathProcessingRuntime.java`, `src/main/java/com/talhanation/recruits/migration/CompatPathingSeams.java`, `build.gradle`, `settings.gradle`, `gradle.properties`, `BUILDING.md`, `VERIFICATION_MATRIX.md`
- **Likely API drift category:** optional-integration reflection drift, path/navigation runtime drift, and build/test support drift
- **Behavior/verification constraints to preserve:** absent compat targets still degrade safely; async path fallback still works when no executor is available; callback delivery behavior stays intact; support docs continue to explain how to interpret accepted inherited debt and canonical verification results.
- **Port execution focus:** update reflected names and runtime path plumbing through the extracted helpers, then revise build/test/docs only as needed to support the migrated code path.
- **Bounded completion signal:** representative compat-safe-degradation and async-runtime coverage still map to the same behavior contracts, and the build/test docs reflect the new canonical commands without broadening port scope.
- **Accepted debt interpretation:** this package is the most likely to intersect final verification runs, so `06-VERIFICATION.md` and `deferred-items.md` must be consulted before classifying any battle-stress failures as newly introduced by the port.

## Verification and debt carry-forward

- Use `08-PORT-INVENTORY.md` as the surface checklist and `.planning/phases/06-full-surface-verification-and-safe-degradation/06-VERIFICATION.md` as the expected verification interpretation baseline.
- Treat `.planning/phases/06-full-surface-verification-and-safe-degradation/deferred-items.md` as the authoritative list of accepted inherited debt during the port unless later work explicitly resolves those scenarios.
- Preserve the Phase 7 rule that migration changes should flow through `NetworkBootstrapSeams`, `StatePersistenceSeams`, and `CompatPathingSeams` instead of re-coupling code to the broad Forge-heavy entrypoints.
- Do not widen any package into new gameplay, broad compat expansion, or unrelated cleanup; if a later task cannot fit inside one of the three packages above, it likely belongs in a separate future plan rather than the bounded 1.21.1 port follow-up.
- Interpret canonical check results by separating three buckets: green migration results, accepted inherited battle-density debt, and genuinely new regressions introduced by the version move.
