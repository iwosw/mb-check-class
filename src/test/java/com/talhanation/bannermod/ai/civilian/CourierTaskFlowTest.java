package com.talhanation.bannermod.ai.civilian;

import com.talhanation.bannermod.shared.logistics.BannerModCourierTask;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsItemFilter;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsNodeRef;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsPriority;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsReservation;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsRoute;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourierTaskFlowTest {

    @Test
    void pickupRemainsPendingUntilReservedCountIsCarried() {
        BannerModCourierTask task = task(12);

        assertTrue(CourierTaskFlow.pickupPending(task, 0));
        assertTrue(CourierTaskFlow.pickupPending(task, 11));
        assertFalse(CourierTaskFlow.pickupPending(task, 12));
        assertFalse(CourierTaskFlow.pickupPending(task, 18));
    }

    @Test
    void targetStorageSwitchesFromSourceToDestinationAfterPickup() {
        BannerModCourierTask task = task(8);

        assertEquals(task.route().source().storageAreaId(), CourierTaskFlow.targetStorageAreaId(task, 0));
        assertEquals(task.route().source().storageAreaId(), CourierTaskFlow.targetStorageAreaId(task, 7));
        assertEquals(task.route().destination().storageAreaId(), CourierTaskFlow.targetStorageAreaId(task, 8));
    }

    @Test
    void missingPickupCountNeverDropsBelowZero() {
        BannerModCourierTask task = task(5);

        assertEquals(5, CourierTaskFlow.missingPickupCount(task, 0));
        assertEquals(2, CourierTaskFlow.missingPickupCount(task, 3));
        assertEquals(0, CourierTaskFlow.missingPickupCount(task, 5));
        assertEquals(0, CourierTaskFlow.missingPickupCount(task, 9));
    }

    private static BannerModCourierTask task(int reservedCount) {
        BannerModLogisticsRoute route = new BannerModLogisticsRoute(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                new BannerModLogisticsNodeRef(UUID.fromString("00000000-0000-0000-0000-000000000010")),
                new BannerModLogisticsNodeRef(UUID.fromString("00000000-0000-0000-0000-000000000020")),
                BannerModLogisticsItemFilter.any(),
                reservedCount,
                BannerModLogisticsPriority.NORMAL
        );
        BannerModLogisticsReservation reservation = new BannerModLogisticsReservation(
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                route.routeId(),
                UUID.fromString("00000000-0000-0000-0000-000000000003"),
                route.filter(),
                reservedCount,
                200L
        );
        return new BannerModCourierTask(route, reservation);
    }
}
