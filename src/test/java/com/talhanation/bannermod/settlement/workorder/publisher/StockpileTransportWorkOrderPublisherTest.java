package com.talhanation.bannermod.settlement.workorder.publisher;

import com.talhanation.bannermod.shared.logistics.BannerModLogisticsPriority;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StockpileTransportWorkOrderPublisherTest {

    private static final int BASE_PRIORITY = 55;

    @Test
    void blankFilterTextResolvesToNullResourceHint() {
        assertNull(StockpileTransportWorkOrderPublisher.resourceHintFromFilter(null));
        assertNull(StockpileTransportWorkOrderPublisher.resourceHintFromFilter(""));
        assertNull(StockpileTransportWorkOrderPublisher.resourceHintFromFilter("   "));
    }

    @Test
    void filterTextIsTrimmedButOtherwisePreserved() {
        assertEquals(
                "minecraft:wheat,minecraft:bread",
                StockpileTransportWorkOrderPublisher.resourceHintFromFilter("  minecraft:wheat,minecraft:bread  ")
        );
    }

    @Test
    void priorityMappingHonoursLogisticsPriorityEnum() {
        assertEquals(BASE_PRIORITY + 20, StockpileTransportWorkOrderPublisher.priorityFor(BannerModLogisticsPriority.HIGH));
        assertEquals(BASE_PRIORITY, StockpileTransportWorkOrderPublisher.priorityFor(BannerModLogisticsPriority.NORMAL));
        assertEquals(BASE_PRIORITY - 20, StockpileTransportWorkOrderPublisher.priorityFor(BannerModLogisticsPriority.LOW));
        assertEquals(BASE_PRIORITY, StockpileTransportWorkOrderPublisher.priorityFor(null));
    }

    @Test
    void priorityTokenMappingIsCaseInsensitiveAndForgiving() {
        assertEquals(BASE_PRIORITY + 20, StockpileTransportWorkOrderPublisher.priorityForToken("high"));
        assertEquals(BASE_PRIORITY + 20, StockpileTransportWorkOrderPublisher.priorityForToken(" HIGH "));
        assertEquals(BASE_PRIORITY, StockpileTransportWorkOrderPublisher.priorityForToken("normal"));
        assertEquals(BASE_PRIORITY - 20, StockpileTransportWorkOrderPublisher.priorityForToken("LOW"));
        assertEquals(BASE_PRIORITY, StockpileTransportWorkOrderPublisher.priorityForToken("garbage"));
        assertEquals(BASE_PRIORITY, StockpileTransportWorkOrderPublisher.priorityForToken(null));
    }
}
