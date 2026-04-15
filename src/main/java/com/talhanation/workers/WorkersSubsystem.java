package com.talhanation.workers;

import com.talhanation.bannerlord.network.civilian.WorkersNetworkRegistrar;
import com.talhanation.bannerlord.registry.civilian.WorkersLifecycleRegistrar;
import com.talhanation.bannerlord.client.shared.events.CommandCategoryManager;
import com.talhanation.bannerlord.client.civilian.gui.WorkerCommandScreen;
import com.talhanation.workers.init.ModItems;
import com.talhanation.workers.init.ModMenuTypes;
import com.talhanation.bannerlord.persistence.civilian.StructureManager;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.network.simple.SimpleChannel;

public class WorkersSubsystem {
    private final WorkersLifecycleRegistrar lifecycleRegistrar;
    private final WorkersNetworkRegistrar networkRegistrar;

    public WorkersSubsystem() {
        lifecycleRegistrar = new WorkersLifecycleRegistrar(this);
        networkRegistrar = new WorkersNetworkRegistrar();
    }

    public void registerCommon(net.minecraftforge.eventbus.api.IEventBus modEventBus) {
        lifecycleRegistrar.registerCommon(modEventBus);
    }

    public void registerRuntimeListeners() {
        lifecycleRegistrar.registerRuntimeListeners();
    }

    public void registerNetwork(SimpleChannel channel) {
        WorkersRuntime.bindChannel(channel);
        networkRegistrar.registerAll(channel, WorkersRuntime.networkIdOffset());
    }

    public int networkMessageCount() {
        return networkRegistrar.messageCount();
    }

    @OnlyIn(Dist.CLIENT)
    public void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ModMenuTypes::registerMenus);
        event.enqueueWork(StructureManager::copyDefaultStructuresIfMissing);
        CommandCategoryManager.register(new WorkerCommandScreen());
    }

    public void registerClientRuntimeListeners() {
        lifecycleRegistrar.registerClientRuntimeListeners();
    }

    public WorkersLifecycleRegistrar lifecycleRegistrar() {
        return lifecycleRegistrar;
    }

    public void addCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.SPAWN_EGGS)) {
            event.accept(ModItems.FARMER_SPAWN_EGG.get());
            event.accept(ModItems.LUMBERJACK_SPAWN_EGG.get());
            event.accept(ModItems.MINER_SPAWN_EGG.get());
            event.accept(ModItems.MERCHANT_SPAWN_EGG.get());
            event.accept(ModItems.BUILDER_SPAWN_EGG.get());
            event.accept(ModItems.FISHERMAN_SPAWN_EGG.get());
            event.accept(ModItems.ANIMAL_FARMER_SPAWN_EGG.get());
        }
    }
}
