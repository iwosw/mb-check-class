package com.talhanation.bannermod.entity.civilian;

public final class MerchantTradeRules {

    private MerchantTradeRules() {
    }

    public static boolean isTradeLimitReached(int currentTrades, int maxTrades) {
        return maxTrades != -1 && currentTrades >= maxTrades;
    }
}
