package com.talhanation.bannermod.entity.military;

import net.minecraft.core.BlockPos;

final class LeaderUpkeepRuntime {
    private static final double UPKEEP_TRIGGER_DISTANCE_SQR = 5000D;

    private final AbstractLeaderEntity leader;

    LeaderUpkeepRuntime(AbstractLeaderEntity leader) {
        this.leader = leader;
    }

    void tickUpkeepTimer() {
        if (leader.waitForRecruitsUpkeepTime > 0) {
            leader.waitForRecruitsUpkeepTime--;
        }
    }

    void handleUpkeepState() {
        if (leader.waitForRecruitsUpkeepTime != 0 || leader.army == null) {
            return;
        }

        boolean allRecruitsResupplied = leader.army.getAllRecruitUnits().stream()
                .allMatch(recruit -> recruit.getUpkeepTimer() > 0 && !recruit.forcedUpkeep);

        if (allRecruitsResupplied) {
            leader.waitForRecruitsUpkeepTime = leader.getAgainResupplyTime();
            leader.setPatrolState(AbstractLeaderEntity.State.PATROLLING);
        }
    }

    boolean shouldStartResupply() {
        BlockPos upkeepPos = leader.getUpkeepPos();
        return upkeepPos != null
                && leader.getWaypointIndex() == 0
                && upkeepPos.distSqr(leader.getOnPos()) < UPKEEP_TRIGGER_DISTANCE_SQR
                && (leader.waitForRecruitsUpkeepTime == 0 || leader.getOtherUpkeepInterruption());
    }

    void startResupply() {
        leader.handleResupply();
        leader.waitForRecruitsUpkeepTime = leader.getResupplyTime();
        leader.setPatrolState(AbstractLeaderEntity.State.UPKEEP);
        leader.retreating = false;
        leader.setRetreatingMessageSent(false);
    }
}
