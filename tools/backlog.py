#!/usr/bin/env python3
"""Small JSON-backed backlog tool for BannerMod agents.

The goal is intentionally modest: keep the backlog ordered, validate required
fields, and return bounded task batches without dumping the whole JSON file into
agent context.
"""

from __future__ import annotations

import argparse
import json
import os
import re
import sys
from datetime import date
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[1]
BACKLOG_PATH = ROOT / "docs" / "BANNERMOD_BACKLOG.json"
VALID_STATUSES = {"open", "in_progress", "done"}
REQUIRED_TASK_FIELDS = ("id", "title", "status", "why", "scope", "acceptance", "updated")
TASK_ID_RE = re.compile(r"^[A-Z][A-Z0-9]+-[0-9]+[A-Z0-9-]*$")
MAX_BACKLOG_BYTES = 5 * 1024 * 1024
MAX_BATCH_LIMIT = 50


def main() -> int:
    parser = argparse.ArgumentParser(description="BannerMod backlog mini-Jira")
    parser.add_argument("--file", default=str(BACKLOG_PATH), help="Backlog JSON path")
    sub = parser.add_subparsers(dest="command", required=True)

    p_list = sub.add_parser("list", help="List task summaries")
    add_common_filters(p_list)

    p_batch = sub.add_parser("batch", help="Print a bounded batch of tasks")
    add_common_filters(p_batch)
    p_batch.add_argument("--limit", type=int, default=5, help="Maximum tasks to print")
    p_batch.add_argument("--offset", type=int, default=0, help="Skip matching tasks")
    p_batch.add_argument("--json", action="store_true", help="Print machine-readable JSON")

    p_show = sub.add_parser("show", help="Show one full task")
    p_show.add_argument("id", help="Task id")
    p_show.add_argument("--json", action="store_true", help="Print machine-readable JSON")

    p_add = sub.add_parser("add", help="Append a new open task")
    p_add.add_argument("id", help="Task id, e.g. UI-008")
    p_add.add_argument("title", help="Task title")
    p_add.add_argument("--why", required=True, help="Why this task exists")
    p_add.add_argument("--scope", action="append", required=True, help="Concrete deliverable; repeatable")
    p_add.add_argument("--acceptance", action="append", required=True, help="Verifiable acceptance item; repeatable")
    p_add.add_argument("--evidence", action="append", default=[], help="Evidence reference; repeatable")
    p_add.add_argument("--dry-run", action="store_true", help="Validate and preview without writing")

    p_progress = sub.add_parser("progress", help="Append a progress note")
    p_progress.add_argument("id", help="Task id")
    p_progress.add_argument("text", help="Progress text")
    p_progress.add_argument("--date", default=today(), help="Progress date")

    p_done = sub.add_parser("done", help="Mark a task done after verification")
    p_done.add_argument("id", help="Task id")
    p_done.add_argument("--verification", required=True, help="Verification result proving acceptance is satisfied")
    p_done.add_argument("--date", default=today(), help="Done date")

    p_validate = sub.add_parser("validate", help="Validate backlog structure")
    p_validate.add_argument("--json", action="store_true", help="Print machine-readable JSON")

    args = parser.parse_args()
    path = Path(args.file)
    data = load(path)

    if args.command == "list":
        return cmd_list(data, args)
    if args.command == "batch":
        return cmd_batch(data, args)
    if args.command == "show":
        return cmd_show(data, args)
    if args.command == "add":
        return cmd_add(path, data, args)
    if args.command == "progress":
        return cmd_progress(path, data, args)
    if args.command == "done":
        return cmd_done(path, data, args)
    if args.command == "validate":
        return cmd_validate(data, args)

    parser.error(f"unknown command: {args.command}")
    return 2


def add_common_filters(parser: argparse.ArgumentParser) -> None:
    parser.add_argument("--status", choices=sorted(VALID_STATUSES), default="open", help="Task status filter")
    parser.add_argument("--prefix", help="ID prefix filter, e.g. UI or PERF")
    parser.add_argument("--query", help="Case-insensitive text filter over id/title/why")


def load(path: Path) -> dict[str, Any]:
    try:
        size = path.stat().st_size
        if size > MAX_BACKLOG_BYTES:
            raise SystemExit(
                f"backlog is {size} bytes, over the {MAX_BACKLOG_BYTES} byte safety limit; "
                "split/archive old entries before using tools/backlog"
            )
        with path.open("r", encoding="utf-8") as fh:
            data = json.load(fh)
    except FileNotFoundError:
        raise SystemExit(f"backlog not found: {path}")
    except json.JSONDecodeError as exc:
        raise SystemExit(f"invalid JSON in {path}: {exc}")
    if not isinstance(data, dict):
        raise SystemExit("backlog root must be an object")
    return data


