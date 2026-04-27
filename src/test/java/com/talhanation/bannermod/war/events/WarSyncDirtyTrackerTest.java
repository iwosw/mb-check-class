package com.talhanation.bannermod.war.events;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarSyncDirtyTrackerTest {
    @AfterEach
    void reset() {
        WarSyncDirtyTracker.reset();
    }

    @Test
    void dirtyMarksAdvanceVersion() {
        int initial = WarSyncDirtyTracker.version();

        WarSyncDirtyTracker.markDirty();
        WarSyncDirtyTracker.markDirty();

        assertTrue(WarSyncDirtyTracker.version() >= initial + 2);
    }

    @Test
    void resetClearsVersion() {
        WarSyncDirtyTracker.markDirty();

        WarSyncDirtyTracker.reset();

        assertEquals(0, WarSyncDirtyTracker.version());
    }
}
