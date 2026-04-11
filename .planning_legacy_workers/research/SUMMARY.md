# Project Research Summary

**Project:** Villager Workers Revival
**Domain:** Minecraft Forge villager-worker automation mod revival
**Researched:** 2026-04-05
**Confidence:** MEDIUM-HIGH

## Executive Summary

Villager Workers is a brownfield Forge mod revival, not a greenfield automation game. The research consistently points to a conservative recovery strategy: preserve the existing entity-centric worker/work-area model, harden server authority, extract a few high-risk logic seams for testing, and only then complete missing mechanics and finish the Forge 1.21.1 port. Experts build this kind of mod by keeping entities as the source of truth, treating packets as thin validated commands, and validating behavior on dedicated servers rather than trusting singleplayer.

The recommended approach is to rebase onto a fresh Forge 1.21.1 MDK with Java 21 and ForgeGradle 7, while keeping the gameplay architecture intact. The MVP should center on reliable worker assignment, persistent work areas, airtight storage/inventory loops, and profession-complete farm/mine/build flows. Do not expand into MineColonies-style colony simulation, new professions, or global logistics before those loops are stable.

The biggest risks are not feature gaps but shippability gaps: the Recruits 1.21.1 dependency may block release, client/server trust boundaries are currently too weak for multiplayer, and giant AI/state-machine classes can hide regressions. Mitigate those by making dependency validation the first gate, centralizing packet authorization, adding unit tests plus GameTests around extracted planners/helpers, and using dedicated-server smoke tests as a release requirement.

## Key Findings

### Recommended Stack

The stack recommendation is unusually clear: use a fresh Forge 1.21.1 baseline and remove legacy build debt instead of patching the old 1.20.1 build in place. Persistent gameplay state should stay in entity/world save data rather than adding any external database, and testing should combine JUnit for pure logic with Forge GameTests and dedicated-server validation for real gameplay behavior.

**Core technologies:**
- **Minecraft Forge 1.21.1 (`52.1.x`)**: loader, registries, networking, GameTest runtime — direct target with official support and lowest migration ambiguity.
- **ForgeGradle 7 + fresh 1.21.1 MDK**: build and run configuration — avoids dragging obsolete Gradle/plugin wiring into the port.
- **Java 21**: compile/runtime target — required for Forge 1.21.x.
- **Mojang official mappings**: readable maintenance baseline — safest mapping choice for a brownfield Forge port.
- **NBT + vanilla/Forge codecs**: persistence layer — matches the mod’s entity/world-state nature without introducing an unnecessary database.
- **CoreLib 1.21.1**: supporting abstraction layer — available and likely the lowest-churn dependency to keep.
- **Recruits 1.21.1-compatible build**: AI/pathing dependency — mandatory but currently the main release gate and least-certain dependency.

### Expected Features

The feature research is strongly opinionated: players will judge this revival on whether existing advertised loops work reliably in multiplayer, not on whether it adds flashy new systems. Table stakes are stable worker ownership/assignment, server-authoritative work areas, per-worker storage binding, predictable deposit/withdraw behavior, and persistence across relog/restart. For MVP, farm, mine, and build must feel complete enough to trust.

**Must have (table stakes):**
- Reliable worker hiring, ownership, reassignment, and permissions.
- Persistent, editable, visible work areas with overlap and authority validation.
- Per-worker storage/home binding with safe inventory reserve/deposit logic.
- Profession-complete **farmer**, **miner**, and **builder** loops using existing mechanics.
- Clear blocked-state feedback plus recall/unstick behavior.
- Save/reload persistence for workers, areas, inventories, and progress.
- Dedicated-server-safe multiplayer behavior.

**Should have (competitive):**
- Animal profession stability if those workers ship in the first release.
- Merchant route polish if existing code is already close.
- Better builder required-items/progress UX.

**Defer (v2+):**
- Colony-wide courier/global logistics systems.
- Request-board/postbox UX.
- Leveling/stats systems.
- Advanced stock rules, craft chains, or broad mod integrations.
- New professions or MineColonies-style colony simulation.

### Architecture Approach

The architecture recommendation is to keep the current entity-centric design and add only a few strategic seams. Workers and work areas should remain the persistence boundary; packet handlers should become thin server-side command entry points; AI goals should become orchestration shells over extracted planner/transfer helpers; and structure/build logic should be consolidated into one canonical server-side path. This gives the team the lowest-churn route to finish mechanics while making testing possible.

