# Phase 6: Farming & Husbandry - Context

**Gathered:** 2026-04-06
**Status:** Ready for planning

<domain>
## Phase Boundary

Finish and stabilize the existing farmer and animal-farmer profession loops so they complete their intended agricultural behavior inside assigned areas and keep operating through normal interruptions, without expanding this phase into new agriculture features or unrelated profession redesign.

</domain>

<decisions>
## Implementation Decisions

### Profession Scope
- Phase 6 should finish the existing farmer and animal-farmer loops already present in the codebase.
- New farming features or broader assignment redesign remain out of scope.

### Item And Storage Behavior
- Farming and husbandry flows must preserve the Phase 5 rule: missing seed, tool, bucket, or breeding-item complaints should only surface after inventory-first and storage-second checks fail.
- Agricultural loops should rely on the shared storage/request seams added in Phase 5 rather than reintroducing profession-local complaint shortcuts.

### Loop Continuity
- Phase 6 should fix interruptions only where they break agricultural continuity inside assigned areas, including resume after temporary stalls or normal world interruptions.
- If a minimal persistence or status fix is needed to keep the farmer or animal-farmer loop alive after interruptions, it is in scope.

### Verification
- Prefer targeted automated coverage for the most regression-prone farmer and animal-farmer logic seams.
- Runtime proof should include one representative farmer flow and one representative animal-farmer flow.

### the agent's Discretion
- Choose the smallest shared seam(s) that best stabilize farmer and animal-farmer continuity without dragging in later profession work.
- Choose the most representative husbandry path, whether breeding, culling, or both, based on what the current code most clearly intends.
- Decide whether fixes belong in profession goals, area scanning, or shared worker helpers based on the least invasive correct implementation.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase Definition
- `.planning/ROADMAP.md` §Phase 6: Farming & Husbandry — Defines the goal and success criteria for farmer and animal profession loops.
- `.planning/REQUIREMENTS.md` §`PROF-01`, `PROF-04` — Defines the agricultural profession requirements this phase owns.

### Project Constraints
- `.planning/PROJECT.md` — Confirms recovery-first scope and the accepted inventory-before-complaint rule.
- `.planning/STATE.md` — Carries the current project position and v1 constraints.
- `.planning/phases/05-storage-loop-integrity/05-CONTEXT.md` — Carries forward the shared storage/request rule that farming flows must preserve.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `src/main/java/com/talhanation/workers/entities/ai/FarmerWorkGoal.java`: Existing farmer loop already covers area selection, harvest, watering, plowing, seed preparation, planting, and post-work status handling.
- `src/main/java/com/talhanation/workers/entities/ai/AnimalFarmerWorkGoal.java`: Existing animal-farmer loop already covers breeding and slaughter decisions inside assigned animal pens.
- `src/main/java/com/talhanation/workers/entities/ai/FarmerAreaSelectionTiming.java` and `FarmerPlantingPreparation.java`: Recently added targeted seams already protect farmer responsiveness and seed-resolution behavior.
- `src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java` plus `GetNeededItemsFromStorage.java`: Shared item-request and complaint gating from Phase 5 is now the canonical path for missing agricultural inputs.

### Established Patterns
- Agricultural professions are implemented as long state-machine goals with direct world scans and imperative transitions.
- Needed tools/materials are requested through `NeededItem` and now through shared request helpers rather than a separate inventory planner.
- Continuity bugs are likely to appear at state transitions between scan, action, and done/reset phases rather than in registries or UI layers.

### Integration Points
- Farmer stabilization will center on `FarmerWorkGoal`, `FarmerEntity`, and crop/work-area scanning behavior.
- Husbandry stabilization will center on `AnimalFarmerWorkGoal`, `AnimalFarmerEntity`, and `AnimalPenArea` behavior.
- Runtime proof can likely reuse the now-stable area/storage control surfaces from earlier phases instead of changing UI or command flows.

</code_context>

<specifics>
## Specific Ideas

- Prefer finishing the core happy path plus interruption recovery over polishing every edge case equally.
- Keep farmer responsiveness and planting continuity high, since those are the most visible agricultural regressions.
- For animal farmers, prioritize making the intended breed/cull loop clearly continue inside the pen rather than adding richer herd-management behavior.

</specifics>

<deferred>
## Deferred Ideas

- New explicit field-assignment UX remains deferred to v2.
- Courier/logistics systems remain deferred to v2.

</deferred>
