# Villager Recruits Stabilization and 1.21.1 Migration Prep

## What This Is

Villager Recruits is a large brownfield Forge mod for Minecraft 1.20.1 focused on recruitable NPC armies, squad control, combat behavior, formations, patrols, faction systems, and related world interactions. This initiative is not about inventing new gameplay; it is about making the unstable dev branch dependable, covering the codebase with automated tests, and reshaping the internals so the actual port to 1.21.1 becomes a short, concrete next step.

## Core Value

The dev branch must become a trustworthy base where the core NPC army mechanics behave predictably, are defended by tests, and are structurally ready for a near-term 1.21.1 migration.

## Requirements

### Validated

- ✓ Recruitable NPC army gameplay exists across multiple entity types and roles, including recruit, ranged, and leader-style units — existing
- ✓ The mod already implements command, patrol, faction, claim, diplomacy, and route-oriented gameplay systems — existing
- ✓ Client-server synchronization for gameplay commands and UI state already exists through a large packet-based network layer — existing
- ✓ Persistent world and team state already exists through `SavedData` managers for claims, factions, groups, teams, and related systems — existing
- ✓ Custom AI, navigation, pathfinding, formations, and behavior orchestration are already present in the entity and pathfinding subsystems — existing
- ✓ The dev branch is stabilized behind reproducible `./gradlew check --continue` verification with JVM tests and Forge GameTests — completed in Phases 1-6 and milestone-audited on 2026-04-09
- ✓ Broad automated coverage now exists across battles, formations, commands, AI, persistence, networking, compat degradation, and migration seams — completed in Phases 2-7
- ✓ The highest-risk logic gaps uncovered during verification were either fixed in code or documented as bounded follow-up work — completed in Phases 3-6
- ✓ Internal seams now isolate the highest-risk version-sensitive areas for the future 1.21.1 port — completed in Phase 7
- ✓ The future 1.21.1 port is now bounded by an inventory, version-surface map, ordered checklist, and handoff brief — completed in Phase 8

### Active

- None. v1 milestone work is complete; future follow-up remains in `REQUIREMENTS.md` v2 and the Phase 8 port-prep artifacts.

### Out of Scope

- Full port to Minecraft 1.21.1 in this initiative — this work should make the port short and well-scoped, but not perform the full migration itself
- New gameplay content, new unit types, or feature expansion — the goal is stabilization, verification, and migration prep rather than growing scope

## Context

This is a brownfield Java 17 / Minecraft Forge 1.20.1 mod with an existing codebase map in `.planning/codebase/`. The architecture remains event-driven around `src/main/java/com/talhanation/recruits/Main.java`, feature-level event coordinators, a deep recruit entity hierarchy under `src/main/java/com/talhanation/recruits/entities/`, a large `src/main/java/com/talhanation/recruits/network/` packet surface, and persistent world state in `src/main/java/com/talhanation/recruits/world/`. The milestone work established a layered automated test harness in `src/test/java` and `src/gametest/java`, published the repo-level `VERIFICATION_MATRIX.md`, hardened the highest-risk battle, command, AI, persistence, and networking surfaces, and extracted migration-ready seams for registration, networking, client sync, persistence orchestration, compat reflection, and async path processing. The current checkout now passes the canonical `./gradlew check --continue` workflow, and the future 1.21.1 effort is bounded by the Phase 8 inventory, version-surface map, checklist, and handoff brief rather than open-ended repo discovery.

## Constraints

- **Tech stack**: Stay within the existing Java 17 + Forge 1.20.1 + Gradle toolchain while preparing future compatibility with 1.21.1 — the current branch still has to build and run in its present environment
- **Behavioral stability**: External gameplay behavior should be preserved unless a change is clearly a bug fix or logic correction — this is stabilization work, not a redesign of player-facing mechanics
- **Testing**: Automated coverage must include both unit-style tests and game tests — the project is not complete with ad hoc manual checking alone
- **Scope**: Verification must cover the full mod surface, with deeper focus on battles, persistence, commands, AI, networking, and formations — these risk areas drive prioritization and roadmap structure
- **Migration prep**: Internal refactors may be substantial if they reduce migration risk or make version-sensitive code easier to isolate — the user explicitly allows bold refactoring in service of the future port

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Prioritize battles, persistence, commands, AI, networking, and formations first | These are the user-identified highest-risk mechanics and the most likely sources of instability | Adopted across Phases 3-6 |
| Aim for broad test coverage, not only a narrow critical path | The user wants the full mod exercised, with deep coverage on critical systems and baseline coverage elsewhere | Adopted across Phases 2-6 |
| Allow substantial internal refactoring when it lowers migration risk | Preparing a near-term 1.21.1 port is part of the goal, so internal seams matter as much as immediate fixes | Adopted across Phases 7-8 |
| Exclude full 1.21.1 port and new content from this initiative | Prevents scope creep and keeps effort focused on stabilization, testing, and migration groundwork | Confirmed at milestone close |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd-transition`):
1. Requirements invalidated? -> Move to Out of Scope with reason
2. Requirements validated? -> Move to Validated with phase reference
3. New requirements emerged? -> Add to Active
4. Decisions to log? -> Add to Key Decisions
5. "What This Is" still accurate? -> Update if drifted

**After each milestone** (via `/gsd-complete-milestone`):
1. Full review of all sections
2. Core Value check - still the right priority?
3. Audit Out of Scope - reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-04-09 after milestone completion*
