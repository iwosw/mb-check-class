# Phase 23: Settlement Governance And Governor Control - Context

**Gathered:** 2026-04-15
**Status:** Ready for planning

<domain>
## Phase Boundary

Turn settlement governance into a first-class gameplay system by making one existing recruit act as a real governor for a claim-bound settlement, publishing local tax/report data, incidents, and recommendations through a bounded heartbeat, and exposing that information through a dedicated governor control screen. This phase stays layered on existing settlement binding, claim legality, and owner/admin authority rules; it does not introduce a treasury/logistics rewrite or a new settlement manager.

</domain>

<decisions>
## Implementation Decisions

### Governor Designation Path
- **D-01:** Phase 23 keeps governor assignment on the existing recruit promotion flow rather than adding a second assignment UI.
- **D-02:** A governor remains a designation on an existing recruit/citizen identity, not a new governor entity type or separate settlement actor.

### Governance Persistence And Authority
- **D-03:** Governor state stays persisted by claim UUID in one narrow `SavedData` manager rather than mutating claims directly or introducing a settlement manager.
- **D-04:** Governor actions stay layered on the existing owner/admin authority model plus settlement binding and claim legality; same-team cooperation does not become a new governor permission tier in this phase.

### Tax Model
- **D-05:** Phase 23 uses item-denominated tax reporting, but only as reported governance output. The snapshot may describe taxes in concrete goods/value terms, but this phase does not move items, mutate storage, or maintain a real treasury.
- **D-06:** Real storage-backed or treasury-backed tax collection is out of scope for Phase 23 and must be deferred to a later economy/logistics follow-up.

### Citizen Counting And Reports
- **D-07:** The first governance slice counts workers and villagers as bound citizens for tax/report purposes.
- **D-08:** Recruits are not part of the taxable citizen count in Phase 23; they are tracked separately as military presence used for garrison recommendations.
- **D-09:** Heartbeat incidents and recommendations stay compact token lists so the same snapshot seam can be reused by later runtime and UI phases.

### Governor Control Screen
- **D-10:** The first governor screen is a report-driven control panel that shows settlement status, local tax summary, incidents, and recommendations from live governance snapshots.
- **D-11:** The first interactive controls on that screen are bounded policy toggles for `garrison priority`, `fortification priority`, and `tax pressure`.
- **D-12:** The screen must stay within Phase 23 governance scope; it does not become a full treasury, logistics, or settlement-management console.

### the agent's Discretion
- Exact heartbeat cadence and refresh timing.
- Exact item/value vocabulary used for item-denominated tax reporting.
- Exact widget layout, wording, and visual arrangement of the governor control screen.
- Exact internal mapping from the three approved policy toggles into recommendation generation, as long as it remains claim-local and bounded.

</decisions>

<specifics>
## Specific Ideas

- Keep the dormant governor slot meaningful by turning the existing promotion path into the real designation path instead of inventing a second entrypoint.
- Make the first governor screen feel like a local settlement command surface: live report data first, then a few bounded policy levers rather than a giant administration panel.
- Tax output should feel concrete enough to read as settlement taxation, even though actual storage movement and treasury accounting stay deferred.

</specifics>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase scope and locked constraints
- `.planning/ROADMAP.md` — Phase 23 goal, plan decomposition, and the fixed boundary for governor control.
- `.planning/STATE.md` — Existing locked decisions for Phase 23 (`claim UUID` governor persistence; compact incident/recommendation token lists) and the adjacent logistics boundary that keeps real collection out of this phase.

### Prior phase decisions that constrain Phase 23
- `.planning/phases/22-citizen-role-unification/22-CONTEXT.md` — Governor work must sit on the existing recruit/citizen seam and preserve the incremental compatibility boundary.
- `.planning/phases/09-settlement-faction-binding-contract/09-CONTEXT.md` — Governor authority and settlement legality must stay derived from the existing settlement/claim contract.

### Architecture guidance
- `.planning/codebase/INTEGRATED_SYSTEM_ARCHITECTURE.md` — Settlement, authority, civilian-vs-military, and UI principles that Phase 23 builds on.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorManager.java` — existing narrow `SavedData` manager for claim-keyed governor snapshots.
- `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorSnapshot.java` — existing snapshot seam already shaped for heartbeat outputs and compact token lists.
- `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorService.java` — existing service seam for assignment, revocation, and snapshot access over claims.
- `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorHeartbeat.java` — existing bounded heartbeat seam for local taxes, incidents, and recommendations.
- `src/main/java/com/talhanation/bannermod/client/military/gui/GovernorScreen.java` and `src/main/java/com/talhanation/bannermod/network/messages/military/MessageToClientUpdateGovernorScreen.java` — existing dedicated screen and packet sync path ready for a real governor control surface.

### Established Patterns
- Shared world state that must survive reloads already lives in narrow manager-style `SavedData` seams rather than inside broad entity rewrites.
- Authority-sensitive actions reuse the shared owner/admin authority contract instead of creating one-off permission models.
- Brownfield UI work in this repo usually keeps the current entrypoint and packet/container pattern, then replaces placeholder behavior with real server-backed data.
- Settlement legality is already derived from claim/faction status through `BannerModSettlementBinding`, not through a separate settlement manager.

### Integration Points
- `src/main/java/com/talhanation/bannermod/events/RecruitEvents.java` already branches profession id `6` into governor designation and screen opening, making the current promotion flow the natural live entrypoint.
- `src/main/java/com/talhanation/bannermod/shared/authority/BannerModAuthorityRules.java` is the canonical authority boundary for governor assignment/revocation.
- `src/main/java/com/talhanation/bannermod/shared/settlement/BannerModSettlementBinding.java` is the canonical legality/status seam for governed claims.
- `src/main/java/com/talhanation/bannermod/events/ClaimEvents.java` already calls the governor heartbeat from the live server claim loop.

</code_context>

<deferred>
## Deferred Ideas

- Real storage-backed or treasury-backed item collection for taxes.
- Broad governor management actions beyond the bounded Phase 23 policy toggles.
- Governor-driven logistics routing, treasury control, or full settlement administration.

</deferred>

---

*Phase: 23-settlement-governance-and-governor-control*
*Context gathered: 2026-04-15*
