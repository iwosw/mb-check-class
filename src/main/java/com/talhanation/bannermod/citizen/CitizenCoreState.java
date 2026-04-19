package com.talhanation.bannermod.citizen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Concrete {@link CitizenCore} implementation that backs
 * {@link com.talhanation.bannermod.entity.citizen.CitizenEntity} and
 * is used as the test-facing fake in unit tests. Holds the same
 * state surface described by {@link CitizenStateSnapshot} — owner,
 * inventory, hold/move/follow/work-area flags, and the full runtime
 * flag bitmap.
 */
public final class CitizenCoreState implements CitizenCore {

    @Nullable
    private UUID ownerUuid;
    @Nullable
    private String teamId;
    private int followState;
    private boolean owned;
    private boolean working;
    @Nullable
    private UUID boundWorkAreaUuid;
    @Nullable
    private Vec3 holdPos;
    @Nullable
    private BlockPos movePos;
    private final SimpleContainer inventory;
    private final EnumMap<RuntimeFlag, Boolean> runtimeFlags = new EnumMap<>(RuntimeFlag.class);

    public CitizenCoreState(int inventorySize) {
        if (inventorySize <= 0) {
            throw new IllegalArgumentException("inventorySize must be positive");
        }
        this.inventory = new SimpleContainer(inventorySize);
        for (RuntimeFlag flag : RuntimeFlag.values()) {
            this.runtimeFlags.put(flag, Boolean.FALSE);
        }
    }

    public CitizenCoreState() {
        this(27);
    }

    public void setTeamId(@Nullable String teamId) {
        this.teamId = teamId;
    }

    public void setWorking(boolean working) {
        this.working = working;
    }

    @Override
    @Nullable
    public UUID getOwnerUUID() {
        return this.ownerUuid;
    }

    @Override
    public void setOwnerUUID(Optional<UUID> ownerUuid) {
        Objects.requireNonNull(ownerUuid, "ownerUuid");
        this.ownerUuid = ownerUuid.orElse(null);
    }

    @Override
    public int getFollowState() {
        return this.followState;
    }

    @Override
    public void setFollowState(int state) {
        this.followState = state;
    }

    @Override
    public SimpleContainer getInventory() {
        return this.inventory;
    }

    @Override
    @Nullable
    public String getTeamId() {
        return this.teamId;
    }

    @Override
    @Nullable
    public Vec3 getHoldPos() {
        return this.holdPos;
    }

    @Override
    public void setHoldPos(@Nullable Vec3 holdPos) {
        this.holdPos = holdPos;
    }

    @Override
    public void clearHoldPos() {
        this.holdPos = null;
    }

    @Override
    @Nullable
    public BlockPos getMovePos() {
        return this.movePos;
    }

    @Override
    public void setMovePos(@Nullable BlockPos movePos) {
        this.movePos = movePos;
    }

    @Override
    public void clearMovePos() {
        this.movePos = null;
    }

    @Override
    public boolean isOwned() {
        return this.owned;
    }

    @Override
    public void setOwned(boolean owned) {
        this.owned = owned;
    }

    @Override
    public boolean isWorking() {
        return this.working;
    }

    @Override
    @Nullable
    public UUID getBoundWorkAreaUUID() {
        return this.boundWorkAreaUuid;
    }

    @Override
    public void setBoundWorkAreaUUID(@Nullable UUID boundWorkAreaUuid) {
        this.boundWorkAreaUuid = boundWorkAreaUuid;
    }

    @Override
    public boolean getRuntimeFlag(RuntimeFlag flag) {
        Boolean value = this.runtimeFlags.get(flag);
        return value != null && value;
    }

    @Override
    public void setRuntimeFlag(RuntimeFlag flag, boolean value) {
        this.runtimeFlags.put(flag, value);
    }
}
