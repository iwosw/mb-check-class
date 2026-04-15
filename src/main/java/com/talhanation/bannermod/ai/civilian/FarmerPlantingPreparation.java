package com.talhanation.bannermod.ai.civilian;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraftforge.common.IPlantable;

import java.util.List;

public class FarmerPlantingPreparation {

    public enum SeedSource {
        CONFIGURED,
        INVENTORY,
        MISSING
    }

    public static ItemStack resolveSeedTemplate(ItemStack configuredSeed, List<ItemStack> inventory) {
        Item resolvedItem = resolveSeedItem(configuredSeed.isEmpty() ? null : configuredSeed.getItem(), inventory.stream().map(ItemStack::getItem).toList());
        if (resolvedItem == null) {
            return ItemStack.EMPTY;
        }

        if (!configuredSeed.isEmpty() && configuredSeed.getItem() == resolvedItem) {
            return configuredSeed.copyWithCount(1);
        }

        for (ItemStack stack : inventory) {
            if (!stack.isEmpty() && stack.getItem() == resolvedItem) {
                return stack.copyWithCount(1);
            }
        }

        return ItemStack.EMPTY;
    }

    public static boolean isSupportedSeed(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        return isSupportedSeedItem(stack.getItem());
    }

    public static Item resolveSeedItem(Item configuredSeedItem, List<Item> inventoryItems) {
        return switch (resolveSeedSource(isSupportedSeedItem(configuredSeedItem), inventoryItems.stream().anyMatch(FarmerPlantingPreparation::isSupportedSeedItem))) {
            case CONFIGURED -> configuredSeedItem;
            case INVENTORY -> inventoryItems.stream().filter(FarmerPlantingPreparation::isSupportedSeedItem).findFirst().orElse(null);
            case MISSING -> null;
        };
    }

    public static SeedSource resolveSeedSource(boolean hasConfiguredSeed, boolean hasInventorySeed) {
        if (hasConfiguredSeed) {
            return SeedSource.CONFIGURED;
        }

        if (hasInventorySeed) {
            return SeedSource.INVENTORY;
        }

        return SeedSource.MISSING;
    }

    public static boolean isSupportedSeedItem(Item item) {
        if (item == null) {
            return false;
        }

        if (item instanceof BlockItem blockItem) {
            return blockItem.getBlock() instanceof CropBlock
                    || blockItem.getBlock() instanceof StemBlock
                    || blockItem.getBlock() instanceof SweetBerryBushBlock;
        }

        return item instanceof IPlantable;
    }

    private FarmerPlantingPreparation() {
    }
}
