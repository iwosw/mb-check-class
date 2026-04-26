# Contributing

BannerMod is a brownfield merge workspace. Contributions should be small, verifiable, and aligned with the live root runtime.

## Source Of Truth

- Use root `src/**` and `build.gradle` as runtime truth.
- Use `docs/BANNERMOD_BACKLOG.md` as the unfinished-work queue.
- Use `.planning/` for execution history and planning context.
- Treat `recruits/` and `workers/` as archive/reference only.

## Flow

1. Pick one backlog item or one clearly bounded bug.
2. Inspect the current worktree with `tools/ai-context-proxy/bin/ctx status`.
3. Reproduce the bug or define acceptance checks before editing.
4. Make the smallest code change that satisfies the check.
5. Add or update focused tests when behavior changes.
6. Run the cheapest relevant verification first, then wider gates only when the touched area needs them.
7. Update `docs/BANNERMOD_BACKLOG.md`, `docs/STATUS.md`, and `.planning/STATE.md` when the shipped behavior or project status changes.
8. Commit atomically by area: code, tools, and docs should usually be separate commits.

## Verification Defaults

- Code compile: `./gradlew compileJava`
- Unit tests: `./gradlew test`
- Focused tests: `./gradlew test --tests <fully.qualified.TestName>`
- Gameplay/multiplayer wiring: `./gradlew verifyGameTestStage`
- Noisy commands: run through `tools/ai-context-proxy/bin/ctx log -- <command...>`

## Documentation Rules

- Keep player guides at root as `MULTIPLAYER_GUIDE_RU.md` and `MULTIPLAYER_GUIDE_EN.md`.
- Keep module documentation under `docs/`.
- Keep planning records under `.planning/`.
- Do not move or edit sibling agent rule files unless you are updating that specific agent's rules.

## Commit Rules

- Commit Java/GameTest changes separately from documentation changes.
- Commit `tools/` changes separately unless they are inseparable from a code change.
- Do not stage generated caches such as `__pycache__/`, Gradle output, logs, or runtime world output.
- If the worktree contains unrelated edits, leave them unstaged.
