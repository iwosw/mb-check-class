package com.talhanation.bannermod;

import com.talhanation.bannermod.governance.BannerModGovernorHeartbeat;
import com.talhanation.bannermod.governance.BannerModGovernorManager;
import com.talhanation.bannermod.governance.BannerModGovernorService;
import com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding;
import com.talhanation.bannermod.events.ClaimEvents;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.RecruitEntity;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.entity.civilian.FarmerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(BannerModMain.MOD_ID)
public class BannerModGovernorControlGameTests {
    private static final UUID FRIENDLY_LEADER_UUID = UUID.fromString("00000000-0000-0000-0000-000000002301");
    private static final UUID HOSTILE_LEADER_UUID = UUID.fromString("00000000-0000-0000-0000-000000002302");
    private static final String FRIENDLY_TEAM_ID = "phase23_friendly";
    private static final String HOSTILE_TEAM_ID = "phase23_hostile";

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void friendlyClaimGovernorDesignationPersistsThroughLiveRuntimeSeam(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player leader = BannerModDedicatedServerGameTestSupport.createFakeServerPlayer(level, FRIENDLY_LEADER_UUID, "phase23-governor-leader");
        BannerModDedicatedServerGameTestSupport.ensureFaction(level, FRIENDLY_TEAM_ID, FRIENDLY_LEADER_UUID, "phase23-governor-leader");
        BannerModDedicatedServerGameTestSupport.joinTeam(level, FRIENDLY_TEAM_ID, leader);

        BlockPos claimPos = helper.absolutePos(new BlockPos(2, 2, 2));
        RecruitsClaim claim = BannerModDedicatedServerGameTestSupport.seedClaim(level, claimPos, FRIENDLY_TEAM_ID, FRIENDLY_LEADER_UUID, "phase23-governor-leader");
        RecruitEntity recruit = BannerModGameTestSupport.spawnOwnedRecruit(helper, leader, new BlockPos(2, 2, 2));
        BannerModDedicatedServerGameTestSupport.assignRecruitToLeader(level, recruit, leader, FRIENDLY_TEAM_ID);

        BannerModGovernorService service = new BannerModGovernorService(BannerModGovernorManager.get(level));
        BannerModGovernorService.OperationResult result = service.assignGovernor(claim, (net.minecraft.server.level.ServerPlayer) leader, recruit);

        helper.assertTrue(result.allowed(), "Expected the friendly claim leader to designate a governor through the live service seam");
        helper.assertTrue(service.getGovernorRecruitUuid(claim) != null && service.getGovernorRecruitUuid(claim).equals(recruit.getUUID()),
                "Expected the persisted governor snapshot to retain the recruit UUID");

        CompoundTag saved = BannerModGovernorManager.get(level).save(new CompoundTag());
        BannerModGovernorManager reloaded = BannerModGovernorManager.load(saved);
        helper.assertTrue(reloaded.getSnapshot(claim.getUUID()) != null && recruit.getUUID().equals(reloaded.getSnapshot(claim.getUUID()).governorRecruitUuid()),
                "Expected governor assignment to survive manager round-trip persistence");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void hostileClaimSwapDegradesGovernorControlAndStopsNormalCollection(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player leader = BannerModDedicatedServerGameTestSupport.createFakeServerPlayer(level, FRIENDLY_LEADER_UUID, "phase23-friendly-leader");
        Player hostileLeader = BannerModDedicatedServerGameTestSupport.createFakeServerPlayer(level, HOSTILE_LEADER_UUID, "phase23-hostile-leader");
        BannerModDedicatedServerGameTestSupport.ensureFaction(level, FRIENDLY_TEAM_ID, FRIENDLY_LEADER_UUID, "phase23-friendly-leader");
        BannerModDedicatedServerGameTestSupport.ensureFaction(level, HOSTILE_TEAM_ID, HOSTILE_LEADER_UUID, "phase23-hostile-leader");
        BannerModDedicatedServerGameTestSupport.joinTeam(level, FRIENDLY_TEAM_ID, leader);
        BannerModDedicatedServerGameTestSupport.joinTeam(level, HOSTILE_TEAM_ID, hostileLeader);

        BlockPos claimPos = helper.absolutePos(new BlockPos(2, 2, 2));
        RecruitsClaim claim = BannerModDedicatedServerGameTestSupport.seedClaim(level, claimPos, FRIENDLY_TEAM_ID, FRIENDLY_LEADER_UUID, "phase23-friendly-leader");
        RecruitEntity recruit = BannerModGameTestSupport.spawnOwnedRecruit(helper, leader, new BlockPos(2, 2, 2));
        BannerModDedicatedServerGameTestSupport.assignRecruitToLeader(level, recruit, leader, FRIENDLY_TEAM_ID);
        FarmerEntity farmer = BannerModGameTestSupport.spawnOwnedFarmer(helper, leader, new BlockPos(3, 2, 2));
        BannerModDedicatedServerGameTestSupport.assignDetachedOwnership(farmer, leader.getUUID());
        BannerModDedicatedServerGameTestSupport.joinTeam(level, FRIENDLY_TEAM_ID, farmer);

        BannerModGovernorService service = new BannerModGovernorService(BannerModGovernorManager.get(level));
        service.assignGovernor(claim, (net.minecraft.server.level.ServerPlayer) leader, recruit);
        BannerModDedicatedServerGameTestSupport.swapClaimFaction(level, claim, HOSTILE_TEAM_ID, HOSTILE_LEADER_UUID, "phase23-hostile-leader");
        BannerModGovernorHeartbeat.runGovernedClaimHeartbeat(level, ClaimEvents.recruitsClaimManager, BannerModGovernorManager.get(level));

        var snapshot = BannerModGovernorManager.get(level).getSnapshot(claim.getUUID());
        helper.assertTrue(snapshot != null && snapshot.taxesCollected() == 0,
                "Expected degraded governor control to stop normal tax collection after a hostile claim swap");
        helper.assertTrue(snapshot != null && snapshot.incidentTokens().contains("degraded_settlement"),
                "Expected degraded governor control to publish an instability incident after claim loss");
        helper.assertTrue(BannerModSettlementBinding.resolveSettlementStatus(ClaimEvents.recruitsClaimManager, claimPos, FRIENDLY_TEAM_ID).status() == BannerModSettlementBinding.Status.DEGRADED_MISMATCH,
                "Expected the live settlement binding seam to report DEGRADED_MISMATCH after the hostile swap");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void governedSettlementPublishesLocalTaxAndAdviceThroughRealHeartbeat(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player leader = BannerModDedicatedServerGameTestSupport.createFakeServerPlayer(level, FRIENDLY_LEADER_UUID, "phase23-report-leader");
        BannerModDedicatedServerGameTestSupport.ensureFaction(level, FRIENDLY_TEAM_ID, FRIENDLY_LEADER_UUID, "phase23-report-leader");
        BannerModDedicatedServerGameTestSupport.joinTeam(level, FRIENDLY_TEAM_ID, leader);

        BlockPos claimPos = helper.absolutePos(new BlockPos(2, 2, 2));
        RecruitsClaim claim = BannerModDedicatedServerGameTestSupport.seedClaim(level, claimPos, FRIENDLY_TEAM_ID, FRIENDLY_LEADER_UUID, "phase23-report-leader");
        RecruitEntity recruit = BannerModGameTestSupport.spawnOwnedRecruit(helper, leader, new BlockPos(2, 2, 2));
        BannerModDedicatedServerGameTestSupport.assignRecruitToLeader(level, recruit, leader, FRIENDLY_TEAM_ID);
        BannerModGameTestSupport.spawnVillagerWithMemories(helper, new BlockPos(3, 2, 2), "phase23-villager");
        FarmerEntity farmer = BannerModGameTestSupport.spawnOwnedFarmer(helper, leader, new BlockPos(4, 2, 2));
        BannerModDedicatedServerGameTestSupport.assignDetachedOwnership(farmer, leader.getUUID());
        BannerModDedicatedServerGameTestSupport.joinTeam(level, FRIENDLY_TEAM_ID, farmer);

        BannerModGovernorService service = new BannerModGovernorService(BannerModGovernorManager.get(level));
        service.assignGovernor(claim, (net.minecraft.server.level.ServerPlayer) leader, recruit);
        BannerModGovernorHeartbeat.runGovernedClaimHeartbeat(level, ClaimEvents.recruitsClaimManager, BannerModGovernorManager.get(level));

        var snapshot = BannerModGovernorManager.get(level).getSnapshot(claim.getUUID());
        helper.assertTrue(snapshot != null && snapshot.taxesDue() > 0,
                "Expected the real governor heartbeat to publish a positive local tax summary for a friendly governed claim");
        helper.assertTrue(snapshot != null && !snapshot.recommendationTokens().isEmpty(),
                "Expected the real governor heartbeat to publish at least one recommendation token for the governed claim");
        helper.assertTrue(snapshot != null && snapshot.lastHeartbeatTick() == level.getGameTime(),
                "Expected the snapshot heartbeat tick to match the live server time after recomputation");
        helper.succeed();
    }
}
