package com.talhanation.bannermod.shared.settlement;

import com.talhanation.bannermod.events.ClaimEvents;
import com.talhanation.bannermod.governance.BannerModGovernorManager;
import com.talhanation.bannermod.settlement.BannerModSettlementManager;
import com.talhanation.bannermod.settlement.BannerModSettlementService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;

public final class BannerModSettlementRefreshSupport {
    private BannerModSettlementRefreshSupport() {
    }

    public static void refreshSnapshot(ServerLevel level, @Nullable BlockPos pos) {
        if (level == null || pos == null || ClaimEvents.recruitsClaimManager == null) {
            return;
        }
        BannerModSettlementService.refreshClaimAt(
                level,
                ClaimEvents.recruitsClaimManager,
                BannerModSettlementManager.get(level),
                BannerModGovernorManager.get(level),
                pos
        );
    }

    public static void refreshTransition(ServerLevel level,
                                         @Nullable BlockPos firstPos,
                                         @Nullable BlockPos secondPos) {
        refreshSnapshot(level, firstPos);
        if (firstPos == null || secondPos == null || !firstPos.equals(secondPos)) {
            refreshSnapshot(level, secondPos);
        }
    }
}
