package com.talhanation.bannermod.entity.military;

import com.talhanation.bannermod.citizen.CitizenCore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

final class RecruitCitizenBridge {
    private RecruitCitizenBridge() {
    }

    static CitizenCore createCore(AbstractRecruitEntity recruit) {
        return new CitizenCore() {
            @Override
            public UUID getOwnerUUID() {
                return recruit.getOwnerUUID();
            }

            @Override
            public void setOwnerUUID(Optional<UUID> ownerUuid) {
                recruit.setOwnerUUID(ownerUuid);
            }

            @Override
            public int getFollowState() {
                return recruit.getFollowState();
            }

            @Override
            public void setFollowState(int state) {
                recruit.setFollowState(state);
            }

            @Override
            public SimpleContainer getInventory() {
                return recruit.getInventory();
            }

            @Override
            public HolderLookup.Provider registryAccess() {
                return recruit.registryAccess();
            }

            @Override
            public String getTeamId() {
                return recruit.getTeam() == null ? null : recruit.getTeam().getName();
            }

            @Override
            public Vec3 getHoldPos() {
                return recruit.getHoldPos();
            }

            @Override
            public void setHoldPos(@Nullable Vec3 holdPos) {
                if (holdPos == null) {
                    recruit.clearHoldPos();
                }
                else {
                    recruit.setHoldPos(holdPos);
                }
            }

            @Override
            public void clearHoldPos() {
                recruit.clearHoldPos();
            }

            @Override
            public BlockPos getMovePos() {
                return recruit.getMovePos();
            }

            @Override
            public void setMovePos(@Nullable BlockPos movePos) {
                if (movePos == null) {
                    recruit.clearMovePos();
                }
                else {
                    recruit.setMovePos(movePos);
                }
            }

            @Override
            public void clearMovePos() {
                recruit.clearMovePos();
            }

            @Override
            public boolean isOwned() {
                return recruit.getIsOwned();
            }

            @Override
            public void setOwned(boolean owned) {
                recruit.setIsOwned(owned);
            }

            @Override
            public boolean isWorking() {
                return recruit.getFollowState() == 6;
            }

            @Override
            public UUID getBoundWorkAreaUUID() {
                return null;
            }

            @Override
            public void setBoundWorkAreaUUID(@Nullable UUID boundWorkAreaUuid) {
            }

            @Override
            public boolean getRuntimeFlag(RuntimeFlag flag) {
                return switch (flag) {
                    case SHOULD_FOLLOW -> recruit.getShouldFollow();
                    case SHOULD_HOLD_POS -> recruit.getShouldHoldPos();
                    case SHOULD_MOVE_POS -> recruit.getShouldMovePos();
                    case SHOULD_PROTECT -> recruit.getShouldProtect();
                    case SHOULD_MOUNT -> recruit.getShouldMount();
                    case SHOULD_BLOCK -> recruit.getShouldBlock();
                    case LISTEN -> recruit.getListen();
                    case IS_FOLLOWING -> recruit.isFollowing();
                    case SHOULD_REST -> recruit.getShouldRest();
                    case SHOULD_RANGED -> recruit.getShouldRanged();
                    case IS_IN_FORMATION -> recruit.isInFormation;
                };
            }

            @Override
            public void setRuntimeFlag(RuntimeFlag flag, boolean value) {
                switch (flag) {
                    case SHOULD_FOLLOW -> recruit.setShouldFollow(value);
                    case SHOULD_HOLD_POS -> recruit.setShouldHoldPos(value);
                    case SHOULD_MOVE_POS -> recruit.setShouldMovePos(value);
                    case SHOULD_PROTECT -> recruit.setShouldProtect(value);
                    case SHOULD_MOUNT -> recruit.setShouldMount(value);
                    case SHOULD_BLOCK -> recruit.setShouldBlock(value);
                    case LISTEN -> recruit.setListen(value);
                    case IS_FOLLOWING -> recruit.setIsFollowing(value);
                    case SHOULD_REST -> recruit.setShouldRest(value);
                    case SHOULD_RANGED -> recruit.setShouldRanged(value);
                    case IS_IN_FORMATION -> recruit.isInFormation = value;
                }
            }
        };
    }
}
