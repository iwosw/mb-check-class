package com.talhanation.recruits.entities.ai.controller;

import com.talhanation.recruits.entities.AbstractLeaderEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecruitCommandStateTransitionsTest {

    @Test
    void moveArrivalResolvesToStableHoldBehavior() {
        assertEquals(2, RecruitCommandStateTransitions.afterMoveArrival(true, 6));
        assertEquals(1, RecruitCommandStateTransitions.afterMoveArrival(false, 1));
    }

    @Test
    void manualCommandsInterruptPatrolLeadersPredictably() {
        assertEquals(AbstractLeaderEntity.State.PAUSED, RecruitCommandStateTransitions.afterManualMovement(AbstractLeaderEntity.State.PATROLLING));
        assertEquals(AbstractLeaderEntity.State.PAUSED, RecruitCommandStateTransitions.afterManualMovement(AbstractLeaderEntity.State.WAITING));
        assertEquals(AbstractLeaderEntity.State.IDLE, RecruitCommandStateTransitions.afterManualMovement(AbstractLeaderEntity.State.RETREATING));
        assertEquals(AbstractLeaderEntity.State.IDLE, RecruitCommandStateTransitions.afterManualMovement(AbstractLeaderEntity.State.UPKEEP));
    }

    @Test
    void helperUsesValueInputsAndOutputsOnly() {
        assertEquals(AbstractLeaderEntity.State.IDLE, RecruitCommandStateTransitions.afterPatrolInterruption(AbstractLeaderEntity.State.IDLE));
        assertEquals(AbstractLeaderEntity.State.ATTACKING, RecruitCommandStateTransitions.afterPatrolInterruption(AbstractLeaderEntity.State.ATTACKING));
    }
}
