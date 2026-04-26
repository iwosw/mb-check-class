package com.talhanation.bannermod.registry.citizen;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.items.citizen.CitizenSpawnEgg;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Item registry for the unified citizen runtime. Currently hosts the single
 * {@link CitizenSpawnEgg} replacement for the legacy 18 recruit/worker spawn
 * eggs.
 */
public final class ModCitizenItems {

    private ModCitizenItems() {
    }

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BannerModMain.MOD_ID);

    public static final RegistryObject<SpawnEggItem> CITIZEN_SPAWN_EGG = ITEMS.register("citizen_spawn_egg",
            () -> new CitizenSpawnEgg(ModCitizenEntityTypes.CITIZEN, 0xC8A055, 0x4E3A22, new Item.Properties()));
}
