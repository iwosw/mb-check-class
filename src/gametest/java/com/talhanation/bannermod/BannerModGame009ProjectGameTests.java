package com.talhanation.bannermod;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("bannermod_game_009")
public class BannerModGame009ProjectGameTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty", templateNamespace = BannerModMain.MOD_ID)
    public static void settlementProjectBindsToExecutableBuildAreaTarget(GameTestHelper helper) {
        BannerModSettlementProjectGameTests.assertSettlementProjectBindsToExecutableBuildAreaTarget(helper);
    }
}
