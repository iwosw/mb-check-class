package com.talhanation.bannermod.settlement;

import com.talhanation.bannermod.shared.logistics.BannerModLogisticsReservation;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsRoute;
import com.talhanation.bannermod.shared.logistics.BannerModSeaTradeEntrypoint;
import com.talhanation.bannermod.shared.logistics.BannerModSeaTradeExecutionRecord;
import com.talhanation.bannermod.shared.logistics.BannerModSeaTradeSummary;

import java.util.List;

final class BannerModSettlementLogisticsDerivationService {

    private BannerModSettlementLogisticsDerivationService() {
    }

    static LogisticsResult derive(List<BannerModSettlementBuildingRecord> buildings,
                                  List<BannerModSettlementResidentRecord> residents,
                                  BannerModSettlementMarketState marketState,
                                  List<BannerModSeaTradeEntrypoint> liveSeaTradeEntrypoints,
                                  List<BannerModLogisticsRoute> localRoutes,
                                  List<BannerModLogisticsReservation> reservations,
                                  List<BannerModSeaTradeExecutionRecord> localSeaTradeExecutions,
                                  boolean governedSettlement,
                                  boolean claimedSettlement) {
        BannerModSeaTradeSummary.Summary seaTradeSummary = BannerModSeaTradeSummary.summarise(liveSeaTradeEntrypoints);
        BannerModSettlementService.ReservationSignalSeed reservationSignalSeed = BannerModSettlementService.summarizeReservationSignalSeed(
                buildings,
                localRoutes,
                reservations
        );
        BannerModSettlementStockpileSummary stockpileSummary = BannerModSettlementService.summarizeStockpiles(buildings, liveSeaTradeEntrypoints);
        BannerModSettlementDesiredGoodsSeed desiredGoodsSeed = BannerModSettlementService.summarizeDesiredGoods(
                buildings,
                stockpileSummary,
                marketState,
                seaTradeSummary
        );
        BannerModSettlementProjectCandidateSeed projectCandidateSeed = BannerModSettlementService.summarizeProjectCandidate(
                buildings,
                stockpileSummary,
                desiredGoodsSeed,
                marketState,
                governedSettlement,
                claimedSettlement
        );
        BannerModSettlementTradeRouteHandoffSeed tradeRouteHandoffSeed = BannerModSettlementService.summarizeTradeRouteHandoffSeed(
                stockpileSummary,
                marketState,
                desiredGoodsSeed,
                reservationSignalSeed,
                seaTradeSummary,
                localSeaTradeExecutions
        );
        BannerModSettlementSupplySignalState supplySignalState = BannerModSettlementService.summarizeSupplySignals(
                desiredGoodsSeed,
                stockpileSummary,
                marketState,
                residents,
                buildings,
                reservationSignalSeed,
                seaTradeSummary
        );
        return new LogisticsResult(
                stockpileSummary,
                desiredGoodsSeed,
                projectCandidateSeed,
                tradeRouteHandoffSeed,
                supplySignalState,
                reservationSignalSeed
        );
    }

    record LogisticsResult(BannerModSettlementStockpileSummary stockpileSummary,
                           BannerModSettlementDesiredGoodsSeed desiredGoodsSeed,
                           BannerModSettlementProjectCandidateSeed projectCandidateSeed,
                           BannerModSettlementTradeRouteHandoffSeed tradeRouteHandoffSeed,
                           BannerModSettlementSupplySignalState supplySignalState,
                           BannerModSettlementService.ReservationSignalSeed reservationSignalSeed) {
    }
}
