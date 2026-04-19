package com.talhanation.bannermod.logistics;

import com.talhanation.bannermod.shared.logistics.BannerModLogisticsAuthoringState;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsPriority;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BannerModLogisticsAuthoringStateTest {

    @Test
    void parseAcceptsDisabledRouteWithBlankDestination() {
        BannerModLogisticsAuthoringState state = BannerModLogisticsAuthoringState.parse("", "", "", "");

        assertNull(state.destinationStorageAreaId());
        assertEquals("", state.filterText());
        assertEquals(16, state.requestedCount());
        assertEquals(BannerModLogisticsPriority.NORMAL, state.priority());
    }

    @Test
    void parseCanonicalizesFilterAndPriority() {
        BannerModLogisticsAuthoringState state = BannerModLogisticsAuthoringState.parse(
                "00000000-0000-0000-0000-000000000010",
                " minecraft:bread , minecraft:bread, minecraft:oak_planks ",
                "32",
                "low"
        );

        assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000010"), state.destinationStorageAreaId());
        assertEquals("minecraft:bread,minecraft:oak_planks", state.filterText());
        assertEquals(32, state.requestedCount());
        assertEquals(BannerModLogisticsPriority.LOW, state.priority());
    }

    @Test
    void parseRejectsInvalidPriority() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> BannerModLogisticsAuthoringState.parse("", "", "16", "urgent"));

        assertEquals("Route priority must be HIGH, NORMAL, or LOW.", exception.getMessage());
    }
}
