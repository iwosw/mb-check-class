package com.talhanation.bannermod.bootstrap;

import com.talhanation.bannermod.network.BannerModNetworkBootstrap;
import com.talhanation.bannermod.events.ClaimEvents;
import com.talhanation.bannermod.events.CommandEvents;
import com.talhanation.bannermod.events.DamageEvent;
import com.talhanation.bannermod.events.FactionEvents;
import com.talhanation.bannermod.events.PillagerEvents;
import com.talhanation.bannermod.events.RecruitEvents;
import com.talhanation.bannermod.events.VillagerEvents;
import com.talhanation.bannermod.events.WorkersVillagerEvents;
import com.talhanation.bannermod.events.WorkersCommandEvents;
import com.talhanation.bannermod.WorkersUpdateChecker;
import com.talhanation.bannermod.client.civilian.events.ScreenEvents;
import com.talhanation.bannermod.client.military.events.ClientPlayerEvents;
import com.talhanation.bannermod.client.military.events.KeyEvents;
import com.talhanation.bannermod.client.military.gui.overlay.ClaimOverlayManager;
import com.talhanation.bannermod.commands.military.PatrolSpawnCommand;
import com.talhanation.bannermod.commands.military.RecruitsAdminCommands;
import com.talhanation.bannermod.config.RecruitsClientConfig;
import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.config.WorkersServerConfig;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(BannerModMain.MOD_ID)
public class BannerModMain {
    public static final String MOD_ID = "bannermod";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static SimpleChannel SIMPLE_CHANNEL;

    // Compat booleans
    public static boolean isMusketModLoaded;
    public static boolean isSmallShipsLoaded;
    public static boolean isSmallShipsCompatible;
    public static boolean isSiegeWeaponsLoaded;
    public static boolean isEpicKnightsLoaded;
    public static boolean isCorpseLoaded;
    public static boolean isRPGZLoaded;

    public BannerModMain() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register recruits configs (explicit filenames avoid SERVER filename collision in ConfigTracker;
        // default `<modid>-<type>.toml` would make both SERVER specs resolve to `bannermod-server.toml`
        // and throw `Config conflict detected!` at ConfigTracker.trackConfig. See 21-10-PLAN.md.)
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, RecruitsClientConfig.CLIENT, "bannermod-recruits-client.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, RecruitsServerConfig.SERVER, "bannermod-recruits-server.toml");
        // Register workers config
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, WorkersServerConfig.SERVER, "bannermod-workers-server.toml");

        // Lifecycle
        modEventBus.addListener(this::setup);

        // Register military deferred registers (from bannermod.registry.military)
        com.talhanation.bannermod.registry.military.ModBlocks.BLOCKS.register(modEventBus);
        com.talhanation.bannermod.registry.military.ModPois.POIS.register(modEventBus);
        com.talhanation.bannermod.registry.military.ModProfessions.PROFESSIONS.register(modEventBus);
        com.talhanation.bannermod.registry.military.ModScreens.MENU_TYPES.register(modEventBus);
        com.talhanation.bannermod.registry.military.ModItems.ITEMS.register(modEventBus);
        com.talhanation.bannermod.registry.military.ModEntityTypes.ENTITY_TYPES.register(modEventBus);

        // Register civilian deferred registers (from bannermod.registry.civilian)
        com.talhanation.bannermod.registry.civilian.ModBlocks.BLOCKS.register(modEventBus);
        com.talhanation.bannermod.registry.civilian.ModPois.POIS.register(modEventBus);
        com.talhanation.bannermod.registry.civilian.ModProfessions.PROFESSIONS.register(modEventBus);
        com.talhanation.bannermod.registry.civilian.ModMenuTypes.MENU_TYPES.register(modEventBus);
        com.talhanation.bannermod.registry.civilian.ModItems.ITEMS.register(modEventBus);
        com.talhanation.bannermod.registry.civilian.ModEntityTypes.ENTITY_TYPES.register(modEventBus);

        // Creative tabs
        modEventBus.addListener(this::addCreativeTabs);

