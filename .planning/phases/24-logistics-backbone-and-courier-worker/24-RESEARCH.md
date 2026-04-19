# Phase 24 Research — Logistics Backbone And Courier Worker

## Scope Recap

- Add a shared logistics backbone for authored storage endpoints.
- Introduce reservations, filters, stock-threshold vocabulary, and bounded failure handling.
- Implement a courier worker that moves items from source storage to destination storage.
- Surface route authoring, priority, and blocked-state feedback through existing BannerMod UI patterns.

## Existing Seams To Reuse

### Authored storage endpoints
- `src/main/java/com/talhanation/bannermod/entity/civilian/workarea/StorageArea.java`
- Storage areas already provide authored bounds, profession filters, and live container discovery. This is the right Phase 24 source/destination anchor instead of arbitrary global chest search.

### Worker inventory and storage loops
- `src/main/java/com/talhanation/bannermod/ai/civilian/GetNeededItemsFromStorage.java`
- `src/main/java/com/talhanation/bannermod/ai/civilian/DepositItemsToStorage.java`
- These goals already solve nearby chest traversal, open/close timing, and blocked reporting. Courier work should reuse or extract from this logic instead of building a second storage-interaction stack.

### Lightweight blocked-state seam
- `src/main/java/com/talhanation/bannermod/entity/civilian/WorkerStorageRequestState.java`
- Pending complaints already carry compact reason-token plus message state. This matches the logged Phase 24 decision to keep blocked-state reporting bounded and reusable.

### Existing UI/message shape
- `src/main/java/com/talhanation/bannermod/client/civilian/gui/StorageAreaScreen.java`
- `src/main/java/com/talhanation/bannermod/network/messages/civilian/MessageUpdateStorageArea.java`
- Existing worker area authoring uses entity-backed screens and one server packet per edit. The first logistics route UI should follow the same brownfield pattern.

## Recommended Architecture

### 1. Keep logistics server-authoritative and service-shaped
Recommendation: add a narrow logistics service or manager for route lookup, reservation lifecycle, and courier task assignment, but do not create a deep settlement-wide logistics aggregate yet.

Why:
- Phase decisions already lock this slice to a service-shaped boundary.
- Current worker AI is entity-goal driven; a small route service fits that model better than a new simulation engine.

### 2. Model reservations as item intents, not slot locks
Recommendation: reserve `(route, item matcher, requested count, source node, destination node, expiration)` rather than concrete slot indexes.

Why:
- Current containers are resolved late and can change between scans.
- Slot-level locking would pull too much inventory synchronization complexity into the first logistics slice.

### 3. Reuse authored storage/work-area endpoints first
Recommendation: define source and destination nodes in terms of existing storage work areas and their scanned containers.

Why:
- `StorageArea` already exists, is player-authored, and is work-area legal/owned.
- This keeps route authoring explicit and avoids opening the whole world as logistics space.

### 4. Courier tasks should be deterministic and priority-first
Recommendation: when multiple tasks are available, sort by explicit route priority first, then stable tie-breakers such as route id or creation order.

Why:
- The roadmap and state already defer optimization.
- Deterministic selection is easier to validate in tests and easier for players to reason about.

### 5. Use one blocked-state vocabulary across worker and UI paths
Recommendation: define compact tokens for source missing, source empty, destination full, no matching route, reservation expired, and delivery failed.

Why:
- `WorkerStorageRequestState` is already token-based.
- Governor and later economy phases can consume the same vocabulary without re-parsing ad hoc strings.

## Proposed Plan Breakdown

1. **Backbone contracts** — logistics node, route, reservation, and blocked-reason vocabulary.
2. **Server runtime** — route registry/service, reservation cleanup, deterministic task selection, and minimal persistence.
3. **Courier execution** — worker profession/runtime loop for reserve, pick up, deliver, and failure release.
4. **Authoring/UI** — route editing plus blocked-state/player feedback.
5. **Validation** — focused unit tests and GameTests for reservation correctness and live courier delivery.

## Risks And Pitfalls

- Do not widen into treasury, taxes, ports, or trade-route simulation; those belong to Phases 25-26.
- Do not make the courier depend on arbitrary chest scanning with no authored route boundary.
- Do not add slot-level reservation logic that fights normal container mutation.
- Do not let blocked-state feedback become chat spam or one-off strings that later phases cannot reuse.

## Validation Direction

- Unit coverage should prove deterministic task ordering, reservation timeout/release, and filter matching.
- Root GameTests should prove a courier can move items between two authored storage endpoints under real runtime behavior.
- Keep validation additive on existing worker/GameTest support rather than building a logistics-only harness.
