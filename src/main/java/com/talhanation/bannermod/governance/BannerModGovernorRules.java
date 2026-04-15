package com.talhanation.bannermod.governance;

import com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding;

import javax.annotation.Nullable;
import java.util.UUID;

public final class BannerModGovernorRules {

    private BannerModGovernorRules() {
    }

    public enum Decision {
        ALLOW,
        MISSING_RECRUIT,
        MISSING_OWNER,
        UNCLAIMED_SETTLEMENT,
        HOSTILE_SETTLEMENT,
        DEGRADED_SETTLEMENT,
        NO_GOVERNOR_ASSIGNED
    }

    public static Decision assignmentDecision(BannerModSettlementBinding.Binding binding,
                                              @Nullable UUID recruitUuid,
                                              @Nullable UUID ownerUuid) {
        if (recruitUuid == null) {
            return Decision.MISSING_RECRUIT;
        }
        if (ownerUuid == null) {
            return Decision.MISSING_OWNER;
        }
        return settlementDecision(binding);
    }

    public static Decision controlDecision(BannerModSettlementBinding.Binding binding,
                                           @Nullable BannerModGovernorSnapshot snapshot) {
        if (snapshot == null || !snapshot.hasGovernor()) {
            return Decision.NO_GOVERNOR_ASSIGNED;
        }
        return settlementDecision(binding);
    }

    public static boolean isAllowed(Decision decision) {
        return decision == Decision.ALLOW;
    }

    private static Decision settlementDecision(BannerModSettlementBinding.Binding binding) {
        if (binding == null) {
            return Decision.UNCLAIMED_SETTLEMENT;
        }
        return switch (binding.status()) {
            case FRIENDLY_CLAIM -> Decision.ALLOW;
            case HOSTILE_CLAIM -> Decision.HOSTILE_SETTLEMENT;
            case DEGRADED_MISMATCH -> Decision.DEGRADED_SETTLEMENT;
            case UNCLAIMED -> Decision.UNCLAIMED_SETTLEMENT;
        };
    }
}
