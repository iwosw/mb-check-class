# Phase 2: Runtime Unification Design - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in `02-CONTEXT.md`.

**Date:** 2026-04-11T11:47:25+07:00
**Phase:** 02-runtime-unification-design
**Areas discussed:** Branding & metadata, Workers asset namespace, Compatibility promise, Config surface

---

## Branding & metadata

| Option | Description | Selected |
|--------|-------------|----------|
| BannerMod-first | Treat `bannermod` as the public identity everywhere practical. | ✓ |
| Recruits-with-Workers | Keep public branding closer to Villager Recruits with Workers described as bundled. | |
| Dual-brand transitional | Use a temporary combined presentation before a later cleanup. | |

**User's choice:** BannerMod-first
**Notes:** Public identity is already locked around `bannermod`; remaining Recruits-facing metadata should be treated as transitional debt.

---

## Workers asset namespace

| Option | Description | Selected |
|--------|-------------|----------|
| Active bannermod, legacy kept | Keep moving active runtime lookups to `bannermod` while preserving `assets/workers/**` as a compatibility layer. | |
| Full bannermod migration | Plan toward moving nearly all worker assets and translation ownership into `bannermod`. | ✓ |
| Long-term mixed namespaces | Intentionally preserve `workers` as a stable internal resource namespace long-term. | |

**User's choice:** Full bannermod migration
**Notes:** The current mixed asset layout is acceptable only as migration state, not as the intended end-state.

---

## Compatibility promise

| Option | Description | Selected |
|--------|-------------|----------|
| Critical-path guarantee | Guarantee only confirmed world-critical/runtime-critical migration paths. | |
| Broad best-effort support | Preserve old worlds and common external references when practical. | |
| Aggressive full migration | Define a stronger migration contract across legacy surfaces. | ✓ |

**User's choice:** Aggressive full migration
**Notes:** Clarified in follow-up: this means strong forward migration of the merged mod's own legacy state, but no compatibility guarantee for old standalone mods or outside integrations that require a separate live `workers` mod identity.

---

## Config surface

| Option | Description | Selected |
|--------|-------------|----------|
| BannerMod-owned config | Target one BannerMod-facing config surface, using migration support as needed. | ✓ |
| Keep separate Workers config | Intentionally preserve a separate Workers config surface long-term. | |
| Hybrid transition | Keep separate config for now, but migrate later. | |

**User's choice:** BannerMod-owned config
**Notes:** Separate Workers config registration can remain only as a migration seam, not as the intended final ownership model.

---

## the agent's Discretion

- Exact ordering of metadata, asset, config, and save-state migration steps.
- Whether migration uses temporary dual-read seams or direct rewrites per surface.

## Deferred Ideas

None.
