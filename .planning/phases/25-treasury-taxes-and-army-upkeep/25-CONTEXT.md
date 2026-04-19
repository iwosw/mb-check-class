# Phase 25: Treasury, Taxes, And Army Upkeep - Context

**Gathered:** 2026-04-18
**Status:** Ready for planning

<domain>
## Phase Boundary

Turn BannerMod's existing governor tax reports, settlement binding, logistics backbone, and recruit upkeep seams into one explicit fiscal model: claim-keyed settlement treasury ledgers with faction-aware reporting and authority, heartbeat-driven tax accrual/deposit, and coarse-cycle army wage/food drains with bounded unpaid or starving penalty states. This phase stays additive over the current settlement, governance, and logistics runtime; it does not introduce a new global economy manager, physical courier/item hauling for tax movement, per-tick recomputation, or the broader morale/disciplines rewrite reserved for later combat phases.

</domain>

<decisions>
## Implementation Decisions

### Treasury Ledger Shape
- **D-01:** Use a claim-keyed settlement treasury ledger as the primary fiscal unit.
- **D-02:** Attach faction ownership to the ledger for reporting and authority decisions, but do not introduce a new global economy manager in this phase.
- **D-03:** Keep treasury/accounting seams narrow and additive so later supply, trade, and settlement economy phases can build on them without rewriting settlement ownership.

### Tax Collection Model
- **D-04:** Convert Phase 23's report-only tax output into heartbeat-driven ledger accrual and deposit.
- **D-05:** Tax collection must stay coarse and heartbeat-based rather than per-tick recomputation.
- **D-06:** Do not model physical courier hauling, chest transfers, or route-executed tax movement in this phase.

### Army Upkeep Model
- **D-07:** Drain wages and food from the shared treasury/accounting seam on a coarse heartbeat or cycle.
- **D-08:** Do not keep recruit upkeep centered on ad hoc recruit-local containers once the Phase 25 seam is in place.
- **D-09:** The first upkeep slice should make settlement and army fiscal state legible through shared accounting vocabulary rather than hidden local timers or per-entity exceptions.

### Failure And Penalty States
- **D-10:** Ship bounded unpaid and starving penalty states in this phase.
- **D-11:** Those penalty states should be shaped so later morale and discipline systems can consume them.
- **D-12:** Do not bundle the broader morale rewrite from Phase 27 into this phase.

### the agent's Discretion
- Exact ledger field names, snapshot shape, and persistence helper boundaries.
- Exact heartbeat cadence and whether tax/upkeep use one shared cadence or closely related coarse cycles.
- Exact penalty tokens, state names, and how they bridge into later morale/discipline hooks, as long as they stay bounded and additive.

</decisions>

<code_context>
## Existing Code Insights

### Reusable Assets
- `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorSnapshot.java` already carries local tax summary fields and policy state that can become an input to treasury accrual instead of a dead-end report.
- `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorHeartbeat.java` already computes bounded local tax/report output on a coarse cadence and is the natural starting seam for heartbeat-driven treasury updates.
- `src/main/java/com/talhanation/bannermod/shared/settlement/BannerModSettlementBinding.java` already provides the claim-derived settlement legality/status boundary this phase should continue to respect.
- `src/main/java/com/talhanation/bannermod/logistics/BannerModSupplyStatus.java` and the recruit upkeep helpers already expose the current food/payment readiness vocabulary that later army-upkeep work must either reuse or replace carefully.
- Phase 24 logistics artifacts already establish server-authoritative, deterministic, route-oriented infrastructure that later economy phases can consume without being pulled directly into Phase 25 collection mechanics.

### Established Patterns
- Settlement-scoped state in this codebase prefers narrow manager or snapshot seams over deep new global managers.
- Brownfield economic or authority-sensitive behavior should remain server-authoritative, heartbeat-driven, and additive over existing claim/owner/faction contracts.
- Earlier phases intentionally separated local tax reporting, logistics routing, and settlement binding so Phase 25 can combine them through one fiscal vocabulary without reopening unrelated route or UI rewrites.

### Integration Points
- Phase 23 governor designation/reporting is the closest existing producer of settlement-local tax information and should inform, not duplicate, Phase 25 ledger accrual.
- Recruit-side upkeep readiness currently exists as an explicit seam and should be bridged into the new accounting-driven upkeep flow rather than left as a parallel model.
- Future Phases 26 and 28 depend on Phase 25 establishing stable treasury/upkeep/accounting vocabulary for supply, trade, and telemetry follow-up.

</code_context>

<specifics>
## Specific Ideas

- Keep the first treasury slice local and claim-bound so settlement identity stays consistent with the existing governor and claim contracts.
- Make taxes and army upkeep readable as one accounting model instead of leaving governor taxes as reports and recruit upkeep as separate hidden logic.
- Prefer explicit fiscal state and bounded penalties over broad simulation depth in the first slice.

</specifics>

<deferred>
## Deferred Ideas

- Physical transport of taxes or wages through courier workers, storage nodes, or trade routes.
- Global economy management, inter-settlement balancing, and broader market simulation.
- Full morale and discipline behavior rewrites beyond bounded unpaid/starving penalty hooks.

</deferred>
