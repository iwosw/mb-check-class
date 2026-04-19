package com.talhanation.bannermod.entity.military;

import com.talhanation.bannermod.util.FormationUtils;
import com.talhanation.bannermod.util.RecruitCommanderUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.List;

final class PatrolRouteRuntime {
    private final AbstractLeaderEntity leader;

    PatrolRouteRuntime(AbstractLeaderEntity leader) {
        this.leader = leader;
    }

    void loadRouteWaypointsFromData(List<BlockPos> positions, List<Integer> waitSecs) {
        leader.WAYPOINTS.clear();
        leader.WAYPOINT_ITEMS.clear();
        leader.WAYPOINT_WAIT_SECONDS.clear();

        if (positions == null || positions.isEmpty()) return;

        for (int i = 0; i < positions.size(); i++) {
            BlockPos pos = resolveServerY(positions.get(i));
            leader.WAYPOINTS.push(pos);
            leader.WAYPOINT_ITEMS.push(leader.getItemStackToRender(pos));
            leader.WAYPOINT_WAIT_SECONDS.add(waitSecs != null && i < waitSecs.size() ? waitSecs.get(i) : 0);
        }

        int nearestIndex = 0;
        double nearestDist = Double.MAX_VALUE;
        for (int i = 0; i < leader.WAYPOINTS.size(); i++) {
            double distance = leader.distanceToSqr(leader.WAYPOINTS.get(i).getX(), 0, leader.WAYPOINTS.get(i).getZ());
            if (distance < nearestDist) {
                nearestDist = distance;
                nearestIndex = i;
            }
        }

        leader.setWaypointIndex(nearestIndex);
        leader.returning = false;
        leader.waitingTime = 0;
        leader.currentWaypoint = leader.WAYPOINTS.get(nearestIndex);
    }

    void tickPatrolling(double distance) {
        if (leader.currentWaypoint != null) {
            if (distance <= leader.getDistanceToReachWaypoint()) {
                if (leader.getLeaderUpkeepRuntime().shouldStartResupply()) {
                    leader.getLeaderUpkeepRuntime().startResupply();
                } else {
                    leader.waitingTime = 0;
                    leader.setPatrolState(AbstractLeaderEntity.State.WAITING);
                }
            } else {
                moveToCurrentWaypoint();
            }
        } else if (hasIndex()) {
            leader.currentWaypoint = leader.WAYPOINTS.get(leader.getWaypointIndex());
        } else {
            leader.setPatrolState(AbstractLeaderEntity.State.IDLE);
        }

        if (leader.enemyArmy != null && !leader.retreating) {
            if (leader.enemyArmySpotted()) {
                leader.setPatrolState(AbstractLeaderEntity.State.ATTACKING);
            } else {
                leader.setTarget(null);
            }
        }
    }

    void tickWaiting(double distance) {
        if (timerElapsed() && hasIndex()) {
            updateWaypointIndex();
            if (hasIndex()) {
                leader.currentWaypoint = leader.WAYPOINTS.get(leader.getWaypointIndex());
            }
            leader.setPatrolState(AbstractLeaderEntity.State.PATROLLING);
        }

        if (distance > 25D && leader.enemyArmy == null) {
            moveToCurrentWaypoint();
        }

        if (leader.enemyArmy != null && leader.enemyArmy.size() > 0) {
            if (leader.enemyArmySpotted()) {
                leader.attackController.setInitPos(leader.enemyArmy.getPosition());
                leader.setPatrolState(AbstractLeaderEntity.State.ATTACKING);
            } else {
                leader.setTarget(null);
            }
        }
    }

    boolean redirectAttackToRetreat() {
        if (leader.retreating && !leader.WAYPOINTS.isEmpty()) {
            leader.setPatrolState(AbstractLeaderEntity.State.RETREATING);
            return true;
        }
        return false;
    }

    void tickRetreating() {
        if (leader.getOwner() != null && !leader.hasSentRetreatingMessage()) {
            leader.getOwner().sendSystemMessage(leader.RETREATING());
            leader.setRetreatingMessageSent(true);
        }
        leader.retreating = true;
        if (leader.army != null) {
            RecruitCommanderUtil.setRecruitsClearTargets(leader.army.getAllRecruitUnits());
            RecruitCommanderUtil.setRecruitsFollow(leader.army.getAllRecruitUnits(), leader.getUUID());
            RecruitCommanderUtil.setRecruitsShields(leader.army.getAllRecruitUnits(), false);
        }
    }

    void moveToCurrentWaypoint() {
        if (leader.tickCount % 20 == 0) {
            leader.getNavigation().moveTo(leader.currentWaypoint.getX(), leader.currentWaypoint.getY(), leader.currentWaypoint.getZ(),
                    AbstractLeaderEntity.PatrolSpeed.fromIndex(leader.getPatrolSpeed()).toSpeed());
            Vec3 forward = leader.position().vectorTo(leader.currentWaypoint.getCenter());

            if (leader.army != null) {
                FormationUtils.lineFormation(forward.normalize(), leader.army.getAllRecruitUnits(), leader.position(), 4, 1.75);
                RecruitCommanderUtil.setRecruitsPatrolMoveSpeed(leader.army.getAllRecruitUnits(), 0.7F, 60);
            }
        }

        if (leader.horizontalCollision || leader.minorHorizontalCollision) {
            leader.getJumpControl().jump();
        }
    }

    void updateWaypointIndex() {
        int currentIndex = leader.getWaypointIndex();
        boolean isCycling = leader.getCycle();
        boolean isLastWaypoint = currentIndex == leader.WAYPOINTS.size() - 1;
        boolean isFirstWaypoint = currentIndex == 0;
        if (isCycling && !leader.retreating) {
            if (isLastWaypoint) {
                leader.setWaypointIndex(0);
            } else {
                increaseIndex();
            }
        } else if (leader.returning || leader.retreating) {
            if (isFirstWaypoint) {
                leader.returning = false;
                leader.retreating = false;
            } else {
                decreaseIndex();
            }
        } else if (isLastWaypoint) {
            leader.returning = true;
        } else {
            increaseIndex();
        }
    }

    private BlockPos resolveServerY(BlockPos pos) {
        int surfaceY = leader.getCommandSenderWorld().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,
                pos.getX(), pos.getZ()) - 1;
        int y = Math.max(surfaceY, leader.getCommandSenderWorld().getMinBuildHeight());
        return new BlockPos(pos.getX(), y, pos.getZ());
    }

    private boolean hasIndex() {
        return !leader.WAYPOINTS.isEmpty() && leader.WAYPOINTS.size() > leader.getWaypointIndex();
    }

    private boolean timerElapsed() {
        int currentIdx = leader.getWaypointIndex();
        int waitSec;
        if (!leader.WAYPOINT_WAIT_SECONDS.isEmpty() && currentIdx < leader.WAYPOINT_WAIT_SECONDS.size()) {
            waitSec = leader.WAYPOINT_WAIT_SECONDS.get(currentIdx);
        } else {
            waitSec = leader.getWaitTimeInMin() * 60;
        }
        return ++leader.waitingTime > waitSec * 20;
    }

    private void decreaseIndex() {
        int nextIndex = leader.getWaypointIndex() - 1;
        if (nextIndex >= 0) {
            leader.setWaypointIndex(nextIndex);
        }
    }

    private void increaseIndex() {
        int nextIndex = leader.getWaypointIndex() + 1;
        if (nextIndex < leader.WAYPOINTS.size()) {
            leader.setWaypointIndex(nextIndex);
        }
    }
}
