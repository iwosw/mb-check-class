# Phase 21: Source Tree Consolidation — Context

**Gathered:** 2026-04-15
**Status:** Ready for replanning (pivot from `bannerlord` → `bannermod` namespace)

<domain>
## Phase Boundary

Consolidate active Java ownership out of the legacy `recruits/` and `workers/` source roots into a single canonical package tree under `src/main/java/com/talhanation/bannermod/**`. Phase 21 closes when: (a) legacy source roots are retired from the build, (b) all moved code lives under `com.talhanation.bannermod.*`, (c) the `com.talhanation.bannerlord.*` namespace is fully retired, and (d) `./gradlew compileJava` plus full validation gate pass from the root-only layout.

**Pivot note (this amendment):** The phase originally targeted `com.talhanation.bannerlord.*` as the convergence namespace. That target is wrong — the convergence namespace is `com.talhanation.bannermod.*`, matching the existing domain-service subtrees (`bannermod.citizen`, `.governance`, `.settlement`, `.logistics`, `.authority`, `.config`) and the overall mod identity. This CONTEXT.md captures the corrective decisions.

</domain>

<decisions>
## Implementation Decisions

### Target namespace
- **D-01:** Convergence namespace is `com.talhanation.bannermod.*`. All phase 21 moves target this root.
- **D-02:** `com.talhanation.bannerlord.*` is fully retired by end of phase 21. No code, not even legacy-compat shims, remains under `bannerlord`.

### Existing `bannermod.*` subtrees (scope of preservation)
- **D-03:** Preserve existing `com.talhanation.bannermod.{citizen,governance,settlement,logistics,authority,config}` subtrees as-is. They were authored outside phase 21 and are load-bearing for other phases.
- **D-04:** New moves from phase 21 land under sibling packages alongside the preserved subtrees. Expected new siblings: `bannermod.{entity,ai,network,persistence,client,compat,shared,registry,bootstrap}` (mirroring what currently exists under `bannerlord/*`).
- **D-05:** If a naming collision or semantic overlap emerges between an existing `bannermod.*` package and a moved subtree (e.g., two `config` packages), do NOT dedup in phase 21 — record the overlap in `MERGE_NOTES.md` and defer reconciliation to a follow-up phase. Phase 21 is a structural move, not a semantic merge.

### Revert strategy
- **D-06:** Hard `git revert` the phase 21 commits that moved code into `bannerlord/*`, then re-execute the amended plans. Rationale: preserves a clean, auditable history of the pivot; commits remain in the log but are neutralized.
- **D-07:** Revert scope (to be validated by planner): all commits from `f1832af feat(21-02): move bootstrap ownership into bannerlord packages` through `a792dc3 docs(21-06): complete shared seam ownership plan`, inclusive. This spans 21-02, 21-03, 21-04, 21-05, 21-06 (`feat`/`fix`) plus their `docs(21-XX)` companions. 21-01 has no `feat` commit on record (doc-only) and needs no revert beyond amending the plan file.
- **D-08:** Revert commits must be contiguous and bundled per wave — one revert commit per original wave commit, with a shared preamble message `revert(21): pivot bannerlord → bannermod`. Do NOT squash; keep per-wave reverts for bisectability.
- **D-09:** The in-flight uncommitted working-tree changes (see `git status` at phase start) must be triaged BEFORE the revert runs. They cannot be carried across the revert. Options the planner must address: (a) commit them as `wip(21): capture in-flight bannerlord edits pre-pivot` before revert so they're in history, (b) stash and discard if obsolete, or (c) re-apply selectively post-amendment. Decision to be made at the top of the rewrite plan.

### Plan amendments
- **D-10:** Edit `21-02-PLAN.md` through `21-09-PLAN.md` in place — frontmatter (`files_modified`, `key_links`, `requirements` refs), body copy, must-haves, and verification steps. Keep plan numbering intact.
- **D-11:** Mechanical find/replace `com.talhanation.bannerlord` → `com.talhanation.bannermod` across plan files is the FIRST pass, but every plan must then be human-reviewed for: (a) references to preserved `bannermod.*` subtrees that must NOT be clobbered, (b) path patterns like `bannerlord/compat/**` that need bannermod-equivalent names, (c) test/gametest paths that may already be correct.
- **D-12:** `21-06-PLAN.md` (shared seam ownership) needs careful re-amendment because its `SUMMARY.md` currently documents the `bannerlord.shared` outcome. After the revert + amend + re-execute, the existing `SUMMARY.md` files for 21-02..21-06 must be deleted so they regenerate fresh against the bannermod outcome.
- **D-13:** The roadmap entry for phase 21 (`ROADMAP.md`) must be updated: title, description bullets, and plan one-liners all rewritten to reference `bannermod`. Phase title stays `Source Tree Consolidation` but drops "Into Bannerlord" in favor of "Into BannerMod" (or equivalent).