def save(path: Path, data: dict[str, Any]) -> None:
    validate_or_exit(data)
    rendered = json.dumps(data, indent=2, ensure_ascii=False) + "\n"
    encoded = rendered.encode("utf-8")
    if len(encoded) > MAX_BACKLOG_BYTES:
        raise SystemExit(
            f"refusing to write {len(encoded)} bytes, over the {MAX_BACKLOG_BYTES} byte safety limit"
        )
    tmp_path = path.with_suffix(path.suffix + ".tmp")
    with tmp_path.open("w", encoding="utf-8") as fh:
        fh.write(rendered)
    os.replace(tmp_path, path)


def tasks(data: dict[str, Any]) -> list[dict[str, Any]]:
    value = data.get("tasks")
    if not isinstance(value, list):
        raise SystemExit("backlog.tasks must be a list")
    return value


def find_task(data: dict[str, Any], task_id: str) -> dict[str, Any]:
    wanted = task_id.upper()
    for task in tasks(data):
        if str(task.get("id", "")).upper() == wanted:
            return task
    raise SystemExit(f"task not found: {task_id}")


def matching(data: dict[str, Any], args: argparse.Namespace) -> list[dict[str, Any]]:
    result = []
    query = (args.query or "").lower()
    prefix = (args.prefix or "").upper()
    for task in tasks(data):
        if task.get("status") != args.status:
            continue
        task_id = str(task.get("id", ""))
        if prefix and not task_id.upper().startswith(prefix):
            continue
        haystack = " ".join(str(task.get(k, "")) for k in ("id", "title", "why")).lower()
        if query and query not in haystack:
            continue
        result.append(task)
    return result


def cmd_list(data: dict[str, Any], args: argparse.Namespace) -> int:
    for task in matching(data, args):
        print(f"{task['id']} [{task['status']}] {task['title']}")
    return 0


def cmd_batch(data: dict[str, Any], args: argparse.Namespace) -> int:
    if args.limit < 1:
        raise SystemExit("--limit must be at least 1")
    if args.limit > MAX_BATCH_LIMIT:
        raise SystemExit(f"--limit must be <= {MAX_BATCH_LIMIT}")
    if args.offset < 0:
        raise SystemExit("--offset must be >= 0")
    selected = matching(data, args)[args.offset : args.offset + args.limit]
    if args.json:
        print(json.dumps(selected, indent=2, ensure_ascii=False))
        return 0
    for task in selected:
        print_task(task, compact=False)
        print()
    if not selected:
        print("No matching tasks.")
    return 0


def cmd_show(data: dict[str, Any], args: argparse.Namespace) -> int:
    task = find_task(data, args.id)
    if args.json:
        print(json.dumps(task, indent=2, ensure_ascii=False))
    else:
        print_task(task, compact=False)
    return 0


def cmd_add(path: Path, data: dict[str, Any], args: argparse.Namespace) -> int:
    task_id = normalize_task_id(args.id)
    if any(str(task.get("id", "")).upper() == task_id for task in tasks(data)):
        raise SystemExit(f"task already exists: {task_id}")
    task = {
        "id": task_id,
        "title": required_text(args.title, "title"),
        "status": "open",
        "updated": today(),
        "why": required_text(args.why, "why"),
        "scope": clean_list(args.scope, "scope"),
        "acceptance": clean_list(args.acceptance, "acceptance"),
        "progress": [],
        "verification": [],
        "evidence": clean_optional_list(args.evidence, "evidence"),
    }
    draft = dict(data)
    draft["tasks"] = [*tasks(data), task]
    validate_or_exit(draft)
    if args.dry_run:
        print("Add validation OK; no file written.")
        print_task(task, compact=False)
        return 0
    data["tasks"] = draft["tasks"]
    save(path, data)
    print(f"Added {task_id}: {task['title']}")
    return 0


def cmd_progress(path: Path, data: dict[str, Any], args: argparse.Namespace) -> int:
    task = find_task(data, args.id)
    task.setdefault("progress", []).append({"date": args.date, "text": args.text})
    task["updated"] = args.date
    if task.get("status") == "open":
        task["status"] = "in_progress"
    save(path, data)
    print(f"Updated progress for {task['id']}")
    return 0


