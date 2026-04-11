# Phase 3: Battle and Formation Regression Lockdown - Context

**Gathered:** 2026-04-05
**Status:** Ready for planning

<domain>
## Phase Boundary

Stabilize representative recruit battles and formation flows with automated coverage focused on ground combat, command-driven formation behavior, and regression-prone battle-density scenarios. This phase should lock down intended combat outcomes and formation behavior without turning every incidental AI quirk into a permanent contract.

</domain>

<decisions>
## Implementation Decisions

### Battle Coverage Baseline
- Ground recruit battles are the core representative baseline for Phase 3; special-case systems such as naval combat are not the starting point.
- The canonical automated baseline should use small deterministic squads rather than only 1v1 micro-cases or large chaotic battles.
- Baseline scenarios should cover mixed melee and ranged recruit groups.
- Assertions should lock down high-level combat outcomes and state transitions rather than tick-perfect choreography.

### Formation Behavior During Combat
- Formations may loosen during engagement, but tests should expect formation intent to recover afterward rather than enforcing rigid lockstep.
- Preserve the current command-driven formation application path through packet handlers and `CommandEvents`, and test observable outcomes through that path.
- Hold-position and return-to-position behavior should be validated through observable recruit behavior rather than internal field implementation details.
- Preserve the current nearby-owned-recruits command radius behavior as an intentional Phase 3 contract unless a later phase deliberately changes it.

### Stress and Regression Detection
- Phase 3 should include defined stress scenarios in addition to deterministic baseline scenarios so battle-density regressions are caught automatically.
- Stress scenarios should be judged by practical stability signals such as successful completion, absence of broken combat-state loops, and bounded behavior, not exact casualty or timing outputs.
- Stress-heavy checks belong in runtime GameTests/verification rather than pure JVM tests.
- Stress scenarios in this phase are for correctness and stability alarms, not full performance certification.

### Edge-Case Scope
- After the core baseline, prioritize ground-battle edge cases around mixed squads, command changes, and formation recovery.
- Deep captain/ship combat coverage is deferred unless it directly blocks the core ground-battle baseline.
- Preserve outward behavior unless tests expose a clear bug or logic fault, in which case it should be fixed deliberately with regression coverage.
- Leave behind reusable battle and formation scenario templates that later command, persistence, and networking phases can extend.

### the agent's Discretion
- Exact squad compositions, structure layouts, and helper API names, as long as they support the locked baseline/stress decisions above.
- Exact pass/fail thresholds for stress scenarios, provided they reflect stability/correctness alarms rather than full performance certification.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase Definition
- `.planning/ROADMAP.md` — Phase 3 goal and success criteria for battle and formation stabilization.
- `.planning/REQUIREMENTS.md` — `BATL-01` through `BATL-04`, which define expected battle behavior, formation stability, regression coverage, and stress-scenario detection.

### Prior Decisions
- `.planning/phases/01-build-reproducibility-baseline/01-CONTEXT.md` — locked build and verification entrypoint decisions this phase must reuse.
- `.planning/phases/02-layered-test-harness-foundations/02-CONTEXT.md` — Phase 2 test-harness direction and the requirement to build on the new split JVM/GameTest structure.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `src/gametest/java/com/talhanation/recruits/gametest/support/RecruitsGameTestSupport.java` and `RecruitsEntityAsserts.java` already provide the runtime harness seed for battle and formation scenarios.
- `src/test/java/com/talhanation/recruits/testsupport/RecruitsFixtures.java`, `MessageCodecAssertions.java`, and `NbtRoundTripAssertions.java` provide reusable JVM-side helpers for smaller pure-logic regression checks.
- `build.gradle` and `BUILDING.md` already keep GameTests as a first-class `check` layer.

### Established Patterns
- Formation application is centralized around packet handlers and `CommandEvents`, with helper logic in `FormationUtils.java`.
- Recruit formation/combat state is encoded on entities through fields and goals such as `isInFormation`, hold-position movement, and return-to-position behavior.
- Leader battle orchestration is centralized around `NPCArmy` snapshots and tactic controllers, making focused regression scenarios practical.

### Integration Points
- `src/main/java/com/talhanation/recruits/CommandEvents.java`
- `src/main/java/com/talhanation/recruits/util/FormationUtils.java`
- `src/main/java/com/talhanation/recruits/entities/AbstractRecruitEntity.java`
- `src/main/java/com/talhanation/recruits/entities/ai/controller/PatrolLeaderAttackController.java`
- `src/gametest/java/com/talhanation/recruits/gametest/**`

</code_context>

<specifics>
## Specific Ideas

- Treat ground recruit battles as the canonical Phase 3 battlefield, not naval combat.
- Prefer representative mixed-squad scenarios over either ultra-minimal micro-tests or only large chaotic fights.
- Use automated stress scenarios as stability alarms without turning this phase into a full performance-tuning effort.

</specifics>

<deferred>
## Deferred Ideas

- Deep captain/ship combat coverage — defer unless it blocks the ground-battle baseline or emerges as a direct regression source.

</deferred>

---

*Phase: 03-battle-and-formation-regression-lockdown*
*Context gathered: 2026-04-05*
