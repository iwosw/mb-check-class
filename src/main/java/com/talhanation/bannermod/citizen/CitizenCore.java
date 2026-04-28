package com.talhanation.bannermod.citizen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Optional;
import java.util.UUID;

public interface CitizenCore {

    enum RuntimeFlag {
        SHOULD_FOLLOW,
        SHOULD_HOLD_POS,
        SHOULD_MOVE_POS,
        SHOULD_PROTECT,
        SHOULD_MOUNT,
        SHOULD_BLOCK,
        LISTEN,
        IS_FOLLOWING,
        SHOULD_REST,
        SHOULD_RANGED,
        IS_IN_FORMATION
    }

    @Nullable
    UUID getOwnerUUID();

    void setOwnerUUID(Optional<UUID> ownerUuid);

    int getFollowState();

    void setFollowState(int state);

    SimpleContainer getInventory();

    HolderLookup.Provider registryAccess();

    @Nullable
    String getTeamId();

    @Nullable
    Vec3 getHoldPos();

    void setHoldPos(@Nullable Vec3 holdPos);

    void clearHoldPos();

    @Nullable
    BlockPos getMovePos();

    void setMovePos(@Nullable BlockPos movePos);

    void clearMovePos();

    boolean isOwned();

    void setOwned(boolean owned);

    boolean isWorking();

    @Nullable
    UUID getBoundWorkAreaUUID();

    void setBoundWorkAreaUUID(@Nullable UUID boundWorkAreaUuid);

    boolean getRuntimeFlag(RuntimeFlag flag);

    void setRuntimeFlag(RuntimeFlag flag, boolean value);

    default CitizenStateSnapshot snapshot() {
        EnumMap<RuntimeFlag, Boolean> flags = new EnumMap<>(RuntimeFlag.class);
        for (RuntimeFlag flag : RuntimeFlag.values()) {
            flags.put(flag, this.getRuntimeFlag(flag));
        }
        return CitizenStateSnapshot.builder()
                .ownerUuid(this.getOwnerUUID())
                .teamId(this.getTeamId())
                .followState(this.getFollowState())
                .owned(this.isOwned())
                .working(this.isWorking())
                .boundWorkAreaUuid(this.getBoundWorkAreaUUID())
                .holdPos(this.getHoldPos())
                .movePos(this.getMovePos())
                .inventoryData(CitizenStateSnapshot.copyInventory(this.getInventory(), this.registryAccess()))
                .runtimeFlags(flags)
                .build();
    }

    default void apply(CitizenStateSnapshot snapshot) {
        this.setOwnerUUID(Optional.ofNullable(snapshot.ownerUuid()));
        this.setFollowState(snapshot.followState());
        this.setOwned(snapshot.owned());
        this.setBoundWorkAreaUUID(snapshot.boundWorkAreaUuid());
        if (snapshot.holdPos() != null) {
            this.setHoldPos(snapshot.holdPos());
        }
        else {
            this.clearHoldPos();
        }
        if (snapshot.movePos() != null) {
            this.setMovePos(snapshot.movePos());
        }
        else {
            this.clearMovePos();
        }
        for (RuntimeFlag flag : RuntimeFlag.values()) {
            this.setRuntimeFlag(flag, snapshot.runtimeFlag(flag));
        }
        CitizenStateSnapshot.restoreInventory(this.getInventory(), snapshot.inventoryData(), this.registryAccess());
    }
}
