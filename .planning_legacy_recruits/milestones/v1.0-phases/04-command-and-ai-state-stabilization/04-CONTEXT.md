# Phase 4: Command and AI State Stabilization - Context

**Gathered:** 2026-04-06
**Status:** Ready for planning

<domain>
## Phase Boundary

Stabilize core recruit command handling and major AI state transitions under server authority. This phase should harden validation on the server side, make invalid command flows fail predictably, and lock down intended command and AI behavior with tests without turning the work into a full persistence/network rewrite or a broad architecture redesign.

</domain>

<decisions>
## Implementation Decisions

### Command Authority and Validation
- Keep command handling server-authoritative and harden validation on the server side rather than moving command truth toward the client.
- Invalid or unauthorized command targets should fail predictably with diagnostics or safe no-op behavior where practical, instead of silent behavior.
- Preserve the current nearby-owned-recruits command radius as the command-selection contract for this phase and encode it with better coverage.
- Prioritize authorization hardening on the highest-risk packet paths first: movement/formation, attack/shields, and patrol/scout state setters.

### AI State Model Stabilization
- Stabilize behavior first and extract narrow testable seams where useful rather than attempting a full command-state redesign.
- First-class regression targets should include move-to-position handoff, hold/return behavior, patrol leader patrol/combat transitions, and scout/messenger task cycles.
- Tests should lock down observable state transitions and safe recovery behavior rather than every internal flag mutation.
- When Phase 4 finds clear logic faults or meaning drift in command/AI state handling, fix them deliberately with tests instead of preserving legacy accidents.

### Test Strategy for Commands and AI
- Use JVM tests for extracted decision and validation seams, and GameTests for packet-path coverage plus live AI transition behavior.
- The representative coverage baseline should include core recruit commands plus the most risk-prone leader and scout control paths, not every packet class equally.
- Leave behind reusable command and AI scenario templates plus helper seams that later persistence and networking phases can extend.
- Diagnostics should surface through explicit assertions, logged or safe failure paths, and tests that prove no desync or stuck-state behavior occurred.

### Scope Boundaries
- Broad persistence/network sync redesign and deep naval/battle performance work stay out of scope unless directly blocking command and AI reliability.
- Patrol, scout, and messenger behavior belongs in scope where it is part of command/AI reliability or known loop-prone flows, not as separate feature expansion.
- Silent no-op command paths should be converted into predictable safe degradation with diagnostics where practical.
- Narrow seam extraction is allowed when it reduces command/AI risk or improves later migration readiness, but this phase should not become a broad architecture rewrite.

### the agent's Discretion
- Exact seam names, helper APIs, and packet-by-packet prioritization, as long as the locked authority, testing, and scope choices above are preserved.
- Exact diagnostics wording and logging/assertion style, provided invalid flows stop being silent and remain safely bounded.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase Definition
- `.planning/ROADMAP.md` — Phase 4 goal and success criteria for server-authoritative commands and stable AI transitions.
- `.planning/REQUIREMENTS.md` — `CMD-01` through `CMD-04`, which define command authority, AI loop safety, automated command/AI verification, and predictable failure behavior.

### Prior Decisions
- `.planning/phases/03-battle-and-formation-regression-lockdown/03-CONTEXT.md` — preserves nearby-owned-recruits command radius and the preference for observable behavior assertions over private-field contracts.
- `.planning/phases/02-layered-test-harness-foundations/02-CONTEXT.md` — keeps the split JVM/GameTest harness as the foundation for new command and AI coverage.
- `.planning/phases/01-build-reproducibility-baseline/01-CONTEXT.md` — keeps `./gradlew check` as the canonical verification entrypoint with distinct unit-test and game-test stages.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `src/test/java/com/talhanation/recruits/network/MessageMovementCodecTest.java` and the shared JVM test helpers already provide a seed seam for command-packet validation coverage.
- `src/gametest/java/com/talhanation/recruits/gametest/battle/FormationRecoveryGameTests.java` plus the existing GameTest support classes already exercise packet-driven command paths and live recruit behavior.
- `src/main/java/com/talhanation/recruits/entities/ai/controller/BattleTacticDecider.java` shows a pattern for extracting narrow JVM-testable AI seams from heavier runtime controllers.

### Established Patterns
- Client UI sends packets that dispatch server-side into `CommandEvents` and entity mutations.
- `AbstractRecruitEntity` uses a mixed numeric `followState` plus derived booleans and ad hoc fields, making meaning drift and invalid combinations a real risk area.
- Patrol leader logic is already a more explicit state machine, while scout and messenger behavior include loop-prone task cycles that can be isolated as regression targets.

### Integration Points
- `src/main/java/com/talhanation/recruits/network/MessageMovement.java`
- `src/main/java/com/talhanation/recruits/network/MessageFormationFollowMovement.java`
- `src/main/java/com/talhanation/recruits/network/MessageAttack.java`
- `src/main/java/com/talhanation/recruits/network/MessageShields.java`
- `src/main/java/com/talhanation/recruits/network/MessagePatrolLeaderSetPatrolState.java`
- `src/main/java/com/talhanation/recruits/network/MessagePatrolLeaderSetRoute.java`
- `src/main/java/com/talhanation/recruits/network/MessageScoutTask.java`
- `src/main/java/com/talhanation/recruits/CommandEvents.java`
- `src/main/java/com/talhanation/recruits/entities/AbstractRecruitEntity.java`
- `src/main/java/com/talhanation/recruits/entities/AbstractLeaderEntity.java`
- `src/main/java/com/talhanation/recruits/entities/MessengerEntity.java`
- `src/main/java/com/talhanation/recruits/entities/ScoutEntity.java`

</code_context>

<specifics>
## Specific Ideas

- Keep the current command radius semantics for this phase and make them explicit through tests instead of changing player-facing reach.
- Prefer narrow state/validation seam extraction over a full command-model rewrite.
- Turn silent invalid command flows into diagnosed safe degradation where practical.

</specifics>

<deferred>
## Deferred Ideas

- Broad persistence/network sync redesign.
- Deep naval combat and battle-performance work not directly required for command/AI reliability.

</deferred>

---

*Phase: 04-command-and-ai-state-stabilization*
*Context gathered: 2026-04-06*