**Major components:**
1. **Bootstrap + compat layer** — Forge wiring, registries, config, dependency/version setup, 1.21.1-specific integration points.
2. **Domain entities** — authoritative worker/work-area state, ownership, bindings, NBT persistence, synced data.
3. **Packet application layer** — centralized permission, chunk-loaded, and target-resolution checks for all mutating actions.
4. **Goal shells + planner helpers** — tick orchestration separated from target selection, storage transfer rules, and build planning.
5. **Structure/build subsystem** — canonical scan/load/rotate/material/entity-restore behavior for builders.
6. **Client UI/rendering** — thin previews and inputs only, never authoritative state.
7. **Test harness** — unit tests for extracted logic, GameTests for in-world behavior, dedicated-server smoke validation.

### Critical Pitfalls

The pitfall research reinforces the same story: the danger is shipping something that compiles but is not trustworthy on real servers. The highest-risk issues are dependency dead-ends, client/server authority leaks, fragile GUI/entity resolution, regression-prone AI state machines, and structure/build drift.

1. **Compile-only porting** — avoid by validating Forge/Recruits/toolchain compatibility first and smoke-testing runtime flows early.
2. **Client-side assumptions in server paths** — avoid by auditing side boundaries and making dedicated-server validation mandatory.
3. **Trusting client packets** — avoid by centralizing authorization, target re-resolution, and loaded-chunk checks server-side.
4. **Entity/menu/persistence fragility** — avoid by replacing null/no-op flows, auditing synced vs persistent state, and testing relog/reload cases.
5. **AI and structure regressions hidden in giant classes** — avoid by extracting helper seams, stabilizing one profession at a time, and adding round-trip/GameTests.

## Implications for Roadmap

Based on research, suggested phase structure:

### Phase 1: Dependency Gate and 1.21.1 Baseline
**Rationale:** The Recruits dependency is the only clear hard blocker; there is no point planning feature completion until the release target is technically viable.
**Delivers:** Confirmed Forge 1.21.1 + Java 21 + MDK baseline, cleaned build, validated dependency matrix, removed obsolete build hazards.
**Addresses:** Foundation for every feature; especially dedicated-server viability and port/bootstrap correctness.
**Avoids:** Compile-only porting, dependency dead-ends, obsolete repo/build drift.

### Phase 2: Server-Authority and Multiplayer Hardening
**Rationale:** Assignment, editing, storage, and UI flows are worthless if packet trust and side leaks make the mod unsafe on servers.
**Delivers:** Centralized command/applicator layer, ownership/team checks, loaded-chunk validation, side-safe GUI/network flows.
**Addresses:** Worker hire/ownership, work-area editing, multiplayer permissions, status/reporting reliability.
**Implements:** Packet application layer and thin-client pattern.
**Avoids:** Client packet trust, dedicated-server crashes, menu/entity desync.

### Phase 3: Persistence, Menu, and Testable Core Extraction
**Rationale:** Before finishing behavior, the project needs stable seams and a safety net around the riskiest logic.
**Delivers:** Audited entity state, non-null failure handling, extracted storage/build planners, first unit tests and GameTests.
**Addresses:** Persistence across relog/restart, menu reliability, inventory loop correctness groundwork.
**Uses:** JUnit, Forge GameTest, entity-as-persistence-boundary architecture.
**Avoids:** Hidden regressions, flaky packet-driven GUIs, save/load bugs.

### Phase 4: Core Profession Recovery (Farm, Mine, Build, Storage)
**Rationale:** This is the actual MVP. These loops are the acceptance bar players will use to judge the revival.
**Delivers:** Stable storage withdraw/deposit behavior, complete farmer/miner/builder loops, clear blocked states, recall/unstick behavior.
**Addresses:** Table-stakes automation and publicly implied existing mechanics.
**Avoids:** Gameplay deadlocks, item loss/dupes, one-profession-fixed-another-broken churn.

### Phase 5: Structure Pipeline Hardening and Builder Finish
**Rationale:** Builder quality depends on canonical structure round-tripping; it deserves dedicated attention rather than being buried in generic AI work.
**Delivers:** Scan→save→load→rotate→build parity, correct material derivation, entity/work-area restoration, builder resume behavior.
**Addresses:** Builder/build-area completion and required-resources UX integrity.
**Avoids:** Rotation drift, broken templates, mismatched material lists.

