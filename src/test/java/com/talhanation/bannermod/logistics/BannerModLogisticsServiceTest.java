package com.talhanation.bannermod.logistics;

import com.talhanation.bannermod.shared.logistics.BannerModCourierTask;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsItemFilter;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsNodeRef;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsPriority;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsRoute;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsService;
import com.talhanation.bannermod.shared.logistics.BannerModSeaTradeDirection;
import com.talhanation.bannermod.shared.logistics.BannerModSeaTradeEntrypoint;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BannerModLogisticsServiceTest {

    @Test
    void claimNextTaskPrefersHigherPriorityThenStableRouteIdOrdering() {
        BannerModLogisticsService service = new BannerModLogisticsService();
        UUID workerId = UUID.randomUUID();

        BannerModLogisticsRoute low = route("00000000-0000-0000-0000-000000000030", BannerModLogisticsPriority.LOW);
        BannerModLogisticsRoute highLater = route("00000000-0000-0000-0000-000000000020", BannerModLogisticsPriority.HIGH);
        BannerModLogisticsRoute highEarlier = route("00000000-0000-0000-0000-000000000010", BannerModLogisticsPriority.HIGH);

        Optional<BannerModCourierTask> claimed = service.claimNextTask(
                workerId,
                List.of(low, highLater, highEarlier),
                route -> true,
                40L,
                20L
        );

        assertTrue(claimed.isPresent());
        assertEquals(highEarlier.routeId(), claimed.get().route().routeId());
        assertEquals(highEarlier.requestedCount(), claimed.get().reservation().reservedCount());
        assertEquals(60L, claimed.get().reservation().expiresAtGameTime());
    }

    @Test
    void cleanupExpiredReservationsMakesRouteClaimableAgain() {
        BannerModLogisticsService service = new BannerModLogisticsService();
        BannerModLogisticsRoute route = route("00000000-0000-0000-0000-000000000100", BannerModLogisticsPriority.NORMAL);

        Optional<BannerModCourierTask> firstClaim = service.claimNextTask(
                UUID.randomUUID(),
                List.of(route),
                candidate -> true,
                10L,
                5L
        );

        assertTrue(firstClaim.isPresent());
        assertFalse(service.claimNextTask(UUID.randomUUID(), List.of(route), candidate -> true, 12L, 5L).isPresent());
        assertEquals(1, service.cleanupExpiredReservations(15L));

        Optional<BannerModCourierTask> secondClaim = service.claimNextTask(
                UUID.randomUUID(),
                List.of(route),
                candidate -> true,
                15L,
                5L
        );

        assertTrue(secondClaim.isPresent());
        assertNotNull(secondClaim.get().reservation());
    }

    @Test
    void claimNextTaskSkipsRoutesRejectedByEligibilityFilter() {
        BannerModLogisticsService service = new BannerModLogisticsService();
        UUID workerId = UUID.randomUUID();
        BannerModLogisticsRoute rejected = route("00000000-0000-0000-0000-000000000110", BannerModLogisticsPriority.HIGH);
        BannerModLogisticsRoute eligible = route("00000000-0000-0000-0000-000000000120", BannerModLogisticsPriority.NORMAL);

        BannerModCourierTask claimed = service.claimNextTask(
                workerId,
                List.of(rejected, eligible),
                candidate -> candidate.routeId().equals(eligible.routeId()),
                25L,
                10L
        ).orElseThrow();

        assertEquals(eligible.routeId(), claimed.route().routeId());
        assertEquals(eligible.routeId(), claimed.reservation().routeId());

        BannerModCourierTask rejectedClaim = service.claimNextTask(
                UUID.randomUUID(),
                List.of(rejected),
                candidate -> true,
                25L,
                10L
        ).orElseThrow();

        assertEquals(rejected.routeId(), rejectedClaim.route().routeId());
        assertEquals(rejected.routeId(), rejectedClaim.reservation().routeId());
    }

    @Test
    void claimNextTaskReturnsExistingWorkerReservationBeforeCreatingAnother() {
        BannerModLogisticsService service = new BannerModLogisticsService();
        UUID workerId = UUID.randomUUID();
        BannerModLogisticsRoute route = route("00000000-0000-0000-0000-000000000200", BannerModLogisticsPriority.NORMAL);

        BannerModCourierTask firstClaim = service.claimNextTask(workerId, List.of(route), candidate -> true, 0L, 20L).orElseThrow();
        BannerModCourierTask secondClaim = service.claimNextTask(workerId, List.of(route), candidate -> true, 5L, 20L).orElseThrow();

        assertEquals(firstClaim.reservation().reservationId(), secondClaim.reservation().reservationId());
        assertEquals(firstClaim.route().routeId(), secondClaim.route().routeId());
    }

    @Test
    void listSeaTradeEntrypointsProjectsPortBackedRoutesInPriorityOrder() {
        BannerModLogisticsService service = new BannerModLogisticsService();
        UUID inlandA = UUID.fromString("00000000-0000-0000-0000-000000000301");
        UUID inlandB = UUID.fromString("00000000-0000-0000-0000-000000000302");
        UUID port = UUID.fromString("00000000-0000-0000-0000-0000000003ff");

        BannerModLogisticsRoute exportLow = route("00000000-0000-0000-0000-000000000310", inlandA, port, BannerModLogisticsPriority.LOW);
        BannerModLogisticsRoute importHigh = route("00000000-0000-0000-0000-000000000305", port, inlandB, BannerModLogisticsPriority.HIGH);
        BannerModLogisticsRoute inlandOnly = route("00000000-0000-0000-0000-000000000399", inlandA, inlandB, BannerModLogisticsPriority.HIGH);

        List<BannerModSeaTradeEntrypoint> entrypoints = service.listSeaTradeEntrypoints(
                List.of(exportLow, inlandOnly, importHigh),
                port::equals
        );

        assertEquals(2, entrypoints.size());
        assertEquals(importHigh.routeId(), entrypoints.get(0).routeId());
        assertEquals(BannerModSeaTradeDirection.IMPORT, entrypoints.get(0).direction());
        assertEquals(inlandB, entrypoints.get(0).settlementStorageAreaId());
        assertEquals(exportLow.routeId(), entrypoints.get(1).routeId());
        assertEquals(BannerModSeaTradeDirection.EXPORT, entrypoints.get(1).direction());
        assertEquals(inlandA, entrypoints.get(1).settlementStorageAreaId());
    }

    @Test
    void listSeaTradeEntrypointsSkipsRoutesWithoutLiveStorageEndpoints() {
        BannerModLogisticsService service = new BannerModLogisticsService();
        UUID inland = UUID.fromString("00000000-0000-0000-0000-000000000401");
        UUID port = UUID.fromString("00000000-0000-0000-0000-0000000004ff");
        UUID missing = UUID.fromString("00000000-0000-0000-0000-000000000499");

        BannerModLogisticsRoute live = route("00000000-0000-0000-0000-000000000410", inland, port, BannerModLogisticsPriority.NORMAL);
        BannerModLogisticsRoute stale = route("00000000-0000-0000-0000-000000000420", inland, missing, BannerModLogisticsPriority.HIGH);

        List<BannerModSeaTradeEntrypoint> entrypoints = service.listSeaTradeEntrypoints(
                List.of(stale, live),
                storageAreaId -> !storageAreaId.equals(missing),
                port::equals
        );

        assertEquals(1, entrypoints.size());
        assertEquals(live.routeId(), entrypoints.get(0).routeId());
        assertEquals(BannerModSeaTradeDirection.EXPORT, entrypoints.get(0).direction());
    }

    private static BannerModLogisticsRoute route(String routeId, BannerModLogisticsPriority priority) {
        return route(routeId, UUID.randomUUID(), UUID.randomUUID(), priority);
    }

    private static BannerModLogisticsRoute route(String routeId, UUID sourceId, UUID destinationId, BannerModLogisticsPriority priority) {
        return new BannerModLogisticsRoute(
                UUID.fromString(routeId),
                new BannerModLogisticsNodeRef(sourceId),
                new BannerModLogisticsNodeRef(destinationId),
                BannerModLogisticsItemFilter.any(),
                16,
                priority
        );
    }
}
