package com.talhanation.bannermod;

import com.talhanation.recruits.Main;
import com.talhanation.recruits.network.RecruitsNetworkRegistrar;
import com.talhanation.workers.WorkersRuntime;
import com.talhanation.workers.WorkersSubsystem;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Main.MOD_ID)
public class IntegratedRuntimeGameTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void recruitAndWorkerSeamsCoexistInBannerModRuntime(GameTestHelper helper) {
        RecruitsNetworkRegistrar recruitsNetworkRegistrar = new RecruitsNetworkRegistrar();
        WorkersSubsystem workersSubsystem = new WorkersSubsystem();

        helper.assertTrue(Main.MOD_ID.equals("bannermod"), "Expected BannerMod root runtime id");
        helper.assertTrue(WorkersRuntime.modId().equals(Main.MOD_ID), "Expected workers subsystem to share BannerMod mod id");
        helper.assertTrue(WorkersRuntime.networkIdOffset() == recruitsNetworkRegistrar.orderedMessageTypes().size(),
                "Expected workers packet offset to follow recruits packet catalog size");
        helper.assertTrue(workersSubsystem.networkMessageCount() > 0,
                "Expected workers subsystem to contribute merged runtime packets");
        helper.succeed();
    }
}
