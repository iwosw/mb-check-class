package com.talhanation.bannerlord.registry.civilian;

import com.talhanation.workers.AttributeEvent;
import com.talhanation.workers.MergedRuntimeCleanupPolicy;
import com.talhanation.workers.UpdateChecker;
import com.talhanation.workers.VillagerEvents;
import com.talhanation.workers.WorkersLegacyMappings;
import com.talhanation.workers.WorkersSubsystem;
import com.talhanation.workers.client.events.ClientEvent;
import com.talhanation.workers.client.events.ScreenEvents;
import com.talhanation.workers.init.ModBlocks;
import com.talhanation.workers.init.ModEntityTypes;
import com.talhanation.workers.init.ModItems;
import com.talhanation.workers.init.ModMenuTypes;
import com.talhanation.workers.init.ModPois;
import com.talhanation.workers.init.ModProfessions;
import com.talhanation.workers.init.ModShortcuts;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

public class WorkersLifecycleRegistrar {

    private final WorkersSubsystem workersSubsystem;

    public WorkersLifecycleRegistrar(WorkersSubsystem workersSubsystem) {
        this.workersSubsystem = workersSubsystem;
    }

    public void registerCommon(IEventBus modEventBus) {
        var settlementConfigPath = com.talhanation.bannerlord.config.BannerModConfigFiles.prepareConfigPath(FMLPaths.CONFIGDIR.get(), com.talhanation.bannerlord.config.BannerModConfigFiles.Surface.SETTLEMENT);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, com.talhanation.workers.config.WorkersServerConfig.SERVER,
                settlementConfigPath.getFileName().toString());

        modEventBus.addListener(AttributeEvent::entityAttributeEvent);
        modEventBus.addListener(workersSubsystem::addCreativeTabs);
        ModBlocks.BLOCKS.register(modEventBus);
        ModPois.POIS.register(modEventBus);
        ModProfessions.PROFESSIONS.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntityTypes.ENTITY_TYPES.register(modEventBus);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            modEventBus.addListener(ClientEvent::entityRenderersEvent);
            modEventBus.addListener(workersSubsystem::clientSetup);
            modEventBus.addListener(ModShortcuts::registerBindings);
        });
    }

    public void registerRuntimeListeners() {
        MinecraftForge.EVENT_BUS.register(new VillagerEvents());
        if (MergedRuntimeCleanupPolicy.enableLegacyUpdateCheckers()) {
            MinecraftForge.EVENT_BUS.register(new UpdateChecker());
        }
        MinecraftForge.EVENT_BUS.register(new WorkersLegacyMappings());
    }

    public void registerClientRuntimeListeners() {
        MinecraftForge.EVENT_BUS.register(new ScreenEvents());
    }
}
