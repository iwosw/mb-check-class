package com.talhanation.bannermod.util;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class RecruitCommanderUtil {

    private static void forEachRecruit(List<AbstractRecruitEntity> recruits, Consumer<AbstractRecruitEntity> action) {
        if (recruits == null || recruits.isEmpty()) return;
        for (AbstractRecruitEntity recruit : recruits) {
            if (recruit != null && recruit.isAlive()) {
                action.accept(recruit);
            }
        }
    }

    private static void forEachTypedRecruit(List<AbstractRecruitEntity> recruits, EntityType<?> type, Consumer<AbstractRecruitEntity> action) {
        forEachRecruit(recruits, recruit -> {
            if (recruit.getType().equals(type)) {
                action.accept(recruit);
            }
        });
    }

    /**
     * Sets recruits to follow the leader in a "protect/follow" state.
     *
     * @param recruits    The list of recruits to command.
     * @param leaderUUID  The UUID of the leader to follow.
     */
    public static void setRecruitsFollow(List<AbstractRecruitEntity> recruits, @Nullable UUID leaderUUID) {
        forEachRecruit(recruits, recruit -> {
            if (leaderUUID == null){
                recruit.setProtectUUID(Optional.empty());
            }
            else{
                recruit.setProtectUUID(Optional.of(leaderUUID));
                recruit.setFollowState(5); // Protect/Follow
            }
        });
    }

    /**
     * Sets recruits to hold their current position.
     *
     * @param recruits The list of recruits to command.
     */
    public static void setRecruitsHoldPos(List<AbstractRecruitEntity> recruits) {
        forEachRecruit(recruits, recruit -> recruit.setFollowState(2)); // Hold Position
    }

    /**
     * Sets recruits to move to a specific position.
     *
     * @param recruits The list of recruits to command.
     * @param pos      The target position to move to.
     */
    public static void setRecruitsMove(List<AbstractRecruitEntity> recruits, BlockPos pos) {
        forEachRecruit(recruits, recruit -> {
            recruit.setMovePos(pos);
            recruit.setFollowState(0); // Needs to be above setShouldMovePos
            recruit.setShouldMovePos(true);
        });
    }

    /**
     * Sets recruits movement speed.
     *
     * @param recruits The list of recruits to command.
     * @param speed    The speed.
     */
    public static void setRecruitsMoveSpeed(List<AbstractRecruitEntity> recruits, float speed) {
        forEachRecruit(recruits, recruit -> recruit.moveSpeed = speed);
    }
    /**
     * Sets recruits movement speed while patrolling.
     *
     * @param recruits The list of recruits to command.
     * @param speed    The speed for recruits within range.
     * @param range    The max distance from hold position for the speed to apply.
     */
    public static void setRecruitsPatrolMoveSpeed(List<AbstractRecruitEntity> recruits, float speed, float range) {
        forEachRecruit(recruits, recruit -> {
            double distance = recruit.distanceToSqr(recruit.getHoldPos()); // Quadratische Distanzberechnung
            if (distance <= range) {
                recruit.moveSpeed = speed;
            } else {
                recruit.moveSpeed = 1.0F;
            }
        });
    }
    /**
     * Clears the targets of all recruits.
     *
     * @param recruits The list of recruits to command.
     */
    public static void setRecruitsClearTargets(List<AbstractRecruitEntity> recruits) {
        forEachRecruit(recruits, recruit -> recruit.setTarget(null));
    }

    /**
     * Sets recruits to wander freely.
     *
     * @param recruits The list of recruits to command.
     */
    public static void setRecruitsWanderFreely(List<AbstractRecruitEntity> recruits) {
        forEachRecruit(recruits, recruit -> {
            recruit.clearHoldPos();
            recruit.setFollowState(0); // Freely
        });
    }

    /**
     * Sets recruits to use shields (blocking).
     *
     * @param recruits The list of recruits to command.
     * @param shields  Whether to use shields.
     */
    public static void setRecruitsShields(List<AbstractRecruitEntity> recruits, boolean shields) {
        forEachRecruit(recruits, recruit -> {
            recruit.clearHoldPos();
            recruit.setShouldBlock(shields);
        });
    }

    /**
     * Sets recruits to move and hold a specific position in formation.
     *
     * @param recruits The list of recruits to command.
     * @param target   The target position for the formation.
     * @param linePos  The line position for the formation.
     */
    public static void setRecruitsMoveAndHold(List<AbstractRecruitEntity> recruits, Vec3 target, Vec3 linePos) {
        forEachRecruit(recruits, recruit -> {
            recruit.reachedMovePos = false;
            Vec3 pos = FormationUtils.calculateLineBlockPosition(target, linePos, recruits.size(), recruits.indexOf(recruit), recruit.getCommandSenderWorld());
            recruit.setFollowState(0); // Needs to be above setShouldMovePos
            recruit.setShouldMovePos(true);
        });
    }

    /**
     * Sets recruits to perform upkeep (resupply).
     *
     * @param recruits The list of recruits to command.
     * @param upkeepPos The position for upkeep.
     * @param upkeepUUID The UUID of the upkeep entity.
     */
    public static void setRecruitsUpkeep(List<AbstractRecruitEntity> recruits, BlockPos upkeepPos, UUID upkeepUUID) {
        forEachRecruit(recruits, recruit -> {
            recruit.clearUpkeepEntity();
            recruit.clearUpkeepPos();
            recruit.setUpkeepPos(upkeepPos);
            recruit.setUpkeepUUID(Optional.ofNullable(upkeepUUID));
            recruit.setUpkeepTimer(0);
            recruit.setTarget(null);
            recruit.forcedUpkeep = true;
        });
    }

    /**
     * Clears the upkeep state for all recruits.
     *
     * @param recruits The list of recruits to command.
     */
    public static void clearRecruitsUpkeep(List<AbstractRecruitEntity> recruits) {
        forEachRecruit(recruits, recruit -> {
            recruit.clearUpkeepEntity();
            recruit.clearUpkeepPos();
            recruit.setUpkeepTimer(0);
        });
    }

    /**
     * Sets recruits to mount a specific entity.
     *
     * @param recruits The list of recruits to command.
     * @param mountUUID The UUID of the entity to mount.
     */
    public static void setRecruitsMount(List<AbstractRecruitEntity> recruits, UUID mountUUID) {
        forEachRecruit(recruits, recruit -> {
            recruit.shouldMount(true, mountUUID);
            recruit.dismount = 0;
        });
    }

    /**
     * Sets recruits to dismount.
     *
     * @param recruits The list of recruits to command.
     */
    public static void setRecruitsDismount(List<AbstractRecruitEntity> recruits) {
        forEachRecruit(recruits, recruit -> {
            recruit.shouldMount(false, null);
            if (recruit.isPassenger()) {
                recruit.stopRiding();
                recruit.dismount = 180;
            }
        });
    }

    /**
     * Sets the aggression state for all recruits.
     *
     * @param recruits The list of recruits to command.
     * @param state    The aggression state to set.
     */
    public static void setRecruitsAggroState(List<AbstractRecruitEntity> recruits, int state) {
        forEachRecruit(recruits, recruit -> recruit.setAggroState(state));
    }

    /**
     * Sets recruits of a specific type to follow the leader.
     *
     * @param recruits The list of recruits to command.
     * @param type     The entity type of the recruits to command.
     */
    public static void setTypedRecruitsFollow(List<AbstractRecruitEntity> recruits, EntityType<?> type) {
        forEachTypedRecruit(recruits, type, recruit -> recruit.setFollowState(5)); // Follow/Protect
    }

    /**
     * Sets recruits of a specific type to hold their position.
     *
     * @param recruits The list of recruits to command.
     * @param type     The entity type of the recruits to command.
     */
    public static void setTypedRecruitsHoldPos(List<AbstractRecruitEntity> recruits, EntityType<?> type) {
        forEachTypedRecruit(recruits, type, recruit -> recruit.setFollowState(2)); // Hold Position
    }

    /**
     * Sets recruits of a specific type to move to a specific position.
     *
     * @param recruits The list of recruits to command.
     * @param pos      The target position to move to.
     * @param type     The entity type of the recruits to command.
     */
    public static void setTypedRecruitsMove(List<AbstractRecruitEntity> recruits, BlockPos pos, EntityType<?> type) {
        forEachTypedRecruit(recruits, type, recruit -> {
            recruit.setMovePos(pos);
            recruit.setFollowState(0); // Needs to be above setShouldMovePos
            recruit.setShouldMovePos(true);
        });
    }

    /**
     * Sets recruits of a specific type to move and hold a specific position in formation.
     *
     * @param recruits The list of recruits to command.
     * @param target   The target position for the formation.
     * @param linePos  The line position for the formation.
     * @param type     The entity type of the recruits to command.
     */
    public static void setTypedRecruitsMoveAndHold(List<AbstractRecruitEntity> recruits, Vec3 target, Vec3 linePos, EntityType<?> type) {
        forEachTypedRecruit(recruits, type, recruit -> {
            recruit.reachedMovePos = false;
            recruit.setFollowState(0); // Needs to be above setShouldMovePos
            recruit.setShouldMovePos(true);
        });
    }

    /**
     * Sets recruits to listen the player owner.
     *
     * @param recruits The list of recruits to command.
     * @param listen   The boolean value.
     */
    public static void setRecruitsListen(List<AbstractRecruitEntity> recruits, boolean listen) {
        forEachRecruit(recruits, recruit -> recruit.setListen(listen)); // Follow/Protect
    }


    /**
     * Sets recruits to listen the player owner.
     *
     * @param recruits The list of recruits to command.
     * @param direction  The direction value.
     */
    public static void setRecruitsMoveToDirection(List<AbstractRecruitEntity> recruits, Vec3 direction){
        forEachRecruit(recruits, recruit -> {
            Vec3 pos = recruit.position().add(direction.scale(20));
            BlockPos blockPos = FormationUtils.getPositionOrSurface(
                    recruit.getCommandSenderWorld(),
                    new BlockPos((int) pos.x, (int) pos.y, (int) pos.z)
            );

            Vec3 targetPos = new Vec3(pos.x, blockPos.getY(), pos.z);

            recruit.setHoldPos(targetPos);
            recruit.setFollowState(3);
        });
    }

}

