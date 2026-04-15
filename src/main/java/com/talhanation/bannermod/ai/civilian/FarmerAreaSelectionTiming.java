package com.talhanation.bannermod.ai.civilian;

public class FarmerAreaSelectionTiming {

    public static final int IDLE_AREA_RESCAN_INTERVAL_TICKS = 20;

    private FarmerAreaSelectionTiming() {
    }

    public static boolean shouldSearchForArea(boolean hasCurrentArea, int cooldownTicks) {
        return hasCurrentArea || cooldownTicks >= IDLE_AREA_RESCAN_INTERVAL_TICKS;
    }

    public static int cooldownAfterWorkCycle() {
        return IDLE_AREA_RESCAN_INTERVAL_TICKS;
    }
}
