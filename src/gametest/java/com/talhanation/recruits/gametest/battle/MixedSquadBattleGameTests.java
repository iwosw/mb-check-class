package com.talhanation.recruits.gametest.battle;

import com.talhanation.recruits.Main;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.entities.ai.FormationTargetSelectionController;
import com.talhanation.recruits.config.RecruitsServerConfig;
import com.talhanation.recruits.gametest.support.RecruitsBattleGameTestSupport;
import com.talhanation.recruits.pathfinding.GlobalPathfindingController;
import com.talhanation.recruits.pathfinding.AsyncPathProcessor;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Main.MOD_ID)
public class MixedSquadBattleGameTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "battle_harness_field", timeoutTicks = 340)
    public static void representativeMixedSquadsResolveBoundedBattle(GameTestHelper helper) {
        AbstractRecruitEntity.resetTargetSearchProfiling();
        AsyncPathProcessor.resetProfiling();
        FormationTargetSelectionController.resetProfiling();
        GlobalPathfindingController.resetProfiling();
        RecruitsBattleGameTestSupport.BattleSquad westSquad = RecruitsBattleGameTestSupport.spawnWestMixedSquad(helper, UUID.fromString("00000000-0000-0000-0000-000000000331"));
        RecruitsBattleGameTestSupport.BattleSquad eastSquad = RecruitsBattleGameTestSupport.spawnEastMixedSquad(helper, UUID.fromString("00000000-0000-0000-0000-000000000332"));

        for (AbstractRecruitEntity recruit : eastSquad.recruits()) {
            recruit.setHealth(Math.min(recruit.getHealth(), 1.0F));
        }

        RecruitsBattleGameTestSupport.setMutualTargets(westSquad.recruits(), eastSquad.recruits());
        RecruitsBattleGameTestSupport.setMutualTargets(eastSquad.recruits(), westSquad.recruits());

        helper.succeedWhen(() -> {
            boolean eastDefeated = eastSquad.recruits().stream().noneMatch(AbstractRecruitEntity::isAlive);
            boolean westSurvivor = westSquad.recruits().stream().anyMatch(AbstractRecruitEntity::isAlive);
            boolean westStillOwned = westSquad.recruits().stream().filter(AbstractRecruitEntity::isAlive).allMatch(AbstractRecruitEntity::isOwned);
            RecruitsBattleGameTestSupport.BattleProfilingSnapshot snapshot = RecruitsBattleGameTestSupport.captureProfilingSnapshot(
                    helper,
                    "mixed_squad_battle",
                    0
            );

            helper.assertTrue(eastDefeated, "Expected deterministic mixed-squad battle to defeat the weakened east squad");
            helper.assertTrue(westSurvivor && westStillOwned,
                    "Expected the winning west squad to keep at least one owned survivor");
            helper.assertTrue(snapshot.controller().totalRequests() > 0,
                    "Expected mixed-squad battle to exercise the global pathfinding controller");
            helper.assertTrue(snapshot.controller().reuseAttempts() > 0,
                    "Expected mixed-squad battle to record path-reuse observability");
            helper.assertTrue(snapshot.controller().reuseHits() <= snapshot.controller().reuseAttempts(),
                    "Expected mixed-squad battle reuse hits to stay bounded by attempts");
            helper.assertTrue(snapshot.controller().flowFieldPrototypeAttempts()
                            == snapshot.controller().flowFieldPrototypeHits() + snapshot.controller().flowFieldPrototypeFallbacks(),
                    "Expected mixed-squad battle optional flow-field attempt accounting to stay balanced");
            helper.assertTrue(snapshot.controller().flowFieldPrototypeAttempts() <= snapshot.controller().flowFieldEligibleRequests(),
                    "Expected mixed-squad battle optional flow-field attempts to stay bounded by eligible requests");
            if (RecruitsServerConfig.EnableOptionalFlowFieldPrototype.get() && snapshot.controller().flowFieldEligibleRequests() > 0) {
                helper.assertTrue(snapshot.controller().flowFieldPrototypeAttempts() > 0,
                        "Expected prototype-enabled mixed-squad battle to attempt the optional flow-field path");
            }
            helper.assertTrue(snapshot.controller().budgetUsedThisTick() >= 0,
                    "Expected mixed-squad battle budget usage to stay non-negative");
            helper.assertTrue(snapshot.controller().requestBudgetPerTick() > 0,
                    "Expected mixed-squad battle to expose the controller budget capacity");
            helper.assertTrue(snapshot.controller().deferredResumes() <= snapshot.controller().deferredRequests(),
                    "Expected mixed-squad battle deferred resumes to stay bounded by deferred requests");
            helper.assertTrue(snapshot.controller().deferredDropsBacklogCap()
                            + snapshot.controller().deferredDropsMaxAge()
                            + snapshot.controller().deferredDropsInvalidated()
                            == snapshot.controller().deferredDrops(),
                    "Expected mixed-squad battle deferred drop accounting to stay balanced");
            helper.assertTrue(snapshot.controller().currentDeferredQueueDepth() <= snapshot.controller().maxDeferredQueueDepth(),
                    "Expected mixed-squad battle deferred queue depth to stay within the recorded maximum");
            helper.assertTrue(snapshot.pathfinding().droppedCallbacks() >= 0,
                    "Expected mixed-squad battle dropped async callbacks to stay non-negative");
            helper.assertTrue(snapshot.targetSearch().searchOpportunities() > 0,
                    "Expected mixed-squad battle to record target-search cadence opportunities");
            helper.assertTrue(snapshot.targetSearch().lodFullTierTicks()
                            + snapshot.targetSearch().lodReducedTierTicks()
                            + snapshot.targetSearch().lodShedTierTicks() > 0,
                    "Expected mixed-squad battle to expose bounded AI LOD tier accounting");
            helper.assertTrue(snapshot.targetSearch().lodSkippedSearches() >= 0,
                    "Expected mixed-squad battle skipped target-search accounting to stay non-negative");
            helper.assertTrue(snapshot.targetSearch().lodSkippedSearches() <= snapshot.targetSearch().searchOpportunities(),
                    "Expected mixed-squad battle skipped target-search accounting to stay bounded by cadence opportunities");
            Main.LOGGER.info("Mixed squad profiling snapshot: {}",
                    RecruitsBattleGameTestSupport.formatProfilingSnapshot(snapshot));
        });
    }
}
