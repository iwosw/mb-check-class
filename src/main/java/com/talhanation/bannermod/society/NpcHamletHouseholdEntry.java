package com.talhanation.bannermod.society;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.UUID;

public record NpcHamletHouseholdEntry(
        UUID householdId,
        BlockPos plotPos,
        @Nullable UUID homeBuildingUuid
) {
    public NpcHamletHouseholdEntry {
        if (householdId == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        if (plotPos == null) {
            throw new IllegalArgumentException("plotPos must not be null");
        }
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("HouseholdId", this.householdId);
        tag.putLong("PlotPos", this.plotPos.asLong());
        if (this.homeBuildingUuid != null) {
            tag.putUUID("HomeBuildingUuid", this.homeBuildingUuid);
        }
        return tag;
    }

    public static NpcHamletHouseholdEntry fromTag(CompoundTag tag) {
        return new NpcHamletHouseholdEntry(
                tag.getUUID("HouseholdId"),
                BlockPos.of(tag.getLong("PlotPos")),
                tag.contains("HomeBuildingUuid") ? tag.getUUID("HomeBuildingUuid") : null
        );
    }
}
