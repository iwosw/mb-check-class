package com.talhanation.bannermod;

import com.talhanation.recruits.Main;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.gametest.support.RecruitsBattleGameTestSupport;
import com.talhanation.recruits.gametest.support.RecruitsCommandGameTestSupport;
import com.talhanation.recruits.network.MessageMovement;
import com.talhanation.workers.entities.FarmerEntity;
import com.talhanation.workers.entities.workarea.BuildArea;
import com.talhanation.workers.entities.workarea.CropArea;
import com.talhanation.workers.network.MessageUpdateBuildArea;
import com.talhanation.workers.network.WorkAreaAuthoringRules;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Main.MOD_ID)
public class BannerModMultiplayerAuthorityConflictGameTests {

    private static final UUID OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000801");
    private static final UUID OUTSIDER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000802");

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void ownerVsOutsiderRecruitCommandsKeepLiveOwnerAuthority(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player owner = createPlayer(helper, level, OWNER_UUID, "authority-owner");
        Player outsider = createPlayer(helper, level, OUTSIDER_UUID, "authority-outsider");
        RecruitsBattleGameTestSupport.BattleSquad squad = RecruitsBattleGameTestSupport.spawnRecoveryPair(
                helper,
                RecruitsBattleGameTestSupport.SquadAnchor.WEST,
                OWNER_UUID,
                "Conflict"
        );

        for (AbstractRecruitEntity recruit : squad.recruits()) {
            RecruitsCommandGameTestSupport.prepareForCommand(recruit, RecruitsCommandGameTestSupport.TARGET_GROUP_UUID);
        }

        MessageMovement.dispatchToServer(
                outsider,
                outsider.getUUID(),
                RecruitsCommandGameTestSupport.TARGET_GROUP_UUID,
                1,
                0,
                false
        );

        for (AbstractRecruitEntity recruit : squad.recruits()) {
            RecruitsCommandGameTestSupport.assertUnchanged(recruit, 0, false);
        }

        MessageMovement.dispatchToServer(
                owner,
                owner.getUUID(),
                RecruitsCommandGameTestSupport.TARGET_GROUP_UUID,
                1,
                0,
                false
        );

        for (AbstractRecruitEntity recruit : squad.recruits()) {
            helper.assertTrue(recruit.getFollowState() == 1,
                    "Expected the live owner to retain recruit command authority while the outsider stays denied");
        }

        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void ownerVsOutsiderSettlementControlKeepsOutsidersDenied(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player owner = createPlayer(helper, level, OWNER_UUID, "settlement-owner");
        ServerPlayer outsider = (ServerPlayer) createPlayer(helper, level, OUTSIDER_UUID, "settlement-outsider");
        FarmerEntity worker = BannerModGameTestSupport.spawnOwnedFarmer(
                helper,
                owner,
                RecruitsBattleGameTestSupport.WEST_FLANK_POS
        );
        CropArea cropArea = BannerModGameTestSupport.spawnOwnedCropArea(
                helper,
                owner,
                RecruitsBattleGameTestSupport.WEST_RANGED_LEFT_POS
        );
        BuildArea buildArea = BannerModGameTestSupport.spawnOwnedBuildArea(
                helper,
                owner,
                RecruitsBattleGameTestSupport.WEST_RANGED_RIGHT_POS
        );
        worker.currentCropArea = cropArea;
        cropArea.setBeingWorkedOn(true);

        MessageUpdateBuildArea outsiderUpdate = new MessageUpdateBuildArea(
                buildArea.getUUID(),
                5,
                6,
                7,
                BannerModGameTestSupport.createMinimalBuildTemplate(),
                false,
                false
        );

        helper.assertFalse(worker.recoverControl(outsider),
                "Expected a live outsider to stay denied from worker recovery while the real owner remains present");
        helper.assertTrue(cropArea.isBeingWorkedOn(),
                "Expected outsider recovery denial to preserve the claimed crop area");
        helper.assertTrue(cropArea.getAuthoringAccess(outsider) == WorkAreaAuthoringRules.AccessLevel.FORBIDDEN,
                "Expected outsider work-area authoring access to stay forbidden");
        helper.assertTrue(buildArea.getAuthoringAccess(outsider) == WorkAreaAuthoringRules.AccessLevel.FORBIDDEN,
                "Expected outsider build-area authoring access to stay forbidden");
        helper.assertTrue(MessageUpdateBuildArea.dispatchToServer(outsider, outsiderUpdate) == WorkAreaAuthoringRules.Decision.FORBIDDEN,
                "Expected the real build-area mutation seam to reject a live outsider");
        helper.assertTrue(buildArea.getWidthSize() == 3 && buildArea.getHeightSize() == 4 && buildArea.getDepthSize() == 3,
                "Expected outsider denial to preserve the owned build-area dimensions");

        helper.succeed();
    }

    private static Player createPlayer(GameTestHelper helper, ServerLevel level, UUID playerId, String name) {
        Player player = BannerModDedicatedServerGameTestSupport.createFakeServerPlayer(level, playerId, name);
        player.moveTo(
                helper.absolutePos(RecruitsBattleGameTestSupport.SquadAnchor.WEST.anchor()).getX() + 0.5D,
                helper.absolutePos(RecruitsBattleGameTestSupport.SquadAnchor.WEST.anchor()).getY(),
                helper.absolutePos(RecruitsBattleGameTestSupport.SquadAnchor.WEST.anchor()).getZ() + 0.5D,
                -90.0F,
                0.0F
        );
        player.setYRot(-90.0F);
        return player;
    }
}
