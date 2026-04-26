package com.talhanation.bannermod.registry.war;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModWarItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, BannerModMain.MOD_ID);

    public static final RegistryObject<BlockItem> SIEGE_STANDARD = ITEMS.register("siege_standard",
            () -> new BlockItem(ModWarBlocks.SIEGE_STANDARD.get(), new Item.Properties().stacksTo(16)));
}
