---
phase: 21-source-tree-consolidation-into-bannerlord
verified: 2026-04-15T19:35:00Z
status: gaps_found
score: 5/6 must-haves verified
overrides_applied: 0
gaps:
  - truth: "Unified resources contain no residual recruits:/workers: namespace strings"
    status: partial
    reason: "`src/main/resources/data/minecraft/tags/point_of_interest_type/acquirable_job_site.json` still lists six `recruits:*` entity IDs (recruit, shieldman, bowman, crossbowman, horseman, nomad). Entities now register under `bannermod:*` (see `registry/military/ModEntityTypes.java` which calls `new ResourceLocation(BannerModMain.MOD_ID, ...)` where `MOD_ID = \"bannermod\"`), so the tag references dead IDs and no longer points at the live entity registrations. This was copied verbatim from the recruits clone during Wave 9 consolidation and the namespace rewrite pass missed JSON tag payloads under `data/`."
    artifacts:
      - path: "src/main/resources/data/minecraft/tags/point_of_interest_type/acquirable_job_site.json"
        issue: "Six POI tag values reference `recruits:<job>` IDs that no longer exist under that namespace — should be `bannermod:<job>` to match the registered entity IDs."
    missing:
      - "Rewrite the six tag values from `recruits:<id>` to `bannermod:<id>` (or delete the tag override if no longer needed for BannerMod)."
      - "Sweep `src/main/resources/data/**` for other `recruits:`/`workers:` namespace strings missed by the Wave 9 rewrite (current scan shows only this one file)."
deferred:
  - truth: "`./gradlew compileTestJava` is green"
    addressed_in: "Future test-stabilization slice"
    evidence: "CONTEXT D-22 explicitly scopes outer compile-green gate to `compileJava`; 39 test-tree errors (D-05 `BannerModSettlementBinding` overlap + smoke-test symbol drift on `WorkersSubsystem`/`WorkersRuntime`/`Main.orderedMessageTypes()`) documented in MERGE_NOTES.md and Wave 9 SUMMARY."
---

# Phase 21 Verification — Post-Wave-9 Refresh (2026-04-15)

Phase 21 goal (ROADMAP §Phase 21): *BannerMod becomes one physical codebase instead of one root build that still composes legacy source trees.* Closure criteria (from the ROADMAP description and the earlier draft of this document):

1. Active Java ownership lives under `src/main/java/com/talhanation/bannermod/**` (single canonical namespace).
2. The legacy `com.talhanation.bannerlord.*` namespace is fully retired.
3. The legacy embedded clones (`recruits/`, `workers/`) no longer compile or contribute resources to the outer build.
4. `./gradlew compileJava` is green from the outer repo root.
5. Resources are unified under `src/main/resources/{assets,data}/bannermod/**`.
6. `mods.toml` declares only `modId="bannermod"`; no residual `recruits`/`workers`/`bannerlord` mod-id refs.

## Observable Truths

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | Single source root (clones no longer composed into outer build) | VERIFIED | `build.gradle` `sourceSets.{main,test,gametest}` reference only `src/{main,test,gametest}/{java,resources}` (lines 46–64). No `recruits/src` or `workers/src` srcDir entry. |
| 2 | Single canonical namespace `com.talhanation.bannermod.*` | VERIFIED | `find src/main/java/com/talhanation -maxdepth 1 -type d` returns only `.../bannermod`. 24 `bannermod.*` subpackages present (authority, ai, bootstrap, citizen, client, commands, compat, config, entity, events, governance, inventory, items, logistics, migration, mixin, network, persistence, registry, settlement, shared, util). Grep for `com.talhanation.bannerlord` across `src/` and `build.gradle`: 0 hits. |
| 3 | Legacy `com.talhanation.recruits/workers/bannerlord` FQNs retired from production source | VERIFIED | `grep -r "com\.talhanation\.(recruits\|workers\|bannerlord)" src/main/java` → 0 matches. (Test tree still has `src/test/java/com/talhanation/{recruits,workers}/**` — this is the documented deferred test-tree residue; see `deferred` section.) |
| 4 | `./gradlew compileJava` compile-green gate | VERIFIED | Re-run at verification time: `BUILD SUCCESSFUL in 14s` (daemon forked, `:compileJava UP-TO-DATE`). |
| 5 | Resources unified under `src/main/resources` | VERIFIED | `src/main/resources/{assets/bannermod,data/minecraft,META-INF/{mods.toml,accesstransformer.cfg},logo.png,pack.mcmeta,mixins.bannermod.json}` all present. `assets/bannermod/` contains `blockstates,lang,models,structures,textures`. No `src/main/resources/assets/{recruits,workers}/` exists. |
| 6 | `mods.toml` declares exactly one mod with `modId="bannermod"` | VERIFIED | `grep -c "\[\[mods\]\]" src/main/resources/META-INF/mods.toml` → 1. `modId="bannermod"`. No `"recruits"`, `"workers"`, or `bannerlord` mod-id strings. (The two residual `recruits`/`workers` strings in `mods.toml` are the URL paths `github.com/talhanation/recruits` and `modrinth.com/mod/villager-recruits` — user-facing links, not mod-id declarations.) |