### Verification and docs impact
- **D-14:** `21-VERIFICATION.md` must be deleted or fully rewritten post-re-execute. Its current contents reflect the bannerlord-era truth and will mislead any verifier agent.
- **D-15:** All `21-source-tree-consolidation-into-bannerlord-0N-SUMMARY.md` files (02, 03, 04, 05, 06) are deleted as part of the revert sequence. Re-execute regenerates them against the amended plans.
- **D-16:** The phase directory name `21-source-tree-consolidation-into-bannerlord/` is intentionally NOT renamed. Directory renames cause git rename-detection churn and break STATE.md references. The directory keeps its historical slug; only the phase title and contents change.
- **D-17:** `MERGE_NOTES.md` gains an entry documenting the pivot: why `bannerlord` was wrong, when the pivot landed, which commits reverted. Serves as the discoverable record for future maintainers.

### Claude's Discretion
- Exact commit ordering within each revert wave (planner decides).
- Whether to revert `docs(21-XX)` commits as separate reverts or fold them into the corresponding feat-revert commit (planner decides based on what gives cleanest `git log`).
- Mechanical rename tooling choice (sed/IDE refactor/jscodeshift-equivalent) for plan body amendments.
- Handling of the existing stale worktree `/tmp/bannermod-31-03-red` flagged by `/gsd-health` — unrelated, planner may ignore.

</decisions>

<specifics>
## Specific Ideas

- The existing `com.talhanation.bannermod.governance.BannerModGovernorService` and sibling services prove `bannermod` is already the mod's canonical namespace. The pivot codifies that convention across the runtime scaffolding too.
- Commit message convention during rewrite: `revert(21-XX): …` for reverts, `feat(21-XX): re-home … into bannermod` for re-execute. Preserves the existing `(21-XX)` scope tag style.
- Phase 21's original framing treated `bannerlord` as a neutral "runtime core" label. That was a misread — `bannerlord` is the name of a Mount & Blade game that is thematically referenced but not the mod's own namespace. `bannermod` is the project's identity.

</specifics>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before replanning.**

### Phase scope and requirements
- `.planning/ROADMAP.md` §Phase 21 — Current phase goal, plan list, status note (will be amended post-pivot).
- `.planning/REQUIREMENTS.md` — SRCMOVE-01 through SRCMOVE-04 requirement text.
- `.planning/phases/21-source-tree-consolidation-into-bannerlord/21-01-PLAN.md` through `21-09-PLAN.md` — existing plans to be amended in place.

### Project-level constraints
- `CLAUDE.md` — Merge stance: "`recruits` is the runtime base. `workers` is preserved as a legacy subsystem to be absorbed incrementally." Bounds what phase 21 is allowed to restructure.
- `MERGE_NOTES.md` — Running log of merge-workspace conflicts and decisions; gains the pivot entry per D-17.

### Existing `bannermod.*` subtrees (do not clobber)
- `src/main/java/com/talhanation/bannermod/citizen/` — Phase 22 territory; preserved.
- `src/main/java/com/talhanation/bannermod/governance/` — Existing domain service; preserved.
- `src/main/java/com/talhanation/bannermod/settlement/` — Existing domain service; preserved.
- `src/main/java/com/talhanation/bannermod/logistics/` — Existing domain service; preserved.
- `src/main/java/com/talhanation/bannermod/authority/` — Existing domain service; preserved.
- `src/main/java/com/talhanation/bannermod/config/` — Existing config helpers; preserved (watch for potential overlap with moved `bannerlord/config` on re-execute).

