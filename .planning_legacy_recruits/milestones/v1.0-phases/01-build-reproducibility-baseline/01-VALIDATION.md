---
phase: 01
slug: build-reproducibility-baseline
status: draft
nyquist_compliant: true
wave_0_complete: true
created: 2026-04-05
---

# Phase 01 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | Gradle 8.8 lifecycle tasks + JUnit 5 smoke test baseline + Forge GameTest server task |
| **Config file** | `build.gradle` |
| **Quick run command** | `./gradlew buildEnvironment` |
| **Full suite command** | `./gradlew clean build && ./gradlew check --continue` |
| **Estimated runtime** | ~120 seconds |

---

## Sampling Rate

- **After every task commit:** Run that task's `<automated>` command from the plan
- **After every plan wave:** Run `./gradlew clean build && ./gradlew check --continue`
- **Before `/gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** 120 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|-----------|-------------------|-------------|--------|
| 01-01-01 | 01 | 1 | BLD-03 | build | `./gradlew buildEnvironment` | ✅ | ⬜ pending |
| 01-02-01 | 02 | 2 | BLD-02 | unit | `./gradlew test` | ✅ / ❌ W0 | ⬜ pending |
| 01-02-02 | 02 | 2 | BLD-04 | game-test | `./gradlew check --continue` | ✅ | ⬜ pending |
| 01-03-01 | 03 | 3 | BLD-01 | docs | `grep -q "./gradlew build" README.md && grep -q "./gradlew build" BUILDING.md` | ✅ / ❌ W0 | ⬜ pending |
| 01-03-02 | 03 | 3 | BLD-04 | docs | `grep -q "game test" BUILDING.md && grep -q "allowLocalMaven" BUILDING.md` | ✅ / ❌ W0 | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

Existing infrastructure covers all phase requirements.

---

## Manual-Only Verifications

All phase behaviors have automated verification.

---

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references
- [x] No watch-mode flags
- [x] Feedback latency < 120s
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
