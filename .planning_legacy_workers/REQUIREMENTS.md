# Requirements: Villager Workers Revival

**Defined:** 2026-04-05
**Core Value:** The mod must reliably let players use the worker-villager mechanics already designed in the codebase without critical bugs or missing core loops.

## v1 Requirements

### Worker Control

- [ ] **WORK-01**: Player can issue the existing worker command flows without the interaction failing or desyncing
- [ ] **WORK-02**: Player can recall or reset a stuck worker through an in-game recovery action
- [ ] **WORK-03**: Player can see a clear blocked or idle reason when a worker cannot continue its job

### Work Areas

- [ ] **AREA-01**: Player can create a work area using the existing assignment flow
- [ ] **AREA-02**: Player can edit an existing work area and have the server apply the change correctly
- [ ] **AREA-03**: Work-area validation prevents invalid overlaps or unauthorized edits
- [ ] **AREA-04**: Player can inspect current work-area state through the existing UI and visual feedback
- [ ] **AREA-05**: Worker assignments and work areas persist and resume across relog, restart, or chunk reload

### Storage

- [ ] **STOR-01**: Player can bind a worker to its intended storage or home location
- [ ] **STOR-02**: Worker can withdraw required tools or materials before starting work, checking its own inventory and configured storage before surfacing a missing-item complaint
- [ ] **STOR-03**: Worker deposits surplus items back into storage without losing or duplicating items
- [ ] **STOR-04**: Worker keeps required reserve items instead of depositing essentials needed for its loop

### Professions

- [ ] **PROF-01**: Farmer worker can till, plant, harvest, and replant within its assigned area
- [ ] **PROF-02**: Miner worker can execute its assigned mining loop safely and continue progress reliably
- [ ] **PROF-03**: Builder worker can use build-area and template mechanics to place intended structures progressively
- [ ] **PROF-04**: Animal worker professions can maintain their intended breed or cull loops inside assigned areas
- [ ] **PROF-05**: Merchant worker mechanics included in the codebase function end-to-end in their intended gameplay flow

### Quality

- [x] **QUAL-01**: Core gameplay flows work on a dedicated server, not just in local singleplayer
- [x] **QUAL-02**: Automated tests cover key logic seams for mechanics, validation, persistence, and regression-prone behavior
- [ ] **QUAL-03**: The project builds cleanly without critical compile-time or startup failures

## v2 Requirements

### Release Target

- **PORT-01**: The mod is ported to Minecraft 1.21.1 with a stable release baseline

### Worker Ownership

- **OWNR-01**: Player can hire a worker through the intended ownership flow
- **OWNR-02**: Ownership and reassignment mechanics work without wiping worker state or permissions unexpectedly

### Work Assignment

- **ASSIGN-01**: Player can explicitly assign a worker to a specific work area instead of relying only on nearest-area auto-selection

### Logistics

- **LOGI-01**: A courier worker profession exists for directed item transport between intended owned or authorized endpoints

## Out of Scope

Explicitly excluded from this roadmap to protect the recovery scope.

| Feature | Reason |
|---------|--------|
| New professions beyond what the current codebase already implies | Recovery effort should finish existing mechanics before expanding scope |
| Colony-wide courier or global logistics systems | High complexity and not required for the selected v1 scope |
| MineColonies-style colony management redesign | Conflicts with the goal of reviving the existing mod rather than replacing it |
| Worker leveling, progression, or stat systems | Nice-to-have and not required to reach functional parity for this milestone |
| Major pathfinding rewrites unrelated to fixing shipped loops | High regression risk during recovery and stabilization |

## Traceability

Which phases cover which requirements. Updated during roadmap creation.

| Requirement | Phase | Status |
|-------------|-------|--------|
| WORK-01 | Phase 2 | Pending |
| WORK-02 | Phase 2 | Pending |
| WORK-03 | Phase 2 | Pending |
| AREA-01 | Phase 3 | Pending |
| AREA-02 | Phase 3 | Pending |
| AREA-03 | Phase 3 | Pending |
| AREA-04 | Phase 3 | Pending |
| AREA-05 | Phase 4 | Accepted |
| STOR-01 | Phase 4 | Accepted |
| STOR-02 | Phase 5 | Pending |
| STOR-03 | Phase 5 | Pending |
| STOR-04 | Phase 5 | Pending |
| PROF-01 | Phase 6 | Pending |
| PROF-02 | Phase 7 | Pending |
| PROF-03 | Phase 8 | Pending |
| PROF-04 | Phase 6 | Pending |
| PROF-05 | Phase 7 | Pending |
| QUAL-01 | Phase 9 | Pending |
| QUAL-02 | Phase 9 | Pending |
| QUAL-03 | Phase 1 | Pending |

**Coverage:**
- v1 requirements: 20 total
- Mapped to phases: 20
- Unmapped: 0 ✓

---
*Requirements defined: 2026-04-05*
*Last updated: 2026-04-06 after Phase 4 acceptance*
