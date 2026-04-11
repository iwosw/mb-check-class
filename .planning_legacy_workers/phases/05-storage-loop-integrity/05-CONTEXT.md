# Phase 5: Storage Loop Integrity - Context

**Gathered:** 2026-04-06
**Status:** Ready for planning

<domain>
## Phase Boundary

Make the shared worker storage loops trustworthy so workers fetch required materials before work, deposit surplus safely, and retain the reserve items needed to keep their loop running, without expanding this phase into broad profession redesign or explicit logistics systems.

</domain>

<decisions>
## Implementation Decisions

### Missing-Item Complaint Rule
- Workers must check their current inventory first and then their configured storage path before surfacing a missing-item complaint.
- Complaints about missing tools or materials such as buckets, hoes, seeds, or similar required items should only appear after those checks fail.

### Storage Logic Focus
- Phase 5 should fix the shared withdraw, deposit, and reserve-item logic before adding profession-specific exceptions.
- Existing configured storage preference and `lastStorage` affinity should be preserved and hardened rather than replaced.

### Deposit And Reserve Rules
- Deposit behavior should never lose or duplicate items.
- Reserve-item retention should be explicit and reliable enough that workers keep the essentials needed for their current loop instead of depositing them away.

### Scope Boundaries
- Phase 5 should stay focused on inventory and storage-loop correctness.
- Broader profession-loop optimization remains in later profession phases unless a minimal fix is required to validate the shared storage loop.
- New courier/logistics systems remain out of scope for v1.

### Verification
- Prefer focused automated coverage around storage withdraw, deposit, reserve retention, and complaint gating seams.
- Runtime proof should use one representative worker path that naturally exercises both fetching and depositing behavior.

### the agent's Discretion
- Choose the smallest shared seam(s) that can express the inventory-first, storage-second complaint rule clearly.
- Choose the representative worker path that best exercises both fetch and deposit without pulling in unrelated profession complexity.
- Decide whether reserve retention belongs in `wantsToKeep`, needed-item accounting, deposit filtering, or a small shared helper based on the least invasive correct fix.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase Definition
- `.planning/ROADMAP.md` §Phase 5: Storage Loop Integrity — Defines the goal and success criteria for withdraw, deposit, and reserve-item trustworthiness.
- `.planning/REQUIREMENTS.md` §`STOR-02`, `STOR-03`, `STOR-04` — Defines the storage-loop requirements this phase owns.

### Project Constraints
- `.planning/PROJECT.md` — Confirms recovery-first scope and logs the accepted inventory-before-complaint rule.
- `.planning/STATE.md` — Carries the current project position and v1 constraints.
- `.planning/phases/04-persistence-binding-resume/04-CONTEXT.md` — Carries forward the requirement to preserve existing binding models and storage affinity.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `src/main/java/com/talhanation/workers/entities/ai/GetNeededItemsFromStorage.java`: Existing shared withdraw loop already consults `worker.neededItems`, scans configured storage areas, and sets `lastStorage` on success.
- `src/main/java/com/talhanation/workers/entities/ai/DepositItemsToStorage.java`: Existing shared deposit loop already handles container selection, deposit iteration, and `lastStorage` updates.
- `src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java`: Shared worker base already tracks `neededItems`, `lastStorage`, `farmedItems`, `forcedDeposit`, and `wantsToKeep`, which are the main shared seams for Phase 5.
- `src/main/java/com/talhanation/workers/entities/ai/AbstractChestGoal.java`: Existing storage-area scan and `lastStorage` prioritization logic is the natural shared binding point for all workers.

### Established Patterns
- Profession goals currently request tools/materials by appending `NeededItem` entries, then defer to the shared chest goals.
- Reserve behavior is partly encoded through per-worker `wantsToKeep` overrides, which suggests a minimal-correct fix may combine shared and profession-aware filtering rather than a brand-new inventory system.
- Missing-item feedback already exists, but complaint timing needs to respect the new inventory-first, storage-second rule.

### Integration Points
- Withdraw complaint gating will likely connect through `NeededItem` creation sites plus `GetNeededItemsFromStorage` failure states.
- Deposit correctness and reserve retention will likely connect through `DepositItemsToStorage.depositItems()` and `AbstractWorkerEntity.wantsToKeep()` overrides.
- Runtime proof can likely reuse the farmer path because it naturally exercises tools, seeds, inventory pickup, and storage deposit behavior.

</code_context>

<specifics>
## Specific Ideas

- Do not let profession code shout about missing items before the shared storage fetch path has had a real chance to satisfy the request.
- Favor one explicit reserve-rule seam over scattered one-off inventory exceptions when possible.
- Treat loss/duplication prevention as more important than micro-optimizing chest choice or pathing in this phase.

</specifics>

<deferred>
## Deferred Ideas

- Courier-style logistics and richer routing remain deferred to v2.

</deferred>
