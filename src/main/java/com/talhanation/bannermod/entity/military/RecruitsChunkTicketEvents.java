package com.talhanation.bannermod.entity.military;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;

@EventBusSubscriber(modid = BannerModMain.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class RecruitsChunkTicketEvents {
    private RecruitsChunkTicketEvents() {
    }

    @SubscribeEvent
    public static void registerTicketControllers(RegisterTicketControllersEvent event) {
        event.register(AbstractChunkLoaderEntity.CHUNK_TICKETS);
    }
}
