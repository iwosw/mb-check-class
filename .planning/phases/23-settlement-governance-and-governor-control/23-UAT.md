---
status: testing
phase: 23-settlement-governance-and-governor-control
source:
  - 23-settlement-governance-and-governor-control-01-SUMMARY.md
  - 23-settlement-governance-and-governor-control-02-SUMMARY.md
  - 23-settlement-governance-and-governor-control-03-SUMMARY.md
  - 23-settlement-governance-and-governor-control-04-SUMMARY.md
  - 23-settlement-governance-and-governor-control-05-SUMMARY.md
  - 23-settlement-governance-and-governor-control-06-SUMMARY.md
  - 23-settlement-governance-and-governor-control-07-SUMMARY.md
started: 2026-04-16T13:00:00Z
updated: 2026-04-18T09:30:57Z
---

## Current Test

number: 3
name: Policy Toggles — Garrison, Fortification, Tax Pressure
expected: |
  On the governor control screen, three policy toggles are visible:
  garrison priority, fortification priority, and tax pressure.
  Each has +/- buttons to step through values.
  Changing a value should persist — close and reopen the screen to verify the value remains.
awaiting: user response

## Tests

### 1. Governor Promotion via Promote Screen
expected: In a friendly claim, right-click a recruit and use the Promote screen to designate them as governor (profession id 6). The button should only be enabled when inside a friendly claim you own. After promotion, the recruit becomes the claim's governor.
result: pass

### 2. Governor Control Screen — Live Data
expected: After designating a governor, open the governor control screen. It should display: settlement status, local tax summary, garrison recommendations, fortification recommendations, and any active incidents. All fields should show real data from the governed claim, not placeholder text.
result: issue
reported: "It shows empty menu with six buttons at the right bottom corner \"+\" three and \"-\" three. Nothing else. Also, recruit somehow doesn't save that he is governor, when I RMB him it shows default recruit inventory, and I can again promote him to governor."
severity: major

### 3. Policy Toggles — Garrison, Fortification, Tax Pressure
expected: On the governor control screen, three policy toggles are visible: garrison priority, fortification priority, and tax pressure. Each has +/- buttons to step through values. Changing a value should persist — close and reopen the screen to verify the value remains.
result: [pending]

### 4. Governor State Persistence
expected: Designate a governor, close the game or reload the world, then reopen. The governor designation should still be active on the same claim, with the same recruit as governor and the same policy settings.
result: [pending]

### 5. Hostile Claim Swap Degrades Governor
expected: While a governor is active on a friendly claim, change the claim's faction (or simulate an enemy faction takeover). The governor status should degrade — the system should deny governance operations on a hostile or mismatched claim instead of silently continuing.
result: [pending]

### 6. Build Gate — compileJava + compileGameTestJava + verifyGameTestStage
expected: Running `./gradlew compileJava compileGameTestJava` completes successfully (exit 0). Running `verifyGameTestStage` executes the three BannerModGovernorControlGameTests and they all pass. (Pre-existing non-governor test failures are acceptable.)
result: [pending]

## Summary

total: 6
passed: 1
issues: 1
pending: 4
skipped: 0
blocked: 0

## Gaps

- truth: "Governor control screen displays live settlement status, local tax summary, garrison recommendations, fortification recommendations, and active incidents from the governed claim instead of placeholder or empty UI."
  status: failed
  reason: "User reported: It shows empty menu with six buttons at the right bottom corner \"+\" three and \"-\" three. Nothing else. Also, recruit somehow doesn't save that he is governor, when I RMB him it shows default recruit inventory, and I can again promote him to governor."
  severity: major
  test: 2
  artifacts: []
  missing: []
