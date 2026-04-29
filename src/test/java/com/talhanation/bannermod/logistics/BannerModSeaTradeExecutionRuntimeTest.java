package com.talhanation.bannermod.logistics;

import com.talhanation.bannermod.shared.logistics.BannerModLogisticsItemFilter;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsNodeRef;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsPriority;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsRoute;
import com.talhanation.bannermod.shared.logistics.BannerModSeaTradeExecutionRecord;
import com.talhanation.bannermod.shared.logistics.BannerModSeaTradeExecutionRuntime;
import com.talhanation.bannermod.shared.logistics.BannerModSeaTradeExecutionState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BannerModSeaTradeExecutionRuntimeTest {

    private static final UUID ROUTE_ID = UUID.fromString("00000000-0000-0000-0000-000000002001");
    private static final UUID SOURCE_ID = UUID.fromString("00000000-0000-0000-0000-000000002101");
    private static final UUID DESTINATION_ID = UUID.fromString("00000000-0000-0000-0000-000000002102");
    private static final UUID CARRIER_ID = UUID.fromString("00000000-0000-0000-0000-000000002201");
    private static final ResourceLocation WHEAT = ResourceLocation.fromNamespaceAndPath("minecraft", "wheat");

    @Test
    void noCarrierFailsDeterministicallyWithRouteIdentity() {
        BannerModSeaTradeExecutionRuntime runtime = new BannerModSeaTradeExecutionRuntime();

        BannerModSeaTradeExecutionRecord record = runtime.start(route(), null);

        assertEquals(ROUTE_ID, record.routeId());
        assertEquals(BannerModSeaTradeExecutionState.FAILED, record.state());
        assertEquals(BannerModSeaTradeExecutionRecord.FAILURE_NO_CARRIER, record.failureReason());
        assertTrue(runtime.route(ROUTE_ID).isPresent());
    }

    @Test
    void loadStartTracksCarrierStorageFilterAndRequestedCount() {
        BannerModSeaTradeExecutionRecord record = newRuntimeWithRoute().route(ROUTE_ID).orElseThrow().loadStarted();

        assertEquals(ROUTE_ID, record.routeId());
        assertEquals(CARRIER_ID, record.boundCarrierId());
        assertEquals(SOURCE_ID, record.sourceStorageAreaId());
        assertEquals(DESTINATION_ID, record.destinationStorageAreaId());
        assertTrue(record.filter().matchesItemId(WHEAT));
        assertEquals(16, record.requestedCount());
        assertEquals(0, record.cargoCount());
        assertEquals(BannerModSeaTradeExecutionState.LOADING, record.state());
    }

    @Test
    void travelPendingStoresClampedCargoCount() {
        BannerModSeaTradeExecutionRuntime runtime = newRuntimeWithRoute();

        BannerModSeaTradeExecutionRecord record = runtime.update(runtime.route(ROUTE_ID).orElseThrow().travelPending(99));

        assertEquals(ROUTE_ID, record.routeId());
        assertEquals(16, record.cargoCount());
        assertEquals(BannerModSeaTradeExecutionState.TRAVELLING, record.state());
    }

    @Test
    void arrivalReadyMovesTravellingCargoToUnloading() {
        BannerModSeaTradeExecutionRecord record = newRuntimeWithRoute()
                .route(ROUTE_ID).orElseThrow()
                .travelPending(6)
                .arrivalReady();

        assertEquals(ROUTE_ID, record.routeId());
        assertEquals(6, record.cargoCount());
        assertEquals(BannerModSeaTradeExecutionState.UNLOADING, record.state());
    }

    @Test
    void unloadCompleteClearsCargoAndPersistsRoundTrip() {
        BannerModSeaTradeExecutionRuntime runtime = newRuntimeWithRoute();
        BannerModSeaTradeExecutionRecord complete = runtime.update(runtime.route(ROUTE_ID).orElseThrow()
                .travelPending(6)
                .arrivalReady()
                .unloadComplete());

        CompoundTag saved = runtime.toTag();
        BannerModSeaTradeExecutionRecord restored = BannerModSeaTradeExecutionRuntime.fromTag(saved).route(ROUTE_ID).orElseThrow();

        assertEquals(complete, restored);
        assertEquals(ROUTE_ID, restored.routeId());
        assertEquals(0, restored.cargoCount());
        assertEquals(BannerModSeaTradeExecutionState.COMPLETE, restored.state());
        assertTrue(restored.failureReason().isBlank());
    }

    @Test
    void failureKeepsRouteIdentityForSettlementFeedback() {
        BannerModSeaTradeExecutionRecord failed = newRuntimeWithRoute()
                .route(ROUTE_ID).orElseThrow()
                .travelPending(6)
                .failed("CARRIER_NAVIGATION_UNSUPPORTED");

        assertEquals(ROUTE_ID, failed.routeId());
        assertEquals(CARRIER_ID, failed.boundCarrierId());
        assertEquals(SOURCE_ID, failed.sourceStorageAreaId());
        assertEquals(DESTINATION_ID, failed.destinationStorageAreaId());
        assertEquals(6, failed.cargoCount());
        assertEquals(BannerModSeaTradeExecutionState.FAILED, failed.state());
        assertEquals("CARRIER_NAVIGATION_UNSUPPORTED", failed.failureReason());
    }

    @Test
    void loadWithNoCargoFailsWithClearReason() {
        BannerModSeaTradeExecutionRecord failed = newRuntimeWithRoute()
                .route(ROUTE_ID).orElseThrow()
                .travelPending(0);

        assertEquals(ROUTE_ID, failed.routeId());
        assertEquals(BannerModSeaTradeExecutionState.FAILED, failed.state());
        assertEquals(BannerModSeaTradeExecutionRecord.FAILURE_NO_CARGO_LOADED, failed.failureReason());
    }

    @Test
    void unknownRouteLookupIsEmpty() {
        assertFalse(newRuntimeWithRoute().route(UUID.randomUUID()).isPresent());
    }

    private static BannerModSeaTradeExecutionRuntime newRuntimeWithRoute() {
        BannerModSeaTradeExecutionRuntime runtime = new BannerModSeaTradeExecutionRuntime();
        runtime.start(route(), CARRIER_ID);
        return runtime;
    }

    private static BannerModLogisticsRoute route() {
        return new BannerModLogisticsRoute(
                ROUTE_ID,
                new BannerModLogisticsNodeRef(SOURCE_ID),
                new BannerModLogisticsNodeRef(DESTINATION_ID),
                BannerModLogisticsItemFilter.ofItemIds(List.of(WHEAT)),
                16,
                BannerModLogisticsPriority.NORMAL
        );
    }
}
