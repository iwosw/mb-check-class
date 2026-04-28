package com.talhanation.bannermod.registry.war;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.war.runtime.SiegeStandardBlockItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModWarItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, BannerModMain.MOD_ID);

    public static final DeferredHolder<Item, BlockItem> SIEGE_STANDARD = ITEMS.register("siege_standard",
            () -> new SiegeStandardBlockItem(ModWarBlocks.SIEGE_STANDARD.get(), new Item.Properties().stacksTo(16)));
}
