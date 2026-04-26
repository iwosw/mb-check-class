package com.talhanation.bannermod.war.runtime;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public record SiegeStandardRecord(
        UUID id,
        UUID warId,
        UUID sidePoliticalEntityId,
        BlockPos pos,
        int radius,
        long placedAtGameTime
) {
    public SiegeStandardRecord {
        radius = Math.max(8, radius);
    }

    public boolean contains(BlockPos point) {
        if (point == null || pos == null) {
            return false;
        }
        long dx = (long) point.getX() - pos.getX();
        long dy = (long) point.getY() - pos.getY();
        long dz = (long) point.getZ() - pos.getZ();
        long r = (long) radius;
        return dx * dx + dy * dy + dz * dz <= r * r;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
        tag.putUUID("WarId", warId);
        tag.putUUID("Side", sidePoliticalEntityId);
        tag.putInt("X", pos.getX());
        tag.putInt("Y", pos.getY());
        tag.putInt("Z", pos.getZ());
        tag.putInt("Radius", radius);
        tag.putLong("PlacedAtGameTime", placedAtGameTime);
        return tag;
    }

    public static SiegeStandardRecord fromTag(CompoundTag tag) {
        return new SiegeStandardRecord(
                tag.getUUID("Id"),
                tag.getUUID("WarId"),
                tag.getUUID("Side"),
                new BlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z")),
                tag.getInt("Radius"),
                tag.getLong("PlacedAtGameTime")
        );
    }
}
