# Phase 9: Verification & Release Confidence - Context

**Gathered:** 2026-04-06
**Status:** Ready for planning

<domain>
## Phase Boundary

Consolidate the recovered mod into a release-confidence baseline by validating that key gameplay seams are covered by automated tests, that dedicated-server startup and representative gameplay flows are exercised with the current recovered code, and that there is one defined final smoke path for the milestone, without adding broad new gameplay work or redesigning the test harness at the finish line.

</domain>

<decisions>
## Implementation Decisions

### Verification Scope
- Phase 9 should consolidate and close release-confidence gaps rather than introduce new gameplay features.
- Existing JUnit seam coverage is the primary test foundation; add only the smallest remaining release-confidence checks that materially improve the recovered baseline.

### Dedicated-Server Confidence
- Dedicated-server validation remains a required release-confidence gate.
- Prefer verifying the already recovered flows on the server-authoritative path rather than inventing a new large test harness late in the milestone.

### Final Smoke Path
- Phase 9 should define one final smoke-test path that exercises the recovered baseline without trying to cover every profession exhaustively.
- The smoke path should build on already stabilized flows from earlier phases.

### Scope Boundaries
- Avoid broad test-harness redesign or new feature work in this phase.
- If a small helper or documentation artifact is needed to make verification reproducible, it is in scope.

### the agent's Discretion
- Choose the smallest remaining high-value test seams or verification docs needed to satisfy release confidence.
- Choose the final smoke path that best represents the recovered mod baseline with the least manual complexity.
- Decide whether remaining verification work belongs in code tests, server run validation, or milestone documentation based on the least invasive correct finish.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase Definition
- `.planning/ROADMAP.md` §Phase 9: Verification & Release Confidence — Defines the goal and success criteria for final validation.
- `.planning/REQUIREMENTS.md` §`QUAL-01`, `QUAL-02` — Defines the quality requirements this phase owns.

### Project Constraints
- `.planning/PROJECT.md` — Confirms recovery-first scope and the requirement for automated tests plus dedicated-server validation.
- `.planning/STATE.md` — Carries the current project position and milestone state.
- Earlier phase contexts and summaries under `.planning/phases/` — Define which flows are considered recovered and therefore candidates for the final smoke path.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `src/test/java/com/talhanation/workers/**`: The codebase now has focused JUnit coverage for work-area rules, worker control/recovery, storage request/deposit rules, farming continuity, mining/merchant rules, and builder progress.
- `build.gradle`: JUnit Platform is configured and Forge run configs already exist for client/server verification.
- Prior phase summaries in `.planning/phases/**`: These capture the intended proof points and manual verification routes already used during recovery.

### Established Patterns
- Recent recovery work extracted small pure logic seams and covered them with JUnit tests instead of adding a large bespoke test framework.
- Manual runtime proof has been kept to representative smoke paths rather than exhaustive profession-by-profession acceptance suites.
- Dedicated-server startup and startup-critical validation were already proven important in early phases and remain part of final release confidence.

### Integration Points
- Final verification will likely combine `./gradlew test`, `./gradlew compileJava -x test`, and a dedicated-server/client smoke path assembled from earlier-phase verified flows.
- If missing, the main deliverable may be a reproducible verification artifact rather than a large code change.

</code_context>

<specifics>
## Specific Ideas

- Prefer a small number of trustworthy verification steps over a sprawling checklist nobody will rerun.
- Use the already stabilized farmer/control/authoring/storage paths as the likely backbone of the final smoke route.
- Treat dedicated-server confidence as a release gate, not optional polish.

</specifics>

<deferred>
## Deferred Ideas

- Large GameTest expansion and port-target verification remain out of scope for this v1 closeout.

</deferred>
