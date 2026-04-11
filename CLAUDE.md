<!-- GSD:project-start source:.planning/PROJECT.md -->
## Project

**BannerMod Merge Workspace**

This workspace is the staged merge of the Forge mods currently living in `recruits/` and `workers/`. The active goal is to converge them into one build entrypoint, one runtime mod, and one planning context without losing either codebase's current behavior, tests, or historical planning artifacts.

**Current merge stance:** `recruits` is the runtime base. `workers` is preserved as a legacy subsystem to be absorbed incrementally.
<!-- GSD:project-end -->

<!-- GSD:workflow-start -->
## Workflow

- Use `.planning/` as the active planning context.
- Use `.planning_legacy_recruits/` and `.planning_legacy_workers/` as archived source contexts.
- Prefer the real code over legacy plans when they disagree, and record the conflict in `MERGE_NOTES.md`.
<!-- GSD:workflow-end -->
