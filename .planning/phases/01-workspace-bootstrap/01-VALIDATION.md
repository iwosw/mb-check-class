---
phase: 1
slug: workspace-bootstrap
status: draft
nyquist_compliant: true
wave_0_complete: true
created: 2026-04-11
---

# Phase 1 — Validation Strategy

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
- **Before verification:** Full suite must be green
- **Max feedback latency:** 60 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|-----------|-------------------|-------------|--------|
| 1-01-01 | 01 | 1 | BOOT-01 | build | `./gradlew compileJava processResources` | ✅ | ⬜ pending |
| 1-01-02 | 01 | 1 | BOOT-02 | docs | `./gradlew test` | ✅ | ⬜ pending |
| 1-02-01 | 02 | 1 | BOOT-03 | docs | `./gradlew compileJava processResources` | ✅ | ⬜ pending |
| 1-02-02 | 02 | 1 | BOOT-04 | docs | `./gradlew test` | ✅ | ⬜ pending |

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