### Source to be moved (currently under bannerlord/*, will move to bannermod/*)
- `src/main/java/com/talhanation/bannerlord/shared/`
- `src/main/java/com/talhanation/bannerlord/entity/{shared,civilian,military}/`
- `src/main/java/com/talhanation/bannerlord/ai/{civilian,military,pathfinding}/`
- `src/main/java/com/talhanation/bannerlord/network/{civilian,military}/`
- `src/main/java/com/talhanation/bannerlord/persistence/{civilian,military}/`
- `src/main/java/com/talhanation/bannerlord/client/{shared,civilian}/`
- `src/main/java/com/talhanation/bannerlord/compat/{recruits,workers}/`
- `src/main/java/com/talhanation/bannerlord/{config,registry,bootstrap}/`

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `com.talhanation.bannermod.governance.BannerModGovernorService` — already the target of `assignGovernor` calls from `RecruitEvents`. After pivot, the bridge plan (21-07) becomes a straight same-namespace call rather than a cross-namespace bridge. Simplifies 21-07's bridge design.
- Existing `bannermod.*` package structure proves Gradle/module/resource bundling already handles that namespace — no build-config surprises.

### Established Patterns
- Class prefix convention under `bannermod.*` uses `BannerMod` as the type prefix (e.g., `BannerModGovernorService`, `BannerModSettlementBinding`). Moved classes from `bannerlord/*` generally use no such prefix (e.g., `AbstractRecruitEntity`). Decision needed during re-execute: adopt the prefix, drop it, or leave as-is. **Default: leave as-is** — phase 21 is a move, not a rename refactor. Capture any prefix decision as a follow-up.
- Compat namespace `bannerlord.compat.{recruits,workers}` becomes `bannermod.compat.{recruits,workers}` — keeps the `compat` convention as-is.

### Integration Points
- `RecruitEvents`, `FactionEvents`, `ClaimEvents`, `CommandEvents`, inventory menus — all currently call into `com.talhanation.bannerlord.*` per the uncommitted working tree. Post-pivot, these callsites flip to `com.talhanation.bannermod.*`. The uncommitted edits may be partially reusable after a simple rename.

</code_context>

<deferred>
## Deferred Ideas

- **Class prefix normalization** (`BannerMod` prefix on moved types) — out of phase 21 scope; file as a follow-up refactor phase once the move is green.
- **Semantic merge of overlapping packages** (e.g., if moved `bannermod.config` overlaps with existing `bannermod.config`) — phase 21 documents the overlap in `MERGE_NOTES.md`; actual merge is a separate phase.
- **Phase directory slug rename** (`21-source-tree-consolidation-into-bannerlord` → `-into-bannermod`) — deliberately deferred; would cause STATE.md/Git churn for zero runtime benefit.
- **Stale worktree cleanup** (`/tmp/bannermod-31-03-red`) — surfaced by `/gsd-health`; unrelated to phase 21; handle separately.

</deferred>

---

## Post-Restore Amendment (2026-04-15, afternoon)

The pre-revert "pivot from bannerlord → bannermod" framing above assumed the moved code still lived under `src/main/java/com/talhanation/bannerlord/*` and needed repackaging. Post-21-01 recovery exposed a different reality:

- `src/main/java/com/talhanation/bannerlord/*` — **empty**. 21-01's revert eliminated the namespace; nothing to rename.
- `src/main/java/com/talhanation/bannermod/*` — **25 files** (authority, citizen, config, governance, logistics, settlement, shared/{authority,settlement,logistics}). Preserved.
- `recruits/` — **untracked embedded git clone**, 404 Java files under `com.talhanation.recruits.*`. Full forward-progress tree (Phase 20 stabilization, migration, compat/workers, pathfinding, formations, factions, claims, diplomacy). 21-01's delete reached in here and wiped 714/798 tracked files; restored post-hoc via `git restore .` inside the clone. Build tree is `recruits/src/main/java`.
- `workers/` — **untracked embedded git clone**, 128 Java files under `com.talhanation.workers.*`. Includes Phase-20 types (`WorkersMain`, `WorkersRuntime`, `WorkersSubsystem`, `WorkersClientManager`, `MergedRuntimeCleanupPolicy`). 21-01 wiped 141/287 files; restored.
- `build.gradle` outer sourceSets include `recruits/src/main/java` and `workers/src/main/java` — the embedded clones compile as part of the outer mod build.

### New Decisions (supersede bannerlord-era framing where they conflict)

