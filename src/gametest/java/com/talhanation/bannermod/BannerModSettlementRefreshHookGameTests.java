package com.talhanation.bannermod;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.civilian.FarmerEntity;
import com.talhanation.bannermod.shared.settlement.BannerModSettlementRefreshSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

/**
 * Live coverage for SETTLEMENT-006: verifies that the
 * {@link com.talhanation.bannermod.events.civilian.SettlementMutationRefreshEvents}
 * subscribers actually drive {@link BannerModSettlementRefreshSupport#refreshSnapshot} when
 * the worker death and the worker-leave (forced removal) events fire on the live Forge bus.
 *
 * <p>Pure JUnit already locks the {@code SettlementContainerHookPolicy} predicate; this
 * GameTest closes the open "live event coverage" line by verifying the wiring through real
 * Minecraft event paths rather than the policy in isolation.</p>
 */
@GameTestHolder(BannerModMain.MOD_ID)
public class BannerModSettlementRefreshHookGameTests {

    private static final UUID LEADER_UUID = UUID.fromString("00000000-0000-0000-0000-000000006001");
    private static final String TEAM_ID = "settlement_006_team";

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty", batch = "settlement_refresh")
    public static void workerDeathTriggersSettlementSnapshotRefresh(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player leader = BannerModDedicatedServerGameTestSupport.createFakeServerPlayer(
                level, LEADER_UUID, "settlement-006-leader");
        BannerModDedicatedServerGameTestSupport.ensureFaction(
                level, TEAM_ID, LEADER_UUID, "settlement-006-leader");
        BannerModDedicatedServerGameTestSupport.joinTeam(level, TEAM_ID, leader);
        BlockPos claimPos = helper.absolutePos(new BlockPos(2, 2, 2));
        BannerModDedicatedServerGameTestSupport.seedClaim(
                level, claimPos, TEAM_ID, LEADER_UUID, "settlement-006-leader");

        FarmerEntity worker = BannerModGameTestSupport.spawnOwnedFarmer(
                helper, leader, new BlockPos(2, 2, 2));
        BannerModDedicatedServerGameTestSupport.assignDetachedOwnership(worker, LEADER_UUID);
        BannerModDedicatedServerGameTestSupport.joinTeam(level, TEAM_ID, worker);

        long baseline = BannerModSettlementRefreshSupport.invocationCount();
        // kill() triggers LivingDeathEvent through the live Forge bus, which is what the
        // SettlementMutationRefreshEvents subscriber listens for. The handler bumps the
        // refresh-support invocation counter as a side effect.
        worker.kill();

        helper.assertTrue(BannerModSettlementRefreshSupport.invocationCount() > baseline,
                "Expected LivingDeathEvent on a worker to trigger SettlementMutationRefreshEvents → "
                        + "BannerModSettlementRefreshSupport.refreshSnapshot at least once.");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty", batch = "settlement_refresh_leave")
    public static void workerForcedRemovalTriggersSettlementSnapshotRefresh(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player leader = BannerModDedicatedServerGameTestSupport.createFakeServerPlayer(
                level, LEADER_UUID, "settlement-006-leader");
        BannerModDedicatedServerGameTestSupport.ensureFaction(
                level, TEAM_ID, LEADER_UUID, "settlement-006-leader");
        BannerModDedicatedServerGameTestSupport.joinTeam(level, TEAM_ID, leader);
        BlockPos claimPos = helper.absolutePos(new BlockPos(2, 2, 2));
        BannerModDedicatedServerGameTestSupport.seedClaim(
                level, claimPos, TEAM_ID, LEADER_UUID, "settlement-006-leader");

        FarmerEntity worker = BannerModGameTestSupport.spawnOwnedFarmer(
                helper, leader, new BlockPos(2, 2, 2));
        BannerModDedicatedServerGameTestSupport.assignDetachedOwnership(worker, LEADER_UUID);
        BannerModDedicatedServerGameTestSupport.joinTeam(level, TEAM_ID, worker);

        long baseline = BannerModSettlementRefreshSupport.invocationCount();
        // discard() sets RemovalReason.DISCARDED which the handler accepts via shouldDestroy().
        // EntityLeaveLevelEvent fires from the level's entity removal pipeline on the next tick.
        worker.remove(Entity.RemovalReason.DISCARDED);

        helper.runAfterDelay(2, () -> {
            helper.assertTrue(BannerModSettlementRefreshSupport.invocationCount() > baseline,
                    "Expected EntityLeaveLevelEvent (DISCARDED) on a worker to trigger refreshSnapshot.");
            helper.succeed();
        });
    }
}