        // Client-side setup
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(BannerModMain.this::clientSetup);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(
                    com.talhanation.bannermod.registry.military.ModShortcuts::registerBindings);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(
                    com.talhanation.bannermod.registry.civilian.ModShortcuts::registerBindings);
        });

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        PatrolSpawnCommand.register(event.getDispatcher());
        RecruitsAdminCommands.register(event.getDispatcher());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void setup(final FMLCommonSetupEvent event) {
        // Workers runtime events
        MinecraftForge.EVENT_BUS.register(new WorkersVillagerEvents());
        MinecraftForge.EVENT_BUS.register(new WorkersCommandEvents());
        // Recruits runtime events — ports the legacy recruits/Main.java registrations into the
        // unified entrypoint. RecruitEvents.onServerStarting is what initializes the static
        // recruitsPlayerUnitManager / recruitsGroupsManager fields read by AbstractRecruitEntity.
        // Without these, right-click-to-hire (and every other recruits-side flow) trips an NPE.
        // See 21-UAT.md gap "Right-clicking a recruit opens the Hire GUI without server-side crash".
        MinecraftForge.EVENT_BUS.register(new RecruitEvents());
        MinecraftForge.EVENT_BUS.register(new ClaimEvents());
        MinecraftForge.EVENT_BUS.register(new FactionEvents());
        MinecraftForge.EVENT_BUS.register(new CommandEvents());
        MinecraftForge.EVENT_BUS.register(new DamageEvent());
        MinecraftForge.EVENT_BUS.register(new PillagerEvents());
        MinecraftForge.EVENT_BUS.register(new VillagerEvents());
        MinecraftForge.EVENT_BUS.register(this);
        if (MergedRuntimeCleanupPolicy.enableLegacyUpdateCheckers()) {
            MinecraftForge.EVENT_BUS.register(new WorkersUpdateChecker());
        }

        // Create shared channel; recruits at [0..N), workers at [N..N+M)
        SIMPLE_CHANNEL = BannerModNetworkBootstrap.createSharedChannel();

        // Sync compat flags
        isMusketModLoaded = ModList.get().isLoaded("musketmod");
        isSmallShipsLoaded = ModList.get().isLoaded("smallships");
        isSmallShipsCompatible = isSmallShipsLoaded;
        isSiegeWeaponsLoaded = ModList.get().isLoaded("siegeweapons");
        isEpicKnightsLoaded = ModList.get().isLoaded("epicknights");
        isCorpseLoaded = ModList.get().isLoaded("corpse");
        isRPGZLoaded = ModList.get().isLoaded("rpgz");
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void clientSetup(FMLClientSetupEvent event) {
        // Military menus
        event.enqueueWork(com.talhanation.bannermod.registry.military.ModScreens::registerMenus);
        // Civilian menus
        event.enqueueWork(com.talhanation.bannermod.registry.civilian.ModMenuTypes::registerMenus);
        event.enqueueWork(com.talhanation.bannermod.persistence.civilian.StructureManager::copyDefaultStructuresIfMissing);
        // Worker command screen
        com.talhanation.bannermod.client.military.events.CommandCategoryManager.register(
                new com.talhanation.bannermod.client.civilian.gui.WorkerCommandScreen());
        MinecraftForge.EVENT_BUS.register(new ScreenEvents());
        // Recruits client-side event handlers — same Phase-21 consolidation defect class
        // as 21-11 (recruits/Main.java was deprecated to a no-op shim and these registrations
        // were not ported into the unified entrypoint). KeyEvents owns the R/U/M hotkey
        // listener that opens Command/Faction/Map screens; ClientPlayerEvents owns
        // client-tick and world-load hooks; ClaimOverlayManager renders the claim HUD.
        // See 21-UAT.md gap "Recruits hotkey screens (Command/Faction/Map) and the claim overlay open in dev client".
        MinecraftForge.EVENT_BUS.register(new KeyEvents());
        MinecraftForge.EVENT_BUS.register(new ClientPlayerEvents());
        MinecraftForge.EVENT_BUS.register(new ClaimOverlayManager());
    }

    private void addCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.SPAWN_EGGS)) {
            // Civilian spawn eggs
            event.accept(com.talhanation.bannermod.registry.civilian.ModItems.FARMER_SPAWN_EGG.get());
            event.accept(com.talhanation.bannermod.registry.civilian.ModItems.LUMBERJACK_SPAWN_EGG.get());
            event.accept(com.talhanation.bannermod.registry.civilian.ModItems.MINER_SPAWN_EGG.get());
            event.accept(com.talhanation.bannermod.registry.civilian.ModItems.MERCHANT_SPAWN_EGG.get());
            event.accept(com.talhanation.bannermod.registry.civilian.ModItems.BUILDER_SPAWN_EGG.get());
            event.accept(com.talhanation.bannermod.registry.civilian.ModItems.FISHERMAN_SPAWN_EGG.get());
            event.accept(com.talhanation.bannermod.registry.civilian.ModItems.ANIMAL_FARMER_SPAWN_EGG.get());
        }
    }
}
