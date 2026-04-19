# Phase 24: Logistics Backbone And Courier Worker - Context

**Gathered:** 2026-04-18
**Status:** Ready for planning

<domain>
## Phase Boundary

Turn BannerMod's profession-local storage behavior into one explicit logistics backbone with authored storage endpoints, lightweight reservations, deterministic courier tasks, and player-visible blocked-state feedback. This phase stays server-authoritative and additive over the current worker/storage/work-area runtime; it does not introduce a settlement-wide logistics manager, global economy simulation, arbitrary chest discovery outside authored endpoints, or Phase 25/26 treasury and trade systems.

</domain>

<decisions>
## Implementation Decisions

### Runtime Shape
- **D-01:** Keep the first logistics slice service-shaped and server-authoritative instead of introducing a deep settlement logistics manager.
- **D-02:** Reuse authored entity-backed endpoints first: existing `StorageArea` work areas plus explicit courier-facing source/destination node records.
- **D-03:** Keep logistics identity narrow and route-oriented: one route connects a source endpoint to a destination endpoint for a filtered item intent.

### Reservations And Failure Handling
- **D-04:** Reservations stay lightweight item intents with timeout and cleanup semantics, not slot-level locks or transactional inventory ownership.
- **D-05:** Blocked-state reporting must use bounded reason tokens/messages so workers and UI can surface shortage, no-container, full-destination, and timeout failures consistently.
- **D-06:** The first slice prefers deterministic priority-first task selection over global optimization.

### Courier Profession
- **D-07:** Courier behavior should follow a simple loop: claim a routeable task, reserve requested items, travel to source, collect, travel to destination, deliver, and release or fail the reservation.
- **D-08:** Courier worker gameplay should reuse existing worker ownership, authority, navigation, and inventory patterns instead of introducing a second civilian-control model.

### Player Surface
- **D-09:** The first authoring surface may stay close to existing work-area UI patterns and packet flows; it does not need a giant economy console.
- **D-10:** Players must be able to see route priority/filter intent and blocked-state feedback without reading logs or debugging output.

### Claude's Discretion
- Exact class names for logistics route, node, reservation, and task records.
- Exact route-screen layout and packet granularity, as long as it follows existing BannerMod screen/message patterns.
- Exact reservation timeout values and retry cadence, as long as they remain deterministic and server-owned.

</decisions>

<code_context>
## Existing Code Insights

### Reusable Assets
- `src/main/java/com/talhanation/bannermod/entity/civilian/workarea/StorageArea.java` already defines authored storage work areas, storage-type filters, container scanning, and worker compatibility checks.
- `src/main/java/com/talhanation/bannermod/ai/civilian/GetNeededItemsFromStorage.java` already has worker-side storage scanning, chest traversal, blocked reporting, and last-storage reuse.
- `src/main/java/com/talhanation/bannermod/ai/civilian/DepositItemsToStorage.java` already has delivery-side chest selection, full-container handling, and blocked reporting.
- `src/main/java/com/talhanation/bannermod/entity/civilian/WorkerStorageRequestState.java` already provides one lightweight pending-complaint seam suitable for reservation/blocking vocabulary.
- `src/main/java/com/talhanation/bannermod/client/civilian/gui/StorageAreaScreen.java` plus `MessageUpdateStorageArea` show the current worker UI/message pattern for authored area configuration.

### Established Patterns
- Worker behaviors in this repo usually stay entity-goal-driven and reuse work-area discovery instead of central simulation managers.
- Brownfield server authority is preserved through narrow service or helper seams plus explicit packets/screens rather than broad client-owned state.
- Current storage interactions discover real containers late and report compact blocked reasons back through the worker entity.

### Integration Points
- `AbstractWorkerEntity` already wires storage deposit/get goals and keeps `lastStorage` plus `WorkerStorageRequestState`, making it the natural courier/route integration point.
- Shared logistics status already exists under `com.talhanation.bannermod.shared.logistics`, so Phase 24 can extend logistics vocabulary without forking unrelated authority or settlement logic.
- Governor/settlement phases already expect additive logistics follow-up, so claim-aware helpers and future heartbeat reporting should consume one route/reservation seam.

</code_context>

<specifics>
## Specific Ideas

- Start from authored `StorageArea` endpoints rather than trying to solve arbitrary world logistics in one phase.
- Make route execution deterministic enough that later treasury/supply phases can depend on it.
- Prefer one reusable blocked-state vocabulary over ad hoc courier-only chat messages.

</specifics>

<deferred>
## Deferred Ideas

- Treasury accounting, army upkeep drains, and settlement-wide economic balancing.
- Global route optimization, convoy simulation, ports, and maritime logistics.
- Arbitrary non-authored chest discovery as the primary logistics model.

</deferred>
