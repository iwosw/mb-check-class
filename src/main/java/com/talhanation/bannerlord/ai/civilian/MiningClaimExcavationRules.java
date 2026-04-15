package com.talhanation.bannerlord.ai.civilian;

import com.talhanation.bannerlord.shared.settlement.BannerModSettlementBinding;

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
