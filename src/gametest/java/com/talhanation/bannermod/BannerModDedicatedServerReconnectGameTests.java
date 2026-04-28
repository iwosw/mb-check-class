package com.talhanation.bannermod;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.RecruitIndex;
import com.talhanation.bannermod.gametest.support.RecruitsBattleGameTestSupport;
import com.talhanation.bannermod.gametest.support.RecruitsCommandGameTestSupport;
import com.talhanation.bannermod.network.messages.military.MessageMovement;
import com.talhanation.bannermod.entity.civilian.FarmerEntity;
import com.talhanation.bannermod.entity.civilian.workarea.CropArea;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;
import java.util.List;

@GameTestHolder(BannerModMain.MOD_ID)
public class BannerModDedicatedServerReconnectGameTests {

    private static final UUID RECONNECTED_OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000704");
    private static final UUID ROUNDTRIP_GROUP_UUID = UUID.fromString("00000000-0000-0000-0000-000000000705");
    private static final String RECONNECTED_OWNER_NAME = "reconnected-owner";
    private static final String RECONNECTED_OWNER_TEAM_ID = "phase07_reconnected_owner";

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty", timeoutTicks = 200)
    public static void reconnectedOwnerRegainsRecruitAndWorkerAuthorityWithoutManualRebinding(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractRecruitEntity recruit = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(
                helper,
                com.talhanation.bannermod.registry.military.ModEntityTypes.RECRUIT.get(),
                RecruitsBattleGameTestSupport.WEST_FRONTLINE_POS,
                "Reconnect Recruit",
                RECONNECTED_OWNER_UUID
        );
        Player temporaryOwner = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        FarmerEntity worker = BannerModGameTestSupport.spawnOwnedFarmer(
                helper,
                temporaryOwner,
                RecruitsBattleGameTestSupport.WEST_FLANK_POS
        );
        CropArea cropArea = BannerModGameTestSupport.spawnOwnedCropArea(
                helper,
                temporaryOwner,
                RecruitsBattleGameTestSupport.WEST_RANGED_LEFT_POS
        );

        BannerModDedicatedServerGameTestSupport.assignDetachedOwnership(recruit, RECONNECTED_OWNER_UUID);
        RecruitsCommandGameTestSupport.prepareForCommand(recruit, RecruitsCommandGameTestSupport.TARGET_GROUP_UUID);
        BannerModDedicatedServerGameTestSupport.assignDetachedOwnership(worker, RECONNECTED_OWNER_UUID);
        BannerModDedicatedServerGameTestSupport.assignDetachedOwnership(cropArea, RECONNECTED_OWNER_UUID, RECONNECTED_OWNER_NAME);
        cropArea.setTeamStringID(RECONNECTED_OWNER_TEAM_ID);
        BannerModDedicatedServerGameTestSupport.seedClaim(level, helper.absolutePos(RecruitsBattleGameTestSupport.WEST_RANGED_LEFT_POS), RECONNECTED_OWNER_TEAM_ID, RECONNECTED_OWNER_UUID, RECONNECTED_OWNER_NAME);
        worker.setCurrentWorkArea(cropArea);
        cropArea.setBeingWorkedOn(true);

        Player reconnectedPlayer = createReconnectedPlayer(helper, level);

        MessageMovement.dispatchToServer(
                reconnectedPlayer,
                reconnectedPlayer.getUUID(),
                RecruitsCommandGameTestSupport.TARGET_GROUP_UUID,
                1,
                0,
                false
        );

        helper.runAfterDelay(5, () -> {
            helper.assertTrue(recruit.getFollowState() == 1,
                    "Expected the reconnecting owner UUID to regain recruit movement authority without rebinding ownership");
            helper.assertTrue(cropArea.canWorkHere(worker),
                    "Expected detached worker ownership to remain valid for the reconnecting owner's crop area");
            helper.assertTrue(worker.recoverControl(reconnectedPlayer),
                    "Expected the reconnecting owner UUID to regain worker recovery authority without manual rebinding");
            helper.assertFalse(cropArea.isBeingWorkedOn(),
                    "Expected reconnect recovery to release the claimed crop area");
            helper.assertTrue(worker.getCurrentWorkArea() == null,
                    "Expected reconnect recovery to clear the worker's crop-area binding");
            helper.succeed();
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty", timeoutTicks = 240)
    public static void reconnectedOwnerRecoversAuthorityAfterOwnershipRoundTrip(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractRecruitEntity originalRecruit = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(
                helper,
                com.talhanation.bannermod.registry.military.ModEntityTypes.RECRUIT.get(),
                RecruitsBattleGameTestSupport.WEST_FRONTLINE_POS,
                "Roundtrip Recruit",
                RECONNECTED_OWNER_UUID
        );
        Player temporaryOwner = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        FarmerEntity originalWorker = BannerModGameTestSupport.spawnOwnedFarmer(
                helper,
                temporaryOwner,
                RecruitsBattleGameTestSupport.WEST_FLANK_POS
        );
        CropArea originalCropArea = BannerModGameTestSupport.spawnOwnedCropArea(
                helper,
                temporaryOwner,
                RecruitsBattleGameTestSupport.WEST_RANGED_LEFT_POS
        );

        BannerModDedicatedServerGameTestSupport.assignDetachedOwnership(originalRecruit, RECONNECTED_OWNER_UUID);
        RecruitsCommandGameTestSupport.prepareForCommand(originalRecruit, RecruitsCommandGameTestSupport.TARGET_GROUP_UUID);
        BannerModDedicatedServerGameTestSupport.assignDetachedOwnership(originalWorker, RECONNECTED_OWNER_UUID);
        BannerModDedicatedServerGameTestSupport.assignDetachedOwnership(originalCropArea, RECONNECTED_OWNER_UUID, RECONNECTED_OWNER_NAME);
        originalCropArea.setTeamStringID(RECONNECTED_OWNER_TEAM_ID);
        BannerModDedicatedServerGameTestSupport.seedClaim(level, helper.absolutePos(RecruitsBattleGameTestSupport.WEST_RANGED_LEFT_POS), RECONNECTED_OWNER_TEAM_ID, RECONNECTED_OWNER_UUID, RECONNECTED_OWNER_NAME);
        originalWorker.setCurrentWorkArea(originalCropArea);
        originalCropArea.setBeingWorkedOn(true);

        helper.runAfterDelay(5, () -> {
            RecruitsCommandGameTestSupport.prepareForCommand(originalRecruit, ROUNDTRIP_GROUP_UUID);

            CompoundTag recruitData = BannerModDedicatedServerGameTestSupport.saveEntity(originalRecruit);
            CompoundTag workerData = BannerModDedicatedServerGameTestSupport.saveEntity(originalWorker);
            CompoundTag cropAreaData = BannerModDedicatedServerGameTestSupport.saveEntity(originalCropArea);

            RecruitIndex.instance().onEntityLeave(originalRecruit);
            originalRecruit.discard();
            originalWorker.discard();
            originalCropArea.discard();

            AbstractRecruitEntity loadedRecruit = BannerModDedicatedServerGameTestSupport.loadEntity(
                    helper,
                    com.talhanation.bannermod.registry.military.ModEntityTypes.RECRUIT.get(),
                    RecruitsBattleGameTestSupport.EAST_FRONTLINE_POS,
                    recruitData
            );
            FarmerEntity loadedWorker = BannerModDedicatedServerGameTestSupport.loadEntity(
                    helper,
                    com.talhanation.bannermod.registry.civilian.ModEntityTypes.FARMER.get(),
                    RecruitsBattleGameTestSupport.EAST_FLANK_POS,
                    workerData
            );
            CropArea loadedCropArea = BannerModDedicatedServerGameTestSupport.loadEntity(
                    helper,
                    com.talhanation.bannermod.registry.civilian.ModEntityTypes.CROPAREA.get(),
                    RecruitsBattleGameTestSupport.EAST_RANGED_LEFT_POS,
                    cropAreaData
            );
            RecruitsBattleGameTestSupport.assignFormationCohort(List.of(loadedRecruit), ROUNDTRIP_GROUP_UUID);
            RecruitsCommandGameTestSupport.prepareForCommand(loadedRecruit, ROUNDTRIP_GROUP_UUID);
            BannerModDedicatedServerGameTestSupport.seedClaim(level, loadedCropArea.blockPosition(), RECONNECTED_OWNER_TEAM_ID, RECONNECTED_OWNER_UUID, RECONNECTED_OWNER_NAME);
            Player reconnectedPlayer = createReconnectedPlayer(helper, level);

            helper.runAfterDelay(5, () -> {
                MessageMovement.dispatchToServer(
                        reconnectedPlayer,
                        reconnectedPlayer.getUUID(),
                        ROUNDTRIP_GROUP_UUID,
                        1,
                        0,
                        false
                );
            });

            helper.runAfterDelay(12, () -> {
                helper.assertTrue(RECONNECTED_OWNER_UUID.equals(loadedRecruit.getOwnerUUID()),
                        "Expected recruit ownership to survive the save/load round trip");
                helper.assertTrue(RECONNECTED_OWNER_UUID.equals(loadedWorker.getOwnerUUID()),
                        "Expected worker ownership to survive the save/load round trip");
                helper.assertTrue(RECONNECTED_OWNER_UUID.equals(loadedCropArea.getPlayerUUID()),
                        "Expected crop-area ownership to survive the save/load round trip");
                helper.assertTrue(loadedRecruit.getFollowState() == 1,
                        "Expected the reconnecting owner UUID to regain recruit command authority after the save/load round trip");
                helper.assertTrue(loadedCropArea.canWorkHere(loadedWorker),
                        "Expected the reloaded crop area to still authorize the reloaded worker by owner UUID");
                helper.assertTrue(workerData.contains("boundWorkArea"),
                        "Expected worker save data to retain the prior work-area binding for reconnect recovery");
                helper.assertTrue(workerData.getUUID("boundWorkArea").equals(cropAreaData.getUUID("UUID")),
                        "Expected worker save data to point at the same crop-area UUID after serialization");
                helper.assertTrue(loadedWorker.recoverControl(reconnectedPlayer),
                        "Expected the reconnecting owner UUID to regain worker recovery authority after the save/load round trip");
                helper.succeed();
            });
        });
    }

    private static Player createReconnectedPlayer(GameTestHelper helper, ServerLevel level) {
        Player player = BannerModDedicatedServerGameTestSupport.createFakeServerPlayer(level, RECONNECTED_OWNER_UUID, RECONNECTED_OWNER_NAME);
        BannerModDedicatedServerGameTestSupport.joinTeam(level, RECONNECTED_OWNER_TEAM_ID, player);
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
