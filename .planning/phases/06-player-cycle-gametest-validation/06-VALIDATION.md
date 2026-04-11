---
phase: 06
slug: player-cycle-gametest-validation
status: draft
nyquist_compliant: true
wave_0_complete: true
created: 2026-04-11
---

# Phase 06 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | Forge GameTest + Gradle |
| **Config file** | `build.gradle` |
| **Quick run command** | `./gradlew compileGameTestJava` |
| **Full suite command** | `./gradlew verifyGameTestStage` |
| **Estimated runtime** | ~120 seconds |

---

## Sampling Rate

- **After every task commit:** Run `./gradlew compileGameTestJava`
- **After every plan wave:** Run `./gradlew verifyGameTestStage`
- **Before `/gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** 120 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|-----------|-------------------|-------------|--------|
| 06-01-01 | 01 | 1 | ownership helper + slice baseline | gametest | `./gradlew compileGameTestJava` | ✅ | ⬜ pending |
| 06-01-02 | 01 | 1 | ownership slice + retained smoke | gametest | `./gradlew verifyGameTestStage` | ✅ | ⬜ pending |
| 06-02-01 | 02 | 2 | settlement labor participation | gametest | `./gradlew verifyGameTestStage` | ✅ | ⬜ pending |
| 06-03-01 | 03 | 2 | upkeep flow transition | gametest | `./gradlew verifyGameTestStage` | ✅ | ⬜ pending |
| 06-04-01 | 04 | 3 | stitched player cycle | gametest | `./gradlew verifyGameTestStage` | ✅ | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- Existing infrastructure covers all phase requirements.

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
