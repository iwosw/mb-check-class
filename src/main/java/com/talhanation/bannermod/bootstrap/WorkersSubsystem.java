package com.talhanation.bannermod.bootstrap;

import com.talhanation.bannermod.client.military.events.CommandCategoryManager;
import com.talhanation.bannermod.events.WorkersVillagerEvents;
import com.talhanation.bannermod.events.WorkersCommandEvents;
import com.talhanation.bannermod.client.civilian.events.ScreenEvents;
import com.talhanation.bannermod.client.civilian.gui.WorkerCommandScreen;
import com.talhanation.bannermod.persistence.civilian.StructureManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Canonical bannermod.bootstrap copy of the Workers subsystem lifecycle and network glue.
 * Replaces the workers.WorkersSubsystem which referenced reverted bannerlord.* classes.
 */
public class WorkersSubsystem {

    public WorkersSubsystem() {
    }

    public void registerCommon(IEventBus modEventBus) {
        MinecraftForge.EVENT_BUS.register(new WorkersVillagerEvents());
        MinecraftForge.EVENT_BUS.register(new WorkersCommandEvents());
    }

    public void registerRuntimeListeners() {
    }

    public void registerNetwork(SimpleChannel channel) {
        WorkersRuntime.bindChannel(channel);
    }

    @OnlyIn(Dist.CLIENT)
    public void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(com.talhanation.bannermod.registry.civilian.ModMenuTypes::registerMenus);
        event.enqueueWork(StructureManager::copyDefaultStructuresIfMissing);
        CommandCategoryManager.register(new WorkerCommandScreen());
        MinecraftForge.EVENT_BUS.register(new ScreenEvents());
    }

    public void registerClientRuntimeListeners() {
    }

    public void addCreativeTabs(BuildCreativeModeTabContentsEvent event) {
    }
}