- **D-18:** Migration source is the embedded clones `recruits/` and `workers/` — **not** an outer `bannerlord/*` tree. Every 21-03..21-09 move is an inter-repo transfer: copy from clone working tree to outer repo's `src/main/java/com/talhanation/bannermod/*`, `git add` in outer repo, `rm` + commit in clone.
- **D-19:** Migration is staged by subsystem, 532 total Java files plus resources. Wave mapping:
  - Wave 3 (21-03): composition root + init/registry (~40 files) — `Main.java`/`WorkersMain.java` + top-level lifecycle + `init/Mod*`.
  - Wave 4 (21-04): events + commands + config + top-level utilities (~30 files).
  - Wave 5 (21-05): military gameplay — `recruits/{entities,pathfinding,mixin,compat,migration,events,util}` (~128 files).
  - Wave 6 (21-06): military UI + persistence — `recruits/{client,inventory,world,items}` (~142 files).
  - Wave 7 (21-07): civilian subsystem — `workers/{entities,client,world,inventory,items,settlement}` (~84 files).
  - Wave 8 (21-08): network consolidation — `recruits/network` + `workers/network` (~133 messages).
  - Wave 9 (21-09): phase closure — residue, resources, `mods.toml`, mixin configs, `build.gradle` source-set cleanup, deprecated forwarders, docs.
- **D-20:** Files leave the clones as they're moved. At the end of each wave, the clone's working tree is shrunk by the moved subsystem, and outer repo's `bannermod.*` subtree grows. Deletions in the clone are committed inside the clone's own git; additions in outer are committed in outer repo's git. No cross-repo atomic commits — each side commits independently per wave.
- **D-21:** Package renaming during copy:
  - `com.talhanation.recruits.{entities,ai,...}` → `com.talhanation.bannermod.{entity,ai,...}.military`
  - `com.talhanation.workers.{entities,ai,...}` → `com.talhanation.bannermod.{entity,ai,...}.civilian`
  - `com.talhanation.{recruits,workers}.network.**` → `com.talhanation.bannermod.network.messages.{military,civilian}.**`
  - Top-level `Main.java`/`WorkersMain.java` → `com.talhanation.bannermod.bootstrap.BannerModMain` (unified).
  - Event classes (`RecruitEvents`, `FactionEvents`, `ClaimEvents`, etc.) → `com.talhanation.bannermod.events.*`.
  - Commands/config merge into existing `bannermod.{commands,config}` subtrees.
- **D-22:** Compile state across waves: the outer repo is **not required to compile cleanly** between waves 3 and 8. Intermediate waves leave dangling imports. Wave 9 is the compile-green gate.
- **D-23:** In each wave, after files land in outer `bannermod.*`, rewrite imports across already-migrated code (and remaining clone callers if they must compile for parallel dev) using mechanical `sed` find/replace on FQNs. Record every `sed` invocation in the wave's SUMMARY.
- **D-24:** Legacy clone entrypoints (`recruits/.../Main.java`, `workers/.../WorkersMain.java`) are demoted to `@Deprecated` stubs at the end of Wave 3 (no `@Mod`, no runtime wiring). They remain in clone's working tree so the clone's own dev tests can still reference them transiently; Wave 9 decides final deletion of clone trees from outer build.
- **D-25:** Original Phase 21 decisions D-06..D-08 (revert strategy) are **resolved** — the revert happened in 21-01. D-09 (in-flight edits) is also moot; no in-flight edits remain.
- **D-26:** Original D-12 (21-06 SUMMARY deletion) is partially done by 21-01 and partially obsolete — the new 21-06 scope is different (military UI, not shared seam).
- **D-27:** The `workers-upstream/` directory briefly appeared at repo root during recovery and was deleted. Not part of the plan. The embedded `workers/` clone is the source of truth.
- **D-28:** `recruits/` clone's `git restore .` also reset 10 gametest files in `src/gametest/java/com/talhanation/recruits/gametest/battle/**` to HEAD, discarding uncommitted modifications. Flag for review post-replan if those edits were load-bearing.

### Canonical references (updated)

- **Migration source:**
  - `recruits/src/main/java/com/talhanation/recruits/**` (404 files)
  - `workers/src/main/java/com/talhanation/workers/**` (128 files)
  - Plus each clone's `src/main/resources/**` and `src/{test,gametest}/java/**` (subset)
- **Migration target:** `src/main/java/com/talhanation/bannermod/**` in outer repo.
- **Preserved (do not clobber):** Existing `bannermod.{authority,citizen,config,governance,logistics,settlement,shared}` subtrees per D-03.

*Post-restore amendment recorded 2026-04-15 after 21-01 wipe recovery.*

---

*Phase: 21-source-tree-consolidation-into-bannerlord*
*Context gathered: 2026-04-15*
*Supersedes prior implicit bannerlord-target framing.*
