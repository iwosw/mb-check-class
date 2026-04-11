# Roadmap: Villager Workers Revival

## Overview

This roadmap revives the unfinished worker-villager mod by recovering its existing gameplay loops in small, verifiable slices: first restore a clean runnable baseline, then stabilize worker control and area management, then harden persistence and storage behavior, then finish each profession loop, and finally prove the mod is safe through automated tests and dedicated-server validation. The 1.21.1 port remains deferred to v2; this roadmap focuses on making the current recovery scope complete, stable, and testable.

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3): Planned milestone work
- Decimal phases (2.1, 2.2): Urgent insertions (marked with INSERTED)

Decimal phases appear between their surrounding integers in numeric order.

- [x] **Phase 1: Recovery Baseline** - Restore a clean runnable baseline for the unfinished mod.
- [x] **Phase 2: Worker Control & Recovery** - Make worker command, recall, and blocked-state feedback reliable.
- [ ] **Phase 3: Work Area Authoring** - Make work areas creatable, editable, visible, and validated.
- [x] **Phase 4: Persistence & Binding Resume** - Preserve worker-area bindings across relog, restart, and chunk reload.
- [ ] **Phase 5: Storage Loop Integrity** - Make withdraw, deposit, and reserve-item behavior trustworthy.
- [ ] **Phase 6: Farming & Husbandry** - Finish and stabilize the farm and animal profession loops.
- [ ] **Phase 7: Mining & Merchant Flows** - Finish and stabilize mining and merchant gameplay loops.
- [ ] **Phase 8: Builder Templates & Construction** - Make builder/template construction complete and resumable.
- [ ] **Phase 9: Verification & Release Confidence** - Prove the recovered mod is test-covered and dedicated-server safe.

## Phase Details

### Phase 1: Recovery Baseline
**Goal**: Players can launch and run the recovered mod baseline without critical compile-time or startup failures while deeper recovery work proceeds.
**Depends on**: Nothing (first phase)
**Requirements**: QUAL-03
**Success Criteria** (what must be TRUE):
  1. The project builds successfully from a clean checkout without critical compile-time errors.
  2. A player can start the game with the mod enabled without a critical startup crash.
  3. Core registries, networking, and existing worker systems initialize without breaking the playable baseline.
**Plans**: 3 plans

Plans:
- [x] 01-01-PLAN.md — Recover Gradle/dependency metadata for a clean 1.20.1 baseline build.
- [x] 01-02-PLAN.md — Repair startup-critical bootstrap wiring while preserving the spawn-item smoke path.
- [x] 01-03-PLAN.md — Add one lightweight regression test and perform client/server startup smoke verification.

### Phase 2: Worker Control & Recovery
**Goal**: Players can issue worker commands confidently and recover workers when a job flow gets stuck.
**Depends on**: Phase 1
**Requirements**: WORK-01, WORK-02, WORK-03
**Success Criteria** (what must be TRUE):
  1. Player can issue the existing worker command flows and see the worker react correctly without obvious desync.
  2. Player can use an in-game recovery action to recall or reset a stuck worker and restore control.
  3. Player can see a clear blocked or idle reason when a worker cannot continue its current job.
**Plans**: 4 plans

Plans:
- [x] 02-01-PLAN.md — Add the shared worker recovery/status foundation and regression seam.
- [x] 02-02-PLAN.md — Wire generic recovery into the existing worker command screen.
- [x] 02-03-PLAN.md — Surface blocked/idle reasons from shared storage and profession goals.
- [x] 02-04-PLAN.md — Verify command response, recovery, and blocked feedback on the farmer proof path.

### Phase 3: Work Area Authoring
**Goal**: Players can create, edit, inspect, and trust work areas through the existing assignment and UI flows.
**Depends on**: Phase 2
**Requirements**: AREA-01, AREA-02, AREA-03, AREA-04
**Success Criteria** (what must be TRUE):
  1. Player can create a work area using the intended in-game assignment flow.
  2. Player can edit an existing work area and have the server apply the change correctly.
  3. Invalid overlaps or unauthorized edits are rejected instead of silently corrupting area state.
  4. Player can inspect current work-area state through the existing UI and visual feedback.
**Plans**: 4 plans
**UI hint**: yes

Plans:
- [ ] 03-01-PLAN.md — Add a shared authoring-rule seam with automated coverage for permission and validation decisions.
- [ ] 03-02-PLAN.md — Harden work-area creation and inspect/open-screen reliability on the existing surfaces.
- [ ] 03-03-PLAN.md — Validate server-side edit packets and remove client-side work-area screen drift.
- [ ] 03-04-PLAN.md — Smoke-verify representative crop and storage authoring flows end-to-end.

### Phase 4: Persistence & Binding Resume
**Goal**: Worker assignments, area links, and storage/home bindings survive world lifecycle events and resume correctly.
**Depends on**: Phase 3
**Requirements**: AREA-05, STOR-01
**Success Criteria** (what must be TRUE):
  1. Worker assignments and work areas persist across relog, restart, and chunk reload.
  2. Player can bind a worker to its intended storage or home location and keep that binding after reload.
  3. Workers resume their intended area- and binding-based behavior after returning to a saved world.
**Plans**: 3 plans

Plans:
- [x] 04-01-PLAN.md — Add the shared UUID persistence seam and regression coverage for worker binding resume.
- [x] 04-02-PLAN.md — Wire persisted work-area rebinding into concrete worker and merchant entities.
- [x] 04-03-PLAN.md — Smoke-verify representative reload continuity for area and storage bindings.

Acceptance note:
- Phase 4 was accepted on code-path validation and focused regression tests. Full gameplay verification for storage/material continuity remains part of later end-to-end verification work.

