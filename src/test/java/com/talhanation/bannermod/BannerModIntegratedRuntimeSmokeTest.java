package com.talhanation.bannermod;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.network.messages.military.RecruitsNetworkRegistrar;
import com.talhanation.bannermod.bootstrap.WorkersRuntime;
import com.talhanation.bannermod.bootstrap.WorkersSubsystem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BannerModIntegratedRuntimeSmokeTest {

    @Test
    void recruitRuntimeIdentityAndWorkerSubsystemSeamShareOneBannerModRuntime() {
        RecruitsNetworkRegistrar recruitsNetworkRegistrar = new RecruitsNetworkRegistrar();
        WorkersSubsystem workersSubsystem = new WorkersSubsystem();

        assertEquals("bannermod", BannerModMain.MOD_ID);
        assertEquals(BannerModMain.MOD_ID, WorkersRuntime.modId());
        assertEquals(recruitsNetworkRegistrar.orderedMessageTypes().size(), WorkersRuntime.networkIdOffset());
        assertEquals(20, workersSubsystem.networkMessageCount());
        assertTrue(workersSubsystem.networkMessageCount() > 0);
    }
}
