package com.talhanation.bannermod;

import com.talhanation.recruits.Main;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.gametest.support.RecruitsBattleGameTestSupport;
import com.talhanation.recruits.gametest.support.RecruitsCommandGameTestSupport;
import com.talhanation.recruits.network.MessageMovement;
import com.talhanation.workers.entities.FarmerEntity;
import com.talhanation.workers.entities.workarea.CropArea;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Main.MOD_ID)
public class BannerModDedicatedServerReconnectGameTests {

    private static final UUID RECONNECTED_OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000704");
    private static final String RECONNECTED_OWNER_NAME = "reconnected-owner";

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty", timeoutTicks = 200)
    public static void reconnectedOwnerRegainsRecruitAndWorkerAuthorityWithoutManualRebinding(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractRecruitEntity recruit = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(
                helper,
                com.talhanation.recruits.init.ModEntityTypes.RECRUIT.get(),
                RecruitsBattleGameTestSupport.WEST_FRONTLINE_POS,
                "Reconnect Recruit",
                RECONNECTED_OWNER_UUID
        );
        Player temporaryOwner = helper.makeMockPlayer();
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
        worker.currentCropArea = cropArea;
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
                com.talhanation.recruits.init.ModEntityTypes.RECRUIT.get(),
                RecruitsBattleGameTestSupport.WEST_FRONTLINE_POS,
                "Roundtrip Recruit",
                RECONNECTED_OWNER_UUID
        );
        Player temporaryOwner = helper.makeMockPlayer();
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
        originalWorker.currentCropArea = originalCropArea;
        originalCropArea.setBeingWorkedOn(true);

        helper.runAfterDelay(5, () -> {
            RecruitsCommandGameTestSupport.prepareForCommand(originalRecruit, RecruitsCommandGameTestSupport.TARGET_GROUP_UUID);

            CompoundTag recruitData = BannerModDedicatedServerGameTestSupport.saveEntity(originalRecruit);
            CompoundTag workerData = BannerModDedicatedServerGameTestSupport.saveEntity(originalWorker);
            CompoundTag cropAreaData = BannerModDedicatedServerGameTestSupport.saveEntity(originalCropArea);

            originalRecruit.discard();
            originalWorker.discard();
            originalCropArea.discard();

            AbstractRecruitEntity loadedRecruit = BannerModDedicatedServerGameTestSupport.loadEntity(
                    helper,
                    com.talhanation.recruits.init.ModEntityTypes.RECRUIT.get(),
                    RecruitsBattleGameTestSupport.EAST_FRONTLINE_POS,
                    recruitData
            );
            FarmerEntity loadedWorker = BannerModDedicatedServerGameTestSupport.loadEntity(
                    helper,
                    com.talhanation.workers.init.ModEntityTypes.FARMER.get(),
                    RecruitsBattleGameTestSupport.EAST_FLANK_POS,
                    workerData
            );
            CropArea loadedCropArea = BannerModDedicatedServerGameTestSupport.loadEntity(
                    helper,
                    com.talhanation.workers.init.ModEntityTypes.CROPAREA.get(),
                    RecruitsBattleGameTestSupport.EAST_RANGED_LEFT_POS,
                    cropAreaData
            );
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
