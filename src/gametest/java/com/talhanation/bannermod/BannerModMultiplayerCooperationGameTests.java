package com.talhanation.bannermod;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.gametest.support.RecruitsBattleGameTestSupport;
import com.talhanation.bannermod.gametest.support.RecruitsCommandGameTestSupport;
import com.talhanation.bannermod.network.messages.military.MessageMovement;
import com.talhanation.bannermod.entity.civilian.FarmerEntity;
import com.talhanation.bannermod.entity.civilian.workarea.BuildArea;
import com.talhanation.bannermod.entity.civilian.workarea.CropArea;
import com.talhanation.bannermod.network.messages.civilian.MessageUpdateBuildArea;
import com.talhanation.bannermod.network.messages.civilian.WorkAreaAuthoringRules;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(BannerModMain.MOD_ID)
public class BannerModMultiplayerCooperationGameTests {

    private static final UUID OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000811");
    private static final UUID ALLY_UUID = UUID.fromString("00000000-0000-0000-0000-000000000812");
    private static final UUID OUTSIDER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000813");
    private static final String TEAM_ID = "phase08_allies";

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void sameTeamCooperationAllowsCommandsAndAuthoringButStillDeniesOutsiders(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player owner = createPlayer(helper, level, OWNER_UUID, "coop-owner");
        ServerPlayer ally = (ServerPlayer) createPlayer(helper, level, ALLY_UUID, "coop-ally");
        ServerPlayer outsider = (ServerPlayer) createPlayer(helper, level, OUTSIDER_UUID, "coop-outsider");
        RecruitsBattleGameTestSupport.BattleSquad squad = RecruitsBattleGameTestSupport.spawnRecoveryPair(
                helper,
                RecruitsBattleGameTestSupport.SquadAnchor.WEST,
                OWNER_UUID,
                "Cooperation"
        );
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

        BannerModDedicatedServerGameTestSupport.joinTeam(level, TEAM_ID, owner, ally, worker);
        BannerModDedicatedServerGameTestSupport.seedClaim(level, helper.absolutePos(RecruitsBattleGameTestSupport.WEST_RANGED_LEFT_POS), TEAM_ID, owner.getUUID(), owner.getScoreboardName());
        for (AbstractRecruitEntity recruit : squad.recruits()) {
            RecruitsCommandGameTestSupport.prepareForCommand(recruit, RecruitsCommandGameTestSupport.TARGET_GROUP_UUID);
            BannerModDedicatedServerGameTestSupport.joinTeam(level, TEAM_ID, recruit);
        }
        cropArea.setTeamStringID(TEAM_ID);
        buildArea.setTeamStringID(TEAM_ID);

        MessageMovement.dispatchToServer(
                ally,
                ally.getUUID(),
                RecruitsCommandGameTestSupport.TARGET_GROUP_UUID,
                1,
                0,
                false
        );

        for (AbstractRecruitEntity recruit : squad.recruits()) {
            helper.assertTrue(recruit.getFollowState() == 1,
                    "Expected a same-team allied player to use the cooperative recruit command path without taking ownership");
        }

        MessageUpdateBuildArea allyUpdate = new MessageUpdateBuildArea(
                buildArea.getUUID(),
                5,
                6,
                7,
                BannerModGameTestSupport.createMinimalBuildTemplate(),
                false,
                false
        );
        MessageUpdateBuildArea outsiderUpdate = new MessageUpdateBuildArea(
                buildArea.getUUID(),
                8,
                9,
                10,
                BannerModGameTestSupport.createMinimalBuildTemplate(),
                false,
                false
        );

        helper.assertTrue(cropArea.getAuthoringAccess(ally) == WorkAreaAuthoringRules.AccessLevel.SAME_TEAM,
                "Expected same-team work-area authoring access to stay allowed for inspection and modification");
        helper.assertTrue(buildArea.getAuthoringAccess(ally) == WorkAreaAuthoringRules.AccessLevel.SAME_TEAM,
                "Expected same-team build-area authoring access to stay allowed");
        helper.assertTrue(cropArea.canWorkHere(worker),
                "Expected the owned worker to remain legal for the same-team crop area");
        helper.assertTrue(MessageUpdateBuildArea.dispatchToServer(ally, allyUpdate) == WorkAreaAuthoringRules.Decision.ALLOW,
                "Expected the real build-area mutation seam to allow same-team cooperation");
        helper.assertTrue(buildArea.getWidthSize() == 5 && buildArea.getHeightSize() == 6 && buildArea.getDepthSize() == 7,
                "Expected the allied same-team mutation to update the owned build area");
        helper.assertTrue(MessageUpdateBuildArea.dispatchToServer(outsider, outsiderUpdate) == WorkAreaAuthoringRules.Decision.FORBIDDEN,
                "Expected outsider denial to remain intact after same-team cooperation support is exercised");
        helper.assertTrue(buildArea.getWidthSize() == 5 && buildArea.getHeightSize() == 6 && buildArea.getDepthSize() == 7,
                "Expected outsider denial to preserve the same-team-authorized build-area update");

        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void sameTeamCooperationStillKeepsWorkerRecoveryOwnerOrAdminOnly(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player owner = createPlayer(helper, level, OWNER_UUID, "recovery-owner");
        Player ally = createPlayer(helper, level, ALLY_UUID, "recovery-ally");
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
        worker.currentCropArea = cropArea;
        cropArea.setBeingWorkedOn(true);

        BannerModDedicatedServerGameTestSupport.joinTeam(level, TEAM_ID, owner, ally, worker);
        cropArea.setTeamStringID(TEAM_ID);
        BannerModDedicatedServerGameTestSupport.seedClaim(level, helper.absolutePos(RecruitsBattleGameTestSupport.WEST_RANGED_LEFT_POS), TEAM_ID, owner.getUUID(), owner.getScoreboardName());

        helper.assertTrue(cropArea.getAuthoringAccess(ally) == WorkAreaAuthoringRules.AccessLevel.SAME_TEAM,
                "Expected same-team access to exist before the worker recovery denial check");
        helper.assertFalse(worker.recoverControl(ally),
                "Expected same-team cooperation to stop at authoring and not silently transfer worker recovery control");
        helper.assertTrue(cropArea.isBeingWorkedOn(),
                "Expected denied same-team recovery to preserve the claimed crop area");
        helper.assertTrue(worker.getCurrentWorkArea() == cropArea,
                "Expected denied same-team recovery to preserve the worker's current work-area binding");

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
