#!/usr/bin/env python3
"""Claude Code PreToolUse hook that nudges raw context reads through ctx."""

from __future__ import annotations

import json
import re
import shlex
import sys


RAW_COMMANDS = {"cat", "grep", "rg", "find", "sed"}
SAFE_PREFIXES = (
    "ctx ",
    "tools/ai-context-proxy/bin/ctx ",
    "./tools/ai-context-proxy/bin/ctx ",
    "python3 tools/ai-context-proxy/",
    "python3 ./tools/ai-context-proxy/",
)


def main() -> int:
    try:
        payload = json.load(sys.stdin)
    except json.JSONDecodeError:
        return 0

    tool_input = payload.get("tool_input") or {}
    command = tool_input.get("command") or ""
    if not command.strip():
        return 0

    stripped = command.strip()
    if stripped.startswith(SAFE_PREFIXES):
        return 0

    if is_raw_context_command(stripped):
        print(
            "Blocked raw context dump. Use ai-context-proxy instead:\n"
            "- ctx search <pattern> [path] instead of broad rg/grep\n"
            "- ctx file <path> instead of cat for large files\n"
            "- ctx exact <path> <start> <end> instead of sed ranges\n"
            "- ctx log -- <command...> for noisy Gradle/Minecraft/test output\n"
            "If raw output is truly required, state why and use a non-Bash file tool or adjust the hook.",
            file=sys.stderr,
        )
        return 2

    return 0


def is_raw_context_command(command: str) -> bool:
    first = first_word(command)
    if first in RAW_COMMANDS:
        return True

    # Catch common shell wrappers and pipelines while avoiding complex parsing.
    return bool(re.search(r"(^|[;&|()]\s*)(cat|grep|rg|find|sed)\s+", command))


def first_word(command: str) -> str:
    try:
        parts = shlex.split(command, posix=True)
    except ValueError:
        return ""
    if not parts:
        return ""
    return parts[0].rsplit("/", 1)[-1]


if __name__ == "__main__":
    raise SystemExit(main())
