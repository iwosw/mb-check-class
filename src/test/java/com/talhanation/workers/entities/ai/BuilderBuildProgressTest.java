package com.talhanation.workers.entities.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuilderBuildProgressTest {

    @Test
    void classifyTreatsStartedStructuredBuildAsInProgress() {
        assertEquals(BuilderBuildProgress.State.IN_PROGRESS,
                BuilderBuildProgress.classify(true, false, true));
        assertEquals(BuilderBuildProgress.State.NOT_STARTED,
                BuilderBuildProgress.classify(false, false, true));
        assertEquals(BuilderBuildProgress.State.NOT_STARTED,
                BuilderBuildProgress.classify(true, false, false));
        assertEquals(BuilderBuildProgress.State.COMPLETE,
                BuilderBuildProgress.classify(true, true, true));
    }

    @Test
    void pendingWorldWorkCountsBreakAndPlacementStages() {
        assertTrue(BuilderBuildProgress.hasPendingWorldWork(1, 0, 0));
        assertTrue(BuilderBuildProgress.hasPendingWorldWork(0, 1, 0));
        assertTrue(BuilderBuildProgress.hasPendingWorldWork(0, 0, 1));
        assertFalse(BuilderBuildProgress.hasPendingWorldWork(0, 0, 0));
    }
}
