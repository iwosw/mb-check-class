# Phase 1: Workspace Bootstrap - Context

**Gathered:** 2026-04-11
**Status:** Ready for planning

<domain>
## Phase Boundary

Establish the merged workspace foundation: one root Gradle entrypoint, one active root `.planning/` context, preserved legacy planning archives, and merge-truth documentation that downstream agents should follow. This phase does not add new gameplay capabilities.

</domain>

<decisions>
## Implementation Decisions

### Canonical docs
- **D-01:** Downstream agents must read `.planning/PROJECT.md`, `.planning/REQUIREMENTS.md`, `.planning/ROADMAP.md`, and `.planning/STATE.md` before planning or implementation.
- **D-02:** For merge-related work, agents must also read `.planning/CODEBASE.md`, `.planning/VERIFICATION.md`, and `MERGE_NOTES.md` as required context, not optional references.
- **D-03:** When historical archives or old plans disagree with current root code or active root docs, the root code and active root docs win; the disagreement should be recorded in `MERGE_NOTES.md`.
- **D-04:** `MERGE_PLAN.md` is historical merge rationale and structure context, not a higher-priority active requirement source than root code or active root planning docs.

### Verification baseline
- **D-05:** Before root GameTests become meaningfully populated, the default validation baseline is `./gradlew compileJava`, `./gradlew processResources`, and `./gradlew test` from the repository root.
- **D-06:** Once root GameTests become meaningful, the intended stricter default is full root `check` coverage.
- **D-07:** Agents should add `./gradlew runGameTestServer` when work touches gameplay/runtime behavior, entity flows, screens, or world interactions.

### Legacy archive rules
- **D-08:** `.planning_legacy_recruits/` and `.planning_legacy_workers/` are historical reference only by default; they are not active requirement sources unless active root docs explicitly point to them.
- **D-09:** Preserved source trees such as `workers/` remain active implementation input when the root build still compiles or copies from them; they are not archival merely because they preserve historical structure.
- **D-10:** If an implementation pattern appears only in a legacy archive or standalone metadata, agents may use it for background but must verify it against the active root code/build before acting on it.
- **D-11:** Ideas that extend beyond this bootstrap phase should be deferred into later phases or backlog notes rather than folded into Phase 1 scope.

### Workspace boundaries
- **D-12:** Root `src/` is the long-term shared home for new code rather than being limited to merge-only support.
- **D-13:** Boundary shift starts now: downstream agents may begin migrating existing code toward root `src/`, not just place new shared code there.
- **D-14:** `recruits/` and `workers/` should be treated primarily as source trees to absorb from over time, with root `src/` becoming the long-term home.
- **D-15:** Active planning files under `.planning/` should describe the target root-owned architecture, not only mirror the current intermediate layout.

### the agent's Discretion
- Exact migration sequencing for moving code from `recruits/` and `workers/` into root `src/`.
- The specific moment when root GameTest coverage is strong enough to promote `./gradlew check` to the default baseline.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Active project context
- `.planning/PROJECT.md` — Project identity, merge stance, constraints, and accepted foundational decisions.
- `.planning/REQUIREMENTS.md` — Required merge outcomes, deferred items, and completed stabilization requirements.
- `.planning/ROADMAP.md` — Phase boundaries and phase ordering for the merged workspace.
- `.planning/STATE.md` — Current milestone, active focus, and latest verified progress state.

### Merge workspace truth and readiness
- `.planning/CODEBASE.md` — Source-of-truth paths, active runtime/build wiring, and current merge risks.
- `.planning/VERIFICATION.md` — Root verification entrypoints and the current recommended validation baseline.
- `MERGE_NOTES.md` — Accepted merge decisions, plan/code conflicts, current layout notes, and compatibility decisions that active work must respect.

### Historical rationale
- `MERGE_PLAN.md` — Historical merge rationale, target structure, and intended absorption order; use for background, not as a higher-priority active spec.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `build.gradle` — Already provides the single merged root build entrypoint, merged source sets, root verification tasks, and resource merge wiring.
- `settings.gradle` — Already establishes the single root project identity as `bannermod`.
- `.planning/codebase/STRUCTURE.md` — Existing structure map that downstream agents can reuse when locating active vs preserved code.
- `.planning/codebase/ARCHITECTURE.md` — Existing merged-runtime architecture map covering the root bootstrap and workers subsystem composition.
- `.planning/codebase/CONVENTIONS.md` — Existing brownfield coding-style guidance for edits across root, recruits, and workers trees.

### Established Patterns
- The active workspace already treats root `.planning/` as the planning source of truth and legacy `.planning_legacy_*` trees as archived context.
- The merged build composes `src/main/java`, `recruits/src/main/java`, and `workers/src/main/java` into one runtime from the root Gradle project.
- Current verification guidance is root-first and task-based: compile, resource merge, unit tests, with GameTest as an additional runtime validation hook.
- Merge decisions and plan/code conflicts are already documented in `MERGE_NOTES.md` rather than being left implicit.

### Integration Points
- Root planning and context generation should write under `.planning/` and `.planning/phases/`.
- Root build and validation decisions connect through `build.gradle` tasks such as `compileJava`, `processResources`, `test`, `check`, and `runGameTestServer`.
- Ongoing structural migration connects to `src/`, `recruits/`, and `workers/`, with the root build remaining the enforcement point for what is still live.

</code_context>

<specifics>
## Specific Ideas

- Merge-related downstream work should follow a two-tier read order: core planning files first, then merge-readiness docs required for merge work.
- Root `src/` should become the real long-term shared home, not remain only a glue layer.
- Planning docs should describe the intended root-owned architecture even while code is still mid-migration.

</specifics>

<deferred>
## Deferred Ideas

None - discussion stayed within phase scope.

</deferred>

---

*Phase: 01-workspace-bootstrap*
*Context gathered: 2026-04-11*
