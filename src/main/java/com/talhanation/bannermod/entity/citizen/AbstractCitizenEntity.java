package com.talhanation.bannermod.entity.citizen;

import com.talhanation.bannermod.citizen.CitizenCore;
import com.talhanation.bannermod.citizen.CitizenRole;
import com.talhanation.bannermod.citizen.CitizenRoleController;
import com.talhanation.bannermod.entity.military.AbstractInventoryEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

/**
 * Canonical base for any controllable citizen unit.
 *
 * <p>Concrete subclasses (legacy recruit/worker wrappers and future single citizen
 * runtime variants) provide concrete {@link CitizenCore} backing storage and role
 * identity. Shared role-controller plumbing and militia conversion policy live here.
 */
public abstract class AbstractCitizenEntity extends AbstractInventoryEntity {

    /**
     * Player-assigned anchor pos ("home"). When set, AI goals (HOMEASSIGN-003) treat
     * it as the canonical sleep/idle target at night or on low stamina. {@code null}
     * means the staffing/AI pipeline can pick its own anchor.
     */
    private static final EntityDataAccessor<Optional<BlockPos>> DATA_HOME_POS =
            SynchedEntityData.defineId(AbstractCitizenEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    /**
     * Optional UUID of the BuildArea (HousePrefab) that backs the home anchor.
     * Used to invalidate {@link #getHomePos()} if the prefab is destroyed.
     */
    private static final EntityDataAccessor<Optional<UUID>> DATA_HOME_BUILD_AREA_UUID =
            SynchedEntityData.defineId(AbstractCitizenEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private CitizenRoleController citizenRoleController = CitizenRoleController.noop(CitizenRole.CIVILIAN_RESIDENT);

    protected AbstractCitizenEntity(EntityType<? extends AbstractInventoryEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_HOME_POS, Optional.empty());
        builder.define(DATA_HOME_BUILD_AREA_UUID, Optional.empty());
    }

    public abstract CitizenCore getCitizenCore();

    public abstract CitizenRole getCitizenRole();

    public CitizenRoleController getCitizenRoleController() {
        return this.citizenRoleController;
    }

    public void setCitizenRoleController(CitizenRoleController controller) {
        this.citizenRoleController = controller == null
                ? CitizenRoleController.noop(this.getCitizenRole())
                : controller;
    }

    public boolean isMilitiaRole() {
        return this.getCitizenRole() == CitizenRole.MILITIA;
    }

    /**
     * Player-assigned home anchor block (bed, sleeping zone, or HousePrefab origin).
     * Subclasses may override to alias an existing concept (recruits alias upkeepPos).
     */
    @Nullable
    public BlockPos getHomePos() {
        return this.entityData.get(DATA_HOME_POS).orElse(null);
    }

    public void setHomePos(@Nullable BlockPos pos) {
        this.entityData.set(DATA_HOME_POS, Optional.ofNullable(pos));
    }

    public void clearHomePos() {
        this.entityData.set(DATA_HOME_POS, Optional.empty());
        this.entityData.set(DATA_HOME_BUILD_AREA_UUID, Optional.empty());
    }

    @Nullable
    public UUID getHomeBuildAreaUUID() {
        return this.entityData.get(DATA_HOME_BUILD_AREA_UUID).orElse(null);
    }

    public void setHomeBuildAreaUUID(@Nullable UUID uuid) {
        this.entityData.set(DATA_HOME_BUILD_AREA_UUID, Optional.ofNullable(uuid));
    }

    public boolean hasHomeAssigned() {
        return getHomePos() != null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        BlockPos home = getHomePos();
        if (home != null) {
            nbt.putInt("HomePosX", home.getX());
            nbt.putInt("HomePosY", home.getY());
            nbt.putInt("HomePosZ", home.getZ());
        }
        UUID buildArea = getHomeBuildAreaUUID();
        if (buildArea != null) {
            nbt.putUUID("HomeBuildAreaUUID", buildArea);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("HomePosX") && nbt.contains("HomePosY") && nbt.contains("HomePosZ")) {
            setHomePos(new BlockPos(nbt.getInt("HomePosX"), nbt.getInt("HomePosY"), nbt.getInt("HomePosZ")));
        }
        if (nbt.hasUUID("HomeBuildAreaUUID")) {
            setHomeBuildAreaUUID(nbt.getUUID("HomeBuildAreaUUID"));
        }
    }
}
