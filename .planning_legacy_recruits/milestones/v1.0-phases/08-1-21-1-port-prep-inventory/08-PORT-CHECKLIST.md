# Phase 8 Port Checklist

This is the ordered execution checklist for the future Minecraft 1.21.1 port. It is derived from `08-PORT-INVENTORY.md` and `08-VERSION-SURFACE-MAP.md` so the port can start from known seams, work packages, and verification rules instead of fresh repository exploration.

## Preconditions

- [ ] Read `08-PORT-INVENTORY.md` in full before changing port-sensitive code.
- [ ] Read `08-VERSION-SURFACE-MAP.md` and keep the three work packages as the active migration scope.
- [ ] Confirm the preserved seam contracts remain the migration entrypoints: `NetworkBootstrapSeams.OrderedMessageCatalog`, `NetworkBootstrapSeams.ChannelRegistrar`, `NetworkBootstrapSeams.LifecycleBinder`, `StatePersistenceSeams.ClientSyncReset`, `StatePersistenceSeams.ActiveSiegeTracker`, `StatePersistenceSeams.SavedDataMutation`, `CompatPathingSeams.ReflectiveLookup`, and `CompatPathingSeams.PathRuntime`.
- [ ] Reconfirm the accepted debt baseline in `.planning/phases/06-full-surface-verification-and-safe-degradation/06-VERIFICATION.md` and `.planning/phases/06-full-surface-verification-and-safe-degradation/deferred-items.md` before interpreting any red GameTest or `check` results.
- [ ] Keep the port in scope: no new gameplay, no broad compat expansion, and no unrelated polish.
- [ ] Treat the existing stabilized 1.20.1 behavior from Phases 3-6 as the default contract unless a version-mandated bug fix is explicitly documented.

## Recommended workstream order

1. **Work package 1:** networking and registration bootstrap migration
2. **Work package 2:** persistence and client synchronization migration
3. **Work package 3:** compat, path runtime, and support-surface migration

This order preserves the Phase 7 seam layering: bootstrap first, stateful server/client flows second, and optional/runtime support surfaces last.

## Ordered workstream checklist

### Work package 1 — Networking and registration bootstrap migration

Reference: `08-VERSION-SURFACE-MAP.md` Work package 1

- [ ] Use `src/main/java/com/talhanation/recruits/network/RecruitsNetworkRegistrar.java` as the starting point for packet registration changes.
- [ ] Keep `src/main/java/com/talhanation/recruits/Main.java` thin and route version-specific lifecycle updates through `src/main/java/com/talhanation/recruits/init/ModLifecycleRegistrar.java`.
- [ ] Preserve `NetworkBootstrapSeams.OrderedMessageCatalog` as the single ordered packet inventory.
- [ ] Preserve `NetworkBootstrapSeams.ChannelRegistrar` as the registration boundary while adapting any 1.21.1 channel API drift.
- [ ] Preserve `NetworkBootstrapSeams.LifecycleBinder` as the lifecycle hookup boundary instead of re-inlining Forge-heavy setup into `Main`.
- [ ] Keep the channel name `default`, stable packet ids, and existing registration order unchanged.
- [ ] Re-run packet-order regression coverage before moving to the next work package so wire-order drift is caught immediately.
- [ ] Treat any red canonical run here as a possible new regression unless it matches the accepted debt already listed in Phase 6.

**Primary touchpoints:** `src/main/java/com/talhanation/recruits/Main.java`, `src/main/java/com/talhanation/recruits/network/**`, `src/main/java/com/talhanation/recruits/init/**`, `src/main/java/com/talhanation/recruits/migration/NetworkBootstrapSeams.java`

**Preserved behavior constraints:** packet ids remain contiguous and stable, message-side behavior does not change, lifecycle registration timing stays consistent with the stabilized branch.

### Work package 2 — Persistence and client synchronization migration

Reference: `08-VERSION-SURFACE-MAP.md` Work package 2

