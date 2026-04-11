# Phase 7: Mining & Merchant Flows - Context

**Gathered:** 2026-04-06
**Status:** Ready for planning

<domain>
## Phase Boundary

Finish and stabilize the existing miner and merchant profession loops so players can use them end-to-end without unrecoverable stalls, while preserving the established recovery, storage, and binding behavior from earlier phases. Adjacent profession loops such as lumberjack and fisherman may only be touched when they share the same minimal seam naturally.

</domain>

<decisions>
## Implementation Decisions

### Profession Scope
- Phase 7 should finish the existing miner and merchant loops first because those are the formal roadmap requirements.
- Lumberjack and fisherman work is only in scope if it falls out of the same shared seam naturally; otherwise it remains a follow-up outside this phase.

### Storage And Recovery Behavior
- Miner and merchant flows must preserve the shared request, storage, and recovery behavior established in Phases 2 through 5.
- Missing-item or blocked-state handling should continue to route through the shared worker-control behavior rather than profession-specific silent stalls.

### Loop Continuity
- Phase 7 should fix continuity or stall issues inside the current mining and merchant flows without redesigning assignment surfaces or profession UX.
- If a minimal persistence or busy-state fix is needed to keep miner or merchant progress alive after normal interruptions, it is in scope.

### Verification
- Prefer targeted automated coverage for the most regression-prone miner and merchant decision seams.
- Runtime proof should include one representative miner path and one representative merchant path.

### the agent's Discretion
- Choose the smallest shared seam(s) that best stabilize miner and merchant continuity without broad profession churn.
- Decide whether a lumberjack or fisherman fix belongs here only if the exact same helper or continuity rule clearly applies.
- Choose the most representative merchant flow based on the mechanics that are actually implemented in the current codebase.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase Definition
- `.planning/ROADMAP.md` §Phase 7: Mining & Merchant Flows — Defines the goal and success criteria for miner and merchant loops.
- `.planning/REQUIREMENTS.md` §`PROF-02`, `PROF-05` — Defines the profession requirements this phase owns.

### Project Constraints
- `.planning/PROJECT.md` — Confirms recovery-first scope and shared storage/request decisions.
- `.planning/STATE.md` — Carries the current project position and v1 constraints.
- `.planning/phases/06-farming-husbandry/06-CONTEXT.md` — Carries forward the pattern of fixing continuity inside assigned areas while preserving prior storage behavior.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `src/main/java/com/talhanation/workers/entities/ai/MinerWorkGoal.java`: Existing miner loop already covers area selection, break/floor scans, pickaxe/tool gating, ore-wall passes, and deposit transitions.
- `src/main/java/com/talhanation/workers/entities/ai/MerchantWorkGoal.java`: Existing merchant flow is already isolated as its own goal and is the main seam for making merchant behavior end-to-end reliable.
- `src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java`: Shared request/recovery/deposit state from earlier phases remains the canonical cross-profession control path.
- `src/main/java/com/talhanation/workers/entities/ai/LumberjackWorkGoal.java` and `FishermanWorkGoal.java`: Adjacent profession loops now use the shared request path too, so they are candidates for opportunistic shared-seam fixes only.

### Established Patterns
- Profession goals are long imperative state machines with direct world scans and explicit transitions.
- Earlier phases already established a pattern of preserving `current*Area` across temporary missing-input or deposit interruptions; miner and merchant should follow that same continuity rule where appropriate.
- Blocked-state surfacing should stay explicit and recoverable rather than devolving into silent idle states.

### Integration Points
- Miner stabilization will center on `MinerWorkGoal`, `MinerEntity`, and mining-area progress/busy-state handling.
- Merchant stabilization will center on `MerchantWorkGoal`, `MerchantEntity`, merchant UI/container flows, and any trade-state continuity logic already present.
- Runtime proof can likely reuse the work-area, storage, and recovery surfaces stabilized in earlier phases rather than introducing new UI.

</code_context>

<specifics>
## Specific Ideas

- Prioritize removing silent miner stalls and making merchant flows clearly resumable or recoverable.
- Preserve the current control surface; make the loop trustworthy underneath it.
- Only spend Phase 7 time on lumberjack/fisherman if the exact same fix clearly resolves a shared regression.

</specifics>

<deferred>
## Deferred Ideas

- Standalone lumberjack/fisherman completion remains deferred unless unlocked by the same shared seam.
- Courier/logistics systems remain deferred to v2.

</deferred>
