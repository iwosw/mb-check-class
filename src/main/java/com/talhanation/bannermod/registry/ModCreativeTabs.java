package com.talhanation.bannermod.registry;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BannerModMain.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register(
            "main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.bannermod.main"))
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .icon(() -> new ItemStack(com.talhanation.bannermod.registry.civilian.ModItems.BANNERMOD_ALMANAC.get()))
                    .displayItems((parameters, output) -> {
                        acceptAll(output, com.talhanation.bannermod.registry.civilian.ModItems.ITEMS.getEntries());
                        acceptAll(output, com.talhanation.bannermod.registry.citizen.ModCitizenItems.ITEMS.getEntries());
                        acceptAll(output, com.talhanation.bannermod.registry.military.ModItems.ITEMS.getEntries());
                        acceptAll(output, com.talhanation.bannermod.registry.war.ModWarItems.ITEMS.getEntries());
                    })
                    .build()
    );

    private ModCreativeTabs() {
    }

    private static void acceptAll(CreativeModeTab.Output output,
                                  Iterable<? extends DeferredHolder<Item, ? extends Item>> entries) {
        if (output == null || entries == null) {
            return;
        }
        for (DeferredHolder<Item, ? extends Item> entry : entries) {
            if (entry != null) {
                output.accept(entry.get());
            }
        }
    }
}
