package com.talhanation.workers;

import com.talhanation.bannermod.settlement.BannerModSettlementBinding;
import com.talhanation.bannerlord.ai.civilian.MiningClaimExcavationRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MiningClaimExcavationRulesTest {

    @Test
    void friendlyAndUnclaimedTargetsStayExcavatable() {
        assertTrue(MiningClaimExcavationRules.allowsExcavation(BannerModSettlementBinding.Status.FRIENDLY_CLAIM));
        assertTrue(MiningClaimExcavationRules.allowsExcavation(BannerModSettlementBinding.Status.UNCLAIMED));
    }

    @Test
    void hostileAndDegradedTargetsAreDenied() {
        assertFalse(MiningClaimExcavationRules.allowsExcavation(BannerModSettlementBinding.Status.HOSTILE_CLAIM));
        assertFalse(MiningClaimExcavationRules.allowsExcavation(BannerModSettlementBinding.Status.DEGRADED_MISMATCH));
    }
}
