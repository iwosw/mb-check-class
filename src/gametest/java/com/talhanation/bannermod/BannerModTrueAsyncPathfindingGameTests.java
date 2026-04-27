package com.talhanation.bannermod;

import com.talhanation.bannermod.ai.pathfinding.AsyncPathNavigation;
import com.talhanation.bannermod.ai.pathfinding.async.PathPriority;
import com.talhanation.bannermod.ai.pathfinding.async.PathResult;
import com.talhanation.bannermod.ai.pathfinding.async.PathResultStatus;
import com.talhanation.bannermod.ai.pathfinding.async.TrueAsyncPathfindingRuntime;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.entity.military.RecruitEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Set;

@GameTestHolder(BannerModMain.MOD_ID)
public class BannerModTrueAsyncPathfindingGameTests {
    private static final BlockPos RECRUIT_POS = new BlockPos(1, 2, 1);
    private static final BlockPos TARGET_POS = new BlockPos(12, 2, 12);

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty", batch = "trueasync_disabled")
    public static void trueAsyncFlagDisabledKeepsRuntimePipelineInactive(GameTestHelper helper) {
        // Per-recruit submit counter is isolated from the global RuntimeProfilingCounters map,
        // so concurrent unrelated tests in other batches cannot bump it. Combined with this
        // test's dedicated batch, the assertion is now race-free without needing reset().
        configureTrueAsync(false);

        RecruitEntity recruit = spawnOwnedRecruit(helper);
        BlockPos absoluteTarget = helper.absolutePos(TARGET_POS);
        AsyncPathNavigation navigation = (AsyncPathNavigation) recruit.getNavigation();

        helper.runAfterDelay(5, () -> navigation.moveTo(absoluteTarget.getX(), absoluteTarget.getY(), absoluteTarget.getZ(), 1.0D));
        helper.runAfterDelay(25, () -> {
            helper.assertTrue(navigation.submitAcceptedCount() == 0L,
                    "Expected true-async runtime pipeline to stay inactive when UseTrueAsyncPathfinding=false (per-recruit submit count must be 0).");
            helper.succeed();
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty", batch = "trueasync_enabled")
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
        helper.assertTrue(navigation.submitAcceptedCount() >= 1L,
                "Expected per-recruit submit counter to record the accepted enqueue.");

        helper.runAfterDelay(20, () -> TrueAsyncPathfindingRuntime.instance().tick(level));
        helper.runAfterDelay(40, helper::succeed);
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty", batch = "trueasync_discard")
    public static void trueAsyncCommitDiscardsResultWhenEntityIsGone(GameTestHelper helper) {
        configureTrueAsync(true);

        RecruitEntity recruit = spawnOwnedRecruit(helper);
        ServerLevel level = helper.getLevel();
        BlockPos absoluteTarget = helper.absolutePos(TARGET_POS);
        AsyncPathNavigation navigation = (AsyncPathNavigation) recruit.getNavigation();
        long epoch = navigation.incrementPathEpoch();
        boolean accepted = TrueAsyncPathfindingRuntime.instance().enqueue(
                navigation,
                level,
                Set.of(absoluteTarget),
                1,
                16.0F,
                epoch,
                PathPriority.FOLLOW
        );
        helper.assertTrue(accepted, "Expected true-async runtime enqueue to accept request before discard scenario.");

        helper.runAfterDelay(8, recruit::discard);
        // Synthesize a result for this recruit and feed it into the committer directly via the
        // test seam. This bypasses AsyncPathScheduler so the test is deterministic regardless of
        // whether the async solver finishes inside the empty harness world's tick budget — what
        // we want to prove is that the COMMITTER recognises the entity-gone state and charges
        // the per-recruit discard counter, not that the solver runs.
        helper.runAfterDelay(20, () -> {
            PathResult synthetic = new PathResult(
                    recruit.getUUID(),
                    /* requestId */ 0L,
                    epoch,
                    PathResultStatus.SUCCESS,
                    /* nodes */ List.of(),
                    /* reached */ false,
                    /* cost */ 0.0D,
                    /* visitedNodes */ 0,
                    /* solveNanos */ 0L,
                    "synthetic-test"
            );
            TrueAsyncPathfindingRuntime.instance().commitForTesting(List.of(synthetic));
        });
        helper.runAfterDelay(40, () -> {
            // Per-recruit discard counter is isolated, so this assertion never races against
            // unrelated parallel tests that also exercise true-async commit paths.
            helper.assertTrue(navigation.commitDiscardEntityGoneCount() >= 1L,
                    "Expected true-async committer to discard at least one result for this recruit when its entity is gone.");
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
