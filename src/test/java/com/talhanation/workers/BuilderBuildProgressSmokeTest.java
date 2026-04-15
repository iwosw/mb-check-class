package com.talhanation.workers;

import com.talhanation.bannermod.ai.civilian.BuilderBuildProgress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuilderBuildProgressSmokeTest {

    @Test
    void classifyBuildResumeStateForMergedWorkersFlow() {
        assertEquals(BuilderBuildProgress.State.NOT_STARTED, BuilderBuildProgress.classify(false, false, false));
        assertEquals(BuilderBuildProgress.State.IN_PROGRESS, BuilderBuildProgress.classify(true, false, true));
        assertEquals(BuilderBuildProgress.State.COMPLETE, BuilderBuildProgress.classify(true, true, true));
    }

    @Test
    void detectPendingWorldWorkAcrossBuildQueues() {
        assertFalse(BuilderBuildProgress.hasPendingWorldWork(0, 0, 0));
        assertTrue(BuilderBuildProgress.hasPendingWorldWork(1, 0, 0));
        assertTrue(BuilderBuildProgress.hasPendingWorldWork(0, 1, 0));
        assertTrue(BuilderBuildProgress.hasPendingWorldWork(0, 0, 1));
    }
}
