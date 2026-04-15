package com.talhanation.recruits.gametest.battle;

import com.talhanation.recruits.Main;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.entities.ai.FormationTargetSelectionController;
import com.talhanation.recruits.entities.ai.navigation.RecruitPathNavigation;
import com.talhanation.recruits.config.RecruitsServerConfig;
import com.talhanation.recruits.gametest.support.RecruitsBattleGameTestSupport;
import com.talhanation.recruits.pathfinding.GlobalPathfindingController;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
@GameTestHolder(Main.MOD_ID)
public class BattleStressGameTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "battle_density_field", timeoutTicks = BattleStressFixtures.BASELINE_DENSE_TIMEOUT_TICKS)
    public static void baselineDenseBattleCompletesWithoutBrokenLoops(GameTestHelper helper) {
        resetProfiling();
        BattleStressFixtures.ScenarioState scenarioState = BattleStressFixtures.spawnBaselineDenseScenario(helper);
        helper.runAfterDelay(scenarioState.scenario().progressProbeTicks(), () -> assertScenarioProgress(helper, scenarioState));
        helper.runAfterDelay(scenarioState.scenario().resolveDeadlineTicks(), () -> assertScenarioResolved(helper, scenarioState));
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "battle_density_field", timeoutTicks = BattleStressFixtures.HEAVY_DENSE_TIMEOUT_TICKS)
    public static void heavierDenseBattleCompletesWithoutBrokenLoops(GameTestHelper helper) {
        resetProfiling();
        BattleStressFixtures.ScenarioState scenarioState = BattleStressFixtures.spawnHeavyDenseScenario(helper);
        helper.runAfterDelay(scenarioState.scenario().progressProbeTicks(), () -> assertScenarioProgress(helper, scenarioState));
        helper.runAfterDelay(scenarioState.scenario().resolveDeadlineTicks(), () -> assertScenarioResolved(helper, scenarioState));
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "battle_density_field", timeoutTicks = BattleStressFixtures.FLOW_FIELD_BENCHMARK_TIMEOUT_TICKS)
    public static void sameDestinationLaneExercisesOptionalFlowFieldPrototype(GameTestHelper helper) {
        RecruitsBattleGameTestSupport.BattleProfilingSnapshot[] baselineSnapshot = new RecruitsBattleGameTestSupport.BattleProfilingSnapshot[1];
        RecruitPathNavigation.clearFlowFieldPrototypeTestOverrides();
        resetProfiling();
        BattleStressFixtures.FlowFieldBenchmarkState baselineState = BattleStressFixtures.spawnFlowFieldBenchmarkScenario(helper);
        helper.runAfterDelay(baselineState.scenario().captureTicks(), () -> {
            baselineSnapshot[0] = captureFlowFieldSnapshot(helper, baselineState, "baseline");
            assertFlowFieldBenchmarkSnapshot(baselineSnapshot[0], false);

            for (AbstractRecruitEntity recruit : baselineState.recruits()) {
                recruit.kill();
            }

            resetProfiling();
            BattleStressFixtures.FlowFieldBenchmarkState prototypeState = BattleStressFixtures.spawnFlowFieldBenchmarkScenario(helper);
            for (AbstractRecruitEntity recruit : prototypeState.recruits()) {
                RecruitPathNavigation.setFlowFieldPrototypeTestOverride(recruit, true);
            }

            helper.runAfterDelay(prototypeState.scenario().captureTicks(), () -> {
                try {
                    RecruitsBattleGameTestSupport.BattleProfilingSnapshot prototypeSnapshot = captureFlowFieldSnapshot(helper, prototypeState, "prototype-enabled");
                    assertFlowFieldBenchmarkSnapshot(prototypeSnapshot, true);
                    Main.LOGGER.info("Phase 18 same-destination benchmark comparison: baseline={}, prototype-enabled={}",
                            RecruitsBattleGameTestSupport.formatProfilingSnapshot(baselineSnapshot[0]),
                            RecruitsBattleGameTestSupport.formatProfilingSnapshot(prototypeSnapshot));
                    helper.succeed();
                } finally {
                    RecruitPathNavigation.clearFlowFieldPrototypeTestOverrides();
                }
            });
        });
    }

    private static void assertScenarioProgress(GameTestHelper helper, BattleStressFixtures.ScenarioState scenarioState) {
        int westLosses = countLosses(scenarioState.westRecruits());
        int eastLosses = countLosses(scenarioState.eastRecruits());
        int combinedLosses = westLosses + eastLosses;

        if (combinedLosses < scenarioState.scenario().minimumCombinedLossesAtProgressProbe()) {
            throw new IllegalArgumentException("Expected stress scenario " + scenarioState.scenario().scenarioId()
                    + " to show visible attrition by tick " + scenarioState.scenario().progressProbeTicks()
                    + "; losses were west=" + westLosses + ", east=" + eastLosses);
        }

        assertProfilingSnapshot(helper, scenarioState, "progress probe");
        assertLivingTargets(helper, scenarioState, "progress probe");
    }

    private static void assertScenarioResolved(GameTestHelper helper, BattleStressFixtures.ScenarioState scenarioState) {
        List<AbstractRecruitEntity> westAlive = aliveRecruits(scenarioState.westRecruits());
        List<AbstractRecruitEntity> eastAlive = aliveRecruits(scenarioState.eastRecruits());
        List<AbstractRecruitEntity> winners = winningSideAlive(scenarioState, westAlive, eastAlive);
        List<AbstractRecruitEntity> losers = losingSideAlive(scenarioState, westAlive, eastAlive);

        if (!losers.isEmpty()) {
            throw new IllegalArgumentException("Expected stress scenario " + scenarioState.scenario().scenarioId()
                    + " to resolve with a " + scenarioState.scenario().expectedWinner() + " win before the stability deadline"
                    + "; survivors were west=" + describeSurvivors(westAlive) + ", east=" + describeSurvivors(eastAlive));
        }

        if (winners.isEmpty()) {
            throw new IllegalArgumentException("Expected stress scenario " + scenarioState.scenario().scenarioId()
                    + " to leave a surviving " + scenarioState.scenario().expectedWinner() + " side instead of a total wipe");
        }

        for (AbstractRecruitEntity recruit : winners) {
            if (!scenarioState.scenario().arenaBounds().contains(helper, recruit.blockPosition())) {
                throw new IllegalArgumentException("Expected surviving recruit to stay inside the dedicated stress arena bounds; recruit="
                        + recruit.getName().getString() + ", pos=" + recruit.blockPosition());
            }
        }

        assertProfilingSnapshot(helper, scenarioState, "resolution");
        assertLivingTargets(helper, scenarioState, "resolution");

        helper.succeed();
    }

    private static void assertProfilingSnapshot(GameTestHelper helper, BattleStressFixtures.ScenarioState scenarioState, String phase) {
        RecruitsBattleGameTestSupport.BattleProfilingSnapshot snapshot = RecruitsBattleGameTestSupport.captureProfilingSnapshot(
                helper,
                scenarioState.scenario().scenarioId(),
                BattleStressFixtures.PROFILING_WARMUP_TICKS
        );

        Main.LOGGER.info("Battle stress profiling snapshot [{}]: {}",
                phase,
                RecruitsBattleGameTestSupport.formatProfilingSnapshot(snapshot));

        if (snapshot.targetSearch().totalSearches() <= 0) {
            throw new IllegalArgumentException("Expected stress scenario " + scenarioState.scenario().scenarioId()
                    + " to record target-search activity by " + phase);
        }

        if (snapshot.targetSearch().candidateEntitiesObserved() <= 0) {
            throw new IllegalArgumentException("Expected stress scenario " + scenarioState.scenario().scenarioId()
                    + " to observe nearby target candidates by " + phase);
        }

        if (snapshot.targetSearch().searchOpportunities() <= 0) {
            throw new IllegalArgumentException("Expected stress scenario " + scenarioState.scenario().scenarioId()
                    + " to record target-search cadence opportunities by " + phase);
        }

        long lodTierTicks = snapshot.targetSearch().lodFullTierTicks()
                + snapshot.targetSearch().lodReducedTierTicks()
                + snapshot.targetSearch().lodShedTierTicks();
        if (lodTierTicks <= 0) {
            throw new IllegalArgumentException("Expected stress scenario " + scenarioState.scenario().scenarioId()
                    + " to record AI LOD tier activity by " + phase);
        }

        if (snapshot.targetSearch().lodSkippedSearches() < 0
                || snapshot.targetSearch().lodReducedTierTicks() < 0
                || snapshot.targetSearch().lodShedTierTicks() < 0) {
            throw new IllegalArgumentException("Expected stress scenario " + scenarioState.scenario().scenarioId()
                    + " to keep AI LOD counters non-negative by " + phase);
        }

        if (snapshot.targetSearch().lodSkippedSearches() > snapshot.targetSearch().searchOpportunities()) {
            throw new IllegalArgumentException("Expected stress scenario " + scenarioState.scenario().scenarioId()
                    + " to keep skipped target-search accounting bounded by cadence opportunities by " + phase);
        }

        if ("heavy_dense_battle".equals(scenarioState.scenario().scenarioId())
                && snapshot.targetSearch().lodSkippedSearches() <= 0) {
            throw new IllegalArgumentException("Expected heavy dense battle to exercise target-search shedding by " + phase);
        }

        if (RecruitsServerConfig.UseAsyncPathfinding.get()
                && snapshot.pathfinding().queueSubmissions() <= 0
                && snapshot.pathfinding().awaitCalls() <= 0
                && snapshot.pathfinding().totalDeliveryCallbacks() <= 0) {
            throw new IllegalArgumentException("Expected stress scenario " + scenarioState.scenario().scenarioId()
                    + " to record async pathfinding activity by " + phase);
        }

        if (snapshot.pathfinding().queueSubmissions() > 0
                && snapshot.pathfinding().totalDeliveryCallbacks() + snapshot.pathfinding().droppedCallbacks() <= 0
                && snapshot.pathfinding().syncFallbacks() <= 0) {
            throw new IllegalArgumentException("Expected stress scenario " + scenarioState.scenario().scenarioId()
                    + " to expose either delivered or explicitly dropped async callbacks by " + phase);
        }

        if (snapshot.controller().totalRequests() <= 0) {
            throw new IllegalArgumentException("Expected stress scenario " + scenarioState.scenario().scenarioId()
                    + " to record global controller activity by " + phase);
        }

        if (snapshot.controller().reuseAttempts() <= 0) {
            throw new IllegalArgumentException("Expected stress scenario " + scenarioState.scenario().scenarioId()
                    + " to record path-reuse attempts by " + phase);
        }

        if (snapshot.controller().reuseHits() + snapshot.controller().reuseMisses() != snapshot.controller().reuseAttempts()) {
            throw new IllegalArgumentException("Expected stress scenario " + scenarioState.scenario().scenarioId()
                    + " to keep reuse accounting balanced by " + phase);
        }

        if (snapshot.controller().flowFieldPrototypeAttempts()
                != snapshot.controller().flowFieldPrototypeHits() + snapshot.controller().flowFieldPrototypeFallbacks()) {
            throw new IllegalArgumentException("Expected stress scenario " + scenarioState.scenario().scenarioId()
                    + " to keep optional flow-field attempt accounting balanced by " + phase);
        }

        if (snapshot.controller().flowFieldPrototypeAttempts() > snapshot.controller().flowFieldEligibleRequests()) {
            throw new IllegalArgumentException("Expected stress scenario " + scenarioState.scenario().scenarioId()
                    + " to keep optional flow-field attempts bounded by eligible requests by " + phase);
        }

        if (RecruitsServerConfig.EnableOptionalFlowFieldPrototype.get()
                && snapshot.controller().flowFieldEligibleRequests() > 0
                && snapshot.controller().flowFieldPrototypeAttempts() <= 0) {
            throw new IllegalArgumentException("Expected stress scenario " + scenarioState.scenario().scenarioId()
                    + " to attempt the optional flow-field path when the prototype is enabled by " + phase);
        }

        assertBudgetAccounting(snapshot, scenarioState, phase);

        long formationActivity = snapshot.formationTargeting().formationSelectionComputations()
                + snapshot.formationTargeting().formationSelectionAssignments()
                + snapshot.formationTargeting().formationSelectionReuses()
                + snapshot.formationTargeting().formationSelectionInvalidations()
                + snapshot.formationTargeting().localFallbackSearches();

        if (snapshot.formationTargeting().formationSelectionRequests() <= 0 || formationActivity <= 0) {
            throw new IllegalArgumentException("Expected stress scenario " + scenarioState.scenario().scenarioId()
                    + " to record observable formation target-selection activity by " + phase);
        }
    }

    private static RecruitsBattleGameTestSupport.BattleProfilingSnapshot captureFlowFieldSnapshot(GameTestHelper helper,
                                                                                                   BattleStressFixtures.FlowFieldBenchmarkState benchmarkState,
                                                                                                   String phase) {
        RecruitsBattleGameTestSupport.BattleProfilingSnapshot snapshot = RecruitsBattleGameTestSupport.captureProfilingSnapshot(
                helper,
                benchmarkState.scenario().scenarioId(),
                0
        );
        Main.LOGGER.info("Flow-field benchmark snapshot [{}]: {}", phase, RecruitsBattleGameTestSupport.formatProfilingSnapshot(snapshot));
        return snapshot;
    }

    private static void assertFlowFieldBenchmarkSnapshot(RecruitsBattleGameTestSupport.BattleProfilingSnapshot snapshot,
                                                         boolean prototypeEnabled) {
        if (snapshot.controller().totalRequests() <= 0) {
            throw new IllegalArgumentException("Expected same-destination benchmark to record controller requests");
        }
        if (snapshot.controller().flowFieldEligibleRequests() <= 0) {
            throw new IllegalArgumentException("Expected same-destination benchmark to produce flow-field-eligible requests");
        }
        if (prototypeEnabled && snapshot.controller().flowFieldPrototypeAttempts() <= 0) {
            throw new IllegalArgumentException("Expected prototype-enabled same-destination benchmark to attempt the optional flow-field path");
        }
        if (!prototypeEnabled && snapshot.controller().flowFieldPrototypeAttempts() != 0) {
            throw new IllegalArgumentException("Expected baseline same-destination benchmark to avoid prototype attempts while disabled");
        }
        if (snapshot.controller().flowFieldPrototypeAttempts()
                != snapshot.controller().flowFieldPrototypeHits() + snapshot.controller().flowFieldPrototypeFallbacks()) {
            throw new IllegalArgumentException("Expected same-destination benchmark flow-field accounting to stay balanced");
        }
    }

    private static void assertBudgetAccounting(RecruitsBattleGameTestSupport.BattleProfilingSnapshot snapshot,
                                               BattleStressFixtures.ScenarioState scenarioState,
                                               String phase) {
        if (snapshot.controller().requestBudgetPerTick() <= 0) {
            throw new IllegalArgumentException("Expected stress scenario " + scenarioState.scenario().scenarioId()
                    + " to expose a positive controller budget by " + phase);
        }

        if (snapshot.controller().budgetUsedThisTick() < 0
                || snapshot.controller().budgetUsedThisTick() > snapshot.controller().requestBudgetPerTick()) {
            throw new IllegalArgumentException("Expected stress scenario " + scenarioState.scenario().scenarioId()
                    + " to keep budget usage within the configured per-tick capacity by " + phase);
        }

        if (snapshot.controller().deferredResumes() > snapshot.controller().deferredRequests()) {
            throw new IllegalArgumentException("Expected stress scenario " + scenarioState.scenario().scenarioId()
                    + " to keep deferred resumes bounded by deferred requests by " + phase);
        }

        long dropBreakdown = snapshot.controller().deferredDropsBacklogCap()
                + snapshot.controller().deferredDropsMaxAge()
                + snapshot.controller().deferredDropsInvalidated();
        if (dropBreakdown != snapshot.controller().deferredDrops()) {
            throw new IllegalArgumentException("Expected stress scenario " + scenarioState.scenario().scenarioId()
                    + " to keep deferred drop accounting balanced by " + phase);
        }

        if (snapshot.controller().currentDeferredQueueDepth() < 0
                || snapshot.controller().currentDeferredQueueDepth() > snapshot.controller().maxDeferredQueueDepth()) {
            throw new IllegalArgumentException("Expected stress scenario " + scenarioState.scenario().scenarioId()
                    + " to keep deferred queue depth within the recorded maximum by " + phase);
        }

        if (snapshot.controller().maxDeferredLatencyTicks() > snapshot.controller().totalDeferredLatencyTicks()) {
            throw new IllegalArgumentException("Expected stress scenario " + scenarioState.scenario().scenarioId()
                    + " to keep deferred latency totals consistent by " + phase);
        }

        if (snapshot.controller().deferredRequests() > 0 && snapshot.controller().maxDeferredQueueDepth() <= 0) {
            throw new IllegalArgumentException("Expected stress scenario " + scenarioState.scenario().scenarioId()
                    + " to record non-zero queue depth when requests are deferred by " + phase);
        }

        long observedBudgetPressure = snapshot.controller().deferredRequests()
                + snapshot.controller().deferredResumes()
                + snapshot.controller().deferredDrops();
        if (snapshot.controller().budgetUsedThisTick() <= 0 && observedBudgetPressure <= 0) {
            throw new IllegalArgumentException("Expected stress scenario " + scenarioState.scenario().scenarioId()
                    + " to record either active controller budget usage or explicit deferral pressure by " + phase);
        }
    }

    private static void resetProfiling() {
        AbstractRecruitEntity.resetTargetSearchProfiling();
        com.talhanation.recruits.pathfinding.AsyncPathProcessor.resetProfiling();
        GlobalPathfindingController.resetProfiling();
        FormationTargetSelectionController.resetProfiling();
    }

    private static void assertLivingTargets(GameTestHelper helper, BattleStressFixtures.ScenarioState scenarioState, String phase) {
        for (AbstractRecruitEntity recruit : aliveRecruits(scenarioState.westRecruits())) {
            assertLivingTarget(recruit, scenarioState, phase);
        }

        for (AbstractRecruitEntity recruit : aliveRecruits(scenarioState.eastRecruits())) {
            assertLivingTarget(recruit, scenarioState, phase);
        }
    }

    private static void assertLivingTarget(AbstractRecruitEntity recruit, BattleStressFixtures.ScenarioState scenarioState, String phase) {
        if (recruit.getTarget() != null && !recruit.getTarget().isAlive()) {
            throw new IllegalArgumentException("Expected surviving recruit to avoid retaining a dead target during " + phase
                    + " for stress scenario " + scenarioState.scenario().scenarioId());
        }
    }

    private static int countLosses(List<AbstractRecruitEntity> recruits) {
        return recruits.size() - aliveRecruits(recruits).size();
    }

    private static List<AbstractRecruitEntity> aliveRecruits(List<AbstractRecruitEntity> recruits) {
        return recruits.stream().filter(AbstractRecruitEntity::isAlive).toList();
    }

    private static List<AbstractRecruitEntity> winningSideAlive(BattleStressFixtures.ScenarioState scenarioState, List<AbstractRecruitEntity> westAlive, List<AbstractRecruitEntity> eastAlive) {
        return scenarioState.scenario().expectedWinner() == BattleStressFixtures.WinningSide.WEST ? westAlive : eastAlive;
    }

    private static List<AbstractRecruitEntity> losingSideAlive(BattleStressFixtures.ScenarioState scenarioState, List<AbstractRecruitEntity> westAlive, List<AbstractRecruitEntity> eastAlive) {
        return scenarioState.scenario().expectedWinner() == BattleStressFixtures.WinningSide.WEST ? eastAlive : westAlive;
    }

    private static String describeSurvivors(List<AbstractRecruitEntity> recruits) {
        if (recruits.isEmpty()) {
            return "0";
        }

        return recruits.size() + " " + recruits.stream()
                .map(recruit -> recruit.getName().getString() + "@" + recruit.blockPosition())
                .toList();
    }
}
