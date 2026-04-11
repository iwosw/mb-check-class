# Phase 8 Bounded Follow-Up Brief

This brief is the handoff proof that the future Minecraft 1.21.1 port is now a bounded follow-up effort. The work no longer starts from repo exploration; it starts from `08-PORT-INVENTORY.md`, `08-VERSION-SURFACE-MAP.md`, and `08-PORT-CHECKLIST.md`.

## Why the port is now bounded

- The migration surface has already been reduced to three named workstreams instead of a repo-wide unknown.
- Each workstream already has concrete entry files, seam contracts, preserved behaviors, and likely drift categories.
- Phase 6 already defines how to interpret verification output, including the limited accepted debt that can remain red without implying a new migration regression.
- Phase 7 already extracted the main high-risk seams, so the future port can adapt bounded helpers instead of rediscovering Forge-heavy coupling from scratch.

## Starting points

Start future port work in this order:

1. `08-PORT-INVENTORY.md` — source-anchor inventory for networking, registration glue, persistence, client state, compat, pathfinding, and support surfaces
2. `08-VERSION-SURFACE-MAP.md` — bounded workstream grouping and drift categories
3. `08-PORT-CHECKLIST.md` — ordered executable checklist with verification commands and exit criteria

Primary code entry files by workstream:

- **Bootstrap/networking:** `src/main/java/com/talhanation/recruits/Main.java`, `src/main/java/com/talhanation/recruits/network/RecruitsNetworkRegistrar.java`, `src/main/java/com/talhanation/recruits/init/ModLifecycleRegistrar.java`
- **Persistence/client sync:** `src/main/java/com/talhanation/recruits/world/RecruitsSavedDataFacade.java`, `src/main/java/com/talhanation/recruits/world/RecruitsFactionManager.java`, `src/main/java/com/talhanation/recruits/client/ClientSyncState.java`, `src/main/java/com/talhanation/recruits/client/ClientManager.java`
- **Compat/path/runtime support:** `src/main/java/com/talhanation/recruits/compat/ReflectiveCompatAccess.java`, `src/main/java/com/talhanation/recruits/pathfinding/PathProcessingRuntime.java`, `build.gradle`, `VERIFICATION_MATRIX.md`

## Preserved contracts

The future port should preserve these Phase 7 seam contracts and use them as the only intended migration boundaries:

- `NetworkBootstrapSeams.OrderedMessageCatalog`
- `NetworkBootstrapSeams.ChannelRegistrar`
- `NetworkBootstrapSeams.LifecycleBinder`
- `StatePersistenceSeams.ClientSyncReset`
- `StatePersistenceSeams.ActiveSiegeTracker`
- `StatePersistenceSeams.SavedDataMutation`
- `CompatPathingSeams.ReflectiveLookup`
- `CompatPathingSeams.PathRuntime`

Preserved behavior rules attached to those contracts:

- packet order, packet ids, and channel identity remain stable
- synchronized client resets still preserve `routesMap`
- active sieges still derive from claim data
- dirty-marking and broadcast ordering remain intact
- absent compat targets still degrade safely
- async path fallback remains intact when executors are unavailable

## Accepted carry-forward debt

The only accepted carry-forward debt from Phase 6 is the inherited battle-density GameTest debt documented in `.planning/phases/06-full-surface-verification-and-safe-degradation/deferred-items.md` and summarized in `06-VERIFICATION.md`:

- `baselinedensebattlecompleteswithoutbrokenloops`
- `heavierdensebattlecompleteswithoutbrokenloops`

These failures are not part of the desired port result, but they are the only documented cases that may remain red without automatically proving the 1.21.1 migration introduced a new regression. Anything outside those scenarios should be treated as new until verified otherwise.

## Out of scope

The future port remains bounded by the project requirements and must not expand into:

- full new gameplay content, units, factions, or mechanics
- broad compatibility expansion beyond the currently supported integrations
- broad UI polish unrelated to correctness or diagnostics
- unrelated cleanup that is not needed to complete the version-sensitive migration

This scope line is already reflected in `08-PORT-INVENTORY.md` and `08-PORT-CHECKLIST.md` and should remain explicit throughout the later port plan.

## Definition of done

The follow-up port effort can be considered bounded and ready to close MIG-03 when all of the following are true:

- the maintainer can work entirely from `08-PORT-INVENTORY.md`, `08-VERSION-SURFACE-MAP.md`, and `08-PORT-CHECKLIST.md` without reopening broad repo discovery
- all three named workstreams have explicit entry files, preserved behavior contracts, and verification expectations
- Phase 7 seams remain the active migration boundaries instead of collapsing back into direct Forge-heavy coupling
- verification can be interpreted through the Phase 6 baseline with accepted carry-forward debt clearly separated from new regressions
- the documented out-of-scope boundaries remain intact, proving the later 1.21.1 port is a bounded technical follow-up instead of a broad new project
