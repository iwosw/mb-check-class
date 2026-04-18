package com.talhanation.bannermod.shared.logistics;

public final class BannerModLogisticsRuntime {

    private static BannerModLogisticsService service = new BannerModLogisticsService();

    private BannerModLogisticsRuntime() {
    }

    public static BannerModLogisticsService service() {
        return service;
    }

    public static void resetForTests() {
        service = new BannerModLogisticsService();
    }
}
