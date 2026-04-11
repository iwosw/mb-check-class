package com.talhanation.bannermod;

import com.talhanation.recruits.Main;
import com.talhanation.recruits.RecruitEvents;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.gametest.support.RecruitsBattleGameTestSupport;
import com.talhanation.workers.entities.FarmerEntity;
import com.talhanation.workers.entities.workarea.CropArea;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;
import java.util.UUID;

@GameTestHolder(Main.MOD_ID)
public class BannerModOwnershipCycleGameTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void sharedOwnershipKeepsPlayerCycleAuthorityAligned(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        AbstractRecruitEntity recruit = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(
                helper,
                com.talhanation.recruits.init.ModEntityTypes.RECRUIT.get(),
                RecruitsBattleGameTestSupport.WEST_FRONTLINE_POS,
                "Integrated Recruit",
                player.getUUID()
        );
        FarmerEntity worker = BannerModGameTestSupport.spawnEntity(
                helper,
                com.talhanation.workers.init.ModEntityTypes.FARMER.get(),
                RecruitsBattleGameTestSupport.WEST_FLANK_POS
        );
        CropArea cropArea = BannerModGameTestSupport.spawnOwnedCropArea(
                helper,
                player,
                RecruitsBattleGameTestSupport.WEST_RANGED_LEFT_POS
        );

        worker.setCustomNameVisible(true);
        worker.setPersistenceRequired();
        recruit.setTarget(worker);
        worker.setTarget(recruit);

        helper.assertTrue(RecruitEvents.canAttack(recruit, worker),
                "Expected the player-cycle slice to begin with hostile recruit-to-worker targeting before ownership sync");
        helper.assertTrue(RecruitEvents.canAttack(worker, recruit),
                "Expected the player-cycle slice to begin with hostile worker-to-recruit targeting before ownership sync");

        worker.setOwnerUUID(Optional.of(player.getUUID()));
        worker.setIsOwned(true);
        worker.setFollowState(2);
        worker.setHoldPos(net.minecraft.world.phys.Vec3.atCenterOf(worker.blockPosition()));
        worker.currentCropArea = cropArea;
        cropArea.setBeingWorkedOn(true);

        helper.assertFalse(RecruitEvents.canAttack(recruit, worker),
                "Expected shared player ownership to stop recruit hostility toward the owned worker in the BannerMod gameplay cycle");
        helper.assertFalse(RecruitEvents.canAttack(worker, recruit),
                "Expected shared player ownership to stop worker hostility toward the owned recruit in the BannerMod gameplay cycle");
        helper.assertTrue(cropArea.canWorkHere(worker),
                "Expected the shared-owner crop area to accept the worker during the BannerMod ownership cycle slice");
        helper.assertTrue(worker.recoverControl(player),
                "Expected the owning player to recover worker control during the BannerMod ownership cycle slice");
        helper.assertFalse(cropArea.isBeingWorkedOn(),
                "Expected worker recovery to release the claimed crop area during the ownership cycle slice");
        helper.assertTrue(worker.getCurrentWorkArea() == null,
                "Expected worker recovery to clear the crop-area binding during the ownership cycle slice");

        worker.setOwnerUUID(Optional.of(UUID.randomUUID()));

        helper.assertTrue(RecruitEvents.canAttack(recruit, worker),
                "Expected divergent ownership to restore recruit hostility after the BannerMod ownership cycle splits actor control");
        helper.assertTrue(RecruitEvents.canAttack(worker, recruit),
                "Expected divergent ownership to restore worker hostility after the BannerMod ownership cycle splits actor control");
        helper.assertFalse(cropArea.canWorkHere(worker),
                "Expected divergent ownership to make the crop area reject the worker after the BannerMod ownership cycle loses shared authority");
        helper.succeed();
    }
}
