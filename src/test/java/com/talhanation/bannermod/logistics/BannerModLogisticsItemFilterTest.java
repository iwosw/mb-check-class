package com.talhanation.bannermod.logistics;

import com.talhanation.bannermod.shared.logistics.BannerModLogisticsItemFilter;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BannerModLogisticsItemFilterTest {

    @Test
    void anyFilterMatchesAnyItemId() {
        BannerModLogisticsItemFilter filter = BannerModLogisticsItemFilter.any();

        assertTrue(filter.isAny());
        assertTrue(filter.matchesItemId(ResourceLocation.tryParse("minecraft:oak_planks")));
        assertFalse(filter.matchesItemId(null));
    }

    @Test
    void itemFilterMatchesOnlyConfiguredItems() {
        ResourceLocation oakPlanks = ResourceLocation.tryParse("minecraft:oak_planks");
        ResourceLocation bread = ResourceLocation.tryParse("minecraft:bread");
        ResourceLocation cobblestone = ResourceLocation.tryParse("minecraft:cobblestone");
        BannerModLogisticsItemFilter filter = BannerModLogisticsItemFilter.ofItemIds(java.util.List.of(oakPlanks, bread));

        assertTrue(filter.matchesItemId(oakPlanks));
        assertTrue(filter.matchesItemId(bread));
        assertFalse(filter.matchesItemId(cobblestone));
    }
}
