package com.talhanation.bannermod;

import com.talhanation.bannermod.ai.pathfinding.AsyncPathNavigation;
import com.talhanation.bannermod.ai.pathfinding.async.AsyncPathScheduler;
import com.talhanation.bannermod.ai.pathfinding.async.CancellationToken;
import com.talhanation.bannermod.ai.pathfinding.async.GridAStarPathSolver;
import com.talhanation.bannermod.ai.pathfinding.async.PathPriority;
import com.talhanation.bannermod.ai.pathfinding.async.PathRequestSnapshot;
import com.talhanation.bannermod.ai.pathfinding.async.PathResult;
import com.talhanation.bannermod.ai.pathfinding.async.PathResultStatus;
import com.talhanation.bannermod.ai.pathfinding.async.RegionSnapshot;
import com.talhanation.bannermod.ai.pathfinding.async.TrueAsyncPathfindingRuntime;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.entity.military.RecruitEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

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
        AtomicBoolean accepted = new AtomicBoolean(false);

        for (int delay = 5; delay <= 45; delay += 10) {
            helper.runAfterDelay(delay, () -> {
                if (accepted.get()) {
                    return;
                }
                accepted.set(TrueAsyncPathfindingRuntime.instance().enqueue(
                    navigation,
                    level,
                    Set.of(absoluteTarget),
                    1,
                    16.0F,
                    navigation.incrementPathEpoch(),
                    PathPriority.FOLLOW
                ));
            });
        }
        helper.runAfterDelay(55, () -> {
            helper.assertTrue(accepted.get(), "Expected true-async runtime enqueue to accept a valid request in GameTest world.");
            helper.assertTrue(navigation.submitAcceptedCount() >= 1L,
                    "Expected per-recruit submit counter to record the accepted enqueue.");
        });
        helper.runAfterDelay(65, () -> TrueAsyncPathfindingRuntime.instance().tick(level));
        helper.runAfterDelay(80, helper::succeed);
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty", batch = "trueasync_discard")
    public static void trueAsyncCommitDiscardsResultWhenEntityIsGone(GameTestHelper helper) {
        configureTrueAsync(true);

        RecruitEntity recruit = spawnOwnedRecruit(helper);
        ServerLevel level = helper.getLevel();
        BlockPos absoluteTarget = helper.absolutePos(TARGET_POS);
        AsyncPathNavigation navigation = (AsyncPathNavigation) recruit.getNavigation();
        final long[] epoch = new long[1];

        helper.runAfterDelay(5, () -> {
            epoch[0] = navigation.incrementPathEpoch();
            boolean accepted = TrueAsyncPathfindingRuntime.instance().enqueue(
                    navigation,
                    level,
                    Set.of(absoluteTarget),
                    1,
                    16.0F,
                    epoch[0],
                    PathPriority.FOLLOW
            );
            helper.assertTrue(accepted, "Expected true-async runtime enqueue to accept request before discard scenario.");
        });

        helper.runAfterDelay(12, recruit::discard);
        // Synthesize a result for this recruit and feed it into the committer directly via the
        // test seam. This bypasses AsyncPathScheduler so the test is deterministic regardless of
        // whether the async solver finishes inside the empty harness world's tick budget — what
        // we want to prove is that the COMMITTER recognises the entity-gone state and charges
        // the per-recruit discard counter, not that the solver runs.
        helper.runAfterDelay(20, () -> {
            // Re-register the pending target before the synthetic commit: production auto-tick
            // (RecruitWorldLifecycleService → TrueAsyncPathfindingRuntime.tick) may have already
            // polled and committed the real solver result, which removes the entry placed by
            // enqueue. Without re-registering, resolveCommitTarget would return null without
            // bumping the per-recruit entity-gone counter and the assertion below would race.
            TrueAsyncPathfindingRuntime.instance().registerPendingTargetForTesting(
                    navigation, /* requestId */ 0L, /* reachRange */ 1, absoluteTarget);
            PathResult synthetic = new PathResult(
                    recruit.getUUID(),
                    /* requestId */ 0L,
                    epoch[0],
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

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty", batch = "trueasync_core")
    public static void trueAsyncSchedulerReturnsSuccessFailureAndCancelledResults(GameTestHelper helper) {
        AsyncPathScheduler scheduler = new AsyncPathScheduler(new GridAStarPathSolver(), 1, 8, defaultCaps());
        BlockPos origin = helper.absolutePos(new BlockPos(0, 2, 0));
        PathRequestSnapshot successRequest = syntheticRequest(origin, origin.offset(2, 0, 2), 101L);
        PathRequestSnapshot failureRequest = syntheticRequest(origin, origin.offset(4, 0, 0), 102L);
        PathRequestSnapshot cancelledRequest = syntheticRequest(origin, origin.offset(2, 0, 2), 103L);

        helper.assertTrue(scheduler.submit(successRequest, openSyntheticRegion(origin, 3, 1, 3), CancellationToken.NONE),
                "Expected scheduler to accept synthetic success request.");
        helper.assertTrue(scheduler.submit(failureRequest, blockedCorridorRegion(origin), CancellationToken.NONE),
                "Expected scheduler to accept synthetic no-path request.");
        helper.assertTrue(scheduler.submit(cancelledRequest, openSyntheticRegion(origin, 3, 1, 3), () -> true),
                "Expected scheduler to accept synthetic cancelled request.");

        helper.runAfterDelay(30, () -> {
            try {
                List<PathResult> results = scheduler.pollCompleted(8);
                helper.assertTrue(results.size() == 3, "Expected three completed async path results, got " + results.size() + ".");
                helper.assertTrue(hasStatus(results, 101L, PathResultStatus.SUCCESS),
                        "Expected success request to return SUCCESS.");
                helper.assertTrue(hasStatus(results, 102L, PathResultStatus.NO_PATH),
                        "Expected blocked request to return NO_PATH.");
                helper.assertTrue(hasStatus(results, 103L, PathResultStatus.CANCELLED),
                        "Expected cancelled request to return CANCELLED.");
                helper.succeed();
            } finally {
                scheduler.close();
            }
        });
    }

    private static RecruitEntity spawnOwnedRecruit(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        return BannerModGameTestSupport.spawnOwnedRecruit(helper, owner, RECRUIT_POS);
    }

    private static void configureTrueAsync(boolean enabled) {
        RecruitsServerConfig.UseTrueAsyncPathfinding.set(enabled);
        RecruitsServerConfig.AsyncPathfindingWorkerThreads.set(1);
        RecruitsServerConfig.AsyncPathfindingMaxQueuedJobs.set(64);
        RecruitsServerConfig.AsyncPathfindingSnapshotBudgetNanos.set(50_000_000);
        RecruitsServerConfig.AsyncPathfindingCommitBudgetNanos.set(2_000_000);
        RecruitsServerConfig.AsyncPathfindingSolveDeadlineMillis.set(150);
        RecruitsServerConfig.AsyncPathfindingPerMobThrottleTicks.set(0);
        RecruitsServerConfig.AsyncPathfindingTargetCoalesceDistance.set(0.0D);
    }

    private static boolean hasStatus(List<PathResult> results, long requestId, PathResultStatus status) {
        return results.stream().anyMatch(result -> result.requestId() == requestId && result.status() == status);
    }

    private static Map<PathPriority, Integer> defaultCaps() {
        Map<PathPriority, Integer> caps = new EnumMap<>(PathPriority.class);
        for (PathPriority priority : PathPriority.values()) {
            caps.put(priority, 8);
        }
        return caps;
    }

    private static RegionSnapshot openSyntheticRegion(BlockPos origin, int sizeX, int sizeY, int sizeZ) {
        return new RegionSnapshot(origin, sizeX, sizeY, sizeZ, new byte[sizeX * sizeY * sizeZ], List.of(), false);
    }

    private static RegionSnapshot blockedCorridorRegion(BlockPos origin) {
        byte[] flags = new byte[5];
        flags[1] = RegionSnapshot.FLAG_SOLID;
        flags[2] = RegionSnapshot.FLAG_SOLID;
        flags[3] = RegionSnapshot.FLAG_SOLID;
        return new RegionSnapshot(origin, 5, 1, 1, flags, List.of(), false);
    }

    private static PathRequestSnapshot syntheticRequest(BlockPos start, BlockPos target, long requestId) {
        return new PathRequestSnapshot(
                UUID.randomUUID(),
                requestId,
                requestId,
                start,
                List.of(target),
                128,
                64.0F,
                0.6D,
                1.8D,
                0.6D,
                false,
                false,
                false,
                PathPriority.WORK,
                10L,
                System.nanoTime() + 1_000_000_000L
        );
    }
}
