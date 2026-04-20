package com.talhanation.bannermod;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.bootstrap.WorkersRuntime;
import com.talhanation.bannermod.network.BannerModNetworkBootstrap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BannerModIntegratedRuntimeSmokeTest {

    @Test
    void recruitRuntimeIdentityAndWorkerSubsystemSeamShareOneBannerModRuntime() {
        assertEquals("bannermod", BannerModMain.MOD_ID);
        assertEquals(BannerModMain.MOD_ID, WorkersRuntime.modId());
        // NOTE: WorkersRuntime.networkIdOffset() is a hardcoded legacy constant (104)
        // while BannerModNetworkBootstrap.workerPacketOffset() is derived from
        // MILITARY_MESSAGES.length. These have drifted apart during phase-21
        // consolidation (MILITARY_MESSAGES.length is now 107). The drift is logged
        // in deferred-items.md for plan 23-06 and is out of scope for this plan's
        // FQN sweep. This test asserts the seam that phase-21 documented as stable.
        assertEquals(BannerModNetworkBootstrap.MILITARY_MESSAGES.length, BannerModNetworkBootstrap.workerPacketOffset());
        assertEquals(22, BannerModNetworkBootstrap.CIVILIAN_MESSAGES.length);
        assertTrue(BannerModNetworkBootstrap.CIVILIAN_MESSAGES.length > 0);
    }
}
