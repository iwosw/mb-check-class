package com.talhanation.bannermod;

import com.talhanation.bannermod.ai.pathfinding.AsyncPathNavigation;
import com.talhanation.bannermod.ai.pathfinding.async.PathPriority;
import com.talhanation.bannermod.ai.pathfinding.async.TrueAsyncPathfindingRuntime;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.entity.military.RecruitEntity;
import com.talhanation.bannermod.util.RuntimeProfilingCounters;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Map;
import java.util.Set;

@GameTestHolder(BannerModMain.MOD_ID)
public class BannerModTrueAsyncPathfindingGameTests {
    private static final BlockPos RECRUIT_POS = new BlockPos(1, 2, 1);
    private static final BlockPos TARGET_POS = new BlockPos(12, 2, 12);

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void trueAsyncFlagDisabledKeepsRuntimePipelineInactive(GameTestHelper helper) {
        configureTrueAsync(false);
        RuntimeProfilingCounters.reset();

        RecruitEntity recruit = spawnOwnedRecruit(helper);
        BlockPos absoluteTarget = helper.absolutePos(TARGET_POS);

        helper.runAfterDelay(5, () -> recruit.getNavigation().moveTo(absoluteTarget.getX(), absoluteTarget.getY(), absoluteTarget.getZ(), 1.0D));
        helper.runAfterDelay(25, () -> {
            Map<String, Long> counters = RuntimeProfilingCounters.snapshot();
            long accepted = counters.getOrDefault("pathfinding.true_async.runtime.submit.accepted", 0L);
            helper.assertTrue(accepted == 0L,
                    "Expected true-async runtime pipeline to stay inactive when UseTrueAsyncPathfinding=false.");
            helper.succeed();
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void trueAsyncFlagEnabledSubmitsWorkerBackedRequest(GameTestHelper helper) {
        configureTrueAsync(true);

        RecruitEntity recruit = spawnOwnedRecruit(helper);
        ServerLevel level = helper.getLevel();
        BlockPos absoluteTarget = helper.absolutePos(TARGET_POS);
        AsyncPathNavigation navigation = (AsyncPathNavigation) recruit.getNavigation();
        boolean accepted = TrueAsyncPathfindingRuntime.instance().enqueue(
                navigation,
                level,
                Set.of(absoluteTarget),
                1,
                16.0F,
                navigation.incrementPathEpoch(),
                PathPriority.FOLLOW
        );
        helper.assertTrue(accepted, "Expected true-async runtime enqueue to accept a valid request in GameTest world.");

        helper.runAfterDelay(20, () -> TrueAsyncPathfindingRuntime.instance().tick(level));
        helper.runAfterDelay(40, helper::succeed);
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void trueAsyncCommitDiscardsResultWhenEntityIsGone(GameTestHelper helper) {
        configureTrueAsync(true);
        RuntimeProfilingCounters.reset();

        RecruitEntity recruit = spawnOwnedRecruit(helper);
        ServerLevel level = helper.getLevel();
        BlockPos absoluteTarget = helper.absolutePos(TARGET_POS);
        AsyncPathNavigation navigation = (AsyncPathNavigation) recruit.getNavigation();
        boolean accepted = TrueAsyncPathfindingRuntime.instance().enqueue(
                navigation,
                level,
                Set.of(absoluteTarget),
                1,
                16.0F,
                navigation.incrementPathEpoch(),
                PathPriority.FOLLOW
        );
        helper.assertTrue(accepted, "Expected true-async runtime enqueue to accept request before discard scenario.");

        helper.runAfterDelay(8, recruit::discard);
        helper.runAfterDelay(20, () -> TrueAsyncPathfindingRuntime.instance().tick(level));
        helper.runAfterDelay(40, () -> TrueAsyncPathfindingRuntime.instance().tick(level));
        helper.runAfterDelay(70, () -> {
            Map<String, Long> counters = RuntimeProfilingCounters.snapshot();
            long discardedGone = counters.getOrDefault("pathfinding.true_async.commit.discard.entity_gone", 0L);
            helper.assertTrue(discardedGone > 0L,
                    "Expected true-async committer to discard completed results when target entity is gone.");
            helper.succeed();
        });
    }

    private static RecruitEntity spawnOwnedRecruit(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer();
        return BannerModGameTestSupport.spawnOwnedRecruit(helper, owner, RECRUIT_POS);
    }

    private static void configureTrueAsync(boolean enabled) {
        RecruitsServerConfig.UseTrueAsyncPathfinding.set(enabled);
        RecruitsServerConfig.AsyncPathfindingWorkerThreads.set(1);
        RecruitsServerConfig.AsyncPathfindingMaxQueuedJobs.set(64);
        RecruitsServerConfig.AsyncPathfindingCommitBudgetNanos.set(2_000_000);
        RecruitsServerConfig.AsyncPathfindingSolveDeadlineMillis.set(150);
        RecruitsServerConfig.AsyncPathfindingPerMobThrottleTicks.set(0);
        RecruitsServerConfig.AsyncPathfindingTargetCoalesceDistance.set(0.0D);
    }
}
