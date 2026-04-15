package com.talhanation.workers.entities.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FarmerLoopProgressTest {

    @Test
    void choosesExistingFieldOrderingBeforeFinishing() {
        assertEquals(FarmerLoopProgress.Action.PREPARE_BREAK_BLOCKS,
                FarmerLoopProgress.selectNextAction(true, true, true).action());
        assertEquals(FarmerLoopProgress.Action.PREPARE_PLOWING,
                FarmerLoopProgress.selectNextAction(false, true, true).action());
        assertEquals(FarmerLoopProgress.Action.PREPARE_PLANT_SEEDS,
                FarmerLoopProgress.selectNextAction(false, false, true).action());
        assertEquals(FarmerLoopProgress.Action.FINISHED,
                FarmerLoopProgress.selectNextAction(false, false, false).action());
    }

    @Test
    void keepsWaitingOnSameFieldWhenHoeOrSeedsAreMissing() {
        FarmerLoopProgress.Decision waitingForHoe = FarmerLoopProgress.waitForRequiredItem(FarmerLoopProgress.Action.PREPARE_PLOWING);
        assertTrue(waitingForHoe.isWaitingForItem());
        assertTrue(waitingForHoe.keepCurrentArea());
        assertEquals(FarmerLoopProgress.Action.PREPARE_PLOWING, waitingForHoe.resumeAction());

        FarmerLoopProgress.Decision waitingForSeeds = FarmerLoopProgress.waitForRequiredItem(FarmerLoopProgress.Action.PREPARE_PLANT_SEEDS);
        assertTrue(waitingForSeeds.isWaitingForItem());
        assertTrue(waitingForSeeds.keepCurrentArea());
        assertEquals(FarmerLoopProgress.Action.PREPARE_PLANT_SEEDS, waitingForSeeds.resumeAction());
    }

    @Test
    void finishesWhenNoWorkRemains() {
        FarmerLoopProgress.Decision finished = FarmerLoopProgress.selectNextAction(false, false, false);

        assertTrue(finished.isFinished());
        assertFalse(finished.keepCurrentArea());
    }
}
