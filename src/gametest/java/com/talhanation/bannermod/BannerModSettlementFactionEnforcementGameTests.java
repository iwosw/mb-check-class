package com.talhanation.bannermod;

import com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding;
import com.talhanation.recruits.Main;
import com.talhanation.workers.config.WorkersServerConfig;
import com.talhanation.workers.entities.FarmerEntity;
import com.talhanation.workers.entities.workarea.CropArea;
import com.talhanation.workers.network.MessageAddWorkArea;
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
public class BannerModSettlementFactionEnforcementGameTests {

    private static final UUID FRIENDLY_OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000001001");
    private static final UUID HOSTILE_PLAYER_UUID = UUID.fromString("00000000-0000-0000-0000-000000001002");
    private static final UUID UNCLAIMED_PLAYER_UUID = UUID.fromString("00000000-0000-0000-0000-000000001003");
    private static final String FRIENDLY_TEAM_ID = "phase10_friendly";
    private static final String HOSTILE_TEAM_ID = "phase10_hostile";

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void friendlyClaimBindingAllowsPlacementAndSettlementOperation(GameTestHelper helper) {
        boolean originalClaimRestriction = WorkersServerConfig.ShouldWorkAreaOnlyBeInFactionClaim.get();
        WorkersServerConfig.ShouldWorkAreaOnlyBeInFactionClaim.set(true);

        try {
            ServerLevel level = helper.getLevel();
            ServerPlayer owner = createPlayer(helper, level, FRIENDLY_OWNER_UUID, "friendly-owner", FRIENDLY_TEAM_ID);
            BlockPos workAreaPos = helper.absolutePos(new BlockPos(2, 2, 2));

            BannerModDedicatedServerGameTestSupport.seedClaim(level, workAreaPos, FRIENDLY_TEAM_ID, owner.getUUID(), owner.getScoreboardName());

            boolean placed = new MessageAddWorkArea(workAreaPos, 0).executeForPlayer(owner);
            CropArea cropArea = findCropArea(level, workAreaPos);
            FarmerEntity worker = BannerModGameTestSupport.spawnOwnedFarmer(helper, owner, new BlockPos(4, 2, 2));
            BannerModDedicatedServerGameTestSupport.joinTeam(level, FRIENDLY_TEAM_ID, worker);
            worker.currentCropArea = cropArea;

            helper.assertTrue(placed,
                    "Expected a faction-aligned player to place a crop area inside a friendly claim through the live placement seam");
            helper.assertTrue(cropArea != null,
                    "Expected the friendly placement path to insert a crop area into the level");
            helper.assertTrue(cropArea.getSettlementBinding().status() == BannerModSettlementBinding.Status.FRIENDLY_CLAIM,
                    "Expected the placed crop area to report FRIENDLY_CLAIM through the shared settlement binding seam");
            helper.assertTrue(cropArea.canWorkHere(worker),
                    "Expected the owned worker to remain operational inside the friendly claim-backed settlement area");
        } finally {
            WorkersServerConfig.ShouldWorkAreaOnlyBeInFactionClaim.set(originalClaimRestriction);
        }

        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void hostileClaimDeniesPlacementAndReportsHostileBinding(GameTestHelper helper) {
        boolean originalClaimRestriction = WorkersServerConfig.ShouldWorkAreaOnlyBeInFactionClaim.get();
        WorkersServerConfig.ShouldWorkAreaOnlyBeInFactionClaim.set(true);

        try {
            ServerLevel level = helper.getLevel();
            ServerPlayer claimOwner = createPlayer(helper, level, FRIENDLY_OWNER_UUID, "claim-owner", FRIENDLY_TEAM_ID);
            ServerPlayer hostilePlayer = createPlayer(helper, level, HOSTILE_PLAYER_UUID, "hostile-player", HOSTILE_TEAM_ID);
            BlockPos workAreaPos = helper.absolutePos(new BlockPos(2, 2, 2));

            BannerModDedicatedServerGameTestSupport.seedClaim(level, workAreaPos, FRIENDLY_TEAM_ID, claimOwner.getUUID(), claimOwner.getScoreboardName());

            boolean placed = new MessageAddWorkArea(workAreaPos, 0).executeForPlayer(hostilePlayer);
            BannerModSettlementBinding.Binding binding = BannerModSettlementBinding.resolveFactionStatus(
                    com.talhanation.recruits.ClaimEvents.recruitsClaimManager,
                    workAreaPos,
                    HOSTILE_TEAM_ID
            );

            helper.assertFalse(placed,
                    "Expected hostile-faction work-area placement to be denied when claim restriction is enabled");
            helper.assertTrue(findCropArea(level, workAreaPos) == null,
                    "Expected hostile placement denial to avoid spawning a crop area in the claimed chunk");
            helper.assertTrue(binding.status() == BannerModSettlementBinding.Status.HOSTILE_CLAIM,
                    "Expected hostile placement denial to resolve as HOSTILE_CLAIM through the shared settlement binding seam");
        } finally {
            WorkersServerConfig.ShouldWorkAreaOnlyBeInFactionClaim.set(originalClaimRestriction);
        }

        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void unclaimedTerritoryDeniesRestrictedPlacementAndReportsUnclaimed(GameTestHelper helper) {
        boolean originalClaimRestriction = WorkersServerConfig.ShouldWorkAreaOnlyBeInFactionClaim.get();
        WorkersServerConfig.ShouldWorkAreaOnlyBeInFactionClaim.set(true);

        try {
            ServerLevel level = helper.getLevel();
            ServerPlayer player = createPlayer(helper, level, UNCLAIMED_PLAYER_UUID, "unclaimed-player", FRIENDLY_TEAM_ID);
            BlockPos workAreaPos = helper.absolutePos(new BlockPos(2, 2, 2));

            if (com.talhanation.recruits.ClaimEvents.recruitsClaimManager != null) {
                com.talhanation.recruits.world.RecruitsClaim existingClaim = com.talhanation.recruits.ClaimEvents.recruitsClaimManager.getClaim(new ChunkPos(workAreaPos));
                if (existingClaim != null) {
                    BannerModDedicatedServerGameTestSupport.removeClaim(level, existingClaim);
                }
            }

            boolean placed = new MessageAddWorkArea(workAreaPos, 0).executeForPlayer(player);
            BannerModSettlementBinding.Binding binding = BannerModSettlementBinding.resolveFactionStatus(
                    com.talhanation.recruits.ClaimEvents.recruitsClaimManager,
                    workAreaPos,
                    FRIENDLY_TEAM_ID
            );

            helper.assertFalse(placed,
                    "Expected unclaimed-territory placement to be denied when faction-claim restriction is enabled");
            helper.assertTrue(findCropArea(level, workAreaPos) == null,
                    "Expected unclaimed placement denial to avoid spawning a crop area without a backing claim");
            helper.assertTrue(binding.status() == BannerModSettlementBinding.Status.UNCLAIMED,
                    "Expected the shared settlement binding seam to report UNCLAIMED for restricted placement in claimless territory");
        } finally {
            WorkersServerConfig.ShouldWorkAreaOnlyBeInFactionClaim.set(originalClaimRestriction);
        }

        helper.succeed();
    }

    private static ServerPlayer createPlayer(GameTestHelper helper, ServerLevel level, UUID playerId, String name, String teamId) {
        Player player = BannerModDedicatedServerGameTestSupport.createFakeServerPlayer(level, playerId, name);
        BannerModDedicatedServerGameTestSupport.ensureFaction(level, teamId, playerId, name);
        BannerModDedicatedServerGameTestSupport.joinTeam(level, teamId, player);
        BlockPos spawnPos = helper.absolutePos(new BlockPos(1, 2, 1));
        player.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, 0.0F, 0.0F);
        return (ServerPlayer) player;
    }

    private static CropArea findCropArea(ServerLevel level, BlockPos workAreaPos) {
        List<CropArea> cropAreas = level.getEntitiesOfClass(CropArea.class, new AABB(workAreaPos.above()).inflate(1.0D));
        return cropAreas.isEmpty() ? null : cropAreas.get(0);
    }
}
