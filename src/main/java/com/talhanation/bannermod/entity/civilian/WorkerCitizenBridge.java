package com.talhanation.bannermod.entity.civilian;

import com.talhanation.bannermod.citizen.CitizenCore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

final class WorkerCitizenBridge {
    private WorkerCitizenBridge() {
    }

    static CitizenCore createCore(AbstractWorkerEntity worker) {
        return new CitizenCore() {
            @Override
            public UUID getOwnerUUID() {
                return worker.getOwnerUUID();
            }

            @Override
            public void setOwnerUUID(Optional<UUID> ownerUuid) {
                worker.setOwnerUUID(ownerUuid);
            }

            @Override
            public int getFollowState() {
                return worker.getFollowState();
            }

            @Override
            public void setFollowState(int state) {
                worker.setFollowState(state);
            }

            @Override
            public SimpleContainer getInventory() {
                return worker.getInventory();
            }

            @Override
            public HolderLookup.Provider registryAccess() {
                return worker.registryAccess();
            }

            @Override
            public String getTeamId() {
                return worker.getTeam() == null ? null : worker.getTeam().getName();
            }

            @Override
            public Vec3 getHoldPos() {
                return worker.getHoldPos();
            }

            @Override
            public void setHoldPos(@Nullable Vec3 holdPos) {
                if (holdPos == null) {
                    worker.clearHoldPos();
                }
                else {
                    worker.setHoldPos(holdPos);
                }
            }

            @Override
            public void clearHoldPos() {
                worker.clearHoldPos();
            }

            @Override
            public BlockPos getMovePos() {
                return worker.getMovePos();
            }

            @Override
            public void setMovePos(@Nullable BlockPos movePos) {
                if (movePos == null) {
                    worker.clearMovePos();
                }
                else {
                    worker.setMovePos(movePos);
                }
            }

            @Override
            public void clearMovePos() {
                worker.clearMovePos();
            }

            @Override
            public boolean isOwned() {
                return worker.getIsOwned();
            }

            @Override
            public void setOwned(boolean owned) {
                worker.setIsOwned(owned);
            }

            @Override
            public boolean isWorking() {
                return worker.isWorking();
            }

            @Override
            public UUID getBoundWorkAreaUUID() {
                return worker.controlAccess().getBoundWorkAreaUUID();
            }

            @Override
            public void setBoundWorkAreaUUID(@Nullable UUID boundWorkAreaUuid) {
                worker.controlAccess().setBoundWorkAreaBinding(boundWorkAreaUuid);
            }

            @Override
            public boolean getRuntimeFlag(RuntimeFlag flag) {
                return switch (flag) {
                    case SHOULD_FOLLOW -> worker.getShouldFollow();
                    case SHOULD_HOLD_POS -> worker.getShouldHoldPos();
                    case SHOULD_MOVE_POS -> worker.getShouldMovePos();
                    case SHOULD_PROTECT -> worker.getShouldProtect();
                    case SHOULD_MOUNT -> worker.getShouldMount();
                    case SHOULD_BLOCK -> worker.getShouldBlock();
                    case LISTEN -> worker.getListen();
                    case IS_FOLLOWING -> worker.isFollowing();
                    case SHOULD_REST -> worker.getShouldRest();
                    case SHOULD_RANGED -> worker.getShouldRanged();
                    case IS_IN_FORMATION -> worker.isInFormation;
                };
            }

            @Override
            public void setRuntimeFlag(RuntimeFlag flag, boolean value) {
                switch (flag) {
                    case SHOULD_FOLLOW -> worker.setShouldFollow(value);
                    case SHOULD_HOLD_POS -> worker.setShouldHoldPos(value);
                    case SHOULD_MOVE_POS -> worker.setShouldMovePos(value);
                    case SHOULD_PROTECT -> worker.setShouldProtect(value);
                    case SHOULD_MOUNT -> worker.setShouldMount(value);
                    case SHOULD_BLOCK -> worker.setShouldBlock(value);
                    case LISTEN -> worker.setListen(value);
                    case IS_FOLLOWING -> worker.setIsFollowing(value);
                    case SHOULD_REST -> worker.setShouldRest(value);
                    case SHOULD_RANGED -> worker.setShouldRanged(value);
                    case IS_IN_FORMATION -> worker.isInFormation = value;
                }
            }
        };
    }
}
