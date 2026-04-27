package com.talhanation.bannermod.war.events;

import java.util.concurrent.atomic.AtomicInteger;

public final class WarSyncDirtyTracker {
    private static final AtomicInteger VERSION = new AtomicInteger();

    private WarSyncDirtyTracker() {
    }

    public static int version() {
        return VERSION.get();
    }

    public static void markDirty() {
        VERSION.incrementAndGet();
    }

    public static void reset() {
        VERSION.set(0);
    }
}
