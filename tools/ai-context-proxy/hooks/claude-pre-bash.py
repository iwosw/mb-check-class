#!/usr/bin/env python3
"""Compatibility wrapper for the shared agent Bash guardrails hook."""

from __future__ import annotations

import runpy
from pathlib import Path


if __name__ == "__main__":
    hook = Path(__file__).resolve().parents[2] / "agent-hooks" / "pre-bash-guardrails.py"
    runpy.run_path(str(hook), run_name="__main__")
