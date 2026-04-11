---
phase: 2
slug: runtime-unification-design
status: draft
nyquist_compliant: true
wave_0_complete: true
created: 2026-04-11
---

# Phase 2 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | Gradle + JUnit 5 |
| **Config file** | `build.gradle` |
| **Quick run command** | `./gradlew compileJava processResources` |
| **Full suite command** | `./gradlew test` |
| **Estimated runtime** | ~60 seconds |

---

## Sampling Rate

- **After every task commit:** Run `./gradlew compileJava processResources`
- **After every plan wave:** Run `./gradlew test`
- **Before `/gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** 60 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|-----------|-------------------|-------------|--------|
| 2-01-01 | 01 | 1 | BOOT-05 | docs+metadata | `./gradlew compileJava processResources` | ✅ | ⬜ pending |
| 2-01-02 | 01 | 1 | BOOT-05 | docs+resources | `./gradlew test` | ✅ | ⬜ pending |
| 2-02-01 | 02 | 1 | BOOT-05 | docs+contract | `./gradlew compileJava processResources` | ✅ | ⬜ pending |
| 2-02-02 | 02 | 1 | BOOT-05 | docs | `./gradlew test` | ✅ | ⬜ pending |

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
- [x] Feedback latency < 60s
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** approved 2026-04-11
