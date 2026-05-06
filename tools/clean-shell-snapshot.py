#!/usr/bin/env python3
"""Strip malformed ``set -o`` lines from Claude Code's bash snapshot file.

Claude Code's harness sources ``~/.claude/shell-snapshots/snapshot-bash-*.sh``
before every Bash tool invocation. Some snapshot generators emit corrupted
``set -o`` lines (with ``grep -n``-style ``N:NAME`` prefixes, ripgrep
``Matches:`` summaries, and one bare ``set -o`` with no argument). The bare
``set -o`` dumps the full options table on stdout, which the model sees as a
26-line preamble before every Bash tool result — pure token waste.

This script:

* finds all snapshot files in ``~/.claude/shell-snapshots/``,
* removes lines matching the malformed pattern,
* optionally replaces them with semantically equivalent ``set -o NAME`` lines
  (default: just drop them — bash defaults already cover braceexpand, hashall,
  interactive-comments, monitor in non-interactive shells where they apply).

Idempotent: running it on a clean file is a no-op.

Usage::

    python3 tools/clean-shell-snapshot.py            # patch all snapshots
    python3 tools/clean-shell-snapshot.py --dry-run  # report only
    python3 tools/clean-shell-snapshot.py --restore  # keep set -o NAME equivalents
"""

from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path


SNAPSHOT_DIR = Path.home() / ".claude" / "shell-snapshots"

# Lines we treat as malformed:
#   set -o N:NAME       (grep -n line-number prefix bled into the snapshot)
#   set -o              (bare, no argument — dumps the full options table)
#   set -o Matches:     (ripgrep summary suffix bled in)
NUMERIC_PREFIX_RE = re.compile(r"^set -o (\d+):([A-Za-z_-]+)\s*$")
BARE_RE = re.compile(r"^set -o\s*$")
MATCHES_RE = re.compile(r"^set -o\s+Matches:\s*$")

# Names recovered from ``N:NAME`` lines; we restore these in --restore mode.
DEFAULT_RESTORED = ("braceexpand", "hashall", "interactive-comments", "monitor")


def is_malformed(line: str) -> bool:
    return bool(
        NUMERIC_PREFIX_RE.match(line)
        or BARE_RE.match(line)
        or MATCHES_RE.match(line)
    )


def patch(content: str, restore: bool) -> tuple[str, int]:
    out: list[str] = []
    dropped = 0
    restored_block_inserted = False
    names_seen: list[str] = []

    for line in content.splitlines(keepends=True):
        if is_malformed(line.rstrip("\n")):
            dropped += 1
            m = NUMERIC_PREFIX_RE.match(line.rstrip("\n"))
            if m:
                name = m.group(2)
                if name not in names_seen:
                    names_seen.append(name)
            if restore and not restored_block_inserted:
                names = names_seen if names_seen else list(DEFAULT_RESTORED)
                for n in names:
                    out.append(f"set -o {n}\n")
                restored_block_inserted = True
            continue
        out.append(line)
    return "".join(out), dropped


def find_snapshots(snapshot_dir: Path) -> list[Path]:
    if not snapshot_dir.is_dir():
        return []
    return sorted(snapshot_dir.glob("snapshot-bash-*.sh"))


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--dir", default=str(SNAPSHOT_DIR), help="Snapshot directory (default: ~/.claude/shell-snapshots)")
    parser.add_argument("--dry-run", action="store_true", help="Report intended changes without writing")
    parser.add_argument(
        "--restore",
        action="store_true",
        help="Replace dropped lines with valid set -o NAME equivalents (default: just drop)",
    )
    args = parser.parse_args()

    snapshot_dir = Path(args.dir).expanduser()
    snapshots = find_snapshots(snapshot_dir)
    if not snapshots:
        print(f"No snapshots found in {snapshot_dir}", file=sys.stderr)
        return 0

    total_dropped = 0
    patched_files = 0
    for path in snapshots:
        try:
            original = path.read_text(encoding="utf-8")
        except OSError as exc:
            print(f"skip {path}: {exc}", file=sys.stderr)
            continue
        cleaned, dropped = patch(original, restore=args.restore)
        if dropped == 0:
            print(f"clean: {path}")
            continue
        total_dropped += dropped
        patched_files += 1
        if args.dry_run:
            print(f"would patch {path}: drop {dropped} malformed line(s)")
            continue
        path.write_text(cleaned, encoding="utf-8")
        print(f"patched {path}: dropped {dropped} malformed line(s)")

    if patched_files == 0:
        print(f"No snapshots needed patching ({len(snapshots)} scanned).")
    elif args.dry_run:
        print(f"\n{patched_files} file(s) would be patched, {total_dropped} line(s) total.")
    else:
        print(f"\nPatched {patched_files} file(s), dropped {total_dropped} line(s).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
