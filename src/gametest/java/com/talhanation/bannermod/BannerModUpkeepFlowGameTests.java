package com.talhanation.bannermod;

import com.talhanation.bannermod.shared.logistics.BannerModSupplyStatus;
import com.talhanation.bannermod.events.FactionEvents;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.gametest.support.RecruitsBattleGameTestSupport;
import com.talhanation.bannermod.entity.civilian.workarea.BuildArea;
import com.talhanation.bannermod.persistence.civilian.BuildBlock;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(BannerModMain.MOD_ID)
public class BannerModUpkeepFlowGameTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void upkeepFlowMovesSameOwnerRecruitFromBlockedToReady(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        AbstractRecruitEntity recruit = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(
                helper,
                com.talhanation.bannermod.registry.military.ModEntityTypes.RECRUIT.get(),
                RecruitsBattleGameTestSupport.WEST_FRONTLINE_POS,
                "Integrated Upkeep Recruit",
                player.getUUID()
        );
        BuildArea buildArea = BannerModGameTestSupport.spawnOwnedBuildArea(
                helper,
                player,
                RecruitsBattleGameTestSupport.WEST_FLANK_POS
        );

        buildArea.setStructureNBT(BannerModGameTestSupport.createMinimalBuildTemplate());
        buildArea.setStartBuild(false);
        buildArea.stackToPlace.push(new BuildBlock(buildArea.blockPosition().above(), Blocks.OAK_PLANKS.defaultBlockState()));
        buildArea.stackToPlace.push(new BuildBlock(buildArea.blockPosition().above(2), Blocks.OAK_PLANKS.defaultBlockState()));
        buildArea.stackToPlace.push(new BuildBlock(buildArea.blockPosition().above(3), Blocks.OAK_PLANKS.defaultBlockState()));
        buildArea.stackToPlace.push(new BuildBlock(buildArea.blockPosition().above(4), Blocks.OAK_PLANKS.defaultBlockState()));

        recruit.forcedUpkeep = true;
        recruit.paymentTimer = 0;
        recruit.setHunger(1.0F);
        recruit.setUpkeepPos(buildArea.blockPosition());

        BannerModSupplyStatus.BuildProjectStatus buildStatus = BannerModSupplyStatus.buildProjectStatus(
                buildArea.hasStructureTemplate(),
                buildArea.hasPendingBuildWork(),
                buildArea.getRequiredMaterials()
        );
        SimpleContainer upkeepContainer = new SimpleContainer(9);
        BannerModSupplyStatus.RecruitSupplyStatus recruitStatus = recruit.getSupplyStatus(upkeepContainer);

        upkeepContainer.setItem(0, new ItemStack(Items.BREAD));
        ItemStack currency = FactionEvents.getCurrency();
        currency.setCount(16);
        upkeepContainer.setItem(1, currency);
        BannerModSupplyStatus.RecruitSupplyStatus resuppliedRecruitStatus = recruit.getSupplyStatus(upkeepContainer);

        helper.assertTrue(buildArea.getPlayerUUID().equals(player.getUUID()),
                "Expected the same BannerMod player to own both the settlement build area and the recruit upkeep source");
        helper.assertTrue(buildStatus.state() == BannerModSupplyStatus.BuildState.NEEDS_MATERIALS,
                "Expected the owned settlement build area to expose pending plank pressure through BannerModSupplyStatus.BuildState.NEEDS_MATERIALS");
        helper.assertTrue(buildStatus.materialTypes() == 1,
                "Expected repeated oak-plank demand to collapse into one material type for the upkeep flow slice");
        helper.assertTrue(buildStatus.materialCount() == 4,
                "Expected the owned settlement build area to report exactly four pending planks in the upkeep flow slice");
        helper.assertTrue(recruitStatus.state() == BannerModSupplyStatus.RecruitSupplyState.NEEDS_FOOD_AND_PAYMENT,
                "Expected the recruit upkeep slice to begin in BannerModSupplyStatus.RecruitSupplyState.NEEDS_FOOD_AND_PAYMENT");
        helper.assertTrue(recruitStatus.blocked(),
                "Expected the recruit upkeep slice to stay blocked until the same-owner upkeep container is resupplied");
        helper.assertTrue("recruit_upkeep_missing_food_and_payment".equals(recruitStatus.reasonToken()),
                "Expected the recruit upkeep slice to preserve the shared missing-food-and-payment reason token");
        helper.assertTrue(recruitStatus.accounting().state() == BannerModSupplyStatus.ArmyUpkeepState.UNPAID_AND_STARVING,
                "Expected the recruit upkeep slice to expose bounded unpaid-and-starving accounting state through BannerModSupplyStatus");
        helper.assertTrue(recruitStatus.accounting().starvingLevel() == 2,
                "Expected the recruit upkeep slice to bound starving accounting pressure from low hunger without a per-tick debt counter");
        helper.assertTrue(resuppliedRecruitStatus.state() == BannerModSupplyStatus.RecruitSupplyState.READY,
                "Expected the same-owner upkeep container resupply to move the recruit into BannerModSupplyStatus.RecruitSupplyState.READY");
        helper.assertFalse(resuppliedRecruitStatus.blocked(),
                "Expected the recruit upkeep slice to stop blocking readiness after bread and payment are supplied");
        helper.assertTrue(resuppliedRecruitStatus.accounting().state() == BannerModSupplyStatus.ArmyUpkeepState.STABLE,
                "Expected the recruit upkeep slice to clear shared accounting pressure once food and payment are supplied");
        helper.succeed();
    }
}
