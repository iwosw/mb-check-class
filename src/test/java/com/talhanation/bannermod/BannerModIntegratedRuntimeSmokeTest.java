package com.talhanation.bannermod;

import com.talhanation.recruits.Main;
import com.talhanation.recruits.network.RecruitsNetworkRegistrar;
import com.talhanation.workers.WorkersRuntime;
import com.talhanation.workers.WorkersSubsystem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BannerModIntegratedRuntimeSmokeTest {

    @Test
    void recruitRuntimeIdentityAndWorkerSubsystemSeamShareOneBannerModRuntime() {
        RecruitsNetworkRegistrar recruitsNetworkRegistrar = new RecruitsNetworkRegistrar();
        WorkersSubsystem workersSubsystem = new WorkersSubsystem();

        assertEquals("bannermod", Main.MOD_ID);
        assertEquals(Main.MOD_ID, WorkersRuntime.modId());
        assertEquals(recruitsNetworkRegistrar.orderedMessageTypes().size(), WorkersRuntime.networkIdOffset());
        assertEquals(20, workersSubsystem.networkMessageCount());
        assertTrue(workersSubsystem.networkMessageCount() > 0);
    }
}
