package com.talhanation.bannermod.ai.civilian;

import com.talhanation.bannermod.settlement.BannerModSettlementBinding;

public final class MiningClaimExcavationRules {

    private MiningClaimExcavationRules() {
    }

    public static boolean allowsExcavation(BannerModSettlementBinding.Status status) {
        if (status == null) {
            return true;
        }
        return switch (status) {
            case FRIENDLY_CLAIM, UNCLAIMED -> true;
            case HOSTILE_CLAIM, DEGRADED_MISMATCH -> false;
        };
    }
}
