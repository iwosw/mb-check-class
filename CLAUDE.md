<!-- GSD:project-start source:.planning/PROJECT.md -->
## Project

**BannerMod Merge Workspace**

This workspace is the realized merged runtime of the Forge mods historically living in `recruits/` and `workers/`. The active root build, runtime, and planning context are unified under `bannermod`; ongoing work is stabilization, architecture cleanup, and gameplay repair without losing historical context.

**Current merge stance:** active code lives under root `src/**`. `recruits/` and `workers/` remain on disk as archive/reference trees only unless a root doc explicitly points to them.
<!-- GSD:project-end -->

<!-- GSD:workflow-start -->
## Workflow

- Use `.planning/` as the active planning context.
- Prefer the real code over legacy plans when they disagree, and record material conflicts in `.planning/STATE.md` or `docs/STATUS.md`.
<!-- GSD:workflow-end -->

## Context Proxy

- Prefer `tools/ai-context-proxy/bin/ctx` for repository context.
- Use `ctx search <pattern> [path]` instead of broad `rg`/`grep`.
- Use `ctx file <path>` before opening large files.
- Use `ctx symbol <path> <symbol>` or `ctx exact <path> <start> <end>` before editing.
- Use `ctx log -- <command...>` for Gradle, Minecraft, and test output.
- Avoid raw `cat`, broad `sed`, broad `find`, and broad `rg`/`grep` context dumps unless `ctx` is insufficient and you state why.
- Optional shell setup: `export PATH="$PWD/tools/ai-context-proxy/wrappers:$PWD/tools/ai-context-proxy/bin:$PATH"`.

## Agent rule files

This repo carries multiple agent rule files: `CLAUDE.md` (Claude Code), `AGENTS.md` (opencode), `GEMINI.md` (Gemini), `.cursorrules` + `.cursor/` (Cursor), `.windsurf/` (Windsurf), `.github/copilot-instructions.md` (Copilot). Each file applies only to its own agent. When adding or changing a rule, edit only the file matching the agent currently running — do not push Claude-specific rules into a sibling file.

## Worktree freshness

Before treating a task as starting from a clean baseline, run `ctx status`. Prior agent sessions routinely leave uncommitted modifications and untracked new packages that intersect the files about to be touched. "Clean checkout" is not the default state.

## Commit hygiene

- **Always work on a feature branch**, never directly on `master`. Create the branch (e.g. `feature/<area>-<short-slug>`) before the first commit of a new task. Push only when the user asks for it.
- Default to atomic commits grouped by area / package. When the worktree is tangled with prior-session work, path-based grouping (one commit per directory or theme) is acceptable instead of fine-grained per-feature splits.
- Do not stage Markdown (`*.md`, `*.mdc`) unless explicitly asked. If Markdown is requested in a commit, only `AGENTS.md` and `CLAUDE.md` are allowed by default; do not commit planning docs or other guide files unless the user explicitly names them.
- Multi-line commit messages: write the message to `/tmp/commit_msg.txt` via the file-writing tool, then `git commit -F /tmp/commit_msg.txt`. The `ctx` pre-bash hook blocks heredoc patterns (`<<'EOF'`).

## Backlog hygiene

