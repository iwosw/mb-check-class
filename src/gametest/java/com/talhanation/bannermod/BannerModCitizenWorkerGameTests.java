package com.talhanation.bannermod;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.gametest.support.RecruitsBattleGameTestSupport;
import com.talhanation.bannermod.entity.civilian.FarmerEntity;
import com.talhanation.bannermod.entity.civilian.workarea.CropArea;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(BannerModMain.MOD_ID)
public class BannerModCitizenWorkerGameTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void workerRecoveryAndBindingSurviveCitizenBackedPersistence(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        FarmerEntity worker = BannerModGameTestSupport.spawnOwnedFarmer(
                helper,
                player,
                RecruitsBattleGameTestSupport.WEST_FLANK_POS
        );
        CropArea cropArea = BannerModGameTestSupport.spawnOwnedCropArea(
                helper,
                player,
                RecruitsBattleGameTestSupport.WEST_RANGED_LEFT_POS
        );
        worker.currentCropArea = cropArea;
        cropArea.setBeingWorkedOn(true);

        CompoundTag saved = BannerModDedicatedServerGameTestSupport.saveEntity(worker);
        FarmerEntity reloaded = BannerModDedicatedServerGameTestSupport.loadEntity(
                helper,
                com.talhanation.bannermod.registry.civilian.ModEntityTypes.FARMER.get(),
                RecruitsBattleGameTestSupport.WEST_RANGED_RIGHT_POS,
                saved
        );

        helper.assertTrue(player.getUUID().equals(reloaded.getOwnerUUID()),
                "Expected worker owner UUID to survive citizen-backed persistence round-trip.");
        helper.assertTrue(reloaded.getCitizenCore().getBoundWorkAreaUUID() != null,
                "Expected worker citizen core to preserve the bound work-area UUID after reload.");
        helper.assertTrue(reloaded.recoverControl(player),
                "Expected worker recovery control to remain available after citizen-backed persistence reload.");
        helper.succeed();
    }
}
