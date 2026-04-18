package com.talhanation.bannermod.shared.logistics;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public record BannerModLogisticsItemFilter(Set<ResourceLocation> itemIds) {

    public BannerModLogisticsItemFilter {
        Objects.requireNonNull(itemIds, "itemIds");
        itemIds = Set.copyOf(itemIds);
    }

    public static BannerModLogisticsItemFilter any() {
        return new BannerModLogisticsItemFilter(Set.of());
    }

    public static BannerModLogisticsItemFilter ofItems(ItemLike... items) {
        LinkedHashSet<ResourceLocation> itemIds = new LinkedHashSet<>();
        for (ItemLike item : items) {
            Objects.requireNonNull(item, "item");
            itemIds.add(BuiltInRegistries.ITEM.getKey(item.asItem()));
        }
        return new BannerModLogisticsItemFilter(itemIds);
    }

    public static BannerModLogisticsItemFilter ofItemIds(Collection<ResourceLocation> itemIds) {
        return new BannerModLogisticsItemFilter(new LinkedHashSet<>(itemIds));
    }

    public boolean matches(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return this.matchesItemId(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    public boolean matchesItemId(ResourceLocation itemId) {
        if (itemId == null) {
            return false;
        }
        if (this.itemIds.isEmpty()) {
            return true;
        }
        return this.itemIds.contains(itemId);
    }

    public boolean isAny() {
        return this.itemIds.isEmpty();
    }
}
