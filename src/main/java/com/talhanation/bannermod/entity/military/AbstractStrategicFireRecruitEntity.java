package com.talhanation.bannermod.entity.military;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class AbstractStrategicFireRecruitEntity extends AbstractRecruitEntity implements IStrategicFire {
    private static final EntityDataAccessor<Optional<BlockPos>> STRATEGIC_FIRE_POS = SynchedEntityData.defineId(AbstractStrategicFireRecruitEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Boolean> SHOULD_STRATEGIC_FIRE = SynchedEntityData.defineId(AbstractStrategicFireRecruitEntity.class, EntityDataSerializers.BOOLEAN);

    protected AbstractStrategicFireRecruitEntity(EntityType<? extends AbstractRecruitEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STRATEGIC_FIRE_POS, Optional.empty());
        builder.define(SHOULD_STRATEGIC_FIRE, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        BlockPos strategicFirePos = this.getStrategicFirePos();
        if (strategicFirePos != null) {
            nbt.putInt("StrategicFirePosX", strategicFirePos.getX());
            nbt.putInt("StrategicFirePosY", strategicFirePos.getY());
            nbt.putInt("StrategicFirePosZ", strategicFirePos.getZ());
            nbt.putBoolean("ShouldStrategicFire", this.getShouldStrategicFire());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("StrategicFirePosX") && nbt.contains("StrategicFirePosY") && nbt.contains("StrategicFirePosZ")) {
            this.setStrategicFirePos(new BlockPos(
                    nbt.getInt("StrategicFirePosX"),
                    nbt.getInt("StrategicFirePosY"),
                    nbt.getInt("StrategicFirePosZ")
            ));
            this.setShouldStrategicFire(nbt.getBoolean("ShouldStrategicFire"));
        }
    }

    @Override
    public void setStrategicFirePos(BlockPos pos) {
        this.entityData.set(STRATEGIC_FIRE_POS, Optional.ofNullable(pos));
    }

    @Nullable
    public BlockPos getStrategicFirePos() {
        return this.entityData.get(STRATEGIC_FIRE_POS).orElse(null);
    }

    @Nullable
    public BlockPos StrategicFirePos() {
        return getStrategicFirePos();
    }

    public void clearArrowsPos() {
        this.entityData.set(STRATEGIC_FIRE_POS, Optional.empty());
    }

    @Override
    public void setShouldStrategicFire(boolean should) {
        this.entityData.set(SHOULD_STRATEGIC_FIRE, should);
    }

    public boolean getShouldStrategicFire() {
        return this.entityData.get(SHOULD_STRATEGIC_FIRE);
    }
}
