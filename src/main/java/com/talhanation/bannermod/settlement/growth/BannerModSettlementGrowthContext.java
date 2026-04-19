package com.talhanation.bannermod.settlement.growth;

import com.talhanation.bannermod.governance.BannerModGovernorSnapshot;
import com.talhanation.bannermod.settlement.BannerModSettlementBuildingRecord;
import com.talhanation.bannermod.settlement.BannerModSettlementDesiredGoodsSeed;
import com.talhanation.bannermod.settlement.BannerModSettlementMarketState;
import com.talhanation.bannermod.settlement.BannerModSettlementProjectCandidateSeed;
import com.talhanation.bannermod.settlement.BannerModSettlementResidentRecord;
import com.talhanation.bannermod.settlement.BannerModSettlementSnapshot;
import com.talhanation.bannermod.settlement.BannerModSettlementStockpileSummary;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Immutable input bundle for {@link BannerModSettlementGrowthManager}. Holds
 * just the seeds and signals needed to score growth candidates. Use
 * {@link #fromSnapshot} for the common case; the canonical record constructor
 * is left accessible for tests that want a minimal input.
 */
public record BannerModSettlementGrowthContext(
        BannerModSettlementProjectCandidateSeed projectCandidateSeed,
        BannerModSettlementDesiredGoodsSeed desiredGoodsSeed,
        BannerModSettlementStockpileSummary stockpileSummary,
        BannerModSettlementMarketState marketState,
        List<BannerModSettlementBuildingRecord> buildings,
        List<BannerModSettlementResidentRecord> residents,
        int residentCapacity,
        int assignedResidentCount,
        int unassignedWorkerCount,
        int missingWorkAreaAssignmentCount,
        @Nullable BannerModGovernorSnapshot governorSnapshot,
        long gameTime
) {
    public BannerModSettlementGrowthContext {
        projectCandidateSeed = projectCandidateSeed == null
                ? BannerModSettlementProjectCandidateSeed.empty() : projectCandidateSeed;
        desiredGoodsSeed = desiredGoodsSeed == null
                ? BannerModSettlementDesiredGoodsSeed.empty() : desiredGoodsSeed;
        stockpileSummary = stockpileSummary == null
                ? BannerModSettlementStockpileSummary.empty() : stockpileSummary;
        marketState = marketState == null
                ? BannerModSettlementMarketState.empty() : marketState;
        buildings = List.copyOf(buildings == null ? List.of() : buildings);
        residents = List.copyOf(residents == null ? List.of() : residents);
        residentCapacity = Math.max(0, residentCapacity);
        assignedResidentCount = Math.max(0, assignedResidentCount);
        unassignedWorkerCount = Math.max(0, unassignedWorkerCount);
        missingWorkAreaAssignmentCount = Math.max(0, missingWorkAreaAssignmentCount);
    }

    public static BannerModSettlementGrowthContext fromSnapshot(
            BannerModSettlementSnapshot snapshot,
            long gameTime
    ) {
        return fromSnapshot(snapshot, null, gameTime);
    }

    public static BannerModSettlementGrowthContext fromSnapshot(
            BannerModSettlementSnapshot snapshot,
            @Nullable BannerModGovernorSnapshot governorSnapshot,
            long gameTime
    ) {
        if (snapshot == null) {
            throw new IllegalArgumentException("snapshot must not be null");
        }
        return new BannerModSettlementGrowthContext(
                snapshot.projectCandidateSeed(),
                snapshot.desiredGoodsSeed(),
                snapshot.stockpileSummary(),
                snapshot.marketState(),
                snapshot.buildings(),
                snapshot.residents(),
                snapshot.residentCapacity(),
                snapshot.assignedResidentCount(),
                snapshot.unassignedWorkerCount(),
                snapshot.missingWorkAreaAssignmentCount(),
                governorSnapshot,
                gameTime
        );
    }

    /** True when the governor rollup carries the {@code under_siege} incident. */
    public boolean isUnderSiege() {
        if (this.governorSnapshot == null) {
            return false;
        }
        for (String token : this.governorSnapshot.incidentTokens()) {
            if ("under_siege".equalsIgnoreCase(token)) {
                return true;
            }
        }
        return false;
    }

    /** Resident capacity not yet filled; non-negative. */
    public int housingHeadroom() {
        return Math.max(0, this.residentCapacity - this.assignedResidentCount);
    }
}
