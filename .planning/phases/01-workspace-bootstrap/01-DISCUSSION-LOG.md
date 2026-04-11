# Phase 1: Workspace Bootstrap - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md - this log preserves the alternatives considered.

**Date:** 2026-04-11
**Phase:** 01-workspace-bootstrap
**Areas discussed:** Canonical docs, Verification baseline, Legacy archive rules, Workspace boundaries

---

## Canonical docs

### Mandatory doc set
| Option | Description | Selected |
|--------|-------------|----------|
| Core four | Always read `.planning/PROJECT.md`, `.planning/REQUIREMENTS.md`, `.planning/ROADMAP.md`, and `.planning/STATE.md` first. | ✓ |
| Core plus readiness docs | Require the core four plus `.planning/CODEBASE.md` and `.planning/VERIFICATION.md`. | |
| Merge docs first | Prioritize `MERGE_NOTES.md` and `MERGE_PLAN.md` ahead of the planning files. | |

### Merge-readiness docs after first pass
| Option | Description | Selected |
|--------|-------------|----------|
| Required for merge work | For merge-related phases, also require `.planning/CODEBASE.md`, `.planning/VERIFICATION.md`, and `MERGE_NOTES.md` before planning. | ✓ |
| Nice-to-have references | Helpful but skippable unless stuck. | |
| Only when phase-specific | Read only if the phase obviously touches those concerns. | |

### Conflict rule
| Option | Description | Selected |
|--------|-------------|----------|
| Root wins, note conflict | Treat root code and active root planning docs as authoritative, and record legacy disagreement in `MERGE_NOTES.md`. | ✓ |
| Ask before deciding | Pause on every legacy conflict. | |
| Prefer oldest plan | Follow legacy plans even if current root code differs. | |

### `MERGE_PLAN.md` status
| Option | Description | Selected |
|--------|-------------|----------|
| Historical rationale | Use it for merge background, not as a higher authority than active root docs/code. | ✓ |
| Active requirements | Treat it as a current spec. | |
| Ignore it unless asked | Keep it out of normal reads. | |

**User's choice:** Core planning files first, merge-readiness docs required for merge work, root docs/code win on conflict, and `MERGE_PLAN.md` remains historical rationale.
**Notes:** This locks a layered read order for downstream agents instead of treating every doc as equal authority.

---

## Verification baseline

### Default baseline
| Option | Description | Selected |
|--------|-------------|----------|
| Compile + resources + test | Run `./gradlew compileJava`, `./gradlew processResources`, and `./gradlew test`. | |
| Full check always | Run `./gradlew check` every time, including the GameTest stage. | ✓ |
| Compile + test only | Skip `processResources` unless clearly needed. | |

### Current GameTest interpretation
| Option | Description | Selected |
|--------|-------------|----------|
| Run it anyway | Keep GameTest in baseline even while coverage is light. | |
| Best effort only | Default to `check`, but fall back if GameTest is low-value. | |
| Skip until populated | Keep GameTest out of the present default until root coverage is meaningful. | ✓ |

### Clarified current rule
| Option | Description | Selected |
|--------|-------------|----------|
| Use compile/resources/test now | Today, use `compileJava`, `processResources`, and `test`; later promote to `check`. | ✓ |
| Use check anyway | Require `check` now. | |
| Agent discretion | Let agents choose per change. | |

### When to add GameTest
| Option | Description | Selected |
|--------|-------------|----------|
| Gameplay/runtime changes | Add `runGameTestServer` when gameplay or runtime behavior changes. | ✓ |
| Any source change | Run it for every change. | |
| Only when user asks | Never include it by default. | |

**User's choice:** The long-term target is stricter `check` coverage, but the current default remains `compileJava`, `processResources`, and `test` until root GameTests are meaningful; add `runGameTestServer` for gameplay/runtime changes.
**Notes:** An initial `Full check always` answer was narrowed into an explicit transitional rule because the root GameTest source set is still mostly placeholder.

---

## Legacy archive rules

### Legacy planning archives
| Option | Description | Selected |
|--------|-------------|----------|
| Historical reference only | Use legacy planning trees for background and provenance, not as active requirements by default. | ✓ |
| Fallback requirements | Use them whenever root docs are incomplete. | |
| Equal authority | Treat legacy and active planning docs as peers. | |

### Preserved source trees
| Option | Description | Selected |
|--------|-------------|----------|
| Active code if still wired | Treat preserved source as live implementation when the root build still compiles or copies from it. | ✓ |
| Read-only legacy | Avoid touching preserved source unless asked. | |
| Migration targets only | Read them for extraction, but prefer moving code out instead of editing there. | |

### Legacy-only patterns
| Option | Description | Selected |
|--------|-------------|----------|
| Reference, then verify in root | Use archives for background, but confirm in active root code/build before acting. | ✓ |
| Adopt it directly | Carry it forward without extra verification. | |
| Ignore archive patterns | Never use them even for historical hints. | |

### Out-of-scope ideas
| Option | Description | Selected |
|--------|-------------|----------|
| Defer to later phase | Capture outside-bootstrap ideas as deferred/backlog items. | ✓ |
| Fold into bootstrap | Expand Phase 1 scope. | |
| Ignore it | Do not record it. | |

**User's choice:** Legacy planning archives are historical reference only; preserved source trees remain live if still wired into the root build; legacy-only patterns need root verification before use; out-of-scope ideas should be deferred.
**Notes:** This separates archived planning from still-active brownfield code.

---

## Workspace boundaries

### Role of root `src/`
| Option | Description | Selected |
|--------|-------------|----------|
| Merge-only support | Keep root `src/` for cross-tree scaffolding, merged resources, and root smoke/tests. | |
| Shared home for all new code | Prefer root `src/` as the new shared home. | ✓ |
| Temporary landing zone | Allow broad short-term use, then clean up later. | |

### Breadth of the shift
| Option | Description | Selected |
|--------|-------------|----------|
| New shared code only | Start with new shared code, leave existing code mostly in place. | |
| Start migrating existing code too | Use root `src/` as the new default home and begin relocation. | ✓ |
| Everything new and touched | Move broadly whenever files are edited. | |

### Role of `recruits/` and `workers/`
| Option | Description | Selected |
|--------|-------------|----------|
| Domain-owned leftovers | Keep both trees as valid homes while shared code trends rootward. | |
| Permanent split domains | Keep both trees as long-term primary homes. | |
| Mostly migration sources | Treat them mainly as source trees to absorb from over time. | ✓ |

### Planning posture
| Option | Description | Selected |
|--------|-------------|----------|
| Track the target architecture | `.planning/` should document the intended root-owned end state. | ✓ |
| Mirror current code only | Keep docs strictly descriptive of today's layout. | |
| Avoid boundary claims | Keep docs vague and let code drift lead. | |

**User's choice:** Root `src/` should become the long-term shared home now, including gradual migration of existing code; `recruits/` and `workers/` are primarily source trees to absorb from; planning should document the intended root-owned architecture.
**Notes:** This intentionally overrides the current structure docs' more conservative framing of root `src/` as merge-only support.

---

## the agent's Discretion

- Exact sequencing and granularity of code migration from `recruits/` and `workers/` into root `src/`.
- The precise threshold for promoting root `check` to the default validation baseline once GameTests become substantive.

## Deferred Ideas

None.
