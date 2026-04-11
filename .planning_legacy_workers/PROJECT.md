# Villager Workers Revival

## What This Is

Villager Workers is an unreleased Minecraft Forge mod that adds worker villagers, work areas, and supporting UI so players can assign villagers to specialized jobs and automate in-world tasks. The current codebase already contains the core entity, AI, networking, GUI, and structure-handling foundations, but the mod is incomplete and still targets Minecraft 1.20.1. This project is to turn the existing implementation into a working 1.21.1 release-ready baseline for players running both client and dedicated server environments.

## Core Value

The mod must reliably let players use the worker-villager mechanics already designed in the codebase without critical bugs or missing core loops.

## Requirements

### Validated

- ✓ Forge mod bootstrap, registry wiring, config registration, and packet channel setup exist in `src/main/java/com/talhanation/workers/WorkersMain.java` — existing
- ✓ Multiple worker entity types, profession AI goals, and persistent worker state already exist across `src/main/java/com/talhanation/workers/entities/` and `src/main/java/com/talhanation/workers/entities/ai/` — existing
- ✓ Work-area entities and client/server editing flows already exist across `src/main/java/com/talhanation/workers/entities/workarea/`, `src/main/java/com/talhanation/workers/network/`, and `src/main/java/com/talhanation/workers/client/gui/` — existing
- ✓ Build-template scanning/loading infrastructure already exists in `src/main/java/com/talhanation/workers/world/StructureManager.java` and `src/main/java/com/talhanation/workers/client/gui/BuildAreaScreen.java` — existing

### Active

- [ ] Complete and stabilize every core worker/work-area mechanic that is already implied by the codebase, using code as the primary source of truth and README/older notes as secondary sources
- [ ] Port the mod from Forge `1.20.1-47.4.1` to a working Minecraft `1.21.1` target
- [ ] Add automated test coverage for the codebase where it meaningfully protects mechanics, serialization, validation, and regression-prone logic
- [ ] Achieve a clean build and runnable mod state without critical crashes or broken core gameplay scenarios
- [ ] Verify the key gameplay flows on both normal gameplay and dedicated server usage
- [ ] Add v2 follow-up features for explicit worker-to-work-area assignment and a courier worker role after the recovery milestone is complete

### Out of Scope

- Brand-new feature expansion beyond the intent already present in code/notes — this work is a recovery and completion effort, not a greenfield redesign
- Supporting legacy Minecraft versions alongside `1.21.1` — the goal is one stable target version
- Replacing the mod's established architecture unless required for correctness or the 1.21.1 port — minimize churn while finishing the existing design

## Context

This is a brownfield recovery project for an unfinished mod. The codebase is Java 17 + Gradle + Minecraft Forge, currently configured for Minecraft 1.20.1 in `build.gradle`, `gradle.properties`, and `src/main/resources/META-INF/mods.toml`. The mod depends heavily on the Recruits ecosystem and uses entity-centric game logic rather than a service-oriented application structure. Existing codebase mapping shows substantial implementation across worker entities, AI goals, work-area entities, client screens, packet handlers, and structure tooling, but no meaningful automated test setup was detected.

The user's definition of success is functional parity with the mechanics already designed in the repository, with permission to add missing pieces where the intended mechanic cannot otherwise work. Source-of-truth order is: existing code first, then README, then older ideas/notes if needed. The finished result must work on Minecraft 1.21.1, include automated tests, build cleanly, and be validated against key gameplay scenarios, including dedicated server behavior.

## Constraints

- **Tech stack**: Stay within the existing Java/Gradle/Forge mod stack unless the 1.21.1 port requires targeted dependency or API changes — preserve the established codebase shape where possible
- **Compatibility**: Final target is Minecraft `1.21.1` with dedicated server support — this defines acceptance for the port
- **Dependency**: The mod currently has a mandatory dependency on Recruits per `src/main/resources/META-INF/mods.toml` — changes must account for its API and version availability on 1.21.1
- **Quality**: Core mechanics must be covered by automated tests where practical and by explicit gameplay verification for end-to-end flows — correctness matters more than raw speed of delivery
- **Scope control**: Existing code is the primary source of truth — avoid speculative redesigns or unrelated new systems

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Existing code is the primary source of truth | The repository already encodes the intended mechanics more reliably than vague recollection | — Pending |
| Missing glue code may be added when required to finish an intended mechanic | Some mechanics may be partially implemented and need completion rather than strict preservation of incomplete behavior | — Pending |
| Done means functional parity plus stability, tests, clean build, and dedicated-server validation | The goal is a working mod baseline, not just a successful compile | — Pending |
| The target release version is Minecraft 1.21.1 only | Focus prevents splitting effort across multiple version lines | — Pending |
| Workers must check inventory before complaining about missing tools/materials | Avoid false blocked states when needed items like hoes, buckets, or seeds are already carried or available in configured storage | Accepted 2026-04-06 |

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
2. Core Value check -> still the right priority?
3. Audit Out of Scope -> reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-04-06 after Phase 4 acceptance*
