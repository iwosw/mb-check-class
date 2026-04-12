package com.talhanation.bannermod;

import com.talhanation.bannermod.settlement.BannerModSettlementBinding;
import com.talhanation.recruits.Main;
import com.talhanation.recruits.world.RecruitsClaim;
import com.talhanation.workers.VillagerEvents;
import com.talhanation.workers.config.WorkersServerConfig;
import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Main.MOD_ID)
public class BannerModClaimWorkerGrowthGameTests {

    private static final UUID FRIENDLY_LEADER_UUID = UUID.fromString("00000000-0000-0000-0000-000000003101");
    private static final UUID HOSTILE_LEADER_UUID = UUID.fromString("00000000-0000-0000-0000-000000003102");
    private static final String FRIENDLY_TEAM_ID = "phase31_claim_friendly";
    private static final String HOSTILE_TEAM_ID = "phase31_claim_hostile";

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void friendlyClaimGrowthSpawnsOwnedWorkerWithFactionDefaults(GameTestHelper helper) {
        ClaimGrowthConfigSnapshot snapshot = ClaimGrowthConfigSnapshot.capture();
        try {
            configureClaimGrowthForTests(20L, 4);

            ServerLevel level = helper.getLevel();
            ServerPlayer leader = createLeader(helper, level, FRIENDLY_LEADER_UUID, "phase31-friendly-leader", FRIENDLY_TEAM_ID);
            BlockPos claimPos = helper.absolutePos(new BlockPos(2, 2, 2));
            RecruitsClaim claim = BannerModDedicatedServerGameTestSupport.seedClaim(level, claimPos, FRIENDLY_TEAM_ID, leader.getUUID(), leader.getScoreboardName());

            AbstractWorkerEntity worker = VillagerEvents.attemptClaimWorkerGrowth(level, claim, FRIENDLY_TEAM_ID, 20L);
            List<AbstractWorkerEntity> claimWorkers = getClaimWorkers(level, claim);

            helper.assertTrue(worker != null, "Expected friendly claim growth to spawn one worker when the claim starts empty.");
            helper.assertTrue(claimWorkers.size() == 1, "Expected exactly one grown worker inside the friendly claim.");
            helper.assertTrue(leader.getUUID().equals(claimWorkers.get(0).getOwnerUUID()), "Expected the grown worker to inherit the claim leader owner UUID.");
            helper.assertTrue(claimWorkers.get(0).getTeam() != null && FRIENDLY_TEAM_ID.equals(claimWorkers.get(0).getTeam().getName()),
                    "Expected the grown worker to join the claim leader's faction team.");
        } finally {
            snapshot.restore();
        }

        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void claimGrowthWaitsForDiminishingCooldownBeforeSecondWorker(GameTestHelper helper) {
        ClaimGrowthConfigSnapshot snapshot = ClaimGrowthConfigSnapshot.capture();
        try {
            configureClaimGrowthForTests(20L, 4);

            ServerLevel level = helper.getLevel();
            ServerPlayer leader = createLeader(helper, level, FRIENDLY_LEADER_UUID, "phase31-cooldown-leader", FRIENDLY_TEAM_ID);
            BlockPos claimPos = helper.absolutePos(new BlockPos(2, 2, 2));
            RecruitsClaim claim = BannerModDedicatedServerGameTestSupport.seedClaim(level, claimPos, FRIENDLY_TEAM_ID, leader.getUUID(), leader.getScoreboardName());

            AbstractWorkerEntity firstWorker = VillagerEvents.attemptClaimWorkerGrowth(level, claim, FRIENDLY_TEAM_ID, 100L);
            AbstractWorkerEntity earlyWorker = VillagerEvents.attemptClaimWorkerGrowth(level, claim, FRIENDLY_TEAM_ID, 139L);
            AbstractWorkerEntity secondWorker = VillagerEvents.attemptClaimWorkerGrowth(level, claim, FRIENDLY_TEAM_ID, 140L);
            List<AbstractWorkerEntity> claimWorkers = getClaimWorkers(level, claim);

            helper.assertTrue(firstWorker != null, "Expected the first claim growth attempt to create one worker.");
            helper.assertTrue(earlyWorker == null, "Expected the second worker to stay blocked until the diminishing cooldown fully expires.");
            helper.assertTrue(secondWorker != null, "Expected a second worker once the required diminishing cooldown has elapsed.");
            helper.assertTrue(claimWorkers.size() == 2, "Expected exactly two workers after the cooldown-gated second growth attempt succeeds.");
        } finally {
            snapshot.restore();
        }

        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void hostileOrUnclaimedTerritoryNeverSpawnsClaimWorkers(GameTestHelper helper) {
        ClaimGrowthConfigSnapshot snapshot = ClaimGrowthConfigSnapshot.capture();
        try {
            configureClaimGrowthForTests(20L, 4);

            ServerLevel level = helper.getLevel();
            ServerPlayer leader = createLeader(helper, level, FRIENDLY_LEADER_UUID, "phase31-hostile-leader", FRIENDLY_TEAM_ID);
            createLeader(helper, level, HOSTILE_LEADER_UUID, "phase31-hostile-player", HOSTILE_TEAM_ID);
            BlockPos claimPos = helper.absolutePos(new BlockPos(2, 2, 2));
            RecruitsClaim claim = BannerModDedicatedServerGameTestSupport.seedClaim(level, claimPos, FRIENDLY_TEAM_ID, leader.getUUID(), leader.getScoreboardName());

            AbstractWorkerEntity hostileWorker = VillagerEvents.attemptClaimWorkerGrowth(
                    level,
                    claim,
                    new BannerModSettlementBinding.Binding(BannerModSettlementBinding.Status.HOSTILE_CLAIM, HOSTILE_TEAM_ID, FRIENDLY_TEAM_ID),
                    200L
            );
            AbstractWorkerEntity unclaimedWorker = VillagerEvents.attemptClaimWorkerGrowth(
                    level,
                    null,
                    new BannerModSettlementBinding.Binding(BannerModSettlementBinding.Status.UNCLAIMED, FRIENDLY_TEAM_ID, null),
                    200L
            );

            helper.assertTrue(hostileWorker == null, "Expected hostile claim territory to deny claim worker growth.");
            helper.assertTrue(unclaimedWorker == null, "Expected unclaimed territory to deny claim worker growth.");
            helper.assertTrue(getClaimWorkers(level, claim).isEmpty(), "Expected hostile or unclaimed claim growth attempts to leave worker count unchanged.");
        } finally {
            snapshot.restore();
        }

        helper.succeed();
    }

    private static ServerPlayer createLeader(GameTestHelper helper, ServerLevel level, UUID playerId, String name, String teamId) {
        Player player = BannerModDedicatedServerGameTestSupport.createFakeServerPlayer(level, playerId, name);
        BannerModDedicatedServerGameTestSupport.ensureFaction(level, teamId, playerId, name);
        BannerModDedicatedServerGameTestSupport.joinTeam(level, teamId, player);
        BlockPos spawnPos = helper.absolutePos(new BlockPos(1, 2, 1));
        player.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, 0.0F, 0.0F);
        return (ServerPlayer) player;
    }

    private static List<AbstractWorkerEntity> getClaimWorkers(ServerLevel level, RecruitsClaim claim) {
        ChunkPos anchorChunk = claim.getCenter() != null ? claim.getCenter() : new ChunkPos(0, 0);
        AABB claimBounds = new AABB(
                anchorChunk.getMinBlockX(),
                level.getMinBuildHeight(),
                anchorChunk.getMinBlockZ(),
                anchorChunk.getMaxBlockX() + 1.0D,
                level.getMaxBuildHeight(),
                anchorChunk.getMaxBlockZ() + 1.0D
        );
        return level.getEntitiesOfClass(AbstractWorkerEntity.class, claimBounds, worker -> worker.isAlive() && claim.containsChunk(worker.chunkPosition()));
    }

    private static void configureClaimGrowthForTests(long baseCooldownTicks, int workerCap) {
        WorkersServerConfig.EnableClaimWorkerGrowth.set(true);
        WorkersServerConfig.ClaimWorkerGrowthBaseCooldownTicks.set(baseCooldownTicks);
        WorkersServerConfig.ClaimWorkerMaxPerClaim.set(workerCap);
        WorkersServerConfig.ClaimWorkerProfessionPool.set(List.of("farmer"));
    }

    private record ClaimGrowthConfigSnapshot(boolean enabled,
                                             long baseCooldownTicks,
                                             int workerCap,
                                             List<? extends String> professionPool) {

        private static ClaimGrowthConfigSnapshot capture() {
            return new ClaimGrowthConfigSnapshot(
                    WorkersServerConfig.EnableClaimWorkerGrowth.get(),
                    WorkersServerConfig.ClaimWorkerGrowthBaseCooldownTicks.get(),
                    WorkersServerConfig.ClaimWorkerMaxPerClaim.get(),
                    List.copyOf(WorkersServerConfig.ClaimWorkerProfessionPool.get())
            );
        }

        private void restore() {
            WorkersServerConfig.EnableClaimWorkerGrowth.set(enabled);
            WorkersServerConfig.ClaimWorkerGrowthBaseCooldownTicks.set(baseCooldownTicks);
            WorkersServerConfig.ClaimWorkerMaxPerClaim.set(workerCap);
            WorkersServerConfig.ClaimWorkerProfessionPool.set(professionPool);
        }
    }
}
