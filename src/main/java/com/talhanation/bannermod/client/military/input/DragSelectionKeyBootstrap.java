package com.talhanation.bannermod.client.military.input;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Mod-bus registrar for the drag-selection keybind. Lives in its own class because
 * the actual handler in {@link DragSelectionHandler} is wired to the Forge event bus,
 * while {@link RegisterKeyMappingsEvent} fires on the mod bus.
 */
@Mod.EventBusSubscriber(modid = BannerModMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class DragSelectionKeyBootstrap {
    private DragSelectionKeyBootstrap() {
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        DragSelectionHandler.register(event);
    }
}
