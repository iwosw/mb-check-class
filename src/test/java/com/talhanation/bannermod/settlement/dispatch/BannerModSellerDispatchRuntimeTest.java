package com.talhanation.bannermod.settlement.dispatch;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BannerModSellerDispatchRuntimeTest {

    private static final UUID SELLER_A = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SELLER_B = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID MARKET_A = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID MARKET_B = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @Test
    void beginDispatchRecordsMovingToStall() {
        BannerModSellerDispatchRuntime runtime = new BannerModSellerDispatchRuntime();
        runtime.beginDispatch(SELLER_A, MARKET_A, 1000L);

        Optional<SellerPhaseRecord> phase = runtime.phase(SELLER_A);
        assertTrue(phase.isPresent());
        assertEquals(SellerPhase.MOVING_TO_STALL, phase.get().phase());
        assertEquals(MARKET_A, phase.get().marketRecordUuid());
        assertEquals(1000L, phase.get().phaseStartGameTime());
        assertEquals(0, phase.get().phaseTickCount());
        assertTrue(runtime.isActive(SELLER_A));
    }

    @Test
    void tickPhaseWalksThroughFullHappyPath() {
        BannerModSellerDispatchRuntime runtime = new BannerModSellerDispatchRuntime();
        runtime.beginDispatch(SELLER_A, MARKET_A, 0L);

        // Drive MOVING_TO_STALL -> AT_STALL.
        for (int i = 0; i < BannerModSellerDispatchRuntime.MOVE_TO_STALL_MAX_TICKS; i++) {
            runtime.tickPhase(SELLER_A, i + 1L);
        }
        assertEquals(SellerPhase.AT_STALL, runtime.phase(SELLER_A).orElseThrow().phase());

        // AT_STALL -> SELLING.
        long t = BannerModSellerDispatchRuntime.MOVE_TO_STALL_MAX_TICKS;
        for (int i = 0; i < BannerModSellerDispatchRuntime.AT_STALL_DELAY_TICKS; i++) {
            runtime.tickPhase(SELLER_A, ++t);
        }
        assertEquals(SellerPhase.SELLING, runtime.phase(SELLER_A).orElseThrow().phase());

        // SELLING -> RETURNING.
        for (int i = 0; i < BannerModSellerDispatchRuntime.SELLING_MAX_TICKS; i++) {
            runtime.tickPhase(SELLER_A, ++t);
        }
        assertEquals(SellerPhase.RETURNING, runtime.phase(SELLER_A).orElseThrow().phase());

        // RETURNING -> RETURNED.
        for (int i = 0; i < BannerModSellerDispatchRuntime.RETURNING_MAX_TICKS; i++) {
            runtime.tickPhase(SELLER_A, ++t);
        }
        assertEquals(SellerPhase.RETURNED, runtime.phase(SELLER_A).orElseThrow().phase());
        assertFalse(runtime.isActive(SELLER_A), "RETURNED seller should be considered idle");
    }

    @Test
    void beginDispatchWhileActiveThrows() {
        BannerModSellerDispatchRuntime runtime = new BannerModSellerDispatchRuntime();
        runtime.beginDispatch(SELLER_A, MARKET_A, 0L);

        assertThrows(
                IllegalStateException.class,
                () -> runtime.beginDispatch(SELLER_A, MARKET_B, 10L),
                "re-dispatching an active seller must throw"
        );
    }

    @Test
    void beginDispatchAllowedAfterReturned() {
        BannerModSellerDispatchRuntime runtime = new BannerModSellerDispatchRuntime();
        runtime.beginDispatch(SELLER_A, MARKET_A, 0L);
        runtime.advance(SELLER_A, SellerPhase.RETURNED, 5L);

        // RETURNED is idle, so a second dispatch is permitted.
        runtime.beginDispatch(SELLER_A, MARKET_B, 6L);
        assertEquals(SellerPhase.MOVING_TO_STALL, runtime.phase(SELLER_A).orElseThrow().phase());
        assertEquals(MARKET_B, runtime.phase(SELLER_A).orElseThrow().marketRecordUuid());
    }

    @Test
    void forceMarketCloseSetsPhaseForAllSellersAtMarket() {
        BannerModSellerDispatchRuntime runtime = new BannerModSellerDispatchRuntime();
        runtime.beginDispatch(SELLER_A, MARKET_A, 0L);
        runtime.beginDispatch(SELLER_B, MARKET_A, 0L);

        UUID sellerOther = UUID.fromString("33333333-3333-3333-3333-333333333333");
        runtime.beginDispatch(sellerOther, MARKET_B, 0L);

        runtime.forceMarketClose(MARKET_A, 42L);

        assertEquals(SellerPhase.MARKET_CLOSED, runtime.phase(SELLER_A).orElseThrow().phase());
        assertEquals(SellerPhase.MARKET_CLOSED, runtime.phase(SELLER_B).orElseThrow().phase());
        assertEquals(42L, runtime.phase(SELLER_A).orElseThrow().phaseStartGameTime());
        // Seller at the other market is untouched.
        assertEquals(SellerPhase.MOVING_TO_STALL, runtime.phase(sellerOther).orElseThrow().phase());
    }

    @Test
    void phaseReturnsEmptyForUnknownResident() {
        BannerModSellerDispatchRuntime runtime = new BannerModSellerDispatchRuntime();
        assertTrue(runtime.phase(SELLER_A).isEmpty());
        assertTrue(runtime.phase(null).isEmpty());
    }

    @Test
    void advanceIsNoOpForUnknownResident() {
        BannerModSellerDispatchRuntime runtime = new BannerModSellerDispatchRuntime();
        runtime.advance(SELLER_A, SellerPhase.SELLING, 5L);
        assertTrue(runtime.phase(SELLER_A).isEmpty());
    }

    @Test
    void activeDispatchesReturnsInsertionOrderSnapshot() {
        BannerModSellerDispatchRuntime runtime = new BannerModSellerDispatchRuntime();
        runtime.beginDispatch(SELLER_A, MARKET_A, 0L);
        runtime.beginDispatch(SELLER_B, MARKET_B, 0L);

        List<SellerPhaseRecord> all = runtime.activeDispatches();
        assertEquals(2, all.size());
        assertEquals(SELLER_A, all.get(0).sellerResidentUuid());
        assertEquals(SELLER_B, all.get(1).sellerResidentUuid());

        runtime.advance(SELLER_A, SellerPhase.SELLING, 1L);
        List<SellerPhaseRecord> selling = runtime.activeDispatches(SellerPhase.SELLING);
        assertEquals(1, selling.size());
        assertEquals(SELLER_A, selling.get(0).sellerResidentUuid());
    }

    @Test
    void resetClearsAllState() {
        BannerModSellerDispatchRuntime runtime = new BannerModSellerDispatchRuntime();
        runtime.beginDispatch(SELLER_A, MARKET_A, 0L);
        runtime.reset();
        assertTrue(runtime.phase(SELLER_A).isEmpty());
        assertEquals(0, runtime.activeDispatches().size());
    }
}
