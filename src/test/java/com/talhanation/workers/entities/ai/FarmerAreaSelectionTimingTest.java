package com.talhanation.workers.entities.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FarmerAreaSelectionTimingTest {

    @Test
    void rescansIdleAreasAtLeastOncePerSecond() {
        assertFalse(FarmerAreaSelectionTiming.shouldSearchForArea(false, 19));
        assertTrue(FarmerAreaSelectionTiming.shouldSearchForArea(false, 20));
    }

    @Test
    void finishedWorkCycleAllowsImmediateRescan() {
        assertTrue(FarmerAreaSelectionTiming.shouldSearchForArea(false, FarmerAreaSelectionTiming.cooldownAfterWorkCycle()));
    }
}
