package com.talhanation.bannermod.entity.military;

import com.talhanation.bannermod.util.NPCArmy;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

final class LeaderCombatRuntime {
    private static final double ENEMY_SCAN_RADIUS = 100D;

    private final AbstractLeaderEntity leader;

    LeaderCombatRuntime(AbstractLeaderEntity leader) {
        this.leader = leader;
    }

    void checkForPotentialEnemies() {
        if (leader.level().isClientSide()) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) leader.level();
        List<LivingEntity> targets = leader.filterCombatCandidates(
                leader.scanNearbyCombatCandidates(serverLevel, ENEMY_SCAN_RADIUS).candidates(),
                target -> leader.shouldAttack(target) && leader.hasLineOfSight(target) && !target.isUnderWater(),
                false
        );

        if (targets.isEmpty()) {
            return;
        }

        leader.enemyArmy = new NPCArmy(serverLevel, targets, null);
        AbstractLeaderEntity.EnemyAction action = AbstractLeaderEntity.EnemyAction.fromIndex(leader.getEnemyAction());
        if (action == AbstractLeaderEntity.EnemyAction.KEEP_PATROLLING) {
            return;
        }

        if (leader.state != AbstractLeaderEntity.State.ATTACKING && leader.canAttackWhilePatrolling()) {
            if (action == AbstractLeaderEntity.EnemyAction.HOLD) {
                leader.getNavigation().stop();
            }
            leader.setPatrolState(AbstractLeaderEntity.State.ATTACKING);
        }
    }

    boolean tickAttacking() {
        if (leader.retreating && !leader.WAYPOINTS.isEmpty()) {
            leader.setPatrolState(AbstractLeaderEntity.State.RETREATING);
            return true;
        }

        if (leader.army == null || leader.enemyArmy == null) {
            leader.setFollowState(0);
            leader.setPatrolState(leader.prevState);
            return true;
        }

        leader.attackController.tick();
        return false;
    }

    void triggerRetreatIfLowHealth() {
        if (leader.getMaxHealth() * 0.25 > leader.getHealth() && leader.state != AbstractLeaderEntity.State.RETREATING) {
            leader.setPatrolState(AbstractLeaderEntity.State.RETREATING);
        }
    }
}