- [ ] Use `src/main/java/com/talhanation/recruits/world/RecruitsSavedDataFacade.java` and `src/main/java/com/talhanation/recruits/world/RecruitsFactionManager.java` as the migration entrypoints for persistence changes.
- [ ] Adapt `SavedData` and related world-data plumbing around `StatePersistenceSeams.SavedDataMutation` rather than changing mutation semantics.
- [ ] Use `src/main/java/com/talhanation/recruits/client/ClientSyncState.java` and `src/main/java/com/talhanation/recruits/client/ClientManager.java` as the entrypoints for client sync migration.
- [ ] Preserve `StatePersistenceSeams.ClientSyncReset` so synchronized remote caches clear without losing `routesMap`.
- [ ] Preserve `StatePersistenceSeams.ActiveSiegeTracker` so active sieges continue to rebuild from claim data.
- [ ] Keep dirty-marking and broadcast ordering intact when adapting persistence hooks.
- [ ] Keep the port out of GUI redesign territory; only migrate lifecycle and cache-reset plumbing needed for correctness.
- [ ] Compare any remaining red canonical verification results against accepted debt before classifying them as new persistence/client regressions.

**Primary touchpoints:** `src/main/java/com/talhanation/recruits/world/**`, `src/main/java/com/talhanation/recruits/client/**`, `src/main/java/com/talhanation/recruits/migration/StatePersistenceSeams.java`

**Preserved behavior constraints:** saved state still reloads cleanly, synchronized resets preserve local routes, and client siege derivation remains claim-driven.

### Work package 3 — Compat, path runtime, and support-surface migration

Reference: `08-VERSION-SURFACE-MAP.md` Work package 3

- [ ] Use `src/main/java/com/talhanation/recruits/compat/ReflectiveCompatAccess.java` as the first stop for optional integration drift.
- [ ] Preserve `CompatPathingSeams.ReflectiveLookup` as the compatibility lookup boundary while updating reflected names, methods, or fields only where drift requires it.
- [ ] Use `src/main/java/com/talhanation/recruits/pathfinding/PathProcessingRuntime.java` and `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathProcessor.java` as the path runtime migration entrypoints.
- [ ] Preserve `CompatPathingSeams.PathRuntime` so executor creation and callback delivery stay isolated from queue and fallback semantics.
- [ ] Keep absent optional mods on null/false/no-op degradation paths instead of widening integration behavior.
- [ ] Keep synchronous fallback behavior intact when no executor is available.
- [ ] Update support surfaces (`build.gradle`, `settings.gradle`, `gradle.properties`, `BUILDING.md`, `VERIFICATION_MATRIX.md`) only as needed to support the migrated seam work.
- [ ] Recheck the accepted battle-density debt after final verification so inherited failures are not mislabeled as new 1.21.1 regressions.

**Primary touchpoints:** `src/main/java/com/talhanation/recruits/compat/**`, `src/main/java/com/talhanation/recruits/pathfinding/**`, `src/main/java/com/talhanation/recruits/migration/CompatPathingSeams.java`, build/test/documentation files named above

**Preserved behavior constraints:** optional compat remains safely degradable, async path processing keeps current fallback semantics, and documentation continues to distinguish accepted debt from new failures.

## Verification commands

Run these in order after major milestones and again at the end of the port:

1. `./gradlew test --continue`
2. `./gradlew runGameTestServer --continue`
3. `./gradlew check --continue`

Interpret them using the Phase 6 baseline:

- `./gradlew test --continue` should reflect JVM and helper-level seam coverage.
- `./gradlew runGameTestServer --continue` may still show the accepted debt scenarios from Phase 6; compare failures to `.planning/phases/06-full-surface-verification-and-safe-degradation/deferred-items.md`.
- `./gradlew check --continue` should only be treated as acceptably red if the remaining failures match the same accepted debt and no new regressions appear.

## Accepted debt interpretation during the port

- The accepted debt is limited to the two inherited battle-density GameTests documented in Phase 6.
- Those failures are interpretation guidance, not blanket permission to ignore new issues.
- If a failure appears outside the documented battle-density scenarios, treat it as new until proven otherwise.
- If the known accepted debt disappears during the port, remove it from the carry-forward interpretation set rather than preserving stale exceptions.

## Exit criteria for calling the port checklist complete

- [ ] All three work packages from `08-VERSION-SURFACE-MAP.md` have been addressed through their existing seam boundaries.
- [ ] Preserved behavior constraints from `08-PORT-INVENTORY.md` still hold after migration changes.
- [ ] Verification results are documented against the Phase 6 baseline with explicit distinction between green results, accepted debt, and new regressions.
- [ ] The final state still respects Phase 8 scope boundaries and project out-of-scope rules.
