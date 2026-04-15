package com.talhanation.bannermod.bootstrap;

import com.talhanation.recruits.client.events.CommandCategoryManager;
import com.talhanation.workers.VillagerEvents;
import com.talhanation.workers.CommandEvents;
import com.talhanation.workers.client.events.ScreenEvents;
import com.talhanation.workers.client.gui.WorkerCommandScreen;
import com.talhanation.workers.init.ModMenuTypes;
import com.talhanation.workers.world.StructureManager;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import static com.talhanation.workers.init.ModItems.ANIMAL_FARMER_SPAWN_EGG;
import static com.talhanation.workers.init.ModItems.BUILDER_SPAWN_EGG;
import static com.talhanation.workers.init.ModItems.FARMER_SPAWN_EGG;
import static com.talhanation.workers.init.ModItems.FISHERMAN_SPAWN_EGG;
import static com.talhanation.workers.init.ModItems.LUMBERJACK_SPAWN_EGG;
import static com.talhanation.workers.init.ModItems.MERCHANT_SPAWN_EGG;
import static com.talhanation.workers.init.ModItems.MINER_SPAWN_EGG;

/**
 * Canonical bannermod.bootstrap copy of the Workers subsystem lifecycle and network glue.
 * Replaces the workers.WorkersSubsystem which referenced reverted bannerlord.* classes.
 */
public class WorkersSubsystem {

    public WorkersSubsystem() {
    }

    public void registerCommon(IEventBus modEventBus) {
        // Register remaining workers deferred registers not handled by BannerModMain
        // (ModBlocks, ModEntityTypes, etc. are registered directly in BannerModMain)
        MinecraftForge.EVENT_BUS.register(new VillagerEvents());
        MinecraftForge.EVENT_BUS.register(new CommandEvents());
    }

    public void registerRuntimeListeners() {
        // Additional forge-bus listener registration if needed beyond BannerModMain
    }

    public void registerNetwork(SimpleChannel channel) {
        WorkersRuntime.bindChannel(channel);
    }

    @OnlyIn(Dist.CLIENT)
    public void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ModMenuTypes::registerMenus);
        event.enqueueWork(StructureManager::copyDefaultStructuresIfMissing);
        CommandCategoryManager.register(new WorkerCommandScreen());
        MinecraftForge.EVENT_BUS.register(new ScreenEvents());
    }

    public void registerClientRuntimeListeners() {
        // Client-side event listeners beyond standard client setup
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
