package com.talhanation.recruits.entities.ai.controller;

import com.talhanation.recruits.entities.AbstractLeaderEntity;

public final class RecruitCommandStateTransitions {

    private RecruitCommandStateTransitions() {
    }

    public static int afterMoveArrival(boolean reachedMovePos, int currentFollowState) {
        if (!reachedMovePos) {
            return currentFollowState;
        }
        return 2;
    }

    public static AbstractLeaderEntity.State afterManualMovement(AbstractLeaderEntity.State patrolState) {
        return afterPatrolInterruption(patrolState);
    }

    public static AbstractLeaderEntity.State afterPatrolInterruption(AbstractLeaderEntity.State patrolState) {
        if (patrolState == null) {
            return null;
        }
        return switch (patrolState) {
            case PATROLLING, WAITING -> AbstractLeaderEntity.State.PAUSED;
            case RETREATING, UPKEEP -> AbstractLeaderEntity.State.IDLE;
            default -> patrolState;
        };
    }
}