def cmd_done(path: Path, data: dict[str, Any], args: argparse.Namespace) -> int:
    task = find_task(data, args.id)
    task["status"] = "done"
    task["doneDate"] = args.date
    task["updated"] = args.date
    task.setdefault("verification", []).append({"date": args.date, "result": args.verification})
    save(path, data)
    print(f"Marked {task['id']} done")
    return 0


def cmd_validate(data: dict[str, Any], args: argparse.Namespace) -> int:
    errors = validate(data)
    if args.json:
        print(json.dumps({"ok": not errors, "errors": errors}, indent=2, ensure_ascii=False))
    elif errors:
        print("Backlog validation failed:")
        for error in errors:
            print(f"- {error}")
    else:
        open_count = sum(1 for task in tasks(data) if task.get("status") == "open")
        in_progress_count = sum(1 for task in tasks(data) if task.get("status") == "in_progress")
        done_count = sum(1 for task in tasks(data) if task.get("status") == "done")
        print(f"Backlog OK: {open_count} open, {in_progress_count} in_progress, {done_count} done")
    return 1 if errors else 0


def validate_or_exit(data: dict[str, Any]) -> None:
    errors = validate(data)
    if errors:
        raise SystemExit("backlog validation failed before save:\n" + "\n".join(f"- {e}" for e in errors))


def validate(data: dict[str, Any]) -> list[str]:
    errors: list[str] = []
    seen: set[str] = set()
    if data.get("schema") != 1:
        errors.append("schema must be 1")
    if not isinstance(data.get("rules"), list) or not data.get("rules"):
        errors.append("rules must be a non-empty list")
    for index, task in enumerate(tasks(data), start=1):
        if not isinstance(task, dict):
            errors.append(f"task #{index} must be an object")
            continue
        task_id = str(task.get("id", ""))
        for field in REQUIRED_TASK_FIELDS:
            if field not in task:
                errors.append(f"{task_id or '#'+str(index)} missing {field}")
        if not TASK_ID_RE.match(task_id):
            errors.append(f"{task_id or '#'+str(index)} has invalid id format; expected PREFIX-001")
        if task_id in seen:
            errors.append(f"duplicate task id: {task_id}")
        seen.add(task_id)
        if task.get("status") not in VALID_STATUSES:
            errors.append(f"{task_id} has invalid status {task.get('status')!r}")
        for field in ("title", "why", "updated"):
            value = task.get(field)
            if not isinstance(value, str) or not value.strip():
                errors.append(f"{task_id} must have non-empty string {field}")
        for field in ("scope", "acceptance"):
            value = task.get(field)
            if not isinstance(value, list) or not value or not all(isinstance(item, str) and item.strip() for item in value):
                errors.append(f"{task_id} must have non-empty string list {field}")
        for field in ("progress", "verification", "evidence"):
            if field in task and not isinstance(task[field], list):
                errors.append(f"{task_id} {field} must be a list")
        if task.get("status") == "done" and not task.get("verification"):
            errors.append(f"{task_id} is done but has no verification entry")
    return errors


def normalize_task_id(value: str) -> str:
    task_id = required_text(value, "id").upper()
    if not TASK_ID_RE.match(task_id):
        raise SystemExit(f"invalid task id {value!r}; expected format like UI-008 or SEC-001")
    return task_id


def required_text(value: str, field: str) -> str:
    text = value.strip()
    if not text:
        raise SystemExit(f"{field} must not be empty")
    return text


def clean_list(values: list[str], field: str) -> list[str]:
    cleaned = [item.strip() for item in values if item.strip()]
    if not cleaned:
        raise SystemExit(f"{field} must contain at least one non-empty item")
    return cleaned


def clean_optional_list(values: list[str], field: str) -> list[str]:
    cleaned = [item.strip() for item in values if item.strip()]
    if len(cleaned) != len(values):
        raise SystemExit(f"{field} contains an empty item")
    return cleaned


def print_task(task: dict[str, Any], compact: bool) -> None:
    print(f"{task['id']} [{task['status']}] {task['title']}")
    if compact:
        return
    print(f"Updated: {task.get('updated', 'unknown')}")
    print(f"Why: {task.get('why', '')}")
    print("Scope:")
    for item in task.get("scope", []):
        print(f"- {item}")
    print("Acceptance:")
    for item in task.get("acceptance", []):
        print(f"- {item}")
    evidence = task.get("evidence") or []
    if evidence:
        print("Evidence:")
        for item in evidence:
            print(f"- {item}")


def today() -> str:
    return date.today().isoformat()


if __name__ == "__main__":
    raise SystemExit(main())
