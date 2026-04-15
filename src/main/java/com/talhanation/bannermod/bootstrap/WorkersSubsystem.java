package com.talhanation.bannermod.bootstrap;

import com.talhanation.bannermod.client.military.events.CommandCategoryManager;
import com.talhanation.bannermod.events.WorkersVillagerEvents;
import com.talhanation.bannermod.events.WorkersCommandEvents;
import com.talhanation.bannermod.client.civilian.events.ScreenEvents;
import com.talhanation.bannermod.client.civilian.gui.WorkerCommandScreen;
import com.talhanation.bannermod.persistence.civilian.StructureManager;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import static com.talhanation.bannermod.registry.civilian.ModItems.ANIMAL_FARMER_SPAWN_EGG;
import static com.talhanation.bannermod.registry.civilian.ModItems.BUILDER_SPAWN_EGG;
import static com.talhanation.bannermod.registry.civilian.ModItems.FARMER_SPAWN_EGG;
import static com.talhanation.bannermod.registry.civilian.ModItems.FISHERMAN_SPAWN_EGG;
import static com.talhanation.bannermod.registry.civilian.ModItems.LUMBERJACK_SPAWN_EGG;
import static com.talhanation.bannermod.registry.civilian.ModItems.MERCHANT_SPAWN_EGG;
import static com.talhanation.bannermod.registry.civilian.ModItems.MINER_SPAWN_EGG;

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
        if (event.getTabKey().equals(CreativeModeTabs.SPAWN_EGGS)) {
            event.accept(FARMER_SPAWN_EGG.get());
            event.accept(LUMBERJACK_SPAWN_EGG.get());
            event.accept(MINER_SPAWN_EGG.get());
            event.accept(MERCHANT_SPAWN_EGG.get());
            event.accept(BUILDER_SPAWN_EGG.get());
            event.accept(FISHERMAN_SPAWN_EGG.get());
            event.accept(ANIMAL_FARMER_SPAWN_EGG.get());
        }
    }
}
