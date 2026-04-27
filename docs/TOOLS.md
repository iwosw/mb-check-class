# Tools

## ai-context-proxy Multitool

`tools/ai-context-proxy/` is a local context multitool for coding agents. It gives compact repository maps, file summaries, exact ranges, symbol reads, compressed searches, compact git status/diff output, and short Gradle/Minecraft/test log summaries.

Use it when a normal command would dump too much context:

```bash
tools/ai-context-proxy/bin/ctx repo-map
tools/ai-context-proxy/bin/ctx file src/main/java/com/talhanation/bannermod/bootstrap/BannerModMain.java
tools/ai-context-proxy/bin/ctx search "CombatStance" src/main/java
tools/ai-context-proxy/bin/ctx log -- ./gradlew compileJava
```

The tool is a guardrail, not a compiler or sandbox. Before editing code, read the exact symbol or exact line range you intend to change.

Full tool notes live in `../tools/ai-context-proxy/README.md`.

## Backlog Tool

`tools/backlog` is the bounded interface to the canonical backlog at `docs/BANNERMOD_BACKLOG.json`. Use it instead of reading the full JSON during normal agent work.

```bash
tools/backlog batch --limit 5
tools/backlog show WAR-007
tools/backlog list --status open
tools/backlog add UI-008 "Readable title" --why "Why this matters" --scope "Concrete deliverable" --acceptance "Observable success check" --dry-run
tools/backlog validate
```

`tools/backlog add` validates new tasks before writing: ID format, duplicate IDs, non-empty `why`, at least one `scope`, and at least one `acceptance` item. Use `--dry-run` to preview a task without mutating the backlog.

Safety limits are deliberate: the tool refuses backlog files over 5 MB, caps `batch --limit` at 50 tasks, and writes through an atomic temp-file replace. This keeps normal use from producing huge outputs or partial JSON writes.

Backlog tasks that change UI, change mechanics, or add player-facing mechanics should include guide-update work for both `MULTIPLAYER_GUIDE_RU.md` and `MULTIPLAYER_GUIDE_EN.md`.

## Agent Guardrails

Shared local guardrail scripts live in `tools/agent-hooks/`.

- Claude Code uses `.claude/settings.local.json`, which runs `tools/ai-context-proxy/hooks/claude-pre-bash.py`; that wrapper delegates to `tools/agent-hooks/pre-bash-guardrails.py`.
- Codex supports project hooks through `.codex/config.toml` with `[features].codex_hooks = true`; this repo wires `PreToolUse` for Bash to the shared guardrail script.
- OpenCode supports project plugins under `.opencode/plugins/`; this repo uses `.opencode/plugins/project-guardrails.js` to block direct backlog JSON access and raw context dumps before Bash execution.
- Cursor, Windsurf, Gemini, and Copilot rule files currently provide instruction-level guardrails only; no repo-local executable hook format is configured here for them.

## UI Design Skill

`minecraft-ui-design` is a repo-local open-agent skill for Minecraft GUI/HUD work:

- Canonical skill: `.agents/skills/minecraft-ui-design/SKILL.md` for Codex and OpenCode-compatible agents.
- Claude adapter: `.claude/commands/minecraft-ui-design.md`, which points Claude Code at the same design contract.

Use it for War Room, political screens, settlement screens, storage/build-area screens, command UI, world-map panels, and HUD overlays. The skill enforces Minecraft-native visual language, non-overlap constraints, server-authoritative UX, localization, and multiplayer-guide updates when player-facing mechanics change.

When finishing backlog work, self-verify every acceptance item and record the result:

```bash
tools/backlog done WAR-007 --verification "compileJava passed; UI declaration flow verified against all acceptance items"
```
