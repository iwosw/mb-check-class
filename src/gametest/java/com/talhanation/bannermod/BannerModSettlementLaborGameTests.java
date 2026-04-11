package com.talhanation.bannermod;

import com.talhanation.recruits.Main;
import com.talhanation.recruits.gametest.support.RecruitsBattleGameTestSupport;
import com.talhanation.workers.entities.FarmerEntity;
import com.talhanation.workers.entities.workarea.CropArea;
import com.talhanation.workers.entities.workarea.StorageArea;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Main.MOD_ID)
public class BannerModSettlementLaborGameTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void settlementLaborKeepsAuthorizationSeparateFromRecoveryControl(GameTestHelper helper) {
        Player ownerPlayer = helper.makeMockPlayer();
        Player outsiderPlayer = helper.makeMockPlayer();
        FarmerEntity worker = BannerModGameTestSupport.spawnOwnedFarmer(
                helper,
                ownerPlayer,
                RecruitsBattleGameTestSupport.WEST_FLANK_POS
        );
        CropArea cropArea = BannerModGameTestSupport.spawnOwnedCropArea(
                helper,
                ownerPlayer,
                RecruitsBattleGameTestSupport.WEST_RANGED_LEFT_POS
        );
        StorageArea storageArea = BannerModGameTestSupport.spawnOwnedStorageArea(
                helper,
                ownerPlayer,
                RecruitsBattleGameTestSupport.WEST_RANGED_RIGHT_POS
        );

        worker.currentCropArea = cropArea;
        cropArea.setBeingWorkedOn(true);

        helper.assertTrue(cropArea.canWorkHere(worker),
                "Expected the owned worker to legally participate in the owned crop area during the BannerMod settlement labor slice");
        helper.assertTrue(storageArea.getPlayerUUID().equals(ownerPlayer.getUUID()),
                "Expected the storage area to stay owned by the same BannerMod player as the settlement worker");
        helper.assertFalse(worker.recoverControl(outsiderPlayer),
                "Expected a non-owner requester to be denied worker recovery even when settlement labor participation is valid");
        helper.assertTrue(cropArea.isBeingWorkedOn(),
                "Expected outsider recovery denial to preserve the claimed crop area so labor authorization remains intact");
        helper.assertTrue(worker.getCurrentWorkArea() == cropArea,
                "Expected outsider recovery denial to preserve the worker's current crop-area binding");
        helper.succeed();
    }
}