### Phase 5: Storage Loop Integrity
**Goal**: Players can trust worker inventory loops to fetch what they need and return what they should without loss or duplication.
**Depends on**: Phase 4
**Requirements**: STOR-02, STOR-03, STOR-04
**Success Criteria** (what must be TRUE):
  1. Worker can withdraw required tools or materials before starting work, first checking its own inventory and then configured storage before surfacing a missing-item complaint.
  2. Worker deposits surplus items back into storage without item loss or duplication.
  3. Worker keeps required reserve items instead of depositing essentials needed for its loop.
**Plans**: 3 plans

Implementation note:
- Missing-item complaints for tools or materials such as buckets, hoes, or seeds must only appear after the worker checks both its own inventory and the configured storage path.

Plans:
- [ ] 05-01-PLAN.md — Add the shared inventory-first missing-item request seam and wire representative profession goals through it.
- [ ] 05-02-PLAN.md — Harden shared deposit transfer and reserve-item retention without changing storage selection.
- [ ] 05-03-PLAN.md — Smoke-verify one representative fetch/work/deposit loop end-to-end.

### Phase 6: Farming & Husbandry
**Goal**: Players can rely on agricultural worker professions to complete their intended loops inside assigned areas.
**Depends on**: Phase 5
**Requirements**: PROF-01, PROF-04
**Success Criteria** (what must be TRUE):
  1. Farmer worker tills, plants, harvests, and replants within its assigned area.
  2. Animal worker professions maintain their intended breed or cull loops inside assigned areas.
  3. These profession loops continue operating after normal interruptions without losing their assigned area context.
**Plans**: 3 plans

Implementation note:
- Farming flows must rely on the Phase 5 lookup rule so seed/tool complaints only surface after inventory-first and storage-second checks fail.

Plans:
- [ ] 06-01-PLAN.md — Stabilize farmer crop-loop continuity so missing-input fetches do not drop the assigned field.
- [ ] 06-02-PLAN.md — Stabilize animal-farmer pen-loop continuity and release stale busy state safely.
- [ ] 06-03-PLAN.md — Smoke-verify representative farmer and animal-farmer loops end-to-end.

### Phase 7: Mining & Merchant Flows
**Goal**: Players can use miner and merchant workers end-to-end without the core loop stalling or misbehaving.
**Depends on**: Phase 6
**Requirements**: PROF-02, PROF-05
**Success Criteria** (what must be TRUE):
  1. Miner worker executes its assigned mining loop safely and continues progress reliably.
  2. Merchant worker mechanics present in the codebase function end-to-end in their intended gameplay flow.
  3. These profession loops surface recoverable failures through the established worker-control behavior rather than becoming unrecoverable.
**Plans**: 3 plans

Plans:
- [x] 07-01-PLAN.md — Stabilize miner continuity and shared missing-input recovery.
- [x] 07-02-PLAN.md — Make merchant trade execution authoritative and regression-covered.
- [x] 07-03-PLAN.md — Smoke-verify representative miner and merchant flows end-to-end.

### Phase 8: Builder Templates & Construction
**Goal**: Players can use the existing build-area and template mechanics to have builder workers place intended structures progressively.
**Depends on**: Phase 7
**Requirements**: PROF-03
**Success Criteria** (what must be TRUE):
  1. Player can use the existing build-area and template flow to start a builder job.
  2. Builder worker places the intended structure progressively instead of failing after setup.
  3. Builder progress survives normal save/reload interruptions closely enough to resume the intended construction loop.
**Plans**: 3 plans
**UI hint**: yes

Plans:
- [x] 08-01-PLAN.md — Add the pure build-progress classification seam and regression coverage for resumable construction.
- [x] 08-02-PLAN.md — Persist builder start/progress state and resume the existing construction loop after reloads.
- [x] 08-03-PLAN.md — Smoke-verify one representative builder template path end-to-end, including reload resume.

### Phase 9: Verification & Release Confidence
**Goal**: The recovered mod is protected by meaningful automated tests and verified to work in dedicated-server gameplay.
**Depends on**: Phase 8
**Requirements**: QUAL-01, QUAL-02
**Success Criteria** (what must be TRUE):
  1. Key gameplay flows run correctly on a dedicated server, not just in local singleplayer.
  2. Automated tests cover key logic seams for mechanics, validation, persistence, and regression-prone behavior.
  3. The final recovery baseline can be exercised through a defined smoke-test path without critical gameplay breakage.
**Plans**: 2 plans

Plans:
- [x] 09-01-PLAN.md — Consolidate the release-confidence regression suite and write one final smoke checklist.
- [x] 09-02-PLAN.md — Run the consolidated automation and dedicated-server final smoke gate.

## Progress

**Execution Order:**
Phases execute in numeric order: 2 → 2.1 → 2.2 → 3 → 3.1 → 4

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Recovery Baseline | 3/3 | Complete | 2026-04-05 |
| 2. Worker Control & Recovery | 4/4 | Complete | 2026-04-06 |
| 3. Work Area Authoring | 4/4 | Complete | 2026-04-09 |
| 4. Persistence & Binding Resume | 3/3 | Complete | 2026-04-09 |
| 5. Storage Loop Integrity | 3/3 | Complete | 2026-04-09 |
| 6. Farming & Husbandry | 3/3 | Complete | 2026-04-09 |
| 7. Mining & Merchant Flows | 3/3 | Complete | 2026-04-09 |
| 8. Builder Templates & Construction | 3/3 | Complete | 2026-04-09 |
| 9. Verification & Release Confidence | 2/2 | Complete | 2026-04-09 |
