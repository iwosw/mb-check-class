package com.talhanation.bannerlord.registry.military;

import com.talhanation.bannerlord.bootstrap.BannerlordMain;
import com.talhanation.recruits.init.ModBlocks;
import com.talhanation.recruits.init.ModEntityTypes;
import com.talhanation.recruits.init.ModItems;
import com.talhanation.recruits.init.ModPois;
import com.talhanation.recruits.init.ModProfessions;
import com.talhanation.recruits.init.ModScreens;
import net.minecraftforge.eventbus.api.IEventBus;

public final class RecruitsRegistryRegistrar {

    private RecruitsRegistryRegistrar() {
    }

    public static void register(IEventBus modEventBus, BannerlordMain main) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModPois.POIS.register(modEventBus);
        ModProfessions.PROFESSIONS.register(modEventBus);
        ModScreens.MENU_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntityTypes.ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener(main::addCreativeTabs);
    }
}
