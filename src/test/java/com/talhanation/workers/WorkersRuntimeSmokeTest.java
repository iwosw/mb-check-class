package com.talhanation.workers;

import com.talhanation.bannermod.bootstrap.WorkersRuntime;
import com.talhanation.bannermod.network.BannerModNetworkBootstrap;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkersRuntimeSmokeTest {

    @Test
    void exposeMergedRuntimeIdentityAndLegacyNamespaceHelpers() {
        assertEquals("bannermod", WorkersRuntime.modId());
        assertEquals(ResourceLocation.fromNamespaceAndPath("bannermod", "builder"), WorkersRuntime.id("builder"));
        assertEquals(ResourceLocation.fromNamespaceAndPath("workers", "builder"), WorkersRuntime.legacyId("builder"));
    }

    @Test
    void exposeMergedAssetPathsUsedByWorkersUiAndStructures() {
        assertEquals(ResourceLocation.fromNamespaceAndPath("bannermod", "textures/gui/workers/builder.png"), WorkersRuntime.mergedGuiTexture("builder.png"));
        assertEquals(ResourceLocation.fromNamespaceAndPath("bannermod", "structures/workers"), WorkersRuntime.mergedStructureRoot());
    }

    @Test
    void keepWorkersPacketsOnDedicatedMergedOffset() {
        assertEquals(BannerModNetworkBootstrap.workerPacketOffset(), WorkersRuntime.networkIdOffset());
    }
}
