package com.talhanation.bannermod.settlement.dispatch;

import com.talhanation.bannermod.settlement.BannerModSettlementMarketState;
import com.talhanation.bannermod.settlement.BannerModSettlementSellerDispatchRecord;
import com.talhanation.bannermod.settlement.BannerModSettlementSellerDispatchState;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BannerModSellerDispatchAdvisorTest {

    private static final UUID SELLER_A = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SELLER_B = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID MARKET_A = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID MARKET_B = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @Test
    void picksReadySellerWhoseTwinIsAlreadyBusy() {
        // 2 READY records; one seller is already active in the runtime. Advisor
        // must return the other (still-idle) seller.
        BannerModSettlementMarketState state = new BannerModSettlementMarketState(
                2, 2, 0, 0, 2, 2,
                List.of(),
                List.of(
                        new BannerModSettlementSellerDispatchRecord(
                                SELLER_A, MARKET_A, "MarketA", BannerModSettlementSellerDispatchState.READY),
                        new BannerModSettlementSellerDispatchRecord(
                                SELLER_B, MARKET_B, "MarketB", BannerModSettlementSellerDispatchState.READY)
                )
        );
        BannerModSellerDispatchRuntime runtime = new BannerModSellerDispatchRuntime();
        runtime.beginDispatch(SELLER_A, MARKET_A, 0L);

        Optional<UUID> picked = BannerModSellerDispatchAdvisor.pickReadySeller(state, runtime, 0L);
        assertTrue(picked.isPresent());
        assertEquals(SELLER_B, picked.get());
    }

    @Test
    void emptyStateReturnsEmpty() {
        BannerModSellerDispatchRuntime runtime = new BannerModSellerDispatchRuntime();
        Optional<UUID> picked = BannerModSellerDispatchAdvisor.pickReadySeller(
                BannerModSettlementMarketState.empty(), runtime, 0L);
        assertTrue(picked.isEmpty());
    }

    @Test
    void allSellersBusyReturnsEmpty() {
        BannerModSettlementMarketState state = new BannerModSettlementMarketState(
                2, 2, 0, 0, 2, 2,
                List.of(),
                List.of(
                        new BannerModSettlementSellerDispatchRecord(
                                SELLER_A, MARKET_A, "MarketA", BannerModSettlementSellerDispatchState.READY),
                        new BannerModSettlementSellerDispatchRecord(
                                SELLER_B, MARKET_B, "MarketB", BannerModSettlementSellerDispatchState.READY)
                )
        );
        BannerModSellerDispatchRuntime runtime = new BannerModSellerDispatchRuntime();
        runtime.beginDispatch(SELLER_A, MARKET_A, 0L);
        runtime.beginDispatch(SELLER_B, MARKET_B, 0L);

        Optional<UUID> picked = BannerModSellerDispatchAdvisor.pickReadySeller(state, runtime, 0L);
        assertTrue(picked.isEmpty());
    }

    @Test
    void marketClosedSeedsAreNotConsidered() {
        BannerModSettlementMarketState state = new BannerModSettlementMarketState(
                1, 0, 0, 0, 1, 0,
                List.of(),
                List.of(
                        new BannerModSettlementSellerDispatchRecord(
                                SELLER_A, MARKET_A, "MarketA", BannerModSettlementSellerDispatchState.MARKET_CLOSED)
                )
        );
        BannerModSellerDispatchRuntime runtime = new BannerModSellerDispatchRuntime();
        Optional<UUID> picked = BannerModSellerDispatchAdvisor.pickReadySeller(state, runtime, 100L);
        assertTrue(picked.isEmpty());
    }

    @Test
    void iterationOrderMatchesRecordList() {
        // Deterministic: first READY, idle seller in list order wins.
        BannerModSettlementMarketState state = new BannerModSettlementMarketState(
                2, 2, 0, 0, 2, 2,
                List.of(),
                List.of(
                        new BannerModSettlementSellerDispatchRecord(
                                SELLER_B, MARKET_B, "MarketB", BannerModSettlementSellerDispatchState.READY),
                        new BannerModSettlementSellerDispatchRecord(
                                SELLER_A, MARKET_A, "MarketA", BannerModSettlementSellerDispatchState.READY)
                )
        );
        BannerModSellerDispatchRuntime runtime = new BannerModSellerDispatchRuntime();
        Optional<UUID> picked = BannerModSellerDispatchAdvisor.pickReadySeller(state, runtime, 0L);
        assertTrue(picked.isPresent());
        assertEquals(SELLER_B, picked.get(), "list-first READY record wins");
    }

    @Test
    void nullInputsReturnEmpty() {
        BannerModSellerDispatchRuntime runtime = new BannerModSellerDispatchRuntime();
        assertTrue(BannerModSellerDispatchAdvisor.pickReadySeller(null, runtime, 0L).isEmpty());
        assertTrue(BannerModSellerDispatchAdvisor.pickReadySeller(
                BannerModSettlementMarketState.empty(), null, 0L).isEmpty());
    }
}
