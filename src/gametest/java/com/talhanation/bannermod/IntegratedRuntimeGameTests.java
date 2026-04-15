package com.talhanation.bannermod;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.bootstrap.WorkersRuntime;
import com.talhanation.bannermod.network.BannerModNetworkBootstrap;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(BannerModMain.MOD_ID)
public class IntegratedRuntimeGameTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void recruitAndWorkerSeamsCoexistInBannerModRuntime(GameTestHelper helper) {
        // NOTE: legacy RecruitsNetworkRegistrar.orderedMessageTypes() and
        // WorkersSubsystem.networkMessageCount() introspection seams were removed
        // by the phase-21 consolidation. The post-consolidation truth is exposed by
        // BannerModNetworkBootstrap.MILITARY_MESSAGES / CIVILIAN_MESSAGES and the
        // dynamic workerPacketOffset() bridge. WorkersRuntime.networkIdOffset() is a
        // hardcoded legacy constant tracked in deferred-items.md (plan 23-06 follow-up)
        // and is intentionally NOT asserted here.
        helper.assertTrue(BannerModMain.MOD_ID.equals("bannermod"),
                "Expected BannerMod root runtime id");
        helper.assertTrue(WorkersRuntime.modId().equals(BannerModMain.MOD_ID),
                "Expected workers subsystem to share BannerMod mod id");
        helper.assertTrue(BannerModNetworkBootstrap.workerPacketOffset() == BannerModNetworkBootstrap.MILITARY_MESSAGES.length,
                "Expected the dynamic worker packet offset to follow the consolidated military packet catalog size");
        helper.assertTrue(BannerModNetworkBootstrap.CIVILIAN_MESSAGES.length > 0,
                "Expected the consolidated civilian packet catalog to be non-empty");
        helper.succeed();
    }
}
