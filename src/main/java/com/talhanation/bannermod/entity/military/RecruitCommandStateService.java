package com.talhanation.bannermod.entity.military;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

final class RecruitCommandStateService {
    private RecruitCommandStateService() {
    }

    static void setAggroState(AbstractRecruitEntity recruit, int state) {
        switch (state) {
            case 0, 3 -> recruit.setTarget(null);
            case 2 -> recruit.setFollowState(0);
            default -> {
            }
        }
        recruit.writeAggroState(state);
    }

    static void setFollowState(AbstractRecruitEntity recruit, int state) {
        switch (state) {
            case 0, 6 -> {
                recruit.setShouldFollow(false);
                recruit.setShouldHoldPos(false);
                recruit.setShouldProtect(false);
                recruit.setShouldMovePos(false);
            }
            case 1 -> {
                recruit.setShouldFollow(true);
                recruit.setShouldHoldPos(false);
                recruit.setShouldProtect(false);
                recruit.setShouldMovePos(false);
            }
            case 2 -> {
                recruit.setShouldFollow(false);
                recruit.setShouldHoldPos(true);
                clearHoldPos(recruit);
                setHoldPos(recruit, recruit.position());
                recruit.setShouldProtect(false);
                recruit.setShouldMovePos(false);
            }
            case 3 -> {
                recruit.setShouldFollow(false);
                recruit.setShouldHoldPos(true);
                recruit.setShouldProtect(false);
                recruit.setShouldMovePos(false);
            }
            case 4 -> {
                recruit.setShouldFollow(false);
                recruit.setShouldHoldPos(true);
                clearHoldPos(recruit);
                setHoldPos(recruit, recruit.getOwner().position());
                recruit.setShouldProtect(false);
                recruit.setShouldMovePos(false);
                state = 3;
            }
            case 5 -> {
                recruit.setShouldFollow(false);
                recruit.setShouldHoldPos(false);
                recruit.setShouldProtect(true);
                recruit.setShouldMovePos(false);
            }
        }
        recruit.writeFollowState(state);
    }

    static void setHoldPos(AbstractRecruitEntity recruit, Vec3 holdPos) {
        recruit.holdPosVec = holdPos;
    }

    static void setMovePos(AbstractRecruitEntity recruit, BlockPos holdPos) {
        recruit.writeMovePos(Optional.of(holdPos));
        recruit.reachedMovePos = false;
    }

    static void clearHoldPos(AbstractRecruitEntity recruit) {
        recruit.holdPosVec = null;
        recruit.writeHoldPos(Optional.empty());
    }

    static void clearMovePos(AbstractRecruitEntity recruit) {
        recruit.writeMovePos(Optional.empty());
    }

    static void shouldMount(AbstractRecruitEntity recruit, boolean should, UUID mountUuid) {
        if (!recruit.isPassenger()) {
            recruit.setShouldMount(should);
            recruit.setMountUUID(mountUuid != null ? Optional.of(mountUuid) : Optional.empty());
        }
        if (should) recruit.dismount = 0;
    }

    static void shouldProtect(AbstractRecruitEntity recruit, boolean should, UUID protectUuid) {
        recruit.setShouldProtect(should);
        recruit.setProtectUUID(protectUuid != null ? Optional.of(protectUuid) : Optional.empty());
    }
}
