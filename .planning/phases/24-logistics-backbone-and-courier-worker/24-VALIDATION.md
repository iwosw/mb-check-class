---
phase: 24
slug: logistics-backbone-and-courier-worker
status: draft
nyquist_compliant: true
wave_0_complete: true
created: 2026-04-18
---

# Phase 24 — Validation Strategy

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5 + Forge GameTest |
| **Quick run command** | `./gradlew test --tests com.talhanation.bannermod.logistics.* --console=plain` |
| **Full suite command** | `./gradlew compileJava test compileGameTestJava verifyGameTestStage --console=plain` |
| **Estimated runtime** | ~120 seconds |

## Sampling Rate

- After each logic-heavy task: run `./gradlew test --tests com.talhanation.bannermod.logistics.* --console=plain`
- After each UI or packet task: run `./gradlew compileJava --console=plain`
- Before phase closeout: run `./gradlew compileJava test compileGameTestJava verifyGameTestStage --console=plain`

## Per-Plan Verification Map

| Task ID | Plan | Requirement | Test Type | Automated Command | Status |
|---------|------|-------------|-----------|-------------------|--------|
| 24-01-01 | 01 | logistics contracts | compile | `./gradlew compileJava --console=plain` | ✅ passed (2026-04-18) |
| 24-02-01 | 02 | reservation/runtime | unit | `./gradlew test --tests com.talhanation.bannermod.logistics.BannerModLogisticsServiceTest --console=plain` | ✅ passed (2026-04-18) |
| 24-03-01 | 03 | courier execution | unit | `./gradlew test --tests com.talhanation.bannermod.ai.civilian.CourierTaskFlowTest --console=plain` | ✅ passed (2026-04-18) |
| 24-04-01 | 04 | route authoring UI | compile | `./gradlew compileJava --console=plain` | ✅ passed (2026-04-18) |
| 24-05-01 | 05 | live logistics validation | full | `./gradlew compileJava test compileGameTestJava verifyGameTestStage --console=plain` | ✅ passed (2026-04-19: `compileJava`, `compileTestJava`, `compileGameTestJava`, targeted logistics/courier tests, and `verifyGameTestStage` all pass after isolating the GameTest world and fixing synced-data registration order) |

## Manual-Only Verifications

| Behavior | Why Manual | Test Instructions |
|----------|------------|-------------------|
| Route authoring readability and blocked-state clarity | UI clarity is difficult to fully prove from automated output | Open the route authoring screen in a dev client, create or edit a route, and confirm priority/filter settings plus blocked-state feedback are understandable without debug logs. |

## Validation Sign-Off

- [x] All planned tasks have an automated verification path
- [x] Sampling continuity stays below 3 unchecked tasks in a row
- [x] No watch-mode flags
- [x] `nyquist_compliant: true` is set

**Approval:** complete - Plans 24-01 through 24-05 are now verified. On 2026-04-19 the closeout path was re-run end-to-end: targeted logistics and courier unit tests pass, `compileTestJava` and `compileGameTestJava` pass, and `verifyGameTestStage` completes green with all 37 required GameTests passing after isolating the GameTest world and fixing synced-data registration order.

## Current Blockers

- No active blockers remain for the historical `24-05` closeout. Remaining compact-Phase-24 work is the folded treasury, upkeep, trade-route, and sea-port foundation beyond the historical logistics plan set.
- Compact-Phase-24 treasury follow-up is not fully re-verified yet: on 2026-04-20 the focused governance/treasury JUnit command was blocked during `compileJava` by unrelated pre-existing `BannerModSettlementService` constructor mismatches outside the treasury/governor slice.
