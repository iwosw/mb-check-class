---
status: resolved
trigger: "Investigate issue: formation-collision-nearest-free-slot-fallback\n\n**Summary:** При построении формации рекруты могут столкнуться друг с другом и толкаться на месте. Нужно добавить fail path: если рекрут не может пройти к своей позиции, он должен найти другую ближайшую и сейчас не занятую позицию. При этом свою исходную позицию в конфликте он освобождает."
created: 2026-04-12T00:00:00Z
updated: 2026-04-12T00:40:00Z
---

## Current Focus

hypothesis: confirmed - the stuck-aware nearest-free-slot fallback resolves the formation deadlock in real gameplay
test: archive resolved session, commit fix artifacts, and record the pattern in the debug knowledge base
expecting: session can be moved to resolved history and documented as a known formation-collision pattern
next_action: archive session and update debug knowledge base

## Symptoms

expected: When formation movement builds or reshuffles, a recruit that cannot reach its assigned slot should fall back to the nearest currently unoccupied formation position instead of deadlocking. The blocked recruit should release its original slot during this collision/fallback so another recruit can claim it.
actual: Recruits can collide while forming up and keep pushing/blocking instead of resolving to nearby free slots.
errors: No stack traces or logs; this is a runtime behavior bug.
reproduction: Reproduces through normal formation building; any formation setup can trigger it, not only a special map or command sequence.
started: Unknown.

## Eliminated

## Evidence

- timestamp: 2026-04-12T00:00:00Z
  checked: knowledge base
  found: No candidate known-pattern match for formation collision fallback behavior.
  implication: investigate code path directly.

- timestamp: 2026-04-12T00:10:00Z
  checked: FormationUtils.java
  found: All formation builders assign one holdPos per recruit and persist that via formationPos, but assignment only happens when the formation is initially built or reshuffled.
  implication: any runtime recovery from blocked slots must happen after initial assignment, not during the one-time build step.

- timestamp: 2026-04-12T00:12:00Z
  checked: RecruitHoldPosGoal.java
  found: Hold-position movement repeatedly calls navigation.moveTo(current holdPos) and only jumps on collision; it has no logic for stuck detection, slot release, or nearest-free-slot fallback.
  implication: a recruit blocked by another recruit will keep pushing toward the same slot indefinitely.

- timestamp: 2026-04-12T00:14:00Z
  checked: AsyncPathNavigation.java
  found: Navigation exposes isStuck(), so runtime stuck detection is already available to higher-level AI goals.
  implication: formation fallback can be implemented in hold-position logic without changing the pathfinding subsystem.

- timestamp: 2026-04-12T00:30:00Z
  checked: FormationUtils.java + RecruitHoldPosGoal.java
  found: Added runtime fallback logic that selects the nearest unoccupied formation slot and swaps holdPos/formationPos claims between recruits when a formation recruit is stuck/colliding.
  implication: the blocked recruit no longer keeps retrying the same slot forever and its original slot is explicitly released to the swapped recruit.

- timestamp: 2026-04-12T00:34:00Z
  checked: ./gradlew test --tests com.talhanation.recruits.util.FormationUtilsTest
  found: Focused unit tests passed.
  implication: nearest-free-slot fallback selection logic is covered and compiling in the merged root build.

## Resolution

root_cause: Formation slot assignment was effectively static. Once a recruit entered hold-position formation movement, RecruitHoldPosGoal only kept pathing back to the same holdPos and never reacted to navigation-stuck/collision states, so blocked recruits kept pushing at occupied paths without releasing or reassigning their slot.
fix: Added a runtime formation fallback helper that picks the nearest currently unoccupied formation slot, swaps slot claims between recruits so the blocked recruit releases its original slot, and invoked it from RecruitHoldPosGoal when navigation reports collision/stuck behavior.
verification: Focused unit tests passed via ./gradlew test --tests com.talhanation.recruits.util.FormationUtilsTest, and user confirmed the in-game formation collision scenario is fixed.
files_changed: [recruits/src/main/java/com/talhanation/recruits/util/FormationUtils.java, recruits/src/main/java/com/talhanation/recruits/entities/ai/RecruitHoldPosGoal.java, recruits/src/test/java/com/talhanation/recruits/util/FormationUtilsTest.java]
