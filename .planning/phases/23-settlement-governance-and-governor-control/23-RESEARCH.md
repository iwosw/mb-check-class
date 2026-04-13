# Phase 23 Research — Settlement Governance And Governor Control

## Scope Recap

- Add a real governor role instead of the dormant governor promotion slot.
- Let governors operate on top of the existing claim-derived settlement model.
- Surface bounded governance outputs now: local tax collection state, garrison guidance, fortification suggestions, and incident/shortage reporting.
- Avoid pulling full treasury, army-upkeep, trade, or logistics-route systems forward from Phases 24-26.

## Existing Seams To Reuse

### Settlement legality and degradation
- `src/main/java/com/talhanation/bannermod/settlement/BannerModSettlementBinding.java`
- The phase should keep settlement identity claim-derived and keyed to friendly claims or degraded mismatches instead of introducing a second world model.

### Ownership and authority
- `src/main/java/com/talhanation/bannermod/authority/BannerModAuthorityRules.java`
- Governor assignment and governor-control actions should reuse owner / same-team / admin vocabulary instead of inventing a governance-only permission system.

### Citizen and wrapper convergence
- `src/main/java/com/talhanation/bannermod/citizen/CitizenRoleController.java`
- `recruits/src/main/java/com/talhanation/recruits/entities/AbstractRecruitEntity.java`
- `workers/src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java`
- Phase 22 created a shared citizen seam, but governor should remain an additive designation layered onto the existing recruit wrapper path rather than forcing a new entity base or widening `CitizenRole` beyond wrapper identity.

### Claim-backed settlement population and growth
- `workers/src/main/java/com/talhanation/workers/VillagerEvents.java`
- `workers/src/main/java/com/talhanation/workers/settlement/WorkerSettlementSpawnRules.java`
- These already provide claim-aware worker growth and settlement participation rules the governor phase can observe.

### Existing placeholder UI seam
- `recruits/src/main/java/com/talhanation/recruits/client/gui/PromoteScreen.java`
- `recruits/src/main/java/com/talhanation/recruits/network/MessagePromoteRecruit.java`
- `recruits/src/main/java/com/talhanation/recruits/RecruitEvents.java`
- The governor button already exists but is hard-disabled. This is the correct low-risk entrypoint for promotion into a real governor designation.

## Recommended Architecture

### 1. Governor is a designation, not a new recruit entity type
Recommendation: do **not** create a new `GovernorEntity` in Phase 23.

Why:
- No governor entity type exists today.
- The current placeholder lives in the promotion UI, but the existing promotion pipeline is entity-transformation-based and only supports concrete companion classes already registered.
- A governance designation attached to an existing recruit/citizen UUID is lower-risk, keeps runtime ids stable, and fits the Phase 22 additive-seam direction.

Implication:
- Promotion id `6` should stop being a dead button and instead call a governance assignment path that designates the selected recruit as the claim's governor.

### 2. Keep settlement governance keyed to claim UUID / claim center
Recommendation: use a lightweight governance manager keyed by `RecruitsClaim.getUUID()`.

Why:
- BannerMod already decided settlements remain claim-derived until code pressure justifies a dedicated settlement manager.
- Governance needs persistence for governor assignment, heartbeat timestamps, local tax totals, and latest recommendations/incidents.
- A claim-keyed manager stores governance state without redefining what a settlement is.

### 3. Treat tax collection in Phase 23 as local governor-owned accrual, not full treasury accounting
Recommendation: add a bounded local tax snapshot now, and leave treasury/faction accounting to Phase 25.

Why:
- ROADMAP asks governors to collect taxes, but later roadmap sequencing explicitly keeps treasury/upkeep as its own phase.
- A settlement-local accrual/report surface satisfies Phase 23 without dragging in faction-wide ledgers, army wage drains, or trade balancing.

Practical rule:
- Governor heartbeat may compute `taxesDue`, `taxesCollected`, `citizenCount`, and `lastCollectionTick` for one settlement.
- Do not add faction treasury mutation, army payments, or economic balancing here.

