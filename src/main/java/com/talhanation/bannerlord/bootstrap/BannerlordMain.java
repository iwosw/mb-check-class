package com.talhanation.bannerlord.bootstrap;

import com.talhanation.bannerlord.network.BannerlordNetworkBootstrap;
import com.talhanation.recruits.Main;
import com.talhanation.recruits.client.events.CommandCategoryManager;
import com.talhanation.recruits.client.gui.commandscreen.CombatCategory;
import com.talhanation.recruits.client.gui.commandscreen.MovementCategory;
import com.talhanation.recruits.client.gui.commandscreen.OtherCategory;
import com.talhanation.recruits.commands.PatrolSpawnCommand;
import com.talhanation.recruits.commands.RecruitsAdminCommands;
import com.talhanation.recruits.init.ModBlocks;
import com.talhanation.recruits.init.ModItems;
import com.talhanation.recruits.init.ModScreens;
import com.talhanation.workers.WorkersSubsystem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(BannerlordMain.MOD_ID)
public class BannerlordMain {
    public static final String MOD_ID = "bannermod";
    public static SimpleChannel SIMPLE_CHANNEL;
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static boolean isMusketModLoaded;
    public static boolean isSmallShipsLoaded;
    public static boolean isSmallShipsCompatible;
    public static boolean isSiegeWeaponsLoaded;
    public static boolean isEpicKnightsLoaded;
    public static boolean isCorpseLoaded;
    public static boolean isRPGZLoaded;

    private final BannerlordLifecycleRegistrar lifecycleRegistrar;
    private final BannerlordNetworkBootstrap networkBootstrap;
    private final WorkersSubsystem workersSubsystem;

    public BannerlordMain() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        workersSubsystem = new WorkersSubsystem();
        lifecycleRegistrar = new BannerlordLifecycleRegistrar(this, workersSubsystem);
        networkBootstrap = new BannerlordNetworkBootstrap();
        lifecycleRegistrar.registerCommon(modEventBus);
        Main.syncFromBannerlord();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        PatrolSpawnCommand.register(event.getDispatcher());
        RecruitsAdminCommands.register(event.getDispatcher());
    }

    public void setup(final FMLCommonSetupEvent event) {
        lifecycleRegistrar.registerRuntimeListeners();
        SIMPLE_CHANNEL = networkBootstrap.createSharedChannel();
        syncCompatibilityFlags();
        Main.syncFromBannerlord();
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ModScreens::registerMenus);
        lifecycleRegistrar.registerClientRuntimeListeners();

        CommandCategoryManager.register(new MovementCategory(), -2);
        CommandCategoryManager.register(new CombatCategory(), -3);
        CommandCategoryManager.register(new OtherCategory(), -1);
    }

    public void addCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.SPAWN_EGGS)) {
            event.accept(ModItems.BOWMAN_SPAWN_EGG.get());
            event.accept(ModItems.RECRUIT_SHIELD_SPAWN_EGG.get());
            event.accept(ModItems.RECRUIT_SPAWN_EGG.get());
            event.accept(ModItems.NOMAD_SPAWN_EGG.get());
            event.accept(ModItems.HORSEMAN_SPAWN_EGG.get());
            event.accept(ModItems.CROSSBOWMAN_SPAWN_EGG.get());
            event.accept(ModItems.VILLAGER_NOBLE_SPAWN_EGG.get());
        }

        if (event.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)) {
            event.accept(ModBlocks.RECRUIT_BLOCK.get());
            event.accept(ModBlocks.BOWMAN_BLOCK.get());
            event.accept(ModBlocks.RECRUIT_SHIELD_BLOCK.get());
            event.accept(ModBlocks.CROSSBOWMAN_BLOCK.get());
            event.accept(ModBlocks.HORSEMAN_BLOCK.get());
            event.accept(ModBlocks.NOMAD_BLOCK.get());
        }
    }

    private static void syncCompatibilityFlags() {
        isMusketModLoaded = ModList.get().isLoaded("musketmod");
        isSmallShipsLoaded = ModList.get().isLoaded("smallships");
        isSiegeWeaponsLoaded = ModList.get().isLoaded("siegeweapons");
        isRPGZLoaded = ModList.get().isLoaded("rpgz");
        isCorpseLoaded = ModList.get().isLoaded("corpse");
        isEpicKnightsLoaded = ModList.get().isLoaded("magistuarmory");

        isSmallShipsCompatible = false;
        if (isSmallShipsLoaded) {
            String smallshipsversion = ModList.get().getModFileById("smallships").versionString();
            isSmallShipsCompatible = smallshipsversion.contains("2.0.0-b1.3") || smallshipsversion.contains("2.0.0-b1.4");
        }
    }
}
