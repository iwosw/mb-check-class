---
status: resolved
trigger: "Investigate issue: outsider-claim-interaction-bypass\n\n**Summary:** Игроки могут разливать лаву и воду в чужих клеймах. Нужно вырубить возможность что-либо кликать в пределах клейма для чужаков, если разрешающая галочка не активирована, включая switch, destroy, place, attack и другие пути обхода, и проверить что нет обходов привата."
created: 2026-04-13T00:00:00Z
updated: 2026-04-13T01:05:00Z
---

## Current Focus

hypothesis: Confirmed and patched: outsider interactions now route through unified claim-aware block/entity denial checks.
test: human verification in a real multiplayer/private-claim workflow.
expecting: outsiders can no longer use usable blocks, pour water/lava, or directly attack/interact inside чужие claims unless permitted.
next_action: archive resolved session, commit fix/docs, and update debug knowledge base

## Symptoms

expected: Полный deny для чужака внутри чужого клейма, если соответствующий разрешающий флаг не включен. Должны блокироваться place, destroy, use/switch, attack, bucket liquid placement и другие возможные interaction paths.
actual: Сейчас чужак может как минимум разливать воду/лаву в чужом клейме и использовать usable блоки внутри чужого клейма.
errors: Нет ошибок, крашей или явных сообщений.
reproduction: Зайти чужим игроком в чужой клейм и попробовать разлить воду, разлить лаву или использовать usable блоки.
started: Неизвестно, когда началось.

## Eliminated

## Evidence

- timestamp: 2026-04-13T00:08:00Z
  checked: .planning/debug/knowledge-base.md
  found: No prior knowledge-base entry matched the reported claim interaction bypass symptoms.
  implication: Treat this as a new claim-protection defect rather than a known pattern.

- timestamp: 2026-04-13T00:12:00Z
  checked: recruits/src/main/java/com/talhanation/recruits/ClaimEvents.java
  found: Claim protection currently hooks BreakEvent, EntityPlaceEvent, FluidPlaceBlockEvent, and RightClickBlock only.
  implication: Any bypass path outside those four events is currently unguarded.

- timestamp: 2026-04-13T00:14:00Z
  checked: recruits/src/main/java/com/talhanation/recruits/ClaimEvents.java lines 336-368
  found: RightClickBlock denial is limited to a whitelist of buttons, doors, trapdoors, shulker boxes, fence gates, anvils, lever/diode/daylight detector, and Container block entities.
  implication: Generic usable blocks and non-whitelisted right-click actions inside claims can bypass protection.

- timestamp: 2026-04-13T00:16:00Z
  checked: recruits/src/main/java/com/talhanation/recruits/ClaimEvents.java lines 388-403
  found: Fluid denial checks only target/source claims and never evaluates the acting player or claim friendliness.
  implication: Bucket/liquid-related placement paths that do not rely on hostile source-claim mismatch can bypass outsider permission checks.

- timestamp: 2026-04-13T00:18:00Z
  checked: recruits/src/main/java/com/talhanation/recruits/DamageEvent.java and recruits/src/main/java/com/talhanation/recruits/RecruitEvents.java
  found: Existing attack logic is faction/team based and does not consult claim ownership or claim permissions.
  implication: Direct outsider attacks inside protected claims are not currently covered by private-claim checks.

- timestamp: 2026-04-13T00:38:00Z
  checked: ./gradlew compileJava compileGameTestJava
  found: Main and gametest sources compile successfully after the claim protection changes.
  implication: The new claim handlers and regression tests are syntactically valid in the merged root build.

- timestamp: 2026-04-13T00:50:00Z
  checked: ./gradlew runGameTestServer
  found: GameTestServer reported "All 63 required tests passed :)" after the claim protection changes.
  implication: Existing required gameplay coverage still passes with the new outsider interaction restrictions, including the added claim-protection regression scenarios.

## Resolution

root_cause: ClaimEvents only denied a narrow subset of RightClickBlock targets and never applied claim-aware checks to entity interaction or direct attack events, so outsiders could still use generic usable blocks, bucket/liquid click paths, and entity actions inside чужие claims.
fix: Replaced the whitelist-style block interaction gate with a unified outsider block interaction check, added RightClickItem plus entity interact/attack claim guards, and expanded claim protection gametests to cover generic use, bucket click, and attack denial paths.
verification: Verified by successful root compile plus full runGameTestServer; all 63 required gametests passed after adding coverage for generic block use, bucket click denial, and hostile attack denial inside claims.
files_changed: [recruits/src/main/java/com/talhanation/recruits/ClaimEvents.java, src/gametest/java/com/talhanation/bannermod/BannerModClaimProtectionGameTests.java]