### 4. Use heartbeat-style updates, not per-tick full scans
Recommendation: run governor updates on a bounded server heartbeat.

Why:
- Project decisions already prefer heartbeat-based economy/governance expansion over per-tick recomputation.
- Governance outputs are advisory/stateful, not twitch combat mechanics.

Suggested inputs for the first heartbeat:
- friendly/degraded settlement binding status
- current claim siege state
- claim worker count and villager count
- worker-bound area presence
- recruit presence inside claim as a first-pass garrison signal
- shortage signals derived from existing shared seams such as `BannerModSupplyStatus`

### 5. Reuse companion special-screen patterns for governor control
Recommendation: add a dedicated governor screen/container + packet sync instead of overloading the promote UI.

Why:
- `AbstractLeaderEntity.openSpecialGUI(...)` already shows the established pattern for special recruit control surfaces.
- Governor control needs to display recommendations, taxes, incidents, and settlement status after promotion; the promote screen should remain a launch point, not the full control surface.

## Data Model Recommendation

Use a new `bannermod.governance` package with a minimal first slice:

- `BannerModGovernorSnapshot` — persisted claim-keyed governance state
- `BannerModGovernorManager` — load/save/update snapshots for the world
- `BannerModGovernorRules` — pure allow/deny + binding checks for assignment/control
- `BannerModGovernorHeartbeat` — recompute local taxes, recommendations, and incidents on heartbeat
- `BannerModGovernorIncident` / `BannerModGovernorRecommendation` — compact enums/records for report output

Recommended snapshot fields:
- claim UUID
- settlement faction id
- governor recruit UUID
- governor owner UUID
- claim center / anchor chunk
- last heartbeat tick
- last tax collection tick
- accrued tax amount
- citizen count snapshot
- latest incidents / shortages
- latest garrison recommendation
- latest fortification recommendation

## Risks And Pitfalls

### Promotion-path risk
- `RecruitEvents.promoteRecruit(...)` currently assumes every profession id maps to a concrete replacement entity.
- Governor must branch into assignment behavior instead of trying to spawn a missing entity type.

### Authority drift risk
- Governance control must not let same-team or outsiders silently assign governors in hostile or degraded claims.
- Use existing `BannerModAuthorityRules` vocabulary and claim-friendly checks together.

### Scope bleed into economy/logistics
- Do not add treasury ledgers, upkeep drains, route logic, stock reservations, or convoy behavior here.
- Keep outputs advisory/local so Phase 24-26 can consume them later.

### Full-world scan cost
- Avoid scanning every entity in every claim every tick.
- Prefer heartbeat cadence and claim-bounded queries centered on the claim anchor or claim bounds.

## Recommended Plan Breakdown

1. **Contracts + persistence foundation** — claim-keyed governance snapshot, manager, pure rules.
2. **Governor assignment authority** — designate/revoke one governor through existing ownership + friendly-claim checks.
3. **Governor heartbeat/reporting** — local tax accrual, garrison/fortification recommendations, incidents/shortages.
4. **Promotion + control UI** — activate the governor promotion path and add a dedicated governor control screen.
5. **Validation** — targeted JUnit plus root GameTests for assignment, persistence, degraded denial, and reporting.

## Validation Architecture

- Quick syntax gate: `./gradlew compileJava --console=plain`
- Quick unit gate: `./gradlew test --tests com.talhanation.bannermod.governance.* --console=plain`
- Full phase gate: `./gradlew compileJava test compileGameTestJava verifyGameTestStage --console=plain`
- New GameTests should reuse `BannerModGameTestSupport` and `BannerModDedicatedServerGameTestSupport` instead of building a second settlement harness.

## Planning Decisions

- Keep governor as a recruit/citizen designation, not a new entity type.
- Keep settlement identity claim-derived; persist governance state by claim UUID.
- Add bounded local tax collection state now; defer treasury/upkeep accounting to Phase 25.
- Drive governance updates through a heartbeat, not per-tick global recomputation.
- Reuse existing special-screen and GameTest helper patterns.