- `docs/BANNERMOD_BACKLOG.md` is the single canonical backlog. When you add a new task, write the section in full: `## <ID> — <Title>`, **Зачем**, **Scope** (concrete deliverables), **Acceptance** (verifiable success criteria). Skipping any of these makes the task invisible to future sessions.
- **DONE = every Acceptance bullet is observably satisfied right now.** Closing a task is a binary check against the existing Acceptance list, not a judgement call. Read each Acceptance bullet line by line; if any one of them describes gameplay-observable behaviour you cannot demonstrate from the current code (e.g. "units rout when outnumbered"), the task stays OPEN even when supporting infrastructure landed. A pure-logic / policy slice that the AI hookup will later consume is **never** enough to close a task whose Acceptance speaks about in-game behaviour — record it as `**Progress <YYYY-MM-DD>.**` and explicitly call out which Acceptance bullets are not yet met.
- When a task is finished, mark it closed by inserting a `**Status: DONE <YYYY-MM-DD>.**` line at the top of its section (right under the heading) and keeping the existing scope/acceptance/progress paragraphs in place as the historical record. **The Status: DONE line is mandatory** — a closed task without that exact-format line is invisible to the `awk '/^## /{...}'` audit the next session will run, and to the human reading the file. Never silently delete a closed task.
- Open tasks have no `Status:` line; in-progress slices use a `**Progress <YYYY-MM-DD>.**` paragraph at the bottom of the section. Multiple Progress paragraphs from different sessions stack chronologically — never edit or remove a prior session's Progress paragraph; append a new one instead.
- Before claiming a closure in chat to the user, run `awk '/^## /{section=$0; getline blank; getline line; if (line ~ /Status: DONE/) print section " -> DONE"; else print section " -> OPEN"}' docs/BANNERMOD_BACKLOG.md` against the file and confirm the section actually shows DONE. The summary you give the user must match what the file contains, not what you intended to write.

## Contribution flow

- Read `docs/STATUS.md` before picking up brownfield work.
- Use `docs/CONTRIBUTING.md` as the contribution flow for code, tests, docs, and commits.
- Use `docs/BANNERMOD_BACKLOG.md` as the canonical active backlog.
- Put module documentation under `docs/`; keep root player guides split as `MULTIPLAYER_GUIDE_RU.md` and `MULTIPLAYER_GUIDE_EN.md`.
- The local context multitool is documented in `tools/ai-context-proxy/README.md` and summarized in `docs/TOOLS.md`.

## Army command pipeline

All server-side movement / face / attack / aggro / stance / strategic-fire commands for recruits MUST flow through the unified pipeline. Bypassing it (e.g. calling `recruit.setMovePos(...)` directly from a packet handler) breaks queueing, priority, and the `CommandIntentLog` audit trail.

- **Entry point:** build a `CommandIntent` (record types in `army/command/CommandIntent.java`) and call `CommandIntentDispatcher.dispatch(player, intent, actors)`. The dispatcher handles selection narrowing, queue vs immediate, and routing to `CommandEvents`.
- **`CommandIntent.Movement` signature:** `(long issuedAtGameTime, int priority, boolean queueMode, int movementState, int formation, boolean tight, @Nullable Vec3 targetPos)`. `priority` is an `int` from `CommandIntentPriority` constants (`LOW=1`, `NORMAL=3`, `HIGH=5`, `IMMEDIATE=10`) — not an enum.
- **`movementState` semantics** (see `MovementFormationCommandService.onMovementCommand`): `0` hold, `1` follow, `2` regroup, `3` wander, `4` come-to-me, `5` patrol, `6` move-to-pos, `7`/`8` formation forward/back. Formation pipeline only triggers when `formation != 0 && movementState ∈ {2, 4, 6, 7, 8}`; otherwise falls through to per-recruit move.
- **Formation is server-authoritative.** Player's saved formation lives in `Player.PERSISTED_NBT_TAG → "Formation"`. Read it on the server with `CommandEvents.getSavedFormation(player)` — do not pass formation indices from the client unless the UI is explicitly picking one. Hardcoding a non-zero formation in a packet silently rebinds the group.
- **Explicit target positions:** when the move target arrives via network (world-map click, etc.) instead of `player.pick(...)`, use the 6-arg overload `CommandEvents.onMovementCommand(player, recruits, state, formation, tight, Vec3)` — the underlying `MovementFormationCommandService` short-circuits the hit-result lookup when `explicitTargetPos != null`.
- **Verifying a wiring change:** `./gradlew compileJava` via `ctx log` is the cheap gate. For runtime verification of formation behavior, save a formation in the command screen, then exercise the command path; `formation == 0` means the player never opened the formation UI and the per-recruit fallback is the correct path.
