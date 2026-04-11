---
status: diagnosed
trigger: "Investigate why Phase 14 optional GameTests `formationCohortRetargetsAfterSharedEnemyDies` and `formationCohortClearsDeadTargetsWhenNoReplacementExists` still fail during root `./gradlew verifyGameTestStage` in /home/kaiserroman/bannermod after commits 272115a0, f8a19731, and 8e1e5a28. You may read code, inspect the saved run output at /home/kaiserroman/.local/share/opencode/tool-output/tool_d7d70bc06001d0DFCbLuuWcK5U, and inspect relevant gametest files/resources. Focus on the smallest likely root cause and return: (1) a concise diagnosis with evidence, (2) the minimal code change you recommend, and (3) any verification command most likely to confirm it. Do not edit files."
created: 2026-04-11T00:00:00Z
updated: 2026-04-11T00:55:00Z
---

## Current Focus

hypothesis: confirmed — the remaining failure is in live candidate filtering, not controller invalidation; dead enemies can still be reconsidered during refresh because searchForTargets filters candidates with targetingConditions only, not an explicit alive/removal check
test: root cause identified; return diagnosis and minimal recommended code change
expecting: replacing candidate filtering with isValidSharedTarget (or equivalent alive/removal guard) will stop dead targets from being republished as replacement/null outcomes
next_action: report concise diagnosis, minimal fix, and verification command

## Symptoms

expected: Phase 14 optional GameTests formationCohortRetargetsAfterSharedEnemyDies and formationCohortClearsDeadTargetsWhenNoReplacementExists pass during root ./gradlew verifyGameTestStage
actual: both optional GameTests still fail after commits 272115a0, f8a19731, and 8e1e5a28
errors: failing optional GameTests during verifyGameTestStage; exact assertions pending log inspection
reproduction: run ./gradlew verifyGameTestStage from repository root
started: after commits 272115a0, f8a19731, and 8e1e5a28 still did not resolve failures

## Eliminated

## Evidence

- timestamp: 2026-04-11T00:05:00Z
  checked: .planning/debug/knowledge-base.md
  found: No knowledge base file exists yet
  implication: No prior resolved pattern to test first

- timestamp: 2026-04-11T00:06:00Z
  checked: repository grep for failing test names
  found: Both failures are defined in recruits/src/gametest/java/com/talhanation/recruits/gametest/battle/FormationTargetSelectionGameTests.java
  implication: One gametest class defines the expected retarget/clear behavior and should reveal the shared code path

- timestamp: 2026-04-11T00:15:00Z
  checked: saved verifyGameTestStage output and FormationTargetSelectionGameTests
  found: The two optional failures are exact assertion failures: retarget test says recruits never get a living replacement target, and no-replacement test says recruits keep stale non-null targets after all enemies die
  implication: Both tests fail in the same post-death refresh path, likely where shared formation target assignments are reused or invalidated

- timestamp: 2026-04-11T00:24:00Z
  checked: FormationTargetSelectionController and AbstractRecruitEntity target search flow
  found: Every search begins with beginRuntimeSelection(); if a cohort assignment already exists and passes isValidSharedTarget it is reused immediately, otherwise computation updates the shared assignment via completeRuntimeSelection(), including storing null assignments for formation-eligible cohorts
  implication: The bug is likely not broad combat logic but a narrow mismatch in how shared assignments are invalidated/reused across the two recruits in the recovery tests

- timestamp: 2026-04-11T00:36:00Z
  checked: .planning/phases/14-formation-level-target-selection-rewrite/14-formation-level-target-selection-rewrite-03-SUMMARY.md
  found: The last three commits explicitly targeted same-tick shared-target invalidation/null publication in the controller seam, but Phase 14 still documented the brownfield GameTests as failing after those fixes
  implication: The unresolved bug is likely outside the controller's same-tick null-sharing rule and in the surrounding live candidate-search behavior

- timestamp: 2026-04-11T00:49:00Z
  checked: nested recruits commits plus current AbstractRecruitEntity searchForTargets implementation
  found: Commit 8e1e5a28 only changed controller invalidation/null-publication behavior, while both search paths still build `nearby` with `getEntitiesOfClass(... entity -> entity != this)` and then remove candidates using only `targetingConditions.test(this, potTarget)` before `chooseTargetFromCandidates(...)`
  implication: controller invalidation can work correctly yet still republish a dead enemy if the live candidate scan does not explicitly exclude dead/removed entities

- timestamp: 2026-04-11T00:52:00Z
  checked: FormationTargetSelectionGameTests recovery harness behavior
  found: The retarget test kills the shared east target and waits 5 ticks before west refresh; the no-replacement test kills all east recruits and also waits 5 ticks. During that window, dead entities can still remain in the world entity list even though the controller has invalidated the prior shared assignment.
  implication: the smallest shared explanation for both failures is that refresh reselects dead east recruits from the nearby entity scan instead of choosing the surviving enemy or null

## Resolution

root_cause: AbstractRecruitEntity.searchForTargetsSync/searchForTargetsAsync still filter nearby candidates with targetingConditions alone, so after the controller invalidates a dead shared assignment the next selection pass can still choose a just-killed enemy that remains in the world list for a few ticks. That makes the recovery tests reassign dead targets instead of retargeting to the survivor or clearing to null.
fix: In both target-search paths, replace the candidate filter `!targetingConditions.test(this, potTarget)` with `!isValidSharedTarget(potTarget)` (or add equivalent `isAlive`/`!isRemoved` guards before chooseTargetFromCandidates). Keep the controller logic unchanged.
verification: Run the two focused optional GameTests after the filter change, then run root verifyGameTestStage if they pass.
files_changed: ["recruits/src/main/java/com/talhanation/recruits/entities/AbstractRecruitEntity.java"]
