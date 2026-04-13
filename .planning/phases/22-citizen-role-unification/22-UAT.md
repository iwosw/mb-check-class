---
status: complete
phase: 22-citizen-role-unification
source:
  - 22-citizen-role-unification-01-SUMMARY.md
  - 22-citizen-role-unification-02-SUMMARY.md
  - 22-citizen-role-unification-03-SUMMARY.md
  - 22-citizen-role-unification-04-SUMMARY.md
started: 2026-04-13T15:28:12Z
updated: 2026-04-13T15:34:55Z
---

## Current Test

[testing complete]

## Tests

### 1. Recruit Interaction Path Still Looks Normal
expected: Spawn or interact with a recruit using the usual recruit flow. The recruit should still present as a normal recruit, keep the same interaction entrypoints you already expect, and not show any obvious behavior or identity regression from the citizen unification work.
result: pass

### 2. Worker Interaction Path Still Looks Normal
expected: Spawn or interact with a worker using the usual worker flow. The worker should still present as a normal worker, keep the same interaction entrypoints you already expect, and not show any obvious behavior or identity regression from the citizen unification work.
result: pass

### 3. Recruit State Survives Save And Reload
expected: Set up a recruit with recognizable state such as owner or team assignment, follow or hold settings, and visible equipment or inventory. Save and reload. The same recruit should come back with that state intact instead of losing ownership, stance, or gear.
result: pass

### 4. Worker Recovery And Work Area Survive Save And Reload
expected: Set up a worker with an assigned work area or recovery-related state, then save and reload. The worker should resume with the remembered binding or recovery state intact instead of forgetting its job context.
result: pass

## Summary

total: 4
passed: 4
issues: 0
pending: 0
skipped: 0
blocked: 0

## Gaps

[]
