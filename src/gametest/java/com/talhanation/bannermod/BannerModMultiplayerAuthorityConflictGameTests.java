package com.talhanation.bannermod;

import com.mojang.authlib.GameProfile;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.gametest.support.RecruitsBattleGameTestSupport;
import com.talhanation.bannermod.gametest.support.RecruitsCommandGameTestSupport;
import com.talhanation.bannermod.network.messages.military.MessageFollowGui;
import com.talhanation.bannermod.network.messages.military.MessageMovement;
import com.talhanation.bannermod.network.messages.military.MessageRest;
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
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(BannerModMain.MOD_ID)
public class BannerModMultiplayerAuthorityConflictGameTests {

    private static final UUID OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000801");
    private static final UUID OUTSIDER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000802");
    private static final UUID TEAMMATE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000803");
    private static final UUID ADMIN_UUID = UUID.fromString("00000000-0000-0000-0000-000000000804");
    private static final String COMMAND_TEAM = "auth_007_command_team";

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
    public static void teammateAndAdminRecruitCommandsUseSameAuthorityAsOwner(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player owner = createPlayer(helper, level, OWNER_UUID, "authority-owner-team");
        ServerPlayer teammate = (ServerPlayer) createPlayer(helper, level, TEAMMATE_UUID, "authority-teammate");
        ServerPlayer admin = createAdminPlayer(helper, level, ADMIN_UUID, "authority-admin");
        RecruitsBattleGameTestSupport.BattleSquad squad = RecruitsBattleGameTestSupport.spawnRecoveryPair(
                helper,
                RecruitsBattleGameTestSupport.SquadAnchor.WEST,
                OWNER_UUID,
                "Authority Team"
        );

        for (AbstractRecruitEntity recruit : squad.recruits()) {
            RecruitsCommandGameTestSupport.prepareForCommand(recruit, RecruitsCommandGameTestSupport.TARGET_GROUP_UUID);
        }
        BannerModDedicatedServerGameTestSupport.joinTeam(level, COMMAND_TEAM, owner, teammate, squad.recruits().get(0), squad.recruits().get(1));

        MessageFollowGui.dispatchToServer(teammate, squad.recruits().get(0).getUUID(), 1);
        helper.assertTrue(squad.recruits().get(0).getFollowState() == 1,
                "Expected teammate single-recruit GUI command to follow canonical team authority");

        MessageRest.dispatchToServer(admin, admin.getUUID(), RecruitsCommandGameTestSupport.TARGET_GROUP_UUID, true);
        for (AbstractRecruitEntity recruit : squad.recruits()) {
            helper.assertTrue(recruit.getShouldRest(),
                    "Expected admin legacy group command to follow canonical admin authority");
        }

        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void unsupportedNationAuthorityDoesNotGrantRecruitCommands(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        createPlayer(helper, level, OWNER_UUID, "authority-owner-nation");
        ServerPlayer outsider = (ServerPlayer) createPlayer(helper, level, OUTSIDER_UUID, "authority-outsider-nation");
        RecruitsBattleGameTestSupport.BattleSquad squad = RecruitsBattleGameTestSupport.spawnRecoveryPair(
                helper,
                RecruitsBattleGameTestSupport.SquadAnchor.WEST,
                OWNER_UUID,
                "Authority Nation"
        );

        for (AbstractRecruitEntity recruit : squad.recruits()) {
            RecruitsCommandGameTestSupport.prepareForCommand(recruit, RecruitsCommandGameTestSupport.TARGET_GROUP_UUID);
        }

        MessageRest.dispatchToServer(outsider, outsider.getUUID(), RecruitsCommandGameTestSupport.TARGET_GROUP_UUID, true);
        for (AbstractRecruitEntity recruit : squad.recruits()) {
            helper.assertFalse(recruit.getShouldRest(),
                    "Expected unsupported nation/outsider authority to stay denied for legacy group commands");
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
        worker.setCurrentWorkArea(cropArea);
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
        helper.assertTrue(dispatchBuildAreaUpdate(buildArea, outsider, outsiderUpdate) == WorkAreaAuthoringRules.Decision.FORBIDDEN,
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

    private static ServerPlayer createAdminPlayer(GameTestHelper helper, ServerLevel level, UUID playerId, String name) {
        FakePlayer player = new FakePlayer(level, new GameProfile(playerId, name)) {
            @Override
            public boolean hasPermissions(int permissionLevel) {
                return true;
            }
        };
        level.addFreshEntity(player);
        player.moveTo(helper.absolutePos(RecruitsBattleGameTestSupport.SquadAnchor.WEST.anchor()).getX() + 0.5D,
                helper.absolutePos(RecruitsBattleGameTestSupport.SquadAnchor.WEST.anchor()).getY(),
                helper.absolutePos(RecruitsBattleGameTestSupport.SquadAnchor.WEST.anchor()).getZ() + 0.5D,
                -90.0F,
                0.0F);
        player.setYRot(-90.0F);
        return player;
    }

    /**
     * Mirrors {@link MessageUpdateBuildArea#executeServerSide} authorization and mutation
     * via {@link WorkAreaAuthoringRules#modifyDecision} + {@link MessageUpdateBuildArea#update},
     * returning the decision so callers can assert ALLOW/FORBIDDEN through the consolidated seam.
     */
    private static WorkAreaAuthoringRules.Decision dispatchBuildAreaUpdate(BuildArea buildArea,
                                                                           ServerPlayer player,
                                                                           MessageUpdateBuildArea update) {
        WorkAreaAuthoringRules.Decision decision = WorkAreaAuthoringRules.modifyDecision(true, buildArea.getAuthoringAccess(player));
        if (WorkAreaAuthoringRules.isAllowed(decision)) {
            update.update(buildArea);
        }
        return decision;
    }
}
