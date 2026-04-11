# Requirements: Villager Recruits stabilization and 1.21.1 migration prep

**Defined:** 2026-04-05
**Core Value:** The dev branch must become a trustworthy base where the core NPC army mechanics behave predictably, are defended by tests, and are structurally ready for a near-term 1.21.1 migration.

## v1 Requirements

### Build and Verification

- [x] **BLD-01**: Maintainer can build the mod from a clean checkout with a documented one-command Gradle workflow
- [x] **BLD-02**: Maintainer can run the full automated verification suite from Gradle, including unit tests and game tests
- [x] **BLD-03**: Maintainer can reproduce dependency resolution consistently without relying on drifting tool versions or undocumented local setup
- [x] **BLD-04**: Maintainer can identify whether a change failed in build, unit-test, or game-test stages from the standard verification output

### Test Infrastructure

- [x] **TEST-01**: Maintainer can add new pure-logic unit tests without booting a Minecraft runtime
- [x] **TEST-02**: Maintainer can add new Forge GameTests for gameplay contracts in a standard project test location
- [x] **TEST-03**: Maintainer can share reusable fixtures and helpers for common recruit, faction, persistence, and networking test scenarios
- [x] **TEST-04**: Maintainer can see a documented verification matrix that distinguishes deep coverage areas from baseline smoke coverage

### Battles and Formations

- [x] **BATL-01**: Player can run representative recruit battles without obvious combat-state regressions in the current dev branch
- [x] **BATL-02**: Player can issue formation-related commands and observe stable formation behavior in representative combat and movement scenarios
- [x] **BATL-03**: Maintainer can verify battle-critical behaviors with automated tests that cover both expected flows and regression-prone edge cases
- [x] **BATL-04**: Maintainer can detect battle-density regressions through defined stress or baseline scenarios instead of ad hoc manual play only

### Commands and AI

- [x] **CMD-01**: Player can issue core recruit commands and receive server-authoritative outcomes without command-state desync
- [x] **CMD-02**: Recruit AI can transition between major command and combat states without getting stuck in known broken loops or invalid states
- [x] **CMD-03**: Maintainer can verify priority command and AI behaviors with automated tests that encode intended behavior rather than legacy accidents
- [x] **CMD-04**: Broken or invalid command flows fail predictably with diagnostics or safe degradation instead of silent no-ops where practical

### Persistence and Networking

- [x] **DATA-01**: Server operator can save and reload worlds without losing or corrupting critical recruit, team, faction, claim, route, or group state
- [x] **DATA-02**: Maintainer can verify persistence round trips, dirty-marking behavior, and restart scenarios with automated coverage for high-risk saved systems
- [x] **DATA-03**: Multiplayer player can use packet-driven gameplay flows without obvious client/server desync in prioritized mechanics
- [x] **DATA-04**: Maintainer can verify invalid, boundary, and side-sensitive packet paths with tests that enforce server-authoritative behavior

### Full-Surface Stabilization

- [x] **STAB-01**: Maintainer can execute a defined full-mod verification pass that covers all major gameplay subsystems, with extra depth on battles, persistence, commands, AI, networking, and formations
- [x] **STAB-02**: Known logic gaps found during verification are either fixed or explicitly documented as deferred with rationale
- [x] **STAB-03**: Optional compatibility paths fail safely without destabilizing the core mod when dependent mods or contexts are absent

### Migration Preparation

- [x] **MIG-01**: Maintainer can identify the main version-sensitive seams for a future 1.21.1 port in networking, persistence, client state, compat, pathfinding, and registration glue
- [x] **MIG-02**: Maintainer can work against narrower internal seams or adapters instead of deep direct coupling to Forge-heavy runtime code in the highest-risk subsystems
- [x] **MIG-03**: Maintainer has a concrete migration-prep checklist and code inventory that reduces the future 1.21.1 port to a bounded follow-up effort

## v2 Requirements

Deferred to future release. Tracked but not in current roadmap.

### Performance and Expansion

- **PERF-01**: Player can run very large-scale battles above the agreed stabilization baseline without measurable performance degradation relative to current expectations
- **PORT-01**: Maintainer can compile and run the mod on Minecraft 1.21.1 with preserved core mechanics
- **CONT-01**: Player can access new gameplay content, units, or mechanics beyond the current mod surface
- **COMP-01**: Maintainer can certify expanded optional mod compatibility beyond the currently supported integrations

## Out of Scope

Explicitly excluded. Documented to prevent scope creep.

| Feature | Reason |
|---------|--------|
| Full Minecraft 1.21.1 port | This initiative prepares the codebase for the port but does not execute the full version migration |
| New gameplay content, units, factions, or mechanics | The current milestone is about stabilization, testing, and migration groundwork, not feature expansion |
| Broad UI polish unrelated to correctness or diagnostics | Cosmetic changes do not materially improve trust, testability, or migration readiness |
| Broad compatibility expansion | Each extra integration multiplies verification and migration cost; current goal is containment and safe degradation |

## Traceability

Which phases cover which requirements. Updated during roadmap creation.

| Requirement | Phase | Status |
|-------------|-------|--------|
| BLD-01 | Phase 1 | Complete |
| BLD-02 | Phase 1 | Complete |
| BLD-03 | Phase 1 | Complete |
| BLD-04 | Phase 1 | Complete |
| TEST-01 | Phase 2 | Complete |
| TEST-02 | Phase 2 | Complete |
| TEST-03 | Phase 2 | Complete |
| TEST-04 | Phase 6 | Complete |
| BATL-01 | Phase 3 | Complete |
| BATL-02 | Phase 3 | Complete |
| BATL-03 | Phase 3 | Complete |
| BATL-04 | Phase 3 | Complete |
| CMD-01 | Phase 4 | Complete |
| CMD-02 | Phase 4 | Complete |
| CMD-03 | Phase 4 | Complete |
| CMD-04 | Phase 4 | Complete |
| DATA-01 | Phase 5 | Complete |
| DATA-02 | Phase 5 | Complete |
| DATA-03 | Phase 5 | Complete |
| DATA-04 | Phase 5 | Complete |
| STAB-01 | Phase 6 | Complete |
| STAB-02 | Phase 6 | Complete |
| STAB-03 | Phase 6 | Complete |
| MIG-01 | Phase 7 | Complete |
| MIG-02 | Phase 7 | Complete |
| MIG-03 | Phase 8 | Complete |

**Coverage:**
- v1 requirements: 26 total
- Mapped to phases: 26
- Unmapped: 0 ✓

---
*Requirements defined: 2026-04-05*
*Last updated: 2026-04-09 after milestone completion*
