package com.talhanation.bannermod.shared.logistics;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BannerModSeaTradeSummaryTest {

    private static final ResourceLocation OAK_PLANKS = new ResourceLocation("minecraft", "oak_planks");
    private static final ResourceLocation IRON_INGOT = new ResourceLocation("minecraft", "iron_ingot");
    private static final ResourceLocation WHEAT = new ResourceLocation("minecraft", "wheat");

    private static BannerModSeaTradeEntrypoint entry(BannerModSeaTradeDirection direction,
                                                     ResourceLocation itemId,
                                                     int count) {
        return new BannerModSeaTradeEntrypoint(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                direction,
                BannerModLogisticsItemFilter.ofItemIds(List.of(itemId)),
                count,
                BannerModLogisticsPriority.NORMAL
        );
    }

    private static BannerModSeaTradeEntrypoint anyFilterEntry(BannerModSeaTradeDirection direction, int count) {
        return new BannerModSeaTradeEntrypoint(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                direction,
                BannerModLogisticsItemFilter.any(),
                count,
                BannerModLogisticsPriority.NORMAL
        );
    }

    @Test
    void emptyEntrypointsFlagsNoPort() {
        BannerModSeaTradeSummary.Summary summary = BannerModSeaTradeSummary.summarise(List.of());

        assertTrue(summary.exportableByItem().isEmpty());
        assertTrue(summary.importableByItem().isEmpty());
        assertTrue(summary.bottlenecks().contains(BannerModSeaTradeSummary.BOTTLENECK_NO_PORT));
    }

    @Test
    void nullEntrypointsAlsoFlagsNoPort() {
        BannerModSeaTradeSummary.Summary summary = BannerModSeaTradeSummary.summarise(null);

        assertTrue(summary.bottlenecks().contains(BannerModSeaTradeSummary.BOTTLENECK_NO_PORT));
    }

    @Test
    void exportAndImportRoutesAggregatePerResource() {
        BannerModSeaTradeSummary.Summary summary = BannerModSeaTradeSummary.summarise(List.of(
                entry(BannerModSeaTradeDirection.EXPORT, OAK_PLANKS, 8),
                entry(BannerModSeaTradeDirection.EXPORT, OAK_PLANKS, 16),
                entry(BannerModSeaTradeDirection.IMPORT, IRON_INGOT, 12)
        ));

        assertEquals(24, BannerModSeaTradeSummary.totalExportableCount(summary, OAK_PLANKS));
        assertEquals(12, BannerModSeaTradeSummary.totalImportableCount(summary, IRON_INGOT));
        assertEquals(0, BannerModSeaTradeSummary.totalExportableCount(summary, WHEAT));
        assertTrue(summary.bottlenecks().isEmpty());
    }

    @Test
    void onlyExportsFlagsTheBottleneck() {
        BannerModSeaTradeSummary.Summary summary = BannerModSeaTradeSummary.summarise(List.of(
                entry(BannerModSeaTradeDirection.EXPORT, OAK_PLANKS, 8)
        ));

        assertTrue(summary.bottlenecks().contains(BannerModSeaTradeSummary.BOTTLENECK_ONLY_EXPORTS));
        assertFalse(summary.bottlenecks().contains(BannerModSeaTradeSummary.BOTTLENECK_ONLY_IMPORTS));
    }

    @Test
    void onlyImportsFlagsTheBottleneck() {
        BannerModSeaTradeSummary.Summary summary = BannerModSeaTradeSummary.summarise(List.of(
                entry(BannerModSeaTradeDirection.IMPORT, OAK_PLANKS, 8)
        ));

        assertTrue(summary.bottlenecks().contains(BannerModSeaTradeSummary.BOTTLENECK_ONLY_IMPORTS));
    }

    @Test
    void unfilteredRouteIsFlaggedSoUiCanRecommendTighteningTheFilter() {
        BannerModSeaTradeSummary.Summary summary = BannerModSeaTradeSummary.summarise(List.of(
                entry(BannerModSeaTradeDirection.EXPORT, OAK_PLANKS, 8),
                entry(BannerModSeaTradeDirection.IMPORT, IRON_INGOT, 12),
                anyFilterEntry(BannerModSeaTradeDirection.IMPORT, 999)
        ));

        // Both directions present so neither directional bottleneck fires.
        assertFalse(summary.bottlenecks().contains(BannerModSeaTradeSummary.BOTTLENECK_ONLY_EXPORTS));
        assertFalse(summary.bottlenecks().contains(BannerModSeaTradeSummary.BOTTLENECK_ONLY_IMPORTS));
        assertTrue(summary.bottlenecks().contains(BannerModSeaTradeSummary.BOTTLENECK_UNFILTERED_ROUTE));

        // Unfiltered route does not contribute to per-item totals.
        assertEquals(12, BannerModSeaTradeSummary.totalImportableCount(summary, IRON_INGOT));
    }

    @Test
    void totalCountsHandleNullSummaryAndNullItemDefensively() {
        assertEquals(0, BannerModSeaTradeSummary.totalExportableCount(null, OAK_PLANKS));
        BannerModSeaTradeSummary.Summary summary = BannerModSeaTradeSummary.summarise(List.of(
                entry(BannerModSeaTradeDirection.EXPORT, OAK_PLANKS, 8)
        ));
        assertEquals(0, BannerModSeaTradeSummary.totalExportableCount(summary, null));
    }

    @Test
    void multiItemFilterDistributesTheRouteCountToEveryListedItem() {
        BannerModSeaTradeEntrypoint multi = new BannerModSeaTradeEntrypoint(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BannerModSeaTradeDirection.EXPORT,
                BannerModLogisticsItemFilter.ofItemIds(List.of(OAK_PLANKS, WHEAT)),
                10,
                BannerModLogisticsPriority.NORMAL
        );

        BannerModSeaTradeSummary.Summary summary = BannerModSeaTradeSummary.summarise(List.of(
                multi,
                entry(BannerModSeaTradeDirection.IMPORT, IRON_INGOT, 5)
        ));

        // Each listed item carries the route's total cap; the upcoming production loop is
        // expected to clamp on actual stock when it reads these numbers.
        assertEquals(10, BannerModSeaTradeSummary.totalExportableCount(summary, OAK_PLANKS));
        assertEquals(10, BannerModSeaTradeSummary.totalExportableCount(summary, WHEAT));
        assertEquals(5, BannerModSeaTradeSummary.totalImportableCount(summary, IRON_INGOT));
    }
}
