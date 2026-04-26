package com.talhanation.bannermod.ai.civilian;

import com.talhanation.bannermod.shared.logistics.BannerModLogisticsItemFilter;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransportContainerExchangeTest {

    @Test
    void blankResourceHintProducesAnyFilter() {
        assertTrue(TransportContainerExchange.filterFromResourceHint(null).isAny());
        assertTrue(TransportContainerExchange.filterFromResourceHint("").isAny());
        assertTrue(TransportContainerExchange.filterFromResourceHint("   ").isAny());
    }

    @Test
    void singleResourceHintMatchesOnlyThatItemId() {
        BannerModLogisticsItemFilter filter = TransportContainerExchange.filterFromResourceHint("minecraft:wheat");

        assertTrue(filter.matchesItemId(ResourceLocation.tryParse("minecraft:wheat")));
        assertFalse(filter.matchesItemId(ResourceLocation.tryParse("minecraft:bread")));
    }

    @Test
    void commaSeparatedResourceHintMatchesAnyListedItemId() {
        BannerModLogisticsItemFilter filter = TransportContainerExchange.filterFromResourceHint("minecraft:wheat, minecraft:bread");

        assertTrue(filter.matchesItemId(ResourceLocation.tryParse("minecraft:wheat")));
        assertTrue(filter.matchesItemId(ResourceLocation.tryParse("minecraft:bread")));
        assertFalse(filter.matchesItemId(ResourceLocation.tryParse("minecraft:cobblestone")));
    }

    @Test
    void unparsableTokensCollapseToAnyFilter() {
        BannerModLogisticsItemFilter filter = TransportContainerExchange.filterFromResourceHint("not a resource location");

        assertTrue(filter.isAny());
    }

    @Test
    void whitespaceAroundTokensIsTrimmed() {
        BannerModLogisticsItemFilter filter = TransportContainerExchange.filterFromResourceHint("  minecraft:bread ,, minecraft:wheat  ");

        assertTrue(filter.matchesItemId(ResourceLocation.tryParse("minecraft:bread")));
        assertTrue(filter.matchesItemId(ResourceLocation.tryParse("minecraft:wheat")));
    }
}