Goal-backward derived truth added during this verification pass:

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 7 | Unified resources contain no residual `recruits:`/`workers:` namespace strings | PARTIAL | `grep -rn '"recruits:\|"workers:' src/main/java/ src/main/resources/` returns 6 hits, all in `src/main/resources/data/minecraft/tags/point_of_interest_type/acquirable_job_site.json` pointing at `recruits:{recruit,shieldman,bowman,crossbowman,horseman,nomad}`. Entities are registered under `bannermod:*` (see `ModEntityTypes` using `BannerModMain.MOD_ID`), so the tag values reference dead IDs. Java source and other resources are clean — previous verification counted "0 hits" because it filtered to `src/main/java` only; the JSON tag was overlooked. See `gaps` in frontmatter. |

**Score:** 5/6 verified against the explicit phase closure criteria (truths 1–6 above). A goal-backward sweep surfaced one partial finding (truth 7) which does not map to a numbered closure criterion but is implicitly required by the "unified resources" goal.

## Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `src/main/java/com/talhanation/bannermod/bootstrap/BannerModMain.java` | Unified mod entrypoint with `MOD_ID = "bannermod"` | VERIFIED | `MOD_ID = "bannermod"` at line 35. Referenced from `WorkersRuntime.MOD_ID = BannerModMain.MOD_ID`. |
| `src/main/java/com/talhanation/bannermod/bootstrap/WorkersRuntime.java` | Reconstructed civilian runtime bootstrap | VERIFIED | Present. Exposes `MOD_ID`, `LEGACY_MOD_ID = "workers"`, and `migrateLegacyId()` used by `WorkersRuntimeLegacyIdMigrationTest`. |
| `src/main/java/com/talhanation/bannermod/ai/military/RecruitAiLodPolicy.java` | Reconstructed from test contract per Wave 9 SUMMARY | VERIFIED | File exists under the documented path; reconstruction recorded in SUMMARY §key-files. |
| `src/main/java/com/talhanation/bannermod/network/messages/military/MessageRecruitCount.java` | Reintroduced as no-op pass-through for packet-ID slot ordering | VERIFIED | Present; rewritten per SUMMARY §key-files. |
| `src/main/resources/META-INF/mods.toml` | Single `[[mods]] modId="bannermod"` block | VERIFIED | Confirmed (Truth 6). |
| `src/main/resources/META-INF/accesstransformer.cfg` | Includes `AxeItem.STRIPPABLES` public AT | VERIFIED | Referenced in Wave 9 SUMMARY key-decisions; file present. |
| `src/main/resources/mixins.bannermod.json` | Single mixin config (`package: com.talhanation.bannermod.mixin`) | VERIFIED | Config lists 8 common mixins + `MixinMinecraft` client mixin under `com.talhanation.bannermod.mixin`. |
| `build.gradle` sourceSets | Outer-only `src/{main,test,gametest}/{java,resources}` | VERIFIED | Lines 46–64. No clone references in srcDirs. |

## Key Link Verification

| From | To | Via | Status | Details |
|------|----|-----|--------|---------|
| `build.gradle` sourceSets | outer `src/**` only | `java.srcDirs` / `resources.srcDirs` | WIRED | No `recruits/src` or `workers/src` composition. |
| POI tag `acquirable_job_site.json` | live recruit entity registrations | entity ID strings | NOT_WIRED | Tag references `recruits:<id>` but live registrations are `bannermod:<id>`. Tag is orphaned from the registrations it used to annotate. |
| `WorkersRuntime.migrateLegacyId` | `workers:*` → `bannermod:*` rewrite | `migrateLegacyId` method | WIRED | Covered by `WorkersRuntimeLegacyIdMigrationTest` (test tree; compile deferred but contract documented). |
| `mixins.bannermod.json` | `com.talhanation.bannermod.mixin.*` classes | mixin package declaration | WIRED | Package path matches existing `src/main/java/com/talhanation/bannermod/mixin/**`. |

## Behavioral Spot-Checks

| Behavior | Command | Result | Status |
|----------|---------|--------|--------|
| Production compile | `./gradlew compileJava` | `BUILD SUCCESSFUL in 14s`, `:compileJava UP-TO-DATE` | PASS |
| Single `[[mods]]` entry | `grep -c "\[\[mods\]\]" src/main/resources/META-INF/mods.toml` | `1` | PASS |
| Zero `bannerlord` FQNs in production | `grep -r "com\.talhanation\.bannerlord" src/main/java build.gradle` | (no output) | PASS |
| Zero `recruits`/`workers` FQNs in production java | `grep -r "com\.talhanation\.(recruits\|workers\|bannerlord)" src/main/java` | 0 matches | PASS |
| Residual namespace strings in unified resources | `grep -rn '"recruits:\|"workers:' src/main/resources/` | 6 hits in POI tag | FAIL |

## Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
|-------------|-------------|-------------|--------|----------|
| SRCMOVE-01 | 21-02 | Canonical shared seam ownership under `bannermod.shared/**` and `bannermod.config`, with `bannermod.{authority,settlement,logistics}` as deprecated forwarders | SATISFIED | Marked `[x]` in REQUIREMENTS.md; preserved by Wave 9 (no regressions to shared-seam subtrees). |
| SRCMOVE-02 | 21-03..21-07 | Active recruit military/shared + worker civilian gameplay re-homed onto `bannermod.{entity,ai,pathfinding,persistence,client}/{shared,military,civilian}/**` | SATISFIED | All expected subpackages present under `bannermod.{entity,ai,persistence,client}.{military,civilian}`; `recruits.*`/`workers.*` owners removed from production java. |
| SRCMOVE-03 | 21-09 | `com.talhanation.recruits/**` reduced to documented compatibility surfaces; no recruit-package file owns live gameplay | SATISFIED | 0 `com.talhanation.recruits` FQNs in production java. Only residue is legacy clone working-tree archives (Option a retention) and deferred test-tree symbols. Marked `requirements-completed: [SRCMOVE-03]` in 21-09 SUMMARY. |
| SRCMOVE-04 | 21-09 | `com.talhanation.workers/**` reduced to enumerated compat surface; canonical civilian ownership under `bannermod.*.civilian/**` | SATISFIED | Same scan: 0 `com.talhanation.workers` FQNs in production java. Civilian registry/entity/client subtrees live under `bannermod.*.civilian`. `WorkersRuntime` is the sole `workers`-named symbol and lives under `bannermod.bootstrap`. Marked `requirements-completed: [SRCMOVE-04]` in 21-09 SUMMARY. |

All four SRCMOVE requirements are fulfilled for production source. SRCMOVE-03/04 coverage assumes the POI tag is *not* a "recruit-package file" (it's a resource file); the namespace-string drift there is flagged as an independent truth-7 gap, not a requirements regression.

## Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
|------|------|---------|----------|--------|
| `build.gradle` | 40 | `group = 'com.talhanation.recruits'` | Info | Gradle project group coordinate is still `com.talhanation.recruits`. Does not affect runtime or compile-green gate; the published `modId` and Java namespace are fully migrated. Worth noting for a future cleanup slice. |
| `build.gradle` | 231–232 | `println` messages reference "BannerMod base source tree plus the merged workers runtime slice" | Info | Accurate post-Wave-9 narration; not a gap. |
| `src/main/resources/data/minecraft/tags/point_of_interest_type/acquirable_job_site.json` | 3–8 | Tag values use `recruits:*` namespace | Warning | See Truth 7 / gaps frontmatter. Functional drift (tag points at dead IDs) rather than a classical stub. |

No TODO/FIXME/placeholder anti-patterns surfaced in the Wave 9 key-files.

## Known Deferred Issues

Per CONTEXT D-22, the outer compile-green gate is scoped to `./gradlew compileJava` only. `./gradlew compileTestJava` surfaces 39 errors covering:

1. Two `BannerModSettlementBinding` classes coexist (`bannermod.shared.settlement` + `bannermod.settlement`) — D-05 defers package overlap reconciliation to a follow-up phase.
2. Integration smoke tests reference `WorkersSubsystem`, `WorkersRuntime`, and `Main.orderedMessageTypes()` with imports/packages that still point at the retired clone namespaces (see `src/test/java/com/talhanation/{recruits,workers}/**` residue).

Both are documented in `MERGE_NOTES.md` and in `21-09-SUMMARY.md §key-decisions`. They do not affect the runtime artifact and are explicitly out of Phase 21 scope.

## Human Verification Required

None blocking phase closure. Recommended (optional) sanity checks before the next phase:

1. Spawn a recruit via `/summon bannermod:recruit` and observe whether villagers still get stolen into the recruit job site (validates whether the stale POI tag has a runtime behavioural effect or is inert).
2. Run the mod in a dev client to confirm assets, lang, and model registrations resolve without `minecraft:missing` textures.

## Gaps Summary

Phase 21 has structurally achieved its goal: the outer repo is a single source root with a single canonical namespace (`com.talhanation.bannermod`), `compileJava` is green, `mods.toml` is unified, and the embedded clones no longer contribute to the build. One narrow residue remains from the Wave 9 resource consolidation: `acquirable_job_site.json` still references six entity IDs under the retired `recruits:` namespace. This is a mechanical fix (rewrite the six strings to `bannermod:*` or delete the tag override) and does not block phase closure in the structural sense, but it is a real functional drift against the "unified resources" goal — entities registered under `bannermod:*` will not appear in the `acquirable_job_site` tag until fixed.

Net posture: **structural phase complete; one resource-level namespace fix outstanding.**

---

*Re-verified 2026-04-15T19:35:00Z by gsd-verifier after the Wave 9 self-written VERIFICATION.md. Supersedes the draft authored inline during 21-09.*
