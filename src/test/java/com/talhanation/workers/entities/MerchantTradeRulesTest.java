package com.talhanation.workers.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MerchantTradeRulesTest {

    @Test
    void limitReachedOnlyAfterConfiguredTradesAreConsumed() {
        assertFalse(MerchantTradeRules.isTradeLimitReached(0, 1));
        assertTrue(MerchantTradeRules.isTradeLimitReached(1, 1));
    }

    @Test
    void unlimitedTradesNeverReachLimit() {
        assertFalse(MerchantTradeRules.isTradeLimitReached(100, -1));
    }
}
