package com.talhanation.bannerlord.bootstrap;

import com.talhanation.bannerlord.registry.civilian.WorkersLifecycleRegistrar;
import com.talhanation.bannerlord.registry.military.RecruitsRegistryRegistrar;
import com.talhanation.recruits.ClaimEvents;
import com.talhanation.recruits.CommandEvents;
import com.talhanation.recruits.DamageEvent;
import com.talhanation.recruits.DebugEvents;
import com.talhanation.recruits.FactionEvents;
import com.talhanation.recruits.PillagerEvents;
import com.talhanation.recruits.RecruitEvents;
import com.talhanation.recruits.UpdateChecker;
import com.talhanation.recruits.VillagerEvents;
import com.talhanation.recruits.client.events.ClientPlayerEvents;
import com.talhanation.recruits.client.events.KeyEvents;
import com.talhanation.recruits.client.gui.overlay.ClaimOverlayManager;
import com.talhanation.recruits.config.RecruitsClientConfig;
import com.talhanation.recruits.config.RecruitsServerConfig;
import com.talhanation.recruits.migration.NetworkBootstrapSeams;
import com.talhanation.workers.MergedRuntimeCleanupPolicy;
import com.talhanation.workers.WorkersSubsystem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

public class BannerlordLifecycleRegistrar implements NetworkBootstrapSeams.LifecycleBinder {

    private final BannerlordMain main;
    private final WorkersSubsystem workersSubsystem;
    private final WorkersLifecycleRegistrar workersLifecycleRegistrar;

    public BannerlordLifecycleRegistrar(BannerlordMain main, WorkersSubsystem workersSubsystem) {
        this.main = main;
        this.workersSubsystem = workersSubsystem;
        this.workersLifecycleRegistrar = workersSubsystem.lifecycleRegistrar();
    }

    @Override
    public void registerCommon(IEventBus modEventBus) {
        var configDir = FMLPaths.CONFIGDIR.get();
        var militaryConfigPath = com.talhanation.bannerlord.config.BannerModConfigFiles.prepareConfigPath(configDir, com.talhanation.bannerlord.config.BannerModConfigFiles.Surface.MILITARY);
        var clientConfigPath = com.talhanation.bannerlord.config.BannerModConfigFiles.prepareConfigPath(configDir, com.talhanation.bannerlord.config.BannerModConfigFiles.Surface.CLIENT);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, RecruitsServerConfig.SERVER, militaryConfigPath.getFileName().toString());
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, RecruitsClientConfig.CLIENT, clientConfigPath.getFileName().toString());
        RecruitsClientConfig.loadConfig(RecruitsClientConfig.CLIENT, clientConfigPath);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(main::clientSetup);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(com.talhanation.recruits.init.ModShortcuts::registerBindings);
        });

        modEventBus.addListener(main::setup);
        RecruitsRegistryRegistrar.register(modEventBus, main);
        workersLifecycleRegistrar.registerCommon(modEventBus);
        MinecraftForge.EVENT_BUS.register(main);
    }

    public void registerRuntimeListeners() {
        MinecraftForge.EVENT_BUS.register(new RecruitEvents());
        MinecraftForge.EVENT_BUS.register(new VillagerEvents());
        MinecraftForge.EVENT_BUS.register(new PillagerEvents());
        MinecraftForge.EVENT_BUS.register(new CommandEvents());
        MinecraftForge.EVENT_BUS.register(new DebugEvents());
        MinecraftForge.EVENT_BUS.register(new FactionEvents());
        MinecraftForge.EVENT_BUS.register(new DamageEvent());
        if (MergedRuntimeCleanupPolicy.enableLegacyUpdateCheckers()) {
            MinecraftForge.EVENT_BUS.register(new UpdateChecker());
        }
        MinecraftForge.EVENT_BUS.register(new ClaimEvents());
        MinecraftForge.EVENT_BUS.register(main);
        workersSubsystem.registerRuntimeListeners();
    }

    public void registerClientRuntimeListeners() {
        MinecraftForge.EVENT_BUS.register(new KeyEvents());
        MinecraftForge.EVENT_BUS.register(new ClientPlayerEvents());
        MinecraftForge.EVENT_BUS.register(new ClaimOverlayManager());
        workersSubsystem.registerClientRuntimeListeners();
    }
}