### Phase 6: 1.21.1 Integration, Performance, and Release Polish
**Rationale:** Once behavior is stable, finish version-specific client/render/pathing fixes and prove the mod can survive real server load.
**Delivers:** Final 1.21.1 integration fixes, Recruits/pathing reconciliation, throttled scans/caching, dedicated-server release checklist.
**Addresses:** Multiplayer shippability, larger worker counts, final parity.
**Avoids:** TPS collapse, client-only false confidence, release-day regressions.

### Phase Ordering Rationale

- Dependency validation comes first because Recruits compatibility can block the entire 1.21.1 target.
- Server-authority hardening comes before feature completion because assignment, storage, and builder flows all rely on safe packet/entity mutations.
- Core extraction and tests must precede deep mechanic fixes so the team can separate logic regressions from API-port noise.
- Farm/mine/build/storage belong together as the MVP because they share the same work-area, inventory, persistence, and pathing foundations.
- Structure hardening is separated because builder correctness is a distinct high-risk subsystem with its own round-trip failure modes.
- Performance/polish belongs last because optimization before correctness will hide, not solve, the core recovery problems.

### Research Flags

Phases likely needing deeper research during planning:
- **Phase 1:** Recruits 1.21.1 compatibility/API shape is still only medium-confidence and should be verified immediately.
- **Phase 5:** Structure/template round-trip behavior may need targeted research if the current code and file formats diverge from 1.21.1 expectations.
- **Phase 6:** Recruits pathfinding integration and performance under multi-worker load may need focused profiling and compatibility research.

Phases with standard patterns (skip research-phase):
- **Phase 2:** Server-authoritative packet validation and side separation are well-documented Forge patterns.
- **Phase 3:** Entity persistence audits, unit-test extraction, and GameTest setup follow established Forge practices.
- **Phase 4:** Recovering farm/mine/storage loops should rely mostly on codebase-guided implementation, not new domain research.

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | HIGH | Strongly grounded in official Forge docs, MDK artifacts, Maven metadata, and clear Java 21/FG7 requirements; only Recruits availability is softer. |
| Features | HIGH | Well-supported by current mod positioning, recent changelogs, and strong category benchmarks for worker-automation mods. |
| Architecture | MEDIUM-HIGH | Based on direct codebase-aware analysis plus Forge architectural guidance; exact pre-port extraction effort still needs implementation validation. |
| Pitfalls | HIGH | Closely aligned with known Forge multiplayer/porting failure modes and codebase-specific concerns. |

**Overall confidence:** MEDIUM-HIGH

### Gaps to Address

- **Recruits 1.21.1 release path:** confirm whether an official compatible build exists or whether the project must carry a fork before roadmap lock.
- **Exact 1.21.1 rendering/menu breakpoints:** validate during implementation because these are sensitive to current code shape even though the churn categories are known.
- **Current builder/template fidelity:** verify how much of the structure pipeline is already functionally correct versus merely present in code.
- **Performance envelope:** define acceptable worker-count and area-size targets early so optimization is measured against real release criteria.

## Sources

### Primary (HIGH confidence)
- Forge 1.21.x official docs — getting started, dedicated-server testing, GameTest, data generation, networking, sides, menus, entity sync, saved data, porting.
- Official Forge 1.21.1 downloads, MDK zip, Forge Maven metadata, and ForgeGradle metadata — target versions and build baseline.
- Local project research inputs — `.planning/research/STACK.md`, `FEATURES.md`, `ARCHITECTURE.md`, `PITFALLS.md`.
- Local codebase context — `.planning/PROJECT.md`, `.planning/codebase/ARCHITECTURE.md`, `.planning/codebase/CONCERNS.md`, `.planning/codebase/TESTING.md`.
- CoreLib Maven metadata — confirms 1.21.1 artifact availability.

### Secondary (MEDIUM confidence)
- ChampionAsh5357 1.20.6→1.21 migration primer — practical migration breakpoints and API churn categories.
- Villager Workers Modrinth project/version APIs — current public feature expectations and 2024-2025 changelog priorities.
- MineColonies wiki — current player expectations for worker-management, logistics, permissions, and blocked-state UX.
- Modrinth ecosystem search for comparable worker mods.

### Tertiary (LOW confidence)
- No low-confidence source materially shaped the recommendations; the main uncertainty is missing official confirmation around Recruits 1.21.1 readiness rather than low-quality sourcing.

---
*Research completed: 2026-04-05*
*Ready for roadmap: yes*
