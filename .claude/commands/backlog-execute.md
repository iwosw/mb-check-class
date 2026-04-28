---
description: Execute a ready batch of backlog tasks under the repo's dependency/worktree rules.
---

Interpret `$ARGUMENTS` as an optional integer task count. If omitted, default to `5`.

Execution contract:

1. Run `tools/backlog ready <N>` to select the ready queue.
2. Treat this as an execution command, not a planning-only request.
3. For parallel task execution, create one dedicated git worktree and one dedicated feature branch per task before editing.
4. For dependency chains, finish the first task and branch the next task from the updated tip of the previous task branch.
5. Follow the repository backlog rules strictly:
   - finish the task to acceptance with verification, or
   - split the remaining scope into child tasks immediately and rewire dependencies.
6. Review each task diff before merge and merge only when the implementation is clearly correct and limited to that task.

Use `docs/CONTRIBUTING.md`, `docs/TOOLS.md`, and `AGENTS.md` as the execution process source of truth while carrying out the batch.
