# Merge Requirements

## Required

- [x] BOOT-01 — Root workspace has one active build entrypoint.
- [x] BOOT-02 — Root workspace has one active planning context.
- [x] BOOT-03 — Legacy planning context from both source mods remains preserved.
- [x] BOOT-04 — Merge conflicts between plans and code are documented.
- [ ] BOOT-05 — Runtime merge work proceeds in explicit stages rather than hidden partial rewrites.

## Deferred Until Full Runtime Merge

- Unified save-data and packet compatibility guarantees.
- Retirement of `recruits/` and `workers/` as standalone source roots.

## Completed During Stabilization

- Final unified mod id and artifact identity are active as `bannermod`.
- Unified registry namespace for Workers content now routes through the active `bannermod` runtime namespace.
- Active runtime accepts legacy `workers:*` ids on confirmed critical compatibility paths (registry remaps and structure/build NBT migration) without reopening a second live namespace.
- Root workspace has a documented merged codebase baseline covering active runtime paths, legacy archives, verification entrypoints, and current open risks.
- Root workspace has lightweight smoke/regression coverage for merged Workers runtime helpers and build-flow progress helpers without requiring full runtime E2E.
