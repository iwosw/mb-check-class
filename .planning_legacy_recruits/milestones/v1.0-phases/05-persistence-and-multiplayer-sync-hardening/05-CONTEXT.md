# Phase 5: Persistence and Multiplayer Sync Hardening - Context

**Gathered:** 2026-04-07
**Status:** Ready for planning

<domain>
## Phase Boundary

Harden the highest-risk persisted world state and multiplayer synchronization flows so critical recruit, team, faction, claim, route-assignment, group, diplomacy, treaty, and player-unit behavior survives restart and remains synchronized under server authority. This phase should tighten restart/reload and packet-side correctness without turning into a full route-library redesign or broad client architecture rewrite.

</domain>

<decisions>
## Implementation Decisions

### Persistence Scope and Ownership
- Prioritize highest-risk server-owned state first: teams, factions, claims, groups, diplomacy, treaties, and player-unit state.
- Include server-owned patrol route assignment and leader entity persistence, but do not treat the client-local editable route library as world-save state for this phase.
- Make dirty-marking and save-trigger behavior explicit for prioritized managers with tests around mutation, dirty state, and reload.
- Treat restart-sensitive lazy or default data creation as something to lock down with tests where it affects correctness.

### Multiplayer Sync Contract
- Prioritize packet-driven flows that mutate critical shared state or client caches: claims, factions, groups, diplomacy, treaties, player-unit info, and route-assignment outcomes.
- Treat `ClientManager` cache lifecycle and stale-cache reset behavior as in scope where it affects correctness or desync risk.
- Invalid, boundary, and side-sensitive packets should enforce server-authoritative safe degradation with explicit tests.
- Tests should lock down observable synchronized outcomes and cache correctness across join, update, and reload boundaries rather than every internal cache mutation detail.

### Test Strategy for Persistence and Networking
- Use JVM tests for `SavedData` round trips, dirty-marking, and packet validation seams; use GameTests for join, reload, and live-sync behavior.
- Restart verification should cover the highest-risk restart and reload scenarios with explicit round-trip checks and representative runtime re-sync flows, not exhaustive world migration simulation.
- Leave behind reusable persistence and networking scenario helpers that Phase 6 full-surface verification can extend.
- Phase 5 verification should verify its own truths and explicitly attribute any remaining red full-suite checks to previously accepted external failures rather than being blocked by unrelated legacy gaps.

### Scope Boundaries
- Full client route-library redesign, broad UI cache refactors, and unrelated combat or pathfinding optimization stay out of scope unless directly blocking persistence or sync correctness.
- Administrative or world-edit mutation packets belong in scope when they directly affect critical persisted or synchronized state, but this phase should not normalize every admin packet in one sweep.
- Narrow manager, save, and sync seam extraction is allowed when it reduces restart or desync risk, but broad storage or network rewrites are out of scope.
- Preserve outward behavior unless restart or desync testing exposes a clear correctness fault, then fix it deliberately with regression coverage.

### the agent's Discretion
- Exact subsystem ordering, helper names, and scenario breakdown, as long as the locked persistence, sync, and scope rules above hold.
- Exact diagnostic wording and reset strategy details, provided stale or invalid flows become observable and safely bounded.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase Definition
- `.planning/ROADMAP.md` — Phase 5 goal and success criteria for restart-safe persistence and multiplayer synchronization.
- `.planning/REQUIREMENTS.md` — `DATA-01` through `DATA-04`, which define persistence round trips, restart safety, prioritized multiplayer sync, and invalid/side-sensitive packet enforcement.

### Prior Decisions
- `.planning/phases/04-command-and-ai-state-stabilization/04-CONTEXT.md` — preserves server-authoritative packet handling and safe degradation for invalid flows.
- `.planning/phases/03-battle-and-formation-regression-lockdown/03-CONTEXT.md` — keeps command/behavior assertions focused on observable outcomes rather than private implementation details.
- `.planning/phases/02-layered-test-harness-foundations/02-CONTEXT.md` — keeps the split JVM/GameTest harness as the base for new persistence and sync verification.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `src/test/java/com/talhanation/recruits/testsupport/NbtRoundTripAssertions.java` and `RecruitsFixtures.java` already provide a starting point for persistence round-trip tests.
- Existing serialization tests for factions and claims already cover part of the world-state surface.
- Phase 4 packet validation tests and GameTest command harnesses provide reusable patterns for side-sensitive and runtime synchronization coverage.

### Established Patterns
- Critical world state mostly uses `SavedData` classes wrapped by manager objects that mutate in-memory state, mark dirty, and broadcast sync packets.
- Player join is the main resynchronization moment for several client caches.
- `ClientManager` is a static cache mirror updated by dedicated client packets, which makes reset/staleness behavior an important correctness seam.
- Patrol route assignment is split between server-owned leader/entity state and a client-local route library, so ownership boundaries matter.

### Integration Points
- `src/main/java/com/talhanation/recruits/world/Recruits*SaveData.java`
- `src/main/java/com/talhanation/recruits/world/Recruits*Manager.java`
- `src/main/java/com/talhanation/recruits/client/ClientManager.java`
- `src/main/java/com/talhanation/recruits/network/MessageToClientUpdate*.java`
- `src/main/java/com/talhanation/recruits/network/MessagePatrolLeaderSetRoute.java`
- `src/main/java/com/talhanation/recruits/network/MessageTransferRoute.java`
- `src/main/java/com/talhanation/recruits/network/MessageToClientReceiveRoute.java`
- `src/main/java/com/talhanation/recruits/entities/AbstractLeaderEntity.java`
- `src/main/java/com/talhanation/recruits/FactionEvents.java`
- `src/main/java/com/talhanation/recruits/RecruitEvents.java`
- `src/main/java/com/talhanation/recruits/ClaimEvents.java`

</code_context>

<specifics>
## Specific Ideas

- Treat server-owned world state and server-owned route assignment as the core persistence contract, not the client-local editable route library.
- Make dirty-marking and restart behavior explicit through tests instead of assuming later saves will rescue correctness.
- Treat stale client cache lifecycle as real sync debt when it can produce observable desync.

</specifics>

<deferred>
## Deferred Ideas

- Full client route-library redesign.
- Broad UI/client cache architecture cleanup beyond correctness-critical stale-state handling.
- Unrelated rendering, target-acquisition, and pathfinding optimization work.

</deferred>

---

*Phase: 05-persistence-and-multiplayer-sync-hardening*
*Context gathered: 2026-04-07*
