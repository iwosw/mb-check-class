---
phase: 23
slug: settlement-governance-and-governor-control
status: draft
nyquist_compliant: true
wave_0_complete: true
created: 2026-04-13
---

# Phase 23 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5 + Forge GameTest |
| **Config file** | `build.gradle` |
| **Quick run command** | `./gradlew test --tests com.talhanation.bannermod.governance.* --console=plain` |
| **Full suite command** | `./gradlew compileJava test compileGameTestJava verifyGameTestStage --console=plain` |
| **Estimated runtime** | ~90 seconds |

---

## Sampling Rate

- **After every task commit:** Run `./gradlew test --tests com.talhanation.bannermod.governance.* --console=plain` when the task adds or changes governance logic or tests; otherwise run `./gradlew compileJava --console=plain`
- **After every plan wave:** Run `./gradlew compileJava test --console=plain`
- **Before verification:** `./gradlew compileJava test compileGameTestJava verifyGameTestStage --console=plain`
- **Max feedback latency:** 90 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|-----------|-------------------|-------------|--------|
| 23-01-01 | 01 | 1 | GOV-01 | syntax | `python3 - <<'PY'\nfrom pathlib import Path\nrequired = [\n Path('src/main/java/com/talhanation/bannermod/governance/BannerModGovernorSnapshot.java'),\n Path('src/main/java/com/talhanation/bannermod/governance/BannerModGovernorRules.java')\n]\nmissing = [str(p) for p in required if not p.exists()]\nif missing: raise SystemExit('Missing: ' + ', '.join(missing))\nPY` | ✅ | ⬜ pending |
| 23-01-02 | 01 | 1 | GOV-01 | unit | `./gradlew test --tests com.talhanation.bannermod.governance.BannerModGovernorRulesTest --console=plain` | ✅ | ⬜ pending |
| 23-02-01 | 02 | 2 | GOV-02 | unit | `./gradlew test --tests com.talhanation.bannermod.governance.BannerModGovernorServiceTest --console=plain` | ✅ | ⬜ pending |
| 23-02-02 | 02 | 2 | GOV-02 | compile | `./gradlew compileJava --console=plain` | ✅ | ⬜ pending |
| 23-03-01 | 03 | 2 | GOV-03 | unit | `./gradlew test --tests com.talhanation.bannermod.governance.BannerModGovernorHeartbeatTest --console=plain` | ✅ | ⬜ pending |
| 23-03-02 | 03 | 2 | GOV-03 | compile | `./gradlew compileJava --console=plain` | ✅ | ⬜ pending |
| 23-04-01 | 04 | 3 | GOV-02 | compile | `./gradlew compileJava --console=plain` | ✅ | ⬜ pending |
| 23-04-02 | 04 | 3 | GOV-03 | compile | `./gradlew compileJava test --console=plain` | ✅ | ⬜ pending |
| 23-05-01 | 05 | 4 | GOV-04 | gametest | `./gradlew compileGameTestJava --console=plain` | ✅ | ⬜ pending |
| 23-05-02 | 05 | 4 | GOV-04 | full | `./gradlew compileJava test compileGameTestJava verifyGameTestStage --console=plain` | ✅ | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

Existing infrastructure covers all phase requirements.

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Governor control screen layout and copy clarity | GOV-04 | UI readability is hard to fully prove from automated output alone | Open the governor UI in a dev client, confirm governor status, settlement status, tax summary, recommendations, and incidents are visible and the promote button no longer dead-ends. |

---

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references
- [x] No watch-mode flags
- [x] Feedback latency < 90s
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
