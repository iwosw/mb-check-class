# Contributing

BannerMod is a brownfield merge workspace. Contributions should be small, verifiable, and aligned with the live root runtime.

## Source Of Truth

- Use root `src/**` and `build.gradle` as runtime truth.
- Use `tools/backlog` as the interface to the unfinished-work queue in `docs/BANNERMOD_BACKLOG.json`.
- Use `.planning/` for execution history and planning context.
- Treat `recruits/` and `workers/` as archive/reference only.

## Flow

1. Pick one backlog item with `tools/backlog batch --limit 5` / `tools/backlog show <ID>`, or pick one clearly bounded bug.
2. Inspect the current worktree with `tools/ai-context-proxy/bin/ctx status`.
3. Reproduce the bug or define acceptance checks before editing.
4. Make the smallest code change that satisfies the check.
5. Add or update focused tests when behavior changes.
6. Self-verify the result against every acceptance item of the backlog task before marking it done.
7. Update `MULTIPLAYER_GUIDE_RU.md`, `MULTIPLAYER_GUIDE_EN.md`, and `docs/BANNERMOD_ALMANAC.html` when the task changes UI, changes mechanics, or adds player-facing mechanics that non-technical players must know.
8. Run the cheapest relevant verification first, then wider gates only when the touched area needs them.
9. Update the backlog through `tools/backlog progress` or `tools/backlog done --verification`, and update `docs/STATUS.md` / `.planning/STATE.md` when shipped behavior or project status changes.
10. Commit atomically by area: code, tools, and docs should usually be separate commits.

## Backlog Intake

- Add backlog work with `tools/backlog add <ID> <title> --why ... --scope ... --acceptance ...`.
- Use `--dry-run` before writing when drafting or reviewing task shape.
- The tool rejects duplicate IDs, invalid ID format, empty fields, and tasks without concrete scope or acceptance checks.
- Include guide-update scope/acceptance when the proposed task changes UI, changes mechanics, or adds player-facing mechanics.

## Verification Defaults

- Code compile: `./gradlew compileJava`
- Unit tests: `./gradlew test`
- Focused tests: `./gradlew test --tests <fully.qualified.TestName>`
- Gameplay/multiplayer wiring: `./gradlew verifyGameTestStage`
- Noisy commands: run through `tools/ai-context-proxy/bin/ctx log -- <command...>`

## Documentation Rules

- Keep player guides at root as `MULTIPLAYER_GUIDE_RU.md` and `MULTIPLAYER_GUIDE_EN.md`.
- Keep `docs/BANNERMOD_ALMANAC.html` as the compact player-facing book. Treat the root guides as the detailed source and refresh the almanac from them plus current code/status whenever player-visible mechanics change. If this becomes frequent, add a generator under `tools/` instead of hand-editing the HTML.
- Keep module documentation under `docs/`.
- Keep planning records under `.planning/`.
- Do not move or edit sibling agent rule files unless you are updating that specific agent's rules.

## Release Command

After CI is green on the release commit, create and push an annotated tag without requiring GitHub CLI:

`git tag -a <version> <commit-sha> -m "BannerMod <version>"`

`git push origin <version>`

Use the literal project version string, for example `v1`. The canonical release path is tag-driven: push a `v*` tag that points at the reviewed `master` commit, let the tag CI finish green, and the workflow overrides the Gradle/mod version with that exact tag name so the uploaded artifact becomes `bannermod-<tag>.jar`. Do not rely on `release.created` to build artifacts, and do not create release tags from dirty worktrees or before the unit coverage, GameTest scenario coverage, and build stages pass.

## Commit Rules

- Commit Java/GameTest changes separately from documentation changes.
- Commit `tools/` changes separately unless they are inseparable from a code change.
- Do not stage generated caches such as `__pycache__/`, Gradle output, logs, or runtime world output.
- If the worktree contains unrelated edits, leave them unstaged.
